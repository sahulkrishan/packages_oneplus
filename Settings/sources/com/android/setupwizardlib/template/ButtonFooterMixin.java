package com.android.setupwizardlib.template;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import com.android.setupwizardlib.R;
import com.android.setupwizardlib.TemplateLayout;

public class ButtonFooterMixin implements Mixin {
    private LinearLayout mButtonContainer;
    private final Context mContext;
    @Nullable
    private final ViewStub mFooterStub;

    public ButtonFooterMixin(TemplateLayout layout) {
        this.mContext = layout.getContext();
        this.mFooterStub = (ViewStub) layout.findManagedViewById(R.id.suw_layout_footer);
    }

    public Button addButton(CharSequence text, @StyleRes int theme) {
        Button button = createThemedButton(this.mContext, theme);
        button.setText(text);
        return addButton(button);
    }

    public Button addButton(@StringRes int text, @StyleRes int theme) {
        Button button = createThemedButton(this.mContext, theme);
        button.setText(text);
        return addButton(button);
    }

    public Button addButton(Button button) {
        ensureFooterInflated().addView(button);
        return button;
    }

    public View addSpace() {
        LinearLayout buttonContainer = ensureFooterInflated();
        View space = new View(buttonContainer.getContext());
        space.setLayoutParams(new LayoutParams(0, 0, 1.0f));
        space.setVisibility(4);
        buttonContainer.addView(space);
        return space;
    }

    public void removeButton(Button button) {
        if (this.mButtonContainer != null) {
            this.mButtonContainer.removeView(button);
        }
    }

    public void removeSpace(View space) {
        if (this.mButtonContainer != null) {
            this.mButtonContainer.removeView(space);
        }
    }

    public void removeAllViews() {
        if (this.mButtonContainer != null) {
            this.mButtonContainer.removeAllViews();
        }
    }

    @NonNull
    private LinearLayout ensureFooterInflated() {
        if (this.mButtonContainer == null) {
            if (this.mFooterStub != null) {
                this.mFooterStub.setLayoutResource(R.layout.suw_glif_footer_button_bar);
                this.mButtonContainer = (LinearLayout) this.mFooterStub.inflate();
            } else {
                throw new IllegalStateException("Footer stub is not found in this template");
            }
        }
        return this.mButtonContainer;
    }

    @SuppressLint({"InflateParams"})
    private Button createThemedButton(Context context, @StyleRes int theme) {
        return (Button) LayoutInflater.from(new ContextThemeWrapper(context, theme)).inflate(R.layout.suw_button, null, false);
    }
}
