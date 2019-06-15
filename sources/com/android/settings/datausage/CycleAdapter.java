package com.android.settings.datausage;

import android.content.Context;
import android.net.NetworkPolicy;
import android.net.NetworkPolicyManager;
import android.net.NetworkStatsHistory.Entry;
import android.util.Pair;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settingslib.net.ChartData;
import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.Objects;

public class CycleAdapter extends ArrayAdapter<CycleItem> {
    private final OnItemSelectedListener mListener;
    private final SpinnerInterface mSpinner;

    public static class CycleItem implements Comparable<CycleItem> {
        public long end;
        public CharSequence label;
        public long start;

        public CycleItem(CharSequence label) {
            this.label = label;
        }

        public CycleItem(Context context, long start, long end) {
            this.label = Utils.formatDateRange(context, start, end);
            this.start = start;
            this.end = end;
        }

        public String toString() {
            return this.label.toString();
        }

        public boolean equals(Object o) {
            boolean z = false;
            if (!(o instanceof CycleItem)) {
                return false;
            }
            CycleItem another = (CycleItem) o;
            if (this.start == another.start && this.end == another.end) {
                z = true;
            }
            return z;
        }

        public int compareTo(CycleItem another) {
            return Long.compare(this.start, another.start);
        }
    }

    public interface SpinnerInterface {
        Object getSelectedItem();

        void setAdapter(CycleAdapter cycleAdapter);

        void setOnItemSelectedListener(OnItemSelectedListener onItemSelectedListener);

        void setSelection(int i);
    }

    public CycleAdapter(Context context, SpinnerInterface spinner, OnItemSelectedListener listener, boolean isHeader) {
        int i;
        if (isHeader) {
            i = R.layout.filter_spinner_item;
        } else {
            i = R.layout.data_usage_cycle_item;
        }
        super(context, i);
        setDropDownViewResource(17367049);
        this.mSpinner = spinner;
        this.mListener = listener;
        this.mSpinner.setAdapter(this);
        this.mSpinner.setOnItemSelectedListener(this.mListener);
    }

    public int findNearestPosition(CycleItem target) {
        if (target != null) {
            for (int i = getCount() - 1; i >= 0; i--) {
                if (((CycleItem) getItem(i)).compareTo(target) >= 0) {
                    return i;
                }
            }
        }
        return 0;
    }

    public boolean updateCycleList(NetworkPolicy policy, ChartData chartData) {
        boolean hasCycles;
        long cycleStart;
        Entry entry;
        CycleItem cycleItem;
        ChartData chartData2 = chartData;
        CycleItem previousItem = (CycleItem) this.mSpinner.getSelectedItem();
        clear();
        Context context = getContext();
        Entry entry2 = null;
        long historyStart = Long.MAX_VALUE;
        long historyEnd = Long.MIN_VALUE;
        if (chartData2 != null) {
            historyStart = chartData2.network.getStart();
            historyEnd = chartData2.network.getEnd();
        }
        long now = System.currentTimeMillis();
        if (historyStart == Long.MAX_VALUE) {
            historyStart = now;
        }
        long historyStart2 = historyStart;
        if (historyEnd == Long.MIN_VALUE) {
            historyEnd = now + 1;
        }
        long historyEnd2 = historyEnd;
        long now2;
        if (policy != null) {
            Iterator<Pair<ZonedDateTime, ZonedDateTime>> it = NetworkPolicyManager.cycleIterator(policy);
            hasCycles = false;
            while (true) {
                Iterator<Pair<ZonedDateTime, ZonedDateTime>> it2 = it;
                if (!it2.hasNext()) {
                    break;
                }
                Iterator<Pair<ZonedDateTime, ZonedDateTime>> it3;
                boolean includeCycle;
                Iterator<Pair<ZonedDateTime, ZonedDateTime>> it4;
                Pair<ZonedDateTime, ZonedDateTime> cycle = (Pair) it2.next();
                cycleStart = ((ZonedDateTime) cycle.first).toInstant().toEpochMilli();
                long cycleEnd = ((ZonedDateTime) cycle.second).toInstant().toEpochMilli();
                if (chartData2 != null) {
                    entry2 = chartData2.network.getValues(cycleStart, cycleEnd, entry2);
                    it3 = it2;
                    includeCycle = entry2.rxBytes + entry2.txBytes > 0;
                    entry = entry2;
                } else {
                    it3 = it2;
                    entry = entry2;
                    includeCycle = true;
                }
                if (includeCycle) {
                    now2 = now;
                    it4 = it3;
                    CycleItem cycleItem2 = cycleItem;
                    cycleItem = new CycleItem(context, cycleStart, cycleEnd);
                    add(cycleItem2);
                    hasCycles = true;
                } else {
                    now2 = now;
                    it4 = it3;
                }
                entry2 = entry;
                it = it4;
                now = now2;
            }
        } else {
            now2 = now;
            hasCycles = false;
        }
        if (!hasCycles) {
            Entry entry3 = entry2;
            long cycleEnd2 = historyEnd2;
            while (true) {
                now = cycleEnd2;
                if (now <= historyStart2) {
                    break;
                }
                boolean includeCycle2;
                cycleStart = now - 2419200000L;
                if (chartData2 != null) {
                    entry3 = chartData2.network.getValues(cycleStart, now, entry3);
                    includeCycle2 = entry3.rxBytes + entry3.txBytes > 0;
                    entry = entry3;
                } else {
                    entry = entry3;
                    includeCycle2 = true;
                }
                if (includeCycle2) {
                    CycleItem cycleItem3 = cycleItem;
                    cycleItem = new CycleItem(context, cycleStart, now);
                    add(cycleItem3);
                }
                cycleEnd2 = cycleStart;
                entry3 = entry;
                chartData2 = chartData;
            }
        }
        if (getCount() > 0) {
            int position = findNearestPosition(previousItem);
            this.mSpinner.setSelection(position);
            if (!Objects.equals((CycleItem) getItem(position), previousItem)) {
                this.mListener.onItemSelected(null, null, position, 0);
                return false;
            }
        }
        return true;
    }
}
