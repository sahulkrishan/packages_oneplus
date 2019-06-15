package com.android.settings.applications.manageapplications;

import android.support.annotation.StringRes;
import com.android.settingslib.applications.ApplicationsState.AppFilter;
import java.util.Objects;

public class AppFilterItem implements Comparable<AppFilterItem> {
    private final AppFilter mFilter;
    @FilterType
    private final int mFilterType;
    @StringRes
    private final int mTitle;

    public AppFilterItem(AppFilter filter, @FilterType int filterType, @StringRes int title) {
        this.mTitle = title;
        this.mFilterType = filterType;
        this.mFilter = filter;
    }

    public int getTitle() {
        return this.mTitle;
    }

    public AppFilter getFilter() {
        return this.mFilter;
    }

    public int getFilterType() {
        return this.mFilterType;
    }

    public int compareTo(AppFilterItem appFilter) {
        if (appFilter == null) {
            return 1;
        }
        if (this == appFilter) {
            return 0;
        }
        return this.mFilterType - appFilter.mFilterType;
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (o == null || !(o instanceof AppFilterItem)) {
            return false;
        }
        if (this == o) {
            return true;
        }
        AppFilterItem other = (AppFilterItem) o;
        if (this.mTitle == other.mTitle && this.mFilterType == other.mFilterType && this.mFilter == other.mFilter) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.mFilter, Integer.valueOf(this.mTitle), Integer.valueOf(this.mFilterType)});
    }
}
