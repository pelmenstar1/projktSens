package com.pelmenstar.projktSens.shared.android.ui;

import android.content.Context;
import android.graphics.Color;
import android.util.SparseArray;

import androidx.annotation.ArrayRes;
import androidx.annotation.ColorInt;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * Represents a linear color transition
 */
public final class LinearColorTransition {
    private static final SparseArray<int[]> transitionCacheByHash = new SparseArray<>(4);
    private static final SparseArray<int[]> transitionCacheByResId = new SparseArray<>(4);

    private static final int[] EMPTY_TCOLORS = new int[1];

    private static final int TRANSITION_FRAMES = 60;
    private static final float TRANSITION_FRAMES_INV = 1f / (float)TRANSITION_FRAMES;

    private final int[] transColors;

    private int index = 0;
    private boolean forward = true;

    private LinearColorTransition(@NotNull int[] transColors) {
        this.transColors = transColors;
    }

    /**
     * Returns empty color transition which contains only 1 color: transparent
     */
    @NotNull
    public static LinearColorTransition empty() {
        return new LinearColorTransition(EMPTY_TCOLORS);
    }

    /**
     * Creates transition between two colors.
     * The transition will be put to the cache.
     * You can control such behaviour in {@link LinearColorTransition#biColor(int, int, boolean)}.
     *
     * @param start start color
     * @param end end color
     */
    @NotNull
    public static LinearColorTransition biColor(@ColorInt int start, @ColorInt int end) {
        return biColor(start, end,true);
    }

    /**
     * Creates transition between two colors.
     * The transition will put to the cache.
     *
     * @param start start color
     * @param end end color
     * @param putToCache determines whether transition should be put to the cache
     */
    @NotNull
    public static LinearColorTransition biColor(@ColorInt int start, @ColorInt int end, boolean putToCache) {
        int hash = 31 * (31 + start) + end;

        int[] tColorsFromCache = transitionCacheByHash.get(hash, null);
        if(tColorsFromCache != null) {
            return new LinearColorTransition(tColorsFromCache);
        }

        int[] tColors = new int[TRANSITION_FRAMES];

        biColorInternal(start, end, tColors, 0);

        if(putToCache) {
            transitionCacheByHash.put(hash, tColors);
        }

        return new LinearColorTransition(tColors);
    }

    private static void biColorInternal(@ColorInt int start, @ColorInt int end, int[] colors, int index) {
        int sr = Color.red(start);
        int sg = Color.green(start);
        int sb = Color.blue(start);

        float mr = (float)(Color.red(end) - sr) * TRANSITION_FRAMES_INV;
        float mg = (float)(Color.green(end) - sg) * TRANSITION_FRAMES_INV;
        float mb = (float)(Color.blue(end) - sb) * TRANSITION_FRAMES_INV;

        float result_r = sr;
        float result_g = sg;
        float result_b = sb;

        int endIdx = index + TRANSITION_FRAMES;
        for(int i = index; i < endIdx; i++) {
            int c = Color.rgb((int)result_r, (int)result_g, (int)result_b);

            result_r += mr;
            result_g += mg;
            result_b += mb;

            colors[i] = c;
        }
    }

    /**
     * Creates a transition between given colors.
     * Unlike {@link LinearColorTransition#biColor(int, int)}, this method gives more control on transition.
     * The transition is still linear.
     * The transition will be put to the cache.
     */
    @NotNull
    public static LinearColorTransition multiple(@NotNull int[] colors) {
        return multiple(colors, true);
    }

    /**
     * Creates a transition between given colors.
     * Unlike {@link LinearColorTransition#biColor(int, int)}, this method gives more control on transition.
     * The transition is still linear.
     *
     * @param putToCache determines whether transition should be put to the cache
     */
    @NotNull
    public static LinearColorTransition multiple(@NotNull int[] colors, boolean putToCache) {
        if(colors.length <= 1) {
            throw new IllegalArgumentException("Colors valuesLength must be > 1");
        }

        if(colors.length == 2) {
            return biColor(colors[0], colors[1]);
        }

        int hash = Arrays.hashCode(colors);
        int[] tColorsFromCache = transitionCacheByHash.get(hash, null);
        if(tColorsFromCache != null) {
            return new LinearColorTransition(tColorsFromCache);
        }

        int idx = 0;
        int maxColors = colors.length - 1;

        int[] tColors = new int[maxColors * TRANSITION_FRAMES];

        int i = 0;

        while(i < maxColors) {
            int start = colors[i];
            i++;
            int end = colors[i];

            biColorInternal(start, end, tColors, idx);
            idx += TRANSITION_FRAMES;
        }

        if(putToCache) {
            transitionCacheByResId.put(hash, tColors);
        }

        return new LinearColorTransition(tColors);
    }

    @NotNull
    public static LinearColorTransition fromArrayRes(@NotNull Context context, @ArrayRes int colorsRes) {
        return fromArrayRes(context, colorsRes, true);
    }

    @NotNull
    public static LinearColorTransition fromArrayRes(@NotNull Context context, @ArrayRes int colorsRes, boolean putToCache) {
        int[] tColorsFromCache = transitionCacheByResId.get(colorsRes, null);
        if(tColorsFromCache != null) {
            return new LinearColorTransition(tColorsFromCache);
        }

        int[] colors = context.getResources().getIntArray(colorsRes);
        LinearColorTransition transition = multiple(colors, putToCache);

        if(putToCache) {
            transitionCacheByResId.put(colorsRes, transition.transColors);
        }

        return transition;
    }

    /**
     * Returns a next color of transition, and moves cursor to next. More helpful in loop
     */
    public int nextColor() {
        synchronized (this) {
            if (forward) {
                if (index == transColors.length - 1) {
                    forward = false;
                } else {
                    index++;
                }
            } else {
                if (index == 0) {
                    forward = true;
                } else {
                    index--;
                }
            }

            return transColors[index];
        }
    }

    /**
     * Copies transition
     */
    @NotNull
    public LinearColorTransition copy() {
        return new LinearColorTransition(transColors);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LinearColorTransition other = (LinearColorTransition) o;

        return Arrays.equals(transColors, other.transColors);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(transColors);
    }
}
