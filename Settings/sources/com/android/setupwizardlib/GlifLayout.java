package com.android.setupwizardlib;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import com.android.setupwizardlib.template.ButtonFooterMixin;
import com.android.setupwizardlib.template.ColoredHeaderMixin;
import com.android.setupwizardlib.template.HeaderMixin;
import com.android.setupwizardlib.template.IconMixin;
import com.android.setupwizardlib.template.ProgressBarMixin;
import com.android.setupwizardlib.template.RequireScrollMixin;
import com.android.setupwizardlib.template.ScrollViewScrollHandlingDelegate;
import com.android.setupwizardlib.view.StatusBarBackgroundLayout;

public class GlifLayout extends TemplateLayout {
    private static final String TAG = "GlifLayout";
    @Nullable
    private ColorStateList mBackgroundBaseColor;
    private boolean mBackgroundPatterned;
    private boolean mLayoutFullscreen;
    private ColorStateList mPrimaryColor;

    public GlifLayout(Context context) {
        this(context, 0, 0);
    }

    public GlifLayout(Context context, int template) {
        this(context, template, 0);
    }

    public GlifLayout(Context context, int template, int containerId) {
        super(context, template, containerId);
        this.mBackgroundPatterned = true;
        this.mLayoutFullscreen = true;
        init(null, R.attr.suwLayoutTheme);
    }

    public GlifLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mBackgroundPatterned = true;
        this.mLayoutFullscreen = true;
        init(attrs, R.attr.suwLayoutTheme);
    }

    @TargetApi(11)
    public GlifLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mBackgroundPatterned = true;
        this.mLayoutFullscreen = true;
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyleAttr) {
        registerMixin(HeaderMixin.class, new ColoredHeaderMixin(this, attrs, defStyleAttr));
        registerMixin(IconMixin.class, new IconMixin(this, attrs, defStyleAttr));
        registerMixin(ProgressBarMixin.class, new ProgressBarMixin(this));
        registerMixin(ButtonFooterMixin.class, new ButtonFooterMixin(this));
        RequireScrollMixin requireScrollMixin = new RequireScrollMixin(this);
        registerMixin(RequireScrollMixin.class, requireScrollMixin);
        ScrollView scrollView = getScrollView();
        if (scrollView != null) {
            requireScrollMixin.setScrollHandlingDelegate(new ScrollViewScrollHandlingDelegate(requireScrollMixin, scrollView));
        }
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.SuwGlifLayout, defStyleAttr, 0);
        ColorStateList primaryColor = a.getColorStateList(R.styleable.SuwGlifLayout_suwColorPrimary);
        if (primaryColor != null) {
            setPrimaryColor(primaryColor);
        }
        setBackgroundBaseColor(a.getColorStateList(R.styleable.SuwGlifLayout_suwBackgroundBaseColor));
        setBackgroundPatterned(a.getBoolean(R.styleable.SuwGlifLayout_suwBackgroundPatterned, true));
        int footer = a.getResourceId(R.styleable.SuwGlifLayout_suwFooter, 0);
        if (footer != 0) {
            inflateFooter(footer);
        }
        int stickyHeader = a.getResourceId(R.styleable.SuwGlifLayout_suwStickyHeader, 0);
        if (stickyHeader != 0) {
            inflateStickyHeader(stickyHeader);
        }
        this.mLayoutFullscreen = a.getBoolean(R.styleable.SuwGlifLayout_suwLayoutFullscreen, true);
        a.recycle();
        if (VERSION.SDK_INT >= 21 && this.mLayoutFullscreen) {
            setSystemUiVisibility(1024);
        }
    }

    /* Access modifiers changed, original: protected */
    public View onInflateTemplate(LayoutInflater inflater, @LayoutRes int template) {
        if (template == 0) {
            template = R.layout.suw_glif_template;
        }
        return inflateTemplate(inflater, R.style.SuwThemeGlif_Light, template);
    }

    /* Access modifiers changed, original: protected */
    public ViewGroup findContainer(int containerId) {
        if (containerId == 0) {
            containerId = R.id.suw_layout_content;
        }
        return super.findContainer(containerId);
    }

    public View inflateFooter(@LayoutRes int footer) {
        ViewStub footerStub = (ViewStub) findManagedViewById(R.id.suw_layout_footer);
        footerStub.setLayoutResource(footer);
        return footerStub.inflate();
    }

    public View inflateStickyHeader(@LayoutRes int header) {
        ViewStub stickyHeaderStub = (ViewStub) findManagedViewById(R.id.suw_layout_sticky_header);
        stickyHeaderStub.setLayoutResource(header);
        return stickyHeaderStub.inflate();
    }

    public ScrollView getScrollView() {
        View view = findManagedViewById(R.id.suw_scroll_view);
        return view instanceof ScrollView ? (ScrollView) view : null;
    }

    public TextView getHeaderTextView() {
        return ((HeaderMixin) getMixin(HeaderMixin.class)).getTextView();
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

    public void setHeaderColor(ColorStateList color) {
        ((ColoredHeaderMixin) getMixin(HeaderMixin.class)).setColor(color);
    }

    public ColorStateList getHeaderColor() {
        return ((ColoredHeaderMixin) getMixin(HeaderMixin.class)).getColor();
    }

    public void setIcon(Drawable icon) {
        ((IconMixin) getMixin(IconMixin.class)).setIcon(icon);
    }

    public Drawable getIcon() {
        return ((IconMixin) getMixin(IconMixin.class)).getIcon();
    }

    public void setPrimaryColor(@NonNull ColorStateList color) {
        this.mPrimaryColor = color;
        updateBackground();
        ((ProgressBarMixin) getMixin(ProgressBarMixin.class)).setColor(color);
    }

    public ColorStateList getPrimaryColor() {
        return this.mPrimaryColor;
    }

    public void setBackgroundBaseColor(@Nullable ColorStateList color) {
        this.mBackgroundBaseColor = color;
        updateBackground();
    }

    @Nullable
    public ColorStateList getBackgroundBaseColor() {
        return this.mBackgroundBaseColor;
    }

    public void setBackgroundPatterned(boolean patterned) {
        this.mBackgroundPatterned = patterned;
        updateBackground();
    }

    public boolean isBackgroundPatterned() {
        return this.mBackgroundPatterned;
    }

    private void updateBackground() {
        View patternBg = findManagedViewById(R.id.suw_pattern_bg);
        if (patternBg != null) {
            Drawable background;
            int backgroundColor = 0;
            if (this.mBackgroundBaseColor != null) {
                backgroundColor = this.mBackgroundBaseColor.getDefaultColor();
            } else if (this.mPrimaryColor != null) {
                backgroundColor = this.mPrimaryColor.getDefaultColor();
            }
            if (this.mBackgroundPatterned) {
                background = new GlifPatternDrawable(backgroundColor);
            } else {
                background = new ColorDrawable(backgroundColor);
            }
            if (patternBg instanceof StatusBarBackgroundLayout) {
                ((StatusBarBackgroundLayout) patternBg).setStatusBarBackground(background);
            } else {
                patternBg.setBackgroundDrawable(background);
            }
        }
    }

    public boolean isProgressBarShown() {
        return ((ProgressBarMixin) getMixin(ProgressBarMixin.class)).isShown();
    }

    public void setProgressBarShown(boolean shown) {
        ((ProgressBarMixin) getMixin(ProgressBarMixin.class)).setShown(shown);
    }

    public ProgressBar peekProgressBar() {
        return ((ProgressBarMixin) getMixin(ProgressBarMixin.class)).peekProgressBar();
    }
}
