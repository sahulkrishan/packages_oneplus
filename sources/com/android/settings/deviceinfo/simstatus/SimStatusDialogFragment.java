package com.android.settings.deviceinfo.simstatus;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;

public class SimStatusDialogFragment extends InstrumentedDialogFragment {
    private static final String DIALOG_TITLE_BUNDLE_KEY = "arg_key_dialog_title";
    private static final String SIM_SLOT_BUNDLE_KEY = "arg_key_sim_slot";
    private static final String TAG = "SimStatusDialog";
    private SimStatusDialogController mController;
    private View mRootView;

    public int getMetricsCategory() {
        return 1246;
    }

    public static void show(Fragment host, int slotId, String dialogTitle) {
        FragmentManager manager = host.getChildFragmentManager();
        if (manager.findFragmentByTag(TAG) == null) {
            Bundle bundle = new Bundle();
            bundle.putInt(SIM_SLOT_BUNDLE_KEY, slotId);
            bundle.putString(DIALOG_TITLE_BUNDLE_KEY, dialogTitle);
            SimStatusDialogFragment dialog = new SimStatusDialogFragment();
            dialog.setArguments(bundle);
            dialog.show(manager, TAG);
        }
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        int slotId = bundle.getInt(SIM_SLOT_BUNDLE_KEY);
        String dialogTitle = bundle.getString(DIALOG_TITLE_BUNDLE_KEY);
        this.mController = new SimStatusDialogController(this, this.mLifecycle, slotId);
        Builder builder = new Builder(getActivity()).setTitle(dialogTitle).setPositiveButton(17039370, null);
        this.mRootView = LayoutInflater.from(builder.getContext()).inflate(R.layout.dialog_sim_status, null);
        this.mController.initialize();
        return builder.setView(this.mRootView).create();
    }

    public void removeSettingFromScreen(int viewId) {
        View view = this.mRootView.findViewById(viewId);
        if (view != null) {
            view.setVisibility(8);
        }
    }

    public void setText(int viewId, CharSequence text) {
        TextView textView = (TextView) this.mRootView.findViewById(viewId);
        if (TextUtils.isEmpty(text)) {
            text = getResources().getString(R.string.device_info_default);
        }
        if (textView != null) {
            textView.setText(text);
        }
    }
}
