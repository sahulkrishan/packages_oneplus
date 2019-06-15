package com.android.settings.development.qstile;

import android.content.ComponentName;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.service.quicksettings.TileService;
import android.support.annotation.VisibleForTesting;
import android.util.EventLog;
import android.util.Log;
import android.view.IWindowManager;
import android.view.WindowManagerGlobal;
import android.widget.Toast;
import com.android.internal.app.LocalePicker;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.statusbar.IStatusBarService.Stub;
import com.android.settingslib.development.DevelopmentSettingsEnabler;
import com.android.settingslib.development.SystemPropPoker;

public abstract class DevelopmentTiles extends TileService {
    private static final String TAG = "DevelopmentTiles";

    public static class AnimationSpeed extends DevelopmentTiles {
        /* Access modifiers changed, original: protected */
        public boolean isEnabled() {
            boolean z = false;
            try {
                if (WindowManagerGlobal.getWindowManagerService().getAnimationScale(0) != 1.0f) {
                    z = true;
                }
                return z;
            } catch (RemoteException e) {
                return false;
            }
        }

        /* Access modifiers changed, original: protected */
        public void setIsEnabled(boolean isEnabled) {
            IWindowManager wm = WindowManagerGlobal.getWindowManagerService();
            float scale = isEnabled ? 10.0f : 1.0f;
            try {
                wm.setAnimationScale(0, scale);
                wm.setAnimationScale(1, scale);
                wm.setAnimationScale(2, scale);
            } catch (RemoteException e) {
            }
        }
    }

    public static class ForceRTL extends DevelopmentTiles {
        /* Access modifiers changed, original: protected */
        public boolean isEnabled() {
            return Global.getInt(getContentResolver(), "debug.force_rtl", 0) != 0;
        }

        /* Access modifiers changed, original: protected */
        public void setIsEnabled(boolean isEnabled) {
            Global.putInt(getContentResolver(), "debug.force_rtl", isEnabled);
            SystemProperties.set("debug.force_rtl", isEnabled ? "1" : "0");
            LocalePicker.updateLocales(getResources().getConfiguration().getLocales());
        }
    }

    public static class GPUProfiling extends DevelopmentTiles {
        /* Access modifiers changed, original: protected */
        public boolean isEnabled() {
            return SystemProperties.get("debug.hwui.profile").equals("visual_bars");
        }

        /* Access modifiers changed, original: protected */
        public void setIsEnabled(boolean isEnabled) {
            SystemProperties.set("debug.hwui.profile", isEnabled ? "visual_bars" : "");
        }
    }

    public static class ShowLayout extends DevelopmentTiles {
        /* Access modifiers changed, original: protected */
        public boolean isEnabled() {
            return SystemProperties.getBoolean("debug.layout", false);
        }

        /* Access modifiers changed, original: protected */
        public void setIsEnabled(boolean isEnabled) {
            SystemProperties.set("debug.layout", isEnabled ? "true" : "false");
        }
    }

    public static class WinscopeTrace extends DevelopmentTiles {
        @VisibleForTesting
        static final int SURFACE_FLINGER_LAYER_TRACE_CONTROL_CODE = 1025;
        @VisibleForTesting
        static final int SURFACE_FLINGER_LAYER_TRACE_STATUS_CODE = 1026;
        private IBinder mSurfaceFlinger;
        private Toast mToast;
        private IWindowManager mWindowManager;

        public void onCreate() {
            super.onCreate();
            this.mWindowManager = WindowManagerGlobal.getWindowManagerService();
            this.mSurfaceFlinger = ServiceManager.getService("SurfaceFlinger");
            this.mToast = Toast.makeText(getApplicationContext(), "Trace files written to /data/misc/wmtrace", 1);
        }

        private boolean isWindowTraceEnabled() {
            try {
                return this.mWindowManager.isWindowTraceEnabled();
            } catch (RemoteException e) {
                String str = DevelopmentTiles.TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Could not get window trace status, defaulting to false.");
                stringBuilder.append(e.toString());
                Log.e(str, stringBuilder.toString());
                return false;
            }
        }

        /* JADX WARNING: Missing block: B:6:0x0023, code skipped:
            if (r2 != null) goto L_0x0025;
     */
        /* JADX WARNING: Missing block: B:7:0x0025, code skipped:
            r2.recycle();
            r1.recycle();
     */
        /* JADX WARNING: Missing block: B:12:0x0049, code skipped:
            if (r2 == null) goto L_0x004c;
     */
        /* JADX WARNING: Missing block: B:13:0x004c, code skipped:
            return r0;
     */
        private boolean isLayerTraceEnabled() {
            /*
            r7 = this;
            r0 = 0;
            r1 = 0;
            r2 = 0;
            r3 = r7.mSurfaceFlinger;	 Catch:{ RemoteException -> 0x002e }
            if (r3 == 0) goto L_0x0023;
        L_0x0007:
            r3 = android.os.Parcel.obtain();	 Catch:{ RemoteException -> 0x002e }
            r1 = r3;
            r3 = android.os.Parcel.obtain();	 Catch:{ RemoteException -> 0x002e }
            r2 = r3;
            r3 = "android.ui.ISurfaceComposer";
            r2.writeInterfaceToken(r3);	 Catch:{ RemoteException -> 0x002e }
            r3 = r7.mSurfaceFlinger;	 Catch:{ RemoteException -> 0x002e }
            r4 = 1026; // 0x402 float:1.438E-42 double:5.07E-321;
            r5 = 0;
            r3.transact(r4, r2, r1, r5);	 Catch:{ RemoteException -> 0x002e }
            r3 = r1.readBoolean();	 Catch:{ RemoteException -> 0x002e }
            r0 = r3;
        L_0x0023:
            if (r2 == 0) goto L_0x004c;
        L_0x0025:
            r2.recycle();
            r1.recycle();
            goto L_0x004c;
        L_0x002c:
            r3 = move-exception;
            goto L_0x004d;
        L_0x002e:
            r3 = move-exception;
            r4 = "DevelopmentTiles";
            r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x002c }
            r5.<init>();	 Catch:{ all -> 0x002c }
            r6 = "Could not get layer trace status, defaulting to false.";
            r5.append(r6);	 Catch:{ all -> 0x002c }
            r6 = r3.toString();	 Catch:{ all -> 0x002c }
            r5.append(r6);	 Catch:{ all -> 0x002c }
            r5 = r5.toString();	 Catch:{ all -> 0x002c }
            android.util.Log.e(r4, r5);	 Catch:{ all -> 0x002c }
            if (r2 == 0) goto L_0x004c;
        L_0x004b:
            goto L_0x0025;
        L_0x004c:
            return r0;
        L_0x004d:
            if (r2 == 0) goto L_0x0055;
        L_0x004f:
            r2.recycle();
            r1.recycle();
        L_0x0055:
            throw r3;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.settings.development.qstile.DevelopmentTiles$WinscopeTrace.isLayerTraceEnabled():boolean");
        }

        /* Access modifiers changed, original: protected */
        public boolean isEnabled() {
            return isWindowTraceEnabled() || isLayerTraceEnabled();
        }

        private void setWindowTraceEnabled(boolean isEnabled) {
            if (isEnabled) {
                try {
                    this.mWindowManager.startWindowTrace();
                    return;
                } catch (RemoteException e) {
                    String str = DevelopmentTiles.TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Could not set window trace status.");
                    stringBuilder.append(e.toString());
                    Log.e(str, stringBuilder.toString());
                    return;
                }
            }
            this.mWindowManager.stopWindowTrace();
        }

        /* JADX WARNING: Failed to extract finally block: empty outs */
        private void setLayerTraceEnabled(boolean r6) {
            /*
            r5 = this;
            r0 = 0;
            r1 = r0;
            r2 = r5.mSurfaceFlinger;	 Catch:{ RemoteException -> 0x0023 }
            if (r2 == 0) goto L_0x001b;
        L_0x0006:
            r2 = android.os.Parcel.obtain();	 Catch:{ RemoteException -> 0x0023 }
            r1 = r2;
            r2 = "android.ui.ISurfaceComposer";
            r1.writeInterfaceToken(r2);	 Catch:{ RemoteException -> 0x0023 }
            r1.writeInt(r6);	 Catch:{ RemoteException -> 0x0023 }
            r2 = r5.mSurfaceFlinger;	 Catch:{ RemoteException -> 0x0023 }
            r3 = 1025; // 0x401 float:1.436E-42 double:5.064E-321;
            r4 = 0;
            r2.transact(r3, r1, r0, r4);	 Catch:{ RemoteException -> 0x0023 }
        L_0x001b:
            if (r1 == 0) goto L_0x0041;
        L_0x001d:
            r1.recycle();
            goto L_0x0041;
        L_0x0021:
            r0 = move-exception;
            goto L_0x0042;
        L_0x0023:
            r0 = move-exception;
            r2 = "DevelopmentTiles";
            r3 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0021 }
            r3.<init>();	 Catch:{ all -> 0x0021 }
            r4 = "Could not set layer tracing.";
            r3.append(r4);	 Catch:{ all -> 0x0021 }
            r4 = r0.toString();	 Catch:{ all -> 0x0021 }
            r3.append(r4);	 Catch:{ all -> 0x0021 }
            r3 = r3.toString();	 Catch:{ all -> 0x0021 }
            android.util.Log.e(r2, r3);	 Catch:{ all -> 0x0021 }
            if (r1 == 0) goto L_0x0041;
        L_0x0040:
            goto L_0x001d;
        L_0x0041:
            return;
        L_0x0042:
            if (r1 == 0) goto L_0x0047;
        L_0x0044:
            r1.recycle();
        L_0x0047:
            throw r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.settings.development.qstile.DevelopmentTiles$WinscopeTrace.setLayerTraceEnabled(boolean):void");
        }

        /* Access modifiers changed, original: protected */
        public void setIsEnabled(boolean isEnabled) {
            setWindowTraceEnabled(isEnabled);
            setLayerTraceEnabled(isEnabled);
            if (!isEnabled) {
                this.mToast.show();
            }
        }
    }

    public abstract boolean isEnabled();

    public abstract void setIsEnabled(boolean z);

    public void onStartListening() {
        super.onStartListening();
        refresh();
    }

    public void refresh() {
        int state;
        if (DevelopmentSettingsEnabler.isDevelopmentSettingsEnabled(this)) {
            state = isEnabled() ? 2 : 1;
        } else {
            if (isEnabled()) {
                setIsEnabled(false);
                SystemPropPoker.getInstance().poke();
            }
            ComponentName cn = new ComponentName(getPackageName(), getClass().getName());
            try {
                getPackageManager().setComponentEnabledSetting(cn, 2, 1);
                IStatusBarService statusBarService = Stub.asInterface(ServiceManager.checkService("statusbar"));
                if (statusBarService != null) {
                    EventLog.writeEvent(1397638484, "117770924");
                    statusBarService.remTile(cn);
                }
            } catch (RemoteException e) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Failed to modify QS tile for component ");
                stringBuilder.append(cn.toString());
                Log.e(str, stringBuilder.toString(), e);
            }
            state = 0;
        }
        try {
            getQsTile().setState(state);
            getQsTile().updateTile();
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    public void onClick() {
        boolean z = true;
        if (getQsTile().getState() != 1) {
            z = false;
        }
        setIsEnabled(z);
        SystemPropPoker.getInstance().poke();
        refresh();
    }
}
