package com.oneplus.settings;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.android.settings.R;

public class OPScreenColorModeForProvision extends Activity {
    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.op_screen_color_mode_for_provision_layout);
        getWindow().getDecorView().setSystemUiVisibility(8448);
        ((Button) findViewById(R.id.next_button)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.oneplus.provision", "com.oneplus.provision.FontSetActivity"));
                OPScreenColorModeForProvision.this.startActivity(intent);
                OPScreenColorModeForProvision.this.overridePendingTransition(R.anim.op_slide_in, R.anim.op_slide_out);
            }
        });
    }

    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.op_reverse_slide_in, R.anim.op_reverse_slide_out);
    }
}
