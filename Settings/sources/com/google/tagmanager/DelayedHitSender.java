package com.google.tagmanager;

import android.content.Context;
import com.google.android.gms.common.util.VisibleForTesting;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

class DelayedHitSender implements HitSender {
    private static DelayedHitSender sInstance;
    private static final Object sInstanceLock = new Object();
    private RateLimiter mRateLimiter;
    private HitSendingThread mSendingThread;
    private String mWrapperQueryParameter;
    private String mWrapperUrl;

    private DelayedHitSender(Context context) {
        this(HitSendingThreadImpl.getInstance(context), new SendHitRateLimiter());
    }

    @VisibleForTesting
    DelayedHitSender(HitSendingThread thread, RateLimiter rateLimiter) {
        this.mSendingThread = thread;
        this.mRateLimiter = rateLimiter;
    }

    public static HitSender getInstance(Context context) {
        DelayedHitSender delayedHitSender;
        synchronized (sInstanceLock) {
            if (sInstance == null) {
                sInstance = new DelayedHitSender(context);
            }
            delayedHitSender = sInstance;
        }
        return delayedHitSender;
    }

    public void setUrlWrapModeForTesting(String url, String queryParameter) {
        this.mWrapperUrl = url;
        this.mWrapperQueryParameter = queryParameter;
    }

    public boolean sendHit(String url) {
        if (this.mRateLimiter.tokenAvailable()) {
            if (!(this.mWrapperUrl == null || this.mWrapperQueryParameter == null)) {
                try {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(this.mWrapperUrl);
                    stringBuilder.append("?");
                    stringBuilder.append(this.mWrapperQueryParameter);
                    stringBuilder.append("=");
                    stringBuilder.append(URLEncoder.encode(url, "UTF-8"));
                    url = stringBuilder.toString();
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("Sending wrapped url hit: ");
                    stringBuilder.append(url);
                    Log.v(stringBuilder.toString());
                } catch (UnsupportedEncodingException e) {
                    Log.w("Error wrapping URL for testing.", e);
                    return false;
                }
            }
            this.mSendingThread.sendHit(url);
            return true;
        }
        Log.w("Too many urls sent too quickly with the TagManagerSender, rate limiting invoked.");
        return false;
    }
}
