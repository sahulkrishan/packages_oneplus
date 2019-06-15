package com.android.settings.shortcut;

import android.app.LauncherActivity;
import android.app.LauncherActivity.ListItem;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutInfo.Builder;
import android.content.pm.ShortcutManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.graphics.drawable.LayerDrawable;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.util.Pair;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.ImageView;
import android.widget.ListView;
import com.android.settings.R;
import com.android.settings.Settings.TetherSettingsActivity;
import com.android.settings.overlay.FeatureFactory;
import com.google.common.primitives.Ints;
import com.oneplus.settings.utils.OPUtils;
import java.util.ArrayList;
import java.util.List;

public class CreateShortcut extends LauncherActivity {
    @VisibleForTesting
    static final String SHORTCUT_ID_PREFIX = "component-shortcut-";

    public static class ShortcutsUpdateTask extends AsyncTask<Void, Void, Void> {
        private final Context mContext;

        public ShortcutsUpdateTask(Context context) {
            this.mContext = context;
        }

        public Void doInBackground(Void... params) {
            ShortcutManager sm = (ShortcutManager) this.mContext.getSystemService(ShortcutManager.class);
            PackageManager pm = this.mContext.getPackageManager();
            List<ShortcutInfo> updates = new ArrayList();
            for (ShortcutInfo info : sm.getPinnedShortcuts()) {
                if (info.getId().startsWith(CreateShortcut.SHORTCUT_ID_PREFIX)) {
                    ResolveInfo ri = pm.resolveActivity(CreateShortcut.getBaseIntent().setComponent(ComponentName.unflattenFromString(info.getId().substring(CreateShortcut.SHORTCUT_ID_PREFIX.length()))), 0);
                    if (ri != null) {
                        updates.add(new Builder(this.mContext, info.getId()).setShortLabel(ri.loadLabel(pm)).build());
                    }
                }
            }
            if (!updates.isEmpty()) {
                sm.updateShortcuts(updates);
            }
            return null;
        }
    }

    /* Access modifiers changed, original: protected */
    public Intent getTargetIntent() {
        return getBaseIntent().addFlags(268435456);
    }

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        if (OPUtils.isWhiteModeOn(getContentResolver())) {
            getWindow().getDecorView().setSystemUiVisibility(8192);
        }
    }

    /* Access modifiers changed, original: protected */
    public void onListItemClick(ListView l, View v, int position, long id) {
        ListItem item = itemForPosition(position);
        logCreateShortcut(item.resolveInfo);
        setResult(-1, createResultIntent(intentForPosition(position), item.resolveInfo, item.label));
        finish();
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public Intent createResultIntent(Intent shortcutIntent, ResolveInfo resolveInfo, CharSequence label) {
        Icon maskableIcon;
        shortcutIntent.setFlags(335544320);
        ShortcutManager sm = (ShortcutManager) getSystemService(ShortcutManager.class);
        ActivityInfo activityInfo = resolveInfo.activityInfo;
        if (activityInfo.icon != 0) {
            maskableIcon = Icon.createWithAdaptiveBitmap(createIcon(activityInfo.icon, R.layout.shortcut_badge_maskable, getResources().getDimensionPixelSize(R.dimen.shortcut_size_maskable)));
        } else {
            maskableIcon = Icon.createWithResource(this, R.drawable.ic_launcher_settings);
        }
        String shortcutId = new StringBuilder();
        shortcutId.append(SHORTCUT_ID_PREFIX);
        shortcutId.append(shortcutIntent.getComponent().flattenToShortString());
        Intent intent = sm.createShortcutResultIntent(new Builder(this, shortcutId.toString()).setShortLabel(label).setIntent(shortcutIntent).setIcon(maskableIcon).build());
        if (intent == null) {
            intent = new Intent();
        }
        intent.putExtra("android.intent.extra.shortcut.ICON_RESOURCE", ShortcutIconResource.fromContext(this, R.mipmap.op_ic_launcher_settings));
        intent.putExtra("android.intent.extra.shortcut.INTENT", shortcutIntent);
        intent.putExtra("android.intent.extra.shortcut.NAME", label);
        if (activityInfo.icon != 0) {
            intent.putExtra("android.intent.extra.shortcut.ICON", createIcon(activityInfo.icon, R.layout.shortcut_badge, getResources().getDimensionPixelSize(R.dimen.shortcut_size)));
        }
        return intent;
    }

    private void logCreateShortcut(ResolveInfo info) {
        if (info != null && info.activityInfo != null) {
            FeatureFactory.getFactory(this).getMetricsFeatureProvider().action(this, 829, info.activityInfo.name, new Pair[0]);
        }
    }

    private Bitmap createIcon(int resource, int layoutRes, int size) {
        View view = LayoutInflater.from(new ContextThemeWrapper(this, 16974372)).inflate(layoutRes, null);
        Drawable iconDrawable = getDrawable(resource);
        if (iconDrawable instanceof LayerDrawable) {
            iconDrawable = ((LayerDrawable) iconDrawable).getDrawable(1);
        }
        ((ImageView) view.findViewById(16908294)).setImageDrawable(iconDrawable);
        int spec = MeasureSpec.makeMeasureSpec(size, Ints.MAX_POWER_OF_TWO);
        view.measure(spec, spec);
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.draw(canvas);
        return bitmap;
    }

    /* Access modifiers changed, original: protected */
    public boolean onEvaluateShowIcons() {
        return false;
    }

    /* Access modifiers changed, original: protected */
    public void onSetContentView() {
        setContentView(R.layout.activity_list);
    }

    /* Access modifiers changed, original: protected */
    public List<ResolveInfo> onQueryPackageManager(Intent queryIntent) {
        List<ResolveInfo> activities = getPackageManager().queryIntentActivities(queryIntent, 128);
        ConnectivityManager cm = (ConnectivityManager) getSystemService("connectivity");
        if (activities == null) {
            return null;
        }
        for (int i = activities.size() - 1; i >= 0; i--) {
            if (((ResolveInfo) activities.get(i)).activityInfo.name.endsWith(TetherSettingsActivity.class.getSimpleName()) && !cm.isTetheringSupported()) {
                activities.remove(i);
            }
        }
        return activities;
    }

    @VisibleForTesting
    static Intent getBaseIntent() {
        return new Intent("android.intent.action.MAIN").addCategory("com.android.settings.SHORTCUT");
    }
}
