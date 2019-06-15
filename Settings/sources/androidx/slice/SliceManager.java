package androidx.slice;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v4.os.BuildCompat;
import java.util.List;
import java.util.Set;

public abstract class SliceManager {
    public abstract int checkSlicePermission(@NonNull Uri uri, int i, int i2);

    @NonNull
    public abstract List<Uri> getPinnedSlices();

    @NonNull
    public abstract Set<SliceSpec> getPinnedSpecs(@NonNull Uri uri);

    public abstract void grantSlicePermission(@NonNull String str, @NonNull Uri uri);

    public abstract void revokeSlicePermission(@NonNull String str, @NonNull Uri uri);

    @NonNull
    public static SliceManager getInstance(@NonNull Context context) {
        if (BuildCompat.isAtLeastP()) {
            return new SliceManagerWrapper(context);
        }
        return new SliceManagerCompat(context);
    }

    @RestrictTo({Scope.LIBRARY})
    SliceManager() {
    }
}
