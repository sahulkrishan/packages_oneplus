package androidx.slice;

import android.app.slice.SliceManager;
import android.app.slice.SliceSpec;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import androidx.slice.widget.SliceLiveData;
import java.util.Collection;
import java.util.Set;

@RequiresApi(api = 28)
@RestrictTo({Scope.LIBRARY})
class SliceViewManagerWrapper extends SliceViewManagerBase {
    private final SliceManager mManager;
    private final Set<SliceSpec> mSpecs;

    SliceViewManagerWrapper(Context context) {
        this(context, (SliceManager) context.getSystemService(SliceManager.class));
    }

    SliceViewManagerWrapper(Context context, SliceManager manager) {
        super(context);
        this.mManager = manager;
        this.mSpecs = SliceConvert.unwrap(SliceLiveData.SUPPORTED_SPECS);
    }

    public void pinSlice(@NonNull Uri uri) {
        this.mManager.pinSlice(uri, this.mSpecs);
    }

    public void unpinSlice(@NonNull Uri uri) {
        this.mManager.unpinSlice(uri);
    }

    @Nullable
    public Slice bindSlice(@NonNull Uri uri) {
        return SliceConvert.wrap(this.mManager.bindSlice(uri, this.mSpecs));
    }

    @Nullable
    public Slice bindSlice(@NonNull Intent intent) {
        return SliceConvert.wrap(this.mManager.bindSlice(intent, this.mSpecs));
    }

    public Collection<Uri> getSliceDescendants(Uri uri) {
        return this.mManager.getSliceDescendants(uri);
    }

    @Nullable
    public Uri mapIntentToUri(@NonNull Intent intent) {
        return this.mManager.mapIntentToUri(intent);
    }
}
