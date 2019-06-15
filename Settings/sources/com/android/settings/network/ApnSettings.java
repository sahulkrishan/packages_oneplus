package com.android.settings.network;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Telephony.Carriers;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.telephony.CarrierConfigManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.Toast;
import com.android.internal.telephony.PhoneConstants.DataState;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.dataconnection.ApnSetting;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UiccController;
import com.android.settings.R;
import com.android.settings.RestrictedSettingsFragment;
import com.android.settings.Utils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.oneplus.settings.utils.OPFirewallUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ApnSettings extends RestrictedSettingsFragment implements OnPreferenceChangeListener {
    private static final String ACTION_RESTORE_DEFAULT_APN = "android.intent.action.restoreDefaultAPN";
    private static final String APN_HIDE_RULE_STRINGS_ARRAY = "apn_hide_rule_strings_array";
    private static final String APN_HIDE_RULE_STRINGS_WITH_ICCIDS_ARRAY = "apn_hide_rule_strings_with_iccids_array";
    public static final String APN_ID = "apn_id";
    private static final int APN_INDEX = 2;
    private static final int BEARER_BITMASK_INDEX = 8;
    private static final int BEARER_INDEX = 7;
    private static final Uri DEFAULTAPN_URI = Uri.parse(RESTORE_CARRIERS_URI);
    private static final int DIALOG_RESTORE_DEFAULTAPN = 1001;
    private static final int EVENT_RESTORE_DEFAULTAPN_COMPLETE = 2;
    private static final int EVENT_RESTORE_DEFAULTAPN_START = 1;
    public static final String EXTRA_POSITION = "position";
    private static final int ID_INDEX = 0;
    private static final String INCLUDE_COMMON_RULES = "include_common_rules";
    private static final int MENU_NEW = 1;
    private static final int MENU_RESTORE = 2;
    public static final String MVNO_MATCH_DATA = "mvno_match_data";
    private static final int MVNO_MATCH_DATA_INDEX = 5;
    public static final String MVNO_TYPE = "mvno_type";
    private static final int MVNO_TYPE_INDEX = 4;
    private static final int NAME_INDEX = 1;
    private static final Uri PREFERAPN_URI = Uri.parse(PREFERRED_APN_URI);
    public static final String PREFERRED_APN_URI = "content://telephony/carriers/preferapn";
    public static final String RESTORE_CARRIERS_URI = "content://telephony/carriers/restore";
    private static final int RO_INDEX = 6;
    public static final String SUB_ID = "sub_id";
    static final String TAG = "ApnSettings";
    private static final int TYPES_INDEX = 3;
    private static final boolean isVzwSim = SystemProperties.getBoolean("vendor.radio.test.vzw.sim", false);
    private static boolean mRestoreDefaultApnMode;
    private OnKeyListener keylistener = new OnKeyListener() {
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
            String str = ApnSettings.TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("onKey keyCode = ");
            stringBuilder.append(keyCode);
            Log.d(str, stringBuilder.toString());
            if (keyCode == 4 && event.getRepeatCount() == 0) {
                return true;
            }
            return false;
        }
    };
    private boolean mAllowAddingApns;
    private PersistableBundle mHideApnsGroupByIccid;
    private String[] mHideApnsWithIccidRule;
    private String[] mHideApnsWithRule;
    private boolean mHideImsApn;
    private IntentFilter mMobileStateFilter;
    private final BroadcastReceiver mMobileStateReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String str = ApnSettings.TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("onReceive : ");
            stringBuilder.append(intent);
            Log.d(str, stringBuilder.toString());
            if (intent.getAction().equals("android.intent.action.ANY_DATA_STATE")) {
                if (AnonymousClass4.$SwitchMap$com$android$internal$telephony$PhoneConstants$DataState[ApnSettings.getMobileDataState(intent).ordinal()] == 1 && !ApnSettings.mRestoreDefaultApnMode) {
                    ApnSettings.this.fillList();
                }
            } else if (intent.getAction().equals("android.intent.action.SIM_STATE_CHANGED")) {
                int slotId = intent.getIntExtra("phone", -1);
                String str2 = ApnSettings.TAG;
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append("slotId: ");
                stringBuilder2.append(slotId);
                Log.d(str2, stringBuilder2.toString());
                if (slotId != -1) {
                    String simStatus = intent.getStringExtra("ss");
                    str2 = ApnSettings.TAG;
                    stringBuilder2 = new StringBuilder();
                    stringBuilder2.append("simStatus: ");
                    stringBuilder2.append(simStatus);
                    Log.d(str2, stringBuilder2.toString());
                    if ("ABSENT".equals(simStatus) && ApnSettings.this.mSubscriptionInfo != null && ApnSettings.this.mSubscriptionInfo.getSimSlotIndex() == slotId) {
                        ApnSettings.this.finish();
                    }
                }
            } else if (intent.getAction().equals(ApnSettings.ACTION_RESTORE_DEFAULT_APN) && ApnSettings.this.mRestoreDefaultApnThread != null && ApnSettings.mRestoreDefaultApnMode) {
                ApnSettings.this.mRestoreDefaultApnThread.quit();
                ApnSettings.this.mRestoreDefaultApnThread = null;
                ApnSettings.this.mRestoreApnProcessHandler = null;
                ApnSettings.this.fillList();
                ApnSettings.this.getPreferenceScreen().setEnabled(true);
                ApnSettings.mRestoreDefaultApnMode = false;
                ApnSettings.this.removeDialog(1001);
                Toast.makeText(ApnSettings.this.getActivity(), ApnSettings.this.getResources().getString(R.string.restore_default_apn_completed), 1).show();
            }
        }
    };
    private String mMvnoMatchData;
    private String mMvnoType;
    private RestoreApnProcessHandler mRestoreApnProcessHandler;
    private RestoreApnUiHandler mRestoreApnUiHandler;
    private HandlerThread mRestoreDefaultApnThread;
    private String mSelectedKey;
    private SubscriptionInfo mSubscriptionInfo;
    private UiccController mUiccController;
    private boolean mUnavailable;
    private UserManager mUserManager;

    /* renamed from: com.android.settings.network.ApnSettings$4 */
    static /* synthetic */ class AnonymousClass4 {
        static final /* synthetic */ int[] $SwitchMap$com$android$internal$telephony$PhoneConstants$DataState = new int[DataState.values().length];

        static {
            try {
                $SwitchMap$com$android$internal$telephony$PhoneConstants$DataState[DataState.CONNECTED.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
        }
    }

    private class RestoreApnProcessHandler extends Handler {
        private Handler mRestoreApnUiHandler;

        public RestoreApnProcessHandler(Looper looper, Handler restoreApnUiHandler) {
            super(looper);
            this.mRestoreApnUiHandler = restoreApnUiHandler;
        }

        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                ApnSettings.this.getSystemService("phone");
                String mccmnc = ApnSettings.this.mSubscriptionInfo == null ? "" : PhoneFactory.getPhone(ApnSettings.this.mSubscriptionInfo.getSimSlotIndex()).getOperatorNumeric();
                String str = ApnSettings.TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("delete apn mccmnc = ");
                stringBuilder.append(mccmnc);
                Log.d(str, stringBuilder.toString());
                stringBuilder = new StringBuilder();
                stringBuilder.append("numeric=\"");
                stringBuilder.append(mccmnc);
                stringBuilder.append("\"");
                ApnSettings.this.getContentResolver().delete(ApnSettings.this.getUriForCurrSubId(ApnSettings.DEFAULTAPN_URI), new StringBuilder(stringBuilder.toString()).toString(), null);
                this.mRestoreApnUiHandler.sendEmptyMessageDelayed(2, 60000);
            }
        }
    }

    private class RestoreApnUiHandler extends Handler {
        private RestoreApnUiHandler() {
        }

        /* synthetic */ RestoreApnUiHandler(ApnSettings x0, AnonymousClass1 x1) {
            this();
        }

        public void handleMessage(Message msg) {
            if (msg.what == 2) {
                Activity activity = ApnSettings.this.getActivity();
                if (activity == null) {
                    ApnSettings.mRestoreDefaultApnMode = false;
                    return;
                }
                ApnSettings.this.fillList();
                ApnSettings.this.getPreferenceScreen().setEnabled(true);
                ApnSettings.mRestoreDefaultApnMode = false;
                ApnSettings.this.removeDialog(1001);
                Toast.makeText(activity, ApnSettings.this.getResources().getString(R.string.restore_default_apn_completed), 1).show();
            }
        }
    }

    public ApnSettings() {
        super("no_config_mobile_networks");
    }

    private static DataState getMobileDataState(Intent intent) {
        String str = intent.getStringExtra("state");
        if (str != null) {
            return (DataState) Enum.valueOf(DataState.class, str);
        }
        return DataState.DISCONNECTED;
    }

    public int getMetricsCategory() {
        return 12;
    }

    public void onCreate(Bundle icicle) {
        getActivity().getWindow().getDecorView().setSystemUiVisibility(0);
        super.onCreate(icicle);
        Activity activity = getActivity();
        int subId = activity.getIntent().getIntExtra("sub_id", -1);
        this.mMobileStateFilter = new IntentFilter("android.intent.action.ANY_DATA_STATE");
        this.mMobileStateFilter.addAction("android.intent.action.SIM_STATE_CHANGED");
        this.mMobileStateFilter.addAction("android.intent.action.CLOSE_SYSTEM_DIALOGS");
        this.mMobileStateFilter.addAction(ACTION_RESTORE_DEFAULT_APN);
        setIfOnlyAvailableForAdmins(true);
        this.mSubscriptionInfo = SubscriptionManager.from(activity).getActiveSubscriptionInfo(subId);
        this.mUiccController = UiccController.getInstance();
        PersistableBundle b = ((CarrierConfigManager) getSystemService("carrier_config")).getConfigForSubId(subId);
        this.mHideImsApn = b.getBoolean("hide_ims_apn_bool");
        this.mAllowAddingApns = b.getBoolean("allow_adding_apns_bool");
        this.mHideApnsWithRule = b.getStringArray(APN_HIDE_RULE_STRINGS_ARRAY);
        this.mHideApnsWithIccidRule = b.getStringArray(APN_HIDE_RULE_STRINGS_WITH_ICCIDS_ARRAY);
        if (this.mSubscriptionInfo != null) {
            String iccid = this.mSubscriptionInfo.getIccId();
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("iccid: ");
            stringBuilder.append(iccid);
            Log.d(str, stringBuilder.toString());
            this.mHideApnsGroupByIccid = b.getPersistableBundle(iccid);
        }
        if (this.mAllowAddingApns && ApnEditor.hasAllApns(b.getStringArray("read_only_apn_types_string_array"))) {
            Log.d(TAG, "not allowing adding APN because all APN types are read only");
            this.mAllowAddingApns = false;
        }
        this.mUserManager = UserManager.get(activity);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getEmptyTextView().setText(R.string.apn_settings_not_available);
        this.mUnavailable = isUiRestricted();
        setHasOptionsMenu(this.mUnavailable ^ 1);
        if (this.mUnavailable) {
            addPreferencesFromResource(R.xml.placeholder_prefs);
            return;
        }
        if (getPreferenceScreen() != null) {
            getPreferenceScreen().removeAll();
        }
        addPreferencesFromResource(R.xml.apn_settings);
    }

    public void onResume() {
        super.onResume();
        if (this.mRestoreDefaultApnThread != null) {
            removeDialog(1001);
            this.mRestoreDefaultApnThread.quit();
            this.mRestoreDefaultApnThread = null;
            this.mRestoreApnProcessHandler = null;
        }
        if (!this.mUnavailable) {
            getActivity().registerReceiver(this.mMobileStateReceiver, this.mMobileStateFilter);
            if (!mRestoreDefaultApnMode) {
                fillList();
            }
        }
    }

    public void onPause() {
        super.onPause();
        if (this.mRestoreDefaultApnThread != null) {
            removeDialog(1001);
            this.mRestoreDefaultApnThread.quit();
            this.mRestoreDefaultApnThread = null;
            this.mRestoreApnProcessHandler = null;
        }
        if (!this.mUnavailable) {
            getActivity().unregisterReceiver(this.mMobileStateReceiver);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mRestoreDefaultApnThread != null) {
            this.mRestoreDefaultApnThread.quit();
        }
    }

    public EnforcedAdmin getRestrictionEnforcedAdmin() {
        UserHandle user = UserHandle.of(this.mUserManager.getUserHandle());
        if (!this.mUserManager.hasUserRestriction("no_config_mobile_networks", user) || this.mUserManager.hasBaseUserRestriction("no_config_mobile_networks", user)) {
            return null;
        }
        return EnforcedAdmin.MULTIPLE_ENFORCED_ADMIN;
    }

    /* JADX WARNING: Removed duplicated region for block: B:62:0x0285  */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x0222  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x01c3  */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x01db  */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x0222  */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x0285  */
    private void fillList() {
        /*
        r37 = this;
        r14 = r37;
        r0 = 0;
        r1 = "phone";
        r14.getSystemService(r1);
        r1 = r14.mSubscriptionInfo;
        if (r1 == 0) goto L_0x0013;
    L_0x000c:
        r1 = r14.mSubscriptionInfo;
        r1 = r1.getSubscriptionId();
        goto L_0x0014;
    L_0x0013:
        r1 = -1;
    L_0x0014:
        r15 = r1;
        r1 = r14.mSubscriptionInfo;
        if (r1 != 0) goto L_0x001c;
    L_0x0019:
        r1 = "";
        goto L_0x002a;
    L_0x001c:
        r1 = r14.mSubscriptionInfo;
        r1 = r1.getSimSlotIndex();
        r1 = com.android.internal.telephony.PhoneFactory.getPhone(r1);
        r1 = r1.getOperatorNumeric();
    L_0x002a:
        r13 = r1;
        r1 = "ApnSettings";
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = "mccmnc = getOperatorNumeric = ";
        r2.append(r3);
        r2.append(r13);
        r2 = r2.toString();
        android.util.Log.d(r1, r2);
        r1 = new java.lang.StringBuilder;
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = "numeric=\"";
        r2.append(r3);
        r2.append(r13);
        r3 = "\" AND NOT (type='ia' AND (apn=\"\" OR apn IS NULL)) AND user_visible!=0";
        r2.append(r3);
        r2 = r2.toString();
        r1.<init>(r2);
        r12 = r1;
        r1 = "ApnSettings";
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = "where = ";
        r2.append(r3);
        r3 = r12.toString();
        r2.append(r3);
        r2 = r2.toString();
        android.util.Log.d(r1, r2);
        r3 = r37.getContentResolver();
        r4 = android.provider.Telephony.Carriers.CONTENT_URI;
        r16 = "_id";
        r17 = "name";
        r18 = "apn";
        r19 = "type";
        r20 = "mvno_type";
        r21 = "mvno_match_data";
        r22 = "read_only";
        r23 = "bearer";
        r24 = "bearer_bitmask";
        r5 = new java.lang.String[]{r16, r17, r18, r19, r20, r21, r22, r23, r24};
        r6 = r12.toString();
        r7 = 0;
        r8 = "_id";
        r11 = r3.query(r4, r5, r6, r7, r8);
        if (r11 == 0) goto L_0x032a;
    L_0x00a0:
        r1 = 0;
        r2 = r14.mUiccController;
        r8 = 1;
        if (r2 == 0) goto L_0x00b4;
    L_0x00a6:
        r2 = r14.mSubscriptionInfo;
        if (r2 == 0) goto L_0x00b4;
    L_0x00aa:
        r2 = r14.mUiccController;
        r3 = android.telephony.SubscriptionManager.getPhoneId(r15);
        r1 = r2.getIccRecords(r3, r8);
    L_0x00b4:
        r16 = r1;
        r1 = "apn_list";
        r1 = r14.findPreference(r1);
        r7 = r1;
        r7 = (android.support.v7.preference.PreferenceGroup) r7;
        r7.removeAll();
        r2 = new java.util.ArrayList;
        r2.<init>();
        r1 = new java.util.ArrayList;
        r1.<init>();
        r6 = r1;
        r9 = new java.util.ArrayList;
        r9.<init>();
        r10 = new java.util.ArrayList;
        r10.<init>();
        r1 = r37.getSelectedApnKey();
        r14.mSelectedKey = r1;
        r1 = "ApnSettings";
        r3 = new java.lang.StringBuilder;
        r3.<init>();
        r4 = "select key = ";
        r3.append(r4);
        r4 = r14.mSelectedKey;
        r3.append(r4);
        r3 = r3.toString();
        android.util.Log.d(r1, r3);
        r11.moveToFirst();
    L_0x00f8:
        r1 = r11.isAfterLast();
        r3 = 0;
        if (r1 != 0) goto L_0x02bc;
    L_0x00ff:
        r1 = r11.getString(r8);
        r4 = 2;
        r5 = r11.getString(r4);
        r4 = r11.getString(r3);
        r3 = 3;
        r3 = r11.getString(r3);
        r8 = 4;
        r17 = r11.getString(r8);
        r8 = 5;
        r18 = r11.getString(r8);
        r8 = 6;
        r8 = r11.getInt(r8);
        r27 = r1;
        r1 = 1;
        if (r8 != r1) goto L_0x0127;
    L_0x0125:
        r8 = r1;
        goto L_0x0128;
    L_0x0127:
        r8 = 0;
    L_0x0128:
        r28 = r7;
        r7 = r37.getActivity();
        r29 = r12;
        r12 = r11.getString(r1);
        r12 = com.android.settings.Utils.getLocalizedName(r7, r12);
        r1 = android.text.TextUtils.isEmpty(r12);
        if (r1 != 0) goto L_0x0141;
    L_0x013e:
        r1 = r12;
        r7 = r1;
        goto L_0x0143;
    L_0x0141:
        r7 = r27;
    L_0x0143:
        r1 = 7;
        r1 = r11.getInt(r1);
        r30 = r12;
        r12 = 8;
        r19 = r11.getInt(r12);
        r12 = android.telephony.ServiceState.getBitmaskForTech(r1);
        r12 = r12 | r19;
        r31 = r13;
        r13 = android.telephony.TelephonyManager.getDefault();
        r13 = r13.getDataNetworkType(r15);
        r13 = r14.networkTypeToRilRidioTechnology(r13);
        r20 = android.telephony.ServiceState.bitmaskHasTech(r12, r13);
        if (r20 != 0) goto L_0x01a0;
    L_0x016a:
        if (r1 != 0) goto L_0x0174;
    L_0x016c:
        if (r19 == 0) goto L_0x016f;
    L_0x016e:
        goto L_0x0174;
    L_0x016f:
        r32 = r1;
        r33 = r12;
        goto L_0x01a4;
    L_0x0174:
        r20 = isVzwSim;
        if (r20 != 0) goto L_0x01a0;
    L_0x0178:
        if (r13 != 0) goto L_0x018f;
    L_0x017a:
        if (r1 != 0) goto L_0x0183;
    L_0x017c:
        if (r13 != 0) goto L_0x0183;
    L_0x017e:
        r32 = r1;
        r33 = r12;
        goto L_0x0193;
    L_0x0183:
        r32 = r1;
        r1 = "ApnSettings";
        r33 = r12;
        r12 = "Do not remove apn when it has bearer and in no service surrounding";
        android.util.Log.d(r1, r12);
        goto L_0x01a4;
    L_0x018f:
        r32 = r1;
        r33 = r12;
    L_0x0193:
        r11.moveToNext();
        r7 = r28;
        r12 = r29;
        r13 = r31;
        r8 = 1;
        goto L_0x00f8;
    L_0x01a0:
        r32 = r1;
        r33 = r12;
    L_0x01a4:
        r1 = new com.android.settings.network.ApnPreference;
        r12 = r37.getPrefContext();
        r1.<init>(r12);
        r12 = r1;
        r12.setKey(r4);
        r12.setTitle(r7);
        r12.setSummary(r5);
        r1 = 0;
        r12.setPersistent(r1);
        r12.setOnPreferenceChangeListener(r14);
        r12.setSubId(r15);
        if (r3 == 0) goto L_0x01cf;
    L_0x01c3:
        r1 = "default";
        r1 = r14.isApnType(r3, r1);
        if (r1 == 0) goto L_0x01cc;
    L_0x01cb:
        goto L_0x01cf;
    L_0x01cc:
        r25 = 0;
        goto L_0x01d1;
    L_0x01cf:
        r25 = 1;
    L_0x01d1:
        r1 = r25;
        r12.setSelectable(r1);
        r34 = r11;
        r11 = 1;
        if (r8 != r11) goto L_0x021c;
    L_0x01db:
        r20 = android.text.TextUtils.isEmpty(r5);
        if (r20 == 0) goto L_0x021c;
    L_0x01e1:
        r11 = "fota";
        r11 = r3.equals(r11);
        if (r11 == 0) goto L_0x021c;
    L_0x01e9:
        r11 = "ApnSettings";
        r35 = r3;
        r3 = new java.lang.StringBuilder;
        r3.<init>();
        r3.append(r7);
        r36 = r7;
        r7 = " apn is null";
        r3.append(r7);
        r3 = r3.toString();
        android.util.Log.d(r11, r3);
        r21 = r1;
        r23 = r4;
        r24 = r5;
        r3 = r8;
        r1 = r28;
        r26 = r29;
        r25 = r30;
        r30 = r31;
        r20 = r32;
        r28 = r33;
        r5 = r34;
        r22 = r35;
        goto L_0x027d;
    L_0x021c:
        r35 = r3;
        r36 = r7;
        if (r1 == 0) goto L_0x0285;
    L_0x0222:
        r3 = r14.mSelectedKey;
        if (r3 == 0) goto L_0x0253;
    L_0x0226:
        r3 = r14.mSelectedKey;
        r3 = r3.equals(r4);
        if (r3 == 0) goto L_0x0253;
    L_0x022e:
        r12.setChecked();
        r0 = 1;
        r3 = "ApnSettings";
        r7 = new java.lang.StringBuilder;
        r7.<init>();
        r11 = "find select key = ";
        r7.append(r11);
        r11 = r14.mSelectedKey;
        r7.append(r11);
        r11 = " apn: ";
        r7.append(r11);
        r7.append(r5);
        r7 = r7.toString();
        android.util.Log.d(r3, r7);
        goto L_0x0256;
    L_0x0253:
        r12.unsetChecked();
    L_0x0256:
        r7 = r0;
        r0 = r14;
        r21 = r1;
        r20 = r32;
        r1 = r12;
        r22 = r35;
        r3 = r6;
        r23 = r4;
        r4 = r16;
        r24 = r5;
        r5 = r17;
        r11 = r6;
        r6 = r18;
        r0.addApnToList(r1, r2, r3, r4, r5, r6);
        r0 = r7;
        r3 = r8;
        r6 = r11;
        r1 = r28;
        r26 = r29;
        r25 = r30;
        r30 = r31;
        r28 = r33;
        r5 = r34;
    L_0x027d:
        r27 = r36;
        r4 = 1;
        r29 = r12;
        r31 = r13;
        goto L_0x02af;
    L_0x0285:
        r21 = r1;
        r23 = r4;
        r24 = r5;
        r11 = r6;
        r20 = r32;
        r22 = r35;
        r1 = r28;
        r27 = r36;
        r7 = r14;
        r3 = r8;
        r4 = 1;
        r8 = r12;
        r5 = r34;
        r11 = r16;
        r26 = r29;
        r25 = r30;
        r28 = r33;
        r29 = r12;
        r12 = r17;
        r30 = r31;
        r31 = r13;
        r13 = r18;
        r7.addApnToList(r8, r9, r10, r11, r12, r13);
    L_0x02af:
        r5.moveToNext();
        r7 = r1;
        r8 = r4;
        r11 = r5;
        r12 = r26;
        r13 = r30;
        goto L_0x00f8;
    L_0x02bc:
        r1 = r7;
        r5 = r11;
        r26 = r12;
        r30 = r13;
        r5.close();
        r3 = r6.isEmpty();
        if (r3 != 0) goto L_0x02ce;
    L_0x02cb:
        r2 = r6;
        r3 = r10;
        goto L_0x02cf;
    L_0x02ce:
        r3 = r9;
    L_0x02cf:
        r4 = r2.iterator();
    L_0x02d3:
        r7 = r4.hasNext();
        if (r7 == 0) goto L_0x02e3;
    L_0x02d9:
        r7 = r4.next();
        r7 = (android.support.v7.preference.Preference) r7;
        r1.addPreference(r7);
        goto L_0x02d3;
    L_0x02e3:
        if (r0 != 0) goto L_0x0316;
    L_0x02e5:
        r4 = r1.getPreferenceCount();
        if (r4 <= 0) goto L_0x0316;
    L_0x02eb:
        r4 = 0;
        r4 = r1.getPreference(r4);
        r4 = (com.android.settings.network.ApnPreference) r4;
        r4.setChecked();
        r7 = r4.getKey();
        r14.setSelectedApnKey(r7);
        r7 = "ApnSettings";
        r8 = new java.lang.StringBuilder;
        r8.<init>();
        r9 = "set key to  ";
        r8.append(r9);
        r9 = r4.getKey();
        r8.append(r9);
        r8 = r8.toString();
        android.util.Log.d(r7, r8);
    L_0x0316:
        r4 = r3.iterator();
    L_0x031a:
        r7 = r4.hasNext();
        if (r7 == 0) goto L_0x032f;
    L_0x0320:
        r7 = r4.next();
        r7 = (android.support.v7.preference.Preference) r7;
        r1.addPreference(r7);
        goto L_0x031a;
    L_0x032a:
        r5 = r11;
        r26 = r12;
        r30 = r13;
    L_0x032f:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settings.network.ApnSettings.fillList():void");
    }

    private boolean isApnType(String types, String requestType) {
        if (TextUtils.isEmpty(types)) {
            return true;
        }
        for (String type : types.split(",")) {
            String type2 = type2.trim();
            if (type2.equals(requestType) || type2.equals("*")) {
                return true;
            }
        }
        return false;
    }

    private void addApnToList(ApnPreference pref, ArrayList<ApnPreference> mnoList, ArrayList<ApnPreference> mvnoList, IccRecords r, String mvnoType, String mvnoMatchData) {
        if (r == null || TextUtils.isEmpty(mvnoType) || TextUtils.isEmpty(mvnoMatchData)) {
            mnoList.add(pref);
        } else if (ApnSetting.mvnoMatches(r, mvnoType, mvnoMatchData)) {
            mvnoList.add(pref);
            this.mMvnoType = mvnoType;
            this.mMvnoMatchData = mvnoMatchData;
        }
    }

    private void appendFilter(StringBuilder where) {
        String key;
        boolean includeCommon = true;
        boolean z = true;
        if (!(this.mHideApnsGroupByIccid == null || this.mHideApnsGroupByIccid.isEmpty())) {
            includeCommon = this.mHideApnsGroupByIccid.getBoolean(INCLUDE_COMMON_RULES, true);
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("apn hidden rules specified iccid, include common rule: ");
            stringBuilder.append(includeCommon);
            Log.d(str, stringBuilder.toString());
            for (String key2 : this.mHideApnsGroupByIccid.keySet()) {
                if (Utils.carrierTableFieldValidate(key2)) {
                    String value = this.mHideApnsGroupByIccid.getString(key2);
                    if (value != null) {
                        StringBuilder stringBuilder2 = new StringBuilder();
                        stringBuilder2.append(" AND ");
                        stringBuilder2.append(key2);
                        stringBuilder2.append(" <> \"");
                        stringBuilder2.append(value);
                        stringBuilder2.append("\"");
                        where.append(stringBuilder2.toString());
                    }
                }
            }
        }
        if (this.mHideApnsWithIccidRule != null) {
            HashMap<String, String> ruleWithIccid = getApnRuleMap(this.mHideApnsWithIccidRule);
            if (isOperatorIccid(ruleWithIccid, this.mSubscriptionInfo == null ? "" : this.mSubscriptionInfo.getIccId())) {
                key2 = (String) ruleWithIccid.get(INCLUDE_COMMON_RULES);
                if (key2 != null && key2.equalsIgnoreCase(String.valueOf(false))) {
                    z = false;
                }
                includeCommon = z;
                String str2 = TAG;
                StringBuilder stringBuilder3 = new StringBuilder();
                stringBuilder3.append("apn hidden rules in iccids, include common rule: ");
                stringBuilder3.append(includeCommon);
                Log.d(str2, stringBuilder3.toString());
                filterWithKey(ruleWithIccid, where);
            }
        }
        if (includeCommon && this.mHideApnsWithRule != null) {
            filterWithKey(getApnRuleMap(this.mHideApnsWithRule), where);
        }
    }

    private void filterWithKey(Map<String, String> rules, StringBuilder where) {
        for (String field : rules.keySet()) {
            if (Utils.carrierTableFieldValidate(field)) {
                String value = (String) rules.get(field);
                if (!TextUtils.isEmpty(value)) {
                    for (String subValue : value.split(",")) {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(" AND ");
                        stringBuilder.append(field);
                        stringBuilder.append(" <> \"");
                        stringBuilder.append(subValue);
                        stringBuilder.append("\"");
                        where.append(stringBuilder.toString());
                    }
                }
            }
        }
    }

    private HashMap<String, String> getApnRuleMap(String[] ruleArray) {
        HashMap<String, String> rules = new HashMap();
        if (ruleArray != null) {
            int length = ruleArray.length;
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("ruleArray size = ");
            stringBuilder.append(length);
            Log.d(str, stringBuilder.toString());
            if (length > 0 && length % 2 == 0) {
                for (int i = 0; i < length; i += 2) {
                    rules.put(ruleArray[i].toLowerCase(), ruleArray[i + 1]);
                }
            }
        }
        return rules;
    }

    private boolean isOperatorIccid(HashMap<String, String> ruleMap, String iccid) {
        String valuesOfIccid = (String) ruleMap.get("iccid");
        if (!TextUtils.isEmpty(valuesOfIccid)) {
            for (String subIccid : valuesOfIccid.split(",")) {
                if (iccid.startsWith(subIccid.trim())) {
                    return true;
                }
            }
        }
        return false;
    }

    private int networkTypeToRilRidioTechnology(int nt) {
        switch (nt) {
            case 1:
                return 1;
            case 2:
                return 2;
            case 3:
                return 3;
            case 4:
                return 5;
            case 5:
                return 7;
            case 6:
                return 8;
            case 7:
                return 6;
            case 8:
                return 9;
            case 9:
                return 10;
            case 10:
                return 11;
            case 12:
                return 12;
            case 13:
                return 14;
            case 14:
                return 13;
            case 15:
                return 15;
            case 16:
                return 16;
            case 17:
                return 17;
            case 18:
                return 18;
            case 19:
                return 19;
            default:
                return 0;
        }
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (!this.mUnavailable) {
            if (this.mAllowAddingApns) {
                menu.add(0, 1, 0, getResources().getString(R.string.menu_new)).setIcon(R.drawable.ic_menu_add_white).setShowAsAction(1);
            }
            menu.add(0, 2, 0, getResources().getString(R.string.menu_restore)).setIcon(17301589);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                addNewApn();
                return true;
            case 2:
                restoreDefaultApn();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addNewApn() {
        int subId;
        Intent intent = new Intent("android.intent.action.INSERT", Carriers.CONTENT_URI);
        if (this.mSubscriptionInfo != null) {
            subId = this.mSubscriptionInfo.getSubscriptionId();
        } else {
            subId = -1;
        }
        intent.putExtra("sub_id", subId);
        if (!(TextUtils.isEmpty(this.mMvnoType) || TextUtils.isEmpty(this.mMvnoMatchData))) {
            intent.putExtra(MVNO_TYPE, this.mMvnoType);
            intent.putExtra(MVNO_MATCH_DATA, this.mMvnoMatchData);
        }
        startActivity(intent);
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        try {
            int subId;
            String str = "android.intent.action.EDIT";
            Intent intent = new Intent(str, ContentUris.withAppendedId(Carriers.CONTENT_URI, (long) Integer.parseInt(preference.getKey())));
            if (this.mSubscriptionInfo != null) {
                subId = this.mSubscriptionInfo.getSubscriptionId();
            } else {
                subId = -1;
            }
            String str2 = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("subId: ");
            stringBuilder.append(subId);
            Log.d(str2, stringBuilder.toString());
            intent.putExtra("sub_id", subId);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("onPreferenceChange(): Preference - ");
        stringBuilder.append(preference);
        stringBuilder.append(", newValue - ");
        stringBuilder.append(newValue);
        stringBuilder.append(", newValue type - ");
        stringBuilder.append(newValue.getClass());
        Log.d(str, stringBuilder.toString());
        if (newValue instanceof String) {
            setSelectedApnKey((String) newValue);
        }
        return true;
    }

    private void setSelectedApnKey(String key) {
        this.mSelectedKey = key;
        ContentResolver resolver = getContentResolver();
        ContentValues values = new ContentValues();
        values.put(APN_ID, this.mSelectedKey);
        resolver.update(getUriForCurrSubId(PREFERAPN_URI), values, null, null);
    }

    private String getSelectedApnKey() {
        String key = null;
        Cursor cursor = getContentResolver().query(getUriForCurrSubId(PREFERAPN_URI), new String[]{OPFirewallUtils._ID}, null, null, "name ASC");
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            key = cursor.getString(0);
        }
        cursor.close();
        return key;
    }

    private boolean restoreDefaultApn() {
        showDialog(1001);
        mRestoreDefaultApnMode = true;
        if (this.mRestoreApnUiHandler == null) {
            this.mRestoreApnUiHandler = new RestoreApnUiHandler(this, null);
        }
        if (this.mRestoreApnProcessHandler == null || this.mRestoreDefaultApnThread == null) {
            this.mRestoreDefaultApnThread = new HandlerThread("Restore default APN Handler: Process Thread");
            this.mRestoreDefaultApnThread.start();
            this.mRestoreApnProcessHandler = new RestoreApnProcessHandler(this.mRestoreDefaultApnThread.getLooper(), this.mRestoreApnUiHandler);
        }
        this.mRestoreApnProcessHandler.sendEmptyMessage(1);
        return true;
    }

    private Uri getUriForCurrSubId(Uri uri) {
        int subId;
        if (this.mSubscriptionInfo != null) {
            subId = this.mSubscriptionInfo.getSubscriptionId();
        } else {
            subId = -1;
        }
        if (!SubscriptionManager.isValidSubscriptionId(subId)) {
            return uri;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("subId/");
        stringBuilder.append(String.valueOf(subId));
        return Uri.withAppendedPath(uri, stringBuilder.toString());
    }

    public Dialog onCreateDialog(int id) {
        if (id != 1001) {
            return null;
        }
        ProgressDialog dialog = new ProgressDialog(getActivity()) {
            public boolean onTouchEvent(MotionEvent event) {
                return true;
            }
        };
        dialog.setMessage(getResources().getString(R.string.restore_default_apn));
        dialog.setCancelable(false);
        dialog.setOnKeyListener(this.keylistener);
        return dialog;
    }

    public int getDialogMetricsCategory(int dialogId) {
        if (dialogId == 1001) {
            return 579;
        }
        return 0;
    }
}
