package com.oneplus.settings.defaultapp.apptype;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import java.util.ArrayList;
import java.util.List;

public class DefaultAppTypeEmail extends DefaultAppTypeInfo {
    public List<Intent> getAppIntent() {
        List<Intent> intentList = new ArrayList();
        Intent intent = new Intent("android.intent.action.SEND");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setDataAndType(Uri.parse("mailto://"), null);
        intentList.add(intent);
        intent = new Intent("android.intent.action.SENDTO");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setDataAndType(Uri.parse("mailto://"), null);
        intentList.add(intent);
        intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addCategory("android.intent.category.BROWSABLE");
        intent.setDataAndType(Uri.parse("mailto://"), null);
        intentList.add(intent);
        return intentList;
    }

    public List<IntentFilter> getAppFilter() {
        List<IntentFilter> filterList = new ArrayList();
        IntentFilter filter = new IntentFilter("android.intent.action.SEND");
        filter.addCategory("android.intent.category.DEFAULT");
        filter.addDataScheme("mailto");
        filterList.add(filter);
        filter.addAction("android.intent.action.SENDTO");
        filter.addCategory("android.intent.category.DEFAULT");
        filter.addDataScheme("mailto");
        filterList.add(filter);
        filter.addAction("android.intent.action.VIEW");
        filter.addCategory("android.intent.category.DEFAULT");
        filter.addCategory("android.intent.category.BROWSABLE");
        filter.addDataScheme("mailto");
        filterList.add(filter);
        return filterList;
    }

    public List<Integer> getAppMatchParam() {
        List<Integer> matchList = new ArrayList();
        matchList.add(Integer.valueOf(6291456));
        matchList.add(Integer.valueOf(2097152));
        matchList.add(Integer.valueOf(2097152));
        return matchList;
    }
}
