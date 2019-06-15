package com.android.settings.inputmethod;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import com.android.settings.R;
import com.android.settings.core.InstrumentedFragment;

public class UserDictionaryAddWordFragment extends InstrumentedFragment {
    private static final int OPTIONS_MENU_DELETE = 1;
    private UserDictionaryAddWordContents mContents;
    private boolean mIsDeleting = false;
    private View mRootView;

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        this.mRootView = inflater.inflate(R.layout.user_dictionary_add_word_fullscreen, null);
        this.mIsDeleting = false;
        if (this.mContents == null) {
            this.mContents = new UserDictionaryAddWordContents(this.mRootView, getArguments());
        } else {
            this.mContents = new UserDictionaryAddWordContents(this.mRootView, this.mContents);
        }
        getActivity().getActionBar().setSubtitle(UserDictionarySettingsUtils.getLocaleDisplayName(getActivity(), this.mContents.getCurrentUserDictionaryLocale()));
        return this.mRootView;
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, 1, 0, R.string.delete).setIcon(R.drawable.ic_delete).setShowAsAction(5);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != 1) {
            return false;
        }
        this.mContents.delete(getActivity());
        this.mIsDeleting = true;
        getActivity().onBackPressed();
        return true;
    }

    public int getMetricsCategory() {
        return 62;
    }

    public void onResume() {
        super.onResume();
        updateSpinner();
    }

    private void updateSpinner() {
        new ArrayAdapter(getActivity(), 17367048, this.mContents.getLocalesList(getActivity())).setDropDownViewResource(17367049);
    }

    public void onPause() {
        super.onPause();
        if (!this.mIsDeleting) {
            this.mContents.apply(getActivity(), null);
        }
    }
}
