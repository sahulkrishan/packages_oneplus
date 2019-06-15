package com.oneplus.settings.notification;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemProperties;
import android.provider.SearchIndexableResource;
import android.provider.Settings.System;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.text.Layout;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.oneplus.settings.utils.OPUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OPEarphoneMode extends SettingsPreferenceFragment implements OnPreferenceChangeListener, OnPreferenceClickListener, Indexable {
    private static final String BLUETOOTH_DISABLE_ABSOLUTE_VOLUME_PROPERTY = "persist.bluetooth.disableabsvol";
    private static final String INTENT_TTS_CALL = "oneplus.intent.action.TTS_CALL";
    private static final String KEY_AUTO_ANSWER_VIA_BLUETOOTH = "auto_answer_via_bluetooth";
    private static final String KEY_AUTO_PLAY = "auto_play";
    private static final String KEY_BLUETOOTH_VOLUME_SWITCH = "bluetooth_volume_switch";
    private static final String KEY_CALL_INFORMATION_BROADCAST = "call_information_broadcast";
    private static final String KEY_GOOGLE_TTS = "google_tts";
    private static final String KEY_NOTIFICATION_RINGTONE = "notification_ringtone";
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = R.xml.op_earphone_mode;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }

        public List<String> getNonIndexableKeys(Context context) {
            List<String> result = new ArrayList();
            if (!OPUtils.isO2()) {
                result.add(OPEarphoneMode.KEY_NOTIFICATION_RINGTONE);
                result.add(OPEarphoneMode.KEY_GOOGLE_TTS);
            }
            return result;
        }
    };
    private SwitchPreference mAutoAnswerViaBluetooth;
    private SwitchPreference mAutoPlay;
    private SwitchPreference mBluetoothVolume;
    private SwitchPreference mCallInformationBroadcast;
    private Context mContext;
    private Preference mGoogleTTS;
    private ListPreference mNotificationRingtone;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.op_earphone_mode);
        this.mContext = getActivity();
        Activity activity = getActivity();
        boolean z = true;
        if (activity != null && activity.getIntent().getBooleanExtra("earmode_from_notify", false)) {
            OPUtils.sendAppTracker("ear.entrance", 0);
        }
        this.mAutoPlay = (SwitchPreference) findPreference(KEY_AUTO_PLAY);
        this.mAutoPlay.setOnPreferenceChangeListener(this);
        this.mAutoPlay.setChecked(System.getIntForUser(getContentResolver(), "oem_auto_play", 0, -2) != 0);
        this.mAutoAnswerViaBluetooth = (SwitchPreference) findPreference(KEY_AUTO_ANSWER_VIA_BLUETOOTH);
        this.mAutoAnswerViaBluetooth.setOnPreferenceChangeListener(this);
        this.mAutoAnswerViaBluetooth.setChecked(System.getIntForUser(getContentResolver(), KEY_AUTO_ANSWER_VIA_BLUETOOTH, 0, -2) != 0);
        this.mCallInformationBroadcast = (SwitchPreference) findPreference(KEY_CALL_INFORMATION_BROADCAST);
        this.mCallInformationBroadcast.setOnPreferenceChangeListener(this);
        SwitchPreference switchPreference = this.mCallInformationBroadcast;
        if (System.getIntForUser(getContentResolver(), "oem_call_information_broadcast", 0, -2) == 0) {
            z = false;
        }
        switchPreference.setChecked(z);
        if (this.mCallInformationBroadcast != null && OPUtils.isGuestMode()) {
            removePreference(KEY_CALL_INFORMATION_BROADCAST);
        }
        this.mNotificationRingtone = (ListPreference) findPreference(KEY_NOTIFICATION_RINGTONE);
        this.mNotificationRingtone.setOnPreferenceChangeListener(this);
        if (!(OPUtils.isO2() || this.mNotificationRingtone == null)) {
            removePreference(KEY_NOTIFICATION_RINGTONE);
        }
        this.mBluetoothVolume = (SwitchPreference) findPreference(KEY_BLUETOOTH_VOLUME_SWITCH);
        this.mBluetoothVolume.setOnPreferenceChangeListener(this);
        this.mGoogleTTS = findPreference(KEY_GOOGLE_TTS);
        this.mGoogleTTS.setOnPreferenceClickListener(this);
        if (!OPUtils.isO2() || OPUtils.isGuestMode()) {
            removePreference(KEY_GOOGLE_TTS);
        }
    }

    public void onResume() {
        super.onResume();
        boolean z = false;
        updateNotificationRingtoneSummary(System.getIntForUser(getContentResolver(), "oem_notification_ringtone", 0, -2));
        this.mBluetoothVolume.setChecked(SystemProperties.getBoolean(BLUETOOTH_DISABLE_ABSOLUTE_VOLUME_PROPERTY, false) ^ 1);
        if (this.mGoogleTTS != null) {
            if (System.getIntForUser(getContentResolver(), "oem_call_information_broadcast", 0, -2) == 1) {
                z = true;
            }
            this.mGoogleTTS.setEnabled(z);
        }
    }

    private void updateNotificationRingtoneSummary(int value) {
        if (this.mNotificationRingtone != null) {
            this.mNotificationRingtone.setValue(String.valueOf(value));
            this.mNotificationRingtone.setSummary(this.mContext.getResources().getStringArray(2130903178)[value]);
        }
    }

    private void confirmCallInformationBroadcast() {
        OnClickListener onClickListener = new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == -1) {
                    OPEarphoneMode.this.sendTTSCallIntent(true);
                    System.putIntForUser(OPEarphoneMode.this.getContentResolver(), "oem_call_information_broadcast", 1, -2);
                } else if (which == -2) {
                    OPEarphoneMode.this.mCallInformationBroadcast.setChecked(false);
                }
            }
        };
        final AlertDialog dialog = new Builder(this.mContext).setTitle(R.string.oneplus_network_permission_alerts).setMessage(R.string.oneplus_network_permission_alerts_message).setPositiveButton(17039370, onClickListener).setNegativeButton(17039360, onClickListener).setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                if (OPEarphoneMode.this.getActivity() != null) {
                    SwitchPreference access$200 = OPEarphoneMode.this.mCallInformationBroadcast;
                    boolean z = false;
                    if (System.getIntForUser(OPEarphoneMode.this.getContentResolver(), "oem_call_information_broadcast", 0, -2) != 0) {
                        z = true;
                    }
                    access$200.setChecked(z);
                }
            }
        }).create();
        dialog.show();
        TextView textview = (TextView) dialog.findViewById(16908299);
        textview.setMovementMethod(LinkMovementMethod.getInstance());
        textview.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                TextView tv = (TextView) v;
                CharSequence text = tv.getText();
                if ((text instanceof SpannableString) && action == 1) {
                    int x = (((int) event.getX()) - tv.getTotalPaddingLeft()) + tv.getScrollX();
                    int y = (((int) event.getY()) - tv.getTotalPaddingTop()) + tv.getScrollY();
                    Layout layout = tv.getLayout();
                    int off = layout.getOffsetForHorizontal(layout.getLineForVertical(y), (float) x);
                    if (((ClickableSpan[]) ((SpannableString) text).getSpans(off, off, ClickableSpan.class)).length != 0) {
                        OPEarphoneMode.this.doClickLink();
                        dialog.cancel();
                    }
                }
                return true;
            }
        });
    }

    private void doClickLink() {
        ActivityInfo info = getBrowserApp(this.mContext);
        if (info != null) {
            String pkgName = info.packageName;
            String browserClassName = info.name;
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            intent.setData(Uri.parse(this.mContext.getResources().getString(R.string.oneplus_network_permission_alerts_html)));
            intent.setClassName(pkgName, browserClassName);
            intent.addFlags(268435456);
            this.mContext.startActivity(intent);
        }
    }

    private ActivityInfo getBrowserApp(Context context) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addCategory("android.intent.category.BROWSABLE");
        intent.setDataAndType(Uri.parse("http://"), null);
        List<ResolveInfo> resolveInfoList = context.getPackageManager().queryIntentActivities(intent, 32);
        if (resolveInfoList.size() > 0) {
            return ((ResolveInfo) resolveInfoList.get(0)).activityInfo;
        }
        return null;
    }

    public boolean onPreferenceClick(Preference preference) {
        if (KEY_GOOGLE_TTS.equals(preference.getKey())) {
            try {
                Intent intent = new Intent();
                intent.setAction("android.speech.tts.engine.INSTALL_TTS_DATA");
                intent.setPackage("com.google.android.tts");
                this.mContext.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    private void sendTTSCallIntent(boolean value) {
        try {
            Intent intent = new Intent(INTENT_TTS_CALL);
            intent.putExtra("tts_call_value", value);
            intent.addFlags(285212672);
            getPrefContext().sendBroadcast(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        int enable;
        if (KEY_AUTO_PLAY.equals(preference.getKey())) {
            enable = ((Boolean) newValue).booleanValue();
            System.putIntForUser(getContentResolver(), "oem_auto_play", enable, -2);
            OPUtils.sendAppTracker("ear.Autoplay", enable);
        } else if (KEY_CALL_INFORMATION_BROADCAST.equals(preference.getKey())) {
            enable = ((Boolean) newValue).booleanValue();
            if (OPUtils.isO2()) {
                System.putIntForUser(getContentResolver(), "oem_call_information_broadcast", enable, -2);
                if (this.mGoogleTTS != null) {
                    this.mGoogleTTS.setEnabled(enable);
                }
            } else if (enable == true) {
                confirmCallInformationBroadcast();
            } else {
                sendTTSCallIntent(false);
                System.putIntForUser(getContentResolver(), "oem_call_information_broadcast", 0, -2);
            }
            OPUtils.sendAppTracker("ear.TTS", enable);
        } else if (KEY_NOTIFICATION_RINGTONE.equals(preference.getKey())) {
            enable = Integer.parseInt((String) newValue);
            this.mNotificationRingtone.setDialogTitle((CharSequence) this.mContext.getResources().getString(R.string.oneplus_notification_dialog_ringtone));
            System.putIntForUser(getContentResolver(), "oem_notification_ringtone", enable, -2);
            updateNotificationRingtoneSummary(enable);
            OPUtils.sendAppTracker("ear.remind", enable);
        } else if (KEY_BLUETOOTH_VOLUME_SWITCH.equals(preference.getKey())) {
            enable = ((Boolean) newValue).booleanValue();
            SystemProperties.set(BLUETOOTH_DISABLE_ABSOLUTE_VOLUME_PROPERTY, enable != false ? "false" : "true");
            OPUtils.sendAppTracker("ear.vol", enable);
        } else if (KEY_AUTO_ANSWER_VIA_BLUETOOTH.equals(preference.getKey())) {
            System.putIntForUser(getContentResolver(), KEY_AUTO_ANSWER_VIA_BLUETOOTH, ((Boolean) newValue).booleanValue(), -2);
        }
        return true;
    }

    public int getMetricsCategory() {
        return OPUtils.ONEPLUS_METRICSLOGGER;
    }
}
