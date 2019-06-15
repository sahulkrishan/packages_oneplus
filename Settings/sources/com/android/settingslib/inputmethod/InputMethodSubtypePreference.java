package com.android.settingslib.inputmethod;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodSubtype;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.inputmethod.InputMethodUtils;
import java.text.Collator;
import java.util.Locale;

public class InputMethodSubtypePreference extends SwitchWithNoTextPreference {
    private final boolean mIsSystemLanguage;
    private final boolean mIsSystemLocale;

    public InputMethodSubtypePreference(Context context, InputMethodSubtype subtype, InputMethodInfo imi) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(imi.getId());
        stringBuilder.append(subtype.hashCode());
        this(context, stringBuilder.toString(), InputMethodAndSubtypeUtil.getSubtypeLocaleNameAsSentence(subtype, context, imi), subtype.getLocale(), context.getResources().getConfiguration().locale);
    }

    @VisibleForTesting
    InputMethodSubtypePreference(Context context, String prefKey, CharSequence title, String subtypeLocaleString, Locale systemLocale) {
        super(context);
        boolean z = false;
        setPersistent(false);
        setKey(prefKey);
        setTitle(title);
        if (TextUtils.isEmpty(subtypeLocaleString)) {
            this.mIsSystemLocale = false;
            this.mIsSystemLanguage = false;
            return;
        }
        this.mIsSystemLocale = subtypeLocaleString.equals(systemLocale.toString());
        if (this.mIsSystemLocale || InputMethodUtils.getLanguageFromLocaleString(subtypeLocaleString).equals(systemLocale.getLanguage())) {
            z = true;
        }
        this.mIsSystemLanguage = z;
    }

    public int compareTo(Preference rhs, Collator collator) {
        int i = 0;
        if (this == rhs) {
            return 0;
        }
        if (!(rhs instanceof InputMethodSubtypePreference)) {
            return super.compareTo(rhs);
        }
        InputMethodSubtypePreference rhsPref = (InputMethodSubtypePreference) rhs;
        if (this.mIsSystemLocale && !rhsPref.mIsSystemLocale) {
            return -1;
        }
        if (!this.mIsSystemLocale && rhsPref.mIsSystemLocale) {
            return 1;
        }
        if (this.mIsSystemLanguage && !rhsPref.mIsSystemLanguage) {
            return -1;
        }
        if (!this.mIsSystemLanguage && rhsPref.mIsSystemLanguage) {
            return 1;
        }
        CharSequence title = getTitle();
        CharSequence rhsTitle = rhs.getTitle();
        boolean emptyTitle = TextUtils.isEmpty(title);
        boolean rhsEmptyTitle = TextUtils.isEmpty(rhsTitle);
        if (!emptyTitle && !rhsEmptyTitle) {
            return collator.compare(title.toString(), rhsTitle.toString());
        }
        int i2 = emptyTitle ? -1 : 0;
        if (rhsEmptyTitle) {
            i = -1;
        }
        return i2 - i;
    }
}
