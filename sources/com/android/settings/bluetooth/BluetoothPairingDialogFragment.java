package com.android.settings.bluetooth;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.R;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;

public class BluetoothPairingDialogFragment extends InstrumentedDialogFragment implements TextWatcher, OnClickListener {
    private static final String TAG = "BTPairingDialogFragment";
    private Builder mBuilder;
    private AlertDialog mDialog;
    private BluetoothPairingController mPairingController;
    private BluetoothPairingDialog mPairingDialogActivity;
    private EditText mPairingView;

    public interface BluetoothPairingDialogListener {
        void onDialogNegativeClick(BluetoothPairingDialogFragment bluetoothPairingDialogFragment);

        void onDialogPositiveClick(BluetoothPairingDialogFragment bluetoothPairingDialogFragment);
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (!isPairingControllerSet()) {
            throw new IllegalStateException("Must call setPairingController() before showing dialog");
        } else if (isPairingDialogActivitySet()) {
            this.mBuilder = new Builder(getActivity());
            this.mDialog = setupDialog();
            this.mDialog.setCanceledOnTouchOutside(false);
            return this.mDialog;
        } else {
            throw new IllegalStateException("Must call setPairingDialogActivity() before showing dialog");
        }
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    public void afterTextChanged(Editable s) {
        Button positiveButton = this.mDialog.getButton(-1);
        if (positiveButton != null) {
            positiveButton.setEnabled(this.mPairingController.isPasskeyValid(s));
        }
        this.mPairingController.updateUserInput(s.toString());
    }

    public void onClick(DialogInterface dialog, int which) {
        if (which == -1) {
            this.mPairingController.onDialogPositiveClick(this);
        } else if (which == -2) {
            this.mPairingController.onDialogNegativeClick(this);
        }
        this.mPairingDialogActivity.dismiss();
    }

    public int getMetricsCategory() {
        return 613;
    }

    /* Access modifiers changed, original: protected */
    public AlertDialog getmDialog() {
        return this.mDialog;
    }

    /* Access modifiers changed, original: 0000 */
    public void setPairingController(BluetoothPairingController pairingController) {
        if (isPairingControllerSet()) {
            throw new IllegalStateException("The controller can only be set once. Forcibly replacing it will lead to undefined behavior");
        }
        this.mPairingController = pairingController;
    }

    /* Access modifiers changed, original: 0000 */
    public boolean isPairingControllerSet() {
        return this.mPairingController != null;
    }

    /* Access modifiers changed, original: 0000 */
    public void setPairingDialogActivity(BluetoothPairingDialog pairingDialogActivity) {
        if (isPairingDialogActivitySet()) {
            throw new IllegalStateException("The pairing dialog activity can only be set once");
        }
        this.mPairingDialogActivity = pairingDialogActivity;
    }

    /* Access modifiers changed, original: 0000 */
    public boolean isPairingDialogActivitySet() {
        return this.mPairingDialogActivity != null;
    }

    private AlertDialog setupDialog() {
        switch (this.mPairingController.getDialogType()) {
            case 0:
                return createUserEntryDialog();
            case 1:
                return createConsentDialog();
            case 2:
                return createDisplayPasskeyOrPinDialog();
            default:
                Log.e(TAG, "Incorrect pairing type received, not showing any dialog");
                return null;
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public CharSequence getPairingViewText() {
        if (this.mPairingView != null) {
            return this.mPairingView.getText();
        }
        return null;
    }

    private AlertDialog createUserEntryDialog() {
        this.mBuilder.setTitle(getString(R.string.bluetooth_pairing_request, new Object[]{this.mPairingController.getDeviceName()}));
        this.mBuilder.setView(createPinEntryView());
        this.mBuilder.setPositiveButton(getString(17039370), this);
        this.mBuilder.setNegativeButton(getString(17039360), this);
        AlertDialog dialog = this.mBuilder.create();
        dialog.setOnShowListener(new -$$Lambda$BluetoothPairingDialogFragment$ItV61WjNe_T4YaZN6BYGTBHLdZc(this));
        return dialog;
    }

    public static /* synthetic */ void lambda$createUserEntryDialog$0(BluetoothPairingDialogFragment bluetoothPairingDialogFragment, DialogInterface d) {
        if (TextUtils.isEmpty(bluetoothPairingDialogFragment.getPairingViewText())) {
            bluetoothPairingDialogFragment.mDialog.getButton(-1).setEnabled(false);
        }
        if (bluetoothPairingDialogFragment.mPairingView != null && bluetoothPairingDialogFragment.mPairingView.requestFocus()) {
            InputMethodManager imm = (InputMethodManager) bluetoothPairingDialogFragment.getContext().getSystemService("input_method");
            if (imm != null) {
                imm.showSoftInput(bluetoothPairingDialogFragment.mPairingView, 1);
            }
        }
    }

    private View createPinEntryView() {
        View view = getActivity().getLayoutInflater().inflate(R.layout.bluetooth_pin_entry, null);
        TextView messageViewCaptionHint = (TextView) view.findViewById(R.id.pin_values_hint);
        TextView messageView2 = (TextView) view.findViewById(R.id.message_below_pin);
        CheckBox alphanumericPin = (CheckBox) view.findViewById(R.id.alphanumeric_pin);
        CheckBox contactSharing = (CheckBox) view.findViewById(R.id.phonebook_sharing_message_entry_pin);
        contactSharing.setText(getString(R.string.bluetooth_pairing_shares_phonebook, new Object[]{this.mPairingController.getDeviceName()}));
        EditText pairingView = (EditText) view.findViewById(R.id.text);
        contactSharing.setVisibility(this.mPairingController.isProfileReady() ? 8 : 0);
        this.mPairingController.setContactSharingState();
        contactSharing.setOnCheckedChangeListener(this.mPairingController);
        contactSharing.setChecked(this.mPairingController.getContactSharingState());
        this.mPairingView = pairingView;
        pairingView.setInputType(2);
        pairingView.addTextChangedListener(this);
        alphanumericPin.setOnCheckedChangeListener(new -$$Lambda$BluetoothPairingDialogFragment$r7iz4I0mbAZSn1y-rbFsqcyiwC0(this));
        int messageId = this.mPairingController.getDeviceVariantMessageId();
        int messageIdHint = this.mPairingController.getDeviceVariantMessageHintId();
        int maxLength = this.mPairingController.getDeviceMaxPasskeyLength();
        alphanumericPin.setVisibility(this.mPairingController.pairingCodeIsAlphanumeric() ? 0 : 8);
        if (messageId != -1) {
            messageView2.setText(messageId);
        } else {
            messageView2.setVisibility(8);
        }
        if (messageIdHint != -1) {
            messageViewCaptionHint.setText(messageIdHint);
        } else {
            messageViewCaptionHint.setVisibility(8);
        }
        pairingView.setFilters(new InputFilter[]{new LengthFilter(maxLength)});
        return view;
    }

    public static /* synthetic */ void lambda$createPinEntryView$1(BluetoothPairingDialogFragment bluetoothPairingDialogFragment, CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            bluetoothPairingDialogFragment.mPairingView.setInputType(1);
        } else {
            bluetoothPairingDialogFragment.mPairingView.setInputType(2);
        }
    }

    private AlertDialog createConfirmationDialog() {
        this.mBuilder.setTitle(getString(R.string.bluetooth_pairing_request, new Object[]{this.mPairingController.getDeviceName()}));
        this.mBuilder.setView(createView());
        this.mBuilder.setPositiveButton(getString(R.string.bluetooth_pairing_accept), this);
        this.mBuilder.setNegativeButton(getString(R.string.bluetooth_pairing_decline), this);
        return this.mBuilder.create();
    }

    private AlertDialog createConsentDialog() {
        return createConfirmationDialog();
    }

    private AlertDialog createDisplayPasskeyOrPinDialog() {
        this.mBuilder.setTitle(getString(R.string.bluetooth_pairing_request, new Object[]{this.mPairingController.getDeviceName()}));
        this.mBuilder.setView(createView());
        this.mBuilder.setNegativeButton(getString(17039360), this);
        AlertDialog dialog = this.mBuilder.create();
        this.mPairingController.notifyDialogDisplayed();
        return dialog;
    }

    private View createView() {
        View view = getActivity().getLayoutInflater().inflate(R.layout.bluetooth_pin_confirm, null);
        TextView pairingViewCaption = (TextView) view.findViewById(R.id.pairing_caption);
        TextView pairingViewContent = (TextView) view.findViewById(R.id.pairing_subhead);
        TextView messagePairing = (TextView) view.findViewById(R.id.pairing_code_message);
        CheckBox contactSharing = (CheckBox) view.findViewById(R.id.phonebook_sharing_message_confirm_pin);
        contactSharing.setText(getString(R.string.bluetooth_pairing_shares_phonebook, new Object[]{this.mPairingController.getDeviceName()}));
        int i = 8;
        contactSharing.setVisibility(this.mPairingController.isProfileReady() ? 8 : 0);
        this.mPairingController.setContactSharingState();
        contactSharing.setChecked(this.mPairingController.getContactSharingState());
        contactSharing.setOnCheckedChangeListener(this.mPairingController);
        if (this.mPairingController.isDisplayPairingKeyVariant()) {
            i = 0;
        }
        messagePairing.setVisibility(i);
        if (this.mPairingController.hasPairingContent()) {
            pairingViewCaption.setVisibility(0);
            pairingViewContent.setVisibility(0);
            pairingViewContent.setText(this.mPairingController.getPairingContent());
        }
        return view;
    }
}
