package com.oneplus.settings.laboratory;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.settings.R;
import java.util.List;

public class OPLabPluginListAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater mInflate;
    private List<OPLabPluginModel> mPluginData;

    private class ViewHolder {
        ImageView featureImage;
        TextView featureTitle;

        private ViewHolder() {
        }
    }

    public OPLabPluginListAdapter(Context context, List<OPLabPluginModel> pluginData) {
        this.mPluginData = pluginData;
        this.mContext = context;
        this.mInflate = LayoutInflater.from(context);
    }

    public void setData(List<OPLabPluginModel> pluginData) {
        this.mPluginData = pluginData;
        notifyDataSetChanged();
    }

    public int getCount() {
        return this.mPluginData.size();
    }

    public OPLabPluginModel getItem(int position) {
        return (OPLabPluginModel) this.mPluginData.get(position);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = this.mInflate.inflate(R.layout.op_lab_feature_plugin_item, null);
            viewHolder.featureImage = (ImageView) convertView.findViewById(R.id.feature_imageview);
            viewHolder.featureTitle = (TextView) convertView.findViewById(R.id.feature_title);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.featureTitle.setText(((OPLabPluginModel) this.mPluginData.get(position)).getFeatureTitle());
        viewHolder.featureImage.setImageResource(((OPLabPluginModel) this.mPluginData.get(position)).geFeatureIconId());
        return convertView;
    }
}
