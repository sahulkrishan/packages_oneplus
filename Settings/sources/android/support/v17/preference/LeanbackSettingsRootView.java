package android.support.v17.preference;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View.OnKeyListener;
import android.widget.FrameLayout;

@RestrictTo({Scope.LIBRARY_GROUP})
public class LeanbackSettingsRootView extends FrameLayout {
    private OnKeyListener mOnBackKeyListener;

    public LeanbackSettingsRootView(Context context) {
        super(context);
    }

    public LeanbackSettingsRootView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LeanbackSettingsRootView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setOnBackKeyListener(OnKeyListener backKeyListener) {
        this.mOnBackKeyListener = backKeyListener;
    }

    public boolean dispatchKeyEvent(@NonNull KeyEvent event) {
        boolean handled = false;
        if (event.getAction() == 1 && event.getKeyCode() == 4 && this.mOnBackKeyListener != null) {
            handled = this.mOnBackKeyListener.onKey(this, event.getKeyCode(), event);
        }
        if (handled || super.dispatchKeyEvent(event)) {
            return true;
        }
        return false;
    }
}
