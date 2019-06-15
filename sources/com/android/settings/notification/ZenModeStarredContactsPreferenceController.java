package com.android.settings.notification;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.text.ListFormatter;
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

    /* JADX WARNING: Missing block: B:6:0x0030, code skipped:
            if (r1 != null) goto L_0x003d;
     */
    /* JADX WARNING: Missing block: B:12:0x003b, code skipped:
            if (r1 == null) goto L_0x0040;
     */
    /* JADX WARNING: Missing block: B:13:0x003d, code skipped:
            r1.close();
     */
    /* JADX WARNING: Missing block: B:14:0x0040, code skipped:
            return r0;
     */
    private java.util.List<java.lang.String> getStarredContacts() {
        /*
        r8 = this;
        r0 = new java.util.ArrayList;
        r0.<init>();
        r1 = r8.mContext;
        r2 = r1.getContentResolver();
        r3 = android.provider.ContactsContract.Contacts.CONTENT_URI;
        r1 = "display_name";
        r4 = new java.lang.String[]{r1};
        r5 = "starred=1";
        r7 = "times_contacted";
        r6 = 0;
        r1 = r2.query(r3, r4, r5, r6, r7);
        r2 = r1.moveToFirst();	 Catch:{ Exception -> 0x003a, all -> 0x0033 }
        if (r2 == 0) goto L_0x0030;
    L_0x0022:
        r2 = 0;
        r2 = r1.getString(r2);	 Catch:{ Exception -> 0x003a, all -> 0x0033 }
        r0.add(r2);	 Catch:{ Exception -> 0x003a, all -> 0x0033 }
        r2 = r1.moveToNext();	 Catch:{ Exception -> 0x003a, all -> 0x0033 }
        if (r2 != 0) goto L_0x0022;
    L_0x0030:
        if (r1 == 0) goto L_0x0040;
    L_0x0032:
        goto L_0x003d;
    L_0x0033:
        r2 = move-exception;
        if (r1 == 0) goto L_0x0039;
    L_0x0036:
        r1.close();
    L_0x0039:
        throw r2;
    L_0x003a:
        r2 = move-exception;
        if (r1 == 0) goto L_0x0040;
    L_0x003d:
        r1.close();
    L_0x0040:
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settings.notification.ZenModeStarredContactsPreferenceController.getStarredContacts():java.util.List");
    }

    private boolean isIntentValid() {
        return (this.mStarredContactsIntent.resolveActivity(this.mPackageManager) == null && this.mFallbackIntent.resolveActivity(this.mPackageManager) == null) ? false : true;
    }
}
