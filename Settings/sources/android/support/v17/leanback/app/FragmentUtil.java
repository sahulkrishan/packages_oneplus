package android.support.v17.leanback.app;

import android.app.Fragment;
import android.content.Context;
import android.os.Build.VERSION;

class FragmentUtil {
    static Context getContext(Fragment fragment) {
        if (VERSION.SDK_INT >= 23) {
            return fragment.getContext();
        }
        return fragment.getActivity();
    }

    private FragmentUtil() {
    }
}
