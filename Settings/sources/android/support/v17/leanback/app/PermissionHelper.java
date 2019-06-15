package android.support.v17.leanback.app;

import android.app.Fragment;
import android.os.Build.VERSION;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;

@RestrictTo({Scope.LIBRARY_GROUP})
public class PermissionHelper {
    public static void requestPermissions(Fragment fragment, String[] permissions, int requestCode) {
        if (VERSION.SDK_INT >= 23) {
            fragment.requestPermissions(permissions, requestCode);
        }
    }

    private PermissionHelper() {
    }
}
