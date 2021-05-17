package com.pelmenstar.projktSens.shared.android

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.pelmenstar.projktSens.shared.android.ui.requestPermissions.RequestPermissionInfo
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RequestPermissionInfoTests {
    @Test
    fun parcel() {
        val perms = arrayOf("1", "2", "3")

        ParcelTestUtils.read_write(RequestPermissionInfo.anyOf("123", *perms), RequestPermissionInfo.CREATOR)
        ParcelTestUtils.read_write(RequestPermissionInfo.everyOf("123", *perms), RequestPermissionInfo.CREATOR)
    }
}