package com.android.settings.deviceinfo.storage;

import android.content.Context;
import android.content.pm.UserInfo;
import android.graphics.drawable.Drawable;
import android.os.UserManager;
import android.util.SparseArray;
import com.android.internal.util.Preconditions;
import com.android.settingslib.Utils;
import com.android.settingslib.utils.AsyncLoader;

public class UserIconLoader extends AsyncLoader<SparseArray<Drawable>> {
    private FetchUserIconTask mTask;

    public interface FetchUserIconTask {
        SparseArray<Drawable> getUserIcons();
    }

    public interface UserIconHandler {
        void handleUserIcons(SparseArray<Drawable> sparseArray);
    }

    public UserIconLoader(Context context, FetchUserIconTask task) {
        super(context);
        this.mTask = (FetchUserIconTask) Preconditions.checkNotNull(task);
    }

    public SparseArray<Drawable> loadInBackground() {
        return this.mTask.getUserIcons();
    }

    /* Access modifiers changed, original: protected */
    public void onDiscardResult(SparseArray<Drawable> sparseArray) {
    }

    public static SparseArray<Drawable> loadUserIconsWithContext(Context context) {
        SparseArray<Drawable> value = new SparseArray();
        UserManager um = (UserManager) context.getSystemService(UserManager.class);
        for (UserInfo userInfo : um.getUsers()) {
            value.put(userInfo.id, Utils.getUserIcon(context, um, userInfo));
        }
        return value;
    }
}
