package android.support.v17.leanback.widget;

import android.support.v17.leanback.R;
import android.support.v17.leanback.widget.Presenter.ViewHolder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class ControlButtonPresenterSelector extends PresenterSelector {
    private final Presenter[] mPresenters = new Presenter[]{this.mPrimaryPresenter};
    private final Presenter mPrimaryPresenter = new ControlButtonPresenter(R.layout.lb_control_button_primary);
    private final Presenter mSecondaryPresenter = new ControlButtonPresenter(R.layout.lb_control_button_secondary);

    static class ActionViewHolder extends ViewHolder {
        View mFocusableView;
        ImageView mIcon;
        TextView mLabel;

        public ActionViewHolder(View view) {
            super(view);
            this.mIcon = (ImageView) view.findViewById(R.id.icon);
            this.mLabel = (TextView) view.findViewById(R.id.label);
            this.mFocusableView = view.findViewById(R.id.button);
        }
    }

    static class ControlButtonPresenter extends Presenter {
        private int mLayoutResourceId;

        ControlButtonPresenter(int layoutResourceId) {
            this.mLayoutResourceId = layoutResourceId;
        }

        public ViewHolder onCreateViewHolder(ViewGroup parent) {
            return new ActionViewHolder(LayoutInflater.from(parent.getContext()).inflate(this.mLayoutResourceId, parent, false));
        }

        public void onBindViewHolder(ViewHolder viewHolder, Object item) {
            Action action = (Action) item;
            ActionViewHolder vh = (ActionViewHolder) viewHolder;
            vh.mIcon.setImageDrawable(action.getIcon());
            if (vh.mLabel != null) {
                if (action.getIcon() == null) {
                    vh.mLabel.setText(action.getLabel1());
                } else {
                    vh.mLabel.setText(null);
                }
            }
            CharSequence contentDescription = TextUtils.isEmpty(action.getLabel2()) ? action.getLabel1() : action.getLabel2();
            if (!TextUtils.equals(vh.mFocusableView.getContentDescription(), contentDescription)) {
                vh.mFocusableView.setContentDescription(contentDescription);
                vh.mFocusableView.sendAccessibilityEvent(32768);
            }
        }

        public void onUnbindViewHolder(ViewHolder viewHolder) {
            ActionViewHolder vh = (ActionViewHolder) viewHolder;
            vh.mIcon.setImageDrawable(null);
            if (vh.mLabel != null) {
                vh.mLabel.setText(null);
            }
            vh.mFocusableView.setContentDescription(null);
        }

        public void setOnClickListener(ViewHolder viewHolder, OnClickListener listener) {
            ((ActionViewHolder) viewHolder).mFocusableView.setOnClickListener(listener);
        }
    }

    public Presenter getPrimaryPresenter() {
        return this.mPrimaryPresenter;
    }

    public Presenter getSecondaryPresenter() {
        return this.mSecondaryPresenter;
    }

    public Presenter getPresenter(Object item) {
        return this.mPrimaryPresenter;
    }

    public Presenter[] getPresenters() {
        return this.mPresenters;
    }
}
