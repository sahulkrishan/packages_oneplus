package android.support.v17.leanback.widget;

import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v17.leanback.widget.GuidedActionAdapter.EditListener;
import android.support.v17.leanback.widget.GuidedActionsStylist.ViewHolder;
import android.util.Pair;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import java.util.ArrayList;

@RestrictTo({Scope.LIBRARY_GROUP})
public class GuidedActionAdapterGroup {
    private static final boolean DEBUG_EDIT = false;
    private static final String TAG_EDIT = "EditableAction";
    ArrayList<Pair<GuidedActionAdapter, GuidedActionAdapter>> mAdapters = new ArrayList();
    private EditListener mEditListener;
    private boolean mImeOpened;

    public void addAdpter(GuidedActionAdapter adapter1, GuidedActionAdapter adapter2) {
        this.mAdapters.add(new Pair(adapter1, adapter2));
        if (adapter1 != null) {
            adapter1.mGroup = this;
        }
        if (adapter2 != null) {
            adapter2.mGroup = this;
        }
    }

    public GuidedActionAdapter getNextAdapter(GuidedActionAdapter adapter) {
        for (int i = 0; i < this.mAdapters.size(); i++) {
            Pair<GuidedActionAdapter, GuidedActionAdapter> pair = (Pair) this.mAdapters.get(i);
            if (pair.first == adapter) {
                return (GuidedActionAdapter) pair.second;
            }
        }
        return null;
    }

    public void setEditListener(EditListener listener) {
        this.mEditListener = listener;
    }

    /* Access modifiers changed, original: 0000 */
    public boolean focusToNextAction(GuidedActionAdapter adapter, GuidedAction action, long nextActionId) {
        int index = 0;
        if (nextActionId == -2) {
            index = adapter.indexOf(action);
            if (index < 0) {
                return false;
            }
            index++;
        }
        while (true) {
            int size = adapter.getCount();
            if (nextActionId == -2) {
                while (index < size && !adapter.getItem(index).isFocusable()) {
                    index++;
                }
            } else {
                while (index < size && adapter.getItem(index).getId() != nextActionId) {
                    index++;
                }
            }
            if (index < size) {
                ViewHolder vh = (ViewHolder) adapter.getGuidedActionsStylist().getActionsGridView().findViewHolderForPosition(index);
                if (vh == null) {
                    return false;
                }
                if (vh.getAction().hasTextEditable()) {
                    openIme(adapter, vh);
                } else {
                    closeIme(vh.itemView);
                    vh.itemView.requestFocus();
                }
                return true;
            }
            adapter = getNextAdapter(adapter);
            if (adapter == null) {
                return false;
            }
            index = 0;
        }
    }

    public void openIme(GuidedActionAdapter adapter, ViewHolder avh) {
        adapter.getGuidedActionsStylist().setEditingMode(avh, true);
        View v = avh.getEditingView();
        if (v != null && avh.isInEditingText()) {
            InputMethodManager mgr = (InputMethodManager) v.getContext().getSystemService("input_method");
            v.setFocusable(true);
            v.requestFocus();
            mgr.showSoftInput(v, 0);
            if (!this.mImeOpened) {
                this.mImeOpened = true;
                this.mEditListener.onImeOpen();
            }
        }
    }

    public void closeIme(View v) {
        if (this.mImeOpened) {
            this.mImeOpened = false;
            ((InputMethodManager) v.getContext().getSystemService("input_method")).hideSoftInputFromWindow(v.getWindowToken(), 0);
            this.mEditListener.onImeClose();
        }
    }

    public void fillAndStay(GuidedActionAdapter adapter, TextView v) {
        ViewHolder avh = adapter.findSubChildViewHolder(v);
        updateTextIntoAction(avh, v);
        this.mEditListener.onGuidedActionEditCanceled(avh.getAction());
        adapter.getGuidedActionsStylist().setEditingMode(avh, false);
        closeIme(v);
        avh.itemView.requestFocus();
    }

    public void fillAndGoNext(GuidedActionAdapter adapter, TextView v) {
        boolean handled = false;
        ViewHolder avh = adapter.findSubChildViewHolder(v);
        updateTextIntoAction(avh, v);
        adapter.performOnActionClick(avh);
        long nextActionId = this.mEditListener.onGuidedActionEditedAndProceed(avh.getAction());
        adapter.getGuidedActionsStylist().setEditingMode(avh, false);
        if (!(nextActionId == -3 || nextActionId == avh.getAction().getId())) {
            handled = focusToNextAction(adapter, avh.getAction(), nextActionId);
        }
        if (!handled) {
            closeIme(v);
            avh.itemView.requestFocus();
        }
    }

    private void updateTextIntoAction(ViewHolder avh, TextView v) {
        GuidedAction action = avh.getAction();
        if (v == avh.getDescriptionView()) {
            if (action.getEditDescription() != null) {
                action.setEditDescription(v.getText());
            } else {
                action.setDescription(v.getText());
            }
        } else if (v != avh.getTitleView()) {
        } else {
            if (action.getEditTitle() != null) {
                action.setEditTitle(v.getText());
            } else {
                action.setTitle(v.getText());
            }
        }
    }
}
