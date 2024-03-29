package com.oneplus.lib.app;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Space;
import com.oneplus.commonctrl.R;
import com.oneplus.lib.widget.TimePicker;
import com.oneplus.lib.widget.TimePicker.OnTimeChangedListener;

public class TimePickerDialog extends OPAlertDialog implements OnClickListener, OnTimeChangedListener {
    private static final String HOUR = "hour";
    private static final String IS_24_HOUR = "is24hour";
    private static final String MINUTE = "minute";
    private final int mInitialHourOfDay;
    private final int mInitialMinute;
    private final boolean mIs24HourView;
    private final TimePicker mTimePicker;
    private final OnTimeSetListener mTimeSetListener;
    private Space mToggleModeSpace;

    public interface OnTimeSetListener {
        void onTimeSet(TimePicker timePicker, int i, int i2);
    }

    public TimePickerDialog(Context context, OnTimeSetListener listener, int hourOfDay, int minute, boolean is24HourView) {
        this(context, 0, listener, hourOfDay, minute, is24HourView);
    }

    static int resolveDialogTheme(Context context, int resId) {
        if (resId != 0) {
            return resId;
        }
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(16843934, outValue, true);
        return outValue.resourceId;
    }

    public TimePickerDialog(Context context, int themeResId, OnTimeSetListener listener, int hourOfDay, int minute, boolean is24HourView) {
        super(context, resolveDialogTheme(context, themeResId));
        this.mTimeSetListener = listener;
        this.mInitialHourOfDay = hourOfDay;
        this.mInitialMinute = minute;
        this.mIs24HourView = is24HourView;
        Context themeContext = getContext();
        View view = LayoutInflater.from(themeContext).inflate(R.layout.op_time_picker_dialog, null);
        setView(view);
        setButton(-1, (CharSequence) themeContext.getString(17039370), (OnClickListener) this);
        setButton(-2, (CharSequence) themeContext.getString(17039360), (OnClickListener) this);
        this.mTimePicker = (TimePicker) view.findViewById(R.id.timePicker);
        this.mTimePicker.setIs24HourView(Boolean.valueOf(this.mIs24HourView));
        this.mTimePicker.setCurrentHour(Integer.valueOf(this.mInitialHourOfDay));
        this.mTimePicker.setCurrentMinute(Integer.valueOf(this.mInitialMinute));
        this.mTimePicker.setOnTimeChangedListener(this);
        this.mToggleModeSpace = (Space) view.findViewById(R.id.time_picker_space);
        this.mToggleModeSpace.setVisibility(0);
    }

    public TimePicker getTimePicker() {
        return this.mTimePicker;
    }

    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
    }

    public void show() {
        super.show();
        getButton(-1).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (TimePickerDialog.this.mTimePicker.validateInput()) {
                    TimePickerDialog.this.onClick(TimePickerDialog.this, -1);
                    TimePickerDialog.this.dismiss();
                }
            }
        });
    }

    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case -2:
                cancel();
                return;
            case -1:
                if (this.mTimeSetListener != null) {
                    this.mTimeSetListener.onTimeSet(this.mTimePicker, this.mTimePicker.getCurrentHour().intValue(), this.mTimePicker.getCurrentMinute().intValue());
                    return;
                }
                return;
            default:
                return;
        }
    }

    public void updateTime(int hourOfDay, int minuteOfHour) {
        this.mTimePicker.setCurrentHour(Integer.valueOf(hourOfDay));
        this.mTimePicker.setCurrentMinute(Integer.valueOf(minuteOfHour));
    }

    public Bundle onSaveInstanceState() {
        Bundle state = super.onSaveInstanceState();
        state.putInt(HOUR, this.mTimePicker.getCurrentHour().intValue());
        state.putInt(MINUTE, this.mTimePicker.getCurrentMinute().intValue());
        state.putBoolean(IS_24_HOUR, this.mTimePicker.is24HourView());
        return state;
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int hour = savedInstanceState.getInt(HOUR);
        int minute = savedInstanceState.getInt(MINUTE);
        this.mTimePicker.setIs24HourView(Boolean.valueOf(savedInstanceState.getBoolean(IS_24_HOUR)));
        this.mTimePicker.setCurrentHour(Integer.valueOf(hour));
        this.mTimePicker.setCurrentMinute(Integer.valueOf(minute));
    }
}
