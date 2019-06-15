package androidx.slice;

import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;

@RestrictTo({Scope.LIBRARY_GROUP})
public class SystemClock implements Clock {
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }
}
