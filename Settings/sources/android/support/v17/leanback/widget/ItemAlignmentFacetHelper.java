package android.support.v17.leanback.widget;

import android.graphics.Rect;
import android.support.v17.leanback.widget.ItemAlignmentFacet.ItemAlignmentDef;
import android.view.View;
import android.view.ViewGroup;

class ItemAlignmentFacetHelper {
    private static Rect sRect = new Rect();

    static int getAlignmentPosition(View itemView, ItemAlignmentDef facet, int orientation) {
        LayoutParams p = (LayoutParams) itemView.getLayoutParams();
        View view = itemView;
        if (facet.mViewId != 0) {
            view = itemView.findViewById(facet.mViewId);
            if (view == null) {
                view = itemView;
            }
        }
        int alignPos = facet.mOffset;
        int alignPos2;
        if (orientation != 0) {
            if (facet.mOffsetWithPadding) {
                if (facet.mOffsetPercent == 0.0f) {
                    alignPos += view.getPaddingTop();
                } else if (facet.mOffsetPercent == 100.0f) {
                    alignPos -= view.getPaddingBottom();
                }
            }
            if (facet.mOffsetPercent != -1.0f) {
                alignPos += (int) ((((float) (view == itemView ? p.getOpticalHeight(view) : view.getHeight())) * facet.mOffsetPercent) / 100.0f);
            }
            if (itemView != view) {
                sRect.top = alignPos;
                ((ViewGroup) itemView).offsetDescendantRectToMyCoords(view, sRect);
                alignPos2 = sRect.top - p.getOpticalTopInset();
            } else {
                alignPos2 = alignPos;
            }
            if (facet.isAlignedToTextViewBaseLine()) {
                return alignPos2 + view.getBaseline();
            }
            return alignPos2;
        } else if (itemView.getLayoutDirection() == 1) {
            if (view == itemView) {
                alignPos2 = p.getOpticalWidth(view);
            } else {
                alignPos2 = view.getWidth();
            }
            alignPos2 -= alignPos;
            if (facet.mOffsetWithPadding) {
                if (facet.mOffsetPercent == 0.0f) {
                    alignPos2 -= view.getPaddingRight();
                } else if (facet.mOffsetPercent == 100.0f) {
                    alignPos2 += view.getPaddingLeft();
                }
            }
            if (facet.mOffsetPercent != -1.0f) {
                if (view == itemView) {
                    alignPos = p.getOpticalWidth(view);
                } else {
                    alignPos = view.getWidth();
                }
                alignPos2 -= (int) ((((float) alignPos) * facet.mOffsetPercent) / 100.0f);
            }
            if (itemView == view) {
                return alignPos2;
            }
            sRect.right = alignPos2;
            ((ViewGroup) itemView).offsetDescendantRectToMyCoords(view, sRect);
            return sRect.right + p.getOpticalRightInset();
        } else {
            if (facet.mOffsetWithPadding) {
                if (facet.mOffsetPercent == 0.0f) {
                    alignPos += view.getPaddingLeft();
                } else if (facet.mOffsetPercent == 100.0f) {
                    alignPos -= view.getPaddingRight();
                }
            }
            if (facet.mOffsetPercent != -1.0f) {
                int opticalWidth;
                if (view == itemView) {
                    opticalWidth = p.getOpticalWidth(view);
                } else {
                    opticalWidth = view.getWidth();
                }
                alignPos += (int) ((((float) opticalWidth) * facet.mOffsetPercent) / 100.0f);
            }
            alignPos2 = alignPos;
            if (itemView == view) {
                return alignPos2;
            }
            sRect.left = alignPos2;
            ((ViewGroup) itemView).offsetDescendantRectToMyCoords(view, sRect);
            return sRect.left - p.getOpticalLeftInset();
        }
    }

    private ItemAlignmentFacetHelper() {
    }
}
