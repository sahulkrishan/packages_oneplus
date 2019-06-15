package com.android.settings.dashboard;

import android.support.v4.view.ViewCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView.ViewHolder;
import com.android.settingslib.drawer.Tile;

public class DashboardItemAnimator extends DefaultItemAnimator {
    public boolean animateChange(ViewHolder oldHolder, ViewHolder newHolder, int fromX, int fromY, int toX, int toY) {
        if ((oldHolder.itemView.getTag() instanceof Tile) && oldHolder == newHolder) {
            if (!isRunning()) {
                fromX = (int) (((float) fromX) + ViewCompat.getTranslationX(oldHolder.itemView));
                fromY = (int) (((float) fromY) + ViewCompat.getTranslationY(oldHolder.itemView));
            }
            if (fromX == toX && fromY == toY) {
                dispatchMoveFinished(oldHolder);
                return false;
            }
        }
        return super.animateChange(oldHolder, newHolder, fromX, fromY, toX, toY);
    }
}
