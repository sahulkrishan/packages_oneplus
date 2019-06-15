package com.oneplus.settings.quickpay;

import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;

public class SceneAnimation {
    private final int MSG_PLAY = 0;
    private final int MSG_STOP = 1;
    private int mDuration;
    private int[] mDurations;
    private int[] mFrameRess;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    int pFrameNo = msg.arg1;
                    SceneAnimation.this.mImageView.setImageResource(SceneAnimation.this.mFrameRess[pFrameNo]);
                    if (pFrameNo == SceneAnimation.this.mLastFrameNo) {
                        SceneAnimation.this.play(0);
                        return;
                    } else {
                        SceneAnimation.this.play(pFrameNo + 1);
                        return;
                    }
                case 1:
                    SceneAnimation.this.mImageView.setImageResource(SceneAnimation.this.mFrameRess[0]);
                    return;
                default:
                    return;
            }
        }
    };
    private ImageView mImageView;
    private int mLastFrameNo;
    private boolean starting = false;

    public boolean isStarting() {
        return this.starting;
    }

    public SceneAnimation(ImageView pImageView, int[] pFrameRess, int[] pDurations) {
        this.mImageView = pImageView;
        this.mFrameRess = pFrameRess;
        this.mDurations = pDurations;
        this.mLastFrameNo = pFrameRess.length - 1;
        this.mImageView.setImageResource(this.mFrameRess[0]);
        stop();
    }

    public SceneAnimation(ImageView pImageView, int[] pFrameRess, int pDuration) {
        this.mImageView = pImageView;
        this.mFrameRess = pFrameRess;
        this.mDuration = pDuration;
        this.mLastFrameNo = pFrameRess.length - 1;
        this.mImageView.setImageResource(this.mFrameRess[0]);
        stop();
    }

    public SceneAnimation(ImageView pImageView, int[] pFrameRess, int pDuration, long pBreakDelay) {
        this.mImageView = pImageView;
        this.mFrameRess = pFrameRess;
        this.mDuration = pDuration;
        this.mLastFrameNo = pFrameRess.length - 1;
        this.mImageView.setImageResource(this.mFrameRess[0]);
        stop();
    }

    private void play(int pFrameNo) {
        if (this.starting) {
            Message msg = this.mHandler.obtainMessage();
            msg.arg1 = pFrameNo;
            msg.what = 0;
            this.mHandler.sendMessageDelayed(msg, (long) this.mDuration);
            return;
        }
        this.mHandler.removeMessages(0);
    }

    public void play() {
        this.starting = true;
        play(0);
    }

    public void stop() {
        this.starting = false;
        this.mHandler.removeMessages(0);
        this.mHandler.sendEmptyMessage(1);
    }
}
