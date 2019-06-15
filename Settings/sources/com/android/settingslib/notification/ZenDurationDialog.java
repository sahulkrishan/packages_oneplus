package com.android.settingslib.notification;

import android.app.ActivityManager;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.provider.Settings.Global;
import android.service.notification.ZenModeConfig;
import android.support.annotation.VisibleForTesting;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.policy.PhoneWindow;
import com.android.settingslib.R;
import java.util.Arrays;

public class ZenDurationDialog {
    @VisibleForTesting
    protected static final int ALWAYS_ASK_CONDITION_INDEX = 2;
    @VisibleForTesting
    protected static final int COUNTDOWN_CONDITION_INDEX = 1;
    private static final int DEFAULT_BUCKET_INDEX = Arrays.binarySearch(MINUTE_BUCKETS, 60);
    @VisibleForTesting
    protected static final int FOREVER_CONDITION_INDEX = 0;
    @VisibleForTesting
    protected static final int MAX_BUCKET_MINUTES = MINUTE_BUCKETS[MINUTE_BUCKETS.length - 1];
    private static final int[] MINUTE_BUCKETS = ZenModeConfig.MINUTE_BUCKETS;
    @VisibleForTesting
    protected static final int MIN_BUCKET_MINUTES = MINUTE_BUCKETS[0];
    private int MAX_MANUAL_DND_OPTIONS = 3;
    @VisibleForTesting
    protected int mBucketIndex = -1;
    @VisibleForTesting
    protected Context mContext;
    @VisibleForTesting
    protected LayoutInflater mLayoutInflater;
    private RadioGroup mZenRadioGroup;
    @VisibleForTesting
    protected LinearLayout mZenRadioGroupContent;

    @VisibleForTesting
    protected static class ConditionTag {
        public int countdownZenDuration;
        public TextView line1;
        public View lines;
        public RadioButton rb;

        protected ConditionTag() {
        }
    }

    public ZenDurationDialog(Context context) {
        this.mContext = context;
    }

    public Dialog createDialog() {
        final int zenDuration = Global.getInt(this.mContext.getContentResolver(), "zen_duration", 0);
        Builder builder = new Builder(this.mContext).setTitle(R.string.zen_mode_duration_settings_title).setNegativeButton(R.string.cancel, null).setPositiveButton(R.string.okay, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ZenDurationDialog.this.updateZenDuration(zenDuration);
            }
        });
        View contentView = getContentView();
        setupRadioButtons(zenDuration);
        builder.setView(contentView);
        return builder.create();
    }

    /* Access modifiers changed, original: protected */
    @VisibleForTesting
    public void updateZenDuration(int currZenDuration) {
        int checkedRadioButtonId = this.mZenRadioGroup.getCheckedRadioButtonId();
        int newZenDuration = Global.getInt(this.mContext.getContentResolver(), "zen_duration", 0);
        switch (checkedRadioButtonId) {
            case 0:
                newZenDuration = 0;
                MetricsLogger.action(this.mContext, 1343);
                break;
            case 1:
                newZenDuration = getConditionTagAt(checkedRadioButtonId).countdownZenDuration;
                MetricsLogger.action(this.mContext, 1342, newZenDuration);
                break;
            case 2:
                newZenDuration = -1;
                MetricsLogger.action(this.mContext, 1344);
                break;
        }
        if (currZenDuration != newZenDuration) {
            Global.putInt(this.mContext.getContentResolver(), "zen_duration", newZenDuration);
        }
    }

    /* Access modifiers changed, original: protected */
    @VisibleForTesting
    public View getContentView() {
        if (this.mLayoutInflater == null) {
            this.mLayoutInflater = new PhoneWindow(this.mContext).getLayoutInflater();
        }
        View contentView = this.mLayoutInflater.inflate(R.layout.zen_mode_duration_dialog, null);
        ScrollView container = (ScrollView) contentView.findViewById(R.id.zen_duration_container);
        this.mZenRadioGroup = (RadioGroup) container.findViewById(R.id.zen_radio_buttons);
        this.mZenRadioGroupContent = (LinearLayout) container.findViewById(R.id.zen_radio_buttons_content);
        for (int i = 0; i < this.MAX_MANUAL_DND_OPTIONS; i++) {
            View radioButton = this.mLayoutInflater.inflate(R.layout.zen_mode_radio_button, this.mZenRadioGroup, false);
            this.mZenRadioGroup.addView(radioButton);
            radioButton.setId(i);
            View radioButtonContent = this.mLayoutInflater.inflate(R.layout.zen_mode_condition, this.mZenRadioGroupContent, false);
            radioButtonContent.setId(this.MAX_MANUAL_DND_OPTIONS + i);
            this.mZenRadioGroupContent.addView(radioButtonContent);
        }
        return contentView;
    }

    /* Access modifiers changed, original: protected */
    @VisibleForTesting
    public void setupRadioButtons(int zenDuration) {
        int checkedIndex = 2;
        if (zenDuration == 0) {
            checkedIndex = 0;
        } else if (zenDuration > 0) {
            checkedIndex = 1;
        }
        bindTag(zenDuration, this.mZenRadioGroupContent.getChildAt(0), 0);
        bindTag(zenDuration, this.mZenRadioGroupContent.getChildAt(1), 1);
        bindTag(zenDuration, this.mZenRadioGroupContent.getChildAt(2), 2);
        getConditionTagAt(checkedIndex).rb.setChecked(true);
    }

    private void bindTag(int currZenDuration, View row, int rowIndex) {
        ConditionTag tag;
        if (row.getTag() != null) {
            tag = (ConditionTag) row.getTag();
        } else {
            tag = new ConditionTag();
        }
        row.setTag(tag);
        if (tag.rb == null) {
            tag.rb = (RadioButton) this.mZenRadioGroup.getChildAt(rowIndex);
        }
        if (currZenDuration <= 0) {
            tag.countdownZenDuration = MINUTE_BUCKETS[DEFAULT_BUCKET_INDEX];
        } else {
            tag.countdownZenDuration = currZenDuration;
        }
        tag.rb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    tag.rb.setChecked(true);
                }
            }
        });
        updateUi(tag, row, rowIndex);
    }

    /* Access modifiers changed, original: protected */
    @VisibleForTesting
    public ConditionTag getConditionTagAt(int index) {
        return (ConditionTag) this.mZenRadioGroupContent.getChildAt(index).getTag();
    }

    private void setupUi(final ConditionTag tag, View row) {
        if (tag.lines == null) {
            tag.lines = row.findViewById(16908290);
            tag.lines.setAccessibilityLiveRegion(1);
        }
        if (tag.line1 == null) {
            tag.line1 = (TextView) row.findViewById(16908308);
        }
        row.findViewById(16908309).setVisibility(8);
        tag.lines.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                tag.rb.setChecked(true);
            }
        });
    }

    private void updateButtons(final ConditionTag tag, final View row, final int rowIndex) {
        ImageView button1 = (ImageView) row.findViewById(16908313);
        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ZenDurationDialog.this.onClickTimeButton(row, tag, false, rowIndex);
            }
        });
        ImageView button2 = (ImageView) row.findViewById(16908314);
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ZenDurationDialog.this.onClickTimeButton(row, tag, true, rowIndex);
            }
        });
        long time = (long) tag.countdownZenDuration;
        boolean z = true;
        if (rowIndex == 1) {
            button1.setVisibility(0);
            button2.setVisibility(0);
            button1.setEnabled(time > ((long) MIN_BUCKET_MINUTES));
            if (tag.countdownZenDuration == MAX_BUCKET_MINUTES) {
                z = false;
            }
            button2.setEnabled(z);
            float f = 0.5f;
            button1.setAlpha(button1.isEnabled() ? 1.0f : 0.5f);
            if (button2.isEnabled()) {
                f = 1.0f;
            }
            button2.setAlpha(f);
            return;
        }
        button1.setVisibility(8);
        button2.setVisibility(8);
    }

    /* Access modifiers changed, original: protected */
    @VisibleForTesting
    public void updateUi(ConditionTag tag, View row, int rowIndex) {
        if (tag.lines == null) {
            setupUi(tag, row);
        }
        updateButtons(tag, row, rowIndex);
        String radioContentText = "";
        switch (rowIndex) {
            case 0:
                radioContentText = this.mContext.getString(17041198);
                break;
            case 1:
                radioContentText = ZenModeConfig.toTimeCondition(this.mContext, tag.countdownZenDuration, ActivityManager.getCurrentUser(), false).line1;
                break;
            case 2:
                radioContentText = this.mContext.getString(R.string.zen_mode_duration_always_prompt_title);
                break;
        }
        tag.line1.setText(radioContentText);
    }

    /* Access modifiers changed, original: protected */
    /* JADX WARNING: Missing block: B:15:0x002b, code skipped:
            r11.mBucketIndex = r3;
            r0 = r8;
     */
    @android.support.annotation.VisibleForTesting
    public void onClickTimeButton(android.view.View r12, com.android.settingslib.notification.ZenDurationDialog.ConditionTag r13, boolean r14, int r15) {
        /*
        r11 = this;
        r0 = -1;
        r1 = MINUTE_BUCKETS;
        r1 = r1.length;
        r2 = r11.mBucketIndex;
        r3 = 0;
        r4 = -1;
        r5 = 1;
        if (r2 != r4) goto L_0x003f;
    L_0x000b:
        r2 = r13.countdownZenDuration;
        r6 = (long) r2;
    L_0x000f:
        r2 = r3;
        if (r2 >= r1) goto L_0x0032;
    L_0x0012:
        if (r14 == 0) goto L_0x0016;
    L_0x0014:
        r3 = r2;
        goto L_0x0019;
    L_0x0016:
        r3 = r1 + -1;
        r3 = r3 - r2;
    L_0x0019:
        r8 = MINUTE_BUCKETS;
        r8 = r8[r3];
        if (r14 == 0) goto L_0x0024;
    L_0x001f:
        r9 = (long) r8;
        r9 = (r9 > r6 ? 1 : (r9 == r6 ? 0 : -1));
        if (r9 > 0) goto L_0x002b;
    L_0x0024:
        if (r14 != 0) goto L_0x002f;
    L_0x0026:
        r9 = (long) r8;
        r9 = (r9 > r6 ? 1 : (r9 == r6 ? 0 : -1));
        if (r9 >= 0) goto L_0x002f;
    L_0x002b:
        r11.mBucketIndex = r3;
        r0 = r8;
        goto L_0x0032;
    L_0x002f:
        r3 = r2 + 1;
        goto L_0x000f;
    L_0x0032:
        if (r0 != r4) goto L_0x003e;
    L_0x0034:
        r2 = DEFAULT_BUCKET_INDEX;
        r11.mBucketIndex = r2;
        r2 = MINUTE_BUCKETS;
        r3 = r11.mBucketIndex;
        r0 = r2[r3];
    L_0x003e:
        goto L_0x0058;
    L_0x003f:
        r2 = r1 + -1;
        r6 = r11.mBucketIndex;
        if (r14 == 0) goto L_0x0047;
    L_0x0045:
        r4 = r5;
    L_0x0047:
        r6 = r6 + r4;
        r2 = java.lang.Math.min(r2, r6);
        r2 = java.lang.Math.max(r3, r2);
        r11.mBucketIndex = r2;
        r2 = MINUTE_BUCKETS;
        r3 = r11.mBucketIndex;
        r0 = r2[r3];
    L_0x0058:
        r13.countdownZenDuration = r0;
        r11.bindTag(r0, r12, r15);
        r2 = r13.rb;
        r2.setChecked(r5);
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settingslib.notification.ZenDurationDialog.onClickTimeButton(android.view.View, com.android.settingslib.notification.ZenDurationDialog$ConditionTag, boolean, int):void");
    }
}
