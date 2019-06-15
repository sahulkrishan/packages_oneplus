package android.support.v17.leanback.widget;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.util.LruCache;
import android.util.SparseArray;
import android.view.View;
import java.util.Map;
import java.util.Map.Entry;

class ViewsStateBundle {
    public static final int LIMIT_DEFAULT = 100;
    public static final int UNLIMITED = Integer.MAX_VALUE;
    private LruCache<String, SparseArray<Parcelable>> mChildStates;
    private int mLimitNumber = 100;
    private int mSavePolicy = 0;

    public void clear() {
        if (this.mChildStates != null) {
            this.mChildStates.evictAll();
        }
    }

    public void remove(int id) {
        if (this.mChildStates != null && this.mChildStates.size() != 0) {
            this.mChildStates.remove(getSaveStatesKey(id));
        }
    }

    public final Bundle saveAsBundle() {
        if (this.mChildStates == null || this.mChildStates.size() == 0) {
            return null;
        }
        Map<String, SparseArray<Parcelable>> snapshot = this.mChildStates.snapshot();
        Bundle bundle = new Bundle();
        for (Entry<String, SparseArray<Parcelable>> e : snapshot.entrySet()) {
            bundle.putSparseParcelableArray((String) e.getKey(), (SparseArray) e.getValue());
        }
        return bundle;
    }

    public final void loadFromBundle(Bundle savedBundle) {
        if (this.mChildStates != null && savedBundle != null) {
            this.mChildStates.evictAll();
            for (String key : savedBundle.keySet()) {
                this.mChildStates.put(key, savedBundle.getSparseParcelableArray(key));
            }
        }
    }

    public final int getSavePolicy() {
        return this.mSavePolicy;
    }

    public final int getLimitNumber() {
        return this.mLimitNumber;
    }

    public final void setSavePolicy(int savePolicy) {
        this.mSavePolicy = savePolicy;
        applyPolicyChanges();
    }

    public final void setLimitNumber(int limitNumber) {
        this.mLimitNumber = limitNumber;
        applyPolicyChanges();
    }

    /* Access modifiers changed, original: protected */
    public void applyPolicyChanges() {
        if (this.mSavePolicy == 2) {
            if (this.mLimitNumber <= 0) {
                throw new IllegalArgumentException();
            } else if (this.mChildStates == null || this.mChildStates.maxSize() != this.mLimitNumber) {
                this.mChildStates = new LruCache(this.mLimitNumber);
            }
        } else if (this.mSavePolicy != 3 && this.mSavePolicy != 1) {
            this.mChildStates = null;
        } else if (this.mChildStates == null || this.mChildStates.maxSize() != Integer.MAX_VALUE) {
            this.mChildStates = new LruCache(Integer.MAX_VALUE);
        }
    }

    public final void loadView(View view, int id) {
        if (this.mChildStates != null) {
            SparseArray<Parcelable> container = (SparseArray) this.mChildStates.remove(getSaveStatesKey(id));
            if (container != null) {
                view.restoreHierarchyState(container);
            }
        }
    }

    /* Access modifiers changed, original: protected|final */
    public final void saveViewUnchecked(View view, int id) {
        if (this.mChildStates != null) {
            String key = getSaveStatesKey(id);
            SparseArray<Parcelable> container = new SparseArray();
            view.saveHierarchyState(container);
            this.mChildStates.put(key, container);
        }
    }

    public final Bundle saveOnScreenView(Bundle bundle, View view, int id) {
        if (this.mSavePolicy != 0) {
            String key = getSaveStatesKey(id);
            SparseArray<Parcelable> container = new SparseArray();
            view.saveHierarchyState(container);
            if (bundle == null) {
                bundle = new Bundle();
            }
            bundle.putSparseParcelableArray(key, container);
        }
        return bundle;
    }

    public final void saveOffscreenView(View view, int id) {
        switch (this.mSavePolicy) {
            case 1:
                remove(id);
                return;
            case 2:
            case 3:
                saveViewUnchecked(view, id);
                return;
            default:
                return;
        }
    }

    static String getSaveStatesKey(int id) {
        return Integer.toString(id);
    }
}
