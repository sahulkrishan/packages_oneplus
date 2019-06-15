package com.android.settings.notification;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.icu.text.ListFormatter;
import android.provider.ContactsContract.Contacts;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceScreen;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.R;
import com.android.settingslib.core.lifecycle.Lifecycle;
import java.util.ArrayList;
import java.util.List;

public class ZenModeStarredContactsPreferenceController extends AbstractZenModePreferenceController implements OnPreferenceClickListener {
    protected static final String KEY = "zen_mode_starred_contacts";
    @VisibleForTesting
    Intent mFallbackIntent;
    private final PackageManager mPackageManager = this.mContext.getPackageManager();
    private Preference mPreference;
    private final int mPriorityCategory;
    @VisibleForTesting
    Intent mStarredContactsIntent = new Intent("com.oneplus.contacts.action.FAVORITES_PEOPLE");

    public ZenModeStarredContactsPreferenceController(Context context, Lifecycle lifecycle, int priorityCategory) {
        super(context, KEY, lifecycle);
        this.mPriorityCategory = priorityCategory;
        this.mStarredContactsIntent.addCategory("android.intent.category.DEFAULT");
        this.mFallbackIntent = new Intent("android.intent.action.MAIN");
        this.mFallbackIntent.addCategory("android.intent.category.APP_CONTACTS");
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mPreference = screen.findPreference(KEY);
        this.mPreference.setOnPreferenceClickListener(this);
    }

    public String getPreferenceKey() {
        return KEY;
    }

    public boolean isAvailable() {
        boolean z = true;
        if (this.mPriorityCategory == 8) {
            if (!(this.mBackend.isPriorityCategoryEnabled(8) && this.mBackend.getPriorityCallSenders() == 2 && isIntentValid())) {
                z = false;
            }
            return z;
        } else if (this.mPriorityCategory != 4) {
            return false;
        } else {
            if (!(this.mBackend.isPriorityCategoryEnabled(4) && this.mBackend.getPriorityMessageSenders() == 2 && isIntentValid())) {
                z = false;
            }
            return z;
        }
    }

    public void updateState(Preference preference) {
        super.updateState(preference);
        List<String> starredContacts = getStarredContacts();
        int numStarredContacts = starredContacts.size();
        List<String> displayContacts = new ArrayList();
        if (numStarredContacts == 0) {
            displayContacts.add(this.mContext.getString(R.string.zen_mode_from_none));
        } else {
            int i = 0;
            while (i < 2 && i < numStarredContacts) {
                displayContacts.add((String) starredContacts.get(i));
                i++;
            }
            if (numStarredContacts == 3) {
                displayContacts.add((String) starredContacts.get(2));
            } else if (numStarredContacts > 2) {
                displayContacts.add(this.mContext.getResources().getQuantityString(R.plurals.zen_mode_starred_contacts_summary_additional_contacts, numStarredContacts - 2, new Object[]{Integer.valueOf(numStarredContacts - 2)}));
            }
        }
        this.mPreference.setSummary(ListFormatter.getInstance().format(displayContacts));
    }

    public boolean onPreferenceClick(Preference preference) {
        if (this.mStarredContactsIntent.resolveActivity(this.mPackageManager) != null) {
            this.mContext.startActivity(this.mStarredContactsIntent);
        } else {
            this.mContext.startActivity(this.mFallbackIntent);
        }
        return true;
    }

    private List<String> getStarredContacts() {
        List<String> starredContacts = new ArrayList();
        Cursor cursor = this.mContext.getContentResolver().query(Contacts.CONTENT_URI, new String[]{"display_name"}, "starred=1", null, "times_contacted");
        if (cursor.moveToFirst()) {
            do {
                starredContacts.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        return starredContacts;
    }

    private boolean isIntentValid() {
        return (this.mStarredContactsIntent.resolveActivity(this.mPackageManager) == null && this.mFallbackIntent.resolveActivity(this.mPackageManager) == null) ? false : true;
    }
}
