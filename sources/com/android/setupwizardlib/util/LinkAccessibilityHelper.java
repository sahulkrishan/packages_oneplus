package com.android.setupwizardlib.util;

import android.graphics.Rect;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v4.view.AccessibilityDelegateCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v4.view.accessibility.AccessibilityNodeProviderCompat;
import android.support.v4.widget.ExploreByTouchHelper;
import android.text.Layout;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.TextView;
import java.util.List;

public class LinkAccessibilityHelper extends AccessibilityDelegateCompat {
    private static final String TAG = "LinkAccessibilityHelper";
    private final AccessibilityDelegateCompat mDelegate;

    @VisibleForTesting
    static class PreOLinkAccessibilityHelper extends ExploreByTouchHelper {
        private final Rect mTempRect = new Rect();
        private final TextView mView;

        PreOLinkAccessibilityHelper(TextView view) {
            super(view);
            this.mView = view;
        }

        /* Access modifiers changed, original: protected */
        public int getVirtualViewAt(float x, float y) {
            CharSequence text = this.mView.getText();
            if (text instanceof Spanned) {
                Spanned spannedText = (Spanned) text;
                int offset = getOffsetForPosition(this.mView, x, y);
                ClickableSpan[] linkSpans = (ClickableSpan[]) spannedText.getSpans(offset, offset, ClickableSpan.class);
                if (linkSpans.length == 1) {
                    return spannedText.getSpanStart(linkSpans[null]);
                }
            }
            return Integer.MIN_VALUE;
        }

        /* Access modifiers changed, original: protected */
        public void getVisibleVirtualViews(List<Integer> virtualViewIds) {
            CharSequence text = this.mView.getText();
            if (text instanceof Spanned) {
                Spanned spannedText = (Spanned) text;
                int i = 0;
                ClickableSpan[] linkSpans = (ClickableSpan[]) spannedText.getSpans(0, spannedText.length(), ClickableSpan.class);
                int length = linkSpans.length;
                while (i < length) {
                    virtualViewIds.add(Integer.valueOf(spannedText.getSpanStart(linkSpans[i])));
                    i++;
                }
            }
        }

        /* Access modifiers changed, original: protected */
        public void onPopulateEventForVirtualView(int virtualViewId, AccessibilityEvent event) {
            ClickableSpan span = getSpanForOffset(virtualViewId);
            if (span != null) {
                event.setContentDescription(getTextForSpan(span));
                return;
            }
            String str = LinkAccessibilityHelper.TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("LinkSpan is null for offset: ");
            stringBuilder.append(virtualViewId);
            Log.e(str, stringBuilder.toString());
            event.setContentDescription(this.mView.getText());
        }

        /* Access modifiers changed, original: protected */
        public void onPopulateNodeForVirtualView(int virtualViewId, AccessibilityNodeInfoCompat info) {
            ClickableSpan span = getSpanForOffset(virtualViewId);
            if (span != null) {
                info.setContentDescription(getTextForSpan(span));
            } else {
                String str = LinkAccessibilityHelper.TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("LinkSpan is null for offset: ");
                stringBuilder.append(virtualViewId);
                Log.e(str, stringBuilder.toString());
                info.setContentDescription(this.mView.getText());
            }
            info.setFocusable(true);
            info.setClickable(true);
            getBoundsForSpan(span, this.mTempRect);
            if (this.mTempRect.isEmpty()) {
                String str2 = LinkAccessibilityHelper.TAG;
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append("LinkSpan bounds is empty for: ");
                stringBuilder2.append(virtualViewId);
                Log.e(str2, stringBuilder2.toString());
                this.mTempRect.set(0, 0, 1, 1);
            }
            info.setBoundsInParent(this.mTempRect);
            info.addAction(16);
        }

        /* Access modifiers changed, original: protected */
        public boolean onPerformActionForVirtualView(int virtualViewId, int action, Bundle arguments) {
            if (action == 16) {
                ClickableSpan span = getSpanForOffset(virtualViewId);
                if (span != null) {
                    span.onClick(this.mView);
                    return true;
                }
                String str = LinkAccessibilityHelper.TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("LinkSpan is null for offset: ");
                stringBuilder.append(virtualViewId);
                Log.e(str, stringBuilder.toString());
            }
            return false;
        }

        private ClickableSpan getSpanForOffset(int offset) {
            CharSequence text = this.mView.getText();
            if (text instanceof Spanned) {
                ClickableSpan[] spans = (ClickableSpan[]) ((Spanned) text).getSpans(offset, offset, ClickableSpan.class);
                if (spans.length == 1) {
                    return spans[0];
                }
            }
            return null;
        }

        private CharSequence getTextForSpan(ClickableSpan span) {
            CharSequence text = this.mView.getText();
            if (!(text instanceof Spanned)) {
                return text;
            }
            Spanned spannedText = (Spanned) text;
            return spannedText.subSequence(spannedText.getSpanStart(span), spannedText.getSpanEnd(span));
        }

        private Rect getBoundsForSpan(ClickableSpan span, Rect outRect) {
            CharSequence text = this.mView.getText();
            outRect.setEmpty();
            if (text instanceof Spanned) {
                Layout layout = this.mView.getLayout();
                if (layout != null) {
                    Spanned spannedText = (Spanned) text;
                    int spanStart = spannedText.getSpanStart(span);
                    int spanEnd = spannedText.getSpanEnd(span);
                    float xStart = layout.getPrimaryHorizontal(spanStart);
                    float xEnd = layout.getPrimaryHorizontal(spanEnd);
                    int lineStart = layout.getLineForOffset(spanStart);
                    int lineEnd = layout.getLineForOffset(spanEnd);
                    layout.getLineBounds(lineStart, outRect);
                    if (lineEnd == lineStart) {
                        outRect.left = (int) Math.min(xStart, xEnd);
                        outRect.right = (int) Math.max(xStart, xEnd);
                    } else if (layout.getParagraphDirection(lineStart) == -1) {
                        outRect.right = (int) xStart;
                    } else {
                        outRect.left = (int) xStart;
                    }
                    outRect.offset(this.mView.getTotalPaddingLeft(), this.mView.getTotalPaddingTop());
                }
            }
            return outRect;
        }

        private static int getOffsetForPosition(TextView view, float x, float y) {
            if (view.getLayout() == null) {
                return -1;
            }
            return getOffsetAtCoordinate(view, getLineAtCoordinate(view, y), x);
        }

        private static float convertToLocalHorizontalCoordinate(TextView view, float x) {
            return Math.min((float) ((view.getWidth() - view.getTotalPaddingRight()) - 1), Math.max(0.0f, x - ((float) view.getTotalPaddingLeft()))) + ((float) view.getScrollX());
        }

        private static int getLineAtCoordinate(TextView view, float y) {
            return view.getLayout().getLineForVertical((int) (Math.min((float) ((view.getHeight() - view.getTotalPaddingBottom()) - 1), Math.max(0.0f, y - ((float) view.getTotalPaddingTop()))) + ((float) view.getScrollY())));
        }

        private static int getOffsetAtCoordinate(TextView view, int line, float x) {
            return view.getLayout().getOffsetForHorizontal(line, convertToLocalHorizontalCoordinate(view, x));
        }
    }

    public LinkAccessibilityHelper(TextView view) {
        AccessibilityDelegateCompat accessibilityDelegateCompat;
        if (VERSION.SDK_INT >= 26) {
            accessibilityDelegateCompat = new AccessibilityDelegateCompat();
        } else {
            accessibilityDelegateCompat = new PreOLinkAccessibilityHelper(view);
        }
        this(accessibilityDelegateCompat);
    }

    @VisibleForTesting
    LinkAccessibilityHelper(@NonNull AccessibilityDelegateCompat delegate) {
        this.mDelegate = delegate;
    }

    public void sendAccessibilityEvent(View host, int eventType) {
        this.mDelegate.sendAccessibilityEvent(host, eventType);
    }

    public void sendAccessibilityEventUnchecked(View host, AccessibilityEvent event) {
        this.mDelegate.sendAccessibilityEventUnchecked(host, event);
    }

    public boolean dispatchPopulateAccessibilityEvent(View host, AccessibilityEvent event) {
        return this.mDelegate.dispatchPopulateAccessibilityEvent(host, event);
    }

    public void onPopulateAccessibilityEvent(View host, AccessibilityEvent event) {
        this.mDelegate.onPopulateAccessibilityEvent(host, event);
    }

    public void onInitializeAccessibilityEvent(View host, AccessibilityEvent event) {
        this.mDelegate.onInitializeAccessibilityEvent(host, event);
    }

    public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfoCompat info) {
        this.mDelegate.onInitializeAccessibilityNodeInfo(host, info);
    }

    public boolean onRequestSendAccessibilityEvent(ViewGroup host, View child, AccessibilityEvent event) {
        return this.mDelegate.onRequestSendAccessibilityEvent(host, child, event);
    }

    public AccessibilityNodeProviderCompat getAccessibilityNodeProvider(View host) {
        return this.mDelegate.getAccessibilityNodeProvider(host);
    }

    public boolean performAccessibilityAction(View host, int action, Bundle args) {
        return this.mDelegate.performAccessibilityAction(host, action, args);
    }

    public final boolean dispatchHoverEvent(MotionEvent event) {
        return (this.mDelegate instanceof ExploreByTouchHelper) && ((ExploreByTouchHelper) this.mDelegate).dispatchHoverEvent(event);
    }
}
