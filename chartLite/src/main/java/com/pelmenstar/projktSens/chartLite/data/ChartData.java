package com.pelmenstar.projktSens.chartLite.data;

import com.pelmenstar.projktSens.shared.AppendableToStringBuilder;
import com.pelmenstar.projktSens.shared.StringUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public final class ChartData extends AppendableToStringBuilder {
    private static final DataSet[] EMPTY_DS = new DataSet[0];

    @NotNull
    private final DataSet[] dataSets;
    private float yMax = Float.MIN_VALUE;
    private float yMin = Float.MAX_VALUE;
    private float xMax = Float.MIN_VALUE;
    private float xMin = Float.MAX_VALUE;

    private final int entryCount;

    public ChartData() {
        dataSets = EMPTY_DS;
        entryCount = 0;
    }

    public ChartData(@NotNull DataSet dataSet) {
        this(new DataSet[]{dataSet});
    }

    public ChartData(@NotNull DataSet @NotNull ... dataSets) {
        this.dataSets = dataSets;

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
        }

        this.entryCount = entryCount;
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
        return Arrays.hashCode(dataSets);
    }

    @Override
    public void append(@NotNull StringBuilder sb) {
        sb.append("{dataSets=");
        StringUtils.appendArray(dataSets, sb);
        sb.append('}');
    }
}
