package com.android.settings.connecteddevice;

import android.content.Context;
import android.content.res.Resources;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.search.SearchIndexableRaw;
import java.util.ArrayList;
import java.util.List;

public class PreviouslyConnectedDeviceDashboardFragment extends DashboardFragment {
    static final String KEY_PREVIOUSLY_CONNECTED_DEVICES = "saved_device_list";
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
            List<SearchIndexableRaw> result = new ArrayList();
            Resources res = context.getResources();
            SearchIndexableRaw data = new SearchIndexableRaw(context);
            data.key = PreviouslyConnectedDeviceDashboardFragment.KEY_PREVIOUSLY_CONNECTED_DEVICES;
            data.title = res.getString(R.string.connected_device_previously_connected_title);
            data.screenTitle = res.getString(R.string.connected_device_previously_connected_title);
            result.add(data);
            return result;
        }
    };
    private static final String TAG = "PreConnectedDeviceFrag";

    public int getHelpResource() {
        return R.string.help_url_previously_connected_devices;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.previously_connected_devices;
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    public int getMetricsCategory() {
        return 1370;
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        ((SavedDeviceGroupController) use(SavedDeviceGroupController.class)).init(this);
    }
}
