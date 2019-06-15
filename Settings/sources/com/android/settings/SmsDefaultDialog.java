package com.android.settings;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController.AlertParams;
import com.android.internal.telephony.SmsApplication;
import com.android.internal.telephony.SmsApplication.SmsApplicationData;
import java.util.ArrayList;
import java.util.List;

public final class SmsDefaultDialog extends AlertActivity implements OnClickListener {
    private SmsApplicationData mNewSmsApplicationData;

    private class AppListAdapter extends BaseAdapter {
        private final List<Item> mItems = getItems();
        private final int mSelectedIndex;

        private class Item {
            final Drawable icon;
            final String label;
            final String packgeName;

            public Item(String label, Drawable icon, String packageName) {
                this.label = label;
                this.icon = icon;
                this.packgeName = packageName;
            }
        }

        public AppListAdapter() {
            int selected = getSelectedIndex();
            if (selected > 0) {
                this.mItems.add(0, (Item) this.mItems.remove(selected));
                selected = 0;
            }
            this.mSelectedIndex = selected;
        }

        public int getCount() {
            return this.mItems != null ? this.mItems.size() : 0;
        }

        public Object getItem(int position) {
            return (this.mItems == null || position >= this.mItems.size()) ? null : this.mItems.get(position);
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            Item item = (Item) getItem(position);
            View view = SmsDefaultDialog.this.getLayoutInflater().inflate(R.layout.app_preference_item, parent, false);
            ((TextView) view.findViewById(16908310)).setText(item.label);
            if (position == this.mSelectedIndex) {
                view.findViewById(R.id.default_label).setVisibility(0);
            } else {
                view.findViewById(R.id.default_label).setVisibility(8);
            }
            ((ImageView) view.findViewById(16908294)).setImageDrawable(item.icon);
            return view;
        }

        public String getPackageName(int position) {
            Item item = (Item) getItem(position);
            if (item != null) {
                return item.packgeName;
            }
            return null;
        }

        public boolean isSelected(int position) {
            return position == this.mSelectedIndex;
        }

        private List<Item> getItems() {
            PackageManager pm = SmsDefaultDialog.this.getPackageManager();
            List<Item> items = new ArrayList();
            for (SmsApplicationData app : SmsApplication.getApplicationCollection(SmsDefaultDialog.this)) {
                try {
                    String packageName = app.mPackageName;
                    ApplicationInfo appInfo = pm.getApplicationInfo(packageName, null);
                    if (appInfo != null) {
                        items.add(new Item(appInfo.loadLabel(pm).toString(), appInfo.loadIcon(pm), packageName));
                    }
                } catch (NameNotFoundException e) {
                }
            }
            return items;
        }

        private int getSelectedIndex() {
            ComponentName appName = SmsApplication.getDefaultSmsApplication(SmsDefaultDialog.this, true);
            if (appName != null) {
                String defaultSmsAppPackageName = appName.getPackageName();
                if (!TextUtils.isEmpty(defaultSmsAppPackageName)) {
                    for (int i = 0; i < this.mItems.size(); i++) {
                        if (TextUtils.equals(((Item) this.mItems.get(i)).packgeName, defaultSmsAppPackageName)) {
                            return i;
                        }
                    }
                }
            }
            return -1;
        }
    }

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String packageName = getIntent().getStringExtra("package");
        setResult(0);
        if (!buildDialog(packageName)) {
            finish();
        }
    }

    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case -2:
                return;
            case -1:
                SmsApplication.setDefaultApplication(this.mNewSmsApplicationData.mPackageName, this);
                setResult(-1);
                return;
            default:
                if (which >= 0) {
                    AppListAdapter adapter = this.mAlertParams.mAdapter;
                    if (!adapter.isSelected(which)) {
                        String packageName = adapter.getPackageName(which);
                        if (!TextUtils.isEmpty(packageName)) {
                            SmsApplication.setDefaultApplication(packageName, this);
                            setResult(-1);
                            return;
                        }
                        return;
                    }
                    return;
                }
                return;
        }
    }

    private boolean buildDialog(String packageName) {
        if (!((TelephonyManager) getSystemService("phone")).isSmsCapable()) {
            return false;
        }
        AlertParams p = this.mAlertParams;
        p.mTitle = getString(R.string.sms_change_default_dialog_title);
        this.mNewSmsApplicationData = SmsApplication.getSmsApplicationData(packageName, this);
        if (this.mNewSmsApplicationData != null) {
            SmsApplicationData oldSmsApplicationData = null;
            ComponentName oldSmsComponent = SmsApplication.getDefaultSmsApplication(this, true);
            if (oldSmsComponent != null) {
                oldSmsApplicationData = SmsApplication.getSmsApplicationData(oldSmsComponent.getPackageName(), this);
                if (oldSmsApplicationData != null && oldSmsApplicationData.mPackageName.equals(this.mNewSmsApplicationData.mPackageName)) {
                    return false;
                }
            }
            if (oldSmsApplicationData != null) {
                p.mMessage = getString(R.string.sms_change_default_dialog_text, new Object[]{this.mNewSmsApplicationData.getApplicationName(this), oldSmsApplicationData.getApplicationName(this)});
            } else {
                p.mMessage = getString(R.string.sms_change_default_no_previous_dialog_text, new Object[]{this.mNewSmsApplicationData.getApplicationName(this)});
            }
            p.mPositiveButtonText = getString(R.string.yes);
            p.mNegativeButtonText = getString(R.string.no);
            p.mPositiveButtonListener = this;
            p.mNegativeButtonListener = this;
        } else {
            p.mAdapter = new AppListAdapter();
            p.mOnClickListener = this;
            if (p.mAdapter.isEmpty()) {
                return false;
            }
        }
        setupAlert();
        return true;
    }
}
