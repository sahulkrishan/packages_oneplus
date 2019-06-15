package com.oneplus.lib.widget;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Parcelable;
import android.support.v4.view.ViewCompat;
import android.text.SpannableStringBuilder;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.style.TtsSpan.VerbatimBuilder;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.AccessibilityDelegate;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import com.oneplus.commonctrl.R;
import com.oneplus.lib.util.AnimatorUtils;
import com.oneplus.lib.util.SystemUtils;
import com.oneplus.lib.widget.NumericTextView.OnValueChangedListener;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Calendar;
import java.util.List;

class TimePickerClockDelegate extends AbstractTimePickerDelegate {
    private static final int AM = 0;
    private static final int[] ATTRS_DISABLED_ALPHA = new int[]{16842803};
    private static final int[] ATTRS_TEXT_COLOR = new int[]{16842904};
    private static final long DELAY_COMMIT_MILLIS = 2000;
    private static final int FROM_EXTERNAL_API = 0;
    private static final int FROM_INPUT_PICKER = 2;
    private static final int FROM_RADIAL_PICKER = 1;
    private static final int HOURS_IN_HALF_DAY = 12;
    private static final int HOUR_INDEX = 0;
    private static final int MINUTE_INDEX = 1;
    private static final int PM = 1;
    private boolean mAllowAutoAdvance;
    private final RadioButton mAmLabel;
    private final View mAmPmLayout;
    private final OnClickListener mClickListener = new OnClickListener() {
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.am_label) {
                TimePickerClockDelegate.this.setAmOrPm(0);
            } else if (id == R.id.pm_label) {
                TimePickerClockDelegate.this.setAmOrPm(1);
            } else if (id == R.id.hours) {
                TimePickerClockDelegate.this.setCurrentItemShowing(0, true, true);
            } else if (id == R.id.minutes) {
                TimePickerClockDelegate.this.setCurrentItemShowing(1, true, true);
            } else {
                return;
            }
            TimePickerClockDelegate.this.tryVibrate();
        }
    };
    private final Runnable mCommitHour = new Runnable() {
        public void run() {
            TimePickerClockDelegate.this.setHour(TimePickerClockDelegate.this.mHourView.getValue());
        }
    };
    private final Runnable mCommitMinute = new Runnable() {
        public void run() {
            TimePickerClockDelegate.this.setMinute(TimePickerClockDelegate.this.mMinuteView.getValue());
        }
    };
    private int mCurrentHour;
    private int mCurrentMinute;
    private final OnValueChangedListener mDigitEnteredListener = new OnValueChangedListener() {
        public void onValueChanged(NumericTextView view, int value, boolean isValid, boolean isFinished) {
            Runnable commitCallback;
            View nextFocusTarget;
            if (view == TimePickerClockDelegate.this.mHourView) {
                commitCallback = TimePickerClockDelegate.this.mCommitHour;
                nextFocusTarget = view.isFocused() ? TimePickerClockDelegate.this.mMinuteView : null;
            } else if (view == TimePickerClockDelegate.this.mMinuteView) {
                commitCallback = TimePickerClockDelegate.this.mCommitMinute;
                nextFocusTarget = null;
            } else {
                return;
            }
            view.removeCallbacks(commitCallback);
            if (isValid) {
                if (isFinished) {
                    commitCallback.run();
                    if (nextFocusTarget != null) {
                        nextFocusTarget.requestFocus();
                    }
                } else {
                    view.postDelayed(commitCallback, 2000);
                }
            }
        }
    };
    private int mDuration;
    private final OnFocusChangeListener mFocusListener = new OnFocusChangeListener() {
        public void onFocusChange(View v, boolean focused) {
            if (focused) {
                int id = v.getId();
                if (id == R.id.am_label) {
                    TimePickerClockDelegate.this.setAmOrPm(0);
                } else if (id == R.id.pm_label) {
                    TimePickerClockDelegate.this.setAmOrPm(1);
                } else if (id == R.id.hours) {
                    TimePickerClockDelegate.this.setCurrentItemShowing(0, true, true);
                } else if (id == R.id.minutes) {
                    TimePickerClockDelegate.this.setCurrentItemShowing(1, true, true);
                } else {
                    return;
                }
                TimePickerClockDelegate.this.tryVibrate();
            }
        }
    };
    private int mHeaderOffset;
    private int mHeaderPositionY;
    private boolean mHourFormatShowLeadingZero;
    private boolean mHourFormatStartsAtZero;
    private final NumericTextView mHourView;
    private final View mImageSeparatorView;
    private int mInputBlockPositionY;
    private boolean mIs24Hour;
    private boolean mIsAmPmAtStart;
    private boolean mIsEnabled = true;
    private boolean mIsToggleTimeMode;
    private boolean mLastAnnouncedIsHour;
    private CharSequence mLastAnnouncedText;
    private final NumericTextView mMinuteView;
    private final OnValueSelectedListener mOnValueSelectedListener = new OnValueSelectedListener() {
        public void onValueSelected(int pickerType, int newValue, boolean autoAdvance) {
            boolean valueChanged = false;
            switch (pickerType) {
                case 0:
                    if (TimePickerClockDelegate.this.getHour() != newValue) {
                        valueChanged = true;
                    }
                    boolean isTransition = TimePickerClockDelegate.this.mAllowAutoAdvance && autoAdvance;
                    TimePickerClockDelegate.this.setHourInternal(newValue, 1, !isTransition);
                    if (isTransition) {
                        TimePickerClockDelegate.this.setCurrentItemShowing(1, true, false);
                        int localizedHour = TimePickerClockDelegate.this.getLocalizedHour(newValue);
                        TimePicker timePicker = TimePickerClockDelegate.this.mDelegator;
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(localizedHour);
                        stringBuilder.append(". ");
                        stringBuilder.append(TimePickerClockDelegate.this.mSelectMinutes);
                        timePicker.announceForAccessibility(stringBuilder.toString());
                        break;
                    }
                    break;
                case 1:
                    if (TimePickerClockDelegate.this.getMinute() != newValue) {
                        valueChanged = true;
                    }
                    TimePickerClockDelegate.this.setMinuteInternal(newValue, 1);
                    break;
            }
            if (TimePickerClockDelegate.this.mOnTimeChangedListener != null && valueChanged) {
                TimePickerClockDelegate.this.mOnTimeChangedListener.onTimeChanged(TimePickerClockDelegate.this.mDelegator, TimePickerClockDelegate.this.getHour(), TimePickerClockDelegate.this.getMinute());
            }
        }
    };
    private final OnValueTypedListener mOnValueTypedListener = new OnValueTypedListener() {
        public void onValueChanged(int pickerType, int newValue) {
            switch (pickerType) {
                case 0:
                    TimePickerClockDelegate.this.setHourInternal(newValue, 2, false);
                    return;
                case 1:
                    TimePickerClockDelegate.this.setMinuteInternal(newValue, 2);
                    return;
                case 2:
                    TimePickerClockDelegate.this.setAmOrPm(newValue);
                    return;
                default:
                    return;
            }
        }
    };
    private final RadioButton mPmLabel;
    private boolean mRadialPickerModeEnabled = true;
    private final LinearLayout mRadialTimePickerHeader;
    private final ImageButton mRadialTimePickerModeButton;
    private final RadialTimePickerView mRadialTimePickerView;
    private float mRadialTimeViewAlpha;
    private float mRadialTimeViewScale;
    private final String mSelectHours;
    private final String mSelectMinutes;
    private final TextView mSeparatorView;
    private final Calendar mTempCalendar;
    private final View mTextInputPickerHeader;
    private final TextInputTimePickerView mTextInputPickerView;

    @Retention(RetentionPolicy.SOURCE)
    private @interface ChangeSource {
    }

    private static class ClickActionDelegate extends AccessibilityDelegate {
        private final AccessibilityAction mClickAction;

        public ClickActionDelegate(Context context, int resId) {
            this.mClickAction = new AccessibilityAction(16, context.getString(resId));
        }

        public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
            super.onInitializeAccessibilityNodeInfo(host, info);
            info.addAction(this.mClickAction);
        }
    }

    private static class NearestTouchDelegate implements OnTouchListener {
        private View mInitialTouchTarget;

        private NearestTouchDelegate() {
        }

        /* synthetic */ NearestTouchDelegate(AnonymousClass1 x0) {
            this();
        }

        public boolean onTouch(View view, MotionEvent motionEvent) {
            int actionMasked = motionEvent.getActionMasked();
            if (actionMasked == 0) {
                if (view instanceof ViewGroup) {
                    this.mInitialTouchTarget = findNearestChild((ViewGroup) view, (int) motionEvent.getX(), (int) motionEvent.getY());
                } else {
                    this.mInitialTouchTarget = null;
                }
            }
            View child = this.mInitialTouchTarget;
            if (child == null) {
                return false;
            }
            float offsetX = (float) (view.getScrollX() - child.getLeft());
            float offsetY = (float) (view.getScrollY() - child.getTop());
            motionEvent.offsetLocation(offsetX, offsetY);
            boolean handled = child.dispatchTouchEvent(motionEvent);
            motionEvent.offsetLocation(-offsetX, -offsetY);
            if (actionMasked == 1 || actionMasked == 3) {
                this.mInitialTouchTarget = null;
            }
            return handled;
        }

        private View findNearestChild(ViewGroup v, int x, int y) {
            View bestChild = null;
            int bestDist = Integer.MAX_VALUE;
            int count = v.getChildCount();
            for (int i = 0; i < count; i++) {
                View child = v.getChildAt(i);
                int dX = x - (child.getLeft() + (child.getWidth() / 2));
                int dY = y - (child.getTop() + (child.getHeight() / 2));
                int dist = (dX * dX) + (dY * dY);
                if (bestDist > dist) {
                    bestChild = child;
                    bestDist = dist;
                }
            }
            return bestChild;
        }
    }

    public TimePickerClockDelegate(TimePicker delegator, Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        Context context2 = context;
        AttributeSet attributeSet = attrs;
        int i = defStyleAttr;
        int i2 = defStyleRes;
        super(delegator, context);
        TypedArray a = this.mContext.obtainStyledAttributes(attributeSet, R.styleable.TimePicker, i, i2);
        LayoutInflater inflater = (LayoutInflater) this.mContext.getSystemService("layout_inflater");
        Resources res = this.mContext.getResources();
        this.mSelectHours = res.getString(R.string.select_hours);
        this.mSelectMinutes = res.getString(R.string.select_minutes);
        View mainView = inflater.inflate(a.getResourceId(R.styleable.TimePicker_internalLayout, R.layout.op_time_picker_material), delegator);
        mainView.setSaveFromParentEnabled(false);
        this.mRadialTimePickerHeader = (LinearLayout) mainView.findViewById(R.id.time_header);
        this.mRadialTimePickerHeader.setOnTouchListener(new NearestTouchDelegate());
        this.mHourView = (NumericTextView) mainView.findViewById(R.id.hours);
        this.mHourView.setOnClickListener(this.mClickListener);
        this.mHourView.setOnFocusChangeListener(this.mFocusListener);
        this.mHourView.setOnDigitEnteredListener(this.mDigitEnteredListener);
        this.mHourView.setAccessibilityDelegate(new ClickActionDelegate(context2, R.string.select_hours));
        this.mSeparatorView = (TextView) mainView.findViewById(R.id.separator);
        this.mImageSeparatorView = mainView.findViewById(R.id.separator_shape);
        this.mMinuteView = (NumericTextView) mainView.findViewById(R.id.minutes);
        this.mMinuteView.setOnClickListener(this.mClickListener);
        this.mMinuteView.setOnFocusChangeListener(this.mFocusListener);
        this.mMinuteView.setOnDigitEnteredListener(this.mDigitEnteredListener);
        this.mMinuteView.setAccessibilityDelegate(new ClickActionDelegate(context2, R.string.select_minutes));
        this.mMinuteView.setRange(0, 59);
        mainView.findViewById(R.id.separator).setActivated(true);
        ((TextView) mainView.findViewById(R.id.separator)).getPaint().setFakeBoldText(true);
        this.mAmPmLayout = mainView.findViewById(R.id.ampm_layout);
        this.mAmPmLayout.setOnTouchListener(new NearestTouchDelegate());
        String[] amPmStrings = TimePicker.getAmPmStrings(context);
        this.mAmLabel = (RadioButton) this.mAmPmLayout.findViewById(R.id.am_label);
        this.mAmLabel.setText(obtainVerbatim(amPmStrings[0]));
        this.mAmLabel.setOnClickListener(this.mClickListener);
        ensureMinimumTextWidth(this.mAmLabel);
        this.mPmLabel = (RadioButton) this.mAmPmLayout.findViewById(R.id.pm_label);
        this.mPmLabel.setText(obtainVerbatim(amPmStrings[1]));
        this.mPmLabel.setOnClickListener(this.mClickListener);
        ensureMinimumTextWidth(this.mPmLabel);
        ColorStateList headerTextColor = null;
        int timeHeaderTextAppearance = a.getResourceId(R.styleable.TimePicker_android_headerTimeTextAppearance, 0);
        if (timeHeaderTextAppearance != 0) {
            TypedArray textAppearance = this.mContext.obtainStyledAttributes(null, ATTRS_TEXT_COLOR, 0, timeHeaderTextAppearance);
            inflater = textAppearance.getColorStateList(0);
            textAppearance.recycle();
        } else {
            Resources resources = res;
        }
        if (null == null) {
            headerTextColor = a.getColorStateList(R.styleable.TimePicker_headerTextColor);
        }
        this.mTextInputPickerHeader = mainView.findViewById(R.id.input_header);
        if (headerTextColor != null) {
            this.mHourView.setTextColor(headerTextColor);
            this.mSeparatorView.setTextColor(headerTextColor);
            this.mMinuteView.setTextColor(headerTextColor);
            this.mAmLabel.setTextColor(headerTextColor);
            this.mPmLabel.setTextColor(headerTextColor);
        }
        a.recycle();
        this.mDuration = context.getResources().getInteger(R.integer.oneplus_contorl_time_part6);
        this.mRadialTimeViewScale = 0.0f;
        this.mRadialTimeViewAlpha = 0.0f;
        this.mRadialTimePickerView = (RadialTimePickerView) mainView.findViewById(R.id.radial_picker);
        this.mRadialTimePickerView.applyAttributes(attributeSet, i, i2);
        this.mRadialTimePickerView.setOnValueSelectedListener(this.mOnValueSelectedListener);
        this.mTextInputPickerView = (TextInputTimePickerView) mainView.findViewById(R.id.input_mode);
        this.mTextInputPickerView.setListener(this.mOnValueTypedListener);
        this.mRadialTimePickerModeButton = (ImageButton) mainView.findViewById(R.id.toggle_mode);
        this.mRadialTimePickerModeButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                TimePickerClockDelegate.this.toggleRadialPickerMode();
            }
        });
        this.mAllowAutoAdvance = true;
        updateHourFormat();
        this.mTempCalendar = Calendar.getInstance(this.mLocale);
        initialize(this.mTempCalendar.get(11), this.mTempCalendar.get(12), this.mIs24Hour, 0);
    }

    private void toggleRadialPickerMode() {
        if (!this.mIsToggleTimeMode) {
            if (this.mHeaderPositionY == 0) {
                int[] headerLocation = new int[2];
                this.mRadialTimePickerHeader.getLocationOnScreen(headerLocation);
                int headerLocationY = (int) this.mRadialTimePickerHeader.findViewById(R.id.hours).getY();
                this.mHeaderPositionY = headerLocation[1];
                View inputBlock = this.mTextInputPickerView.getInputBlock();
                this.mHeaderOffset = Math.abs(((int) this.mTextInputPickerView.getInputBlock().findViewById(R.id.input_hour).getY()) - headerLocationY);
                if (this.mHeaderOffset == 0) {
                    this.mHeaderOffset = Math.round(this.mContext.getResources().getDisplayMetrics().density * 4.0f);
                }
                inputBlock.getLocationOnScreen(headerLocation);
                this.mInputBlockPositionY = headerLocation[1];
            }
            if (this.mRadialPickerModeEnabled) {
                animationInInputTimeField();
                this.mRadialTimePickerModeButton.setImageResource(R.drawable.op_btn_clock_material);
                this.mRadialPickerModeEnabled = false;
            } else {
                animationOutInputTimeField();
                this.mRadialTimePickerModeButton.setImageResource(R.drawable.op_btn_keyboard_key_material);
                this.mRadialPickerModeEnabled = true;
            }
        }
    }

    private void animationInInputTimeField() {
        this.mTextInputPickerHeader.setVisibility(4);
        this.mTextInputPickerView.setVisibility(4);
        this.mTextInputPickerHeader.setVisibility(0);
        this.mTextInputPickerHeader.animate().alpha(1.0f).setDuration((long) this.mDuration).start();
        this.mRadialTimePickerView.animate().scaleX(this.mRadialTimeViewScale).scaleY(this.mRadialTimeViewScale).alpha(this.mRadialTimeViewAlpha).setDuration((long) this.mDuration).setInterpolator(AnimatorUtils.FastOutSlowInInterpolator).start();
        this.mRadialTimePickerHeader.setTranslationY(0.0f);
        this.mRadialTimePickerHeader.animate().setInterpolator(AnimatorUtils.FastOutSlowInInterpolator).translationY((float) (((this.mInputBlockPositionY - this.mHeaderPositionY) + this.mRadialTimePickerHeader.getPaddingTop()) + this.mHeaderOffset)).setDuration((long) this.mDuration).setListener(new AnimatorListener() {
            public void onAnimationStart(Animator animator) {
                TimePickerClockDelegate.this.mTextInputPickerView.showInputBlock(true);
                TimePickerClockDelegate.this.mTextInputPickerView.showLabels(true);
                TimePickerClockDelegate.this.mIsToggleTimeMode = true;
            }

            public void onAnimationEnd(Animator animator) {
                TimePickerClockDelegate.this.mRadialTimePickerView.setVisibility(4);
                TimePickerClockDelegate.this.mRadialTimePickerHeader.setVisibility(4);
                TimePickerClockDelegate.this.mTextInputPickerView.setVisibility(0);
                TimePickerClockDelegate.this.mIsToggleTimeMode = false;
            }

            public void onAnimationCancel(Animator animator) {
            }

            public void onAnimationRepeat(Animator animator) {
            }
        }).start();
    }

    private void animationOutInputTimeField() {
        View inputBlock = this.mTextInputPickerView.getInputBlock();
        this.mRadialTimePickerView.setVisibility(0);
        this.mRadialTimePickerHeader.setVisibility(0);
        this.mRadialTimePickerView.setAlpha(0.0f);
        this.mTextInputPickerHeader.animate().alpha(0.0f).setDuration((long) this.mDuration).start();
        this.mTextInputPickerView.showLabels(false);
        this.mRadialTimePickerView.animate().scaleX(1.0f).scaleY(1.0f).alpha(1.0f).setInterpolator(AnimatorUtils.FastOutSlowInInterpolator).setDuration((long) this.mDuration).start();
        this.mRadialTimePickerHeader.animate().translationY(0.0f).setInterpolator(AnimatorUtils.FastOutSlowInInterpolator).setDuration((long) this.mDuration).setListener(new AnimatorListener() {
            public void onAnimationStart(Animator animator) {
                TimePickerClockDelegate.this.mTextInputPickerView.showInputBlock(false);
                TimePickerClockDelegate.this.mIsToggleTimeMode = true;
            }

            public void onAnimationEnd(Animator animator) {
                TimePickerClockDelegate.this.mRadialTimePickerView.setVisibility(0);
                TimePickerClockDelegate.this.mRadialTimePickerHeader.setVisibility(0);
                TimePickerClockDelegate.this.mTextInputPickerHeader.setVisibility(4);
                TimePickerClockDelegate.this.mTextInputPickerView.setVisibility(4);
                TimePickerClockDelegate.this.updateTextInputPicker();
                TimePickerClockDelegate.this.mIsToggleTimeMode = false;
            }

            public void onAnimationCancel(Animator animator) {
            }

            public void onAnimationRepeat(Animator animator) {
            }
        }).start();
    }

    public boolean validateInput() {
        return this.mTextInputPickerView.validateInput();
    }

    private static void ensureMinimumTextWidth(TextView v) {
        v.measure(0, 0);
        int minWidth = v.getMeasuredWidth();
        v.setMinWidth(minWidth);
        v.setMinimumWidth(minWidth);
    }

    /* JADX WARNING: Removed duplicated region for block: B:27:0x0055  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0052  */
    /* JADX WARNING: Removed duplicated region for block: B:42:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x006a A:{Catch:{ Exception -> 0x0072 }} */
    private void updateHourFormat() {
        /*
        r10 = this;
        r0 = r10.mLocale;
        r1 = r10.mIs24Hour;
        if (r1 == 0) goto L_0x0009;
    L_0x0006:
        r1 = "Hm";
        goto L_0x000b;
    L_0x0009:
        r1 = "hm";
    L_0x000b:
        r0 = android.text.format.DateFormat.getBestDateTimePattern(r0, r1);
        r1 = r0.length();
        r2 = 0;
        r3 = 0;
        r4 = 0;
        r5 = r4;
    L_0x0017:
        r6 = 75;
        r7 = 72;
        if (r5 >= r1) goto L_0x003f;
    L_0x001d:
        r8 = r0.charAt(r5);
        if (r8 == r7) goto L_0x0031;
    L_0x0023:
        r9 = 104; // 0x68 float:1.46E-43 double:5.14E-322;
        if (r8 == r9) goto L_0x0031;
    L_0x0027:
        if (r8 == r6) goto L_0x0031;
    L_0x0029:
        r9 = 107; // 0x6b float:1.5E-43 double:5.3E-322;
        if (r8 != r9) goto L_0x002e;
    L_0x002d:
        goto L_0x0031;
    L_0x002e:
        r5 = r5 + 1;
        goto L_0x0017;
    L_0x0031:
        r3 = r8;
        r9 = r5 + 1;
        if (r9 >= r1) goto L_0x003f;
    L_0x0036:
        r9 = r5 + 1;
        r9 = r0.charAt(r9);
        if (r8 != r9) goto L_0x003f;
    L_0x003e:
        r2 = 1;
    L_0x003f:
        r10.mHourFormatShowLeadingZero = r2;
        r5 = 1;
        if (r3 == r6) goto L_0x0048;
    L_0x0044:
        if (r3 != r7) goto L_0x0047;
    L_0x0046:
        goto L_0x0048;
    L_0x0047:
        goto L_0x0049;
    L_0x0048:
        r4 = r5;
    L_0x0049:
        r10.mHourFormatStartsAtZero = r4;
        r4 = r10.mHourFormatStartsAtZero;
        r4 = r4 ^ r5;
        r5 = r10.mIs24Hour;
        if (r5 == 0) goto L_0x0055;
    L_0x0052:
        r5 = 23;
        goto L_0x0057;
    L_0x0055:
        r5 = 11;
    L_0x0057:
        r5 = r5 + r4;
        r6 = r10.mHourView;
        r6.setRange(r4, r5);
        r6 = r10.mHourView;
        r7 = r10.mHourFormatShowLeadingZero;
        r6.setShowLeadingZeroes(r7);
        r6 = android.os.Build.VERSION.SDK_INT;	 Catch:{ Exception -> 0x0072 }
        r7 = 24;
        if (r6 < r7) goto L_0x0071;
    L_0x006a:
        r6 = r10.mTextInputPickerView;	 Catch:{ Exception -> 0x0072 }
        r7 = r10.mLocale;	 Catch:{ Exception -> 0x0072 }
        com.oneplus.lib.widget.TimePickerCompat24.setHourFormat(r6, r7);	 Catch:{ Exception -> 0x0072 }
    L_0x0071:
        goto L_0x0076;
    L_0x0072:
        r6 = move-exception;
        r6.printStackTrace();
    L_0x0076:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.lib.widget.TimePickerClockDelegate.updateHourFormat():void");
    }

    static final CharSequence obtainVerbatim(String text) {
        return new SpannableStringBuilder().append(text, new VerbatimBuilder(text).build(), 0);
    }

    private int multiplyAlphaComponent(int color, float alphaMod) {
        return (((int) ((((float) ((color >> 24) & 255)) * alphaMod) + 1056964608)) << 24) | (ViewCompat.MEASURED_SIZE_MASK & color);
    }

    private void initialize(int hourOfDay, int minute, boolean is24HourView, int index) {
        this.mCurrentHour = hourOfDay;
        this.mCurrentMinute = minute;
        this.mIs24Hour = is24HourView;
        updateUI(index);
    }

    private void updateUI(int index) {
        updateHeaderAmPm();
        updateHeaderHour(this.mCurrentHour, false);
        updateHeaderSeparator();
        updateHeaderMinute(this.mCurrentMinute, false);
        updateRadialPicker(index);
        updateTextInputPicker();
        this.mDelegator.invalidate();
    }

    private void updateTextInputPicker() {
        this.mTextInputPickerView.updateTextInputValues(getLocalizedHour(this.mCurrentHour), this.mCurrentMinute, this.mCurrentHour < 12 ? 0 : 1, this.mIs24Hour, this.mHourFormatStartsAtZero);
    }

    private void updateRadialPicker(int index) {
        this.mRadialTimePickerView.initialize(this.mCurrentHour, this.mCurrentMinute, this.mIs24Hour);
        setCurrentItemShowing(index, false, true);
    }

    private void updateHeaderAmPm() {
        if (this.mIs24Hour) {
            this.mAmPmLayout.setVisibility(8);
            return;
        }
        boolean isAmPmAtStart = DateFormat.getBestDateTimePattern(this.mLocale, "hm").startsWith("a");
        setAmPmAtStart(isAmPmAtStart);
        setInputAmPmAtStart(isAmPmAtStart);
        updateAmPmLabelStates(this.mCurrentHour < 12 ? 0 : 1);
    }

    private void setInputAmPmAtStart(boolean isAmPmAtStart) {
        this.mTextInputPickerView.setAmPmAtStart(isAmPmAtStart);
    }

    private void setAmPmAtStart(boolean isAmPmAtStart) {
        if (this.mIsAmPmAtStart != isAmPmAtStart) {
            this.mIsAmPmAtStart = isAmPmAtStart;
            if (isAmPmAtStart) {
                this.mRadialTimePickerHeader.removeView(this.mAmPmLayout);
                this.mRadialTimePickerHeader.addView(this.mAmPmLayout, 0);
                return;
            }
            this.mRadialTimePickerHeader.removeView(this.mAmPmLayout);
            this.mRadialTimePickerHeader.addView(this.mAmPmLayout);
        }
    }

    public void setHour(int hour) {
        setHourInternal(hour, 0, true);
    }

    private void setHourInternal(int hour, int source, boolean announce) {
        if (this.mCurrentHour != hour) {
            this.mCurrentHour = hour;
            updateHeaderHour(hour, announce);
            updateHeaderAmPm();
            int i = 1;
            if (source != 1) {
                this.mRadialTimePickerView.setCurrentHour(hour);
                RadialTimePickerView radialTimePickerView = this.mRadialTimePickerView;
                if (hour < 12) {
                    i = 0;
                }
                radialTimePickerView.setAmOrPm(i);
            }
            if (source != 2) {
                updateTextInputPicker();
            }
            this.mDelegator.invalidate();
            onTimeChanged();
        }
    }

    public int getHour() {
        int currentHour = this.mRadialTimePickerView.getCurrentHour();
        if (this.mIs24Hour) {
            return currentHour;
        }
        if (this.mRadialTimePickerView.getAmOrPm() == 1) {
            return (currentHour % 12) + 12;
        }
        return currentHour % 12;
    }

    public void setMinute(int minute) {
        setMinuteInternal(minute, 0);
    }

    private void setMinuteInternal(int minute, int source) {
        if (this.mCurrentMinute != minute) {
            this.mCurrentMinute = minute;
            updateHeaderMinute(minute, true);
            if (source != 1) {
                this.mRadialTimePickerView.setCurrentMinute(minute);
            }
            if (source != 2) {
                updateTextInputPicker();
            }
            this.mDelegator.invalidate();
            onTimeChanged();
        }
    }

    public int getMinute() {
        return this.mRadialTimePickerView.getCurrentMinute();
    }

    public void setIs24Hour(boolean is24Hour) {
        this.mTextInputPickerView.setIs24Hour(is24Hour);
        if (this.mIs24Hour != is24Hour) {
            this.mIs24Hour = is24Hour;
            this.mCurrentHour = getHour();
            updateHourFormat();
            updateUI(this.mRadialTimePickerView.getCurrentItemShowing());
        }
    }

    public boolean is24Hour() {
        return this.mIs24Hour;
    }

    public void setEnabled(boolean enabled) {
        this.mHourView.setEnabled(enabled);
        this.mMinuteView.setEnabled(enabled);
        this.mAmLabel.setEnabled(enabled);
        this.mPmLabel.setEnabled(enabled);
        this.mRadialTimePickerView.setEnabled(enabled);
        this.mIsEnabled = enabled;
    }

    public boolean isEnabled() {
        return this.mIsEnabled;
    }

    public int getBaseline() {
        return -1;
    }

    public Parcelable onSaveInstanceState(Parcelable superState) {
        return new SavedState(superState, getHour(), getMinute(), is24Hour(), getCurrentItemShowing());
    }

    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            SavedState ss = (SavedState) state;
            initialize(ss.getHour(), ss.getMinute(), ss.is24HourMode(), ss.getCurrentItemShowing());
            this.mRadialTimePickerView.invalidate();
        }
    }

    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        onPopulateAccessibilityEvent(event);
        return true;
    }

    public void onPopulateAccessibilityEvent(AccessibilityEvent event) {
        int flags;
        if (this.mIs24Hour) {
            flags = 1 | 128;
        } else {
            flags = 1 | 64;
        }
        this.mTempCalendar.set(11, getHour());
        this.mTempCalendar.set(12, getMinute());
        String selectedTime = DateUtils.formatDateTime(this.mContext, this.mTempCalendar.getTimeInMillis(), flags);
        String selectionMode = this.mRadialTimePickerView.getCurrentItemShowing() == 0 ? this.mSelectHours : this.mSelectMinutes;
        List text = event.getText();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(selectedTime);
        stringBuilder.append(" ");
        stringBuilder.append(selectionMode);
        text.add(stringBuilder.toString());
    }

    public View getHourView() {
        return this.mHourView;
    }

    public View getMinuteView() {
        return this.mMinuteView;
    }

    public View getAmView() {
        return this.mAmLabel;
    }

    public View getPmView() {
        return this.mPmLabel;
    }

    private int getCurrentItemShowing() {
        return this.mRadialTimePickerView.getCurrentItemShowing();
    }

    private void onTimeChanged() {
        this.mDelegator.sendAccessibilityEvent(4);
        if (this.mOnTimeChangedListener != null) {
            this.mOnTimeChangedListener.onTimeChanged(this.mDelegator, getHour(), getMinute());
        }
        if (this.mAutoFillChangeListener != null) {
            this.mAutoFillChangeListener.onTimeChanged(this.mDelegator, getHour(), getMinute());
        }
    }

    private void tryVibrate() {
        this.mDelegator.performHapticFeedback(4);
    }

    private void updateAmPmLabelStates(int amOrPm) {
        boolean isPm = false;
        boolean isAm = amOrPm == 0;
        this.mAmLabel.setActivated(isAm);
        this.mAmLabel.setChecked(isAm);
        this.mAmLabel.getPaint().setFakeBoldText(isAm);
        if (amOrPm == 1) {
            isPm = true;
        }
        this.mPmLabel.setActivated(isPm);
        this.mPmLabel.setChecked(isPm);
        this.mPmLabel.getPaint().setFakeBoldText(isPm);
    }

    private int getLocalizedHour(int hourOfDay) {
        if (!this.mIs24Hour) {
            hourOfDay %= 12;
        }
        if (this.mHourFormatStartsAtZero || hourOfDay != 0) {
            return hourOfDay;
        }
        return this.mIs24Hour ? 24 : 12;
    }

    private void updateHeaderHour(int hourOfDay, boolean announce) {
        this.mHourView.setValue(getLocalizedHour(hourOfDay));
        if (announce) {
            tryAnnounceForAccessibility(this.mHourView.getText(), true);
        }
    }

    private void updateHeaderMinute(int minuteOfHour, boolean announce) {
        this.mMinuteView.setValue(minuteOfHour);
        if (announce) {
            tryAnnounceForAccessibility(this.mMinuteView.getText(), false);
        }
    }

    private void updateHeaderSeparator() {
        String separatorText;
        String bestDateTimePattern = DateFormat.getBestDateTimePattern(this.mLocale, this.mIs24Hour ? "Hm" : "hm");
        int hIndex = lastIndexOfAny(bestDateTimePattern, new char[]{'H', 'h', 'K', 'k'});
        if (hIndex == -1) {
            separatorText = ":";
        } else {
            separatorText = Character.toString(bestDateTimePattern.charAt(hIndex + 1));
        }
        this.mTextInputPickerView.updateSeparator(separatorText);
    }

    private static int lastIndexOfAny(String str, char[] any) {
        if (lengthAny > 0) {
            for (int i = str.length() - 1; i >= 0; i--) {
                char c = str.charAt(i);
                for (char c2 : any) {
                    if (c == c2) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    private void tryAnnounceForAccessibility(CharSequence text, boolean isHour) {
        if (this.mLastAnnouncedIsHour != isHour || !text.equals(this.mLastAnnouncedText)) {
            this.mDelegator.announceForAccessibility(text);
            this.mLastAnnouncedText = text;
            this.mLastAnnouncedIsHour = isHour;
        }
    }

    private void setCurrentItemShowing(int index, boolean animateCircle, boolean announce) {
        this.mRadialTimePickerView.setCurrentItemShowing(index, animateCircle);
        if (index == 0) {
            if (announce) {
                this.mDelegator.announceForAccessibility(this.mSelectHours);
            }
        } else if (announce) {
            this.mDelegator.announceForAccessibility(this.mSelectMinutes);
        }
        boolean z = false;
        this.mHourView.setActivated(index == 0);
        NumericTextView numericTextView = this.mMinuteView;
        if (index == 1) {
            z = true;
        }
        numericTextView.setActivated(z);
        if (index == 0) {
            resetInputTimeTextAppearance(R.style.OPTextAppearance_Material_TimePicker_TimeLabel, this.mHourView);
            resetInputTimeTextAppearance(R.style.OPTextAppearance_Material_TimePicker_TimeLabelUnActivated, this.mMinuteView);
            return;
        }
        resetInputTimeTextAppearance(R.style.OPTextAppearance_Material_TimePicker_TimeLabel, this.mMinuteView);
        resetInputTimeTextAppearance(R.style.OPTextAppearance_Material_TimePicker_TimeLabelUnActivated, this.mHourView);
    }

    private void resetInputTimeTextAppearance(int textAppearance, TextView targetTextView) {
        if (SystemUtils.isAtLeastM()) {
            targetTextView.setTextAppearance(textAppearance);
        } else {
            targetTextView.setTextAppearance(this.mContext, textAppearance);
        }
    }

    private void setAmOrPm(int amOrPm) {
        updateAmPmLabelStates(amOrPm);
        if (this.mRadialTimePickerView.setAmOrPm(amOrPm)) {
            this.mCurrentHour = getHour();
            updateTextInputPicker();
            if (this.mOnTimeChangedListener != null) {
                this.mOnTimeChangedListener.onTimeChanged(this.mDelegator, getHour(), getMinute());
            }
        }
    }
}
