package com.pelmenstar.projktSens.shared.android.ui;

import android.content.Context;
import android.graphics.Color;
import android.util.SparseArray;

import androidx.annotation.ArrayRes;
import androidx.annotation.ColorInt;

import com.pelmenstar.projktSens.shared.AppendableToStringBuilder;
import com.pelmenstar.projktSens.shared.StringUtils;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * Represents a linear color transition
 */
public final class LinearColorTransition extends AppendableToStringBuilder implements Cloneable {
    private static final class CacheEntry extends AppendableToStringBuilder {
        public final int @NotNull [] colors;
        public final int framesPerColor;

        private CacheEntry(int @NotNull [] colors, int framesPerColor) {
            this.colors = colors;
            this.framesPerColor = framesPerColor;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (other == null || getClass() != other.getClass()) return false;

            CacheEntry o = (CacheEntry) other;

            return framesPerColor == o.framesPerColor && Arrays.equals(colors, o.colors);
        }

        @Override
        public int hashCode() {
            int result = Arrays.hashCode(colors);
            result = 31 * result + framesPerColor;

            return result;
        }

        @Override
        public void append(@NotNull StringBuilder sb) {
            sb.append("{colors=");
            StringUtils.appendHexColors(colors, sb);
            sb.append(", framesPerColor=");
            sb.append(framesPerColor);
            sb.append('}');
        }
    }

    private static final SparseArray<CacheEntry> transitionCacheByHash = new SparseArray<>(4);
    private static final SparseArray<CacheEntry> transitionCacheByResId = new SparseArray<>(4);

    private static final int[] EMPTY_TCOLORS = new int[1];
    private static final int DEFAULT_TRANSITION_FRAMES = 60;

    private final int[] transColors;
    private final int framesPerColor;

    private int index = 0;
    private int step = 1;
    private int forwardLimitMask = 0xffffffff;

    private int limit;

    private final int forwardLimit;

    private LinearColorTransition(int @NotNull [] transColors, int framesPerColor) {
        this.transColors = transColors;
        this.framesPerColor = framesPerColor;
        forwardLimit = limit = transColors.length - 1;
    }

    @SuppressWarnings("CopyConstructorMissesField")
    public LinearColorTransition(@NotNull LinearColorTransition other) {
        this(other.transColors, other.framesPerColor);
    }

    /**
     * Returns empty color transition which contains only 1 color: transparent
     */
    @NotNull
    public static LinearColorTransition empty() {
        return new LinearColorTransition(EMPTY_TCOLORS, 1);
    }

    /**
     * Creates transition between two colors.
     * The transition will be put to the cache.
     *
     * @param start start color
     * @param end   end color
     */
    @NotNull
    public static LinearColorTransition biColor(@ColorInt int start, @ColorInt int end) {
        return biColor(start, end, DEFAULT_TRANSITION_FRAMES, true);
    }

    @NotNull
    public static LinearColorTransition biColor(@ColorInt int start, @ColorInt int end, int frames) {
        return biColor(start, end, frames, true);
    }

    /**
     * Creates transition between two colors.
     * The transition will put to the cache.
     *
     * @param start      start color
     * @param end        end color
     * @param putToCache determines whether transition should be put to the cache
     */
    @NotNull
    public static LinearColorTransition biColor(
            @ColorInt int start, @ColorInt int end,
            int frames,
            boolean putToCache
    ) {
        int hash = 31 * (31 + start) + end;

        CacheEntry cacheEntry = transitionCacheByHash.get(hash, null);
        if (cacheEntry != null) {
            if(cacheEntry.framesPerColor == frames) {
                return new LinearColorTransition(cacheEntry.colors, frames);
            }
        }

        int[] tColors = new int[frames];

        biColorInternal(start, end, tColors, 0, frames);

        if (putToCache) {
            transitionCacheByHash.put(hash, new CacheEntry(tColors, frames));
        }

        return new LinearColorTransition(tColors, frames);
    }

    private static void biColorInternal(
            @ColorInt int start, @ColorInt int end,
            int @NotNull [] colors,
            int index,
            int frames
    ) {
        int sr = Color.red(start);
        int sg = Color.green(start);
        int sb = Color.blue(start);

        float invFrames = 1f / frames;
        float mr = (float) (Color.red(end) - sr) * invFrames;
        float mg = (float) (Color.green(end) - sg) * invFrames;
        float mb = (float) (Color.blue(end) - sb) * invFrames;

        float result_r = sr;
        float result_g = sg;
        float result_b = sb;

        int endIdx = index + frames;
        for (int i = index; i < endIdx; i++) {
            int c = Color.rgb((int) result_r, (int) result_g, (int) result_b);

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
    public static LinearColorTransition multiple(@ColorInt int @NotNull [] colors) {
        return multiple(colors, DEFAULT_TRANSITION_FRAMES, true);
    }

    @NotNull
    public static LinearColorTransition multiple(@ColorInt int @NotNull [] colors, int framesPerColor) {
        return multiple(colors, framesPerColor, true);
    }

    /**
     * Creates a transition between given colors.
     * Unlike {@link LinearColorTransition#biColor(int, int)}, this method gives more control on transition.
     * The transition is still linear.
     *
     * @param putToCache determines whether transition should be put to the cache
     */
    @NotNull
    public static LinearColorTransition multiple(
            @ColorInt int @NotNull [] colors,
            int framesPerColor,
            boolean putToCache
    ) {
        if (colors.length <= 1) {
            throw new IllegalArgumentException("Colors valuesLength must be > 1");
        }

        if (colors.length == 2) {
            return biColor(colors[0], colors[1], framesPerColor, putToCache);
        }

        int hash = Arrays.hashCode(colors);
        CacheEntry cacheEntry = transitionCacheByHash.get(hash, null);
        if (cacheEntry != null) {
            if(cacheEntry.framesPerColor == framesPerColor) {
                return new LinearColorTransition(cacheEntry.colors, framesPerColor);
            }
        }

        int idx = 0;
        int maxColors = colors.length - 1;

        int[] tColors = new int[maxColors * framesPerColor];

        int i = 0;

        while (i < maxColors) {
            int start = colors[i];
            i++;
            int end = colors[i];

            biColorInternal(start, end, tColors, idx, framesPerColor);
            idx += framesPerColor;
        }

        if (putToCache) {
            transitionCacheByResId.put(hash, new CacheEntry(tColors, framesPerColor));
        }

        return new LinearColorTransition(tColors, framesPerColor);
    }

    @NotNull
    public static LinearColorTransition fromArrayRes(@NotNull Context context, @ArrayRes int colorsRes) {
        return fromArrayRes(context, colorsRes, DEFAULT_TRANSITION_FRAMES, true);
    }

    @NotNull
    public static LinearColorTransition fromArrayRes(
            @NotNull Context context,
            @ArrayRes int colorsRes,
            int frames
    ) {
        return fromArrayRes(context, colorsRes, frames, true);
    }

    @NotNull
    public static LinearColorTransition fromArrayRes(
            @NotNull Context context,
            @ArrayRes int colorsRes,
            int frames,
            boolean putToCache) {
        CacheEntry cacheEntry = transitionCacheByResId.get(colorsRes, null);
        if (cacheEntry != null) {
            if(frames == cacheEntry.framesPerColor) {
                return new LinearColorTransition(cacheEntry.colors, frames);
            }
        }

        int[] colors = context.getResources().getIntArray(colorsRes);
        LinearColorTransition transition = multiple(colors, frames, putToCache);

        if (putToCache) {
            transitionCacheByResId.put(
                    colorsRes,
                    new CacheEntry(transition.transColors, frames)
            );
        }

        return transition;
    }

    /**
     * Returns a next color of transition, and moves cursor to next. More helpful in loop
     */
    @ColorInt
    public int nextColor() {
        int s = step;
        if (index == limit) {
            int newMask = ~forwardLimitMask;
            limit = forwardLimit & newMask;

            s = -s;
            step = s;
            forwardLimitMask = newMask;
        }
        index += s;

        return transColors[index];
    }

    public int getFramesPerColor() {
        return framesPerColor;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        LinearColorTransition o = (LinearColorTransition) other;

        return Arrays.equals(transColors, o.transColors) &&
                framesPerColor == o.framesPerColor;
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(transColors);
        result = result * 31 + framesPerColor;

        return result;
    }

    @Override
    public void append(@NotNull StringBuilder sb) {
        sb.append("{resultColors=");
        StringUtils.appendHexColors(transColors, sb);
        sb.append('}');
    }
}
