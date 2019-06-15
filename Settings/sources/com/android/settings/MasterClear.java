package com.android.settings;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.support.annotation.VisibleForTesting;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.telephony.euicc.EuiccManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.OpFeatures;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ScrollView;
import android.widget.TextView;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.password.ChooseLockSettingsHelper;
import com.oneplus.settings.ui.OPFactoryResetConfirmCategory;
import com.oneplus.settings.ui.OPFactoryResetConfirmCategory.OnFactoryResetConfirmListener;

public class MasterClear extends SettingsPreferenceFragment implements OnGlobalLayoutListener, OnPreferenceChangeListener, OnFactoryResetConfirmListener {
    @VisibleForTesting
    static final int CREDENTIAL_CONFIRM_REQUEST = 56;
    static final String ERASE_ESIMS_EXTRA = "erase_esim";
    static final String ERASE_EXTERNAL_EXTRA = "erase_sd";
    @VisibleForTesting
    static final int KEYGUARD_REQUEST = 55;
    private static final String KEY_SHOW_ESIM_RESET_CHECKBOX = "masterclear.allow_retain_esim_profiles_after_fdr";
    private static final String TAG = "MasterClear";
    private View mContentView;
    @VisibleForTesting
    CheckBox mEsimStorage;
    private View mEsimStorageContainer;
    @VisibleForTesting
    CheckBox mExternalStorage;
    private View mExternalStorageContainer;
    @VisibleForTesting
    Button mInitiateButton;
    @VisibleForTesting
    protected final OnClickListener mInitiateListener = new OnClickListener() {
        public void onClick(View view) {
            Context context = view.getContext();
            if (Utils.isDemoUser(context)) {
                ComponentName componentName = Utils.getDeviceOwnerComponent(context);
                if (componentName != null) {
                    context.startActivity(new Intent().setPackage(componentName.getPackageName()).setAction("android.intent.action.FACTORY_RESET"));
                }
            } else if (!MasterClear.this.runKeyguardConfirmation(55)) {
                Intent intent = MasterClear.this.getAccountConfirmationIntent();
                if (intent != null) {
                    MasterClear.this.showAccountCredentialConfirmation(intent);
                } else {
                    MasterClear.this.showFinalConfirmation("");
                }
            }
        }
    };
    private OPFactoryResetConfirmCategory mOPFactoryResetConfirmCategory;
    private SwitchPreference mOptionalSwitchPreference;
    @VisibleForTesting
    ScrollView mScrollView;

    public void onGlobalLayout() {
        this.mInitiateButton.setEnabled(hasReachedBottom(this.mScrollView));
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.op_reset_all_data_settings);
        establishInitialState();
        getActivity().setTitle(R.string.master_clear_short_title);
    }

    private boolean runKeyguardConfirmation(int request) {
        return new ChooseLockSettingsHelper(getActivity(), this).launchConfirmationActivity(request, getActivity().getResources().getText(R.string.master_clear_short_title));
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public boolean isValidRequestCode(int requestCode) {
        return requestCode == 55 || requestCode == 56;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        onActivityResultInternal(requestCode, resultCode, data);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void onActivityResultInternal(int requestCode, int resultCode, Intent data) {
        if (!isValidRequestCode(requestCode)) {
            return;
        }
        if (resultCode != -1) {
            establishInitialState();
            return;
        }
        if (56 != requestCode) {
            Intent accountConfirmationIntent = getAccountConfirmationIntent();
            Intent intent = accountConfirmationIntent;
            if (accountConfirmationIntent != null) {
                showAccountCredentialConfirmation(intent);
            }
        }
        if (data != null) {
            showFinalConfirmation(data.getStringExtra("power_on_psw"));
        }
    }

    private void showFinalConfirmation(String psw) {
        Bundle args = new Bundle();
        args.putBoolean(ERASE_EXTERNAL_EXTRA, this.mOptionalSwitchPreference.isChecked());
        args.putString("power_on_psw", psw);
        ((SettingsActivity) getActivity()).startPreferencePanel(MasterClearConfirm.class.getName(), args, R.string.master_clear_confirm_title, null, null, 0);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void showFinalConfirmation() {
        Bundle args = new Bundle();
        args.putBoolean(ERASE_EXTERNAL_EXTRA, this.mExternalStorage.isChecked());
        args.putBoolean(ERASE_ESIMS_EXTRA, this.mEsimStorage.isChecked());
        new SubSettingLauncher(getContext()).setDestination(MasterClearConfirm.class.getName()).setArguments(args).setTitle((int) R.string.master_clear_confirm_title).setSourceMetricsCategory(getMetricsCategory()).launch();
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void showAccountCredentialConfirmation(Intent intent) {
        startActivityForResult(intent, 56);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public Intent getAccountConfirmationIntent() {
        Context context = getActivity();
        String accountType = context.getString(R.string.account_type);
        String packageName = context.getString(R.string.account_confirmation_package);
        String className = context.getString(R.string.account_confirmation_class);
        if (TextUtils.isEmpty(accountType) || TextUtils.isEmpty(packageName) || TextUtils.isEmpty(className)) {
            Log.i(TAG, "Resources not set for account confirmation.");
            return null;
        }
        Account[] accounts = AccountManager.get(context).getAccountsByType(accountType);
        if (accounts == null || accounts.length <= 0) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("No ");
            stringBuilder.append(accountType);
            stringBuilder.append(" accounts installed!");
            Log.d(str, stringBuilder.toString());
        } else {
            Intent requestAccountConfirmation = new Intent().setPackage(packageName).setComponent(new ComponentName(packageName, className));
            ResolveInfo resolution = context.getPackageManager().resolveActivity(requestAccountConfirmation, null);
            if (resolution != null && resolution.activityInfo != null && packageName.equals(resolution.activityInfo.packageName)) {
                return requestAccountConfirmation;
            }
            String str2 = TAG;
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("Unable to resolve Activity: ");
            stringBuilder2.append(packageName);
            stringBuilder2.append("/");
            stringBuilder2.append(className);
            Log.i(str2, stringBuilder2.toString());
        }
        return null;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void establishInitialState() {
        this.mOptionalSwitchPreference = (SwitchPreference) findPreference("op_optional_reset");
        if (OpFeatures.isSupport(new int[]{85})) {
            this.mOptionalSwitchPreference.setChecked(true);
        } else {
            this.mOptionalSwitchPreference.setChecked(false);
        }
        this.mOptionalSwitchPreference.setOnPreferenceChangeListener(this);
        this.mOPFactoryResetConfirmCategory = (OPFactoryResetConfirmCategory) findPreference("op_factory_reset_confirm");
        this.mOPFactoryResetConfirmCategory.setOnFactoryResetConfirmListener(this);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public boolean showWipeEuicc() {
        Context context = getContext();
        boolean z = false;
        if (!isEuiccEnabled(context)) {
            return false;
        }
        ContentResolver cr = context.getContentResolver();
        if (!(Global.getInt(cr, "euicc_provisioned", 0) == 0 && Global.getInt(cr, "development_settings_enabled", 0) == 0)) {
            z = true;
        }
        return z;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public boolean showWipeEuiccCheckbox() {
        return SystemProperties.getBoolean(KEY_SHOW_ESIM_RESET_CHECKBOX, false);
    }

    /* Access modifiers changed, original: protected */
    @VisibleForTesting
    public boolean isEuiccEnabled(Context context) {
        return ((EuiccManager) context.getSystemService("euicc")).isEnabled();
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public boolean hasReachedBottom(ScrollView scrollView) {
        if (scrollView.getChildCount() < 1) {
            return true;
        }
        boolean z = false;
        if (scrollView.getChildAt(0).getBottom() - (scrollView.getHeight() + scrollView.getScrollY()) <= 0) {
            z = true;
        }
        return z;
    }

    private void getContentDescription(View v, StringBuffer description) {
        if (v.getVisibility() == 0) {
            if (v instanceof ViewGroup) {
                ViewGroup vGroup = (ViewGroup) v;
                for (int i = 0; i < vGroup.getChildCount(); i++) {
                    getContentDescription(vGroup.getChildAt(i), description);
                }
            } else if (v instanceof TextView) {
                description.append(((TextView) v).getText());
                description.append(",");
            }
        }
    }

    private boolean isExtStorageEncrypted() {
        return "".equals(SystemProperties.get("vold.decrypt")) ^ 1;
    }

    /* JADX WARNING: Removed duplicated region for block: B:52:0x0167  */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x0167  */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x0167  */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x0129 A:{ExcHandler: NotFoundException (e android.content.res.Resources$NotFoundException), Splitter:B:29:0x00f9} */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x0167  */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x0167  */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing block: B:45:0x0129, code skipped:
            r0 = e;
     */
    /* JADX WARNING: Missing block: B:46:0x012a, code skipped:
            r27 = r8;
     */
    /* JADX WARNING: Missing block: B:49:0x0148, code skipped:
            r27 = r8;
            r28 = r11;
     */
    private void loadAccountList(android.os.UserManager r31) {
        /*
        r30 = this;
        r1 = r30;
        r0 = r1.mContentView;
        r2 = 2131361809; // 0x7f0a0011 float:1.834338E38 double:1.0530326487E-314;
        r2 = r0.findViewById(r2);
        r0 = r1.mContentView;
        r3 = 2131361808; // 0x7f0a0010 float:1.8343379E38 double:1.053032648E-314;
        r0 = r0.findViewById(r3);
        r3 = r0;
        r3 = (android.widget.LinearLayout) r3;
        r3.removeAllViews();
        r4 = r30.getActivity();
        r0 = android.os.UserHandle.myUserId();
        r5 = r31;
        r6 = r5.getProfiles(r0);
        r7 = r6.size();
        r8 = android.accounts.AccountManager.get(r4);
        r0 = "layout_inflater";
        r0 = r4.getSystemService(r0);
        r9 = r0;
        r9 = (android.view.LayoutInflater) r9;
        r0 = 0;
        r11 = r0;
        r0 = 0;
    L_0x003c:
        r12 = r0;
        if (r12 >= r7) goto L_0x01ac;
    L_0x003f:
        r13 = r6.get(r12);
        r13 = (android.content.pm.UserInfo) r13;
        r14 = r13.id;
        r15 = new android.os.UserHandle;
        r15.<init>(r14);
        r10 = r8.getAccountsAsUser(r14);
        r16 = r6;
        r6 = r10.length;
        if (r6 != 0) goto L_0x005a;
        r18 = r8;
        goto L_0x01a4;
    L_0x005a:
        r11 = r11 + r6;
        r0 = android.accounts.AccountManager.get(r4);
        r18 = r8;
        r8 = r0.getAuthenticatorTypesAsUser(r14);
        r19 = r11;
        r11 = r8.length;
        r20 = r14;
        r14 = 16908310; // 0x1020016 float:2.387729E-38 double:8.353815E-317;
        r0 = 1;
        if (r7 <= r0) goto L_0x0095;
    L_0x0070:
        r0 = com.android.settings.Utils.inflateCategoryHeader(r9, r3);
        r17 = r0.findViewById(r14);
        r14 = r17;
        r14 = (android.widget.TextView) r14;
        r17 = r13.isManagedProfile();
        if (r17 == 0) goto L_0x008a;
    L_0x0082:
        r17 = 2131887023; // 0x7f1203af float:1.9408641E38 double:1.053292139E-314;
    L_0x0085:
        r21 = r13;
        r13 = r17;
        goto L_0x008e;
    L_0x008a:
        r17 = 2131887022; // 0x7f1203ae float:1.940864E38 double:1.0532921384E-314;
        goto L_0x0085;
    L_0x008e:
        r14.setText(r13);
        r3.addView(r0);
        goto L_0x0097;
    L_0x0095:
        r21 = r13;
    L_0x0097:
        r0 = 0;
    L_0x0098:
        r13 = r0;
        if (r13 >= r6) goto L_0x01a2;
    L_0x009b:
        r14 = r10[r13];
        r0 = 0;
        r17 = 0;
    L_0x00a0:
        r22 = r17;
        r23 = r0;
        r0 = r22;
        if (r0 >= r11) goto L_0x00c4;
    L_0x00a8:
        r24 = r6;
        r6 = r14.type;
        r25 = r10;
        r10 = r8[r0];
        r10 = r10.type;
        r6 = r6.equals(r10);
        if (r6 == 0) goto L_0x00bb;
    L_0x00b8:
        r6 = r8[r0];
        goto L_0x00ca;
    L_0x00bb:
        r17 = r0 + 1;
        r0 = r23;
        r6 = r24;
        r10 = r25;
        goto L_0x00a0;
    L_0x00c4:
        r24 = r6;
        r25 = r10;
        r6 = r23;
    L_0x00ca:
        if (r6 != 0) goto L_0x00f5;
    L_0x00cc:
        r0 = "MasterClear";
        r10 = new java.lang.StringBuilder;
        r10.<init>();
        r26 = r8;
        r8 = "No descriptor for account name=";
        r10.append(r8);
        r8 = r14.name;
        r10.append(r8);
        r8 = " type=";
        r10.append(r8);
        r8 = r14.type;
        r10.append(r8);
        r8 = r10.toString();
        android.util.Log.w(r0, r8);
        r28 = r11;
        goto L_0x0196;
    L_0x00f5:
        r26 = r8;
        r0 = 0;
        r8 = r0;
        r0 = r6.iconId;	 Catch:{ NameNotFoundException -> 0x0147, NotFoundException -> 0x0129 }
        if (r0 == 0) goto L_0x0122;
    L_0x00fd:
        r0 = r6.packageName;	 Catch:{ NameNotFoundException -> 0x011c, NotFoundException -> 0x0129 }
        r10 = 0;
        r0 = r4.createPackageContextAsUser(r0, r10, r15);	 Catch:{ NameNotFoundException -> 0x011c, NotFoundException -> 0x0129 }
        r10 = r4.getPackageManager();	 Catch:{ NameNotFoundException -> 0x011c, NotFoundException -> 0x0129 }
        r27 = r8;
        r8 = r6.iconId;	 Catch:{ NameNotFoundException -> 0x0118, NotFoundException -> 0x0116 }
        r8 = r0.getDrawable(r8);	 Catch:{ NameNotFoundException -> 0x0118, NotFoundException -> 0x0116 }
        r8 = r10.getUserBadgedIcon(r8, r15);	 Catch:{ NameNotFoundException -> 0x0118, NotFoundException -> 0x0116 }
        r0 = r8;
        goto L_0x0124;
    L_0x0116:
        r0 = move-exception;
        goto L_0x012c;
    L_0x0118:
        r0 = move-exception;
        r28 = r11;
        goto L_0x014c;
    L_0x011c:
        r0 = move-exception;
        r27 = r8;
        r28 = r11;
        goto L_0x014c;
    L_0x0122:
        r27 = r8;
    L_0x0124:
        r27 = r8;
        r28 = r11;
        goto L_0x0165;
    L_0x0129:
        r0 = move-exception;
        r27 = r8;
    L_0x012c:
        r8 = "MasterClear";
        r10 = new java.lang.StringBuilder;
        r10.<init>();
        r28 = r11;
        r11 = "Invalid icon id for account type ";
        r10.append(r11);
        r11 = r6.type;
        r10.append(r11);
        r10 = r10.toString();
        android.util.Log.w(r8, r10, r0);
        goto L_0x0165;
    L_0x0147:
        r0 = move-exception;
        r27 = r8;
        r28 = r11;
    L_0x014c:
        r8 = "MasterClear";
        r10 = new java.lang.StringBuilder;
        r10.<init>();
        r11 = "Bad package name for account type ";
        r10.append(r11);
        r11 = r6.type;
        r10.append(r11);
        r10 = r10.toString();
        android.util.Log.w(r8, r10);
    L_0x0165:
        if (r27 != 0) goto L_0x016f;
    L_0x0167:
        r0 = r4.getPackageManager();
        r27 = r0.getDefaultActivityIcon();
    L_0x016f:
        r0 = r27;
        r8 = 2131558657; // 0x7f0d0101 float:1.8742636E38 double:1.0531299045E-314;
        r10 = 0;
        r8 = r9.inflate(r8, r3, r10);
        r10 = 16908294; // 0x1020006 float:2.3877246E-38 double:8.353807E-317;
        r10 = r8.findViewById(r10);
        r10 = (android.widget.ImageView) r10;
        r10.setImageDrawable(r0);
        r10 = 16908310; // 0x1020016 float:2.387729E-38 double:8.353815E-317;
        r11 = r8.findViewById(r10);
        r11 = (android.widget.TextView) r11;
        r10 = r14.name;
        r11.setText(r10);
        r3.addView(r8);
    L_0x0196:
        r0 = r13 + 1;
        r6 = r24;
        r10 = r25;
        r8 = r26;
        r11 = r28;
        goto L_0x0098;
    L_0x01a2:
        r11 = r19;
    L_0x01a4:
        r0 = r12 + 1;
        r6 = r16;
        r8 = r18;
        goto L_0x003c;
    L_0x01ac:
        r16 = r6;
        r18 = r8;
        r0 = 1;
        if (r11 <= 0) goto L_0x01bb;
    L_0x01b3:
        r6 = 0;
        r2.setVisibility(r6);
        r3.setVisibility(r6);
        goto L_0x01bc;
    L_0x01bb:
        r6 = 0;
    L_0x01bc:
        r8 = r1.mContentView;
        r10 = 2131362808; // 0x7f0a03f8 float:1.8345407E38 double:1.0530331423E-314;
        r8 = r8.findViewById(r10);
        r10 = r31.getUserCount();
        r10 = r10 - r7;
        if (r10 <= 0) goto L_0x01cd;
    L_0x01cc:
        goto L_0x01ce;
    L_0x01cd:
        r0 = r6;
    L_0x01ce:
        if (r0 == 0) goto L_0x01d1;
    L_0x01d0:
        goto L_0x01d4;
    L_0x01d1:
        r10 = 8;
        r6 = r10;
    L_0x01d4:
        r8.setVisibility(r6);
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settings.MasterClear.loadAccountList(android.os.UserManager):void");
    }

    public int getMetricsCategory() {
        return 66;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return preference == this.mOptionalSwitchPreference ? true : true;
    }

    public void onFactoryResetConfirmClick() {
        if (!runKeyguardConfirmation(55)) {
            showFinalConfirmation("");
        }
    }
}
