package com.oneplus.settings.system;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieComposition.Factory;
import com.airbnb.lottie.OnCompositionLoadedListener;
import com.android.settings.R;

public class RamBoostLottieAnimPreference extends Preference implements OnClickListener {
    private static final int MSG_PLAY = 0;
    private String animFile = null;
    private LottieAnimationView anim_res;
    private ImageView img_play;
    private Context mContext;
    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                RamBoostLottieAnimPreference.this.startOrStopAnim();
            }
        }
    };
    private OnPreferenceViewClickListener mListener;
    private int resid = R.layout.op_preference_ramboost_lottie;
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

    public RamBoostLottieAnimPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initViews(context);
    }

    public RamBoostLottieAnimPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context);
    }

    public RamBoostLottieAnimPreference(Context context) {
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
        this.img_play.setOnClickListener(this);
        this.img_play.setVisibility(isShowPlayBtn() ? 0 : 8);
        this.anim_res.setOnClickListener(this);
        if (!TextUtils.isEmpty(this.animFile)) {
            Factory.fromAssetFileName(this.mContext, this.animFile, new OnCompositionLoadedListener() {
                public void onCompositionLoaded(LottieComposition composition) {
                    RamBoostLottieAnimPreference.this.anim_res.setComposition(composition);
                    RamBoostLottieAnimPreference.this.anim_res.setProgress(0.1f);
                }
            });
        }
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
