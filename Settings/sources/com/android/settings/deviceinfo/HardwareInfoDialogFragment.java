package com.android.settings.deviceinfo;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemProperties;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;

public class HardwareInfoDialogFragment extends InstrumentedDialogFragment {
    public static final String TAG = "HardwareInfo";

    public int getMetricsCategory() {
        return 862;
    }

    public static HardwareInfoDialogFragment newInstance() {
        return new HardwareInfoDialogFragment();
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Builder builder = new Builder(getActivity()).setTitle(R.string.hardware_info).setPositiveButton(17039370, null);
        View content = LayoutInflater.from(builder.getContext()).inflate(R.layout.dialog_hardware_info, null);
        setText(content, R.id.model_label, R.id.model_value, DeviceModelPreferenceController.getDeviceModel());
        setText(content, R.id.serial_number_label, R.id.serial_number_value, getSerialNumber());
        setText(content, R.id.hardware_rev_label, R.id.hardware_rev_value, SystemProperties.get("ro.boot.hardware.revision"));
        return builder.setView(content).create();
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void setText(View content, int labelViewId, int valueViewId, String value) {
        if (content != null) {
            View labelView = content.findViewById(labelViewId);
            TextView valueView = (TextView) content.findViewById(valueViewId);
            if (TextUtils.isEmpty(value)) {
                labelView.setVisibility(8);
                valueView.setVisibility(8);
            } else {
                labelView.setVisibility(0);
                valueView.setVisibility(0);
                valueView.setText(value);
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public String getSerialNumber() {
        return Build.getSerial();
    }
}
