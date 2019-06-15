package androidx.slice.builders.impl;

import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import androidx.slice.Clock;
import androidx.slice.Slice;
import androidx.slice.Slice.Builder;
import androidx.slice.SliceSpec;
import androidx.slice.SystemClock;

@RestrictTo({Scope.LIBRARY})
public abstract class TemplateBuilderImpl {
    private Clock mClock;
    private final Builder mSliceBuilder;
    private final SliceSpec mSpec;

    @RestrictTo({Scope.LIBRARY})
    public abstract void apply(Builder builder);

    protected TemplateBuilderImpl(Builder b, SliceSpec spec) {
        this(b, spec, new SystemClock());
    }

    protected TemplateBuilderImpl(Builder b, SliceSpec spec, Clock clock) {
        this.mSliceBuilder = b;
        this.mSpec = spec;
        this.mClock = clock;
    }

    public Slice build() {
        this.mSliceBuilder.setSpec(this.mSpec);
        apply(this.mSliceBuilder);
        return this.mSliceBuilder.build();
    }

    @RestrictTo({Scope.LIBRARY})
    public Builder getBuilder() {
        return this.mSliceBuilder;
    }

    @RestrictTo({Scope.LIBRARY})
    public Builder createChildBuilder() {
        return new Builder(this.mSliceBuilder);
    }

    @RestrictTo({Scope.LIBRARY})
    public Clock getClock() {
        return this.mClock;
    }
}
