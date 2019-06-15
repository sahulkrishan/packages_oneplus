package com.oneplus.settings;

import android.bluetooth.BluetoothAdapter;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.Bundle;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Selection;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.google.common.base.Ascii;
import com.oneplus.lib.widget.OPEditText;
import com.oneplus.settings.utils.OPUtils;

public class OPDeviceName extends SettingsPreferenceFragment implements OnClickListener {
    private static final int BLUETOOTH_NAME_MAX_LENGTH_BYTES = 32;
    private static String devicename;
    private static OPEditText mDeviceName;
    private static TextView mOKView;
    private static View mView;
    private MenuItem mOKMenuItem;
    private String nameTemp = null;

    class Utf8ByteLengthFilter implements InputFilter {
        private final int mMaxBytes;

        Utf8ByteLengthFilter(int maxBytes) {
            this.mMaxBytes = maxBytes;
        }

        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            int i;
            Spanned spanned;
            CharSequence charSequence = source;
            int i2 = end;
            int srcByteCount = 0;
            int i3 = start;
            while (true) {
                int i4 = 1;
                if (i3 >= i2) {
                    break;
                }
                char c = charSequence.charAt(i3);
                if (c >= 128) {
                    i4 = c < 2048 ? 2 : 3;
                }
                srcByteCount += i4;
                i3++;
            }
            i3 = dest.length();
            int destByteCount = 0;
            for (int i5 = 0; i5 < i3; i5++) {
                if (i5 < dstart) {
                    i = dend;
                } else if (i5 < dend) {
                    spanned = dest;
                }
                char c2 = dest.charAt(i5);
                int i6 = c2 < 128 ? 1 : c2 < 2048 ? 2 : 3;
                destByteCount += i6;
            }
            spanned = dest;
            int i7 = dstart;
            i = dend;
            int keepBytes = this.mMaxBytes - destByteCount;
            if (keepBytes <= 0) {
                return "";
            }
            if (keepBytes >= srcByteCount) {
                return null;
            }
            int keepBytes2 = keepBytes;
            for (keepBytes = start; keepBytes < i2; keepBytes++) {
                char c3 = charSequence.charAt(keepBytes);
                int i8 = c3 < 128 ? 1 : c3 < 2048 ? 2 : 3;
                keepBytes2 -= i8;
                if (keepBytes2 < 0) {
                    return charSequence.subSequence(start, keepBytes);
                }
            }
            return null;
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        getActivity().getWindow().setSoftInputMode(5);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.op_device_name, container, false);
        mDeviceName = (OPEditText) mView.findViewById(R.id.device_name);
        String modified = System.getString(getActivity().getContentResolver(), "oem_oneplus_modified_devicename");
        String mOPDeviceName = System.getString(getActivity().getContentResolver(), "oem_oneplus_devicename");
        mOPDeviceName = OPUtils.resetDeviceNameIfInvalid(getActivity());
        if (modified == null && (mOPDeviceName == null || mOPDeviceName.equals(OPUtils.COMPANY) || mOPDeviceName.equals("ONE E1001") || mOPDeviceName.equals("ONE E1003") || mOPDeviceName.equals("ONE E1005"))) {
            mOPDeviceName = SystemProperties.get("ro.display.series");
            System.putString(getActivity().getContentResolver(), "oem_oneplus_devicename", mOPDeviceName);
            System.putString(getActivity().getContentResolver(), "oem_oneplus_modified_devicename", "1");
        }
        if (mOPDeviceName.length() > 32) {
            mOPDeviceName = mOPDeviceName.substring(0, 31);
            System.putString(getActivity().getContentResolver(), "oem_oneplus_devicename", mOPDeviceName);
        }
        mDeviceName.setText(mOPDeviceName);
        if (mOPDeviceName != null) {
            mDeviceName.setSelection(mOPDeviceName.length());
        }
        mDeviceName.selectAll();
        mDeviceName.addTextChangedListener(new TextWatcher() {
            String name;
            int num;

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (OPDeviceName.mDeviceName != null && OPDeviceName.mDeviceName.length() != 0 && (OPDeviceName.mDeviceName.getText() instanceof Spannable)) {
                    OPDeviceName.this.nameTemp = OPDeviceName.mDeviceName.getText().toString();
                }
            }

            public void afterTextChanged(Editable s) {
                if (!(OPDeviceName.mDeviceName == null || OPDeviceName.mDeviceName.length() == 0)) {
                    this.name = OPDeviceName.mDeviceName.getText().toString();
                    this.num = this.name.getBytes().length;
                    if (this.num > 32) {
                        OPDeviceName.mDeviceName.setText(OPDeviceName.this.nameTemp);
                        CharSequence text = OPDeviceName.mDeviceName.getText();
                        if (text instanceof Spannable) {
                            Selection.setSelection((Spannable) text, text.length());
                        }
                    }
                }
                boolean flag = (s.length() == 0 || s.toString().trim().isEmpty()) ? false : true;
                OPDeviceName.mOKView.setEnabled(flag);
                if (OPDeviceName.this.mOKMenuItem != null) {
                    OPDeviceName.this.mOKMenuItem.setEnabled(flag);
                }
            }
        });
        mOKView = (TextView) mView.findViewById(R.id.ok);
        mOKView.setOnClickListener(this);
        setHasOptionsMenu(true);
        return mView;
    }

    public void onClick(View v) {
        if (v.getId() == R.id.ok) {
            String dn = mDeviceName.getText().toString().trim();
            if (dn.length() != 0) {
                System.putString(getActivity().getContentResolver(), "oem_oneplus_devicename", dn);
            }
            finish();
        }
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        this.mOKMenuItem = menu.add(0, 1, 0, R.string.oneplus_device_name_ok);
        this.mOKMenuItem.setEnabled(true).setShowAsAction(2);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private boolean isNotEmojiCharacter(char codePoint) {
        return codePoint == 0 || codePoint == 9 || codePoint == 10 || codePoint == 13 || ((codePoint >= ' ' && codePoint <= 55295) || ((codePoint >= 57344 && codePoint <= 65533) || (codePoint >= Ascii.MIN && codePoint <= 65535)));
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == 1) {
            String dn = mDeviceName.getText().toString().trim();
            if (dn.length() != 0) {
                if (dn.equalsIgnoreCase("null")) {
                    Toast.makeText(getActivity(), R.string.wifi_p2p_failed_rename_message, 0).show();
                    return true;
                }
                int i = 0;
                while (i < dn.length()) {
                    if (isNotEmojiCharacter(dn.charAt(i))) {
                        i++;
                    } else {
                        Toast.makeText(getActivity(), R.string.wifi_p2p_failed_rename_message, 0).show();
                        return true;
                    }
                }
                System.putString(getActivity().getContentResolver(), "oem_oneplus_devicename", dn);
                System.putString(getActivity().getContentResolver(), "oem_oneplus_modified_devicename", "1");
                BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                if (adapter != null) {
                    adapter.setName(dn);
                }
                WifiP2pManager mWifiP2pManager = (WifiP2pManager) getSystemService("wifip2p");
                Channel mChannel = mWifiP2pManager.initialize(getActivity(), getActivity().getMainLooper(), null);
                if (mWifiP2pManager != null) {
                    mWifiP2pManager.setDeviceName(mChannel, dn, null);
                }
            }
            finish();
            return true;
        } else if (itemId != 16908332) {
            return super.onOptionsItemSelected(item);
        } else {
            finish();
            return true;
        }
    }

    public void onResume() {
        super.onResume();
    }

    public int getMetricsCategory() {
        return OPUtils.ONEPLUS_METRICSLOGGER;
    }
}
