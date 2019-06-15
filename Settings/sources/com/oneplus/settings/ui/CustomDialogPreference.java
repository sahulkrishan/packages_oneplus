package com.oneplus.settings.ui;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.BaseSavedState;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import com.android.internal.R;

public abstract class CustomDialogPreference extends Preference implements OnClickListener, OnDismissListener {
    private Builder mBuilder;
    private Dialog mDialog;
    private int mDialogLayoutResId;
    private CharSequence mNegativeButtonText;
    private CharSequence mNeutralButtonText;
    private CharSequence mPositiveButtonText;
    private int mWhichButtonClicked;

    private static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        Bundle dialogBundle;
        boolean isDialogShowing;

        public SavedState(Parcel source) {
            super(source);
            boolean z = true;
            if (source.readInt() != 1) {
                z = false;
            }
            this.isDialogShowing = z;
            this.dialogBundle = source.readBundle();
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.isDialogShowing);
            dest.writeBundle(this.dialogBundle);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }
    }

    public CustomDialogPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DialogPreference, defStyleAttr, defStyleRes);
        this.mPositiveButtonText = a.getString(3);
        this.mNegativeButtonText = a.getString(4);
        this.mDialogLayoutResId = a.getResourceId(5, this.mDialogLayoutResId);
        a.recycle();
    }

    public CustomDialogPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public CustomDialogPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 16842897);
    }

    public CustomDialogPreference(Context context) {
        this(context, null);
    }

    public void setPositiveButtonText(CharSequence positiveButtonText) {
        this.mPositiveButtonText = positiveButtonText;
    }

    public void setPositiveButtonText(int positiveButtonTextResId) {
        setPositiveButtonText(getContext().getString(positiveButtonTextResId));
    }

    public CharSequence getPositiveButtonText() {
        return this.mPositiveButtonText;
    }

    public void setNegativeButtonText(CharSequence negativeButtonText) {
        this.mNegativeButtonText = negativeButtonText;
    }

    public void setNegativeButtonText(int negativeButtonTextResId) {
        setNegativeButtonText(getContext().getString(negativeButtonTextResId));
    }

    public CharSequence getNegativeButtonText() {
        return this.mNegativeButtonText;
    }

    public void setNeutralButtonText(CharSequence neutralButtonText) {
        this.mNeutralButtonText = neutralButtonText;
    }

    public void setNeutralButtonText(int neutralButtonTextResId) {
        setNeutralButtonText(getContext().getString(neutralButtonTextResId));
    }

    public CharSequence getNeutralButtonText() {
        return this.mNeutralButtonText;
    }

    public void setDialogLayoutResource(int dialogLayoutResId) {
        this.mDialogLayoutResId = dialogLayoutResId;
    }

    public int getDialogLayoutResource() {
        return this.mDialogLayoutResId;
    }

    /* Access modifiers changed, original: protected */
    public void onPrepareDialogBuilder(Builder builder) {
    }

    /* Access modifiers changed, original: protected */
    public void onClick() {
        if (this.mDialog == null || !this.mDialog.isShowing()) {
            showDialog(null);
        }
    }

    /* Access modifiers changed, original: protected */
    public void showDialog(Bundle state) {
        Context context = getContext();
        this.mWhichButtonClicked = -2;
        this.mBuilder = new Builder(context, com.android.settings.R.style.f908Theme.AlertDialog);
        if (this.mPositiveButtonText != null) {
            this.mBuilder.setPositiveButton(this.mPositiveButtonText, this);
        }
        if (this.mNegativeButtonText != null) {
            this.mBuilder.setNegativeButton(this.mNegativeButtonText, this);
        }
        if (this.mNeutralButtonText != null) {
            this.mBuilder.setNeutralButton(this.mNeutralButtonText, this);
        }
        View contentView = onCreateDialogView();
        if (contentView != null) {
            onBindDialogView(contentView);
            this.mBuilder.setView(contentView);
        }
        onPrepareDialogBuilder(this.mBuilder);
        Dialog dialog = this.mBuilder.create();
        this.mDialog = dialog;
        if (state != null) {
            dialog.onRestoreInstanceState(state);
        }
        dialog.setOnDismissListener(this);
        dialog.show();
    }

    /* Access modifiers changed, original: protected */
    public View onCreateDialogView() {
        if (this.mDialogLayoutResId == 0) {
            return null;
        }
        return LayoutInflater.from(this.mBuilder.getContext()).inflate(this.mDialogLayoutResId, null);
    }

    /* Access modifiers changed, original: protected */
    public void onBindDialogView(View view) {
    }

    public void onClick(DialogInterface dialog, int which) {
        this.mWhichButtonClicked = which;
    }

    public void onDismiss(DialogInterface dialog) {
        this.mDialog = null;
        onDialogClosed(this.mWhichButtonClicked);
    }

    /* Access modifiers changed, original: protected */
    public void onDialogClosed(int whichButton) {
    }

    public Dialog getDialog() {
        return this.mDialog;
    }

    public void onActivityDestroy() {
        if (this.mDialog != null && this.mDialog.isShowing()) {
            this.mDialog.dismiss();
        }
    }

    /* Access modifiers changed, original: protected */
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        if (this.mDialog == null || !this.mDialog.isShowing()) {
            return superState;
        }
        SavedState myState = new SavedState(superState);
        myState.isDialogShowing = true;
        myState.dialogBundle = this.mDialog.onSaveInstanceState();
        return myState;
    }

    /* Access modifiers changed, original: protected */
    public void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        if (myState.isDialogShowing) {
            showDialog(myState.dialogBundle);
        }
    }
}
