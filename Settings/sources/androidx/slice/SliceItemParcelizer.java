package androidx.slice;

import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import androidx.versionedparcelable.VersionedParcel;

@RestrictTo({Scope.LIBRARY})
public final class SliceItemParcelizer {
    public static SliceItem read(VersionedParcel parcel) {
        SliceItem obj = new SliceItem();
        obj.mHints = (String[]) parcel.readArray(obj.mHints, 1);
        obj.mFormat = parcel.readString(obj.mFormat, 2);
        obj.mSubType = parcel.readString(obj.mSubType, 3);
        obj.mHolder = (SliceItemHolder) parcel.readVersionedParcelable(obj.mHolder, 4);
        obj.onPostParceling();
        return obj;
    }

    public static void write(SliceItem obj, VersionedParcel parcel) {
        parcel.setSerializationFlags(true, true);
        obj.onPreParceling(parcel.isStream());
        parcel.writeArray(obj.mHints, 1);
        parcel.writeString(obj.mFormat, 2);
        parcel.writeString(obj.mSubType, 3);
        parcel.writeVersionedParcelable(obj.mHolder, 4);
    }
}
