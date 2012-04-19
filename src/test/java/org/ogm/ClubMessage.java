package org.ogm;

import org.ogm.annotations.Property;

public class ClubMessage extends PersistentEntity {

    @Property
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
