package org.ogm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface RelatedTo {

    String type() default "";
    org.neo4j.graphdb.Direction direction() default org.neo4j.graphdb.Direction.OUTGOING;

}
