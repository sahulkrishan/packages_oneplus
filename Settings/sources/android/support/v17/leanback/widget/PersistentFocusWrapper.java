package android.support.v17.leanback.widget;

import android.content.Context;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.BaseSavedState;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import java.util.ArrayList;

class PersistentFocusWrapper extends FrameLayout {
    private static final boolean DEBUG = false;
    private static final String TAG = "PersistentFocusWrapper";
    private boolean mPersistFocusVertical = true;
    private int mSelectedPosition = -1;

    static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        int mSelectedPosition;

        SavedState(Parcel in) {
            super(in);
            this.mSelectedPosition = in.readInt();
        }

        SavedState(Parcelable superState) {
            super(superState);
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.mSelectedPosition);
        }
    }

    public PersistentFocusWrapper(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PersistentFocusWrapper(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /* Access modifiers changed, original: 0000 */
    public int getGrandChildCount() {
        ViewGroup wrapper = (ViewGroup) getChildAt(0);
        if (wrapper == null) {
            return 0;
        }
        return wrapper.getChildCount();
    }

    public void clearSelection() {
        this.mSelectedPosition = -1;
        if (hasFocus()) {
            clearFocus();
        }
    }

    public void persistFocusVertical() {
        this.mPersistFocusVertical = true;
    }

    public void persistFocusHorizontal() {
        this.mPersistFocusVertical = false;
    }

    private boolean shouldPersistFocusFromDirection(int direction) {
        return (this.mPersistFocusVertical && (direction == 33 || direction == 130)) || (!this.mPersistFocusVertical && (direction == 17 || direction == 66));
    }

    public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {
        if (hasFocus() || getGrandChildCount() == 0 || !shouldPersistFocusFromDirection(direction)) {
            super.addFocusables(views, direction, focusableMode);
        } else {
            views.add(this);
        }
    }

    public void requestChildFocus(View child, View focused) {
        super.requestChildFocus(child, focused);
        View view = focused;
        while (view != null && view.getParent() != child) {
            view = (View) view.getParent();
        }
        this.mSelectedPosition = view == null ? -1 : ((ViewGroup) child).indexOfChild(view);
    }

    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
        ViewGroup wrapper = (ViewGroup) getChildAt(0);
        if (wrapper == null || this.mSelectedPosition < 0 || this.mSelectedPosition >= getGrandChildCount() || !wrapper.getChildAt(this.mSelectedPosition).requestFocus(direction, previouslyFocusedRect)) {
            return super.requestFocus(direction, previouslyFocusedRect);
        }
        return true;
    }

    /* Access modifiers changed, original: protected */
    public Parcelable onSaveInstanceState() {
        SavedState savedState = new SavedState(super.onSaveInstanceState());
        savedState.mSelectedPosition = this.mSelectedPosition;
        return savedState;
    }

    /* Access modifiers changed, original: protected */
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            SavedState savedState = (SavedState) state;
            this.mSelectedPosition = ((SavedState) state).mSelectedPosition;
            super.onRestoreInstanceState(savedState.getSuperState());
            return;
        }
        super.onRestoreInstanceState(state);
    }
}
