package com.pelmenstar.projktSens.shared.android

import com.pelmenstar.projktSens.shared.android.ui.requestPermissions.ModePermissionArray
import org.junit.Test

class ModePermissionArrayTests {
    @Test
    fun parcel() {
        ParcelTestUtils.read_write(
            ModePermissionArray.anyOf("123", "321"),
            ModePermissionArray.CREATOR
        )
        ParcelTestUtils.read_write(
            ModePermissionArray.everyOf("123", "321"),
            ModePermissionArray.CREATOR
        )
    }
}