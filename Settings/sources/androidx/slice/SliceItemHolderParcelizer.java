package androidx.slice;

import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import androidx.versionedparcelable.VersionedParcel;

@RestrictTo({Scope.LIBRARY})
public final class SliceItemHolderParcelizer {
    public static SliceItemHolder read(VersionedParcel parcel) {
        SliceItemHolder obj = new SliceItemHolder();
        obj.mVersionedParcelable = parcel.readVersionedParcelable(obj.mVersionedParcelable, 1);
        obj.mParcelable = parcel.readParcelable(obj.mParcelable, 2);
        obj.mStr = parcel.readString(obj.mStr, 3);
        obj.mInt = parcel.readInt(obj.mInt, 4);
        obj.mLong = parcel.readLong(obj.mLong, 5);
        return obj;
    }

    public static void write(SliceItemHolder obj, VersionedParcel parcel) {
        parcel.setSerializationFlags(true, true);
        parcel.writeVersionedParcelable(obj.mVersionedParcelable, 1);
        parcel.writeParcelable(obj.mParcelable, 2);
        parcel.writeString(obj.mStr, 3);
        parcel.writeInt(obj.mInt, 4);
        parcel.writeLong(obj.mLong, 5);
    }
}
