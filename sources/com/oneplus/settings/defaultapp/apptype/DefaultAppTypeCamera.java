package com.oneplus.settings.defaultapp.apptype;

import android.content.Intent;
import android.content.IntentFilter;
import java.util.ArrayList;
import java.util.List;

public class DefaultAppTypeCamera extends DefaultAppTypeInfo {
    public List<Intent> getAppIntent() {
        List<Intent> intentList = new ArrayList();
        intentList.add(new Intent("android.media.action.IMAGE_CAPTURE"));
        intentList.add(new Intent("android.media.action.VIDEO_CAPTURE"));
        intentList.add(new Intent("android.media.action.VIDEO_CAMERA"));
        intentList.add(new Intent("com.oppo.action.CAMERA"));
        return intentList;
    }

    public List<IntentFilter> getAppFilter() {
        List<IntentFilter> filterList = new ArrayList();
        IntentFilter filter = new IntentFilter("android.media.action.IMAGE_CAPTURE");
        filter.addCategory("android.intent.category.DEFAULT");
        filterList.add(filter);
        filter = new IntentFilter("android.media.action.VIDEO_CAPTURE");
        filter.addCategory("android.intent.category.DEFAULT");
        filterList.add(filter);
        filter = new IntentFilter("android.media.action.VIDEO_CAMERA");
        filter.addCategory("android.intent.category.DEFAULT");
        filterList.add(filter);
        filter.addAction("com.oppo.action.CAMERA");
        filter.addCategory("android.intent.category.DEFAULT");
        filterList.add(filter);
        return filterList;
    }

    public List<Integer> getAppMatchParam() {
        List<Integer> matchList = new ArrayList();
        matchList.add(Integer.valueOf(1048576));
        matchList.add(Integer.valueOf(1048576));
        matchList.add(Integer.valueOf(1048576));
        matchList.add(Integer.valueOf(1048576));
        return matchList;
    }
}
