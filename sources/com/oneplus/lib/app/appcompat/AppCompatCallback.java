package com.oneplus.lib.app.appcompat;

import android.support.annotation.Nullable;
import com.oneplus.lib.app.appcompat.ActionMode.Callback;

public interface AppCompatCallback {
    void onSupportActionModeFinished(ActionMode actionMode);

    void onSupportActionModeStarted(ActionMode actionMode);

    @Nullable
    ActionMode onWindowStartingSupportActionMode(Callback callback);
}
