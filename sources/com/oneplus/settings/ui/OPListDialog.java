package com.oneplus.settings.ui;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.provider.Settings.System;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import com.android.settings.R;
import com.oneplus.settings.utils.OPUtils;
import com.oneplus.settings.utils.OPVibrateUtils;

public class OPListDialog {
    private static final int TYPE_NEGATIVE = 2;
    private static final int TYPE_POSITIVE = 1;
    private Context mContext;
    private int mCurrentIndex = 0;
    private AlertDialog mDialog = null;
    private DialogListAdapter mDialogListAdapter;
    private String[] mListEntries;
    private String[] mListEntriesValue;
    private ListView mListView;
    private int mNegativeCount = 0;
    private OnDialogListItemClickListener mOnDialogListItemClickListener;
    private int mPositiveCount = 0;
    private RadioGroup mRootContainer;
    private String mVibrateKey;
    private Button n = null;
    private Button p = null;
    private boolean status = true;

    class DialogListAdapter extends BaseAdapter {

        class ViewHolder {
            ViewHolder() {
            }
        }

        DialogListAdapter() {
        }

        public int getCount() {
            return OPListDialog.this.mListEntriesValue.length;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            return convertView;
        }
    }

    public interface OnDialogListItemClickListener {
        void OnDialogListCancelClick();

        void OnDialogListConfirmClick(int i);

        void OnDialogListItemClick(int i);
    }

    public OPListDialog(Context ctx) {
        this.mContext = ctx;
        this.mRootContainer = (RadioGroup) LayoutInflater.from(ctx).inflate(R.layout.op_list_dialog_item_layout, null);
        this.mDialog = new Builder(this.mContext).setView(this.mRootContainer).create();
        this.mDialog.setCanceledOnTouchOutside(true);
    }

    public OPListDialog(Context ctx, CharSequence title, String[] listEntriesValue, String[] listEntries) {
        this.mContext = ctx;
        this.mListEntriesValue = listEntriesValue;
        this.mListEntries = listEntries;
        View view = LayoutInflater.from(ctx).inflate(R.layout.op_list_dialog_item_layout, null);
        this.mRootContainer = (RadioGroup) view.findViewById(R.id.radioGroup);
        this.mDialog = new Builder(this.mContext).setView(this.mRootContainer).setTitle(title).setView(view).setPositiveButton(R.string.okay, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (OPListDialog.this.mOnDialogListItemClickListener != null) {
                    OPListDialog.this.mOnDialogListItemClickListener.OnDialogListConfirmClick(OPListDialog.this.mCurrentIndex);
                }
                dialog.dismiss();
            }
        }).setNegativeButton(R.string.cancel, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (OPListDialog.this.mOnDialogListItemClickListener != null) {
                    OPListDialog.this.mOnDialogListItemClickListener.OnDialogListCancelClick();
                }
                dialog.dismiss();
            }
        }).create();
        this.mDialog.setCanceledOnTouchOutside(true);
        this.mDialog.setOnCancelListener(new OnCancelListener() {
            public void onCancel(DialogInterface dialogInterface) {
                if (OPListDialog.this.mOnDialogListItemClickListener != null) {
                    OPListDialog.this.mOnDialogListItemClickListener.OnDialogListCancelClick();
                }
            }
        });
    }

    public void setOnDialogListItemClickListener(OnDialogListItemClickListener onDialogListItemClickListener) {
        this.mOnDialogListItemClickListener = onDialogListItemClickListener;
    }

    public void setVibrateKey(String key) {
        if (OPUtils.isSupportXVibrate()) {
            this.mCurrentIndex = OPVibrateUtils.getRealXVibrateValueToIndex(System.getInt(this.mContext.getContentResolver(), key, 0));
        } else {
            this.mCurrentIndex = System.getInt(this.mContext.getContentResolver(), key, 0);
        }
        int groupSize = this.mListEntriesValue.length;
        for (int i = 0; i < groupSize; i++) {
            RadioButton radioButton = (RadioButton) this.mRootContainer.getChildAt(i);
            radioButton.setVisibility(0);
            radioButton.setText(this.mListEntries[i]);
            if (this.mCurrentIndex == i) {
                radioButton.setChecked(true);
            } else {
                radioButton.setChecked(false);
            }
            radioButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    int index = 0;
                    int viewId = v.getId();
                    if (R.id.item_1 == viewId) {
                        index = 0;
                    } else if (R.id.item_2 == viewId) {
                        index = 1;
                    } else if (R.id.item_3 == viewId) {
                        index = 2;
                    } else if (R.id.item_4 == viewId) {
                        index = 3;
                    } else if (R.id.item_5 == viewId) {
                        index = 4;
                    } else if (R.id.item_6 == viewId) {
                        index = 5;
                    }
                    OPListDialog.this.mCurrentIndex = index;
                    if (OPListDialog.this.mOnDialogListItemClickListener != null) {
                        OPListDialog.this.mOnDialogListItemClickListener.OnDialogListItemClick(index);
                    }
                }
            });
        }
    }

    public void setVibrateLevelKey(String key) {
        this.mCurrentIndex = System.getInt(this.mContext.getContentResolver(), key, 0);
        int groupSize = this.mListEntriesValue.length;
        for (int i = 0; i < groupSize; i++) {
            RadioButton radioButton = (RadioButton) this.mRootContainer.getChildAt(i);
            radioButton.setVisibility(0);
            radioButton.setText(this.mListEntries[i]);
            if (this.mCurrentIndex == i) {
                radioButton.setChecked(true);
            } else {
                radioButton.setChecked(false);
            }
            radioButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    int index = 0;
                    int viewId = v.getId();
                    if (R.id.item_1 == viewId) {
                        index = 0;
                    } else if (R.id.item_2 == viewId) {
                        index = 1;
                    } else if (R.id.item_3 == viewId) {
                        index = 2;
                    } else if (R.id.item_4 == viewId) {
                        index = 3;
                    } else if (R.id.item_5 == viewId) {
                        index = 4;
                    } else if (R.id.item_6 == viewId) {
                        index = 5;
                    }
                    OPListDialog.this.mCurrentIndex = index;
                    if (OPListDialog.this.mOnDialogListItemClickListener != null) {
                        OPListDialog.this.mOnDialogListItemClickListener.OnDialogListItemClick(index);
                    }
                }
            });
        }
    }

    public void setMessage(String msg) {
        this.mDialog.setMessage(msg);
    }

    public void setTitle(int resId) {
        this.mDialog.setTitle(resId);
    }

    public void setTitle(String title) {
        this.mDialog.setTitle(title);
    }

    public void show() {
        this.mDialog.show();
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public void setPositiveButton(String text, OnClickListener listener, int count) {
        this.mDialog.setButton(-1, text, listener);
    }

    public void setNegativeButton(String text, final OnClickListener listener, int count) {
        this.mDialog.setButton(-2, text, listener);
        this.mDialog.setOnCancelListener(new OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                listener.onClick(OPListDialog.this.mDialog, 2);
            }
        });
    }

    public void dismiss() {
        setStatus(false);
        if (this.mDialog != null) {
            try {
                this.mDialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setButtonType(int type, int count, boolean isDisable) {
        if (count > 0) {
            if (type == -1) {
                this.p = this.mDialog.getButton(-1);
                this.p.setEnabled(isDisable);
                this.mPositiveCount = count;
            } else if (type == -2) {
                this.n = this.mDialog.getButton(-2);
                this.n.setEnabled(isDisable);
                this.mNegativeCount = count;
            }
        }
    }

    public View getRootContainer() {
        return this.mRootContainer;
    }

    public Button getPButton() {
        if (this.p != null) {
            return this.p;
        }
        return null;
    }

    public Button getNButton() {
        if (this.n != null) {
            return this.n;
        }
        return null;
    }
}
