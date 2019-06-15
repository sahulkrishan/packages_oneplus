package android.support.v17.leanback.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.v17.leanback.R;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

final class RowContainerView extends LinearLayout {
    private Drawable mForeground;
    private boolean mForegroundBoundsChanged;
    private ViewGroup mHeaderDock;

    public RowContainerView(Context context) {
        this(context, null, 0);
    }

    public RowContainerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RowContainerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mForegroundBoundsChanged = true;
        setOrientation(1);
        LayoutInflater.from(context).inflate(R.layout.lb_row_container, this);
        this.mHeaderDock = (ViewGroup) findViewById(R.id.lb_row_container_header_dock);
        setLayoutParams(new LayoutParams(-2, -2));
    }

    public void addHeaderView(View headerView) {
        if (this.mHeaderDock.indexOfChild(headerView) < 0) {
            this.mHeaderDock.addView(headerView, 0);
        }
    }

    public void removeHeaderView(View headerView) {
        if (this.mHeaderDock.indexOfChild(headerView) >= 0) {
            this.mHeaderDock.removeView(headerView);
        }
    }

    public void addRowView(View view) {
        addView(view);
    }

    public void showHeader(boolean show) {
        this.mHeaderDock.setVisibility(show ? 0 : 8);
    }

    public void setForeground(Drawable d) {
        this.mForeground = d;
        setWillNotDraw(this.mForeground == null);
        invalidate();
    }

    public void setForegroundColor(@ColorInt int color) {
        if (this.mForeground instanceof ColorDrawable) {
            ((ColorDrawable) this.mForeground.mutate()).setColor(color);
            invalidate();
            return;
        }
        setForeground(new ColorDrawable(color));
    }

    public Drawable getForeground() {
        return this.mForeground;
    }

    /* Access modifiers changed, original: protected */
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.mForegroundBoundsChanged = true;
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (this.mForeground != null) {
            if (this.mForegroundBoundsChanged) {
                this.mForegroundBoundsChanged = false;
                this.mForeground.setBounds(0, 0, getWidth(), getHeight());
            }
            this.mForeground.draw(canvas);
        }
    }

    public boolean hasOverlappingRendering() {
        return false;
    }
}
