package com.android.setupwizardlib.template;

import android.content.res.ColorStateList;
import android.os.Build.VERSION;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewStub;
import android.widget.ProgressBar;
import com.android.setupwizardlib.R;
import com.android.setupwizardlib.TemplateLayout;

public class ProgressBarMixin implements Mixin {
    @Nullable
    private ColorStateList mColor;
    private TemplateLayout mTemplateLayout;

    public ProgressBarMixin(TemplateLayout layout) {
        this.mTemplateLayout = layout;
    }

    public boolean isShown() {
        View progressBar = this.mTemplateLayout.findManagedViewById(R.id.suw_layout_progress);
        return progressBar != null && progressBar.getVisibility() == 0;
    }

    public void setShown(boolean shown) {
        View progressBar;
        if (shown) {
            progressBar = getProgressBar();
            if (progressBar != null) {
                progressBar.setVisibility(0);
                return;
            }
            return;
        }
        progressBar = peekProgressBar();
        if (progressBar != null) {
            progressBar.setVisibility(8);
        }
    }

    private ProgressBar getProgressBar() {
        if (peekProgressBar() == null) {
            ViewStub progressBarStub = (ViewStub) this.mTemplateLayout.findManagedViewById(R.id.suw_layout_progress_stub);
            if (progressBarStub != null) {
                progressBarStub.inflate();
            }
            setColor(this.mColor);
        }
        return peekProgressBar();
    }

    public ProgressBar peekProgressBar() {
        return (ProgressBar) this.mTemplateLayout.findManagedViewById(R.id.suw_layout_progress);
    }

    public void setColor(@Nullable ColorStateList color) {
        this.mColor = color;
        if (VERSION.SDK_INT >= 21) {
            ProgressBar bar = peekProgressBar();
            if (bar != null) {
                bar.setIndeterminateTintList(color);
                if (VERSION.SDK_INT >= 23 || color != null) {
                    bar.setProgressBackgroundTintList(color);
                }
            }
        }
    }

    @Nullable
    public ColorStateList getColor() {
        return this.mColor;
    }
}
