package com.android.settings.inputmethod;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.UserDictionary.Words;
import android.support.annotation.NonNull;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import java.util.Iterator;
import java.util.Locale;
import java.util.TreeSet;

public class UserDictionaryList extends SettingsPreferenceFragment {
    public static final String USER_DICTIONARY_SETTINGS_INTENT_ACTION = "android.settings.USER_DICTIONARY_SETTINGS";
    private String mLocale;

    public int getMetricsCategory() {
        return 61;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setPreferenceScreen(getPreferenceManager().createPreferenceScreen(getActivity()));
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().getActionBar().setTitle(R.string.user_dict_settings_title);
        Intent intent = getActivity().getIntent();
        String locale = null;
        String localeFromIntent = intent == null ? null : intent.getStringExtra("locale");
        Bundle arguments = getArguments();
        String localeFromArguments = arguments == null ? null : arguments.getString("locale");
        if (localeFromArguments != null) {
            locale = localeFromArguments;
        } else if (localeFromIntent != null) {
            locale = localeFromIntent;
        }
        this.mLocale = locale;
    }

    @NonNull
    public static TreeSet<String> getUserDictionaryLocalesSet(Context context) {
        Cursor cursor = context.getContentResolver().query(Words.CONTENT_URI, new String[]{"locale"}, null, null, null);
        TreeSet<String> localeSet = new TreeSet();
        if (cursor == null) {
            return localeSet;
        }
        try {
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex("locale");
                do {
                    String locale = cursor.getString(columnIndex);
                    localeSet.add(locale != null ? locale : "");
                } while (cursor.moveToNext());
            }
            cursor.close();
            InputMethodManager imm = (InputMethodManager) context.getSystemService("input_method");
            for (InputMethodInfo imi : imm.getEnabledInputMethodList()) {
                for (InputMethodSubtype subtype : imm.getEnabledInputMethodSubtypeList(imi, true)) {
                    String locale2 = subtype.getLocale();
                    if (!TextUtils.isEmpty(locale2)) {
                        localeSet.add(locale2);
                    }
                }
            }
            if (!localeSet.contains(Locale.getDefault().getLanguage().toString())) {
                localeSet.add(Locale.getDefault().toString());
            }
            return localeSet;
        } catch (Throwable th) {
            cursor.close();
        }
    }

    /* Access modifiers changed, original: protected */
    public void createUserDictSettings(PreferenceGroup userDictGroup) {
        Activity activity = getActivity();
        userDictGroup.removeAll();
        TreeSet<String> localeSet = getUserDictionaryLocalesSet(activity);
        if (this.mLocale != null) {
            localeSet.add(this.mLocale);
        }
        if (localeSet.size() > 1) {
            localeSet.add("");
        }
        if (localeSet.isEmpty()) {
            userDictGroup.addPreference(createUserDictionaryPreference(null, activity));
            return;
        }
        Iterator it = localeSet.iterator();
        while (it.hasNext()) {
            userDictGroup.addPreference(createUserDictionaryPreference((String) it.next(), activity));
        }
    }

    /* Access modifiers changed, original: protected */
    public Preference createUserDictionaryPreference(String locale, Activity activity) {
        Preference newPref = new Preference(getPrefContext());
        Intent intent = new Intent(USER_DICTIONARY_SETTINGS_INTENT_ACTION);
        if (locale == null) {
            newPref.setTitle(Locale.getDefault().getDisplayName());
        } else {
            if ("".equals(locale)) {
                newPref.setTitle(getString(R.string.user_dict_settings_all_languages));
            } else {
                newPref.setTitle(Utils.createLocaleFromString(locale).getDisplayName());
            }
            intent.putExtra("locale", locale);
            newPref.getExtras().putString("locale", locale);
        }
        newPref.setIntent(intent);
        newPref.setFragment(UserDictionarySettings.class.getName());
        return newPref;
    }

    public void onResume() {
        super.onResume();
        createUserDictSettings(getPreferenceScreen());
    }
}
