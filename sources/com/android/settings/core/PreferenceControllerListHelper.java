package com.android.settings.core;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import com.android.settingslib.core.AbstractPreferenceController;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.xmlpull.v1.XmlPullParserException;

public class PreferenceControllerListHelper {
    private static final String TAG = "PrefCtrlListHelper";

    public static List<BasePreferenceController> getPreferenceControllersFromXml(Context context, int xmlResId) {
        List<BasePreferenceController> controllers = new ArrayList();
        try {
            for (Bundle metadata : PreferenceXmlParserUtils.extractMetadata(context, xmlResId, 10)) {
                String controllerName = metadata.getString("controller");
                if (!TextUtils.isEmpty(controllerName)) {
                    IllegalStateException e;
                    try {
                        e = BasePreferenceController.createInstance(context, controllerName);
                    } catch (IllegalStateException e2) {
                        String str = TAG;
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("Could not find Context-only controller for pref: ");
                        stringBuilder.append(controllerName);
                        Log.d(str, stringBuilder.toString());
                        str = metadata.getString("key");
                        if (TextUtils.isEmpty(str)) {
                            String str2 = TAG;
                            StringBuilder stringBuilder2 = new StringBuilder();
                            stringBuilder2.append("Controller requires key but it's not defined in xml: ");
                            stringBuilder2.append(controllerName);
                            Log.w(str2, stringBuilder2.toString());
                        } else {
                            try {
                                e = BasePreferenceController.createInstance(context, controllerName, str);
                            } catch (IllegalStateException e3) {
                                String str3 = TAG;
                                StringBuilder stringBuilder3 = new StringBuilder();
                                stringBuilder3.append("Cannot instantiate controller from reflection: ");
                                stringBuilder3.append(controllerName);
                                Log.w(str3, stringBuilder3.toString());
                            }
                        }
                    }
                    controllers.add(e);
                }
            }
            return controllers;
        } catch (IOException | XmlPullParserException e4) {
            Log.e(TAG, "Failed to parse preference xml for getting controllers", e4);
            return controllers;
        }
    }

    public static List<BasePreferenceController> filterControllers(List<BasePreferenceController> input, List<AbstractPreferenceController> filter) {
        if (input == null || filter == null) {
            return input;
        }
        String key;
        Set<String> keys = new TreeSet();
        List<BasePreferenceController> filteredList = new ArrayList();
        for (AbstractPreferenceController controller : filter) {
            key = controller.getPreferenceKey();
            if (key != null) {
                keys.add(key);
            }
        }
        for (BasePreferenceController controller2 : input) {
            if (keys.contains(controller2.getPreferenceKey())) {
                key = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(controller2.getPreferenceKey());
                stringBuilder.append(" already has a controller");
                Log.w(key, stringBuilder.toString());
            } else {
                filteredList.add(controller2);
            }
        }
        return filteredList;
    }
}
