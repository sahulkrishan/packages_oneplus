package android.support.v17.leanback.util;

import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;

@RestrictTo({Scope.LIBRARY})
public final class MathUtil {
    private MathUtil() {
    }

    public static int safeLongToInt(long numLong) {
        if (((long) ((int) numLong)) == numLong) {
            return (int) numLong;
        }
        throw new ArithmeticException("Input overflows int.\n");
    }
}
