package com.oneplus.settings.better;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import com.android.settings.R;
import com.oneplus.settings.BaseActivity;

public class OPFnaticModeIntroduction extends BaseActivity {
    public static final int ACTIVITED_MODE = 1;
    public static final int DEFAULT_MODE = 0;
    private static final String PSW = "alwaysfnatic";
    public static final int TAPS_TO_ACTIVE_HIDDEN_WALLPAPERS = 4;
    private int mCurrentMode = 0;
    private View mDescritionView;
    private int mDevHitCountdown = 4;
    private ImageView mLogoView;
    private EditText mPSWText;

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.op_fnatic_mode_activity);
        this.mLogoView = (ImageView) findViewById(R.id.op_fnatic_mode_head_icon);
        this.mLogoView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                OPFnaticModeIntroduction.this.enableFnaticWallPapers();
            }
        });
        this.mDescritionView = findViewById(R.id.op_fnatic_mode_top_description_1);
        this.mPSWText = (EditText) findViewById(R.id.color_egg_password);
        this.mPSWText.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
                if (TextUtils.equals(OPFnaticModeIntroduction.PSW, s.toString())) {
                    OPFnaticModeIntroduction.this.startActivity(new Intent("oneplus.intent.action.ONEPLUS_FNATIC_WALLPAPERS"));
                }
            }
        });
        this.mPSWText.setOnEditorActionListener(new OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                OPFnaticModeIntroduction.this.resetTaState();
                return false;
            }
        });
    }

    public void onResume() {
        super.onResume();
        resetTaState();
    }

    private void resetTaState() {
        this.mDevHitCountdown = 4;
        this.mDescritionView.setVisibility(0);
        this.mDescritionView.setAlpha(1.0f);
        this.mPSWText.setVisibility(4);
        this.mPSWText.setText("");
        this.mCurrentMode = 0;
    }

    public void onPause() {
        super.onPause();
        hideKeyboard(this.mPSWText);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != 16908332) {
            return super.onOptionsItemSelected(item);
        }
        onBackPressed();
        return true;
    }

    private void enableFnaticWallPapers() {
        if (this.mCurrentMode != 1) {
            if (this.mDevHitCountdown > 0) {
                this.mDevHitCountdown--;
            } else {
                this.mCurrentMode = 1;
                ObjectAnimator alpahAnimator = ObjectAnimator.ofFloat(this.mDescritionView, "alpha", new float[]{1.0f, 0.0f});
                alpahAnimator.addListener(new AnimatorListener() {
                    public void onAnimationStart(Animator animation) {
                    }

                    public void onAnimationEnd(Animator animation) {
                        OPFnaticModeIntroduction.this.mDescritionView.setVisibility(4);
                    }

                    public void onAnimationCancel(Animator animation) {
                    }

                    public void onAnimationRepeat(Animator animation) {
                    }
                });
                alpahAnimator.setDuration(225);
                alpahAnimator.start();
                AnimationSet animSet = new AnimationSet(true);
                ScaleAnimation scaleAnimator = new ScaleAnimation(0.5f, 1.0f, 1.0f, 1.0f, 2, 0.5f, 2, 0.5f);
                scaleAnimator.setDuration(225);
                AlphaAnimation alpahAnimator1 = new AlphaAnimation(0.0f, 1.0f);
                alpahAnimator1.setDuration(225);
                animSet.addAnimation(scaleAnimator);
                animSet.addAnimation(alpahAnimator1);
                animSet.setStartOffset(225);
                this.mPSWText.startAnimation(animSet);
                this.mPSWText.setVisibility(0);
                this.mPSWText.requestFocus();
                showKeyboard(this.mPSWText);
            }
        }
    }

    public void showKeyboard(View v) {
        ((InputMethodManager) v.getContext().getSystemService("input_method")).showSoftInput(v, 2);
    }

    public static void hideKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService("input_method");
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
        }
    }
}
