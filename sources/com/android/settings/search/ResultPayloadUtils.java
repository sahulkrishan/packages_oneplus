package com.android.settings.search;

import android.os.Parcel;
import android.os.Parcelable.Creator;

public class ResultPayloadUtils {
    private static final String TAG = "PayloadUtil";

    public static byte[] marshall(ResultPayload payload) {
        Parcel parcel = Parcel.obtain();
        payload.writeToParcel(parcel, 0);
        byte[] bytes = parcel.marshall();
        parcel.recycle();
        return bytes;
    }

    public static <T> T unmarshall(byte[] bytes, Creator<T> creator) {
        Parcel parcel = unmarshall(bytes);
        T result = creator.createFromParcel(parcel);
        parcel.recycle();
        return result;
    }

    private static Parcel unmarshall(byte[] bytes) {
        Parcel parcel = Parcel.obtain();
        parcel.unmarshall(bytes, 0, bytes.length);
        parcel.setDataPosition(0);
        return parcel;
    }
}
