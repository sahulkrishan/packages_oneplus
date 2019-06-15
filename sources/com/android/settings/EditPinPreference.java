package com.android.settings;

import android.app.Dialog;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout.LayoutParams;
import com.android.settingslib.CustomEditTextPreference;

class EditPinPreference extends CustomEditTextPreference {
    private OnPinEnteredListener mPinListener;

    interface OnPinEnteredListener {
        void onPinEntered(EditPinPreference editPinPreference, boolean z);
    }

    public EditPinPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditPinPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setOnPinEnteredListener(OnPinEnteredListener listener) {
        this.mPinListener = listener;
    }

    /* Access modifiers changed, original: protected */
    public void onBindDialogView(View view) {
        super.onBindDialogView(view);
        EditText editText = (EditText) view.findViewById(16908291);
        LayoutParams etParam = new LayoutParams(-1, -1);
        etParam.setMargins(8, 0, 0, 8);
        editText.setLayoutParams(etParam);
        if (editText != null) {
            editText.setInputType(18);
            editText.setTextAlignment(5);
        }
    }

    public boolean isDialogOpen() {
        Dialog dialog = getDialog();
        return dialog != null && dialog.isShowing();
    }

    /* Access modifiers changed, original: protected */
    public void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (this.mPinListener != null) {
            this.mPinListener.onPinEntered(this, positiveResult);
        }
    }

    public void showPinDialog() {
        Dialog dialog = getDialog();
        if (dialog == null || !dialog.isShowing()) {
            onClick();
        }
    }
}
