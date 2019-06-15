package com.android.settings.connecteddevice.usb;

import android.content.Context;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbPort;
import android.hardware.usb.UsbPortStatus;
import android.net.ConnectivityManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.annotation.VisibleForTesting;

public class UsbBackend {
    static final int NONPD_ROLE_SWAP_TIMEOUT_MS = 15000;
    static final int PD_ROLE_SWAP_TIMEOUT_MS = 3000;
    private final boolean mFileTransferRestricted;
    private final boolean mFileTransferRestrictedBySystem;
    private final boolean mMidiSupported;
    private UsbPort mPort;
    private UsbPortStatus mPortStatus;
    private final boolean mTetheringRestricted;
    private final boolean mTetheringRestrictedBySystem;
    private final boolean mTetheringSupported;
    private UsbManager mUsbManager;

    public UsbBackend(Context context) {
        this(context, (UserManager) context.getSystemService("user"));
    }

    @VisibleForTesting
    public UsbBackend(Context context, UserManager userManager) {
        this.mUsbManager = (UsbManager) context.getSystemService(UsbManager.class);
        this.mFileTransferRestricted = isUsbFileTransferRestricted(userManager);
        this.mFileTransferRestrictedBySystem = isUsbFileTransferRestrictedBySystem(userManager);
        this.mTetheringRestricted = isUsbTetheringRestricted(userManager);
        this.mTetheringRestrictedBySystem = isUsbTetheringRestrictedBySystem(userManager);
        this.mMidiSupported = context.getPackageManager().hasSystemFeature("android.software.midi");
        this.mTetheringSupported = ((ConnectivityManager) context.getSystemService("connectivity")).isTetheringSupported();
        updatePorts();
    }

    public long getCurrentFunctions() {
        return this.mUsbManager.getCurrentFunctions();
    }

    public void setCurrentFunctions(long functions) {
        this.mUsbManager.setCurrentFunctions(functions);
    }

    public long getDefaultUsbFunctions() {
        return this.mUsbManager.getScreenUnlockedFunctions();
    }

    public void setDefaultUsbFunctions(long functions) {
        this.mUsbManager.setScreenUnlockedFunctions(functions);
    }

    public boolean areFunctionsSupported(long functions) {
        boolean z = false;
        if ((!this.mMidiSupported && (8 & functions) != 0) || (!this.mTetheringSupported && (32 & functions) != 0)) {
            return false;
        }
        if (!(areFunctionDisallowed(functions) || areFunctionsDisallowedBySystem(functions))) {
            z = true;
        }
        return z;
    }

    public int getPowerRole() {
        updatePorts();
        return this.mPortStatus == null ? 0 : this.mPortStatus.getCurrentPowerRole();
    }

    public int getDataRole() {
        updatePorts();
        return this.mPortStatus == null ? 0 : this.mPortStatus.getCurrentDataRole();
    }

    public void setPowerRole(int role) {
        int newDataRole = getDataRole();
        if (!areAllRolesSupported()) {
            switch (role) {
                case 1:
                    newDataRole = 1;
                    break;
                case 2:
                    newDataRole = 2;
                    break;
                default:
                    newDataRole = 0;
                    break;
            }
        }
        if (this.mPort != null) {
            this.mUsbManager.setPortRoles(this.mPort, role, newDataRole);
        }
    }

    public void setDataRole(int role) {
        int newPowerRole = getPowerRole();
        if (!areAllRolesSupported()) {
            switch (role) {
                case 1:
                    newPowerRole = 1;
                    break;
                case 2:
                    newPowerRole = 2;
                    break;
                default:
                    newPowerRole = 0;
                    break;
            }
        }
        if (this.mPort != null) {
            this.mUsbManager.setPortRoles(this.mPort, newPowerRole, role);
        }
    }

    public boolean areAllRolesSupported() {
        return this.mPort != null && this.mPortStatus != null && this.mPortStatus.isRoleCombinationSupported(2, 2) && this.mPortStatus.isRoleCombinationSupported(2, 1) && this.mPortStatus.isRoleCombinationSupported(1, 2) && this.mPortStatus.isRoleCombinationSupported(1, 1);
    }

    public static String usbFunctionsToString(long functions) {
        return Long.toBinaryString(functions);
    }

    public static long usbFunctionsFromString(String functions) {
        return Long.parseLong(functions, 2);
    }

    public static String dataRoleToString(int role) {
        return Integer.toString(role);
    }

    public static int dataRoleFromString(String role) {
        return Integer.parseInt(role);
    }

    private static boolean isUsbFileTransferRestricted(UserManager userManager) {
        return userManager.hasUserRestriction("no_usb_file_transfer");
    }

    private static boolean isUsbTetheringRestricted(UserManager userManager) {
        return userManager.hasUserRestriction("no_config_tethering");
    }

    private static boolean isUsbFileTransferRestrictedBySystem(UserManager userManager) {
        return userManager.hasBaseUserRestriction("no_usb_file_transfer", UserHandle.of(UserHandle.myUserId()));
    }

    private static boolean isUsbTetheringRestrictedBySystem(UserManager userManager) {
        return userManager.hasBaseUserRestriction("no_config_tethering", UserHandle.of(UserHandle.myUserId()));
    }

    private boolean areFunctionDisallowed(long functions) {
        return (this.mFileTransferRestricted && !((4 & functions) == 0 && (16 & functions) == 0)) || (this.mTetheringRestricted && (32 & functions) != 0);
    }

    private boolean areFunctionsDisallowedBySystem(long functions) {
        return (this.mFileTransferRestrictedBySystem && !((4 & functions) == 0 && (16 & functions) == 0)) || (this.mTetheringRestrictedBySystem && (32 & functions) != 0);
    }

    private void updatePorts() {
        this.mPort = null;
        this.mPortStatus = null;
        UsbPort[] ports = this.mUsbManager.getPorts();
        if (ports != null) {
            int N = ports.length;
            for (int i = 0; i < N; i++) {
                UsbPortStatus status = this.mUsbManager.getPortStatus(ports[i]);
                if (status.isConnected()) {
                    this.mPort = ports[i];
                    this.mPortStatus = status;
                    break;
                }
            }
        }
    }
}
