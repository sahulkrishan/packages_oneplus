package com.oneplus.settings.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.support.v7.preference.Preference.BaseSavedState;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.settings.R;
import com.oneplus.settings.utils.OPUtils;
import java.util.Arrays;

public class OPLedColorPickerPreference extends CustomDialogPreference {
    private String mColor;
    private Context mContext;
    private String mDefaultColor;
    private String[] mDefaultColors;
    private int mDisabledCellColor;
    ImageView mImageView;
    private TextView mMessage;
    private CharSequence mMessageText;
    private String[] mPalette;
    private int[] mPaletteNamesResIds;
    private int mRippleEffectColor;
    private String mTmpColor;
    private boolean mUseColorLabelAsSummary;
    private View[] mViews;
    private boolean mVisibility;

    private static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        String tmpColor;

        public SavedState(Parcel source) {
            super(source);
            try {
                this.tmpColor = source.readString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(this.tmpColor);
        }
    }

    public OPLedColorPickerPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mDefaultColor = "";
        this.mVisibility = false;
        setLayoutResource(R.layout.op_colorpicker_preference);
        this.mContext = context;
        this.mDefaultColors = new String[]{this.mContext.getResources().getString(R.color.op_primary_default_light), this.mContext.getResources().getString(R.color.op_primary_default_dark)};
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ColorPickerPreference, 0, 0);
        this.mRippleEffectColor = a.getColor(1, context.getResources().getColor(R.color.colorpicker_ripple_effect_color));
        this.mDisabledCellColor = a.getColor(0, context.getResources().getColor(R.color.colorpicker_disabled_cell_color));
        a.recycle();
        setNeutralButtonText((int) R.string.color_picker_default);
        setNegativeButtonText(17039360);
        setPositiveButtonText(17039370);
        setDialogLayoutResource(R.layout.op_led_preference_dialog_colorpicker);
        if (getSummary() == null) {
            this.mUseColorLabelAsSummary = true;
        } else {
            this.mUseColorLabelAsSummary = false;
        }
    }

    public OPLedColorPickerPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public OPLedColorPickerPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OPLedColorPickerPreference(Context context) {
        this(context, null);
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        this.mImageView = (ImageView) view.findViewById(R.id.secondary_icon);
        if (this.mImageView != null) {
            String showColor;
            Drawable imageDrawable = this.mImageView.getDrawable();
            if (this.mColor == null || TextUtils.isEmpty(this.mColor)) {
                showColor = this.mDefaultColor;
            } else {
                showColor = this.mColor;
            }
            this.mImageView.setImageTintList(ColorStateList.valueOf(Color.parseColor(showColor)));
            if (this.mVisibility) {
                this.mImageView.setVisibility(0);
            }
        }
    }

    public void setImageViewVisibility() {
        this.mVisibility = true;
    }

    public void setMessage(CharSequence message) {
        this.mMessage.setText(message);
    }

    public void setMessage(int messageResId) {
        this.mMessage.setText(getContext().getString(messageResId));
    }

    public void setMessageText(CharSequence message) {
        this.mMessageText = message;
    }

    public void setMessageText(int messageResId) {
        this.mMessageText = getContext().getString(messageResId);
    }

    public void init() {
        this.mColor = getColor();
        this.mTmpColor = this.mColor;
        setSelection(this.mTmpColor, 0);
    }

    public void setColor(String color) {
        this.mColor = color;
        updateSummary();
        if (callChangeListener(this.mColor)) {
            onSetColor(this.mColor);
        }
        if (this.mImageView != null) {
            String showColor;
            Drawable imageDrawable = this.mImageView.getDrawable();
            if (this.mColor == null || TextUtils.isEmpty(this.mColor)) {
                showColor = this.mDefaultColor;
            } else {
                showColor = color;
            }
            this.mImageView.setImageTintList(ColorStateList.valueOf(Color.parseColor(showColor)));
        }
        persistString(color);
    }

    public String getColor() {
        return this.mColor != null ? this.mColor : getPersistedString(getDefaultColor());
    }

    public void setDefaultColor(String color) {
        this.mDefaultColor = color;
    }

    public String getDefaultColor() {
        return this.mDefaultColor;
    }

    public void setColorPalette(String[] colors, int[] colorStringResIds) {
        this.mPalette = colors;
        this.mPaletteNamesResIds = colorStringResIds;
    }

    public void setColorPalette(String[] colors) {
        this.mPalette = colors;
    }

    public RippleDrawable createRippleDrawable(String color) {
        return new RippleDrawable(new ColorStateList(new int[][]{new int[0]}, new int[]{this.mRippleEffectColor}), new ColorDrawable(TextUtils.isEmpty(color) ? this.mDisabledCellColor : Color.parseColor(color)), null);
    }

    public void setSelection(String color, int visibility) {
        if (color != null) {
            int index = this.mPalette == null ? -1 : Arrays.asList(this.mPalette).indexOf(color);
            if (index >= 0) {
                this.mViews[index].findViewById(new int[]{R.id.check_0, R.id.check_1, R.id.check_2, R.id.check_3, R.id.check_4, R.id.check_5, R.id.check_6, R.id.check_7}[index]).setVisibility(visibility);
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public void onSetColor(String color) {
    }

    /* Access modifiers changed, original: protected */
    public void onSetColorPalette(String[] colors) {
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
    }

    /* Access modifiers changed, original: protected */
    public void onBindDialogView(View view) {
        super.onBindDialogView(view);
        onSetColorPalette(this.mPalette);
        updateSummary();
        int paletteLenght = this.mPalette.length;
        if (paletteLenght != 0 && paletteLenght <= 4) {
            view.findViewById(R.id.colors_row_2).setVisibility(8);
        }
        int[] ids = new int[]{R.id.color_0, R.id.color_1, R.id.color_2, R.id.color_3, R.id.color_4, R.id.color_5, R.id.color_6, R.id.color_7};
        this.mViews = new View[this.mPalette.length];
        for (int i = 0; i < this.mPalette.length; i++) {
            this.mViews[i] = view.findViewById(ids[i]);
            this.mViews[i].setBackground(createRippleDrawable(this.mPalette[i]));
            this.mViews[i].setTag(Integer.valueOf(i));
            this.mViews[i].setOnClickListener(paletteLenght > 0 ? new OnClickListener() {
                public void onClick(View v) {
                    int index = ((Integer) v.getTag()).intValue();
                    if (!OPLedColorPickerPreference.this.mPalette[index].equals(OPLedColorPickerPreference.this.mTmpColor)) {
                        OPLedColorPickerPreference.this.setSelection(OPLedColorPickerPreference.this.mTmpColor, 8);
                        OPLedColorPickerPreference.this.setTmpColor(OPLedColorPickerPreference.this.mPalette[index]);
                    }
                }
            } : null);
        }
        this.mMessage = (TextView) view.findViewById(R.id.message);
        if (this.mMessageText != null) {
            setMessage(this.mMessageText);
        } else {
            setMessage((int) R.string.color_picker_message_default);
        }
        init();
    }

    /* Access modifiers changed, original: protected */
    public void onDialogClosed(int whichButton) {
        super.onDialogClosed(whichButton);
        boolean equal = false;
        boolean positiveResult = whichButton == -1;
        boolean defaultResult = whichButton == -3;
        if (positiveResult) {
            if ((this.mColor == null && this.mTmpColor == null) || (this.mColor != null && this.mColor.equals(this.mTmpColor))) {
                equal = true;
            }
            if (!equal) {
                setColor(this.mTmpColor);
            }
        } else if (defaultResult) {
            if ((this.mColor == null && this.mDefaultColor == null) || (this.mColor != null && this.mColor.equals(this.mDefaultColor))) {
                equal = true;
            }
            if (!equal) {
                setColor(this.mDefaultColor);
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setColor(restoreValue ? getPersistedString(this.mDefaultColor) : (String) defaultValue);
    }

    /* Access modifiers changed, original: protected */
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        if (this.mTmpColor == null) {
            return superState;
        }
        SavedState myState = new SavedState(superState);
        myState.tmpColor = this.mTmpColor;
        return myState;
    }

    /* Access modifiers changed, original: protected */
    public void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        if (!(myState.tmpColor.equals(this.mTmpColor) || this.mViews == null)) {
            setSelection(this.mTmpColor, 8);
            setTmpColor(myState.tmpColor);
        }
    }

    private void setTmpColor(String color) {
        this.mTmpColor = color;
        setSelection(this.mTmpColor, 0);
    }

    private void updateSummary() {
        if (this.mUseColorLabelAsSummary || (this.mPaletteNamesResIds != null && this.mPaletteNamesResIds.length >= 0)) {
            int index = this.mPalette == null ? -1 : Arrays.asList(this.mPalette).indexOf(this.mColor);
            if (this.mPaletteNamesResIds != null && index >= 0 && index < this.mPaletteNamesResIds.length) {
                setSummary((CharSequence) getContext().getString(this.mPaletteNamesResIds[index]));
            } else if (this.mColor != null && !this.mColor.equals(this.mDefaultColor) && !TextUtils.isEmpty(this.mColor) && !isDefaultColor()) {
                setSummary((CharSequence) getContext().getString(R.string.color_picker_unknown_label));
            } else if (OPUtils.isAndroidModeOn(this.mContext.getContentResolver())) {
                setSummary((CharSequence) getContext().getString(R.string.oneplus_colorful_mode_cannot_change_color_accent));
                if (this.mImageView != null) {
                    this.mImageView.setVisibility(8);
                }
            } else {
                setSummary((CharSequence) getContext().getString(R.string.op_primary_default_light_label));
            }
        }
    }

    private boolean isDefaultColor() {
        for (String equals : this.mDefaultColors) {
            if (equals.equals(this.mColor)) {
                return true;
            }
        }
        return false;
    }
}
