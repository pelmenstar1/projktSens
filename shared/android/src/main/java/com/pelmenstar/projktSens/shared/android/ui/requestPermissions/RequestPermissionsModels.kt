@file:Suppress("NOTHING_TO_INLINE")

package com.pelmenstar.projktSens.shared.android.ui.requestPermissions

import android.app.Activity
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import com.pelmenstar.projktSens.shared.*
import com.pelmenstar.projktSens.shared.android.ext.readNonNullString

class ModePermissionArray : Parcelable, Collection<String>, AppendableToStringBuilder {
    val mode: Int
    private val androidPermissions: Array<out String>

    override val size: Int
        get() = androidPermissions.size

    internal constructor(mode: Int, permissions: Array<out String>) {
        this.mode = mode
        this.androidPermissions = permissions
    }

    private constructor(parcel: Parcel) {
        mode = parcel.readInt()

        val length = parcel.readInt()
        androidPermissions = Array(length) { parcel.readNonNullString() }
    }

    @RequiresApi(23)
    fun request(activity: Activity, requestCode: Int) {
        activity.requestPermissions(androidPermissions, requestCode)
    }

    override fun contains(element: String): Boolean {
        return androidPermissions.contains(element)
    }

    override fun containsAll(elements: Collection<String>): Boolean {
        return elements.all { contains(it) }
    }

    override fun isEmpty(): Boolean = androidPermissions.isEmpty()
    override fun iterator(): Iterator<String> = ArrayIterator(androidPermissions)

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(mode)

        dest.writeInt(androidPermissions.size)
        androidPermissions.forEach(dest::writeString)
    }

    override fun describeContents(): Int = 0

    override fun equals(other: Any?): Boolean {
        return equalsPattern(other) { o ->
            mode == o.mode && androidPermissions contentEquals o.androidPermissions
        }
    }

    override fun hashCode(): Int {
        var result = mode
        result = 31 * result + androidPermissions.contentHashCode()
        return result
    }

    override fun append(sb: StringBuilder) {
        sb.run {
            append("{mode=")
            append(
                when (mode) {
                    MODE_ANY -> "ANY"
                    MODE_EVERY -> "EVERY"

                    else -> ""
                }
            )

            append(", androidPermissions=")
            appendArray(androidPermissions)
            append('}')
        }
    }

    companion object {
        const val MODE_ANY = 0
        const val MODE_EVERY = 1

        @JvmField
        val CREATOR = object : Parcelable.Creator<ModePermissionArray> {
            override fun createFromParcel(source: Parcel) = ModePermissionArray(source)
            override fun newArray(size: Int) = arrayOfNulls<ModePermissionArray>(size)
        }

        fun anyOf(vararg androidPermissions: String): ModePermissionArray {
            return ModePermissionArray(MODE_ANY, androidPermissions)
        }

        fun everyOf(vararg androidPermissions: String): ModePermissionArray {
            return ModePermissionArray(MODE_EVERY, androidPermissions)
        }
    }
}

class RequestPermissionInfo : Parcelable, AppendableToStringBuilder {
    val id: Int

    @StringRes
    val userDescriptionId: Int

    @StringRes
    val whyTextId: Int
    val modePermissions: ModePermissionArray

    constructor(
        id: Int,
        @StringRes userDescriptionId: Int,
        @StringRes whyTextId: Int,
        permissions: ModePermissionArray
    ) {
        this.id = id
        this.userDescriptionId = userDescriptionId
        this.whyTextId = whyTextId
        this.modePermissions = permissions
    }

    private constructor(parcel: Parcel) {
        id = parcel.readInt()
        userDescriptionId = parcel.readInt()
        whyTextId = parcel.readInt()
        modePermissions = ModePermissionArray.CREATOR.createFromParcel(parcel)
    }

    @RequiresApi(23)
    fun request(activity: Activity, requestCode: Int) {
        modePermissions.request(activity, requestCode)
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeInt(userDescriptionId)
        dest.writeInt(whyTextId)
        modePermissions.writeToParcel(dest, 0)
    }

    override fun describeContents(): Int = 0

    override fun equals(other: Any?): Boolean {
        return equalsPattern(other) { o ->
            id == o.id &&
            userDescriptionId == o.userDescriptionId &&
            whyTextId == o.whyTextId &&
            modePermissions == o.modePermissions
        }
    }

    override fun hashCode(): Int {
        var result = userDescriptionId
        result = 31 * result + modePermissions.hashCode()
        result = 31 * result + whyTextId
        result = 31 * result + id

        return result
    }

    override fun append(sb: StringBuilder) {
        sb.run {
            append("{id=")
            append(id)
            append(", userDescriptionId=")
            append(userDescriptionId)
            append(", whyTextId=")
            append(whyTextId)
            append(", modePermissions=")
            modePermissions.append(this)
            append('}')
        }
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<RequestPermissionInfo> {
            override fun createFromParcel(source: Parcel) = RequestPermissionInfo(source)
            override fun newArray(size: Int) = arrayOfNulls<RequestPermissionInfo>(size)
        }
    }
}

class RequestPermissionsContext : Parcelable, Collection<RequestPermissionInfo>,
    AppendableToStringBuilder {
    private val permissions: Array<out RequestPermissionInfo>
    override val size: Int
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

    override fun contains(element: RequestPermissionInfo): Boolean {
        return permissions.contains(element)
    }

    override fun containsAll(elements: Collection<RequestPermissionInfo>): Boolean {
        return elements.all { contains(it) }
    }

    override fun isEmpty(): Boolean = permissions.isEmpty()
    override fun iterator(): Iterator<RequestPermissionInfo> = ArrayIterator(permissions)

    override fun equals(other: Any?): Boolean {
        return equalsPattern(other) { o ->
            permissions contentEquals o.permissions
        }
    }

    override fun hashCode(): Int = permissions.contentHashCode()

    override fun append(sb: StringBuilder) {
        sb.appendArray(permissions)
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(permissions.size)

        for (perm in permissions) {
            perm.writeToParcel(dest, 0)
        }
    }

    override fun describeContents(): Int = 0

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<RequestPermissionsContext> {
            override fun createFromParcel(source: Parcel) = RequestPermissionsContext(source)
            override fun newArray(size: Int) = arrayOfNulls<RequestPermissionsContext>(size)
        }
    }
}

class RequestPermissionsContextBuilder {
    object PermissionArrayModeSelect {
        fun anyOf(vararg permissions: String): ModePermissionArray {
            return ModePermissionArray(ModePermissionArray.MODE_ANY, permissions)
        }

        fun everyOf(vararg permissions: String): ModePermissionArray {
            return ModePermissionArray(ModePermissionArray.MODE_EVERY, permissions)
        }
    }

    @JvmField
    var _permissions = emptyArray<RequestPermissionInfo>()

    inline fun permission(
        id: Int,
        @StringRes userDescriptionId: Int,
        @StringRes whyTextId: Int,
        block: PermissionArrayModeSelect.() -> ModePermissionArray
    ) {
        val modeArray = block(PermissionArrayModeSelect)
        val perm = RequestPermissionInfo(id, userDescriptionId, whyTextId, modeArray)

        _permissions = _permissions.add(perm)
    }
}

inline fun RequestPermissionsContext(block: RequestPermissionsContextBuilder.() -> Unit): RequestPermissionsContext {
    val builder = RequestPermissionsContextBuilder()
    builder.block()

    return RequestPermissionsContext(builder._permissions)
}