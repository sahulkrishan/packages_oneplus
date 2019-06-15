package com.android.settings.datausage;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.support.v7.preference.Preference;
import android.util.ArraySet;
import com.android.settingslib.utils.AsyncLoader;

public class AppPrefLoader extends AsyncLoader<ArraySet<Preference>> {
    private PackageManager mPackageManager;
    private ArraySet<String> mPackages;
    private Context mPrefContext;

    public AppPrefLoader(Context prefContext, ArraySet<String> pkgs, PackageManager pm) {
        super(prefContext);
        this.mPackages = pkgs;
        this.mPackageManager = pm;
        this.mPrefContext = prefContext;
    }

    public ArraySet<Preference> loadInBackground() {
        ArraySet<Preference> results = new ArraySet();
        int size = this.mPackages.size();
        for (int i = 1; i < size; i++) {
            try {
                ApplicationInfo info = this.mPackageManager.getApplicationInfo((String) this.mPackages.valueAt(i), 0);
                Preference preference = new Preference(this.mPrefContext);
                preference.setIcon(info.loadIcon(this.mPackageManager));
                preference.setTitle(info.loadLabel(this.mPackageManager));
                preference.setSelectable(false);
                results.add(preference);
            } catch (NameNotFoundException e) {
            }
        }
        return results;
    }

    /* Access modifiers changed, original: protected */
    public void onDiscardResult(ArraySet<Preference> arraySet) {
    }
}
