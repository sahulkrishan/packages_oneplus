package com.android.settings.display;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;
import android.support.annotation.VisibleForTesting;
import android.util.AttributeSet;
import android.util.IconDrawableFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import com.android.settings.R;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AppGridView extends GridView {

    @VisibleForTesting
    public static class ActivityEntry implements Comparable<ActivityEntry> {
        public final ResolveInfo info;
        public final String label;
        private final IconDrawableFactory mIconFactory;
        private final int mUserId = UserHandle.myUserId();

        public ActivityEntry(ResolveInfo info, String label, IconDrawableFactory iconFactory) {
            this.info = info;
            this.label = label;
            this.mIconFactory = iconFactory;
        }

        public int compareTo(ActivityEntry entry) {
            return this.label.compareToIgnoreCase(entry.label);
        }

        public String toString() {
            return this.label;
        }

        public Drawable getIcon() {
            return this.mIconFactory.getBadgedIcon(this.info.activityInfo, this.info.activityInfo.applicationInfo, this.mUserId);
        }
    }

    @VisibleForTesting
    public static class AppsAdapter extends ArrayAdapter<ActivityEntry> {
        private final int mIconResId;
        private final PackageManager mPackageManager;

        public AppsAdapter(Context context, int layout, int textResId, int iconResId) {
            super(context, layout, textResId);
            this.mIconResId = iconResId;
            this.mPackageManager = context.getPackageManager();
            loadAllApps();
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            ((ImageView) view.findViewById(this.mIconResId)).setImageDrawable(((ActivityEntry) getItem(position)).getIcon());
            return view;
        }

        public boolean hasStableIds() {
            return true;
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public boolean isEnabled(int position) {
            return false;
        }

        private void loadAllApps() {
            Intent mainIntent = new Intent("android.intent.action.MAIN", null);
            mainIntent.addCategory("android.intent.category.LAUNCHER");
            PackageManager pm = this.mPackageManager;
            ArrayList<ActivityEntry> results = new ArrayList();
            List<ResolveInfo> infos = pm.queryIntentActivities(mainIntent, null);
            IconDrawableFactory iconFactory = IconDrawableFactory.newInstance(getContext());
            for (ResolveInfo info : infos) {
                CharSequence label = info.loadLabel(pm);
                if (label != null) {
                    results.add(new ActivityEntry(info, label.toString(), iconFactory));
                }
            }
            Collections.sort(results);
            addAll(results);
        }
    }

    public AppGridView(Context context) {
        this(context, null);
    }

    public AppGridView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AppGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public AppGridView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleResId) {
        super(context, attrs, defStyleAttr, defStyleResId);
        setNumColumns(-1);
        setColumnWidth(getResources().getDimensionPixelSize(R.dimen.screen_zoom_preview_app_icon_width));
        setAdapter(new AppsAdapter(context, R.layout.screen_zoom_preview_app_icon, 16908308, 16908295));
    }
}
