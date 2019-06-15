package com.oneplus.settings;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.Settings.System;
import android.support.v7.preference.AndroidResources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RadioButton;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.search.SearchIndexableRaw;
import com.oneplus.settings.utils.OPUtils;
import java.util.ArrayList;
import java.util.List;

public class OPFontStyleSettings extends SettingsPreferenceFragment implements OnClickListener, Indexable {
    private static final String OP_THEME_PACKAGE = "com.oneplus.skin";
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
            Resources res = context.getResources();
            SearchIndexableRaw data = new SearchIndexableRaw(context);
            data.title = res.getString(R.string.oneplus_font_style);
            data.screenTitle = res.getString(R.string.oneplus_font_style);
            data.keywords = res.getString(R.string.oneplus_font_switch);
            List<SearchIndexableRaw> result = new ArrayList(1);
            result.add(data);
            return result;
        }
    };
    private View mSlateFont;
    private RadioButton mSlateFontButton;
    private View mSystemFont;
    private RadioButton mSystemFontButton;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        if (getActivity() != null) {
            getActivity().setTitle(R.string.oneplus_font_style);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        ViewGroup listContainer = (ViewGroup) root.findViewById(AndroidResources.ANDROID_R_LIST_CONTAINER);
        listContainer.removeAllViews();
        View content = inflater.inflate(R.layout.op_font_style, listContainer, false);
        listContainer.addView(content);
        this.mSystemFontButton = (RadioButton) content.findViewById(R.id.system_font_button);
        this.mSlateFontButton = (RadioButton) content.findViewById(R.id.slate_font_button);
        this.mSystemFont = content.findViewById(R.id.system_font);
        this.mSlateFont = content.findViewById(R.id.slate_font);
        this.mSystemFont.setOnClickListener(this);
        this.mSlateFont.setOnClickListener(this);
        return root;
    }

    public void onResume() {
        super.onResume();
        boolean z = false;
        int value = System.getIntForUser(getContentResolver(), "oem_font_mode", 1, 0);
        this.mSystemFontButton.setChecked(value == 1);
        RadioButton radioButton = this.mSlateFontButton;
        if (value == 2) {
            z = true;
        }
        radioButton.setChecked(z);
    }

    public void onClick(View v) {
        if (v.getId() == R.id.system_font) {
            if (System.getIntForUser(getContentResolver(), "oem_font_mode", 1, 0) != 1) {
                setFontStyle(1);
                this.mSystemFontButton.setChecked(true);
                this.mSlateFontButton.setChecked(false);
            }
        } else if (v.getId() == R.id.slate_font && System.getIntForUser(getContentResolver(), "oem_font_mode", 1, 0) != 2) {
            setFontStyle(2);
            this.mSlateFontButton.setChecked(true);
            this.mSystemFontButton.setChecked(false);
        }
    }

    private void setFontStyle(final int value) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(300);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.putInt(OPFontStyleSettings.this.getContentResolver(), "oem_font_mode", value);
                Intent intent = new Intent("android.settings.OEM_FONT_MODE");
                intent.setPackage(OPFontStyleSettings.OP_THEME_PACKAGE);
                intent.putExtra("oem_font_mode", value);
                intent.addFlags(268435456);
                OPFontStyleSettings.this.getPrefContext().sendBroadcast(intent);
            }
        }).start();
    }

    public int getMetricsCategory() {
        return OPUtils.ONEPLUS_METRICSLOGGER;
    }
}
