package com.android.settings.fuelgauge.anomaly;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import com.android.settings.R;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;

public class AnomalyDialogFragment extends InstrumentedDialogFragment implements OnClickListener {
    private static final String ARG_ANOMALY = "anomaly";
    private static final String ARG_METRICS_KEY = "metrics_key";
    @VisibleForTesting
    Anomaly mAnomaly;
    @VisibleForTesting
    AnomalyUtils mAnomalyUtils;

    public interface AnomalyDialogListener {
        void onAnomalyHandled(Anomaly anomaly);
    }

    public static AnomalyDialogFragment newInstance(Anomaly anomaly, int metricsKey) {
        AnomalyDialogFragment dialogFragment = new AnomalyDialogFragment();
        Bundle args = new Bundle(2);
        args.putParcelable("anomaly", anomaly);
        args.putInt(ARG_METRICS_KEY, metricsKey);
        dialogFragment.setArguments(args);
        return dialogFragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initAnomalyUtils();
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void initAnomalyUtils() {
        this.mAnomalyUtils = AnomalyUtils.getInstance(getContext());
    }

    public int getMetricsCategory() {
        return 988;
    }

    public void onClick(DialogInterface dialog, int which) {
        AnomalyDialogListener lsn = (AnomalyDialogListener) getTargetFragment();
        if (lsn != null) {
            this.mAnomalyUtils.getAnomalyAction(this.mAnomaly).handlePositiveAction(this.mAnomaly, getArguments().getInt(ARG_METRICS_KEY));
            lsn.onAnomalyHandled(this.mAnomaly);
        }
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        Context context = getContext();
        AnomalyUtils anomalyUtils = AnomalyUtils.getInstance(context);
        this.mAnomaly = (Anomaly) bundle.getParcelable("anomaly");
        anomalyUtils.logAnomaly(this.mMetricsFeatureProvider, this.mAnomaly, 988);
        int actionType = this.mAnomalyUtils.getAnomalyAction(this.mAnomaly).getActionType();
        if (actionType != 0) {
            switch (actionType) {
                case 2:
                    return new Builder(context).setTitle(R.string.dialog_location_title).setMessage(getString(R.string.dialog_location_message, new Object[]{this.mAnomaly.displayName})).setPositiveButton(R.string.dialog_location_ok, this).setNegativeButton(R.string.dlg_cancel, null).create();
                case 3:
                    return new Builder(context).setTitle(R.string.dialog_background_check_title).setMessage(getString(R.string.dialog_background_check_message, new Object[]{this.mAnomaly.displayName})).setPositiveButton(R.string.dialog_background_check_ok, this).setNegativeButton(R.string.dlg_cancel, null).create();
                default:
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("unknown type ");
                    stringBuilder.append(this.mAnomaly.type);
                    throw new IllegalArgumentException(stringBuilder.toString());
            }
        }
        int i;
        Builder title = new Builder(context).setTitle(R.string.dialog_stop_title);
        if (this.mAnomaly.type == 0) {
            i = R.string.dialog_stop_message;
        } else {
            i = R.string.dialog_stop_message_wakeup_alarm;
        }
        return title.setMessage(getString(i, new Object[]{this.mAnomaly.displayName})).setPositiveButton(R.string.dialog_stop_ok, this).setNegativeButton(R.string.dlg_cancel, null).create();
    }
}
