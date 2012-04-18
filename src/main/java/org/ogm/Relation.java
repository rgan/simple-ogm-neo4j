package org.ogm;

import org.ogm.annotations.Direction;

import java.util.Set;

public class Relation {
    private String type;
    private Direction direction;
    private Set<PersistentEntity> entities;

    public Relation(Direction direction, String type, Set<PersistentEntity> entities) {
        this.direction = direction;
        this.type = type;
        this.entities = entities;
    }

    public String getType() {
        return type;
    }

    public Direction getDirection() {
        return direction;
    }

    public Set<PersistentEntity> getEntities() {
        return entities;
    }
}
