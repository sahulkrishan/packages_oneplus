package com.oneplus.lib.util.loading;

import android.app.Dialog;

public class DialogLoadingHelper extends LoadingHelper {
    Dialog mDialog;

    public DialogLoadingHelper(Dialog dialog) {
        this.mDialog = dialog;
    }

    /* Access modifiers changed, original: protected */
    public Object showProgree() {
        if (this.mDialog != null) {
            try {
                this.mDialog.show();
            } catch (Throwable th) {
            }
        }
        return this.mDialog;
    }

    /* Access modifiers changed, original: protected */
    public void hideProgree(Object progreeView) {
        if (this.mDialog != null) {
            try {
                this.mDialog.dismiss();
            } catch (Throwable th) {
            }
        }
    }
}