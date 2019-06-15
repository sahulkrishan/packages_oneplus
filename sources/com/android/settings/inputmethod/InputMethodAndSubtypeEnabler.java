package com.android.settings.inputmethod;

import android.os.Bundle;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.inputmethod.InputMethodAndSubtypeEnablerManager;

public class InputMethodAndSubtypeEnabler extends SettingsPreferenceFragment {
    private InputMethodAndSubtypeEnablerManager mManager;

    public int getMetricsCategory() {
        return 60;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        String targetImi = getStringExtraFromIntentOrArguments("input_method_id");
        PreferenceScreen root = getPreferenceManager().createPreferenceScreen(getPrefContext());
        this.mManager = new InputMethodAndSubtypeEnablerManager(this);
        this.mManager.init(this, targetImi, root);
        setPreferenceScreen(root);
    }

    private String getStringExtraFromIntentOrArguments(String name) {
        String fromIntent = getActivity().getIntent().getStringExtra(name);
        if (fromIntent != null) {
            return fromIntent;
        }
        Bundle arguments = getArguments();
        return arguments == null ? null : arguments.getString(name);
    }

    public void onActivityCreated(Bundle icicle) {
        super.onActivityCreated(icicle);
        String title = getStringExtraFromIntentOrArguments("android.intent.extra.TITLE");
        if (!TextUtils.isEmpty(title)) {
            getActivity().setTitle(title);
        }
    }

    public void onResume() {
        super.onResume();
        this.mManager.refresh(getContext(), this);
    }

    public void onPause() {
        super.onPause();
        this.mManager.save(getContext(), this);
    }
}
