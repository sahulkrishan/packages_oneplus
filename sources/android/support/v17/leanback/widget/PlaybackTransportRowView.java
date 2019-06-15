package android.support.v17.leanback.widget;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v17.leanback.R;
import android.util.AttributeSet;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

@RestrictTo({Scope.LIBRARY_GROUP})
public class PlaybackTransportRowView extends LinearLayout {
    private OnUnhandledKeyListener mOnUnhandledKeyListener;

    @RestrictTo({Scope.LIBRARY_GROUP})
    public interface OnUnhandledKeyListener {
        boolean onUnhandledKey(KeyEvent keyEvent);
    }

    public PlaybackTransportRowView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PlaybackTransportRowView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /* Access modifiers changed, original: 0000 */
    public void setOnUnhandledKeyListener(OnUnhandledKeyListener listener) {
        this.mOnUnhandledKeyListener = listener;
    }

    /* Access modifiers changed, original: 0000 */
    public OnUnhandledKeyListener getOnUnhandledKeyListener() {
        return this.mOnUnhandledKeyListener;
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean z = true;
        if (super.dispatchKeyEvent(event)) {
            return true;
        }
        if (this.mOnUnhandledKeyListener == null || !this.mOnUnhandledKeyListener.onUnhandledKey(event)) {
            z = false;
        }
        return z;
    }

    /* Access modifiers changed, original: protected */
    public boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
        View focused = findFocus();
        if (focused != null && focused.requestFocus(direction, previouslyFocusedRect)) {
            return true;
        }
        View progress = findViewById(R.id.playback_progress);
        if (progress != null && progress.isFocusable() && progress.requestFocus(direction, previouslyFocusedRect)) {
            return true;
        }
        return super.onRequestFocusInDescendants(direction, previouslyFocusedRect);
    }

    public View focusSearch(View focused, int direction) {
        if (focused != null) {
            int index;
            View view;
            if (direction == 33) {
                for (index = indexOfChild(getFocusedChild()) - 1; index >= 0; index--) {
                    view = getChildAt(index);
                    if (view.hasFocusable()) {
                        return view;
                    }
                }
            } else if (direction == 130) {
                index = indexOfChild(getFocusedChild());
                while (true) {
                    index++;
                    if (index >= getChildCount()) {
                        break;
                    }
                    view = getChildAt(index);
                    if (view.hasFocusable()) {
                        return view;
                    }
                }
            } else if ((direction == 17 || direction == 66) && (getFocusedChild() instanceof ViewGroup)) {
                return FocusFinder.getInstance().findNextFocus((ViewGroup) getFocusedChild(), focused, direction);
            }
        }
        return super.focusSearch(focused, direction);
    }

    public boolean hasOverlappingRendering() {
        return false;
    }
}
