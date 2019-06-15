package com.oneplus.settings.carcharger;

import android.content.Context;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.settings.R;
import com.oneplus.settings.better.OPAppModel;
import java.util.ArrayList;
import java.util.List;

public class OPCarChargerOpenAppAdapter extends BaseAdapter {
    private int hasRecommendedCount;
    private Context mContext;
    private LayoutInflater mInflater;
    private List<OPAppModel> mOpenAppList = new ArrayList();
    private PackageManager mPackageManager;
    private String mPackageName;
    private int mSelectedPosition;

    class ItemViewHolder {
        ImageView appIconIv;
        TextView appNameTv;
        View bottomLine;
        View groupDivider;
        RelativeLayout parent;
        RadioButton radioButton;
        TextView titleTv;

        ItemViewHolder() {
        }
    }

    public OPCarChargerOpenAppAdapter(Context context, PackageManager packageManager) {
        this.mContext = context;
        this.mPackageManager = packageManager;
        this.mInflater = LayoutInflater.from(this.mContext);
    }

    public void setData(List<OPAppModel> list) {
        this.mOpenAppList = list;
        notifyDataSetChanged();
    }

    public void setSelectedPosition(int position) {
        this.mSelectedPosition = position;
        notifyDataSetChanged();
    }

    public void setHasRecommendedCount(int count) {
        this.hasRecommendedCount = count;
    }

    public void setSelectedItem(String pacakgeName) {
        if (pacakgeName != null) {
            this.mPackageName = pacakgeName;
        } else {
            this.mPackageName = "";
        }
        notifyDataSetChanged();
    }

    public int getCount() {
        return this.mOpenAppList.size();
    }

    public OPAppModel getItem(int position) {
        return (OPAppModel) this.mOpenAppList.get(position);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ItemViewHolder mItemViewHolder;
        OPAppModel model = (OPAppModel) this.mOpenAppList.get(position);
        if (convertView == null) {
            convertView = this.mInflater.inflate(R.layout.op_car_charger_open_app_item, null);
            mItemViewHolder = new ItemViewHolder();
            mItemViewHolder.parent = (RelativeLayout) convertView.findViewById(R.id.parent);
            mItemViewHolder.titleTv = (TextView) convertView.findViewById(R.id.tv);
            mItemViewHolder.appIconIv = (ImageView) convertView.findViewById(R.id.icon);
            mItemViewHolder.appNameTv = (TextView) convertView.findViewById(R.id.name);
            mItemViewHolder.bottomLine = convertView.findViewById(R.id.bottom_line);
            mItemViewHolder.groupDivider = convertView.findViewById(R.id.group_divider_area);
            mItemViewHolder.radioButton = (RadioButton) convertView.findViewById(R.id.radio_button);
            convertView.setTag(mItemViewHolder);
        } else {
            mItemViewHolder = (ItemViewHolder) convertView.getTag();
        }
        mItemViewHolder.titleTv.setVisibility(0);
        if (position == 1) {
            mItemViewHolder.titleTv.setText(R.string.oneplus_auto_open_app_recommended);
        } else if (position == this.hasRecommendedCount + 1) {
            mItemViewHolder.titleTv.setText(R.string.oneplus_auto_open_app_other_applications);
        } else {
            mItemViewHolder.titleTv.setVisibility(8);
        }
        if (position < 1) {
            mItemViewHolder.appIconIv.setVisibility(4);
        } else {
            mItemViewHolder.appIconIv.setVisibility(0);
            mItemViewHolder.appIconIv.setImageDrawable(model.getAppIcon());
        }
        mItemViewHolder.appNameTv.setText(model.getLabel());
        if (position == 0 || position == this.hasRecommendedCount) {
            mItemViewHolder.bottomLine.setVisibility(0);
        } else {
            mItemViewHolder.bottomLine.setVisibility(8);
        }
        mItemViewHolder.groupDivider.setVisibility(8);
        if (this.mPackageName.equals(model.getPkgName())) {
            mItemViewHolder.radioButton.setChecked(true);
        } else {
            mItemViewHolder.radioButton.setChecked(false);
        }
        return convertView;
    }
}
