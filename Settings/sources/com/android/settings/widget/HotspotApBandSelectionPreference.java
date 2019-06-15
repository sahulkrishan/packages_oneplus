package com.android.settings.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference.BaseSavedState;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import com.android.settings.R;
import com.android.settingslib.CustomDialogPreference;
import java.util.ArrayList;

public class HotspotApBandSelectionPreference extends CustomDialogPreference implements OnCheckedChangeListener, OnShowListener {
    @VisibleForTesting
    static final String KEY_CHECKED_BANDS = "checked_bands";
    @VisibleForTesting
    static final String KEY_HOTSPOT_SUPER_STATE = "hotspot_super_state";
    private static final int UNSET = Integer.MIN_VALUE;
    private String[] mBandEntries;
    @VisibleForTesting
    CheckBox mBox2G;
    @VisibleForTesting
    CheckBox mBox5G;
    private int mExistingConfigValue = Integer.MIN_VALUE;
    @VisibleForTesting
    ArrayList<Integer> mRestoredBands;
    @VisibleForTesting
    boolean mShouldRestore;

    private static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        boolean enabled2G;
        boolean enabled5G;
        boolean shouldRestore;

        public SavedState(Parcelable source) {
            super(source);
        }

        private SavedState(Parcel in) {
            super(in);
            boolean z = false;
            this.shouldRestore = in.readByte() == (byte) 1;
            this.enabled2G = in.readByte() == (byte) 1;
            if (in.readByte() == (byte) 1) {
                z = true;
            }
            this.enabled5G = z;
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeByte((byte) this.shouldRestore);
            dest.writeByte((byte) this.enabled2G);
            dest.writeByte((byte) this.enabled5G);
        }

        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("HotspotApBandSelectionPreference.SavedState{");
            stringBuilder.append(Integer.toHexString(System.identityHashCode(this)));
            stringBuilder.append(" shouldRestore=");
            stringBuilder.append(this.shouldRestore);
            stringBuilder.append(" enabled2G=");
            stringBuilder.append(this.enabled2G);
            stringBuilder.append(" enabled5G=");
            stringBuilder.append(this.enabled5G);
            stringBuilder.append("}");
            return stringBuilder.toString();
        }
    }

    public HotspotApBandSelectionPreference(Context context) {
        super(context);
    }

    public HotspotApBandSelectionPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HotspotApBandSelectionPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public HotspotApBandSelectionPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /* Access modifiers changed, original: protected */
    public void onRestoreInstanceState(Parcelable state) {
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        this.mShouldRestore = myState.shouldRestore;
        if (this.mShouldRestore) {
            this.mRestoredBands = new ArrayList();
            if (myState.enabled2G) {
                this.mRestoredBands.add(Integer.valueOf(0));
            }
            if (myState.enabled5G) {
                this.mRestoredBands.add(Integer.valueOf(1));
            }
        } else {
            this.mRestoredBands = null;
        }
        updatePositiveButton();
    }

    /* Access modifiers changed, original: protected */
    public void onBindDialogView(View view) {
        super.onBindDialogView(view);
        Context context = getContext();
        setOnShowListener(this);
        this.mBandEntries = context.getResources().getStringArray(R.array.wifi_ap_band_config_full);
        addApBandViews((LinearLayout) view);
        updatePositiveButton();
        this.mRestoredBands = null;
        this.mShouldRestore = false;
    }

    /* Access modifiers changed, original: protected */
    public Parcelable onSaveInstanceState() {
        SavedState myState = new SavedState(super.onSaveInstanceState());
        boolean z = false;
        myState.shouldRestore = getDialog() != null;
        boolean z2 = this.mBox2G != null && this.mBox2G.isChecked();
        myState.enabled2G = z2;
        if (this.mBox5G != null && this.mBox5G.isChecked()) {
            z = true;
        }
        myState.enabled5G = z;
        return myState;
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView instanceof CheckBox) {
            updatePositiveButton();
        }
    }

    /* Access modifiers changed, original: protected */
    public void onClick() {
        if (!isDialogOpen()) {
            super.onClick();
        }
    }

    /* Access modifiers changed, original: protected */
    public void onClick(DialogInterface dialog, int which) {
        if (which != -1) {
            return;
        }
        if (this.mBox2G.isChecked() || this.mBox5G.isChecked()) {
            int wifiBand = getWifiBand();
            this.mExistingConfigValue = wifiBand;
            callChangeListener(Integer.valueOf(wifiBand));
        }
    }

    public void setExistingConfigValue(int band) {
        this.mExistingConfigValue = band;
    }

    private void addApBandViews(LinearLayout view) {
        this.mBox2G = (CheckBox) view.findViewById(R.id.box_2g);
        this.mBox2G.setText(this.mBandEntries[0]);
        this.mBox2G.setChecked(restoreBandIfNeeded(0));
        this.mBox2G.setOnCheckedChangeListener(this);
        this.mBox5G = (CheckBox) view.findViewById(R.id.box_5g);
        this.mBox5G.setText(this.mBandEntries[1]);
        this.mBox5G.setChecked(restoreBandIfNeeded(1));
        this.mBox5G.setOnCheckedChangeListener(this);
    }

    private boolean restoreBandIfNeeded(int band) {
        return (isBandPreviouslySelected(band) && !this.mShouldRestore) || (this.mShouldRestore && this.mRestoredBands.contains(Integer.valueOf(band)));
    }

    private void updatePositiveButton() {
        AlertDialog dialog = (AlertDialog) getDialog();
        Button button = dialog == null ? null : dialog.getButton(-1);
        if (button != null && this.mBox5G != null && this.mBox2G != null) {
            boolean z = this.mBox2G.isChecked() || this.mBox5G.isChecked();
            button.setEnabled(z);
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public int getWifiBand() {
        boolean checked_2g = this.mBox2G.isChecked();
        boolean checked_5g = this.mBox5G.isChecked();
        if (checked_2g && checked_5g) {
            return -1;
        }
        if (checked_2g && !checked_5g) {
            return 0;
        }
        if (checked_5g && !checked_2g) {
            return 1;
        }
        throw new IllegalStateException("Wifi Config only supports selecting one or all bands");
    }

    private boolean isBandPreviouslySelected(int bandIndex) {
        boolean z = false;
        switch (this.mExistingConfigValue) {
            case -1:
                return true;
            case 0:
                if (bandIndex == 0) {
                    z = true;
                }
                return z;
            case 1:
                if (bandIndex == 1) {
                    z = true;
                }
                return z;
            default:
                return false;
        }
    }

    public void onShow(DialogInterface dialog) {
        updatePositiveButton();
    }
}
