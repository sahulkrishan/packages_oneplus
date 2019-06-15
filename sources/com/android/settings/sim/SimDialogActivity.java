package com.android.settings.sim;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.settings.R;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SimDialogActivity extends Activity {
    public static final int CALLS_PICK = 1;
    public static final int DATA_PICK = 0;
    public static String DIALOG_TYPE_KEY = "dialog_type";
    public static final int INVALID_PICK = -1;
    public static final int PREFERRED_PICK = 3;
    public static String PREFERRED_SIM = "preferred_sim";
    public static final int SMS_PICK = 2;
    private static String TAG = "SimDialogActivity";

    private class SelectAccountListAdapter extends ArrayAdapter<String> {
        private final float OPACITY = 0.54f;
        private Context mContext;
        private int mDialogId;
        private int mResId;
        private List<SubscriptionInfo> mSubInfoList;

        private class ViewHolder {
            ImageView icon;
            TextView summary;
            TextView title;

            private ViewHolder() {
            }

            /* synthetic */ ViewHolder(SelectAccountListAdapter x0, AnonymousClass1 x1) {
                this();
            }
        }

        public SelectAccountListAdapter(List<SubscriptionInfo> subInfoList, Context context, int resource, String[] arr, int dialogId) {
            super(context, resource, arr);
            this.mContext = context;
            this.mResId = resource;
            this.mDialogId = dialogId;
            this.mSubInfoList = subInfoList;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView;
            ViewHolder holder;
            LayoutInflater inflater = (LayoutInflater) this.mContext.getSystemService("layout_inflater");
            if (convertView == null) {
                rowView = inflater.inflate(this.mResId, null);
                holder = new ViewHolder(this, null);
                holder.title = (TextView) rowView.findViewById(R.id.title);
                holder.summary = (TextView) rowView.findViewById(R.id.summary);
                holder.icon = (ImageView) rowView.findViewById(R.id.icon);
                rowView.setTag(holder);
            } else {
                rowView = convertView;
                holder = (ViewHolder) rowView.getTag();
            }
            SubscriptionInfo sir = (SubscriptionInfo) this.mSubInfoList.get(position);
            if (sir == null) {
                holder.title.setText((CharSequence) getItem(position));
                holder.summary.setText("");
                holder.icon.setImageDrawable(SimDialogActivity.this.getResources().getDrawable(R.drawable.ic_live_help));
                holder.icon.setAlpha(0.54f);
            } else {
                holder.title.setText(sir.getDisplayName());
                holder.summary.setText(sir.getNumber());
                holder.icon.setImageBitmap(sir.createIconBitmap(this.mContext));
            }
            return rowView;
        }
    }

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int dialogType = getIntent().getIntExtra(DIALOG_TYPE_KEY, -1);
        switch (dialogType) {
            case 0:
            case 1:
            case 2:
                createDialog(this, dialogType).show();
                return;
            case 3:
                displayPreferredDialog(getIntent().getIntExtra(PREFERRED_SIM, 0));
                return;
            default:
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Invalid dialog type ");
                stringBuilder.append(dialogType);
                stringBuilder.append(" sent.");
                throw new IllegalArgumentException(stringBuilder.toString());
        }
    }

    private void displayPreferredDialog(int slotId) {
        Resources res = getResources();
        final Context context = getApplicationContext();
        final SubscriptionInfo sir = SubscriptionManager.from(context).getActiveSubscriptionInfoForSimSlotIndex(slotId);
        if (sir != null) {
            Builder alertDialogBuilder = new Builder(this);
            alertDialogBuilder.setTitle(R.string.sim_preferred_title);
            alertDialogBuilder.setMessage(res.getString(R.string.sim_preferred_message, new Object[]{sir.getDisplayName()}));
            alertDialogBuilder.setPositiveButton(R.string.yes, new OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    int subId = sir.getSubscriptionId();
                    PhoneAccountHandle phoneAccountHandle = SimDialogActivity.this.subscriptionIdToPhoneAccountHandle(subId);
                    SimDialogActivity.setDefaultDataSubId(context, subId);
                    SimDialogActivity.setDefaultSmsSubId(context, subId);
                    SimDialogActivity.this.setUserSelectedOutgoingPhoneAccount(phoneAccountHandle);
                    SimDialogActivity.this.finish();
                }
            });
            alertDialogBuilder.setNegativeButton(R.string.no, new OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    SimDialogActivity.this.finish();
                }
            });
            alertDialogBuilder.create().show();
            return;
        }
        finish();
    }

    private static void setDefaultDataSubId(Context context, int subId) {
        SubscriptionManager.from(context).setDefaultDataSubId(subId);
        Toast.makeText(context, R.string.data_switch_started, 1).show();
    }

    private static void setDefaultSmsSubId(Context context, int subId) {
        SubscriptionManager.from(context).setDefaultSmsSubId(subId);
    }

    private void setUserSelectedOutgoingPhoneAccount(PhoneAccountHandle phoneAccount) {
        TelecomManager.from(this).setUserSelectedOutgoingPhoneAccount(phoneAccount);
    }

    private PhoneAccountHandle subscriptionIdToPhoneAccountHandle(int subId) {
        TelecomManager telecomManager = TelecomManager.from(this);
        TelephonyManager telephonyManager = TelephonyManager.from(this);
        Iterator<PhoneAccountHandle> phoneAccounts = telecomManager.getCallCapablePhoneAccounts().listIterator();
        while (phoneAccounts.hasNext()) {
            PhoneAccountHandle phoneAccountHandle = (PhoneAccountHandle) phoneAccounts.next();
            if (subId == telephonyManager.getSubIdForPhoneAccount(telecomManager.getPhoneAccount(phoneAccountHandle))) {
                return phoneAccountHandle;
            }
        }
        return null;
    }

    public Dialog createDialog(Context context, int id) {
        Builder builder;
        final Context context2 = context;
        final int i = id;
        ArrayList<String> list = new ArrayList();
        final List<SubscriptionInfo> subInfoList = SubscriptionManager.from(context).getActiveSubscriptionInfoList();
        int selectableSubInfoLength = subInfoList == null ? 0 : subInfoList.size();
        AnonymousClass3 selectionListener = new OnClickListener() {
            public void onClick(DialogInterface dialog, int value) {
                switch (i) {
                    case 0:
                        SimDialogActivity.setDefaultDataSubId(context2, ((SubscriptionInfo) subInfoList.get(value)).getSubscriptionId());
                        break;
                    case 1:
                        SimDialogActivity.this.setUserSelectedOutgoingPhoneAccount(value < 1 ? null : (PhoneAccountHandle) TelecomManager.from(context2).getCallCapablePhoneAccounts().get(value - 1));
                        break;
                    case 2:
                        SimDialogActivity.setDefaultSmsSubId(context2, ((SubscriptionInfo) subInfoList.get(value)).getSubscriptionId());
                        break;
                    default:
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("Invalid dialog type ");
                        stringBuilder.append(i);
                        stringBuilder.append(" in SIM dialog.");
                        throw new IllegalArgumentException(stringBuilder.toString());
                }
                SimDialogActivity.this.finish();
            }
        };
        AnonymousClass4 keyListener = new OnKeyListener() {
            public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {
                if (keyCode == 4) {
                    SimDialogActivity.this.finish();
                }
                return true;
            }
        };
        ArrayList<SubscriptionInfo> callsSubInfoList = new ArrayList();
        int subId;
        if (i == 1) {
            TelecomManager telecomManager = TelecomManager.from(context);
            TelephonyManager telephonyManager = TelephonyManager.from(context);
            Iterator<PhoneAccountHandle> phoneAccounts = telecomManager.getCallCapablePhoneAccounts().listIterator();
            list.add(getResources().getString(R.string.sim_calls_ask_first_prefs_title));
            callsSubInfoList.add(null);
            while (phoneAccounts.hasNext()) {
                PhoneAccount phoneAccount = telecomManager.getPhoneAccount((PhoneAccountHandle) phoneAccounts.next());
                list.add((String) phoneAccount.getLabel());
                subId = telephonyManager.getSubIdForPhoneAccount(phoneAccount);
                if (subId != -1) {
                    callsSubInfoList.add(SubscriptionManager.from(context).getActiveSubscriptionInfo(subId));
                } else {
                    callsSubInfoList.add(null);
                }
            }
        } else {
            for (subId = 0; subId < selectableSubInfoLength; subId++) {
                CharSequence displayName = ((SubscriptionInfo) subInfoList.get(subId)).getDisplayName();
                if (displayName == null) {
                    displayName = "";
                }
                list.add(displayName.toString());
            }
        }
        String[] arr = (String[]) list.toArray(new String[0]);
        Builder builder2 = new Builder(context2);
        Builder builder3 = builder2;
        SelectAccountListAdapter adapter = new SelectAccountListAdapter(i == 1 ? callsSubInfoList : subInfoList, builder2.getContext(), R.layout.select_account_list_item, arr, i);
        switch (i) {
            case 0:
                builder = builder3;
                builder.setTitle(R.string.select_sim_for_data);
                break;
            case 1:
                builder = builder3;
                builder.setTitle(R.string.select_sim_for_calls);
                break;
            case 2:
                builder = builder3;
                builder.setTitle(R.string.sim_card_select_title);
                break;
            default:
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Invalid dialog type ");
                stringBuilder.append(i);
                stringBuilder.append(" in SIM dialog.");
                throw new IllegalArgumentException(stringBuilder.toString());
        }
        Dialog dialog = builder.setAdapter(adapter, selectionListener).create();
        dialog.setOnKeyListener(keyListener);
        dialog.setOnCancelListener(new OnCancelListener() {
            public void onCancel(DialogInterface dialogInterface) {
                SimDialogActivity.this.finish();
            }
        });
        return dialog;
    }
}
