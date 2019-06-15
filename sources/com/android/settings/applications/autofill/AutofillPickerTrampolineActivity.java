package com.android.settings.applications.autofill;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.autofill.AutofillManager;
import com.android.settings.applications.defaultapps.DefaultAutofillPicker;

public class AutofillPickerTrampolineActivity extends Activity {
    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String packageName = intent.getData().getSchemeSpecificPart();
        String currentService = DefaultAutofillPicker.getDefaultKey(this);
        if (currentService == null || !currentService.startsWith(packageName)) {
            AutofillManager afm = (AutofillManager) getSystemService(AutofillManager.class);
            if (afm != null && afm.hasAutofillFeature() && afm.isAutofillSupported()) {
                startActivity(new Intent(this, AutofillPickerActivity.class).setFlags(33554432).setData(intent.getData()));
                finish();
                return;
            }
            setResult(0);
            finish();
            return;
        }
        setResult(-1);
        finish();
    }
}
