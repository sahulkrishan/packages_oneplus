package com.android.settings.localepicker;

import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.os.LocaleList;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.android.internal.app.LocalePicker;
import com.android.internal.app.LocalePickerWithRegion;
import com.android.internal.app.LocalePickerWithRegion.LocaleSelectedListener;
import com.android.internal.app.LocaleStore;
import com.android.internal.app.LocaleStore.LocaleInfo;
import com.android.settings.R;
import com.android.settings.RestrictedSettingsFragment;
import java.util.ArrayList;
import java.util.List;

public class LocaleListEditor extends RestrictedSettingsFragment implements LocaleSelectedListener {
    private static final String CFGKEY_REMOVE_DIALOG = "showingLocaleRemoveDialog";
    private static final String CFGKEY_REMOVE_MODE = "localeRemoveMode";
    private static final int MENU_ID_REMOVE = 2;
    private LocaleDragAndDropAdapter mAdapter;
    private View mAddLanguage;
    private boolean mIsUiRestricted;
    private Menu mMenu;
    private boolean mRemoveMode;
    private boolean mShowingRemoveDialog;

    public LocaleListEditor() {
        super("no_config_locale");
    }

    public int getMetricsCategory() {
        return 344;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        LocaleStore.fillCache(getContext());
        this.mAdapter = new LocaleDragAndDropAdapter(getContext(), getUserLocaleList());
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstState) {
        View result = super.onCreateView(inflater, container, savedInstState);
        configureDragAndDrop(inflater.inflate(R.layout.locale_order_list, (ViewGroup) result));
        return result;
    }

    public void onResume() {
        super.onResume();
        boolean previouslyRestricted = this.mIsUiRestricted;
        this.mIsUiRestricted = isUiRestricted();
        TextView emptyView = getEmptyTextView();
        if (this.mIsUiRestricted && !previouslyRestricted) {
            emptyView.setText(R.string.language_empty_list_user_restricted);
            emptyView.setVisibility(0);
            updateVisibilityOfRemoveMenu();
        } else if (!this.mIsUiRestricted && previouslyRestricted) {
            emptyView.setVisibility(8);
            updateVisibilityOfRemoveMenu();
        }
    }

    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            this.mRemoveMode = savedInstanceState.getBoolean(CFGKEY_REMOVE_MODE, false);
            this.mShowingRemoveDialog = savedInstanceState.getBoolean(CFGKEY_REMOVE_DIALOG, false);
        }
        setRemoveMode(this.mRemoveMode);
        this.mAdapter.restoreState(savedInstanceState);
        if (this.mShowingRemoveDialog) {
            showRemoveLocaleWarningDialog();
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(CFGKEY_REMOVE_MODE, this.mRemoveMode);
        outState.putBoolean(CFGKEY_REMOVE_DIALOG, this.mShowingRemoveDialog);
        this.mAdapter.saveState(outState);
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int itemId = menuItem.getItemId();
        if (itemId == 2) {
            if (this.mRemoveMode) {
                showRemoveLocaleWarningDialog();
            } else {
                setRemoveMode(true);
            }
            return true;
        } else if (itemId != 16908332 || !this.mRemoveMode) {
            return super.onOptionsItemSelected(menuItem);
        } else {
            setRemoveMode(false);
            return true;
        }
    }

    private void setRemoveMode(boolean mRemoveMode) {
        this.mRemoveMode = mRemoveMode;
        this.mAdapter.setRemoveMode(mRemoveMode);
        this.mAddLanguage.setVisibility(mRemoveMode ? 4 : 0);
        updateVisibilityOfRemoveMenu();
    }

    private void showRemoveLocaleWarningDialog() {
        int checkedCount = this.mAdapter.getCheckedCount();
        if (checkedCount == 0) {
            setRemoveMode(1 ^ this.mRemoveMode);
        } else if (checkedCount == this.mAdapter.getItemCount()) {
            this.mShowingRemoveDialog = true;
            new Builder(getActivity()).setTitle(R.string.dlg_remove_locales_error_title).setMessage(R.string.dlg_remove_locales_error_message).setPositiveButton(17039379, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }
            }).setOnDismissListener(new OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    LocaleListEditor.this.mShowingRemoveDialog = false;
                }
            }).create().show();
        } else {
            String title = getResources().getQuantityString(R.plurals.dlg_remove_locales_title, checkedCount);
            this.mShowingRemoveDialog = true;
            new Builder(getActivity()).setTitle(title).setMessage(R.string.dlg_remove_locales_message).setNegativeButton(17039369, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    LocaleListEditor.this.setRemoveMode(false);
                }
            }).setPositiveButton(17039379, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    LocaleListEditor.this.mRemoveMode = false;
                    LocaleListEditor.this.mShowingRemoveDialog = false;
                    LocaleListEditor.this.mAdapter.removeChecked();
                    LocaleListEditor.this.setRemoveMode(false);
                }
            }).setOnDismissListener(new OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    LocaleListEditor.this.mShowingRemoveDialog = false;
                }
            }).create().show();
        }
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem menuItem = menu.add(0, 2, 0, R.string.locale_remove_menu);
        menuItem.setShowAsAction(4);
        menuItem.setIcon(R.drawable.ic_delete);
        super.onCreateOptionsMenu(menu, inflater);
        this.mMenu = menu;
        updateVisibilityOfRemoveMenu();
    }

    private List<LocaleInfo> getUserLocaleList() {
        List<LocaleInfo> result = new ArrayList();
        LocaleList localeList = LocalePicker.getLocales();
        for (int i = 0; i < localeList.size(); i++) {
            result.add(LocaleStore.getLocaleInfo(localeList.get(i)));
        }
        return result;
    }

    private void configureDragAndDrop(View view) {
        RecyclerView list = (RecyclerView) view.findViewById(R.id.dragList);
        LocaleLinearLayoutManager llm = new LocaleLinearLayoutManager(getContext(), this.mAdapter);
        llm.setAutoMeasureEnabled(true);
        list.setLayoutManager(llm);
        list.setHasFixedSize(true);
        this.mAdapter.setRecyclerView(list);
        list.setAdapter(this.mAdapter);
        this.mAddLanguage = view.findViewById(R.id.add_language);
        this.mAddLanguage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                LocaleListEditor.this.getFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).replace(LocaleListEditor.this.getId(), LocalePickerWithRegion.createLanguagePicker(LocaleListEditor.this.getContext(), LocaleListEditor.this, true)).addToBackStack("localeListEditor").commit();
            }
        });
    }

    public void onLocaleSelected(LocaleInfo locale) {
        this.mAdapter.addLocale(locale);
        updateVisibilityOfRemoveMenu();
    }

    private void updateVisibilityOfRemoveMenu() {
        if (this.mMenu != null) {
            int i = 2;
            MenuItem menuItemRemove = this.mMenu.findItem(2);
            if (menuItemRemove != null) {
                if (!this.mRemoveMode) {
                    i = 0;
                }
                menuItemRemove.setShowAsAction(i);
                boolean z = true;
                if (!(this.mAdapter.getItemCount() > 1) || this.mIsUiRestricted) {
                    z = false;
                }
                menuItemRemove.setVisible(z);
            }
        }
    }
}
