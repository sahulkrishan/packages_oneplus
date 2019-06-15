package com.android.settings.fuelgauge;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.Checkable;
import android.widget.TextView;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.R;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.applications.ApplicationsState.AppEntry;
import com.android.settingslib.fuelgauge.PowerWhitelistBackend;

public class HighPowerDetail extends InstrumentedDialogFragment implements OnClickListener, View.OnClickListener {
    private static final String ARG_DEFAULT_ON = "default_on";
    @VisibleForTesting
    PowerWhitelistBackend mBackend;
    @VisibleForTesting
    BatteryUtils mBatteryUtils;
    private boolean mDefaultOn;
    @VisibleForTesting
    boolean mIsEnabled;
    private CharSequence mLabel;
    private Checkable mOptionOff;
    private Checkable mOptionOn;
    @VisibleForTesting
    String mPackageName;
    @VisibleForTesting
    int mPackageUid;

    public int getMetricsCategory() {
        return 540;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getContext();
        this.mBatteryUtils = BatteryUtils.getInstance(context);
        this.mBackend = PowerWhitelistBackend.getInstance(context);
        this.mPackageName = getArguments().getString("package");
        this.mPackageUid = getArguments().getInt("uid");
        PackageManager pm = context.getPackageManager();
        boolean z = false;
        try {
            this.mLabel = pm.getApplicationInfo(this.mPackageName, 0).loadLabel(pm);
        } catch (NameNotFoundException e) {
            this.mLabel = this.mPackageName;
        }
        this.mDefaultOn = getArguments().getBoolean(ARG_DEFAULT_ON);
        if (this.mDefaultOn || this.mBackend.isWhitelisted(this.mPackageName)) {
            z = true;
        }
        this.mIsEnabled = z;
    }

    public Checkable setup(View view, boolean on) {
        ((TextView) view.findViewById(16908310)).setText(on ? R.string.ignore_optimizations_on : R.string.ignore_optimizations_off);
        ((TextView) view.findViewById(16908304)).setText(on ? R.string.ignore_optimizations_on_desc : R.string.ignore_optimizations_off_desc);
        view.setClickable(true);
        view.setOnClickListener(this);
        if (!on && this.mBackend.isSysWhitelisted(this.mPackageName)) {
            view.setEnabled(false);
        }
        return (Checkable) view;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Builder b = new Builder(getContext()).setTitle(this.mLabel).setNegativeButton(R.string.cancel, null).setView(R.layout.ignore_optimizations_content);
        if (!this.mBackend.isSysWhitelisted(this.mPackageName)) {
            b.setPositiveButton(R.string.done, this);
        }
        return b.create();
    }

    public void onStart() {
        super.onStart();
        this.mOptionOn = setup(getDialog().findViewById(R.id.ignore_on), true);
        this.mOptionOff = setup(getDialog().findViewById(R.id.ignore_off), false);
        updateViews();
    }

    private void updateViews() {
        this.mOptionOn.setChecked(this.mIsEnabled);
        this.mOptionOff.setChecked(this.mIsEnabled ^ 1);
    }

    public void onClick(View v) {
        if (v == this.mOptionOn) {
            this.mIsEnabled = true;
            updateViews();
        } else if (v == this.mOptionOff) {
            this.mIsEnabled = false;
            updateViews();
        }
    }

    public void onClick(DialogInterface dialog, int which) {
        if (which == -1) {
            boolean newValue = this.mIsEnabled;
            if (newValue != this.mBackend.isWhitelisted(this.mPackageName)) {
                logSpecialPermissionChange(newValue, this.mPackageName, getContext());
                if (newValue) {
                    this.mBatteryUtils.setForceAppStandby(this.mPackageUid, this.mPackageName, 0);
                    this.mBackend.addApp(this.mPackageName);
                    return;
                }
                this.mBackend.removeApp(this.mPackageName);
            }
        }
    }

    @VisibleForTesting
    static void logSpecialPermissionChange(boolean whitelist, String packageName, Context context) {
        int logCategory;
        if (whitelist) {
            logCategory = 765;
        } else {
            logCategory = 764;
        }
        FeatureFactory.getFactory(context).getMetricsFeatureProvider().action(context, logCategory, packageName, new Pair[0]);
    }

    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        Fragment target = getTargetFragment();
        if (target != null && target.getActivity() != null) {
            target.onActivityResult(getTargetRequestCode(), 0, null);
        }
    }

    public static CharSequence getSummary(Context context, AppEntry entry) {
        return getSummary(context, entry.info.packageName);
    }

    public static CharSequence getSummary(Context context, String pkg) {
        int i;
        PowerWhitelistBackend powerWhitelist = PowerWhitelistBackend.getInstance(context);
        if (powerWhitelist.isSysWhitelisted(pkg)) {
            i = R.string.high_power_system;
        } else if (powerWhitelist.isWhitelisted(pkg)) {
            i = R.string.high_power_on;
        } else {
            i = R.string.high_power_off;
        }
        return context.getString(i);
    }

    public static void show(Fragment caller, int uid, String packageName, int requestCode) {
        HighPowerDetail fragment = new HighPowerDetail();
        Bundle args = new Bundle();
        args.putString("package", packageName);
        args.putInt("uid", uid);
        fragment.setArguments(args);
        fragment.setTargetFragment(caller, requestCode);
        fragment.show(caller.getFragmentManager(), HighPowerDetail.class.getSimpleName());
    }
}
