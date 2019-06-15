package com.oneplus.settings;

import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.IActivityManager;
import android.content.ComponentName;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.Settings.System;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RadioButton;
import com.android.settings.R;
import com.oneplus.settings.utils.OPConstants;
import com.oneplus.settings.utils.OPUtils;
import java.util.List;

public class OPNotchDisplayGuideActivity extends BaseActivity implements OnClickListener {
    private static final int HIDE_NOTCH = 1;
    private static final String ONEPLUS_NOTCH_MODE = "op_camera_notch_ignore";
    private static final int SHOW_NOTCH = 0;
    private ActivityManager mAm;
    private IActivityManager mAms;
    private ImageView mHideNotch;
    private RadioButton mHideNotchBtn;
    private View mHideNotchMode;
    private ImageView mShowNotch;
    private RadioButton mShowNotchBtn;
    private View mShowNotchMode;

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.op_fullscreen_app_guide_layout);
        this.mAm = (ActivityManager) getSystemService("activity");
        this.mAms = ActivityManager.getService();
        this.mShowNotchMode = findViewById(R.id.op_show_notch_mode);
        this.mShowNotchMode.setOnClickListener(this);
        this.mHideNotchMode = findViewById(R.id.op_hide_notch_mode);
        this.mHideNotchMode.setOnClickListener(this);
        this.mShowNotchBtn = (RadioButton) findViewById(R.id.op_show_notch_btn);
        this.mHideNotchBtn = (RadioButton) findViewById(R.id.op_hide_notch_btn);
        this.mShowNotch = (ImageView) findViewById(R.id.op_show_notch_image);
        this.mHideNotch = (ImageView) findViewById(R.id.op_hide_notch_image);
        if (OPUtils.isBlackModeOn(SettingsBaseApplication.mApplication.getContentResolver())) {
            this.mShowNotch.setBackgroundResource(R.drawable.op_fullscreen_mode_guide_dark);
            this.mHideNotch.setBackgroundResource(R.drawable.op_compatibility_mode_guide_dark);
        } else {
            this.mShowNotch.setBackgroundResource(R.drawable.op_fullscreen_mode_guide_light);
            this.mHideNotch.setBackgroundResource(R.drawable.op_compatibility_mode_guide_light);
        }
        setCurrentMode();
    }

    public void onClick(View v) {
        if (v == this.mShowNotchMode) {
            this.mShowNotchBtn.setChecked(true);
            this.mHideNotchBtn.setChecked(false);
            setNotchMode(0);
            OPUtils.sendAppTracker("notch_display", 1);
        } else if (v == this.mHideNotchMode) {
            this.mShowNotchBtn.setChecked(false);
            this.mHideNotchBtn.setChecked(true);
            setNotchMode(1);
            OPUtils.sendAppTracker("notch_display", 0);
        }
    }

    private void setCurrentMode() {
        boolean z = false;
        int notchMode = System.getInt(getContentResolver(), ONEPLUS_NOTCH_MODE, 0);
        this.mShowNotchBtn.setChecked(notchMode == 0);
        RadioButton radioButton = this.mHideNotchBtn;
        if (notchMode == 1) {
            z = true;
        }
        radioButton.setChecked(z);
    }

    private void setNotchMode(int mode) {
        removeRunningTask();
        killSomeProcess();
        System.putInt(getContentResolver(), ONEPLUS_NOTCH_MODE, mode);
    }

    private void killSomeProcess() {
        this.mAm.killBackgroundProcesses(OPConstants.PACKAGENAME_DIALER);
        this.mAm.killBackgroundProcesses(OPConstants.PACKAGENAME_CONTACTS);
        this.mAm.killBackgroundProcesses(OPConstants.PACKAGENAME_MMS);
    }

    private void removeRunningTask() {
        List<RecentTaskInfo> recentTaskInfos = null;
        try {
            recentTaskInfos = ActivityManager.getService().getRecentTasks(Integer.MAX_VALUE, 2, -2).getList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (recentTaskInfos != null) {
            boolean skipSettings = false;
            for (RecentTaskInfo recentTaskInfo : recentTaskInfos) {
                if (!skipSettings) {
                    ComponentName topActivity = recentTaskInfo != null ? recentTaskInfo.topActivity : null;
                    if (topActivity != null && "com.android.settings".equals(topActivity.getPackageName())) {
                        skipSettings = true;
                    }
                }
                if (recentTaskInfo != null) {
                    try {
                        ActivityManager.getService().removeTask(recentTaskInfo.persistentId);
                    } catch (RemoteException e2) {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("Failed to remove task=");
                        stringBuilder.append(recentTaskInfo.persistentId);
                        Log.w("OPNotchDisplayGuideActivity", stringBuilder.toString(), e2);
                    }
                }
            }
        }
    }
}
