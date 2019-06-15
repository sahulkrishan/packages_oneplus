package com.android.settings.support;

import android.content.Intent;
import android.net.Uri.Builder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import java.text.ParseException;

public final class SupportPhone implements Parcelable {
    public static final Creator<SupportPhone> CREATOR = new Creator<SupportPhone>() {
        public SupportPhone createFromParcel(Parcel in) {
            return new SupportPhone(in);
        }

        public SupportPhone[] newArray(int size) {
            return new SupportPhone[size];
        }
    };
    public final boolean isTollFree;
    public final String language;
    public final String number;

    public SupportPhone(String config) throws ParseException {
        String[] tokens = config.split(":");
        if (tokens.length == 3) {
            this.language = tokens[0];
            this.isTollFree = TextUtils.equals(tokens[1], "tollfree");
            this.number = tokens[2];
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Phone config is invalid ");
        stringBuilder.append(config);
        throw new ParseException(stringBuilder.toString(), 0);
    }

    protected SupportPhone(Parcel in) {
        this.language = in.readString();
        this.number = in.readString();
        this.isTollFree = in.readInt() != 0;
    }

    public Intent getDialIntent() {
        return new Intent("android.intent.action.DIAL").setData(new Builder().scheme("tel").appendPath(this.number).build());
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.language);
        dest.writeString(this.number);
        dest.writeInt(this.isTollFree);
    }
}
