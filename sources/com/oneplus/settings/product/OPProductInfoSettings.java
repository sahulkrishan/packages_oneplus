package com.oneplus.settings.product;

import android.content.Context;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.view.View;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.oneplus.settings.utils.OPUtils;
import java.util.Arrays;
import java.util.List;

public class OPProductInfoSettings extends SettingsPreferenceFragment implements Indexable {
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            SearchIndexableResource sir = new SearchIndexableResource(context);
            if (!OPUtils.isO2()) {
                sir.xmlResId = R.xml.op_product_info;
            }
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }
    };
    private final String ONEPLUS_PRODUCT_INFO_INTRODUCE = "oneplus_product_info_introduce";
    private final String PKG_PRODUCT_INFO_INTRODUCE_ = "com.oneplus.noviceteaching";

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        addPreferencesFromResource(R.xml.op_product_info);
    }

    public int getMetricsCategory() {
        return OPUtils.ONEPLUS_METRICSLOGGER;
    }
}
