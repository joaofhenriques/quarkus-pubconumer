package org.acme.solaceconnector;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SolaceConsume {
    String value();
    SolaceQueueTye type() default SolaceQueueTye.DURABLE_NON_EXCLUSIVE;
    boolean autoCreate() default false;
}
