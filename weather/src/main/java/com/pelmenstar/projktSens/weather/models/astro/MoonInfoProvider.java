package com.pelmenstar.projktSens.weather.models.astro;

import com.pelmenstar.projktSens.shared.time.ShortDateInt;

/**
 * Responsible for providing various information related with moon like moon phase.
 */
public interface MoonInfoProvider {
    /**
     * Returns moon phase on specified date. Moon phase is in range of [0; 1].
     *
     * @throws IllegalArgumentException if date is invalid
     */
    float getMoonPhase(@ShortDateInt int date);
}
