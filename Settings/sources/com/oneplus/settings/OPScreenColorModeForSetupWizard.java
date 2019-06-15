package com.oneplus.settings;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.android.settings.R;

public class OPScreenColorModeForSetupWizard extends Activity {
    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.op_screen_color_mode_for_setupwizard_layout);
        final Intent intent = new Intent();
        ((Button) findViewById(R.id.next_button)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                intent.setComponent(new ComponentName("com.oneplus.setupwizard", "com.oneplus.setupwizard.OneplusFontSetActivity"));
                OPScreenColorModeForSetupWizard.this.startActivity(intent);
                OPScreenColorModeForSetupWizard.this.overridePendingTransition(R.anim.op_slide_in, R.anim.op_slide_out);
            }
        });
    }
}
