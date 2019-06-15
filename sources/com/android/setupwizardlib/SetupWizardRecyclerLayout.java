package com.android.setupwizardlib;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.setupwizardlib.template.RecyclerMixin;
import com.android.setupwizardlib.template.RecyclerViewScrollHandlingDelegate;
import com.android.setupwizardlib.template.RequireScrollMixin;

public class SetupWizardRecyclerLayout extends SetupWizardLayout {
    private static final String TAG = "RecyclerLayout";
    protected RecyclerMixin mRecyclerMixin;

    public SetupWizardRecyclerLayout(Context context) {
        this(context, 0, 0);
    }

    public SetupWizardRecyclerLayout(Context context, int template, int containerId) {
        super(context, template, containerId);
        init(context, null, 0);
    }

    public SetupWizardRecyclerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public SetupWizardRecyclerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        this.mRecyclerMixin.parseAttributes(attrs, defStyleAttr);
        registerMixin(RecyclerMixin.class, this.mRecyclerMixin);
        RequireScrollMixin requireScrollMixin = (RequireScrollMixin) getMixin(RequireScrollMixin.class);
        requireScrollMixin.setScrollHandlingDelegate(new RecyclerViewScrollHandlingDelegate(requireScrollMixin, getRecyclerView()));
    }

    /* Access modifiers changed, original: protected */
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        this.mRecyclerMixin.onLayout();
    }

    public Adapter<? extends ViewHolder> getAdapter() {
        return this.mRecyclerMixin.getAdapter();
    }

    public void setAdapter(Adapter<? extends ViewHolder> adapter) {
        this.mRecyclerMixin.setAdapter(adapter);
    }

    public RecyclerView getRecyclerView() {
        return this.mRecyclerMixin.getRecyclerView();
    }

    /* Access modifiers changed, original: protected */
    public ViewGroup findContainer(int containerId) {
        if (containerId == 0) {
            containerId = R.id.suw_recycler_view;
        }
        return super.findContainer(containerId);
    }

    /* Access modifiers changed, original: protected */
    public View onInflateTemplate(LayoutInflater inflater, int template) {
        if (template == 0) {
            template = R.layout.suw_recycler_template;
        }
        return super.onInflateTemplate(inflater, template);
    }

    /* Access modifiers changed, original: protected */
    public void onTemplateInflated() {
        View recyclerView = findViewById(R.id.suw_recycler_view);
        if (recyclerView instanceof RecyclerView) {
            this.mRecyclerMixin = new RecyclerMixin(this, (RecyclerView) recyclerView);
            return;
        }
        throw new IllegalStateException("SetupWizardRecyclerLayout should use a template with recycler view");
    }

    public <T extends View> T findManagedViewById(int id) {
        View header = this.mRecyclerMixin.getHeader();
        if (header != null) {
            T view = header.findViewById(id);
            if (view != null) {
                return view;
            }
        }
        return super.findViewById(id);
    }

    @Deprecated
    public void setDividerInset(int inset) {
        this.mRecyclerMixin.setDividerInset(inset);
    }

    public void setDividerInsets(int start, int end) {
        this.mRecyclerMixin.setDividerInsets(start, end);
    }

    @Deprecated
    public int getDividerInset() {
        return this.mRecyclerMixin.getDividerInset();
    }

    public int getDividerInsetStart() {
        return this.mRecyclerMixin.getDividerInsetStart();
    }

    public int getDividerInsetEnd() {
        return this.mRecyclerMixin.getDividerInsetEnd();
    }

    public Drawable getDivider() {
        return this.mRecyclerMixin.getDivider();
    }
}
