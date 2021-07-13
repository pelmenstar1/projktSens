package com.pelmenstar.projktSens.shared.android;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.StringRes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class allows to get {@link String} from exact instance or string resource.
 */
public final class StringOrResource implements Parcelable {
    @StringRes
    private final int resource;

    @Nullable
    private final String str;

    public StringOrResource(@StringRes int res) {
        if(res == -1) {
            throw new IllegalArgumentException("res was -1 which is forbidden value");
        }

        resource = res;
        str = null;
    }

    public StringOrResource(@NotNull String str) {
        this.str = str;
        resource = -1;
    }

    private StringOrResource(@NotNull Parcel parcel) {
        if(parcel.readInt() == 0) {
            str = parcel.readString();
            resource = -1;
        } else {
            resource = parcel.readInt();
            str = null;
        }
    }

    /**
     * Returns exact {@link String} instance passed in {@link StringOrResource#StringOrResource(String)}.
     * If there is no {@link String} instance, returns null.
     */
    @Nullable
    public String getString() {
        return str;
    }

    /**
     * Returns string resource if exists, otherwise, -1
     */
    @StringRes
    public int getResource() {
        return resource;
    }

    /**
     * Returns {@link String} instance from value passed to {@link StringOrResource#StringOrResource(String)}.
     * If there is no exact {@link String} instance, takes {@link String} from resources using string resource.
     *
     * @param context takes {@link android.content.res.Resources} from this context to
     *                retrieve actual value of string resource.
     */
    @NotNull
    public String getValue(@NotNull Context context) {
        if(str != null) {
            return str;
        } else {
            return context.getResources().getString(resource);
        }
    }

    @Override
    public void writeToParcel(@NotNull Parcel dest, int flags) {
        if(str != null) {
            dest.writeInt(0);
            dest.writeString(str);
        } else {
            dest.writeInt(1);
            dest.writeInt(resource);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        StringOrResource o = (StringOrResource) other;

        if(resource == -1 && o.resource == -1) {
            // if in both objects resource == -1, then str in both objects can't be null
            //noinspection ConstantConditions
            return str.equals(o.str);
        } else {
            return resource == o.resource;
        }
    }

    @Override
    public int hashCode() {
        int result = resource;
        result = 31 * result + (str != null ? str.hashCode() : 0);
        return result;
    }

    @Override
    @NotNull
    public String toString() {
        if(str != null) {
            return "{string=" + str + '}';
        } else {
            return "{resource=" + resource + '}';
        }
    }

    @NotNull
    public static final Creator<StringOrResource> CREATOR = new Creator<StringOrResource>() {
        @Override
        @NotNull
        public StringOrResource createFromParcel(Parcel in) {
            return new StringOrResource(in);
        }

        @Override
        @NotNull
        public StringOrResource @NotNull [] newArray(int size) {
            return new StringOrResource[size];
        }
    };
}
