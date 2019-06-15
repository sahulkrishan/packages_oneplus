package com.android.settings.applications.autofill;

import android.content.Intent;
import android.os.Bundle;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.applications.defaultapps.DefaultAutofillPicker;

public class AutofillPickerActivity extends SettingsActivity {
    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        String packageName = intent.getData().getSchemeSpecificPart();
        intent.putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT, DefaultAutofillPicker.class.getName());
        intent.putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT_TITLE_RESID, R.string.autofill_app);
        intent.putExtra("package_name", packageName);
        super.onCreate(savedInstanceState);
    }

    /* Access modifiers changed, original: protected */
    public boolean isValidFragment(String fragmentName) {
        return super.isValidFragment(fragmentName) || DefaultAutofillPicker.class.getName().equals(fragmentName);
    }
}
