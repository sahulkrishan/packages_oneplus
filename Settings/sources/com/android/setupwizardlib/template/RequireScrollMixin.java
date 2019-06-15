package com.android.setupwizardlib.template;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.android.setupwizardlib.TemplateLayout;
import com.android.setupwizardlib.view.NavigationBar;

public class RequireScrollMixin implements Mixin {
    private ScrollHandlingDelegate mDelegate;
    private boolean mEverScrolledToBottom = false;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    @Nullable
    private OnRequireScrollStateChangedListener mListener;
    private boolean mRequiringScrollToBottom = false;
    @NonNull
    private final TemplateLayout mTemplateLayout;

    public interface OnRequireScrollStateChangedListener {
        void onRequireScrollStateChanged(boolean z);
    }

    interface ScrollHandlingDelegate {
        void pageScrollDown();

        void startListening();
    }

    public RequireScrollMixin(@NonNull TemplateLayout templateLayout) {
        this.mTemplateLayout = templateLayout;
    }

    public void setScrollHandlingDelegate(@NonNull ScrollHandlingDelegate delegate) {
        this.mDelegate = delegate;
    }

    public void setOnRequireScrollStateChangedListener(@Nullable OnRequireScrollStateChangedListener listener) {
        this.mListener = listener;
    }

    public OnRequireScrollStateChangedListener getOnRequireScrollStateChangedListener() {
        return this.mListener;
    }

    public OnClickListener createOnClickListener(@Nullable final OnClickListener listener) {
        return new OnClickListener() {
            public void onClick(View view) {
                if (RequireScrollMixin.this.mRequiringScrollToBottom) {
                    RequireScrollMixin.this.mDelegate.pageScrollDown();
                } else if (listener != null) {
                    listener.onClick(view);
                }
            }
        };
    }

    public void requireScrollWithNavigationBar(@NonNull final NavigationBar navigationBar) {
        setOnRequireScrollStateChangedListener(new OnRequireScrollStateChangedListener() {
            public void onRequireScrollStateChanged(boolean scrollNeeded) {
                int i = 8;
                navigationBar.getMoreButton().setVisibility(scrollNeeded ? 0 : 8);
                Button nextButton = navigationBar.getNextButton();
                if (!scrollNeeded) {
                    i = 0;
                }
                nextButton.setVisibility(i);
            }
        });
        navigationBar.getMoreButton().setOnClickListener(createOnClickListener(null));
        requireScroll();
    }

    public void requireScrollWithButton(@NonNull Button button, @StringRes int moreText, @Nullable OnClickListener onClickListener) {
        requireScrollWithButton(button, button.getContext().getText(moreText), onClickListener);
    }

    public void requireScrollWithButton(@NonNull final Button button, final CharSequence moreText, @Nullable OnClickListener onClickListener) {
        final CharSequence nextText = button.getText();
        button.setOnClickListener(createOnClickListener(onClickListener));
        setOnRequireScrollStateChangedListener(new OnRequireScrollStateChangedListener() {
            public void onRequireScrollStateChanged(boolean scrollNeeded) {
                button.setText(scrollNeeded ? moreText : nextText);
            }
        });
        requireScroll();
    }

    public boolean isScrollingRequired() {
        return this.mRequiringScrollToBottom;
    }

    public void requireScroll() {
        this.mDelegate.startListening();
    }

    /* Access modifiers changed, original: 0000 */
    public void notifyScrollabilityChange(boolean canScrollDown) {
        if (canScrollDown != this.mRequiringScrollToBottom) {
            if (!canScrollDown) {
                postScrollStateChange(false);
                this.mRequiringScrollToBottom = false;
                this.mEverScrolledToBottom = true;
            } else if (!this.mEverScrolledToBottom) {
                postScrollStateChange(true);
                this.mRequiringScrollToBottom = true;
            }
        }
    }

    private void postScrollStateChange(final boolean scrollNeeded) {
        this.mHandler.post(new Runnable() {
            public void run() {
                if (RequireScrollMixin.this.mListener != null) {
                    RequireScrollMixin.this.mListener.onRequireScrollStateChanged(scrollNeeded);
                }
            }
        });
    }
}
