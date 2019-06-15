package com.android.settings.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.support.annotation.ColorInt;
import android.support.annotation.StringRes;
import android.support.annotation.VisibleForTesting;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.TouchDelegate;
import android.view.View;
import android.view.View.BaseSavedState;
import android.view.View.OnClickListener;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;
import java.util.ArrayList;
import java.util.List;

public class SwitchBar extends LinearLayout implements OnCheckedChangeListener {
    private static final int[] XML_ATTRIBUTES = new int[]{R.attr.switchBarMarginStart, R.attr.switchBarMarginEnd, R.attr.switchBarBackgroundColor, R.attr.switchBarBackgroundActivatedColor};
    @ColorInt
    private int mBackgroundActivatedColor;
    @ColorInt
    private int mBackgroundColor;
    private boolean mDisabledByAdmin;
    private EnforcedAdmin mEnforcedAdmin;
    private String mLabel;
    private boolean mLoggingIntialized;
    private final MetricsFeatureProvider mMetricsFeatureProvider;
    private String mMetricsTag;
    @StringRes
    private int mOffTextId;
    @StringRes
    private int mOnTextId;
    private View mRestrictedIcon;
    private String mSummary;
    private final TextAppearanceSpan mSummarySpan;
    private ToggleSwitch mSwitch;
    private final List<OnSwitchChangeListener> mSwitchChangeListeners;
    private TextView mTextView;

    public interface OnSwitchChangeListener {
        void onSwitchChanged(Switch switchR, boolean z);
    }

    static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in, null);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        boolean checked;
        boolean visible;

        /* synthetic */ SavedState(Parcel x0, AnonymousClass1 x1) {
            this(x0);
        }

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            this.checked = ((Boolean) in.readValue(null)).booleanValue();
            this.visible = ((Boolean) in.readValue(null)).booleanValue();
        }

        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeValue(Boolean.valueOf(this.checked));
            out.writeValue(Boolean.valueOf(this.visible));
        }

        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("SwitchBar.SavedState{");
            stringBuilder.append(Integer.toHexString(System.identityHashCode(this)));
            stringBuilder.append(" checked=");
            stringBuilder.append(this.checked);
            stringBuilder.append(" visible=");
            stringBuilder.append(this.visible);
            stringBuilder.append("}");
            return stringBuilder.toString();
        }
    }

    public SwitchBar(Context context) {
        this(context, null);
    }

    public SwitchBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwitchBar(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SwitchBar(final Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mSwitchChangeListeners = new ArrayList();
        this.mEnforcedAdmin = null;
        LayoutInflater.from(context).inflate(R.layout.switch_bar, this);
        TypedArray a = context.obtainStyledAttributes(attrs, XML_ATTRIBUTES);
        int switchBarMarginStart = (int) a.getDimension(0, 0.0f);
        int switchBarMarginEnd = (int) a.getDimension(1, 0.0f);
        this.mBackgroundColor = a.getColor(2, 0);
        this.mBackgroundActivatedColor = a.getColor(3, 0);
        a.recycle();
        this.mTextView = (TextView) findViewById(R.id.switch_text);
        this.mSummarySpan = new TextAppearanceSpan(this.mContext, R.style.f870TextAppearance.Small.SwitchBar);
        ((MarginLayoutParams) this.mTextView.getLayoutParams()).setMarginStart(switchBarMarginStart);
        this.mSwitch = (ToggleSwitch) findViewById(R.id.switch_widget);
        this.mSwitch.setSaveEnabled(false);
        ((MarginLayoutParams) this.mSwitch.getLayoutParams()).setMarginEnd(switchBarMarginEnd);
        setBackgroundColor(this.mBackgroundColor);
        setSwitchBarText(R.string.switch_on_text, R.string.switch_off_text);
        addOnSwitchChangeListener(new -$$Lambda$SwitchBar$xcPsCGGwUScwZOtx6bxg2zuPXc8(this));
        this.mRestrictedIcon = findViewById(R.id.restricted_icon);
        this.mRestrictedIcon.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (SwitchBar.this.mDisabledByAdmin) {
                    MetricsFeatureProvider access$300 = SwitchBar.this.mMetricsFeatureProvider;
                    Context access$100 = SwitchBar.this.mContext;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(SwitchBar.this.mMetricsTag);
                    stringBuilder.append("/switch_bar|restricted");
                    access$300.count(access$100, stringBuilder.toString(), 1);
                    RestrictedLockUtils.sendShowAdminSupportDetailsIntent(context, SwitchBar.this.mEnforcedAdmin);
                }
            }
        });
        setVisibility(8);
        this.mMetricsFeatureProvider = FeatureFactory.getFactory(context).getMetricsFeatureProvider();
    }

    public void setMetricsTag(String tag) {
        this.mMetricsTag = tag;
    }

    public void setTextViewLabelAndBackground(boolean isChecked) {
        this.mLabel = getResources().getString(isChecked ? this.mOnTextId : this.mOffTextId);
        setBackgroundColor(isChecked ? this.mBackgroundActivatedColor : this.mBackgroundColor);
        updateText();
    }

    public void setSwitchBarText(int onText, int offText) {
        this.mOnTextId = onText;
        this.mOffTextId = offText;
        setTextViewLabelAndBackground(isChecked());
    }

    public void setSummary(String summary) {
        this.mSummary = summary;
        updateText();
    }

    private void updateText() {
        if (TextUtils.isEmpty(this.mSummary)) {
            this.mTextView.setText(this.mLabel);
            return;
        }
        SpannableStringBuilder ssb = new SpannableStringBuilder(this.mLabel).append(10);
        int start = ssb.length();
        ssb.append(this.mSummary);
        ssb.setSpan(this.mSummarySpan, start, ssb.length(), 0);
        this.mTextView.setText(ssb);
    }

    public void setChecked(boolean checked) {
        setTextViewLabelAndBackground(checked);
        this.mSwitch.setChecked(checked);
    }

    public void setCheckedInternal(boolean checked) {
        setTextViewLabelAndBackground(checked);
        this.mSwitch.setCheckedInternal(checked);
    }

    public boolean isChecked() {
        return this.mSwitch.isChecked();
    }

    public void setEnabled(boolean enabled) {
        if (enabled && this.mDisabledByAdmin) {
            setDisabledByAdmin(null);
            return;
        }
        super.setEnabled(enabled);
        this.mTextView.setEnabled(enabled);
        this.mSwitch.setEnabled(enabled);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public View getDelegatingView() {
        return this.mDisabledByAdmin ? this.mRestrictedIcon : this.mSwitch;
    }

    public void setDisabledByAdmin(EnforcedAdmin admin) {
        this.mEnforcedAdmin = admin;
        if (admin != null) {
            super.setEnabled(true);
            this.mDisabledByAdmin = true;
            this.mTextView.setEnabled(false);
            this.mSwitch.setEnabled(false);
            this.mSwitch.setVisibility(8);
            this.mRestrictedIcon.setVisibility(0);
        } else {
            this.mDisabledByAdmin = false;
            this.mSwitch.setVisibility(0);
            this.mRestrictedIcon.setVisibility(8);
            setEnabled(true);
        }
        setTouchDelegate(new TouchDelegate(new Rect(0, 0, getWidth(), getHeight()), getDelegatingView()));
    }

    public final ToggleSwitch getSwitch() {
        return this.mSwitch;
    }

    public void show() {
        if (!isShowing()) {
            setVisibility(0);
            this.mSwitch.setOnCheckedChangeListener(this);
            post(new -$$Lambda$SwitchBar$H3bwEmU9c2USPE1paf4Zlyfzp3I(this));
        }
    }

    public void hide() {
        if (isShowing()) {
            setVisibility(8);
            this.mSwitch.setOnCheckedChangeListener(null);
        }
    }

    /* Access modifiers changed, original: protected */
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (w > 0 && h > 0) {
            setTouchDelegate(new TouchDelegate(new Rect(0, 0, w, h), getDelegatingView()));
        }
    }

    public boolean isShowing() {
        return getVisibility() == 0;
    }

    public void propagateChecked(boolean isChecked) {
        int count = this.mSwitchChangeListeners.size();
        for (int n = 0; n < count; n++) {
            ((OnSwitchChangeListener) this.mSwitchChangeListeners.get(n)).onSwitchChanged(this.mSwitch, isChecked);
        }
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (this.mLoggingIntialized) {
            MetricsFeatureProvider metricsFeatureProvider = this.mMetricsFeatureProvider;
            Context context = this.mContext;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(this.mMetricsTag);
            stringBuilder.append("/switch_bar|");
            stringBuilder.append(isChecked);
            metricsFeatureProvider.count(context, stringBuilder.toString(), 1);
        }
        this.mLoggingIntialized = true;
        propagateChecked(isChecked);
    }

    public void addOnSwitchChangeListener(OnSwitchChangeListener listener) {
        if (this.mSwitchChangeListeners.contains(listener)) {
            throw new IllegalStateException("Cannot add twice the same OnSwitchChangeListener");
        }
        this.mSwitchChangeListeners.add(listener);
    }

    public void removeOnSwitchChangeListener(OnSwitchChangeListener listener) {
        if (this.mSwitchChangeListeners.contains(listener)) {
            this.mSwitchChangeListeners.remove(listener);
            return;
        }
        throw new IllegalStateException("Cannot remove OnSwitchChangeListener");
    }

    public Parcelable onSaveInstanceState() {
        SavedState ss = new SavedState(super.onSaveInstanceState());
        ss.checked = this.mSwitch.isChecked();
        ss.visible = isShowing();
        return ss;
    }

    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        this.mSwitch.setCheckedInternal(ss.checked);
        setTextViewLabelAndBackground(ss.checked);
        setVisibility(ss.visible ? 0 : 8);
        this.mSwitch.setOnCheckedChangeListener(ss.visible ? this : null);
        requestLayout();
    }
}
