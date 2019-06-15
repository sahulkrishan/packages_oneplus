package com.oneplus.settings.better;

import android.content.Context;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.net.LocalSocketAddress.Namespace;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.util.Log;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.ui.RadioButtonPreference;
import com.android.settings.ui.RadioButtonPreference.OnClickListener;
import java.io.OutputStream;

public class OPScreenBetterSettings extends SettingsPreferenceFragment implements OnPreferenceClickListener, OnClickListener {
    private static final String KEY_COOL_SETTINGS = "cool_settings";
    private static final String KEY_NORMAL_SETTINGS = "nomal_settings";
    private static final String KEY_WARM_SETTINGS = "warm_settings";
    private static final String TAG = "OPScreenBetterSettings";
    private static final String TYPE_ONE = "oem:qdcm:mode_1";
    private static final String TYPE_SERVER = "pps";
    private static final String TYPE_THREE = "oem:qdcm:mode_3";
    private static final String TYPE_TWO = "oem:qdcm:mode_2";
    private String M_TYPE_STRING = TYPE_ONE;
    private int TYPE_SETTINGS_ID = 1;
    private LocalSocket localSocket;
    private int mBetterStatus = 1;
    private Context mContext;
    private RadioButtonPreference mCoolSettings;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    OPScreenBetterSettings.this.M_TYPE_STRING = OPScreenBetterSettings.TYPE_ONE;
                    break;
                case 2:
                    OPScreenBetterSettings.this.M_TYPE_STRING = OPScreenBetterSettings.TYPE_TWO;
                    break;
                case 3:
                    OPScreenBetterSettings.this.M_TYPE_STRING = OPScreenBetterSettings.TYPE_THREE;
                    break;
                default:
                    OPScreenBetterSettings.this.M_TYPE_STRING = OPScreenBetterSettings.TYPE_ONE;
                    break;
            }
            OPScreenBetterSettings.this.mBetterStatus = msg.what;
            try {
                if (System.getInt(OPScreenBetterSettings.this.mContext.getContentResolver(), "oem_eyecare_enable", 0) == 0) {
                    OPScreenBetterSettings.this.localSocket = new LocalSocket();
                    OPScreenBetterSettings.this.localSocket.connect(new LocalSocketAddress(OPScreenBetterSettings.TYPE_SERVER, Namespace.RESERVED));
                    OutputStream os = OPScreenBetterSettings.this.localSocket.getOutputStream();
                    int i = 0;
                    while (i < 3) {
                        os.write(OPScreenBetterSettings.this.M_TYPE_STRING.getBytes());
                        OPScreenBetterSettings.this.localSocket.setReceiveBufferSize(1024);
                        byte[] buffer = new byte[1024];
                        OPScreenBetterSettings.this.localSocket.getInputStream().read(buffer);
                        String result = new String(buffer);
                        String str;
                        StringBuilder stringBuilder;
                        if (result.contains("Success")) {
                            str = OPScreenBetterSettings.TAG;
                            stringBuilder = new StringBuilder();
                            stringBuilder.append("succ buffer : ");
                            stringBuilder.append(result.substring(0, 8));
                            Log.i(str, stringBuilder.toString());
                            os.close();
                        } else {
                            str = OPScreenBetterSettings.TAG;
                            stringBuilder = new StringBuilder();
                            stringBuilder.append("fail buffer : ");
                            stringBuilder.append(result.substring(0, 8));
                            stringBuilder.append(" i = ");
                            stringBuilder.append(i);
                            Log.i(str, stringBuilder.toString());
                            os.flush();
                            i++;
                        }
                    }
                    os.close();
                }
                System.putInt(OPScreenBetterSettings.this.mContext.getContentResolver(), "oem_better_status", OPScreenBetterSettings.this.mBetterStatus);
                String str2 = OPScreenBetterSettings.TAG;
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append("M_TYPE_STRING : ");
                stringBuilder2.append(OPScreenBetterSettings.this.M_TYPE_STRING);
                Log.i(str2, stringBuilder2.toString());
            } catch (Exception e) {
                e.printStackTrace();
                Log.i(OPScreenBetterSettings.TAG, "socket exception !");
            }
        }
    };
    private RadioButtonPreference mNormalSettings;
    private RadioButtonPreference mWarmSettings;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.op_screenbetter_settings);
        this.mContext = getActivity();
        this.mNormalSettings = (RadioButtonPreference) findPreference(KEY_NORMAL_SETTINGS);
        this.mWarmSettings = (RadioButtonPreference) findPreference(KEY_WARM_SETTINGS);
        this.mCoolSettings = (RadioButtonPreference) findPreference(KEY_COOL_SETTINGS);
        this.mNormalSettings.setOnClickListener(this);
        this.mWarmSettings.setOnClickListener(this);
        this.mCoolSettings.setOnClickListener(this);
    }

    public void onResume() {
        super.onResume();
        this.mBetterStatus = System.getInt(this.mContext.getContentResolver(), "oem_better_status", 1);
        updateRadioButtons(this.mBetterStatus);
        this.mHandler.sendEmptyMessage(this.mBetterStatus);
    }

    public boolean onPreferenceClick(Preference preference) {
        return true;
    }

    public void onRadioButtonClicked(RadioButtonPreference emiter) {
        if (emiter == this.mNormalSettings) {
            this.TYPE_SETTINGS_ID = 1;
        } else if (emiter == this.mWarmSettings) {
            this.TYPE_SETTINGS_ID = 2;
        } else if (emiter == this.mCoolSettings) {
            this.TYPE_SETTINGS_ID = 3;
        }
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("TYPE_SETTINGS_ID : ");
        stringBuilder.append(this.TYPE_SETTINGS_ID);
        Log.i(str, stringBuilder.toString());
        this.mHandler.sendEmptyMessage(this.TYPE_SETTINGS_ID);
        updateRadioButtons(emiter);
    }

    private void updateRadioButtons(RadioButtonPreference activated) {
        System.out.println("url : updateLocation mode : 2-");
        if (activated == null) {
            this.mNormalSettings.setChecked(false);
            this.mWarmSettings.setChecked(false);
            this.mCoolSettings.setChecked(false);
        } else if (activated == this.mNormalSettings) {
            this.mNormalSettings.setChecked(true);
            this.mWarmSettings.setChecked(false);
            this.mCoolSettings.setChecked(false);
        } else if (activated == this.mWarmSettings) {
            this.mNormalSettings.setChecked(false);
            this.mWarmSettings.setChecked(true);
            this.mCoolSettings.setChecked(false);
        } else if (activated == this.mCoolSettings) {
            this.mNormalSettings.setChecked(false);
            this.mWarmSettings.setChecked(false);
            this.mCoolSettings.setChecked(true);
        }
    }

    private void updateRadioButtons(int activated) {
        if (activated == 1) {
            this.mNormalSettings.setChecked(true);
            this.mWarmSettings.setChecked(false);
            this.mCoolSettings.setChecked(false);
        } else if (activated == 2) {
            this.mNormalSettings.setChecked(false);
            this.mWarmSettings.setChecked(true);
            this.mCoolSettings.setChecked(false);
        } else if (activated == 3) {
            this.mNormalSettings.setChecked(false);
            this.mWarmSettings.setChecked(false);
            this.mCoolSettings.setChecked(true);
        }
    }

    public int getMetricsCategory() {
        return 0;
    }
}
