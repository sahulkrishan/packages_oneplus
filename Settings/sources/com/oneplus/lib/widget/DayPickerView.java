package com.oneplus.lib.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.widget.ImageButton;
import com.oneplus.commonctrl.R;
import com.oneplus.lib.util.MathUtils;
import com.oneplus.lib.widget.ViewPager.OnPageChangeListener;
import com.oneplus.lib.widget.util.ViewUtils;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

class DayPickerView extends ViewGroup {
    private static final int[] ATTRS_TEXT_COLOR = new int[]{16842904};
    private static final String DATE_FORMAT = "MM/dd/yyyy";
    private static final DateFormat DATE_FORMATTER = new SimpleDateFormat(DATE_FORMAT);
    private static final int DEFAULT_END_YEAR = 2100;
    private static final int DEFAULT_LAYOUT = R.layout.op_day_picker_content_material;
    private static final int DEFAULT_START_YEAR = 1900;
    private final AccessibilityManager mAccessibilityManager;
    private final DayPickerPagerAdapter mAdapter;
    private Context mContext;
    private final Calendar mMaxDate;
    private final Calendar mMinDate;
    private final ImageButton mNextButton;
    private final OnClickListener mOnClickListener;
    private OnDaySelectedListener mOnDaySelectedListener;
    private final OnPageChangeListener mOnPageChangedListener;
    private final ImageButton mPrevButton;
    private final Calendar mSelectedDay;
    private Calendar mTempCalendar;
    private final ViewPager mViewPager;

    public interface OnDaySelectedListener {
        void onDaySelected(DayPickerView dayPickerView, Calendar calendar);
    }

    public DayPickerView(Context context) {
        this(context, null);
    }

    public DayPickerView(Context context, AttributeSet attrs) {
        this(context, attrs, 16843613);
    }

    public DayPickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public DayPickerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        Context context2 = context;
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mSelectedDay = Calendar.getInstance();
        this.mMinDate = Calendar.getInstance();
        this.mMaxDate = Calendar.getInstance();
        this.mOnPageChangedListener = new OnPageChangeListener() {
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                float alpha = Math.abs(0.5f - positionOffset) * 2.0f;
                DayPickerView.this.mPrevButton.setAlpha(alpha);
                DayPickerView.this.mNextButton.setAlpha(alpha);
            }

            public void onPageScrollStateChanged(int state) {
            }

            public void onPageSelected(int position) {
                DayPickerView.this.updateButtonVisibility(position);
            }
        };
        this.mOnClickListener = new OnClickListener() {
            public void onClick(View v) {
                int direction;
                if (v == DayPickerView.this.mPrevButton) {
                    direction = -1;
                } else if (v == DayPickerView.this.mNextButton) {
                    direction = 1;
                } else {
                    return;
                }
                int nextItem = DayPickerView.this.mViewPager.getCurrentItem() + direction;
                DayPickerView.this.mViewPager.setCurrentItem(nextItem, DayPickerView.this.mAccessibilityManager.isEnabled() ^ 1);
            }
        };
        this.mContext = context2;
        this.mAccessibilityManager = (AccessibilityManager) context2.getSystemService("accessibility");
        TypedArray a = context2.obtainStyledAttributes(attrs, R.styleable.OPCalendarView, defStyleAttr, defStyleRes);
        int firstDayOfWeek = a.getInt(R.styleable.OPCalendarView_android_firstDayOfWeek, Calendar.getInstance().get(7));
        String minDate = a.getString(R.styleable.OPCalendarView_android_minDate);
        String maxDate = a.getString(R.styleable.OPCalendarView_android_maxDate);
        int monthTextAppearanceResId = a.getResourceId(R.styleable.OPCalendarView_monthTextAppearance, R.style.TextAppearance_Material_Widget_Calendar_Month);
        int dayOfWeekTextAppearanceResId = a.getResourceId(R.styleable.OPCalendarView_weekDayTextAppearance, R.style.TextAppearance_Material_Widget_Calendar_DayOfWeek);
        int dayTextAppearanceResId = a.getResourceId(R.styleable.OPCalendarView_dateTextAppearance, R.style.TextAppearance_Material_Widget_Calendar_Day);
        ColorStateList daySelectorColor = a.getColorStateList(R.styleable.OPCalendarView_daySelectorColor);
        a.recycle();
        this.mAdapter = new DayPickerPagerAdapter(context2, R.layout.op_date_picker_month_item_material, R.id.month_view);
        this.mAdapter.setMonthTextAppearance(monthTextAppearanceResId);
        this.mAdapter.setDayOfWeekTextAppearance(dayOfWeekTextAppearanceResId);
        this.mAdapter.setDayTextAppearance(dayTextAppearanceResId);
        this.mAdapter.setDaySelectorColor(daySelectorColor);
        ViewGroup content = (ViewGroup) LayoutInflater.from(context).inflate(DEFAULT_LAYOUT, this, false);
        while (content.getChildCount() > 0) {
            View child = content.getChildAt(0);
            content.removeViewAt(0);
            addView(child);
            context2 = context;
        }
        this.mPrevButton = (ImageButton) findViewById(R.id.prev);
        this.mPrevButton.setOnClickListener(this.mOnClickListener);
        this.mNextButton = (ImageButton) findViewById(R.id.next);
        this.mNextButton.setOnClickListener(this.mOnClickListener);
        this.mViewPager = (ViewPager) findViewById(R.id.day_picker_view_pager);
        this.mViewPager.setAdapter(this.mAdapter);
        this.mViewPager.setOnPageChangeListener(this.mOnPageChangedListener);
        if (monthTextAppearanceResId != 0) {
            TypedArray ta = this.mContext.obtainStyledAttributes(null, ATTRS_TEXT_COLOR, 0, monthTextAppearanceResId);
            a = ta.getColorStateList(0);
            if (a != null) {
                this.mPrevButton.setImageTintList(a);
                this.mNextButton.setImageTintList(a);
            }
            ta.recycle();
        }
        Calendar tempDate = Calendar.getInstance();
        if (!parseDate(minDate, tempDate)) {
            tempDate.set(DEFAULT_START_YEAR, 0, 1);
        }
        long minDateMillis = tempDate.getTimeInMillis();
        if (!parseDate(maxDate, tempDate)) {
            tempDate.set(DEFAULT_END_YEAR, 11, 31);
        }
        long maxDateMillis = tempDate.getTimeInMillis();
        if (maxDateMillis >= minDateMillis) {
            minDate = MathUtils.constrain(System.currentTimeMillis(), minDateMillis, maxDateMillis);
            setFirstDayOfWeek(firstDayOfWeek);
            setMinDate(minDateMillis);
            setMaxDate(maxDateMillis);
            setDate(minDate, false);
            this.mAdapter.setOnDaySelectedListener(new com.oneplus.lib.widget.DayPickerPagerAdapter.OnDaySelectedListener() {
                public void onDaySelected(DayPickerPagerAdapter adapter, Calendar day) {
                    if (DayPickerView.this.mOnDaySelectedListener != null) {
                        DayPickerView.this.mOnDaySelectedListener.onDaySelected(DayPickerView.this, day);
                    }
                }
            });
            return;
        }
        String str = minDate;
        String str2 = maxDate;
        throw new IllegalArgumentException("maxDate must be >= minDate");
    }

    public static boolean parseDate(String date, Calendar outDate) {
        if (date == null || date.isEmpty()) {
            return false;
        }
        try {
            outDate.setTime(DATE_FORMATTER.parse(date));
            return true;
        } catch (ParseException e) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Date: ");
            stringBuilder.append(date);
            stringBuilder.append(" not in format: ");
            stringBuilder.append(DATE_FORMAT);
            Log.w("SearchView", stringBuilder.toString());
            return false;
        }
    }

    private void updateButtonVisibility(int position) {
        boolean hasNext = true;
        int i = 0;
        boolean hasPrev = position > 0;
        if (position >= this.mAdapter.getCount() - 1) {
            hasNext = false;
        }
        this.mPrevButton.setVisibility(hasPrev ? 0 : 4);
        ImageButton imageButton = this.mNextButton;
        if (!hasNext) {
            i = 4;
        }
        imageButton.setVisibility(i);
    }

    /* Access modifiers changed, original: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        ViewPager viewPager = this.mViewPager;
        measureChild(viewPager, widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(viewPager.getMeasuredWidthAndState(), viewPager.getMeasuredHeightAndState());
        int pagerWidth = viewPager.getMeasuredWidth();
        int pagerHeight = viewPager.getMeasuredHeight();
        int buttonWidthSpec = MeasureSpec.makeMeasureSpec(pagerWidth, Integer.MIN_VALUE);
        int buttonHeightSpec = MeasureSpec.makeMeasureSpec(pagerHeight, Integer.MIN_VALUE);
        this.mPrevButton.measure(buttonWidthSpec, buttonHeightSpec);
        this.mNextButton.measure(buttonWidthSpec, buttonHeightSpec);
    }

    public void onRtlPropertiesChanged(int layoutDirection) {
        super.onRtlPropertiesChanged(layoutDirection);
        requestLayout();
    }

    /* Access modifiers changed, original: protected */
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        ImageButton leftButton;
        ImageButton rightButton;
        if (ViewUtils.isLayoutRtl(this)) {
            leftButton = this.mNextButton;
            rightButton = this.mPrevButton;
        } else {
            leftButton = this.mPrevButton;
            rightButton = this.mNextButton;
        }
        int width = right - left;
        this.mViewPager.layout(0, 0, width, bottom - top);
        SimpleMonthView monthView = (SimpleMonthView) this.mViewPager.getChildAt(0);
        int monthHeight = monthView.getMonthHeight();
        int cellWidth = monthView.getCellWidth();
        int leftDW = leftButton.getMeasuredWidth();
        int leftDH = leftButton.getMeasuredHeight();
        int leftIconTop = monthView.getPaddingTop() + ((monthHeight - leftDH) / 2);
        int leftIconLeft = monthView.getPaddingLeft() + ((cellWidth - leftDW) / 2);
        leftButton.layout(leftIconLeft, leftIconTop, leftIconLeft + leftDW, leftIconTop + leftDH);
        int rightDW = rightButton.getMeasuredWidth();
        int rightDH = rightButton.getMeasuredHeight();
        int rightIconTop = monthView.getPaddingTop() + ((monthHeight - rightDH) / 2);
        int rightIconRight = (width - monthView.getPaddingRight()) - ((cellWidth - rightDW) / 2);
        rightButton.layout(rightIconRight - rightDW, rightIconTop, rightIconRight, rightIconTop + rightDH);
    }

    public void setDayOfWeekTextAppearance(int resId) {
        this.mAdapter.setDayOfWeekTextAppearance(resId);
    }

    public int getDayOfWeekTextAppearance() {
        return this.mAdapter.getDayOfWeekTextAppearance();
    }

    public void setDayTextAppearance(int resId) {
        this.mAdapter.setDayTextAppearance(resId);
    }

    public int getDayTextAppearance() {
        return this.mAdapter.getDayTextAppearance();
    }

    public void setDate(long timeInMillis) {
        setDate(timeInMillis, false);
    }

    public void setDate(long timeInMillis, boolean animate) {
        setDate(timeInMillis, animate, true);
    }

    private void setDate(long timeInMillis, boolean animate, boolean setSelected) {
        boolean dateClamped = false;
        if (timeInMillis < this.mMinDate.getTimeInMillis()) {
            timeInMillis = this.mMinDate.getTimeInMillis();
            dateClamped = true;
        } else if (timeInMillis > this.mMaxDate.getTimeInMillis()) {
            timeInMillis = this.mMaxDate.getTimeInMillis();
            dateClamped = true;
        }
        getTempCalendarForTime(timeInMillis);
        if (setSelected || dateClamped) {
            this.mSelectedDay.setTimeInMillis(timeInMillis);
        }
        int position = getPositionFromDay(timeInMillis);
        if (position != this.mViewPager.getCurrentItem()) {
            this.mViewPager.setCurrentItem(position, animate);
        }
        this.mAdapter.setSelectedDay(this.mTempCalendar);
    }

    public long getDate() {
        return this.mSelectedDay.getTimeInMillis();
    }

    public boolean getBoundsForDate(long timeInMillis, Rect outBounds) {
        if (getPositionFromDay(timeInMillis) != this.mViewPager.getCurrentItem()) {
            return false;
        }
        this.mTempCalendar.setTimeInMillis(timeInMillis);
        return this.mAdapter.getBoundsForDate(this.mTempCalendar, outBounds);
    }

    public void setFirstDayOfWeek(int firstDayOfWeek) {
        this.mAdapter.setFirstDayOfWeek(firstDayOfWeek);
    }

    public int getFirstDayOfWeek() {
        return this.mAdapter.getFirstDayOfWeek();
    }

    public void setMinDate(long timeInMillis) {
        this.mMinDate.setTimeInMillis(timeInMillis);
        onRangeChanged();
    }

    public long getMinDate() {
        return this.mMinDate.getTimeInMillis();
    }

    public void setMaxDate(long timeInMillis) {
        this.mMaxDate.setTimeInMillis(timeInMillis);
        onRangeChanged();
    }

    public long getMaxDate() {
        return this.mMaxDate.getTimeInMillis();
    }

    public void onRangeChanged() {
        this.mAdapter.setRange(this.mMinDate, this.mMaxDate);
        setDate(this.mSelectedDay.getTimeInMillis(), false, false);
        updateButtonVisibility(this.mViewPager.getCurrentItem());
    }

    public void setOnDaySelectedListener(OnDaySelectedListener listener) {
        this.mOnDaySelectedListener = listener;
    }

    private int getDiffMonths(Calendar start, Calendar end) {
        return (end.get(2) - start.get(2)) + (12 * (end.get(1) - start.get(1)));
    }

    private int getPositionFromDay(long timeInMillis) {
        return MathUtils.constrain(getDiffMonths(this.mMinDate, getTempCalendarForTime(timeInMillis)), 0, getDiffMonths(this.mMinDate, this.mMaxDate));
    }

    private Calendar getTempCalendarForTime(long timeInMillis) {
        if (this.mTempCalendar == null) {
            this.mTempCalendar = Calendar.getInstance();
        }
        this.mTempCalendar.setTimeInMillis(timeInMillis);
        return this.mTempCalendar;
    }

    public int getMostVisiblePosition() {
        return this.mViewPager.getCurrentItem();
    }

    public void setPosition(int position) {
        this.mViewPager.setCurrentItem(position, false);
    }
}
