package org.ogm;

import org.neo4j.graphdb.Direction;
import org.ogm.annotations.RelatedTo;

public class ClubWithSingleMessage extends PersistentEntity {

    @RelatedTo(type = "single_message", direction = Direction.INCOMING)
    private ClubMessage singleMessage;

    public void setMessage(ClubMessage msg) {
        this.singleMessage = msg;
    }

    public ClubMessage getMessage() {
        return singleMessage;
    }
}
