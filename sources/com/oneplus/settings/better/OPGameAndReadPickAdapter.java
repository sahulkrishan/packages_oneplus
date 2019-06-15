package com.oneplus.settings.better;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.settings.R;
import java.util.ArrayList;
import java.util.List;

public class OPGameAndReadPickAdapter extends BaseAdapter {
    private List<OPAppModel> mAppList = new ArrayList();
    private int mAppType;
    private Context mContext;
    private LayoutInflater mInflater;
    private List<Boolean> mSelectedList = new ArrayList();

    class ItemViewHolder {
        ImageView appIconIv;
        TextView appNameTv;
        View bottomLine;
        CheckBox checkBox;
        View groupDivider;
        RelativeLayout parent;
        TextView summaryTv;
        TextView titleTv;

        ItemViewHolder() {
        }
    }

    public OPGameAndReadPickAdapter(Context context, List<OPAppModel> appList) {
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

    public void setAppType(int type) {
        this.mAppType = type;
    }

    public void setGameAPP(int position, boolean isGameAPP) {
        this.mSelectedList.set(position, Boolean.valueOf(isGameAPP));
        notifyDataSetChanged();
    }

    public boolean getGameAPP(int isGameAPP) {
        return ((Boolean) this.mSelectedList.get(isGameAPP)).booleanValue();
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
            convertView = this.mInflater.inflate(R.layout.op_game_read_app_list_item, null);
            mItemViewHolder = new ItemViewHolder();
            mItemViewHolder.parent = (RelativeLayout) convertView.findViewById(R.id.parent);
            mItemViewHolder.titleTv = (TextView) convertView.findViewById(R.id.tv);
            mItemViewHolder.appIconIv = (ImageView) convertView.findViewById(R.id.icon);
            mItemViewHolder.appNameTv = (TextView) convertView.findViewById(R.id.name);
            mItemViewHolder.summaryTv = (TextView) convertView.findViewById(R.id.summary);
            mItemViewHolder.bottomLine = convertView.findViewById(R.id.bottom_line);
            mItemViewHolder.groupDivider = convertView.findViewById(R.id.group_divider_area);
            mItemViewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.check_box);
            convertView.setTag(mItemViewHolder);
        } else {
            mItemViewHolder = (ItemViewHolder) convertView.getTag();
        }
        if (position == 0) {
            mItemViewHolder.titleTv.setVisibility(0);
            if (this.mAppType != 68) {
                mItemViewHolder.titleTv.setText(R.string.oneplus_app_list);
            } else if (model.isGameAPP()) {
                mItemViewHolder.titleTv.setText(R.string.oneplus_recommended_opening);
            } else {
                mItemViewHolder.titleTv.setText(R.string.oneplus_other_applications);
            }
        } else if (this.mAppType != 68) {
            mItemViewHolder.titleTv.setVisibility(8);
        } else if (!model.isGameAPP() && ((OPAppModel) this.mAppList.get(position - 1)).isGameAPP()) {
            mItemViewHolder.titleTv.setVisibility(0);
            mItemViewHolder.titleTv.setText(R.string.oneplus_other_applications);
        } else if (!model.isGameAPP() || ((OPAppModel) this.mAppList.get(Math.min(position + 1, getCount() - 1))).isGameAPP()) {
            mItemViewHolder.titleTv.setVisibility(8);
        } else {
            mItemViewHolder.titleTv.setVisibility(8);
        }
        mItemViewHolder.appIconIv.setImageDrawable(model.getAppIcon());
        mItemViewHolder.appNameTv.setText(model.getLabel());
        mItemViewHolder.groupDivider.setVisibility(8);
        if (getSelected(position)) {
            mItemViewHolder.checkBox.setChecked(true);
        } else {
            mItemViewHolder.checkBox.setChecked(false);
        }
        return convertView;
    }
}
