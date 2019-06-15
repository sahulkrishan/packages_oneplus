package com.android.settings.inputmethod;

import android.app.ActionBar;
import android.app.ListFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.UserDictionary.Words;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AlphabetIndexer;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.core.instrumentation.Instrumentable;
import com.android.settingslib.core.instrumentation.VisibilityLoggerMixin;

public class UserDictionarySettings extends ListFragment implements Instrumentable, LoaderCallbacks<Cursor> {
    private static final String DELETE_SELECTION_WITHOUT_SHORTCUT = "word=? AND shortcut is null OR shortcut=''";
    private static final String DELETE_SELECTION_WITH_SHORTCUT = "word=? AND shortcut=?";
    private static final int LOADER_ID = 1;
    private static final int OPTIONS_MENU_ADD = 1;
    private Cursor mCursor;
    private String mLocale;
    private VisibilityLoggerMixin mVisibilityLoggerMixin;

    private static class MyAdapter extends SimpleCursorAdapter implements SectionIndexer {
        private AlphabetIndexer mIndexer;
        private final ViewBinder mViewBinder = new ViewBinder() {
            public boolean setViewValue(View v, Cursor c, int columnIndex) {
                if (columnIndex != 2) {
                    return false;
                }
                String shortcut = c.getString(2);
                if (TextUtils.isEmpty(shortcut)) {
                    v.setVisibility(8);
                } else {
                    ((TextView) v).setText(shortcut);
                    v.setVisibility(0);
                }
                v.invalidate();
                return true;
            }
        };

        public MyAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
            super(context, layout, c, from, to);
            if (c != null) {
                this.mIndexer = new AlphabetIndexer(c, c.getColumnIndexOrThrow(UserDictionaryAddWordContents.EXTRA_WORD), context.getString(17039915));
            }
            setViewBinder(this.mViewBinder);
        }

        public int getPositionForSection(int section) {
            return this.mIndexer == null ? 0 : this.mIndexer.getPositionForSection(section);
        }

        public int getSectionForPosition(int position) {
            return this.mIndexer == null ? 0 : this.mIndexer.getSectionForPosition(position);
        }

        public Object[] getSections() {
            return this.mIndexer == null ? null : this.mIndexer.getSections();
        }
    }

    public int getMetricsCategory() {
        return 514;
    }

    public void onCreate(Bundle savedInstanceState) {
        String locale;
        super.onCreate(savedInstanceState);
        this.mVisibilityLoggerMixin = new VisibilityLoggerMixin(getMetricsCategory(), FeatureFactory.getFactory(getContext()).getMetricsFeatureProvider());
        Intent intent = getActivity().getIntent();
        String localeFromIntent = intent == null ? null : intent.getStringExtra("locale");
        Bundle arguments = getArguments();
        String localeFromArguments = arguments == null ? null : arguments.getString("locale");
        if (localeFromArguments != null) {
            locale = localeFromArguments;
        } else if (localeFromIntent != null) {
            locale = localeFromIntent;
        } else {
            locale = null;
        }
        this.mLocale = locale;
        setHasOptionsMenu(true);
        getLoaderManager().initLoader(1, null, this);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.user_dict_settings_title);
            actionBar.setSubtitle(UserDictionarySettingsUtils.getLocaleDisplayName(getActivity(), this.mLocale));
        }
        return inflater.inflate(17367236, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView emptyView = (TextView) getView().findViewById(16908292);
        if (emptyView != null) {
            emptyView.setText(R.string.user_dict_settings_empty_text);
        }
        ListView listView = getListView();
        listView.setFastScrollEnabled(true);
        listView.setEmptyView(emptyView);
    }

    public void onResume() {
        super.onResume();
        this.mVisibilityLoggerMixin.onResume();
        getLoaderManager().restartLoader(1, null, this);
    }

    private ListAdapter createAdapter() {
        return new MyAdapter(getActivity(), R.layout.user_dictionary_item, this.mCursor, new String[]{UserDictionaryAddWordContents.EXTRA_WORD, UserDictionaryAddWordContents.EXTRA_SHORTCUT}, new int[]{16908308, 16908309});
    }

    public void onListItemClick(ListView l, View v, int position, long id) {
        String word = getWord(position);
        String shortcut = getShortcut(position);
        if (word != null) {
            showAddOrEditDialog(word, shortcut);
        }
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, 1, 0, R.string.user_dict_settings_add_menu_title).setIcon(R.drawable.ic_menu_add_white).setShowAsAction(5);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != 1) {
            return false;
        }
        showAddOrEditDialog(null, null);
        return true;
    }

    public void onPause() {
        super.onPause();
        this.mVisibilityLoggerMixin.onPause();
    }

    private void showAddOrEditDialog(String editingWord, String editingShortcut) {
        int i;
        Bundle args = new Bundle();
        String str = UserDictionaryAddWordContents.EXTRA_MODE;
        if (editingWord == null) {
            i = 1;
        } else {
            i = 0;
        }
        args.putInt(str, i);
        args.putString(UserDictionaryAddWordContents.EXTRA_WORD, editingWord);
        args.putString(UserDictionaryAddWordContents.EXTRA_SHORTCUT, editingShortcut);
        args.putString("locale", this.mLocale);
        new SubSettingLauncher(getContext()).setDestination(UserDictionaryAddWordFragment.class.getName()).setArguments(args).setTitle((int) R.string.user_dict_settings_add_dialog_title).setSourceMetricsCategory(getMetricsCategory()).launch();
    }

    private String getWord(int position) {
        if (this.mCursor == null) {
            return null;
        }
        this.mCursor.moveToPosition(position);
        if (this.mCursor.isAfterLast()) {
            return null;
        }
        return this.mCursor.getString(this.mCursor.getColumnIndexOrThrow(UserDictionaryAddWordContents.EXTRA_WORD));
    }

    private String getShortcut(int position) {
        if (this.mCursor == null) {
            return null;
        }
        this.mCursor.moveToPosition(position);
        if (this.mCursor.isAfterLast()) {
            return null;
        }
        return this.mCursor.getString(this.mCursor.getColumnIndexOrThrow(UserDictionaryAddWordContents.EXTRA_SHORTCUT));
    }

    public static void deleteWord(String word, String shortcut, ContentResolver resolver) {
        if (TextUtils.isEmpty(shortcut)) {
            resolver.delete(Words.CONTENT_URI, DELETE_SELECTION_WITHOUT_SHORTCUT, new String[]{word});
            return;
        }
        resolver.delete(Words.CONTENT_URI, DELETE_SELECTION_WITH_SHORTCUT, new String[]{word, shortcut});
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new UserDictionaryCursorLoader(getContext(), this.mLocale);
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        this.mCursor = data;
        getListView().setAdapter(createAdapter());
    }

    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
