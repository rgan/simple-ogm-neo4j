package org.ogm;

import org.neo4j.graphdb.Direction;
import org.ogm.annotations.Property;
import org.ogm.annotations.RelatedTo;

import java.util.HashSet;
import java.util.Set;

public class Club extends PersistentEntity {

    @Property
    private String name;
    private String nonPersistentProperty;
    @RelatedTo(type = "message", direction = Direction.INCOMING)
    private Set<ClubMessage> clubMessages;

    public Club() {
        this(null, null);
    }

    public Club(String name, String nonPersistentProperty) {
        this.name = name;
        this.nonPersistentProperty = nonPersistentProperty;
        this.clubMessages = new HashSet<ClubMessage>();
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

    public void addMessage(ClubMessage message) {
        this.clubMessages.add(message);
    }

    public Set<ClubMessage> getMessages() {
        return clubMessages;
    }
}
