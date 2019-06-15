package com.android.settings.inputmethod;

import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.TextUtils;
import android.view.View;
import android.view.textservice.SpellCheckerInfo;
import com.android.settings.CustomListPreference;
import com.android.settings.R;

class SpellCheckerPreference extends CustomListPreference {
    private Intent mIntent;
    private final SpellCheckerInfo[] mScis;

    public SpellCheckerPreference(Context context, SpellCheckerInfo[] scis) {
        super(context, null);
        this.mScis = scis;
        setWidgetLayoutResource(R.layout.preference_widget_gear);
        CharSequence[] labels = new CharSequence[scis.length];
        CharSequence[] values = new CharSequence[scis.length];
        for (int i = 0; i < scis.length; i++) {
            labels[i] = scis[i].loadLabel(context.getPackageManager());
            values[i] = String.valueOf(i);
        }
        setEntries(labels);
        setEntryValues(values);
    }

    /* Access modifiers changed, original: protected */
    public void onPrepareDialogBuilder(Builder builder, OnClickListener listener) {
        builder.setTitle(R.string.choose_spell_checker);
        builder.setSingleChoiceItems(getEntries(), findIndexOfValue(getValue()), listener);
    }

    public void setSelected(SpellCheckerInfo currentSci) {
        if (currentSci == null) {
            setValue(null);
            return;
        }
        for (int i = 0; i < this.mScis.length; i++) {
            if (this.mScis[i].getId().equals(currentSci.getId())) {
                setValueIndex(i);
                return;
            }
        }
    }

    public void setValue(String value) {
        super.setValue(value);
        int index = value != null ? Integer.parseInt(value) : -1;
        if (index == -1) {
            this.mIntent = null;
            return;
        }
        SpellCheckerInfo sci = this.mScis[index];
        String settingsActivity = sci.getSettingsActivity();
        if (TextUtils.isEmpty(settingsActivity)) {
            this.mIntent = null;
        } else {
            this.mIntent = new Intent("android.intent.action.MAIN");
            this.mIntent.setClassName(sci.getPackageName(), settingsActivity);
        }
    }

    public boolean callChangeListener(Object newValue) {
        return super.callChangeListener(newValue != null ? this.mScis[Integer.parseInt((String) newValue)] : null);
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        View settingsButton = view.findViewById(R.id.settings_button);
        settingsButton.setVisibility(this.mIntent != null ? 0 : 4);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SpellCheckerPreference.this.onSettingsButtonClicked();
            }
        });
    }

    private void onSettingsButtonClicked() {
        Context context = getContext();
        try {
            Intent intent = this.mIntent;
            if (intent != null) {
                context.startActivity(intent);
            }
        } catch (ActivityNotFoundException e) {
        }
    }
}
