package com.pelmenstar.projktSens.shared.android.ui.requestPermissions;

import com.pelmenstar.projktSens.shared.IntPair;

public final class PackedPermissionState {
    private PackedPermissionState() {}

    public static long create(int id, int state) {
        return IntPair.of(id, state);
    }

    public static int getId(long packed) {
        return IntPair.getFirst(packed);
    }

    public static int getState(long packed) {
        return IntPair.getSecond(packed);
    }
}
