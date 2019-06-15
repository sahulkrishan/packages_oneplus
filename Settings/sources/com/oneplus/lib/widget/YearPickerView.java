package com.oneplus.lib.widget;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import com.oneplus.commonctrl.R;
import java.util.Calendar;

class YearPickerView extends FrameLayout {
    private static final int ITEM_LAYOUT = R.layout.op_year_label_text_view;
    private final int mChildSize;
    private OnYearSelectedListener mOnYearSelectedListener;
    private NumberPicker mPicker;
    private final int mViewSize;

    public interface OnYearSelectedListener {
        void onYearChanged(YearPickerView yearPickerView, int i);
    }

    public YearPickerView(Context context, AttributeSet attrs) {
        this(context, attrs, 16844068);
    }

    public YearPickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public YearPickerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        Resources res = context.getResources();
        LayoutInflater.from(context).inflate(ITEM_LAYOUT, this, true);
        this.mViewSize = res.getDimensionPixelOffset(R.dimen.datepicker_view_animator_height);
        this.mChildSize = res.getDimensionPixelOffset(R.dimen.datepicker_year_label_height);
        this.mPicker = (NumberPicker) findViewById(R.id.year_picker);
        this.mPicker.setSelectNumberCount(5);
    }

    public void setCurrentYear() {
        if (this.mOnYearSelectedListener != null) {
            this.mOnYearSelectedListener.onYearChanged(this, this.mPicker.getValue());
        }
    }

    public void setOnYearSelectedListener(OnYearSelectedListener listener) {
        this.mOnYearSelectedListener = listener;
    }

    public void setYear(int year) {
        this.mPicker.setValue(year);
    }

    @Deprecated
    public void setSelectionCentered(int position) {
    }

    public void setRange(Calendar min, Calendar max) {
        int minYear = min.get(1);
        int maxYear = max.get(1);
        this.mPicker.setMinValue(minYear);
        this.mPicker.setMaxValue(maxYear);
    }
}
