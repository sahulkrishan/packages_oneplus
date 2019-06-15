package com.android.settings.wrapper;

import android.content.om.IOverlayManager;
import android.content.om.IOverlayManager.Stub;
import android.os.RemoteException;
import android.os.ServiceManager;
import java.util.ArrayList;
import java.util.List;

public class OverlayManagerWrapper {
    private final IOverlayManager mOverlayManager;

    public static class OverlayInfo {
        public static final String CATEGORY_THEME = "android.theme";
        public final String category;
        private final boolean mEnabled;
        public final String packageName;

        public OverlayInfo(String packageName, String category, boolean enabled) {
            this.packageName = packageName;
            this.category = category;
            this.mEnabled = enabled;
        }

        public OverlayInfo(android.content.om.OverlayInfo info) {
            this.mEnabled = info.isEnabled();
            this.category = info.category;
            this.packageName = info.packageName;
        }

        public boolean isEnabled() {
            return this.mEnabled;
        }
    }

    public OverlayManagerWrapper(IOverlayManager overlayManager) {
        this.mOverlayManager = overlayManager;
    }

    public OverlayManagerWrapper() {
        this(Stub.asInterface(ServiceManager.getService("overlay")));
    }

    public List<OverlayInfo> getOverlayInfosForTarget(String overlay, int userId) {
        if (this.mOverlayManager == null) {
            return new ArrayList();
        }
        try {
            List<android.content.om.OverlayInfo> infos = this.mOverlayManager.getOverlayInfosForTarget(overlay, userId);
            ArrayList<OverlayInfo> result = new ArrayList(infos.size());
            for (int i = 0; i < infos.size(); i++) {
                result.add(new OverlayInfo((android.content.om.OverlayInfo) infos.get(i)));
            }
            return result;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean setEnabled(String overlay, boolean enabled, int userId) {
        if (this.mOverlayManager == null) {
            return false;
        }
        try {
            return this.mOverlayManager.setEnabled(overlay, enabled, userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean setEnabledExclusiveInCategory(String overlay, int userId) {
        if (this.mOverlayManager == null) {
            return false;
        }
        try {
            return this.mOverlayManager.setEnabledExclusiveInCategory(overlay, userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
}
