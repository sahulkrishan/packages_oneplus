package com.oneplus.settings.better;

import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class OPAppModel implements Parcelable {
    public static final Creator<OPAppModel> CREATOR = new Creator<OPAppModel>() {
        public OPAppModel createFromParcel(Parcel in) {
            return new OPAppModel(in);
        }

        public OPAppModel[] newArray(int size) {
            return new OPAppModel[size];
        }
    };
    private Drawable appIcon;
    private String appLabel;
    private boolean editMode;
    private boolean isGameAPP;
    private boolean isSelected;
    private String label;
    private int lockMode;
    private String pkgName;
    private Drawable shortCutIcon;
    private String shortCutId;
    private int type;
    private int uid;

    public OPAppModel(String pkgName, String label, String shortCutId, int uid, boolean isSelected) {
        this.pkgName = pkgName;
        this.label = label;
        this.shortCutId = shortCutId;
        this.uid = uid;
        this.isSelected = isSelected;
    }

    public boolean isEditMode() {
        return this.editMode;
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }

    public boolean isGameAPP() {
        return this.isGameAPP;
    }

    public void setGameAPP(boolean isGameAPP) {
        this.isGameAPP = isGameAPP;
    }

    public boolean isSelected() {
        return this.isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public String getShortCutId() {
        return this.shortCutId;
    }

    public void setShortCutId(String shortCutId) {
        this.shortCutId = shortCutId;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getPkgName() {
        return this.pkgName;
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getAppLabel() {
        return this.appLabel;
    }

    public void setAppLabel(String appLabel) {
        this.appLabel = appLabel;
    }

    public Drawable getAppIcon() {
        return this.appIcon;
    }

    public void setAppIcon(Drawable appIcon) {
        this.appIcon = appIcon;
    }

    public Drawable getShortCutIcon() {
        return this.shortCutIcon;
    }

    public void setShortCutIcon(Drawable shortCutIcon) {
        this.shortCutIcon = shortCutIcon;
    }

    public int getUid() {
        return this.uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getLockMode() {
        return this.lockMode;
    }

    public void setLockMode(int lockMode) {
        this.lockMode = lockMode;
    }

    public static Creator<OPAppModel> getCreator() {
        return CREATOR;
    }

    public OPAppModel(Parcel in) {
        readFromParcel(in);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.pkgName);
        out.writeString(this.label);
        out.writeInt(this.uid);
        out.writeInt(this.lockMode);
        out.writeInt(this.isSelected);
        out.writeInt(this.isGameAPP);
    }

    public void readFromParcel(Parcel in) {
        this.pkgName = in.readString();
        this.pkgName = in.readString();
        this.uid = in.readInt();
        this.lockMode = in.readInt();
        boolean z = false;
        this.isSelected = in.readInt() == 1;
        if (in.readInt() == 1) {
            z = true;
        }
        this.isGameAPP = z;
    }
}
