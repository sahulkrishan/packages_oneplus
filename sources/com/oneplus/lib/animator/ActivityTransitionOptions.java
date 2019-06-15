package com.oneplus.lib.animator;

import android.app.Activity;
import android.view.View;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class ActivityTransitionOptions {
    private WeakReference<Activity> mActivity;
    private ArrayList<ShareElementViewAttrs> mShareElementViewAttrs;
    private View[] mShareElementViews;

    public ActivityTransitionOptions(Activity activity, View[] views) {
        this.mActivity = new WeakReference(activity);
        this.mShareElementViews = views;
    }

    public static ActivityTransitionOptions makeSceneTransitionOptions(Activity activity, View... views) {
        return new ActivityTransitionOptions(activity, views);
    }

    public void captureViewAttrs() {
        if (this.mShareElementViews != null) {
            this.mShareElementViewAttrs = new ArrayList();
            for (View v : this.mShareElementViews) {
                int[] location = new int[2];
                v.getLocationOnScreen(location);
                this.mShareElementViewAttrs.add(new ShareElementViewAttrs(v.getId(), (float) location[0], (float) location[1], (float) v.getWidth(), (float) v.getHeight()));
            }
        }
    }

    public Activity getActivity() {
        return (Activity) this.mActivity.get();
    }

    public ArrayList<ShareElementViewAttrs> getShareElementViewAttrs() {
        return this.mShareElementViewAttrs;
    }
}
