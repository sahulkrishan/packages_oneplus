package com.oneplus.settings.ringtone;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import com.android.settings.R;
import com.oneplus.settings.ringtone.OPRingtoneManager.ResultRing;
import com.oneplus.settings.utils.OPNotificationUtils;
import java.util.ArrayList;
import java.util.List;

public class OPSystemRingtonePicker extends OPRingtoneBaseActivity implements OnPreferenceClickListener {
    private static final String DEFUALT_SELECT_KEY = "defualt_select";
    private static final String LOCAL_SELECT_KEY = "local_select";
    private static final String NO_SELECT_KEY = "no_select";
    private Cursor mCursor;
    private OPRadioButtonPreference mDefualtPreference;
    private OPRadioButtonPreference mLocalPreference;
    private OPRadioButtonPreference mNOPreference;
    private List<OPRadioButtonPreference> mSystemRings = null;
    private Uri mUriForLocalItem;

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.op_ring_system_fragment);
        this.mCursor = this.mRingtoneManager.getCursor();
        this.mLocalPreference = (OPRadioButtonPreference) findPreference(LOCAL_SELECT_KEY);
        this.mNOPreference = (OPRadioButtonPreference) findPreference(NO_SELECT_KEY);
        this.mDefualtPreference = (OPRadioButtonPreference) findPreference(DEFUALT_SELECT_KEY);
        getPreferenceScreen().removePreference(this.mDefualtPreference);
        this.mLocalPreference.setOnPreferenceClickListener(this);
        this.mNOPreference.setOnPreferenceClickListener(this);
        initPreference();
        updateSelected();
    }

    private void initPreference() {
        if (this.mSystemRings == null) {
            this.mSystemRings = new ArrayList();
            if (this.mCursor != null && this.mCursor.moveToFirst()) {
                do {
                    OPRadioButtonPreference p = new OPRadioButtonPreference(this);
                    p.setTitle(OPNotificationUtils.replaceWith(this, this.mCursor.getString(1), OPRingtoneManager.getSettingForType(this.mType)));
                    p.setKey(OPRingtoneManager.getUriFromCursor(this.mCursor).toString());
                    p.setOnPreferenceClickListener(this);
                    this.mSystemRings.add(p);
                    getPreferenceScreen().addPreference(p);
                    p.setChecked(false);
                } while (this.mCursor.moveToNext());
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public void updateSelected() {
        if (this.mUriForDefaultItem == null) {
            this.mLocalPreference.setChecked(false);
            this.mNOPreference.setChecked(true);
            updateChecks("-1");
            this.mLocalPreference.setSummary(R.string.oneplus_no_choice);
            this.mUriForLocalItem = null;
            return;
        }
        this.mNOPreference.setChecked(false);
        if (OPRingtoneManager.isSystemRingtone(getApplicationContext(), this.mUriForDefaultItem, this.mType)) {
            this.mLocalPreference.setChecked(false);
            updateChecks(this.mUriForDefaultItem.toString());
            this.mLocalPreference.setSummary(R.string.oneplus_no_choice);
            this.mUriForLocalItem = null;
            return;
        }
        CharSequence charSequence;
        this.mLocalPreference.setChecked(true);
        ResultRing summany = OPRingtoneManager.getLocatRingtoneTitle(getApplicationContext(), this.mUriForDefaultItem, this.mType, getSimId());
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
    }

    private void updateChecks(String key) {
        if (this.mSystemRings != null) {
            for (OPRadioButtonPreference p : this.mSystemRings) {
                p.setChecked(p.getKey().equals(key));
            }
        }
    }

    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        if (key.equals(LOCAL_SELECT_KEY)) {
            stopAnyPlayingRingtone();
            Intent intent = new Intent(this, OPLocalRingtonePickerActivity.class);
            intent.putExtra(OPRingtoneManager.EXTRA_RINGTONE_TYPE, this.mType);
            intent.putExtra(OPRingtoneManager.EXTRA_RINGTONE_SIMID, getSimId());
            intent.putExtra(OPRingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
            intent.putExtra(OPRingtoneManager.EXTRA_RINGTONE_EXISTING_URI, this.mUriForLocalItem);
            startActivity(intent);
        } else if (key.equals(NO_SELECT_KEY)) {
            stopAnyPlayingRingtone();
            updateChecks("-1");
            if (getSimId() == 2) {
                OPRingtoneManager.setActualRingtoneUriBySubId(getApplicationContext(), 1, null);
            } else {
                OPRingtoneManager.setActualRingtoneUriBySubId(getApplicationContext(), 0, null);
            }
            this.mLocalPreference.setChecked(false);
            this.mNOPreference.setChecked(true);
            this.mLocalPreference.setSummary(R.string.oneplus_no_choice);
            this.mUriForLocalItem = null;
            this.mUriForDefaultItem = null;
        } else {
            this.mUriForDefaultItem = Uri.parse(key);
            playRingtone(300, this.mUriForDefaultItem);
            if (getSimId() == 2) {
                OPRingtoneManager.setActualRingtoneUriBySubId(getApplicationContext(), 1, this.mUriForDefaultItem);
            } else {
                OPRingtoneManager.setActualRingtoneUriBySubId(getApplicationContext(), 0, this.mUriForDefaultItem);
            }
            updateChecks(key);
            this.mLocalPreference.setChecked(false);
            this.mNOPreference.setChecked(false);
            this.mLocalPreference.setSummary(R.string.oneplus_no_choice);
            this.mUriForLocalItem = null;
        }
        return true;
    }

    /* Access modifiers changed, original: protected */
    public void onDestroy() {
        if (this.mCursor != null) {
            this.mCursor.close();
            this.mCursor = null;
        }
        super.onDestroy();
    }
}
