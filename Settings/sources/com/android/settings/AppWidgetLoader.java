package com.android.settings;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AppWidgetLoader<Item extends LabelledItem> {
    private static final boolean LOGD = false;
    private static final String TAG = "AppWidgetAdapter";
    private AppWidgetManager mAppWidgetManager;
    private Context mContext;
    ItemConstructor<Item> mItemConstructor;

    public interface ItemConstructor<Item> {
        Item createItem(Context context, AppWidgetProviderInfo appWidgetProviderInfo, Bundle bundle);
    }

    interface LabelledItem {
        CharSequence getLabel();
    }

    public AppWidgetLoader(Context context, AppWidgetManager appWidgetManager, ItemConstructor<Item> itemConstructor) {
        this.mContext = context;
        this.mAppWidgetManager = appWidgetManager;
        this.mItemConstructor = itemConstructor;
    }

    /* Access modifiers changed, original: 0000 */
    public void putCustomAppWidgets(List<Item> items, Intent intent) {
        ArrayList<Bundle> customExtras = null;
        ArrayList<AppWidgetProviderInfo> customInfo = intent.getParcelableArrayListExtra("customInfo");
        if (customInfo == null || customInfo.size() == 0) {
            Log.i(TAG, "EXTRA_CUSTOM_INFO not present.");
        } else {
            int i;
            Parcelable p;
            String str;
            int customInfoSize = customInfo.size();
            int i2 = 0;
            for (i = 0; i < customInfoSize; i++) {
                p = (Parcelable) customInfo.get(i);
                if (p == null || !(p instanceof AppWidgetProviderInfo)) {
                    customInfo = null;
                    str = TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("error using EXTRA_CUSTOM_INFO index=");
                    stringBuilder.append(i);
                    Log.e(str, stringBuilder.toString());
                    break;
                }
            }
            customExtras = intent.getParcelableArrayListExtra("customExtras");
            if (customExtras == null) {
                customInfo = null;
                Log.e(TAG, "EXTRA_CUSTOM_INFO without EXTRA_CUSTOM_EXTRAS");
            } else {
                i = customExtras.size();
                if (customInfoSize != i) {
                    customInfo = null;
                    customExtras = null;
                    str = TAG;
                    StringBuilder stringBuilder2 = new StringBuilder();
                    stringBuilder2.append("list size mismatch: EXTRA_CUSTOM_INFO: ");
                    stringBuilder2.append(customInfoSize);
                    stringBuilder2.append(" EXTRA_CUSTOM_EXTRAS: ");
                    stringBuilder2.append(i);
                    Log.e(str, stringBuilder2.toString());
                } else {
                    while (i2 < i) {
                        p = (Parcelable) customExtras.get(i2);
                        if (p == null || !(p instanceof Bundle)) {
                            customInfo = null;
                            customExtras = null;
                            String str2 = TAG;
                            StringBuilder stringBuilder3 = new StringBuilder();
                            stringBuilder3.append("error using EXTRA_CUSTOM_EXTRAS index=");
                            stringBuilder3.append(i2);
                            Log.e(str2, stringBuilder3.toString());
                            break;
                        }
                        i2++;
                    }
                }
            }
        }
        putAppWidgetItems(customInfo, customExtras, items, 0, true);
    }

    /* Access modifiers changed, original: 0000 */
    public void putAppWidgetItems(List<AppWidgetProviderInfo> appWidgets, List<Bundle> customExtras, List<Item> items, int categoryFilter, boolean ignoreFilter) {
        if (appWidgets != null) {
            int size = appWidgets.size();
            for (int i = 0; i < size; i++) {
                AppWidgetProviderInfo info = (AppWidgetProviderInfo) appWidgets.get(i);
                if (ignoreFilter || (info.widgetCategory & categoryFilter) != 0) {
                    items.add((LabelledItem) this.mItemConstructor.createItem(this.mContext, info, customExtras != null ? (Bundle) customExtras.get(i) : null));
                }
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public List<Item> getItems(Intent intent) {
        boolean sortCustomAppWidgets = intent.getBooleanExtra("customSort", true);
        List<Item> items = new ArrayList();
        putInstalledAppWidgets(items, intent.getIntExtra("categoryFilter", 1));
        if (sortCustomAppWidgets) {
            putCustomAppWidgets(items, intent);
        }
        Collections.sort(items, new Comparator<Item>() {
            Collator mCollator = Collator.getInstance();

            public int compare(Item lhs, Item rhs) {
                return this.mCollator.compare(lhs.getLabel(), rhs.getLabel());
            }
        });
        if (!sortCustomAppWidgets) {
            List<Item> customItems = new ArrayList();
            putCustomAppWidgets(customItems, intent);
            items.addAll(customItems);
        }
        return items;
    }

    /* Access modifiers changed, original: 0000 */
    public void putInstalledAppWidgets(List<Item> items, int categoryFilter) {
        putAppWidgetItems(this.mAppWidgetManager.getInstalledProviders(categoryFilter), null, items, categoryFilter, false);
    }
}
