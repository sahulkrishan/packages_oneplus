package com.android.settings.deviceinfo;

import com.oneplus.settings.utils.OPUtils;
import java.util.function.Predicate;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$lwniU_peSTkeyPRoDzcAe5p0U-0 implements Predicate {
    public static final /* synthetic */ -$$Lambda$lwniU_peSTkeyPRoDzcAe5p0U-0 INSTANCE = new -$$Lambda$lwniU_peSTkeyPRoDzcAe5p0U-0();

    private /* synthetic */ -$$Lambda$lwniU_peSTkeyPRoDzcAe5p0U-0() {
    }

    public final boolean test(Object obj) {
        return OPUtils.isEmojiCharacter(((Character) obj).charValue());
    }
}
