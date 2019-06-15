package com.android.settings.widget;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.view.PointerIconCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.IconDrawableFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.applications.AppInfoBase;
import com.android.settings.applications.LayoutPreference;
import com.android.settings.applications.appinfo.AppInfoDashboardFragment;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.applications.ApplicationsState.AppEntry;
import com.android.settingslib.core.lifecycle.Lifecycle;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class EntityHeaderController {
    public static final String PREF_KEY_APP_HEADER = "pref_app_header";
    private static final String TAG = "AppDetailFeature";
    private int mAction1;
    private int mAction2;
    private final Activity mActivity;
    private final Context mAppContext;
    private Intent mAppNotifPrefIntent;
    private OnClickListener mEditRuleNameOnClickListener;
    private final Fragment mFragment;
    private boolean mHasAppInfoLink;
    private final View mHeader;
    private Drawable mIcon;
    private String mIconContentDescription;
    private boolean mIsInstantApp;
    private CharSequence mLabel;
    private Lifecycle mLifecycle;
    private final int mMetricsCategory;
    private String mPackageName;
    private RecyclerView mRecyclerView;
    private CharSequence mSecondSummary;
    private CharSequence mSummary;
    private int mUid = -10000;

    @Retention(RetentionPolicy.SOURCE)
    public @interface ActionType {
        public static final int ACTION_DND_RULE_PREFERENCE = 2;
        public static final int ACTION_NONE = 0;
        public static final int ACTION_NOTIF_PREFERENCE = 1;
    }

    public static EntityHeaderController newInstance(Activity activity, Fragment fragment, View header) {
        return new EntityHeaderController(activity, fragment, header);
    }

    private EntityHeaderController(Activity activity, Fragment fragment, View header) {
        this.mActivity = activity;
        this.mAppContext = activity.getApplicationContext();
        this.mFragment = fragment;
        this.mMetricsCategory = FeatureFactory.getFactory(this.mAppContext).getMetricsFeatureProvider().getMetricsCategory(fragment);
        if (header != null) {
            this.mHeader = header;
        } else {
            this.mHeader = LayoutInflater.from(fragment.getContext()).inflate(R.layout.settings_entity_header, null);
        }
    }

    public EntityHeaderController setRecyclerView(RecyclerView recyclerView, Lifecycle lifecycle) {
        this.mRecyclerView = recyclerView;
        this.mLifecycle = lifecycle;
        return this;
    }

    public EntityHeaderController setIcon(Drawable icon) {
        if (icon != null) {
            this.mIcon = icon.getConstantState().newDrawable(this.mAppContext.getResources());
        }
        return this;
    }

    public EntityHeaderController setIcon(AppEntry appEntry) {
        this.mIcon = IconDrawableFactory.newInstance(this.mAppContext).getBadgedIcon(appEntry.info);
        return this;
    }

    public EntityHeaderController setIconContentDescription(String contentDescription) {
        this.mIconContentDescription = contentDescription;
        return this;
    }

    public EntityHeaderController setLabel(CharSequence label) {
        this.mLabel = label;
        return this;
    }

    public EntityHeaderController setLabel(AppEntry appEntry) {
        this.mLabel = appEntry.label;
        return this;
    }

    public EntityHeaderController setSummary(CharSequence summary) {
        this.mSummary = summary;
        return this;
    }

    public EntityHeaderController setSummary(PackageInfo packageInfo) {
        if (packageInfo != null) {
            this.mSummary = packageInfo.versionName;
        }
        return this;
    }

    public EntityHeaderController setSecondSummary(CharSequence summary) {
        this.mSecondSummary = summary;
        return this;
    }

    public EntityHeaderController setSecondSummary(PackageInfo packageInfo) {
        if (packageInfo != null) {
            this.mSummary = packageInfo.versionName;
        }
        return this;
    }

    public EntityHeaderController setHasAppInfoLink(boolean hasAppInfoLink) {
        this.mHasAppInfoLink = hasAppInfoLink;
        return this;
    }

    public EntityHeaderController setButtonActions(int action1, int action2) {
        this.mAction1 = action1;
        this.mAction2 = action2;
        return this;
    }

    public EntityHeaderController setPackageName(String packageName) {
        this.mPackageName = packageName;
        return this;
    }

    public EntityHeaderController setUid(int uid) {
        this.mUid = uid;
        return this;
    }

    public EntityHeaderController setAppNotifPrefIntent(Intent appNotifPrefIntent) {
        this.mAppNotifPrefIntent = appNotifPrefIntent;
        return this;
    }

    public EntityHeaderController setIsInstantApp(boolean isInstantApp) {
        this.mIsInstantApp = isInstantApp;
        return this;
    }

    public EntityHeaderController setEditZenRuleNameListener(OnClickListener listener) {
        this.mEditRuleNameOnClickListener = listener;
        return this;
    }

    public LayoutPreference done(Activity activity, Context uiContext) {
        LayoutPreference pref = new LayoutPreference(uiContext, done(activity));
        pref.setOrder(NotificationManagerCompat.IMPORTANCE_UNSPECIFIED);
        pref.setSelectable(false);
        pref.setKey(PREF_KEY_APP_HEADER);
        return pref;
    }

    public View done(Activity activity, boolean rebindActions) {
        styleActionBar(activity);
        ImageView iconView = (ImageView) this.mHeader.findViewById(R.id.entity_header_icon);
        if (iconView != null) {
            iconView.setImageDrawable(this.mIcon);
            iconView.setContentDescription(this.mIconContentDescription);
        }
        setText(R.id.entity_header_title, this.mLabel);
        setText(R.id.entity_header_summary, this.mSummary);
        setText(R.id.entity_header_second_summary, this.mSecondSummary);
        if (this.mIsInstantApp) {
            setText(R.id.install_type, this.mHeader.getResources().getString(R.string.install_type_instant));
        }
        if (rebindActions) {
            bindHeaderButtons();
        }
        return this.mHeader;
    }

    public EntityHeaderController bindHeaderButtons() {
        ImageButton button1 = (ImageButton) this.mHeader.findViewById(16908313);
        ImageButton button2 = (ImageButton) this.mHeader.findViewById(16908314);
        bindAppInfoLink(this.mHeader.findViewById(R.id.entity_header_content));
        bindButton(button1, this.mAction1);
        bindButton(button2, this.mAction2);
        return this;
    }

    private void bindAppInfoLink(View entityHeaderContent) {
        if (!this.mHasAppInfoLink) {
            return;
        }
        if (entityHeaderContent == null || this.mPackageName == null || this.mPackageName.equals(Utils.OS_PKG) || this.mUid == -10000) {
            Log.w(TAG, "Missing ingredients to build app info link, skip");
        } else {
            entityHeaderContent.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    AppInfoBase.startAppInfoFragment(AppInfoDashboardFragment.class, R.string.application_info_label, EntityHeaderController.this.mPackageName, EntityHeaderController.this.mUid, EntityHeaderController.this.mFragment, 0, EntityHeaderController.this.mMetricsCategory);
                }
            });
        }
    }

    public EntityHeaderController styleActionBar(Activity activity) {
        if (activity == null) {
            Log.w(TAG, "No activity, cannot style actionbar.");
            return this;
        }
        ActionBar actionBar = activity.getActionBar();
        if (actionBar == null) {
            Log.w(TAG, "No actionbar, cannot style actionbar.");
            return this;
        }
        actionBar.setBackgroundDrawable(new ColorDrawable(com.android.settingslib.Utils.getColorAttr(activity, 16843827)));
        actionBar.setElevation(0.0f);
        if (!(this.mRecyclerView == null || this.mLifecycle == null)) {
            ActionBarShadowController.attachToRecyclerView(this.mActivity, this.mLifecycle, this.mRecyclerView);
        }
        return this;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public View done(Activity activity) {
        return done(activity, true);
    }

    private void bindButton(ImageButton button, int action) {
        if (button != null) {
            switch (action) {
                case 0:
                    button.setVisibility(8);
                    return;
                case 1:
                    if (this.mAppNotifPrefIntent == null) {
                        button.setVisibility(8);
                    } else {
                        button.setOnClickListener(new OnClickListener() {
                            public void onClick(View v) {
                                FeatureFactory.getFactory(EntityHeaderController.this.mAppContext).getMetricsFeatureProvider().actionWithSource(EntityHeaderController.this.mAppContext, EntityHeaderController.this.mMetricsCategory, PointerIconCompat.TYPE_TOP_RIGHT_DIAGONAL_DOUBLE_ARROW);
                                EntityHeaderController.this.mFragment.startActivity(EntityHeaderController.this.mAppNotifPrefIntent);
                            }
                        });
                        button.setVisibility(0);
                    }
                    return;
                case 2:
                    if (this.mEditRuleNameOnClickListener == null) {
                        button.setVisibility(8);
                    } else {
                        button.setImageResource(R.drawable.ic_mode_edit);
                        button.setVisibility(0);
                        button.setOnClickListener(this.mEditRuleNameOnClickListener);
                    }
                    return;
                default:
                    return;
            }
        }
    }

    private void setText(int id, CharSequence text) {
        TextView textView = (TextView) this.mHeader.findViewById(id);
        if (textView != null) {
            textView.setText(text);
            textView.setVisibility(TextUtils.isEmpty(text) ? 8 : 0);
        }
    }
}
