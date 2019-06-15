package com.oneplus.settings.backgroundoptimize;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.widget.Checkable;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settingslib.applications.ApplicationsState.AppEntry;
import com.oneplus.settings.SettingsBaseApplication;

public class BgOptimizeDetail extends DialogFragment implements OnClickListener, View.OnClickListener {
    private static final String ARG_DEFAULT_ON = "default_on";
    private static BgOptimizeDetail mFragment;
    private boolean mCurrentOptimized;
    private CharSequence mLabel;
    private Checkable mOptionNoOptimze;
    private Checkable mOptionOptimze;
    private String mPackageName;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mPackageName = getArguments().getString("package");
        PackageManager pm = getContext().getPackageManager();
        boolean z = false;
        try {
            this.mLabel = pm.getApplicationInfo(this.mPackageName, 0).loadLabel(pm);
        } catch (NameNotFoundException e) {
            this.mLabel = this.mPackageName;
        }
        if (BgOActivityManager.getInstance(getContext()).getAppControlMode(this.mPackageName, 0) == 0) {
            z = true;
        }
        this.mCurrentOptimized = z;
    }

    public Checkable setup(View view, boolean on) {
        ((TextView) view.findViewById(16908310)).setText(on ? R.string.ignore_optimizations_on : R.string.ignore_optimizations_off);
        ((TextView) view.findViewById(16908304)).setText(on ? R.string.not_optimize_summery : R.string.optimize_summery);
        view.setClickable(true);
        view.setOnClickListener(this);
        return (Checkable) view;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new Builder(getContext()).setTitle(this.mLabel).setNegativeButton(R.string.cancel, null).setPositiveButton(R.string.done, this).setView(R.layout.ignore_optimizations_content).create();
    }

    public void onStart() {
        super.onStart();
        this.mOptionNoOptimze = setup(getDialog().findViewById(R.id.ignore_on), true);
        this.mOptionOptimze = setup(getDialog().findViewById(R.id.ignore_off), false);
        updateViews();
    }

    private void updateViews() {
        this.mOptionOptimze.setChecked(this.mCurrentOptimized);
        this.mOptionNoOptimze.setChecked(this.mCurrentOptimized ^ 1);
    }

    public void onClick(View v) {
        if (v == this.mOptionOptimze) {
            this.mCurrentOptimized = true;
        } else if (v == this.mOptionNoOptimze) {
            this.mCurrentOptimized = false;
        }
        updateViews();
    }

    public void onClick(DialogInterface dialog, int which) {
        if (which == -1) {
            BgOActivityManager.getInstance(getContext()).setAppControlMode(this.mPackageName, 0, this.mCurrentOptimized ^ 1);
        }
    }

    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        Fragment target = getTargetFragment();
        if (target != null) {
            target.onActivityResult(getTargetRequestCode(), 0, new Intent());
        }
    }

    public static CharSequence getSummary(Context context, AppEntry entry) {
        return getSummary(context, entry.info.packageName);
    }

    public static CharSequence getSummary(Context context, String pkg) {
        return SettingsBaseApplication.mApplication.getString(BgOActivityManager.getInstance(context).getAppControlMode(pkg, 0) == 0 ? R.string.ignore_optimizations_off : R.string.ignore_optimizations_on);
    }

    public static void show(Fragment caller, String packageName, int requestCode) {
        mFragment = null;
        mFragment = new BgOptimizeDetail();
        Bundle args = new Bundle();
        args.putString("package", packageName);
        mFragment.setArguments(args);
        mFragment.setTargetFragment(caller, requestCode);
        mFragment.show(caller.getFragmentManager(), BgOptimizeDetail.class.getSimpleName());
    }
}
