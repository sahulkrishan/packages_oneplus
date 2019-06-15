package com.android.setupwizardlib;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import com.android.setupwizardlib.template.ListMixin;
import com.android.setupwizardlib.template.ListViewScrollHandlingDelegate;
import com.android.setupwizardlib.template.RequireScrollMixin;

public class SetupWizardListLayout extends SetupWizardLayout {
    private static final String TAG = "SetupWizardListLayout";
    private ListMixin mListMixin;

    public SetupWizardListLayout(Context context) {
        this(context, 0, 0);
    }

    public SetupWizardListLayout(Context context, int template) {
        this(context, template, 0);
    }

    public SetupWizardListLayout(Context context, int template, int containerId) {
        super(context, template, containerId);
        init(context, null, 0);
    }

    public SetupWizardListLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    @TargetApi(11)
    public SetupWizardListLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        this.mListMixin = new ListMixin(this, attrs, defStyleAttr);
        registerMixin(ListMixin.class, this.mListMixin);
        RequireScrollMixin requireScrollMixin = (RequireScrollMixin) getMixin(RequireScrollMixin.class);
        requireScrollMixin.setScrollHandlingDelegate(new ListViewScrollHandlingDelegate(requireScrollMixin, getListView()));
    }

    /* Access modifiers changed, original: protected */
    public View onInflateTemplate(LayoutInflater inflater, int template) {
        if (template == 0) {
            template = R.layout.suw_list_template;
        }
        return super.onInflateTemplate(inflater, template);
    }

    /* Access modifiers changed, original: protected */
    public ViewGroup findContainer(int containerId) {
        if (containerId == 0) {
            containerId = 16908298;
        }
        return super.findContainer(containerId);
    }

    /* Access modifiers changed, original: protected */
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        this.mListMixin.onLayout();
    }

    public ListView getListView() {
        return this.mListMixin.getListView();
    }

    public void setAdapter(ListAdapter adapter) {
        this.mListMixin.setAdapter(adapter);
    }

    public ListAdapter getAdapter() {
        return this.mListMixin.getAdapter();
    }

    @Deprecated
    public void setDividerInset(int inset) {
        this.mListMixin.setDividerInset(inset);
    }

    public void setDividerInsets(int start, int end) {
        this.mListMixin.setDividerInsets(start, end);
    }

    @Deprecated
    public int getDividerInset() {
        return this.mListMixin.getDividerInset();
    }

    public int getDividerInsetStart() {
        return this.mListMixin.getDividerInsetStart();
    }

    public int getDividerInsetEnd() {
        return this.mListMixin.getDividerInsetEnd();
    }

    public Drawable getDivider() {
        return this.mListMixin.getDivider();
    }
}
