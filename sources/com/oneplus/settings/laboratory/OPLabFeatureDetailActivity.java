package com.oneplus.settings.laboratory;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.support.graphics.drawable.PathInterpolatorCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.preference.VibratorSceneUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.android.settings.R;
import com.oneplus.settings.BaseActivity;
import com.oneplus.settings.laboratory.OPRadioButtinGroup.OnRadioGroupClickListener;
import com.oneplus.settings.utils.OPUtils;

public class OPLabFeatureDetailActivity extends BaseActivity implements OnClickListener, OnRadioGroupClickListener {
    private static final String KEY_ONEPLUS_DC_DIMMING_VALUE = "oneplus_dc_dimming_value";
    private static final int ONEPLUS_LAB_FEATURE_DISLIKE = -1;
    private static final String ONEPLUS_LAB_FEATURE_ICON_ID = "oneplus_lab_feature_icon_id";
    private static final String ONEPLUS_LAB_FEATURE_KEY = "oneplus_lab_feature_key";
    private static final int ONEPLUS_LAB_FEATURE_LIKE = 1;
    private static final String ONEPLUS_LAB_FEATURE_SUMMARY = "oneplus_lab_feature_Summary";
    private static final String ONEPLUS_LAB_FEATURE_TITLE = "oneplus_lab_feature_title";
    private static final String ONEPLUS_LAB_FEATURE_TOGGLE_COUNT = "oneplus_lab_feature_toggle_count";
    private static final String ONEPLUS_LAB_FEATURE_TOGGLE_NAMES = "oneplus_lab_feature_toggle_names";
    private static final String SHOW_IMPORTANCE_SLIDER = "show_importance_slider";
    private static Toast mToast;
    private View mActiviteFeatureToggle;
    private TextView mCommunirySummary;
    private TextView mCommuniryTitle;
    private TextView mDescriptionSummary;
    private ImageButton mDislikeImageButton;
    private String[] mFeatureToggleNames;
    private int mIconId;
    private ImageView mImageView;
    private Intent mIntent;
    private ImageButton mLikeImageButton;
    private OPRadioButtinGroup mMultiToggleGroup;
    private String mOneplusLabFeatureKey;
    private String mOneplusLabFeatureTitle;
    private int mOneplusLabFeatureToggleCount;
    private SharedPreferences mSharedPreferences;
    private Switch mSwitch;
    private long[] mVibratePattern;
    private Vibrator mVibrator;

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.op_lab_feature_details_activity);
        if (OPUtils.isSupportXVibrate()) {
            this.mVibrator = (Vibrator) getSystemService("vibrator");
        }
        initIntent();
        initView();
    }

    private void initIntent() {
        this.mIntent = getIntent();
        this.mOneplusLabFeatureToggleCount = this.mIntent.getIntExtra(ONEPLUS_LAB_FEATURE_TOGGLE_COUNT, 2);
        this.mFeatureToggleNames = this.mIntent.getStringArrayExtra(ONEPLUS_LAB_FEATURE_TOGGLE_NAMES);
        this.mOneplusLabFeatureTitle = this.mIntent.getStringExtra(ONEPLUS_LAB_FEATURE_TITLE);
        this.mOneplusLabFeatureKey = this.mIntent.getStringExtra(ONEPLUS_LAB_FEATURE_KEY);
        this.mIconId = this.mIntent.getIntExtra(ONEPLUS_LAB_FEATURE_ICON_ID, 0);
        setTitle(this.mOneplusLabFeatureTitle);
    }

    private void initView() {
        this.mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        this.mDescriptionSummary = (TextView) findViewById(R.id.op_lab_feature_description_summary);
        this.mCommuniryTitle = (TextView) findViewById(R.id.op_lab_feature_communiry_title);
        this.mCommunirySummary = (TextView) findViewById(R.id.op_lab_feature_communiry_summary);
        this.mActiviteFeatureToggle = findViewById(R.id.op_lab_feature_toggle);
        this.mSwitch = (Switch) findViewById(R.id.op_lab_feature_switch);
        boolean z = true;
        Switch switchR;
        if (SHOW_IMPORTANCE_SLIDER.equals(this.mOneplusLabFeatureKey)) {
            switchR = this.mSwitch;
            if (Secure.getInt(getContentResolver(), this.mOneplusLabFeatureKey, 0) != 1) {
                z = false;
            }
            switchR.setChecked(z);
        } else {
            switchR = this.mSwitch;
            if (System.getInt(getContentResolver(), this.mOneplusLabFeatureKey, 0) != 1) {
                z = false;
            }
            switchR.setChecked(z);
        }
        this.mMultiToggleGroup = (OPRadioButtinGroup) findViewById(R.id.op_lab_feature_multi_toggle_group);
        if (isMultiToggle()) {
            this.mMultiToggleGroup.addChild(this.mOneplusLabFeatureToggleCount, this.mFeatureToggleNames);
            this.mMultiToggleGroup.setOnRadioGroupClickListener(this);
            this.mMultiToggleGroup.setSelect(System.getInt(getContentResolver(), this.mOneplusLabFeatureKey, 0));
            this.mActiviteFeatureToggle.setVisibility(8);
        } else {
            this.mMultiToggleGroup.setVisibility(8);
        }
        this.mLikeImageButton = (ImageButton) findViewById(R.id.op_lab_feature_communiry_like);
        this.mDislikeImageButton = (ImageButton) findViewById(R.id.op_lab_feature_communiry_dislike);
        this.mActiviteFeatureToggle.setOnClickListener(this);
        this.mLikeImageButton.setOnClickListener(this);
        this.mDislikeImageButton.setOnClickListener(this);
        this.mDescriptionSummary.setText(this.mIntent.getStringExtra(ONEPLUS_LAB_FEATURE_SUMMARY));
        setLikeOrDislike();
    }

    public boolean isMultiToggle() {
        return this.mOneplusLabFeatureToggleCount > 2;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != 16908332) {
            return super.onOptionsItemSelected(item);
        }
        finish();
        return true;
    }

    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.op_lab_feature_communiry_dislike) {
            saveActitiveHistory(-1);
        } else if (id == R.id.op_lab_feature_communiry_like) {
            saveActitiveHistory(1);
        } else if (id == R.id.op_lab_feature_toggle) {
            boolean z = false;
            if (this.mSwitch.isChecked()) {
                this.mSwitch.setChecked(false);
            } else {
                this.mSwitch.setChecked(true);
            }
            if (VibratorSceneUtils.systemVibrateEnabled(this)) {
                this.mVibratePattern = VibratorSceneUtils.getVibratorScenePattern(this, this.mVibrator, 1003);
                VibratorSceneUtils.vibrateIfNeeded(this.mVibratePattern, this.mVibrator);
            }
            if (this.mSwitch.isChecked()) {
                z = true;
            }
            boolean isChecked = z;
            if (SHOW_IMPORTANCE_SLIDER.equals(this.mOneplusLabFeatureKey)) {
                Secure.putInt(getContentResolver(), this.mOneplusLabFeatureKey, isChecked);
            } else {
                System.putInt(getContentResolver(), this.mOneplusLabFeatureKey, isChecked);
            }
            if (KEY_ONEPLUS_DC_DIMMING_VALUE.equalsIgnoreCase(this.mOneplusLabFeatureKey)) {
                OPUtils.sendAnalytics("dc_dimming", NotificationCompat.CATEGORY_STATUS, this.mSwitch.isChecked() ? "1" : "0");
            }
        }
    }

    public void OnRadioGroupClick(int clickId) {
        System.putInt(getContentResolver(), this.mOneplusLabFeatureKey, clickId);
        OPUtils.sendAppTracker(this.mOneplusLabFeatureKey, clickId);
    }

    private void saveActitiveHistory(int likeOrDislike) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.mOneplusLabFeatureKey);
        stringBuilder.append("_feedback");
        OPUtils.sendAppTracker(stringBuilder.toString(), likeOrDislike);
        Editor editor = this.mSharedPreferences.edit();
        editor.putInt(this.mOneplusLabFeatureKey, likeOrDislike);
        editor.commit();
        showToastTip();
        setLikeOrDislike();
    }

    private void showToastTip() {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(getApplicationContext(), R.string.oneplus_lab_feedback_toast, PathInterpolatorCompat.MAX_NUM_POINTS);
        mToast.show();
    }

    private void setLikeOrDislike() {
        if (this.mSharedPreferences.contains(this.mOneplusLabFeatureKey)) {
            highlightUserChoose(this.mSharedPreferences.getInt(this.mOneplusLabFeatureKey, 1));
            return;
        }
        this.mLikeImageButton.getBackground().setTint(getColor(R.color.oneplus_laboratory_grey_color));
        this.mDislikeImageButton.getBackground().setTint(getColor(R.color.oneplus_laboratory_grey_color));
        this.mLikeImageButton.setImageResource(R.drawable.op_ic_oneplus_lab_feature_like);
        this.mDislikeImageButton.setImageResource(R.drawable.op_ic_oneplus_lab_feature_dislike);
    }

    private void highlightUserChoose(int likeOrDislike) {
        if (likeOrDislike == 1) {
            this.mLikeImageButton.getBackground().setTint(getColor(R.color.oneplus_accent_color));
            this.mDislikeImageButton.getBackground().setTint(getColor(R.color.oneplus_laboratory_grey_color));
            this.mLikeImageButton.setImageResource(R.drawable.op_ic_oneplus_lab_feature_like_fill);
            this.mDislikeImageButton.setImageResource(R.drawable.op_ic_oneplus_lab_feature_dislike);
        } else if (likeOrDislike == -1) {
            this.mLikeImageButton.getBackground().setTint(getColor(R.color.oneplus_laboratory_grey_color));
            this.mDislikeImageButton.getBackground().setTint(getColor(R.color.oneplus_accent_color));
            this.mLikeImageButton.setImageResource(R.drawable.op_ic_oneplus_lab_feature_like);
            this.mDislikeImageButton.setImageResource(R.drawable.op_ic_oneplus_lab_feature_dislike_fill);
        } else {
            this.mLikeImageButton.setImageResource(R.drawable.op_ic_oneplus_lab_feature_like);
            this.mDislikeImageButton.setImageResource(R.drawable.op_ic_oneplus_lab_feature_dislike);
            this.mLikeImageButton.getBackground().setTint(getColor(R.color.oneplus_laboratory_grey_color));
            this.mDislikeImageButton.getBackground().setTint(getColor(R.color.oneplus_laboratory_grey_color));
        }
    }
}
