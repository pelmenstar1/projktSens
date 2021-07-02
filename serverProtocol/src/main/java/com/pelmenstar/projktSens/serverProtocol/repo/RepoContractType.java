package com.pelmenstar.projktSens.serverProtocol.repo;

import org.jetbrains.annotations.NotNull;

public final class RepoContractType {
    public static final int CONTRACT_RAW = 0;
    public static final int CONTRACT_JSON = 1;

    private RepoContractType() {
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
    public static RepoContract get(int contractType) {
        switch (contractType) {
            case CONTRACT_RAW:
                return RawRepoContract.INSTANCE;
            case CONTRACT_JSON:
                return JsonRepoContract.INSTANCE;
            default:
                throw new IllegalArgumentException("contractType");
        }
    }
}
