package android.arch.lifecycle;

import android.arch.core.internal.SafeIterableMap;
import android.support.annotation.CallSuper;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.util.Iterator;
import java.util.Map.Entry;

public class MediatorLiveData<T> extends MutableLiveData<T> {
    private SafeIterableMap<LiveData<?>, Source<?>> mSources = new SafeIterableMap();

    private static class Source<V> implements Observer<V> {
        final LiveData<V> mLiveData;
        final Observer<? super V> mObserver;
        int mVersion = -1;

        Source(LiveData<V> liveData, Observer<? super V> observer) {
            this.mLiveData = liveData;
            this.mObserver = observer;
        }

        /* Access modifiers changed, original: 0000 */
        public void plug() {
            this.mLiveData.observeForever(this);
        }

        /* Access modifiers changed, original: 0000 */
        public void unplug() {
            this.mLiveData.removeObserver(this);
        }

        public void onChanged(@Nullable V v) {
            if (this.mVersion != this.mLiveData.getVersion()) {
                this.mVersion = this.mLiveData.getVersion();
                this.mObserver.onChanged(v);
            }
        }
    }

    @MainThread
    public <S> void addSource(@NonNull LiveData<S> source, @NonNull Observer<? super S> onChanged) {
        Source<S> e = new Source(source, onChanged);
        Source<?> existing = (Source) this.mSources.putIfAbsent(source, e);
        if (existing == null || existing.mObserver == onChanged) {
            if (existing == null && hasActiveObservers()) {
                e.plug();
            }
            return;
        }
        throw new IllegalArgumentException("This source was already added with the different observer");
    }

    @MainThread
    public <S> void removeSource(@NonNull LiveData<S> toRemote) {
        Source<?> source = (Source) this.mSources.remove(toRemote);
        if (source != null) {
            source.unplug();
        }
    }

    /* Access modifiers changed, original: protected */
    @CallSuper
    public void onActive() {
        Iterator it = this.mSources.iterator();
        while (it.hasNext()) {
            ((Source) ((Entry) it.next()).getValue()).plug();
        }
    }

    /* Access modifiers changed, original: protected */
    @CallSuper
    public void onInactive() {
        Iterator it = this.mSources.iterator();
        while (it.hasNext()) {
            ((Source) ((Entry) it.next()).getValue()).unplug();
        }
    }
}
