package android.support.v17.leanback.widget;

import android.support.annotation.NonNull;
import android.text.TextUtils;

public class GuidedActionDiffCallback extends DiffCallback<GuidedAction> {
    static final GuidedActionDiffCallback sInstance = new GuidedActionDiffCallback();

    public static GuidedActionDiffCallback getInstance() {
        return sInstance;
    }

    public boolean areItemsTheSame(@NonNull GuidedAction oldItem, @NonNull GuidedAction newItem) {
        boolean z = true;
        if (oldItem == null) {
            if (newItem != null) {
                z = false;
            }
            return z;
        } else if (newItem == null) {
            return false;
        } else {
            if (oldItem.getId() != newItem.getId()) {
                z = false;
            }
            return z;
        }
    }

    public boolean areContentsTheSame(@NonNull GuidedAction oldItem, @NonNull GuidedAction newItem) {
        boolean z = true;
        if (oldItem == null) {
            if (newItem != null) {
                z = false;
            }
            return z;
        } else if (newItem == null) {
            return false;
        } else {
            if (!(oldItem.getCheckSetId() == newItem.getCheckSetId() && oldItem.mActionFlags == newItem.mActionFlags && TextUtils.equals(oldItem.getTitle(), newItem.getTitle()) && TextUtils.equals(oldItem.getDescription(), newItem.getDescription()) && oldItem.getInputType() == newItem.getInputType() && TextUtils.equals(oldItem.getEditTitle(), newItem.getEditTitle()) && TextUtils.equals(oldItem.getEditDescription(), newItem.getEditDescription()) && oldItem.getEditInputType() == newItem.getEditInputType() && oldItem.getDescriptionEditInputType() == newItem.getDescriptionEditInputType())) {
                z = false;
            }
            return z;
        }
    }
}
