package com.oneplus.settings.packageuninstaller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

public class UninstallEventReceiver extends BroadcastReceiver {
    private static final Object sLock = new Object();
    private static EventResultPersister sReceiver;

    @NonNull
    private static EventResultPersister getReceiver(@NonNull Context context) {
        synchronized (sLock) {
            if (sReceiver == null) {
                sReceiver = new EventResultPersister(TemporaryFileManager.getUninstallStateFile(context));
            }
        }
        return sReceiver;
    }

    public void onReceive(Context context, Intent intent) {
        getReceiver(context).onEventReceived(context, intent);
    }

    static int addObserver(@NonNull Context context, int id, @NonNull EventResultObserver observer) throws OutOfIdsException {
        return getReceiver(context).addObserver(id, observer);
    }

    static void removeObserver(@NonNull Context context, int id) {
        getReceiver(context).removeObserver(id);
    }

    static int getNewId(@NonNull Context context) throws OutOfIdsException {
        return getReceiver(context).getNewId();
    }
}
