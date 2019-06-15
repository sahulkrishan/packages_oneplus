package com.oneplus.lib.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewCompat;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.oneplus.commonctrl.R;
import com.oneplus.lib.util.AnimatorUtils;
import com.oneplus.lib.util.MathUtils;
import com.oneplus.lib.util.SystemUtils;

public class TextInputTimePickerView extends RelativeLayout {
    private static final int AM = 0;
    public static final int AMPM = 2;
    public static final int HOURS = 0;
    public static final int MINUTES = 1;
    private static final int PM = 1;
    private static final int STATE_ACTIVE = 0;
    private static final int STATE_NORAML = 1;
    private final RadioButton mAmLabel;
    private final RadioGroup mAmPmGroup;
    private final LinearLayout mAmPmParent;
    private final OnClickListener mClickListener;
    private final TextView mErrorLabel;
    private boolean mErrorShowing;
    private final TextView mHeaderLabel;
    private final EditText mHourEditText;
    private boolean mHourFormatStartsAtZero;
    private final TextView mHourLabel;
    private final View mInputBlock;
    private boolean mIs24Hour;
    private boolean mIsAmPmAtStart;
    private int mLabelAlphaDuration;
    private OnValueTypedListener mListener;
    private final EditText mMinuteEditText;
    private final TextView mMinuteLabel;
    private final RadioButton mPmLabel;
    private int[] mTimeColorStates;
    private int[] mTimeLabelColorStates;

    interface OnValueTypedListener {
        void onValueChanged(int i, int i2);
    }

    public TextInputTimePickerView(Context context) {
        this(context, null);
    }

    public TextInputTimePickerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TextInputTimePickerView(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs, defStyle, 0);
    }

    public TextInputTimePickerView(Context context, AttributeSet attrs, int defStyle, int defStyleRes) {
        super(context, attrs, defStyle, defStyleRes);
        this.mTimeColorStates = new int[2];
        this.mTimeLabelColorStates = new int[2];
        this.mClickListener = new OnClickListener() {
            public void onClick(View v) {
                int id = v.getId();
                if (id == R.id.am_label2) {
                    TextInputTimePickerView.this.updateAmPmLabel(true);
                    TextInputTimePickerView.this.mListener.onValueChanged(2, 0);
                } else if (id == R.id.pm_label2) {
                    TextInputTimePickerView.this.updateAmPmLabel(false);
                    TextInputTimePickerView.this.mListener.onValueChanged(2, 1);
                }
            }
        };
        LayoutInflater.from(context).inflate(R.layout.time_picker_text_input_material, this, true);
        this.mAmPmParent = (LinearLayout) findViewById(R.id.input_am_pm_parent);
        this.mInputBlock = findViewById(R.id.input_block);
        this.mHourEditText = (EditText) findViewById(R.id.input_hour);
        this.mMinuteEditText = (EditText) findViewById(R.id.input_minute);
        this.mHeaderLabel = (TextView) findViewById(R.id.top_label);
        this.mErrorLabel = (TextView) findViewById(R.id.label_error);
        this.mHourLabel = (TextView) findViewById(R.id.label_hour);
        this.mMinuteLabel = (TextView) findViewById(R.id.label_minute);
        int[] ATTRS_LABEL = new int[]{R.attr.opPickerColorUnActivated, R.attr.opPickerInputLabelUnActivated};
        TypedArray a = context.obtainStyledAttributes(attrs, new int[]{R.attr.opPickerColorActivated, R.attr.opPickerColorUnActivated});
        this.mLabelAlphaDuration = context.getResources().getInteger(R.integer.oneplus_contorl_time_part6);
        this.mTimeColorStates[0] = a.getColor(0, ViewCompat.MEASURED_STATE_MASK);
        this.mTimeColorStates[1] = a.getColor(1, ViewCompat.MEASURED_STATE_MASK);
        a.recycle();
        a = context.obtainStyledAttributes(attrs, ATTRS_LABEL);
        this.mTimeLabelColorStates[0] = a.getColor(0, ViewCompat.MEASURED_STATE_MASK);
        this.mTimeLabelColorStates[1] = a.getColor(1, ViewCompat.MEASURED_STATE_MASK);
        a.recycle();
        this.mHourEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    TextInputTimePickerView.this.mHourEditText.setActivated(true);
                    TextInputTimePickerView.this.mMinuteEditText.setActivated(false);
                    TextInputTimePickerView.this.resetInputTimeTextAppearance(R.style.OPTextAppearance_Material_TimePicker_InputField, TextInputTimePickerView.this.mHourEditText);
                    TextInputTimePickerView.this.resetInputTimeTextAppearance(R.style.OPTextAppearance_Material_TimePicker_InputFieldUnActive, TextInputTimePickerView.this.mMinuteEditText);
                    TextInputTimePickerView.this.mHourEditText.setTextColor(TextInputTimePickerView.this.mTimeColorStates[0]);
                    TextInputTimePickerView.this.mMinuteEditText.setTextColor(TextInputTimePickerView.this.mTimeColorStates[1]);
                    TextInputTimePickerView.this.resetInputTimeLabelState(true);
                }
            }
        });
        this.mMinuteEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    TextInputTimePickerView.this.mMinuteEditText.setActivated(true);
                    TextInputTimePickerView.this.mHourEditText.setActivated(false);
                    TextInputTimePickerView.this.resetInputTimeTextAppearance(R.style.OPTextAppearance_Material_TimePicker_InputFieldUnActive, TextInputTimePickerView.this.mHourEditText);
                    TextInputTimePickerView.this.resetInputTimeTextAppearance(R.style.OPTextAppearance_Material_TimePicker_InputField, TextInputTimePickerView.this.mMinuteEditText);
                    TextInputTimePickerView.this.mMinuteEditText.setTextColor(TextInputTimePickerView.this.mTimeColorStates[0]);
                    TextInputTimePickerView.this.mHourEditText.setTextColor(TextInputTimePickerView.this.mTimeColorStates[1]);
                    TextInputTimePickerView.this.resetInputTimeLabelState(false);
                }
            }
        });
        this.mHourEditText.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            public void afterTextChanged(Editable editable) {
                TextInputTimePickerView.this.parseAndSetHourInternal(editable.toString());
            }
        });
        this.mMinuteEditText.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            public void afterTextChanged(Editable editable) {
                TextInputTimePickerView.this.parseAndSetMinuteInternal(editable.toString());
            }
        });
        String[] amPmStrings = TimePicker.getAmPmStrings(context);
        this.mAmPmGroup = (RadioGroup) findViewById(R.id.am_pm_group);
        this.mAmLabel = (RadioButton) findViewById(R.id.am_label2);
        this.mAmLabel.setText(TimePickerClockDelegate.obtainVerbatim(amPmStrings[0]));
        this.mAmLabel.setOnClickListener(this.mClickListener);
        ensureMinimumTextWidth(this.mAmLabel);
        this.mPmLabel = (RadioButton) findViewById(R.id.pm_label2);
        this.mPmLabel.setText(TimePickerClockDelegate.obtainVerbatim(amPmStrings[1]));
        this.mPmLabel.setOnClickListener(this.mClickListener);
        ensureMinimumTextWidth(this.mPmLabel);
    }

    private void resetInputTimeTextAppearance(int textAppearance, TextView targetTextView) {
        if (SystemUtils.isAtLeastM()) {
            targetTextView.setTextAppearance(textAppearance);
        } else {
            targetTextView.setTextAppearance(getContext(), textAppearance);
        }
    }

    private void resetInputTimeLabelState(boolean hoursFocus) {
        this.mMinuteLabel.setTextColor(hoursFocus ? this.mTimeLabelColorStates[1] : this.mTimeLabelColorStates[0]);
        this.mHourLabel.setTextColor(hoursFocus ? this.mTimeLabelColorStates[0] : this.mTimeLabelColorStates[1]);
    }

    public View getInputBlock() {
        return this.mInputBlock;
    }

    private void updateAmPmLabel(boolean isAm) {
        this.mAmLabel.setActivated(isAm);
        this.mAmLabel.setChecked(isAm);
        this.mAmLabel.setTextColor(isAm ? this.mTimeColorStates[0] : this.mTimeColorStates[1]);
        this.mAmLabel.getPaint().setFakeBoldText(isAm);
        this.mPmLabel.setActivated(isAm ^ 1);
        this.mPmLabel.setChecked(isAm ^ 1);
        this.mPmLabel.setTextColor(isAm ? this.mTimeColorStates[1] : this.mTimeColorStates[0]);
        this.mPmLabel.getPaint().setFakeBoldText(isAm ^ 1);
    }

    public void showLabels(boolean show) {
        if (show) {
            this.mHourLabel.animate().alpha(1.0f).setDuration((long) this.mLabelAlphaDuration).setInterpolator(AnimatorUtils.FastOutSlowInInterpolator).start();
            this.mMinuteLabel.animate().alpha(1.0f).setDuration((long) this.mLabelAlphaDuration).setInterpolator(AnimatorUtils.FastOutSlowInInterpolator).start();
            this.mHeaderLabel.animate().alpha(1.0f).setDuration((long) this.mLabelAlphaDuration).setInterpolator(AnimatorUtils.FastOutSlowInInterpolator).start();
            this.mHourLabel.animate().alpha(1.0f).setDuration((long) this.mLabelAlphaDuration).setInterpolator(AnimatorUtils.FastOutSlowInInterpolator).start();
            return;
        }
        this.mHourLabel.animate().alpha(0.0f).setDuration((long) this.mLabelAlphaDuration).setInterpolator(AnimatorUtils.FastOutSlowInInterpolator).start();
        this.mMinuteLabel.animate().alpha(0.0f).setDuration((long) this.mLabelAlphaDuration).setInterpolator(AnimatorUtils.FastOutSlowInInterpolator).start();
        this.mHeaderLabel.animate().alpha(0.0f).setDuration((long) this.mLabelAlphaDuration).setInterpolator(AnimatorUtils.FastOutSlowInInterpolator).start();
        this.mHourLabel.animate().alpha(0.0f).setDuration((long) this.mLabelAlphaDuration).setInterpolator(AnimatorUtils.FastOutSlowInInterpolator).start();
    }

    public void showInputBlock(boolean show) {
        int i = 4;
        this.mInputBlock.setVisibility(show ? 0 : 4);
        RadioGroup radioGroup = this.mAmPmGroup;
        if (show) {
            i = 0;
        }
        radioGroup.setVisibility(i);
    }

    public void setAmPmAtStart(boolean isAmPmAtStart) {
        if (this.mIs24Hour) {
            this.mAmPmParent.removeView(this.mAmPmGroup);
            return;
        }
        if (this.mIsAmPmAtStart != isAmPmAtStart) {
            this.mIsAmPmAtStart = isAmPmAtStart;
            if (isAmPmAtStart) {
                this.mAmPmParent.removeView(this.mAmPmGroup);
                this.mAmPmParent.addView(this.mAmPmGroup, 0);
            } else {
                this.mAmPmParent.removeView(this.mAmPmGroup);
                this.mAmPmParent.addView(this.mAmPmGroup);
            }
            this.mAmPmParent.requestLayout();
        }
    }

    public void setIs24Hour(boolean is24Hour) {
        if (this.mIs24Hour != is24Hour) {
            this.mIs24Hour = is24Hour;
            setAmPmAtStart(this.mIsAmPmAtStart);
        }
    }

    private static void ensureMinimumTextWidth(TextView v) {
        v.measure(0, 0);
        int minWidth = v.getMeasuredWidth();
        v.setMinWidth(minWidth);
        v.setMinimumWidth(minWidth);
    }

    /* Access modifiers changed, original: 0000 */
    public void setListener(OnValueTypedListener listener) {
        this.mListener = listener;
    }

    /* Access modifiers changed, original: 0000 */
    public void setHourFormat(int maxCharLength) {
        this.mHourEditText.setFilters(new InputFilter[]{new LengthFilter(maxCharLength)});
        this.mMinuteEditText.setFilters(new InputFilter[]{new LengthFilter(maxCharLength)});
    }

    /* Access modifiers changed, original: 0000 */
    public boolean validateInput() {
        boolean z = false;
        boolean inputValid = parseAndSetHourInternal(this.mHourEditText.getText().toString()) && parseAndSetMinuteInternal(this.mMinuteEditText.getText().toString());
        if (!inputValid) {
            z = true;
        }
        setError(z);
        return inputValid;
    }

    /* Access modifiers changed, original: 0000 */
    public void updateSeparator(String separatorText) {
    }

    private void setError(boolean enabled) {
        this.mErrorShowing = enabled;
        int i = 4;
        this.mErrorLabel.setVisibility(enabled ? 0 : 4);
        this.mHourLabel.setVisibility(enabled ? 4 : 0);
        TextView textView = this.mMinuteLabel;
        if (!enabled) {
            i = 0;
        }
        textView.setVisibility(i);
    }

    /* Access modifiers changed, original: 0000 */
    public void updateTextInputValues(int localizedHour, int minute, int amOrPm, boolean is24Hour, boolean hourFormatStartsAtZero) {
        String format = "%d";
        this.mIs24Hour = is24Hour;
        this.mHourFormatStartsAtZero = hourFormatStartsAtZero;
        this.mAmPmGroup.setVisibility(is24Hour ? 8 : 0);
        updateAmPmLabel(amOrPm == 0);
        this.mHourEditText.setText(String.format("%d", new Object[]{Integer.valueOf(localizedHour)}));
        this.mMinuteEditText.setText(String.format("%d", new Object[]{Integer.valueOf(minute)}));
        if (this.mErrorShowing) {
            validateInput();
        }
    }

    private boolean parseAndSetHourInternal(String input) {
        try {
            int hour = Integer.parseInt(input);
            if (isValidLocalizedHour(hour)) {
                this.mListener.onValueChanged(0, getHourOfDayFromLocalizedHour(hour));
                return true;
            }
            int minHour = this.mHourFormatStartsAtZero ^ 1;
            this.mListener.onValueChanged(0, getHourOfDayFromLocalizedHour(MathUtils.constrain(hour, minHour, this.mIs24Hour ? 23 : 11 + minHour)));
            return false;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean parseAndSetMinuteInternal(String input) {
        try {
            int minutes = Integer.parseInt(input);
            if (minutes >= 0) {
                if (minutes <= 59) {
                    this.mListener.onValueChanged(1, minutes);
                    return true;
                }
            }
            this.mListener.onValueChanged(1, MathUtils.constrain(minutes, 0, 59));
            return false;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isValidLocalizedHour(int localizedHour) {
        int minHour = this.mHourFormatStartsAtZero ^ 1;
        int maxHour = (this.mIs24Hour ? 23 : 11) + minHour;
        if (localizedHour < minHour || localizedHour > maxHour) {
            return false;
        }
        return true;
    }

    private int getHourOfDayFromLocalizedHour(int localizedHour) {
        int hourOfDay = localizedHour;
        if (!this.mIs24Hour) {
            if (!this.mHourFormatStartsAtZero && localizedHour == 12) {
                hourOfDay = 0;
            }
            if (this.mPmLabel.isChecked()) {
                return hourOfDay + 12;
            }
            return hourOfDay;
        } else if (this.mHourFormatStartsAtZero || localizedHour != 24) {
            return hourOfDay;
        } else {
            return 0;
        }
    }
}
