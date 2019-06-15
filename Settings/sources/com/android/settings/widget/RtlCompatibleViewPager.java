package com.android.settings.widget;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.ClassLoaderCreator;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View.BaseSavedState;
import java.util.Locale;

public final class RtlCompatibleViewPager extends ViewPager {

    static class RtlSavedState extends BaseSavedState {
        public static final ClassLoaderCreator<RtlSavedState> CREATOR = new ClassLoaderCreator<RtlSavedState>() {
            public RtlSavedState createFromParcel(Parcel source, ClassLoader loader) {
                return new RtlSavedState(source, loader);
            }

            public RtlSavedState createFromParcel(Parcel in) {
                return new RtlSavedState(in, null);
            }

            public RtlSavedState[] newArray(int size) {
                return new RtlSavedState[size];
            }
        };
        int position;

        public RtlSavedState(Parcelable superState) {
            super(superState);
        }

        private RtlSavedState(Parcel in, ClassLoader loader) {
            super(in, loader);
            this.position = in.readInt();
        }

        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(this.position);
        }
    }

    public RtlCompatibleViewPager(Context context) {
        this(context, null);
    }

    public RtlCompatibleViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public int getCurrentItem() {
        return getRtlAwareIndex(super.getCurrentItem());
    }

    public void setCurrentItem(int item) {
        super.setCurrentItem(getRtlAwareIndex(item));
    }

    public Parcelable onSaveInstanceState() {
        RtlSavedState rtlSavedState = new RtlSavedState(super.onSaveInstanceState());
        rtlSavedState.position = getCurrentItem();
        return rtlSavedState;
    }

    public void onRestoreInstanceState(Parcelable state) {
        RtlSavedState rtlSavedState = (RtlSavedState) state;
        super.onRestoreInstanceState(rtlSavedState.getSuperState());
        setCurrentItem(rtlSavedState.position);
    }

    public int getRtlAwareIndex(int index) {
        if (TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == 1) {
            return (getAdapter().getCount() - index) - 1;
        }
        return index;
    }
}
