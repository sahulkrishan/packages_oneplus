package com.android.settings.inputmethod;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.UserDictionary.Words;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import com.android.settings.R;
import com.android.settings.Utils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.TreeSet;

public class UserDictionaryAddWordContents {
    public static final String EXTRA_LOCALE = "locale";
    public static final String EXTRA_MODE = "mode";
    public static final String EXTRA_ORIGINAL_SHORTCUT = "originalShortcut";
    public static final String EXTRA_ORIGINAL_WORD = "originalWord";
    public static final String EXTRA_SHORTCUT = "shortcut";
    public static final String EXTRA_WORD = "word";
    private static final int FREQUENCY_FOR_USER_DICTIONARY_ADDS = 250;
    private static final String[] HAS_WORD_PROJECTION = new String[]{EXTRA_WORD};
    private static final String HAS_WORD_SELECTION_ALL_LOCALES = "word=? AND locale is null";
    private static final String HAS_WORD_SELECTION_ONE_LOCALE = "word=? AND locale=?";
    public static final int MODE_EDIT = 0;
    public static final int MODE_INSERT = 1;
    private String mLocale;
    private final int mMode;
    private final String mOldShortcut;
    private final String mOldWord;
    private String mSavedShortcut;
    private String mSavedWord;
    private final EditText mShortcutEditText;
    private final EditText mWordEditText;

    public static class LocaleRenderer {
        private final String mDescription;
        private final String mLocaleString;

        public LocaleRenderer(Context context, String localeString) {
            this.mLocaleString = localeString;
            if (localeString == null) {
                this.mDescription = context.getString(R.string.user_dict_settings_more_languages);
            } else if ("".equals(localeString)) {
                this.mDescription = context.getString(R.string.user_dict_settings_all_languages);
            } else {
                this.mDescription = Utils.createLocaleFromString(localeString).getDisplayName();
            }
        }

        public String toString() {
            return this.mDescription;
        }

        public String getLocaleString() {
            return this.mLocaleString;
        }

        public boolean isMoreLanguages() {
            return this.mLocaleString == null;
        }
    }

    UserDictionaryAddWordContents(View view, Bundle args) {
        this.mWordEditText = (EditText) view.findViewById(R.id.user_dictionary_add_word_text);
        this.mShortcutEditText = (EditText) view.findViewById(R.id.user_dictionary_add_shortcut);
        String word = args.getString(EXTRA_WORD);
        if (word != null) {
            this.mWordEditText.setText(word);
            this.mWordEditText.setSelection(this.mWordEditText.getText().length());
        }
        String shortcut = args.getString(EXTRA_SHORTCUT);
        if (!(shortcut == null || this.mShortcutEditText == null)) {
            this.mShortcutEditText.setText(shortcut);
        }
        this.mMode = args.getInt(EXTRA_MODE);
        this.mOldWord = args.getString(EXTRA_WORD);
        this.mOldShortcut = args.getString(EXTRA_SHORTCUT);
        updateLocale(args.getString("locale"));
    }

    UserDictionaryAddWordContents(View view, UserDictionaryAddWordContents oldInstanceToBeEdited) {
        this.mWordEditText = (EditText) view.findViewById(R.id.user_dictionary_add_word_text);
        this.mShortcutEditText = (EditText) view.findViewById(R.id.user_dictionary_add_shortcut);
        this.mMode = 0;
        this.mOldWord = oldInstanceToBeEdited.mSavedWord;
        this.mOldShortcut = oldInstanceToBeEdited.mSavedShortcut;
        updateLocale(oldInstanceToBeEdited.getCurrentUserDictionaryLocale());
    }

    /* Access modifiers changed, original: 0000 */
    public void updateLocale(String locale) {
        this.mLocale = locale == null ? Locale.getDefault().toString() : locale;
    }

    /* Access modifiers changed, original: 0000 */
    public void saveStateIntoBundle(Bundle outState) {
        outState.putString(EXTRA_WORD, this.mWordEditText.getText().toString());
        outState.putString(EXTRA_ORIGINAL_WORD, this.mOldWord);
        if (this.mShortcutEditText != null) {
            outState.putString(EXTRA_SHORTCUT, this.mShortcutEditText.getText().toString());
        }
        if (this.mOldShortcut != null) {
            outState.putString(EXTRA_ORIGINAL_SHORTCUT, this.mOldShortcut);
        }
        outState.putString("locale", this.mLocale);
    }

    /* Access modifiers changed, original: 0000 */
    public void delete(Context context) {
        if (this.mMode == 0 && !TextUtils.isEmpty(this.mOldWord)) {
            UserDictionarySettings.deleteWord(this.mOldWord, this.mOldShortcut, context.getContentResolver());
        }
    }

    /* Access modifiers changed, original: 0000 */
    public int apply(Context context, Bundle outParameters) {
        String newShortcut;
        if (outParameters != null) {
            saveStateIntoBundle(outParameters);
        }
        ContentResolver resolver = context.getContentResolver();
        if (this.mMode == 0 && !TextUtils.isEmpty(this.mOldWord)) {
            UserDictionarySettings.deleteWord(this.mOldWord, this.mOldShortcut, resolver);
        }
        String newWord = this.mWordEditText.getText().toString();
        if (this.mShortcutEditText == null) {
            newShortcut = null;
        } else {
            newShortcut = this.mShortcutEditText.getText().toString();
            if (TextUtils.isEmpty(newShortcut)) {
                newShortcut = null;
            }
        }
        if (TextUtils.isEmpty(newWord)) {
            return 1;
        }
        this.mSavedWord = newWord;
        this.mSavedShortcut = newShortcut;
        if (TextUtils.isEmpty(newShortcut) && hasWord(newWord, context)) {
            return 2;
        }
        Locale locale = null;
        UserDictionarySettings.deleteWord(newWord, null, resolver);
        if (!TextUtils.isEmpty(newShortcut)) {
            UserDictionarySettings.deleteWord(newWord, newShortcut, resolver);
        }
        String str = newWord.toString();
        if (!TextUtils.isEmpty(this.mLocale)) {
            locale = Utils.createLocaleFromString(this.mLocale);
        }
        Words.addWord(context, str, 250, newShortcut, locale);
        return 0;
    }

    private boolean hasWord(String word, Context context) {
        Cursor cursor;
        boolean z = true;
        if ("".equals(this.mLocale)) {
            cursor = context.getContentResolver().query(Words.CONTENT_URI, HAS_WORD_PROJECTION, HAS_WORD_SELECTION_ALL_LOCALES, new String[]{word}, null);
        } else {
            cursor = context.getContentResolver().query(Words.CONTENT_URI, HAS_WORD_PROJECTION, HAS_WORD_SELECTION_ONE_LOCALE, new String[]{word, this.mLocale}, null);
        }
        if (cursor == null) {
            if (cursor != null) {
                cursor.close();
            }
            return false;
        }
        try {
            if (cursor.getCount() <= 0) {
                z = false;
            }
            if (cursor != null) {
                cursor.close();
            }
            return z;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private static void addLocaleDisplayNameToList(Context context, ArrayList<LocaleRenderer> list, String locale) {
        if (locale != null) {
            list.add(new LocaleRenderer(context, locale));
        }
    }

    public ArrayList<LocaleRenderer> getLocalesList(Activity activity) {
        TreeSet<String> locales = UserDictionaryList.getUserDictionaryLocalesSet(activity);
        locales.remove(this.mLocale);
        String systemLocale = Locale.getDefault().toString();
        locales.remove(systemLocale);
        locales.remove("");
        ArrayList<LocaleRenderer> localesList = new ArrayList();
        addLocaleDisplayNameToList(activity, localesList, this.mLocale);
        if (!systemLocale.equals(this.mLocale)) {
            addLocaleDisplayNameToList(activity, localesList, systemLocale);
        }
        Iterator it = locales.iterator();
        while (it.hasNext()) {
            addLocaleDisplayNameToList(activity, localesList, (String) it.next());
        }
        if (!"".equals(this.mLocale)) {
            addLocaleDisplayNameToList(activity, localesList, "");
        }
        localesList.add(new LocaleRenderer(activity, null));
        return localesList;
    }

    public String getCurrentUserDictionaryLocale() {
        return this.mLocale;
    }
}
