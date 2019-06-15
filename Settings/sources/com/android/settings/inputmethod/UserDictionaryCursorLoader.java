package com.android.settings.inputmethod;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.provider.UserDictionary.Words;
import android.support.annotation.VisibleForTesting;
import android.util.ArraySet;
import com.oneplus.settings.utils.OPFirewallUtils;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public class UserDictionaryCursorLoader extends CursorLoader {
    static final int INDEX_SHORTCUT = 2;
    @VisibleForTesting
    static final String[] QUERY_PROJECTION = new String[]{OPFirewallUtils._ID, UserDictionaryAddWordContents.EXTRA_WORD, UserDictionaryAddWordContents.EXTRA_SHORTCUT};
    private static final String QUERY_SELECTION = "locale=?";
    private static final String QUERY_SELECTION_ALL_LOCALES = "locale is null";
    private final String mLocale;

    public UserDictionaryCursorLoader(Context context, String locale) {
        super(context);
        this.mLocale = locale;
    }

    public Cursor loadInBackground() {
        Cursor candidate;
        MatrixCursor result = new MatrixCursor(QUERY_PROJECTION);
        if ("".equals(this.mLocale)) {
            candidate = getContext().getContentResolver().query(Words.CONTENT_URI, QUERY_PROJECTION, QUERY_SELECTION_ALL_LOCALES, null, "UPPER(word)");
        } else {
            String queryLocale = this.mLocale != null ? this.mLocale : Locale.getDefault().toString();
            candidate = getContext().getContentResolver().query(Words.CONTENT_URI, QUERY_PROJECTION, QUERY_SELECTION, new String[]{queryLocale}, "UPPER(word)");
        }
        Set<Integer> hashSet = new ArraySet();
        candidate.moveToFirst();
        while (!candidate.isAfterLast()) {
            int id = candidate.getInt(0);
            String word = candidate.getString(1);
            String shortcut = candidate.getString(2);
            int hash = Objects.hash(new Object[]{word, shortcut});
            if (!hashSet.contains(Integer.valueOf(hash))) {
                hashSet.add(Integer.valueOf(hash));
                result.addRow(new Object[]{Integer.valueOf(id), word, shortcut});
            }
            candidate.moveToNext();
        }
        return result;
    }
}
