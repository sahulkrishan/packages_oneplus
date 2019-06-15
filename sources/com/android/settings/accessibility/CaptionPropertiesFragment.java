package com.android.settings.accessibility;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceFrameLayout;
import android.preference.PreferenceFrameLayout.LayoutParams;
import android.provider.Settings.Secure;
import android.support.v4.view.ViewCompat;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceCategory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.accessibility.CaptioningManager;
import android.view.accessibility.CaptioningManager.CaptionStyle;
import com.android.internal.widget.SubtitleView;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.accessibility.ListDialogPreference.OnValueChangedListener;
import com.android.settings.widget.SwitchBar;
import com.android.settings.widget.ToggleSwitch;
import com.android.settings.widget.ToggleSwitch.OnBeforeCheckedChangeListener;
import com.android.settingslib.accessibility.AccessibilityUtils;
import java.util.Locale;

public class CaptionPropertiesFragment extends SettingsPreferenceFragment implements OnPreferenceChangeListener, OnValueChangedListener {
    private static final float LINE_HEIGHT_RATIO = 0.0533f;
    private static final String PREF_BACKGROUND_COLOR = "captioning_background_color";
    private static final String PREF_BACKGROUND_OPACITY = "captioning_background_opacity";
    private static final String PREF_CUSTOM = "custom";
    private static final String PREF_EDGE_COLOR = "captioning_edge_color";
    private static final String PREF_EDGE_TYPE = "captioning_edge_type";
    private static final String PREF_FONT_SIZE = "captioning_font_size";
    private static final String PREF_FOREGROUND_COLOR = "captioning_foreground_color";
    private static final String PREF_FOREGROUND_OPACITY = "captioning_foreground_opacity";
    private static final String PREF_LOCALE = "captioning_locale";
    private static final String PREF_PRESET = "captioning_preset";
    private static final String PREF_TYPEFACE = "captioning_typeface";
    private static final String PREF_WINDOW_COLOR = "captioning_window_color";
    private static final String PREF_WINDOW_OPACITY = "captioning_window_opacity";
    private ColorPreference mBackgroundColor;
    private ColorPreference mBackgroundOpacity;
    private CaptioningManager mCaptioningManager;
    private PreferenceCategory mCustom;
    private ColorPreference mEdgeColor;
    private EdgeTypePreference mEdgeType;
    private ListPreference mFontSize;
    private ColorPreference mForegroundColor;
    private ColorPreference mForegroundOpacity;
    private LocalePreference mLocale;
    private PresetPreference mPreset;
    private SubtitleView mPreviewText;
    private View mPreviewViewport;
    private View mPreviewWindow;
    private boolean mShowingCustom;
    private SwitchBar mSwitchBar;
    private ToggleSwitch mToggleSwitch;
    private ListPreference mTypeface;
    private ColorPreference mWindowColor;
    private ColorPreference mWindowOpacity;

    public int getMetricsCategory() {
        return 3;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mCaptioningManager = (CaptioningManager) getSystemService("captioning");
        addPreferencesFromResource(R.xml.captioning_settings);
        initializeAllPreferences();
        updateAllPreferences();
        refreshShowingCustom();
        installUpdateListeners();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView;
        if (((WindowManager) getSystemService("window")).getDefaultDisplay().getWidth() == 1440) {
            rootView = inflater.inflate(R.layout.captioning_preview_2k, container, false);
        } else {
            rootView = inflater.inflate(R.layout.captioning_preview, container, false);
        }
        if (container instanceof PreferenceFrameLayout) {
            ((LayoutParams) rootView.getLayoutParams()).removeBorders = true;
        }
        ((ViewGroup) rootView.findViewById(R.id.properties_fragment)).addView(super.onCreateView(inflater, container, savedInstanceState), -1, -1);
        return rootView;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        boolean enabled = this.mCaptioningManager.isEnabled();
        this.mPreviewText = (SubtitleView) view.findViewById(R.id.preview_text);
        this.mPreviewText.setVisibility(enabled ? 0 : 4);
        this.mPreviewWindow = view.findViewById(R.id.preview_window);
        this.mPreviewViewport = view.findViewById(R.id.preview_viewport);
        this.mPreviewViewport.addOnLayoutChangeListener(new OnLayoutChangeListener() {
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                CaptionPropertiesFragment.this.refreshPreviewText();
            }
        });
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        boolean enabled = this.mCaptioningManager.isEnabled();
        this.mSwitchBar = ((SettingsActivity) getActivity()).getSwitchBar();
        this.mSwitchBar.setSwitchBarText(R.string.accessibility_caption_master_switch_title, R.string.accessibility_caption_master_switch_title);
        this.mSwitchBar.setCheckedInternal(enabled);
        this.mToggleSwitch = this.mSwitchBar.getSwitch();
        getPreferenceScreen().setEnabled(enabled);
        refreshPreviewText();
        installSwitchBarToggleSwitch();
    }

    public void onDestroyView() {
        super.onDestroyView();
        removeSwitchBarToggleSwitch();
    }

    private void refreshPreviewText() {
        Context context = getActivity();
        if (context != null) {
            SubtitleView preview = this.mPreviewText;
            if (preview != null) {
                applyCaptionProperties(this.mCaptioningManager, preview, this.mPreviewViewport, this.mCaptioningManager.getRawUserStyle());
                Locale locale = this.mCaptioningManager.getLocale();
                if (locale != null) {
                    preview.setText(AccessibilityUtils.getTextForLocale(context, locale, R.string.captioning_preview_text));
                } else {
                    preview.setText(R.string.captioning_preview_text);
                }
                CaptionStyle style = this.mCaptioningManager.getUserStyle();
                if (style.hasWindowColor()) {
                    this.mPreviewWindow.setBackgroundColor(style.windowColor);
                } else {
                    this.mPreviewWindow.setBackgroundColor(CaptionStyle.DEFAULT.windowColor);
                }
            }
        }
    }

    public static void applyCaptionProperties(CaptioningManager manager, SubtitleView previewText, View previewWindow, int styleId) {
        previewText.setStyle(styleId);
        Context context = previewText.getContext();
        ContentResolver cr = context.getContentResolver();
        float fontScale = manager.getFontScale();
        if (previewWindow != null) {
            previewText.setTextSize((LINE_HEIGHT_RATIO * (((float) Math.max(9 * previewWindow.getWidth(), 16 * previewWindow.getHeight())) / 16.0f)) * fontScale);
        } else {
            previewText.setTextSize(context.getResources().getDimension(R.dimen.caption_preview_text_size) * fontScale);
        }
        Locale locale = manager.getLocale();
        if (locale != null) {
            previewText.setText(AccessibilityUtils.getTextForLocale(context, locale, R.string.captioning_preview_characters));
        } else {
            previewText.setText(R.string.captioning_preview_characters);
        }
    }

    /* Access modifiers changed, original: protected */
    public void onInstallSwitchBarToggleSwitch() {
        this.mToggleSwitch.setOnBeforeCheckedChangeListener(new OnBeforeCheckedChangeListener() {
            public boolean onBeforeCheckedChanged(ToggleSwitch toggleSwitch, boolean checked) {
                CaptionPropertiesFragment.this.mSwitchBar.setCheckedInternal(checked);
                Secure.putInt(CaptionPropertiesFragment.this.getActivity().getContentResolver(), "accessibility_captioning_enabled", checked);
                CaptionPropertiesFragment.this.getPreferenceScreen().setEnabled(checked);
                if (CaptionPropertiesFragment.this.mPreviewText != null) {
                    CaptionPropertiesFragment.this.mPreviewText.setVisibility(checked ? 0 : 4);
                }
                return false;
            }
        });
    }

    private void installSwitchBarToggleSwitch() {
        onInstallSwitchBarToggleSwitch();
        this.mSwitchBar.show();
    }

    private void removeSwitchBarToggleSwitch() {
        this.mSwitchBar.hide();
        this.mToggleSwitch.setOnBeforeCheckedChangeListener(null);
    }

    private void initializeAllPreferences() {
        this.mLocale = (LocalePreference) findPreference(PREF_LOCALE);
        this.mFontSize = (ListPreference) findPreference(PREF_FONT_SIZE);
        Resources res = getResources();
        int[] presetValues = res.getIntArray(2130903097);
        String[] presetTitles = res.getStringArray(2130903096);
        this.mPreset = (PresetPreference) findPreference(PREF_PRESET);
        this.mPreset.setValues(presetValues);
        this.mPreset.setTitles(presetTitles);
        this.mCustom = (PreferenceCategory) findPreference(PREF_CUSTOM);
        this.mShowingCustom = true;
        int[] colorValues = res.getIntArray(2130903089);
        String[] colorTitles = res.getStringArray(2130903088);
        this.mForegroundColor = (ColorPreference) this.mCustom.findPreference(PREF_FOREGROUND_COLOR);
        this.mForegroundColor.setTitles(colorTitles);
        this.mForegroundColor.setValues(colorValues);
        int[] opacityValues = res.getIntArray(2130903095);
        String[] opacityTitles = res.getStringArray(2130903094);
        this.mForegroundOpacity = (ColorPreference) this.mCustom.findPreference(PREF_FOREGROUND_OPACITY);
        this.mForegroundOpacity.setTitles(opacityTitles);
        this.mForegroundOpacity.setValues(opacityValues);
        this.mEdgeColor = (ColorPreference) this.mCustom.findPreference(PREF_EDGE_COLOR);
        this.mEdgeColor.setTitles(colorTitles);
        this.mEdgeColor.setValues(colorValues);
        int[] bgColorValues = new int[(colorValues.length + 1)];
        String[] bgColorTitles = new String[(colorTitles.length + 1)];
        System.arraycopy(colorValues, 0, bgColorValues, 1, colorValues.length);
        System.arraycopy(colorTitles, 0, bgColorTitles, 1, colorTitles.length);
        bgColorValues[0] = 0;
        bgColorTitles[0] = getString(R.string.color_none);
        this.mBackgroundColor = (ColorPreference) this.mCustom.findPreference(PREF_BACKGROUND_COLOR);
        this.mBackgroundColor.setTitles(bgColorTitles);
        this.mBackgroundColor.setValues(bgColorValues);
        this.mBackgroundOpacity = (ColorPreference) this.mCustom.findPreference(PREF_BACKGROUND_OPACITY);
        this.mBackgroundOpacity.setTitles(opacityTitles);
        this.mBackgroundOpacity.setValues(opacityValues);
        this.mWindowColor = (ColorPreference) this.mCustom.findPreference(PREF_WINDOW_COLOR);
        this.mWindowColor.setTitles(bgColorTitles);
        this.mWindowColor.setValues(bgColorValues);
        this.mWindowOpacity = (ColorPreference) this.mCustom.findPreference(PREF_WINDOW_OPACITY);
        this.mWindowOpacity.setTitles(opacityTitles);
        this.mWindowOpacity.setValues(opacityValues);
        this.mEdgeType = (EdgeTypePreference) this.mCustom.findPreference(PREF_EDGE_TYPE);
        this.mTypeface = (ListPreference) this.mCustom.findPreference(PREF_TYPEFACE);
    }

    private void installUpdateListeners() {
        this.mPreset.setOnValueChangedListener(this);
        this.mForegroundColor.setOnValueChangedListener(this);
        this.mForegroundOpacity.setOnValueChangedListener(this);
        this.mEdgeColor.setOnValueChangedListener(this);
        this.mBackgroundColor.setOnValueChangedListener(this);
        this.mBackgroundOpacity.setOnValueChangedListener(this);
        this.mWindowColor.setOnValueChangedListener(this);
        this.mWindowOpacity.setOnValueChangedListener(this);
        this.mEdgeType.setOnValueChangedListener(this);
        this.mTypeface.setOnPreferenceChangeListener(this);
        this.mFontSize.setOnPreferenceChangeListener(this);
        this.mLocale.setOnPreferenceChangeListener(this);
    }

    private void updateAllPreferences() {
        this.mPreset.setValue(this.mCaptioningManager.getRawUserStyle());
        this.mFontSize.setValue(Float.toString(this.mCaptioningManager.getFontScale()));
        CaptionStyle attrs = CaptionStyle.getCustomStyle(getContentResolver());
        this.mEdgeType.setValue(attrs.edgeType);
        this.mEdgeColor.setValue(attrs.edgeColor);
        boolean hasForegroundColor = attrs.hasForegroundColor();
        int windowColor = ViewCompat.MEASURED_SIZE_MASK;
        parseColorOpacity(this.mForegroundColor, this.mForegroundOpacity, hasForegroundColor ? attrs.foregroundColor : ViewCompat.MEASURED_SIZE_MASK);
        parseColorOpacity(this.mBackgroundColor, this.mBackgroundOpacity, attrs.hasBackgroundColor() ? attrs.backgroundColor : ViewCompat.MEASURED_SIZE_MASK);
        if (attrs.hasWindowColor()) {
            windowColor = attrs.windowColor;
        }
        parseColorOpacity(this.mWindowColor, this.mWindowOpacity, windowColor);
        String rawTypeface = attrs.mRawTypeface;
        this.mTypeface.setValue(rawTypeface == null ? "" : rawTypeface);
        String rawLocale = this.mCaptioningManager.getRawLocale();
        this.mLocale.setValue(rawLocale == null ? "" : rawLocale);
    }

    private void parseColorOpacity(ColorPreference color, ColorPreference opacity, int value) {
        int colorValue;
        int opacityValue;
        if (!CaptionStyle.hasColor(value)) {
            colorValue = ViewCompat.MEASURED_SIZE_MASK;
            opacityValue = (value & 255) << 24;
        } else if ((value >>> 24) == 0) {
            colorValue = 0;
            opacityValue = (value & 255) << 24;
        } else {
            opacityValue = ViewCompat.MEASURED_STATE_MASK & value;
            colorValue = value | ViewCompat.MEASURED_STATE_MASK;
        }
        opacity.setValue(ViewCompat.MEASURED_SIZE_MASK | opacityValue);
        color.setValue(colorValue);
    }

    private int mergeColorOpacity(ColorPreference color, ColorPreference opacity) {
        int colorValue = color.getValue();
        int opacityValue = opacity.getValue();
        if (!CaptionStyle.hasColor(colorValue)) {
            return 16776960 | Color.alpha(opacityValue);
        }
        if (colorValue == 0) {
            return Color.alpha(opacityValue);
        }
        return (ViewCompat.MEASURED_SIZE_MASK & colorValue) | (ViewCompat.MEASURED_STATE_MASK & opacityValue);
    }

    private void refreshShowingCustom() {
        boolean customPreset = this.mPreset.getValue() == -1;
        if (!customPreset && this.mShowingCustom) {
            getPreferenceScreen().removePreference(this.mCustom);
            this.mShowingCustom = false;
        } else if (customPreset && !this.mShowingCustom) {
            getPreferenceScreen().addPreference(this.mCustom);
            this.mShowingCustom = true;
        }
    }

    public void onValueChanged(ListDialogPreference preference, int value) {
        ContentResolver cr = getActivity().getContentResolver();
        if (this.mForegroundColor == preference || this.mForegroundOpacity == preference) {
            Secure.putInt(cr, "accessibility_captioning_foreground_color", mergeColorOpacity(this.mForegroundColor, this.mForegroundOpacity));
        } else if (this.mBackgroundColor == preference || this.mBackgroundOpacity == preference) {
            Secure.putInt(cr, "accessibility_captioning_background_color", mergeColorOpacity(this.mBackgroundColor, this.mBackgroundOpacity));
        } else if (this.mWindowColor == preference || this.mWindowOpacity == preference) {
            Secure.putInt(cr, "accessibility_captioning_window_color", mergeColorOpacity(this.mWindowColor, this.mWindowOpacity));
        } else if (this.mEdgeColor == preference) {
            Secure.putInt(cr, "accessibility_captioning_edge_color", value);
        } else if (this.mPreset == preference) {
            Secure.putInt(cr, "accessibility_captioning_preset", value);
            refreshShowingCustom();
        } else if (this.mEdgeType == preference) {
            Secure.putInt(cr, "accessibility_captioning_edge_type", value);
        }
        refreshPreviewText();
    }

    public boolean onPreferenceChange(Preference preference, Object value) {
        ContentResolver cr = getActivity().getContentResolver();
        if (this.mTypeface == preference) {
            Secure.putString(cr, "accessibility_captioning_typeface", (String) value);
        } else if (this.mFontSize == preference) {
            Secure.putFloat(cr, "accessibility_captioning_font_scale", Float.parseFloat((String) value));
        } else if (this.mLocale == preference) {
            Secure.putString(cr, "accessibility_captioning_locale", (String) value);
        }
        refreshPreviewText();
        return true;
    }
}
