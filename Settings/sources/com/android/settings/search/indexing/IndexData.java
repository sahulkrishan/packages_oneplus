package com.android.settings.search.indexing;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import com.android.settings.SettingsActivity;
import com.android.settings.search.DatabaseIndexingUtils;
import com.android.settings.search.ResultPayload;
import com.android.settings.search.ResultPayloadUtils;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public class IndexData {
    private static final String EMPTY = "";
    private static final String HYPHEN = "-";
    private static final String LIST_DELIMITERS = "[,]\\s*";
    private static final String NON_BREAKING_HYPHEN = "‑";
    private static final Pattern REMOVE_DIACRITICALS_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    private static final String SPACE = " ";
    public final String childClassName;
    public final String className;
    public final boolean enabled;
    public final String entries;
    public final int iconResId;
    public final String intentAction;
    public final String intentTargetClass;
    public final String intentTargetPackage;
    public final String key;
    public final String locale;
    public final String normalizedSummaryOn;
    public final String normalizedTitle;
    public final byte[] payload;
    public final int payloadType;
    public final String screenTitle;
    public final String spaceDelimitedKeywords;
    public final String updatedSummaryOn;
    public final String updatedTitle;
    public final int userId;

    public static class Builder {
        private String mChildClassName;
        private String mClassName;
        private boolean mEnabled;
        private String mEntries;
        private int mIconResId;
        private String mIntentAction;
        private String mIntentTargetClass;
        private String mIntentTargetPackage;
        private String mKey;
        private String mKeywords;
        private ResultPayload mPayload;
        private int mPayloadType;
        private String mScreenTitle;
        private String mSummaryOn;
        private String mTitle;
        private int mUserId;

        public Builder setTitle(String title) {
            this.mTitle = title;
            return this;
        }

        public Builder setSummaryOn(String summaryOn) {
            this.mSummaryOn = summaryOn;
            return this;
        }

        public Builder setEntries(String entries) {
            this.mEntries = entries;
            return this;
        }

        public Builder setClassName(String className) {
            this.mClassName = className;
            return this;
        }

        public Builder setChildClassName(String childClassName) {
            this.mChildClassName = childClassName;
            return this;
        }

        public Builder setScreenTitle(String screenTitle) {
            this.mScreenTitle = screenTitle;
            return this;
        }

        public Builder setIconResId(int iconResId) {
            this.mIconResId = iconResId;
            return this;
        }

        public Builder setKeywords(String keywords) {
            this.mKeywords = keywords;
            return this;
        }

        public Builder setIntentAction(String intentAction) {
            this.mIntentAction = intentAction;
            return this;
        }

        public Builder setIntentTargetPackage(String intentTargetPackage) {
            this.mIntentTargetPackage = intentTargetPackage;
            return this;
        }

        public Builder setIntentTargetClass(String intentTargetClass) {
            this.mIntentTargetClass = intentTargetClass;
            return this;
        }

        public Builder setEnabled(boolean enabled) {
            this.mEnabled = enabled;
            return this;
        }

        public Builder setKey(String key) {
            this.mKey = key;
            return this;
        }

        public Builder setUserId(int userId) {
            this.mUserId = userId;
            return this;
        }

        public Builder setPayload(ResultPayload payload) {
            this.mPayload = payload;
            if (this.mPayload != null) {
                setPayloadType(this.mPayload.getType());
            }
            return this;
        }

        private Builder setPayloadType(int payloadType) {
            this.mPayloadType = payloadType;
            return this;
        }

        private void setIntent(Context context) {
            if (this.mPayload == null) {
                this.mPayload = new ResultPayload(buildIntent(context));
                this.mPayloadType = 0;
            }
        }

        private Intent buildIntent(Context context) {
            if (TextUtils.isEmpty(this.mIntentAction)) {
                return DatabaseIndexingUtils.buildSearchResultPageIntent(context, this.mClassName, this.mKey, this.mScreenTitle);
            }
            Intent intent = new Intent(this.mIntentAction);
            String targetClass = this.mIntentTargetClass;
            if (!(TextUtils.isEmpty(this.mIntentTargetPackage) || TextUtils.isEmpty(targetClass))) {
                intent.setComponent(new ComponentName(this.mIntentTargetPackage, targetClass));
            }
            intent.putExtra(SettingsActivity.EXTRA_FRAGMENT_ARG_KEY, this.mKey);
            return intent;
        }

        public IndexData build(Context context) {
            setIntent(context);
            return new IndexData(this);
        }
    }

    private IndexData(Builder builder) {
        byte[] marshall;
        this.locale = Locale.getDefault().toString();
        this.updatedTitle = normalizeHyphen(builder.mTitle);
        this.updatedSummaryOn = normalizeHyphen(builder.mSummaryOn);
        if (Locale.JAPAN.toString().equalsIgnoreCase(this.locale)) {
            this.normalizedTitle = normalizeJapaneseString(builder.mTitle);
            this.normalizedSummaryOn = normalizeJapaneseString(builder.mSummaryOn);
        } else {
            this.normalizedTitle = normalizeString(builder.mTitle);
            this.normalizedSummaryOn = normalizeString(builder.mSummaryOn);
        }
        this.entries = builder.mEntries;
        this.className = builder.mClassName;
        this.childClassName = builder.mChildClassName;
        this.screenTitle = builder.mScreenTitle;
        this.iconResId = builder.mIconResId;
        this.spaceDelimitedKeywords = normalizeKeywords(builder.mKeywords);
        this.intentAction = builder.mIntentAction;
        this.intentTargetPackage = builder.mIntentTargetPackage;
        this.intentTargetClass = builder.mIntentTargetClass;
        this.enabled = builder.mEnabled;
        this.key = builder.mKey;
        this.userId = builder.mUserId;
        this.payloadType = builder.mPayloadType;
        if (builder.mPayload != null) {
            marshall = ResultPayloadUtils.marshall(builder.mPayload);
        } else {
            marshall = null;
        }
        this.payload = marshall;
    }

    public int getDocId() {
        if (!TextUtils.isEmpty(this.key)) {
            return this.key.hashCode();
        }
        return Objects.hash(new Object[]{this.updatedTitle, this.className, this.screenTitle, this.intentTargetClass});
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(this.updatedTitle);
        stringBuilder.append(": ");
        stringBuilder.append(this.updatedSummaryOn);
        return stringBuilder.toString();
    }

    public static String normalizeKeywords(String input) {
        return input != null ? input.replaceAll(LIST_DELIMITERS, SPACE) : "";
    }

    public static String normalizeHyphen(String input) {
        return input != null ? input.replaceAll(NON_BREAKING_HYPHEN, HYPHEN) : "";
    }

    public static String normalizeString(String input) {
        return REMOVE_DIACRITICALS_PATTERN.matcher(Normalizer.normalize(input != null ? normalizeHyphen(input).replaceAll(HYPHEN, "") : "", Form.NFD)).replaceAll("").toLowerCase();
    }

    public static String normalizeJapaneseString(String input) {
        String normalized = Normalizer.normalize(input != null ? input.replaceAll(HYPHEN, "") : "", Form.NFKD);
        StringBuffer sb = new StringBuffer();
        int length = normalized.length();
        for (int i = 0; i < length; i++) {
            char c = normalized.charAt(i);
            if (c < 12353 || c > 12438) {
                sb.append(c);
            } else {
                sb.append((char) ((c - 12353) + 12449));
            }
        }
        return REMOVE_DIACRITICALS_PATTERN.matcher(sb.toString()).replaceAll("").toLowerCase();
    }
}
