package android.arch.lifecycle;

import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

@Deprecated
public class ViewModelStores {
    private ViewModelStores() {
    }

    @Deprecated
    @MainThread
    @NonNull
    public static ViewModelStore of(@NonNull FragmentActivity activity) {
        return activity.getViewModelStore();
    }

    @Deprecated
    @MainThread
    @NonNull
    public static ViewModelStore of(@NonNull Fragment fragment) {
        return fragment.getViewModelStore();
    }
}
