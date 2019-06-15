package android.support.v17.leanback.widget;

import android.graphics.Rect;
import android.support.v17.leanback.widget.Parallax.IntProperty;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Property;
import android.view.View;
import android.view.View.OnLayoutChangeListener;

public class RecyclerViewParallax extends Parallax<ChildPositionProperty> {
    boolean mIsVertical;
    OnLayoutChangeListener mOnLayoutChangeListener = new OnLayoutChangeListener() {
        public void onLayoutChange(View view, int l, int t, int r, int b, int oldL, int oldT, int oldR, int oldB) {
            RecyclerViewParallax.this.updateValues();
        }
    };
    OnScrollListener mOnScrollListener = new OnScrollListener() {
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            RecyclerViewParallax.this.updateValues();
        }
    };
    RecyclerView mRecylerView;

    public static final class ChildPositionProperty extends IntProperty {
        int mAdapterPosition;
        float mFraction;
        int mOffset;
        int mViewId;

        ChildPositionProperty(String name, int index) {
            super(name, index);
        }

        public ChildPositionProperty adapterPosition(int adapterPosition) {
            this.mAdapterPosition = adapterPosition;
            return this;
        }

        public ChildPositionProperty viewId(int viewId) {
            this.mViewId = viewId;
            return this;
        }

        public ChildPositionProperty offset(int offset) {
            this.mOffset = offset;
            return this;
        }

        public ChildPositionProperty fraction(float fraction) {
            this.mFraction = fraction;
            return this;
        }

        public int getAdapterPosition() {
            return this.mAdapterPosition;
        }

        public int getViewId() {
            return this.mViewId;
        }

        public int getOffset() {
            return this.mOffset;
        }

        public float getFraction() {
            return this.mFraction;
        }

        /* Access modifiers changed, original: 0000 */
        public void updateValue(RecyclerViewParallax source) {
            ViewHolder viewHolder;
            Object recyclerView = source.mRecylerView;
            if (recyclerView == null) {
                viewHolder = null;
            } else {
                viewHolder = recyclerView.findViewHolderForAdapterPosition(this.mAdapterPosition);
            }
            if (viewHolder != null) {
                View trackingView = viewHolder.itemView.findViewById(this.mViewId);
                if (trackingView != null) {
                    Rect rect = new Rect(0, 0, trackingView.getWidth(), trackingView.getHeight());
                    recyclerView.offsetDescendantRectToMyCoords(trackingView, rect);
                    float tx = 0.0f;
                    float ty = 0.0f;
                    while (trackingView != recyclerView && trackingView != null) {
                        if (trackingView.getParent() != recyclerView || !recyclerView.isAnimating()) {
                            tx += trackingView.getTranslationX();
                            ty += trackingView.getTranslationY();
                        }
                        trackingView = (View) trackingView.getParent();
                    }
                    rect.offset((int) tx, (int) ty);
                    if (source.mIsVertical) {
                        source.setIntPropertyValue(getIndex(), (rect.top + this.mOffset) + ((int) (this.mFraction * ((float) rect.height()))));
                    } else {
                        source.setIntPropertyValue(getIndex(), (rect.left + this.mOffset) + ((int) (this.mFraction * ((float) rect.width()))));
                    }
                }
            } else if (recyclerView == null || recyclerView.getLayoutManager().getChildCount() == 0) {
                source.setIntPropertyValue(getIndex(), Integer.MAX_VALUE);
            } else if (recyclerView.findContainingViewHolder(recyclerView.getLayoutManager().getChildAt(0)).getAdapterPosition() < this.mAdapterPosition) {
                source.setIntPropertyValue(getIndex(), Integer.MAX_VALUE);
            } else {
                source.setIntPropertyValue(getIndex(), Integer.MIN_VALUE);
            }
        }
    }

    public ChildPositionProperty createProperty(String name, int index) {
        return new ChildPositionProperty(name, index);
    }

    public float getMaxValue() {
        if (this.mRecylerView == null) {
            return 0.0f;
        }
        return (float) (this.mIsVertical ? this.mRecylerView.getHeight() : this.mRecylerView.getWidth());
    }

    public void setRecyclerView(RecyclerView recyclerView) {
        if (this.mRecylerView != recyclerView) {
            if (this.mRecylerView != null) {
                this.mRecylerView.removeOnScrollListener(this.mOnScrollListener);
                this.mRecylerView.removeOnLayoutChangeListener(this.mOnLayoutChangeListener);
            }
            this.mRecylerView = recyclerView;
            if (this.mRecylerView != null) {
                this.mRecylerView.getLayoutManager();
                boolean z = false;
                if (LayoutManager.getProperties(this.mRecylerView.getContext(), null, 0, 0).orientation == 1) {
                    z = true;
                }
                this.mIsVertical = z;
                this.mRecylerView.addOnScrollListener(this.mOnScrollListener);
                this.mRecylerView.addOnLayoutChangeListener(this.mOnLayoutChangeListener);
            }
        }
    }

    public void updateValues() {
        for (Property prop : getProperties()) {
            ((ChildPositionProperty) prop).updateValue(this);
        }
        super.updateValues();
    }

    public RecyclerView getRecyclerView() {
        return this.mRecylerView;
    }
}
