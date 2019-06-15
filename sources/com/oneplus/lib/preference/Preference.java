package com.oneplus.lib.preference;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.AbsSavedState;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.oneplus.commonctrl.R;
import com.oneplus.lib.preference.PreferenceManager.OnPreferenceTreeClickListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Preference implements Comparable<Preference> {
    public static final int DEFAULT_ORDER = Integer.MAX_VALUE;
    private boolean mBaseMethodCalled;
    private boolean mCanRecycleLayout;
    private Context mContext;
    private Object mDefaultValue;
    private String mDependencyKey;
    private boolean mDependencyMet;
    private List<Preference> mDependents;
    private boolean mEnabled;
    private Bundle mExtras;
    private String mFragment;
    private Drawable mIcon;
    private int mIconResId;
    private long mId;
    private View mImageFrame;
    private Intent mIntent;
    private boolean mIsAvatarIcon;
    private String mKey;
    private int mLayoutResId;
    private OnPreferenceChangeInternalListener mListener;
    private OnPreferenceChangeListener mOnChangeListener;
    private OnPreferenceClickListener mOnClickListener;
    private int mOrder;
    private boolean mParentDependencyMet;
    private boolean mPersistent;
    private PreferenceManager mPreferenceManager;
    private boolean mRequiresKey;
    private Drawable mSecondaryIcon;
    private int mSecondaryIconResId;
    private boolean mSelectable;
    private boolean mShouldDisableView;
    private CharSequence mSummary;
    private CharSequence mSummaryOff;
    private CharSequence mSummaryOn;
    private CharSequence mTitle;
    private int mTitleRes;
    private ViewGroup mWidgetFrame;
    private int mWidgetLayoutResId;

    public static class BaseSavedState extends AbsSavedState {
        public static final Creator<BaseSavedState> CREATOR = new Creator<BaseSavedState>() {
            public BaseSavedState createFromParcel(Parcel in) {
                return new BaseSavedState(in);
            }

            public BaseSavedState[] newArray(int size) {
                return new BaseSavedState[size];
            }
        };

        public BaseSavedState(Parcel source) {
            super(source);
        }

        public BaseSavedState(Parcelable superState) {
            super(superState);
        }
    }

    interface OnPreferenceChangeInternalListener {
        void onPreferenceChange(Preference preference);

        void onPreferenceHierarchyChange(Preference preference);
    }

    public interface OnPreferenceChangeListener {
        boolean onPreferenceChange(Preference preference, Object obj);
    }

    public interface OnPreferenceClickListener {
        boolean onPreferenceClick(Preference preference);
    }

    public Preference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this.mOrder = Integer.MAX_VALUE;
        this.mEnabled = true;
        this.mSelectable = true;
        this.mPersistent = true;
        this.mDependencyMet = true;
        this.mParentDependencyMet = true;
        this.mShouldDisableView = true;
        this.mLayoutResId = R.layout.preference;
        this.mCanRecycleLayout = true;
        this.mContext = context;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Preference, defStyleAttr, defStyleRes);
        this.mIsAvatarIcon = a.getBoolean(R.styleable.Preference_opUseAvatarIcon, false);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("mIsAvatarIcon = ");
        stringBuilder.append(this.mIsAvatarIcon);
        Log.i("Preference", stringBuilder.toString());
        this.mIconResId = a.getResourceId(R.styleable.Preference_android_icon, 0);
        this.mKey = a.getString(R.styleable.Preference_android_key);
        this.mTitleRes = a.getResourceId(R.styleable.Preference_android_title, 0);
        this.mTitle = a.getString(R.styleable.Preference_android_title);
        this.mSummary = a.getString(R.styleable.Preference_android_summary);
        this.mOrder = a.getInt(R.styleable.Preference_android_order, this.mOrder);
        this.mFragment = a.getString(R.styleable.Preference_android_fragment);
        this.mLayoutResId = a.getResourceId(R.styleable.Preference_android_layout, this.mLayoutResId);
        this.mWidgetLayoutResId = a.getResourceId(R.styleable.Preference_android_widgetLayout, this.mWidgetLayoutResId);
        this.mEnabled = a.getBoolean(R.styleable.Preference_android_enabled, true);
        this.mSelectable = a.getBoolean(R.styleable.Preference_android_selectable, true);
        this.mPersistent = a.getBoolean(R.styleable.Preference_android_persistent, this.mPersistent);
        this.mDependencyKey = a.getString(R.styleable.Preference_android_dependency);
        if (a.hasValue(R.styleable.Preference_android_defaultValue)) {
            this.mDefaultValue = onGetDefaultValue(a, R.styleable.Preference_android_defaultValue);
        }
        this.mShouldDisableView = a.getBoolean(R.styleable.Preference_android_shouldDisableView, this.mShouldDisableView);
        a.recycle();
    }

    public Preference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public Preference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.op_preferenceStyle);
    }

    public Preference(Context context) {
        this(context, null);
    }

    /* Access modifiers changed, original: protected */
    public Object onGetDefaultValue(TypedArray a, int index) {
        return null;
    }

    public void setIntent(Intent intent) {
        this.mIntent = intent;
    }

    public Intent getIntent() {
        return this.mIntent;
    }

    public void setFragment(String fragment) {
        this.mFragment = fragment;
    }

    public String getFragment() {
        return this.mFragment;
    }

    public Bundle getExtras() {
        if (this.mExtras == null) {
            this.mExtras = new Bundle();
        }
        return this.mExtras;
    }

    public Bundle peekExtras() {
        return this.mExtras;
    }

    public void setLayoutResource(int layoutResId) {
        if (layoutResId != this.mLayoutResId) {
            this.mCanRecycleLayout = false;
        }
        this.mLayoutResId = layoutResId;
    }

    public int getLayoutResource() {
        return this.mLayoutResId;
    }

    public void setWidgetLayoutResource(int widgetLayoutResId) {
        if (widgetLayoutResId != this.mWidgetLayoutResId) {
            this.mCanRecycleLayout = false;
        }
        this.mWidgetLayoutResId = widgetLayoutResId;
    }

    public int getWidgetLayoutResource() {
        return this.mWidgetLayoutResId;
    }

    public View getView(View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = onCreateView(parent);
        }
        onBindView(convertView);
        return convertView;
    }

    /* Access modifiers changed, original: protected */
    public View onCreateView(ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) this.mContext.getSystemService("layout_inflater");
        View layout = layoutInflater.inflate(this.mLayoutResId, parent, false);
        this.mWidgetFrame = (ViewGroup) layout.findViewById(16908312);
        if (this.mWidgetFrame != null) {
            if (this.mWidgetLayoutResId != 0) {
                layoutInflater.inflate(this.mWidgetLayoutResId, this.mWidgetFrame);
            } else {
                this.mWidgetFrame.setVisibility(8);
            }
            if (isSummaryEmpty() && this.mWidgetFrame.getVisibility() != 8) {
                LayoutParams params = (LayoutParams) this.mWidgetFrame.getLayoutParams();
                if (params != null) {
                    params.topMargin = 0;
                    params.gravity = 16;
                    this.mWidgetFrame.setLayoutParams(params);
                }
            }
        }
        return layout;
    }

    /* Access modifiers changed, original: protected */
    public void onBindView(View view) {
        TextView titleView = (TextView) view.findViewById(16908310);
        int i = 8;
        if (titleView != null) {
            CharSequence title = getTitle();
            if (TextUtils.isEmpty(title)) {
                titleView.setVisibility(8);
            } else {
                titleView.setText(title);
                titleView.setVisibility(0);
            }
        }
        TextView summaryView = (TextView) view.findViewById(16908304);
        if (summaryView != null) {
            CharSequence summary = getSummary();
            if (TextUtils.isEmpty(summary)) {
                summaryView.setVisibility(8);
            } else {
                summaryView.setText(summary);
                summaryView.setVisibility(0);
            }
        }
        ImageView imageView = (ImageView) view.findViewById(16908294);
        if (imageView != null) {
            if (!(this.mIconResId == 0 && this.mIcon == null)) {
                if (this.mIcon == null) {
                    this.mIcon = getContext().getDrawable(this.mIconResId);
                }
                if (this.mIcon != null) {
                    imageView.setImageDrawable(this.mIcon);
                }
            }
            imageView.setVisibility(this.mIcon != null ? 0 : 8);
        }
        this.mImageFrame = view.findViewById(R.id.icon_frame);
        if (this.mImageFrame != null) {
            this.mImageFrame.setVisibility(this.mIcon != null ? 0 : 8);
        }
        doUpdateImageFrameParams();
        View textLayout = view.findViewById(R.id.text_layout);
        if (textLayout != null) {
            View topSpace = view.findViewById(R.id.top_space);
            LayoutParams params = (LayoutParams) textLayout.getLayoutParams();
            if (!(textLayout == null || params == null)) {
                if (!(this.mImageFrame == null || this.mImageFrame.getVisibility() == 8)) {
                    params.setMarginStart(0);
                }
                if (isSummaryEmpty()) {
                    params.gravity = 16;
                    params.height = -2;
                    if (topSpace != null) {
                        topSpace.setVisibility(8);
                    }
                    textLayout.setLayoutParams(params);
                } else if (topSpace != null) {
                    topSpace.setVisibility(0);
                }
            }
        }
        ImageView secondaryImageView = (ImageView) view.findViewById(R.id.secondary_icon);
        if (secondaryImageView != null) {
            if (!(this.mSecondaryIconResId == 0 && this.mSecondaryIcon == null)) {
                if (this.mSecondaryIcon == null) {
                    this.mSecondaryIcon = getContext().getDrawable(this.mSecondaryIconResId);
                }
                if (this.mSecondaryIcon != null) {
                    secondaryImageView.setImageDrawable(this.mSecondaryIcon);
                }
            }
            if (this.mSecondaryIcon != null) {
                i = 0;
            }
            secondaryImageView.setVisibility(i);
        }
        if (this.mShouldDisableView) {
            setEnabledStateOnViews(view, isEnabled());
        }
    }

    private void setEnabledStateOnViews(View v, boolean enabled) {
        v.setEnabled(enabled);
        if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            for (int i = vg.getChildCount() - 1; i >= 0; i--) {
                setEnabledStateOnViews(vg.getChildAt(i), enabled);
            }
        }
    }

    public void setOrder(int order) {
        if (order != this.mOrder) {
            this.mOrder = order;
            notifyHierarchyChanged();
        }
    }

    public int getOrder() {
        return this.mOrder;
    }

    public void setTitle(CharSequence title) {
        if ((title == null && this.mTitle != null) || (title != null && !title.equals(this.mTitle))) {
            this.mTitleRes = 0;
            this.mTitle = title;
            notifyChanged();
        }
    }

    public void setTitle(int titleResId) {
        setTitle(this.mContext.getString(titleResId));
        this.mTitleRes = titleResId;
    }

    public int getTitleRes() {
        return this.mTitleRes;
    }

    public CharSequence getTitle() {
        return this.mTitle;
    }

    public void setIcon(Drawable icon) {
        if ((icon == null && this.mIcon != null) || (icon != null && this.mIcon != icon)) {
            this.mIcon = icon;
            notifyChanged();
        }
    }

    public void setIcon(int iconResId) {
        if (this.mIconResId != iconResId) {
            this.mIconResId = iconResId;
            setIcon(this.mContext.getDrawable(iconResId));
        }
    }

    public Drawable getIcon() {
        return this.mIcon;
    }

    public void setUseAvatarIcon(boolean isAvatarIcon) {
        this.mIsAvatarIcon = isAvatarIcon;
        doUpdateImageFrameParams();
    }

    private void doUpdateImageFrameParams() {
        if (this.mImageFrame != null && this.mImageFrame.getVisibility() != 8) {
            LayoutParams params = (LayoutParams) this.mImageFrame.getLayoutParams();
            if (params != null) {
                if (this.mIsAvatarIcon) {
                    params.width = this.mContext.getResources().getDimensionPixelSize(R.dimen.oneplus_contorl_avatar_mini);
                    params.height = params.width;
                    params.setMarginStart(this.mContext.getResources().getDimensionPixelSize(R.dimen.oneplus_contorl_margin_avatar_left2));
                    params.setMarginEnd(this.mContext.getResources().getDimensionPixelSize(R.dimen.oneplus_contorl_margin_avatar_right3));
                    if (isSummaryEmpty()) {
                        params.topMargin = 0;
                        params.gravity = 16;
                    } else {
                        params.topMargin = this.mContext.getResources().getDimensionPixelSize(R.dimen.oneplus_contorl_margin_avatar_top3);
                    }
                } else if (isSummaryEmpty()) {
                    params.topMargin = 0;
                    params.gravity = 16;
                } else {
                    params.topMargin = this.mContext.getResources().getDimensionPixelSize(R.dimen.oneplus_contorl_margin_top4);
                }
                this.mImageFrame.setLayoutParams(params);
            }
        }
    }

    public void setSecondaryIcon(Drawable icon) {
        if ((icon == null && this.mSecondaryIcon != null) || (icon != null && this.mSecondaryIcon != icon)) {
            this.mSecondaryIcon = icon;
            notifyChanged();
        }
    }

    public void setSecondaryIcon(int iconResId) {
        this.mSecondaryIconResId = iconResId;
        setSecondaryIcon(this.mContext.getDrawable(iconResId));
    }

    public Drawable getSecondaryIcon() {
        return this.mSecondaryIcon;
    }

    public CharSequence getSummary() {
        return this.mSummary;
    }

    public void setSummary(CharSequence summary) {
        if ((summary == null && this.mSummary != null) || (summary != null && !summary.equals(this.mSummary))) {
            this.mSummary = summary;
            notifyChanged();
        }
    }

    /* Access modifiers changed, original: protected */
    public void setSummaryOnFromTwoState(CharSequence summary) {
        this.mSummaryOn = summary;
    }

    /* Access modifiers changed, original: protected */
    public void setSummaryOffFromTwoState(CharSequence summary) {
        this.mSummaryOff = summary;
    }

    private boolean isSummaryEmpty() {
        return TextUtils.isEmpty(this.mSummary) && TextUtils.isEmpty(this.mSummaryOn) && TextUtils.isEmpty(this.mSummaryOff);
    }

    public void setSummary(int summaryResId) {
        setSummary(this.mContext.getString(summaryResId));
    }

    public void setEnabled(boolean enabled) {
        if (this.mEnabled != enabled) {
            this.mEnabled = enabled;
            notifyDependencyChange(shouldDisableDependents());
            notifyChanged();
        }
    }

    public boolean isEnabled() {
        return this.mEnabled && this.mDependencyMet && this.mParentDependencyMet;
    }

    public void setSelectable(boolean selectable) {
        if (this.mSelectable != selectable) {
            this.mSelectable = selectable;
            notifyChanged();
        }
    }

    public boolean isSelectable() {
        return this.mSelectable;
    }

    public void setShouldDisableView(boolean shouldDisableView) {
        this.mShouldDisableView = shouldDisableView;
        notifyChanged();
    }

    public boolean getShouldDisableView() {
        return this.mShouldDisableView;
    }

    /* Access modifiers changed, original: 0000 */
    public long getId() {
        return this.mId;
    }

    /* Access modifiers changed, original: protected */
    public void onClick() {
    }

    public void setKey(String key) {
        this.mKey = key;
        if (this.mRequiresKey && !hasKey()) {
            requireKey();
        }
    }

    public String getKey() {
        return this.mKey;
    }

    /* Access modifiers changed, original: 0000 */
    public void requireKey() {
        if (this.mKey != null) {
            this.mRequiresKey = true;
            return;
        }
        throw new IllegalStateException("Preference does not have a key assigned.");
    }

    public boolean hasKey() {
        return TextUtils.isEmpty(this.mKey) ^ 1;
    }

    public boolean isPersistent() {
        return this.mPersistent;
    }

    /* Access modifiers changed, original: protected */
    public boolean shouldPersist() {
        return this.mPreferenceManager != null && isPersistent() && hasKey();
    }

    public void setPersistent(boolean persistent) {
        this.mPersistent = persistent;
    }

    /* Access modifiers changed, original: protected */
    public boolean callChangeListener(Object newValue) {
        return this.mOnChangeListener == null ? true : this.mOnChangeListener.onPreferenceChange(this, newValue);
    }

    public void setOnPreferenceChangeListener(OnPreferenceChangeListener onPreferenceChangeListener) {
        this.mOnChangeListener = onPreferenceChangeListener;
    }

    public OnPreferenceChangeListener getOnPreferenceChangeListener() {
        return this.mOnChangeListener;
    }

    public void setOnPreferenceClickListener(OnPreferenceClickListener onPreferenceClickListener) {
        this.mOnClickListener = onPreferenceClickListener;
    }

    public OnPreferenceClickListener getOnPreferenceClickListener() {
        return this.mOnClickListener;
    }

    public void performClick(PreferenceScreen preferenceScreen) {
        if (isEnabled()) {
            onClick();
            if (this.mOnClickListener == null || !this.mOnClickListener.onPreferenceClick(this)) {
                PreferenceManager preferenceManager = getPreferenceManager();
                if (preferenceManager != null) {
                    OnPreferenceTreeClickListener listener = preferenceManager.getOnPreferenceTreeClickListener();
                    if (!(preferenceScreen == null || listener == null || !listener.onPreferenceTreeClick(preferenceScreen, this))) {
                        return;
                    }
                }
                if (this.mIntent != null) {
                    getContext().startActivity(this.mIntent);
                }
            }
        }
    }

    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    public Context getContext() {
        return this.mContext;
    }

    public SharedPreferences getSharedPreferences() {
        if (this.mPreferenceManager == null) {
            return null;
        }
        return this.mPreferenceManager.getSharedPreferences();
    }

    public Editor getEditor() {
        if (this.mPreferenceManager == null) {
            return null;
        }
        return this.mPreferenceManager.getEditor();
    }

    public boolean shouldCommit() {
        if (this.mPreferenceManager == null) {
            return false;
        }
        return this.mPreferenceManager.shouldCommit();
    }

    public int compareTo(Preference another) {
        if (this.mOrder != another.mOrder) {
            return this.mOrder - another.mOrder;
        }
        if (this.mTitle == another.mTitle) {
            return 0;
        }
        if (this.mTitle == null) {
            return 1;
        }
        if (another.mTitle == null) {
            return -1;
        }
        return CharSequences.compareToIgnoreCase(this.mTitle, another.mTitle);
    }

    /* Access modifiers changed, original: final */
    public final void setOnPreferenceChangeInternalListener(OnPreferenceChangeInternalListener listener) {
        this.mListener = listener;
    }

    /* Access modifiers changed, original: protected */
    public void notifyChanged() {
        if (this.mListener != null) {
            this.mListener.onPreferenceChange(this);
        }
    }

    /* Access modifiers changed, original: protected */
    public void notifyHierarchyChanged() {
        if (this.mListener != null) {
            this.mListener.onPreferenceHierarchyChange(this);
        }
    }

    public PreferenceManager getPreferenceManager() {
        return this.mPreferenceManager;
    }

    /* Access modifiers changed, original: protected */
    public void onAttachedToHierarchy(PreferenceManager preferenceManager) {
        this.mPreferenceManager = preferenceManager;
        this.mId = preferenceManager.getNextId();
        dispatchSetInitialValue();
    }

    /* Access modifiers changed, original: protected */
    public void onAttachedToActivity() {
        registerDependency();
    }

    private void registerDependency() {
        if (!TextUtils.isEmpty(this.mDependencyKey)) {
            Preference preference = findPreferenceInHierarchy(this.mDependencyKey);
            if (preference != null) {
                preference.registerDependent(this);
                return;
            }
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Dependency \"");
            stringBuilder.append(this.mDependencyKey);
            stringBuilder.append("\" not found for preference \"");
            stringBuilder.append(this.mKey);
            stringBuilder.append("\" (title: \"");
            stringBuilder.append(this.mTitle);
            stringBuilder.append("\"");
            throw new IllegalStateException(stringBuilder.toString());
        }
    }

    private void unregisterDependency() {
        if (this.mDependencyKey != null) {
            Preference oldDependency = findPreferenceInHierarchy(this.mDependencyKey);
            if (oldDependency != null) {
                oldDependency.unregisterDependent(this);
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public Preference findPreferenceInHierarchy(String key) {
        if (TextUtils.isEmpty(key) || this.mPreferenceManager == null) {
            return null;
        }
        return this.mPreferenceManager.findPreference(key);
    }

    private void registerDependent(Preference dependent) {
        if (this.mDependents == null) {
            this.mDependents = new ArrayList();
        }
        this.mDependents.add(dependent);
        dependent.onDependencyChanged(this, shouldDisableDependents());
    }

    private void unregisterDependent(Preference dependent) {
        if (this.mDependents != null) {
            this.mDependents.remove(dependent);
        }
    }

    public void notifyDependencyChange(boolean disableDependents) {
        List<Preference> dependents = this.mDependents;
        if (dependents != null) {
            int dependentsCount = dependents.size();
            for (int i = 0; i < dependentsCount; i++) {
                ((Preference) dependents.get(i)).onDependencyChanged(this, disableDependents);
            }
        }
    }

    public void onDependencyChanged(Preference dependency, boolean disableDependent) {
        if (this.mDependencyMet == disableDependent) {
            this.mDependencyMet = disableDependent ^ 1;
            notifyDependencyChange(shouldDisableDependents());
            notifyChanged();
        }
    }

    public void onParentChanged(Preference parent, boolean disableChild) {
        if (this.mParentDependencyMet == disableChild) {
            this.mParentDependencyMet = disableChild ^ 1;
            notifyDependencyChange(shouldDisableDependents());
            notifyChanged();
        }
    }

    public boolean shouldDisableDependents() {
        return isEnabled() ^ 1;
    }

    public void setDependency(String dependencyKey) {
        unregisterDependency();
        this.mDependencyKey = dependencyKey;
        registerDependency();
    }

    public String getDependency() {
        return this.mDependencyKey;
    }

    /* Access modifiers changed, original: protected */
    public void onPrepareForRemoval() {
        unregisterDependency();
    }

    public void setDefaultValue(Object defaultValue) {
        this.mDefaultValue = defaultValue;
    }

    private void dispatchSetInitialValue() {
        if (shouldPersist() && getSharedPreferences().contains(this.mKey)) {
            onSetInitialValue(true, null);
        } else if (this.mDefaultValue != null) {
            onSetInitialValue(false, this.mDefaultValue);
        }
    }

    /* Access modifiers changed, original: protected */
    public void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
    }

    private void tryCommit(Editor editor) {
        if (this.mPreferenceManager.shouldCommit()) {
            try {
                editor.apply();
            } catch (AbstractMethodError e) {
                editor.commit();
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public boolean persistString(String value) {
        if (!shouldPersist()) {
            return false;
        }
        if (TextUtils.equals(value, getPersistedString(null))) {
            return true;
        }
        Editor editor = this.mPreferenceManager.getEditor();
        editor.putString(this.mKey, value);
        tryCommit(editor);
        return true;
    }

    /* Access modifiers changed, original: protected */
    public String getPersistedString(String defaultReturnValue) {
        if (shouldPersist()) {
            return this.mPreferenceManager.getSharedPreferences().getString(this.mKey, defaultReturnValue);
        }
        return defaultReturnValue;
    }

    /* Access modifiers changed, original: protected */
    public boolean persistStringSet(Set<String> values) {
        if (!shouldPersist()) {
            return false;
        }
        if (values.equals(getPersistedStringSet(null))) {
            return true;
        }
        Editor editor = this.mPreferenceManager.getEditor();
        editor.putStringSet(this.mKey, values);
        tryCommit(editor);
        return true;
    }

    /* Access modifiers changed, original: protected */
    public Set<String> getPersistedStringSet(Set<String> defaultReturnValue) {
        if (shouldPersist()) {
            return this.mPreferenceManager.getSharedPreferences().getStringSet(this.mKey, defaultReturnValue);
        }
        return defaultReturnValue;
    }

    /* Access modifiers changed, original: protected */
    public boolean persistInt(int value) {
        if (!shouldPersist()) {
            return false;
        }
        if (value == getPersistedInt(~value)) {
            return true;
        }
        Editor editor = this.mPreferenceManager.getEditor();
        editor.putInt(this.mKey, value);
        tryCommit(editor);
        return true;
    }

    /* Access modifiers changed, original: protected */
    public int getPersistedInt(int defaultReturnValue) {
        if (shouldPersist()) {
            return this.mPreferenceManager.getSharedPreferences().getInt(this.mKey, defaultReturnValue);
        }
        return defaultReturnValue;
    }

    /* Access modifiers changed, original: protected */
    public boolean persistFloat(float value) {
        if (!shouldPersist()) {
            return false;
        }
        if (value == getPersistedFloat(Float.NaN)) {
            return true;
        }
        Editor editor = this.mPreferenceManager.getEditor();
        editor.putFloat(this.mKey, value);
        tryCommit(editor);
        return true;
    }

    /* Access modifiers changed, original: protected */
    public float getPersistedFloat(float defaultReturnValue) {
        if (shouldPersist()) {
            return this.mPreferenceManager.getSharedPreferences().getFloat(this.mKey, defaultReturnValue);
        }
        return defaultReturnValue;
    }

    /* Access modifiers changed, original: protected */
    public boolean persistLong(long value) {
        if (!shouldPersist()) {
            return false;
        }
        if (value == getPersistedLong(~value)) {
            return true;
        }
        Editor editor = this.mPreferenceManager.getEditor();
        editor.putLong(this.mKey, value);
        tryCommit(editor);
        return true;
    }

    /* Access modifiers changed, original: protected */
    public long getPersistedLong(long defaultReturnValue) {
        if (shouldPersist()) {
            return this.mPreferenceManager.getSharedPreferences().getLong(this.mKey, defaultReturnValue);
        }
        return defaultReturnValue;
    }

    /* Access modifiers changed, original: protected */
    public boolean persistBoolean(boolean value) {
        if (!shouldPersist()) {
            return false;
        }
        if (value == getPersistedBoolean(value ^ 1)) {
            return true;
        }
        Editor editor = this.mPreferenceManager.getEditor();
        editor.putBoolean(this.mKey, value);
        tryCommit(editor);
        return true;
    }

    /* Access modifiers changed, original: protected */
    public boolean getPersistedBoolean(boolean defaultReturnValue) {
        if (shouldPersist()) {
            return this.mPreferenceManager.getSharedPreferences().getBoolean(this.mKey, defaultReturnValue);
        }
        return defaultReturnValue;
    }

    /* Access modifiers changed, original: 0000 */
    public boolean canRecycleLayout() {
        return this.mCanRecycleLayout;
    }

    public String toString() {
        return getFilterableStringBuilder().toString();
    }

    /* Access modifiers changed, original: 0000 */
    public StringBuilder getFilterableStringBuilder() {
        StringBuilder sb = new StringBuilder();
        CharSequence title = getTitle();
        if (!TextUtils.isEmpty(title)) {
            sb.append(title);
            sb.append(' ');
        }
        CharSequence summary = getSummary();
        if (!TextUtils.isEmpty(summary)) {
            sb.append(summary);
            sb.append(' ');
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb;
    }

    public void saveHierarchyState(Bundle container) {
        dispatchSaveInstanceState(container);
    }

    /* Access modifiers changed, original: 0000 */
    public void dispatchSaveInstanceState(Bundle container) {
        if (hasKey()) {
            this.mBaseMethodCalled = false;
            Parcelable state = onSaveInstanceState();
            if (!this.mBaseMethodCalled) {
                throw new IllegalStateException("Derived class did not call super.onSaveInstanceState()");
            } else if (state != null) {
                container.putParcelable(this.mKey, state);
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public Parcelable onSaveInstanceState() {
        this.mBaseMethodCalled = true;
        return BaseSavedState.EMPTY_STATE;
    }

    public void restoreHierarchyState(Bundle container) {
        dispatchRestoreInstanceState(container);
    }

    /* Access modifiers changed, original: 0000 */
    public void dispatchRestoreInstanceState(Bundle container) {
        if (hasKey()) {
            Parcelable state = container.getParcelable(this.mKey);
            if (state != null) {
                this.mBaseMethodCalled = false;
                onRestoreInstanceState(state);
                if (!this.mBaseMethodCalled) {
                    throw new IllegalStateException("Derived class did not call super.onRestoreInstanceState()");
                }
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public void onRestoreInstanceState(Parcelable state) {
        this.mBaseMethodCalled = true;
        if (state != BaseSavedState.EMPTY_STATE && state != null) {
            throw new IllegalArgumentException("Wrong state class -- expecting Preference State");
        }
    }
}
