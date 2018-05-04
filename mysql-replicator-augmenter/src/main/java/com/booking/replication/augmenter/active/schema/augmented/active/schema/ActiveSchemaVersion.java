package com.booking.replication.augmenter.active.schema.augmented.active.schema;

import com.booking.replication.augmenter.model.AugmentedSchemaChangeEventData;

import java.sql.SQLException;
import java.util.HashMap;

public interface ActiveSchemaVersion {
    void loadActiveSchema() throws SQLException;

    String schemaTablesToJson();

    String schemaCreateStatementsToJson();

    String toJson();

    TableSchemaVersion getTableSchemaVersion(String tableName);

    AugmentedSchemaChangeEventData transitionSchemaToNextVersion(HashMap<String, String> schemaTransitionSequence, Long timestamp)
            throws Exception;

    void applyDDL(HashMap<String, String> sequence)
            throws Exception, SQLException;
}