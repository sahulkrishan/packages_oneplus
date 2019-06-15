package com.oneplus.settings;

import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.SearchIndexableResource;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.support.v17.leanback.media.MediaPlayerGlue;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.ui.RadioButtonPreference;
import com.android.settings.ui.RadioButtonPreference.OnClickListener;
import com.android.settingslib.display.DisplayDensityUtils;
import com.oneplus.settings.highpowerapp.PackageUtils;
import com.oneplus.settings.utils.OPApplicationUtils;
import com.oneplus.settings.utils.OPConstants;
import com.oneplus.settings.utils.OPUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OPScreenResolutionAdjust extends SettingsPreferenceFragment implements OnClickListener, Indexable {
    private static final int DEFAULT_DENSITY_INDEX = 1;
    private static final int DEFAULT_MODE = 2;
    private static final String KEY_OP_1080P_MODE = "op_1080p_mode";
    private static final String KEY_OP_INTELLIGENT_SWITCH_RESOLUTION_MODE = "op_intelligent_switch_resolution_mode";
    private static final String KEY_OP_OTHER_RESOLUTION_MODE = "op_other_resolution_mode";
    public static final String ONEPLUS_SCREEN_RESOLUTION_ADJUST = "oneplus_screen_resolution_adjust";
    public static final int OP_1080P_MODE = 1;
    public static final int OP_INTELLIGENT_SWITCH_RESOLUTION_MODE = 2;
    public static final int OP_OTHER_RESOLUTION_MODE = 0;
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = R.xml.op_screen_resolution_adjust_select;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }

        public List<String> getNonIndexableKeys(Context context) {
            return new ArrayList();
        }
    };
    private int[] DPI_VALUES_1080P = new int[]{380, 420, 480, 500, 540};
    private int[] DPI_VALUES_OTHER;
    private RadioButtonPreference m1080PMode;
    private ActivityManager mAm;
    private Context mContext;
    private int mEnterValue;
    private Handler mHandler = new Handler();
    private RadioButtonPreference mIntelligentSwitchResolutionMode;
    private RadioButtonPreference mOtherResolutionMode;
    private AlertDialog mWarnDialog;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.op_screen_resolution_adjust_select);
        this.mContext = SettingsBaseApplication.mApplication;
        this.mAm = (ActivityManager) getSystemService("activity");
        this.mIntelligentSwitchResolutionMode = (RadioButtonPreference) findPreference(KEY_OP_INTELLIGENT_SWITCH_RESOLUTION_MODE);
        this.mOtherResolutionMode = (RadioButtonPreference) findPreference(KEY_OP_OTHER_RESOLUTION_MODE);
        this.m1080PMode = (RadioButtonPreference) findPreference(KEY_OP_1080P_MODE);
        this.mIntelligentSwitchResolutionMode.setOnClickListener(this);
        this.mOtherResolutionMode.setOnClickListener(this);
        this.m1080PMode.setOnClickListener(this);
        this.DPI_VALUES_OTHER = this.mContext.getResources().getIntArray(R.array.oneplus_screen_dpi_values);
        this.mEnterValue = Global.getInt(this.mContext.getContentResolver(), ONEPLUS_SCREEN_RESOLUTION_ADJUST, 2);
    }

    public void onResume() {
        super.onResume();
        boolean z = false;
        if (getActivity().isInMultiWindowMode()) {
            this.mIntelligentSwitchResolutionMode.setEnabled(false);
            this.mOtherResolutionMode.setEnabled(false);
            this.m1080PMode.setEnabled(false);
        }
        int value = Global.getInt(this.mContext.getContentResolver(), ONEPLUS_SCREEN_RESOLUTION_ADJUST, 2);
        this.mIntelligentSwitchResolutionMode.setChecked(value == 2);
        this.mOtherResolutionMode.setChecked(value == 0);
        RadioButtonPreference radioButtonPreference = this.m1080PMode;
        if (value == 1) {
            z = true;
        }
        radioButtonPreference.setChecked(z);
    }

    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        super.onMultiWindowModeChanged(isInMultiWindowMode);
        if (!isInMultiWindowMode) {
            this.mIntelligentSwitchResolutionMode.setEnabled(true);
            this.mOtherResolutionMode.setEnabled(true);
            this.m1080PMode.setEnabled(true);
        }
    }

    public void onRadioButtonClicked(RadioButtonPreference emiter) {
        if (emiter == this.mIntelligentSwitchResolutionMode) {
            if (!isIntelligentSwitchMode()) {
                showWarnigDialog(2);
            }
        } else if (emiter == this.mOtherResolutionMode) {
            if (!isOtherMode()) {
                showWarnigDialog(0);
            }
        } else if (emiter == this.m1080PMode && !is1080pMode()) {
            showWarnigDialog(1);
        }
    }

    private void changeScreenResolution(int mode) {
        if (mode == 2) {
            this.mIntelligentSwitchResolutionMode.setChecked(true);
            this.mOtherResolutionMode.setChecked(false);
            this.m1080PMode.setChecked(false);
            if (is1080pMode()) {
                DisplayDensityUtils.setForcedDisplayDensity(0, this.DPI_VALUES_OTHER[getCurrent1080pDpiIndex()]);
            }
            Global.putInt(this.mContext.getContentResolver(), ONEPLUS_SCREEN_RESOLUTION_ADJUST, 2);
        } else if (mode == 0) {
            this.mIntelligentSwitchResolutionMode.setChecked(false);
            this.mOtherResolutionMode.setChecked(true);
            this.m1080PMode.setChecked(false);
            if (is1080pMode()) {
                DisplayDensityUtils.setForcedDisplayDensity(0, this.DPI_VALUES_OTHER[getCurrent1080pDpiIndex()]);
            }
            Global.putInt(this.mContext.getContentResolver(), ONEPLUS_SCREEN_RESOLUTION_ADJUST, 0);
        } else if (mode == 1) {
            this.mIntelligentSwitchResolutionMode.setChecked(false);
            this.mOtherResolutionMode.setChecked(false);
            this.m1080PMode.setChecked(true);
            if (!is1080pMode()) {
                DisplayDensityUtils.setForcedDisplayDensity(0, this.DPI_VALUES_1080P[getCurrentOtherDpiIndex()]);
            }
            Global.putInt(this.mContext.getContentResolver(), ONEPLUS_SCREEN_RESOLUTION_ADJUST, 1);
        }
        removeRunningTask();
        killRunningProcess();
    }

    public void showWarnigDialog(final int mode) {
        this.mWarnDialog = new Builder(getActivity()).setMessage(R.string.oneplus_switch_resolution_kill_process_tips).setPositiveButton(R.string.oneplus_switch_resolution_kill_process_confirm, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                OPScreenResolutionAdjust.this.changeScreenResolution(mode);
            }
        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create();
        this.mWarnDialog.show();
    }

    private boolean isIntelligentSwitchMode() {
        if (Global.getInt(this.mContext.getContentResolver(), ONEPLUS_SCREEN_RESOLUTION_ADJUST, 2) == 2) {
            return true;
        }
        return false;
    }

    private boolean isOtherMode() {
        if (Global.getInt(this.mContext.getContentResolver(), ONEPLUS_SCREEN_RESOLUTION_ADJUST, 2) == 0) {
            return true;
        }
        return false;
    }

    private boolean is1080pMode() {
        if (Global.getInt(this.mContext.getContentResolver(), ONEPLUS_SCREEN_RESOLUTION_ADJUST, 2) == 1) {
            return true;
        }
        return false;
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
                        Log.w("OPScreenResolutionAdjust", stringBuilder.toString(), e2);
                    }
                }
            }
        }
    }

    private void killRunningProcess() {
        List<RunningAppProcessInfo> runningProcessInfos = this.mAm.getRunningAppProcesses();
        if (runningProcessInfos != null) {
            for (RunningAppProcessInfo runningProcessInfo : runningProcessInfos) {
                if (runningProcessInfo != null) {
                    if (!PackageUtils.isSystemApplication(this.mContext, runningProcessInfo.processName)) {
                        if (runningProcessInfo.uid > MediaPlayerGlue.FAST_FORWARD_REWIND_STEP) {
                            if (!OPUtils.isO2() || !OPApplicationUtils.isOnePlusO2UninstallationApp(runningProcessInfo.processName)) {
                                if (OPUtils.isO2() || !OPApplicationUtils.isOnePlusH2UninstallationApp(runningProcessInfo.processName)) {
                                    StringBuilder stringBuilder = new StringBuilder();
                                    stringBuilder.append("killRunningProcess--processName:");
                                    stringBuilder.append(runningProcessInfo.processName);
                                    stringBuilder.append(" uid:");
                                    stringBuilder.append(runningProcessInfo.uid);
                                    Log.d("OPScreenResolutionAdjust", stringBuilder.toString());
                                    this.mAm.killUid(runningProcessInfo.uid, "change screen resolution");
                                    String curInteractor = Secure.getStringForUser(this.mContext.getContentResolver(), "voice_interaction_service", -2);
                                    String curRecognizer = Secure.getStringForUser(this.mContext.getContentResolver(), "voice_recognition_service", -2);
                                    if (!TextUtils.isEmpty(curInteractor) || !TextUtils.isEmpty(curRecognizer)) {
                                        String curInteractorPkg = curInteractor.split("\\/")[0];
                                        if (TextUtils.isEmpty(curInteractor) || !(TextUtils.equals(runningProcessInfo.processName, curInteractorPkg) || runningProcessInfo.processName.contains(curInteractorPkg))) {
                                            String curRecognizerPkg = curRecognizer.split("\\/")[0];
                                            if (!TextUtils.isEmpty(curRecognizer) && (TextUtils.equals(runningProcessInfo.processName, curRecognizerPkg) || runningProcessInfo.processName.contains(curRecognizerPkg))) {
                                                StringBuilder stringBuilder2 = new StringBuilder();
                                                stringBuilder2.append("forceStopPackage-curRecognizer-PackageName:");
                                                stringBuilder2.append(curRecognizerPkg);
                                                Log.d("OPScreenResolutionAdjust", stringBuilder2.toString());
                                                this.mAm.forceStopPackage(curRecognizerPkg);
                                            }
                                        } else {
                                            StringBuilder stringBuilder3 = new StringBuilder();
                                            stringBuilder3.append("forceStopPackage-curInteractor-PackageName:");
                                            stringBuilder3.append(curInteractorPkg);
                                            Log.d("OPScreenResolutionAdjust", stringBuilder3.toString());
                                            this.mAm.forceStopPackage(curInteractorPkg);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private int getCurrent1080pDpiIndex() {
        int index = 0;
        String currentDpi = Secure.getStringForUser(this.mContext.getContentResolver(), "display_density_forced", -2);
        if (TextUtils.isEmpty(currentDpi)) {
            return 1;
        }
        for (int i = 0; i < this.DPI_VALUES_1080P.length; i++) {
            if (currentDpi.equals(String.valueOf(this.DPI_VALUES_1080P[i]))) {
                index = i;
                break;
            }
        }
        return index;
    }

    private int getCurrentOtherDpiIndex() {
        int index = 0;
        String currentDpi = Secure.getStringForUser(this.mContext.getContentResolver(), "display_density_forced", -2);
        if (TextUtils.isEmpty(currentDpi)) {
            return 1;
        }
        for (int i = 0; i < this.DPI_VALUES_OTHER.length; i++) {
            if (currentDpi.equals(String.valueOf(this.DPI_VALUES_OTHER[i]))) {
                index = i;
                break;
            }
        }
        return index;
    }

    private void delayRefreshUI() {
        this.mOtherResolutionMode.setEnabled(false);
        this.m1080PMode.setEnabled(false);
        this.mHandler.postDelayed(new Runnable() {
            public void run() {
                OPScreenResolutionAdjust.this.mOtherResolutionMode.setEnabled(true);
                OPScreenResolutionAdjust.this.m1080PMode.setEnabled(true);
            }
        }, 1000);
    }

    public void onDestroy() {
        super.onDestroy();
        int mExitValue = Global.getInt(this.mContext.getContentResolver(), ONEPLUS_SCREEN_RESOLUTION_ADJUST, 2);
        if (this.mEnterValue == mExitValue) {
            return;
        }
        if (mExitValue == 2) {
            OPUtils.sendAnalytics("resolution", NotificationCompat.CATEGORY_STATUS, "1");
        } else if (mExitValue == 0) {
            OPUtils.sendAnalytics("resolution", NotificationCompat.CATEGORY_STATUS, "2");
        } else if (mExitValue == 1) {
            OPUtils.sendAnalytics("resolution", NotificationCompat.CATEGORY_STATUS, "3");
        }
    }

    public int getMetricsCategory() {
        return OPUtils.ONEPLUS_METRICSLOGGER;
    }
}
