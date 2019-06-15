package com.android.setupwizardlib;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build.VERSION;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.BaseSavedState;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import com.android.setupwizardlib.template.HeaderMixin;
import com.android.setupwizardlib.template.NavigationBarMixin;
import com.android.setupwizardlib.template.ProgressBarMixin;
import com.android.setupwizardlib.template.RequireScrollMixin;
import com.android.setupwizardlib.template.ScrollViewScrollHandlingDelegate;
import com.android.setupwizardlib.view.Illustration;
import com.android.setupwizardlib.view.NavigationBar;

public class SetupWizardLayout extends TemplateLayout {
    private static final String TAG = "SetupWizardLayout";

    protected static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel parcel) {
                return new SavedState(parcel);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        boolean mIsProgressBarShown = false;

        public SavedState(Parcelable parcelable) {
            super(parcelable);
        }

        public SavedState(Parcel source) {
            super(source);
            boolean z = false;
            if (source.readInt() != 0) {
                z = true;
            }
            this.mIsProgressBarShown = z;
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.mIsProgressBarShown);
        }
    }

    public SetupWizardLayout(Context context) {
        super(context, 0, 0);
        init(null, R.attr.suwLayoutTheme);
    }

    public SetupWizardLayout(Context context, int template) {
        this(context, template, 0);
    }

    public SetupWizardLayout(Context context, int template, int containerId) {
        super(context, template, containerId);
        init(null, R.attr.suwLayoutTheme);
    }

    public SetupWizardLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, R.attr.suwLayoutTheme);
    }

    @TargetApi(11)
    public SetupWizardLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyleAttr) {
        Drawable backgroundTile;
        registerMixin(HeaderMixin.class, new HeaderMixin(this, attrs, defStyleAttr));
        registerMixin(ProgressBarMixin.class, new ProgressBarMixin(this));
        registerMixin(NavigationBarMixin.class, new NavigationBarMixin(this));
        RequireScrollMixin requireScrollMixin = new RequireScrollMixin(this);
        registerMixin(RequireScrollMixin.class, requireScrollMixin);
        ScrollView scrollView = getScrollView();
        if (scrollView != null) {
            requireScrollMixin.setScrollHandlingDelegate(new ScrollViewScrollHandlingDelegate(requireScrollMixin, scrollView));
        }
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.SuwSetupWizardLayout, defStyleAttr, 0);
        Drawable background = a.getDrawable(R.styleable.SuwSetupWizardLayout_suwBackground);
        if (background != null) {
            setLayoutBackground(background);
        } else {
            backgroundTile = a.getDrawable(R.styleable.SuwSetupWizardLayout_suwBackgroundTile);
            if (backgroundTile != null) {
                setBackgroundTile(backgroundTile);
            }
        }
        backgroundTile = a.getDrawable(R.styleable.SuwSetupWizardLayout_suwIllustration);
        if (backgroundTile != null) {
            setIllustration(backgroundTile);
        } else {
            Drawable illustrationImage = a.getDrawable(R.styleable.SuwSetupWizardLayout_suwIllustrationImage);
            Drawable horizontalTile = a.getDrawable(R.styleable.SuwSetupWizardLayout_suwIllustrationHorizontalTile);
            if (!(illustrationImage == null || horizontalTile == null)) {
                setIllustration(illustrationImage, horizontalTile);
            }
        }
        int decorPaddingTop = a.getDimensionPixelSize(R.styleable.SuwSetupWizardLayout_suwDecorPaddingTop, -1);
        if (decorPaddingTop == -1) {
            decorPaddingTop = getResources().getDimensionPixelSize(R.dimen.suw_decor_padding_top);
        }
        setDecorPaddingTop(decorPaddingTop);
        float illustrationAspectRatio = a.getFloat(R.styleable.SuwSetupWizardLayout_suwIllustrationAspectRatio, -1.0f);
        if (illustrationAspectRatio == -1.0f) {
            TypedValue out = new TypedValue();
            getResources().getValue(R.dimen.suw_illustration_aspect_ratio, out, true);
            illustrationAspectRatio = out.getFloat();
        }
        setIllustrationAspectRatio(illustrationAspectRatio);
        a.recycle();
    }

    /* Access modifiers changed, original: protected */
    public Parcelable onSaveInstanceState() {
        SavedState ss = new SavedState(super.onSaveInstanceState());
        ss.mIsProgressBarShown = isProgressBarShown();
        return ss;
    }

    /* Access modifiers changed, original: protected */
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            SavedState ss = (SavedState) state;
            super.onRestoreInstanceState(ss.getSuperState());
            setProgressBarShown(ss.mIsProgressBarShown);
            return;
        }
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Ignoring restore instance state ");
        stringBuilder.append(state);
        Log.w(str, stringBuilder.toString());
        super.onRestoreInstanceState(state);
    }

    /* Access modifiers changed, original: protected */
    public View onInflateTemplate(LayoutInflater inflater, int template) {
        if (template == 0) {
            template = R.layout.suw_template;
        }
        return inflateTemplate(inflater, R.style.SuwThemeMaterial_Light, template);
    }

    /* Access modifiers changed, original: protected */
    public ViewGroup findContainer(int containerId) {
        if (containerId == 0) {
            containerId = R.id.suw_layout_content;
        }
        return super.findContainer(containerId);
    }

    public NavigationBar getNavigationBar() {
        return ((NavigationBarMixin) getMixin(NavigationBarMixin.class)).getNavigationBar();
    }

    public ScrollView getScrollView() {
        View view = findManagedViewById(R.id.suw_bottom_scroll_view);
        return view instanceof ScrollView ? (ScrollView) view : null;
    }

    public void requireScrollToBottom() {
        RequireScrollMixin requireScrollMixin = (RequireScrollMixin) getMixin(RequireScrollMixin.class);
        NavigationBar navigationBar = getNavigationBar();
        if (navigationBar != null) {
            requireScrollMixin.requireScrollWithNavigationBar(navigationBar);
        } else {
            Log.e(TAG, "Cannot require scroll. Navigation bar is null.");
        }
    }

    public void setHeaderText(int title) {
        ((HeaderMixin) getMixin(HeaderMixin.class)).setText(title);
    }

    public void setHeaderText(CharSequence title) {
        ((HeaderMixin) getMixin(HeaderMixin.class)).setText(title);
    }

    public CharSequence getHeaderText() {
        return ((HeaderMixin) getMixin(HeaderMixin.class)).getText();
    }

    public TextView getHeaderTextView() {
        return ((HeaderMixin) getMixin(HeaderMixin.class)).getTextView();
    }

    public void setIllustration(Drawable drawable) {
        View view = findManagedViewById(R.id.suw_layout_decor);
        if (view instanceof Illustration) {
            ((Illustration) view).setIllustration(drawable);
        }
    }

    public void setIllustration(int asset, int horizontalTile) {
        View view = findManagedViewById(R.id.suw_layout_decor);
        if (view instanceof Illustration) {
            ((Illustration) view).setIllustration(getIllustration(asset, horizontalTile));
        }
    }

    private void setIllustration(Drawable asset, Drawable horizontalTile) {
        View view = findManagedViewById(R.id.suw_layout_decor);
        if (view instanceof Illustration) {
            ((Illustration) view).setIllustration(getIllustration(asset, horizontalTile));
        }
    }

    public void setIllustrationAspectRatio(float aspectRatio) {
        View view = findManagedViewById(R.id.suw_layout_decor);
        if (view instanceof Illustration) {
            ((Illustration) view).setAspectRatio(aspectRatio);
        }
    }

    public void setDecorPaddingTop(int paddingTop) {
        View view = findManagedViewById(R.id.suw_layout_decor);
        if (view != null) {
            view.setPadding(view.getPaddingLeft(), paddingTop, view.getPaddingRight(), view.getPaddingBottom());
        }
    }

    public void setLayoutBackground(Drawable background) {
        View view = findManagedViewById(R.id.suw_layout_decor);
        if (view != null) {
            view.setBackgroundDrawable(background);
        }
    }

    public void setBackgroundTile(int backgroundTile) {
        setBackgroundTile(getContext().getResources().getDrawable(backgroundTile));
    }

    private void setBackgroundTile(Drawable backgroundTile) {
        if (backgroundTile instanceof BitmapDrawable) {
            ((BitmapDrawable) backgroundTile).setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
        }
        setLayoutBackground(backgroundTile);
    }

    private Drawable getIllustration(int asset, int horizontalTile) {
        Context context = getContext();
        return getIllustration(context.getResources().getDrawable(asset), context.getResources().getDrawable(horizontalTile));
    }

    @SuppressLint({"RtlHardcoded"})
    private Drawable getIllustration(Drawable asset, Drawable horizontalTile) {
        if (getContext().getResources().getBoolean(R.bool.suwUseTabletLayout)) {
            if (horizontalTile instanceof BitmapDrawable) {
                ((BitmapDrawable) horizontalTile).setTileModeX(TileMode.REPEAT);
                ((BitmapDrawable) horizontalTile).setGravity(48);
            }
            if (asset instanceof BitmapDrawable) {
                ((BitmapDrawable) asset).setGravity(51);
            }
            LayerDrawable layers = new LayerDrawable(new Drawable[]{horizontalTile, asset});
            if (VERSION.SDK_INT >= 19) {
                layers.setAutoMirrored(true);
            }
            return layers;
        }
        if (VERSION.SDK_INT >= 19) {
            asset.setAutoMirrored(true);
        }
        return asset;
    }

    public boolean isProgressBarShown() {
        return ((ProgressBarMixin) getMixin(ProgressBarMixin.class)).isShown();
    }

    public void setProgressBarShown(boolean shown) {
        ((ProgressBarMixin) getMixin(ProgressBarMixin.class)).setShown(shown);
    }

    @Deprecated
    public void showProgressBar() {
        setProgressBarShown(true);
    }

    @Deprecated
    public void hideProgressBar() {
        setProgressBarShown(false);
    }

    public void setProgressBarColor(ColorStateList color) {
        ((ProgressBarMixin) getMixin(ProgressBarMixin.class)).setColor(color);
    }

    public ColorStateList getProgressBarColor() {
        return ((ProgressBarMixin) getMixin(ProgressBarMixin.class)).getColor();
    }
}
