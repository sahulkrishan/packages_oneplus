package android.support.v17.leanback.widget;

import android.content.Context;
import android.support.v17.leanback.R;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class ListRowHoverCardView extends LinearLayout {
    private final TextView mDescriptionView;
    private final TextView mTitleView;

    public ListRowHoverCardView(Context context) {
        this(context, null);
    }

    public ListRowHoverCardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ListRowHoverCardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater.from(context).inflate(R.layout.lb_list_row_hovercard, this);
        this.mTitleView = (TextView) findViewById(R.id.title);
        this.mDescriptionView = (TextView) findViewById(R.id.description);
    }

    public final CharSequence getTitle() {
        return this.mTitleView.getText();
    }

    public final void setTitle(CharSequence text) {
        if (TextUtils.isEmpty(text)) {
            this.mTitleView.setVisibility(8);
            return;
        }
        this.mTitleView.setText(text);
        this.mTitleView.setVisibility(0);
    }

    public final CharSequence getDescription() {
        return this.mDescriptionView.getText();
    }

    public final void setDescription(CharSequence text) {
        if (TextUtils.isEmpty(text)) {
            this.mDescriptionView.setVisibility(8);
            return;
        }
        this.mDescriptionView.setText(text);
        this.mDescriptionView.setVisibility(0);
    }
}
