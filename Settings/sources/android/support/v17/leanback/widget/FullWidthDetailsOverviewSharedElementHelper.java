package android.support.v17.leanback.widget;

import android.app.Activity;
import android.os.Handler;
import android.support.v17.leanback.transition.TransitionHelper;
import android.support.v17.leanback.transition.TransitionListener;
import android.support.v17.leanback.widget.FullWidthDetailsOverviewRowPresenter.Listener;
import android.support.v17.leanback.widget.FullWidthDetailsOverviewRowPresenter.ViewHolder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import java.lang.ref.WeakReference;

public class FullWidthDetailsOverviewSharedElementHelper extends Listener {
    static final boolean DEBUG = false;
    private static final long DEFAULT_TIMEOUT = 5000;
    static final String TAG = "DetailsTransitionHelper";
    Activity mActivityToRunTransition;
    private boolean mAutoStartSharedElementTransition = true;
    String mSharedElementName;
    private boolean mStartedPostpone;
    ViewHolder mViewHolder;

    static class TransitionTimeOutRunnable implements Runnable {
        WeakReference<FullWidthDetailsOverviewSharedElementHelper> mHelperRef;

        TransitionTimeOutRunnable(FullWidthDetailsOverviewSharedElementHelper helper) {
            this.mHelperRef = new WeakReference(helper);
        }

        public void run() {
            FullWidthDetailsOverviewSharedElementHelper helper = (FullWidthDetailsOverviewSharedElementHelper) this.mHelperRef.get();
            if (helper != null) {
                helper.startPostponedEnterTransition();
            }
        }
    }

    public void setSharedElementEnterTransition(Activity activity, String sharedElementName) {
        setSharedElementEnterTransition(activity, sharedElementName, DEFAULT_TIMEOUT);
    }

    public void setSharedElementEnterTransition(Activity activity, String sharedElementName, long timeoutMs) {
        if ((activity == null && !TextUtils.isEmpty(sharedElementName)) || (activity != null && TextUtils.isEmpty(sharedElementName))) {
            throw new IllegalArgumentException();
        } else if (activity != this.mActivityToRunTransition || !TextUtils.equals(sharedElementName, this.mSharedElementName)) {
            this.mActivityToRunTransition = activity;
            this.mSharedElementName = sharedElementName;
            setAutoStartSharedElementTransition(TransitionHelper.getSharedElementEnterTransition(activity.getWindow()) != null);
            ActivityCompat.postponeEnterTransition(this.mActivityToRunTransition);
            if (timeoutMs > 0) {
                new Handler().postDelayed(new TransitionTimeOutRunnable(this), timeoutMs);
            }
        }
    }

    public void setAutoStartSharedElementTransition(boolean enabled) {
        this.mAutoStartSharedElementTransition = enabled;
    }

    public boolean getAutoStartSharedElementTransition() {
        return this.mAutoStartSharedElementTransition;
    }

    public void onBindLogo(ViewHolder vh) {
        this.mViewHolder = vh;
        if (this.mAutoStartSharedElementTransition) {
            if (this.mViewHolder != null) {
                ViewCompat.setTransitionName(this.mViewHolder.getLogoViewHolder().view, null);
            }
            this.mViewHolder.getDetailsDescriptionFrame().postOnAnimation(new Runnable() {
                public void run() {
                    ViewCompat.setTransitionName(FullWidthDetailsOverviewSharedElementHelper.this.mViewHolder.getLogoViewHolder().view, FullWidthDetailsOverviewSharedElementHelper.this.mSharedElementName);
                    Object transition = TransitionHelper.getSharedElementEnterTransition(FullWidthDetailsOverviewSharedElementHelper.this.mActivityToRunTransition.getWindow());
                    if (transition != null) {
                        TransitionHelper.addTransitionListener(transition, new TransitionListener() {
                            public void onTransitionEnd(Object transition) {
                                if (FullWidthDetailsOverviewSharedElementHelper.this.mViewHolder.getActionsRow().isFocused()) {
                                    FullWidthDetailsOverviewSharedElementHelper.this.mViewHolder.getActionsRow().requestFocus();
                                }
                                TransitionHelper.removeTransitionListener(transition, this);
                            }
                        });
                    }
                    FullWidthDetailsOverviewSharedElementHelper.this.startPostponedEnterTransitionInternal();
                }
            });
        }
    }

    public void startPostponedEnterTransition() {
        new Handler().post(new Runnable() {
            public void run() {
                FullWidthDetailsOverviewSharedElementHelper.this.startPostponedEnterTransitionInternal();
            }
        });
    }

    /* Access modifiers changed, original: 0000 */
    public void startPostponedEnterTransitionInternal() {
        if (!this.mStartedPostpone && this.mViewHolder != null) {
            ActivityCompat.startPostponedEnterTransition(this.mActivityToRunTransition);
            this.mStartedPostpone = true;
        }
    }
}
