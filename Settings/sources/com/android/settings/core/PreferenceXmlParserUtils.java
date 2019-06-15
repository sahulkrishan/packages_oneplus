package com.android.settings.core;

import android.content.Context;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.util.Xml;
import com.android.internal.R;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.xmlpull.v1.XmlPullParserException;

public class PreferenceXmlParserUtils {
    private static final String ENTRIES_SEPARATOR = "|";
    public static final String METADATA_CONTROLLER = "controller";
    public static final String METADATA_ICON = "icon";
    public static final String METADATA_KEY = "key";
    public static final String METADATA_KEYWORDS = "keywords";
    public static final String METADATA_PLATFORM_SLICE_FLAG = "platform_slice";
    public static final String METADATA_PREF_TYPE = "type";
    public static final String METADATA_SUMMARY = "summary";
    public static final String METADATA_TITLE = "title";
    @VisibleForTesting
    static final String PREF_SCREEN_TAG = "PreferenceScreen";
    private static final List<String> SUPPORTED_PREF_TYPES = Arrays.asList(new String[]{"Preference", "PreferenceCategory", PREF_SCREEN_TAG});
    private static final String TAG = "PreferenceXmlParserUtil";

    @Retention(RetentionPolicy.SOURCE)
    public @interface MetadataFlag {
        public static final int FLAG_INCLUDE_PREF_SCREEN = 1;
        public static final int FLAG_NEED_KEY = 2;
        public static final int FLAG_NEED_KEYWORDS = 256;
        public static final int FLAG_NEED_PLATFORM_SLICE_FLAG = 128;
        public static final int FLAG_NEED_PREF_CONTROLLER = 8;
        public static final int FLAG_NEED_PREF_ICON = 64;
        public static final int FLAG_NEED_PREF_SUMMARY = 32;
        public static final int FLAG_NEED_PREF_TITLE = 16;
        public static final int FLAG_NEED_PREF_TYPE = 4;
    }

    @Deprecated
    public static String getDataKey(Context context, AttributeSet attrs) {
        return getStringData(context, attrs, R.styleable.Preference, 6);
    }

    @Deprecated
    public static String getDataTitle(Context context, AttributeSet attrs) {
        return getStringData(context, attrs, R.styleable.Preference, 4);
    }

    @Deprecated
    public static String getDataSummary(Context context, AttributeSet attrs) {
        return getStringData(context, attrs, R.styleable.Preference, 7);
    }

    public static String getDataSummaryOn(Context context, AttributeSet attrs) {
        return getStringData(context, attrs, R.styleable.CheckBoxPreference, 0);
    }

    public static String getDataSummaryOff(Context context, AttributeSet attrs) {
        return getStringData(context, attrs, R.styleable.CheckBoxPreference, 1);
    }

    public static String getDataEntries(Context context, AttributeSet attrs) {
        return getDataEntries(context, attrs, R.styleable.ListPreference, 0);
    }

    public static String getDataKeywords(Context context, AttributeSet attrs) {
        return getStringData(context, attrs, com.android.settings.R.styleable.Preference, 27);
    }

    @Deprecated
    public static String getController(Context context, AttributeSet attrs) {
        return getStringData(context, attrs, com.android.settings.R.styleable.Preference, 18);
    }

    @Deprecated
    public static int getDataIcon(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.Preference);
        int dataIcon = ta.getResourceId(0, 0);
        ta.recycle();
        return dataIcon;
    }

    public static List<Bundle> extractMetadata(Context context, int xmlResId, int flags) throws IOException, XmlPullParserException {
        List<Bundle> metadata = new ArrayList();
        if (xmlResId <= 0) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(xmlResId);
            stringBuilder.append(" is invalid.");
            Log.d(str, stringBuilder.toString());
            return metadata;
        }
        int next;
        int type;
        XmlResourceParser parser = context.getResources().getXml(xmlResId);
        while (true) {
            next = parser.next();
            type = next;
            if (next == 1 || type == 2) {
                next = parser.getDepth();
            }
        }
        next = parser.getDepth();
        while (true) {
            if (type == 2) {
                String nodeName = parser.getName();
                if ((hasFlag(flags, 1) || !TextUtils.equals(PREF_SCREEN_TAG, nodeName)) && (SUPPORTED_PREF_TYPES.contains(nodeName) || nodeName.endsWith("Preference"))) {
                    Bundle preferenceMetadata = new Bundle();
                    TypedArray preferenceAttributes = context.obtainStyledAttributes(Xml.asAttributeSet(parser), com.android.settings.R.styleable.Preference);
                    if (hasFlag(flags, 4)) {
                        preferenceMetadata.putString("type", nodeName);
                    }
                    if (hasFlag(flags, 2)) {
                        preferenceMetadata.putString("key", getKey(preferenceAttributes));
                    }
                    if (hasFlag(flags, 8)) {
                        preferenceMetadata.putString("controller", getController(preferenceAttributes));
                    }
                    if (hasFlag(flags, 16)) {
                        preferenceMetadata.putString("title", getTitle(preferenceAttributes));
                    }
                    if (hasFlag(flags, 32)) {
                        preferenceMetadata.putString("summary", getSummary(preferenceAttributes));
                    }
                    if (hasFlag(flags, 64)) {
                        preferenceMetadata.putInt("icon", getIcon(preferenceAttributes));
                    }
                    if (hasFlag(flags, 128)) {
                        preferenceMetadata.putBoolean("platform_slice", getPlatformSlice(preferenceAttributes));
                    }
                    if (hasFlag(flags, 256)) {
                        preferenceMetadata.putString("keywords", getKeywords(preferenceAttributes));
                    }
                    metadata.add(preferenceMetadata);
                    preferenceAttributes.recycle();
                }
            }
            int next2 = parser.next();
            type = next2;
            if (next2 == 1 || (type == 3 && parser.getDepth() <= next)) {
                parser.close();
            }
        }
        parser.close();
        return metadata;
    }

    public static String getDataChildFragment(Context context, AttributeSet attrs) {
        return getStringData(context, attrs, com.android.settings.R.styleable.Preference, 13);
    }

    @Deprecated
    private static String getStringData(Context context, AttributeSet set, int[] attrs, int resId) {
        TypedArray ta = context.obtainStyledAttributes(set, attrs);
        String data = ta.getString(resId);
        ta.recycle();
        return data;
    }

    private static boolean hasFlag(int flags, int flag) {
        return (flags & flag) != 0;
    }

    private static String getDataEntries(Context context, AttributeSet set, int[] attrs, int resId) {
        TypedArray sa = context.obtainStyledAttributes(set, attrs);
        TypedValue tv = sa.peekValue(resId);
        sa.recycle();
        String[] data = null;
        if (!(tv == null || tv.type != 1 || tv.resourceId == 0)) {
            data = context.getResources().getStringArray(tv.resourceId);
        }
        int n = 0;
        int count = data == null ? 0 : data.length;
        if (count == 0) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        while (n < count) {
            result.append(data[n]);
            result.append(ENTRIES_SEPARATOR);
            n++;
        }
        return result.toString();
    }

    private static String getKey(TypedArray styledAttributes) {
        return styledAttributes.getString(6);
    }

    private static String getTitle(TypedArray styledAttributes) {
        return styledAttributes.getString(4);
    }

    private static String getSummary(TypedArray styledAttributes) {
        return styledAttributes.getString(7);
    }

    private static String getController(TypedArray styledAttributes) {
        return styledAttributes.getString(18);
    }

    private static int getIcon(TypedArray styledAttributes) {
        return styledAttributes.getResourceId(0, 0);
    }

    private static boolean getPlatformSlice(TypedArray styledAttributes) {
        return styledAttributes.getBoolean(32, false);
    }

    private static String getKeywords(TypedArray styleAttributes) {
        return styleAttributes.getString(27);
    }
}
