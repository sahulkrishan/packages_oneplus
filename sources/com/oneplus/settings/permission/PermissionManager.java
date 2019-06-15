package com.oneplus.settings.permission;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.GlobalPermissionState.PackagePermissionState;
import android.util.Log;
import java.util.List;

public class PermissionManager {
    private static final String CLS_NAME_SERVICE = "com.oneplus.permissionutil.ControlService";
    private static final String KEY_GRANT_STATE = "key_is_granted";
    public static final String KEY_PERM_NAME = "key_perm_name";
    private static final String KEY_PKG_NAME = "key_pkg_name";
    private static final String KEY_PKG_PEMISSIONS = "KEY_PKG_PEMISSIONS";
    private static final String KEY_USER_FIXED = "key_is_user_fixed";
    private static final String LOG_TAG = "PermissionManager";
    public static final int MSG_GET_PACKAGE_PERMISSION_STATES = 5;
    public static final int MSG_REPLY_PACKAGE_PERMISSION_STATES = 6;
    public static final int MSG_REPLY_UPDATE_RESULT = 2;
    public static final int MSG_SET_USER_DECISION = 1;
    private static final String PKG_NAME_CUSTOM_PERMISSION_UTIL = "com.oneplus.permissionutil";
    private Callback mCallback = null;
    private Context mContext;
    private Handler mHandler;
    private boolean mIsBound = false;
    private Messenger mLocalClient;
    private Object mLock = new Object();
    private Messenger mRemoteService;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(PermissionManager.LOG_TAG, "Connected to custom permission control service.");
            PermissionManager.this.mRemoteService = new Messenger(service);
            if (PermissionManager.this.mCallback != null) {
                PermissionManager.this.mCallback.onServiceConnected();
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            PermissionManager.this.mRemoteService = null;
            if (PermissionManager.this.mWorker != null) {
                PermissionManager.this.mWorker.quitSafely();
            }
        }
    };
    private HandlerThread mWorker;

    public interface Callback {
        void onPermissionDataObtained(PackagePermissionState packagePermissionState);

        void onPermissionDataUpdated(int i);

        void onServiceConnected();
    }

    private static class Holder {
        static final PermissionManager INSTANCE = new PermissionManager();

        private Holder() {
        }
    }

    private class IncomingHandler extends Handler {
        public IncomingHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 2) {
                i = msg.arg1;
                if (PermissionManager.this.mCallback != null) {
                    PermissionManager.this.mCallback.onPermissionDataUpdated(i);
                }
            } else if (i == 6) {
                PackagePermissionState permissionState = (PackagePermissionState) msg.getData().getParcelable(PermissionManager.KEY_PKG_PEMISSIONS);
                if (PermissionManager.this.mCallback != null) {
                    PermissionManager.this.mCallback.onPermissionDataObtained(permissionState);
                }
            }
        }
    }

    public static PermissionManager get() {
        return Holder.INSTANCE;
    }

    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }

    public void connectToPermissionControlService(Context context) {
        synchronized (this.mLock) {
            if (!this.mIsBound) {
                this.mWorker = new HandlerThread("PermissionDataClient");
                this.mWorker.start();
                this.mHandler = new IncomingHandler(this.mWorker.getLooper());
                this.mLocalClient = new Messenger(this.mHandler);
                Intent serviceIntent = new Intent("com.oneplus.service.bind");
                serviceIntent.setComponent(new ComponentName(PKG_NAME_CUSTOM_PERMISSION_UTIL, CLS_NAME_SERVICE));
                this.mIsBound = context.bindService(serviceIntent, this.mServiceConnection, 1);
                this.mContext = context;
            } else if (this.mCallback != null) {
                this.mCallback.onServiceConnected();
            }
        }
    }

    public void disconnect(Context context) {
        synchronized (this.mLock) {
            if (this.mIsBound && this.mContext == context) {
                context.unbindService(this.mServiceConnection);
                this.mIsBound = false;
                this.mContext = null;
            }
        }
    }

    public void queryPermissionData(Context context, String packageName) {
        Bundle data = new Bundle();
        data.putString(KEY_PKG_NAME, packageName);
        Message msg = Message.obtain(null, 5);
        msg.setData(data);
        msg.replyTo = this.mLocalClient;
        try {
            if (this.mRemoteService != null) {
                this.mRemoteService.send(msg);
            }
        } catch (RemoteException e) {
            String str = LOG_TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Got exception while query permission data for ");
            stringBuilder.append(packageName);
            Log.e(str, stringBuilder.toString(), e);
        }
    }

    public void updatePermissionsData(Context context, String packageName, List<String> permissions, boolean granted, boolean userFixed) {
        if (permissions != null) {
            for (String permission : permissions) {
                updatePermissionData(context, packageName, permission, granted, userFixed);
            }
        }
    }

    public void updatePermissionData(Context context, String packageName, String permissionName, boolean granted, boolean userFixed) {
        if (!isSystemOrSystemUpdatedApp(context.getPackageManager(), packageName)) {
            String str = LOG_TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("  Update permission data to granted=");
            stringBuilder.append(granted);
            stringBuilder.append(" and fixed=");
            stringBuilder.append(userFixed);
            stringBuilder.append(" for ");
            stringBuilder.append(permissionName);
            stringBuilder.append(" for package ");
            stringBuilder.append(packageName);
            Log.d(str, stringBuilder.toString());
            Bundle data = new Bundle();
            data.putString(KEY_PKG_NAME, packageName);
            data.putString(KEY_PERM_NAME, permissionName);
            data.putBoolean(KEY_GRANT_STATE, granted);
            data.putBoolean(KEY_USER_FIXED, userFixed);
            Message msg = Message.obtain(null, 1);
            msg.setData(data);
            msg.replyTo = this.mLocalClient;
            try {
                if (this.mRemoteService != null) {
                    this.mRemoteService.send(msg);
                }
            } catch (RemoteException e) {
                String str2 = LOG_TAG;
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append("Got exception while syncing permission data for ");
                stringBuilder2.append(permissionName);
                Log.e(str2, stringBuilder2.toString(), e);
            }
        }
    }

    public static boolean isSystemOrSystemUpdatedApp(PackageManager pm, String packageName) {
        boolean systemApp = false;
        try {
            ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);
            if ((ai.flags & 1) == 0 && (ai.flags & 128) == 0) {
                return systemApp;
            }
            return true;
        } catch (NameNotFoundException e) {
            return systemApp;
        }
    }
}
