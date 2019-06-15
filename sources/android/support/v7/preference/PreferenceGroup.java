package android.support.v7.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.annotation.VisibleForTesting;
import android.support.v4.content.res.TypedArrayUtils;
import android.support.v4.util.SimpleArrayMap;
import android.support.v7.preference.Preference.BaseSavedState;
import android.text.TextUtils;
import android.util.AttributeSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class PreferenceGroup extends Preference {
    private boolean mAttachedToHierarchy;
    private final Runnable mClearRecycleCacheRunnable;
    private int mCurrentPreferenceOrder;
    private final Handler mHandler;
    private final SimpleArrayMap<String, Long> mIdRecycleCache;
    private int mInitialExpandedChildrenCount;
    private boolean mOrderingAsAdded;
    private PreferenceInstanceStateCallback mPreferenceInstanceStateCallback;
    private List<Preference> mPreferenceList;

    interface PreferenceInstanceStateCallback {
        Parcelable restoreInstanceState(Parcelable parcelable);

        Parcelable saveInstanceState(Parcelable parcelable);
    }

    public interface PreferencePositionCallback {
        int getPreferenceAdapterPosition(Preference preference);

        int getPreferenceAdapterPosition(String str);
    }

    static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        int mInitialExpandedChildrenCount;

        SavedState(Parcel source) {
            super(source);
            this.mInitialExpandedChildrenCount = source.readInt();
        }

        SavedState(Parcelable superState, int initialExpandedChildrenCount) {
            super(superState);
            this.mInitialExpandedChildrenCount = initialExpandedChildrenCount;
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.mInitialExpandedChildrenCount);
        }
    }

    public PreferenceGroup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mOrderingAsAdded = true;
        this.mCurrentPreferenceOrder = 0;
        this.mAttachedToHierarchy = false;
        this.mInitialExpandedChildrenCount = Integer.MAX_VALUE;
        this.mIdRecycleCache = new SimpleArrayMap();
        this.mHandler = new Handler();
        this.mClearRecycleCacheRunnable = new Runnable() {
            public void run() {
                synchronized (this) {
                    PreferenceGroup.this.mIdRecycleCache.clear();
                }
            }
        };
        this.mPreferenceList = new ArrayList();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PreferenceGroup, defStyleAttr, defStyleRes);
        this.mOrderingAsAdded = TypedArrayUtils.getBoolean(a, 2, 2, true);
        if (a.hasValue(1)) {
            this.mInitialExpandedChildrenCount = TypedArrayUtils.getInt(a, 1, 1, -1);
        }
        a.recycle();
    }

    public PreferenceGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public PreferenceGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public void setOrderingAsAdded(boolean orderingAsAdded) {
        this.mOrderingAsAdded = orderingAsAdded;
    }

    public boolean isOrderingAsAdded() {
        return this.mOrderingAsAdded;
    }

    public void setInitialExpandedChildrenCount(int expandedCount) {
        this.mInitialExpandedChildrenCount = expandedCount;
    }

    public int getInitialExpandedChildrenCount() {
        return this.mInitialExpandedChildrenCount;
    }

    public void addItemFromInflater(Preference preference) {
        addPreference(preference);
    }

    public int getPreferenceCount() {
        return this.mPreferenceList.size();
    }

    public Preference getPreference(int index) {
        return (Preference) this.mPreferenceList.get(index);
    }

    public boolean addPreference(Preference preference) {
        if (this.mPreferenceList.contains(preference)) {
            return true;
        }
        int i;
        if (preference.getOrder() == Integer.MAX_VALUE) {
            if (this.mOrderingAsAdded) {
                i = this.mCurrentPreferenceOrder;
                this.mCurrentPreferenceOrder = i + 1;
                preference.setOrder(i);
            }
            if (preference instanceof PreferenceGroup) {
                ((PreferenceGroup) preference).setOrderingAsAdded(this.mOrderingAsAdded);
            }
        }
        i = Collections.binarySearch(this.mPreferenceList, preference);
        if (i < 0) {
            i = (i * -1) - 1;
        }
        if (!onPrepareAddPreference(preference)) {
            return false;
        }
        long id;
        synchronized (this) {
            this.mPreferenceList.add(i, preference);
        }
        PreferenceManager preferenceManager = getPreferenceManager();
        String key = preference.getKey();
        if (key == null || !this.mIdRecycleCache.containsKey(key)) {
            id = preferenceManager.getNextId();
        } else {
            id = ((Long) this.mIdRecycleCache.get(key)).longValue();
            this.mIdRecycleCache.remove(key);
        }
        preference.onAttachedToHierarchy(preferenceManager, id);
        preference.assignParent(this);
        if (this.mAttachedToHierarchy) {
            preference.onAttached();
        }
        notifyHierarchyChanged();
        return true;
    }

    public boolean removePreference(Preference preference) {
        boolean returnValue = removePreferenceInt(preference);
        notifyHierarchyChanged();
        return returnValue;
    }

    private boolean removePreferenceInt(Preference preference) {
        boolean success;
        synchronized (this) {
            preference.onPrepareForRemoval();
            if (preference.getParent() == this) {
                preference.assignParent(null);
            }
            success = this.mPreferenceList.remove(preference);
            if (success) {
                String key = preference.getKey();
                if (key != null) {
                    this.mIdRecycleCache.put(key, Long.valueOf(preference.getId()));
                    this.mHandler.removeCallbacks(this.mClearRecycleCacheRunnable);
                    this.mHandler.post(this.mClearRecycleCacheRunnable);
                }
                if (this.mAttachedToHierarchy) {
                    preference.onDetached();
                }
            }
        }
        return success;
    }

    public void removeAll() {
        synchronized (this) {
            List<Preference> preferenceList = this.mPreferenceList;
            for (int i = preferenceList.size() - 1; i >= 0; i--) {
                removePreferenceInt((Preference) preferenceList.get(0));
            }
        }
        notifyHierarchyChanged();
    }

    /* Access modifiers changed, original: protected */
    public boolean onPrepareAddPreference(Preference preference) {
        preference.onParentChanged(this, shouldDisableDependents());
        return true;
    }

    public Preference findPreference(CharSequence key) {
        if (TextUtils.equals(getKey(), key)) {
            return this;
        }
        int preferenceCount = getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            Preference preference = getPreference(i);
            String curKey = preference.getKey();
            if (curKey != null && curKey.equals(key)) {
                return preference;
            }
            if (preference instanceof PreferenceGroup) {
                Preference returnedPreference = ((PreferenceGroup) preference).findPreference(key);
                if (returnedPreference != null) {
                    return returnedPreference;
                }
            }
        }
        return null;
    }

    /* Access modifiers changed, original: protected */
    public boolean isOnSameScreenAsChildren() {
        return true;
    }

    @RestrictTo({Scope.LIBRARY_GROUP})
    public boolean isAttached() {
        return this.mAttachedToHierarchy;
    }

    public void onAttached() {
        super.onAttached();
        this.mAttachedToHierarchy = true;
        int preferenceCount = getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            getPreference(i).onAttached();
        }
    }

    public void onDetached() {
        super.onDetached();
        int i = 0;
        this.mAttachedToHierarchy = false;
        int preferenceCount = getPreferenceCount();
        while (i < preferenceCount) {
            getPreference(i).onDetached();
            i++;
        }
    }

    public void notifyDependencyChange(boolean disableDependents) {
        super.notifyDependencyChange(disableDependents);
        int preferenceCount = getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            getPreference(i).onParentChanged(this, disableDependents);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void sortPreferences() {
        synchronized (this) {
            Collections.sort(this.mPreferenceList);
        }
    }

    /* Access modifiers changed, original: protected */
    public void dispatchSaveInstanceState(Bundle container) {
        super.dispatchSaveInstanceState(container);
        int preferenceCount = getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            getPreference(i).dispatchSaveInstanceState(container);
        }
    }

    /* Access modifiers changed, original: protected */
    public void dispatchRestoreInstanceState(Bundle container) {
        super.dispatchRestoreInstanceState(container);
        int preferenceCount = getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            getPreference(i).dispatchRestoreInstanceState(container);
        }
    }

    /* Access modifiers changed, original: protected */
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        if (this.mPreferenceInstanceStateCallback != null) {
            return this.mPreferenceInstanceStateCallback.saveInstanceState(superState);
        }
        return superState;
    }

    /* Access modifiers changed, original: protected */
    public void onRestoreInstanceState(Parcelable state) {
        if (this.mPreferenceInstanceStateCallback != null) {
            state = this.mPreferenceInstanceStateCallback.restoreInstanceState(state);
        }
        super.onRestoreInstanceState(state);
    }

    /* Access modifiers changed, original: final */
    public final void setPreferenceInstanceStateCallback(PreferenceInstanceStateCallback callback) {
        this.mPreferenceInstanceStateCallback = callback;
    }

    /* Access modifiers changed, original: final */
    @VisibleForTesting
    public final PreferenceInstanceStateCallback getPreferenceInstanceStateCallback() {
        return this.mPreferenceInstanceStateCallback;
    }
}
