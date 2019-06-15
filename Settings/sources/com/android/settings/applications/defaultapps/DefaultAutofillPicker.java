package com.android.settings.applications.defaultapps;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.service.autofill.AutofillServiceInfo;
import android.support.v7.preference.Preference;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.content.PackageMonitor;
import com.android.settings.R;
import com.android.settings.applications.defaultapps.DefaultAppPickerFragment.ConfirmationDialogFragment;
import com.android.settingslib.applications.DefaultAppInfo;
import com.android.settingslib.utils.ThreadUtils;
import com.android.settingslib.widget.CandidateInfo;
import java.util.ArrayList;
import java.util.List;

public class DefaultAutofillPicker extends DefaultAppPickerFragment {
    static final Intent AUTOFILL_PROBE = new Intent("android.service.autofill.AutofillService");
    public static final String EXTRA_PACKAGE_NAME = "package_name";
    static final String SETTING = "autofill_service";
    private static final String TAG = "DefaultAutofillPicker";
    private OnClickListener mCancelListener;
    private final PackageMonitor mSettingsPackageMonitor = new PackageMonitor() {
        public void onPackageAdded(String packageName, int uid) {
            ThreadUtils.postOnMainThread(new -$$Lambda$DefaultAutofillPicker$1$FkWp-TdrMINB6fYhO2TMWiQylcc(this));
        }

        public void onPackageModified(String packageName) {
            ThreadUtils.postOnMainThread(new -$$Lambda$DefaultAutofillPicker$1$25IAggSj280QPpgEn1surevHwi4(this));
        }

        public void onPackageRemoved(String packageName, int uid) {
            ThreadUtils.postOnMainThread(new -$$Lambda$DefaultAutofillPicker$1$wTLnu3hVgtYHDTidiWNsKDdM5mo(this));
        }
    };

    static final class AutofillSettingIntentProvider implements SettingIntentProvider {
        private final Context mContext;
        private final String mSelectedKey;

        public AutofillSettingIntentProvider(Context context, String key) {
            this.mSelectedKey = key;
            this.mContext = context;
        }

        public Intent getIntent() {
            for (ResolveInfo resolveInfo : this.mContext.getPackageManager().queryIntentServices(DefaultAutofillPicker.AUTOFILL_PROBE, 128)) {
                ServiceInfo serviceInfo = resolveInfo.serviceInfo;
                if (TextUtils.equals(this.mSelectedKey, new ComponentName(serviceInfo.packageName, serviceInfo.name).flattenToString())) {
                    try {
                        String settingsActivity = new AutofillServiceInfo(this.mContext, serviceInfo).getSettingsActivity();
                        if (TextUtils.isEmpty(settingsActivity)) {
                            return null;
                        }
                        return new Intent("android.intent.action.MAIN").setComponent(new ComponentName(serviceInfo.packageName, settingsActivity));
                    } catch (SecurityException e) {
                        String str = DefaultAutofillPicker.TAG;
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("Error getting info for ");
                        stringBuilder.append(serviceInfo);
                        stringBuilder.append(": ");
                        stringBuilder.append(e);
                        Log.w(str, stringBuilder.toString());
                        return null;
                    }
                }
            }
            return null;
        }
    }

    public static class AutofillPickerConfirmationDialogFragment extends ConfirmationDialogFragment {
        public void onCreate(Bundle savedInstanceState) {
            setCancelListener(((DefaultAutofillPicker) getTargetFragment()).mCancelListener);
            super.onCreate(savedInstanceState);
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Activity activity = getActivity();
        if (!(activity == null || activity.getIntent().getStringExtra("package_name") == null)) {
            this.mCancelListener = new -$$Lambda$DefaultAutofillPicker$83FPzHGzIc3oGHojfgRT8534BXQ(activity);
        }
        this.mSettingsPackageMonitor.register(activity, activity.getMainLooper(), false);
        update();
    }

    static /* synthetic */ void lambda$onCreate$0(Activity activity, DialogInterface d, int w) {
        activity.setResult(0);
        activity.finish();
    }

    /* Access modifiers changed, original: protected */
    public ConfirmationDialogFragment newConfirmationDialogFragment(String selectedKey, CharSequence confirmationMessage) {
        AutofillPickerConfirmationDialogFragment fragment = new AutofillPickerConfirmationDialogFragment();
        fragment.init(this, selectedKey, confirmationMessage);
        return fragment;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.default_autofill_settings;
    }

    public int getMetricsCategory() {
        return 792;
    }

    /* Access modifiers changed, original: protected */
    public boolean shouldShowItemNone() {
        return true;
    }

    private void update() {
        updateCandidates();
        addAddServicePreference();
    }

    public void onDestroy() {
        this.mSettingsPackageMonitor.unregister();
        super.onDestroy();
    }

    private Preference newAddServicePreferenceOrNull() {
        String searchUri = Secure.getString(getActivity().getContentResolver(), "autofill_service_search_uri");
        if (TextUtils.isEmpty(searchUri)) {
            return null;
        }
        Intent addNewServiceIntent = new Intent("android.intent.action.VIEW", Uri.parse(searchUri));
        Preference preference = new Preference(getPrefContext());
        preference.setTitle((int) R.string.print_menu_item_add_service);
        preference.setIcon((int) R.drawable.ic_menu_add);
        preference.setOrder(2147483646);
        preference.setIntent(addNewServiceIntent);
        preference.setPersistent(false);
        return preference;
    }

    private void addAddServicePreference() {
        Preference addNewServicePreference = newAddServicePreferenceOrNull();
        if (addNewServicePreference != null) {
            getPreferenceScreen().addPreference(addNewServicePreference);
        }
    }

    /* Access modifiers changed, original: protected */
    public List<DefaultAppInfo> getCandidates() {
        List<DefaultAppInfo> candidates = new ArrayList();
        List<ResolveInfo> resolveInfos = this.mPm.queryIntentServices(AUTOFILL_PROBE, 128);
        Context context = getContext();
        for (ResolveInfo info : resolveInfos) {
            String permission = info.serviceInfo.permission;
            if ("android.permission.BIND_AUTOFILL_SERVICE".equals(permission)) {
                candidates.add(new DefaultAppInfo(context, this.mPm, this.mUserId, new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name)));
            }
            if ("android.permission.BIND_AUTOFILL".equals(permission)) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("AutofillService from '");
                stringBuilder.append(info.serviceInfo.packageName);
                stringBuilder.append("' uses unsupported permission ");
                stringBuilder.append("android.permission.BIND_AUTOFILL");
                stringBuilder.append(". It works for now, but might not be supported on future releases");
                Log.w(str, stringBuilder.toString());
                candidates.add(new DefaultAppInfo(context, this.mPm, this.mUserId, new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name)));
            }
        }
        return candidates;
    }

    public static String getDefaultKey(Context context) {
        String setting = Secure.getString(context.getContentResolver(), SETTING);
        if (setting != null) {
            ComponentName componentName = ComponentName.unflattenFromString(setting);
            if (componentName != null) {
                return componentName.flattenToString();
            }
        }
        return null;
    }

    /* Access modifiers changed, original: protected */
    public String getDefaultKey() {
        return getDefaultKey(getContext());
    }

    /* Access modifiers changed, original: protected */
    public CharSequence getConfirmationMessage(CandidateInfo appInfo) {
        if (appInfo == null) {
            return null;
        }
        CharSequence appName = appInfo.loadLabel();
        return Html.fromHtml(getContext().getString(R.string.autofill_confirmation_message, new Object[]{appName}));
    }

    /* Access modifiers changed, original: protected */
    public boolean setDefaultKey(String key) {
        Secure.putString(getContext().getContentResolver(), SETTING, key);
        Activity activity = getActivity();
        if (activity != null) {
            String packageName = activity.getIntent().getStringExtra("package_name");
            if (packageName != null) {
                int result;
                if (key == null || !key.startsWith(packageName)) {
                    result = 0;
                } else {
                    result = -1;
                }
                activity.setResult(result);
                activity.finish();
            }
        }
        return true;
    }
}
