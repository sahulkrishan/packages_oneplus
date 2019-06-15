package com.oneplus.settings.aboutphone;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.settings.R;

public class OPAboutPhoneHardWareController {
    private String mCameraMessage;
    private String mCpuMessage;
    private final View mHardWareInfoView;
    private Drawable mIcon;
    private String mScreenMessage;
    private String mStorageMessage;

    public static OPAboutPhoneHardWareController newInstance(Activity activity, Fragment fragment, View header) {
        return new OPAboutPhoneHardWareController(activity, fragment, header);
    }

    private OPAboutPhoneHardWareController(Activity activity, Fragment fragment, View header) {
        if (header != null) {
            this.mHardWareInfoView = header;
        } else {
            this.mHardWareInfoView = LayoutInflater.from(fragment.getContext()).inflate(R.layout.op_about_phone_hareware_layout, null);
        }
    }

    public OPAboutPhoneHardWareController setPhoneImage(Drawable icon) {
        this.mIcon = icon;
        return this;
    }

    public OPAboutPhoneHardWareController setCpuMessage(String message) {
        this.mCpuMessage = message;
        return this;
    }

    public OPAboutPhoneHardWareController setStorageMessage(String message) {
        this.mStorageMessage = message;
        return this;
    }

    public OPAboutPhoneHardWareController setCameraMessage(String message) {
        this.mCameraMessage = message;
        return this;
    }

    public OPAboutPhoneHardWareController setScreenMessage(String message) {
        this.mScreenMessage = message;
        return this;
    }

    public View done() {
        TextView mCpuMessageView = (TextView) this.mHardWareInfoView.findViewById(R.id.cpu_message);
        TextView mStorageMessageView = (TextView) this.mHardWareInfoView.findViewById(R.id.storage_message);
        TextView mCameraMessageView = (TextView) this.mHardWareInfoView.findViewById(R.id.camera_message);
        TextView mScreenMessageView = (TextView) this.mHardWareInfoView.findViewById(R.id.screen_message);
        ((ImageView) this.mHardWareInfoView.findViewById(R.id.phone_image)).setImageDrawable(this.mIcon);
        mCpuMessageView.setText(this.mCpuMessage);
        mCpuMessageView.setVisibility(0);
        mStorageMessageView.setText(this.mStorageMessage);
        mCameraMessageView.setText(this.mCameraMessage);
        mScreenMessageView.setText(this.mScreenMessage);
        return this.mHardWareInfoView;
    }
}
