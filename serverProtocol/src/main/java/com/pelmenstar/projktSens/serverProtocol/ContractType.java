package com.pelmenstar.projktSens.serverProtocol;

import org.jetbrains.annotations.NotNull;

public final class ContractType {
    public static final int RAW = 0;

    private ContractType() {
    }

    public static boolean isValid(int contractType) {
        return contractType == RAW;
    }

    @NotNull
    public static Contract toObject(int contractType) {
        if (contractType == RAW) {
            return RawContract.INSTANCE;
        }
        throw new IllegalArgumentException("contractType");
    }
}
