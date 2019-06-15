package com.android.settings.password;

import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.android.settings.R;

public class PasswordRequirementAdapter extends Adapter<PasswordRequirementViewHolder> {
    private String[] mRequirements;

    public static class PasswordRequirementViewHolder extends ViewHolder {
        private TextView mDescriptionText;

        public PasswordRequirementViewHolder(View itemView) {
            super(itemView);
            this.mDescriptionText = (TextView) itemView;
        }
    }

    public PasswordRequirementAdapter() {
        setHasStableIds(true);
    }

    public PasswordRequirementViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new PasswordRequirementViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.password_requirement_item, parent, false));
    }

    public int getItemCount() {
        return this.mRequirements.length;
    }

    public void setRequirements(String[] requirements) {
        this.mRequirements = requirements;
        notifyDataSetChanged();
    }

    public long getItemId(int position) {
        return (long) this.mRequirements[position].hashCode();
    }

    public void onBindViewHolder(PasswordRequirementViewHolder holder, int position) {
        holder.mDescriptionText.setText(this.mRequirements[position]);
    }
}
