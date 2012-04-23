package org.ogm;


import com.google.common.collect.Sets;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.RelationshipType;

import java.util.Set;

public class Relation {
    private RelationshipType type;
    private Direction direction;
    private Set<PersistentEntity> entities;

    public Relation(Direction direction, String type, Set<? extends PersistentEntity> entities) {
        this.direction = direction;
        this.type = DynamicRelationshipType.withName(type);
        this.entities = entities == null ? null : Sets.newHashSet(entities);
    }

    public RelationshipType getType() {
        return type;
    }

    public Direction getDirection() {
        return direction;
    }

    public Set<? extends PersistentEntity> getEntities() {
        return entities;
    }

    public String getTypeName() {
        return type.name();
    }

    public void add(Set<? extends PersistentEntity> entities) {
        for (PersistentEntity entity : entities) {
            this.entities.add(entity);
        }
    }
}
