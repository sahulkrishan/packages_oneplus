package com.android.settings.dashboard;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import com.android.settings.SettingsActivity;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.drawer.DashboardCategory;
import com.android.settingslib.drawer.Tile;
import com.android.settingslib.utils.ThreadUtils;
import java.util.List;

public class SummaryLoader {
    private static final boolean DEBUG = false;
    public static final String SUMMARY_PROVIDER_FACTORY = "SUMMARY_PROVIDER_FACTORY";
    private static final String TAG = "SummaryLoader";
    private final Activity mActivity;
    private final String mCategoryKey;
    private final DashboardFeatureProvider mDashboardFeatureProvider;
    private boolean mListening;
    private ArraySet<BroadcastReceiver> mReceivers = new ArraySet();
    private SummaryConsumer mSummaryConsumer;
    private final ArrayMap<SummaryProvider, ComponentName> mSummaryProviderMap = new ArrayMap();
    private final ArrayMap<String, CharSequence> mSummaryTextMap = new ArrayMap();
    private final Worker mWorker;
    private boolean mWorkerListening;
    private final HandlerThread mWorkerThread;

    public interface SummaryConsumer {
        void notifySummaryChanged(Tile tile);
    }

    public interface SummaryProvider {
        void setListening(boolean z);
    }

    public interface SummaryProviderFactory {
        SummaryProvider createSummaryProvider(Activity activity, SummaryLoader summaryLoader);
    }

    private class Worker extends Handler {
        private static final int MSG_GET_CATEGORY_TILES_AND_SET_LISTENING = 1;
        private static final int MSG_GET_PROVIDER = 2;
        private static final int MSG_SET_LISTENING = 3;

        public Worker(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            boolean z = true;
            switch (msg.what) {
                case 1:
                    DashboardCategory category = SummaryLoader.this.mDashboardFeatureProvider.getTilesForCategory(SummaryLoader.this.mCategoryKey);
                    if (category != null && category.getTilesCount() != 0) {
                        for (Tile tile : category.getTiles()) {
                            SummaryLoader.this.makeProviderW(tile);
                        }
                        SummaryLoader.this.setListeningW(true);
                        break;
                    }
                    return;
                    break;
                case 2:
                    SummaryLoader.this.makeProviderW(msg.obj);
                    break;
                case 3:
                    if (msg.obj == null || !msg.obj.equals(Integer.valueOf(1))) {
                        z = false;
                    }
                    SummaryLoader.this.setListeningW(z);
                    break;
            }
        }
    }

    public SummaryLoader(Activity activity, String categoryKey) {
        this.mDashboardFeatureProvider = FeatureFactory.getFactory(activity).getDashboardFeatureProvider(activity);
        this.mCategoryKey = categoryKey;
        this.mWorkerThread = new HandlerThread(TAG, 10);
        this.mWorkerThread.start();
        this.mWorker = new Worker(this.mWorkerThread.getLooper());
        this.mActivity = activity;
    }

    public void release() {
        this.mWorkerThread.quitSafely();
        setListeningW(false);
    }

    public void setSummaryConsumer(SummaryConsumer summaryConsumer) {
        this.mSummaryConsumer = summaryConsumer;
    }

    public void setSummary(SummaryProvider provider, CharSequence summary) {
        ThreadUtils.postOnMainThread(new -$$Lambda$SummaryLoader$EirySW2ETuFFjqqH756jJXvHagg(this, (ComponentName) this.mSummaryProviderMap.get(provider), summary));
    }

    public static /* synthetic */ void lambda$setSummary$0(SummaryLoader summaryLoader, ComponentName component, CharSequence summary) {
        Tile tile = summaryLoader.getTileFromCategory(summaryLoader.mDashboardFeatureProvider.getTilesForCategory(summaryLoader.mCategoryKey), component);
        if (tile != null) {
            summaryLoader.updateSummaryIfNeeded(tile, summary);
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void updateSummaryIfNeeded(Tile tile, CharSequence summary) {
        if (!TextUtils.equals(tile.summary, summary)) {
            this.mSummaryTextMap.put(this.mDashboardFeatureProvider.getDashboardKeyForTile(tile), summary);
            tile.summary = summary;
            if (this.mSummaryConsumer != null) {
                this.mSummaryConsumer.notifySummaryChanged(tile);
            }
        }
    }

    public void setListening(boolean listening) {
        if (this.mListening != listening) {
            this.mListening = listening;
            for (int i = 0; i < this.mReceivers.size(); i++) {
                this.mActivity.unregisterReceiver((BroadcastReceiver) this.mReceivers.valueAt(i));
            }
            this.mReceivers.clear();
            this.mWorker.removeMessages(3);
            if (!listening) {
                this.mWorker.obtainMessage(3, Integer.valueOf(0)).sendToTarget();
            } else if (!this.mSummaryProviderMap.isEmpty()) {
                this.mWorker.obtainMessage(3, Integer.valueOf(1)).sendToTarget();
            } else if (!this.mWorker.hasMessages(1)) {
                this.mWorker.sendEmptyMessage(1);
            }
        }
    }

    private SummaryProvider getSummaryProvider(Tile tile) {
        if (!this.mActivity.getPackageName().equals(tile.intent.getComponent().getPackageName())) {
            return null;
        }
        Bundle metaData = getMetaData(tile);
        if (metaData == null) {
            return null;
        }
        String clsName = metaData.getString(SettingsActivity.META_DATA_KEY_FRAGMENT_CLASS);
        if (clsName == null) {
            return null;
        }
        try {
            return ((SummaryProviderFactory) Class.forName(clsName).getField(SUMMARY_PROVIDER_FACTORY).get(null)).createSummaryProvider(this.mActivity, this);
        } catch (ClassCastException | ClassNotFoundException | IllegalAccessException | NoSuchFieldException e) {
            return null;
        }
    }

    private Bundle getMetaData(Tile tile) {
        return tile.metaData;
    }

    public void registerReceiver(final BroadcastReceiver receiver, final IntentFilter filter) {
        this.mActivity.runOnUiThread(new Runnable() {
            public void run() {
                if (SummaryLoader.this.mListening) {
                    SummaryLoader.this.mReceivers.add(receiver);
                    SummaryLoader.this.mActivity.registerReceiver(receiver, filter);
                }
            }
        });
    }

    public void updateSummaryToCache(DashboardCategory category) {
        if (category != null) {
            for (Tile tile : category.getTiles()) {
                String key = this.mDashboardFeatureProvider.getDashboardKeyForTile(tile);
                if (this.mSummaryTextMap.containsKey(key)) {
                    tile.summary = (CharSequence) this.mSummaryTextMap.get(key);
                }
            }
        }
    }

    private synchronized void setListeningW(boolean listening) {
        if (this.mWorkerListening != listening) {
            this.mWorkerListening = listening;
            for (SummaryProvider p : this.mSummaryProviderMap.keySet()) {
                try {
                    p.setListening(listening);
                } catch (Exception e) {
                    Log.d(TAG, "Problem in setListening", e);
                }
            }
        }
    }

    private synchronized void makeProviderW(Tile tile) {
        SummaryProvider provider = getSummaryProvider(tile);
        if (provider != null) {
            this.mSummaryProviderMap.put(provider, tile.intent.getComponent());
        }
    }

    private Tile getTileFromCategory(DashboardCategory category, ComponentName component) {
        if (category == null || category.getTilesCount() == 0) {
            return null;
        }
        List<Tile> tiles = category.getTiles();
        int tileCount = tiles.size();
        for (int j = 0; j < tileCount; j++) {
            Tile tile = (Tile) tiles.get(j);
            if (component.equals(tile.intent.getComponent())) {
                return tile;
            }
        }
        return null;
    }
}
