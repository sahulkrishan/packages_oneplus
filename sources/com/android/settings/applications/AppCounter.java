package com.android.settings.applications;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.UserInfo;
import android.os.AsyncTask;
import android.os.UserHandle;
import android.os.UserManager;
import com.android.settingslib.wrapper.PackageManagerWrapper;

public abstract class AppCounter extends AsyncTask<Void, Void, Integer> {
    protected final PackageManagerWrapper mPm;
    protected final UserManager mUm;

    public abstract boolean includeInCount(ApplicationInfo applicationInfo);

    public abstract void onCountComplete(int i);

    public AppCounter(Context context, PackageManagerWrapper packageManager) {
        this.mPm = packageManager;
        this.mUm = (UserManager) context.getSystemService("user");
    }

    /* Access modifiers changed, original: protected|varargs */
    public Integer doInBackground(Void... params) {
        int count = 0;
        for (UserInfo user : this.mUm.getProfiles(UserHandle.myUserId())) {
            for (ApplicationInfo info : this.mPm.getInstalledApplicationsAsUser(33280 | (user.isAdmin() ? 4194304 : 0), user.id)) {
                if (includeInCount(info)) {
                    count++;
                }
            }
        }
        return Integer.valueOf(count);
    }

    /* Access modifiers changed, original: protected */
    public void onPostExecute(Integer count) {
        onCountComplete(count.intValue());
    }

    /* Access modifiers changed, original: 0000 */
    public void executeInForeground() {
        onPostExecute(doInBackground(new Void[0]));
    }
}
