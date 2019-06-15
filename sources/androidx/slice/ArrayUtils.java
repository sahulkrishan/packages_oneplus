package androidx.slice;

import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v4.util.ObjectsCompat;
import java.lang.reflect.Array;

@RestrictTo({Scope.LIBRARY})
class ArrayUtils {
    public static <T> boolean contains(T[] array, T item) {
        for (T t : array) {
            if (ObjectsCompat.equals(t, item)) {
                return true;
            }
        }
        return false;
    }

    public static <T> T[] appendElement(Class<T> kind, T[] array, T element) {
        int end;
        T[] result;
        if (array != null) {
            end = array.length;
            result = (Object[]) Array.newInstance(kind, end + 1);
            System.arraycopy(array, 0, result, 0, end);
        } else {
            end = 0;
            result = (Object[]) Array.newInstance(kind, 1);
        }
        result[end] = element;
        return result;
    }

    public static <T> T[] removeElement(Class<T> kind, T[] array, T element) {
        if (array == null || !contains(array, element)) {
            return array;
        }
        int length = array.length;
        int i = 0;
        while (i < length) {
            if (!ObjectsCompat.equals(array[i], element)) {
                i++;
            } else if (length == 1) {
                return null;
            } else {
                Object[] result = (Object[]) Array.newInstance(kind, length - 1);
                System.arraycopy(array, 0, result, 0, i);
                System.arraycopy(array, i + 1, result, i, (length - i) - 1);
                return result;
            }
        }
        return array;
    }

    private ArrayUtils() {
    }
}
