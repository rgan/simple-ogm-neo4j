package org.ogm;

import org.neo4j.graphdb.Node;
import org.ogm.annotations.Property;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class PersistentEntity {

    private Long id;
    private Set<Field> persistentFields;
    private PropertyConverter converter;
    public static final String TYPE = "__TYPE__";

    public PersistentEntity() {
        converter = new PropertyConverter();
        persistentFields = new HashSet<Field>();
        Field[] fields = getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.getAnnotation(Property.class) != null) {
                if (!field.isAccessible()) field.setAccessible(true);
                persistentFields.add(field);
            }
        }
    }

    public Long getId() {
        return id;
    }

    void setId(Long id) {
        this.id = id;
    }

    public Map<String, Object> getProperties() {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(TYPE, this.getClass().getName());
        for (Field persistentField : persistentFields) {
            try {
                properties.put(persistentField.getName(), converter.convertToNeo4jPropertyValue(persistentField.get(this)));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return properties;
    }

    protected Set<Field> getPersistentFields() {
        return persistentFields;
    }

    public static PersistentEntity from(Node node) {
        String typeProperty = (String) node.getProperty(TYPE);
        PersistentEntity entity = createInstance(typeProperty);
        for (Field field : entity.getPersistentFields()) {
            try {
                field.set(entity, node.getProperty(field.getName()));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return entity;
    }

    private static PersistentEntity createInstance(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            return (PersistentEntity) clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
