package org.ogm;

import java.util.Arrays;

public class PropertyConverter {

    // neo4j only supports Java primitives as property type
    private static Class<?>[] Neo4JTypes = { Boolean.class, Short.class, Byte.class, Integer.class, Double.class,
                                             Long.class, Float.class, String.class, Character.class};

    public PropertyConverter() {

    }

    public Object convertToNeo4jPropertyValue(Object value) {
        if (isPrimitive(value.getClass())) {
            return value;
        }
        throw new RuntimeException("Unsupported type:" + value.getClass());
    }

    private boolean isPrimitive(Class<?> aClass) {
       return aClass.isPrimitive() || Arrays.asList(Neo4JTypes).contains(aClass);
    }
}
