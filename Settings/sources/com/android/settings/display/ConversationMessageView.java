package com.android.settings.display;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.ConstantState;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.settings.R;
import com.google.common.primitives.Ints;

public class ConversationMessageView extends FrameLayout {
    private TextView mContactIconView;
    private final int mIconBackgroundColor;
    private final CharSequence mIconText;
    private final int mIconTextColor;
    private final boolean mIncoming;
    private LinearLayout mMessageBubble;
    private final CharSequence mMessageText;
    private ViewGroup mMessageTextAndInfoView;
    private TextView mMessageTextView;
    private TextView mStatusTextView;
    private final CharSequence mTimestampText;

    public ConversationMessageView(Context context) {
        this(context, null);
    }

    public ConversationMessageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ConversationMessageView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ConversationMessageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ConversationMessageView);
        this.mIncoming = a.getBoolean(3, true);
        this.mMessageText = a.getString(4);
        this.mTimestampText = a.getString(5);
        this.mIconText = a.getString(1);
        this.mIconTextColor = a.getColor(2, 0);
        this.mIconBackgroundColor = a.getColor(0, 0);
        a.recycle();
        LayoutInflater.from(context).inflate(R.layout.conversation_message_icon, this);
        LayoutInflater.from(context).inflate(R.layout.conversation_message_content, this);
    }

    /* Access modifiers changed, original: protected */
    public void onFinishInflate() {
        this.mMessageBubble = (LinearLayout) findViewById(R.id.message_content);
        this.mMessageTextAndInfoView = (ViewGroup) findViewById(R.id.message_text_and_info);
        this.mMessageTextView = (TextView) findViewById(R.id.message_text);
        this.mStatusTextView = (TextView) findViewById(R.id.message_status);
        this.mContactIconView = (TextView) findViewById(R.id.conversation_icon);
        updateViewContent();
    }

    /* Access modifiers changed, original: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        updateViewAppearance();
        int horizontalSpace = MeasureSpec.getSize(widthMeasureSpec);
        int unspecifiedMeasureSpec = MeasureSpec.makeMeasureSpec(0, 0);
        int iconMeasureSpec = MeasureSpec.makeMeasureSpec(0, 0);
        this.mContactIconView.measure(iconMeasureSpec, iconMeasureSpec);
        iconMeasureSpec = MeasureSpec.makeMeasureSpec(Math.max(this.mContactIconView.getMeasuredWidth(), this.mContactIconView.getMeasuredHeight()), Ints.MAX_POWER_OF_TWO);
        this.mContactIconView.measure(iconMeasureSpec, iconMeasureSpec);
        this.mMessageBubble.measure(MeasureSpec.makeMeasureSpec((((horizontalSpace - (this.mContactIconView.getMeasuredWidth() * 2)) - getResources().getDimensionPixelSize(R.dimen.message_bubble_arrow_width)) - getPaddingLeft()) - getPaddingRight(), Integer.MIN_VALUE), unspecifiedMeasureSpec);
        setMeasuredDimension(horizontalSpace, (getPaddingBottom() + Math.max(this.mContactIconView.getMeasuredHeight(), this.mMessageBubble.getMeasuredHeight())) + getPaddingTop());
    }

    /* Access modifiers changed, original: protected */
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int iconLeft;
        int contentLeft;
        boolean isRtl = isLayoutRtl(this);
        int iconWidth = this.mContactIconView.getMeasuredWidth();
        int iconHeight = this.mContactIconView.getMeasuredHeight();
        int iconTop = getPaddingTop();
        int contentWidth = (((right - left) - iconWidth) - getPaddingLeft()) - getPaddingRight();
        int contentHeight = this.mMessageBubble.getMeasuredHeight();
        int contentTop = iconTop;
        if (this.mIncoming) {
            if (isRtl) {
                iconLeft = ((right - left) - getPaddingRight()) - iconWidth;
                contentLeft = iconLeft - contentWidth;
            } else {
                iconLeft = getPaddingLeft();
                contentLeft = iconLeft + iconWidth;
            }
        } else if (isRtl) {
            iconLeft = getPaddingLeft();
            contentLeft = iconLeft + iconWidth;
        } else {
            iconLeft = ((right - left) - getPaddingRight()) - iconWidth;
            contentLeft = iconLeft - contentWidth;
        }
        this.mContactIconView.layout(iconLeft, iconTop, iconLeft + iconWidth, iconTop + iconHeight);
        this.mMessageBubble.layout(contentLeft, contentTop, contentLeft + contentWidth, contentTop + contentHeight);
    }

    private static boolean isLayoutRtl(View view) {
        return 1 == view.getLayoutDirection();
    }

    private void updateViewContent() {
        this.mMessageTextView.setText(this.mMessageText);
        this.mStatusTextView.setText(this.mTimestampText);
        this.mContactIconView.setText(this.mIconText);
        this.mContactIconView.setTextColor(this.mIconTextColor);
        this.mContactIconView.setBackground(getTintedDrawable(getContext(), getContext().getDrawable(R.drawable.conversation_message_icon), this.mIconBackgroundColor));
    }

    private void updateViewAppearance() {
        int textLeftPadding;
        int textRightPadding;
        int gravity;
        int bubbleDrawableResId;
        int bubbleColorResId;
        Resources res = getResources();
        int arrowWidth = res.getDimensionPixelOffset(R.dimen.message_bubble_arrow_width);
        int messageTextLeftRightPadding = res.getDimensionPixelOffset(R.dimen.message_text_left_right_padding);
        int textTopPadding = res.getDimensionPixelOffset(R.dimen.message_text_top_padding);
        int textBottomPadding = res.getDimensionPixelOffset(R.dimen.message_text_bottom_padding);
        if (this.mIncoming) {
            textLeftPadding = messageTextLeftRightPadding + arrowWidth;
            textRightPadding = messageTextLeftRightPadding;
        } else {
            textLeftPadding = messageTextLeftRightPadding;
            textRightPadding = messageTextLeftRightPadding + arrowWidth;
        }
        if (this.mIncoming) {
            gravity = 8388627;
        } else {
            gravity = 8388629;
        }
        int messageTopPadding = res.getDimensionPixelSize(R.dimen.message_padding_default);
        int metadataTopPadding = res.getDimensionPixelOffset(R.dimen.message_metadata_top_padding);
        if (this.mIncoming) {
            bubbleDrawableResId = R.drawable.msg_bubble_incoming;
        } else {
            bubbleDrawableResId = R.drawable.msg_bubble_outgoing;
        }
        if (this.mIncoming) {
            bubbleColorResId = R.color.message_bubble_incoming;
        } else {
            bubbleColorResId = R.color.message_bubble_outgoing;
        }
        Context context = getContext();
        this.mMessageTextAndInfoView.setBackground(getTintedDrawable(context, context.getDrawable(bubbleDrawableResId), context.getColor(bubbleColorResId)));
        if (isLayoutRtl(this)) {
            this.mMessageTextAndInfoView.setPadding(textRightPadding, textTopPadding + metadataTopPadding, textLeftPadding, textBottomPadding);
        } else {
            this.mMessageTextAndInfoView.setPadding(textLeftPadding, textTopPadding + metadataTopPadding, textRightPadding, textBottomPadding);
        }
        setPadding(getPaddingLeft(), messageTopPadding, getPaddingRight(), 0);
        this.mMessageBubble.setGravity(gravity);
        updateTextAppearance();
    }

    private void updateTextAppearance() {
        int messageColorResId;
        int timestampColorResId;
        if (this.mIncoming) {
            messageColorResId = R.color.message_text_incoming;
        } else {
            messageColorResId = R.color.message_text_outgoing;
        }
        if (this.mIncoming) {
            timestampColorResId = R.color.timestamp_text_incoming;
        } else {
            timestampColorResId = R.color.timestamp_text_outgoing;
        }
        int messageColor = getContext().getColor(messageColorResId);
        this.mMessageTextView.setTextColor(messageColor);
        this.mMessageTextView.setLinkTextColor(messageColor);
        this.mStatusTextView.setTextColor(timestampColorResId);
    }

    private static Drawable getTintedDrawable(Context context, Drawable drawable, int color) {
        Drawable retDrawable;
        ConstantState constantStateDrawable = drawable.getConstantState();
        if (constantStateDrawable != null) {
            retDrawable = constantStateDrawable.newDrawable(context.getResources()).mutate();
        } else {
            retDrawable = drawable;
        }
        retDrawable.setColorFilter(color, Mode.SRC_ATOP);
        return retDrawable;
    }
}
