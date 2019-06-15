package android.support.v17.leanback.widget;

import android.content.Context;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import java.util.ArrayList;

@RestrictTo({Scope.LIBRARY_GROUP})
public class NonOverlappingLinearLayout extends LinearLayout {
    boolean mDeferFocusableViewAvailableInLayout;
    boolean mFocusableViewAvailableFixEnabled;
    final ArrayList<ArrayList<View>> mSortedAvailableViews;

    public NonOverlappingLinearLayout(Context context) {
        this(context, null);
    }

    public NonOverlappingLinearLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NonOverlappingLinearLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mFocusableViewAvailableFixEnabled = false;
        this.mSortedAvailableViews = new ArrayList();
    }

    public boolean hasOverlappingRendering() {
        return false;
    }

    public void setFocusableViewAvailableFixEnabled(boolean enabled) {
        this.mFocusableViewAvailableFixEnabled = enabled;
    }

    /* Access modifiers changed, original: protected */
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        int i = 0;
        try {
            boolean z = this.mFocusableViewAvailableFixEnabled && getOrientation() == 0 && getLayoutDirection() == 1;
            this.mDeferFocusableViewAvailableInLayout = z;
            if (this.mDeferFocusableViewAvailableInLayout) {
                while (this.mSortedAvailableViews.size() > getChildCount()) {
                    this.mSortedAvailableViews.remove(this.mSortedAvailableViews.size() - 1);
                }
                while (this.mSortedAvailableViews.size() < getChildCount()) {
                    this.mSortedAvailableViews.add(new ArrayList());
                }
            }
            super.onLayout(changed, l, t, r, b);
            if (this.mDeferFocusableViewAvailableInLayout) {
                for (int i2 = 0; i2 < this.mSortedAvailableViews.size(); i2++) {
                    for (int j = 0; j < ((ArrayList) this.mSortedAvailableViews.get(i2)).size(); j++) {
                        super.focusableViewAvailable((View) ((ArrayList) this.mSortedAvailableViews.get(i2)).get(j));
                    }
                }
            }
            if (this.mDeferFocusableViewAvailableInLayout) {
                this.mDeferFocusableViewAvailableInLayout = false;
                while (i < this.mSortedAvailableViews.size()) {
                    ((ArrayList) this.mSortedAvailableViews.get(i)).clear();
                    i++;
                }
            }
        } catch (Throwable th) {
            if (this.mDeferFocusableViewAvailableInLayout) {
                this.mDeferFocusableViewAvailableInLayout = false;
                while (i < this.mSortedAvailableViews.size()) {
                    ((ArrayList) this.mSortedAvailableViews.get(i)).clear();
                    i++;
                }
            }
        }
    }

    public void focusableViewAvailable(View v) {
        if (this.mDeferFocusableViewAvailableInLayout) {
            View i = v;
            int index = -1;
            while (i != this && i != null) {
                if (i.getParent() == this) {
                    index = indexOfChild(i);
                    break;
                }
                i = (View) i.getParent();
            }
            if (index != -1) {
                ((ArrayList) this.mSortedAvailableViews.get(index)).add(v);
                return;
            }
            return;
        }
        super.focusableViewAvailable(v);
    }
}
