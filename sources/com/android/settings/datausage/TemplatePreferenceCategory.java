package com.android.settings.datausage;

import android.content.Context;
import android.net.NetworkTemplate;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.util.AttributeSet;
import com.android.settings.datausage.TemplatePreference.NetworkServices;

public class TemplatePreferenceCategory extends PreferenceCategory implements TemplatePreference {
    private int mSubId;
    private NetworkTemplate mTemplate;

    public TemplatePreferenceCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setTemplate(NetworkTemplate template, int subId, NetworkServices services) {
        this.mTemplate = template;
        this.mSubId = subId;
    }

    public boolean addPreference(Preference preference) {
        if (preference instanceof TemplatePreference) {
            return super.addPreference(preference);
        }
        throw new IllegalArgumentException("TemplatePreferenceCategories can only hold TemplatePreferences");
    }

    public void pushTemplates(NetworkServices services) {
        if (this.mTemplate != null) {
            for (int i = 0; i < getPreferenceCount(); i++) {
                ((TemplatePreference) getPreference(i)).setTemplate(this.mTemplate, this.mSubId, services);
            }
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("null mTemplate for ");
        stringBuilder.append(getKey());
        throw new RuntimeException(stringBuilder.toString());
    }
}
