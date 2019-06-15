package androidx.slice;

import android.app.slice.SliceManager;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import java.util.List;
import java.util.Set;

@RequiresApi(api = 28)
@RestrictTo({Scope.LIBRARY})
class SliceManagerWrapper extends SliceManager {
    private final Context mContext;
    private final SliceManager mManager;

    SliceManagerWrapper(Context context) {
        this(context, (SliceManager) context.getSystemService(SliceManager.class));
    }

    SliceManagerWrapper(Context context, SliceManager manager) {
        this.mContext = context;
        this.mManager = manager;
    }

    @NonNull
    public Set<SliceSpec> getPinnedSpecs(@NonNull Uri uri) {
        return SliceConvert.wrap(this.mManager.getPinnedSpecs(uri));
    }

    public int checkSlicePermission(@NonNull Uri uri, int pid, int uid) {
        return this.mManager.checkSlicePermission(uri, pid, uid);
    }

    public void grantSlicePermission(@NonNull String toPackage, @NonNull Uri uri) {
        this.mManager.grantSlicePermission(toPackage, uri);
    }

    public void revokeSlicePermission(@NonNull String toPackage, @NonNull Uri uri) {
        this.mManager.revokeSlicePermission(toPackage, uri);
    }

    public List<Uri> getPinnedSlices() {
        return this.mManager.getPinnedSlices();
    }
}
