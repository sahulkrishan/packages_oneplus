package com.oneplus.settings;

import android.content.Context;
import android.os.ConditionVariable;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.storage.IStorageManager;
import android.os.storage.IStorageManager.Stub;
import android.text.TextUtils;
import android.util.Log;
import com.oneplus.settings.utils.OPUtils;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

public class OPRebootWipeUserdata {
    private static File COMMAND_FILE = new File(RECOVERY_DIR, "command");
    private static File COMMAND_FILE_OP2 = new File(RECOVERY_DIR_OP2, "command");
    private static File LOG_FILE = new File(RECOVERY_DIR, "log");
    private static File LOG_FILE_OP2 = new File(RECOVERY_DIR_OP2, "log");
    private static File RECOVERY_DIR = new File("/cache/recovery");
    private static File RECOVERY_DIR_OP2 = new File("/op2/recovery");
    private static final String TAG = "OPRebootWipeUserdata";

    public static void rebootWipeUserData(Context context, boolean shutdown, String reason, String wipeType, String password) throws IOException {
        ConditionVariable condition = new ConditionVariable();
        String shutdownArg = null;
        if (shutdown) {
            shutdownArg = "--shutdown_after";
        }
        String reasonArg = null;
        if (!TextUtils.isEmpty(reason)) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("--reason=");
            stringBuilder.append(sanitizeArg(reason));
            reasonArg = stringBuilder.toString();
        }
        String localeArg = new StringBuilder();
        localeArg.append("--locale=");
        localeArg.append(Locale.getDefault().toString());
        localeArg = localeArg.toString();
        String psword = new StringBuilder();
        psword.append("--password=");
        psword.append(password);
        psword = psword.toString();
        bootCommand(context, shutdownArg, wipeType, reasonArg, localeArg, psword);
    }

    private static String sanitizeArg(String arg) {
        return arg.replace(0, '?').replace(10, '?');
    }

    private static void bootCommand(Context context, String... args) throws IOException {
        FileWriter command;
        Log.d(TAG, "bootCommand start");
        if (OPUtils.isSupportOP2Recovey()) {
            RECOVERY_DIR_OP2.mkdirs();
            COMMAND_FILE_OP2.delete();
            LOG_FILE_OP2.delete();
            command = new FileWriter(COMMAND_FILE_OP2);
        } else {
            RECOVERY_DIR.mkdirs();
            COMMAND_FILE.delete();
            LOG_FILE.delete();
            command = new FileWriter(COMMAND_FILE);
        }
        try {
            for (String arg : args) {
                if (!TextUtils.isEmpty(arg)) {
                    command.write(arg);
                    command.write("\n");
                }
            }
            try {
                IBinder service = ServiceManager.getService("mount");
                Log.d(TAG, "bootCommand get mount Service");
                IStorageManager mountService = Stub.asInterface(service);
                mountService.setField("SystemLocale", "");
                Log.d(TAG, "bootCommand setField StorageManager.SYSTEM_LOCALE_KEY");
                mountService.setField("PatternVisible", "");
                Log.d(TAG, "bootCommand setField StorageManager.PATTERN_VISIBLE_KEY");
                mountService.setField("PasswordVisible", "");
                Log.d(TAG, "bootCommand setField StorageManager.PASSWORD_VISIBLE_KEY");
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            ((PowerManager) context.getSystemService("power")).reboot("recovery");
            throw new IOException("Reboot failed (no permissions?)");
        } finally {
            command.close();
        }
    }
}
