package com.android.settings.widget;

import android.graphics.Rect;
import android.view.TouchDelegate;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$SwitchBar$H3bwEmU9c2USPE1paf4Zlyfzp3I implements Runnable {
    private final /* synthetic */ SwitchBar f$0;

    public /* synthetic */ -$$Lambda$SwitchBar$H3bwEmU9c2USPE1paf4Zlyfzp3I(SwitchBar switchBar) {
        this.f$0 = switchBar;
    }

    public final void run() {
        this.f$0.setTouchDelegate(new TouchDelegate(new Rect(0, 0, this.f$0.getWidth(), this.f$0.getHeight()), this.f$0.getDelegatingView()));
    }
}
