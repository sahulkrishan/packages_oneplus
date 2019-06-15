package com.android.settings.datausage;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.ContentObserver;
import android.net.NetworkTemplate;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.provider.Settings.Global;
import android.support.annotation.VisibleForTesting;
import android.support.v4.content.res.TypedArrayUtils;
import android.support.v7.preference.AndroidResources;
import android.support.v7.preference.Preference.BaseSavedState;
import android.support.v7.preference.PreferenceViewHolder;
import android.telephony.SubscriptionManager;
import android.telephony.SubscriptionManager.OnSubscriptionsChangedListener;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import com.android.settings.R;
import com.android.settings.datausage.TemplatePreference.NetworkServices;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.CustomDialogPreference;

public class CellDataPreference extends CustomDialogPreference implements TemplatePreference {
    private static final String TAG = "CellDataPreference";
    public boolean mChecked;
    private final DataStateListener mDataStateListener = new DataStateListener() {
        public void onChange(boolean selfChange) {
            CellDataPreference.this.updateChecked();
        }
    };
    @VisibleForTesting
    final OnSubscriptionsChangedListener mOnSubscriptionsChangeListener = new OnSubscriptionsChangedListener() {
        public void onSubscriptionsChanged() {
            CellDataPreference.this.updateEnabled();
        }
    };
    public int mSubId = -1;
    @VisibleForTesting
    SubscriptionManager mSubscriptionManager;
    private TelephonyManager mTelephonyManager;

    public static abstract class DataStateListener extends ContentObserver {
        public DataStateListener() {
            super(new Handler(Looper.getMainLooper()));
        }

        public void setListener(boolean listening, int subId, Context context) {
            if (listening) {
                Uri uri = Global.getUriFor("mobile_data");
                if (TelephonyManager.getDefault().getSimCount() != 1) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("mobile_data");
                    stringBuilder.append(subId);
                    uri = Global.getUriFor(stringBuilder.toString());
                }
                context.getContentResolver().registerContentObserver(uri, false, this);
                return;
            }
            context.getContentResolver().unregisterContentObserver(this);
        }
    }

    public static class CellDataState extends BaseSavedState {
        public static final Creator<CellDataState> CREATOR = new Creator<CellDataState>() {
            public CellDataState createFromParcel(Parcel source) {
                return new CellDataState(source);
            }

            public CellDataState[] newArray(int size) {
                return new CellDataState[size];
            }
        };
        public boolean mChecked;
        public int mSubId;

        public CellDataState(Parcelable base) {
            super(base);
        }

        public CellDataState(Parcel source) {
            super(source);
            this.mChecked = source.readByte() != (byte) 0;
            this.mSubId = source.readInt();
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeByte((byte) this.mChecked);
            dest.writeInt(this.mSubId);
        }
    }

    public CellDataPreference(Context context, AttributeSet attrs) {
        super(context, attrs, TypedArrayUtils.getAttr(context, R.attr.switchPreferenceStyle, 16843629));
    }

    /* Access modifiers changed, original: protected */
    public void onRestoreInstanceState(Parcelable s) {
        CellDataState state = (CellDataState) s;
        super.onRestoreInstanceState(state.getSuperState());
        this.mTelephonyManager = TelephonyManager.from(getContext());
        this.mChecked = state.mChecked;
        if (this.mSubId == -1) {
            this.mSubId = state.mSubId;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(getKey());
            stringBuilder.append(this.mSubId);
            setKey(stringBuilder.toString());
        }
        notifyChanged();
    }

    /* Access modifiers changed, original: protected */
    public Parcelable onSaveInstanceState() {
        CellDataState state = new CellDataState(super.onSaveInstanceState());
        state.mChecked = this.mChecked;
        state.mSubId = this.mSubId;
        return state;
    }

    public void onAttached() {
        super.onAttached();
        this.mDataStateListener.setListener(true, this.mSubId, getContext());
        if (this.mSubscriptionManager != null) {
            this.mSubscriptionManager.addOnSubscriptionsChangedListener(this.mOnSubscriptionsChangeListener);
        }
    }

    public void onDetached() {
        this.mDataStateListener.setListener(false, this.mSubId, getContext());
        if (this.mSubscriptionManager != null) {
            this.mSubscriptionManager.removeOnSubscriptionsChangedListener(this.mOnSubscriptionsChangeListener);
        }
        super.onDetached();
    }

    public void setTemplate(NetworkTemplate template, int subId, NetworkServices services) {
        if (subId != -1) {
            this.mSubscriptionManager = SubscriptionManager.from(getContext());
            this.mTelephonyManager = TelephonyManager.from(getContext());
            this.mSubscriptionManager.addOnSubscriptionsChangedListener(this.mOnSubscriptionsChangeListener);
            if (this.mSubId == -1) {
                this.mSubId = subId;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(getKey());
                stringBuilder.append(subId);
                setKey(stringBuilder.toString());
            }
            updateEnabled();
            updateChecked();
            return;
        }
        throw new IllegalArgumentException("CellDataPreference needs a SubscriptionInfo");
    }

    private void updateChecked() {
        setChecked(this.mTelephonyManager.getDataEnabled(this.mSubId));
    }

    private void updateEnabled() {
        setEnabled(this.mSubscriptionManager.getActiveSubscriptionInfo(this.mSubId) != null);
    }

    /* Access modifiers changed, original: protected */
    public void performClick(View view) {
        Context context = getContext();
        FeatureFactory.getFactory(context).getMetricsFeatureProvider().action(context, 178, this.mChecked ^ 1);
        if (this.mChecked) {
            super.performClick(view);
        } else {
            setMobileDataEnabled(true);
        }
    }

    private void setMobileDataEnabled(boolean enabled) {
        this.mTelephonyManager.setDataEnabled(this.mSubId, enabled);
        setChecked(enabled);
    }

    private void setChecked(boolean checked) {
        if (this.mChecked != checked) {
            this.mChecked = checked;
            notifyChanged();
        }
    }

    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        View switchView = holder.findViewById(AndroidResources.ANDROID_R_SWITCH_WIDGET);
        switchView.setClickable(false);
        ((Checkable) switchView).setChecked(this.mChecked);
    }

    /* Access modifiers changed, original: protected */
    public void onPrepareDialogBuilder(Builder builder, OnClickListener listener) {
        showDisableDialog(builder, listener);
    }

    private void showDisableDialog(Builder builder, OnClickListener listener) {
        builder.setTitle(null).setMessage(R.string.data_usage_disable_mobile).setPositiveButton(17039370, listener).setNegativeButton(17039360, null);
    }

    /* Access modifiers changed, original: protected */
    public void onClick(DialogInterface dialog, int which) {
        if (which == -1) {
            setMobileDataEnabled(false);
        }
    }
}
