package android.support.v17.leanback.widget;

import android.support.v17.leanback.transition.LeanbackTransitionHelper;
import android.support.v17.leanback.transition.TransitionHelper;
import android.support.v17.leanback.widget.BrowseFrameLayout.OnFocusSearchListener;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.ViewGroup;

public class TitleHelper {
    private final OnFocusSearchListener mOnFocusSearchListener = new OnFocusSearchListener() {
        public View onFocusSearch(View focused, int direction) {
            if (focused != TitleHelper.this.mTitleView && direction == 33) {
                return TitleHelper.this.mTitleView;
            }
            boolean z = true;
            if (ViewCompat.getLayoutDirection(focused) != 1) {
                z = false;
            }
            int forward = z ? 17 : 66;
            if (TitleHelper.this.mTitleView.hasFocus() && (direction == 130 || direction == forward)) {
                return TitleHelper.this.mSceneRoot;
            }
            return null;
        }
    };
    ViewGroup mSceneRoot;
    private Object mSceneWithTitle;
    private Object mSceneWithoutTitle;
    private Object mTitleDownTransition;
    private Object mTitleUpTransition;
    View mTitleView;

    public TitleHelper(ViewGroup sceneRoot, View titleView) {
        if (sceneRoot == null || titleView == null) {
            throw new IllegalArgumentException("Views may not be null");
        }
        this.mSceneRoot = sceneRoot;
        this.mTitleView = titleView;
        createTransitions();
    }

    private void createTransitions() {
        this.mTitleUpTransition = LeanbackTransitionHelper.loadTitleOutTransition(this.mSceneRoot.getContext());
        this.mTitleDownTransition = LeanbackTransitionHelper.loadTitleInTransition(this.mSceneRoot.getContext());
        this.mSceneWithTitle = TransitionHelper.createScene(this.mSceneRoot, new Runnable() {
            public void run() {
                TitleHelper.this.mTitleView.setVisibility(0);
            }
        });
        this.mSceneWithoutTitle = TransitionHelper.createScene(this.mSceneRoot, new Runnable() {
            public void run() {
                TitleHelper.this.mTitleView.setVisibility(4);
            }
        });
    }

    public void showTitle(boolean show) {
        if (show) {
            TransitionHelper.runTransition(this.mSceneWithTitle, this.mTitleDownTransition);
        } else {
            TransitionHelper.runTransition(this.mSceneWithoutTitle, this.mTitleUpTransition);
        }
    }

    public ViewGroup getSceneRoot() {
        return this.mSceneRoot;
    }

    public View getTitleView() {
        return this.mTitleView;
    }

    public OnFocusSearchListener getOnFocusSearchListener() {
        return this.mOnFocusSearchListener;
    }
}
