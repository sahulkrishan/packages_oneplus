package com.oneplus.settings.multiapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import com.android.settings.R;
import com.oneplus.settings.better.OPAppModel;
import java.util.ArrayList;
import java.util.List;

public class OPMultiAppAdapter extends BaseAdapter {
    private List<OPAppModel> mAppList = new ArrayList();
    private Context mContext;
    private LayoutInflater mInflater;
    private List<Boolean> mSelectedList = new ArrayList();

    class ItemViewHolder {
        ImageView appIconIv;
        TextView appNameTv;
        View bottomLine;
        View groupDivider;
        RelativeLayout parent;
        TextView summaryTv;
        Switch switchBt;
        TextView titleTv;

        ItemViewHolder() {
        }
    }

    public OPMultiAppAdapter(Context context, List<OPAppModel> appList) {
        this.mAppList = appList;
        this.mContext = context;
        this.mInflater = LayoutInflater.from(this.mContext);
    }

    public void setData(List<OPAppModel> list) {
        this.mAppList = list;
        this.mSelectedList.clear();
        for (int i = 0; i < this.mAppList.size(); i++) {
            this.mSelectedList.add(Boolean.valueOf(((OPAppModel) this.mAppList.get(i)).isSelected()));
        }
        notifyDataSetChanged();
    }

    public void setSelected(int position, boolean selected) {
        this.mSelectedList.set(position, Boolean.valueOf(selected));
        notifyDataSetChanged();
    }

    public boolean getSelected(int position) {
        return ((Boolean) this.mSelectedList.get(position)).booleanValue();
    }

    public int getCount() {
        return this.mAppList.size();
    }

    public OPAppModel getItem(int position) {
        return (OPAppModel) this.mAppList.get(position);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ItemViewHolder mItemViewHolder;
        OPAppModel model = (OPAppModel) this.mAppList.get(position);
        if (convertView == null) {
            convertView = this.mInflater.inflate(R.layout.op_multi_app_item, null);
            mItemViewHolder = new ItemViewHolder();
            mItemViewHolder.parent = (RelativeLayout) convertView.findViewById(R.id.parent);
            mItemViewHolder.titleTv = (TextView) convertView.findViewById(R.id.tv);
            mItemViewHolder.appIconIv = (ImageView) convertView.findViewById(R.id.icon);
            mItemViewHolder.appNameTv = (TextView) convertView.findViewById(R.id.name);
            mItemViewHolder.summaryTv = (TextView) convertView.findViewById(R.id.summary);
            mItemViewHolder.bottomLine = convertView.findViewById(R.id.bottom_line);
            mItemViewHolder.groupDivider = convertView.findViewById(R.id.group_divider_area);
            mItemViewHolder.switchBt = (Switch) convertView.findViewById(R.id.switch_button);
            convertView.setTag(mItemViewHolder);
        } else {
            mItemViewHolder = (ItemViewHolder) convertView.getTag();
        }
        mItemViewHolder.bottomLine.setVisibility(0);
        mItemViewHolder.groupDivider.setVisibility(8);
        if (position == 0) {
            mItemViewHolder.titleTv.setVisibility(0);
            if (model.isSelected()) {
                mItemViewHolder.titleTv.setText(R.string.oneplus_multi_app_enable_apps);
                if (!((OPAppModel) this.mAppList.get(Math.min(position + 1, getCount() - 1))).isSelected()) {
                    mItemViewHolder.bottomLine.setVisibility(8);
                }
            } else {
                mItemViewHolder.titleTv.setText(R.string.oneplus_multi_app_disable_apps);
            }
        } else if (!model.isSelected() && ((OPAppModel) this.mAppList.get(position - 1)).isSelected()) {
            mItemViewHolder.titleTv.setVisibility(0);
            mItemViewHolder.titleTv.setText(R.string.oneplus_multi_app_disable_apps);
        } else if (!model.isSelected() || ((OPAppModel) this.mAppList.get(Math.min(position + 1, getCount() - 1))).isSelected()) {
            mItemViewHolder.titleTv.setVisibility(8);
        } else {
            mItemViewHolder.bottomLine.setVisibility(8);
            mItemViewHolder.titleTv.setVisibility(8);
        }
        mItemViewHolder.appIconIv.setImageDrawable(model.getAppIcon());
        mItemViewHolder.appNameTv.setText(model.getLabel());
        mItemViewHolder.switchBt.setClickable(false);
        mItemViewHolder.switchBt.setBackground(null);
        if (getSelected(position)) {
            mItemViewHolder.switchBt.setChecked(true);
        } else {
            mItemViewHolder.switchBt.setChecked(false);
        }
        return convertView;
    }
}
