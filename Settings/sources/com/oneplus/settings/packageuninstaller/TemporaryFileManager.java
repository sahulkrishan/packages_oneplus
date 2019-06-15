package com.oneplus.settings.packageuninstaller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.Log;
import java.io.File;
import java.io.IOException;

public class TemporaryFileManager extends BroadcastReceiver {
    private static final String LOG_TAG = TemporaryFileManager.class.getSimpleName();

    @NonNull
    public static File getStagedFile(@NonNull Context context) throws IOException {
        return File.createTempFile("package", ".apk", context.getNoBackupFilesDir());
    }

    @NonNull
    public static File getInstallStateFile(@NonNull Context context) {
        return new File(context.getNoBackupFilesDir(), "install_results.xml");
    }

    @NonNull
    public static File getUninstallStateFile(@NonNull Context context) {
        return new File(context.getNoBackupFilesDir(), "uninstall_results.xml");
    }

    public void onReceive(Context context, Intent intent) {
        long systemBootTime = System.currentTimeMillis() - SystemClock.elapsedRealtime();
        File[] filesOnBoot = context.getNoBackupFilesDir().listFiles();
        if (filesOnBoot != null) {
            for (File fileOnBoot : filesOnBoot) {
                if (systemBootTime <= fileOnBoot.lastModified()) {
                    String str = LOG_TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(fileOnBoot.getName());
                    stringBuilder.append(" was created before onBoot broadcast was received");
                    Log.w(str, stringBuilder.toString());
                } else if (!fileOnBoot.delete()) {
                    String str2 = LOG_TAG;
                    StringBuilder stringBuilder2 = new StringBuilder();
                    stringBuilder2.append("Could not delete ");
                    stringBuilder2.append(fileOnBoot.getName());
                    stringBuilder2.append(" onBoot");
                    Log.w(str2, stringBuilder2.toString());
                }
            }
        }
    }
}
