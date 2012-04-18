package org.ogm;

import org.ogm.annotations.Direction;
import org.ogm.annotations.RelatedTo;

public class ClubWithSingleMessage extends PersistentEntity {

    @RelatedTo(type = "single_message", direction = Direction.INCOMING)
    private ClubMessage singleMessage;
}
