package com.android.settings.widget;

import android.widget.Switch;
import com.android.settings.widget.SwitchBar.OnSwitchChangeListener;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$SwitchBar$xcPsCGGwUScwZOtx6bxg2zuPXc8 implements OnSwitchChangeListener {
    private final /* synthetic */ SwitchBar f$0;

    public /* synthetic */ -$$Lambda$SwitchBar$xcPsCGGwUScwZOtx6bxg2zuPXc8(SwitchBar switchBar) {
        this.f$0 = switchBar;
    }

    public final void onSwitchChanged(Switch switchR, boolean z) {
        this.f$0.setTextViewLabelAndBackground(z);
    }
}
