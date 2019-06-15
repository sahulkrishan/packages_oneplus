package com.oneplus.settings.packageuninstaller;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.android.settings.R;
import com.oneplus.settings.packageuninstaller.UninstallAppProgress.ProgressFragment;

public class UninstallAppProgressFragment extends Fragment implements OnClickListener, ProgressFragment {
    private static final String TAG = "UninstallAppProgressF";
    private Button mDeviceManagerButton;
    private Button mOkButton;
    private Button mUsersButton;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.op_uninstall_progress, container, false);
        PackageUtil.initSnippetForInstalledApp(getContext(), ((UninstallAppProgress) getActivity()).getAppInfo(), root.findViewById(R.id.app_snippet));
        this.mDeviceManagerButton = (Button) root.findViewById(R.id.device_manager_button);
        this.mUsersButton = (Button) root.findViewById(R.id.users_button);
        this.mDeviceManagerButton.setVisibility(8);
        this.mDeviceManagerButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClassName("com.android.settings", "com.android.settings.Settings$DeviceAdminSettingsActivity");
                intent.setFlags(1342177280);
                UninstallAppProgressFragment.this.startActivity(intent);
                UninstallAppProgressFragment.this.getActivity().finish();
            }
        });
        this.mUsersButton.setVisibility(8);
        this.mUsersButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent("android.settings.USER_SETTINGS");
                intent.setFlags(1342177280);
                UninstallAppProgressFragment.this.startActivity(intent);
                UninstallAppProgressFragment.this.getActivity().finish();
            }
        });
        this.mOkButton = (Button) root.findViewById(R.id.ok_button);
        this.mOkButton.setOnClickListener(this);
        return root;
    }

    public void onClick(View v) {
        UninstallAppProgress activity = (UninstallAppProgress) getActivity();
        if (v == this.mOkButton && activity != null) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Finished uninstalling pkg: ");
            stringBuilder.append(activity.getAppInfo().packageName);
            Log.i(str, stringBuilder.toString());
            activity.setResultAndFinish();
        }
    }

    public void setUsersButtonVisible(boolean visible) {
        this.mUsersButton.setVisibility(visible ? 0 : 8);
    }

    public void setDeviceManagerButtonVisible(boolean visible) {
        this.mDeviceManagerButton.setVisibility(visible ? 0 : 8);
    }

    public void showCompletion(CharSequence statusText) {
        View root = getView();
        root.findViewById(R.id.progress_view).setVisibility(8);
        root.findViewById(R.id.status_view).setVisibility(0);
        ((TextView) root.findViewById(R.id.status_text)).setText(statusText);
        root.findViewById(R.id.ok_panel).setVisibility(0);
    }
}
