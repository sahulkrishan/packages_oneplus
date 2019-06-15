package androidx.slice;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.util.ArrayMap;
import android.util.Pair;
import androidx.slice.SliceViewManager.SliceCallback;
import androidx.slice.widget.SliceLiveData;
import java.util.concurrent.Executor;

@RestrictTo({Scope.LIBRARY})
public abstract class SliceViewManagerBase extends SliceViewManager {
    protected final Context mContext;
    private final ArrayMap<Pair<Uri, SliceCallback>, SliceListenerImpl> mListenerLookup = new ArrayMap();

    private class SliceListenerImpl {
        private final SliceCallback mCallback;
        private final Executor mExecutor;
        private final ContentObserver mObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
            public void onChange(boolean selfChange) {
                AsyncTask.execute(SliceListenerImpl.this.mUpdateSlice);
            }
        };
        private boolean mPinned;
        private final Runnable mUpdateSlice = new Runnable() {
            public void run() {
                SliceListenerImpl.this.tryPin();
                final Slice s = Slice.bindSlice(SliceViewManagerBase.this.mContext, SliceListenerImpl.this.mUri, SliceLiveData.SUPPORTED_SPECS);
                SliceListenerImpl.this.mExecutor.execute(new Runnable() {
                    public void run() {
                        SliceListenerImpl.this.mCallback.onSliceUpdated(s);
                    }
                });
            }
        };
        private Uri mUri;

        SliceListenerImpl(Uri uri, Executor executor, SliceCallback callback) {
            this.mUri = uri;
            this.mExecutor = executor;
            this.mCallback = callback;
        }

        /* Access modifiers changed, original: 0000 */
        public void startListening() {
            SliceViewManagerBase.this.mContext.getContentResolver().registerContentObserver(this.mUri, true, this.mObserver);
            tryPin();
        }

        private void tryPin() {
            if (!this.mPinned) {
                try {
                    SliceViewManagerBase.this.pinSlice(this.mUri);
                    this.mPinned = true;
                } catch (SecurityException e) {
                }
            }
        }

        /* Access modifiers changed, original: 0000 */
        public void stopListening() {
            SliceViewManagerBase.this.mContext.getContentResolver().unregisterContentObserver(this.mObserver);
            if (this.mPinned) {
                SliceViewManagerBase.this.unpinSlice(this.mUri);
                this.mPinned = false;
            }
        }
    }

    SliceViewManagerBase(Context context) {
        this.mContext = context;
    }

    public void registerSliceCallback(@NonNull Uri uri, @NonNull SliceCallback callback) {
        final Handler h = new Handler(Looper.getMainLooper());
        registerSliceCallback(uri, new Executor() {
            public void execute(@NonNull Runnable command) {
                h.post(command);
            }
        }, callback);
    }

    public void registerSliceCallback(@NonNull Uri uri, @NonNull Executor executor, @NonNull SliceCallback callback) {
        getListener(uri, callback, new SliceListenerImpl(uri, executor, callback)).startListening();
    }

    public void unregisterSliceCallback(@NonNull Uri uri, @NonNull SliceCallback callback) {
        synchronized (this.mListenerLookup) {
            SliceListenerImpl impl = (SliceListenerImpl) this.mListenerLookup.remove(new Pair(uri, callback));
            if (impl != null) {
                impl.stopListening();
            }
        }
    }

    private SliceListenerImpl getListener(Uri uri, SliceCallback callback, SliceListenerImpl listener) {
        Pair<Uri, SliceCallback> key = new Pair(uri, callback);
        synchronized (this.mListenerLookup) {
            if (this.mListenerLookup.containsKey(key)) {
                ((SliceListenerImpl) this.mListenerLookup.get(key)).stopListening();
            }
            this.mListenerLookup.put(key, listener);
        }
        return listener;
    }
}
