package android.support.v17.leanback.widget;

import android.graphics.drawable.Drawable;
import android.support.v17.leanback.R;
import android.support.v17.leanback.widget.Presenter.ViewHolder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

class ActionPresenterSelector extends PresenterSelector {
    private final Presenter mOneLineActionPresenter = new OneLineActionPresenter();
    private final Presenter[] mPresenters = new Presenter[]{this.mOneLineActionPresenter, this.mTwoLineActionPresenter};
    private final Presenter mTwoLineActionPresenter = new TwoLineActionPresenter();

    static abstract class ActionPresenter extends Presenter {
        ActionPresenter() {
        }

        public void onBindViewHolder(ViewHolder viewHolder, Object item) {
            Action action = (Action) item;
            ActionViewHolder vh = (ActionViewHolder) viewHolder;
            vh.mAction = action;
            Drawable icon = action.getIcon();
            if (icon != null) {
                vh.view.setPaddingRelative(vh.view.getResources().getDimensionPixelSize(R.dimen.lb_action_with_icon_padding_start), 0, vh.view.getResources().getDimensionPixelSize(R.dimen.lb_action_with_icon_padding_end), 0);
            } else {
                int padding = vh.view.getResources().getDimensionPixelSize(R.dimen.lb_action_padding_horizontal);
                vh.view.setPaddingRelative(padding, 0, padding, 0);
            }
            if (vh.mLayoutDirection == 1) {
                vh.mButton.setCompoundDrawablesWithIntrinsicBounds(null, null, icon, null);
            } else {
                vh.mButton.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
            }
        }

        public void onUnbindViewHolder(ViewHolder viewHolder) {
            ActionViewHolder vh = (ActionViewHolder) viewHolder;
            vh.mButton.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            vh.view.setPadding(0, 0, 0, 0);
            vh.mAction = null;
        }
    }

    static class ActionViewHolder extends ViewHolder {
        Action mAction;
        Button mButton;
        int mLayoutDirection;

        public ActionViewHolder(View view, int layoutDirection) {
            super(view);
            this.mButton = (Button) view.findViewById(R.id.lb_action_button);
            this.mLayoutDirection = layoutDirection;
        }
    }

    static class OneLineActionPresenter extends ActionPresenter {
        OneLineActionPresenter() {
        }

        public ViewHolder onCreateViewHolder(ViewGroup parent) {
            return new ActionViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.lb_action_1_line, parent, false), parent.getLayoutDirection());
        }

        public void onBindViewHolder(ViewHolder viewHolder, Object item) {
            super.onBindViewHolder(viewHolder, item);
            ((ActionViewHolder) viewHolder).mButton.setText(((Action) item).getLabel1());
        }
    }

    static class TwoLineActionPresenter extends ActionPresenter {
        TwoLineActionPresenter() {
        }

        public ViewHolder onCreateViewHolder(ViewGroup parent) {
            return new ActionViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.lb_action_2_lines, parent, false), parent.getLayoutDirection());
        }

        public void onBindViewHolder(ViewHolder viewHolder, Object item) {
            super.onBindViewHolder(viewHolder, item);
            Action action = (Action) item;
            ActionViewHolder vh = (ActionViewHolder) viewHolder;
            CharSequence line1 = action.getLabel1();
            CharSequence line2 = action.getLabel2();
            if (TextUtils.isEmpty(line1)) {
                vh.mButton.setText(line2);
            } else if (TextUtils.isEmpty(line2)) {
                vh.mButton.setText(line1);
            } else {
                Button button = vh.mButton;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(line1);
                stringBuilder.append("\n");
                stringBuilder.append(line2);
                button.setText(stringBuilder.toString());
            }
        }
    }

    ActionPresenterSelector() {
    }

    public Presenter getPresenter(Object item) {
        if (TextUtils.isEmpty(((Action) item).getLabel2())) {
            return this.mOneLineActionPresenter;
        }
        return this.mTwoLineActionPresenter;
    }

    public Presenter[] getPresenters() {
        return this.mPresenters;
    }
}
