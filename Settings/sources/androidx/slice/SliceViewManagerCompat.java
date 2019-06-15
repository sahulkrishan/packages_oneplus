package androidx.slice;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import androidx.slice.compat.SliceProviderCompat;
import androidx.slice.widget.SliceLiveData;
import java.util.Collection;

@RestrictTo({Scope.LIBRARY})
class SliceViewManagerCompat extends SliceViewManagerBase {
    SliceViewManagerCompat(Context context) {
        super(context);
    }

    public void pinSlice(@NonNull Uri uri) {
        SliceProviderCompat.pinSlice(this.mContext, uri, SliceLiveData.SUPPORTED_SPECS);
    }

    public void unpinSlice(@NonNull Uri uri) {
        SliceProviderCompat.unpinSlice(this.mContext, uri, SliceLiveData.SUPPORTED_SPECS);
    }

    @Nullable
    public Slice bindSlice(@NonNull Uri uri) {
        return SliceProviderCompat.bindSlice(this.mContext, uri, SliceLiveData.SUPPORTED_SPECS);
    }

    @Nullable
    public Slice bindSlice(@NonNull Intent intent) {
        return SliceProviderCompat.bindSlice(this.mContext, intent, SliceLiveData.SUPPORTED_SPECS);
    }

    @Nullable
    public Uri mapIntentToUri(@NonNull Intent intent) {
        return SliceProviderCompat.mapIntentToUri(this.mContext, intent);
    }

    public Collection<Uri> getSliceDescendants(Uri uri) {
        return SliceProviderCompat.getSliceDescendants(this.mContext, uri);
    }
}
