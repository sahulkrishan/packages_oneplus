package com.android.settings;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.UserHandle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import com.android.internal.R;
import com.oneplus.settings.ringtone.OPRingtoneManager;
import java.io.PrintStream;

public class RingtonePreference extends Preference {
    private static final String TAG = "RingtonePreference";
    private int mRequestCode;
    private int mRingtoneType;
    private boolean mShowDefault;
    private boolean mShowSilent;
    protected Context mUserContext;
    protected int mUserId;

    public RingtonePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RingtonePreference, 0, 0);
        this.mRingtoneType = a.getInt(0, 1);
        this.mShowDefault = a.getBoolean(1, true);
        this.mShowSilent = a.getBoolean(2, true);
        setUserId(UserHandle.myUserId());
        a.recycle();
    }

    public void setUserId(int userId) {
        this.mUserId = userId;
        this.mUserContext = Utils.createPackageContextAsUser(getContext(), this.mUserId);
    }

    public int getUserId() {
        return this.mUserId;
    }

    public int getRingtoneType() {
        return this.mRingtoneType;
    }

    public void setRingtoneType(int type) {
        this.mRingtoneType = type;
    }

    public boolean getShowDefault() {
        return this.mShowDefault;
    }

    public void setShowDefault(boolean showDefault) {
        this.mShowDefault = showDefault;
    }

    public boolean getShowSilent() {
        return this.mShowSilent;
    }

    public void setShowSilent(boolean showSilent) {
        this.mShowSilent = showSilent;
    }

    public int getRequestCode() {
        return this.mRequestCode;
    }

    public void onPrepareRingtonePickerIntent(Intent ringtonePickerIntent) {
        if (ringtonePickerIntent != null) {
            ringtonePickerIntent.putExtra(OPRingtoneManager.EXTRA_RINGTONE_EXISTING_URI, onRestoreRingtone());
            ringtonePickerIntent.putExtra(OPRingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, this.mShowDefault);
            if (this.mShowDefault) {
                ringtonePickerIntent.putExtra(OPRingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, RingtoneManager.getDefaultUri(getRingtoneType()));
            }
            PrintStream printStream = System.out;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("zhuyang--mRingtoneType:");
            stringBuilder.append(this.mRingtoneType);
            printStream.println(stringBuilder.toString());
            ringtonePickerIntent.putExtra("android.intent.extra.ringtone.SHOW_SILENT", this.mShowSilent);
            ringtonePickerIntent.putExtra(OPRingtoneManager.EXTRA_RINGTONE_TYPE, this.mRingtoneType);
            ringtonePickerIntent.putExtra(OPRingtoneManager.EXTRA_RINGTONE_TITLE, getTitle());
            ringtonePickerIntent.putExtra("android.intent.extra.ringtone.AUDIO_ATTRIBUTES_FLAGS", 64);
        }
    }

    /* Access modifiers changed, original: protected */
    public void onSaveRingtone(Uri ringtoneUri) {
        persistString(ringtoneUri != null ? ringtoneUri.toString() : "");
    }

    /* Access modifiers changed, original: protected */
    public Uri onRestoreRingtone() {
        String uriString = getPersistedString(null);
        if (TextUtils.isEmpty(uriString)) {
            return null;
        }
        return Uri.parse(uriString);
    }

    /* Access modifiers changed, original: protected */
    public Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    /* Access modifiers changed, original: protected */
    public void onSetInitialValue(boolean restorePersistedValue, Object defaultValueObj) {
        String defaultValue = (String) defaultValueObj;
        if (!(restorePersistedValue || TextUtils.isEmpty(defaultValue))) {
            onSaveRingtone(Uri.parse(defaultValue));
        }
    }

    /* Access modifiers changed, original: protected */
    public void onAttachedToHierarchy(PreferenceManager preferenceManager) {
        super.onAttachedToHierarchy(preferenceManager);
    }

    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            Uri uri = (Uri) data.getParcelableExtra(OPRingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (callChangeListener(uri != null ? uri.toString() : "")) {
                onSaveRingtone(uri);
            }
        }
        return true;
    }
}
