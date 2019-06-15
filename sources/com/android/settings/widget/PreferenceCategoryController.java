package com.android.settings.widget;

import android.content.Context;
import com.android.settings.core.BasePreferenceController;
import com.android.settingslib.core.AbstractPreferenceController;
import java.util.ArrayList;
import java.util.List;

public class PreferenceCategoryController extends BasePreferenceController {
    private final List<AbstractPreferenceController> mChildren = new ArrayList();
    private final String mKey;

    public PreferenceCategoryController(Context context, String key) {
        super(context, key);
        this.mKey = key;
    }

    public int getAvailabilityStatus() {
        if (this.mChildren == null || this.mChildren.isEmpty()) {
            return 2;
        }
        for (AbstractPreferenceController controller : this.mChildren) {
            if (controller.isAvailable()) {
                return 0;
            }
        }
        return 1;
    }

    public String getPreferenceKey() {
        return this.mKey;
    }

    public PreferenceCategoryController setChildren(List<AbstractPreferenceController> childrenController) {
        this.mChildren.clear();
        if (childrenController != null) {
            this.mChildren.addAll(childrenController);
        }
        return this;
    }
}
