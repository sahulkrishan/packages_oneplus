package android.support.v17.leanback.widget.picker;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v17.leanback.R;
import android.support.v17.leanback.widget.picker.PickerUtility.DateConstant;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

@RestrictTo({Scope.LIBRARY_GROUP})
public class DatePicker extends Picker {
    private static final int[] DATE_FIELDS = new int[]{5, 2, 1};
    static final String DATE_FORMAT = "MM/dd/yyyy";
    static final String LOG_TAG = "DatePicker";
    int mColDayIndex;
    int mColMonthIndex;
    int mColYearIndex;
    DateConstant mConstant;
    Calendar mCurrentDate;
    final DateFormat mDateFormat;
    private String mDatePickerFormat;
    PickerColumn mDayColumn;
    Calendar mMaxDate;
    Calendar mMinDate;
    PickerColumn mMonthColumn;
    Calendar mTempDate;
    PickerColumn mYearColumn;

    public DatePicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DatePicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mDateFormat = new SimpleDateFormat(DATE_FORMAT);
        updateCurrentLocale();
        TypedArray attributesArray = context.obtainStyledAttributes(attrs, R.styleable.lbDatePicker);
        String minDate = attributesArray.getString(R.styleable.lbDatePicker_android_minDate);
        String maxDate = attributesArray.getString(R.styleable.lbDatePicker_android_maxDate);
        this.mTempDate.clear();
        if (TextUtils.isEmpty(minDate)) {
            this.mTempDate.set(1900, 0, 1);
        } else if (!parseDate(minDate, this.mTempDate)) {
            this.mTempDate.set(1900, 0, 1);
        }
        this.mMinDate.setTimeInMillis(this.mTempDate.getTimeInMillis());
        this.mTempDate.clear();
        if (TextUtils.isEmpty(maxDate)) {
            this.mTempDate.set(2100, 0, 1);
        } else if (!parseDate(maxDate, this.mTempDate)) {
            this.mTempDate.set(2100, 0, 1);
        }
        this.mMaxDate.setTimeInMillis(this.mTempDate.getTimeInMillis());
        String datePickerFormat = attributesArray.getString(R.styleable.lbDatePicker_datePickerFormat);
        if (TextUtils.isEmpty(datePickerFormat)) {
            datePickerFormat = new String(android.text.format.DateFormat.getDateFormatOrder(context));
        }
        setDatePickerFormat(datePickerFormat);
    }

    private boolean parseDate(String date, Calendar outDate) {
        try {
            outDate.setTime(this.mDateFormat.parse(date));
            return true;
        } catch (ParseException e) {
            String str = LOG_TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Date: ");
            stringBuilder.append(date);
            stringBuilder.append(" not in format: ");
            stringBuilder.append(DATE_FORMAT);
            Log.w(str, stringBuilder.toString());
            return false;
        }
    }

    /* Access modifiers changed, original: 0000 */
    public String getBestYearMonthDayPattern(String datePickerFormat) {
        String yearPattern;
        if (PickerUtility.SUPPORTS_BEST_DATE_TIME_PATTERN) {
            yearPattern = android.text.format.DateFormat.getBestDateTimePattern(this.mConstant.locale, datePickerFormat);
        } else {
            DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getContext());
            if (dateFormat instanceof SimpleDateFormat) {
                yearPattern = ((SimpleDateFormat) dateFormat).toLocalizedPattern();
            } else {
                yearPattern = DATE_FORMAT;
            }
        }
        return TextUtils.isEmpty(yearPattern) ? DATE_FORMAT : yearPattern;
    }

    /* Access modifiers changed, original: 0000 */
    public List<CharSequence> extractSeparators() {
        String hmaPattern = getBestYearMonthDayPattern(this.mDatePickerFormat);
        List<CharSequence> separators = new ArrayList();
        StringBuilder sb = new StringBuilder();
        char[] dateFormats = new char[]{'Y', StateProperty.TARGET_Y, 'M', 'm', 'D', 'd'};
        boolean processingQuote = false;
        char lastChar = 0;
        for (int i = 0; i < hmaPattern.length(); i++) {
            char c = hmaPattern.charAt(i);
            if (c != ' ') {
                if (c != '\'') {
                    if (processingQuote) {
                        sb.append(c);
                    } else if (!isAnyOf(c, dateFormats)) {
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

    public void setDatePickerFormat(String datePickerFormat) {
        if (TextUtils.isEmpty(datePickerFormat)) {
            datePickerFormat = new String(android.text.format.DateFormat.getDateFormatOrder(getContext()));
        }
        if (!TextUtils.equals(this.mDatePickerFormat, datePickerFormat)) {
            this.mDatePickerFormat = datePickerFormat;
            List<CharSequence> separators = extractSeparators();
            if (separators.size() == datePickerFormat.length() + 1) {
                setSeparators(separators);
                this.mDayColumn = null;
                this.mMonthColumn = null;
                this.mYearColumn = null;
                this.mColMonthIndex = -1;
                this.mColDayIndex = -1;
                this.mColYearIndex = -1;
                String dateFieldsPattern = datePickerFormat.toUpperCase();
                ArrayList<PickerColumn> columns = new ArrayList(3);
                for (int i = 0; i < dateFieldsPattern.length(); i++) {
                    char charAt = dateFieldsPattern.charAt(i);
                    PickerColumn pickerColumn;
                    if (charAt != 'D') {
                        if (charAt != 'M') {
                            if (charAt != 'Y') {
                                throw new IllegalArgumentException("datePicker format error");
                            } else if (this.mYearColumn == null) {
                                pickerColumn = new PickerColumn();
                                this.mYearColumn = pickerColumn;
                                columns.add(pickerColumn);
                                this.mColYearIndex = i;
                                this.mYearColumn.setLabelFormat("%d");
                            } else {
                                throw new IllegalArgumentException("datePicker format error");
                            }
                        } else if (this.mMonthColumn == null) {
                            pickerColumn = new PickerColumn();
                            this.mMonthColumn = pickerColumn;
                            columns.add(pickerColumn);
                            this.mMonthColumn.setStaticLabels(this.mConstant.months);
                            this.mColMonthIndex = i;
                        } else {
                            throw new IllegalArgumentException("datePicker format error");
                        }
                    } else if (this.mDayColumn == null) {
                        pickerColumn = new PickerColumn();
                        this.mDayColumn = pickerColumn;
                        columns.add(pickerColumn);
                        this.mDayColumn.setLabelFormat("%02d");
                        this.mColDayIndex = i;
                    } else {
                        throw new IllegalArgumentException("datePicker format error");
                    }
                }
                setColumns(columns);
                updateSpinners(false);
                return;
            }
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Separators size: ");
            stringBuilder.append(separators.size());
            stringBuilder.append(" must equal");
            stringBuilder.append(" the size of datePickerFormat: ");
            stringBuilder.append(datePickerFormat.length());
            stringBuilder.append(" + 1");
            throw new IllegalStateException(stringBuilder.toString());
        }
    }

    public String getDatePickerFormat() {
        return this.mDatePickerFormat;
    }

    private void updateCurrentLocale() {
        this.mConstant = PickerUtility.getDateConstantInstance(Locale.getDefault(), getContext().getResources());
        this.mTempDate = PickerUtility.getCalendarForLocale(this.mTempDate, this.mConstant.locale);
        this.mMinDate = PickerUtility.getCalendarForLocale(this.mMinDate, this.mConstant.locale);
        this.mMaxDate = PickerUtility.getCalendarForLocale(this.mMaxDate, this.mConstant.locale);
        this.mCurrentDate = PickerUtility.getCalendarForLocale(this.mCurrentDate, this.mConstant.locale);
        if (this.mMonthColumn != null) {
            this.mMonthColumn.setStaticLabels(this.mConstant.months);
            setColumnAt(this.mColMonthIndex, this.mMonthColumn);
        }
    }

    public final void onColumnValueChanged(int column, int newVal) {
        this.mTempDate.setTimeInMillis(this.mCurrentDate.getTimeInMillis());
        int oldVal = getColumnAt(column).getCurrentValue();
        if (column == this.mColDayIndex) {
            this.mTempDate.add(5, newVal - oldVal);
        } else if (column == this.mColMonthIndex) {
            this.mTempDate.add(2, newVal - oldVal);
        } else if (column == this.mColYearIndex) {
            this.mTempDate.add(1, newVal - oldVal);
        } else {
            throw new IllegalArgumentException();
        }
        setDate(this.mTempDate.get(1), this.mTempDate.get(2), this.mTempDate.get(5));
        updateSpinners(false);
    }

    public void setMinDate(long minDate) {
        this.mTempDate.setTimeInMillis(minDate);
        if (this.mTempDate.get(1) != this.mMinDate.get(1) || this.mTempDate.get(6) == this.mMinDate.get(6)) {
            this.mMinDate.setTimeInMillis(minDate);
            if (this.mCurrentDate.before(this.mMinDate)) {
                this.mCurrentDate.setTimeInMillis(this.mMinDate.getTimeInMillis());
            }
            updateSpinners(false);
        }
    }

    public long getMinDate() {
        return this.mMinDate.getTimeInMillis();
    }

    public void setMaxDate(long maxDate) {
        this.mTempDate.setTimeInMillis(maxDate);
        if (this.mTempDate.get(1) != this.mMaxDate.get(1) || this.mTempDate.get(6) == this.mMaxDate.get(6)) {
            this.mMaxDate.setTimeInMillis(maxDate);
            if (this.mCurrentDate.after(this.mMaxDate)) {
                this.mCurrentDate.setTimeInMillis(this.mMaxDate.getTimeInMillis());
            }
            updateSpinners(false);
        }
    }

    public long getMaxDate() {
        return this.mMaxDate.getTimeInMillis();
    }

    public long getDate() {
        return this.mCurrentDate.getTimeInMillis();
    }

    private void setDate(int year, int month, int dayOfMonth) {
        this.mCurrentDate.set(year, month, dayOfMonth);
        if (this.mCurrentDate.before(this.mMinDate)) {
            this.mCurrentDate.setTimeInMillis(this.mMinDate.getTimeInMillis());
        } else if (this.mCurrentDate.after(this.mMaxDate)) {
            this.mCurrentDate.setTimeInMillis(this.mMaxDate.getTimeInMillis());
        }
    }

    public void updateDate(int year, int month, int dayOfMonth, boolean animation) {
        if (isNewDate(year, month, dayOfMonth)) {
            setDate(year, month, dayOfMonth);
            updateSpinners(animation);
        }
    }

    private boolean isNewDate(int year, int month, int dayOfMonth) {
        if (this.mCurrentDate.get(1) == year && this.mCurrentDate.get(2) == dayOfMonth && this.mCurrentDate.get(5) == month) {
            return false;
        }
        return true;
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
    public void updateSpinnersImpl(boolean animation) {
        int[] dateFieldIndices = new int[]{this.mColDayIndex, this.mColMonthIndex, this.mColYearIndex};
        boolean allLargerDateFieldsHaveBeenEqualToMinDate = true;
        boolean allLargerDateFieldsHaveBeenEqualToMaxDate = true;
        for (int i = DATE_FIELDS.length - 1; i >= 0; i--) {
            if (dateFieldIndices[i] >= 0) {
                boolean dateFieldChanged;
                int currField = DATE_FIELDS[i];
                PickerColumn currPickerColumn = getColumnAt(dateFieldIndices[i]);
                if (allLargerDateFieldsHaveBeenEqualToMinDate) {
                    dateFieldChanged = false | updateMin(currPickerColumn, this.mMinDate.get(currField));
                } else {
                    dateFieldChanged = false | updateMin(currPickerColumn, this.mCurrentDate.getActualMinimum(currField));
                }
                if (allLargerDateFieldsHaveBeenEqualToMaxDate) {
                    dateFieldChanged |= updateMax(currPickerColumn, this.mMaxDate.get(currField));
                } else {
                    dateFieldChanged |= updateMax(currPickerColumn, this.mCurrentDate.getActualMaximum(currField));
                }
                allLargerDateFieldsHaveBeenEqualToMinDate &= this.mCurrentDate.get(currField) == this.mMinDate.get(currField) ? 1 : 0;
                allLargerDateFieldsHaveBeenEqualToMaxDate &= this.mCurrentDate.get(currField) == this.mMaxDate.get(currField) ? 1 : 0;
                if (dateFieldChanged) {
                    setColumnAt(dateFieldIndices[i], currPickerColumn);
                }
                setColumnValue(dateFieldIndices[i], this.mCurrentDate.get(currField), animation);
            }
        }
    }

    private void updateSpinners(final boolean animation) {
        post(new Runnable() {
            public void run() {
                DatePicker.this.updateSpinnersImpl(animation);
            }
        });
    }
}
