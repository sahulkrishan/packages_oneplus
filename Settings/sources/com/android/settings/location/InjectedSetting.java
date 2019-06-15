package com.android.settings.location;

import android.content.Intent;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.annotations.Immutable;
import java.util.Objects;

@Immutable
class InjectedSetting {
    public final String className;
    public final int iconId;
    public final UserHandle mUserHandle;
    public final String packageName;
    public final String settingsActivity;
    public final String title;
    public final String userRestriction;

    public static class Builder {
        private String mClassName;
        private int mIconId;
        private String mPackageName;
        private String mSettingsActivity;
        private String mTitle;
        private UserHandle mUserHandle;
        private String mUserRestriction;

        public Builder setPackageName(String packageName) {
            this.mPackageName = packageName;
            return this;
        }

        public Builder setClassName(String className) {
            this.mClassName = className;
            return this;
        }

        public Builder setTitle(String title) {
            this.mTitle = title;
            return this;
        }

        public Builder setIconId(int iconId) {
            this.mIconId = iconId;
            return this;
        }

        public Builder setUserHandle(UserHandle userHandle) {
            this.mUserHandle = userHandle;
            return this;
        }

        public Builder setSettingsActivity(String settingsActivity) {
            this.mSettingsActivity = settingsActivity;
            return this;
        }

        public Builder setUserRestriction(String userRestriction) {
            this.mUserRestriction = userRestriction;
            return this;
        }

        public InjectedSetting build() {
            if (this.mPackageName != null && this.mClassName != null && !TextUtils.isEmpty(this.mTitle) && !TextUtils.isEmpty(this.mSettingsActivity)) {
                return new InjectedSetting(this);
            }
            if (Log.isLoggable("SettingsInjector", 5)) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Illegal setting specification: package=");
                stringBuilder.append(this.mPackageName);
                stringBuilder.append(", class=");
                stringBuilder.append(this.mClassName);
                stringBuilder.append(", title=");
                stringBuilder.append(this.mTitle);
                stringBuilder.append(", settingsActivity=");
                stringBuilder.append(this.mSettingsActivity);
                Log.w("SettingsInjector", stringBuilder.toString());
            }
            return null;
        }
    }

    private InjectedSetting(Builder builder) {
        this.packageName = builder.mPackageName;
        this.className = builder.mClassName;
        this.title = builder.mTitle;
        this.iconId = builder.mIconId;
        this.mUserHandle = builder.mUserHandle;
        this.settingsActivity = builder.mSettingsActivity;
        this.userRestriction = builder.mUserRestriction;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("InjectedSetting{mPackageName='");
        stringBuilder.append(this.packageName);
        stringBuilder.append('\'');
        stringBuilder.append(", mClassName='");
        stringBuilder.append(this.className);
        stringBuilder.append('\'');
        stringBuilder.append(", label=");
        stringBuilder.append(this.title);
        stringBuilder.append(", iconId=");
        stringBuilder.append(this.iconId);
        stringBuilder.append(", userId=");
        stringBuilder.append(this.mUserHandle.getIdentifier());
        stringBuilder.append(", settingsActivity='");
        stringBuilder.append(this.settingsActivity);
        stringBuilder.append('\'');
        stringBuilder.append(", userRestriction='");
        stringBuilder.append(this.userRestriction);
        stringBuilder.append('}');
        return stringBuilder.toString();
    }

    public Intent getServiceIntent() {
        Intent intent = new Intent();
        intent.setClassName(this.packageName, this.className);
        return intent;
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (!(o instanceof InjectedSetting)) {
            return false;
        }
        InjectedSetting that = (InjectedSetting) o;
        if (!(Objects.equals(this.packageName, that.packageName) && Objects.equals(this.className, that.className) && Objects.equals(this.title, that.title) && Objects.equals(Integer.valueOf(this.iconId), Integer.valueOf(that.iconId)) && Objects.equals(this.mUserHandle, that.mUserHandle) && Objects.equals(this.settingsActivity, that.settingsActivity) && Objects.equals(this.userRestriction, that.userRestriction))) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        int i = 0;
        int result = 31 * ((31 * ((31 * ((31 * ((31 * ((31 * this.packageName.hashCode()) + this.className.hashCode())) + this.title.hashCode())) + this.iconId)) + (this.mUserHandle == null ? 0 : this.mUserHandle.hashCode()))) + this.settingsActivity.hashCode());
        if (this.userRestriction != null) {
            i = this.userRestriction.hashCode();
        }
        return result + i;
    }
}
