package com.booking.replication.augmenter.model;

import com.booking.replication.augmenter.model.AugmentedEventData;
import com.booking.replication.augmenter.model.AugmentedRow;

import java.util.ArrayList;
import java.util.List;

public class AugmentedEventDataImplementation implements AugmentedEventData {

    private String mysqlTableName;
    private String binlogFileName;
    private List<AugmentedRow> singleRowEvents = new ArrayList<>();

    AugmentedEventDataImplementation(String binlogFileName) {
        this.binlogFileName = binlogFileName;
    }

    public void addSingleRowEvent(AugmentedRow au) {
        singleRowEvents.add(au);
    }

    public List<AugmentedRow> getSingleRowEvents() {
        return singleRowEvents;
    }

    public String getTableName() {
        return mysqlTableName;
    }

    public void setMysqlTableName(String mysqlTableName) {
        this.mysqlTableName = mysqlTableName;
    }

    public String getBinlogFileName() {
        return binlogFileName;
    }
}