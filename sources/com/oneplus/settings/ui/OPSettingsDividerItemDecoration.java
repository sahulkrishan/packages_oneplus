package com.oneplus.settings.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceGroupAdapter;
import android.support.v7.preference.PreferenceViewHolder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ItemDecoration;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.support.v7.widget.RecyclerView.State;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import com.android.settings.R;
import com.oneplus.settings.utils.OPUtils;

public class OPSettingsDividerItemDecoration extends ItemDecoration {
    private int mCategoryStartIndex = -1;
    private Context mContext;
    private Drawable mDivider;
    private int mDividerHeight;
    private boolean mHasCategory;
    boolean mLTRLayout;
    private int mMarginLeft2;
    private int mMarginLeft4;

    public OPSettingsDividerItemDecoration(Context context, int dividerDrawable, int dividerHeight) {
        this.mContext = context;
        this.mDivider = ContextCompat.getDrawable(context, dividerDrawable);
        Resources res = context.getResources();
        this.mDividerHeight = res.getDimensionPixelSize(dividerHeight);
        this.mMarginLeft2 = res.getDimensionPixelSize(R.dimen.oneplus_settings_layout_margin_left2);
        this.mMarginLeft4 = res.getDimensionPixelSize(R.dimen.oneplus_settings_layout_margin_left4);
        this.mLTRLayout = OPUtils.isLTRLayout(context);
    }

    public void onDrawOver(Canvas c, RecyclerView parent, State state) {
        RecyclerView recyclerView = parent;
        if (this.mDivider != null) {
            int childCount = parent.getChildCount();
            int width = parent.getWidth();
            if (parent.getAdapter() instanceof PreferenceGroupAdapter) {
                Canvas canvas;
                PreferenceGroupAdapter adapter = (PreferenceGroupAdapter) parent.getAdapter();
                LayoutManager layoutManager = parent.getLayoutManager();
                int firstItemPosition = 0;
                if (layoutManager instanceof LinearLayoutManager) {
                    firstItemPosition = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
                }
                int childViewIndex = 0;
                while (childViewIndex < childCount) {
                    View view = recyclerView.getChildAt(childViewIndex);
                    ViewHolder holder = recyclerView.getChildViewHolder(view);
                    boolean z = true;
                    if (adapter.getItem(childViewIndex) instanceof PreferenceCategory) {
                        this.mHasCategory = true;
                        if (this.mCategoryStartIndex == -1) {
                            this.mCategoryStartIndex = childViewIndex;
                        }
                    }
                    if (!this.mHasCategory || childViewIndex < (this.mCategoryStartIndex - 1) - firstItemPosition) {
                        z = false;
                    }
                    boolean hasCategory = z;
                    if (shouldDrawDividerBelow(view, recyclerView)) {
                        int top = ((int) ViewCompat.getY(view)) + view.getHeight();
                        if (childViewIndex == childCount - 1) {
                            this.mDivider.setBounds(0, top, width, this.mDividerHeight + top);
                        } else if (itemHasIcon(view, recyclerView)) {
                            if (!hasCategory) {
                                this.mDivider.setBounds(0, top, width, this.mDividerHeight + top);
                            } else if (this.mLTRLayout) {
                                this.mDivider.setBounds(this.mMarginLeft4, top, width, this.mDividerHeight + top);
                            } else {
                                this.mDivider.setBounds(0, top, width - this.mMarginLeft4, this.mDividerHeight + top);
                            }
                        } else if (!hasCategory) {
                            this.mDivider.setBounds(0, top, width, this.mDividerHeight + top);
                        } else if (this.mLTRLayout) {
                            this.mDivider.setBounds(this.mMarginLeft2, top, width, this.mDividerHeight + top);
                        } else {
                            this.mDivider.setBounds(0, top, width - this.mMarginLeft2, this.mDividerHeight + top);
                        }
                        this.mDivider.draw(c);
                    } else {
                        canvas = c;
                    }
                    childViewIndex++;
                }
                canvas = c;
            }
        }
    }

    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, State state) {
        if (shouldDrawDividerBelow(view, parent)) {
            outRect.bottom = this.mDividerHeight;
        }
    }

    private boolean shouldDrawDividerBelow(View view, RecyclerView parent) {
        ViewHolder holder = parent.getChildViewHolder(view);
        boolean z = false;
        boolean dividerAllowedBelow = (holder instanceof PreferenceViewHolder) && ((PreferenceViewHolder) holder).isDividerAllowedBelow();
        if (!dividerAllowedBelow) {
            return false;
        }
        boolean nextAllowed = true;
        int index = parent.indexOfChild(view);
        if (index < parent.getChildCount() - 1) {
            ViewHolder nextHolder = parent.getChildViewHolder(parent.getChildAt(index + 1));
            if ((nextHolder instanceof PreferenceViewHolder) && ((PreferenceViewHolder) nextHolder).isDividerAllowedAbove()) {
                z = true;
            }
            nextAllowed = z;
        }
        return nextAllowed;
    }

    private boolean itemHasIcon(View view, RecyclerView parent) {
        ViewHolder holder = parent.getChildViewHolder(view);
        if (!(holder instanceof PreferenceViewHolder)) {
            return false;
        }
        View icon = ((PreferenceViewHolder) holder).findViewById(16908294);
        if (icon == null || icon.getVisibility() == 8 || icon.getWidth() == 0) {
            return false;
        }
        return true;
    }

    public void setDivider(Drawable divider) {
        if (divider != null) {
            this.mDividerHeight = divider.getIntrinsicHeight();
        } else {
            this.mDividerHeight = 0;
        }
        this.mDivider = divider;
    }

    public void setDividerHeight(int dividerHeight) {
        this.mDividerHeight = dividerHeight;
    }
}
