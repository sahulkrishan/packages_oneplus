package android.support.v17.leanback.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v17.leanback.R;
import android.support.v17.leanback.widget.SearchOrbView.Colors;
import android.support.v17.leanback.widget.TitleViewAdapter.Provider;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class TitleView extends FrameLayout implements Provider {
    private int flags;
    private ImageView mBadgeView;
    private boolean mHasSearchListener;
    private SearchOrbView mSearchOrbView;
    private TextView mTextView;
    private final TitleViewAdapter mTitleViewAdapter;

    public TitleView(Context context) {
        this(context, null);
    }

    public TitleView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.browseTitleViewStyle);
    }

    public TitleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.flags = 6;
        this.mHasSearchListener = false;
        this.mTitleViewAdapter = new TitleViewAdapter() {
            public View getSearchAffordanceView() {
                return TitleView.this.getSearchAffordanceView();
            }

            public void setOnSearchClickedListener(OnClickListener listener) {
                TitleView.this.setOnSearchClickedListener(listener);
            }

            public void setAnimationEnabled(boolean enable) {
                TitleView.this.enableAnimation(enable);
            }

            public Drawable getBadgeDrawable() {
                return TitleView.this.getBadgeDrawable();
            }

            public Colors getSearchAffordanceColors() {
                return TitleView.this.getSearchAffordanceColors();
            }

            public CharSequence getTitle() {
                return TitleView.this.getTitle();
            }

            public void setBadgeDrawable(Drawable drawable) {
                TitleView.this.setBadgeDrawable(drawable);
            }

            public void setSearchAffordanceColors(Colors colors) {
                TitleView.this.setSearchAffordanceColors(colors);
            }

            public void setTitle(CharSequence titleText) {
                TitleView.this.setTitle(titleText);
            }

            public void updateComponentsVisibility(int flags) {
                TitleView.this.updateComponentsVisibility(flags);
            }
        };
        View rootView = LayoutInflater.from(context).inflate(R.layout.lb_title_view, this);
        this.mBadgeView = (ImageView) rootView.findViewById(R.id.title_badge);
        this.mTextView = (TextView) rootView.findViewById(R.id.title_text);
        this.mSearchOrbView = (SearchOrbView) rootView.findViewById(R.id.title_orb);
        setClipToPadding(false);
        setClipChildren(false);
    }

    public void setTitle(CharSequence titleText) {
        this.mTextView.setText(titleText);
        updateBadgeVisibility();
    }

    public CharSequence getTitle() {
        return this.mTextView.getText();
    }

    public void setBadgeDrawable(Drawable drawable) {
        this.mBadgeView.setImageDrawable(drawable);
        updateBadgeVisibility();
    }

    public Drawable getBadgeDrawable() {
        return this.mBadgeView.getDrawable();
    }

    public void setOnSearchClickedListener(OnClickListener listener) {
        this.mHasSearchListener = listener != null;
        this.mSearchOrbView.setOnOrbClickedListener(listener);
        updateSearchOrbViewVisiblity();
    }

    public View getSearchAffordanceView() {
        return this.mSearchOrbView;
    }

    public void setSearchAffordanceColors(Colors colors) {
        this.mSearchOrbView.setOrbColors(colors);
    }

    public Colors getSearchAffordanceColors() {
        return this.mSearchOrbView.getOrbColors();
    }

    public void enableAnimation(boolean enable) {
        SearchOrbView searchOrbView = this.mSearchOrbView;
        boolean z = enable && this.mSearchOrbView.hasFocus();
        searchOrbView.enableOrbColorAnimation(z);
    }

    public void updateComponentsVisibility(int flags) {
        this.flags = flags;
        if ((flags & 2) == 2) {
            updateBadgeVisibility();
        } else {
            this.mBadgeView.setVisibility(8);
            this.mTextView.setVisibility(8);
        }
        updateSearchOrbViewVisiblity();
    }

    private void updateSearchOrbViewVisiblity() {
        int i = 4;
        if (this.mHasSearchListener && (this.flags & 4) == 4) {
            i = 0;
        }
        this.mSearchOrbView.setVisibility(i);
    }

    private void updateBadgeVisibility() {
        if (this.mBadgeView.getDrawable() != null) {
            this.mBadgeView.setVisibility(0);
            this.mTextView.setVisibility(8);
            return;
        }
        this.mBadgeView.setVisibility(8);
        this.mTextView.setVisibility(0);
    }

    public TitleViewAdapter getTitleViewAdapter() {
        return this.mTitleViewAdapter;
    }
}
