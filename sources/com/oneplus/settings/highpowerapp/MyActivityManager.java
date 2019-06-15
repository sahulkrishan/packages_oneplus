package com.oneplus.settings.highpowerapp;

import android.content.Context;
import android.util.Log;
import android.util.OpFeatures;
import com.oneplus.settings.backgroundoptimize.Logutil;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MyActivityManager implements IMyActivityManager {
    private static final String TAG = MyActivityManager.class.getSimpleName();
    private static volatile MyActivityManager instance;
    private Method getBgPowerHungryList;
    Field isLocked = null;
    Field isStopped = null;
    Field packageName = null;
    Field powerLevel = null;
    private Method stopBgPowerHungryApp;
    Field timeStamp = null;
    Field uId = null;

    private MyActivityManager(Context ctx) {
        try {
            Class<?> c = Class.forName("com.oneplus.highpower.HighPowerDetectManager");
            this.getBgPowerHungryList = c.getDeclaredMethod("getBgPowerHungryList", new Class[0]);
            this.stopBgPowerHungryApp = c.getDeclaredMethod("stopBgPowerHungryApp", new Class[]{String.class, Integer.TYPE});
        } catch (Exception e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("MyActivityManager Exception=");
            stringBuilder.append(e);
            Log.e(str, stringBuilder.toString());
            e.printStackTrace();
        }
    }

    public static MyActivityManager get(Context ctx) {
        if (instance == null) {
            synchronized (MyActivityManager.class) {
                if (instance == null) {
                    instance = new MyActivityManager(ctx);
                }
            }
        }
        return instance;
    }

    public boolean getBgMonitorMode() {
        return false;
    }

    public void setBgMonitorMode(boolean auto) {
    }

    public List<HighPowerApp> getBgPowerHungryList() {
        return convert((List) invoke(this.getBgPowerHungryList, new Object[0]));
    }

    public void stopBgPowerHungryApp(String pkg, int powerLevel) {
        invoke(this.stopBgPowerHungryApp, pkg, Integer.valueOf(powerLevel));
    }

    private List<HighPowerApp> convert(List<?> l) {
        MyActivityManager myActivityManager = this;
        ArrayList list = new ArrayList();
        Iterator iterator = l.iterator();
        while (true) {
            Iterator iterator2 = iterator;
            if (!iterator2.hasNext()) {
                return list;
            }
            Object o = iterator2.next();
            if (myActivityManager.packageName == null) {
                try {
                    myActivityManager.packageName = o.getClass().getField("pkgName");
                    myActivityManager.packageName.setAccessible(true);
                    myActivityManager.powerLevel = o.getClass().getField("powerLevel");
                    myActivityManager.powerLevel.setAccessible(true);
                    myActivityManager.isLocked = o.getClass().getField("isLocked");
                    myActivityManager.isLocked.setAccessible(true);
                    myActivityManager.isStopped = o.getClass().getField("isStopped");
                    myActivityManager.isStopped.setAccessible(true);
                    myActivityManager.timeStamp = o.getClass().getField("timeStamp");
                    myActivityManager.timeStamp.setAccessible(true);
                    if (OpFeatures.isSupport(new int[]{28})) {
                        myActivityManager.uId = o.getClass().getField("uid");
                        myActivityManager.uId.setAccessible(true);
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
            try {
                String pn = (String) myActivityManager.packageName.get(o);
                int level = ((Integer) myActivityManager.powerLevel.get(o)).intValue();
                boolean lock = ((Boolean) myActivityManager.isLocked.get(o)).booleanValue();
                boolean stop = ((Boolean) myActivityManager.isStopped.get(o)).booleanValue();
                long time = ((Long) myActivityManager.timeStamp.get(o)).longValue();
                boolean stop2;
                int level2;
                if (OpFeatures.isSupport(new int[]{28})) {
                    int uid = ((Integer) myActivityManager.uId.get(o)).intValue();
                    stop2 = stop;
                    level2 = level;
                    list.add(new HighPowerApp(pn, uid, level, lock, stop2, time));
                    String str = TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("convert pn: ");
                    stringBuilder.append(pn);
                    stringBuilder.append(", uid =");
                    stringBuilder.append(uid);
                    stringBuilder.append(", level=");
                    stringBuilder.append(level2);
                    stringBuilder.append(", lock=");
                    stringBuilder.append(level2);
                    stringBuilder.append(", stop=");
                    stringBuilder.append(stop2);
                    Logutil.loge(str, stringBuilder.toString());
                } else {
                    stop2 = stop;
                    level2 = level;
                    list.add(new HighPowerApp(pn, level2, lock, stop2, time));
                    String str2 = TAG;
                    StringBuilder stringBuilder2 = new StringBuilder();
                    stringBuilder2.append("convert pn: ");
                    stringBuilder2.append(pn);
                    stringBuilder2.append(", level=");
                    stringBuilder2.append(level2);
                    stringBuilder2.append(", lock=");
                    stringBuilder2.append(level2);
                    stringBuilder2.append(", stop=");
                    stringBuilder2.append(stop2);
                    Logutil.loge(str2, stringBuilder2.toString());
                }
            } catch (Exception e12) {
                e12.printStackTrace();
            }
            iterator = iterator2;
            myActivityManager = this;
        }
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
