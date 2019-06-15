package androidx.slice;

import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import androidx.versionedparcelable.VersionedParcel;

@RestrictTo({Scope.LIBRARY})
public final class SliceSpecParcelizer {
    public static SliceSpec read(VersionedParcel parcel) {
        SliceSpec obj = new SliceSpec();
        obj.mType = parcel.readString(obj.mType, 1);
        obj.mRevision = parcel.readInt(obj.mRevision, 2);
        return obj;
    }

    public static void write(SliceSpec obj, VersionedParcel parcel) {
        parcel.setSerializationFlags(true, false);
        parcel.writeString(obj.mType, 1);
        parcel.writeInt(obj.mRevision, 2);
    }
}
