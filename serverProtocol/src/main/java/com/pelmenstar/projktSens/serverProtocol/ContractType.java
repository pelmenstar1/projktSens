package com.pelmenstar.projktSens.serverProtocol;

import org.jetbrains.annotations.NotNull;

public final class ContractType {
    public static final int CONTRACT_RAW = 0;
    public static final int CONTRACT_JSON = 1;

    private ContractType() {
    }

    public static boolean isValid(int contractType) {
        switch (contractType) {
            case CONTRACT_RAW:
            case CONTRACT_JSON:
                return true;
            default:
                return false;
        }
    }

    @NotNull
    public static Contract get(int contractType) {
        switch (contractType) {
            case CONTRACT_RAW:
                return RawContract.INSTANCE;
            case CONTRACT_JSON:
                return JsonContract.INSTANCE;
            default:
                throw new IllegalArgumentException("contractType");
        }
    }
}
