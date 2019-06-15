package com.oneplus.settings.defaultapp.apptype;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import java.util.ArrayList;
import java.util.List;

public class DefaultAppTypeMusic extends DefaultAppTypeInfo {
    public List<Intent> getAppIntent() {
        List<Intent> intentList = new ArrayList();
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setDataAndType(Uri.parse("content://"), "audio/mpeg");
        intentList.add(intent);
        intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setDataAndType(Uri.parse("file://"), "audio/mpeg");
        intentList.add(intent);
        intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setDataAndType(Uri.parse("content://"), "audio/aac");
        intentList.add(intent);
        intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setDataAndType(Uri.parse("file://"), "audio/aac");
        intentList.add(intent);
        intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setDataAndType(Uri.parse("content://"), "audio/x-wav");
        intentList.add(intent);
        intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setDataAndType(Uri.parse("file://"), "audio/x-wav");
        intentList.add(intent);
        return intentList;
    }

    public List<IntentFilter> getAppFilter() {
        List<IntentFilter> filterList = new ArrayList();
        IntentFilter filter = new IntentFilter("android.intent.action.VIEW");
        filter.addCategory("android.intent.category.DEFAULT");
        filter.addDataScheme("content");
        try {
            filter.addDataType("audio/mpeg");
        } catch (Exception e) {
            e.printStackTrace();
        }
        filterList.add(filter);
        filter = new IntentFilter("android.intent.action.VIEW");
        filter.addCategory("android.intent.category.DEFAULT");
        filter.addDataScheme("file");
        try {
            filter.addDataType("audio/mpeg");
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        filterList.add(filter);
        filter = new IntentFilter("android.intent.action.VIEW");
        filter.addCategory("android.intent.category.DEFAULT");
        filter.addDataScheme("content");
        try {
            filter.addDataType("audio/aac");
        } catch (Exception e22) {
            e22.printStackTrace();
        }
        filterList.add(filter);
        filter = new IntentFilter("android.intent.action.VIEW");
        filter.addCategory("android.intent.category.DEFAULT");
        filter.addDataScheme("file");
        try {
            filter.addDataType("audio/aac");
        } catch (Exception e222) {
            e222.printStackTrace();
        }
        filterList.add(filter);
        filter = new IntentFilter("android.intent.action.VIEW");
        filter.addCategory("android.intent.category.DEFAULT");
        filter.addDataScheme("content");
        try {
            filter.addDataType("audio/x-wav");
        } catch (Exception e2222) {
            e2222.printStackTrace();
        }
        filterList.add(filter);
        filter = new IntentFilter("android.intent.action.VIEW");
        filter.addCategory("android.intent.category.DEFAULT");
        filter.addDataScheme("file");
        try {
            filter.addDataType("audio/x-wav");
        } catch (Exception e22222) {
            e22222.printStackTrace();
        }
        filterList.add(filter);
        return filterList;
    }

    public List<Integer> getAppMatchParam() {
        List<Integer> matchList = new ArrayList();
        matchList.add(Integer.valueOf(6291456));
        matchList.add(Integer.valueOf(6291456));
        matchList.add(Integer.valueOf(6291456));
        matchList.add(Integer.valueOf(6291456));
        matchList.add(Integer.valueOf(6291456));
        matchList.add(Integer.valueOf(6291456));
        matchList.add(Integer.valueOf(6291456));
        matchList.add(Integer.valueOf(6291456));
        return matchList;
    }
}
