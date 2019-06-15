package com.android.settings.deviceinfo.storage;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.TextPaint;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.util.OpFeatures;
import android.util.Pair;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.android.settings.R;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.widget.DonutView;
import com.oneplus.settings.utils.OPConstants;
import com.oneplus.settings.utils.OPUtils;

public class StorageSummaryDonutPreference extends Preference implements OnClickListener {
    private double mPercent;

    private static class BoldLinkSpan extends StyleSpan {
        public BoldLinkSpan() {
            super(1);
        }

        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setColor(ds.linkColor);
        }
    }

    public StorageSummaryDonutPreference(Context context) {
        this(context, null);
    }

    public StorageSummaryDonutPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mPercent = -1.0d;
        setLayoutResource(R.layout.storage_summary_donut);
        setEnabled(false);
    }

    public void setPercent(long usedBytes, long totalBytes) {
        if (totalBytes != 0) {
            this.mPercent = ((double) usedBytes) / ((double) totalBytes);
        }
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        view.itemView.setClickable(false);
        DonutView donut = (DonutView) view.findViewById(R.id.donut);
        if (donut != null) {
            donut.setPercentage(this.mPercent);
        }
        Button deletionHelperButton = (Button) view.findViewById(R.id.deletion_helper_button);
        if (deletionHelperButton != null) {
            deletionHelperButton.setOnClickListener(this);
        }
    }

    public void onClick(View v) {
        if (v != null && R.id.deletion_helper_button == v.getId()) {
            Context context = getContext();
            if (OpFeatures.isSupport(new int[]{1}) || !OPUtils.isAppExist(context, "com.oneplus.security")) {
                FeatureFactory.getFactory(context).getMetricsFeatureProvider().action(context, 840, new Pair[0]);
                getContext().startActivity(new Intent("android.os.storage.action.MANAGE_STORAGE"));
                return;
            }
            try {
                getContext().startActivity(new Intent(OPConstants.ONEPLUS_CLEAN_ACTIVITY_ACTION));
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
