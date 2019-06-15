package com.android.settings.network;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.Telephony.Carriers;
import android.support.v14.preference.MultiSelectListPreference;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.telephony.CarrierConfigManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.util.ArrayUtils;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;
import com.android.settings.password.ChooseLockSettingsHelper;
import com.android.settings.widget.ValidatedEditTextPreference;
import com.android.settingslib.datetime.ZoneGetter;
import com.android.settingslib.utils.ThreadUtils;
import com.oneplus.settings.utils.OPFirewallUtils;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ApnEditor extends SettingsPreferenceFragment implements OnPreferenceChangeListener, OnKeyListener {
    private static final String APN_DEFALUT_VALUES_STRING_ARRAY = "apn_default_values_strings_array";
    @VisibleForTesting
    static final int APN_INDEX = 2;
    private static final int AUTH_TYPE_INDEX = 14;
    private static final int BEARER_BITMASK_INDEX = 19;
    private static final int BEARER_INDEX = 18;
    @VisibleForTesting
    static final int CARRIER_ENABLED_INDEX = 17;
    private static final int EDITED_INDEX = 23;
    private static final int ID_INDEX = 0;
    private static final String KEY_AUTH_TYPE = "auth_type";
    private static final String KEY_BEARER_MULTI = "bearer_multi";
    private static final String KEY_CARRIER_ENABLED = "carrier_enabled";
    private static final String KEY_MVNO_TYPE = "mvno_type";
    private static final String KEY_PASSWORD = "apn_password";
    private static final String KEY_PROTOCOL = "apn_protocol";
    private static final String KEY_ROAMING_PROTOCOL = "apn_roaming_protocol";
    @VisibleForTesting
    static final int MCC_INDEX = 9;
    private static final int MENU_CANCEL = 3;
    private static final int MENU_DELETE = 1;
    private static final int MENU_SAVE = 2;
    private static final int MMSC_INDEX = 8;
    private static final int MMSPORT_INDEX = 13;
    private static final int MMSPROXY_INDEX = 12;
    @VisibleForTesting
    static final int MNC_INDEX = 10;
    private static final int MVNO_MATCH_DATA_INDEX = 22;
    private static final int MVNO_TYPE_INDEX = 21;
    @VisibleForTesting
    static final int NAME_INDEX = 1;
    private static final int PASSWORD_INDEX = 7;
    private static final int PERSISTENT_INDEX = 25;
    private static final int PORT_INDEX = 4;
    private static final int PROTOCOL_INDEX = 16;
    private static final int PROXY_INDEX = 3;
    private static final int READONLY_INDEX = 26;
    private static final int ROAMING_PROTOCOL_INDEX = 20;
    private static final int SERVER_INDEX = 6;
    private static final String TAG = ApnEditor.class.getSimpleName();
    private static final int TYPE_INDEX = 15;
    private static final int USER_EDITABLE_INDEX = 24;
    private static final int USER_INDEX = 5;
    private static final boolean VDBG = false;
    @VisibleForTesting
    static String sNotSet;
    private static final String[] sProjection = new String[]{OPFirewallUtils._ID, ZoneGetter.KEY_DISPLAYNAME, "apn", "proxy", "port", "user", "server", ChooseLockSettingsHelper.EXTRA_KEY_PASSWORD, "mmsc", "mcc", "mnc", "numeric", "mmsproxy", "mmsport", "authtype", "type", "protocol", KEY_CARRIER_ENABLED, "bearer", "bearer_bitmask", "roaming_protocol", "mvno_type", ApnSettings.MVNO_MATCH_DATA, "edited", "user_editable", Utils.PERSISTENT, Utils.READ_ONLY};
    private static final String[] sUIConfigurableItems = new String[]{ZoneGetter.KEY_DISPLAYNAME, "apn", "proxy", "port", "user", "server", ChooseLockSettingsHelper.EXTRA_KEY_PASSWORD, "mmsc", "mmsproxy", "mmsport", "authtype", "type", "protocol", KEY_CARRIER_ENABLED, "bearer", "bearer_bitmask", "roaming_protocol"};
    ValidatedEditTextPreference mApn;
    @VisibleForTesting
    ApnData mApnData;
    ValidatedEditTextPreference mApnType;
    ListPreference mAuthType;
    private int mBearerInitialVal = 0;
    MultiSelectListPreference mBearerMulti;
    SwitchPreference mCarrierEnabled;
    private Uri mCarrierUri;
    private String mCurMcc;
    private String mCurMnc;
    private boolean mDeletableApn;
    ValidatedEditTextPreference mMcc;
    ValidatedEditTextPreference mMmsPort;
    ValidatedEditTextPreference mMmsProxy;
    ValidatedEditTextPreference mMmsc;
    ValidatedEditTextPreference mMnc;
    ValidatedEditTextPreference mMvnoMatchData;
    private String mMvnoMatchDataStr;
    ListPreference mMvnoType;
    private String mMvnoTypeStr;
    ValidatedEditTextPreference mName;
    private boolean mNewApn;
    ValidatedEditTextPreference mPassword;
    ValidatedEditTextPreference mPort;
    ListPreference mProtocol;
    ValidatedEditTextPreference mProxy;
    private boolean mReadOnlyApn;
    private String[] mReadOnlyApnFields;
    private String[] mReadOnlyApnTypes;
    ListPreference mRoamingProtocol;
    ValidatedEditTextPreference mServer;
    private int mSubId;
    private TelephonyManager mTelephonyManager;
    ValidatedEditTextPreference mUser;

    @VisibleForTesting
    static class ApnData {
        Object[] mData;
        Uri mUri;

        ApnData(int numberOfField) {
            this.mData = new Object[numberOfField];
        }

        ApnData(Uri uri, Cursor cursor) {
            this.mUri = uri;
            this.mData = new Object[cursor.getColumnCount()];
            for (int i = 0; i < this.mData.length; i++) {
                switch (cursor.getType(i)) {
                    case 1:
                        this.mData[i] = Integer.valueOf(cursor.getInt(i));
                        break;
                    case 2:
                        this.mData[i] = Float.valueOf(cursor.getFloat(i));
                        break;
                    case 3:
                        this.mData[i] = cursor.getString(i);
                        break;
                    case 4:
                        this.mData[i] = cursor.getBlob(i);
                        break;
                    default:
                        this.mData[i] = null;
                        break;
                }
            }
        }

        /* Access modifiers changed, original: 0000 */
        public Uri getUri() {
            return this.mUri;
        }

        /* Access modifiers changed, original: 0000 */
        public void setUri(Uri uri) {
            this.mUri = uri;
        }

        /* Access modifiers changed, original: 0000 */
        public Integer getInteger(int index) {
            return (Integer) this.mData[index];
        }

        /* Access modifiers changed, original: 0000 */
        public Integer getInteger(int index, Integer defaultValue) {
            Integer val = getInteger(index);
            return val == null ? defaultValue : val;
        }

        /* Access modifiers changed, original: 0000 */
        public String getString(int index) {
            return (String) this.mData[index];
        }

        /* Access modifiers changed, original: 0000 */
        public void setObject(int index, Object value) {
            this.mData[index] = value;
        }
    }

    public static class ErrorDialog extends InstrumentedDialogFragment {
        public static void showError(ApnEditor editor) {
            ErrorDialog dialog = new ErrorDialog();
            dialog.setTargetFragment(editor, 0);
            dialog.show(editor.getFragmentManager(), "error");
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new Builder(getContext()).setTitle(R.string.error_title).setPositiveButton(17039370, null).setMessage(((ApnEditor) getTargetFragment()).validateApnData()).create();
        }

        public int getMetricsCategory() {
            return 530;
        }
    }

    public void onCreate(Bundle icicle) {
        int i;
        boolean z = false;
        getActivity().getWindow().getDecorView().setSystemUiVisibility(0);
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.apn_editor);
        sNotSet = getResources().getString(R.string.apn_not_set);
        this.mName = (ValidatedEditTextPreference) findPreference("apn_name");
        this.mApn = (ValidatedEditTextPreference) findPreference("apn_apn");
        this.mProxy = (ValidatedEditTextPreference) findPreference("apn_http_proxy");
        this.mPort = (ValidatedEditTextPreference) findPreference("apn_http_port");
        this.mUser = (ValidatedEditTextPreference) findPreference("apn_user");
        this.mServer = (ValidatedEditTextPreference) findPreference("apn_server");
        this.mPassword = (ValidatedEditTextPreference) findPreference(KEY_PASSWORD);
        this.mMmsProxy = (ValidatedEditTextPreference) findPreference("apn_mms_proxy");
        this.mMmsPort = (ValidatedEditTextPreference) findPreference("apn_mms_port");
        this.mMmsc = (ValidatedEditTextPreference) findPreference("apn_mmsc");
        this.mMcc = (ValidatedEditTextPreference) findPreference("apn_mcc");
        this.mMnc = (ValidatedEditTextPreference) findPreference("apn_mnc");
        this.mApnType = (ValidatedEditTextPreference) findPreference("apn_type");
        this.mAuthType = (ListPreference) findPreference(KEY_AUTH_TYPE);
        this.mProtocol = (ListPreference) findPreference(KEY_PROTOCOL);
        this.mRoamingProtocol = (ListPreference) findPreference(KEY_ROAMING_PROTOCOL);
        this.mCarrierEnabled = (SwitchPreference) findPreference(KEY_CARRIER_ENABLED);
        this.mBearerMulti = (MultiSelectListPreference) findPreference(KEY_BEARER_MULTI);
        this.mBearerMulti.setPositiveButtonText((int) R.string.dlg_ok);
        this.mBearerMulti.setNegativeButtonText((int) R.string.dlg_cancel);
        this.mMvnoType = (ListPreference) findPreference("mvno_type");
        this.mMvnoMatchData = (ValidatedEditTextPreference) findPreference(ApnSettings.MVNO_MATCH_DATA);
        Intent intent = getIntent();
        String action = intent.getAction();
        this.mSubId = intent.getIntExtra("sub_id", -1);
        this.mReadOnlyApn = false;
        this.mReadOnlyApnTypes = null;
        this.mReadOnlyApnFields = null;
        CarrierConfigManager configManager = (CarrierConfigManager) getSystemService("carrier_config");
        if (configManager != null) {
            PersistableBundle b = configManager.getConfigForSubId(this.mSubId);
            if (b != null) {
                this.mReadOnlyApnTypes = b.getStringArray("read_only_apn_types_string_array");
                if (!ArrayUtils.isEmpty(this.mReadOnlyApnTypes)) {
                    for (String apnType : this.mReadOnlyApnTypes) {
                        String str = TAG;
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("onCreate: read only APN type: ");
                        stringBuilder.append(apnType);
                        Log.d(str, stringBuilder.toString());
                    }
                }
                this.mReadOnlyApnFields = b.getStringArray("read_only_apn_fields_string_array");
            }
        }
        Uri uri = null;
        String str2;
        StringBuilder stringBuilder2;
        if (action.equals("android.intent.action.EDIT")) {
            uri = intent.getData();
            if (!uri.isPathPrefixMatch(Carriers.CONTENT_URI)) {
                str2 = TAG;
                stringBuilder2 = new StringBuilder();
                stringBuilder2.append("Edit request not for carrier table. Uri: ");
                stringBuilder2.append(uri);
                Log.e(str2, stringBuilder2.toString());
                finish();
                return;
            }
        } else if (action.equals("android.intent.action.INSERT")) {
            this.mCarrierUri = intent.getData();
            if (this.mCarrierUri.isPathPrefixMatch(Carriers.CONTENT_URI)) {
                this.mNewApn = true;
                this.mMvnoTypeStr = intent.getStringExtra("mvno_type");
                this.mMvnoMatchDataStr = intent.getStringExtra(ApnSettings.MVNO_MATCH_DATA);
            } else {
                str2 = TAG;
                stringBuilder2 = new StringBuilder();
                stringBuilder2.append("Insert request not for carrier table. Uri: ");
                stringBuilder2.append(this.mCarrierUri);
                Log.e(str2, stringBuilder2.toString());
                finish();
                return;
            }
        } else {
            finish();
            return;
        }
        if (uri != null) {
            this.mApnData = getApnDataFromUri(uri);
        } else {
            this.mApnData = new ApnData(sProjection.length);
            if (action.equals("android.intent.action.INSERT")) {
                setDefaultData();
            }
        }
        if (this.mApnData == null) {
            Log.d(TAG, "mApnData was null return");
            finish();
            return;
        }
        this.mTelephonyManager = (TelephonyManager) getSystemService("phone");
        boolean isUserEdited = this.mApnData.getInteger(23, Integer.valueOf(1)).intValue() == 1;
        String str3 = TAG;
        StringBuilder stringBuilder3 = new StringBuilder();
        stringBuilder3.append("onCreate: EDITED ");
        stringBuilder3.append(isUserEdited);
        Log.d(str3, stringBuilder3.toString());
        if (!isUserEdited && (this.mApnData.getInteger(24, Integer.valueOf(1)).intValue() == 0 || apnTypesMatch(this.mReadOnlyApnTypes, this.mApnData.getString(15)) || this.mApnData.getInteger(26).intValue() == 1)) {
            Log.d(TAG, "onCreate: apnTypesMatch; read-only APN");
            this.mReadOnlyApn = true;
            disableAllFields();
        } else if (!ArrayUtils.isEmpty(this.mReadOnlyApnFields)) {
            disableFields(this.mReadOnlyApnFields);
        }
        this.mDeletableApn = this.mApnData.getInteger(25, Integer.valueOf(0)).intValue() != 1;
        for (i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
            getPreferenceScreen().getPreference(i).setOnPreferenceChangeListener(this);
        }
        if (icicle == null) {
            z = true;
        }
        fillUI(z);
    }

    @VisibleForTesting
    static String formatInteger(String value) {
        try {
            return String.format("%d", new Object[]{Integer.valueOf(Integer.parseInt(value))});
        } catch (NumberFormatException e) {
            return value;
        }
    }

    static boolean hasAllApns(String[] apnTypes) {
        if (ArrayUtils.isEmpty(apnTypes)) {
            return false;
        }
        List apnList = Arrays.asList(apnTypes);
        if (apnList.contains("*")) {
            Log.d(TAG, "hasAllApns: true because apnList.contains(PhoneConstants.APN_TYPE_ALL)");
            return true;
        }
        for (String apn : PhoneConstants.APN_TYPES) {
            if (!apnList.contains(apn)) {
                return false;
            }
        }
        Log.d(TAG, "hasAllApns: true");
        return true;
    }

    private boolean apnTypesMatch(String[] apnTypesArray1, String apnTypes2) {
        if (ArrayUtils.isEmpty(apnTypesArray1)) {
            return false;
        }
        if (hasAllApns(apnTypesArray1) || TextUtils.isEmpty(apnTypes2)) {
            return true;
        }
        List apnTypesList1 = Arrays.asList(apnTypesArray1);
        for (String apn : apnTypes2.split(",")) {
            if (apnTypesList1.contains(apn.trim())) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("apnTypesMatch: true because match found for ");
                stringBuilder.append(apn.trim());
                Log.d(str, stringBuilder.toString());
                return true;
            }
        }
        Log.d(TAG, "apnTypesMatch: false");
        return false;
    }

    private android.support.v7.preference.Preference getPreferenceFromFieldName(java.lang.String r2) {
        /*
        r1 = this;
        r0 = r2.hashCode();
        switch(r0) {
            case -2135515857: goto L_0x00e9;
            case -1954254981: goto L_0x00df;
            case -1640523526: goto L_0x00d4;
            case -1393032351: goto L_0x00c9;
            case -1230508389: goto L_0x00be;
            case -1039601666: goto L_0x00b3;
            case -989163880: goto L_0x00a8;
            case -905826493: goto L_0x009e;
            case -520149991: goto L_0x0093;
            case 96799: goto L_0x0088;
            case 107917: goto L_0x007c;
            case 108258: goto L_0x0070;
            case 3355632: goto L_0x0064;
            case 3373707: goto L_0x0059;
            case 3446913: goto L_0x004e;
            case 3575610: goto L_0x0042;
            case 3599307: goto L_0x0037;
            case 106941038: goto L_0x002c;
            case 1183882708: goto L_0x0020;
            case 1216985755: goto L_0x0015;
            case 1433229538: goto L_0x0009;
            default: goto L_0x0007;
        };
    L_0x0007:
        goto L_0x00f4;
    L_0x0009:
        r0 = "authtype";
        r0 = r2.equals(r0);
        if (r0 == 0) goto L_0x00f4;
    L_0x0011:
        r0 = 13;
        goto L_0x00f5;
    L_0x0015:
        r0 = "password";
        r0 = r2.equals(r0);
        if (r0 == 0) goto L_0x00f4;
    L_0x001d:
        r0 = 6;
        goto L_0x00f5;
    L_0x0020:
        r0 = "mmsport";
        r0 = r2.equals(r0);
        if (r0 == 0) goto L_0x00f4;
    L_0x0028:
        r0 = 8;
        goto L_0x00f5;
    L_0x002c:
        r0 = "proxy";
        r0 = r2.equals(r0);
        if (r0 == 0) goto L_0x00f4;
    L_0x0034:
        r0 = 2;
        goto L_0x00f5;
    L_0x0037:
        r0 = "user";
        r0 = r2.equals(r0);
        if (r0 == 0) goto L_0x00f4;
    L_0x003f:
        r0 = 4;
        goto L_0x00f5;
    L_0x0042:
        r0 = "type";
        r0 = r2.equals(r0);
        if (r0 == 0) goto L_0x00f4;
    L_0x004a:
        r0 = 12;
        goto L_0x00f5;
    L_0x004e:
        r0 = "port";
        r0 = r2.equals(r0);
        if (r0 == 0) goto L_0x00f4;
    L_0x0056:
        r0 = 3;
        goto L_0x00f5;
    L_0x0059:
        r0 = "name";
        r0 = r2.equals(r0);
        if (r0 == 0) goto L_0x00f4;
    L_0x0061:
        r0 = 0;
        goto L_0x00f5;
    L_0x0064:
        r0 = "mmsc";
        r0 = r2.equals(r0);
        if (r0 == 0) goto L_0x00f4;
    L_0x006c:
        r0 = 9;
        goto L_0x00f5;
    L_0x0070:
        r0 = "mnc";
        r0 = r2.equals(r0);
        if (r0 == 0) goto L_0x00f4;
    L_0x0078:
        r0 = 11;
        goto L_0x00f5;
    L_0x007c:
        r0 = "mcc";
        r0 = r2.equals(r0);
        if (r0 == 0) goto L_0x00f4;
    L_0x0084:
        r0 = 10;
        goto L_0x00f5;
    L_0x0088:
        r0 = "apn";
        r0 = r2.equals(r0);
        if (r0 == 0) goto L_0x00f4;
    L_0x0090:
        r0 = 1;
        goto L_0x00f5;
    L_0x0093:
        r0 = "mvno_match_data";
        r0 = r2.equals(r0);
        if (r0 == 0) goto L_0x00f4;
    L_0x009b:
        r0 = 20;
        goto L_0x00f5;
    L_0x009e:
        r0 = "server";
        r0 = r2.equals(r0);
        if (r0 == 0) goto L_0x00f4;
    L_0x00a6:
        r0 = 5;
        goto L_0x00f5;
    L_0x00a8:
        r0 = "protocol";
        r0 = r2.equals(r0);
        if (r0 == 0) goto L_0x00f4;
    L_0x00b0:
        r0 = 14;
        goto L_0x00f5;
    L_0x00b3:
        r0 = "roaming_protocol";
        r0 = r2.equals(r0);
        if (r0 == 0) goto L_0x00f4;
    L_0x00bb:
        r0 = 15;
        goto L_0x00f5;
    L_0x00be:
        r0 = "bearer_bitmask";
        r0 = r2.equals(r0);
        if (r0 == 0) goto L_0x00f4;
    L_0x00c6:
        r0 = 18;
        goto L_0x00f5;
    L_0x00c9:
        r0 = "bearer";
        r0 = r2.equals(r0);
        if (r0 == 0) goto L_0x00f4;
    L_0x00d1:
        r0 = 17;
        goto L_0x00f5;
    L_0x00d4:
        r0 = "carrier_enabled";
        r0 = r2.equals(r0);
        if (r0 == 0) goto L_0x00f4;
    L_0x00dc:
        r0 = 16;
        goto L_0x00f5;
    L_0x00df:
        r0 = "mmsproxy";
        r0 = r2.equals(r0);
        if (r0 == 0) goto L_0x00f4;
    L_0x00e7:
        r0 = 7;
        goto L_0x00f5;
    L_0x00e9:
        r0 = "mvno_type";
        r0 = r2.equals(r0);
        if (r0 == 0) goto L_0x00f4;
    L_0x00f1:
        r0 = 19;
        goto L_0x00f5;
    L_0x00f4:
        r0 = -1;
    L_0x00f5:
        switch(r0) {
            case 0: goto L_0x0133;
            case 1: goto L_0x0130;
            case 2: goto L_0x012d;
            case 3: goto L_0x012a;
            case 4: goto L_0x0127;
            case 5: goto L_0x0124;
            case 6: goto L_0x0121;
            case 7: goto L_0x011e;
            case 8: goto L_0x011b;
            case 9: goto L_0x0118;
            case 10: goto L_0x0115;
            case 11: goto L_0x0112;
            case 12: goto L_0x010f;
            case 13: goto L_0x010c;
            case 14: goto L_0x0109;
            case 15: goto L_0x0106;
            case 16: goto L_0x0103;
            case 17: goto L_0x0100;
            case 18: goto L_0x0100;
            case 19: goto L_0x00fd;
            case 20: goto L_0x00fa;
            default: goto L_0x00f8;
        };
    L_0x00f8:
        r0 = 0;
        return r0;
    L_0x00fa:
        r0 = r1.mMvnoMatchData;
        return r0;
    L_0x00fd:
        r0 = r1.mMvnoType;
        return r0;
    L_0x0100:
        r0 = r1.mBearerMulti;
        return r0;
    L_0x0103:
        r0 = r1.mCarrierEnabled;
        return r0;
    L_0x0106:
        r0 = r1.mRoamingProtocol;
        return r0;
    L_0x0109:
        r0 = r1.mProtocol;
        return r0;
    L_0x010c:
        r0 = r1.mAuthType;
        return r0;
    L_0x010f:
        r0 = r1.mApnType;
        return r0;
    L_0x0112:
        r0 = r1.mMnc;
        return r0;
    L_0x0115:
        r0 = r1.mMcc;
        return r0;
    L_0x0118:
        r0 = r1.mMmsc;
        return r0;
    L_0x011b:
        r0 = r1.mMmsPort;
        return r0;
    L_0x011e:
        r0 = r1.mMmsProxy;
        return r0;
    L_0x0121:
        r0 = r1.mPassword;
        return r0;
    L_0x0124:
        r0 = r1.mServer;
        return r0;
    L_0x0127:
        r0 = r1.mUser;
        return r0;
    L_0x012a:
        r0 = r1.mPort;
        return r0;
    L_0x012d:
        r0 = r1.mProxy;
        return r0;
    L_0x0130:
        r0 = r1.mApn;
        return r0;
    L_0x0133:
        r0 = r1.mName;
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settings.network.ApnEditor.getPreferenceFromFieldName(java.lang.String):android.support.v7.preference.Preference");
    }

    private void disableFields(String[] apnFields) {
        for (String apnField : apnFields) {
            Preference preference = getPreferenceFromFieldName(apnField);
            if (preference != null) {
                preference.setEnabled(false);
            }
        }
    }

    private void disableAllFields() {
        this.mName.setEnabled(false);
        this.mApn.setEnabled(false);
        this.mProxy.setEnabled(false);
        this.mPort.setEnabled(false);
        this.mUser.setEnabled(false);
        this.mServer.setEnabled(false);
        this.mPassword.setEnabled(false);
        this.mMmsProxy.setEnabled(false);
        this.mMmsPort.setEnabled(false);
        this.mMmsc.setEnabled(false);
        this.mMcc.setEnabled(false);
        this.mMnc.setEnabled(false);
        this.mApnType.setEnabled(false);
        this.mAuthType.setEnabled(false);
        this.mProtocol.setEnabled(false);
        this.mRoamingProtocol.setEnabled(false);
        this.mCarrierEnabled.setEnabled(false);
        this.mBearerMulti.setEnabled(false);
        this.mMvnoType.setEnabled(false);
        this.mMvnoMatchData.setEnabled(false);
    }

    public int getMetricsCategory() {
        return 13;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void fillUI(boolean firstTime) {
        String numeric;
        if (firstTime) {
            this.mName.setText(this.mApnData.getString(1));
            this.mApn.setText(this.mApnData.getString(2));
            this.mProxy.setText(this.mApnData.getString(3));
            this.mPort.setText(this.mApnData.getString(4));
            this.mUser.setText(this.mApnData.getString(5));
            this.mServer.setText(this.mApnData.getString(6));
            this.mPassword.setText(this.mApnData.getString(7));
            this.mMmsProxy.setText(this.mApnData.getString(12));
            this.mMmsPort.setText(this.mApnData.getString(13));
            this.mMmsc.setText(this.mApnData.getString(8));
            this.mMcc.setText(this.mApnData.getString(9));
            this.mMnc.setText(this.mApnData.getString(10));
            this.mApnType.setText(this.mApnData.getString(15));
            if (this.mNewApn) {
                numeric = PhoneFactory.getPhone(SubscriptionManager.getPhoneId(this.mSubId)).getOperatorNumeric();
                if (numeric != null && numeric.length() > 4) {
                    String mcc = numeric.substring(0, 3);
                    String mnc = numeric.substring(3);
                    this.mMcc.setText(mcc);
                    this.mMnc.setText(mnc);
                    this.mCurMnc = mnc;
                    this.mCurMcc = mcc;
                }
                this.mApnType.setText("default");
            }
            int authVal = this.mApnData.getInteger(14, Integer.valueOf(-1)).intValue();
            if (authVal != -1) {
                this.mAuthType.setValueIndex(authVal);
            } else {
                this.mAuthType.setValue(null);
            }
            this.mProtocol.setValue(this.mApnData.getString(16));
            this.mRoamingProtocol.setValue(this.mApnData.getString(20));
            this.mCarrierEnabled.setChecked(this.mApnData.getInteger(17, Integer.valueOf(1)).intValue() == 1);
            this.mBearerInitialVal = this.mApnData.getInteger(18, Integer.valueOf(0)).intValue();
            HashSet<String> bearers = new HashSet();
            int bearerBitmask = this.mApnData.getInteger(19, Integer.valueOf(0)).intValue();
            if (bearerBitmask != 0) {
                int bearerBitmask2 = bearerBitmask;
                bearerBitmask = 1;
                while (bearerBitmask2 != 0) {
                    if ((bearerBitmask2 & 1) == 1) {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("");
                        stringBuilder.append(bearerBitmask);
                        bearers.add(stringBuilder.toString());
                    }
                    bearerBitmask2 >>= 1;
                    bearerBitmask++;
                }
            } else if (this.mBearerInitialVal == 0) {
                bearers.add("0");
            }
            if (this.mBearerInitialVal != 0) {
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append("");
                stringBuilder2.append(this.mBearerInitialVal);
                if (!bearers.contains(stringBuilder2.toString())) {
                    stringBuilder2 = new StringBuilder();
                    stringBuilder2.append("");
                    stringBuilder2.append(this.mBearerInitialVal);
                    bearers.add(stringBuilder2.toString());
                }
            }
            this.mBearerMulti.setValues(bearers);
            this.mMvnoType.setValue(this.mApnData.getString(21));
            this.mMvnoMatchData.setEnabled(false);
            this.mMvnoMatchData.setText(this.mApnData.getString(22));
            String localizedName = Utils.getLocalizedName(getActivity(), this.mApnData.getString(1));
            if (!TextUtils.isEmpty(localizedName)) {
                this.mName.setText(localizedName);
            }
        }
        this.mName.setSummary((CharSequence) checkNull(this.mName.getText()));
        this.mApn.setSummary((CharSequence) checkNull(this.mApn.getText()));
        this.mProxy.setSummary((CharSequence) checkNull(this.mProxy.getText()));
        this.mPort.setSummary((CharSequence) checkNull(this.mPort.getText()));
        this.mUser.setSummary((CharSequence) checkNull(this.mUser.getText()));
        this.mServer.setSummary((CharSequence) checkNull(this.mServer.getText()));
        this.mPassword.setSummary((CharSequence) starify(this.mPassword.getText()));
        this.mMmsProxy.setSummary((CharSequence) checkNull(this.mMmsProxy.getText()));
        this.mMmsPort.setSummary((CharSequence) checkNull(this.mMmsPort.getText()));
        this.mMmsc.setSummary((CharSequence) checkNull(this.mMmsc.getText()));
        this.mMcc.setSummary((CharSequence) formatInteger(checkNull(this.mMcc.getText())));
        this.mMnc.setSummary((CharSequence) formatInteger(checkNull(this.mMnc.getText())));
        this.mApnType.setSummary((CharSequence) checkApnType(this.mApnType.getText()));
        numeric = this.mAuthType.getValue();
        if (numeric != null) {
            int authValIndex = Integer.parseInt(numeric);
            this.mAuthType.setValueIndex(authValIndex);
            this.mAuthType.setSummary(getResources().getStringArray(R.array.apn_auth_entries)[authValIndex]);
        } else {
            this.mAuthType.setSummary(sNotSet);
        }
        this.mProtocol.setSummary(checkNull(protocolDescription(this.mProtocol.getValue(), this.mProtocol)));
        this.mRoamingProtocol.setSummary(checkNull(protocolDescription(this.mRoamingProtocol.getValue(), this.mRoamingProtocol)));
        this.mBearerMulti.setSummary((CharSequence) checkNull(bearerMultiDescription(this.mBearerMulti.getValues())));
        this.mMvnoType.setSummary(checkNull(mvnoDescription(this.mMvnoType.getValue())));
        this.mMvnoMatchData.setSummary((CharSequence) checkNull(this.mMvnoMatchData.getText()));
        if (getResources().getBoolean(R.bool.config_allow_edit_carrier_enabled)) {
            this.mCarrierEnabled.setEnabled(true);
        } else {
            this.mCarrierEnabled.setEnabled(false);
        }
    }

    private String protocolDescription(String raw, ListPreference protocol) {
        int protocolIndex = protocol.findIndexOfValue(raw);
        if (protocolIndex == -1) {
            return null;
        }
        try {
            return getResources().getStringArray(R.array.apn_protocol_entries)[protocolIndex];
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    private String bearerMultiDescription(Set<String> raw) {
        String[] values = getResources().getStringArray(R.array.bearer_entries);
        StringBuilder retVal = new StringBuilder();
        boolean first = true;
        for (String bearer : raw) {
            int bearerIndex = this.mBearerMulti.findIndexOfValue(bearer);
            if (first) {
                try {
                    retVal.append(values[bearerIndex]);
                    first = false;
                } catch (ArrayIndexOutOfBoundsException e) {
                }
            } else {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(", ");
                stringBuilder.append(values[bearerIndex]);
                retVal.append(stringBuilder.toString());
            }
        }
        String val = retVal.toString();
        if (TextUtils.isEmpty(val)) {
            return null;
        }
        return val;
    }

    private String mvnoDescription(String newValue) {
        int mvnoIndex = this.mMvnoType.findIndexOfValue(newValue);
        String oldValue = this.mMvnoType.getValue();
        if (mvnoIndex == -1) {
            return null;
        }
        String[] values = getResources().getStringArray(R.array.mvno_type_entries);
        boolean z = true;
        boolean mvnoMatchDataUneditable = this.mReadOnlyApn || (this.mReadOnlyApnFields != null && Arrays.asList(this.mReadOnlyApnFields).contains(ApnSettings.MVNO_MATCH_DATA));
        ValidatedEditTextPreference validatedEditTextPreference = this.mMvnoMatchData;
        if (mvnoMatchDataUneditable || mvnoIndex == 0) {
            z = false;
        }
        validatedEditTextPreference.setEnabled(z);
        if (!(newValue == null || newValue.equals(oldValue))) {
            String numeric;
            if (values[mvnoIndex].equals("SPN")) {
                this.mMvnoMatchData.setText(this.mTelephonyManager.getSimOperatorName());
            } else if (values[mvnoIndex].equals("IMSI")) {
                numeric = this.mTelephonyManager.getSimOperator(this.mSubId);
                ValidatedEditTextPreference validatedEditTextPreference2 = this.mMvnoMatchData;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(numeric);
                stringBuilder.append("x");
                validatedEditTextPreference2.setText(stringBuilder.toString());
            } else if (values[mvnoIndex].equals("GID")) {
                this.mMvnoMatchData.setText(this.mTelephonyManager.getGroupIdLevel1());
            } else if (values[mvnoIndex].equals("ICCID") && this.mMvnoMatchDataStr != null) {
                numeric = TAG;
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append("mMvnoMatchDataStr: ");
                stringBuilder2.append(this.mMvnoMatchDataStr);
                Log.d(numeric, stringBuilder2.toString());
                this.mMvnoMatchData.setText(this.mMvnoMatchDataStr);
            }
        }
        try {
            return values[mvnoIndex];
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        String protocol;
        if (KEY_AUTH_TYPE.equals(key)) {
            try {
                int index = Integer.parseInt((String) newValue);
                this.mAuthType.setValueIndex(index);
                this.mAuthType.setSummary(getResources().getStringArray(R.array.apn_auth_entries)[index]);
            } catch (NumberFormatException e) {
                return false;
            }
        } else if (KEY_PROTOCOL.equals(key)) {
            protocol = protocolDescription((String) newValue, this.mProtocol);
            if (protocol == null) {
                return false;
            }
            this.mProtocol.setSummary(protocol);
            this.mProtocol.setValue((String) newValue);
        } else if (KEY_ROAMING_PROTOCOL.equals(key)) {
            protocol = protocolDescription((String) newValue, this.mRoamingProtocol);
            if (protocol == null) {
                return false;
            }
            this.mRoamingProtocol.setSummary(protocol);
            this.mRoamingProtocol.setValue((String) newValue);
        } else if (KEY_BEARER_MULTI.equals(key)) {
            protocol = bearerMultiDescription((Set) newValue);
            if (protocol == null) {
                return false;
            }
            this.mBearerMulti.setValues((Set) newValue);
            this.mBearerMulti.setSummary((CharSequence) protocol);
        } else if ("mvno_type".equals(key)) {
            protocol = mvnoDescription((String) newValue);
            if (protocol == null) {
                return false;
            }
            this.mMvnoType.setValue((String) newValue);
            this.mMvnoType.setSummary(protocol);
        } else if (KEY_PASSWORD.equals(key)) {
            this.mPassword.setSummary((CharSequence) starify(newValue != null ? String.valueOf(newValue) : ""));
        } else if (!KEY_CARRIER_ENABLED.equals(key)) {
            preference.setSummary(checkNull(newValue != null ? String.valueOf(newValue) : null));
        }
        return true;
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (!(this.mNewApn || this.mReadOnlyApn || !this.mDeletableApn)) {
            menu.add(0, 1, 0, R.string.menu_delete).setIcon(R.drawable.ic_delete);
        }
        menu.add(0, 2, 0, R.string.menu_save).setIcon(17301582);
        menu.add(0, 3, 0, R.string.menu_cancel).setIcon(17301560);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                deleteApn();
                finish();
                return true;
            case 2:
                if (validateAndSaveApnData()) {
                    finish();
                }
                return true;
            case 3:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.setOnKeyListener(this);
        view.setFocusableInTouchMode(true);
        view.requestFocus();
    }

    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() != 0 || keyCode != 4) {
            return false;
        }
        if (validateAndSaveApnData()) {
            finish();
        }
        return true;
    }

    /* Access modifiers changed, original: 0000 */
    public boolean setStringValueAndCheckIfDiff(ContentValues cv, String key, String value, boolean assumeDiff, int index) {
        String valueFromLocalCache = this.mApnData.getString(index);
        boolean isDiff = assumeDiff || (!(TextUtils.isEmpty(value) && TextUtils.isEmpty(valueFromLocalCache)) && (value == null || !value.equals(valueFromLocalCache)));
        if (isDiff && value != null) {
            cv.put(key, value);
        }
        return isDiff;
    }

    /* Access modifiers changed, original: 0000 */
    public boolean setIntValueAndCheckIfDiff(ContentValues cv, String key, int value, boolean assumeDiff, int index) {
        boolean isDiff = assumeDiff || value != this.mApnData.getInteger(index).intValue();
        if (isDiff) {
            cv.put(key, Integer.valueOf(value));
        }
        return isDiff;
    }

    /* Access modifiers changed, original: 0000 */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x0238  */
    @com.android.internal.annotations.VisibleForTesting
    public boolean validateAndSaveApnData() {
        /*
        r20 = this;
        r10 = r20;
        r0 = r10.mReadOnlyApn;
        r11 = 1;
        if (r0 == 0) goto L_0x0008;
    L_0x0007:
        return r11;
    L_0x0008:
        r0 = r10.mName;
        r0 = r0.getText();
        r12 = r10.checkNotSet(r0);
        r0 = r10.mApn;
        r0 = r0.getText();
        r13 = r10.checkNotSet(r0);
        r0 = r10.mMcc;
        r0 = r0.getText();
        r14 = r10.checkNotSet(r0);
        r0 = r10.mMnc;
        r0 = r0.getText();
        r15 = r10.checkNotSet(r0);
        r16 = r20.validateApnData();
        r6 = 0;
        if (r16 == 0) goto L_0x003b;
    L_0x0037:
        r20.showError();
        return r6;
    L_0x003b:
        r0 = new android.content.ContentValues;
        r0.<init>();
        r9 = r0;
        r7 = r10.mNewApn;
        r2 = "name";
        r5 = 1;
        r0 = r10;
        r1 = r9;
        r3 = r12;
        r4 = r7;
        r7 = r0.setStringValueAndCheckIfDiff(r1, r2, r3, r4, r5);
        r2 = "apn";
        r5 = 2;
        r3 = r13;
        r4 = r7;
        r7 = r0.setStringValueAndCheckIfDiff(r1, r2, r3, r4, r5);
        r2 = "proxy";
        r0 = r10.mProxy;
        r0 = r0.getText();
        r3 = r10.checkNotSet(r0);
        r5 = 3;
        r0 = r10;
        r4 = r7;
        r7 = r0.setStringValueAndCheckIfDiff(r1, r2, r3, r4, r5);
        r2 = "port";
        r0 = r10.mPort;
        r0 = r0.getText();
        r3 = r10.checkNotSet(r0);
        r5 = 4;
        r0 = r10;
        r4 = r7;
        r7 = r0.setStringValueAndCheckIfDiff(r1, r2, r3, r4, r5);
        r2 = "mmsproxy";
        r0 = r10.mMmsProxy;
        r0 = r0.getText();
        r3 = r10.checkNotSet(r0);
        r5 = 12;
        r0 = r10;
        r4 = r7;
        r7 = r0.setStringValueAndCheckIfDiff(r1, r2, r3, r4, r5);
        r2 = "mmsport";
        r0 = r10.mMmsPort;
        r0 = r0.getText();
        r3 = r10.checkNotSet(r0);
        r5 = 13;
        r0 = r10;
        r4 = r7;
        r7 = r0.setStringValueAndCheckIfDiff(r1, r2, r3, r4, r5);
        r2 = "user";
        r0 = r10.mUser;
        r0 = r0.getText();
        r3 = r10.checkNotSet(r0);
        r5 = 5;
        r0 = r10;
        r4 = r7;
        r7 = r0.setStringValueAndCheckIfDiff(r1, r2, r3, r4, r5);
        r2 = "server";
        r0 = r10.mServer;
        r0 = r0.getText();
        r3 = r10.checkNotSet(r0);
        r5 = 6;
        r0 = r10;
        r4 = r7;
        r7 = r0.setStringValueAndCheckIfDiff(r1, r2, r3, r4, r5);
        r2 = "password";
        r0 = r10.mPassword;
        r0 = r0.getText();
        r3 = r10.checkNotSet(r0);
        r5 = 7;
        r0 = r10;
        r4 = r7;
        r7 = r0.setStringValueAndCheckIfDiff(r1, r2, r3, r4, r5);
        r2 = "mmsc";
        r0 = r10.mMmsc;
        r0 = r0.getText();
        r3 = r10.checkNotSet(r0);
        r5 = 8;
        r0 = r10;
        r4 = r7;
        r7 = r0.setStringValueAndCheckIfDiff(r1, r2, r3, r4, r5);
        r0 = r10.mAuthType;
        r8 = r0.getValue();
        if (r8 == 0) goto L_0x010a;
    L_0x00fa:
        r2 = "authtype";
        r3 = java.lang.Integer.parseInt(r8);
        r5 = 14;
        r0 = r10;
        r1 = r9;
        r4 = r7;
        r0 = r0.setIntValueAndCheckIfDiff(r1, r2, r3, r4, r5);
        r7 = r0;
    L_0x010a:
        r2 = "protocol";
        r0 = r10.mProtocol;
        r0 = r0.getValue();
        r3 = r10.checkNotSet(r0);
        r5 = 16;
        r0 = r10;
        r1 = r9;
        r4 = r7;
        r7 = r0.setStringValueAndCheckIfDiff(r1, r2, r3, r4, r5);
        r2 = "roaming_protocol";
        r0 = r10.mRoamingProtocol;
        r0 = r0.getValue();
        r3 = r10.checkNotSet(r0);
        r5 = 20;
        r0 = r10;
        r4 = r7;
        r7 = r0.setStringValueAndCheckIfDiff(r1, r2, r3, r4, r5);
        r2 = "type";
        r0 = r20.getUserEnteredApnType();
        r3 = r10.checkApnType(r0);
        r5 = 15;
        r0 = r10;
        r4 = r7;
        r7 = r0.setStringValueAndCheckIfDiff(r1, r2, r3, r4, r5);
        r2 = "mcc";
        r5 = 9;
        r3 = r14;
        r4 = r7;
        r7 = r0.setStringValueAndCheckIfDiff(r1, r2, r3, r4, r5);
        r2 = "mnc";
        r5 = 10;
        r3 = r15;
        r4 = r7;
        r7 = r0.setStringValueAndCheckIfDiff(r1, r2, r3, r4, r5);
        r0 = "numeric";
        r1 = new java.lang.StringBuilder;
        r1.<init>();
        r1.append(r14);
        r1.append(r15);
        r1 = r1.toString();
        r9.put(r0, r1);
        r0 = r10.mCurMnc;
        if (r0 == 0) goto L_0x018e;
    L_0x0171:
        r0 = r10.mCurMcc;
        if (r0 == 0) goto L_0x018e;
    L_0x0175:
        r0 = r10.mCurMnc;
        r0 = r0.equals(r15);
        if (r0 == 0) goto L_0x018e;
    L_0x017d:
        r0 = r10.mCurMcc;
        r0 = r0.equals(r14);
        if (r0 == 0) goto L_0x018e;
    L_0x0185:
        r0 = "current";
        r1 = java.lang.Integer.valueOf(r11);
        r9.put(r0, r1);
    L_0x018e:
        r0 = r10.mBearerMulti;
        r5 = r0.getValues();
        r0 = 0;
        r1 = r5.iterator();
    L_0x0199:
        r2 = r1.hasNext();
        if (r2 == 0) goto L_0x01b7;
    L_0x019f:
        r2 = r1.next();
        r2 = (java.lang.String) r2;
        r3 = java.lang.Integer.parseInt(r2);
        if (r3 != 0) goto L_0x01ad;
    L_0x01ab:
        r0 = 0;
        goto L_0x01b7;
    L_0x01ad:
        r3 = java.lang.Integer.parseInt(r2);
        r3 = android.telephony.ServiceState.getBitmaskForTech(r3);
        r0 = r0 | r3;
        goto L_0x0199;
    L_0x01b7:
        r4 = r0;
        r2 = "bearer_bitmask";
        r17 = 19;
        r0 = r10;
        r1 = r9;
        r3 = r4;
        r11 = r4;
        r4 = r7;
        r18 = r5;
        r5 = r17;
        r7 = r0.setIntValueAndCheckIfDiff(r1, r2, r3, r4, r5);
        if (r11 == 0) goto L_0x01dd;
    L_0x01cb:
        r0 = r10.mBearerInitialVal;
        if (r0 != 0) goto L_0x01d0;
    L_0x01cf:
        goto L_0x01dd;
    L_0x01d0:
        r0 = r10.mBearerInitialVal;
        r0 = android.telephony.ServiceState.bitmaskHasTech(r11, r0);
        if (r0 == 0) goto L_0x01db;
    L_0x01d8:
        r0 = r10.mBearerInitialVal;
        goto L_0x01de;
    L_0x01db:
        r3 = r6;
        goto L_0x01df;
    L_0x01dd:
        r0 = 0;
    L_0x01de:
        r3 = r0;
    L_0x01df:
        r2 = "bearer";
        r5 = 18;
        r0 = r10;
        r1 = r9;
        r4 = r7;
        r0 = r0.setIntValueAndCheckIfDiff(r1, r2, r3, r4, r5);
        r6 = "mvno_type";
        r1 = r10.mMvnoType;
        r1 = r1.getValue();
        r7 = r10.checkNotSet(r1);
        r1 = 21;
        r4 = r10;
        r5 = r9;
        r2 = r8;
        r8 = r0;
        r19 = r9;
        r9 = r1;
        r0 = r4.setStringValueAndCheckIfDiff(r5, r6, r7, r8, r9);
        r6 = "mvno_match_data";
        r1 = r10.mMvnoMatchData;
        r1 = r1.getText();
        r7 = r10.checkNotSet(r1);
        r9 = 22;
        r5 = r19;
        r8 = r0;
        r0 = r4.setStringValueAndCheckIfDiff(r5, r6, r7, r8, r9);
        r6 = "carrier_enabled";
        r1 = r10.mCarrierEnabled;
        r7 = r1.isChecked();
        r9 = 17;
        r4 = r10;
        r5 = r19;
        r8 = r0;
        r0 = r4.setIntValueAndCheckIfDiff(r5, r6, r7, r8, r9);
        r1 = "edited";
        r4 = 1;
        r5 = java.lang.Integer.valueOf(r4);
        r4 = r19;
        r4.put(r1, r5);
        if (r0 == 0) goto L_0x024c;
    L_0x0238:
        r1 = r10.mApnData;
        r1 = r1.getUri();
        if (r1 != 0) goto L_0x0243;
    L_0x0240:
        r1 = r10.mCarrierUri;
        goto L_0x0249;
    L_0x0243:
        r1 = r10.mApnData;
        r1 = r1.getUri();
    L_0x0249:
        r10.updateApnDataToDatabase(r1, r4);
    L_0x024c:
        r1 = 1;
        return r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settings.network.ApnEditor.validateAndSaveApnData():boolean");
    }

    private void updateApnDataToDatabase(Uri uri, ContentValues values) {
        ThreadUtils.postOnBackgroundThread(new -$$Lambda$ApnEditor$1vSLgWOnd4pMuFU2qFaSz0HXNw8(this, uri, values));
    }

    public static /* synthetic */ void lambda$updateApnDataToDatabase$0(ApnEditor apnEditor, Uri uri, ContentValues values) {
        Log.d(TAG, "postOnBackgroundThread updateApnDataToDatabase start");
        if (!uri.equals(apnEditor.mCarrierUri)) {
            apnEditor.getContentResolver().update(uri, values, null, null);
        } else if (apnEditor.getContentResolver().insert(apnEditor.mCarrierUri, values) == null) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Can't add a new apn to database ");
            stringBuilder.append(apnEditor.mCarrierUri);
            Log.e(str, stringBuilder.toString());
        }
        Log.d(TAG, "postOnBackgroundThread updateApnDataToDatabase end");
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public String validateApnData() {
        String errorMsg = null;
        String name = checkNotSet(this.mName.getText());
        String apn = checkNotSet(this.mApn.getText());
        String mcc = checkNotSet(this.mMcc.getText());
        String mnc = checkNotSet(this.mMnc.getText());
        if (TextUtils.isEmpty(name)) {
            errorMsg = getResources().getString(R.string.error_name_empty);
        } else if (TextUtils.isEmpty(apn)) {
            errorMsg = getResources().getString(R.string.error_apn_empty);
        } else if (mcc == null || mcc.length() != 3) {
            errorMsg = getResources().getString(R.string.error_mcc_not3);
        } else if (mnc == null || (mnc.length() & 65534) != 2) {
            errorMsg = getResources().getString(R.string.error_mnc_not23);
        }
        if (errorMsg != null || ArrayUtils.isEmpty(this.mReadOnlyApnTypes) || !apnTypesMatch(this.mReadOnlyApnTypes, getUserEnteredApnType())) {
            return errorMsg;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (String type : this.mReadOnlyApnTypes) {
            stringBuilder.append(type);
            stringBuilder.append(", ");
            String str = TAG;
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("validateApnData: appending type: ");
            stringBuilder2.append(type);
            Log.d(str, stringBuilder2.toString());
        }
        if (stringBuilder.length() >= 2) {
            stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());
        }
        return String.format(getResources().getString(R.string.error_adding_apn_type), new Object[]{stringBuilder});
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void showError() {
        ErrorDialog.showError(this);
    }

    private void deleteApn() {
        if (this.mApnData.getUri() != null) {
            getContentResolver().delete(this.mApnData.getUri(), null, null);
            this.mApnData = new ApnData(sProjection.length);
        }
    }

    private String starify(String value) {
        if (value == null || value.length() == 0) {
            return sNotSet;
        }
        char[] password = new char[value.length()];
        for (int i = 0; i < password.length; i++) {
            password[i] = '*';
        }
        return new String(password);
    }

    private String checkNull(String value) {
        return TextUtils.isEmpty(value) ? sNotSet : value;
    }

    private String checkNotSet(String value) {
        return sNotSet.equals(value) ? null : value;
    }

    private String checkApnType(String value) {
        if (value == null || value.length() == 0) {
            return "default";
        }
        return value;
    }

    private void setDefaultData() {
        CarrierConfigManager configManager = (CarrierConfigManager) getSystemService("carrier_config");
        if (configManager != null) {
            PersistableBundle b = configManager.getConfigForSubId(this.mSubId);
            if (b != null) {
                PersistableBundle defaultValues = b.getPersistableBundle(APN_DEFALUT_VALUES_STRING_ARRAY);
                if (defaultValues != null && !defaultValues.isEmpty()) {
                    for (String key : defaultValues.keySet()) {
                        if (fieldValidate(key)) {
                            setAppData(key, defaultValues.get(key));
                        }
                    }
                }
            }
        }
    }

    private void setAppData(String key, Object object) {
        int index = findIndexOfKey(key);
        if (index >= 0) {
            this.mApnData.setObject(index, object);
        }
    }

    private int findIndexOfKey(String key) {
        for (int i = 0; i < sProjection.length; i++) {
            if (sProjection[i].equals(key)) {
                return i;
            }
        }
        return -1;
    }

    private boolean fieldValidate(String field) {
        for (String tableField : sUIConfigurableItems) {
            if (tableField.equalsIgnoreCase(field)) {
                return true;
            }
        }
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(field);
        stringBuilder.append(" is not configurable");
        Log.w(str, stringBuilder.toString());
        return false;
    }

    private String getUserEnteredApnType() {
        String userEnteredApnType = this.mApnType.getText();
        if (userEnteredApnType != null) {
            userEnteredApnType = userEnteredApnType.trim();
        }
        if ((!TextUtils.isEmpty(userEnteredApnType) && !"*".equals(userEnteredApnType)) || ArrayUtils.isEmpty(this.mReadOnlyApnTypes)) {
            return userEnteredApnType;
        }
        StringBuilder editableApnTypes = new StringBuilder();
        List<String> readOnlyApnTypes = Arrays.asList(this.mReadOnlyApnTypes);
        boolean first = true;
        for (String apnType : PhoneConstants.APN_TYPES) {
            if (!(readOnlyApnTypes.contains(apnType) || apnType.equals("ia") || apnType.equals("emergency"))) {
                if (first) {
                    first = false;
                } else {
                    editableApnTypes.append(",");
                }
                editableApnTypes.append(apnType);
            }
        }
        userEnteredApnType = editableApnTypes.toString();
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("getUserEnteredApnType: changed apn type to editable apn types: ");
        stringBuilder.append(userEnteredApnType);
        Log.d(str, stringBuilder.toString());
        return userEnteredApnType;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public ApnData getApnDataFromUri(Uri uri) {
        ApnData apnData = null;
        Cursor cursor;
        try {
            cursor = getContentResolver().query(uri, sProjection, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                apnData = new ApnData(uri, cursor);
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("getApnDataFromUri exception");
            stringBuilder.append(e);
            Log.d(str, stringBuilder.toString());
            apnData = null;
        } catch (Throwable th) {
            r0.addSuppressed(th);
        }
        if (apnData == null) {
            String str2 = TAG;
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("Can't get apnData from Uri ");
            stringBuilder2.append(uri);
            Log.d(str2, stringBuilder2.toString());
        }
        return apnData;
    }
}
