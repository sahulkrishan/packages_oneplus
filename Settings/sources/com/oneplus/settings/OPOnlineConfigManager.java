package com.oneplus.settings;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import com.android.settings.R;
import com.android.settingslib.datetime.ZoneGetter;
import com.oneplus.config.ConfigGrabber;
import com.oneplus.config.ConfigObserver;
import com.oneplus.config.ConfigObserver.ConfigUpdater;
import com.oneplus.settings.utils.OPPrefUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class OPOnlineConfigManager {
    private static String TAG = OPOnlineConfigManager.class.getSimpleName();
    private static List<String> localMultiAppWhiteList;
    private static Object lock = new Object();
    private static final List<String> multiAppWhiteList = new ArrayList();
    private static OPOnlineConfigManager onlineConfigManager = null;
    private String CONFIG_NAME = "ROM_APP_OPSettings";
    final int MSG_GET_ONLINECONFIG = 1;
    private String OP_MULTIAPP_WHITE_LIST = "op_multiapp_white_list_p";
    private ConfigObserver mBackgroundConfigObserver;
    private Context mContext;
    private Handler mHandler;
    private HandlerThread mHandlerThread;

    class BackgroundConfigUpdater implements ConfigUpdater {
        BackgroundConfigUpdater() {
        }

        public void updateConfig(JSONArray configJsonArray) {
            OPOnlineConfigManager.this.parseConfigFromJson(configJsonArray);
        }
    }

    private OPOnlineConfigManager(Context context) {
        this.mContext = context;
        this.mHandlerThread = new HandlerThread(TAG);
        this.mHandlerThread.start();
        this.mHandler = new Handler(this.mHandlerThread.getLooper()) {
            public void handleMessage(Message msg) {
                Log.d(OPOnlineConfigManager.TAG, "settings handleMessage....");
                if (msg.what == 1) {
                    OPOnlineConfigManager.this.parseConfigFromJson(new ConfigGrabber(OPOnlineConfigManager.this.mContext, OPOnlineConfigManager.this.CONFIG_NAME).grabConfig());
                }
            }
        };
        localMultiAppWhiteList = Arrays.asList(context.getResources().getStringArray(R.array.op_multiapp_white_list));
    }

    public static List<String> getMultiAppWhiteList() {
        synchronized (multiAppWhiteList) {
            List list;
            if (multiAppWhiteList.isEmpty()) {
                list = localMultiAppWhiteList;
                return list;
            }
            list = multiAppWhiteList;
            return list;
        }
    }

    public static OPOnlineConfigManager getInstence(Context context) {
        synchronized (lock) {
            if (onlineConfigManager == null) {
                onlineConfigManager = new OPOnlineConfigManager(context);
            }
        }
        return onlineConfigManager;
    }

    public void init() {
        this.mBackgroundConfigObserver = new ConfigObserver(this.mContext, this.mHandler, new BackgroundConfigUpdater(), this.CONFIG_NAME);
        this.mBackgroundConfigObserver.register();
        this.mHandler.sendEmptyMessage(1);
    }

    private void parseConfigFromJson(JSONArray jsonArray) {
        String oldJson;
        String str;
        StringBuilder stringBuilder;
        if (jsonArray == null) {
            oldJson = OPPrefUtil.getString(this.OP_MULTIAPP_WHITE_LIST, null);
            if (Build.DEBUG_ONEPLUS) {
                str = TAG;
                stringBuilder = new StringBuilder();
                stringBuilder.append("settings parseConfigFromJson jsonArray is null,use old json:");
                stringBuilder.append(oldJson);
                Log.d(str, stringBuilder.toString());
            }
            if (oldJson != null) {
                try {
                    jsonArray = new JSONArray(oldJson);
                } catch (JSONException e) {
                    String str2 = TAG;
                    StringBuilder stringBuilder2 = new StringBuilder();
                    stringBuilder2.append("settings parseConfigFromJson JSONException:");
                    stringBuilder2.append(e.getMessage());
                    Log.e(str2, stringBuilder2.toString());
                    e.printStackTrace();
                }
            } else {
                Log.d(TAG, "settings parseConfigFromJson jsonArray is null, return");
                return;
            }
        }
        if (Build.DEBUG_ONEPLUS) {
            oldJson = TAG;
            StringBuilder stringBuilder3 = new StringBuilder();
            stringBuilder3.append("settings parseConfigFromJson jsonArray=");
            stringBuilder3.append(jsonArray.toString());
            Log.d(oldJson, stringBuilder3.toString());
        }
        int index = 0;
        while (index < jsonArray.length()) {
            try {
                JSONObject json = jsonArray.getJSONObject(index);
                if (json.getString(ZoneGetter.KEY_DISPLAYNAME).equals(this.OP_MULTIAPP_WHITE_LIST)) {
                    JSONArray whiteNumberListJsonArray = json.getJSONArray("value");
                    OPPrefUtil.putString(this.OP_MULTIAPP_WHITE_LIST, whiteNumberListJsonArray.toString());
                    synchronized (multiAppWhiteList) {
                        multiAppWhiteList.clear();
                        for (int i = 0; i < whiteNumberListJsonArray.length(); i++) {
                            multiAppWhiteList.add(whiteNumberListJsonArray.getString(i));
                        }
                    }
                }
                index++;
            } catch (JSONException e2) {
                str = TAG;
                stringBuilder = new StringBuilder();
                stringBuilder.append("settings parseConfigFromJson JSONException:");
                stringBuilder.append(e2.getMessage());
                Log.e(str, stringBuilder.toString());
            } catch (Exception e3) {
                str = TAG;
                stringBuilder = new StringBuilder();
                stringBuilder.append("settings parseConfigFromJson Exception:");
                stringBuilder.append(e3.getMessage());
                Log.e(str, stringBuilder.toString());
            }
        }
    }
}
