package com.jayway.jsonpath.spi.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.jayway.jsonpath.JsonPathException;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;

public class PojoJsonProvider extends JacksonJsonProvider {
    public PojoJsonProvider() {
    }

    public PojoJsonProvider(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    public PojoJsonProvider(ObjectMapper objectMapper, ObjectReader objectReader) {
        super(objectMapper, objectReader);
    }

    @Override
    public boolean isMap(Object obj) {
        return !isArray(obj);
    }

    @Override
    public boolean isArray(Object obj) {
        return super.isArray(obj) || obj.getClass().isArray();
    }

    @Override
    public Object getArrayIndex(Object obj, int idx) {
        return super.getArrayIndex(wrap(obj), idx);
    }

    private List wrap(Object obj) {
        if (obj.getClass().isArray()) {
            int length = Array.getLength(obj);
            List<Object> result = new ArrayList<Object>();
            for (int i = 0; i < length; i++) {
                Object o = Array.get(obj, i);
                result.add(i, o);
            }
            return result;
        } else {
            return (List) obj;
        }
    }

    @Override
    public Collection<String> getPropertyKeys(Object obj) {
        if (isArray(obj)) {
            throw new UnsupportedOperationException();
        } else if (obj instanceof Map) {
            return super.getPropertyKeys(obj);
        } else {
            Field[] declaredFields = obj.getClass().getDeclaredFields();
            List<String> results = new ArrayList<String>();
            for (Field declaredField : declaredFields) {
                results.add(declaredField.getName());
            }
            return results;
        }

    }

    @Override
    public Iterable<? extends Object> toIterable(Object obj) {
        if (isArray(obj)) {
            return wrap(obj);
        } else {
            throw new JsonPathException("Cannot iterate over " + obj != null ? obj.getClass().getName() : "null");
        }
    }

    @Override
    public Object unwrap(Object obj) {
        return super.unwrap(obj);
    }

    @Override
    public Object getMapValue(Object obj, String key) {
        if (obj instanceof Map) {
            return super.getMapValue(obj, key);
        }
        try {
            Field field = obj.getClass().getDeclaredField(key);
            field.setAccessible(true);
            return field.get(obj);
        } catch (NoSuchFieldException e) {
            return UNDEFINED;
        } catch (IllegalAccessException e) {
            return UNDEFINED;
        }
    }
}
