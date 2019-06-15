package com.android.settings.fuelgauge.batterytip;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.Utils;
import com.android.settings.core.InstrumentedPreferenceFragment;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;
import com.android.settings.fuelgauge.batterytip.BatteryTipPreferenceController.BatteryTipListener;
import com.android.settings.fuelgauge.batterytip.actions.BatteryTipAction;
import com.android.settings.fuelgauge.batterytip.tips.BatteryTip;
import com.android.settings.fuelgauge.batterytip.tips.HighUsageTip;
import java.util.List;

public class BatteryTipDialogFragment extends InstrumentedDialogFragment implements OnClickListener {
    private static final String ARG_BATTERY_TIP = "battery_tip";
    private static final String ARG_METRICS_KEY = "metrics_key";
    @VisibleForTesting
    BatteryTip mBatteryTip;
    @VisibleForTesting
    int mMetricsKey;

    public static BatteryTipDialogFragment newInstance(BatteryTip batteryTip, int metricsKey) {
        BatteryTipDialogFragment dialogFragment = new BatteryTipDialogFragment();
        Bundle args = new Bundle(1);
        args.putParcelable(ARG_BATTERY_TIP, batteryTip);
        args.putInt(ARG_METRICS_KEY, metricsKey);
        dialogFragment.setArguments(args);
        return dialogFragment;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        Context context = getContext();
        this.mBatteryTip = (BatteryTip) bundle.getParcelable(ARG_BATTERY_TIP);
        this.mMetricsKey = bundle.getInt(ARG_METRICS_KEY);
        RecyclerView restrictionView;
        switch (this.mBatteryTip.getType()) {
            case 1:
                List<AppInfo> restrictedAppList = this.mBatteryTip.getRestrictAppList();
                int num = restrictedAppList.size();
                CharSequence appLabel = Utils.getApplicationLabel(context, ((AppInfo) restrictedAppList.get(0)).packageName);
                Builder builder = new Builder(context).setTitle(context.getResources().getQuantityString(R.plurals.battery_tip_restrict_app_dialog_title, num, new Object[]{Integer.valueOf(num)})).setPositiveButton(R.string.battery_tip_restrict_app_dialog_ok, this).setNegativeButton(17039360, null);
                if (num == 1) {
                    builder.setMessage(getString(R.string.battery_tip_restrict_app_dialog_message, new Object[]{appLabel}));
                } else if (num <= 5) {
                    builder.setMessage(getString(R.string.battery_tip_restrict_apps_less_than_5_dialog_message));
                    restrictionView = (RecyclerView) LayoutInflater.from(context).inflate(R.layout.recycler_view, null);
                    restrictionView.setLayoutManager(new LinearLayoutManager(context));
                    restrictionView.setAdapter(new HighUsageAdapter(context, restrictedAppList));
                    builder.setView(restrictionView);
                } else {
                    builder.setMessage(context.getString(R.string.battery_tip_restrict_apps_more_than_5_dialog_message, new Object[]{restrictAppTip.getRestrictAppsString(context)}));
                }
                return builder.create();
            case 2:
                HighUsageTip highUsageTip = this.mBatteryTip;
                restrictionView = (RecyclerView) LayoutInflater.from(context).inflate(R.layout.recycler_view, null);
                restrictionView.setLayoutManager(new LinearLayoutManager(context));
                restrictionView.setAdapter(new HighUsageAdapter(context, highUsageTip.getHighUsageAppList()));
                return new Builder(context).setMessage(getString(R.string.battery_tip_dialog_message, new Object[]{Integer.valueOf(highUsageTip.getHighUsageAppList().size())})).setView(restrictionView).setPositiveButton(17039370, null).create();
            case 6:
                return new Builder(context).setMessage(R.string.battery_tip_dialog_summary_message).setPositiveButton(17039370, null).create();
            case 7:
                CharSequence name = Utils.getApplicationLabel(context, this.mBatteryTip.getPackageName());
                return new Builder(context).setTitle(getString(R.string.battery_tip_unrestrict_app_dialog_title)).setMessage(R.string.battery_tip_unrestrict_app_dialog_message).setPositiveButton(R.string.battery_tip_unrestrict_app_dialog_ok, this).setNegativeButton(R.string.battery_tip_unrestrict_app_dialog_cancel, null).create();
            default:
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("unknown type ");
                stringBuilder.append(this.mBatteryTip.getType());
                throw new IllegalArgumentException(stringBuilder.toString());
        }
    }

    public int getMetricsCategory() {
        return 1323;
    }

    public void onClick(DialogInterface dialog, int which) {
        BatteryTipListener lsn = (BatteryTipListener) getTargetFragment();
        if (lsn != null) {
            BatteryTipAction action = BatteryTipUtils.getActionForBatteryTip(this.mBatteryTip, (SettingsActivity) getActivity(), (InstrumentedPreferenceFragment) getTargetFragment());
            if (action != null) {
                action.handlePositiveAction(this.mMetricsKey);
            }
            lsn.onBatteryTipHandled(this.mBatteryTip);
        }
    }
}
