package com.android.settings.deviceinfo.firmwareversion;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;

public class FirmwareVersionDialogFragment extends InstrumentedDialogFragment {
    private static final String TAG = "firmwareVersionDialog";
    private View mRootView;

    public static void show(Fragment host) {
        FragmentManager manager = host.getChildFragmentManager();
        if (manager.findFragmentByTag(TAG) == null) {
            new FirmwareVersionDialogFragment().show(manager, TAG);
        }
    }

    public int getMetricsCategory() {
        return 1247;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Builder builder = new Builder(getActivity()).setTitle(R.string.firmware_title).setPositiveButton(17039370, null);
        this.mRootView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_firmware_version, null);
        initializeControllers();
        return builder.setView(this.mRootView).create();
    }

    public void setText(int viewId, CharSequence text) {
        TextView view = (TextView) this.mRootView.findViewById(viewId);
        if (view != null) {
            view.setText(text);
        }
    }

    public void removeSettingFromScreen(int viewId) {
        View view = this.mRootView.findViewById(viewId);
        if (view != null) {
            view.setVisibility(8);
        }
    }

    public void registerClickListener(int viewId, OnClickListener listener) {
        View view = this.mRootView.findViewById(viewId);
        if (view != null) {
            view.setOnClickListener(listener);
        }
    }

    private void initializeControllers() {
        new FirmwareVersionDialogController(this).initialize();
        new SecurityPatchLevelDialogController(this).initialize();
        new BasebandVersionDialogController(this).initialize();
        new KernelVersionDialogController(this).initialize();
        new BuildNumberDialogController(this).initialize();
    }
}
