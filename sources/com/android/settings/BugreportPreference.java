package com.android.settings;

import android.app.ActivityManager;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.TextView;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.CustomDialogPreference;

public class BugreportPreference extends CustomDialogPreference {
    private static final String TAG = "BugreportPreference";
    private TextView mFullSummary;
    private CheckedTextView mFullTitle;
    private TextView mInteractiveSummary;
    private CheckedTextView mInteractiveTitle;

    public BugreportPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /* Access modifiers changed, original: protected */
    public void onPrepareDialogBuilder(Builder builder, OnClickListener listener) {
        super.onPrepareDialogBuilder(builder, listener);
        View dialogView = View.inflate(getContext(), R.layout.bugreport_options_dialog, null);
        this.mInteractiveTitle = (CheckedTextView) dialogView.findViewById(R.id.bugreport_option_interactive_title);
        this.mInteractiveSummary = (TextView) dialogView.findViewById(R.id.bugreport_option_interactive_summary);
        this.mFullTitle = (CheckedTextView) dialogView.findViewById(R.id.bugreport_option_full_title);
        this.mFullSummary = (TextView) dialogView.findViewById(R.id.bugreport_option_full_summary);
        View.OnClickListener l = new View.OnClickListener() {
            public void onClick(View v) {
                if (v == BugreportPreference.this.mFullTitle || v == BugreportPreference.this.mFullSummary) {
                    BugreportPreference.this.mInteractiveTitle.setChecked(false);
                    BugreportPreference.this.mFullTitle.setChecked(true);
                }
                if (v == BugreportPreference.this.mInteractiveTitle || v == BugreportPreference.this.mInteractiveSummary) {
                    BugreportPreference.this.mInteractiveTitle.setChecked(true);
                    BugreportPreference.this.mFullTitle.setChecked(false);
                }
            }
        };
        this.mInteractiveTitle.setOnClickListener(l);
        this.mFullTitle.setOnClickListener(l);
        this.mInteractiveSummary.setOnClickListener(l);
        this.mFullSummary.setOnClickListener(l);
        builder.setPositiveButton(17040820, listener);
        builder.setView(dialogView);
    }

    /* Access modifiers changed, original: protected */
    public void onClick(DialogInterface dialog, int which) {
        if (which == -1) {
            Context context = getContext();
            if (this.mFullTitle.isChecked()) {
                Log.v(TAG, "Taking full bugreport right away");
                FeatureFactory.getFactory(context).getMetricsFeatureProvider().action(context, 295, new Pair[0]);
                takeBugreport(0);
                return;
            }
            Log.v(TAG, "Taking interactive bugreport right away");
            FeatureFactory.getFactory(context).getMetricsFeatureProvider().action(context, 294, new Pair[0]);
            takeBugreport(1);
        }
    }

    private void takeBugreport(int bugreportType) {
        try {
            ActivityManager.getService().requestBugReport(bugreportType);
        } catch (RemoteException e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("error taking bugreport (bugreportType=");
            stringBuilder.append(bugreportType);
            stringBuilder.append(")");
            Log.e(str, stringBuilder.toString(), e);
        }
    }
}
