package com.oneplus.settings.gestures;

import android.graphics.drawable.Drawable;

public class OPGestureAppModel {
    private String ShortCutId;
    private Drawable appIcon;
    private String pkgName;
    private String title;
    private int uid;

    public OPGestureAppModel(String pkgName, String label, String shortCutId, int uid) {
        this.pkgName = pkgName;
        this.title = label;
        this.ShortCutId = shortCutId;
        this.uid = uid;
    }

    public Drawable getAppIcon() {
        return this.appIcon;
    }

    public void setAppIcon(Drawable appIcon) {
        this.appIcon = appIcon;
    }

    public String getPkgName() {
        return this.pkgName;
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getUid() {
        return this.uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getShortCutId() {
        return this.ShortCutId;
    }

    public void setShortCutId(String shortCutId) {
        this.ShortCutId = shortCutId;
    }
}
