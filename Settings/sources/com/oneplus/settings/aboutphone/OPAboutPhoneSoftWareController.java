package com.oneplus.settings.aboutphone;

import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;
import android.os.UserManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.deviceinfo.HardwareInfoDialogFragment;
import com.android.settings.deviceinfo.firmwareversion.FirmwareVersionDialogFragment;
import com.android.settings.password.ChooseLockSettingsHelper;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.android.settingslib.development.DevelopmentSettingsEnabler;

public class OPAboutPhoneSoftWareController {
    private static final int ACTIVITY_TRIGGER_COUNT = 3;
    private static final int DELAY_TIMER_MILLIS = 500;
    static final int REQUEST_CONFIRM_PASSWORD_FOR_DEV_PREF = 100;
    static final int TAPS_TO_BE_A_DEVELOPER = 7;
    private String TAG = "OPAboutPhoneSoftWareController";
    private Activity mActivity;
    private Context mContext;
    private EnforcedAdmin mDebuggingFeaturesDisallowedAdmin;
    private boolean mDebuggingFeaturesDisallowedBySystem;
    private int mDevHitCountdown;
    private Toast mDevHitToast;
    private Fragment mFragment;
    private EnforcedAdmin mFunDisallowedAdmin;
    private boolean mFunDisallowedBySystem;
    private final long[] mHits = new long[3];
    private Drawable mLeftIcon;
    private String mLeftIntent;
    private String mLeftSummary;
    private String mLeftTitle;
    public boolean mProcessingLastDevHit;
    private Drawable mRightIcon;
    private String mRightIntent;
    private String mRightSummary;
    private String mRightTitle;
    private final View mSoftWareInfoView;
    private final UserManager mUm;
    private final UserManager mUserManager;

    public static OPAboutPhoneSoftWareController newInstance(Activity activity, Fragment fragment, View header) {
        return new OPAboutPhoneSoftWareController(activity, fragment, header);
    }

    private OPAboutPhoneSoftWareController(Activity activity, Fragment fragment, View header) {
        if (header != null) {
            this.mSoftWareInfoView = header;
        } else {
            this.mSoftWareInfoView = LayoutInflater.from(fragment.getContext()).inflate(R.layout.op_about_phone_software_layout, null);
        }
        this.mActivity = activity;
        this.mFragment = fragment;
        this.mContext = fragment.getContext();
        this.mUm = (UserManager) this.mContext.getSystemService("user");
        this.mDebuggingFeaturesDisallowedAdmin = RestrictedLockUtils.checkIfRestrictionEnforced(this.mContext, "no_debugging_features", UserHandle.myUserId());
        this.mDebuggingFeaturesDisallowedBySystem = RestrictedLockUtils.hasBaseUserRestriction(this.mContext, "no_debugging_features", UserHandle.myUserId());
        this.mDevHitCountdown = DevelopmentSettingsEnabler.isDevelopmentSettingsEnabled(this.mContext) ? -1 : 7;
        this.mDevHitToast = null;
        this.mUserManager = (UserManager) this.mContext.getSystemService("user");
        this.mFunDisallowedAdmin = RestrictedLockUtils.checkIfRestrictionEnforced(this.mContext, "no_fun", UserHandle.myUserId());
        this.mFunDisallowedBySystem = RestrictedLockUtils.hasBaseUserRestriction(this.mContext, "no_fun", UserHandle.myUserId());
    }

    public OPAboutPhoneSoftWareController setLefImage(Drawable icon) {
        this.mLeftIcon = icon;
        return this;
    }

    public OPAboutPhoneSoftWareController setRightImage(Drawable icon) {
        this.mRightIcon = icon;
        return this;
    }

    public OPAboutPhoneSoftWareController setLeftTitle(String title) {
        this.mLeftTitle = title;
        return this;
    }

    public OPAboutPhoneSoftWareController setRightTitle(String title) {
        this.mRightTitle = title;
        return this;
    }

    public OPAboutPhoneSoftWareController setLeftSummary(String summary) {
        this.mLeftSummary = summary;
        return this;
    }

    public OPAboutPhoneSoftWareController setRightSummary(String summary) {
        this.mRightSummary = summary;
        return this;
    }

    public OPAboutPhoneSoftWareController setLeftIntentString(String mIntent) {
        this.mLeftIntent = mIntent;
        return this;
    }

    public OPAboutPhoneSoftWareController setRightIntentString(String mIntent) {
        this.mRightIntent = mIntent;
        return this;
    }

    public void enableDevelopmentSettings() {
        this.mDevHitCountdown = 0;
        this.mProcessingLastDevHit = false;
        DevelopmentSettingsEnabler.setDevelopmentSettingsEnabled(this.mContext, true);
        if (this.mDevHitToast != null) {
            this.mDevHitToast.cancel();
        }
        this.mDevHitToast = Toast.makeText(this.mContext, R.string.show_dev_on, 1);
        this.mDevHitToast.show();
    }

    /* Access modifiers changed, original: 0000 */
    public void arrayCopy() {
        System.arraycopy(this.mHits, 1, this.mHits, 0, this.mHits.length - 1);
    }

    private void bindAction(View mView, final String mIntent) {
        if (mView != null && mIntent != null) {
            mView.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if ("com.android.FirmwareVersionDialogFragment".equals(mIntent)) {
                        FirmwareVersionDialogFragment.show(OPAboutPhoneSoftWareController.this.mFragment);
                    } else if ("build.number".equals(mIntent)) {
                        if (!Utils.isMonkeyRunning()) {
                            if ((OPAboutPhoneSoftWareController.this.mUm.isAdminUser() || OPAboutPhoneSoftWareController.this.mUm.isDemoUser()) && Utils.isDeviceProvisioned(OPAboutPhoneSoftWareController.this.mContext)) {
                                if (OPAboutPhoneSoftWareController.this.mUm.hasUserRestriction("no_debugging_features")) {
                                    if (OPAboutPhoneSoftWareController.this.mUm.isDemoUser()) {
                                        ComponentName componentName = Utils.getDeviceOwnerComponent(OPAboutPhoneSoftWareController.this.mContext);
                                        if (componentName != null) {
                                            Intent requestDebugFeatures = new Intent().setPackage(componentName.getPackageName()).setAction("com.android.settings.action.REQUEST_DEBUG_FEATURES");
                                            if (OPAboutPhoneSoftWareController.this.mContext.getPackageManager().resolveActivity(requestDebugFeatures, 0) != null) {
                                                OPAboutPhoneSoftWareController.this.mContext.startActivity(requestDebugFeatures);
                                                return;
                                            }
                                        }
                                    }
                                    if (!(OPAboutPhoneSoftWareController.this.mDebuggingFeaturesDisallowedAdmin == null || OPAboutPhoneSoftWareController.this.mDebuggingFeaturesDisallowedBySystem)) {
                                        RestrictedLockUtils.sendShowAdminSupportDetailsIntent(OPAboutPhoneSoftWareController.this.mContext, OPAboutPhoneSoftWareController.this.mDebuggingFeaturesDisallowedAdmin);
                                    }
                                }
                                if (OPAboutPhoneSoftWareController.this.mDevHitCountdown > 0) {
                                    OPAboutPhoneSoftWareController.this.mDevHitCountdown = OPAboutPhoneSoftWareController.this.mDevHitCountdown - 1;
                                    if (OPAboutPhoneSoftWareController.this.mDevHitCountdown == 0 && !OPAboutPhoneSoftWareController.this.mProcessingLastDevHit) {
                                        OPAboutPhoneSoftWareController.this.mDevHitCountdown = OPAboutPhoneSoftWareController.this.mDevHitCountdown + 1;
                                        ChooseLockSettingsHelper helper = new ChooseLockSettingsHelper(OPAboutPhoneSoftWareController.this.mActivity, OPAboutPhoneSoftWareController.this.mFragment);
                                        OPAboutPhoneSoftWareController.this.mProcessingLastDevHit = helper.launchConfirmationActivity(100, OPAboutPhoneSoftWareController.this.mContext.getString(R.string.unlock_set_unlock_launch_picker_title));
                                        if (!OPAboutPhoneSoftWareController.this.mProcessingLastDevHit) {
                                            OPAboutPhoneSoftWareController.this.enableDevelopmentSettings();
                                        }
                                    } else if (OPAboutPhoneSoftWareController.this.mDevHitCountdown > 0 && OPAboutPhoneSoftWareController.this.mDevHitCountdown < 5) {
                                        if (OPAboutPhoneSoftWareController.this.mDevHitToast != null) {
                                            OPAboutPhoneSoftWareController.this.mDevHitToast.cancel();
                                        }
                                        OPAboutPhoneSoftWareController.this.mDevHitToast = Toast.makeText(OPAboutPhoneSoftWareController.this.mContext, OPAboutPhoneSoftWareController.this.mContext.getResources().getQuantityString(R.plurals.show_dev_countdown, OPAboutPhoneSoftWareController.this.mDevHitCountdown, new Object[]{Integer.valueOf(OPAboutPhoneSoftWareController.this.mDevHitCountdown)}), 0);
                                        OPAboutPhoneSoftWareController.this.mDevHitToast.show();
                                    }
                                } else if (OPAboutPhoneSoftWareController.this.mDevHitCountdown < 0) {
                                    if (OPAboutPhoneSoftWareController.this.mDevHitToast != null) {
                                        OPAboutPhoneSoftWareController.this.mDevHitToast.cancel();
                                    }
                                    OPAboutPhoneSoftWareController.this.mDevHitToast = Toast.makeText(OPAboutPhoneSoftWareController.this.mContext, R.string.show_dev_already, 1);
                                    OPAboutPhoneSoftWareController.this.mDevHitToast.show();
                                }
                            }
                        }
                    } else if ("build.model".equals(mIntent)) {
                        HardwareInfoDialogFragment.newInstance().show(OPAboutPhoneSoftWareController.this.mFragment.getFragmentManager(), HardwareInfoDialogFragment.TAG);
                    } else {
                        OPAboutPhoneSoftWareController.this.mFragment.startActivity(new Intent(mIntent));
                    }
                }
            });
        }
    }

    public View done() {
        ImageView mLeftIconView = (ImageView) this.mSoftWareInfoView.findViewById(R.id.left_image);
        ImageView mRightIconView = (ImageView) this.mSoftWareInfoView.findViewById(R.id.right_image);
        TextView mLeftTitleView = (TextView) this.mSoftWareInfoView.findViewById(R.id.left_title);
        TextView mRightTitleView = (TextView) this.mSoftWareInfoView.findViewById(R.id.right_title);
        TextView mLeftSummaryView = (TextView) this.mSoftWareInfoView.findViewById(R.id.left_summary);
        TextView mRightSummaryView = (TextView) this.mSoftWareInfoView.findViewById(R.id.right_summary);
        View mLeftView = this.mSoftWareInfoView.findViewById(R.id.left_view);
        View mRighttView = this.mSoftWareInfoView.findViewById(R.id.right_view);
        setImageDrawable(mLeftIconView, this.mLeftIcon);
        setImageDrawable(mRightIconView, this.mRightIcon);
        setText(mLeftTitleView, this.mLeftTitle);
        setText(mRightTitleView, this.mRightTitle);
        setText(mLeftSummaryView, this.mLeftSummary);
        setText(mRightSummaryView, this.mRightSummary);
        bindAction(mLeftView, this.mLeftIntent);
        bindAction(mRighttView, this.mRightIntent);
        return this.mSoftWareInfoView;
    }

    private void setImageDrawable(ImageView mImageView, Drawable icon) {
        if (icon != null) {
            mImageView.setImageDrawable(icon);
        } else {
            mImageView.setVisibility(8);
        }
    }

    private void setText(TextView textView, String text) {
        if (textView != null) {
            textView.setText(text);
            textView.setVisibility(TextUtils.isEmpty(text) ? 8 : 0);
        }
    }
}
