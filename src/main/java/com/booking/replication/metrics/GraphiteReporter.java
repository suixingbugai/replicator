package com.booking.replication.metrics;

import com.booking.replication.configuration.MetricsReporterConfiguration;
import com.booking.replication.util.Duration;
import com.codahale.metrics.graphite.Graphite;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * This class provides a Graphite Reporter.
 */
public class GraphiteReporter extends MetricsReporter {

    /**
     * Start a metrics graphite reporter.
     */
    public GraphiteReporter(Duration frequency, MetricsReporterConfiguration reporterConfiguration) {
        this.frequency = frequency;

        String[] urlSplit = reporterConfiguration.url.split(":");
        String hostName = urlSplit[0];
        int port = 3002;
        if (urlSplit.length > 1) {
            port = Integer.parseInt(urlSplit[1]);
        }

        reporter = com.codahale.metrics.graphite.GraphiteReporter
                .forRegistry(com.booking.replication.Metrics.registry)
                .prefixedWith(reporterConfiguration.namespace)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.SECONDS)
                .build(new Graphite(new InetSocketAddress(hostName, port)));
    }



}
