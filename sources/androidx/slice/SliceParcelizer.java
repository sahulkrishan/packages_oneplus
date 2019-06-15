package androidx.slice;

import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import androidx.versionedparcelable.VersionedParcel;

@RestrictTo({Scope.LIBRARY})
public final class SliceParcelizer {
    public static Slice read(VersionedParcel parcel) {
        Slice obj = new Slice();
        obj.mSpec = (SliceSpec) parcel.readVersionedParcelable(obj.mSpec, 1);
        obj.mItems = (SliceItem[]) parcel.readArray(obj.mItems, 2);
        obj.mHints = (String[]) parcel.readArray(obj.mHints, 3);
        obj.mUri = parcel.readString(obj.mUri, 4);
        return obj;
    }

    public static void write(Slice obj, VersionedParcel parcel) {
        parcel.setSerializationFlags(true, false);
        parcel.writeVersionedParcelable(obj.mSpec, 1);
        parcel.writeArray(obj.mItems, 2);
        parcel.writeArray(obj.mHints, 3);
        parcel.writeString(obj.mUri, 4);
    }
}
