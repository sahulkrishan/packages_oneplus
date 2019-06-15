package android.support.v17.leanback.widget;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.os.Build.VERSION;
import android.support.v17.leanback.R;
import android.support.v4.widget.TextViewCompat;
import android.text.SpannableStringBuilder;
import android.text.SpannedString;
import android.text.style.ForegroundColorSpan;
import android.text.style.ReplacementSpan;
import android.util.AttributeSet;
import android.util.Property;
import android.view.ActionMode.Callback;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.EditText;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class StreamingTextView extends EditText {
    static final boolean ANIMATE_DOTS_FOR_PENDING = true;
    private static final boolean DEBUG = false;
    private static final boolean DOTS_FOR_PENDING = true;
    private static final boolean DOTS_FOR_STABLE = false;
    private static final Pattern SPLIT_PATTERN = Pattern.compile("\\S+");
    private static final Property<StreamingTextView, Integer> STREAM_POSITION_PROPERTY = new Property<StreamingTextView, Integer>(Integer.class, "streamPosition") {
        public Integer get(StreamingTextView view) {
            return Integer.valueOf(view.getStreamPosition());
        }

        public void set(StreamingTextView view, Integer value) {
            view.setStreamPosition(value.intValue());
        }
    };
    private static final long STREAM_UPDATE_DELAY_MILLIS = 50;
    private static final String TAG = "StreamingTextView";
    private static final float TEXT_DOT_SCALE = 1.3f;
    Bitmap mOneDot;
    final Random mRandom = new Random();
    int mStreamPosition;
    private ObjectAnimator mStreamingAnimation;
    Bitmap mTwoDot;

    private class DottySpan extends ReplacementSpan {
        private final int mPosition;
        private final int mSeed;

        public DottySpan(int seed, int pos) {
            this.mSeed = seed;
            this.mPosition = pos;
        }

        public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
            Canvas canvas2 = canvas;
            Paint paint2 = paint;
            int width = (int) paint2.measureText(text, start, end);
            int dotWidth = StreamingTextView.this.mOneDot.getWidth();
            int sliceWidth = 2 * dotWidth;
            int sliceCount = width / sliceWidth;
            int prop = (width % sliceWidth) / 2;
            boolean rtl = StreamingTextView.isLayoutRtl(StreamingTextView.this);
            StreamingTextView.this.mRandom.setSeed((long) this.mSeed);
            int oldAlpha = paint.getAlpha();
            int i = 0;
            while (i < sliceCount) {
                int i2;
                if (this.mPosition + i >= StreamingTextView.this.mStreamPosition) {
                    i2 = width;
                    break;
                }
                int i3;
                float left = (float) (((i * sliceWidth) + prop) + (dotWidth / 2));
                if (rtl) {
                    i2 = width;
                    i3 = ((x + ((float) width)) - left) - ((float) dotWidth);
                } else {
                    i2 = width;
                    i3 = x + left;
                }
                width = i3;
                paint2.setAlpha((StreamingTextView.this.mRandom.nextInt(4) + 1) * 63);
                if (StreamingTextView.this.mRandom.nextBoolean()) {
                    canvas2.drawBitmap(StreamingTextView.this.mTwoDot, width, (float) (y - StreamingTextView.this.mTwoDot.getHeight()), paint2);
                } else {
                    canvas2.drawBitmap(StreamingTextView.this.mOneDot, width, (float) (y - StreamingTextView.this.mOneDot.getHeight()), paint2);
                }
                i++;
                width = i2;
                int i4 = end;
            }
            paint2.setAlpha(oldAlpha);
        }

        public int getSize(Paint paint, CharSequence text, int start, int end, FontMetricsInt fontMetricsInt) {
            return (int) paint.measureText(text, start, end);
        }
    }

    public StreamingTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StreamingTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /* Access modifiers changed, original: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mOneDot = getScaledBitmap(R.drawable.lb_text_dot_one, TEXT_DOT_SCALE);
        this.mTwoDot = getScaledBitmap(R.drawable.lb_text_dot_two, TEXT_DOT_SCALE);
        reset();
    }

    private Bitmap getScaledBitmap(int resourceId, float scaled) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resourceId);
        return Bitmap.createScaledBitmap(bitmap, (int) (((float) bitmap.getWidth()) * scaled), (int) (((float) bitmap.getHeight()) * scaled), false);
    }

    public void reset() {
        this.mStreamPosition = -1;
        cancelStreamAnimation();
        setText("");
    }

    public void updateRecognizedText(String stableText, String pendingText) {
        if (stableText == null) {
            stableText = "";
        }
        SpannableStringBuilder displayText = new SpannableStringBuilder(stableText);
        if (pendingText != null) {
            int pendingTextStart = displayText.length();
            displayText.append(pendingText);
            addDottySpans(displayText, pendingText, pendingTextStart);
        }
        this.mStreamPosition = Math.max(stableText.length(), this.mStreamPosition);
        updateText(new SpannedString(displayText));
        startStreamAnimation();
    }

    /* Access modifiers changed, original: 0000 */
    public int getStreamPosition() {
        return this.mStreamPosition;
    }

    /* Access modifiers changed, original: 0000 */
    public void setStreamPosition(int streamPosition) {
        this.mStreamPosition = streamPosition;
        invalidate();
    }

    private void startStreamAnimation() {
        cancelStreamAnimation();
        int pos = getStreamPosition();
        int animLen = length() - pos;
        if (animLen > 0) {
            if (this.mStreamingAnimation == null) {
                this.mStreamingAnimation = new ObjectAnimator();
                this.mStreamingAnimation.setTarget(this);
                this.mStreamingAnimation.setProperty(STREAM_POSITION_PROPERTY);
            }
            this.mStreamingAnimation.setIntValues(new int[]{pos, totalLen});
            this.mStreamingAnimation.setDuration(STREAM_UPDATE_DELAY_MILLIS * ((long) animLen));
            this.mStreamingAnimation.start();
        }
    }

    private void cancelStreamAnimation() {
        if (this.mStreamingAnimation != null) {
            this.mStreamingAnimation.cancel();
        }
    }

    private void addDottySpans(SpannableStringBuilder displayText, String text, int textStart) {
        Matcher m = SPLIT_PATTERN.matcher(text);
        while (m.find()) {
            int wordStart = m.start() + textStart;
            displayText.setSpan(new DottySpan(text.charAt(m.start()), wordStart), wordStart, m.end() + textStart, 33);
        }
    }

    private void addColorSpan(SpannableStringBuilder displayText, int color, String text, int textStart) {
        displayText.setSpan(new ForegroundColorSpan(color), textStart, text.length() + textStart, 33);
    }

    public void setFinalRecognizedText(CharSequence finalText) {
        updateText(finalText);
    }

    private void updateText(CharSequence displayText) {
        setText(displayText);
        bringPointIntoView(length());
    }

    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(StreamingTextView.class.getCanonicalName());
    }

    public static boolean isLayoutRtl(View view) {
        boolean z = false;
        if (VERSION.SDK_INT < 17) {
            return false;
        }
        if (1 == view.getLayoutDirection()) {
            z = true;
        }
        return z;
    }

    public void updateRecognizedText(String stableText, List<Float> list) {
    }

    public void setCustomSelectionActionModeCallback(Callback actionModeCallback) {
        super.setCustomSelectionActionModeCallback(TextViewCompat.wrapCustomSelectionActionModeCallback(this, actionModeCallback));
    }
}
