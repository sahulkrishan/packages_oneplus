package com.oneplus.lib.design.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.os.Build.VERSION;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.ClassLoaderCreator;
import android.os.Parcelable.Creator;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;
import android.support.annotation.VisibleForTesting;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.AbsSavedState;
import android.support.v4.view.AccessibilityDelegateCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v4.widget.Space;
import android.support.v4.widget.TextViewCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewStructure;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.AccelerateInterpolator;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.oneplus.commonctrl.R;
import com.oneplus.lib.util.AnimatorUtils;

public class OPTextInputLayout extends LinearLayout {
    private static final int ANIMATION_DURATION = 200;
    private static final int INVALID_MAX_LENGTH = -1;
    private static final String LOG_TAG = "TextInputLayout";
    private ValueAnimator mAnimator;
    final CollapsingTextHelper mCollapsingTextHelper;
    boolean mCounterEnabled;
    private int mCounterMaxLength;
    private int mCounterOverflowTextAppearance;
    private boolean mCounterOverflowed;
    private int mCounterTextAppearance;
    private TextView mCounterView;
    private ColorStateList mDefaultTextColor;
    EditText mEditText;
    private CharSequence mError;
    private boolean mErrorEnabled;
    private boolean mErrorShown;
    private int mErrorTextAppearance;
    TextView mErrorView;
    private ColorStateList mFocusedTextColor;
    private boolean mHasPasswordToggleTintList;
    private boolean mHasPasswordToggleTintMode;
    private boolean mHasReconstructedEditTextBackground;
    private CharSequence mHint;
    private boolean mHintAnimationEnabled;
    private boolean mHintEnabled;
    private boolean mHintExpanded;
    private boolean mInDrawableStateChanged;
    private LinearLayout mIndicatorArea;
    private int mIndicatorsAdded;
    private final FrameLayout mInputFrame;
    private Drawable mOriginalEditTextEndDrawable;
    private CharSequence mOriginalHint;
    private CharSequence mPasswordToggleContentDesc;
    private Drawable mPasswordToggleDrawable;
    private Drawable mPasswordToggleDummyDrawable;
    private boolean mPasswordToggleEnabled;
    private ColorStateList mPasswordToggleTintList;
    private Mode mPasswordToggleTintMode;
    private OPCheckableImageButton mPasswordToggleView;
    private boolean mPasswordToggledVisible;
    private boolean mRestoringSavedState;
    private Paint mTmpPaint;
    private final Rect mTmpRect;
    private Typeface mTypeface;

    static class SavedState extends AbsSavedState {
        public static final Creator<SavedState> CREATOR = new ClassLoaderCreator<SavedState>() {
            public SavedState createFromParcel(Parcel in, ClassLoader loader) {
                return new SavedState(in, loader);
            }

            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in, null);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        CharSequence error;

        SavedState(Parcelable superState) {
            super(superState);
        }

        SavedState(Parcel source, ClassLoader loader) {
            super(source, loader);
            this.error = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source);
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            TextUtils.writeToParcel(this.error, dest, flags);
        }

        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("TextInputLayout.SavedState{");
            stringBuilder.append(Integer.toHexString(System.identityHashCode(this)));
            stringBuilder.append(" error=");
            stringBuilder.append(this.error);
            stringBuilder.append("}");
            return stringBuilder.toString();
        }
    }

    private class TextInputAccessibilityDelegate extends AccessibilityDelegateCompat {
        TextInputAccessibilityDelegate() {
        }

        public void onInitializeAccessibilityEvent(View host, AccessibilityEvent event) {
            super.onInitializeAccessibilityEvent(host, event);
            event.setClassName(OPTextInputLayout.class.getSimpleName());
        }

        public void onPopulateAccessibilityEvent(View host, AccessibilityEvent event) {
            super.onPopulateAccessibilityEvent(host, event);
            CharSequence text = OPTextInputLayout.this.mCollapsingTextHelper.getText();
            if (!TextUtils.isEmpty(text)) {
                event.getText().add(text);
            }
        }

        public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfoCompat info) {
            super.onInitializeAccessibilityNodeInfo(host, info);
            info.setClassName(OPTextInputLayout.class.getSimpleName());
            CharSequence text = OPTextInputLayout.this.mCollapsingTextHelper.getText();
            if (!TextUtils.isEmpty(text)) {
                info.setText(text);
            }
            if (OPTextInputLayout.this.mEditText != null) {
                info.setLabelFor(OPTextInputLayout.this.mEditText);
            }
            CharSequence error = OPTextInputLayout.this.mErrorView != null ? OPTextInputLayout.this.mErrorView.getText() : null;
            if (!TextUtils.isEmpty(error)) {
                info.setContentInvalid(true);
                info.setError(error);
            }
        }
    }

    public OPTextInputLayout(Context context) {
        this(context, null);
    }

    public OPTextInputLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OPTextInputLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs);
        this.mTmpRect = new Rect();
        this.mCollapsingTextHelper = new CollapsingTextHelper(this);
        setOrientation(1);
        setWillNotDraw(false);
        setAddStatesFromChildren(true);
        this.mInputFrame = new FrameLayout(context);
        this.mInputFrame.setAddStatesFromChildren(true);
        addView(this.mInputFrame);
        this.mCollapsingTextHelper.setTextSizeInterpolator(OPAnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
        this.mCollapsingTextHelper.setPositionInterpolator(new AccelerateInterpolator());
        this.mCollapsingTextHelper.setCollapsedTextGravity(8388659);
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.OPTextInputLayout, defStyleAttr, R.style.Widget_Design_OPTextInputLayout);
        this.mHintEnabled = a.getBoolean(R.styleable.OPTextInputLayout_opHintEnabled, true);
        setHint(a.getText(R.styleable.OPTextInputLayout_android_hint));
        this.mHintAnimationEnabled = a.getBoolean(R.styleable.OPTextInputLayout_opHintAnimationEnabled, true);
        if (a.hasValue(R.styleable.OPTextInputLayout_android_textColorHint)) {
            ColorStateList colorStateList = a.getColorStateList(R.styleable.OPTextInputLayout_android_textColorHint);
            this.mFocusedTextColor = colorStateList;
            this.mDefaultTextColor = colorStateList;
        }
        if (a.getResourceId(R.styleable.OPTextInputLayout_opHintTextAppearance, 0) != 0) {
            setHintTextAppearance(a.getResourceId(R.styleable.OPTextInputLayout_opHintTextAppearance, 0));
        }
        this.mErrorTextAppearance = a.getResourceId(R.styleable.OPTextInputLayout_opErrorTextAppearance, 0);
        boolean errorEnabled = a.getBoolean(R.styleable.OPTextInputLayout_opErrorEnabled, false);
        boolean counterEnabled = a.getBoolean(R.styleable.OPTextInputLayout_opCounterEnabled, false);
        setCounterMaxLength(a.getInt(R.styleable.OPTextInputLayout_opCounterMaxLength, -1));
        this.mCounterTextAppearance = a.getResourceId(R.styleable.OPTextInputLayout_opCounterTextAppearance, 0);
        this.mCounterOverflowTextAppearance = a.getResourceId(R.styleable.OPTextInputLayout_opCounterOverflowTextAppearance, 0);
        this.mPasswordToggleEnabled = a.getBoolean(R.styleable.OPTextInputLayout_opPasswordToggleEnabled, false);
        this.mPasswordToggleDrawable = a.getDrawable(R.styleable.OPTextInputLayout_opPasswordToggleDrawable);
        this.mPasswordToggleContentDesc = a.getText(R.styleable.OPTextInputLayout_opPasswordToggleContentDescription);
        if (a.hasValue(R.styleable.OPTextInputLayout_opPasswordToggleTint)) {
            this.mHasPasswordToggleTintList = true;
            this.mPasswordToggleTintList = a.getColorStateList(R.styleable.OPTextInputLayout_opPasswordToggleTint);
        }
        if (a.hasValue(R.styleable.OPTextInputLayout_opPasswordToggleTintMode)) {
            this.mHasPasswordToggleTintMode = true;
            this.mPasswordToggleTintMode = OPViewUtils.parseTintMode(a.getInt(R.styleable.OPTextInputLayout_opPasswordToggleTintMode, -1), null);
        }
        a.recycle();
        setErrorEnabled(errorEnabled);
        setCounterEnabled(counterEnabled);
        applyPasswordToggleTint();
        if (ViewCompat.getImportantForAccessibility(this) == 0) {
            ViewCompat.setImportantForAccessibility(this, 1);
        }
        ViewCompat.setAccessibilityDelegate(this, new TextInputAccessibilityDelegate());
    }

    public void addView(View child, int index, LayoutParams params) {
        if (child instanceof EditText) {
            FrameLayout.LayoutParams flp = new FrameLayout.LayoutParams(params);
            flp.gravity = 16 | (flp.gravity & -113);
            this.mInputFrame.addView(child, flp);
            this.mInputFrame.setLayoutParams(params);
            updateInputLayoutMargins();
            setEditText((EditText) child);
            return;
        }
        super.addView(child, index, params);
    }

    public void setTypeface(@Nullable Typeface typeface) {
        if ((this.mTypeface != null && !this.mTypeface.equals(typeface)) || (this.mTypeface == null && typeface != null)) {
            this.mTypeface = typeface;
            this.mCollapsingTextHelper.setTypefaces(typeface);
            if (this.mCounterView != null) {
                this.mCounterView.setTypeface(typeface);
            }
            if (this.mErrorView != null) {
                this.mErrorView.setTypeface(typeface);
            }
        }
    }

    @NonNull
    public Typeface getTypeface() {
        return this.mTypeface;
    }

    public void dispatchProvideAutofillStructure(ViewStructure structure, int flags) {
        if (this.mOriginalHint == null || this.mEditText == null) {
            super.dispatchProvideAutofillStructure(structure, flags);
            return;
        }
        CharSequence hint = this.mEditText.getHint();
        this.mEditText.setHint(this.mOriginalHint);
        try {
            super.dispatchProvideAutofillStructure(structure, flags);
        } finally {
            this.mEditText.setHint(hint);
        }
    }

    private void setEditText(EditText editText) {
        if (this.mEditText == null) {
            if (!(editText instanceof OPTextInputEditText)) {
                Log.i(LOG_TAG, "EditText added is not a OPTextInputEditText. Please switch to using that class instead.");
            }
            this.mEditText = editText;
            this.mEditText.setTypeface(Typeface.DEFAULT);
            if (!hasPasswordTransformation()) {
                this.mCollapsingTextHelper.setTypefaces(this.mEditText.getTypeface());
            }
            this.mCollapsingTextHelper.setExpandedTextSize(this.mEditText.getTextSize());
            int editTextGravity = this.mEditText.getGravity();
            this.mCollapsingTextHelper.setCollapsedTextGravity(48 | (editTextGravity & -113));
            this.mCollapsingTextHelper.setExpandedTextGravity(editTextGravity);
            this.mEditText.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                    OPTextInputLayout.this.updateLabelState(OPTextInputLayout.this.mRestoringSavedState ^ 1);
                    if (OPTextInputLayout.this.mCounterEnabled) {
                        OPTextInputLayout.this.updateCounter(s.length());
                    }
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }
            });
            if (this.mDefaultTextColor == null) {
                this.mDefaultTextColor = this.mEditText.getHintTextColors();
            }
            if (this.mHintEnabled && TextUtils.isEmpty(this.mHint)) {
                this.mOriginalHint = this.mEditText.getHint();
                setHint(this.mOriginalHint);
                this.mEditText.setHint(null);
            }
            if (this.mCounterView != null) {
                updateCounter(this.mEditText.getText().length());
            }
            if (this.mIndicatorArea != null) {
                adjustIndicatorPadding();
            }
            updatePasswordToggleView();
            updateLabelState(false, true);
            return;
        }
        throw new IllegalArgumentException("We already have an EditText, can only have one");
    }

    private void updateInputLayoutMargins() {
        int newTopMargin;
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) this.mInputFrame.getLayoutParams();
        if (this.mHintEnabled) {
            if (this.mTmpPaint == null) {
                this.mTmpPaint = new Paint();
            }
            this.mTmpPaint.setTypeface(this.mCollapsingTextHelper.getCollapsedTypeface());
            this.mTmpPaint.setTextSize(this.mCollapsingTextHelper.getCollapsedTextSize());
            newTopMargin = (int) (-this.mTmpPaint.ascent());
        } else {
            newTopMargin = 0;
        }
        if (newTopMargin != lp.topMargin) {
            lp.topMargin = newTopMargin;
            this.mInputFrame.requestLayout();
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void updateLabelState(boolean animate) {
        updateLabelState(animate, false);
    }

    /* Access modifiers changed, original: 0000 */
    public void updateLabelState(boolean animate, boolean force) {
        boolean isEnabled = isEnabled();
        boolean hasText = (this.mEditText == null || TextUtils.isEmpty(this.mEditText.getText())) ? false : true;
        boolean isFocused = arrayContains(getDrawableState(), 16842908);
        boolean isErrorShowing = true ^ TextUtils.isEmpty(getError());
        if (this.mDefaultTextColor != null) {
            this.mCollapsingTextHelper.setExpandedTextColor(this.mDefaultTextColor);
        }
        if (isEnabled && this.mCounterOverflowed && this.mCounterView != null) {
            this.mCollapsingTextHelper.setCollapsedTextColor(this.mCounterView.getTextColors());
        } else if (!((isEnabled && isFocused && this.mFocusedTextColor != null) || this.mDefaultTextColor == null)) {
            this.mCollapsingTextHelper.setCollapsedTextColor(this.mDefaultTextColor);
        }
        if (hasText || (isEnabled() && (isFocused || isErrorShowing))) {
            if (force || this.mHintExpanded) {
                collapseHint(animate);
            }
        } else if (force || !this.mHintExpanded) {
            expandHint(animate);
        }
    }

    @Nullable
    public EditText getEditText() {
        return this.mEditText;
    }

    public void setHint(@Nullable CharSequence hint) {
        if (this.mHintEnabled) {
            setHintInternal(hint);
            sendAccessibilityEvent(2048);
        }
    }

    private void setHintInternal(CharSequence hint) {
        this.mHint = hint;
        this.mCollapsingTextHelper.setText(hint);
    }

    @Nullable
    public CharSequence getHint() {
        return this.mHintEnabled ? this.mHint : null;
    }

    public void setHintEnabled(boolean enabled) {
        if (enabled != this.mHintEnabled) {
            this.mHintEnabled = enabled;
            CharSequence editTextHint = this.mEditText.getHint();
            if (!this.mHintEnabled) {
                if (!TextUtils.isEmpty(this.mHint) && TextUtils.isEmpty(editTextHint)) {
                    this.mEditText.setHint(this.mHint);
                }
                setHintInternal(null);
            } else if (!TextUtils.isEmpty(editTextHint)) {
                if (TextUtils.isEmpty(this.mHint)) {
                    setHint(editTextHint);
                }
                this.mEditText.setHint(null);
            }
            if (this.mEditText != null) {
                updateInputLayoutMargins();
            }
        }
    }

    public boolean isHintEnabled() {
        return this.mHintEnabled;
    }

    public void setHintTextAppearance(@StyleRes int resId) {
        this.mCollapsingTextHelper.setCollapsedTextAppearance(resId);
        this.mFocusedTextColor = this.mCollapsingTextHelper.getCollapsedTextColor();
        if (this.mEditText != null) {
            updateLabelState(false);
            updateInputLayoutMargins();
        }
    }

    private void addIndicator(TextView indicator, int index) {
        if (this.mIndicatorArea == null) {
            this.mIndicatorArea = new LinearLayout(getContext());
            this.mIndicatorArea.setOrientation(0);
            addView(this.mIndicatorArea, -1, -2);
            this.mIndicatorArea.addView(new Space(getContext()), new LinearLayout.LayoutParams(0, 0, 1.0f));
            if (this.mEditText != null) {
                adjustIndicatorPadding();
            }
        }
        this.mIndicatorArea.setVisibility(0);
        this.mIndicatorArea.addView(indicator, index);
        this.mIndicatorsAdded++;
    }

    private void adjustIndicatorPadding() {
        ViewCompat.setPaddingRelative(this.mIndicatorArea, ViewCompat.getPaddingStart(this.mEditText), 0, ViewCompat.getPaddingEnd(this.mEditText), this.mEditText.getPaddingBottom());
    }

    private void removeIndicator(TextView indicator) {
        if (this.mIndicatorArea != null) {
            this.mIndicatorArea.removeView(indicator);
            int i = this.mIndicatorsAdded - 1;
            this.mIndicatorsAdded = i;
            if (i == 0) {
                this.mIndicatorArea.setVisibility(8);
            }
        }
    }

    public void setErrorEnabled(boolean enabled) {
        if (this.mErrorEnabled != enabled) {
            if (this.mErrorView != null) {
                this.mErrorView.animate().cancel();
            }
            if (enabled) {
                this.mErrorView = new TextView(getContext());
                this.mErrorView.setId(R.id.op_textinput_error);
                if (this.mTypeface != null) {
                    this.mErrorView.setTypeface(this.mTypeface);
                }
                boolean useDefaultColor = false;
                try {
                    TextViewCompat.setTextAppearance(this.mErrorView, this.mErrorTextAppearance);
                    if (VERSION.SDK_INT >= 23 && this.mErrorView.getTextColors().getDefaultColor() == -65281) {
                        useDefaultColor = true;
                    }
                } catch (Exception e) {
                    useDefaultColor = true;
                }
                if (useDefaultColor) {
                    TextViewCompat.setTextAppearance(this.mErrorView, 16974321);
                    this.mErrorView.setTextColor(ContextCompat.getColor(getContext(), R.color.op_error_color_material_default));
                }
                this.mErrorView.setVisibility(4);
                ViewCompat.setAccessibilityLiveRegion(this.mErrorView, 1);
                addIndicator(this.mErrorView, 0);
            } else {
                this.mErrorShown = false;
                updateEditTextBackground();
                removeIndicator(this.mErrorView);
                this.mErrorView = null;
            }
            this.mErrorEnabled = enabled;
        }
    }

    public void setErrorTextAppearance(@StyleRes int resId) {
        this.mErrorTextAppearance = resId;
        if (this.mErrorView != null) {
            TextViewCompat.setTextAppearance(this.mErrorView, resId);
        }
    }

    public boolean isErrorEnabled() {
        return this.mErrorEnabled;
    }

    public void setError(@Nullable CharSequence error) {
        boolean z = ViewCompat.isLaidOut(this) && isEnabled() && (this.mErrorView == null || !TextUtils.equals(this.mErrorView.getText(), error));
        setError(error, z, true);
    }

    private void setError(@Nullable final CharSequence error, boolean animate, boolean shakeAnimate) {
        this.mError = error;
        if (!this.mErrorEnabled) {
            if (!TextUtils.isEmpty(error)) {
                setErrorEnabled(true);
            } else {
                return;
            }
        }
        this.mErrorShown = TextUtils.isEmpty(error) ^ 1;
        this.mErrorView.animate().cancel();
        if (this.mErrorShown) {
            this.mErrorView.setText(error);
            this.mErrorView.setVisibility(0);
            if (shakeAnimate) {
                ValueAnimator mOPAlarmAnimator = ObjectAnimator.ofFloat(this.mErrorView, View.TRANSLATION_X, new float[]{0.0f, 15.0f, 0.0f});
                mOPAlarmAnimator.setDuration(30).setInterpolator(AnimatorUtils.FastOutLinearInInterpolatorSine);
                mOPAlarmAnimator.setRepeatCount(4);
                mOPAlarmAnimator.start();
            }
            if (animate) {
                if (this.mErrorView.getAlpha() == 1.0f) {
                    this.mErrorView.setAlpha(0.0f);
                }
                this.mErrorView.animate().alpha(1.0f).setDuration(200).setInterpolator(AnimatorUtils.LinearOutSlowInInterpolator).setListener(new AnimatorListenerAdapter() {
                    public void onAnimationStart(Animator animator) {
                        OPTextInputLayout.this.mErrorView.setVisibility(0);
                    }
                }).start();
            } else {
                this.mErrorView.setAlpha(1.0f);
            }
        } else if (this.mErrorView.getVisibility() == 0) {
            if (animate) {
                this.mErrorView.animate().alpha(0.0f).setDuration(200).setInterpolator(AnimatorUtils.FastOutLinearInInterpolator).setListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animator) {
                        OPTextInputLayout.this.mErrorView.setText(error);
                        OPTextInputLayout.this.mErrorView.setVisibility(4);
                    }
                }).start();
            } else {
                this.mErrorView.setText(error);
                this.mErrorView.setVisibility(4);
            }
        }
        updateEditTextBackground();
        updateLabelState(animate);
    }

    public void setCounterEnabled(boolean enabled) {
        if (this.mCounterEnabled != enabled) {
            if (enabled) {
                this.mCounterView = new TextView(getContext());
                this.mCounterView.setId(R.id.op_textinput_counter);
                if (this.mTypeface != null) {
                    this.mCounterView.setTypeface(this.mTypeface);
                }
                this.mCounterView.setMaxLines(1);
                try {
                    TextViewCompat.setTextAppearance(this.mCounterView, this.mCounterTextAppearance);
                } catch (Exception e) {
                    TextViewCompat.setTextAppearance(this.mCounterView, 16974321);
                    this.mCounterView.setTextColor(ContextCompat.getColor(getContext(), R.color.op_error_color_material_default));
                }
                addIndicator(this.mCounterView, -1);
                if (this.mEditText == null) {
                    updateCounter(0);
                } else {
                    updateCounter(this.mEditText.getText().length());
                }
            } else {
                removeIndicator(this.mCounterView);
                this.mCounterView = null;
            }
            this.mCounterEnabled = enabled;
        }
    }

    public boolean isCounterEnabled() {
        return this.mCounterEnabled;
    }

    public void setCounterMaxLength(int maxLength) {
        if (this.mCounterMaxLength != maxLength) {
            if (maxLength > 0) {
                this.mCounterMaxLength = maxLength;
            } else {
                this.mCounterMaxLength = -1;
            }
            if (this.mCounterEnabled) {
                updateCounter(this.mEditText == null ? 0 : this.mEditText.getText().length());
            }
        }
    }

    public void setEnabled(boolean enabled) {
        recursiveSetEnabled(this, enabled);
        super.setEnabled(enabled);
    }

    private static void recursiveSetEnabled(ViewGroup vg, boolean enabled) {
        int count = vg.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = vg.getChildAt(i);
            child.setEnabled(enabled);
            if (child instanceof ViewGroup) {
                recursiveSetEnabled((ViewGroup) child, enabled);
            }
        }
    }

    public int getCounterMaxLength() {
        return this.mCounterMaxLength;
    }

    /* Access modifiers changed, original: 0000 */
    public void updateCounter(int length) {
        boolean wasCounterOverflowed = this.mCounterOverflowed;
        if (this.mCounterMaxLength == -1) {
            this.mCounterView.setText(String.valueOf(length));
            this.mCounterOverflowed = false;
        } else {
            this.mCounterOverflowed = length > this.mCounterMaxLength;
            if (wasCounterOverflowed != this.mCounterOverflowed) {
                TextViewCompat.setTextAppearance(this.mCounterView, this.mCounterOverflowed ? this.mCounterOverflowTextAppearance : this.mCounterTextAppearance);
            }
            this.mCounterView.setText(getContext().getString(R.string.op_character_counter_pattern, new Object[]{Integer.valueOf(length), Integer.valueOf(this.mCounterMaxLength)}));
        }
        if (this.mEditText != null && wasCounterOverflowed != this.mCounterOverflowed) {
            updateLabelState(false);
            updateEditTextBackground();
        }
    }

    private void updateEditTextBackground() {
        if (this.mEditText != null) {
            Drawable editTextBackground = this.mEditText.getBackground();
            if (editTextBackground != null) {
                ensureBackgroundDrawableStateWorkaround();
                if (OPDrawableUtils.canSafelyMutateDrawable(editTextBackground)) {
                    editTextBackground = editTextBackground.mutate();
                }
                if ((!this.mErrorShown || this.mErrorView == null) && (!this.mCounterOverflowed || this.mCounterView == null)) {
                    DrawableCompat.clearColorFilter(editTextBackground);
                    this.mEditText.refreshDrawableState();
                }
            }
        }
    }

    private void ensureBackgroundDrawableStateWorkaround() {
        int sdk = VERSION.SDK_INT;
        if (sdk == 21 || sdk == 22) {
            Drawable bg = this.mEditText.getBackground();
            if (!(bg == null || this.mHasReconstructedEditTextBackground)) {
                Drawable newBg = bg.getConstantState().newDrawable();
                if (bg instanceof DrawableContainer) {
                    this.mHasReconstructedEditTextBackground = OPDrawableUtils.setContainerConstantState((DrawableContainer) bg, newBg.getConstantState());
                }
                if (!this.mHasReconstructedEditTextBackground) {
                    ViewCompat.setBackground(this.mEditText, newBg);
                    this.mHasReconstructedEditTextBackground = true;
                }
            }
        }
    }

    public Parcelable onSaveInstanceState() {
        SavedState ss = new SavedState(super.onSaveInstanceState());
        if (this.mErrorShown) {
            ss.error = getError();
        }
        return ss;
    }

    /* Access modifiers changed, original: protected */
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            SavedState ss = (SavedState) state;
            super.onRestoreInstanceState(ss.getSuperState());
            setError(ss.error);
            requestLayout();
            return;
        }
        super.onRestoreInstanceState(state);
    }

    /* Access modifiers changed, original: protected */
    public void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        this.mRestoringSavedState = true;
        super.dispatchRestoreInstanceState(container);
        this.mRestoringSavedState = false;
    }

    @Nullable
    public CharSequence getError() {
        return this.mErrorEnabled ? this.mError : null;
    }

    public boolean isHintAnimationEnabled() {
        return this.mHintAnimationEnabled;
    }

    public void setHintAnimationEnabled(boolean enabled) {
        this.mHintAnimationEnabled = enabled;
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (this.mHintEnabled) {
            this.mCollapsingTextHelper.draw(canvas);
        }
    }

    /* Access modifiers changed, original: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        updatePasswordToggleView();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void updatePasswordToggleView() {
        if (this.mEditText != null) {
            Drawable[] compounds;
            if (shouldShowPasswordIcon()) {
                if (this.mPasswordToggleView == null) {
                    this.mPasswordToggleView = (OPCheckableImageButton) LayoutInflater.from(getContext()).inflate(R.layout.op_design_text_input_password_icon, this.mInputFrame, false);
                    this.mPasswordToggleView.setImageDrawable(this.mPasswordToggleDrawable);
                    this.mPasswordToggleView.setContentDescription(this.mPasswordToggleContentDesc);
                    this.mInputFrame.addView(this.mPasswordToggleView);
                    this.mPasswordToggleView.setOnClickListener(new OnClickListener() {
                        public void onClick(View view) {
                            OPTextInputLayout.this.passwordVisibilityToggleRequested();
                        }
                    });
                }
                if (this.mEditText != null && ViewCompat.getMinimumHeight(this.mEditText) <= 0) {
                    this.mEditText.setMinimumHeight(ViewCompat.getMinimumHeight(this.mPasswordToggleView));
                }
                this.mPasswordToggleView.setVisibility(0);
                this.mPasswordToggleView.setChecked(this.mPasswordToggledVisible);
                if (this.mPasswordToggleDummyDrawable == null) {
                    this.mPasswordToggleDummyDrawable = new ColorDrawable();
                }
                this.mPasswordToggleDummyDrawable.setBounds(0, 0, this.mPasswordToggleView.getMeasuredWidth(), 1);
                compounds = TextViewCompat.getCompoundDrawablesRelative(this.mEditText);
                if (compounds[2] != this.mPasswordToggleDummyDrawable) {
                    this.mOriginalEditTextEndDrawable = compounds[2];
                }
                TextViewCompat.setCompoundDrawablesRelative(this.mEditText, compounds[0], compounds[1], this.mPasswordToggleDummyDrawable, compounds[3]);
                this.mPasswordToggleView.setPadding(this.mEditText.getPaddingLeft(), this.mEditText.getPaddingTop(), this.mEditText.getPaddingRight(), this.mEditText.getPaddingBottom());
            } else {
                if (this.mPasswordToggleView != null && this.mPasswordToggleView.getVisibility() == 0) {
                    this.mPasswordToggleView.setVisibility(8);
                }
                if (this.mPasswordToggleDummyDrawable != null) {
                    compounds = TextViewCompat.getCompoundDrawablesRelative(this.mEditText);
                    if (compounds[2] == this.mPasswordToggleDummyDrawable) {
                        TextViewCompat.setCompoundDrawablesRelative(this.mEditText, compounds[0], compounds[1], this.mOriginalEditTextEndDrawable, compounds[3]);
                        this.mPasswordToggleDummyDrawable = null;
                    }
                }
            }
        }
    }

    public void setPasswordVisibilityToggleDrawable(@DrawableRes int resId) {
        Drawable drawable;
        if (resId != 0) {
            drawable = getResources().getDrawable(resId);
        } else {
            drawable = null;
        }
        setPasswordVisibilityToggleDrawable(drawable);
    }

    public void setPasswordVisibilityToggleDrawable(@Nullable Drawable icon) {
        this.mPasswordToggleDrawable = icon;
        if (this.mPasswordToggleView != null) {
            this.mPasswordToggleView.setImageDrawable(icon);
        }
    }

    public void setPasswordVisibilityToggleContentDescription(@StringRes int resId) {
        setPasswordVisibilityToggleContentDescription(resId != 0 ? getResources().getText(resId) : null);
    }

    public void setPasswordVisibilityToggleContentDescription(@Nullable CharSequence description) {
        this.mPasswordToggleContentDesc = description;
        if (this.mPasswordToggleView != null) {
            this.mPasswordToggleView.setContentDescription(description);
        }
    }

    @Nullable
    public Drawable getPasswordVisibilityToggleDrawable() {
        return this.mPasswordToggleDrawable;
    }

    @Nullable
    public CharSequence getPasswordVisibilityToggleContentDescription() {
        return this.mPasswordToggleContentDesc;
    }

    public boolean isPasswordVisibilityToggleEnabled() {
        return this.mPasswordToggleEnabled;
    }

    public void setPasswordVisibilityToggleEnabled(boolean enabled) {
        if (this.mPasswordToggleEnabled != enabled) {
            this.mPasswordToggleEnabled = enabled;
            if (!(enabled || !this.mPasswordToggledVisible || this.mEditText == null)) {
                this.mEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            this.mPasswordToggledVisible = false;
            updatePasswordToggleView();
        }
    }

    public void setPasswordVisibilityToggleTintList(@Nullable ColorStateList tintList) {
        this.mPasswordToggleTintList = tintList;
        this.mHasPasswordToggleTintList = true;
        applyPasswordToggleTint();
    }

    public void setPasswordVisibilityToggleTintMode(@Nullable Mode mode) {
        this.mPasswordToggleTintMode = mode;
        this.mHasPasswordToggleTintMode = true;
        applyPasswordToggleTint();
    }

    /* Access modifiers changed, original: 0000 */
    public void passwordVisibilityToggleRequested() {
        if (this.mPasswordToggleEnabled) {
            int selection = this.mEditText.getSelectionEnd();
            if (hasPasswordTransformation()) {
                this.mEditText.setTransformationMethod(null);
                this.mPasswordToggledVisible = true;
            } else {
                this.mEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                this.mPasswordToggledVisible = false;
            }
            this.mPasswordToggleView.setChecked(this.mPasswordToggledVisible);
            this.mEditText.setSelection(selection);
        }
    }

    private boolean hasPasswordTransformation() {
        return this.mEditText != null && (this.mEditText.getTransformationMethod() instanceof PasswordTransformationMethod);
    }

    private boolean shouldShowPasswordIcon() {
        return this.mPasswordToggleEnabled && (hasPasswordTransformation() || this.mPasswordToggledVisible);
    }

    private void applyPasswordToggleTint() {
        if (this.mPasswordToggleDrawable == null) {
            return;
        }
        if (this.mHasPasswordToggleTintList || this.mHasPasswordToggleTintMode) {
            this.mPasswordToggleDrawable = DrawableCompat.wrap(this.mPasswordToggleDrawable).mutate();
            if (this.mHasPasswordToggleTintList) {
                DrawableCompat.setTintList(this.mPasswordToggleDrawable, this.mPasswordToggleTintList);
            }
            if (this.mHasPasswordToggleTintMode) {
                DrawableCompat.setTintMode(this.mPasswordToggleDrawable, this.mPasswordToggleTintMode);
            }
            if (this.mPasswordToggleView != null && this.mPasswordToggleView.getDrawable() != this.mPasswordToggleDrawable) {
                this.mPasswordToggleView.setImageDrawable(this.mPasswordToggleDrawable);
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (this.mHintEnabled && this.mEditText != null) {
            Rect rect = this.mTmpRect;
            OPViewGroupUtils.getDescendantRect(this, this.mEditText, rect);
            int l = rect.left + this.mEditText.getCompoundPaddingLeft();
            int r = rect.right - this.mEditText.getCompoundPaddingRight();
            this.mCollapsingTextHelper.setExpandedBounds(l, rect.top + this.mEditText.getCompoundPaddingTop(), r, rect.bottom - this.mEditText.getCompoundPaddingBottom());
            this.mCollapsingTextHelper.setCollapsedBounds(l, getPaddingTop(), r, (bottom - top) - getPaddingBottom());
            this.mCollapsingTextHelper.recalculate();
        }
    }

    private void collapseHint(boolean animate) {
        if (this.mAnimator != null && this.mAnimator.isRunning()) {
            this.mAnimator.cancel();
        }
        if (animate && this.mHintAnimationEnabled) {
            animateToExpansionFraction(1.0f);
        } else {
            this.mCollapsingTextHelper.setExpansionFraction(1.0f);
        }
        this.mHintExpanded = false;
    }

    /* Access modifiers changed, original: protected */
    public void drawableStateChanged() {
        if (!this.mInDrawableStateChanged) {
            boolean z = true;
            this.mInDrawableStateChanged = true;
            super.drawableStateChanged();
            int[] state = getDrawableState();
            boolean changed = false;
            if (!(ViewCompat.isLaidOut(this) && isEnabled())) {
                z = false;
            }
            updateLabelState(z);
            updateEditTextBackground();
            if (this.mCollapsingTextHelper != null) {
                changed = false | this.mCollapsingTextHelper.setState(state);
            }
            if (changed) {
                invalidate();
            }
            this.mInDrawableStateChanged = false;
        }
    }

    private void expandHint(boolean animate) {
        if (this.mAnimator != null && this.mAnimator.isRunning()) {
            this.mAnimator.cancel();
        }
        if (animate && this.mHintAnimationEnabled) {
            animateToExpansionFraction(0.0f);
        } else {
            this.mCollapsingTextHelper.setExpansionFraction(0.0f);
        }
        this.mHintExpanded = true;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void animateToExpansionFraction(float target) {
        if (this.mCollapsingTextHelper.getExpansionFraction() != target) {
            if (this.mAnimator == null) {
                this.mAnimator = new ValueAnimator();
                this.mAnimator.setInterpolator(AnimatorUtils.FastOutSlowInInterpolator);
                this.mAnimator.setDuration(225);
                this.mAnimator.addUpdateListener(new AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator animator) {
                        OPTextInputLayout.this.mCollapsingTextHelper.setExpansionFraction(((Float) animator.getAnimatedValue()).floatValue());
                    }
                });
            }
            this.mAnimator.setFloatValues(new float[]{this.mCollapsingTextHelper.getExpansionFraction(), target});
            this.mAnimator.start();
        }
    }

    /* Access modifiers changed, original: final */
    @VisibleForTesting
    public final boolean isHintExpanded() {
        return this.mHintExpanded;
    }

    private static boolean arrayContains(int[] array, int value) {
        for (int v : array) {
            if (v == value) {
                return true;
            }
        }
        return false;
    }
}
