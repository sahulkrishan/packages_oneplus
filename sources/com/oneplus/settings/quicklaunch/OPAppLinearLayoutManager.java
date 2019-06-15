package com.oneplus.settings.quicklaunch;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView.Recycler;
import android.support.v7.widget.RecyclerView.State;
import android.view.View;
import com.android.settings.R;

public class OPAppLinearLayoutManager extends LinearLayoutManager {
    private final AccessibilityActionCompat mActionMoveBottom = new AccessibilityActionCompat(R.id.action_drag_move_bottom, this.mContext.getString(R.string.action_drag_label_move_bottom));
    private final AccessibilityActionCompat mActionMoveDown = new AccessibilityActionCompat(R.id.action_drag_move_down, this.mContext.getString(R.string.action_drag_label_move_down));
    private final AccessibilityActionCompat mActionMoveTop = new AccessibilityActionCompat(R.id.action_drag_move_top, this.mContext.getString(R.string.action_drag_label_move_top));
    private final AccessibilityActionCompat mActionMoveUp = new AccessibilityActionCompat(R.id.action_drag_move_up, this.mContext.getString(R.string.action_drag_label_move_up));
    private final AccessibilityActionCompat mActionRemove = new AccessibilityActionCompat(R.id.action_drag_remove, this.mContext.getString(R.string.action_drag_label_remove));
    private final OPAppDragAndDropAdapter mAdapter;
    private final Context mContext;

    public OPAppLinearLayoutManager(Context context, OPAppDragAndDropAdapter adapter) {
        super(context);
        this.mContext = context;
        this.mAdapter = adapter;
    }

    public void onInitializeAccessibilityNodeInfoForItem(Recycler recycler, State state, View host, AccessibilityNodeInfoCompat info) {
        super.onInitializeAccessibilityNodeInfoForItem(recycler, state, host, info);
        int itemCount = getItemCount();
        int position = getPosition(host);
        OPAppDragCell dragCell = (OPAppDragCell) host;
        String description = new StringBuilder();
        description.append(position + 1);
        description.append(", ");
        description.append(dragCell.getCheckbox().getContentDescription());
        info.setContentDescription(description.toString());
        if (!this.mAdapter.isRemoveMode()) {
            if (position > 0) {
                info.addAction(this.mActionMoveUp);
                info.addAction(this.mActionMoveTop);
            }
            if (position + 1 < itemCount) {
                info.addAction(this.mActionMoveDown);
                info.addAction(this.mActionMoveBottom);
            }
            if (itemCount > 1) {
                info.addAction(this.mActionRemove);
            }
        }
    }

    public boolean performAccessibilityActionForItem(Recycler recycler, State state, View host, int action, Bundle args) {
        int itemCount = getItemCount();
        int position = getPosition(host);
        boolean result = false;
        switch (action) {
            case R.id.action_drag_move_bottom /*2131361825*/:
                if (position != itemCount - 1) {
                    this.mAdapter.onItemMove(position, itemCount - 1);
                    result = true;
                    break;
                }
                break;
            case R.id.action_drag_move_down /*2131361826*/:
                if (position + 1 < itemCount) {
                    this.mAdapter.onItemMove(position, position + 1);
                    result = true;
                    break;
                }
                break;
            case R.id.action_drag_move_top /*2131361827*/:
                if (position != 0) {
                    this.mAdapter.onItemMove(position, 0);
                    result = true;
                    break;
                }
                break;
            case R.id.action_drag_move_up /*2131361828*/:
                if (position > 0) {
                    this.mAdapter.onItemMove(position, position - 1);
                    result = true;
                    break;
                }
                break;
            case R.id.action_drag_remove /*2131361829*/:
                if (itemCount > 1) {
                    this.mAdapter.removeItem(position);
                    result = true;
                    break;
                }
                break;
            default:
                return super.performAccessibilityActionForItem(recycler, state, host, action, args);
        }
        if (result) {
            this.mAdapter.doTheUpdate();
        }
        return result;
    }
}
