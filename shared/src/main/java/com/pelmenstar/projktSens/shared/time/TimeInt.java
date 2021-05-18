package com.pelmenstar.projktSens.shared.time;

import java.lang.annotation.*;

/**
 * Marks that field, method or parameter contains or returns time-int (seconds of day)
 */
@Documented
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.SOURCE)
public @interface TimeInt {
}
