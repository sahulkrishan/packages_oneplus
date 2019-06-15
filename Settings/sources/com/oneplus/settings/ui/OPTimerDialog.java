package com.oneplus.settings.ui;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.os.Handler;
import android.os.Message;
import android.widget.Button;

public class OPTimerDialog {
    private static final int TYPE_NEGATIVE = 2;
    private static final int TYPE_POSITIVE = 1;
    private Context mContext;
    private AlertDialog mDialog = null;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (OPTimerDialog.this.mPositiveCount > 0) {
                        OPTimerDialog.this.mPositiveCount = OPTimerDialog.this.mPositiveCount - 1;
                        if (OPTimerDialog.this.p != null) {
                            OPTimerDialog.this.p.getText();
                            OPTimerDialog oPTimerDialog = OPTimerDialog.this;
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append("已经达到定时关机时间,");
                            stringBuilder.append(String.valueOf(OPTimerDialog.this.mPositiveCount));
                            stringBuilder.append("s后确认关机?");
                            oPTimerDialog.setMessage(stringBuilder.toString());
                        }
                        if (OPTimerDialog.this.mHandler != null) {
                            OPTimerDialog.this.mHandler.sendEmptyMessageDelayed(1, 1000);
                            return;
                        }
                        return;
                    } else if (OPTimerDialog.this.p != null && OPTimerDialog.this.status) {
                        if (OPTimerDialog.this.p.isEnabled()) {
                            OPTimerDialog.this.p.performClick();
                            return;
                        } else {
                            OPTimerDialog.this.p.setEnabled(true);
                            return;
                        }
                    } else {
                        return;
                    }
                case 2:
                    if (OPTimerDialog.this.mNegativeCount > 0) {
                        OPTimerDialog.this.mNegativeCount = OPTimerDialog.this.mNegativeCount - 1;
                        if (OPTimerDialog.this.n != null) {
                            OPTimerDialog.this.n.setText(OPTimerDialog.this.getTimeText((String) OPTimerDialog.this.n.getText(), OPTimerDialog.this.mNegativeCount));
                        }
                        OPTimerDialog.this.mHandler.sendEmptyMessageDelayed(2, 1000);
                        return;
                    } else if (OPTimerDialog.this.n == null) {
                        return;
                    } else {
                        if (OPTimerDialog.this.n.isEnabled()) {
                            OPTimerDialog.this.n.performClick();
                            return;
                        } else {
                            OPTimerDialog.this.n.setEnabled(true);
                            return;
                        }
                    }
                default:
                    return;
            }
        }
    };
    private int mNegativeCount = 0;
    private int mPositiveCount = 0;
    private Button n = null;
    private Button p = null;
    private boolean status = true;

    public OPTimerDialog(Context ctx) {
        this.mContext = ctx;
        this.mDialog = new Builder(this.mContext).setCancelable(false).create();
        this.mDialog.getWindow().setType(2003);
        this.mDialog.getWindow().setType(2009);
        this.mDialog.setCanceledOnTouchOutside(false);
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
                listener.onClick(OPTimerDialog.this.mDialog, 2);
            }
        });
    }

    public void dismiss() {
        setStatus(false);
        if (this.mHandler != null) {
            this.mHandler = null;
        }
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

    public String getTimeText(String text, int count) {
        if (text == null || text.length() <= 0 || count <= 0) {
            return text;
        }
        int index = text.indexOf("(");
        StringBuilder stringBuilder;
        if (index > 0) {
            text = text.substring(0, index);
            stringBuilder = new StringBuilder();
            stringBuilder.append(text);
            stringBuilder.append("(");
            stringBuilder.append(count);
            stringBuilder.append("s)");
            return stringBuilder.toString();
        }
        stringBuilder = new StringBuilder();
        stringBuilder.append(text);
        stringBuilder.append("(");
        stringBuilder.append(count);
        stringBuilder.append("s)");
        return stringBuilder.toString();
    }

    public boolean isShowing() {
        if (this.mDialog != null) {
            return this.mDialog.isShowing();
        }
        return false;
    }
}
