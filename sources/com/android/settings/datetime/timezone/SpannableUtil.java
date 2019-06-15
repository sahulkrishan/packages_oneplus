package com.android.settings.datetime.timezone;

import android.content.res.Resources;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import java.util.Formatter;
import java.util.Locale;

public class SpannableUtil {
    public static Spannable getResourcesText(Resources res, int resId, Object... args) {
        Locale locale = res.getConfiguration().getLocales().get(0);
        SpannableStringBuilder builder = new SpannableStringBuilder();
        new Formatter(builder, locale).format(res.getString(resId), args);
        return builder;
    }
}
