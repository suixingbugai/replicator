package com.booking.replication.augmenter;

import com.booking.replication.augmenter.active.schema.ActiveSchemaVersion;
import com.booking.replication.augmenter.exception.TableMapException;
import com.booking.replication.augmenter.model.AugmentedEvent;
import com.booking.replication.augmenter.model.AugmentedEventData;
import com.booking.replication.augmenter.model.AugmentedEventHeader;
import com.booking.replication.augmenter.model.AugmentedEventImplementation;
import com.booking.replication.supplier.model.RawEvent;
import com.booking.replication.augmenter.transaction.TransactionEventData;
import com.booking.replication.supplier.model.handler.JSONInvocationHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URISyntaxException;
import java.sql.SQLException;


public class EventAugmenter implements Augmenter {

    public final static String UUID_FIELD_NAME = "_replicator_uuid";
    public final static String XID_FIELD_NAME = "_replicator_xid";

    private ActiveSchemaVersion activeSchemaVersion;
    private final boolean       applyUuid;
    private final boolean       applyXid;

    private static final Logger LOGGER = LogManager.getLogger(EventAugmenter.class);

    public EventAugmenter(
            ActiveSchemaVersion asv,
            boolean             applyUuid,
            boolean             applyXid

    ) throws
            SQLException,
            URISyntaxException {

        activeSchemaVersion = asv;
        this.applyUuid      = applyUuid;
        this.applyXid       = applyXid;
    }


    public AugmentedEvent mapDataEventToSchema(
            RawEvent             abstractRowRawEvent,
            TransactionEventData currentTransaction
        ) throws Exception {

        AugmentedEvent au = new AugmentedEventImplementation(
                AugmentedEventHeader.getProxy(new JSONInvocationHandler(String.format(
                        "{\"timestamp\": %s, \"eventType\": \"PSEUDO_GTID\", \"tableName\": \"TABLE\"}",
                        System.currentTimeMillis()
                ).getBytes())),
                null
        );

        switch (abstractRowRawEvent.getHeader().getRawEventType()) {

            // TODO: IMPLEMENT
            case UPDATE_ROWS:
                break;

            case WRITE_ROWS:
                break;

            case DELETE_ROWS:
                break;

//            default:
//                throw new TableMapException("RBR event type expected! Received type: " +
//                        abstractRowRawEvent.getHeader().getRawEventType().toString(), abstractRowRawEvent
//                );
        }

        if (au == null) {
            throw new TableMapException(
                    "Augmented event ended up as null - something went wrong!",
                    abstractRowRawEvent
            );
        }

        return au;
    }

    @Override
    public AugmentedEvent apply(RawEvent rawEvent) {
        EventAugmenter.LOGGER.info("transforming event");

        AugmentedEvent augmentedEvent = null;
        try {
            augmentedEvent = mapDataEventToSchema(rawEvent, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return augmentedEvent;
    }
}