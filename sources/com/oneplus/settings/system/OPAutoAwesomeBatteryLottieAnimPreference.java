package com.oneplus.settings.system;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import com.airbnb.lottie.LottieAnimationView;
import com.android.settings.R;
import com.oneplus.settings.utils.OPUtils;

public class OPAutoAwesomeBatteryLottieAnimPreference extends Preference implements OnClickListener {
    private static final int MSG_PLAY = 0;
    private String animFile = null;
    private LottieAnimationView anim_res;
    private ImageView img_play;
    private Context mContext;
    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                OPAutoAwesomeBatteryLottieAnimPreference.this.startOrStopAnim();
            }
        }
    };
    private OnPreferenceViewClickListener mListener;
    private int resid = R.layout.op_preference_auto_awesome_battery_lottie;
    private boolean showPlayBtn = false;

    public interface OnPreferenceViewClickListener {
        void onPreferenceViewClick(View view);
    }

    public boolean isShowPlayBtn() {
        return this.showPlayBtn;
    }

    public void setShowPlayBtn(boolean showPlayBtn) {
        this.showPlayBtn = showPlayBtn;
    }

    public void setAnimFile(String animFile) {
        this.animFile = animFile;
    }

    public OPAutoAwesomeBatteryLottieAnimPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initViews(context);
    }

    public OPAutoAwesomeBatteryLottieAnimPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context);
    }

    public OPAutoAwesomeBatteryLottieAnimPreference(Context context) {
        super(context);
        initViews(context);
    }

    private void initViews(Context context) {
        this.mContext = context;
        setLayoutResource(this.resid);
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        this.img_play = (ImageView) view.findViewById(R.id.img_play);
        this.anim_res = (LottieAnimationView) view.findViewById(R.id.anim_res);
        if (OPUtils.isBlackModeOn(this.mContext.getContentResolver())) {
            this.anim_res.setAnimation("auto_awesome_battery_dark.json");
        } else {
            this.anim_res.setAnimation("auto_awesome_battery_light.json");
        }
        this.anim_res.loop(true);
        startAnim();
    }

    public void setViewOnClick(OnPreferenceViewClickListener listener) {
        this.mListener = listener;
    }

    public void playOrStopAnim() {
        this.mHandler.removeMessages(0);
        this.mHandler.sendEmptyMessage(0);
    }

    private void startOrStopAnim() {
        if (this.anim_res.isAnimating()) {
            stopAnim();
        } else {
            startAnim();
        }
    }

    private void startAnim() {
        this.img_play.setVisibility(8);
        this.anim_res.resumeAnimation();
    }

    public void stopAnim() {
        if (this.anim_res != null) {
            this.anim_res.pauseAnimation();
        }
        if (this.img_play != null) {
            this.img_play.setVisibility(0);
        }
    }

    public void onClick(View view) {
        if (this.mListener != null) {
            this.mListener.onPreferenceViewClick(view);
        }
    }
}
