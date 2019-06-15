package com.android.settings.notification;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import com.android.settings.R;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;

public class ZenModeScheduleDaysSelection extends ScrollView {
    private final SimpleDateFormat mDayFormat = new SimpleDateFormat("EEEE");
    private final SparseBooleanArray mDays = new SparseBooleanArray();
    private final LinearLayout mLayout = new LinearLayout(this.mContext);

    public ZenModeScheduleDaysSelection(Context context, int[] days) {
        super(context);
        int hPad = context.getResources().getDimensionPixelSize(R.dimen.zen_schedule_day_margin);
        this.mLayout.setPadding(hPad, 0, hPad, 0);
        addView(this.mLayout);
        if (days != null) {
            for (int put : days) {
                this.mDays.put(put, true);
            }
        }
        this.mLayout.setOrientation(1);
        Calendar c = Calendar.getInstance();
        int[] daysOfWeek = getDaysOfWeekForLocale(c);
        LayoutInflater inflater = LayoutInflater.from(context);
        for (final int day : daysOfWeek) {
            CheckBox checkBox = (CheckBox) inflater.inflate(R.layout.zen_schedule_rule_day, this, false);
            c.set(7, day);
            checkBox.setText(this.mDayFormat.format(c.getTime()));
            checkBox.setChecked(this.mDays.get(day));
            checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    ZenModeScheduleDaysSelection.this.mDays.put(day, isChecked);
                    ZenModeScheduleDaysSelection.this.onChanged(ZenModeScheduleDaysSelection.this.getDays());
                }
            });
            this.mLayout.addView(checkBox);
        }
    }

    private int[] getDays() {
        SparseBooleanArray rt = new SparseBooleanArray(this.mDays.size());
        int i = 0;
        for (int i2 = 0; i2 < this.mDays.size(); i2++) {
            int day = this.mDays.keyAt(i2);
            if (this.mDays.valueAt(i2)) {
                rt.put(day, true);
            }
        }
        int[] rta = new int[rt.size()];
        while (i < rta.length) {
            rta[i] = rt.keyAt(i);
            i++;
        }
        Arrays.sort(rta);
        return rta;
    }

    protected static int[] getDaysOfWeekForLocale(Calendar c) {
        int[] daysOfWeek = new int[7];
        int currentDay = c.getFirstDayOfWeek();
        for (int i = 0; i < daysOfWeek.length; i++) {
            if (currentDay > 7) {
                currentDay = 1;
            }
            daysOfWeek[i] = currentDay;
            currentDay++;
        }
        return daysOfWeek;
    }

    /* Access modifiers changed, original: protected */
    public void onChanged(int[] days) {
    }
}
