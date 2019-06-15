package com.oneplus.setting.lib;

import android.R;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.ArraySet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.Toolbar;
import com.android.settingslib.drawer.CategoryManager;
import java.util.ArrayList;
import java.util.List;

public class SettingsDrawerActivity extends Activity {
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    protected static final boolean DEBUG_TIMING = false;
    public static final String EXTRA_SHOW_MENU = "show_drawer_menu";
    private static final String TAG = "SettingsDrawerActivity";
    private static ArraySet<ComponentName> sTileBlacklist = new ArraySet();
    private final List<CategoryListener> mCategoryListeners = new ArrayList();
    private FrameLayout mContentHeaderContainer;
    private final PackageReceiver mPackageReceiver = new PackageReceiver();

    private class CategoriesUpdateTask extends AsyncTask<Void, Void, Void> {
        private final CategoryManager mCategoryManager;

        public CategoriesUpdateTask() {
            this.mCategoryManager = CategoryManager.get(SettingsDrawerActivity.this);
        }

        /* Access modifiers changed, original: protected|varargs */
        public Void doInBackground(Void... params) {
            this.mCategoryManager.reloadAllCategories(SettingsDrawerActivity.this, SettingsDrawerActivity.this.getSettingPkg());
            return null;
        }

        /* Access modifiers changed, original: protected */
        public void onPostExecute(Void result) {
            this.mCategoryManager.updateCategoryFromBlacklist(SettingsDrawerActivity.sTileBlacklist);
            SettingsDrawerActivity.this.onCategoriesChanged();
        }
    }

    public interface CategoryListener {
        void onCategoriesChanged();
    }

    private class PackageReceiver extends BroadcastReceiver {
        private PackageReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            new CategoriesUpdateTask().execute(new Void[0]);
        }
    }

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        long startTime = System.currentTimeMillis();
        TypedArray theme = getTheme().obtainStyledAttributes(R.styleable.Theme);
        if (!theme.getBoolean(38, false)) {
            getWindow().addFlags(Integer.MIN_VALUE);
            requestWindowFeature(1);
        }
        super.setContentView(com.android.settings.R.layout.op_settings_with_drawer);
        this.mContentHeaderContainer = (FrameLayout) findViewById(com.android.settings.R.id.content_header_container);
        Toolbar toolbar = (Toolbar) findViewById(com.android.settings.R.id.action_bar);
        if (theme.getBoolean(38, false)) {
            toolbar.setVisibility(8);
        } else {
            setActionBar(toolbar);
        }
    }

    public boolean onNavigateUp() {
        if (!super.onNavigateUp()) {
            finish();
        }
        return true;
    }

    /* Access modifiers changed, original: protected */
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter("android.intent.action.PACKAGE_ADDED");
        filter.addAction("android.intent.action.PACKAGE_REMOVED");
        filter.addAction("android.intent.action.PACKAGE_CHANGED");
        filter.addAction("android.intent.action.PACKAGE_REPLACED");
        filter.addDataScheme("package");
        registerReceiver(this.mPackageReceiver, filter);
        new CategoriesUpdateTask().execute(new Void[0]);
    }

    /* Access modifiers changed, original: protected */
    public void onPause() {
        unregisterReceiver(this.mPackageReceiver);
        super.onPause();
    }

    public void addCategoryListener(CategoryListener listener) {
        this.mCategoryListeners.add(listener);
    }

    public void remCategoryListener(CategoryListener listener) {
        this.mCategoryListeners.remove(listener);
    }

    public void setContentView(int layoutResID) {
        ViewGroup parent = (ViewGroup) findViewById(com.android.settings.R.id.content_frame);
        if (parent != null) {
            parent.removeAllViews();
        }
        LayoutInflater.from(this).inflate(layoutResID, parent);
    }

    public void setContentView(View view) {
        ((ViewGroup) findViewById(com.android.settings.R.id.content_frame)).addView(view);
    }

    public void setContentView(View view, LayoutParams params) {
        ((ViewGroup) findViewById(com.android.settings.R.id.content_frame)).addView(view, params);
    }

    private void onCategoriesChanged() {
        int N = this.mCategoryListeners.size();
        for (int i = 0; i < N; i++) {
            ((CategoryListener) this.mCategoryListeners.get(i)).onCategoriesChanged();
        }
    }

    public boolean setTileEnabled(ComponentName component, boolean enabled) {
        PackageManager pm = getPackageManager();
        int state = pm.getComponentEnabledSetting(component);
        if ((state == 1) == enabled && state != 0) {
            return false;
        }
        if (enabled) {
            sTileBlacklist.remove(component);
        } else {
            sTileBlacklist.add(component);
        }
        pm.setComponentEnabledSetting(component, enabled ? 1 : 2, 1);
        return true;
    }

    public void updateCategories() {
        new CategoriesUpdateTask().execute(new Void[0]);
    }

    public String getSettingPkg() {
        return "com.android.settings";
    }
}
