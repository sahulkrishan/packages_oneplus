package android.support.v17.leanback.widget;

import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v17.leanback.widget.GuidedActionAutofillSupport.OnAutofillListener;
import android.support.v17.leanback.widget.GuidedActionsStylist.ViewHolder;
import android.support.v17.leanback.widget.ImeKeyMonitor.ImeKeyListener;
import android.support.v7.util.DiffUtil;
import android.support.v7.util.DiffUtil.Callback;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import java.util.ArrayList;
import java.util.List;

@RestrictTo({Scope.LIBRARY_GROUP})
public class GuidedActionAdapter extends Adapter {
    static final boolean DEBUG = false;
    static final boolean DEBUG_EDIT = false;
    static final String TAG = "GuidedActionAdapter";
    static final String TAG_EDIT = "EditableAction";
    private final ActionAutofillListener mActionAutofillListener;
    private final ActionEditListener mActionEditListener;
    private final ActionOnFocusListener mActionOnFocusListener;
    private final ActionOnKeyListener mActionOnKeyListener;
    private final List<GuidedAction> mActions;
    private ClickListener mClickListener;
    DiffCallback<GuidedAction> mDiffCallback;
    GuidedActionAdapterGroup mGroup;
    private final boolean mIsSubAdapter;
    private final OnClickListener mOnClickListener = new OnClickListener() {
        public void onClick(View v) {
            if (v != null && v.getWindowToken() != null && GuidedActionAdapter.this.getRecyclerView() != null) {
                ViewHolder avh = (ViewHolder) GuidedActionAdapter.this.getRecyclerView().getChildViewHolder(v);
                GuidedAction action = avh.getAction();
                if (action.hasTextEditable()) {
                    GuidedActionAdapter.this.mGroup.openIme(GuidedActionAdapter.this, avh);
                } else if (action.hasEditableActivatorView()) {
                    GuidedActionAdapter.this.performOnActionClick(avh);
                } else {
                    GuidedActionAdapter.this.handleCheckedActions(avh);
                    if (action.isEnabled() && !action.infoOnly()) {
                        GuidedActionAdapter.this.performOnActionClick(avh);
                    }
                }
            }
        }
    };
    final GuidedActionsStylist mStylist;

    private class ActionOnFocusListener implements OnFocusChangeListener {
        private FocusListener mFocusListener;
        private View mSelectedView;

        ActionOnFocusListener(FocusListener focusListener) {
            this.mFocusListener = focusListener;
        }

        public void setFocusListener(FocusListener focusListener) {
            this.mFocusListener = focusListener;
        }

        public void unFocus() {
            if (this.mSelectedView != null && GuidedActionAdapter.this.getRecyclerView() != null) {
                RecyclerView.ViewHolder vh = GuidedActionAdapter.this.getRecyclerView().getChildViewHolder(this.mSelectedView);
                if (vh != null) {
                    GuidedActionAdapter.this.mStylist.onAnimateItemFocused((ViewHolder) vh, false);
                    return;
                }
                Log.w(GuidedActionAdapter.TAG, "RecyclerView returned null view holder", new Throwable());
            }
        }

        public void onFocusChange(View v, boolean hasFocus) {
            if (GuidedActionAdapter.this.getRecyclerView() != null) {
                ViewHolder avh = (ViewHolder) GuidedActionAdapter.this.getRecyclerView().getChildViewHolder(v);
                if (hasFocus) {
                    this.mSelectedView = v;
                    if (this.mFocusListener != null) {
                        this.mFocusListener.onGuidedActionFocused(avh.getAction());
                    }
                } else if (this.mSelectedView == v) {
                    GuidedActionAdapter.this.mStylist.onAnimateItemPressedCancelled(avh);
                    this.mSelectedView = null;
                }
                GuidedActionAdapter.this.mStylist.onAnimateItemFocused(avh, hasFocus);
            }
        }
    }

    private class ActionOnKeyListener implements OnKeyListener {
        private boolean mKeyPressed = false;

        ActionOnKeyListener() {
        }

        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (v == null || event == null || GuidedActionAdapter.this.getRecyclerView() == null) {
                return false;
            }
            if (!(keyCode == 23 || keyCode == 66 || keyCode == 160)) {
                switch (keyCode) {
                    case 99:
                    case 100:
                        break;
                }
            }
            ViewHolder avh = (ViewHolder) GuidedActionAdapter.this.getRecyclerView().getChildViewHolder(v);
            GuidedAction action = avh.getAction();
            if (!action.isEnabled() || action.infoOnly()) {
                event.getAction();
                return true;
            }
            switch (event.getAction()) {
                case 0:
                    if (!this.mKeyPressed) {
                        this.mKeyPressed = true;
                        GuidedActionAdapter.this.mStylist.onAnimateItemPressed(avh, this.mKeyPressed);
                        break;
                    }
                    break;
                case 1:
                    if (this.mKeyPressed) {
                        this.mKeyPressed = false;
                        GuidedActionAdapter.this.mStylist.onAnimateItemPressed(avh, this.mKeyPressed);
                        break;
                    }
                    break;
            }
            return false;
        }
    }

    public interface ClickListener {
        void onGuidedActionClicked(GuidedAction guidedAction);
    }

    public interface EditListener {
        void onGuidedActionEditCanceled(GuidedAction guidedAction);

        long onGuidedActionEditedAndProceed(GuidedAction guidedAction);

        void onImeClose();

        void onImeOpen();
    }

    public interface FocusListener {
        void onGuidedActionFocused(GuidedAction guidedAction);
    }

    private class ActionAutofillListener implements OnAutofillListener {
        private ActionAutofillListener() {
        }

        /* synthetic */ ActionAutofillListener(GuidedActionAdapter x0, AnonymousClass1 x1) {
            this();
        }

        public void onAutofill(View view) {
            GuidedActionAdapter.this.mGroup.fillAndGoNext(GuidedActionAdapter.this, (EditText) view);
        }
    }

    private class ActionEditListener implements OnEditorActionListener, ImeKeyListener {
        ActionEditListener() {
        }

        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == 5 || actionId == 6) {
                GuidedActionAdapter.this.mGroup.fillAndGoNext(GuidedActionAdapter.this, v);
                return true;
            } else if (actionId != 1) {
                return false;
            } else {
                GuidedActionAdapter.this.mGroup.fillAndStay(GuidedActionAdapter.this, v);
                return true;
            }
        }

        public boolean onKeyPreIme(EditText editText, int keyCode, KeyEvent event) {
            if (keyCode == 4 && event.getAction() == 1) {
                GuidedActionAdapter.this.mGroup.fillAndStay(GuidedActionAdapter.this, editText);
                return true;
            } else if (keyCode != 66 || event.getAction() != 1) {
                return false;
            } else {
                GuidedActionAdapter.this.mGroup.fillAndGoNext(GuidedActionAdapter.this, editText);
                return true;
            }
        }
    }

    public GuidedActionAdapter(List<GuidedAction> actions, ClickListener clickListener, FocusListener focusListener, GuidedActionsStylist presenter, boolean isSubAdapter) {
        this.mActions = actions == null ? new ArrayList() : new ArrayList(actions);
        this.mClickListener = clickListener;
        this.mStylist = presenter;
        this.mActionOnKeyListener = new ActionOnKeyListener();
        this.mActionOnFocusListener = new ActionOnFocusListener(focusListener);
        this.mActionEditListener = new ActionEditListener();
        this.mActionAutofillListener = new ActionAutofillListener(this, null);
        this.mIsSubAdapter = isSubAdapter;
        if (!isSubAdapter) {
            this.mDiffCallback = GuidedActionDiffCallback.getInstance();
        }
    }

    public void setDiffCallback(DiffCallback<GuidedAction> diffCallback) {
        this.mDiffCallback = diffCallback;
    }

    public void setActions(List<GuidedAction> actions) {
        if (!this.mIsSubAdapter) {
            this.mStylist.collapseAction(false);
        }
        this.mActionOnFocusListener.unFocus();
        if (this.mDiffCallback != null) {
            final List<GuidedAction> oldActions = new ArrayList();
            oldActions.addAll(this.mActions);
            this.mActions.clear();
            this.mActions.addAll(actions);
            DiffUtil.calculateDiff(new Callback() {
                public int getOldListSize() {
                    return oldActions.size();
                }

                public int getNewListSize() {
                    return GuidedActionAdapter.this.mActions.size();
                }

                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return GuidedActionAdapter.this.mDiffCallback.areItemsTheSame(oldActions.get(oldItemPosition), GuidedActionAdapter.this.mActions.get(newItemPosition));
                }

                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    return GuidedActionAdapter.this.mDiffCallback.areContentsTheSame(oldActions.get(oldItemPosition), GuidedActionAdapter.this.mActions.get(newItemPosition));
                }

                @Nullable
                public Object getChangePayload(int oldItemPosition, int newItemPosition) {
                    return GuidedActionAdapter.this.mDiffCallback.getChangePayload(oldActions.get(oldItemPosition), GuidedActionAdapter.this.mActions.get(newItemPosition));
                }
            }).dispatchUpdatesTo((Adapter) this);
            return;
        }
        this.mActions.clear();
        this.mActions.addAll(actions);
        notifyDataSetChanged();
    }

    public int getCount() {
        return this.mActions.size();
    }

    public GuidedAction getItem(int position) {
        return (GuidedAction) this.mActions.get(position);
    }

    public int indexOf(GuidedAction action) {
        return this.mActions.indexOf(action);
    }

    public GuidedActionsStylist getGuidedActionsStylist() {
        return this.mStylist;
    }

    public void setClickListener(ClickListener clickListener) {
        this.mClickListener = clickListener;
    }

    public void setFocusListener(FocusListener focusListener) {
        this.mActionOnFocusListener.setFocusListener(focusListener);
    }

    @RestrictTo({Scope.LIBRARY_GROUP})
    public List<GuidedAction> getActions() {
        return new ArrayList(this.mActions);
    }

    public int getItemViewType(int position) {
        return this.mStylist.getItemViewType((GuidedAction) this.mActions.get(position));
    }

    /* Access modifiers changed, original: 0000 */
    public RecyclerView getRecyclerView() {
        return this.mIsSubAdapter ? this.mStylist.getSubActionsGridView() : this.mStylist.getActionsGridView();
    }

    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder vh = this.mStylist.onCreateViewHolder(parent, viewType);
        View v = vh.itemView;
        v.setOnKeyListener(this.mActionOnKeyListener);
        v.setOnClickListener(this.mOnClickListener);
        v.setOnFocusChangeListener(this.mActionOnFocusListener);
        setupListeners(vh.getEditableTitleView());
        setupListeners(vh.getEditableDescriptionView());
        return vh;
    }

    private void setupListeners(EditText edit) {
        if (edit != null) {
            edit.setPrivateImeOptions("EscapeNorth=1;");
            edit.setOnEditorActionListener(this.mActionEditListener);
            if (edit instanceof ImeKeyMonitor) {
                ((ImeKeyMonitor) edit).setImeKeyListener(this.mActionEditListener);
            }
            if (edit instanceof GuidedActionAutofillSupport) {
                ((GuidedActionAutofillSupport) edit).setOnAutofillListener(this.mActionAutofillListener);
            }
        }
    }

    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position < this.mActions.size()) {
            this.mStylist.onBindViewHolder((ViewHolder) holder, (GuidedAction) this.mActions.get(position));
        }
    }

    public int getItemCount() {
        return this.mActions.size();
    }

    public ViewHolder findSubChildViewHolder(View v) {
        if (getRecyclerView() == null) {
            return null;
        }
        ViewHolder result = null;
        ViewParent parent = v.getParent();
        while (parent != getRecyclerView() && parent != null && v != null) {
            v = (View) parent;
            parent = parent.getParent();
        }
        if (!(parent == null || v == null)) {
            result = (ViewHolder) getRecyclerView().getChildViewHolder(v);
        }
        return result;
    }

    public void handleCheckedActions(ViewHolder avh) {
        GuidedAction action = avh.getAction();
        int actionCheckSetId = action.getCheckSetId();
        if (getRecyclerView() != null && actionCheckSetId != 0) {
            if (actionCheckSetId != -1) {
                int size = this.mActions.size();
                for (int i = 0; i < size; i++) {
                    GuidedAction a = (GuidedAction) this.mActions.get(i);
                    if (a != action && a.getCheckSetId() == actionCheckSetId && a.isChecked()) {
                        a.setChecked(false);
                        ViewHolder vh = (ViewHolder) getRecyclerView().findViewHolderForPosition(i);
                        if (vh != null) {
                            this.mStylist.onAnimateItemChecked(vh, false);
                        }
                    }
                }
            }
            if (!action.isChecked()) {
                action.setChecked(true);
                this.mStylist.onAnimateItemChecked(avh, true);
            } else if (actionCheckSetId == -1) {
                action.setChecked(false);
                this.mStylist.onAnimateItemChecked(avh, false);
            }
        }
    }

    public void performOnActionClick(ViewHolder avh) {
        if (this.mClickListener != null) {
            this.mClickListener.onGuidedActionClicked(avh.getAction());
        }
    }
}
