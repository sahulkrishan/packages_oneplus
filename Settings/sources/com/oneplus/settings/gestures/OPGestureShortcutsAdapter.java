package com.oneplus.settings.gestures;

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
import java.util.ArrayList;
import java.util.List;

public class OPGestureShortcutsAdapter extends BaseAdapter {
    private static final int VIEW_TYPE_ITEM = 1;
    private static final int VIEW_TYPE_TITLE = 0;
    private Context mContext;
    private List<OPGestureAppModel> mGestureAppList = new ArrayList();
    private String mGestureSummary;
    private LayoutInflater mInflater;
    public PackageManager mPackageManager;
    public int mSelectedPosition;

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

    public OPGestureShortcutsAdapter(Context ctx, List<OPGestureAppModel> gestureAppList, String gestureSummary) {
        this.mContext = ctx;
        this.mInflater = LayoutInflater.from(this.mContext);
        this.mGestureAppList = gestureAppList;
        this.mGestureSummary = gestureSummary;
    }

    public void setData(List<OPGestureAppModel> list) {
        this.mGestureAppList = list;
        notifyDataSetChanged();
    }

    public void setSelectedPosition(int position) {
        this.mSelectedPosition = position;
        notifyDataSetChanged();
    }

    public void setSelectedSummary(String summary) {
        this.mGestureSummary = summary;
        notifyDataSetChanged();
    }

    public int getCount() {
        return this.mGestureAppList.size();
    }

    public OPGestureAppModel getItem(int position) {
        return (OPGestureAppModel) this.mGestureAppList.get(position);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ItemViewHolder mItemViewHolder;
        OPGestureAppModel model = (OPGestureAppModel) this.mGestureAppList.get(position);
        if (convertView == null) {
            convertView = this.mInflater.inflate(R.layout.op_gesture_app_item, null);
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
            mItemViewHolder.titleTv.setText(R.string.oneplus_gestures_fast_entrance);
        } else {
            mItemViewHolder.titleTv.setVisibility(8);
        }
        mItemViewHolder.appIconIv.setVisibility(0);
        mItemViewHolder.appIconIv.setImageDrawable(model.getAppIcon());
        mItemViewHolder.appNameTv.setText(model.getTitle());
        if (position == 0) {
            mItemViewHolder.bottomLine.setVisibility(0);
        } else {
            mItemViewHolder.bottomLine.setVisibility(8);
        }
        mItemViewHolder.groupDivider.setVisibility(8);
        if (this.mGestureSummary.equals(model.getTitle())) {
            mItemViewHolder.radioButton.setChecked(true);
        } else {
            mItemViewHolder.radioButton.setChecked(false);
        }
        return convertView;
    }
}
