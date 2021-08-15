package com.pelmenstar.projktSens.serverProtocol;

import org.jetbrains.annotations.NotNull;

public final class ContractType {
    public static final int RAW = 0;
    public static final int JSON = 1;

    private ContractType() {
    }

    public static boolean isValid(int contractType) {
        switch (contractType) {
            case RAW:
            case JSON:
                return true;
            default:
                return false;
        }
    }

    @NotNull
    public static Contract toObject(int contractType) {
        switch (contractType) {
            case RAW:
                return RawContract.INSTANCE;
            case JSON:
                return JsonContract.INSTANCE;
            default:
                throw new IllegalArgumentException("contractType");
        }
    }
}
