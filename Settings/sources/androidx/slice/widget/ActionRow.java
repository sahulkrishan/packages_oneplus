package androidx.slice.widget;

import android.app.PendingIntent.CanceledException;
import android.app.RemoteInput;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build.VERSION;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v4.graphics.drawable.IconCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ImageViewCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.slice.SliceItem;
import androidx.slice.core.SliceAction;
import androidx.slice.core.SliceActionImpl;
import androidx.slice.core.SliceQuery;
import java.util.Iterator;
import java.util.List;

@RestrictTo({Scope.LIBRARY})
public class ActionRow extends FrameLayout {
    private static final int MAX_ACTIONS = 5;
    private static final String TAG = "ActionRow";
    private final LinearLayout mActionsGroup;
    private int mColor = ViewCompat.MEASURED_STATE_MASK;
    private final boolean mFullActions;
    private final int mIconPadding;
    private final int mSize;

    public ActionRow(Context context, boolean fullActions) {
        super(context);
        this.mFullActions = fullActions;
        this.mSize = (int) TypedValue.applyDimension(1, 48.0f, context.getResources().getDisplayMetrics());
        this.mIconPadding = (int) TypedValue.applyDimension(1, 12.0f, context.getResources().getDisplayMetrics());
        this.mActionsGroup = new LinearLayout(context);
        this.mActionsGroup.setOrientation(0);
        this.mActionsGroup.setLayoutParams(new LayoutParams(-1, -2));
        addView(this.mActionsGroup);
    }

    private void setColor(int color) {
        this.mColor = color;
        for (int i = 0; i < this.mActionsGroup.getChildCount(); i++) {
            View view = this.mActionsGroup.getChildAt(i);
            if (((Integer) view.getTag()).intValue() == 0) {
                ImageViewCompat.setImageTintList((ImageView) view, ColorStateList.valueOf(this.mColor));
            }
        }
    }

    private ImageView addAction(IconCompat icon, boolean allowTint) {
        ImageView imageView = new ImageView(getContext());
        imageView.setPadding(this.mIconPadding, this.mIconPadding, this.mIconPadding, this.mIconPadding);
        imageView.setScaleType(ScaleType.FIT_CENTER);
        imageView.setImageDrawable(icon.loadDrawable(getContext()));
        if (allowTint) {
            ImageViewCompat.setImageTintList(imageView, ColorStateList.valueOf(this.mColor));
        }
        imageView.setBackground(SliceViewUtil.getDrawable(getContext(), 16843534));
        imageView.setTag(Boolean.valueOf(allowTint));
        addAction(imageView);
        return imageView;
    }

    public void setActions(@NonNull List<SliceAction> actions, int color) {
        removeAllViews();
        this.mActionsGroup.removeAllViews();
        addView(this.mActionsGroup);
        if (color != -1) {
            setColor(color);
        }
        Iterator it = actions.iterator();
        while (true) {
            int tint = 0;
            if (it.hasNext()) {
                SliceAction action = (SliceAction) it.next();
                if (this.mActionsGroup.getChildCount() < 5) {
                    SliceItem sliceItem = ((SliceActionImpl) action).getSliceItem();
                    final SliceItem actionItem = ((SliceActionImpl) action).getActionItem();
                    SliceItem input = SliceQuery.find(sliceItem, "input");
                    SliceItem image = SliceQuery.find(sliceItem, "image");
                    if (input == null || image == null) {
                        if (action.getIcon() != null) {
                            IconCompat iconItem = action.getIcon();
                            if (!(iconItem == null || actionItem == null)) {
                                boolean tint2;
                                if (action.getImageMode() == 0) {
                                    tint2 = true;
                                }
                                addAction(iconItem, tint2).setOnClickListener(new OnClickListener() {
                                    public void onClick(View v) {
                                        try {
                                            actionItem.fireAction(null, null);
                                        } catch (CanceledException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }
                        }
                    } else if (VERSION.SDK_INT >= 21) {
                        handleSetRemoteInputActions(input, image, actionItem);
                    } else {
                        String str = TAG;
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("Received RemoteInput on API <20 ");
                        stringBuilder.append(input);
                        Log.w(str, stringBuilder.toString());
                    }
                } else {
                    return;
                }
            }
            if (getChildCount() == 0) {
                tint = 8;
            }
            setVisibility(tint);
            return;
        }
    }

    private void addAction(View child) {
        this.mActionsGroup.addView(child, new LinearLayout.LayoutParams(this.mSize, this.mSize, 1.0f));
    }

    @RequiresApi(21)
    private void handleSetRemoteInputActions(final SliceItem input, SliceItem image, final SliceItem action) {
        if (input.getRemoteInput().getAllowFreeFormInput()) {
            addAction(image.getIcon(), image.hasHint("no_tint") ^ 1).setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    ActionRow.this.handleRemoteInputClick(v, action, input.getRemoteInput());
                }
            });
            createRemoteInputView(this.mColor, getContext());
        }
    }

    @RequiresApi(21)
    private void createRemoteInputView(int color, Context context) {
        View riv = RemoteInputView.inflate(context, this);
        riv.setVisibility(4);
        addView(riv, new LayoutParams(-1, -1));
        riv.setBackgroundColor(color);
    }

    @RequiresApi(21)
    private boolean handleRemoteInputClick(View view, SliceItem action, RemoteInput input) {
        if (input == null) {
            return false;
        }
        RemoteInputView riv = null;
        for (ViewParent p = view.getParent().getParent(); p != null; p = p.getParent()) {
            if (p instanceof View) {
                riv = findRemoteInputView((View) p);
                if (riv != null) {
                    break;
                }
            }
        }
        if (riv == null) {
            return false;
        }
        int width = view.getWidth();
        if (view instanceof TextView) {
            TextView tv = (TextView) view;
            if (tv.getLayout() != null) {
                width = Math.min(width, ((int) tv.getLayout().getLineWidth(0)) + (tv.getCompoundPaddingLeft() + tv.getCompoundPaddingRight()));
            }
        }
        int cx = view.getLeft() + (width / 2);
        int cy = view.getTop() + (view.getHeight() / 2);
        int w = riv.getWidth();
        int h = riv.getHeight();
        riv.setRevealParameters(cx, cy, Math.max(Math.max(cx + cy, (h - cy) + cx), Math.max((w - cx) + cy, (w - cx) + (h - cy))));
        riv.setAction(action);
        riv.setRemoteInput(new RemoteInput[]{input}, input);
        riv.focusAnimated();
        return true;
    }

    @RequiresApi(21)
    private RemoteInputView findRemoteInputView(View v) {
        if (v == null) {
            return null;
        }
        return (RemoteInputView) v.findViewWithTag(RemoteInputView.VIEW_TAG);
    }
}
