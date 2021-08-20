package com.pelmenstar.projktSens.chartLite.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ChartData {
    private static final DataSet[] EMPTY_DS = new DataSet[0];
    @NotNull
    private final DataSet[] dataSets;
    private float yMax = Float.MIN_VALUE;
    private float yMin = Float.MAX_VALUE;
    private float xMax = Float.MIN_VALUE;
    private float xMin = Float.MAX_VALUE;

    public ChartData() {
        dataSets = EMPTY_DS;
    }

    public ChartData(@NotNull DataSet dataSet) {
        this(new DataSet[]{dataSet});
    }

    public ChartData(@NotNull DataSet... dataSets) {
        this.dataSets = dataSets;

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
        }
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

    @NotNull
    public DataSet[] getDataSets() {
        return dataSets;
    }

    @NotNull
    public DataSet get(int index) {
        return dataSets[index];
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

    public int getEntryCount() {
        int count = 0;

        for (DataSet set : dataSets) {
            count += set.count();
        }

        return count;
    }

    public boolean isContainsAnyEntry() {
        for (DataSet set : dataSets) {
            if (set.count() > 0) {
                return true;
            }
        }

        return false;
    }
}
