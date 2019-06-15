package com.oneplus.settings.aboutphone;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Toast;
import com.android.settings.R;
import com.android.settings.applications.LayoutPreference;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.dashboard.SummaryLoader.SummaryProviderFactory;
import com.android.settings.deviceinfo.DeviceModelPreferenceController;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.oneplus.settings.aboutphone.SoftwareInfoAdapter.OnItemClickListener;
import com.oneplus.settings.utils.OPUtils;
import java.util.Arrays;
import java.util.List;

public class OPAboutPhone extends DashboardFragment implements Indexable, View {
    private static final String KEY_HARDWARE_VIEW = "hardware_view";
    private static final String KEY_SOFT_VIEW_1 = "soft_view";
    static final int REQUEST_CONFIRM_PASSWORD_FOR_DEV_PREF = 100;
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = R.xml.op_about_phone;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }
    };
    public static final SummaryProviderFactory SUMMARY_PROVIDER_FACTORY = -$$Lambda$OPAboutPhone$RKK-fwpKlUC1qzXgjTLs813iYKo.INSTANCE;
    private static final String TAG = "OPAboutPhone";
    private SoftwareInfoAdapter mAdapter;
    private Context mContext;
    private View mCurrentClickView;
    private Toast mDevHitToast;
    private AboutPhonePresenter mPresenter;
    private RecyclerView mRecyclerView;

    private static class SummaryProvider implements com.android.settings.dashboard.SummaryLoader.SummaryProvider {
        private final SummaryLoader mSummaryLoader;

        public SummaryProvider(SummaryLoader summaryLoader) {
            this.mSummaryLoader = summaryLoader;
        }

        public void setListening(boolean listening) {
            if (listening) {
                this.mSummaryLoader.setSummary(this, DeviceModelPreferenceController.getDeviceModel());
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.op_about_phone;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mContext = getActivity();
        initView();
        this.mPresenter = new AboutPhonePresenter(getActivity(), this, this);
    }

    public void displayHardWarePreference(int phoneImageResId, String cameraInfo, String cpuName, String screenInfo, String totalMemory) {
        OPAboutPhoneHardWareController mOPAboutPhoneHardWareController = OPAboutPhoneHardWareController.newInstance(getActivity(), this, ((LayoutPreference) getPreferenceScreen().findPreference(KEY_HARDWARE_VIEW)).findViewById(R.id.phone_hardware_info));
        mOPAboutPhoneHardWareController.setPhoneImage(this.mContext.getDrawable(phoneImageResId)).done();
        mOPAboutPhoneHardWareController.setCameraMessage(cameraInfo);
        mOPAboutPhoneHardWareController.setCpuMessage(cpuName);
        mOPAboutPhoneHardWareController.setScreenMessage(screenInfo);
        int ramsize = (int) Math.ceil((double) Float.valueOf(totalMemory).floatValue());
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(ramsize);
        stringBuilder.append("GB RAM + ");
        stringBuilder.append(OPUtils.showROMStorage(getActivity()));
        stringBuilder.append(" ROM");
        mOPAboutPhoneHardWareController.setStorageMessage(stringBuilder.toString());
        mOPAboutPhoneHardWareController.done();
    }

    public void displaySoftWarePreference(List<SoftwareInfoEntity> list) {
        this.mAdapter = new SoftwareInfoAdapter(this.mContext, list);
        this.mRecyclerView.setAdapter(this.mAdapter);
        this.mAdapter.notifyDataSetChanged();
        this.mAdapter.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(View view, int position) {
                OPAboutPhone.this.mCurrentClickView = view;
                OPAboutPhone.this.mPresenter.onItemClick(position);
            }
        });
    }

    private void initView() {
        this.mRecyclerView = (RecyclerView) ((LayoutPreference) getPreferenceScreen().findPreference(KEY_SOFT_VIEW_1)).findViewById(R.id.phone_software_info).findViewById(R.id.recycler_view_software_info);
        GridLayoutManager manager = new GridLayoutManager(this.mContext, 2);
        manager.setOrientation(1);
        this.mRecyclerView.setLayoutManager(manager);
        this.mRecyclerView.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                OPAboutPhone.this.mRecyclerView.getParent().requestDisallowInterceptTouchEvent(false);
                return false;
            }
        });
    }

    public void performHapticFeedback() {
        if (this.mCurrentClickView != null) {
            this.mCurrentClickView.performHapticFeedback(1);
        }
    }

    public void showLongToast(int resId) {
        showLongToast(this.mContext.getString(resId));
    }

    public void showLongToast(String text) {
        this.mDevHitToast = Toast.makeText(getActivity(), text, 1);
        this.mDevHitToast.show();
    }

    public void cancelToast() {
        if (this.mDevHitToast != null) {
            this.mDevHitToast.cancel();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100) {
            if (resultCode == -1) {
                this.mPresenter.enableDevelopmentSettings();
            }
            this.mPresenter.mProcessingLastDevHit = false;
        }
    }

    public void onResume() {
        super.onResume();
        this.mPresenter.onResume();
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    public int getMetricsCategory() {
        return OPUtils.ONEPLUS_METRICSLOGGER;
    }
}
