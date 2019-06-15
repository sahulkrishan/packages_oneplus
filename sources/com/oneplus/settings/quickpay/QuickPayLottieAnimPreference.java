package com.oneplus.settings.quickpay;

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
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieComposition.Factory;
import com.airbnb.lottie.OnCompositionLoadedListener;
import com.android.settings.R;
import com.oneplus.lib.util.ReflectUtil;
import com.oneplus.settings.utils.OPConstants;
import com.oneplus.settings.utils.OPUtils;

public class QuickPayLottieAnimPreference extends Preference implements OnClickListener {
    private static final int MSG_PLAY = 0;
    private String animFile = "op_quickpay_instroduction_anim_enchilada_white.json";
    private LottieAnimationView anim_quickpay_instructions;
    private ImageView img_quickpay_play;
    private Context mContext;
    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                QuickPayLottieAnimPreference.this.startOrStopAnim();
            }
        }
    };
    private OnPreferenceViewClickListener mListener;
    private int resid = R.layout.op_quickpay_instructions_lottie;

    public interface OnPreferenceViewClickListener {
        void onPreferenceViewClick(View view);
    }

    private void setAnimFile(String animFile) {
        this.animFile = animFile;
    }

    public QuickPayLottieAnimPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initViews(context);
    }

    public QuickPayLottieAnimPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context);
    }

    public QuickPayLottieAnimPreference(Context context) {
        super(context);
        initViews(context);
    }

    private void initViews(Context context) {
        this.mContext = context;
        setLayoutResource(this.resid);
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        this.img_quickpay_play = (ImageView) view.findViewById(R.id.img_quickpay_play);
        this.anim_quickpay_instructions = (LottieAnimationView) view.findViewById(R.id.anim_quickpay_instructions);
        this.img_quickpay_play.setOnClickListener(this);
        this.img_quickpay_play.setEnabled(false);
        this.anim_quickpay_instructions.setOnClickListener(this);
        boolean blackModel = OPUtils.isBlackModeOn(this.mContext.getContentResolver());
        if (OPUtils.isSupportCustomFingerprint()) {
            if (blackModel) {
                setAnimFile("op_quickpay_instroduction_anim_custom_black.json");
            } else {
                setAnimFile("op_quickpay_instroduction_anim_custom_white.json");
            }
        } else if (ReflectUtil.isFeatureSupported(OPConstants.FEATURE_QUICKPAY_ANIM_FOR_ENCHILADA)) {
            if (blackModel) {
                setAnimFile("op_quickpay_instroduction_anim_enchilada_black.json");
            } else {
                setAnimFile("op_quickpay_instroduction_anim_enchilada_white.json");
            }
        } else if (OPUtils.isSurportBackFingerprint(this.mContext)) {
            if (blackModel) {
                setAnimFile("op_quickpay_instroduction_anim_dumpling_black.json");
            } else {
                setAnimFile("op_quickpay_instroduction_anim_dumpling_white.json");
            }
        } else if (blackModel) {
            setAnimFile("op_quickpay_instroduction_anim_cheeseburger_black.json");
        } else {
            setAnimFile("op_quickpay_instroduction_anim_cheeseburger_white.json");
        }
        Factory.fromAssetFileName(this.mContext, this.animFile, new OnCompositionLoadedListener() {
            public void onCompositionLoaded(LottieComposition composition) {
                QuickPayLottieAnimPreference.this.anim_quickpay_instructions.setComposition(composition);
                QuickPayLottieAnimPreference.this.anim_quickpay_instructions.setProgress(0.1f);
                QuickPayLottieAnimPreference.this.img_quickpay_play.setEnabled(true);
            }
        });
    }

    public void setViewOnClick(OnPreferenceViewClickListener listener) {
        this.mListener = listener;
    }

    public void playOrStopAnim() {
        this.mHandler.removeMessages(0);
        this.mHandler.sendEmptyMessage(0);
    }

    private void startOrStopAnim() {
        if (this.anim_quickpay_instructions.isAnimating()) {
            stopAnim();
        } else {
            startAnim();
        }
    }

    private void startAnim() {
        this.img_quickpay_play.setVisibility(8);
        this.anim_quickpay_instructions.resumeAnimation();
    }

    public void stopAnim() {
        if (this.anim_quickpay_instructions != null) {
            this.anim_quickpay_instructions.pauseAnimation();
        }
        if (this.img_quickpay_play != null) {
            this.img_quickpay_play.setVisibility(0);
        }
    }

    public void onClick(View view) {
        if (this.mListener != null) {
            this.mListener.onPreferenceViewClick(view);
        }
    }
}
