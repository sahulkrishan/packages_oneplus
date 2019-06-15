package com.android.settings.development;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;
import com.android.settings.R;

public class DevelopmentSettingsDisabledActivity extends Activity {
    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toast.makeText(this, R.string.dev_settings_disabled_warning, 0).show();
        finish();
    }
}
