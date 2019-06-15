package com.android.settings.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.android.settingslib.CustomEditTextPreference;

public class ValidatedEditTextPreference extends CustomEditTextPreference {
    private boolean mIsPassword;
    private boolean mIsSummaryPassword;
    private final EditTextWatcher mTextWatcher = new EditTextWatcher();
    private Validator mValidator;

    private class EditTextWatcher implements TextWatcher {
        private EditTextWatcher() {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        public void beforeTextChanged(CharSequence s, int start, int before, int count) {
        }

        public void afterTextChanged(Editable s) {
            EditText editText = ValidatedEditTextPreference.this.getEditText();
            if (ValidatedEditTextPreference.this.mValidator != null && editText != null) {
                AlertDialog dialog = (AlertDialog) ValidatedEditTextPreference.this.getDialog();
                dialog.getButton(-1).setEnabled(ValidatedEditTextPreference.this.mValidator.isTextValid(editText.getText().toString()));
            }
        }
    }

    public interface Validator {
        boolean isTextValid(String str);
    }

    public ValidatedEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public ValidatedEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ValidatedEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ValidatedEditTextPreference(Context context) {
        super(context);
    }

    /* Access modifiers changed, original: protected */
    public void onBindDialogView(View view) {
        super.onBindDialogView(view);
        EditText editText = (EditText) view.findViewById(16908291);
        LayoutParams etParam = new LayoutParams(-1, -1);
        etParam.setMargins(8, 0, 0, 8);
        editText.setLayoutParams(etParam);
        if (!(editText == null || TextUtils.isEmpty(editText.getText()))) {
            editText.setSelection(editText.getText().length());
        }
        if (this.mValidator != null && editText != null) {
            editText.removeTextChangedListener(this.mTextWatcher);
            if (this.mIsPassword) {
                editText.setInputType(Const.CODE_C1_SPC);
                editText.setMaxLines(1);
            }
            editText.addTextChangedListener(this.mTextWatcher);
        }
    }

    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        TextView textView = (TextView) holder.findViewById(16908304);
        if (textView != null) {
            if (this.mIsSummaryPassword) {
                textView.setInputType(129);
            } else {
                textView.setInputType(1);
            }
        }
    }

    public void setIsPassword(boolean isPassword) {
        this.mIsPassword = isPassword;
    }

    public void setIsSummaryPassword(boolean isPassword) {
        this.mIsSummaryPassword = isPassword;
    }

    @VisibleForTesting(otherwise = 5)
    public boolean isPassword() {
        return this.mIsPassword;
    }

    public void setValidator(Validator validator) {
        this.mValidator = validator;
    }
}
