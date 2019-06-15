package com.android.setupwizardlib.items;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.android.setupwizardlib.R;

public class ButtonItem extends AbstractItem implements android.view.View.OnClickListener {
    private Button mButton;
    private boolean mEnabled = true;
    private OnClickListener mListener;
    private CharSequence mText;
    private int mTheme = R.style.SuwButtonItem;

    public interface OnClickListener {
        void onClick(ButtonItem buttonItem);
    }

    public ButtonItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SuwButtonItem);
        this.mEnabled = a.getBoolean(R.styleable.SuwButtonItem_android_enabled, true);
        this.mText = a.getText(R.styleable.SuwButtonItem_android_text);
        this.mTheme = a.getResourceId(R.styleable.SuwButtonItem_android_theme, R.style.SuwButtonItem);
        a.recycle();
    }

    public void setOnClickListener(OnClickListener listener) {
        this.mListener = listener;
    }

    public void setText(CharSequence text) {
        this.mText = text;
    }

    public CharSequence getText() {
        return this.mText;
    }

    public void setTheme(int theme) {
        this.mTheme = theme;
        this.mButton = null;
    }

    public int getTheme() {
        return this.mTheme;
    }

    public void setEnabled(boolean enabled) {
        this.mEnabled = enabled;
    }

    public int getCount() {
        return 0;
    }

    public boolean isEnabled() {
        return this.mEnabled;
    }

    public int getLayoutResource() {
        return 0;
    }

    public final void onBindView(View view) {
        throw new UnsupportedOperationException("Cannot bind to ButtonItem's view");
    }

    /* Access modifiers changed, original: protected */
    public Button createButton(ViewGroup parent) {
        if (this.mButton == null) {
            Context context = parent.getContext();
            if (this.mTheme != 0) {
                context = new ContextThemeWrapper(context, this.mTheme);
            }
            this.mButton = createButton(context);
            this.mButton.setOnClickListener(this);
        } else if (this.mButton.getParent() instanceof ViewGroup) {
            ((ViewGroup) this.mButton.getParent()).removeView(this.mButton);
        }
        this.mButton.setEnabled(this.mEnabled);
        this.mButton.setText(this.mText);
        this.mButton.setId(getViewId());
        return this.mButton;
    }

    public void onClick(View v) {
        if (this.mListener != null) {
            this.mListener.onClick(this);
        }
    }

    @SuppressLint({"InflateParams"})
    private Button createButton(Context context) {
        return (Button) LayoutInflater.from(context).inflate(R.layout.suw_button, null, false);
    }
}
