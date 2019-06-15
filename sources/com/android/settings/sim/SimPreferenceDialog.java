package com.android.settings.sim;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.Resources;
import android.graphics.Paint.Style;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settings.Utils;

public class SimPreferenceDialog extends Activity {
    private final String SIM_NAME = "sim_name";
    private final String TINT_POS = "tint_pos";
    Builder mBuilder;
    private String[] mColorStrings;
    private Context mContext;
    View mDialogLayout;
    private int mSlotId;
    private SubscriptionInfo mSubInfoRecord;
    private SubscriptionManager mSubscriptionManager;
    private int[] mTintArr;
    private int mTintSelectorPos;

    private class SelectColorAdapter extends ArrayAdapter<CharSequence> {
        private Context mContext;
        private int mResId;

        private class ViewHolder {
            ImageView icon;
            TextView label;
            ShapeDrawable swatch;

            private ViewHolder() {
            }

            /* synthetic */ ViewHolder(SelectColorAdapter x0, AnonymousClass1 x1) {
                this();
            }
        }

        public SelectColorAdapter(Context context, int resource, String[] arr) {
            super(context, resource, arr);
            this.mContext = context;
            this.mResId = resource;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView;
            ViewHolder holder;
            LayoutInflater inflater = (LayoutInflater) this.mContext.getSystemService("layout_inflater");
            Resources res = this.mContext.getResources();
            int iconSize = res.getDimensionPixelSize(R.dimen.color_swatch_size);
            int strokeWidth = res.getDimensionPixelSize(R.dimen.color_swatch_stroke_width);
            if (convertView == null) {
                rowView = inflater.inflate(this.mResId, null);
                holder = new ViewHolder(this, null);
                ShapeDrawable drawable = new ShapeDrawable(new OvalShape());
                drawable.setIntrinsicHeight(iconSize);
                drawable.setIntrinsicWidth(iconSize);
                drawable.getPaint().setStrokeWidth((float) strokeWidth);
                holder.label = (TextView) rowView.findViewById(R.id.color_text);
                holder.icon = (ImageView) rowView.findViewById(R.id.color_icon);
                holder.swatch = drawable;
                rowView.setTag(holder);
            } else {
                rowView = convertView;
                holder = (ViewHolder) rowView.getTag();
            }
            holder.label.setText((CharSequence) getItem(position));
            holder.swatch.getPaint().setColor(SimPreferenceDialog.this.mTintArr[position]);
            holder.swatch.getPaint().setStyle(Style.FILL_AND_STROKE);
            holder.icon.setVisibility(0);
            holder.icon.setImageDrawable(holder.swatch);
            return rowView;
        }

        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View rowView = getView(position, convertView, parent);
            ViewHolder holder = (ViewHolder) rowView.getTag();
            if (SimPreferenceDialog.this.mTintSelectorPos == position) {
                holder.swatch.getPaint().setStyle(Style.FILL_AND_STROKE);
            } else {
                holder.swatch.getPaint().setStyle(Style.STROKE);
            }
            holder.icon.setVisibility(0);
            return rowView;
        }
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mContext = this;
        this.mSlotId = getIntent().getExtras().getInt(SimSettings.EXTRA_SLOT_ID, -1);
        this.mSubscriptionManager = SubscriptionManager.from(this.mContext);
        this.mSubInfoRecord = this.mSubscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(this.mSlotId);
        this.mTintArr = this.mContext.getResources().getIntArray(17236082);
        this.mColorStrings = this.mContext.getResources().getStringArray(R.array.color_picker);
        this.mTintSelectorPos = 0;
        this.mBuilder = new Builder(this.mContext);
        this.mDialogLayout = ((LayoutInflater) this.mContext.getSystemService("layout_inflater")).inflate(R.layout.multi_sim_dialog, null);
        this.mBuilder.setView(this.mDialogLayout);
        createEditDialog(bundle);
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt("tint_pos", this.mTintSelectorPos);
        savedInstanceState.putString("sim_name", ((EditText) this.mDialogLayout.findViewById(R.id.sim_name)).getText().toString());
        super.onSaveInstanceState(savedInstanceState);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int pos = savedInstanceState.getInt("tint_pos");
        ((Spinner) this.mDialogLayout.findViewById(R.id.spinner)).setSelection(pos);
        this.mTintSelectorPos = pos;
        EditText nameText = (EditText) this.mDialogLayout.findViewById(R.id.sim_name);
        nameText.setText(savedInstanceState.getString("sim_name"));
        Utils.setEditTextCursorPosition(nameText);
    }

    private void createEditDialog(Bundle bundle) {
        CharSequence string;
        Resources res = this.mContext.getResources();
        EditText nameText = (EditText) this.mDialogLayout.findViewById(R.id.sim_name);
        nameText.setText(this.mSubInfoRecord.getDisplayName());
        Utils.setEditTextCursorPosition(nameText);
        final Spinner tintSpinner = (Spinner) this.mDialogLayout.findViewById(R.id.spinner);
        SelectColorAdapter adapter = new SelectColorAdapter(this.mContext, R.layout.settings_color_picker_item, this.mColorStrings);
        adapter.setDropDownViewResource(17367049);
        tintSpinner.setAdapter(adapter);
        for (int i = 0; i < this.mTintArr.length; i++) {
            if (this.mTintArr[i] == this.mSubInfoRecord.getIconTint()) {
                tintSpinner.setSelection(i);
                this.mTintSelectorPos = i;
                break;
            }
        }
        tintSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                tintSpinner.setSelection(pos);
                SimPreferenceDialog.this.mTintSelectorPos = pos;
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
        TextView numberView = (TextView) this.mDialogLayout.findViewById(R.id.number);
        String rawNumber = tm.getLine1Number(this.mSubInfoRecord.getSubscriptionId());
        if (TextUtils.isEmpty(rawNumber)) {
            numberView.setText(res.getString(17039374));
        } else {
            numberView.setText(PhoneNumberUtils.formatNumber(rawNumber));
        }
        String simCarrierName = tm.getSimOperatorName(this.mSubInfoRecord.getSubscriptionId());
        TextView carrierView = (TextView) this.mDialogLayout.findViewById(R.id.carrier);
        if (TextUtils.isEmpty(simCarrierName)) {
            string = this.mContext.getString(17039374);
        } else {
            string = simCarrierName;
        }
        carrierView.setText(string);
        this.mBuilder.setTitle(String.format(res.getString(R.string.sim_editor_title), new Object[]{Integer.valueOf(this.mSubInfoRecord.getSimSlotIndex() + 1)}));
        this.mBuilder.setPositiveButton(R.string.okay, new OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                EditText nameText = (EditText) SimPreferenceDialog.this.mDialogLayout.findViewById(R.id.sim_name);
                Utils.setEditTextCursorPosition(nameText);
                String displayName = nameText.getText().toString();
                int subId = SimPreferenceDialog.this.mSubInfoRecord.getSubscriptionId();
                SimPreferenceDialog.this.mSubInfoRecord.setDisplayName(displayName);
                SimPreferenceDialog.this.mSubscriptionManager.setDisplayName(displayName, subId, 2);
                int tintSelected = tintSpinner.getSelectedItemPosition();
                int subscriptionId = SimPreferenceDialog.this.mSubInfoRecord.getSubscriptionId();
                int tint = SimPreferenceDialog.this.mTintArr[tintSelected];
                SimPreferenceDialog.this.mSubInfoRecord.setIconTint(tint);
                SimPreferenceDialog.this.mSubscriptionManager.setIconTint(tint, subscriptionId);
                dialog.dismiss();
            }
        });
        this.mBuilder.setNegativeButton(R.string.cancel, new OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        this.mBuilder.setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialogInListener) {
                SimPreferenceDialog.this.finish();
            }
        });
        this.mBuilder.create().show();
    }
}
