package com.oneplus.settings.statusbar;

import android.app.ActivityManager;
import android.content.Context;
import android.provider.Settings.Secure;
import android.support.v14.preference.SwitchPreference;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import java.util.Set;

public class StatusBarSwitch extends SwitchPreference {
    private static final String TAG = "StatusBarSwitch";
    private Set<String> mBlacklist;
    private Utils mUtils;

    public StatusBarSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mUtils = new Utils(context);
    }

    public void onAttached() {
        super.onAttached();
        updateUI();
    }

    public void onDetached() {
        super.onDetached();
    }

    /* Access modifiers changed, original: protected */
    public boolean persistBoolean(boolean value) {
        updateList();
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("set key:");
        stringBuilder.append(getKey());
        stringBuilder.append(" value:");
        stringBuilder.append(value);
        Log.i(str, stringBuilder.toString());
        if (value) {
            if (this.mBlacklist.remove(getKey())) {
                setList(this.mBlacklist);
            }
        } else if (!this.mBlacklist.contains(getKey())) {
            this.mBlacklist.add(getKey());
            setList(this.mBlacklist);
        }
        return true;
    }

    private void setList(Set<String> blacklist) {
        Secure.putStringForUser(getContext().getContentResolver(), Utils.ICON_BLACKLIST, TextUtils.join(",", blacklist), ActivityManager.getCurrentUser());
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" setList blacklist:");
        stringBuilder.append(blacklist);
        Log.i(str, stringBuilder.toString());
    }

    private void updateUI() {
        updateList();
        setChecked(this.mBlacklist.contains(getKey()) ^ 1);
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" updateUI blacklist:");
        stringBuilder.append(this.mBlacklist);
        Log.i(str, stringBuilder.toString());
    }

    private void updateList() {
        this.mBlacklist = Utils.getIconBlacklist(this.mUtils.getValue(Utils.ICON_BLACKLIST));
    }
}
