package android.support.v17.leanback.widget.picker;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.IntRange;
import android.support.v17.leanback.R;
import android.support.v17.leanback.widget.picker.PickerUtility.TimeConstant;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TimePicker extends Picker {
    private static final int AM_INDEX = 0;
    private static final int HOURS_IN_HALF_DAY = 12;
    private static final int PM_INDEX = 1;
    static final String TAG = "TimePicker";
    PickerColumn mAmPmColumn;
    int mColAmPmIndex;
    int mColHourIndex;
    int mColMinuteIndex;
    private final TimeConstant mConstant;
    private int mCurrentAmPmIndex;
    private int mCurrentHour;
    private int mCurrentMinute;
    PickerColumn mHourColumn;
    private boolean mIs24hFormat;
    PickerColumn mMinuteColumn;
    private String mTimePickerFormat;

    public TimePicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimePicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mConstant = PickerUtility.getTimeConstantInstance(Locale.getDefault(), context.getResources());
        TypedArray attributesArray = context.obtainStyledAttributes(attrs, R.styleable.lbTimePicker);
        this.mIs24hFormat = attributesArray.getBoolean(R.styleable.lbTimePicker_is24HourFormat, DateFormat.is24HourFormat(context));
        boolean useCurrentTime = attributesArray.getBoolean(R.styleable.lbTimePicker_useCurrentTime, true);
        updateColumns();
        updateColumnsRange();
        if (useCurrentTime) {
            Calendar currentDate = PickerUtility.getCalendarForLocale(null, this.mConstant.locale);
            setHour(currentDate.get(11));
            setMinute(currentDate.get(12));
            setAmPmValue();
        }
    }

    private static boolean updateMin(PickerColumn column, int value) {
        if (value == column.getMinValue()) {
            return false;
        }
        column.setMinValue(value);
        return true;
    }

    private static boolean updateMax(PickerColumn column, int value) {
        if (value == column.getMaxValue()) {
            return false;
        }
        column.setMaxValue(value);
        return true;
    }

    /* Access modifiers changed, original: 0000 */
    public String getBestHourMinutePattern() {
        String hourPattern;
        if (PickerUtility.SUPPORTS_BEST_DATE_TIME_PATTERN) {
            hourPattern = DateFormat.getBestDateTimePattern(this.mConstant.locale, this.mIs24hFormat ? "Hma" : "hma");
        } else {
            java.text.DateFormat dateFormat = SimpleDateFormat.getTimeInstance(3, this.mConstant.locale);
            if (dateFormat instanceof SimpleDateFormat) {
                hourPattern = ((SimpleDateFormat) dateFormat).toPattern().replace("s", "");
                if (this.mIs24hFormat) {
                    hourPattern = hourPattern.replace('h', 'H').replace("a", "");
                }
            } else {
                hourPattern = this.mIs24hFormat ? "H:mma" : "h:mma";
            }
        }
        String hourPattern2 = hourPattern;
        return TextUtils.isEmpty(hourPattern2) ? "h:mma" : hourPattern2;
    }

    /* Access modifiers changed, original: 0000 */
    public List<CharSequence> extractSeparators() {
        String hmaPattern = getBestHourMinutePattern();
        List<CharSequence> separators = new ArrayList();
        StringBuilder sb = new StringBuilder();
        char[] timeFormats = new char[]{'H', 'h', 'K', 'k', 'm', 'M', 'a'};
        boolean processingQuote = false;
        char lastChar = 0;
        for (int i = 0; i < hmaPattern.length(); i++) {
            char c = hmaPattern.charAt(i);
            if (c != ' ') {
                if (c != '\'') {
                    if (processingQuote) {
                        sb.append(c);
                    } else if (!isAnyOf(c, timeFormats)) {
                        sb.append(c);
                    } else if (c != lastChar) {
                        separators.add(sb.toString());
                        sb.setLength(0);
                    }
                    lastChar = c;
                } else if (processingQuote) {
                    processingQuote = false;
                } else {
                    sb.setLength(0);
                    processingQuote = true;
                }
            }
        }
        separators.add(sb.toString());
        return separators;
    }

    private static boolean isAnyOf(char c, char[] any) {
        for (char c2 : any) {
            if (c == c2) {
                return true;
            }
        }
        return false;
    }

    private String extractTimeFields() {
        String hmaPattern = getBestHourMinutePattern();
        boolean z = true;
        boolean isRTL = TextUtils.getLayoutDirectionFromLocale(this.mConstant.locale) == 1;
        if (hmaPattern.indexOf(97) >= 0 && hmaPattern.indexOf("a") <= hmaPattern.indexOf("m")) {
            z = false;
        }
        boolean isAmPmAtEnd = z;
        String timePickerFormat = isRTL ? "mh" : "hm";
        if (is24Hour()) {
            return timePickerFormat;
        }
        StringBuilder stringBuilder;
        if (isAmPmAtEnd) {
            stringBuilder = new StringBuilder();
            stringBuilder.append(timePickerFormat);
            stringBuilder.append("a");
        } else {
            stringBuilder = new StringBuilder();
            stringBuilder.append("a");
            stringBuilder.append(timePickerFormat);
        }
        return stringBuilder.toString();
    }

    private void updateColumns() {
        String timePickerFormat = getBestHourMinutePattern();
        if (!TextUtils.equals(timePickerFormat, this.mTimePickerFormat)) {
            this.mTimePickerFormat = timePickerFormat;
            String timeFieldsPattern = extractTimeFields();
            List<CharSequence> separators = extractSeparators();
            if (separators.size() == timeFieldsPattern.length() + 1) {
                setSeparators(separators);
                timeFieldsPattern = timeFieldsPattern.toUpperCase();
                this.mAmPmColumn = null;
                this.mMinuteColumn = null;
                this.mHourColumn = null;
                this.mColAmPmIndex = -1;
                this.mColMinuteIndex = -1;
                this.mColHourIndex = -1;
                ArrayList<PickerColumn> columns = new ArrayList(3);
                for (int i = 0; i < timeFieldsPattern.length(); i++) {
                    char charAt = timeFieldsPattern.charAt(i);
                    PickerColumn pickerColumn;
                    if (charAt == 'A') {
                        pickerColumn = new PickerColumn();
                        this.mAmPmColumn = pickerColumn;
                        columns.add(pickerColumn);
                        this.mAmPmColumn.setStaticLabels(this.mConstant.ampm);
                        this.mColAmPmIndex = i;
                        updateMin(this.mAmPmColumn, 0);
                        updateMax(this.mAmPmColumn, 1);
                    } else if (charAt == 'H') {
                        pickerColumn = new PickerColumn();
                        this.mHourColumn = pickerColumn;
                        columns.add(pickerColumn);
                        this.mHourColumn.setStaticLabels(this.mConstant.hours24);
                        this.mColHourIndex = i;
                    } else if (charAt == 'M') {
                        pickerColumn = new PickerColumn();
                        this.mMinuteColumn = pickerColumn;
                        columns.add(pickerColumn);
                        this.mMinuteColumn.setStaticLabels(this.mConstant.minutes);
                        this.mColMinuteIndex = i;
                    } else {
                        throw new IllegalArgumentException("Invalid time picker format.");
                    }
                }
                setColumns(columns);
                return;
            }
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Separators size: ");
            stringBuilder.append(separators.size());
            stringBuilder.append(" must equal");
            stringBuilder.append(" the size of timeFieldsPattern: ");
            stringBuilder.append(timeFieldsPattern.length());
            stringBuilder.append(" + 1");
            throw new IllegalStateException(stringBuilder.toString());
        }
    }

    private void updateColumnsRange() {
        updateMin(this.mHourColumn, this.mIs24hFormat ^ 1);
        updateMax(this.mHourColumn, this.mIs24hFormat ? 23 : 12);
        updateMin(this.mMinuteColumn, 0);
        updateMax(this.mMinuteColumn, 59);
        if (this.mAmPmColumn != null) {
            updateMin(this.mAmPmColumn, 0);
            updateMax(this.mAmPmColumn, 1);
        }
    }

    private void setAmPmValue() {
        if (!is24Hour()) {
            setColumnValue(this.mColAmPmIndex, this.mCurrentAmPmIndex, false);
        }
    }

    public void setHour(@IntRange(from = 0, to = 23) int hour) {
        if (hour < 0 || hour > 23) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("hour: ");
            stringBuilder.append(hour);
            stringBuilder.append(" is not in [0-23] range in");
            throw new IllegalArgumentException(stringBuilder.toString());
        }
        this.mCurrentHour = hour;
        if (!is24Hour()) {
            if (this.mCurrentHour >= 12) {
                this.mCurrentAmPmIndex = 1;
                if (this.mCurrentHour > 12) {
                    this.mCurrentHour -= 12;
                }
            } else {
                this.mCurrentAmPmIndex = 0;
                if (this.mCurrentHour == 0) {
                    this.mCurrentHour = 12;
                }
            }
            setAmPmValue();
        }
        setColumnValue(this.mColHourIndex, this.mCurrentHour, false);
    }

    public int getHour() {
        if (this.mIs24hFormat) {
            return this.mCurrentHour;
        }
        if (this.mCurrentAmPmIndex == 0) {
            return this.mCurrentHour % 12;
        }
        return (this.mCurrentHour % 12) + 12;
    }

    public void setMinute(@IntRange(from = 0, to = 59) int minute) {
        if (minute < 0 || minute > 59) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("minute: ");
            stringBuilder.append(minute);
            stringBuilder.append(" is not in [0-59] range.");
            throw new IllegalArgumentException(stringBuilder.toString());
        }
        this.mCurrentMinute = minute;
        setColumnValue(this.mColMinuteIndex, this.mCurrentMinute, false);
    }

    public int getMinute() {
        return this.mCurrentMinute;
    }

    public void setIs24Hour(boolean is24Hour) {
        if (this.mIs24hFormat != is24Hour) {
            int currentHour = getHour();
            int currentMinute = getMinute();
            this.mIs24hFormat = is24Hour;
            updateColumns();
            updateColumnsRange();
            setHour(currentHour);
            setMinute(currentMinute);
            setAmPmValue();
        }
    }

    public boolean is24Hour() {
        return this.mIs24hFormat;
    }

    public boolean isPm() {
        return this.mCurrentAmPmIndex == 1;
    }

    public void onColumnValueChanged(int columnIndex, int newValue) {
        if (columnIndex == this.mColHourIndex) {
            this.mCurrentHour = newValue;
        } else if (columnIndex == this.mColMinuteIndex) {
            this.mCurrentMinute = newValue;
        } else if (columnIndex == this.mColAmPmIndex) {
            this.mCurrentAmPmIndex = newValue;
        } else {
            throw new IllegalArgumentException("Invalid column index.");
        }
    }
}
