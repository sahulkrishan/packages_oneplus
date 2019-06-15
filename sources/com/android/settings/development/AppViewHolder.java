package com.android.settings.development;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settingslib.applications.ApplicationsState.AppEntry;

public class AppViewHolder {
    public ImageView appIcon;
    public TextView appName;
    public TextView disabled;
    public AppEntry entry;
    public View rootView;
    public TextView summary;

    public static AppViewHolder createOrRecycle(LayoutInflater inflater, View convertView) {
        if (convertView != null) {
            return (AppViewHolder) convertView.getTag();
        }
        convertView = inflater.inflate(R.layout.preference_app, null);
        AppViewHolder holder = new AppViewHolder();
        holder.rootView = convertView;
        holder.appName = (TextView) convertView.findViewById(16908310);
        holder.appIcon = (ImageView) convertView.findViewById(16908294);
        holder.summary = (TextView) convertView.findViewById(16908304);
        holder.disabled = (TextView) convertView.findViewById(R.id.appendix);
        convertView.setTag(holder);
        return holder;
    }
}
