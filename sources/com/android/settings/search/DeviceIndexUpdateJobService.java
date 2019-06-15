package com.android.settings.search;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.net.Uri;
import android.net.Uri.Builder;
import android.util.Log;
import androidx.slice.Slice;
import androidx.slice.SliceItem;
import androidx.slice.SliceMetadata;
import androidx.slice.SliceViewManager;
import androidx.slice.SliceViewManager.SliceCallback;
import androidx.slice.core.SliceQuery;
import androidx.slice.widget.ListContent;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.slices.SettingsSliceProvider;
import com.android.settings.slices.SliceDeepLinkSpringBoard;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;

public class DeviceIndexUpdateJobService extends JobService {
    private static final boolean DEBUG = false;
    private static final String TAG = "DeviceIndexUpdate";
    @VisibleForTesting
    protected boolean mRunningJob;

    public boolean onStartJob(JobParameters params) {
        if (!this.mRunningJob) {
            this.mRunningJob = true;
            Thread thread = new Thread(new -$$Lambda$DeviceIndexUpdateJobService$CyjXGsZVpAu5iTckScg1Ee8_bGU(this, params));
            thread.setPriority(1);
            thread.start();
        }
        return true;
    }

    public boolean onStopJob(JobParameters params) {
        if (!this.mRunningJob) {
            return false;
        }
        this.mRunningJob = false;
        return true;
    }

    /* Access modifiers changed, original: protected */
    @VisibleForTesting
    public void updateIndex(JobParameters params) {
        DeviceIndexFeatureProvider indexProvider = FeatureFactory.getFactory(this).getDeviceIndexFeatureProvider();
        SliceViewManager manager = getSliceViewManager();
        Uri baseUri = new Builder().scheme("content").authority(SettingsSliceProvider.SLICE_AUTHORITY).build();
        Uri platformBaseUri = new Builder().scheme("content").authority("android.settings.slices").build();
        Collection<Uri> slices = manager.getSliceDescendants(baseUri);
        slices.addAll(manager.getSliceDescendants(platformBaseUri));
        indexProvider.clearIndex(this);
        for (Uri slice : slices) {
            if (this.mRunningJob) {
                Slice loadedSlice = bindSliceSynchronous(manager, slice);
                SliceMetadata metaData = getMetadata(loadedSlice);
                CharSequence title = findTitle(loadedSlice, metaData);
                if (title != null) {
                    indexProvider.index(this, title, slice, DeviceIndexFeatureProvider.createDeepLink(new Intent(SliceDeepLinkSpringBoard.ACTION_VIEW_SLICE).setPackage(getPackageName()).putExtra("slice", slice.toString()).toUri(2)), metaData.getSliceKeywords());
                }
            } else {
                return;
            }
        }
        jobFinished(params, false);
    }

    /* Access modifiers changed, original: protected */
    public SliceViewManager getSliceViewManager() {
        return SliceViewManager.getInstance(this);
    }

    /* Access modifiers changed, original: protected */
    public SliceMetadata getMetadata(Slice loadedSlice) {
        return SliceMetadata.from(this, loadedSlice);
    }

    /* Access modifiers changed, original: protected */
    public CharSequence findTitle(Slice loadedSlice, SliceMetadata metaData) {
        ListContent content = new ListContent(null, loadedSlice);
        SliceItem headerItem = content.getHeaderItem();
        if (headerItem == null) {
            if (content.getRowItems().size() == 0) {
                return null;
            }
            headerItem = (SliceItem) content.getRowItems().get(0);
        }
        SliceItem title = SliceQuery.find(headerItem, "text", "title", null);
        if (title != null) {
            return title.getText();
        }
        title = SliceQuery.find(headerItem, "text", "large", null);
        if (title != null) {
            return title.getText();
        }
        title = SliceQuery.find(headerItem, "text");
        if (title != null) {
            return title.getText();
        }
        return null;
    }

    /* Access modifiers changed, original: protected */
    public Slice bindSliceSynchronous(SliceViewManager manager, Uri slice) {
        Slice[] returnSlice = new Slice[1];
        CountDownLatch latch = new CountDownLatch(1);
        final Slice[] sliceArr = returnSlice;
        final CountDownLatch countDownLatch = latch;
        final SliceViewManager sliceViewManager = manager;
        final Uri uri = slice;
        AnonymousClass1 callback = new SliceCallback() {
            public void onSliceUpdated(Slice s) {
                try {
                    if (SliceMetadata.from(DeviceIndexUpdateJobService.this, s).getLoadingState() == 2) {
                        sliceArr[0] = s;
                        countDownLatch.countDown();
                        sliceViewManager.unregisterSliceCallback(uri, this);
                    }
                } catch (Exception e) {
                    String str = DeviceIndexUpdateJobService.TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(uri);
                    stringBuilder.append(" cannot be indexed");
                    Log.w(str, stringBuilder.toString(), e);
                    sliceArr[0] = s;
                }
            }
        };
        manager.registerSliceCallback(slice, callback);
        callback.onSliceUpdated(manager.bindSlice(slice));
        try {
            latch.await();
        } catch (InterruptedException e) {
        }
        return returnSlice[0];
    }
}
