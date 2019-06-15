package android.support.v17.leanback.app;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v17.leanback.R;
import android.support.v17.leanback.widget.SearchOrbView.Colors;
import android.support.v17.leanback.widget.TitleHelper;
import android.support.v17.leanback.widget.TitleViewAdapter;
import android.support.v17.leanback.widget.TitleViewAdapter.Provider;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public class BrandedSupportFragment extends Fragment {
    private static final String TITLE_SHOW = "titleShow";
    private Drawable mBadgeDrawable;
    private OnClickListener mExternalOnSearchClickedListener;
    private boolean mSearchAffordanceColorSet;
    private Colors mSearchAffordanceColors;
    private boolean mShowingTitle = true;
    private CharSequence mTitle;
    private TitleHelper mTitleHelper;
    private View mTitleView;
    private TitleViewAdapter mTitleViewAdapter;

    public View onInflateTitleView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        TypedValue typedValue = new TypedValue();
        return inflater.inflate(parent.getContext().getTheme().resolveAttribute(R.attr.browseTitleViewLayout, typedValue, true) ? typedValue.resourceId : R.layout.lb_browse_title, parent, false);
    }

    public void installTitleView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View titleLayoutRoot = onInflateTitleView(inflater, parent, savedInstanceState);
        if (titleLayoutRoot != null) {
            parent.addView(titleLayoutRoot);
            setTitleView(titleLayoutRoot.findViewById(R.id.browse_title_group));
            return;
        }
        setTitleView(null);
    }

    public void setTitleView(View titleView) {
        this.mTitleView = titleView;
        if (this.mTitleView == null) {
            this.mTitleViewAdapter = null;
            this.mTitleHelper = null;
            return;
        }
        this.mTitleViewAdapter = ((Provider) this.mTitleView).getTitleViewAdapter();
        this.mTitleViewAdapter.setTitle(this.mTitle);
        this.mTitleViewAdapter.setBadgeDrawable(this.mBadgeDrawable);
        if (this.mSearchAffordanceColorSet) {
            this.mTitleViewAdapter.setSearchAffordanceColors(this.mSearchAffordanceColors);
        }
        if (this.mExternalOnSearchClickedListener != null) {
            setOnSearchClickedListener(this.mExternalOnSearchClickedListener);
        }
        if (getView() instanceof ViewGroup) {
            this.mTitleHelper = new TitleHelper((ViewGroup) getView(), this.mTitleView);
        }
    }

    public View getTitleView() {
        return this.mTitleView;
    }

    public TitleViewAdapter getTitleViewAdapter() {
        return this.mTitleViewAdapter;
    }

    /* Access modifiers changed, original: 0000 */
    public TitleHelper getTitleHelper() {
        return this.mTitleHelper;
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(TITLE_SHOW, this.mShowingTitle);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null) {
            this.mShowingTitle = savedInstanceState.getBoolean(TITLE_SHOW);
        }
        if (this.mTitleView != null && (view instanceof ViewGroup)) {
            this.mTitleHelper = new TitleHelper((ViewGroup) view, this.mTitleView);
            this.mTitleHelper.showTitle(this.mShowingTitle);
        }
    }

    public void onDestroyView() {
        super.onDestroyView();
        this.mTitleHelper = null;
    }

    public void showTitle(boolean show) {
        if (show != this.mShowingTitle) {
            this.mShowingTitle = show;
            if (this.mTitleHelper != null) {
                this.mTitleHelper.showTitle(show);
            }
        }
    }

    public void showTitle(int flags) {
        if (this.mTitleViewAdapter != null) {
            this.mTitleViewAdapter.updateComponentsVisibility(flags);
        }
        showTitle(true);
    }

    public void setBadgeDrawable(Drawable drawable) {
        if (this.mBadgeDrawable != drawable) {
            this.mBadgeDrawable = drawable;
            if (this.mTitleViewAdapter != null) {
                this.mTitleViewAdapter.setBadgeDrawable(drawable);
            }
        }
    }

    public Drawable getBadgeDrawable() {
        return this.mBadgeDrawable;
    }

    public void setTitle(CharSequence title) {
        this.mTitle = title;
        if (this.mTitleViewAdapter != null) {
            this.mTitleViewAdapter.setTitle(title);
        }
    }

    public CharSequence getTitle() {
        return this.mTitle;
    }

    public void setOnSearchClickedListener(OnClickListener listener) {
        this.mExternalOnSearchClickedListener = listener;
        if (this.mTitleViewAdapter != null) {
            this.mTitleViewAdapter.setOnSearchClickedListener(listener);
        }
    }

    public void setSearchAffordanceColors(Colors colors) {
        this.mSearchAffordanceColors = colors;
        this.mSearchAffordanceColorSet = true;
        if (this.mTitleViewAdapter != null) {
            this.mTitleViewAdapter.setSearchAffordanceColors(this.mSearchAffordanceColors);
        }
    }

    public Colors getSearchAffordanceColors() {
        if (this.mSearchAffordanceColorSet) {
            return this.mSearchAffordanceColors;
        }
        if (this.mTitleViewAdapter != null) {
            return this.mTitleViewAdapter.getSearchAffordanceColors();
        }
        throw new IllegalStateException("Fragment views not yet created");
    }

    public void setSearchAffordanceColor(int color) {
        setSearchAffordanceColors(new Colors(color));
    }

    public int getSearchAffordanceColor() {
        return getSearchAffordanceColors().color;
    }

    public void onStart() {
        super.onStart();
        if (this.mTitleViewAdapter != null) {
            showTitle(this.mShowingTitle);
            this.mTitleViewAdapter.setAnimationEnabled(true);
        }
    }

    public void onPause() {
        if (this.mTitleViewAdapter != null) {
            this.mTitleViewAdapter.setAnimationEnabled(false);
        }
        super.onPause();
    }

    public void onResume() {
        super.onResume();
        if (this.mTitleViewAdapter != null) {
            this.mTitleViewAdapter.setAnimationEnabled(true);
        }
    }

    public final boolean isShowingTitle() {
        return this.mShowingTitle;
    }
}
