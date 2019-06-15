package com.android.settings.vpn2;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import com.android.settings.R;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;

public class ConfirmLockdownFragment extends InstrumentedDialogFragment implements OnClickListener {
    private static final String ARG_ALWAYS_ON = "always_on";
    private static final String ARG_LOCKDOWN_DST = "lockdown_new";
    private static final String ARG_LOCKDOWN_SRC = "lockdown_old";
    private static final String ARG_OPTIONS = "options";
    private static final String ARG_REPLACING = "replacing";
    private static final String TAG = "ConfirmLockdown";

    public interface ConfirmLockdownListener {
        void onConfirmLockdown(Bundle bundle, boolean z, boolean z2);
    }

    public int getMetricsCategory() {
        return 548;
    }

    public static boolean shouldShow(boolean replacing, boolean fromLockdown, boolean toLockdown) {
        return replacing || (toLockdown && !fromLockdown);
    }

    public static void show(Fragment parent, boolean replacing, boolean alwaysOn, boolean fromLockdown, boolean toLockdown, Bundle options) {
        if (parent.getFragmentManager().findFragmentByTag(TAG) == null) {
            Bundle args = new Bundle();
            args.putBoolean(ARG_REPLACING, replacing);
            args.putBoolean(ARG_ALWAYS_ON, alwaysOn);
            args.putBoolean(ARG_LOCKDOWN_SRC, fromLockdown);
            args.putBoolean(ARG_LOCKDOWN_DST, toLockdown);
            args.putParcelable(ARG_OPTIONS, options);
            ConfirmLockdownFragment frag = new ConfirmLockdownFragment();
            frag.setArguments(args);
            frag.setTargetFragment(parent, 0);
            frag.show(parent.getFragmentManager(), TAG);
        }
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int messageId;
        boolean replacing = getArguments().getBoolean(ARG_REPLACING);
        boolean alwaysOn = getArguments().getBoolean(ARG_ALWAYS_ON);
        boolean wasLockdown = getArguments().getBoolean(ARG_LOCKDOWN_SRC);
        boolean nowLockdown = getArguments().getBoolean(ARG_LOCKDOWN_DST);
        int titleId = nowLockdown ? R.string.vpn_require_connection_title : replacing ? R.string.vpn_replace_vpn_title : R.string.vpn_set_vpn_title;
        int actionId = replacing ? R.string.vpn_replace : nowLockdown ? R.string.vpn_turn_on : R.string.okay;
        if (nowLockdown) {
            if (replacing) {
                messageId = R.string.vpn_replace_always_on_vpn_enable_message;
            } else {
                messageId = R.string.vpn_first_always_on_vpn_message;
            }
        } else if (wasLockdown) {
            messageId = R.string.vpn_replace_always_on_vpn_disable_message;
        } else {
            messageId = R.string.vpn_replace_vpn_message;
        }
        return new Builder(getActivity()).setTitle(titleId).setMessage(messageId).setNegativeButton(R.string.cancel, null).setPositiveButton(actionId, this).create();
    }

    public void onClick(DialogInterface dialog, int which) {
        if (getTargetFragment() instanceof ConfirmLockdownListener) {
            ((ConfirmLockdownListener) getTargetFragment()).onConfirmLockdown((Bundle) getArguments().getParcelable(ARG_OPTIONS), getArguments().getBoolean(ARG_ALWAYS_ON), getArguments().getBoolean(ARG_LOCKDOWN_DST));
        }
    }
}
