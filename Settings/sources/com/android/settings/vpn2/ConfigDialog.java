package com.android.settings.vpn2;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.SystemProperties;
import android.security.KeyStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import com.android.internal.net.VpnProfile;
import com.android.settings.R;
import java.net.InetAddress;

class ConfigDialog extends AlertDialog implements TextWatcher, OnClickListener, OnItemSelectedListener, OnCheckedChangeListener {
    private TextView mAlwaysOnInvalidReason;
    private CheckBox mAlwaysOnVpn;
    private TextView mDnsServers;
    private boolean mEditing;
    private boolean mExists;
    private Spinner mIpsecCaCert;
    private TextView mIpsecIdentifier;
    private TextView mIpsecSecret;
    private Spinner mIpsecServerCert;
    private Spinner mIpsecUserCert;
    private final KeyStore mKeyStore = KeyStore.getInstance();
    private TextView mL2tpSecret;
    private final DialogInterface.OnClickListener mListener;
    private CheckBox mMppe;
    private TextView mName;
    private TextView mPassword;
    private final VpnProfile mProfile;
    private TextView mRoutes;
    private CheckBox mSaveLogin;
    private TextView mSearchDomains;
    private TextView mServer;
    private CheckBox mShowOptions;
    private Spinner mType;
    private TextView mUsername;
    private View mView;

    ConfigDialog(Context context, DialogInterface.OnClickListener listener, VpnProfile profile, boolean editing, boolean exists) {
        super(context);
        this.mListener = listener;
        this.mProfile = profile;
        this.mEditing = editing;
        this.mExists = exists;
    }

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedState) {
        this.mView = getLayoutInflater().inflate(R.layout.vpn_dialog, null);
        setView(this.mView);
        Context context = getContext();
        this.mName = (TextView) this.mView.findViewById(R.id.name);
        this.mType = (Spinner) this.mView.findViewById(R.id.type);
        this.mServer = (TextView) this.mView.findViewById(R.id.server);
        this.mUsername = (TextView) this.mView.findViewById(R.id.username);
        this.mPassword = (TextView) this.mView.findViewById(R.id.password);
        this.mSearchDomains = (TextView) this.mView.findViewById(R.id.search_domains);
        this.mDnsServers = (TextView) this.mView.findViewById(R.id.dns_servers);
        this.mRoutes = (TextView) this.mView.findViewById(R.id.routes);
        this.mMppe = (CheckBox) this.mView.findViewById(R.id.mppe);
        this.mL2tpSecret = (TextView) this.mView.findViewById(R.id.l2tp_secret);
        this.mIpsecIdentifier = (TextView) this.mView.findViewById(R.id.ipsec_identifier);
        this.mIpsecSecret = (TextView) this.mView.findViewById(R.id.ipsec_secret);
        this.mIpsecUserCert = (Spinner) this.mView.findViewById(R.id.ipsec_user_cert);
        this.mIpsecCaCert = (Spinner) this.mView.findViewById(R.id.ipsec_ca_cert);
        this.mIpsecServerCert = (Spinner) this.mView.findViewById(R.id.ipsec_server_cert);
        this.mSaveLogin = (CheckBox) this.mView.findViewById(R.id.save_login);
        this.mShowOptions = (CheckBox) this.mView.findViewById(R.id.show_options);
        this.mAlwaysOnVpn = (CheckBox) this.mView.findViewById(R.id.always_on_vpn);
        this.mAlwaysOnInvalidReason = (TextView) this.mView.findViewById(R.id.always_on_invalid_reason);
        this.mName.setText(this.mProfile.name);
        this.mType.setSelection(this.mProfile.type);
        this.mServer.setText(this.mProfile.server);
        if (this.mProfile.saveLogin) {
            this.mUsername.setText(this.mProfile.username);
            this.mPassword.setText(this.mProfile.password);
        }
        this.mSearchDomains.setText(this.mProfile.searchDomains);
        this.mDnsServers.setText(this.mProfile.dnsServers);
        this.mRoutes.setText(this.mProfile.routes);
        this.mMppe.setChecked(this.mProfile.mppe);
        this.mL2tpSecret.setText(this.mProfile.l2tpSecret);
        this.mIpsecIdentifier.setText(this.mProfile.ipsecIdentifier);
        this.mIpsecSecret.setText(this.mProfile.ipsecSecret);
        loadCertificates(this.mIpsecUserCert, "USRPKEY_", 0, this.mProfile.ipsecUserCert);
        loadCertificates(this.mIpsecCaCert, "CACERT_", R.string.vpn_no_ca_cert, this.mProfile.ipsecCaCert);
        loadCertificates(this.mIpsecServerCert, "USRCERT_", R.string.vpn_no_server_cert, this.mProfile.ipsecServerCert);
        this.mSaveLogin.setChecked(this.mProfile.saveLogin);
        this.mAlwaysOnVpn.setChecked(this.mProfile.key.equals(VpnUtils.getLockdownVpn()));
        if (SystemProperties.getBoolean("persist.radio.imsregrequired", false)) {
            this.mAlwaysOnVpn.setVisibility(8);
        }
        this.mName.addTextChangedListener(this);
        this.mType.setOnItemSelectedListener(this);
        this.mServer.addTextChangedListener(this);
        this.mUsername.addTextChangedListener(this);
        this.mPassword.addTextChangedListener(this);
        this.mDnsServers.addTextChangedListener(this);
        this.mRoutes.addTextChangedListener(this);
        this.mIpsecSecret.addTextChangedListener(this);
        this.mIpsecUserCert.setOnItemSelectedListener(this);
        this.mShowOptions.setOnClickListener(this);
        this.mAlwaysOnVpn.setOnCheckedChangeListener(this);
        boolean z = this.mEditing || !validate(true);
        this.mEditing = z;
        if (this.mEditing) {
            setTitle(R.string.vpn_edit);
            this.mView.findViewById(R.id.editor).setVisibility(0);
            changeType(this.mProfile.type);
            this.mSaveLogin.setVisibility(8);
            if (!(this.mProfile.searchDomains.isEmpty() && this.mProfile.dnsServers.isEmpty() && this.mProfile.routes.isEmpty())) {
                showAdvancedOptions();
            }
            if (this.mExists) {
                setButton(-3, context.getString(R.string.vpn_forget), this.mListener);
            }
            setButton(-1, context.getString(R.string.vpn_save), this.mListener);
        } else {
            setTitle(context.getString(R.string.vpn_connect_to, new Object[]{this.mProfile.name}));
            setButton(-1, context.getString(R.string.vpn_connect), this.mListener);
        }
        setButton(-2, context.getString(R.string.vpn_cancel), this.mListener);
        super.onCreate(savedState);
        updateUiControls();
        getWindow().setSoftInputMode(20);
    }

    public void onRestoreInstanceState(Bundle savedState) {
        super.onRestoreInstanceState(savedState);
        if (this.mShowOptions.isChecked()) {
            showAdvancedOptions();
        }
    }

    public void afterTextChanged(Editable field) {
        updateUiControls();
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    public void onClick(View view) {
        if (view == this.mShowOptions) {
            showAdvancedOptions();
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent == this.mType) {
            changeType(position);
        }
        updateUiControls();
    }

    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (compoundButton == this.mAlwaysOnVpn) {
            updateUiControls();
        }
    }

    public boolean isVpnAlwaysOn() {
        return this.mAlwaysOnVpn.isChecked();
    }

    private void updateUiControls() {
        VpnProfile profile = getProfile();
        if (profile.isValidLockdownProfile()) {
            this.mAlwaysOnVpn.setEnabled(true);
            this.mAlwaysOnInvalidReason.setVisibility(8);
        } else {
            this.mAlwaysOnVpn.setChecked(false);
            this.mAlwaysOnVpn.setEnabled(false);
            if (!profile.isTypeValidForLockdown()) {
                this.mAlwaysOnInvalidReason.setText(R.string.vpn_always_on_invalid_reason_type);
            } else if (!profile.isServerAddressNumeric()) {
                this.mAlwaysOnInvalidReason.setText(R.string.vpn_always_on_invalid_reason_server);
            } else if (!profile.hasDns()) {
                this.mAlwaysOnInvalidReason.setText(R.string.vpn_always_on_invalid_reason_no_dns);
            } else if (profile.areDnsAddressesNumeric()) {
                this.mAlwaysOnInvalidReason.setText(R.string.vpn_always_on_invalid_reason_other);
            } else {
                this.mAlwaysOnInvalidReason.setText(R.string.vpn_always_on_invalid_reason_dns);
            }
            this.mAlwaysOnInvalidReason.setVisibility(0);
        }
        if (this.mAlwaysOnVpn.isChecked()) {
            this.mSaveLogin.setChecked(true);
            this.mSaveLogin.setEnabled(false);
        } else {
            this.mSaveLogin.setChecked(this.mProfile.saveLogin);
            this.mSaveLogin.setEnabled(true);
        }
        getButton(-1).setEnabled(validate(this.mEditing));
    }

    private void showAdvancedOptions() {
        this.mView.findViewById(R.id.options).setVisibility(0);
        this.mShowOptions.setVisibility(8);
    }

    /* JADX WARNING: Missing block: B:3:0x0045, code skipped:
            r6.mView.findViewById(com.android.settings.R.id.ipsec_user).setVisibility(0);
     */
    /* JADX WARNING: Missing block: B:4:0x004e, code skipped:
            r6.mView.findViewById(com.android.settings.R.id.ipsec_peer).setVisibility(0);
     */
    /* JADX WARNING: Missing block: B:6:0x0061, code skipped:
            r6.mView.findViewById(com.android.settings.R.id.ipsec_psk).setVisibility(0);
     */
    /* JADX WARNING: Missing block: B:9:?, code skipped:
            return;
     */
    /* JADX WARNING: Missing block: B:10:?, code skipped:
            return;
     */
    private void changeType(int r7) {
        /*
        r6 = this;
        r0 = r6.mMppe;
        r1 = 8;
        r0.setVisibility(r1);
        r0 = r6.mView;
        r2 = 2131362490; // 0x7f0a02ba float:1.8344762E38 double:1.053032985E-314;
        r0 = r0.findViewById(r2);
        r0.setVisibility(r1);
        r0 = r6.mView;
        r3 = 2131362464; // 0x7f0a02a0 float:1.834471E38 double:1.0530329723E-314;
        r0 = r0.findViewById(r3);
        r0.setVisibility(r1);
        r0 = r6.mView;
        r4 = 2131362467; // 0x7f0a02a3 float:1.8344715E38 double:1.053032974E-314;
        r0 = r0.findViewById(r4);
        r0.setVisibility(r1);
        r0 = r6.mView;
        r5 = 2131362463; // 0x7f0a029f float:1.8344707E38 double:1.053032972E-314;
        r0 = r0.findViewById(r5);
        r0.setVisibility(r1);
        r0 = 0;
        switch(r7) {
            case 0: goto L_0x006b;
            case 1: goto L_0x0058;
            case 2: goto L_0x003c;
            case 3: goto L_0x0061;
            case 4: goto L_0x0045;
            case 5: goto L_0x004e;
            default: goto L_0x003b;
        };
    L_0x003b:
        goto L_0x0071;
    L_0x003c:
        r1 = r6.mView;
        r1 = r1.findViewById(r2);
        r1.setVisibility(r0);
    L_0x0045:
        r1 = r6.mView;
        r1 = r1.findViewById(r4);
        r1.setVisibility(r0);
    L_0x004e:
        r1 = r6.mView;
        r1 = r1.findViewById(r5);
        r1.setVisibility(r0);
        goto L_0x0071;
    L_0x0058:
        r1 = r6.mView;
        r1 = r1.findViewById(r2);
        r1.setVisibility(r0);
    L_0x0061:
        r1 = r6.mView;
        r1 = r1.findViewById(r3);
        r1.setVisibility(r0);
        goto L_0x0071;
    L_0x006b:
        r1 = r6.mMppe;
        r1.setVisibility(r0);
    L_0x0071:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settings.vpn2.ConfigDialog.changeType(int):void");
    }

    private boolean validate(boolean editing) {
        if (this.mAlwaysOnVpn.isChecked() && !getProfile().isValidLockdownProfile()) {
            return false;
        }
        boolean z = true;
        if (!editing) {
            if (this.mUsername.getText().length() == 0 || this.mPassword.getText().length() == 0) {
                z = false;
            }
            return z;
        } else if (this.mName.getText().length() == 0 || this.mServer.getText().length() == 0 || !validateAddresses(this.mDnsServers.getText().toString(), false) || !validateAddresses(this.mRoutes.getText().toString(), true)) {
            return false;
        } else {
            switch (this.mType.getSelectedItemPosition()) {
                case 0:
                case 5:
                    return true;
                case 1:
                case 3:
                    if (this.mIpsecSecret.getText().length() == 0) {
                        z = false;
                    }
                    return z;
                case 2:
                case 4:
                    if (this.mIpsecUserCert.getSelectedItemPosition() == 0) {
                        z = false;
                    }
                    return z;
                default:
                    return false;
            }
        }
    }

    private boolean validateAddresses(String addresses, boolean cidr) {
        try {
            for (String address : addresses.split(" ")) {
                String address2;
                if (!address2.isEmpty()) {
                    int prefixLength = 32;
                    if (cidr) {
                        String[] parts = address2.split("/", 2);
                        address2 = parts[0];
                        prefixLength = Integer.parseInt(parts[1]);
                    }
                    byte[] bytes = InetAddress.parseNumericAddress(address2).getAddress();
                    int integer = (((bytes[1] & 255) << 16) | (((bytes[2] & 255) << 8) | (bytes[3] & 255))) | ((bytes[0] & 255) << 24);
                    if (bytes.length != 4 || prefixLength < 0 || prefixLength > 32 || (prefixLength < 32 && (integer << prefixLength) != 0)) {
                        return false;
                    }
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void loadCertificates(Spinner spinner, String prefix, int firstId, String selected) {
        Context context = getContext();
        String first = firstId == 0 ? "" : context.getString(firstId);
        String[] certificates = this.mKeyStore.list(prefix);
        int i = 1;
        if (certificates == null || certificates.length == 0) {
            certificates = new String[]{first};
        } else {
            String[] array = new String[(certificates.length + 1)];
            array[0] = first;
            System.arraycopy(certificates, 0, array, 1, certificates.length);
            certificates = array;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter(context, 17367048, certificates);
        adapter.setDropDownViewResource(17367049);
        spinner.setAdapter(adapter);
        while (i < certificates.length) {
            if (certificates[i].equals(selected)) {
                spinner.setSelection(i);
                return;
            }
            i++;
        }
    }

    /* Access modifiers changed, original: 0000 */
    public boolean isEditing() {
        return this.mEditing;
    }

    /* Access modifiers changed, original: 0000 */
    /* JADX WARNING: Missing block: B:4:0x008d, code skipped:
            if (r5.mIpsecUserCert.getSelectedItemPosition() == 0) goto L_0x0099;
     */
    /* JADX WARNING: Missing block: B:5:0x008f, code skipped:
            r0.ipsecUserCert = (java.lang.String) r5.mIpsecUserCert.getSelectedItem();
     */
    /* JADX WARNING: Missing block: B:7:0x009f, code skipped:
            if (r5.mIpsecCaCert.getSelectedItemPosition() == 0) goto L_0x00ab;
     */
    /* JADX WARNING: Missing block: B:8:0x00a1, code skipped:
            r0.ipsecCaCert = (java.lang.String) r5.mIpsecCaCert.getSelectedItem();
     */
    /* JADX WARNING: Missing block: B:10:0x00b1, code skipped:
            if (r5.mIpsecServerCert.getSelectedItemPosition() == 0) goto L_0x00ec;
     */
    /* JADX WARNING: Missing block: B:11:0x00b3, code skipped:
            r0.ipsecServerCert = (java.lang.String) r5.mIpsecServerCert.getSelectedItem();
     */
    /* JADX WARNING: Missing block: B:13:0x00ca, code skipped:
            r0.ipsecIdentifier = r5.mIpsecIdentifier.getText().toString();
            r0.ipsecSecret = r5.mIpsecSecret.getText().toString();
     */
    /* JADX WARNING: Missing block: B:15:0x00ec, code skipped:
            r2 = true;
     */
    /* JADX WARNING: Missing block: B:16:0x00f4, code skipped:
            if (r0.username.isEmpty() == false) goto L_0x0101;
     */
    /* JADX WARNING: Missing block: B:18:0x00fc, code skipped:
            if (r0.password.isEmpty() != false) goto L_0x00ff;
     */
    /* JADX WARNING: Missing block: B:19:0x00ff, code skipped:
            r1 = false;
     */
    /* JADX WARNING: Missing block: B:20:0x0101, code skipped:
            r1 = true;
     */
    /* JADX WARNING: Missing block: B:22:0x0108, code skipped:
            if (r5.mSaveLogin.isChecked() != false) goto L_0x0113;
     */
    /* JADX WARNING: Missing block: B:24:0x010c, code skipped:
            if (r5.mEditing == false) goto L_0x0111;
     */
    /* JADX WARNING: Missing block: B:25:0x010e, code skipped:
            if (r1 == false) goto L_0x0111;
     */
    /* JADX WARNING: Missing block: B:26:0x0111, code skipped:
            r2 = false;
     */
    /* JADX WARNING: Missing block: B:27:0x0113, code skipped:
            r0.saveLogin = r2;
     */
    /* JADX WARNING: Missing block: B:28:0x0115, code skipped:
            return r0;
     */
    public com.android.internal.net.VpnProfile getProfile() {
        /*
        r5 = this;
        r0 = new com.android.internal.net.VpnProfile;
        r1 = r5.mProfile;
        r1 = r1.key;
        r0.<init>(r1);
        r1 = r5.mName;
        r1 = r1.getText();
        r1 = r1.toString();
        r0.name = r1;
        r1 = r5.mType;
        r1 = r1.getSelectedItemPosition();
        r0.type = r1;
        r1 = r5.mServer;
        r1 = r1.getText();
        r1 = r1.toString();
        r1 = r1.trim();
        r0.server = r1;
        r1 = r5.mUsername;
        r1 = r1.getText();
        r1 = r1.toString();
        r0.username = r1;
        r1 = r5.mPassword;
        r1 = r1.getText();
        r1 = r1.toString();
        r0.password = r1;
        r1 = r5.mSearchDomains;
        r1 = r1.getText();
        r1 = r1.toString();
        r1 = r1.trim();
        r0.searchDomains = r1;
        r1 = r5.mDnsServers;
        r1 = r1.getText();
        r1 = r1.toString();
        r1 = r1.trim();
        r0.dnsServers = r1;
        r1 = r5.mRoutes;
        r1 = r1.getText();
        r1 = r1.toString();
        r1 = r1.trim();
        r0.routes = r1;
        r1 = r0.type;
        switch(r1) {
            case 0: goto L_0x00e3;
            case 1: goto L_0x00be;
            case 2: goto L_0x007b;
            case 3: goto L_0x00ca;
            case 4: goto L_0x0087;
            case 5: goto L_0x0099;
            default: goto L_0x007a;
        };
    L_0x007a:
        goto L_0x00ec;
    L_0x007b:
        r1 = r5.mL2tpSecret;
        r1 = r1.getText();
        r1 = r1.toString();
        r0.l2tpSecret = r1;
    L_0x0087:
        r1 = r5.mIpsecUserCert;
        r1 = r1.getSelectedItemPosition();
        if (r1 == 0) goto L_0x0099;
    L_0x008f:
        r1 = r5.mIpsecUserCert;
        r1 = r1.getSelectedItem();
        r1 = (java.lang.String) r1;
        r0.ipsecUserCert = r1;
    L_0x0099:
        r1 = r5.mIpsecCaCert;
        r1 = r1.getSelectedItemPosition();
        if (r1 == 0) goto L_0x00ab;
    L_0x00a1:
        r1 = r5.mIpsecCaCert;
        r1 = r1.getSelectedItem();
        r1 = (java.lang.String) r1;
        r0.ipsecCaCert = r1;
    L_0x00ab:
        r1 = r5.mIpsecServerCert;
        r1 = r1.getSelectedItemPosition();
        if (r1 == 0) goto L_0x00ec;
    L_0x00b3:
        r1 = r5.mIpsecServerCert;
        r1 = r1.getSelectedItem();
        r1 = (java.lang.String) r1;
        r0.ipsecServerCert = r1;
        goto L_0x00ec;
    L_0x00be:
        r1 = r5.mL2tpSecret;
        r1 = r1.getText();
        r1 = r1.toString();
        r0.l2tpSecret = r1;
    L_0x00ca:
        r1 = r5.mIpsecIdentifier;
        r1 = r1.getText();
        r1 = r1.toString();
        r0.ipsecIdentifier = r1;
        r1 = r5.mIpsecSecret;
        r1 = r1.getText();
        r1 = r1.toString();
        r0.ipsecSecret = r1;
        goto L_0x00ec;
    L_0x00e3:
        r1 = r5.mMppe;
        r1 = r1.isChecked();
        r0.mppe = r1;
    L_0x00ec:
        r1 = r0.username;
        r1 = r1.isEmpty();
        r2 = 1;
        r3 = 0;
        if (r1 == 0) goto L_0x0101;
    L_0x00f6:
        r1 = r0.password;
        r1 = r1.isEmpty();
        if (r1 != 0) goto L_0x00ff;
    L_0x00fe:
        goto L_0x0101;
    L_0x00ff:
        r1 = r3;
        goto L_0x0102;
    L_0x0101:
        r1 = r2;
    L_0x0102:
        r4 = r5.mSaveLogin;
        r4 = r4.isChecked();
        if (r4 != 0) goto L_0x0113;
    L_0x010a:
        r4 = r5.mEditing;
        if (r4 == 0) goto L_0x0111;
    L_0x010e:
        if (r1 == 0) goto L_0x0111;
    L_0x0110:
        goto L_0x0113;
    L_0x0111:
        r2 = r3;
    L_0x0113:
        r0.saveLogin = r2;
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settings.vpn2.ConfigDialog.getProfile():com.android.internal.net.VpnProfile");
    }
}
