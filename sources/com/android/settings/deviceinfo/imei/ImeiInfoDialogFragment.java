package com.android.settings.deviceinfo.imei;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;

public class ImeiInfoDialogFragment extends InstrumentedDialogFragment {
    private static final String DIALOG_TITLE_BUNDLE_KEY = "arg_key_dialog_title";
    private static final String SLOT_ID_BUNDLE_KEY = "arg_key_slot_id";
    @VisibleForTesting
    static final String TAG = "ImeiInfoDialog";
    private View mRootView;

    public static void show(@NonNull Fragment host, int slotId, String dialogTitle) {
        FragmentManager manager = host.getChildFragmentManager();
        if (manager.findFragmentByTag(TAG) == null) {
            Bundle bundle = new Bundle();
            bundle.putInt(SLOT_ID_BUNDLE_KEY, slotId);
            bundle.putString(DIALOG_TITLE_BUNDLE_KEY, dialogTitle);
            ImeiInfoDialogFragment dialog = new ImeiInfoDialogFragment();
            dialog.setArguments(bundle);
            dialog.show(manager, TAG);
        }
    }

    public int getMetricsCategory() {
        return 1240;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        int slotId = bundle.getInt(SLOT_ID_BUNDLE_KEY);
        String dialogTitle = bundle.getString(DIALOG_TITLE_BUNDLE_KEY);
        ImeiInfoDialogController controller = new ImeiInfoDialogController(this, slotId);
        Builder builder = new Builder(getActivity()).setTitle(dialogTitle).setPositiveButton(17039370, null);
        this.mRootView = LayoutInflater.from(builder.getContext()).inflate(R.layout.dialog_imei_info, null);
        controller.populateImeiInfo();
        return builder.setView(this.mRootView).create();
    }

    public void removeViewFromScreen(int viewId) {
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
