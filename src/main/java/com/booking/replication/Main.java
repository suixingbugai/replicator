package com.booking.replication;

import com.booking.replication.binlog.event.BinlogEventParserProviderCode;
import com.booking.replication.coordinator.CoordinatorInterface;
import com.booking.replication.coordinator.FileCoordinator;
import com.booking.replication.coordinator.ZookeeperCoordinator;
import com.booking.replication.monitor.IReplicatorHealthTracker;
import com.booking.replication.monitor.ReplicatorHealthAssessment;
import com.booking.replication.monitor.ReplicatorHealthTrackerProxy;
import com.booking.replication.sql.QueryInspector;
import com.booking.replication.util.Cmd;
import com.booking.replication.util.StartupParameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import joptsimple.OptionSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.codahale.metrics.MetricRegistry.name;
import static spark.Spark.get;
import static spark.Spark.port;

public class Main {

    private static int BINLOG_PARSER_PROVIDER_CODE;

    /**
     * Main.
     */
    public static void main(String[] args) throws Exception {
        OptionSet optionSet = Cmd.parseArgs(args);

        StartupParameters startupParameters = new StartupParameters(optionSet);

        BINLOG_PARSER_PROVIDER_CODE = startupParameters.getParser();

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        String  configPath = startupParameters.getConfigPath();

        final Configuration configuration;

        try {
            InputStream in = Files.newInputStream(Paths.get(configPath));
            configuration = mapper.readValue(in, Configuration.class);

            if (configuration == null) {
                throw new RuntimeException(String.format("Unable to load configuration from file: %s", configPath));
            }

            configuration.loadStartupParameters(startupParameters);
            configuration.validate();

            try {
                System.out.println("loaded configuration: \n" + configuration.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }

            QueryInspector.setIsPseudoGTIDPattern(configuration.getpGTIDPattern());

            CoordinatorInterface coordinator;
            switch (configuration.getMetadataStoreType()) {
                case Configuration.METADATASTORE_ZOOKEEPER:
                    coordinator = new ZookeeperCoordinator(configuration);
                    break;
                case Configuration.METADATASTORE_FILE:
                    coordinator = new FileCoordinator(configuration);
                    break;
                default:
                    throw new RuntimeException(String.format(
                            "Metadata store type not implemented: %s",
                            configuration.getMetadataStoreType()));
            }

            Coordinator.setImplementation(coordinator);

            ReplicatorHealthTrackerProxy healthTracker = new ReplicatorHealthTrackerProxy();

            if (configuration.getHealthTrackerPort() > 0) {
                startServerForHealthInquiries(configuration.getHealthTrackerPort(), healthTracker);
            }

            Coordinator.onLeaderElection(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Metrics.startReporters(configuration);
                            new Replicator(
                                    configuration,
                                    healthTracker,
                                    Metrics.registry.counter(name("events", "applierEventsObserved")),
                                    BINLOG_PARSER_PROVIDER_CODE
                            ).start();
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.exit(1);
                        }
                    }
                }
            );

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void startServerForHealthInquiries(int port, IReplicatorHealthTracker healthTracker) {
        port(port);

        get("/is_healthy",
                (req, response) ->
                {
                    try
                    {
                        ReplicatorHealthAssessment healthAssessment = healthTracker.getLastHealthAssessment();

                        if (healthAssessment.isOk())
                        {
                            //For Marathon any HTTP code between 200 and 399 indicates we're healthy

                            response.status(200);
                            // don't really need the response body
                            return "";
                        }
                        else
                        {
                            response.status(503);
                            return healthAssessment.getDiagnosis();
                        }
                    }
                    catch (Exception e)
                    {
                        response.status(503);

                        String errorMessage = "Failed to assess the health status of the Replicator";

                        LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME).warn(errorMessage, e);

                        return errorMessage;
                    }
                });
    }
}
