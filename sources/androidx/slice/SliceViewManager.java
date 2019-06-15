package androidx.slice;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.annotation.WorkerThread;
import android.support.v4.os.BuildCompat;
import java.util.Collection;
import java.util.concurrent.Executor;

public abstract class SliceViewManager {

    public interface SliceCallback {
        void onSliceUpdated(@NonNull Slice slice);
    }

    @Nullable
    public abstract Slice bindSlice(@NonNull Intent intent);

    @Nullable
    public abstract Slice bindSlice(@NonNull Uri uri);

    @WorkerThread
    @NonNull
    public abstract Collection<Uri> getSliceDescendants(@NonNull Uri uri);

    @Nullable
    public abstract Uri mapIntentToUri(@NonNull Intent intent);

    public abstract void pinSlice(@NonNull Uri uri);

    public abstract void registerSliceCallback(@NonNull Uri uri, @NonNull SliceCallback sliceCallback);

    public abstract void registerSliceCallback(@NonNull Uri uri, @NonNull Executor executor, @NonNull SliceCallback sliceCallback);

    public abstract void unpinSlice(@NonNull Uri uri);

    public abstract void unregisterSliceCallback(@NonNull Uri uri, @NonNull SliceCallback sliceCallback);

    @NonNull
    public static SliceViewManager getInstance(@NonNull Context context) {
        if (BuildCompat.isAtLeastP()) {
            return new SliceViewManagerWrapper(context);
        }
        return new SliceViewManagerCompat(context);
    }

    @RestrictTo({Scope.LIBRARY})
    SliceViewManager() {
    }
}
