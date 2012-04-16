package org.ogm;

import org.ogm.annotations.Property;

public class Club extends PersistentEntity {

    @Property
    private String name;
    private String nonPersistentProperty;

    public Club() {
    }

    public Club(String name, String nonPersistentProperty) {
        this.name = name;
        this.nonPersistentProperty = nonPersistentProperty;
    }

    public String getName() {
        return name;
    }

    public String getNonPersistentProperty() {
        return nonPersistentProperty;
    }

    public void setName(String name) {
        this.name = name;
    }
}
