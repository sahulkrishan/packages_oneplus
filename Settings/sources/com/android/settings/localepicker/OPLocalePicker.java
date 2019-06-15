package com.android.settings.localepicker;

import android.app.Dialog;
import android.os.Bundle;
import android.os.LocaleList;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.android.internal.app.LocalePicker;
import com.android.internal.app.LocalePicker.LocaleSelectionListener;
import com.android.internal.app.LocaleStore;
import com.android.settings.DialogCreatable;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment.SettingsDialogFragment;
import com.android.settings.Utils;
import com.oneplus.settings.utils.OPUtils;
import java.util.Locale;

public class OPLocalePicker extends LocalePicker implements LocaleSelectionListener, DialogCreatable {
    private static final int DLG_SHOW_GLOBAL_WARNING = 1;
    private static final String SAVE_TARGET_LOCALE = "locale";
    private static final String TAG = "LocalePicker";
    private SettingsDialogFragment mDialogFragment;
    private Locale mTargetLocale;

    public OPLocalePicker() {
        setLocaleSelectionListener(this);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.containsKey("locale")) {
            this.mTargetLocale = new Locale(savedInstanceState.getString("locale"));
        }
        setHasOptionsMenu(true);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        Utils.forcePrepareCustomPreferencesList(container, view, (ListView) view.findViewById(16908298), false);
        return view;
    }

    public void onLocaleSelected(Locale locale) {
        getActivity().onBackPressed();
        LocaleList localeList = new LocaleList(OPUtils.ZH_EN_ID.equals(LocaleStore.getLocaleInfo(locale).getId()) ? new Locale[]{locale} : new Locale[]{locale, Locale.forLanguageTag(OPUtils.ZH_EN_ID)});
        LocaleList.setDefault(localeList);
        updateLocales(localeList);
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (this.mTargetLocale != null) {
            outState.putString("locale", this.mTargetLocale.toString());
        }
    }

    /* Access modifiers changed, original: protected */
    public void showDialog(int dialogId) {
        if (this.mDialogFragment != null) {
            Log.e(TAG, "Old dialog fragment not null!");
        }
        this.mDialogFragment = new SettingsDialogFragment(this, dialogId);
        this.mDialogFragment.show(getActivity().getFragmentManager(), Integer.toString(dialogId));
    }

    public Dialog onCreateDialog(final int dialogId) {
        return Utils.buildGlobalChangeWarningDialog(getActivity(), R.string.pref_title_lang_selection, new Runnable() {
            public void run() {
                OPLocalePicker.this.removeDialog(dialogId);
                OPLocalePicker.this.getActivity().onBackPressed();
                OPLocalePicker.updateLocale(OPLocalePicker.this.mTargetLocale);
            }
        });
    }

    /* Access modifiers changed, original: protected */
    public void removeDialog(int dialogId) {
        if (this.mDialogFragment != null && this.mDialogFragment.getDialogId() == dialogId) {
            this.mDialogFragment.dismiss();
        }
        this.mDialogFragment = null;
    }

    public int getDialogMetricsCategory(int dialogId) {
        return 0;
    }
}
