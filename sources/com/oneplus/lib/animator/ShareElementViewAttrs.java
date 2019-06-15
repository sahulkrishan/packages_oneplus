package com.oneplus.lib.animator;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class ShareElementViewAttrs implements Parcelable {
    public static final Creator<ShareElementViewAttrs> CREATOR = new Creator<ShareElementViewAttrs>() {
        public ShareElementViewAttrs createFromParcel(Parcel source) {
            return new ShareElementViewAttrs(source);
        }

        public ShareElementViewAttrs[] newArray(int size) {
            return new ShareElementViewAttrs[size];
        }
    };
    public float alpha;
    public float height;
    public int id;
    public float startX;
    public float startY;
    public float width;

    public ShareElementViewAttrs(int id, float startX, float startY, float width, float height) {
        this(id, startX, startY, width, height, 1.0f);
    }

    public ShareElementViewAttrs(int id, float startX, float startY, float width, float height, float alpha) {
        this.id = id;
        this.startX = startX;
        this.startY = startY;
        this.width = width;
        this.height = height;
        this.alpha = alpha;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeFloat(this.startX);
        dest.writeFloat(this.startY);
        dest.writeFloat(this.width);
        dest.writeFloat(this.height);
        dest.writeFloat(this.alpha);
    }

    protected ShareElementViewAttrs(Parcel in) {
        this.id = in.readInt();
        this.startX = in.readFloat();
        this.startY = in.readFloat();
        this.width = in.readFloat();
        this.height = in.readFloat();
        this.alpha = in.readFloat();
    }
}
