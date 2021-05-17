@file:Suppress("NOTHING_TO_INLINE")

package com.pelmenstar.projktSens.shared.android.ui.requestPermissions

import android.os.Parcel
import android.os.Parcelable
import com.pelmenstar.projktSens.shared.add
import com.pelmenstar.projktSens.shared.android.readNonNullString
import com.pelmenstar.projktSens.shared.equalsPattern

class ModePermissionArray : Parcelable {
    val mode: Int
    val androidPermissions: Array<out String>

    internal constructor(mode: Int, permissions: Array<out String>) {
        this.mode = mode
        this.androidPermissions = permissions
    }

    private constructor(parcel: Parcel) {
        mode = parcel.readInt()

        val length = parcel.readInt()
        androidPermissions = Array(length) { parcel.readNonNullString() }
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(mode)

        dest.writeInt(androidPermissions.size)
        androidPermissions.forEach(dest::writeString)
    }

    override fun describeContents(): Int = 0

    override fun equals(other: Any?): Boolean {
        return equalsPattern(other) { o ->
            mode == o.mode && androidPermissions.contentEquals(o.androidPermissions)
        }
    }

    override fun hashCode(): Int {
        var result = mode
        result = 31 * result + androidPermissions.contentHashCode()
        return result
    }

    companion object {
        const val MODE_ANY = 0
        const val MODE_EVERY = 1

        @JvmField
        val CREATOR = object : Parcelable.Creator<ModePermissionArray> {
            override fun createFromParcel(source: Parcel) = ModePermissionArray(source)
            override fun newArray(size: Int) = arrayOfNulls<ModePermissionArray>(size)
        }

        fun anyOf(vararg androidPermissions: String) = ModePermissionArray(MODE_ANY, androidPermissions)
        fun everyOf(vararg androidPermissions: String) = ModePermissionArray(MODE_EVERY, androidPermissions)
    }
}

class RequestPermissionInfo : Parcelable {
    val userDescription: String
    val modePermissions: ModePermissionArray

    constructor(needToRequestString: String, permissions: ModePermissionArray) {
        this.userDescription = needToRequestString
        this.modePermissions = permissions
    }

    private constructor(parcel: Parcel) {
        userDescription = parcel.readNonNullString()
        modePermissions = ModePermissionArray.CREATOR.createFromParcel(parcel)
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(userDescription)
        modePermissions.writeToParcel(dest, 0)
    }

    override fun describeContents(): Int = 0

    override fun equals(other: Any?): Boolean {
        return equalsPattern(other) { o ->
            userDescription == o.userDescription && modePermissions == o.modePermissions
        }
    }

    override fun hashCode(): Int {
        var result = userDescription.hashCode()
        result = 31 * result + modePermissions.hashCode()
        return result
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<RequestPermissionInfo> {
            override fun createFromParcel(source: Parcel) = RequestPermissionInfo(source)
            override fun newArray(size: Int) = arrayOfNulls<RequestPermissionInfo>(size)
        }
    }
}

class RequestPermissionsContext: Parcelable {
    private val permissions: Array<out RequestPermissionInfo>
    val count: Int
        get() = permissions.size

    constructor(permissions: Array<out RequestPermissionInfo>) {
        this.permissions = permissions
    }

    private constructor(parcel: Parcel) {
        val size = parcel.readInt()
        permissions = Array(size) { RequestPermissionInfo.CREATOR.createFromParcel(parcel) }
    }

    operator fun get(index: Int): RequestPermissionInfo {
        return permissions[index]
    }

    override fun equals(other: Any?): Boolean {
        return equalsPattern(other) { o ->
            permissions contentEquals o.permissions
        }
    }

    override fun hashCode(): Int {
        return permissions.contentHashCode()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(permissions.size)

        for(perm in permissions) {
            perm.writeToParcel(dest, 0)
        }
    }

    override fun describeContents(): Int = 0

    companion object {
        @JvmField
        val CREATOR = object: Parcelable.Creator<RequestPermissionsContext> {
            override fun createFromParcel(source: Parcel) = RequestPermissionsContext(source)
            override fun newArray(size: Int) = arrayOfNulls<RequestPermissionsContext>(size)
        }
    }
}

class RequestPermissionsContextBuilder {
    object PermissionArrayModeSelect {
        fun anyOf(vararg permissions: String) = ModePermissionArray(ModePermissionArray.MODE_ANY, permissions)
        fun everyOf(vararg permissions: String) = ModePermissionArray(ModePermissionArray.MODE_EVERY, permissions)
    }

    @JvmField
    var _permissions = emptyArray<RequestPermissionInfo>()

    inline fun permission(userDescription: String, block: PermissionArrayModeSelect.() -> ModePermissionArray) {
        val modeArray = block(PermissionArrayModeSelect)
        val perm = RequestPermissionInfo(userDescription, modeArray)

        _permissions = _permissions.add(perm)
    }
}

inline fun RequestPermissionsContext(block: RequestPermissionsContextBuilder.() -> Unit): RequestPermissionsContext {
    val builder = RequestPermissionsContextBuilder()
    builder.block()

    return RequestPermissionsContext(builder._permissions)
}