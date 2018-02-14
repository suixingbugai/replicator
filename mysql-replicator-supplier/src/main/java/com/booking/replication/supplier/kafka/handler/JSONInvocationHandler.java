package com.booking.replication.supplier.kafka.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.beans.Introspector;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

public class JSONInvocationHandler implements InvocationHandler {
    private static final TypeReference<Map<String, Object>> TYPE_REFERENCE = new TypeReference<Map<String, Object>>(){};

    private final Map<String, Object> map;

    public JSONInvocationHandler(ObjectMapper mapper, byte[] data) throws IOException {
        this.map = mapper.readValue(data, JSONInvocationHandler.TYPE_REFERENCE);
    }

    public JSONInvocationHandler(byte[] data) throws IOException {
        this(new ObjectMapper(), data);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        if (method.getName().startsWith("get") && method.getName().length() > 3) {
            return this.invokeGet(method, 3);
        } else if (method.getName().startsWith("is") && method.getName().length() > 2) {
            return this.invokeGet(method, 2);
        } else if (method.getName().startsWith("set") && method.getName().length() > 3) {
            return this.invokeSet(method, args[0]);
        } else if (method.getName().equals("toString")) {
            return this.map.toString();
        } else {
            return method.getReturnType().cast(this.map.get(method.getName()));
        }
    }

    private Object invokeGet(Method method, int length) {
        return method.getReturnType().cast(this.map.get(this.getPropertyName(method, length)));
    }

    private Object invokeSet(Method method, Object value) {
        this.map.put(this.getPropertyName(method, 3), value);
        return null;
    }

    private String getPropertyName(Method method, int length) {
        return Introspector.decapitalize(method.getName().substring(length));
    }
}