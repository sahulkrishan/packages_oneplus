package com.oneplus.settings.backgroundoptimize;

import android.os.RemoteException;
import java.util.List;

public interface IBgOActivityManager {
    public static final int BgO_PKG_BLACKLIST = 0;
    public static final int BgO_PKG_WHITELIST = 1;

    List<AppControlMode> getAllAppControlModes(int i) throws RemoteException;

    int getAppControlMode(String str, int i) throws RemoteException;

    int getAppControlState(int i) throws RemoteException;

    int setAppControlMode(String str, int i, int i2) throws RemoteException;

    int setAppControlState(int i, int i2) throws RemoteException;
}
