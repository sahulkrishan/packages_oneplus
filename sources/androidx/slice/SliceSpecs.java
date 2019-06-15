package androidx.slice;

import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;

@RestrictTo({Scope.LIBRARY_GROUP})
public class SliceSpecs {
    public static final SliceSpec BASIC = new SliceSpec("androidx.slice.BASIC", 1);
    public static final SliceSpec LIST = new SliceSpec("androidx.slice.LIST", 1);
    public static final SliceSpec MESSAGING = new SliceSpec("androidx.slice.MESSAGING", 1);

    private SliceSpecs() {
    }
}
