package com.android.settings.slices;

import android.app.PendingIntent;
import android.support.v4.util.Consumer;
import androidx.slice.builders.ListBuilder.InputRangeBuilder;
import androidx.slice.builders.SliceAction;
import com.android.settings.core.SliderPreferenceController;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$SliceBuilderUtils$qRPBF1K1kbSIREThP22FAM_L1N0 implements Consumer {
    private final /* synthetic */ SliceData f$0;
    private final /* synthetic */ CharSequence f$1;
    private final /* synthetic */ SliceAction f$2;
    private final /* synthetic */ SliderPreferenceController f$3;
    private final /* synthetic */ PendingIntent f$4;

    public /* synthetic */ -$$Lambda$SliceBuilderUtils$qRPBF1K1kbSIREThP22FAM_L1N0(SliceData sliceData, CharSequence charSequence, SliceAction sliceAction, SliderPreferenceController sliderPreferenceController, PendingIntent pendingIntent) {
        this.f$0 = sliceData;
        this.f$1 = charSequence;
        this.f$2 = sliceAction;
        this.f$3 = sliderPreferenceController;
        this.f$4 = pendingIntent;
    }

    public final void accept(Object obj) {
        ((InputRangeBuilder) obj).setTitle(this.f$0.getTitle()).setSubtitle(this.f$1).setPrimaryAction(this.f$2).setMax(this.f$3.getMaxSteps()).setValue(this.f$3.getSliderPosition()).setInputAction(this.f$4);
    }
}
