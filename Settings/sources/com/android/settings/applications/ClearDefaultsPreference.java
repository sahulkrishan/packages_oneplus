package com.android.settings.applications;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.usb.IUsbManager;
import android.hardware.usb.IUsbManager.Stub;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.support.v4.content.res.TypedArrayUtils;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.BulletSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settingslib.applications.AppUtils;
import com.android.settingslib.applications.ApplicationsState.AppEntry;

public class ClearDefaultsPreference extends Preference {
    protected static final String TAG = ClearDefaultsPreference.class.getSimpleName();
    private Button mActivitiesButton;
    protected AppEntry mAppEntry;
    private AppWidgetManager mAppWidgetManager;
    private String mPackageName;
    private PackageManager mPm;
    private IUsbManager mUsbManager;

    public ClearDefaultsPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setLayoutResource(R.layout.app_preferred_settings);
        this.mAppWidgetManager = AppWidgetManager.getInstance(context);
        this.mPm = context.getPackageManager();
        this.mUsbManager = Stub.asInterface(ServiceManager.getService("usb"));
    }

    public ClearDefaultsPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ClearDefaultsPreference(Context context, AttributeSet attrs) {
        this(context, attrs, TypedArrayUtils.getAttr(context, R.attr.preferenceStyle, 16842894));
    }

    public ClearDefaultsPreference(Context context) {
        this(context, null);
    }

    public void setPackageName(String packageName) {
        this.mPackageName = packageName;
    }

    public void setAppEntry(AppEntry entry) {
        this.mAppEntry = entry;
    }

    public void onBindViewHolder(final PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        this.mActivitiesButton = (Button) view.findViewById(R.id.clear_activities_button);
        this.mActivitiesButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (ClearDefaultsPreference.this.mUsbManager != null) {
                    int userId = UserHandle.myUserId();
                    ClearDefaultsPreference.this.mPm.clearPackagePreferredActivities(ClearDefaultsPreference.this.mPackageName);
                    if (ClearDefaultsPreference.this.isDefaultBrowser(ClearDefaultsPreference.this.mPackageName)) {
                        ClearDefaultsPreference.this.mPm.setDefaultBrowserPackageNameAsUser(null, userId);
                    }
                    try {
                        ClearDefaultsPreference.this.mUsbManager.clearDefaults(ClearDefaultsPreference.this.mPackageName, userId);
                    } catch (RemoteException e) {
                        Log.e(ClearDefaultsPreference.TAG, "mUsbManager.clearDefaults", e);
                    }
                    ClearDefaultsPreference.this.mAppWidgetManager.setBindAppWidgetPermission(ClearDefaultsPreference.this.mPackageName, false);
                    ClearDefaultsPreference.this.resetLaunchDefaultsUi((TextView) view.findViewById(R.id.auto_launch));
                }
            }
        });
        updateUI(view);
    }

    public boolean updateUI(PreferenceViewHolder view) {
        boolean hasBindAppWidgetPermission = this.mAppWidgetManager.hasBindAppWidgetPermission(this.mAppEntry.info.packageName);
        TextView autoLaunchView = (TextView) view.findViewById(R.id.auto_launch);
        boolean autoLaunchEnabled = AppUtils.hasPreferredActivities(this.mPm, this.mPackageName) || isDefaultBrowser(this.mPackageName) || AppUtils.hasUsbDefaults(this.mUsbManager, this.mPackageName);
        if (autoLaunchEnabled || hasBindAppWidgetPermission) {
            boolean useBullets = hasBindAppWidgetPermission && autoLaunchEnabled;
            if (hasBindAppWidgetPermission) {
                autoLaunchView.setText(R.string.auto_launch_label_generic);
            } else {
                autoLaunchView.setText(R.string.auto_launch_label);
            }
            Context context = getContext();
            CharSequence text = null;
            int bulletIndent = context.getResources().getDimensionPixelSize(R.dimen.installed_app_details_bullet_offset);
            if (autoLaunchEnabled) {
                CharSequence autoLaunchEnableText = context.getText(R.string.auto_launch_enable_text);
                SpannableString s = new SpannableString(autoLaunchEnableText);
                if (useBullets) {
                    s.setSpan(new BulletSpan(bulletIndent), 0, autoLaunchEnableText.length(), 0);
                }
                text = null == null ? TextUtils.concat(new CharSequence[]{s, "\n"}) : TextUtils.concat(new CharSequence[]{null, "\n", s, "\n"});
            }
            if (hasBindAppWidgetPermission) {
                CharSequence alwaysAllowBindAppWidgetsText = context.getText(R.string.always_allow_bind_appwidgets_text);
                SpannableString s2 = new SpannableString(alwaysAllowBindAppWidgetsText);
                if (useBullets) {
                    s2.setSpan(new BulletSpan(bulletIndent), 0, alwaysAllowBindAppWidgetsText.length(), 0);
                }
                text = text == null ? TextUtils.concat(new CharSequence[]{s2, "\n"}) : TextUtils.concat(new CharSequence[]{text, "\n", s2, "\n"});
            }
            autoLaunchView.setText(text);
            this.mActivitiesButton.setEnabled(true);
        } else {
            resetLaunchDefaultsUi(autoLaunchView);
        }
        return true;
    }

    private boolean isDefaultBrowser(String packageName) {
        return packageName.equals(this.mPm.getDefaultBrowserPackageNameAsUser(UserHandle.myUserId()));
    }

    private void resetLaunchDefaultsUi(TextView autoLaunchView) {
        autoLaunchView.setText(R.string.auto_launch_disable_text);
        this.mActivitiesButton.setEnabled(false);
    }
}
