package com.pelmenstar.projktSens.shared.android

import android.os.Parcel
import android.os.Parcelable
import kotlin.test.assertEquals

object ParcelTestUtils {
    fun<T:Parcelable> read_write(value: T, creator: Parcelable.Creator<T>) {
        val parcel = Parcel.obtain()
        value.writeToParcel(parcel, 0)
        parcel.setDataPosition(0)

        val readFromParcel = creator.createFromParcel(parcel)

        assertEquals(value, readFromParcel)
    }
}