package androidx.slice;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import androidx.slice.compat.SliceProviderCompat;
import java.util.List;
import java.util.Set;

@RestrictTo({Scope.LIBRARY})
class SliceManagerCompat extends SliceManager {
    private final Context mContext;

    SliceManagerCompat(Context context) {
        this.mContext = context;
    }

    @NonNull
    public Set<SliceSpec> getPinnedSpecs(@NonNull Uri uri) {
        return SliceProviderCompat.getPinnedSpecs(this.mContext, uri);
    }

    public int checkSlicePermission(Uri uri, int pid, int uid) {
        return SliceProviderCompat.checkSlicePermission(this.mContext, this.mContext.getPackageName(), uri, pid, uid);
    }

    public void grantSlicePermission(String toPackage, Uri uri) {
        SliceProviderCompat.grantSlicePermission(this.mContext, this.mContext.getPackageName(), toPackage, uri);
    }

    public void revokeSlicePermission(String toPackage, Uri uri) {
        SliceProviderCompat.revokeSlicePermission(this.mContext, this.mContext.getPackageName(), toPackage, uri);
    }

    public List<Uri> getPinnedSlices() {
        return SliceProviderCompat.getPinnedSlices(this.mContext);
    }
}
