package com.oneplus.settings.defaultapp.apptype;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import java.util.ArrayList;
import java.util.List;

public class DefaultAppTypeGallery extends DefaultAppTypeInfo {
    public List<Intent> getAppIntent() {
        List<Intent> intentList = new ArrayList();
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setDataAndType(Uri.parse("content://"), "image/*");
        intentList.add(intent);
        intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setDataAndType(Uri.parse("file://"), "image/*");
        intentList.add(intent);
        return intentList;
    }

    public List<IntentFilter> getAppFilter() {
        List<IntentFilter> filterList = new ArrayList();
        IntentFilter filter = new IntentFilter("android.intent.action.VIEW");
        filter.addCategory("android.intent.category.DEFAULT");
        filter.addDataScheme("content");
        try {
            filter.addDataType("image/*");
        } catch (Exception e) {
            e.printStackTrace();
        }
        filterList.add(filter);
        filter = new IntentFilter("android.intent.action.VIEW");
        filter.addCategory("android.intent.category.DEFAULT");
        filter.addDataScheme("file");
        try {
            filter.addDataType("image/*");
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        filterList.add(filter);
        return filterList;
    }

    public List<Integer> getAppMatchParam() {
        List<Integer> matchList = new ArrayList();
        matchList.add(Integer.valueOf(6291456));
        matchList.add(Integer.valueOf(6291456));
        return matchList;
    }
}
