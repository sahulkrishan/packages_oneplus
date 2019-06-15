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
import com.oneplus.settings.better.OPAppModel;
import java.util.ArrayList;
import java.util.List;

public class OPGestureAppAdapter extends BaseAdapter {
    private static final int VIEW_TYPE_ITEM = 1;
    private static final int VIEW_TYPE_TITLE = 0;
    private Context mContext;
    private List<OPAppModel> mGestureAppList = new ArrayList();
    private String mGesturePackageName;
    private String mGestureSummary;
    private int mGestureUid;
    private boolean mHasShortCut;
    private LayoutInflater mInflater;
    public PackageManager mPackageManager;
    public int mSelectedPosition;
    private String mShortcutName;

    class ItemViewHolder {
        ImageView appIconIv;
        TextView appNameTv;
        View bottomLine;
        View groupDivider;
        RelativeLayout parent;
        RadioButton radioButton;
        TextView summaryTv;
        TextView titleTv;

        ItemViewHolder() {
        }
    }

    public OPGestureAppAdapter(Context ctx, PackageManager packageManager, String gestureSummary) {
        this.mContext = ctx;
        this.mGestureSummary = gestureSummary;
        this.mInflater = LayoutInflater.from(this.mContext);
        this.mPackageManager = packageManager;
    }

    public void setData(List<OPAppModel> list) {
        this.mGestureAppList = list;
        notifyDataSetChanged();
    }

    public void setSelectedPosition(int position) {
        this.mSelectedPosition = position;
        notifyDataSetChanged();
    }

    public void setSelectedItem(String summary, String pacakgeName, int pacakgeUid, boolean hasShortcut, String shortCutName) {
        this.mGestureSummary = summary;
        this.mGesturePackageName = pacakgeName;
        this.mGestureUid = pacakgeUid;
        this.mHasShortCut = hasShortcut;
        this.mShortcutName = shortCutName;
        notifyDataSetChanged();
    }

    public int getCount() {
        return this.mGestureAppList.size();
    }

    public OPAppModel getItem(int position) {
        return (OPAppModel) this.mGestureAppList.get(position);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ItemViewHolder mItemViewHolder;
        OPAppModel model = (OPAppModel) this.mGestureAppList.get(position);
        if (convertView == null) {
            convertView = this.mInflater.inflate(R.layout.op_gesture_app_item, null);
            mItemViewHolder = new ItemViewHolder();
            mItemViewHolder.parent = (RelativeLayout) convertView.findViewById(R.id.parent);
            mItemViewHolder.titleTv = (TextView) convertView.findViewById(R.id.tv);
            mItemViewHolder.appIconIv = (ImageView) convertView.findViewById(R.id.icon);
            mItemViewHolder.appNameTv = (TextView) convertView.findViewById(R.id.name);
            mItemViewHolder.summaryTv = (TextView) convertView.findViewById(R.id.summary);
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
        } else if (position == 6) {
            mItemViewHolder.titleTv.setText(R.string.oneplus_gestures_open_apps);
        } else {
            mItemViewHolder.titleTv.setVisibility(8);
        }
        if (position < 6) {
            mItemViewHolder.appIconIv.setVisibility(8);
        } else {
            mItemViewHolder.appIconIv.setVisibility(0);
            mItemViewHolder.appIconIv.setImageDrawable(model.getAppIcon());
        }
        mItemViewHolder.appNameTv.setText(model.getLabel());
        if (position == 0 || position == 5) {
            mItemViewHolder.bottomLine.setVisibility(0);
        } else {
            mItemViewHolder.bottomLine.setVisibility(8);
        }
        mItemViewHolder.groupDivider.setVisibility(8);
        if ((position >= 6 || !this.mGestureSummary.equals(model.getLabel())) && (position < 6 || !this.mGesturePackageName.equals(model.getPkgName()))) {
            mItemViewHolder.radioButton.setChecked(false);
            mItemViewHolder.summaryTv.setVisibility(8);
        } else {
            mItemViewHolder.radioButton.setChecked(true);
            if (this.mHasShortCut) {
                mItemViewHolder.summaryTv.setText(this.mShortcutName);
                mItemViewHolder.summaryTv.setVisibility(0);
            } else {
                mItemViewHolder.summaryTv.setText("");
                mItemViewHolder.summaryTv.setVisibility(8);
            }
        }
        return convertView;
    }
}
