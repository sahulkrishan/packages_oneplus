package com.android.setupwizardlib.items;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import com.android.setupwizardlib.R;
import com.android.setupwizardlib.items.ItemHierarchy.Observer;
import java.util.ArrayList;
import java.util.Iterator;

public abstract class AbstractItemHierarchy implements ItemHierarchy {
    private static final String TAG = "AbstractItemHierarchy";
    private int mId = 0;
    private ArrayList<Observer> mObservers = new ArrayList();

    public AbstractItemHierarchy(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SuwAbstractItem);
        this.mId = a.getResourceId(R.styleable.SuwAbstractItem_android_id, 0);
        a.recycle();
    }

    public void setId(int id) {
        this.mId = id;
    }

    public int getId() {
        return this.mId;
    }

    public int getViewId() {
        return getId();
    }

    public void registerObserver(Observer observer) {
        this.mObservers.add(observer);
    }

    public void unregisterObserver(Observer observer) {
        this.mObservers.remove(observer);
    }

    public void notifyChanged() {
        Iterator it = this.mObservers.iterator();
        while (it.hasNext()) {
            ((Observer) it.next()).onChanged(this);
        }
    }

    public void notifyItemRangeChanged(int position, int itemCount) {
        String str;
        StringBuilder stringBuilder;
        if (position < 0) {
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("notifyItemRangeChanged: Invalid position=");
            stringBuilder.append(position);
            Log.w(str, stringBuilder.toString());
        } else if (itemCount < 0) {
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("notifyItemRangeChanged: Invalid itemCount=");
            stringBuilder.append(itemCount);
            Log.w(str, stringBuilder.toString());
        } else {
            Iterator it = this.mObservers.iterator();
            while (it.hasNext()) {
                ((Observer) it.next()).onItemRangeChanged(this, position, itemCount);
            }
        }
    }

    public void notifyItemRangeInserted(int position, int itemCount) {
        String str;
        StringBuilder stringBuilder;
        if (position < 0) {
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("notifyItemRangeInserted: Invalid position=");
            stringBuilder.append(position);
            Log.w(str, stringBuilder.toString());
        } else if (itemCount < 0) {
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("notifyItemRangeInserted: Invalid itemCount=");
            stringBuilder.append(itemCount);
            Log.w(str, stringBuilder.toString());
        } else {
            Iterator it = this.mObservers.iterator();
            while (it.hasNext()) {
                ((Observer) it.next()).onItemRangeInserted(this, position, itemCount);
            }
        }
    }

    public void notifyItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
        String str;
        StringBuilder stringBuilder;
        if (fromPosition < 0) {
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("notifyItemRangeMoved: Invalid fromPosition=");
            stringBuilder.append(fromPosition);
            Log.w(str, stringBuilder.toString());
        } else if (toPosition < 0) {
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("notifyItemRangeMoved: Invalid toPosition=");
            stringBuilder.append(toPosition);
            Log.w(str, stringBuilder.toString());
        } else if (itemCount < 0) {
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("notifyItemRangeMoved: Invalid itemCount=");
            stringBuilder.append(itemCount);
            Log.w(str, stringBuilder.toString());
        } else {
            Iterator it = this.mObservers.iterator();
            while (it.hasNext()) {
                ((Observer) it.next()).onItemRangeMoved(this, fromPosition, toPosition, itemCount);
            }
        }
    }

    public void notifyItemRangeRemoved(int position, int itemCount) {
        String str;
        StringBuilder stringBuilder;
        if (position < 0) {
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("notifyItemRangeInserted: Invalid position=");
            stringBuilder.append(position);
            Log.w(str, stringBuilder.toString());
        } else if (itemCount < 0) {
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("notifyItemRangeInserted: Invalid itemCount=");
            stringBuilder.append(itemCount);
            Log.w(str, stringBuilder.toString());
        } else {
            Iterator it = this.mObservers.iterator();
            while (it.hasNext()) {
                ((Observer) it.next()).onItemRangeRemoved(this, position, itemCount);
            }
        }
    }
}
