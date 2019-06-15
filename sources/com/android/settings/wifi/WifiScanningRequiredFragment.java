package com.android.settings.wifi;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings.Global;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.android.settings.R;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;
import com.android.settingslib.HelpUtils;

public class WifiScanningRequiredFragment extends InstrumentedDialogFragment implements OnClickListener {
    private static final String TAG = "WifiScanReqFrag";

    public static WifiScanningRequiredFragment newInstance() {
        return new WifiScanningRequiredFragment();
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Builder builder = new Builder(getContext()).setTitle(R.string.wifi_settings_scanning_required_title).setView(R.layout.wifi_settings_scanning_required_view).setPositiveButton(R.string.wifi_settings_scanning_required_turn_on, this).setNegativeButton(R.string.cancel, null);
        addButtonIfNeeded(builder);
        return builder.create();
    }

    public int getMetricsCategory() {
        return 1373;
    }

    public void onClick(DialogInterface dialog, int which) {
        Context context = getContext();
        ContentResolver contentResolver = context.getContentResolver();
        if (which == -3) {
            openHelpPage();
        } else if (which == -1) {
            Global.putInt(contentResolver, "wifi_scan_always_enabled", 1);
            Toast.makeText(context, context.getString(R.string.wifi_settings_scanning_required_enabled), 0).show();
            if (getTargetFragment() != null) {
                getTargetFragment().onActivityResult(getTargetRequestCode(), -1, null);
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void addButtonIfNeeded(Builder builder) {
        if (!TextUtils.isEmpty(getContext().getString(R.string.help_uri_wifi_scanning_required))) {
            builder.setNeutralButton(R.string.learn_more, this);
        }
    }

    private void openHelpPage() {
        Intent intent = getHelpIntent(getContext());
        if (intent != null) {
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Activity was not found for intent, ");
                stringBuilder.append(intent.toString());
                Log.e(str, stringBuilder.toString());
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public Intent getHelpIntent(Context context) {
        return HelpUtils.getHelpIntent(context, context.getString(R.string.help_uri_wifi_scanning_required), context.getClass().getName());
    }
}
