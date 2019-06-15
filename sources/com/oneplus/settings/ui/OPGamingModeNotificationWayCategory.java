package com.oneplus.settings.ui;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;
import com.airbnb.lottie.LottieAnimationView;
import com.android.settings.R;
import com.oneplus.settings.utils.OPUtils;

public class OPGamingModeNotificationWayCategory extends Preference {
    private static final String GAME_MODE_BLOCK_NOTIFICATION = "game_mode_block_notification";
    private static final int SHIELDING_NOTIFICATION_VALUE = 1;
    private static final int SUSPENSION_NOTICE_VALUE = 0;
    private static final int WEAK_TEXT_REMINDING_VALUE = 2;
    private ContentResolver mContentResolver;
    private Context mContext;
    private boolean mHasInited = false;
    private int mLayoutResId = R.layout.op_gaming_mode_notification_way_instructions_category;
    private TextView mNoficationWaySummary;
    private ImageView mShieldingNotificationImageView;
    private LottieAnimationView mSuspensionNoticeAnim;
    private LottieAnimationView mWeakTextRemindingAnim;

    public OPGamingModeNotificationWayCategory(Context context) {
        super(context);
        initViews(context);
    }

    public OPGamingModeNotificationWayCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context);
    }

    public OPGamingModeNotificationWayCategory(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initViews(context);
    }

    private void initViews(Context context) {
        this.mContext = context;
        this.mContentResolver = this.mContext.getContentResolver();
        setLayoutResource(this.mLayoutResId);
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        view.itemView.setClickable(false);
        this.mSuspensionNoticeAnim = (LottieAnimationView) view.findViewById(R.id.suspension_notice_anim);
        this.mWeakTextRemindingAnim = (LottieAnimationView) view.findViewById(R.id.weak_text_reminding_anim);
        this.mShieldingNotificationImageView = (ImageView) view.findViewById(R.id.shielding_notificationimageview);
        this.mNoficationWaySummary = (TextView) view.findViewById(R.id.nofication_way_summary);
        if (OPUtils.isBlackModeOn(this.mContentResolver)) {
            this.mSuspensionNoticeAnim.setAnimation("op_suspension_notice_anim_dark.json");
        } else {
            this.mSuspensionNoticeAnim.setAnimation("op_suspension_notice_anim_light.json");
        }
        if (OPUtils.isBlackModeOn(this.mContentResolver)) {
            this.mWeakTextRemindingAnim.setAnimation("op_weak_text_reminding_anim_dark.json");
        } else {
            this.mWeakTextRemindingAnim.setAnimation("op_weak_text_reminding_anim_light.json");
        }
        if (OPUtils.isBlackModeOn(this.mContentResolver)) {
            this.mShieldingNotificationImageView.setImageDrawable(this.mContext.getResources().getDrawable(R.drawable.op_shielding_notification_dark));
        } else {
            this.mShieldingNotificationImageView.setImageDrawable(this.mContext.getResources().getDrawable(R.drawable.op_shielding_notification_light));
        }
        this.mWeakTextRemindingAnim.loop(true);
        this.mWeakTextRemindingAnim.playAnimation();
        view.setDividerAllowedBelow(false);
        this.mHasInited = true;
        startAnim();
    }

    public void startAnim() {
        if (this.mHasInited) {
            setAnimTypes(System.getIntForUser(this.mContext.getContentResolver(), "game_mode_block_notification", 0, -2));
        }
    }

    public void stopAnim() {
        if (this.mSuspensionNoticeAnim != null) {
            this.mSuspensionNoticeAnim.cancelAnimation();
        }
        if (this.mWeakTextRemindingAnim != null) {
            this.mWeakTextRemindingAnim.cancelAnimation();
        }
    }

    public void releaseAnim() {
        if (this.mSuspensionNoticeAnim != null) {
            this.mSuspensionNoticeAnim.cancelAnimation();
        }
        if (this.mWeakTextRemindingAnim != null) {
            this.mWeakTextRemindingAnim.cancelAnimation();
        }
        this.mSuspensionNoticeAnim = null;
        this.mWeakTextRemindingAnim = null;
    }

    public void setAnimTypes(int type) {
        if (this.mHasInited) {
            stopAnim();
            switch (type) {
                case 0:
                    this.mWeakTextRemindingAnim.setVisibility(8);
                    this.mShieldingNotificationImageView.setVisibility(8);
                    this.mSuspensionNoticeAnim.setVisibility(0);
                    this.mSuspensionNoticeAnim.playAnimation();
                    this.mNoficationWaySummary.setText(R.string.oneplus_suspension_notice_summary);
                    break;
                case 1:
                    this.mSuspensionNoticeAnim.setVisibility(8);
                    this.mWeakTextRemindingAnim.setVisibility(8);
                    this.mShieldingNotificationImageView.setVisibility(0);
                    this.mNoficationWaySummary.setText(R.string.oneplus_shielding_notification_summary);
                    break;
                case 2:
                    this.mSuspensionNoticeAnim.setVisibility(8);
                    this.mShieldingNotificationImageView.setVisibility(8);
                    this.mWeakTextRemindingAnim.setVisibility(0);
                    this.mWeakTextRemindingAnim.playAnimation();
                    this.mNoficationWaySummary.setText(R.string.oneplus_weak_text_reminding_summary);
                    break;
            }
        }
    }
}
