package com.oneplus.custom.utils;

import android.os.Build.VERSION;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import vendor.oneplus.hardware.param.V1_0.IOneplusParam;
import vendor.oneplus.hardware.param.V1_0.IOneplusParam.getParamBufCallback;

public class ParamReader {
    private static final int PARAM_BACKCOVER_COLOR = 2;
    private static final int PARAM_CUST_FLAG = 4;
    private static final int PARAM_GET_SECURE_WP_KEY = 26;
    private static final String TAG = "ParamReader";
    private static final String custom_back_cover_fn = "/sys/module/param_read_write/parameters/backcover_color";
    private static final String custom_fn = "/sys/module/param_read_write/parameters/cust_flag";
    private static boolean mParamReadRet = false;
    private static ArrayList<Byte> mParamReadbyte = new ArrayList();

    private static IOneplusParam getOneplusParamService() {
        try {
            return IOneplusParam.getService();
        } catch (RemoteException e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Exception getting OnePlus param service: ");
            stringBuilder.append(e);
            MyLog.e(str, stringBuilder.toString());
            return null;
        }
    }

    public static int getCustFlagVal() {
        int result = 0;
        String str;
        StringBuilder stringBuilder;
        if (VERSION.SDK_INT <= 27) {
            BufferedReader br = null;
            File themeStateFile = new File(custom_fn);
            if (themeStateFile.exists()) {
                try {
                    br = new BufferedReader(new FileReader(themeStateFile));
                    while (true) {
                        String readLine = br.readLine();
                        String line = readLine;
                        if (readLine != null) {
                            int i = -1;
                            switch (line.hashCode()) {
                                case 49:
                                    if (line.equals("1")) {
                                        i = 0;
                                        break;
                                    }
                                    break;
                                case 50:
                                    if (line.equals("2")) {
                                        i = 1;
                                        break;
                                    }
                                    break;
                                case 51:
                                    if (line.equals("3")) {
                                        i = 2;
                                        break;
                                    }
                                    break;
                                default:
                                    break;
                            }
                            switch (i) {
                                case 0:
                                    result = 1;
                                    break;
                                case 1:
                                    result = 2;
                                    break;
                                case 2:
                                    result = 3;
                                    break;
                                default:
                                    break;
                            }
                        }
                        try {
                            br.close();
                        } catch (Exception e) {
                            MyLog.e(TAG, e.getMessage());
                        }
                        str = TAG;
                        stringBuilder = new StringBuilder();
                        stringBuilder.append("getCustFlagVal ~P result = ");
                        stringBuilder.append(result);
                        MyLog.v(str, stringBuilder.toString());
                        return result;
                    }
                } catch (Exception e2) {
                    MyLog.e(TAG, e2.getMessage());
                    if (br != null) {
                        try {
                            br.close();
                        } catch (Exception e22) {
                            MyLog.e(TAG, e22.getMessage());
                        }
                    }
                    str = TAG;
                    stringBuilder = new StringBuilder();
                } catch (Throwable th) {
                    if (br != null) {
                        try {
                            br.close();
                        } catch (Exception e222) {
                            MyLog.e(TAG, e222.getMessage());
                        }
                    }
                    str = TAG;
                    stringBuilder = new StringBuilder();
                }
            } else {
                MyLog.w(TAG, "custom_fn not existed");
                return 0;
            }
        }
        try {
            Object oRemoteService = Class.forName("android.os.ServiceManager").getMethod("getService", new Class[]{String.class}).invoke(null, new Object[]{"ParamService"});
            Object oIParamService = Class.forName("com.oem.os.IParamService$Stub").getMethod("asInterface", new Class[]{IBinder.class}).invoke(null, new Object[]{oRemoteService});
            result = ((Integer) oIParamService.getClass().getMethod("getParamIntSYNC", new Class[]{Integer.TYPE}).invoke(oIParamService, new Object[]{Integer.valueOf(4)})).intValue();
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("getCustFlagVal P~ result = ");
            stringBuilder.append(result);
            MyLog.v(str, stringBuilder.toString());
        } catch (Exception e3) {
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("getCustFlagVal throws exception: ");
            stringBuilder.append(e3);
            MyLog.e(str, stringBuilder.toString());
        }
        return result;
    }

    public static String getBackCoverColorVal() {
        String result = "00000000";
        Object oIParamService = null;
        if (VERSION.SDK_INT <= 27) {
            File custFile = new File(custom_back_cover_fn);
            if (custFile.exists()) {
                String readLine;
                StringBuilder stringBuilder;
                try {
                    BufferedReader br = new BufferedReader(new FileReader(custFile));
                    while (true) {
                        readLine = br.readLine();
                        String line = readLine;
                        if (readLine != null) {
                            result = line.toLowerCase();
                        } else {
                            try {
                                break;
                            } catch (Exception e) {
                                Log.e(TAG, e.getMessage());
                            }
                        }
                    }
                    br.close();
                    readLine = TAG;
                    stringBuilder = new StringBuilder();
                } catch (Exception e2) {
                    MyLog.e(TAG, e2.getMessage());
                    if (oIParamService != null) {
                        try {
                            oIParamService.close();
                        } catch (Exception e22) {
                            Log.e(TAG, e22.getMessage());
                        }
                    }
                    readLine = TAG;
                    stringBuilder = new StringBuilder();
                } catch (Throwable th) {
                    if (oIParamService != null) {
                        try {
                            oIParamService.close();
                        } catch (Exception e222) {
                            Log.e(TAG, e222.getMessage());
                        }
                    }
                    readLine = TAG;
                    stringBuilder = new StringBuilder();
                }
                stringBuilder.append("getBackCoverColorVal ~P result = ");
                stringBuilder.append(result);
                MyLog.v(readLine, stringBuilder.toString());
                return result;
            }
            MyLog.w(TAG, "custom_back_cover_fn not existed");
            return result;
        }
        try {
            Object oRemoteService = Class.forName("android.os.ServiceManager").getMethod("getService", new Class[]{String.class}).invoke(oIParamService, new Object[]{"ParamService"});
            oIParamService = Class.forName("com.oem.os.IParamService$Stub").getMethod("asInterface", new Class[]{IBinder.class}).invoke(oIParamService, new Object[]{oRemoteService});
            result = Integer.toHexString(((Integer) oIParamService.getClass().getMethod("getParamIntSYNC", new Class[]{Integer.TYPE}).invoke(oIParamService, new Object[]{Integer.valueOf(2)})).intValue());
            String str = TAG;
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("getBackCoverColorVal P~ result = ");
            stringBuilder2.append(result);
            MyLog.v(str, stringBuilder2.toString());
        } catch (Exception e3) {
            String str2 = TAG;
            StringBuilder stringBuilder3 = new StringBuilder();
            stringBuilder3.append("getBackCoverColorVal throws exception: ");
            stringBuilder3.append(e3);
            MyLog.e(str2, stringBuilder3.toString());
        }
        return result;
    }

    public static byte[] getSecureWPKey() {
        final CountDownLatch cdl = new CountDownLatch(1);
        try {
            getParamBufCallback cbk = new getParamBufCallback() {
                public void onValues(boolean result, ArrayList<Byte> param) {
                    ParamReader.mParamReadRet = result;
                    ParamReader.mParamReadbyte = param;
                    cdl.countDown();
                }
            };
            IOneplusParam op = getOneplusParamService();
            if (op == null) {
                MyLog.e(TAG, "Can't get OneplusParamService");
                return null;
            }
            op.getParamBuf(26, cbk);
            cdl.await(100, TimeUnit.MILLISECONDS);
            byte[] myResult = new byte[mParamReadbyte.size()];
            for (int i = 0; i < mParamReadbyte.size(); i++) {
                if (((Byte) mParamReadbyte.get(i)).byteValue() == (byte) 0) {
                    break;
                }
                myResult[i] = ((Byte) mParamReadbyte.get(i)).byteValue();
            }
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("get WP key result = ");
            stringBuilder.append(myResult);
            MyLog.v(str, stringBuilder.toString());
            return myResult;
        } catch (Exception e) {
            String str2 = TAG;
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("getParamBuf throws exception: ");
            stringBuilder2.append(e);
            MyLog.e(str2, stringBuilder2.toString());
            return null;
        }
    }
}
