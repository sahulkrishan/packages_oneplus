package android.support.v17.leanback.app;

import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v17.leanback.R;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ErrorSupportFragment extends BrandedSupportFragment {
    private Drawable mBackgroundDrawable;
    private Button mButton;
    private OnClickListener mButtonClickListener;
    private String mButtonText;
    private Drawable mDrawable;
    private ViewGroup mErrorFrame;
    private ImageView mImageView;
    private boolean mIsBackgroundTranslucent = true;
    private CharSequence mMessage;
    private TextView mTextView;

    public void setDefaultBackground(boolean translucent) {
        this.mBackgroundDrawable = null;
        this.mIsBackgroundTranslucent = translucent;
        updateBackground();
        updateMessage();
    }

    public boolean isBackgroundTranslucent() {
        return this.mIsBackgroundTranslucent;
    }

    public void setBackgroundDrawable(Drawable drawable) {
        this.mBackgroundDrawable = drawable;
        if (drawable != null) {
            int opacity = drawable.getOpacity();
            boolean z = opacity == -3 || opacity == -2;
            this.mIsBackgroundTranslucent = z;
        }
        updateBackground();
        updateMessage();
    }

    public Drawable getBackgroundDrawable() {
        return this.mBackgroundDrawable;
    }

    public void setImageDrawable(Drawable drawable) {
        this.mDrawable = drawable;
        updateImageDrawable();
    }

    public Drawable getImageDrawable() {
        return this.mDrawable;
    }

    public void setMessage(CharSequence message) {
        this.mMessage = message;
        updateMessage();
    }

    public CharSequence getMessage() {
        return this.mMessage;
    }

    public void setButtonText(String text) {
        this.mButtonText = text;
        updateButton();
    }

    public String getButtonText() {
        return this.mButtonText;
    }

    public void setButtonClickListener(OnClickListener clickListener) {
        this.mButtonClickListener = clickListener;
        updateButton();
    }

    public OnClickListener getButtonClickListener() {
        return this.mButtonClickListener;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.lb_error_fragment, container, false);
        this.mErrorFrame = (ViewGroup) root.findViewById(R.id.error_frame);
        updateBackground();
        installTitleView(inflater, this.mErrorFrame, savedInstanceState);
        this.mImageView = (ImageView) root.findViewById(R.id.image);
        updateImageDrawable();
        this.mTextView = (TextView) root.findViewById(R.id.message);
        updateMessage();
        this.mButton = (Button) root.findViewById(R.id.button);
        updateButton();
        FontMetricsInt metrics = getFontMetricsInt(this.mTextView);
        setTopMargin(this.mTextView, metrics.ascent + container.getResources().getDimensionPixelSize(R.dimen.lb_error_under_image_baseline_margin));
        setTopMargin(this.mButton, container.getResources().getDimensionPixelSize(R.dimen.lb_error_under_message_baseline_margin) - metrics.descent);
        return root;
    }

    private void updateBackground() {
        if (this.mErrorFrame == null) {
            return;
        }
        if (this.mBackgroundDrawable != null) {
            this.mErrorFrame.setBackground(this.mBackgroundDrawable);
        } else {
            this.mErrorFrame.setBackgroundColor(this.mErrorFrame.getResources().getColor(this.mIsBackgroundTranslucent ? R.color.lb_error_background_color_translucent : R.color.lb_error_background_color_opaque));
        }
    }

    private void updateMessage() {
        if (this.mTextView != null) {
            this.mTextView.setText(this.mMessage);
            this.mTextView.setVisibility(TextUtils.isEmpty(this.mMessage) ? 8 : 0);
        }
    }

    private void updateImageDrawable() {
        if (this.mImageView != null) {
            this.mImageView.setImageDrawable(this.mDrawable);
            this.mImageView.setVisibility(this.mDrawable == null ? 8 : 0);
        }
    }

    private void updateButton() {
        if (this.mButton != null) {
            this.mButton.setText(this.mButtonText);
            this.mButton.setOnClickListener(this.mButtonClickListener);
            this.mButton.setVisibility(TextUtils.isEmpty(this.mButtonText) ? 8 : 0);
            this.mButton.requestFocus();
        }
    }

    public void onStart() {
        super.onStart();
        this.mErrorFrame.requestFocus();
    }

    private static FontMetricsInt getFontMetricsInt(TextView textView) {
        Paint paint = new Paint(1);
        paint.setTextSize(textView.getTextSize());
        paint.setTypeface(textView.getTypeface());
        return paint.getFontMetricsInt();
    }

    private static void setTopMargin(TextView textView, int topMargin) {
        MarginLayoutParams lp = (MarginLayoutParams) textView.getLayoutParams();
        lp.topMargin = topMargin;
        textView.setLayoutParams(lp);
    }
}
