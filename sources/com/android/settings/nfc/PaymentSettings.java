package com.android.settings.nfc;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.preference.PreferenceScreen;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.nfc.PaymentBackend.PaymentAppInfo;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.search.SearchIndexableRaw;
import java.util.ArrayList;
import java.util.List;

public class PaymentSettings extends SettingsPreferenceFragment implements Indexable {
    static final String PAYMENT_KEY = "payment";
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
            List<SearchIndexableRaw> result = new ArrayList();
            Resources res = context.getResources();
            SearchIndexableRaw data = new SearchIndexableRaw(context);
            data.key = PaymentSettings.PAYMENT_KEY;
            data.title = res.getString(R.string.nfc_payment_settings_title);
            data.screenTitle = res.getString(R.string.nfc_payment_settings_title);
            data.keywords = res.getString(R.string.keywords_payment_settings);
            result.add(data);
            return result;
        }

        public List<String> getNonIndexableKeys(Context context) {
            List<String> nonVisibleKeys = super.getNonIndexableKeys(context);
            if (context.getPackageManager().hasSystemFeature("android.hardware.nfc")) {
                return nonVisibleKeys;
            }
            nonVisibleKeys.add(PaymentSettings.PAYMENT_KEY);
            return nonVisibleKeys;
        }
    };
    public static final String TAG = "PaymentSettings";
    private PaymentBackend mPaymentBackend;

    public int getMetricsCategory() {
        return 70;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.nfc_payment_settings;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mPaymentBackend = new PaymentBackend(getActivity());
        setHasOptionsMenu(true);
        PreferenceScreen screen = getPreferenceScreen();
        List<PaymentAppInfo> appInfos = this.mPaymentBackend.getPaymentAppInfos();
        if (appInfos != null && appInfos.size() > 0) {
            NfcPaymentPreference preference = new NfcPaymentPreference(getPrefContext(), this.mPaymentBackend);
            preference.setKey(PAYMENT_KEY);
            screen.addPreference(preference);
            screen.addPreference(new NfcForegroundPreference(getPrefContext(), this.mPaymentBackend));
        }
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewGroup contentRoot = (ViewGroup) getListView().getParent();
        View emptyView = getActivity().getLayoutInflater().inflate(R.layout.nfc_payment_empty, contentRoot, false);
        contentRoot.addView(emptyView);
        setEmptyView(emptyView);
    }

    public void onResume() {
        super.onResume();
        this.mPaymentBackend.onResume();
    }

    public void onPause() {
        super.onPause();
        this.mPaymentBackend.onPause();
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem menuItem = menu.add(R.string.nfc_payment_how_it_works);
        menuItem.setIntent(new Intent(getActivity(), HowItWorks.class));
        menuItem.setShowAsActionFlags(0);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != 16908332) {
            return super.onOptionsItemSelected(item);
        }
        finishFragment();
        return true;
    }
}
