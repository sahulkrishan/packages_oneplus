package com.android.settings.fuelgauge.batterytip;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.UserHandle;
import android.support.v7.widget.RecyclerView.Adapter;
import android.util.IconDrawableFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settingslib.utils.StringUtil;
import java.util.List;

public class HighUsageAdapter extends Adapter<ViewHolder> {
    private final Context mContext;
    private final List<AppInfo> mHighUsageAppList;
    private final IconDrawableFactory mIconDrawableFactory;
    private final PackageManager mPackageManager;

    public static class ViewHolder extends android.support.v7.widget.RecyclerView.ViewHolder {
        public ImageView appIcon;
        public TextView appName;
        public TextView appTime;
        public View view;

        public ViewHolder(View v) {
            super(v);
            this.view = v;
            this.appIcon = (ImageView) v.findViewById(R.id.app_icon);
            this.appName = (TextView) v.findViewById(R.id.app_name);
            this.appTime = (TextView) v.findViewById(R.id.app_screen_time);
        }
    }

    public HighUsageAdapter(Context context, List<AppInfo> highUsageAppList) {
        this.mContext = context;
        this.mHighUsageAppList = highUsageAppList;
        this.mIconDrawableFactory = IconDrawableFactory.newInstance(context);
        this.mPackageManager = context.getPackageManager();
    }

    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(this.mContext).inflate(R.layout.app_high_usage_item, parent, false));
    }

    public void onBindViewHolder(ViewHolder holder, int position) {
        AppInfo app = (AppInfo) this.mHighUsageAppList.get(position);
        holder.appIcon.setImageDrawable(Utils.getBadgedIcon(this.mIconDrawableFactory, this.mPackageManager, app.packageName, UserHandle.getUserId(app.uid)));
        holder.appName.setText(Utils.getApplicationLabel(this.mContext, app.packageName));
        if (app.screenOnTimeMs != 0) {
            holder.appTime.setText(StringUtil.formatElapsedTime(this.mContext, (double) app.screenOnTimeMs, false));
        }
    }

    public int getItemCount() {
        return this.mHighUsageAppList.size();
    }
}
