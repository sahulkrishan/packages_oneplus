package com.android.settings.applications;

import android.content.pm.ApplicationInfo;
import android.content.pm.UserInfo;
import android.os.AsyncTask;
import android.os.UserHandle;
import android.os.UserManager;
import com.android.settingslib.wrapper.PackageManagerWrapper;
import java.util.ArrayList;
import java.util.List;

public abstract class AppLister extends AsyncTask<Void, Void, List<UserAppInfo>> {
    protected final PackageManagerWrapper mPm;
    protected final UserManager mUm;

    public abstract boolean includeInCount(ApplicationInfo applicationInfo);

    public abstract void onAppListBuilt(List<UserAppInfo> list);

    public AppLister(PackageManagerWrapper packageManager, UserManager userManager) {
        this.mPm = packageManager;
        this.mUm = userManager;
    }

    /* Access modifiers changed, original: protected|varargs */
    public List<UserAppInfo> doInBackground(Void... params) {
        List<UserAppInfo> result = new ArrayList();
        for (UserInfo user : this.mUm.getProfiles(UserHandle.myUserId())) {
            for (ApplicationInfo info : this.mPm.getInstalledApplicationsAsUser(33280 | (user.isAdmin() ? 4194304 : 0), user.id)) {
                if (includeInCount(info)) {
                    result.add(new UserAppInfo(user, info));
                }
            }
        }
        return result;
    }

    /* Access modifiers changed, original: protected */
    public void onPostExecute(List<UserAppInfo> list) {
        onAppListBuilt(list);
    }
}
