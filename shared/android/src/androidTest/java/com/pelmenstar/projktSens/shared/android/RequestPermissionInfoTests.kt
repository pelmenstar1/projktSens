package com.pelmenstar.projktSens.shared.android

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.pelmenstar.projktSens.shared.android.ui.requestPermissions.ModePermissionArray
import com.pelmenstar.projktSens.shared.android.ui.requestPermissions.RequestPermissionInfo
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RequestPermissionInfoTests {
    @Test
    fun parcel() {
        val perms = arrayOf("1", "2", "3")

        ParcelTestUtils.read_write(
            RequestPermissionInfo("123", ModePermissionArray.anyOf(*perms)),
            RequestPermissionInfo.CREATOR)
        ParcelTestUtils.read_write(
            RequestPermissionInfo("123", ModePermissionArray.everyOf(*perms)),
            RequestPermissionInfo.CREATOR
        )
    }
}