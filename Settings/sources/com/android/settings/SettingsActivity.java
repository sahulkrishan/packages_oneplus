package com.android.settings;

import android.app.ActionBar;
import android.app.ActivityManager.TaskDescription;
import android.app.Fragment;
import android.app.FragmentManager.BackStackEntry;
import android.app.FragmentManager.OnBackStackChangedListener;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.annotation.VisibleForTesting;
import android.support.v14.preference.PreferenceFragment;
import android.support.v14.preference.PreferenceFragment.OnPreferenceStartFragmentCallback;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceManager.OnPreferenceTreeClickListener;
import android.text.TextUtils;
import android.transition.TransitionManager;
import android.util.FeatureFlagUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import com.android.internal.util.ArrayUtils;
import com.android.settings.Settings.BluetoothSettingsActivity;
import com.android.settings.Settings.ConnectedDeviceDashboardActivity;
import com.android.settings.Settings.DataUsageSummaryActivity;
import com.android.settings.Settings.DataUsageSummaryLegacyActivity;
import com.android.settings.Settings.DateTimeSettingsActivity;
import com.android.settings.Settings.DevelopmentSettingsDashboardActivity;
import com.android.settings.Settings.NetworkDashboardActivity;
import com.android.settings.Settings.OPAboutPhoneActivity;
import com.android.settings.Settings.OPCloudServiceSettings;
import com.android.settings.Settings.PowerUsageSummaryActivity;
import com.android.settings.Settings.SMQQtiFeedbackActivity;
import com.android.settings.Settings.SimSettingsActivity;
import com.android.settings.Settings.UserSettingsActivity;
import com.android.settings.Settings.WifiDisplaySettingsActivity;
import com.android.settings.Settings.WifiSettingsActivity;
import com.android.settings.applications.manageapplications.ManageApplications;
import com.android.settings.backup.BackupSettingsActivity;
import com.android.settings.core.FeatureFlags;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.core.gateway.SettingsGateway;
import com.android.settings.dashboard.DashboardFeatureProvider;
import com.android.settings.dashboard.DashboardSummary;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.search.DeviceIndexFeatureProvider;
import com.android.settings.sim.SimSettings;
import com.android.settings.slices.SliceDeepLinkSpringBoard;
import com.android.settings.wfd.WifiDisplaySettings;
import com.android.settings.widget.SwitchBar;
import com.android.settingslib.core.instrumentation.Instrumentable;
import com.android.settingslib.core.instrumentation.SharedPreferencesLogger;
import com.android.settingslib.development.DevelopmentSettingsEnabler;
import com.android.settingslib.drawer.DashboardCategory;
import com.android.settingslib.drawer.Tile;
import com.android.settingslib.utils.ThreadUtils;
import com.oneplus.setting.lib.SettingsDrawerActivity;
import com.oneplus.settings.SettingsBaseApplication;
import com.oneplus.settings.utils.OPUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.codeaurora.internal.IExtTelephony;
import org.codeaurora.internal.IExtTelephony.Stub;

public class SettingsActivity extends SettingsDrawerActivity implements OnPreferenceTreeClickListener, OnPreferenceStartFragmentCallback, ButtonBarHandler, OnBackStackChangedListener {
    public static final String BACK_STACK_PREFS = ":settings:prefs";
    public static final String EXTRA_FRAGMENT_ARG_KEY = ":settings:fragment_args_key";
    @Deprecated
    public static final String EXTRA_HIDE_DRAWER = ":settings:hide_drawer";
    public static final String EXTRA_LAUNCH_ACTIVITY_ACTION = ":settings:launch_activity_action";
    protected static final String EXTRA_PREFS_SET_BACK_TEXT = "extra_prefs_set_back_text";
    protected static final String EXTRA_PREFS_SET_NEXT_TEXT = "extra_prefs_set_next_text";
    protected static final String EXTRA_PREFS_SHOW_BUTTON_BAR = "extra_prefs_show_button_bar";
    private static final String EXTRA_PREFS_SHOW_SKIP = "extra_prefs_show_skip";
    public static final String EXTRA_SHOW_FRAGMENT = ":settings:show_fragment";
    public static final String EXTRA_SHOW_FRAGMENT_ARGUMENTS = ":settings:show_fragment_args";
    public static final String EXTRA_SHOW_FRAGMENT_AS_SHORTCUT = ":settings:show_fragment_as_shortcut";
    public static final String EXTRA_SHOW_FRAGMENT_AS_SUBSETTING = ":settings:show_fragment_as_subsetting";
    public static final String EXTRA_SHOW_FRAGMENT_TITLE = ":settings:show_fragment_title";
    public static final String EXTRA_SHOW_FRAGMENT_TITLE_RESID = ":settings:show_fragment_title_resid";
    public static final String EXTRA_SHOW_FRAGMENT_TITLE_RES_PACKAGE_NAME = ":settings:show_fragment_title_res_package_name";
    private static final String EXTRA_UI_OPTIONS = "settings:ui_options";
    private static final String LOG_TAG = "SettingsActivity";
    public static final String META_DATA_KEY_FRAGMENT_CLASS = "com.android.settings.FRAGMENT_CLASS";
    public static final String META_DATA_KEY_LAUNCH_ACTIVITY_ACTION = "com.android.settings.ACTIVITY_ACTION";
    private static final String ONEPLUS_CLOUD_PACKAGE = "com.oneplus.cloud";
    private static final String SAVE_KEY_CATEGORIES = ":settings:categories";
    protected boolean hasActionBar = true;
    private String mActivityAction;
    private BroadcastReceiver mBatteryInfoReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.BATTERY_CHANGED".equals(intent.getAction())) {
                boolean batteryPresent = Utils.isBatteryPresent(intent);
                if (SettingsActivity.this.mBatteryPresent != batteryPresent) {
                    SettingsActivity.this.mBatteryPresent = batteryPresent;
                    SettingsActivity.this.updateTilesList();
                }
            }
        }
    };
    private boolean mBatteryPresent = true;
    private ArrayList<DashboardCategory> mCategories = new ArrayList();
    private ViewGroup mContent;
    private DashboardFeatureProvider mDashboardFeatureProvider;
    private BroadcastReceiver mDevelopmentSettingsListener;
    private String mFragmentClass;
    private CharSequence mInitialTitle;
    private int mInitialTitleResId;
    private boolean mIsShowingDashboard;
    private Button mNextButton;
    private SmqSettings mSMQ;
    private SwitchBar mSwitchBar;

    public SwitchBar getSwitchBar() {
        return this.mSwitchBar;
    }

    public boolean onPreferenceStartFragment(PreferenceFragment caller, Preference pref) {
        int metricsCategory;
        SubSettingLauncher arguments = new SubSettingLauncher(this).setDestination(pref.getFragment()).setArguments(pref.getExtras());
        if (caller instanceof Instrumentable) {
            metricsCategory = ((Instrumentable) caller).getMetricsCategory();
        } else {
            metricsCategory = 0;
        }
        arguments.setSourceMetricsCategory(metricsCategory).setTitle(-1).launch();
        return true;
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        return false;
    }

    public SharedPreferences getSharedPreferences(String name, int mode) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getPackageName());
        stringBuilder.append("_preferences");
        if (name.equals(stringBuilder.toString())) {
            return new SharedPreferencesLogger(this, getMetricsTag(), FeatureFactory.getFactory(this).getMetricsFeatureProvider());
        }
        return super.getSharedPreferences(name, mode);
    }

    private String getMetricsTag() {
        String tag = getClass().getName();
        if (getIntent() != null && getIntent().hasExtra(EXTRA_SHOW_FRAGMENT)) {
            tag = getIntent().getStringExtra(EXTRA_SHOW_FRAGMENT);
        }
        if (tag.startsWith("com.android.settings.")) {
            return tag.replace("com.android.settings.", "");
        }
        return tag;
    }

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedState) {
        Bundle bundle = savedState;
        super.onCreate(savedState);
        Log.d(LOG_TAG, "Starting onCreate");
        long startTime = System.currentTimeMillis();
        this.mDashboardFeatureProvider = FeatureFactory.getFactory(this).getDashboardFeatureProvider(this);
        OPUtils.setLightNavigationBar(getWindow(), OPUtils.getThemeMode(getContentResolver()));
        getMetaData();
        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_LAUNCH_ACTIVITY_ACTION)) {
            if (this.mActivityAction != null) {
                try {
                    startActivity(new Intent(this.mActivityAction));
                } catch (ActivityNotFoundException e) {
                    String str = LOG_TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Activity not found for action: ");
                    stringBuilder.append(this.mActivityAction);
                    Log.w(str, stringBuilder.toString());
                }
            }
            finish();
            return;
        }
        if (intent.hasExtra(EXTRA_UI_OPTIONS)) {
            getWindow().setUiOptions(intent.getIntExtra(EXTRA_UI_OPTIONS, 0));
        }
        this.mSMQ = new SmqSettings(getApplicationContext());
        String initialFragmentName = intent.getStringExtra(EXTRA_SHOW_FRAGMENT);
        this.mIsShowingDashboard = intent.getComponent().getClassName().equals(Settings.class.getName());
        boolean isSubSettings = (this instanceof SubSettings) || intent.getBooleanExtra(EXTRA_SHOW_FRAGMENT_AS_SUBSETTING, false);
        if (isSubSettings) {
            setTheme(R.style.f971Theme.SubSettings);
        }
        setContentView(this.mIsShowingDashboard ? R.layout.settings_main_dashboard : R.layout.settings_main_prefs);
        this.mContent = (ViewGroup) findViewById(R.id.main_content);
        getFragmentManager().addOnBackStackChangedListener(this);
        if (bundle != null) {
            setTitleFromIntent(intent);
            ArrayList<DashboardCategory> categories = bundle.getParcelableArrayList(SAVE_KEY_CATEGORIES);
            if (categories != null) {
                this.mCategories.clear();
                this.mCategories.addAll(categories);
                setTitleFromBackStack();
            }
        } else {
            launchSettingFragment(initialFragmentName, isSubSettings, intent);
        }
        View search_bar = findViewById(R.id.search_bar);
        if (search_bar != null) {
            search_bar.setVisibility(8);
        }
        ActionBar actionBar = getActionBar();
        if (!(actionBar == null || this.mIsShowingDashboard)) {
            boolean deviceProvisioned = Utils.isDeviceProvisioned(this);
            actionBar.setDisplayHomeAsUpEnabled(deviceProvisioned);
            actionBar.setHomeButtonEnabled(deviceProvisioned);
            actionBar.setDisplayShowTitleEnabled(1 ^ this.mIsShowingDashboard);
        }
        if (this.mIsShowingDashboard && actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.oneplus_main_home_bg_ab)));
        }
        this.mSwitchBar = (SwitchBar) findViewById(R.id.switch_bar);
        if (this.mSwitchBar != null) {
            this.mSwitchBar.setMetricsTag(getMetricsTag());
        }
        if (intent.getBooleanExtra(EXTRA_PREFS_SHOW_BUTTON_BAR, false)) {
            View buttonBar = findViewById(R.id.button_bar);
            if (buttonBar != null) {
                buttonBar.setVisibility(0);
                Button backButton = (Button) findViewById(R.id.back_button);
                backButton.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        SettingsActivity.this.setResult(0, null);
                        SettingsActivity.this.finish();
                    }
                });
                Button skipButton = (Button) findViewById(R.id.skip_button);
                skipButton.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        SettingsActivity.this.setResult(-1, null);
                        SettingsActivity.this.finish();
                    }
                });
                this.mNextButton = (Button) findViewById(R.id.next_button);
                this.mNextButton.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        SettingsActivity.this.setResult(-1, null);
                        SettingsActivity.this.finish();
                    }
                });
                if (intent.hasExtra(EXTRA_PREFS_SET_NEXT_TEXT) != null) {
                    initialFragmentName = intent.getStringExtra(EXTRA_PREFS_SET_NEXT_TEXT);
                    if (TextUtils.isEmpty(initialFragmentName)) {
                        this.mNextButton.setVisibility(8);
                    } else {
                        this.mNextButton.setText(initialFragmentName);
                    }
                }
                if (intent.hasExtra(EXTRA_PREFS_SET_BACK_TEXT) != null) {
                    initialFragmentName = intent.getStringExtra(EXTRA_PREFS_SET_BACK_TEXT);
                    if (TextUtils.isEmpty(initialFragmentName)) {
                        backButton.setVisibility(8);
                    } else {
                        backButton.setText(initialFragmentName);
                    }
                }
                if (intent.getBooleanExtra(EXTRA_PREFS_SHOW_SKIP, false) != null) {
                    skipButton.setVisibility(0);
                }
            }
        }
    }

    private float dip2px(Context context, float dpValue) {
        return (dpValue * context.getResources().getDisplayMetrics().density) + 0.5f;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void launchSettingFragment(String initialFragmentName, boolean isSubSettings, Intent intent) {
        if (this.mIsShowingDashboard || initialFragmentName == null) {
            this.mInitialTitleResId = R.string.dashboard_title;
            switchToFragment(DashboardSummary.class.getName(), null, false, false, this.mInitialTitleResId, this.mInitialTitle, false);
            return;
        }
        setTitleFromIntent(intent);
        switchToFragment(initialFragmentName, intent.getBundleExtra(EXTRA_SHOW_FRAGMENT_ARGUMENTS), true, false, this.mInitialTitleResId, this.mInitialTitle, false);
    }

    private void setTitleFromIntent(Intent intent) {
        Log.d(LOG_TAG, "Starting to set activity title");
        int initialTitleResId = intent.getIntExtra(EXTRA_SHOW_FRAGMENT_TITLE_RESID, -1);
        if (initialTitleResId > 0) {
            this.mInitialTitle = null;
            this.mInitialTitleResId = initialTitleResId;
            String initialTitleResPackageName = intent.getStringExtra(EXTRA_SHOW_FRAGMENT_TITLE_RES_PACKAGE_NAME);
            if (initialTitleResPackageName != null) {
                try {
                    this.mInitialTitle = createPackageContextAsUser(initialTitleResPackageName, null, new UserHandle(UserHandle.myUserId())).getResources().getText(this.mInitialTitleResId);
                    setTitle(this.mInitialTitle);
                    this.mInitialTitleResId = -1;
                    return;
                } catch (NameNotFoundException e) {
                    String str = LOG_TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Could not find package");
                    stringBuilder.append(initialTitleResPackageName);
                    Log.w(str, stringBuilder.toString());
                }
            } else {
                setTitle(this.mInitialTitleResId);
            }
        } else {
            this.mInitialTitleResId = -1;
            String initialTitle = intent.getStringExtra(EXTRA_SHOW_FRAGMENT_TITLE);
            this.mInitialTitle = initialTitle != null ? initialTitle : getTitle();
            setTitle(this.mInitialTitle);
        }
        Log.d(LOG_TAG, "Done setting title");
    }

    public void onBackStackChanged() {
        setTitleFromBackStack();
    }

    private void setTitleFromBackStack() {
        int count = getFragmentManager().getBackStackEntryCount();
        if (count == 0) {
            if (this.mInitialTitleResId > 0) {
                setTitle(this.mInitialTitleResId);
            } else {
                setTitle(this.mInitialTitle);
            }
            return;
        }
        setTitleFromBackStackEntry(getFragmentManager().getBackStackEntryAt(count - 1));
    }

    private void setTitleFromBackStackEntry(BackStackEntry bse) {
        CharSequence title;
        int titleRes = bse.getBreadCrumbTitleRes();
        if (titleRes > 0) {
            title = getText(titleRes);
        } else {
            title = bse.getBreadCrumbTitle();
        }
        if (title != null) {
            setTitle(title);
        }
    }

    /* Access modifiers changed, original: protected */
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState(outState);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void saveState(Bundle outState) {
        if (this.mCategories.size() > 0) {
            outState.putParcelableArrayList(SAVE_KEY_CATEGORIES, this.mCategories);
        }
    }

    /* Access modifiers changed, original: protected */
    public void onResume() {
        super.onResume();
        this.mDevelopmentSettingsListener = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                SettingsActivity.this.updateTilesList();
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(this.mDevelopmentSettingsListener, new IntentFilter(DevelopmentSettingsEnabler.DEVELOPMENT_SETTINGS_CHANGED_ACTION));
        registerReceiver(this.mBatteryInfoReceiver, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
        updateTilesList();
        updateDeviceIndex();
    }

    /* Access modifiers changed, original: protected */
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(this.mDevelopmentSettingsListener);
        this.mDevelopmentSettingsListener = null;
        unregisterReceiver(this.mBatteryInfoReceiver);
    }

    public void setTaskDescription(TaskDescription taskDescription) {
        taskDescription.setIcon(getBitmapFromXmlResource(R.mipmap.op_ic_launcher_settings));
        super.setTaskDescription(taskDescription);
    }

    /* Access modifiers changed, original: protected */
    public boolean isValidFragment(String fragmentName) {
        for (String equals : SettingsGateway.ENTRY_FRAGMENTS) {
            if (equals.equals(fragmentName)) {
                return true;
            }
        }
        return false;
    }

    public Intent getIntent() {
        Intent superIntent = super.getIntent();
        String startingFragment = getStartingFragmentClass(superIntent);
        Intent modIntent;
        if (startingFragment != null) {
            modIntent = new Intent(superIntent);
            modIntent.putExtra(EXTRA_SHOW_FRAGMENT, startingFragment);
            Bundle args = superIntent.getBundleExtra(EXTRA_SHOW_FRAGMENT_ARGUMENTS);
            if (args != null) {
                args = new Bundle(args);
            } else {
                args = new Bundle();
            }
            args.putParcelable(SliceDeepLinkSpringBoard.INTENT, superIntent);
            modIntent.putExtra(EXTRA_SHOW_FRAGMENT_ARGUMENTS, args);
            return modIntent;
        } else if (this.mActivityAction == null) {
            return superIntent;
        } else {
            modIntent = new Intent(superIntent);
            modIntent.putExtra(EXTRA_LAUNCH_ACTIVITY_ACTION, this.mActivityAction);
            return modIntent;
        }
    }

    private String getStartingFragmentClass(Intent intent) {
        if (this.mFragmentClass != null) {
            return this.mFragmentClass;
        }
        String intentClass = intent.getComponent().getClassName();
        if (intentClass.equals(getClass().getName())) {
            return null;
        }
        if ("com.android.settings.RunningServices".equals(intentClass) || "com.android.settings.applications.StorageUse".equals(intentClass)) {
            intentClass = ManageApplications.class.getName();
        }
        return intentClass;
    }

    public void finishPreferencePanel(int resultCode, Intent resultData) {
        setResult(resultCode, resultData);
        finish();
    }

    private Fragment switchToFragment(String fragmentName, Bundle args, boolean validate, boolean addToBackStack, int titleResId, CharSequence title, boolean withTransition) {
        if (fragmentName.equals(getString(R.string.qtifeedback_intent_action))) {
            Intent newIntent = new Intent(getString(R.string.qtifeedback_intent_action));
            newIntent.addCategory("android.intent.category.DEFAULT");
            startActivity(newIntent);
            finish();
            return null;
        }
        String str = LOG_TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Switching to fragment ");
        stringBuilder.append(fragmentName);
        Log.d(str, stringBuilder.toString());
        IExtTelephony extTelephony = Stub.asInterface(ServiceManager.getService("extphone"));
        try {
            if (fragmentName.equals(SimSettings.class.getName()) && extTelephony != null && extTelephony.isVendorApkAvailable("com.qualcomm.qti.simsettings")) {
                Log.i(LOG_TAG, "switchToFragment, launch simSettings  ");
                Intent provisioningIntent = new Intent("com.android.settings.sim.SIM_SUB_INFO_SETTINGS");
                if (!getPackageManager().queryIntentActivities(provisioningIntent, 0).isEmpty()) {
                    startActivity(provisioningIntent);
                }
                finish();
                return null;
            }
        } catch (RemoteException e) {
            Log.i(LOG_TAG, "couldn't connect to extphone service, launch the default sim cards activity");
        }
        if (!validate || isValidFragment(fragmentName)) {
            Fragment f = Fragment.instantiate(this, fragmentName, args);
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.main_content, f);
            if (withTransition) {
                TransitionManager.beginDelayedTransition(this.mContent);
            }
            if (addToBackStack) {
                transaction.addToBackStack(BACK_STACK_PREFS);
            }
            if (titleResId > 0) {
                transaction.setBreadCrumbTitle(titleResId);
            } else if (title != null) {
                transaction.setBreadCrumbTitle(title);
            }
            transaction.commitAllowingStateLoss();
            getFragmentManager().executePendingTransactions();
            Log.d(LOG_TAG, "Executed frag manager pendingTransactions");
            return f;
        }
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append("Invalid fragment for this activity: ");
        stringBuilder2.append(fragmentName);
        throw new IllegalArgumentException(stringBuilder2.toString());
    }

    public void startPreferencePanel(String fragmentClass, Bundle args, int titleRes, CharSequence titleText, Fragment resultTo, int resultRequestCode) {
        String title = null;
        if (titleRes < 0) {
            if (titleText != null) {
                title = titleText.toString();
            } else {
                title = "";
            }
        }
        Utils.startWithFragment((Context) this, fragmentClass, args, resultTo, resultRequestCode, titleRes, (CharSequence) title, false);
    }

    private void updateTilesList() {
        AsyncTask.execute(new Runnable() {
            public void run() {
                SettingsActivity.this.doUpdateTilesList();
            }
        });
    }

    private void updateDeviceIndex() {
        ThreadUtils.postOnBackgroundThread(new -$$Lambda$SettingsActivity$HXMcoLHGNmdxTucTgqvnp3fY_K8(this, FeatureFactory.getFactory(this).getDeviceIndexFeatureProvider()));
    }

    public static /* synthetic */ void lambda$updateDeviceIndex$0(SettingsActivity settingsActivity, DeviceIndexFeatureProvider indexProvider) {
        Log.d(LOG_TAG, "postOnBackgroundThread update deviceIndex start");
        indexProvider.updateIndex(settingsActivity, false);
        Log.d(LOG_TAG, "postOnBackgroundThread update deviceIndex end");
    }

    private boolean isExistAPP(String packagename) {
        PackageInfo info;
        boolean isExist = false;
        try {
            info = SettingsBaseApplication.mApplication.getPackageManager().getPackageInfo(packagename, 0);
        } catch (NameNotFoundException e1) {
            info = null;
            e1.printStackTrace();
        }
        if (info != null) {
            return true;
        }
        return isExist;
    }

    private void doUpdateTilesList() {
        Throwable th;
        PackageManager pm = getPackageManager();
        UserManager um = UserManager.get(this);
        boolean isAdmin = um.isAdminUser();
        FeatureFactory featureFactory = FeatureFactory.getFactory(this);
        String packageName = getPackageName();
        StringBuilder changedList = new StringBuilder();
        boolean z = setTileEnabled(changedList, new ComponentName(packageName, WifiSettingsActivity.class.getName()), pm.hasSystemFeature("android.hardware.wifi"), isAdmin) || false;
        z = setTileEnabled(changedList, new ComponentName(packageName, BluetoothSettingsActivity.class.getName()), pm.hasSystemFeature("android.hardware.bluetooth"), isAdmin) || z;
        boolean somethingChanged = z;
        if (this.mSMQ.isShowSmqSettings()) {
            z = setTileEnabled(changedList, new ComponentName(packageName, SMQQtiFeedbackActivity.class.getName()), this.mSMQ.isShowSmqSettings(), isAdmin) || somethingChanged;
            somethingChanged = z;
        }
        z = setTileEnabled(changedList, new ComponentName(packageName, DataUsageSummaryActivity.class.getName()), Utils.isBandwidthControlEnabled(), isAdmin) || somethingChanged;
        z = setTileEnabled(changedList, new ComponentName(packageName, ConnectedDeviceDashboardActivity.class.getName()), UserManager.isDeviceInDemoMode(this) ^ 1, isAdmin) || z;
        z = setTileEnabled(changedList, new ComponentName(packageName, SimSettingsActivity.class.getName()), Utils.showSimCardTile(this), isAdmin) || z;
        z = setTileEnabled(changedList, new ComponentName(packageName, PowerUsageSummaryActivity.class.getName()), this.mBatteryPresent, isAdmin) || z;
        somethingChanged = z;
        ComponentName componentName = new ComponentName(packageName, OPCloudServiceSettings.class.getName());
        boolean z2 = isExistAPP("com.oneplus.cloud") && !OPUtils.isGuestMode();
        z = setTileEnabled(changedList, componentName, z2, isAdmin) || somethingChanged;
        somethingChanged = z;
        z = FeatureFlagUtils.isEnabled(this, FeatureFlags.DATA_USAGE_SETTINGS_V2);
        ComponentName componentName2 = new ComponentName(packageName, DataUsageSummaryActivity.class.getName());
        boolean z3 = Utils.isBandwidthControlEnabled() && z;
        z2 = setTileEnabled(changedList, componentName2, z3, isAdmin) || somethingChanged;
        somethingChanged = z2;
        componentName2 = new ComponentName(packageName, DataUsageSummaryLegacyActivity.class.getName());
        z3 = Utils.isBandwidthControlEnabled() && !z;
        z2 = setTileEnabled(changedList, componentName2, z3, isAdmin) || somethingChanged;
        somethingChanged = z2;
        componentName2 = new ComponentName(packageName, UserSettingsActivity.class.getName());
        z3 = UserManager.supportsMultipleUsers() && !Utils.isMonkeyRunning();
        z2 = setTileEnabled(changedList, componentName2, z3, isAdmin) || somethingChanged;
        z2 = setTileEnabled(changedList, new ComponentName(packageName, NetworkDashboardActivity.class.getName()), UserManager.isDeviceInDemoMode(this) ^ 1, isAdmin) || z2;
        z2 = setTileEnabled(changedList, new ComponentName(packageName, DateTimeSettingsActivity.class.getName()), UserManager.isDeviceInDemoMode(this) ^ 1, isAdmin) || z2;
        somethingChanged = z2;
        z2 = DevelopmentSettingsEnabler.isDevelopmentSettingsEnabled(this) && !Utils.isMonkeyRunning();
        z3 = um.isAdminUser() || um.isDemoUser();
        boolean z4 = setTileEnabled(changedList, new ComponentName(packageName, DevelopmentSettingsDashboardActivity.class.getName()), z2, z3) || somethingChanged;
        z4 = setTileEnabled(changedList, new ComponentName(packageName, BackupSettingsActivity.class.getName()), true, isAdmin) || z4;
        z4 = setTileEnabled(changedList, new ComponentName(packageName, WifiDisplaySettingsActivity.class.getName()), WifiDisplaySettings.isAvailable(this), isAdmin) || z4;
        boolean somethingChanged2 = setTileEnabled(changedList, new ComponentName(packageName, OPAboutPhoneActivity.class.getName()), featureFactory.getAccountFeatureProvider().isAboutPhoneV2Enabled(this), isAdmin) || z4;
        UserManager userManager;
        if (isAdmin) {
            userManager = um;
        } else {
            List<DashboardCategory> categories = this.mDashboardFeatureProvider.getAllCategories();
            synchronized (categories) {
                PackageManager pm2;
                try {
                    Iterator it = categories.iterator();
                    while (it.hasNext()) {
                        DashboardCategory category = (DashboardCategory) it.next();
                        int tileCount = category.getTilesCount();
                        Iterator it2 = it;
                        it = category.getTiles().iterator();
                        while (it.hasNext()) {
                            Iterator it3 = it;
                            pm2 = pm;
                            Tile tile = (Tile) it.next();
                            try {
                                pm = tile.intent.getComponent();
                                userManager = um;
                                String name = pm.getClassName();
                                if (!ArrayUtils.contains(SettingsGateway.SETTINGS_FOR_RESTRICTED, name)) {
                                    if (!z3 || !DevelopmentSettingsDashboardActivity.class.getName().equals(name)) {
                                        somethingChanged = false;
                                        if (!packageName.equals(pm.getPackageName()) && !isEnabledForRestricted) {
                                            boolean z5;
                                            if (!setTileEnabled(changedList, pm, false, isAdmin)) {
                                                if (!somethingChanged2) {
                                                    z5 = false;
                                                    somethingChanged2 = z5;
                                                }
                                            }
                                            z5 = true;
                                            somethingChanged2 = z5;
                                        }
                                        it = it3;
                                        pm = pm2;
                                        um = userManager;
                                    }
                                }
                                somethingChanged = true;
                                if (!packageName.equals(pm.getPackageName())) {
                                }
                                it = it3;
                                pm = pm2;
                                um = userManager;
                            } catch (Throwable th2) {
                                th = th2;
                                throw th;
                            }
                        }
                        it = it2;
                        um = um;
                    }
                    userManager = um;
                } catch (Throwable th3) {
                    th = th3;
                    pm2 = pm;
                    userManager = um;
                    throw th;
                }
            }
        }
        if (somethingChanged2) {
            String str = LOG_TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Enabled state changed for some tiles, reloading all categories ");
            stringBuilder.append(changedList.toString());
            Log.d(str, stringBuilder.toString());
            updateCategories();
            return;
        }
        Log.d(LOG_TAG, "No enabled state changed, skipping updateCategory call");
    }

    private boolean setTileEnabled(StringBuilder changedList, ComponentName component, boolean enabled, boolean isAdmin) {
        if (!(isAdmin || !getPackageName().equals(component.getPackageName()) || ArrayUtils.contains(SettingsGateway.SETTINGS_FOR_RESTRICTED, component.getClassName()))) {
            enabled = false;
        }
        boolean changed = setTileEnabled(component, enabled);
        if (changed) {
            changedList.append(component.toShortString());
            changedList.append(",");
        }
        return changed;
    }

    private void getMetaData() {
        try {
            ActivityInfo ai = getPackageManager().getActivityInfo(getComponentName(), 128);
            if (ai != null) {
                if (ai.metaData != null) {
                    this.mFragmentClass = ai.metaData.getString(META_DATA_KEY_FRAGMENT_CLASS);
                    this.mActivityAction = ai.metaData.getString(META_DATA_KEY_LAUNCH_ACTIVITY_ACTION);
                }
            }
        } catch (NameNotFoundException e) {
            String str = LOG_TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Cannot get Metadata for: ");
            stringBuilder.append(getComponentName().toString());
            Log.d(str, stringBuilder.toString());
        }
    }

    public boolean hasNextButton() {
        return this.mNextButton != null;
    }

    public Button getNextButton() {
        return this.mNextButton;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public Bitmap getBitmapFromXmlResource(int drawableRes) {
        Drawable drawable = getResources().getDrawable(drawableRes, getTheme());
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}
