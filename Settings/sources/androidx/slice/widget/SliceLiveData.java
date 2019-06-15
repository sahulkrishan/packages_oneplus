package androidx.slice.widget;

import android.app.PendingIntent.CanceledException;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v4.util.ArraySet;
import androidx.slice.Slice;
import androidx.slice.SliceItem;
import androidx.slice.SliceMetadata;
import androidx.slice.SliceSpec;
import androidx.slice.SliceSpecs;
import androidx.slice.SliceStructure;
import androidx.slice.SliceUtils;
import androidx.slice.SliceUtils.SliceActionListener;
import androidx.slice.SliceViewManager;
import androidx.slice.SliceViewManager.SliceCallback;
import androidx.slice.core.SliceQuery;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Set;

public final class SliceLiveData {
    @RestrictTo({Scope.LIBRARY})
    public static final SliceSpec OLD_BASIC = new SliceSpec("androidx.app.slice.BASIC", 1);
    @RestrictTo({Scope.LIBRARY})
    public static final SliceSpec OLD_LIST = new SliceSpec("androidx.app.slice.LIST", 1);
    @RestrictTo({Scope.LIBRARY})
    public static final Set<SliceSpec> SUPPORTED_SPECS = new ArraySet(Arrays.asList(new SliceSpec[]{SliceSpecs.BASIC, SliceSpecs.LIST, OLD_BASIC, OLD_LIST}));

    public interface OnErrorListener {
        public static final int ERROR_INVALID_INPUT = 3;
        public static final int ERROR_SLICE_NO_LONGER_PRESENT = 2;
        public static final int ERROR_STRUCTURE_CHANGED = 1;
        public static final int ERROR_UNKNOWN = 0;

        public @interface ErrorType {
        }

        void onSliceError(@ErrorType int i, @Nullable Throwable th);
    }

    private static class CachedLiveDataImpl extends LiveData<Slice> {
        private boolean mActive;
        private final Context mContext;
        private final OnErrorListener mListener;
        private boolean mLive;
        private Context mPendingContext;
        private Intent mPendingIntent;
        private Uri mPendingUri;
        private final SliceCallback mSliceCallback;
        private final SliceViewManager mSliceViewManager;
        private SliceStructure mStructure;
        private final Runnable mUpdateSlice;
        private Uri mUri;

        private CachedLiveDataImpl(final Context context, SliceViewManager manager, final InputStream input, final OnErrorListener listener) {
            this.mUpdateSlice = new Runnable() {
                public void run() {
                    CachedLiveDataImpl.this.mSliceCallback.onSliceUpdated(CachedLiveDataImpl.this.mSliceViewManager.bindSlice(CachedLiveDataImpl.this.mUri));
                }
            };
            this.mSliceCallback = new SliceCallback() {
                public void onSliceUpdated(@NonNull Slice s) {
                    if (CachedLiveDataImpl.this.mPendingUri != null) {
                        if (s == null) {
                            CachedLiveDataImpl.this.onSliceError(2, null);
                            return;
                        }
                        if (!CachedLiveDataImpl.this.mStructure.equals(new SliceStructure(s))) {
                            CachedLiveDataImpl.this.onSliceError(1, null);
                            return;
                        } else if (SliceMetadata.from(CachedLiveDataImpl.this.mContext, s).getLoadingState() == 2) {
                            SliceItem item = SliceQuery.findItem(s, CachedLiveDataImpl.this.mPendingUri);
                            if (item != null) {
                                try {
                                    item.fireAction(CachedLiveDataImpl.this.mPendingContext, CachedLiveDataImpl.this.mPendingIntent);
                                    CachedLiveDataImpl.this.mPendingUri = null;
                                    CachedLiveDataImpl.this.mPendingContext = null;
                                    CachedLiveDataImpl.this.mPendingIntent = null;
                                } catch (CanceledException e) {
                                    CachedLiveDataImpl.this.onSliceError(0, e);
                                    return;
                                }
                            }
                            CachedLiveDataImpl.this.onSliceError(0, new NullPointerException());
                            return;
                        }
                    }
                    CachedLiveDataImpl.this.postValue(s);
                }
            };
            this.mContext = context;
            this.mSliceViewManager = manager;
            this.mUri = null;
            this.mListener = listener;
            AsyncTask.execute(new Runnable() {
                public void run() {
                    try {
                        Slice s = SliceUtils.parseSlice(context, input, "UTF-8", new SliceActionListener() {
                            public void onSliceAction(Uri actionUri, Context context, Intent intent) {
                                CachedLiveDataImpl.this.goLive(actionUri, context, intent);
                            }
                        });
                        CachedLiveDataImpl.this.mStructure = new SliceStructure(s);
                        CachedLiveDataImpl.this.mUri = s.getUri();
                        CachedLiveDataImpl.this.postValue(s);
                    } catch (Exception e) {
                        listener.onSliceError(3, e);
                    }
                }
            });
        }

        private void goLive(Uri actionUri, Context context, Intent intent) {
            this.mLive = true;
            this.mPendingUri = actionUri;
            this.mPendingContext = context;
            this.mPendingIntent = intent;
            if (this.mActive) {
                AsyncTask.execute(this.mUpdateSlice);
                this.mSliceViewManager.registerSliceCallback(this.mUri, this.mSliceCallback);
            }
        }

        /* Access modifiers changed, original: protected */
        public void onActive() {
            this.mActive = true;
            if (this.mLive) {
                AsyncTask.execute(this.mUpdateSlice);
                this.mSliceViewManager.registerSliceCallback(this.mUri, this.mSliceCallback);
            }
        }

        /* Access modifiers changed, original: protected */
        public void onInactive() {
            this.mActive = false;
            if (this.mLive) {
                this.mSliceViewManager.unregisterSliceCallback(this.mUri, this.mSliceCallback);
            }
        }

        private void onSliceError(int error, Throwable t) {
            this.mListener.onSliceError(error, t);
            if (this.mLive) {
                this.mSliceViewManager.unregisterSliceCallback(this.mUri, this.mSliceCallback);
                this.mLive = false;
            }
        }
    }

    private static class SliceLiveDataImpl extends LiveData<Slice> {
        private final Intent mIntent;
        private final SliceCallback mSliceCallback;
        private final SliceViewManager mSliceViewManager;
        private final Runnable mUpdateSlice;
        private Uri mUri;

        private SliceLiveDataImpl(Context context, Uri uri) {
            this.mUpdateSlice = new Runnable() {
                public void run() {
                    Slice s;
                    if (SliceLiveDataImpl.this.mUri != null) {
                        s = SliceLiveDataImpl.this.mSliceViewManager.bindSlice(SliceLiveDataImpl.this.mUri);
                    } else {
                        s = SliceLiveDataImpl.this.mSliceViewManager.bindSlice(SliceLiveDataImpl.this.mIntent);
                    }
                    if (SliceLiveDataImpl.this.mUri == null && s != null) {
                        SliceLiveDataImpl.this.mUri = s.getUri();
                        SliceLiveDataImpl.this.mSliceViewManager.registerSliceCallback(SliceLiveDataImpl.this.mUri, SliceLiveDataImpl.this.mSliceCallback);
                    }
                    SliceLiveDataImpl.this.postValue(s);
                }
            };
            this.mSliceCallback = new SliceCallback() {
                public void onSliceUpdated(@NonNull Slice s) {
                    SliceLiveDataImpl.this.postValue(s);
                }
            };
            this.mSliceViewManager = SliceViewManager.getInstance(context);
            this.mUri = uri;
            this.mIntent = null;
        }

        private SliceLiveDataImpl(Context context, Intent intent) {
            this.mUpdateSlice = /* anonymous class already generated */;
            this.mSliceCallback = /* anonymous class already generated */;
            this.mSliceViewManager = SliceViewManager.getInstance(context);
            this.mUri = null;
            this.mIntent = intent;
        }

        /* Access modifiers changed, original: protected */
        public void onActive() {
            AsyncTask.execute(this.mUpdateSlice);
            if (this.mUri != null) {
                this.mSliceViewManager.registerSliceCallback(this.mUri, this.mSliceCallback);
            }
        }

        /* Access modifiers changed, original: protected */
        public void onInactive() {
            if (this.mUri != null) {
                this.mSliceViewManager.unregisterSliceCallback(this.mUri, this.mSliceCallback);
            }
        }
    }

    @NonNull
    public static LiveData<Slice> fromUri(@NonNull Context context, @NonNull Uri uri) {
        return new SliceLiveDataImpl(context.getApplicationContext(), uri, null);
    }

    @NonNull
    public static LiveData<Slice> fromIntent(@NonNull Context context, @NonNull Intent intent) {
        return new SliceLiveDataImpl(context.getApplicationContext(), intent, null);
    }

    @NonNull
    public static LiveData<Slice> fromStream(@NonNull Context context, @NonNull InputStream input, OnErrorListener listener) {
        return fromStream(context, SliceViewManager.getInstance(context), input, listener);
    }

    @RestrictTo({Scope.LIBRARY_GROUP})
    @NonNull
    public static LiveData<Slice> fromStream(@NonNull Context context, SliceViewManager manager, @NonNull InputStream input, OnErrorListener listener) {
        return new CachedLiveDataImpl(context, manager, input, listener);
    }

    private SliceLiveData() {
    }
}
