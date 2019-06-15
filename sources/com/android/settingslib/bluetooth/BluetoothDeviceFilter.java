package com.android.settingslib.bluetooth;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothUuid;
import android.os.ParcelUuid;
import android.util.Log;

public final class BluetoothDeviceFilter {
    public static final Filter ALL_FILTER = new AllFilter();
    public static final Filter BONDED_DEVICE_FILTER = new BondedDeviceFilter();
    private static final Filter[] FILTERS = new Filter[]{ALL_FILTER, new AudioFilter(), new TransferFilter(), new PanuFilter(), new NapFilter()};
    private static final String TAG = "BluetoothDeviceFilter";
    public static final Filter UNBONDED_DEVICE_FILTER = new UnbondedDeviceFilter();

    public interface Filter {
        boolean matches(BluetoothDevice bluetoothDevice);
    }

    private static final class AllFilter implements Filter {
        private AllFilter() {
        }

        public boolean matches(BluetoothDevice device) {
            return true;
        }
    }

    private static final class BondedDeviceFilter implements Filter {
        private BondedDeviceFilter() {
        }

        public boolean matches(BluetoothDevice device) {
            return device.getBondState() == 12;
        }
    }

    private static abstract class ClassUuidFilter implements Filter {
        public abstract boolean matches(ParcelUuid[] parcelUuidArr, BluetoothClass bluetoothClass);

        private ClassUuidFilter() {
        }

        public boolean matches(BluetoothDevice device) {
            return matches(device.getUuids(), device.getBluetoothClass());
        }
    }

    private static final class UnbondedDeviceFilter implements Filter {
        private UnbondedDeviceFilter() {
        }

        public boolean matches(BluetoothDevice device) {
            String str = BluetoothDeviceFilter.TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("UnbondedDeviceFilter : ");
            stringBuilder.append(device.getBondState() != 12);
            Log.d(str, stringBuilder.toString());
            if (device.getBondState() != 12) {
                return true;
            }
            return false;
        }
    }

    private static final class AudioFilter extends ClassUuidFilter {
        private AudioFilter() {
            super();
        }

        /* Access modifiers changed, original: 0000 */
        /* JADX WARNING: Removed duplicated region for block: B:14:0x0025 A:{RETURN} */
        public boolean matches(android.os.ParcelUuid[] r4, android.bluetooth.BluetoothClass r5) {
            /*
            r3 = this;
            r0 = 0;
            r1 = 1;
            if (r4 == 0) goto L_0x0016;
        L_0x0004:
            r2 = com.android.settingslib.bluetooth.A2dpProfile.SINK_UUIDS;
            r2 = android.bluetooth.BluetoothUuid.containsAnyUuid(r4, r2);
            if (r2 == 0) goto L_0x000d;
        L_0x000c:
            return r1;
        L_0x000d:
            r2 = com.android.settingslib.bluetooth.HeadsetProfile.UUIDS;
            r2 = android.bluetooth.BluetoothUuid.containsAnyUuid(r4, r2);
            if (r2 == 0) goto L_0x0025;
        L_0x0015:
            return r1;
        L_0x0016:
            if (r5 == 0) goto L_0x0025;
        L_0x0018:
            r2 = r5.doesClassMatch(r1);
            if (r2 != 0) goto L_0x0024;
        L_0x001e:
            r2 = r5.doesClassMatch(r0);
            if (r2 == 0) goto L_0x0025;
        L_0x0024:
            return r1;
        L_0x0025:
            return r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.settingslib.bluetooth.BluetoothDeviceFilter$AudioFilter.matches(android.os.ParcelUuid[], android.bluetooth.BluetoothClass):boolean");
        }
    }

    private static final class NapFilter extends ClassUuidFilter {
        private NapFilter() {
            super();
        }

        /* Access modifiers changed, original: 0000 */
        public boolean matches(ParcelUuid[] uuids, BluetoothClass btClass) {
            boolean z = true;
            if (uuids != null && BluetoothUuid.isUuidPresent(uuids, BluetoothUuid.NAP)) {
                return true;
            }
            if (btClass == null || !btClass.doesClassMatch(5)) {
                z = false;
            }
            return z;
        }
    }

    private static final class PanuFilter extends ClassUuidFilter {
        private PanuFilter() {
            super();
        }

        /* Access modifiers changed, original: 0000 */
        public boolean matches(ParcelUuid[] uuids, BluetoothClass btClass) {
            boolean z = true;
            if (uuids != null && BluetoothUuid.isUuidPresent(uuids, BluetoothUuid.PANU)) {
                return true;
            }
            if (btClass == null || !btClass.doesClassMatch(4)) {
                z = false;
            }
            return z;
        }
    }

    private static final class TransferFilter extends ClassUuidFilter {
        private TransferFilter() {
            super();
        }

        /* Access modifiers changed, original: 0000 */
        public boolean matches(ParcelUuid[] uuids, BluetoothClass btClass) {
            boolean z = true;
            if (uuids != null && BluetoothUuid.isUuidPresent(uuids, BluetoothUuid.ObexObjectPush)) {
                return true;
            }
            if (btClass == null || !btClass.doesClassMatch(2)) {
                z = false;
            }
            return z;
        }
    }

    private BluetoothDeviceFilter() {
    }

    public static Filter getFilter(int filterType) {
        if (filterType >= 0 && filterType < FILTERS.length) {
            return FILTERS[filterType];
        }
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Invalid filter type ");
        stringBuilder.append(filterType);
        stringBuilder.append(" for device picker");
        Log.w(str, stringBuilder.toString());
        return ALL_FILTER;
    }
}
