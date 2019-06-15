package com.oneplus.settings.aboutphone;

import android.content.Context;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.settings.R;
import java.util.List;

public class SoftwareInfoAdapter extends Adapter<ViewHolder> {
    private Context mContext;
    private List<SoftwareInfoEntity> mList;
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, int i);
    }

    class ViewHolder extends android.support.v7.widget.RecyclerView.ViewHolder {
        ImageView imageView;
        TextView tvSummary;
        TextView tvTitle;
        View viewDividerBottom;
        View viewDividerRight;

        public ViewHolder(View itemView) {
            super(itemView);
            this.tvTitle = (TextView) itemView.findViewById(R.id.tv_title_software_info);
            this.tvSummary = (TextView) itemView.findViewById(R.id.tv_summary_software_info);
            this.imageView = (ImageView) itemView.findViewById(R.id.img_software_info);
            this.viewDividerRight = itemView.findViewById(R.id.divider_right);
            this.viewDividerBottom = itemView.findViewById(R.id.divider_bottom);
        }
    }

    public SoftwareInfoAdapter(Context context, List<SoftwareInfoEntity> list) {
        this.mContext = context;
        this.mList = list;
    }

    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(this.mContext).inflate(R.layout.op_aboute_phone_software_item, null));
    }

    public void onBindViewHolder(final ViewHolder holder, final int position) {
        if (position >= 0 && position < this.mList.size()) {
            SoftwareInfoEntity entity = (SoftwareInfoEntity) this.mList.get(position);
            holder.tvTitle.setText(entity.getTitle());
            holder.tvSummary.setText(entity.getSummary());
            holder.imageView.setImageDrawable(this.mContext.getDrawable(entity.getResIcon()));
        }
        if (position % 2 == 1) {
            holder.viewDividerRight.setVisibility(4);
            if (position >= this.mList.size() - 1) {
                holder.viewDividerBottom.setVisibility(4);
            }
        }
        if (position % 2 == 0 && position >= this.mList.size() - 2) {
            holder.viewDividerBottom.setVisibility(4);
        }
        if (this.mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    SoftwareInfoAdapter.this.mOnItemClickListener.onItemClick(holder.itemView, position);
                }
            });
        }
    }

    public int getItemCount() {
        return this.mList.size();
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }
}
