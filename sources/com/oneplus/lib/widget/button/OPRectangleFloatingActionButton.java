package com.oneplus.lib.widget.button;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.oneplus.commonctrl.R;
import com.oneplus.lib.util.AnimatorUtils;

public class OPRectangleFloatingActionButton extends RelativeLayout {
    private boolean mIsDisappear1;
    private boolean mIsDisappear2;
    private boolean mIsSwitchState;
    private ImageView mNormalImageView;
    private ImageView mSwitchImageView;
    private ImageView mTempImageView;

    public OPRectangleFloatingActionButton(Context context) {
        this(context, null);
    }

    public OPRectangleFloatingActionButton(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.OPRectangleFloatingActionButtonStyle);
    }

    public OPRectangleFloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mIsDisappear1 = false;
        this.mIsDisappear2 = false;
        this.mIsSwitchState = false;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.OPRectangleFloatingActionButton, defStyleAttr, R.style.OnePlus_Widget_Design_RectangleFloatingActionButton);
        ColorStateList backgroundTint = a.getColorStateList(R.styleable.OPRectangleFloatingActionButton_op_tint_color);
        Drawable shapeDrawable = getResources().getDrawable(R.drawable.op_rectangle_floating_action_button).mutate();
        shapeDrawable.setTintList(backgroundTint);
        setBackground(new RippleDrawable(ColorStateList.valueOf(getResources().getColor(R.color.white)), shapeDrawable, null));
        ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(R.layout.op_float_switch_button, this);
        this.mNormalImageView = (ImageView) findViewById(R.id.normal_imageview);
        this.mNormalImageView.setImageDrawable(a.getDrawable(R.styleable.OPRectangleFloatingActionButton_op_image));
        this.mSwitchImageView = (ImageView) findViewById(R.id.switch_imageview);
    }

    public void setImageResource(int resId) {
        this.mNormalImageView.setImageResource(resId);
    }

    public void setNormalImageView(int resId) {
        this.mNormalImageView.setImageResource(resId);
    }

    public void setNormalImageView(Drawable drawable) {
        this.mNormalImageView.setImageDrawable(drawable);
    }

    public void setSwitchImageView(int resId) {
        this.mSwitchImageView.setImageResource(resId);
        if (!this.mIsSwitchState) {
            this.mSwitchImageView.setScaleX(0.0f);
            this.mSwitchImageView.setScaleY(0.0f);
        }
    }

    public void setSwitchImageView(Drawable drawable) {
        this.mSwitchImageView.setImageDrawable(drawable);
        if (!this.mIsSwitchState) {
            this.mSwitchImageView.setScaleX(0.0f);
            this.mSwitchImageView.setScaleY(0.0f);
        }
    }

    public boolean isFabDisappear1() {
        return this.mIsDisappear1;
    }

    public void fabDisappear1() {
        this.mIsDisappear1 = true;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("fabDisappear1 mIsDisappear1:");
        stringBuilder.append(this.mIsDisappear1);
        Log.d("OPRectangleFloatingActionButton", stringBuilder.toString());
        setPivotType(5);
        animate().scaleX(0.0f).scaleY(0.0f).setDuration(225).setInterpolator(AnimatorUtils.FastOutLinearInInterpolator);
    }

    public void fabAppears1() {
        this.mIsDisappear1 = false;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("fabAppears1 mIsDisappear1:");
        stringBuilder.append(this.mIsDisappear1);
        Log.d("OPRectangleFloatingActionButton", stringBuilder.toString());
        setPivotType(5);
        animate().scaleX(1.0f).scaleY(1.0f).setDuration(225).setInterpolator(AnimatorUtils.LinearOutSlowInInterpolator);
    }

    public boolean isFabDisappear2() {
        return this.mIsDisappear2;
    }

    public void fabDisappear2() {
        this.mIsDisappear2 = true;
        setPivotType(9);
        animate().scaleX(0.0f).scaleY(0.0f).setDuration(225).setInterpolator(AnimatorUtils.FastOutLinearInInterpolator);
    }

    public void fabAppears2() {
        this.mIsDisappear2 = false;
        setPivotType(9);
        animate().scaleX(1.0f).scaleY(1.0f).setDuration(225).setInterpolator(AnimatorUtils.LinearOutSlowInInterpolator);
    }

    public void fabSlide() {
        setPivotType(9);
        animate().scaleX(0.75f).scaleY(0.75f).setDuration(225).setInterpolator(AnimatorUtils.FastOutLinearInInterpolator);
        this.mNormalImageView.animate().scaleX(0.0f).scaleY(0.0f).setDuration(225).setInterpolator(AnimatorUtils.FastOutLinearInInterpolator);
        this.mSwitchImageView.animate().scaleX(1.0f).scaleY(1.0f).setDuration(225).setInterpolator(AnimatorUtils.LinearOutSlowInInterpolator);
    }

    public void fabSlideRevert() {
        setPivotType(9);
        animate().scaleX(1.0f).scaleY(1.0f).setDuration(225).setInterpolator(AnimatorUtils.FastOutLinearInInterpolator);
        this.mNormalImageView.animate().scaleX(1.0f).scaleY(1.0f).setDuration(225).setInterpolator(AnimatorUtils.FastOutLinearInInterpolator);
        this.mSwitchImageView.animate().scaleX(0.0f).scaleY(0.0f).setDuration(225).setInterpolator(AnimatorUtils.LinearOutSlowInInterpolator);
    }

    public boolean isSwitchState() {
        return this.mIsSwitchState;
    }

    public void fabSwitch() {
        this.mIsSwitchState = true;
        this.mNormalImageView.animate().scaleX(0.0f).scaleY(0.0f).setDuration(225).setInterpolator(AnimatorUtils.FastOutLinearInInterpolator);
        this.mSwitchImageView.animate().scaleX(1.0f).scaleY(1.0f).setDuration(225).setInterpolator(AnimatorUtils.LinearOutSlowInInterpolator);
    }

    public void fabSwitchRevert() {
        this.mIsSwitchState = false;
        this.mNormalImageView.animate().scaleX(1.0f).scaleY(1.0f).setDuration(225).setInterpolator(AnimatorUtils.FastOutLinearInInterpolator);
        this.mSwitchImageView.animate().scaleX(0.0f).scaleY(0.0f).setDuration(225).setInterpolator(AnimatorUtils.LinearOutSlowInInterpolator);
    }

    public void doFabAppearSwitch1(int resID) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("doFabAppearSwitch1 mIsSwitchState : ");
        stringBuilder.append(this.mIsSwitchState);
        Log.d("OPRectangleFloatingActionButton", stringBuilder.toString());
        stringBuilder = new StringBuilder();
        stringBuilder.append("doFabAppearSwitch1 isDisappear1:");
        stringBuilder.append(this.mIsDisappear1);
        Log.d("OPRectangleFloatingActionButton", stringBuilder.toString());
        if (isFabDisappear1()) {
            if (isSwitchState()) {
                setSwitchImageView(resID);
            } else {
                setImageResource(resID);
            }
            fabAppears1();
        } else if (isSwitchState()) {
            setImageResource(resID);
            fabSwitchRevert();
        } else {
            setSwitchImageView(resID);
            fabSwitch();
        }
    }

    public void setPivotType(int type) {
        switch (type) {
            case 1:
                setPivotY(0.0f);
                setPivotX(0.0f);
                return;
            case 2:
                setPivotY(0.0f);
                setPivotX((float) (getWidth() / 2));
                return;
            case 3:
                setPivotY(0.0f);
                setPivotX((float) getWidth());
                return;
            case 4:
                setPivotY((float) (getHeight() / 2));
                setPivotX(0.0f);
                return;
            case 5:
                setPivotY((float) (getHeight() / 2));
                setPivotX((float) (getWidth() / 2));
                return;
            case 6:
                setPivotY((float) (getHeight() / 2));
                setPivotX((float) getWidth());
                return;
            case 7:
                setPivotY((float) getHeight());
                setPivotX(0.0f);
                return;
            case 8:
                setPivotY((float) getHeight());
                setPivotX((float) (getWidth() / 2));
                return;
            case 9:
                setPivotY((float) getHeight());
                setPivotX((float) getWidth());
                return;
            default:
                return;
        }
    }
}
