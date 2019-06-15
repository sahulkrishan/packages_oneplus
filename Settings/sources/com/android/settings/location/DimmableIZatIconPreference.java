package com.android.settings.location;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.preference.PreferenceViewHolder;
import com.android.settings.widget.AppPreference;
import com.android.settings.widget.RestrictedAppPreference;
import dalvik.system.DexClassLoader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class DimmableIZatIconPreference {
    private static final int ICON_ALPHA_DISABLED = 102;
    private static final int ICON_ALPHA_ENABLED = 255;
    private static final String TAG = "DimmableIZatIconPreference";
    private static Method mGetConsentMethod;
    private static Method mGetXtProxyMethod;
    private static String mIzatPackage;
    private static DexClassLoader mLoader;
    private static Class mNotifierClz;
    private static Method mShowIzatMethod;
    private static Class mXtProxyClz;

    private static class IZatAppPreference extends AppPreference {
        private boolean mChecked;

        private IZatAppPreference(Context context) {
            super(context);
            Object notifier = Proxy.newProxyInstance(DimmableIZatIconPreference.mLoader, new Class[]{DimmableIZatIconPreference.mNotifierClz}, new InvocationHandler() {
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    if (method.getName().equals("userConsentNotify")) {
                        boolean z = false;
                        if (args[0] != null && (args[0] instanceof Boolean)) {
                            boolean consent = ((Boolean) args[0]).booleanValue();
                            if (IZatAppPreference.this.mChecked != consent) {
                                IZatAppPreference.this.mChecked = consent;
                                IZatAppPreference iZatAppPreference = IZatAppPreference.this;
                                if (!(IZatAppPreference.this.isEnabled() && IZatAppPreference.this.mChecked)) {
                                    z = true;
                                }
                                DimmableIZatIconPreference.dimIcon(iZatAppPreference, z);
                            }
                        }
                    }
                    return null;
                }
            });
            try {
                this.mChecked = ((Boolean) DimmableIZatIconPreference.mGetConsentMethod.invoke(DimmableIZatIconPreference.mGetXtProxyMethod.invoke(null, new Object[]{context, notifier}), new Object[0])).booleanValue();
            } catch (ExceptionInInitializerError | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        public void onBindViewHolder(PreferenceViewHolder view) {
            super.onBindViewHolder(view);
            boolean z = (isEnabled() && this.mChecked) ? false : true;
            DimmableIZatIconPreference.dimIcon(this, z);
        }
    }

    private static class IZatRestrictedAppPreference extends RestrictedAppPreference {
        private boolean mChecked;

        private IZatRestrictedAppPreference(Context context, String userRestriction) {
            super(context, userRestriction);
            Object notifier = Proxy.newProxyInstance(DimmableIZatIconPreference.mLoader, new Class[]{DimmableIZatIconPreference.mNotifierClz}, new InvocationHandler() {
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    if (method.getName().equals("userConsentNotify")) {
                        boolean z = false;
                        if (args[0] != null && (args[0] instanceof Boolean)) {
                            boolean consent = ((Boolean) args[0]).booleanValue();
                            if (IZatRestrictedAppPreference.this.mChecked != consent) {
                                IZatRestrictedAppPreference.this.mChecked = consent;
                                IZatRestrictedAppPreference iZatRestrictedAppPreference = IZatRestrictedAppPreference.this;
                                if (!(IZatRestrictedAppPreference.this.isEnabled() && IZatRestrictedAppPreference.this.mChecked)) {
                                    z = true;
                                }
                                DimmableIZatIconPreference.dimIcon(iZatRestrictedAppPreference, z);
                            }
                        }
                    }
                    return null;
                }
            });
            try {
                this.mChecked = ((Boolean) DimmableIZatIconPreference.mGetConsentMethod.invoke(DimmableIZatIconPreference.mGetXtProxyMethod.invoke(null, new Object[]{context, notifier}), new Object[0])).booleanValue();
            } catch (ExceptionInInitializerError | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        public void onBindViewHolder(PreferenceViewHolder view) {
            super.onBindViewHolder(view);
            boolean z = (isEnabled() && this.mChecked) ? false : true;
            DimmableIZatIconPreference.dimIcon(this, z);
        }
    }

    private static void load(Context context) {
        if (mLoader == null) {
            try {
                if (mXtProxyClz == null || mNotifierClz == null) {
                    mLoader = new DexClassLoader("/system/framework/izat.xt.srv.jar", context.getFilesDir().getAbsolutePath(), null, ClassLoader.getSystemClassLoader());
                    mXtProxyClz = Class.forName("com.qti.izat.XTProxy", true, mLoader);
                    mNotifierClz = Class.forName("com.qti.izat.XTProxy$Notifier", true, mLoader);
                    mIzatPackage = (String) mXtProxyClz.getField("IZAT_XT_PACKAGE").get(null);
                    mGetXtProxyMethod = mXtProxyClz.getMethod("getXTProxy", new Class[]{Context.class, mNotifierClz});
                    mGetConsentMethod = mXtProxyClz.getMethod("getUserConsent", new Class[0]);
                    mShowIzatMethod = mXtProxyClz.getMethod("showIzat", new Class[]{Context.class, String.class});
                }
            } catch (ClassNotFoundException | IllegalAccessException | LinkageError | NoSuchFieldException | NoSuchMethodException | NullPointerException | SecurityException e) {
                mXtProxyClz = null;
                mNotifierClz = null;
                mIzatPackage = null;
                mGetXtProxyMethod = null;
                mGetConsentMethod = null;
                mShowIzatMethod = null;
                e.printStackTrace();
            }
        }
    }

    static boolean showIzat(Context context, String packageName) {
        load(context);
        boolean show = true;
        try {
            if (mShowIzatMethod == null) {
                return show;
            }
            return ((Boolean) mShowIzatMethod.invoke(null, new Object[]{context, packageName})).booleanValue();
        } catch (ExceptionInInitializerError | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
            return show;
        }
    }

    private static boolean isIzatPackage(Context context, InjectedSetting info) {
        return mIzatPackage != null && mIzatPackage.equals(info.packageName);
    }

    private static void dimIcon(AppPreference pref, boolean dimmed) {
        Drawable icon = pref.getIcon();
        if (icon != null) {
            icon.mutate().setAlpha(dimmed ? 102 : 255);
            pref.setIcon(icon);
        }
    }

    static AppPreference getAppPreference(Context context, InjectedSetting info) {
        if (isIzatPackage(context, info)) {
            return new IZatAppPreference(context);
        }
        return new AppPreference(context);
    }

    static RestrictedAppPreference getRestrictedAppPreference(Context context, InjectedSetting info) {
        if (isIzatPackage(context, info)) {
            return new IZatRestrictedAppPreference(context, info.userRestriction);
        }
        return new RestrictedAppPreference(context, info.userRestriction);
    }
}
