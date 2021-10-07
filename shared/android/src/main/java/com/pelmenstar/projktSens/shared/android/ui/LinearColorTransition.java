package com.pelmenstar.projktSens.shared.android.ui;

import android.content.Context;
import android.graphics.Color;

import androidx.annotation.ArrayRes;
import androidx.annotation.ColorInt;

import com.pelmenstar.projktSens.shared.AppendableToStringBuilder;
import com.pelmenstar.projktSens.shared.EmptyArray;
import com.pelmenstar.projktSens.shared.StringUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * Represents a linear color transition
 */
public final class LinearColorTransition extends AppendableToStringBuilder implements Cloneable {
    private static final class IntArrayRef {
        private int @NotNull [] value;

        public IntArrayRef(int @NotNull [] initialValue) {
            value = initialValue;
        }
    }

    private static final class FlatCache {
        private final IntArrayRef arrayRef = new IntArrayRef(EmptyArray.INT);

        public int addAndAllocSize(int key, int dataSize) {
            int[] array = arrayRef.value;
            int dataStartPos = array.length + 2;
            int[] newArray = new int[dataStartPos + dataSize];
            System.arraycopy(array, 0, newArray, 0, array.length);
            newArray[array.length] = key;
            newArray[array.length + 1] = dataSize;

            arrayRef.value = newArray;

            return dataStartPos;
        }

        @Nullable
        public LinearColorTransition createTransitionIfInCache(int key) {
            int pos = 0;
            int[] array = arrayRef.value;

            while(pos < array.length) {
                int currentKey = array[pos];
                int currentDataSize = array[pos + 1];

                if(currentKey == key) {
                    return new LinearColorTransition(arrayRef, pos + 2, currentDataSize);
                }

                pos += currentDataSize + 2;
            }

            return null;
        }
    }

    private static final FlatCache transitionCacheByHash = new FlatCache();
    private static final FlatCache transitionCacheByResId = new FlatCache();

    private static final IntArrayRef EMPTY_DATA_REF = new IntArrayRef(new int[1]);
    public static final int TRANSITION_FRAMES = 60;
    private static final float INV_TRANSITION_FRAMES = 1f / (float) TRANSITION_FRAMES;

    private final IntArrayRef dataRef;
    private final int offset;
    private final int length;

    private int index = 0;
    private int step = 1;

    private int limitMask = 0xffffffff;
    private int limit;

    private LinearColorTransition(@NotNull IntArrayRef dataRef, int offset, int length) {
        this.dataRef = dataRef;
        this.offset = offset;
        this.length = length;

        recomputeLimit();
    }

    @SuppressWarnings("CopyConstructorMissesField")
    public LinearColorTransition(@NotNull LinearColorTransition other) {
        this(other.dataRef, other.offset, other.length);
    }

    /**
     * Returns empty color transition which contains only 1 color: transparent
     */
    @NotNull
    public static LinearColorTransition empty() {
        return new LinearColorTransition(EMPTY_DATA_REF, 0, 1);
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
        int hash = 31 * (31 + start) + end;

        LinearColorTransition cached = transitionCacheByHash.createTransitionIfInCache(hash);

        if(cached != null) {
            return cached;
        }

        int startPos = transitionCacheByHash.addAndAllocSize(hash, TRANSITION_FRAMES);

        biColorInternal(start, end, transitionCacheByHash.arrayRef.value, startPos);

        return new LinearColorTransition(
                transitionCacheByHash.arrayRef, startPos, TRANSITION_FRAMES
        );
    }

    private static void biColorInternal(
            @ColorInt int start, @ColorInt int end,
            int @NotNull [] colors,
            int index
    ) {
        int sr = Color.red(start);
        int sg = Color.green(start);
        int sb = Color.blue(start);

        float mr = (float) (Color.red(end) - sr) * INV_TRANSITION_FRAMES;
        float mg = (float) (Color.green(end) - sg) * INV_TRANSITION_FRAMES;
        float mb = (float) (Color.blue(end) - sb) * INV_TRANSITION_FRAMES;

        float result_r = sr;
        float result_g = sg;
        float result_b = sb;

        int endIdx = index + TRANSITION_FRAMES;
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
        if (colors.length <= 1) {
            throw new IllegalArgumentException("Colors valuesLength must be > 1");
        }

        int hash = Arrays.hashCode(colors);
        LinearColorTransition cached = transitionCacheByHash.createTransitionIfInCache(
                hash
        );

        if(cached != null) {
            return cached;
        }

        int maxColors = colors.length - 1;

        int tColorsLength = maxColors * TRANSITION_FRAMES;
        int startPos = transitionCacheByHash.addAndAllocSize(hash, tColorsLength);
        int tColorPos = startPos;
        int[] data = transitionCacheByHash.arrayRef.value;

        for(int i = 0; i < maxColors; i++) {
            int start = colors[i];
            int end = colors[i + 1];

            biColorInternal(start, end, data, tColorPos);
            tColorPos += TRANSITION_FRAMES;
        }

        return new LinearColorTransition(
                transitionCacheByHash.arrayRef, startPos, tColorsLength
        );
    }

    @NotNull
    public static LinearColorTransition fromArrayRes(@NotNull Context context, @ArrayRes int colorsRes) {
        LinearColorTransition cached = transitionCacheByResId.createTransitionIfInCache(colorsRes);
        if(cached != null) {
            return cached;
        }

        int[] colors = context.getResources().getIntArray(colorsRes);
        LinearColorTransition transition = multiple(colors);

        int startPos = transitionCacheByResId.addAndAllocSize(
                colorsRes, transition.length
        );
        System.arraycopy(
                transition.dataRef.value, transition.offset,
                transitionCacheByResId.arrayRef.value, startPos,
                transition.length
        );

        return transition;
    }

    /**
     * Returns a next color of transition, and moves cursor to next. More helpful in loop
     */
    @ColorInt
    public int nextColor() {
        if (index == limit) {
            limitMask = ~limitMask;
            step = -step;

            recomputeLimit();
        }
        index += step;

        return dataRef.value[index];
    }

    private void recomputeLimit() {
        limit = offset + ((length - 1) & limitMask);
    }

    @Override
    public void append(@NotNull StringBuilder sb) {
        sb.append("{resultColors=");
        StringUtils.appendHexColors(dataRef.value, offset, length, sb);
        sb.append('}');
    }
}
