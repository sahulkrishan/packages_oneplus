package com.android.settings;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Global;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import com.android.setupwizardlib.util.WizardManagerHelper;
import java.util.Objects;

public class FallbackHome extends Activity {
    private static final int PROGRESS_TIMEOUT = 2000;
    private static final String TAG = "FallbackHome";
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            FallbackHome.this.maybeFinish();
        }
    };
    private final Runnable mProgressTimeoutRunnable = new -$$Lambda$FallbackHome$t1fq3k7x_PY-DiX5Fz-YbaIlCdg(this);
    private boolean mProvisioned;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            FallbackHome.this.maybeFinish();
        }
    };

    public static /* synthetic */ void lambda$new$0(FallbackHome fallbackHome) {
        View v = fallbackHome.getLayoutInflater().inflate(R.layout.fallback_home_finishing_boot, null);
        fallbackHome.setContentView(v);
        v.setAlpha(0.0f);
        v.animate().alpha(1.0f).setDuration(500).setInterpolator(AnimationUtils.loadInterpolator(fallbackHome, AndroidResources.FAST_OUT_SLOW_IN)).start();
        fallbackHome.getWindow().addFlags(128);
    }

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean z = false;
        if (Global.getInt(getContentResolver(), WizardManagerHelper.SETTINGS_GLOBAL_DEVICE_PROVISIONED, 0) != 0) {
            z = true;
        }
        this.mProvisioned = z;
        if (this.mProvisioned) {
            getWindow().getDecorView().setSystemUiVisibility(1536);
        } else {
            setTheme(R.style.f175FallbackHome.SetupWizard);
            getWindow().getDecorView().setSystemUiVisibility(4102);
        }
        registerReceiver(this.mReceiver, new IntentFilter("android.intent.action.USER_UNLOCKED"));
        maybeFinish();
    }

    /* Access modifiers changed, original: protected */
    public void onResume() {
        super.onResume();
        if (this.mProvisioned) {
            this.mHandler.postDelayed(this.mProgressTimeoutRunnable, 2000);
        }
    }

    /* Access modifiers changed, original: protected */
    public void onPause() {
        super.onPause();
        this.mHandler.removeCallbacks(this.mProgressTimeoutRunnable);
    }

    /* Access modifiers changed, original: protected */
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(this.mReceiver);
        this.mHandler.removeCallbacksAndMessages(null);
    }

    private void maybeFinish() {
        if (isFinishing()) {
            Log.w(TAG, "maybeFinish, Activity isFinishing, return.");
            return;
        }
        if (((UserManager) getSystemService(UserManager.class)).isUserUnlocked()) {
            ResolveInfo homeInfo = getPackageManager().resolveActivity(new Intent("android.intent.action.MAIN").addCategory("android.intent.category.HOME"), 0);
            if (homeInfo == null || homeInfo.activityInfo == null || !Objects.equals(getPackageName(), homeInfo.activityInfo.packageName)) {
                Log.d(TAG, "User unlocked and real home found; let's go!");
                ((PowerManager) getSystemService(PowerManager.class)).userActivity(SystemClock.uptimeMillis(), false);
                finish();
            } else if (!UserManager.isSplitSystemUser() || UserHandle.myUserId() != 0) {
                Log.d(TAG, "User unlocked but no home; let's hope someone enables one soon?");
                this.mHandler.sendEmptyMessageDelayed(0, 500);
            }
        }
    }
}
