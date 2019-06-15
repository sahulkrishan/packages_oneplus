package com.android.settings.bluetooth;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;

abstract class BluetoothNameDialogFragment extends InstrumentedDialogFragment implements TextWatcher {
    private static final String KEY_NAME = "device_name";
    private static final String KEY_NAME_EDITED = "device_name_edited";
    private AlertDialog mAlertDialog;
    private boolean mDeviceNameEdited;
    private boolean mDeviceNameUpdated;
    EditText mDeviceNameView;
    private Button mOkButton;

    public abstract String getDeviceName();

    public abstract int getDialogTitle();

    public abstract void setDeviceName(String str);

    BluetoothNameDialogFragment() {
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String deviceName = getDeviceName();
        if (savedInstanceState != null) {
            deviceName = savedInstanceState.getString(KEY_NAME, deviceName);
            this.mDeviceNameEdited = savedInstanceState.getBoolean(KEY_NAME_EDITED, false);
        }
        this.mAlertDialog = new Builder(getActivity()).setTitle(getDialogTitle()).setView(createDialogView(deviceName)).setPositiveButton(R.string.bluetooth_rename_button, new -$$Lambda$BluetoothNameDialogFragment$pGuotXbZkr5ej_7pdbB840goZcw(this)).setNegativeButton(17039360, null).create();
        this.mAlertDialog.setOnShowListener(new -$$Lambda$BluetoothNameDialogFragment$UwiP0mVIJKgl9XIciCSL5BjtkO4(this));
        return this.mAlertDialog;
    }

    public static /* synthetic */ void lambda$onCreateDialog$1(BluetoothNameDialogFragment bluetoothNameDialogFragment, DialogInterface d) {
        if (bluetoothNameDialogFragment.mDeviceNameView != null && bluetoothNameDialogFragment.mDeviceNameView.requestFocus()) {
            InputMethodManager imm = (InputMethodManager) bluetoothNameDialogFragment.getContext().getSystemService("input_method");
            if (imm != null) {
                imm.showSoftInput(bluetoothNameDialogFragment.mDeviceNameView, 1);
            }
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putString(KEY_NAME, this.mDeviceNameView.getText().toString());
        outState.putBoolean(KEY_NAME_EDITED, this.mDeviceNameEdited);
    }

    private View createDialogView(String deviceName) {
        View view = ((LayoutInflater) getActivity().getSystemService("layout_inflater")).inflate(R.layout.dialog_edittext, null);
        this.mDeviceNameView = (EditText) view.findViewById(R.id.edittext);
        this.mDeviceNameView.setFilters(new InputFilter[]{new BluetoothLengthDeviceNameFilter()});
        this.mDeviceNameView.setText(deviceName);
        if (!TextUtils.isEmpty(deviceName)) {
            this.mDeviceNameView.setSelection(deviceName.length());
        }
        this.mDeviceNameView.addTextChangedListener(this);
        Utils.setEditTextCursorPosition(this.mDeviceNameView);
        this.mDeviceNameView.setOnEditorActionListener(new OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId != 6) {
                    return false;
                }
                if (!(v.length() == 0 || v.getText().toString().trim().isEmpty())) {
                    BluetoothNameDialogFragment.this.setDeviceName(v.getText().toString().trim());
                }
                BluetoothNameDialogFragment.this.mAlertDialog.dismiss();
                return true;
            }
        });
        return view;
    }

    public void onDestroy() {
        super.onDestroy();
        this.mAlertDialog = null;
        this.mDeviceNameView = null;
        this.mOkButton = null;
    }

    public void onResume() {
        super.onResume();
        if (this.mOkButton == null) {
            this.mOkButton = this.mAlertDialog.getButton(-1);
            this.mOkButton.setEnabled(this.mDeviceNameEdited);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void updateDeviceName() {
        String name = getDeviceName();
        if (name != null) {
            this.mDeviceNameUpdated = true;
            this.mDeviceNameEdited = false;
            this.mDeviceNameView.setText(name);
        }
    }

    public void afterTextChanged(Editable s) {
        if (this.mDeviceNameUpdated) {
            this.mDeviceNameUpdated = false;
            this.mOkButton.setEnabled(false);
            return;
        }
        boolean z = true;
        this.mDeviceNameEdited = true;
        if (this.mOkButton != null) {
            Button button = this.mOkButton;
            if (s.toString().trim().length() == 0) {
                z = false;
            }
            button.setEnabled(z);
        }
    }

    public void onConfigurationChanged(Configuration newConfig, CharSequence s) {
        super.onConfigurationChanged(newConfig);
        if (this.mOkButton != null) {
            Button button = this.mOkButton;
            boolean z = (s.length() == 0 || s.toString().trim().isEmpty()) ? false : true;
            button.setEnabled(z);
        }
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }
}
