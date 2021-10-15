package com.pelmenstar.projktSens.chartLite.data;

import com.pelmenstar.projktSens.shared.AppendableToStringBuilder;
import com.pelmenstar.projktSens.shared.StringUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public final class ChartData extends AppendableToStringBuilder {
    private static final @NotNull DataSet @NotNull [] EMPTY_DS = new DataSet[0];

    private final @NotNull DataSet @NotNull [] dataSets;

    private final int hash;

    private final float xMin;
    private final float xMax;

    private final float yMin;
    private final float yMax;

    private final int entryCount;

    public ChartData() {
        dataSets = EMPTY_DS;
        entryCount = 0;

        xMin = Float.MAX_VALUE;
        xMax = Float.MIN_VALUE;
        yMin = Float.MAX_VALUE;
        yMax = Float.MIN_VALUE;
        hash = 0;
    }

    public ChartData(@NotNull DataSet dataSet) {
        this(new DataSet[]{dataSet});
    }

    public ChartData(@NotNull DataSet @NotNull ... dataSets) {
        this.dataSets = dataSets;

        float xMin = Float.MAX_VALUE;
        float xMax = Float.MIN_VALUE;
        float yMin = Float.MAX_VALUE;
        float yMax = Float.MIN_VALUE;
        int hash = 0;

        int entryCount = 0;
        for (DataSet set : dataSets) {
            float dxMin = set.getXMin();
            float dxMax = set.getXMax();

            float dyMin = set.getYMin();
            float dyMax = set.getYMax();

            if (dxMin < xMin) {
                xMin = dxMin;
            }

            if (dxMax > xMax) {
                xMax = dxMax;
            }

            if (dyMin < yMin) {
                yMin = dyMin;
            }

            if (dyMax > yMax) {
                yMax = dyMax;
            }

            entryCount += set.getEntryCount();
            hash = 31 * hash + set.getEntriesHash();
        }

        this.hash = hash;
        this.entryCount = entryCount;
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
    }

    public int length() {
        return dataSets.length;
    }

    public float getYMin() {
        return yMin;
    }

    public float getYMax() {
        return yMax;
    }

    public float getXMin() {
        return xMin;
    }

    public float getXMax() {
        return xMax;
    }

    public @NotNull DataSet @NotNull [] getDataSets() {
        return dataSets;
    }

    @NotNull
    public DataSet get(int index) {
        return dataSets[index];
    }

    public int getEntryCount() {
        return entryCount;
    }

    public int indexOf(@Nullable DataSet dataSet) {
        for (int i = 0; i < dataSets.length; i++) {
            if (dataSets[i].equals(dataSet)) {
                return i;
            }
        }

        return -1;
    }

    public boolean contains(@Nullable DataSet dataSet) {
        for (DataSet set : dataSets) {
            if (set.equals(dataSet)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        ChartData o = (ChartData) other;

        return Arrays.equals(dataSets, o.dataSets);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public void append(@NotNull StringBuilder sb) {
        sb.append("{dataSets=");
        StringUtils.appendArray(dataSets, sb);
        sb.append('}');
    }
}
