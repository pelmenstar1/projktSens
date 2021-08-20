package com.pelmenstar.projktSens.shared.android

import com.pelmenstar.projktSens.shared.android.ui.requestPermissions.ModePermissionArray
import com.pelmenstar.projktSens.shared.android.ui.requestPermissions.RequestPermissionInfo
import com.pelmenstar.projktSens.shared.android.ui.requestPermissions.RequestPermissionsContext
import org.junit.Test

class RequestPermissionsContextTests {
    @Test
    fun parcel() {
        ParcelTestUtils.read_write(
            RequestPermissionsContext(
                arrayOf(
                    RequestPermissionInfo(
                        43, 545, ModePermissionArray.anyOf("548", "554")
                    ),
                    RequestPermissionInfo(
                        4356, 437438, ModePermissionArray.anyOf("4334", "324")
                    )
                ),
            ),
            RequestPermissionsContext.CREATOR
        )
    }
}