package com.oneplus.settings;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.settings.R;
import com.oneplus.settings.utils.OPUtils;

public class OPAuthenticationInformationSettings extends Activity {
    private static final String ONEPLUS_18821_FOR_CN_CMITTID = "2018CP7481";
    private static final String ONEPLUS_18857_FOR_CN_CMITTID = "2018CP7482";
    private static final String ONEPLUS_A3000 = "oneplus A3000";
    private static final String ONEPLUS_A3000_CMIITID = "2016CP1331";
    private static final String ONEPLUS_A3010 = "oneplus A3010";
    private static final String ONEPLUS_A3010_CMIITID = "2016CP5088";
    private static final String ONEPLUS_A5000 = "ONEPLUS A5000";
    private static final String ONEPLUS_A5000_CMITTID = "2017CP2198";
    private static final String ONEPLUS_A5010 = "ONEPLUS A5010";
    private static final String ONEPLUS_A5010_CMITTID = "2017CP6039";
    private static final String ONEPLUS_A6000 = "ONEPLUS A6000";
    private static final String ONEPLUS_A6000_CMITTID = "2018CP1307";
    private static final String ONEPLUS_XXXXXXXX_CMITTID = "XXXXXXXXXX";
    private ImageView mAuthenticationImage;
    private TextView mCmiitIdView;
    private TextView mModelTextView;

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(R.string.oneplus_authentication_information);
        setContentView(R.layout.op_authentication_information_settings);
        this.mModelTextView = (TextView) findViewById(R.id.authentication_information_model);
        this.mCmiitIdView = (TextView) findViewById(R.id.authentication_information_cmiit_id);
        this.mAuthenticationImage = (ImageView) findViewById(R.id.authentication_information_image);
        if (OPUtils.isWhiteModeOn(getContentResolver())) {
            getWindow().getDecorView().setSystemUiVisibility(8192);
        }
        setDeviceType();
        setCmiitID();
    }

    private void setDeviceType() {
        String deviceModel = getResources().getString(R.string.oneplus_authentication_information_model);
        this.mModelTextView.setText(String.format(deviceModel, new Object[]{Build.MODEL}));
    }

    private void setCmiitID() {
        String cmiitid = getResources().getString(R.string.oneplus_authentication_information_cmiit_id);
        if (Build.MODEL.equalsIgnoreCase(ONEPLUS_A3000)) {
            this.mCmiitIdView.setText(String.format(cmiitid, new Object[]{ONEPLUS_A3000_CMIITID}));
        } else if (Build.MODEL.equalsIgnoreCase(ONEPLUS_A3010)) {
            this.mCmiitIdView.setText(String.format(cmiitid, new Object[]{ONEPLUS_A3010_CMIITID}));
        } else if (Build.MODEL.equalsIgnoreCase("ONEPLUS A5000")) {
            this.mCmiitIdView.setText(String.format(cmiitid, new Object[]{ONEPLUS_A5000_CMITTID}));
        } else if (Build.MODEL.equalsIgnoreCase("ONEPLUS A5010")) {
            this.mCmiitIdView.setText(String.format(cmiitid, new Object[]{ONEPLUS_A5010_CMITTID}));
        } else if (Build.MODEL.equalsIgnoreCase(ONEPLUS_A6000)) {
            this.mAuthenticationImage.setImageResource(R.drawable.op_authentication_information_image_17819);
            this.mCmiitIdView.setText(String.format(cmiitid, new Object[]{ONEPLUS_A6000_CMITTID}));
        } else if (Build.MODEL.equalsIgnoreCase(getString(R.string.oneplus_model_for_china_and_india)) || Build.MODEL.equalsIgnoreCase(getString(R.string.oneplus_model_for_europe_and_america))) {
            this.mCmiitIdView.setText(String.format(cmiitid, new Object[]{getString(R.string.oneplus_cmittid)}));
            this.mAuthenticationImage.setImageResource(R.drawable.op_authentication_information_image_17819);
        } else if (Build.MODEL.equalsIgnoreCase(getString(R.string.oneplus_oneplus_model_18821_for_cn))) {
            this.mCmiitIdView.setText(String.format(cmiitid, new Object[]{ONEPLUS_18821_FOR_CN_CMITTID}));
            this.mAuthenticationImage.setImageResource(R.drawable.op_authentication_information_image_17819);
        } else if (Build.MODEL.equalsIgnoreCase(getString(R.string.oneplus_oneplus_model_18857_for_cn))) {
            this.mCmiitIdView.setText(String.format(cmiitid, new Object[]{ONEPLUS_18857_FOR_CN_CMITTID}));
            this.mAuthenticationImage.setImageResource(R.drawable.op_authentication_information_image_17819);
        } else {
            this.mCmiitIdView.setText(String.format(cmiitid, new Object[]{ONEPLUS_XXXXXXXX_CMITTID}));
            this.mAuthenticationImage.setImageResource(R.drawable.op_authentication_information_image_17819);
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
