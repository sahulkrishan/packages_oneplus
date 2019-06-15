package com.oneplus.settings.ringtone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.SwitchPreference;
import com.android.settings.R;
import com.oneplus.settings.ringtone.OPRingtoneManager.ResultRing;
import com.oneplus.settings.utils.OPNotificationUtils;
import java.util.ArrayList;
import java.util.List;

public class OPRingtonePickerActivity extends OPRingtoneBaseActivity implements OnPreferenceClickListener {
    private static final String DEFUALT_SELECT_KEY = "defualt_select";
    private static final String LOCAL_SELECT_KEY = "local_select";
    private static final String MAIN_KEY = "setting_title";
    private static final String NO_SELECT_KEY = "no_select";
    private static final String SIM1_SELECT_KEY = "sim1_select";
    private static final String SIM2_SELECT_KEY = "sim2_select";
    private static final String SWITCH_KEY = "setting_key";
    private Cursor mCursor;
    private OPRadioButtonPreference mDefualtPreference;
    private OPRadioButtonPreference mLocalPreference;
    private PreferenceCategory mMainRoot;
    private OPRadioButtonPreference mNOPreference;
    private int mRequestCode = 100;
    private Preference mSim1Layout;
    private Uri mSim1Uri;
    private Preference mSim2Layout;
    private Uri mSim2Uri;
    private final BroadcastReceiver mSimStateReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.SIM_STATE_CHANGED".equals(intent.getAction()) && OPRingtonePickerActivity.this.mType == 1 && OPRingtonePickerActivity.this.isMultiSimEnabled()) {
                if (OPRingtonePickerActivity.this.mSim1Layout != null) {
                    OPRingtonePickerActivity.this.mSim1Layout.setEnabled(OPRingtonePickerActivity.this.getSim1Enable());
                }
                if (OPRingtonePickerActivity.this.mSim2Layout != null) {
                    OPRingtonePickerActivity.this.mSim2Layout.setEnabled(OPRingtonePickerActivity.this.getSim2Enable());
                }
            }
        }
    };
    private SwitchPreference mSwitch;
    private List<OPRadioButtonPreference> mSystemRings = null;
    private Uri mUriForLocalItem;

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updatePreference();
    }

    private void updatePreference() {
        this.mCursor = this.mRingtoneManager.getCursor();
        if (this.mType == 1 && isMultiSimEnabled()) {
            addPreferencesFromResource(R.xml.op_ring_switch_fragment);
            this.mMainRoot = (PreferenceCategory) findPreference(MAIN_KEY);
            this.mSwitch = (SwitchPreference) findPreference(SWITCH_KEY);
            this.mSim1Layout = findPreference(SIM1_SELECT_KEY);
            this.mSim2Layout = findPreference(SIM2_SELECT_KEY);
            this.mLocalPreference = (OPRadioButtonPreference) findPreference(LOCAL_SELECT_KEY);
            this.mNOPreference = (OPRadioButtonPreference) findPreference(NO_SELECT_KEY);
            this.mSwitch.setOnPreferenceClickListener(this);
            this.mSim1Layout.setOnPreferenceClickListener(this);
            this.mSim2Layout.setOnPreferenceClickListener(this);
            this.mSwitch.setChecked(OPRingtoneManager.isRingSimSwitchOn(getApplicationContext()));
            if (this.mContactsRingtone || isProfileId()) {
                getPreferenceScreen().removePreference(this.mSwitch);
            }
            switchSimRingtone(false);
        } else {
            addPreferencesFromResource(R.xml.op_ring_system_fragment);
            this.mLocalPreference = (OPRadioButtonPreference) findPreference(LOCAL_SELECT_KEY);
            this.mNOPreference = (OPRadioButtonPreference) findPreference(NO_SELECT_KEY);
            this.mDefualtPreference = (OPRadioButtonPreference) findPreference(DEFUALT_SELECT_KEY);
            this.mDefualtPreference.setOnPreferenceClickListener(this);
            if (!this.mHasDefaultItem) {
                getPreferenceScreen().removePreference(this.mDefualtPreference);
            }
            initPreference(true);
        }
        this.mLocalPreference.setOnPreferenceClickListener(this);
        this.mNOPreference.setOnPreferenceClickListener(this);
        updateSelected();
    }

    private void switchSimRingtone(boolean is) {
        if (!this.mSwitch.isChecked() || this.mContactsRingtone) {
            this.mMainRoot.removePreference(this.mSim1Layout);
            this.mMainRoot.removePreference(this.mSim2Layout);
            initPreference(true);
            return;
        }
        this.mMainRoot.removePreference(this.mLocalPreference);
        this.mMainRoot.removePreference(this.mNOPreference);
    }

    private void initPreference(boolean is) {
        int i = 0;
        if (this.mSystemRings == null) {
            this.mSystemRings = new ArrayList();
            if (this.mCursor != null && this.mCursor.moveToFirst()) {
                do {
                    OPRadioButtonPreference p = new OPRadioButtonPreference(this);
                    p.setTitle(OPNotificationUtils.replaceWith(this, this.mCursor.getString(1), OPRingtoneManager.getSettingForType(this.mType)));
                    Uri uri = OPRingtoneManager.getUriFromCursor(this.mCursor);
                    p.setKey(uri.toString());
                    p.setOnPreferenceClickListener(this);
                    this.mSystemRings.add(p);
                    getPreferenceScreen().addPreference(p);
                    if (this.mUriForDefaultItem == null || !this.mUriForDefaultItem.equals(uri)) {
                        p.setChecked(false);
                    } else {
                        p.setChecked(true);
                    }
                } while (this.mCursor.moveToNext());
                return;
            }
            return;
        }
        while (true) {
            int i2 = i;
            if (i2 >= this.mSystemRings.size()) {
                break;
            }
            if (is) {
                getPreferenceScreen().addPreference((Preference) this.mSystemRings.get(i2));
            } else {
                getPreferenceScreen().removePreference((Preference) this.mSystemRings.get(i2));
            }
            i = i2 + 1;
        }
        if (is) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    OPRingtonePickerActivity.this.updateSelected();
                }
            });
        }
    }

    /* Access modifiers changed, original: protected */
    public void updateSelected() {
        if (this.mType == 1) {
            updateSimSwitch();
        }
        if (this.mUriForDefaultItem == null) {
            this.mLocalPreference.setChecked(false);
            this.mNOPreference.setChecked(true);
            updateChecks("-1");
            this.mLocalPreference.setSummary(R.string.oneplus_no_choice);
            this.mUriForLocalItem = null;
            if (this.mHasDefaultItem && this.mDefualtPreference != null) {
                this.mDefualtPreference.setChecked(false);
            }
        } else {
            this.mNOPreference.setChecked(false);
            if (OPRingtoneManager.isDefault(this.mUriForDefaultItem) && this.mHasDefaultItem) {
                if (this.mDefualtPreference != null) {
                    this.mDefualtPreference.setChecked(true);
                }
                this.mLocalPreference.setChecked(false);
                this.mLocalPreference.setSummary(R.string.oneplus_no_choice);
                updateChecks("-1");
                this.mUriForLocalItem = null;
                return;
            }
            if (OPRingtoneManager.isDefault(this.mUriForDefaultItem)) {
                this.mUriForDefaultItem = OPRingtoneManager.ringtoneRestoreFromDefault(getApplicationContext(), this.mType, this.mUriForDefaultItem);
            }
            if (OPRingtoneManager.isSystemRingtone(getApplicationContext(), this.mUriForDefaultItem, this.mType)) {
                this.mLocalPreference.setChecked(false);
                updateChecks(this.mUriForDefaultItem.toString());
                this.mLocalPreference.setSummary(R.string.oneplus_no_choice);
                this.mUriForLocalItem = null;
                if (this.mHasDefaultItem && this.mDefualtPreference != null) {
                    this.mDefualtPreference.setChecked(false);
                }
            } else {
                CharSequence charSequence;
                this.mLocalPreference.setChecked(true);
                if (this.mHasDefaultItem && this.mDefualtPreference != null) {
                    this.mDefualtPreference.setChecked(false);
                }
                ResultRing summany = OPRingtoneManager.getLocatRingtoneTitle(getApplicationContext(), this.mUriForDefaultItem, this.mType, 0);
                OPRadioButtonPreference oPRadioButtonPreference = this.mLocalPreference;
                if (summany.title != null) {
                    charSequence = summany.title;
                } else {
                    charSequence = getString(R.string.oneplus_no_choice);
                }
                oPRadioButtonPreference.setSummary(charSequence);
                Uri uri = summany.ringUri;
                this.mUriForDefaultItem = uri;
                this.mUriForLocalItem = uri;
                updateChecks("-1");
                if (this.mType == 2) {
                    getPreferenceScreen().addPreference(this.mLocalPreference);
                }
            }
        }
    }

    private void updateSimSwitch() {
        if (isMultiSimEnabled()) {
            Preference preference;
            if (this.mSim1Layout != null) {
                this.mSim1Layout.setEnabled(getSim1Enable());
            }
            if (this.mSim2Layout != null) {
                this.mSim2Layout.setEnabled(getSim2Enable());
            }
            this.mSim1Uri = OPRingtoneManager.getActualRingtoneUriBySubId(getApplicationContext(), 0);
            this.mSim2Uri = OPRingtoneManager.getActualRingtoneUriBySubId(getApplicationContext(), 1);
            ResultRing summany1 = OPRingtoneManager.getLocatRingtoneTitle(getApplicationContext(), this.mSim1Uri, this.mType, 1);
            ResultRing summany2 = OPRingtoneManager.getLocatRingtoneTitle(getApplicationContext(), this.mSim2Uri, this.mType, 2);
            this.mSim1Uri = summany1.ringUri;
            this.mSim2Uri = summany2.ringUri;
            String type = OPRingtoneManager.getSettingForType(this.mType);
            if (this.mSim1Layout != null) {
                CharSequence replaceWith;
                preference = this.mSim1Layout;
                if (summany1.title != null) {
                    replaceWith = OPNotificationUtils.replaceWith(this, summany1.title, type);
                } else {
                    replaceWith = getString(R.string.oneplus_no_ringtone);
                }
                preference.setSummary(replaceWith);
            }
            if (this.mSim2Layout != null) {
                CharSequence replaceWith2;
                preference = this.mSim2Layout;
                if (summany2.title != null) {
                    replaceWith2 = OPNotificationUtils.replaceWith(this, summany2.title, type);
                } else {
                    replaceWith2 = getString(R.string.oneplus_no_ringtone);
                }
                preference.setSummary(replaceWith2);
            }
        }
    }

    public boolean onPreferenceClick(Preference preference) {
        this.isSelectedNone = false;
        String key = preference.getKey();
        Intent intent;
        if (key.equals(SWITCH_KEY)) {
            stopAnyPlayingRingtone();
            OPRingtoneManager.setRingSimSwitch(getApplicationContext(), this.mSwitch.isChecked());
            if (this.mSwitch.isChecked()) {
                OPRingtoneManager.updateActualRingtone2(getApplicationContext());
                this.mMainRoot.removePreference(this.mLocalPreference);
                this.mMainRoot.removePreference(this.mNOPreference);
                this.mMainRoot.addPreference(this.mSim1Layout);
                this.mMainRoot.addPreference(this.mSim2Layout);
                initPreference(false);
            } else {
                OPRingtoneManager.updateActualRingtone(getApplicationContext());
                this.mMainRoot.addPreference(this.mLocalPreference);
                this.mMainRoot.addPreference(this.mNOPreference);
                this.mMainRoot.removePreference(this.mSim1Layout);
                this.mMainRoot.removePreference(this.mSim2Layout);
                initPreference(true);
            }
        } else if (key.equals(LOCAL_SELECT_KEY)) {
            stopAnyPlayingRingtone();
            intent = new Intent(this, OPLocalRingtonePickerActivity.class);
            intent.putExtra(OPRingtoneManager.EXTRA_RINGTONE_TYPE, this.mType);
            intent.putExtra(OPRingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, this.mHasDefaultItem);
            intent.putExtra(OPRingtoneManager.EXTRA_RINGTONE_EXISTING_URI, this.mUriForLocalItem);
            intent.putExtra(OPRingtoneManager.EXTRA_RINGTONE_FOR_CONTACTS, this.mContactsRingtone);
            startActivityForResult(intent, this.mRequestCode);
        } else if (key.equals(NO_SELECT_KEY)) {
            stopAnyPlayingRingtone();
            updateChecks("-1");
            if (!isThreePart()) {
                OPRingtoneManager.setActualDefaultRingtoneUri(getApplicationContext(), this.mType, null);
            }
            if (this.mHasDefaultItem && this.mDefualtPreference != null) {
                this.mDefualtPreference.setChecked(false);
            }
            this.mLocalPreference.setChecked(false);
            this.mNOPreference.setChecked(true);
            this.isSelectedNone = true;
            this.mLocalPreference.setSummary(R.string.oneplus_no_choice);
            this.mUriForLocalItem = null;
            this.mUriForDefaultItem = null;
        } else if (key.equals(SIM1_SELECT_KEY)) {
            intent = new Intent(this, OPSystemRingtonePicker.class);
            intent.putExtra(OPRingtoneManager.EXTRA_RINGTONE_SIMID, 1);
            intent.putExtra(OPRingtoneManager.EXTRA_RINGTONE_EXISTING_URI, this.mSim1Uri);
            intent.putExtra(OPRingtoneManager.EXTRA_RINGTONE_TYPE, this.mType);
            startActivity(intent);
        } else if (key.equals(SIM2_SELECT_KEY)) {
            intent = new Intent(this, OPSystemRingtonePicker.class);
            intent.putExtra(OPRingtoneManager.EXTRA_RINGTONE_SIMID, 2);
            intent.putExtra(OPRingtoneManager.EXTRA_RINGTONE_EXISTING_URI, this.mSim2Uri);
            intent.putExtra(OPRingtoneManager.EXTRA_RINGTONE_TYPE, this.mType);
            startActivity(intent);
        } else if (key.equals(DEFUALT_SELECT_KEY)) {
            this.mUriForDefaultItem = this.mDefualtUri;
            playRingtone(300, this.mUriForDefaultItem);
            updateChecks("-1");
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("mUriForDefaultItem:");
            stringBuilder.append(this.mUriForDefaultItem);
            stringBuilder.append(" key:");
            stringBuilder.append(key);
            OPMyLog.d("", stringBuilder.toString());
            this.mLocalPreference.setChecked(false);
            this.mNOPreference.setChecked(false);
            if (this.mDefualtPreference != null) {
                this.mDefualtPreference.setChecked(true);
            }
            this.mLocalPreference.setSummary(R.string.oneplus_no_choice);
            this.mUriForLocalItem = null;
        } else {
            this.mUriForDefaultItem = Uri.parse(key);
            playRingtone(300, this.mUriForDefaultItem);
            if (!isThreePart()) {
                OPRingtoneManager.setActualDefaultRingtoneUri(getUserContext(), this.mType, this.mUriForDefaultItem);
            }
            updateChecks(key);
            if (this.mHasDefaultItem && this.mDefualtPreference != null) {
                this.mDefualtPreference.setChecked(false);
            }
            this.mLocalPreference.setChecked(false);
            this.mNOPreference.setChecked(false);
            this.mLocalPreference.setSummary(R.string.oneplus_no_choice);
            this.mUriForLocalItem = null;
        }
        return true;
    }

    private void updateChecks(String key) {
        if (this.mSystemRings != null) {
            for (OPRadioButtonPreference p : this.mSystemRings) {
                p.setChecked(p.getKey().equals(key));
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public void onStart() {
        super.onStart();
        if (this.mType == 1 && isMultiSimEnabled()) {
            registerReceiver(this.mSimStateReceiver, new IntentFilter("android.intent.action.SIM_STATE_CHANGED"));
        }
    }

    /* Access modifiers changed, original: protected */
    public void onStop() {
        if (this.mType == 1 && isMultiSimEnabled()) {
            unregisterReceiver(this.mSimStateReceiver);
        }
        super.onStop();
    }

    /* Access modifiers changed, original: protected */
    public void onDestroy() {
        if (this.mCursor != null) {
            this.mCursor.close();
            this.mCursor = null;
        }
        super.onDestroy();
    }

    /* Access modifiers changed, original: protected */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == this.mRequestCode && data != null) {
            Uri uri = (Uri) data.getParcelableExtra(OPRingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (uri != null && !uri.equals(this.mUriForDefaultItem)) {
                this.mUriForDefaultItem = uri;
                updateSelected();
            }
        }
    }
}
