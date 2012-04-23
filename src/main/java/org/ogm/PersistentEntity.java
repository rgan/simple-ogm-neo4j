package org.ogm;

import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.ogm.annotations.Property;
import org.ogm.annotations.RelatedTo;

import java.lang.reflect.Field;
import java.util.*;

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
            RelatedTo annotation = field.getAnnotation(RelatedTo.class);
            if (annotation != null) {
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
                Object value = persistentField.get(this);
                if (value != null) {
                    properties.put(persistentField.getName(), converter.convertToNeo4jPropertyValue(value));
                }
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
        if (val == null) {
            return null;
        }
        if (val instanceof PersistentEntity) {
            entities.add((PersistentEntity) val);
            return entities;
        }
        if (val instanceof Iterable) {
            for (Object o : (Iterable) val) {
                if (!(o instanceof PersistentEntity)) {
                    throw new RuntimeException("Relationships must be with persistent entities: Not a persistent entity-" + val);
                }
                entities.add((PersistentEntity) o);
            }
            return entities;
        }
        throw new RuntimeException("Relationships must be with persistent entities: Not a persistent entity-" + val);
    }


    protected Set<Field> getPersistentFields() {
        return persistentFields;
    }

    protected Set<Field> getRelationshipFields() {
        return relationshipFields;
    }

    public static PersistentEntity from(Node node) {
        String typeProperty = (String) node.getProperty(TYPE);
        PersistentEntity entity = createInstance(typeProperty);
        for (Field field : entity.persistentFields) {
            try {
                field.set(entity, node.getProperty(field.getName()));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        createRelationships(entity, node);
        return entity;
    }

    private static void createRelationships(PersistentEntity entity, Node node) {
        for (Field field : entity.relationshipFields) {
            RelatedTo annotation = field.getAnnotation(RelatedTo.class);
            Iterable<Relationship> relationships = node.getRelationships(DynamicRelationshipType.withName(annotation.type()),
                    annotation.direction());
            try {
                Set<? extends PersistentEntity> entities = relatedEntitiesFrom(node, relationships);
                if (Collection.class.isAssignableFrom(field.getType())) {
                    field.set(entity, entities);
                } else {
                    field.set(entity, entities.size() == 1 ? entities.iterator().next() : null);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static Set<? extends PersistentEntity> relatedEntitiesFrom(Node node, Iterable<Relationship> relationships) {
        Set<PersistentEntity> entities = new HashSet<PersistentEntity>();
        for (Relationship relationship : relationships) {
             entities.add(PersistentEntity.from(relationship.getOtherNode(node)));
        }
        return entities;
    }

    private Field getRelationshipField(Relation relation) {
        for (Field field : relationshipFields) {
           RelatedTo annotation = field.getAnnotation(RelatedTo.class);
           if (annotation.type().equals(relation.getTypeName()) &&
                   annotation.direction().equals(relation.getDirection())) {
               return field;
           }
        }
        throw new RuntimeException("Invalid relation for entity:" + relation.getTypeName());
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
