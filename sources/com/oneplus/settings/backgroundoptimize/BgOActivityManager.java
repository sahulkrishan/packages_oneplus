package com.oneplus.settings.backgroundoptimize;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;
import com.android.settings.inputmethod.UserDictionaryAddWordContents;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BgOActivityManager implements IBgOActivityManager {
    private static final String TAG = BgOActivityManager.class.getSimpleName();
    private static volatile BgOActivityManager instance;
    private Method getAllAppControlModes;
    private Method getAppControlMode;
    private Method getAppControlState;
    private ActivityManager mActivityManager;
    private Method setAppControlMode;
    private Method setAppControlState;

    private BgOActivityManager(Context ctx) {
        try {
            Class<?> c = Class.forName("com.oneplus.appboot.AppControlModeManager");
            this.getAllAppControlModes = c.getDeclaredMethod("getAllAppControlModes", new Class[]{Integer.TYPE});
            this.getAppControlMode = c.getDeclaredMethod("getAppControlMode", new Class[]{String.class, Integer.TYPE});
            this.setAppControlMode = c.getDeclaredMethod("setAppControlMode", new Class[]{String.class, Integer.TYPE, Integer.TYPE});
            this.getAppControlState = c.getDeclaredMethod("getAppControlState", new Class[]{Integer.TYPE});
            this.setAppControlState = c.getDeclaredMethod("setAppControlState", new Class[]{Integer.TYPE, Integer.TYPE});
        } catch (Exception e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("BgOActivityManager Exception=");
            stringBuilder.append(e);
            Log.e(str, stringBuilder.toString());
            e.printStackTrace();
        }
    }

    public static BgOActivityManager getInstance(Context ctx) {
        if (instance == null) {
            synchronized (BgOActivityManager.class) {
                if (instance == null) {
                    instance = new BgOActivityManager(ctx);
                }
            }
        }
        return instance;
    }

    private List<AppControlMode> convert(List<?> l) {
        Field packageName = null;
        Field mode = null;
        Field value = null;
        List<AppControlMode> list = new ArrayList();
        for (Object o : l) {
            if (packageName == null) {
                try {
                    packageName = o.getClass().getField("packageName");
                    packageName.setAccessible(true);
                    mode = o.getClass().getField(UserDictionaryAddWordContents.EXTRA_MODE);
                    mode.setAccessible(true);
                    value = o.getClass().getField("value");
                    value.setAccessible(true);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
            try {
                String pn = (String) packageName.get(o);
                int m = ((Integer) mode.get(o)).intValue();
                int n = ((Integer) value.get(o)).intValue();
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("AppControl # convert: ");
                stringBuilder.append(pn);
                stringBuilder.append(", mode=");
                stringBuilder.append(m);
                stringBuilder.append(", value=");
                stringBuilder.append(n);
                Logutil.loge(str, stringBuilder.toString());
                list.add(new AppControlMode(pn, m, n));
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e2) {
                e2.printStackTrace();
            } catch (Exception e12) {
                e12.printStackTrace();
            }
        }
        return list;
    }

    public Map<String, AppControlMode> getAllAppControlModesMap(int mode) {
        List<AppControlMode> list = getAllAppControlModes(mode);
        Map<String, AppControlMode> map = new HashMap();
        for (AppControlMode app : list) {
            map.put(app.packageName, app);
        }
        return map;
    }

    public List<AppControlMode> getAllAppControlModes(int mode) {
        Object o = invoke(this.getAllAppControlModes, Integer.valueOf(mode));
        List<AppControlMode> ret = null;
        if (o != null) {
            try {
                ret = convert((List) o);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (ret == null) {
            return new ArrayList();
        }
        return ret;
    }

    public int getAppControlMode(String packageName, int mode) {
        Object o = invoke(this.getAppControlMode, packageName, Integer.valueOf(mode));
        int ret = 0;
        if (o != null) {
            ret = ((Integer) o).intValue();
        }
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("AppControl # getAppControlMode packageName: ");
        stringBuilder.append(packageName);
        stringBuilder.append(", mode=");
        stringBuilder.append(mode);
        stringBuilder.append(", value=");
        stringBuilder.append(ret);
        Logutil.loge(str, stringBuilder.toString());
        return ret;
    }

    public int setAppControlMode(String packageName, int mode, int value) {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("AppControl # setAppControlMode packageName: ");
        stringBuilder.append(packageName);
        stringBuilder.append(", mode=");
        stringBuilder.append(mode);
        stringBuilder.append(", value=");
        stringBuilder.append(value);
        Logutil.loge(str, stringBuilder.toString());
        invoke(this.setAppControlMode, packageName, Integer.valueOf(mode), Integer.valueOf(value));
        return 0;
    }

    public int getAppControlState(int mode) {
        Object o = invoke(this.getAppControlState, Integer.valueOf(mode));
        int ret = 0;
        if (o != null) {
            ret = ((Integer) o).intValue();
        }
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("AppControl # getAppControlState mode=");
        stringBuilder.append(mode);
        stringBuilder.append(", value=");
        stringBuilder.append(ret);
        Logutil.loge(str, stringBuilder.toString());
        return ret;
    }

    public int setAppControlState(int mode, int value) {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("AppControl # setAppControlState mode=");
        stringBuilder.append(mode);
        stringBuilder.append(", value=");
        stringBuilder.append(value);
        Logutil.loge(str, stringBuilder.toString());
        invoke(this.setAppControlState, Integer.valueOf(mode), Integer.valueOf(value));
        return 0;
    }

    public Object invoke(Method m, Object... args) {
        if (m == null) {
            return null;
        }
        try {
            return m.invoke(null, args);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalArgumentException e2) {
            e2.printStackTrace();
            return null;
        } catch (InvocationTargetException e3) {
            e3.printStackTrace();
            return null;
        }
    }
}
