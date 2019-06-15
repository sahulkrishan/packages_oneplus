package androidx.slice.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.text.SpannableStringBuilder;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.slice.SliceItem;
import androidx.slice.core.SliceQuery;
import androidx.slice.widget.SliceView.OnSliceActionListener;

@RestrictTo({Scope.LIBRARY})
public class MessageView extends SliceChildView {
    private TextView mDetails;
    private ImageView mIcon;
    private int mRowIndex;

    public MessageView(Context context) {
        super(context);
    }

    public int getMode() {
        return 2;
    }

    public void resetView() {
    }

    /* Access modifiers changed, original: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mDetails = (TextView) findViewById(16908304);
        this.mIcon = (ImageView) findViewById(16908294);
    }

    public void setSliceItem(SliceItem slice, boolean isHeader, int index, int rowCount, OnSliceActionListener observer) {
        setSliceActionListener(observer);
        this.mRowIndex = index;
        SliceItem source = SliceQuery.findSubtype(slice, "image", "source");
        if (source != null) {
            int iconSize = (int) TypedValue.applyDimension(1, 24.0f, getContext().getResources().getDisplayMetrics());
            Bitmap iconBm = Bitmap.createBitmap(iconSize, iconSize, Config.ARGB_8888);
            Canvas iconCanvas = new Canvas(iconBm);
            Drawable d = source.getIcon().loadDrawable(getContext());
            d.setBounds(0, 0, iconSize, iconSize);
            d.draw(iconCanvas);
            this.mIcon.setImageBitmap(SliceViewUtil.getCircularBitmap(iconBm));
        }
        SpannableStringBuilder builder = new SpannableStringBuilder();
        for (SliceItem text : SliceQuery.findAll(slice, "text")) {
            if (builder.length() != 0) {
                builder.append(10);
            }
            builder.append(text.getText());
        }
        this.mDetails.setText(builder.toString());
    }
}
