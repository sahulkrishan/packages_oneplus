package com.android.settings.display;

import android.content.Context;
import android.text.BidiFormatter;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Slog;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout.LayoutParams;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settingslib.CustomEditTextPreference;
import com.android.settingslib.display.DisplayDensityUtils;
import java.text.NumberFormat;

public class DensityPreference extends CustomEditTextPreference {
    private static final String TAG = "DensityPreference";

    public DensityPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void onAttached() {
        super.onAttached();
        CharSequence dpValue = BidiFormatter.getInstance().unicodeWrap(NumberFormat.getInstance().format((long) getCurrentSwDp()));
        setSummary((CharSequence) getContext().getString(R.string.density_pixel_summary, new Object[]{dpValue}));
    }

    private int getCurrentSwDp() {
        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        return (int) (((float) Math.min(metrics.widthPixels, metrics.heightPixels)) / metrics.density);
    }

    /* Access modifiers changed, original: protected */
    public void onBindDialogView(View view) {
        super.onBindDialogView(view);
        EditText editText = (EditText) view.findViewById(16908291);
        LayoutParams etParam = new LayoutParams(-1, -1);
        etParam.setMargins(8, 0, 0, 8);
        editText.setLayoutParams(etParam);
        if (editText != null) {
            editText.setInputType(2);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(getCurrentSwDp());
            stringBuilder.append("");
            editText.setText(stringBuilder.toString());
            Utils.setEditTextCursorPosition(editText);
        }
    }

    /* Access modifiers changed, original: protected */
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            try {
                DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
                DisplayDensityUtils.setForcedDisplayDensity(0, Math.max((160 * Math.min(metrics.widthPixels, metrics.heightPixels)) / Math.max(Integer.parseInt(getText()), 320), 120));
            } catch (Exception e) {
                Slog.e(TAG, "Couldn't save density", e);
            }
        }
    }
}
