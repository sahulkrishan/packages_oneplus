package com.android.settings.password;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import com.android.settings.R;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;
import com.oneplus.settings.utils.OPUtils;

public class SetupSkipDialog extends InstrumentedDialogFragment implements OnClickListener {
    private static final String ARG_FRP_SUPPORTED = "frp_supported";
    public static final String EXTRA_FRP_SUPPORTED = ":settings:frp_supported";
    public static final int RESULT_SKIP = 11;
    private static final String TAG_SKIP_DIALOG = "skip_dialog";

    public static SetupSkipDialog newInstance(boolean isFrpSupported) {
        SetupSkipDialog dialog = new SetupSkipDialog();
        Bundle args = new Bundle();
        args.putBoolean(ARG_FRP_SUPPORTED, isFrpSupported);
        dialog.setArguments(args);
        return dialog;
    }

    public int getMetricsCategory() {
        return 573;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return onCreateDialogBuilder().create();
    }

    @NonNull
    public Builder onCreateDialogBuilder() {
        int i;
        Bundle args = getArguments();
        Builder title = new Builder(getContext()).setPositiveButton(R.string.skip_anyway_button_label, this).setNegativeButton(R.string.go_back_button_label, this).setTitle(R.string.lock_screen_intro_skip_title);
        if (args.getBoolean(ARG_FRP_SUPPORTED)) {
            i = R.string.lock_screen_intro_skip_dialog_text_frp;
        } else {
            i = R.string.oneplus_lock_screen_intro_skip_dialog_text;
        }
        return title.setMessage(i);
    }

    public void onClick(DialogInterface dialog, int button) {
        if (button == -1) {
            new Handler().postDelayed(new Runnable() {
                final Activity activity = SetupSkipDialog.this.getActivity();

                public void run() {
                    if (this.activity != null) {
                        if (OPUtils.isO2()) {
                            this.activity.setResult(11);
                            this.activity.finish();
                        } else {
                            try {
                                ComponentName componentName;
                                Intent intent = new Intent();
                                if (OPUtils.isGuestMode()) {
                                    componentName = new ComponentName("com.oneplus.provision", "com.oneplus.provision.UserSettingSuccess");
                                } else {
                                    componentName = new ComponentName("com.oneplus.provision", "com.oneplus.provision.GesturesActivity");
                                }
                                intent.setComponent(componentName);
                                this.activity.startActivity(intent);
                                this.activity.overridePendingTransition(R.anim.op_slide_in, R.anim.op_slide_out);
                            } catch (ActivityNotFoundException e) {
                                e.printStackTrace();
                                this.activity.finish();
                            }
                        }
                    }
                }
            }, 300);
        }
    }

    public void show(FragmentManager manager) {
        show(manager, TAG_SKIP_DIALOG);
    }
}
