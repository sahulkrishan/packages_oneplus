package com.android.settings.nfc;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.android.settings.R;
import com.oneplus.settings.utils.OPUtils;

public class HowItWorks extends Activity {
    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (OPUtils.isWhiteModeOn(getContentResolver())) {
            getWindow().getDecorView().setSystemUiVisibility(8192);
        }
        setContentView(R.layout.nfc_payment_how_it_works);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        ((Button) findViewById(R.id.nfc_how_it_works_button)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                HowItWorks.this.finish();
            }
        });
    }

    public boolean onNavigateUp() {
        finish();
        return true;
    }
}
