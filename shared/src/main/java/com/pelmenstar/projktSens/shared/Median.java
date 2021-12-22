package com.pelmenstar.projktSens.shared;

import org.jetbrains.annotations.NotNull;

public final class Median {
    private Median() {}

    private static void swap(float @NotNull [] values, int i1, int i2) {
        float t = values[i1];
        values[i1] = values[i2];
        values[i2] = t;
    }

    private static void swapIfGreater(float @NotNull [] values, int i1, int i2) {
        float a = values[i1];
        float b = values[i2];

        if(a > b) {
            values[i1] = b;
            values[i2] = a;
        }
    }

    private static float quickSelect(float @NotNull [] values, int k) {
        int n = values.length;
        int i, j, mid;
        float a, temp;

        int l = 0;
        int ir = n - 1;
        for (; ; ) {
            if (ir <= l + 1) {
                if (ir == l + 1 && values[ir] < values[l]) {
                    swap(values, l, ir);
                }

                return values[k];
            } else {
                mid = (l + ir) >> 1;
                swap(values, mid, l + 1);

                swapIfGreater(values, l, ir);
                swapIfGreater(values, l + 1, ir);
                swapIfGreater(values, l, l + 1);

                i = l + 1;
                j = ir;
                a = values[l + 1];

                for (; ; ) {
                    do i++; while (values[i] < a);
                    do j--; while (values[j] > a);
                    if (j < i) break;

                    swap(values, i, j);
                }
                values[l + 1] = values[j];
                values[j] = a;

                if (j >= k) ir = j - 1;
                if (j <= k) l = i;
            }
        }
    }

    public static float compute(float @NotNull [] values) {
        int mid = values.length / 2;
        if(mid * 2 == values.length) {
            return (quickSelect(values, mid) + quickSelect(values, mid + 1)) * 0.5f;
        } else {
            return quickSelect(values, mid);
        }
    }
}
