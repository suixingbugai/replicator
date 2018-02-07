package com.booking.replication.model;

public class EventImplementation<Header extends EventHeader, Data extends EventData> implements Event {
    private Header header;
    private Data data;

    public EventImplementation(Header header, Data data) {
        this.header = header;
        this.data = data;
    }

    public EventImplementation() {
        this(null, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Header getHeader() {
        return this.header;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Data getData() {
        return this.data;
    }
}