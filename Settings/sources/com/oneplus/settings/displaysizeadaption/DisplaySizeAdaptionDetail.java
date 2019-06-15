package com.oneplus.settings.displaysizeadaption;

import android.app.AlertDialog.Builder;
import android.app.AppOpsManager;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Checkable;
import android.widget.TextView;
import android.widget.Toast;
import com.android.settings.R;
import com.android.settingslib.applications.ApplicationsState.AppEntry;
import com.oneplus.settings.SettingsBaseApplication;
import com.oneplus.settings.utils.OPUtils;

public class DisplaySizeAdaptionDetail extends DialogFragment implements OnClickListener, View.OnClickListener {
    private static final String ARG_DEFAULT_ON = "default_on";
    private static DisplaySizeAdaptiongeManager mManager;
    private AppOpsManager mAppOpsManager;
    private Context mContext;
    private Checkable mDefault;
    private Checkable mFullScreen;
    private CharSequence mLabel;
    private int mOriginValue;
    private Checkable mOriginalSize;
    private String mPackageName;
    private int mSelectedValue;
    private int mUid;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mPackageName = getArguments().getString("package");
        PackageManager pm = getContext().getPackageManager();
        this.mContext = getContext();
        try {
            this.mLabel = pm.getApplicationInfo(this.mPackageName, 0).loadLabel(pm);
            this.mUid = pm.getApplicationInfo(this.mPackageName, 0).uid;
        } catch (Exception e) {
            this.mLabel = this.mPackageName;
        }
        mManager = DisplaySizeAdaptiongeManager.getInstance(this.mContext);
        this.mSelectedValue = mManager.getAppTypeValue(this.mPackageName);
        this.mOriginValue = this.mSelectedValue;
    }

    public Checkable setup(View view, int value) {
        if (value == 1) {
            if (OPUtils.isSupportScreenCutting()) {
                ((TextView) view.findViewById(16908310)).setText(R.string.oneplus_screen_color_mode_default);
            } else {
                ((TextView) view.findViewById(16908310)).setText(R.string.oneplus_display_size_adaption_full_screen);
            }
        } else if (value == 0) {
            ((TextView) view.findViewById(16908310)).setText(R.string.oneplus_display_size_adaption_original_size);
        } else {
            ((TextView) view.findViewById(16908310)).setText(R.string.oneplus_app_display_fullscreen);
        }
        view.setClickable(true);
        view.setOnClickListener(this);
        return (Checkable) view;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new Builder(getContext()).setTitle(this.mLabel).setNegativeButton(R.string.cancel, null).setPositiveButton(R.string.done, this).setView(R.layout.op_display_size_content).create();
    }

    public void onStart() {
        super.onStart();
        this.mDefault = setup(getDialog().findViewById(R.id.ignore_default), 3);
        if (OPUtils.isSupportScreenCutting()) {
            getDialog().findViewById(R.id.ignore_default).setVisibility(0);
        } else {
            getDialog().findViewById(R.id.ignore_on).setVisibility(0);
        }
        this.mOriginalSize = setup(getDialog().findViewById(R.id.ignore_on), 0);
        this.mFullScreen = setup(getDialog().findViewById(R.id.ignore_off), 1);
        updateViews();
    }

    private void updateViews() {
        boolean z = false;
        this.mFullScreen.setChecked(this.mSelectedValue == 1);
        this.mOriginalSize.setChecked(this.mSelectedValue == 0);
        Checkable checkable = this.mDefault;
        if (this.mSelectedValue == 3 || this.mSelectedValue == 2) {
            z = true;
        }
        checkable.setChecked(z);
        if (this.mSelectedValue == 0 && OPUtils.isSupportScreenCutting()) {
            this.mFullScreen.setChecked(true);
        }
    }

    public void onClick(View v) {
        if (v == this.mFullScreen) {
            this.mSelectedValue = 1;
        } else if (v == this.mOriginalSize) {
            this.mSelectedValue = 0;
        } else if (v == this.mDefault) {
            this.mSelectedValue = 2;
        }
        updateViews();
    }

    public void onClick(DialogInterface dialog, int which) {
        if (which == -1) {
            if (this.mSelectedValue == 3) {
                this.mSelectedValue = 2;
            }
            mManager.setClassApp(this.mUid, this.mPackageName, this.mSelectedValue);
            if (OPUtils.isSupportScreenCutting() && this.mOriginValue != this.mSelectedValue) {
                Toast.makeText(this.mContext, this.mContext.getResources().getString(R.string.exception_hints), 1).show();
            }
        }
    }

    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        Fragment target = getTargetFragment();
        if (target != null) {
            target.onActivityResult(getTargetRequestCode(), 0, null);
        }
    }

    public static CharSequence getSummary(Context context, AppEntry entry) {
        return getSummary(context, entry.info.packageName);
    }

    public static CharSequence getSummary(Context context, String pkg) {
        int value = DisplaySizeAdaptiongeManager.getInstance(context).getAppTypeValue(pkg);
        if (value == 1) {
            if (OPUtils.isSupportScreenCutting()) {
                return SettingsBaseApplication.mApplication.getString(R.string.oneplus_screen_color_mode_default);
            }
            return SettingsBaseApplication.mApplication.getString(R.string.oneplus_display_size_adaption_full_screen);
        } else if (value != 0) {
            return SettingsBaseApplication.mApplication.getString(R.string.oneplus_app_display_fullscreen);
        } else {
            if (OPUtils.isSupportScreenCutting()) {
                return SettingsBaseApplication.mApplication.getString(R.string.oneplus_screen_color_mode_default);
            }
            return SettingsBaseApplication.mApplication.getString(R.string.oneplus_display_size_adaption_original_size);
        }
    }

    public static void show(Fragment caller, String packageName, int requestCode) {
        DisplaySizeAdaptionDetail fragment = new DisplaySizeAdaptionDetail();
        Bundle args = new Bundle();
        args.putString("package", packageName);
        fragment.setArguments(args);
        fragment.setTargetFragment(caller, requestCode);
        fragment.show(caller.getFragmentManager(), DisplaySizeAdaptionDetail.class.getSimpleName());
    }
}
