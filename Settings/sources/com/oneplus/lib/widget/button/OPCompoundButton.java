package com.oneplus.lib.widget.button;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View.BaseSavedState;
import android.view.ViewDebug.ExportedProperty;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.Checkable;
import com.oneplus.commonctrl.R;

public abstract class OPCompoundButton extends Button implements Checkable {
    private static final int[] CHECKED_STATE_SET = new int[]{16842912};
    private static final int[] INDETERMINATE_STATE_SET = new int[]{R.attr.state_indeterminate};
    public static String TAG = OPCompoundButton.class.getSimpleName();
    private boolean mBroadcasting;
    private Drawable mButtonDrawable;
    private int mButtonResource;
    private ColorStateList mButtonTintList;
    private Mode mButtonTintMode;
    private boolean mChecked;
    private boolean mHasButtonTint;
    private boolean mHasButtonTintMode;
    private boolean mIndeterminate;
    private OnCheckedChangeListener mOnCheckedChangeListener;
    private OnCheckedChangeListener mOnCheckedChangeWidgetListener;
    private OnTriStateCheckedChangeListener mOnTriStateCheckedChangeListener;
    private boolean mThreeState;

    public interface OnCheckedChangeListener {
        void onCheckedChanged(OPCompoundButton oPCompoundButton, boolean z);
    }

    public interface OnTriStateCheckedChangeListener {
        void onCheckedChanged(OPCompoundButton oPCompoundButton, Boolean bool);
    }

    static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        boolean checked;
        boolean indeterminate;
        boolean threeState;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            this.checked = ((Boolean) in.readValue(null)).booleanValue();
            this.threeState = ((Boolean) in.readValue(null)).booleanValue();
            this.indeterminate = ((Boolean) in.readValue(null)).booleanValue();
        }

        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeValue(Boolean.valueOf(this.checked));
            out.writeValue(Boolean.valueOf(this.threeState));
            out.writeValue(Boolean.valueOf(this.indeterminate));
        }

        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("CompoundButton.SavedState{");
            stringBuilder.append(Integer.toHexString(System.identityHashCode(this)));
            stringBuilder.append(" checked=");
            stringBuilder.append(this.checked);
            stringBuilder.append(", indeterminate=");
            stringBuilder.append(this.indeterminate);
            stringBuilder.append(", threeState=");
            stringBuilder.append(this.threeState);
            stringBuilder.append("}");
            return stringBuilder.toString();
        }
    }

    public OPCompoundButton(Context context) {
        this(context, null);
    }

    public OPCompoundButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OPCompoundButton(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public OPCompoundButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        Boolean bool = null;
        this.mButtonTintList = null;
        this.mButtonTintMode = null;
        this.mHasButtonTint = false;
        this.mHasButtonTintMode = false;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.OPCompoundbutton, defStyleAttr, defStyleRes);
        Drawable d = a.getDrawable(R.styleable.OPCompoundbutton_android_button);
        if (d != null) {
            setButtonDrawable(d);
        }
        if (a.hasValue(R.styleable.OPCompoundbutton_android_buttonTintMode)) {
            this.mButtonTintMode = parseTintMode(a.getInt(R.styleable.OPCompoundbutton_android_buttonTintMode, -1), this.mButtonTintMode);
            this.mHasButtonTintMode = true;
        }
        if (a.hasValue(R.styleable.OPCompoundbutton_android_buttonTint)) {
            this.mButtonTintList = a.getColorStateList(R.styleable.OPCompoundbutton_android_buttonTint);
            this.mHasButtonTint = true;
        }
        boolean threeState = a.getBoolean(R.styleable.OPCompoundbutton_threeState, false);
        boolean checked = a.getBoolean(R.styleable.OPCompoundbutton_android_checked, false);
        boolean indeterminate = a.getBoolean(R.styleable.OPCompoundbutton_indeterminate, false);
        setThreeState(threeState);
        if (indeterminate) {
            if (!indeterminate) {
                bool = Boolean.valueOf(checked);
            }
            setTriStateChecked(bool);
        } else {
            setCheckedInternal(checked);
        }
        setRadius(a.getDimensionPixelSize(R.styleable.OPCompoundbutton_android_radius, -1));
        a.recycle();
        applyButtonTint();
    }

    private void setRadius(int nRadius) {
        if (nRadius != -1) {
            Drawable background = getBackground();
            if (background == null || !(background instanceof RippleDrawable)) {
                Log.i(TAG, "setRaidus fail , background not a rippleDrawable");
            } else {
                background.mutate();
                ((RippleDrawable) background).setRadius(nRadius);
            }
        }
    }

    private static Mode parseTintMode(int value, Mode defaultMode) {
        if (value == 3) {
            return Mode.SRC_OVER;
        }
        if (value == 5) {
            return Mode.SRC_IN;
        }
        if (value == 9) {
            return Mode.SRC_ATOP;
        }
        switch (value) {
            case 14:
                return Mode.MULTIPLY;
            case 15:
                return Mode.SCREEN;
            case 16:
                return Mode.ADD;
            default:
                return defaultMode;
        }
    }

    public void toggle() {
        setCheckedInternal(this.mChecked ^ 1);
    }

    public boolean performClick() {
        if (!this.mThreeState) {
            toggle();
        } else if (this.mIndeterminate) {
            setTriStateChecked(Boolean.valueOf(true));
        } else {
            setTriStateChecked(Boolean.valueOf(this.mChecked ^ 1));
        }
        boolean handled = super.performClick();
        if (!handled) {
            playSoundEffect(0);
        }
        return handled;
    }

    public void setThreeState(boolean threeState) {
        this.mThreeState = threeState;
    }

    @ExportedProperty
    public boolean isThreeState() {
        return this.mThreeState;
    }

    @ExportedProperty
    public boolean isIndeterminate() {
        return this.mIndeterminate;
    }

    @ExportedProperty
    public boolean isChecked() {
        return this.mChecked;
    }

    public void setChecked(boolean checked) {
        setCheckedInternal(checked);
    }

    public void setCheckedInternal(boolean checked) {
        setCheckedInternal(checked, false);
    }

    public void setCheckedInternal(boolean checked, boolean force) {
        boolean checkedChanged = this.mChecked != checked;
        if (checkedChanged || force) {
            this.mChecked = checked;
            refreshDrawableState();
            notifyViewAccessibilityStateChangedIfNeededInternal(0);
            if (checkedChanged && !this.mBroadcasting) {
                this.mBroadcasting = true;
                if (this.mOnCheckedChangeListener != null) {
                    this.mOnCheckedChangeListener.onCheckedChanged(this, this.mChecked);
                }
                if (this.mOnCheckedChangeWidgetListener != null) {
                    this.mOnCheckedChangeWidgetListener.onCheckedChanged(this, this.mChecked);
                }
                this.mBroadcasting = false;
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public void setTriStateChecked(Boolean checked) {
        if (!(this.mIndeterminate == (checked == null) && (checked == null || checked.booleanValue() == this.mChecked))) {
            this.mIndeterminate = checked == null;
            if (checked != null) {
                setCheckedInternal(checked.booleanValue(), true);
            } else {
                refreshDrawableState();
                notifyViewAccessibilityStateChangedIfNeededInternal(0);
            }
            if (!this.mBroadcasting) {
                this.mBroadcasting = true;
                if (this.mOnTriStateCheckedChangeListener != null) {
                    this.mOnTriStateCheckedChangeListener.onCheckedChanged(this, checked);
                }
                this.mBroadcasting = false;
            }
        }
    }

    private void notifyViewAccessibilityStateChangedIfNeededInternal(int changeType) {
        try {
            Class.forName("android.view.View").getMethod("notifyViewAccessibilityStateChangedIfNeeded", new Class[]{Integer.TYPE}).invoke(this, new Object[]{Integer.valueOf(changeType)});
        } catch (Exception e) {
            Log.e(TAG, "notifyViewAccessibilityStateChangedIfNeeded with Exception!", e);
        }
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        this.mOnCheckedChangeListener = listener;
    }

    public void setOnTriStateCheckedChangeListener(OnTriStateCheckedChangeListener listener) {
        this.mOnTriStateCheckedChangeListener = listener;
    }

    /* Access modifiers changed, original: 0000 */
    public void setOnCheckedChangeWidgetListener(OnCheckedChangeListener listener) {
        this.mOnCheckedChangeWidgetListener = listener;
    }

    public void setButtonDrawable(int resid) {
        if (resid == 0 || resid != this.mButtonResource) {
            this.mButtonResource = resid;
            Drawable d = null;
            if (this.mButtonResource != 0) {
                d = getContext().getDrawable(this.mButtonResource);
            }
            setButtonDrawable(d);
        }
    }

    public void setButtonDrawable(Drawable d) {
        if (this.mButtonDrawable != d) {
            if (this.mButtonDrawable != null) {
                this.mButtonDrawable.setCallback(null);
                unscheduleDrawable(this.mButtonDrawable);
            }
            this.mButtonDrawable = d;
            if (d != null) {
                d.setCallback(this);
                boolean z = true;
                try {
                    Class.forName("android.graphics.drawable.Drawable").getMethod("setLayoutDirection", new Class[]{Integer.TYPE}).invoke(d, new Object[]{Integer.valueOf(getLayoutDirection())});
                } catch (Exception e) {
                    Log.e(TAG, "setLayoutDirection with Exception!", e);
                }
                if (d.isStateful()) {
                    d.setState(getDrawableState());
                }
                if (getVisibility() != 0) {
                    z = false;
                }
                d.setVisible(z, false);
                setMinHeight(d.getIntrinsicHeight());
                applyButtonTint();
            }
        }
    }

    public Drawable getButtonDrawable() {
        return this.mButtonDrawable;
    }

    public void setButtonTintList(ColorStateList tint) {
        this.mButtonTintList = tint;
        this.mHasButtonTint = true;
        applyButtonTint();
    }

    public ColorStateList getButtonTintList() {
        return this.mButtonTintList;
    }

    public void setButtonTintMode(Mode tintMode) {
        this.mButtonTintMode = tintMode;
        this.mHasButtonTintMode = true;
        applyButtonTint();
    }

    public Mode getButtonTintMode() {
        return this.mButtonTintMode;
    }

    private void applyButtonTint() {
        if (this.mButtonDrawable == null) {
            return;
        }
        if (this.mHasButtonTint || this.mHasButtonTintMode) {
            this.mButtonDrawable = this.mButtonDrawable.mutate();
            if (this.mHasButtonTint) {
                this.mButtonDrawable.setTintList(this.mButtonTintList);
            }
            if (this.mHasButtonTintMode) {
                this.mButtonDrawable.setTintMode(this.mButtonTintMode);
            }
            if (this.mButtonDrawable.isStateful()) {
                this.mButtonDrawable.setState(getDrawableState());
            }
        }
    }

    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(OPCompoundButton.class.getName());
        event.setChecked(this.mChecked);
    }

    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(OPCompoundButton.class.getName());
        info.setCheckable(true);
        info.setChecked(this.mChecked);
    }

    public int getCompoundPaddingLeft() {
        int padding = super.getCompoundPaddingLeft();
        if (isLayoutRtl()) {
            return padding;
        }
        Drawable buttonDrawable = this.mButtonDrawable;
        if (buttonDrawable != null) {
            return padding + buttonDrawable.getIntrinsicWidth();
        }
        return padding;
    }

    public int getCompoundPaddingRight() {
        int padding = super.getCompoundPaddingRight();
        if (!isLayoutRtl()) {
            return padding;
        }
        Drawable buttonDrawable = this.mButtonDrawable;
        if (buttonDrawable != null) {
            return padding + buttonDrawable.getIntrinsicWidth();
        }
        return padding;
    }

    public int getHorizontalOffsetForDrawables() {
        Drawable buttonDrawable = this.mButtonDrawable;
        return buttonDrawable != null ? buttonDrawable.getIntrinsicWidth() : 0;
    }

    /* Access modifiers changed, original: protected */
    public void onDraw(Canvas canvas) {
        int verticalGravity;
        int drawableHeight;
        Drawable buttonDrawable = this.mButtonDrawable;
        if (buttonDrawable != null) {
            int top;
            verticalGravity = getGravity() & 112;
            drawableHeight = buttonDrawable.getIntrinsicHeight();
            int drawableWidth = buttonDrawable.getIntrinsicWidth();
            int left = 0;
            if (verticalGravity == 16) {
                top = (getHeight() - drawableHeight) / 2;
            } else if (verticalGravity != 80) {
                top = 0;
            } else {
                top = getHeight() - drawableHeight;
            }
            int bottom = top + drawableHeight;
            if (isLayoutRtl()) {
                left = getWidth() - drawableWidth;
            }
            int right = isLayoutRtl() ? getWidth() : drawableWidth;
            buttonDrawable.setBounds(left, top, right, bottom);
            Drawable background = getBackground();
            if (background != null) {
                background.setHotspotBounds(left, top, right, bottom);
            }
        }
        super.onDraw(canvas);
        if (buttonDrawable != null) {
            verticalGravity = getScrollX();
            drawableHeight = getScrollY();
            if (verticalGravity == 0 && drawableHeight == 0) {
                buttonDrawable.draw(canvas);
                return;
            }
            canvas.translate((float) verticalGravity, (float) drawableHeight);
            buttonDrawable.draw(canvas);
            canvas.translate((float) (-verticalGravity), (float) (-drawableHeight));
        }
    }

    /* Access modifiers changed, original: protected */
    public int[] onCreateDrawableState(int extraSpace) {
        int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isIndeterminate()) {
            mergeDrawableStates(drawableState, INDETERMINATE_STATE_SET);
        } else if (isChecked()) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }
        return drawableState;
    }

    /* Access modifiers changed, original: protected */
    public void drawableStateChanged() {
        super.drawableStateChanged();
        if (this.mButtonDrawable != null) {
            this.mButtonDrawable.setState(getDrawableState());
            invalidate();
        }
    }

    public void drawableHotspotChanged(float x, float y) {
        super.drawableHotspotChanged(x, y);
        if (this.mButtonDrawable != null) {
            this.mButtonDrawable.setHotspot(x, y);
        }
    }

    /* Access modifiers changed, original: protected */
    public boolean verifyDrawable(Drawable who) {
        return super.verifyDrawable(who) || who == this.mButtonDrawable;
    }

    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        if (this.mButtonDrawable != null) {
            this.mButtonDrawable.jumpToCurrentState();
        }
    }

    public Parcelable onSaveInstanceState() {
        SavedState ss = new SavedState(super.onSaveInstanceState());
        ss.checked = isChecked();
        ss.threeState = this.mThreeState;
        ss.indeterminate = this.mIndeterminate;
        return ss;
    }

    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        this.mThreeState = ss.threeState;
        if (this.mThreeState) {
            setTriStateChecked(ss.indeterminate ? null : Boolean.valueOf(ss.checked));
        } else {
            setCheckedInternal(ss.checked);
        }
        requestLayout();
    }

    public boolean isLayoutRtl() {
        return getLayoutDirection() == 1;
    }
}
