package com.oneplus.lib.widget;

import android.annotation.TargetApi;
import android.icu.text.DecimalFormatSymbols;
import java.util.Locale;

@TargetApi(24)
public final class TimePickerCompat24 {
    private TimePickerCompat24() {
    }

    public static void setHourFormat(TextInputTimePickerView textInputPickerView, Locale locale) {
        char[] digits = DecimalFormatSymbols.getInstance(locale).getDigits();
        int maxCharLength = 0;
        for (int i = 0; i < 10; i++) {
            maxCharLength = Math.max(maxCharLength, String.valueOf(digits[i]).length());
        }
        textInputPickerView.setHourFormat(maxCharLength * 2);
    }
}
