package com.android.settings.search;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.provider.SearchIndexableResource;
import android.support.annotation.CallSuper;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.core.PreferenceControllerListHelper;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.core.PreferenceXmlParserUtils;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settingslib.core.AbstractPreferenceController;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParserException;

public class BaseSearchIndexProvider implements SearchIndexProvider {
    private static final String TAG = "BaseSearchIndex";

    public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
        return null;
    }

    public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
        return null;
    }

    @CallSuper
    public List<String> getNonIndexableKeys(Context context) {
        if (!isPageSearchEnabled(context)) {
            return getNonIndexableKeysFromXml(context);
        }
        List<AbstractPreferenceController> controllers = getPreferenceControllers(context);
        if (controllers == null || controllers.isEmpty()) {
            return new ArrayList();
        }
        List<String> nonIndexableKeys = new ArrayList();
        for (AbstractPreferenceController controller : controllers) {
            if (controller instanceof PreferenceControllerMixin) {
                ((PreferenceControllerMixin) controller).updateNonIndexableKeys(nonIndexableKeys);
            } else if (controller instanceof BasePreferenceController) {
                ((BasePreferenceController) controller).updateNonIndexableKeys(nonIndexableKeys);
            } else {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(controller.getClass().getName());
                stringBuilder.append(" must implement ");
                stringBuilder.append(PreferenceControllerMixin.class.getName());
                stringBuilder.append(" treating the key non-indexable");
                Log.e(str, stringBuilder.toString());
                nonIndexableKeys.add(controller.getPreferenceKey());
            }
        }
        return nonIndexableKeys;
    }

    public List<AbstractPreferenceController> getPreferenceControllers(Context context) {
        List<AbstractPreferenceController> controllersFromCode = createPreferenceControllers(context);
        List<SearchIndexableResource> res = getXmlResourcesToIndex(context, true);
        if (res == null || res.isEmpty()) {
            return controllersFromCode;
        }
        List<BasePreferenceController> controllersFromXml = new ArrayList();
        for (SearchIndexableResource sir : res) {
            controllersFromXml.addAll(PreferenceControllerListHelper.getPreferenceControllersFromXml(context, sir.xmlResId));
        }
        controllersFromXml = PreferenceControllerListHelper.filterControllers(controllersFromXml, controllersFromCode);
        List<AbstractPreferenceController> allControllers = new ArrayList();
        if (controllersFromCode != null) {
            allControllers.addAll(controllersFromCode);
        }
        allControllers.addAll(controllersFromXml);
        return allControllers;
    }

    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return null;
    }

    /* Access modifiers changed, original: protected */
    public boolean isPageSearchEnabled(Context context) {
        return true;
    }

    private List<String> getNonIndexableKeysFromXml(Context context) {
        List<SearchIndexableResource> resources = getXmlResourcesToIndex(context, true);
        if (resources == null || resources.isEmpty()) {
            return new ArrayList();
        }
        List<String> nonIndexableKeys = new ArrayList();
        for (SearchIndexableResource res : resources) {
            nonIndexableKeys.addAll(getNonIndexableKeysFromXml(context, res.xmlResId));
        }
        return nonIndexableKeys;
    }

    @VisibleForTesting(otherwise = 4)
    public List<String> getNonIndexableKeysFromXml(Context context, int xmlResId) {
        List<String> nonIndexableKeys = new ArrayList();
        XmlResourceParser parser = context.getResources().getXml(xmlResId);
        AttributeSet attrs = Xml.asAttributeSet(parser);
        while (parser.next() != 1) {
            try {
                String key = PreferenceXmlParserUtils.getDataKey(context, attrs);
                if (!TextUtils.isEmpty(key)) {
                    nonIndexableKeys.add(key);
                }
            } catch (IOException | XmlPullParserException e) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Error parsing non-indexable from xml ");
                stringBuilder.append(xmlResId);
                Log.w(str, stringBuilder.toString());
            }
        }
        return nonIndexableKeys;
    }
}
