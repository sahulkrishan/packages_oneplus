package com.android.settings;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import com.android.settings.ActivityPicker.PickAdapter.Item;
import com.android.settings.AppWidgetLoader.ItemConstructor;
import java.util.List;

public class AppWidgetPickActivity extends ActivityPicker implements ItemConstructor<Item> {
    static final boolean LOGD = false;
    private static final String TAG = "AppWidgetPickActivity";
    private int mAppWidgetId;
    private AppWidgetLoader<Item> mAppWidgetLoader;
    private AppWidgetManager mAppWidgetManager;
    List<Item> mItems;
    private PackageManager mPackageManager;

    public void onCreate(Bundle icicle) {
        this.mPackageManager = getPackageManager();
        this.mAppWidgetManager = AppWidgetManager.getInstance(this);
        this.mAppWidgetLoader = new AppWidgetLoader(this, this.mAppWidgetManager, this);
        super.onCreate(icicle);
        setResultData(0, null);
        Intent intent = getIntent();
        if (intent.hasExtra("appWidgetId")) {
            this.mAppWidgetId = intent.getIntExtra("appWidgetId", 0);
        } else {
            finish();
        }
    }

    /* Access modifiers changed, original: protected */
    public List<Item> getItems() {
        this.mItems = this.mAppWidgetLoader.getItems(getIntent());
        return this.mItems;
    }

    public Item createItem(Context context, AppWidgetProviderInfo info, Bundle extras) {
        CharSequence label = info.label;
        Drawable icon = null;
        if (info.icon != 0) {
            try {
                int density = context.getResources().getDisplayMetrics().densityDpi;
                int iconDensity = density != 160 ? density != 213 ? density != 240 ? density != 320 ? density != 480 ? (int) ((((float) density) * 0.75f) + 0.5f) : 320 : 240 : 160 : 160 : 120;
                icon = this.mPackageManager.getResourcesForApplication(info.provider.getPackageName()).getDrawableForDensity(info.icon, iconDensity);
            } catch (NameNotFoundException e) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Can't load icon drawable 0x");
                stringBuilder.append(Integer.toHexString(info.icon));
                stringBuilder.append(" for provider: ");
                stringBuilder.append(info.provider);
                Log.w(str, stringBuilder.toString());
            }
            if (icon == null) {
                String str2 = TAG;
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append("Can't load icon drawable 0x");
                stringBuilder2.append(Integer.toHexString(info.icon));
                stringBuilder2.append(" for provider: ");
                stringBuilder2.append(info.provider);
                Log.w(str2, stringBuilder2.toString());
            }
        }
        Item item = new Item(context, label, icon);
        item.packageName = info.provider.getPackageName();
        item.className = info.provider.getClassName();
        item.extras = extras;
        return item;
    }

    public void onClick(DialogInterface dialog, int which) {
        Intent intent = getIntentForPosition(which);
        if (((Item) this.mItems.get(which)).extras != null) {
            setResultData(-1, intent);
        } else {
            int result;
            Bundle options = null;
            try {
                if (intent.getExtras() != null) {
                    options = intent.getExtras().getBundle("appWidgetOptions");
                }
                this.mAppWidgetManager.bindAppWidgetId(this.mAppWidgetId, intent.getComponent(), options);
                result = -1;
            } catch (IllegalArgumentException e) {
                result = 0;
            }
            setResultData(result, null);
        }
        finish();
    }

    /* Access modifiers changed, original: 0000 */
    public void setResultData(int code, Intent intent) {
        Intent result = intent != null ? intent : new Intent();
        result.putExtra("appWidgetId", this.mAppWidgetId);
        setResult(code, result);
    }
}
