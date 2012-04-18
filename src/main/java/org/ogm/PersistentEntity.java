package org.ogm;

import org.neo4j.graphdb.Node;
import org.ogm.annotations.Property;
import org.ogm.annotations.RelatedTo;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class PersistentEntity {

    private Long id;
    private Set<Field> persistentFields;
    private Set<Field> relationshipFields;
    private PropertyConverter converter;
    public static final String TYPE = "__TYPE__";

    public PersistentEntity() {
        converter = new PropertyConverter();
        persistentFields = new HashSet<Field>();
        relationshipFields = new HashSet<Field>();
        Field[] fields = getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.getAnnotation(Property.class) != null) {
                if (!field.isAccessible()) field.setAccessible(true);
                persistentFields.add(field);
            }
            if (field.getAnnotation(RelatedTo.class) != null) {
                if (!field.isAccessible()) field.setAccessible(true);
                relationshipFields.add(field);
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

    public Set<Relation> getRelations() {
        Set<Relation> relations = new HashSet<Relation>();
        for (Field field : relationshipFields) {
            RelatedTo annotation = field.getAnnotation(RelatedTo.class);
            try {
                relations.add(new Relation(annotation.direction(), annotation.type(), asEntities(field.get(this))));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return relations;
    }

    private Set<PersistentEntity> asEntities(Object val) {
        Set<PersistentEntity> entities = new HashSet<PersistentEntity>();
        if (val == null ) {
            return null;
        }
        if (val instanceof PersistentEntity) {
            entities.add((PersistentEntity)val);
            return entities;
        }
        if (val instanceof Iterable) {
            for (Object o : (Iterable) val) {
                if (!(o instanceof PersistentEntity)) {
                    throw new RuntimeException("Relationships must be with persistent entities: Not a persistent entity-" + val);
                }
                entities.add((PersistentEntity)o);
            }
            return entities;
        }
        throw new RuntimeException("Relationships must be with persistent entities: Not a persistent entity-" + val);
    }


    protected Set<Field> getPersistentFields() {
        return persistentFields;
    }

    public Set<Field> getRelationshipFields() {
        return relationshipFields;
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
