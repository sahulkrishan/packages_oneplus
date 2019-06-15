package com.android.settings.slices;

import android.net.Uri;
import android.text.TextUtils;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class SliceData {
    private final String mFragmentClassName;
    private final int mIconResource;
    private final boolean mIsPlatformDefined;
    private final String mKey;
    private final String mKeywords;
    private final String mPreferenceController;
    private final CharSequence mScreenTitle;
    private final int mSliceType;
    private final String mSummary;
    private final String mTitle;
    private final Uri mUri;

    static class Builder {
        private String mFragmentClassName;
        private int mIconResource;
        private boolean mIsPlatformDefined;
        private String mKey;
        private String mKeywords;
        private String mPrefControllerClassName;
        private CharSequence mScreenTitle;
        private int mSliceType;
        private String mSummary;
        private String mTitle;
        private Uri mUri;

        Builder() {
        }

        public Builder setKey(String key) {
            this.mKey = key;
            return this;
        }

        public Builder setTitle(String title) {
            this.mTitle = title;
            return this;
        }

        public Builder setSummary(String summary) {
            this.mSummary = summary;
            return this;
        }

        public Builder setScreenTitle(CharSequence screenTitle) {
            this.mScreenTitle = screenTitle;
            return this;
        }

        public Builder setKeywords(String keywords) {
            this.mKeywords = keywords;
            return this;
        }

        public Builder setIcon(int iconResource) {
            this.mIconResource = iconResource;
            return this;
        }

        public Builder setPreferenceControllerClassName(String controllerClassName) {
            this.mPrefControllerClassName = controllerClassName;
            return this;
        }

        public Builder setFragmentName(String fragmentClassName) {
            this.mFragmentClassName = fragmentClassName;
            return this;
        }

        public Builder setUri(Uri uri) {
            this.mUri = uri;
            return this;
        }

        public Builder setSliceType(int sliceType) {
            this.mSliceType = sliceType;
            return this;
        }

        public Builder setPlatformDefined(boolean isPlatformDefined) {
            this.mIsPlatformDefined = isPlatformDefined;
            return this;
        }

        public SliceData build() {
            if (TextUtils.isEmpty(this.mKey)) {
                throw new InvalidSliceDataException("Key cannot be empty");
            } else if (TextUtils.isEmpty(this.mTitle)) {
                throw new InvalidSliceDataException("Title cannot be empty");
            } else if (TextUtils.isEmpty(this.mFragmentClassName)) {
                throw new InvalidSliceDataException("Fragment Name cannot be empty");
            } else if (!TextUtils.isEmpty(this.mPrefControllerClassName)) {
                return new SliceData(this);
            } else {
                throw new InvalidSliceDataException("Preference Controller cannot be empty");
            }
        }

        public String getKey() {
            return this.mKey;
        }
    }

    public static class InvalidSliceDataException extends RuntimeException {
        public InvalidSliceDataException(String message) {
            super(message);
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface SliceType {
        public static final int INTENT = 0;
        public static final int SLIDER = 2;
        public static final int SWITCH = 1;
    }

    public String getKey() {
        return this.mKey;
    }

    public String getTitle() {
        return this.mTitle;
    }

    public String getSummary() {
        return this.mSummary;
    }

    public CharSequence getScreenTitle() {
        return this.mScreenTitle;
    }

    public String getKeywords() {
        return this.mKeywords;
    }

    public int getIconResource() {
        return this.mIconResource;
    }

    public String getFragmentClassName() {
        return this.mFragmentClassName;
    }

    public Uri getUri() {
        return this.mUri;
    }

    public String getPreferenceController() {
        return this.mPreferenceController;
    }

    public int getSliceType() {
        return this.mSliceType;
    }

    public boolean isPlatformDefined() {
        return this.mIsPlatformDefined;
    }

    private SliceData(Builder builder) {
        this.mKey = builder.mKey;
        this.mTitle = builder.mTitle;
        this.mSummary = builder.mSummary;
        this.mScreenTitle = builder.mScreenTitle;
        this.mKeywords = builder.mKeywords;
        this.mIconResource = builder.mIconResource;
        this.mFragmentClassName = builder.mFragmentClassName;
        this.mUri = builder.mUri;
        this.mPreferenceController = builder.mPrefControllerClassName;
        this.mSliceType = builder.mSliceType;
        this.mIsPlatformDefined = builder.mIsPlatformDefined;
    }

    public int hashCode() {
        return this.mKey.hashCode();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof SliceData)) {
            return false;
        }
        return TextUtils.equals(this.mKey, ((SliceData) obj).mKey);
    }
}
