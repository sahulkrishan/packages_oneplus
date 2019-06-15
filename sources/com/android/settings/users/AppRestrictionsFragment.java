package com.android.settings.users;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.RestrictionEntry;
import android.content.RestrictionsManager;
import android.content.pm.IPackageManager;
import android.content.pm.IPackageManager.Stub;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.v14.preference.MultiSelectListPreference;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settingslib.users.AppRestrictionsHelper;
import com.android.settingslib.users.AppRestrictionsHelper.OnDisableUiForPackageListener;
import com.android.settingslib.users.AppRestrictionsHelper.SelectableAppInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

public class AppRestrictionsFragment extends SettingsPreferenceFragment implements OnPreferenceChangeListener, OnClickListener, OnPreferenceClickListener, OnDisableUiForPackageListener {
    private static final int CUSTOM_REQUEST_CODE_START = 1000;
    private static final boolean DEBUG = false;
    private static final String DELIMITER = ";";
    public static final String EXTRA_NEW_USER = "new_user";
    public static final String EXTRA_USER_ID = "user_id";
    private static final int MAX_APP_RESTRICTIONS = 100;
    private static final String PKG_PREFIX = "pkg_";
    private static final String TAG = AppRestrictionsFragment.class.getSimpleName();
    private PreferenceGroup mAppList;
    private boolean mAppListChanged;
    private AsyncTask mAppLoadingTask;
    private int mCustomRequestCode = 1000;
    private HashMap<Integer, AppRestrictionsPreference> mCustomRequestMap = new HashMap();
    private boolean mFirstTime = true;
    private AppRestrictionsHelper mHelper;
    protected IPackageManager mIPm;
    private boolean mNewUser;
    protected PackageManager mPackageManager;
    private BroadcastReceiver mPackageObserver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            AppRestrictionsFragment.this.onPackageChanged(intent);
        }
    };
    protected boolean mRestrictedProfile;
    private PackageInfo mSysPackageInfo;
    protected UserHandle mUser;
    private BroadcastReceiver mUserBackgrounding = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (AppRestrictionsFragment.this.mAppListChanged) {
                AppRestrictionsFragment.this.mHelper.applyUserAppsStates(AppRestrictionsFragment.this);
            }
        }
    };
    protected UserManager mUserManager;

    private class AppLoadingTask extends AsyncTask<Void, Void, Void> {
        private AppLoadingTask() {
        }

        /* synthetic */ AppLoadingTask(AppRestrictionsFragment x0, AnonymousClass1 x1) {
            this();
        }

        /* Access modifiers changed, original: protected|varargs */
        public Void doInBackground(Void... params) {
            AppRestrictionsFragment.this.mHelper.fetchAndMergeApps();
            return null;
        }

        /* Access modifiers changed, original: protected */
        public void onPostExecute(Void result) {
            AppRestrictionsFragment.this.populateApps();
        }
    }

    class RestrictionsResultReceiver extends BroadcastReceiver {
        private static final String CUSTOM_RESTRICTIONS_INTENT = "android.intent.extra.restrictions_intent";
        boolean invokeIfCustom;
        String packageName;
        AppRestrictionsPreference preference;

        RestrictionsResultReceiver(String packageName, AppRestrictionsPreference preference, boolean invokeIfCustom) {
            this.packageName = packageName;
            this.preference = preference;
            this.invokeIfCustom = invokeIfCustom;
        }

        public void onReceive(Context context, Intent intent) {
            Bundle results = getResultExtras(true);
            if (results != null) {
                ArrayList<RestrictionEntry> restrictions = results.getParcelableArrayList("android.intent.extra.restrictions_list");
                Intent restrictionsIntent = (Intent) results.getParcelable(CUSTOM_RESTRICTIONS_INTENT);
                if (restrictions != null && restrictionsIntent == null) {
                    AppRestrictionsFragment.this.onRestrictionsReceived(this.preference, restrictions);
                    if (AppRestrictionsFragment.this.mRestrictedProfile) {
                        AppRestrictionsFragment.this.mUserManager.setApplicationRestrictions(this.packageName, RestrictionsManager.convertRestrictionsToBundle(restrictions), AppRestrictionsFragment.this.mUser);
                    }
                } else if (restrictionsIntent != null) {
                    this.preference.setRestrictions(restrictions);
                    if (this.invokeIfCustom && AppRestrictionsFragment.this.isResumed()) {
                        assertSafeToStartCustomActivity(restrictionsIntent);
                        AppRestrictionsFragment.this.startActivityForResult(restrictionsIntent, AppRestrictionsFragment.this.generateCustomActivityRequestCode(this.preference));
                    }
                }
            }
        }

        private void assertSafeToStartCustomActivity(Intent intent) {
            if (intent.getPackage() == null || !intent.getPackage().equals(this.packageName)) {
                List<ResolveInfo> resolveInfos = AppRestrictionsFragment.this.mPackageManager.queryIntentActivities(intent, 0);
                if (resolveInfos.size() == 1) {
                    if (!this.packageName.equals(((ResolveInfo) resolveInfos.get(0)).activityInfo.packageName)) {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("Application ");
                        stringBuilder.append(this.packageName);
                        stringBuilder.append(" is not allowed to start activity ");
                        stringBuilder.append(intent);
                        throw new SecurityException(stringBuilder.toString());
                    }
                }
            }
        }
    }

    static class AppRestrictionsPreference extends SwitchPreference {
        private boolean hasSettings;
        private boolean immutable;
        private OnClickListener listener;
        private List<Preference> mChildren = new ArrayList();
        private boolean panelOpen;
        private ArrayList<RestrictionEntry> restrictions;

        AppRestrictionsPreference(Context context, OnClickListener listener) {
            super(context);
            setLayoutResource(R.layout.preference_app_restrictions);
            this.listener = listener;
        }

        private void setSettingsEnabled(boolean enable) {
            this.hasSettings = enable;
        }

        /* Access modifiers changed, original: 0000 */
        public void setRestrictions(ArrayList<RestrictionEntry> restrictions) {
            this.restrictions = restrictions;
        }

        /* Access modifiers changed, original: 0000 */
        public void setImmutable(boolean immutable) {
            this.immutable = immutable;
        }

        /* Access modifiers changed, original: 0000 */
        public boolean isImmutable() {
            return this.immutable;
        }

        /* Access modifiers changed, original: 0000 */
        public ArrayList<RestrictionEntry> getRestrictions() {
            return this.restrictions;
        }

        /* Access modifiers changed, original: 0000 */
        public boolean isPanelOpen() {
            return this.panelOpen;
        }

        /* Access modifiers changed, original: 0000 */
        public void setPanelOpen(boolean open) {
            this.panelOpen = open;
        }

        /* Access modifiers changed, original: 0000 */
        public List<Preference> getChildren() {
            return this.mChildren;
        }

        public void onBindViewHolder(PreferenceViewHolder view) {
            super.onBindViewHolder(view);
            View appRestrictionsSettings = view.findViewById(R.id.app_restrictions_settings);
            int i = 8;
            appRestrictionsSettings.setVisibility(this.hasSettings ? 0 : 8);
            View findViewById = view.findViewById(R.id.settings_divider);
            if (this.hasSettings) {
                i = 0;
            }
            findViewById.setVisibility(i);
            appRestrictionsSettings.setOnClickListener(this.listener);
            appRestrictionsSettings.setTag(this);
            findViewById = view.findViewById(R.id.app_restrictions_pref);
            findViewById.setOnClickListener(this.listener);
            findViewById.setTag(this);
            ViewGroup widget = (ViewGroup) view.findViewById(16908312);
            widget.setEnabled(isImmutable() ^ 1);
            if (widget.getChildCount() > 0) {
                final Switch toggle = (Switch) widget.getChildAt(0);
                toggle.setEnabled(isImmutable() ^ 1);
                toggle.setTag(this);
                toggle.setClickable(true);
                toggle.setFocusable(true);
                toggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        AppRestrictionsPreference.this.listener.onClick(toggle);
                    }
                });
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public void init(Bundle icicle) {
        if (icicle != null) {
            this.mUser = new UserHandle(icicle.getInt("user_id"));
        } else {
            Bundle args = getArguments();
            if (args != null) {
                if (args.containsKey("user_id")) {
                    this.mUser = new UserHandle(args.getInt("user_id"));
                }
                this.mNewUser = args.getBoolean(EXTRA_NEW_USER, false);
            }
        }
        if (this.mUser == null) {
            this.mUser = Process.myUserHandle();
        }
        this.mHelper = new AppRestrictionsHelper(getContext(), this.mUser);
        this.mPackageManager = getActivity().getPackageManager();
        this.mIPm = Stub.asInterface(ServiceManager.getService("package"));
        this.mUserManager = (UserManager) getActivity().getSystemService("user");
        this.mRestrictedProfile = this.mUserManager.getUserInfo(this.mUser.getIdentifier()).isRestricted();
        try {
            this.mSysPackageInfo = this.mPackageManager.getPackageInfo("android", 64);
        } catch (NameNotFoundException e) {
        }
        addPreferencesFromResource(R.xml.app_restrictions);
        this.mAppList = getAppPreferenceGroup();
        this.mAppList.setOrderingAsAdded(false);
    }

    public int getMetricsCategory() {
        return 97;
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("user_id", this.mUser.getIdentifier());
    }

    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(this.mUserBackgrounding, new IntentFilter("android.intent.action.USER_BACKGROUND"));
        IntentFilter packageFilter = new IntentFilter();
        packageFilter.addAction("android.intent.action.PACKAGE_ADDED");
        packageFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        packageFilter.addDataScheme("package");
        getActivity().registerReceiver(this.mPackageObserver, packageFilter);
        this.mAppListChanged = false;
        if (this.mAppLoadingTask == null || this.mAppLoadingTask.getStatus() == Status.FINISHED) {
            this.mAppLoadingTask = new AppLoadingTask(this, null).execute(new Void[0]);
        }
    }

    public void onPause() {
        super.onPause();
        this.mNewUser = false;
        getActivity().unregisterReceiver(this.mUserBackgrounding);
        getActivity().unregisterReceiver(this.mPackageObserver);
        if (this.mAppListChanged) {
            new AsyncTask<Void, Void, Void>() {
                /* Access modifiers changed, original: protected|varargs */
                public Void doInBackground(Void... params) {
                    AppRestrictionsFragment.this.mHelper.applyUserAppsStates(AppRestrictionsFragment.this);
                    return null;
                }
            }.execute(new Void[0]);
        }
    }

    private void onPackageChanged(Intent intent) {
        String action = intent.getAction();
        AppRestrictionsPreference pref = (AppRestrictionsPreference) findPreference(getKeyForPackage(intent.getData().getSchemeSpecificPart()));
        if (pref != null) {
            if (("android.intent.action.PACKAGE_ADDED".equals(action) && pref.isChecked()) || ("android.intent.action.PACKAGE_REMOVED".equals(action) && !pref.isChecked())) {
                pref.setEnabled(true);
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public PreferenceGroup getAppPreferenceGroup() {
        return getPreferenceScreen();
    }

    public void onDisableUiForPackage(String packageName) {
        AppRestrictionsPreference pref = (AppRestrictionsPreference) findPreference(getKeyForPackage(packageName));
        if (pref != null) {
            pref.setEnabled(false);
        }
    }

    private boolean isPlatformSigned(PackageInfo pi) {
        if (pi == null || pi.signatures == null || !this.mSysPackageInfo.signatures[0].equals(pi.signatures[0])) {
            return false;
        }
        return true;
    }

    private boolean isAppEnabledForUser(PackageInfo pi) {
        boolean z = false;
        if (pi == null) {
            return false;
        }
        int flags = pi.applicationInfo.flags;
        int privateFlags = pi.applicationInfo.privateFlags;
        if ((8388608 & flags) != 0 && (privateFlags & 1) == 0) {
            z = true;
        }
        return z;
    }

    private void populateApps() {
        Context context = getActivity();
        if (context != null) {
            PackageManager pm = this.mPackageManager;
            IPackageManager ipm = this.mIPm;
            int userId = this.mUser.getIdentifier();
            if (Utils.getExistingUser(this.mUserManager, this.mUser) != null) {
                this.mAppList.removeAll();
                boolean z = false;
                List<ResolveInfo> receivers = pm.queryBroadcastReceivers(new Intent("android.intent.action.GET_RESTRICTION_ENTRIES"), 0);
                for (SelectableAppInfo app : this.mHelper.getVisibleApps()) {
                    String packageName = app.packageName;
                    if (packageName != null) {
                        boolean isSettingsApp = packageName.equals(context.getPackageName());
                        AppRestrictionsPreference p = new AppRestrictionsPreference(getPrefContext(), this);
                        boolean hasSettings = resolveInfoListHasPackage(receivers, packageName);
                        if (isSettingsApp) {
                            addLocationAppRestrictionsPreference(app, p);
                            this.mHelper.setPackageSelected(packageName, true);
                        } else {
                            PackageInfo pi;
                            Drawable drawable = null;
                            PackageInfo pi2 = null;
                            try {
                                pi = ipm.getPackageInfo(packageName, 4194368, userId);
                            } catch (RemoteException e) {
                                pi = pi2;
                            }
                            if (pi != null) {
                                if (!this.mRestrictedProfile || !isAppUnsupportedInRestrictedProfile(pi)) {
                                    if (app.icon != null) {
                                        drawable = app.icon.mutate();
                                    }
                                    p.setIcon(drawable);
                                    p.setChecked(z);
                                    p.setTitle(app.activityName);
                                    p.setKey(getKeyForPackage(packageName));
                                    boolean z2 = (hasSettings && app.masterEntry == null) ? true : z;
                                    p.setSettingsEnabled(z2);
                                    p.setPersistent(z);
                                    p.setOnPreferenceChangeListener(this);
                                    p.setOnPreferenceClickListener(this);
                                    p.setSummary((CharSequence) getPackageSummary(pi, app));
                                    if (pi.requiredForAllUsers || isPlatformSigned(pi)) {
                                        p.setChecked(true);
                                        p.setImmutable(true);
                                        if (hasSettings) {
                                            if (app.masterEntry == null) {
                                                requestRestrictionsForApp(packageName, p, z);
                                            }
                                        }
                                    } else if (!this.mNewUser && isAppEnabledForUser(pi)) {
                                        p.setChecked(true);
                                    }
                                    if (app.masterEntry != null) {
                                        p.setImmutable(true);
                                        p.setChecked(this.mHelper.isPackageSelected(packageName));
                                    }
                                    p.setOrder(100 * (this.mAppList.getPreferenceCount() + 2));
                                    this.mHelper.setPackageSelected(packageName, p.isChecked());
                                    this.mAppList.addPreference(p);
                                    z = false;
                                }
                            }
                        }
                    }
                }
                this.mAppListChanged = true;
                if (this.mNewUser && this.mFirstTime) {
                    this.mFirstTime = false;
                    this.mHelper.applyUserAppsStates(this);
                }
            }
        }
    }

    private String getPackageSummary(PackageInfo pi, SelectableAppInfo app) {
        if (app.masterEntry != null) {
            if (!this.mRestrictedProfile || pi.restrictedAccountType == null) {
                return getString(R.string.user_restrictions_controlled_by, new Object[]{app.masterEntry.activityName});
            }
            return getString(R.string.app_sees_restricted_accounts_and_controlled_by, new Object[]{app.masterEntry.activityName});
        } else if (pi.restrictedAccountType != null) {
            return getString(R.string.app_sees_restricted_accounts);
        } else {
            return null;
        }
    }

    private static boolean isAppUnsupportedInRestrictedProfile(PackageInfo pi) {
        return pi.requiredAccountType != null && pi.restrictedAccountType == null;
    }

    private void addLocationAppRestrictionsPreference(SelectableAppInfo app, AppRestrictionsPreference p) {
        String packageName = app.packageName;
        p.setIcon((int) R.drawable.ic_settings_location);
        p.setKey(getKeyForPackage(packageName));
        ArrayList<RestrictionEntry> restrictions = RestrictionUtils.getRestrictions(getActivity(), this.mUser);
        RestrictionEntry locationRestriction = (RestrictionEntry) restrictions.get(0);
        p.setTitle((CharSequence) locationRestriction.getTitle());
        p.setRestrictions(restrictions);
        p.setSummary((CharSequence) locationRestriction.getDescription());
        p.setChecked(locationRestriction.getSelectedState());
        p.setPersistent(false);
        p.setOnPreferenceClickListener(this);
        p.setOrder(100);
        this.mAppList.addPreference(p);
    }

    private String getKeyForPackage(String packageName) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(PKG_PREFIX);
        stringBuilder.append(packageName);
        return stringBuilder.toString();
    }

    private boolean resolveInfoListHasPackage(List<ResolveInfo> receivers, String packageName) {
        for (ResolveInfo info : receivers) {
            if (info.activityInfo.packageName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    private void updateAllEntries(String prefKey, boolean checked) {
        for (int i = 0; i < this.mAppList.getPreferenceCount(); i++) {
            Preference pref = this.mAppList.getPreference(i);
            if ((pref instanceof AppRestrictionsPreference) && prefKey.equals(pref.getKey())) {
                ((AppRestrictionsPreference) pref).setChecked(checked);
            }
        }
    }

    public void onClick(View v) {
        if (v.getTag() instanceof AppRestrictionsPreference) {
            AppRestrictionsPreference pref = (AppRestrictionsPreference) v.getTag();
            if (v.getId() == R.id.app_restrictions_settings) {
                onAppSettingsIconClicked(pref);
            } else if (!pref.isImmutable()) {
                pref.setChecked(pref.isChecked() ^ 1);
                String packageName = pref.getKey().substring(PKG_PREFIX.length());
                if (packageName.equals(getActivity().getPackageName())) {
                    ((RestrictionEntry) pref.restrictions.get(0)).setSelectedState(pref.isChecked());
                    RestrictionUtils.setRestrictions(getActivity(), pref.restrictions, this.mUser);
                    return;
                }
                this.mHelper.setPackageSelected(packageName, pref.isChecked());
                if (pref.isChecked() && pref.hasSettings && pref.restrictions == null) {
                    requestRestrictionsForApp(packageName, pref, false);
                }
                this.mAppListChanged = true;
                if (!this.mRestrictedProfile) {
                    this.mHelper.applyUserAppState(packageName, pref.isChecked(), this);
                }
                updateAllEntries(pref.getKey(), pref.isChecked());
            }
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        if (key == null || !key.contains(DELIMITER)) {
            return false;
        }
        StringTokenizer st = new StringTokenizer(key, DELIMITER);
        String packageName = st.nextToken();
        String restrictionKey = st.nextToken();
        PreferenceGroup preferenceGroup = this.mAppList;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(PKG_PREFIX);
        stringBuilder.append(packageName);
        ArrayList<RestrictionEntry> restrictions = ((AppRestrictionsPreference) preferenceGroup.findPreference(stringBuilder.toString())).getRestrictions();
        if (restrictions != null) {
            Iterator it = restrictions.iterator();
            while (it.hasNext()) {
                RestrictionEntry entry = (RestrictionEntry) it.next();
                if (entry.getKey().equals(restrictionKey)) {
                    switch (entry.getType()) {
                        case 1:
                            entry.setSelectedState(((Boolean) newValue).booleanValue());
                            break;
                        case 2:
                        case 3:
                            ListPreference listPref = (ListPreference) preference;
                            entry.setSelectedString((String) newValue);
                            listPref.setSummary(findInArray(entry.getChoiceEntries(), entry.getChoiceValues(), (String) newValue));
                            break;
                        case 4:
                            Set<String> set = (Set) newValue;
                            String[] selectedValues = new String[set.size()];
                            set.toArray(selectedValues);
                            entry.setAllSelectedStrings(selectedValues);
                            break;
                        default:
                            continue;
                    }
                    this.mUserManager.setApplicationRestrictions(packageName, RestrictionsManager.convertRestrictionsToBundle(restrictions), this.mUser);
                }
            }
        }
        return true;
    }

    private void removeRestrictionsForApp(AppRestrictionsPreference preference) {
        for (Preference p : preference.mChildren) {
            this.mAppList.removePreference(p);
        }
        preference.mChildren.clear();
    }

    private void onAppSettingsIconClicked(AppRestrictionsPreference preference) {
        if (preference.getKey().startsWith(PKG_PREFIX)) {
            if (preference.isPanelOpen()) {
                removeRestrictionsForApp(preference);
            } else {
                requestRestrictionsForApp(preference.getKey().substring(PKG_PREFIX.length()), preference, true);
            }
            preference.setPanelOpen(preference.isPanelOpen() ^ 1);
        }
    }

    private void requestRestrictionsForApp(String packageName, AppRestrictionsPreference preference, boolean invokeIfCustom) {
        Bundle oldEntries = this.mUserManager.getApplicationRestrictions(packageName, this.mUser);
        Intent intent = new Intent("android.intent.action.GET_RESTRICTION_ENTRIES");
        intent.setPackage(packageName);
        intent.putExtra("android.intent.extra.restrictions_bundle", oldEntries);
        intent.addFlags(32);
        getActivity().sendOrderedBroadcast(intent, null, new RestrictionsResultReceiver(packageName, preference, invokeIfCustom), null, -1, null, null);
    }

    private void onRestrictionsReceived(AppRestrictionsPreference preference, ArrayList<RestrictionEntry> restrictions) {
        removeRestrictionsForApp(preference);
        int count = 1;
        Iterator it = restrictions.iterator();
        while (it.hasNext()) {
            RestrictionEntry entry = (RestrictionEntry) it.next();
            Preference p = null;
            switch (entry.getType()) {
                case 1:
                    p = new SwitchPreference(getPrefContext());
                    p.setTitle(entry.getTitle());
                    p.setSummary(entry.getDescription());
                    ((SwitchPreference) p).setChecked(entry.getSelectedState());
                    break;
                case 2:
                case 3:
                    p = new ListPreference(getPrefContext());
                    p.setTitle(entry.getTitle());
                    String value = entry.getSelectedString();
                    if (value == null) {
                        value = entry.getDescription();
                    }
                    p.setSummary(findInArray(entry.getChoiceEntries(), entry.getChoiceValues(), value));
                    ((ListPreference) p).setEntryValues(entry.getChoiceValues());
                    ((ListPreference) p).setEntries(entry.getChoiceEntries());
                    ((ListPreference) p).setValue(value);
                    ((ListPreference) p).setDialogTitle((CharSequence) entry.getTitle());
                    break;
                case 4:
                    p = new MultiSelectListPreference(getPrefContext());
                    p.setTitle(entry.getTitle());
                    ((MultiSelectListPreference) p).setEntryValues(entry.getChoiceValues());
                    ((MultiSelectListPreference) p).setEntries(entry.getChoiceEntries());
                    HashSet<String> set = new HashSet();
                    Collections.addAll(set, entry.getAllSelectedStrings());
                    ((MultiSelectListPreference) p).setValues(set);
                    ((MultiSelectListPreference) p).setDialogTitle((CharSequence) entry.getTitle());
                    break;
            }
            if (p != null) {
                p.setPersistent(false);
                p.setOrder(preference.getOrder() + count);
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(preference.getKey().substring(PKG_PREFIX.length()));
                stringBuilder.append(DELIMITER);
                stringBuilder.append(entry.getKey());
                p.setKey(stringBuilder.toString());
                this.mAppList.addPreference(p);
                p.setOnPreferenceChangeListener(this);
                p.setIcon((int) R.drawable.empty_icon);
                preference.mChildren.add(p);
                count++;
            }
        }
        preference.setRestrictions(restrictions);
        if (count == 1 && preference.isImmutable() && preference.isChecked()) {
            this.mAppList.removePreference(preference);
        }
    }

    private int generateCustomActivityRequestCode(AppRestrictionsPreference preference) {
        this.mCustomRequestCode++;
        this.mCustomRequestMap.put(Integer.valueOf(this.mCustomRequestCode), preference);
        return this.mCustomRequestCode;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        AppRestrictionsPreference pref = (AppRestrictionsPreference) this.mCustomRequestMap.get(Integer.valueOf(requestCode));
        String str;
        if (pref == null) {
            str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Unknown requestCode ");
            stringBuilder.append(requestCode);
            Log.w(str, stringBuilder.toString());
            return;
        }
        if (resultCode == -1) {
            str = pref.getKey().substring(PKG_PREFIX.length());
            ArrayList<RestrictionEntry> list = data.getParcelableArrayListExtra("android.intent.extra.restrictions_list");
            Bundle bundle = data.getBundleExtra("android.intent.extra.restrictions_bundle");
            if (list != null) {
                pref.setRestrictions(list);
                this.mUserManager.setApplicationRestrictions(str, RestrictionsManager.convertRestrictionsToBundle(list), this.mUser);
            } else if (bundle != null) {
                this.mUserManager.setApplicationRestrictions(str, bundle, this.mUser);
            }
        }
        this.mCustomRequestMap.remove(Integer.valueOf(requestCode));
    }

    private String findInArray(String[] choiceEntries, String[] choiceValues, String selectedString) {
        for (int i = 0; i < choiceValues.length; i++) {
            if (choiceValues[i].equals(selectedString)) {
                return choiceEntries[i];
            }
        }
        return selectedString;
    }

    public boolean onPreferenceClick(Preference preference) {
        if (!preference.getKey().startsWith(PKG_PREFIX)) {
            return false;
        }
        AppRestrictionsPreference arp = (AppRestrictionsPreference) preference;
        if (!arp.isImmutable()) {
            String packageName = arp.getKey().substring(PKG_PREFIX.length());
            boolean newEnabledState = arp.isChecked() ^ true;
            arp.setChecked(newEnabledState);
            this.mHelper.setPackageSelected(packageName, newEnabledState);
            updateAllEntries(arp.getKey(), newEnabledState);
            this.mAppListChanged = true;
            this.mHelper.applyUserAppState(packageName, newEnabledState, this);
        }
        return true;
    }
}
