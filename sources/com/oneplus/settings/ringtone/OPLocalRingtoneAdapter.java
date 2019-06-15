package com.oneplus.settings.ringtone;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.android.settings.R;
import com.oneplus.lib.widget.button.OPRadioButton;
import java.util.ArrayList;
import java.util.List;

public class OPLocalRingtoneAdapter extends BaseAdapter {
    private Context mContext;
    private List mData = new ArrayList();

    static class HoldView {
        OPRadioButton button;
        TextView mTitle;

        HoldView() {
        }
    }

    public static class RingtoneData {
        public String filepath;
        public boolean isCheck;
        public Uri mUri;
        public String mimetype;
        public String title;

        public RingtoneData(Uri uri, String t, boolean is) {
            this.mUri = uri;
            this.title = t;
            this.isCheck = is;
        }
    }

    public OPLocalRingtoneAdapter(Context context, List list) {
        this.mContext = context;
        this.mData = list;
    }

    public int getCount() {
        return this.mData.size();
    }

    public Object getItem(int position) {
        return this.mData.get(position);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        HoldView holdview;
        if (convertView == null) {
            holdview = new HoldView();
            convertView = LayoutInflater.from(this.mContext).inflate(R.layout.op_locatringtone_item, null);
            holdview.mTitle = (TextView) convertView.findViewById(16908310);
            holdview.button = (OPRadioButton) convertView.findViewById(R.id.id_button);
            convertView.setTag(holdview);
        } else {
            holdview = (HoldView) convertView.getTag();
        }
        if (this.mData != null) {
            RingtoneData data = (RingtoneData) this.mData.get(position);
            if (data != null) {
                holdview.mTitle.setText(data.title);
                holdview.button.setChecked(data.isCheck);
            }
        }
        return convertView;
    }
}
