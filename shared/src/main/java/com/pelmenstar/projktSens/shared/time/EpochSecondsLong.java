package com.pelmenstar.projktSens.shared.time;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks that field, parameter or method returns or contains UNIX epoch
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD,ElementType.METHOD, ElementType.PARAMETER })
public @interface EpochSecondsLong {
}
