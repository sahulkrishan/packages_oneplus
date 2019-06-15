package com.oneplus.settings;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.OpFeatures;
import android.view.MenuItem;
import android.widget.ImageView;
import com.android.settings.R;
import com.oneplus.settings.utils.OPUtils;

public class OPRegulatoryInfoDisplayActivity extends Activity {
    private static final String ONEPLUS_A3000 = "ONEPLUS A3000";
    private static final String ONEPLUS_A5000 = "ONEPLUS A5000";
    private static final String ONEPLUS_A5010 = "ONEPLUS A5010";
    private static final String ONEPLUS_A6000 = "ONEPLUS A6000";
    private static final String ONEPLUS_A6003 = "ONEPLUS A6003";
    private static final String ONEPLUS_A6010 = "ONEPLUS A6010";
    private ImageView mRegulatoryInfoImage;

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (OPUtils.isnoDisplaySarValueProject() || (Build.MODEL.equals(getString(R.string.oneplus_oneplus_model_18821_for_eu)) && OPUtils.isEUVersion())) {
            finish();
        }
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.op_regulatory_info);
        this.mRegulatoryInfoImage = (ImageView) findViewById(R.id.regulatory_info_image);
        if (Build.MODEL.equalsIgnoreCase("ONEPLUS A5000")) {
            this.mRegulatoryInfoImage.setImageResource(R.drawable.op_regulatory_info_16859);
        } else if (Build.MODEL.equalsIgnoreCase("ONEPLUS A5010")) {
            this.mRegulatoryInfoImage.setImageResource(R.drawable.op_regulatory_info_17801);
        } else if (Build.MODEL.equalsIgnoreCase(ONEPLUS_A6000)) {
            this.mRegulatoryInfoImage.setImageResource(R.drawable.op_regulatory_info_17819_00);
        } else if (Build.MODEL.equalsIgnoreCase(ONEPLUS_A6003)) {
            this.mRegulatoryInfoImage.setImageResource(R.drawable.op_regulatory_info_17819_03);
        } else if (Build.MODEL.equalsIgnoreCase(ONEPLUS_A6010)) {
            this.mRegulatoryInfoImage.setImageResource(R.drawable.op_regulatory_info_18801_10);
        } else if (Build.MODEL.equalsIgnoreCase(getString(R.string.oneplus_model_for_china_and_india))) {
            this.mRegulatoryInfoImage.setImageResource(R.drawable.op_regulatory_info_for_00);
        } else {
            if (Build.MODEL.equalsIgnoreCase(getString(R.string.oneplus_model_for_europe_and_america))) {
                if (!OpFeatures.isSupport(new int[]{85}) && OPUtils.isO2()) {
                    this.mRegulatoryInfoImage.setImageResource(R.drawable.op_regulatory_info_for_03_global);
                }
            }
            if (Build.MODEL.equalsIgnoreCase(getString(R.string.oneplus_model_for_europe_and_america))) {
                this.mRegulatoryInfoImage.setImageResource(R.drawable.op_regulatory_info_for_03);
            } else if (OPUtils.isOP3() || OPUtils.isOP3T()) {
                this.mRegulatoryInfoImage.setImageResource(R.drawable.op_regulatory_info);
            } else if (Build.MODEL.equals(getString(R.string.oneplus_oneplus_model_18821_for_us)) || Build.MODEL.equals(getString(R.string.oneplus_oneplus_model_18831_for_us)) || Build.MODEL.equals(getString(R.string.oneplus_oneplus_model_18857_for_us)) || Build.MODEL.equals(getString(R.string.oneplus_oneplus_model_18825_for_us))) {
                this.mRegulatoryInfoImage.setImageResource(R.drawable.op_regulatory_info_custom);
            } else if (Build.MODEL.equals(getString(R.string.oneplus_oneplus_model_18821_for_eu))) {
                this.mRegulatoryInfoImage.setImageResource(R.drawable.op_regulatory_for_saudi_arabia);
            } else {
                this.mRegulatoryInfoImage.setImageResource(R.drawable.op_regulatory_info_other);
            }
        }
        if (OPUtils.isWhiteModeOn(getContentResolver())) {
            getWindow().getDecorView().setSystemUiVisibility(8192);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != 16908332) {
            return super.onOptionsItemSelected(item);
        }
        finish();
        return true;
    }
}
