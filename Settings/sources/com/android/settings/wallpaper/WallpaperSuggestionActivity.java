package com.android.settings.wallpaper;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import com.android.settings.R;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.display.WallpaperPreferenceController;

public class WallpaperSuggestionActivity extends Activity {
    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PackageManager pm = getPackageManager();
        Intent intent = new Intent().setClassName(getString(R.string.config_wallpaper_picker_package), getString(R.string.config_wallpaper_picker_class)).addFlags(33554432);
        if (pm.resolveActivity(intent, 0) != null) {
            startActivity(intent);
        } else {
            startFallbackSuggestion();
        }
        finish();
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void startFallbackSuggestion() {
        new SubSettingLauncher(this).setDestination(WallpaperTypeSettings.class.getName()).setTitle((int) R.string.wallpaper_suggestion_title).setSourceMetricsCategory(35).addFlags(33554432).launch();
    }

    @VisibleForTesting
    public static boolean isSuggestionComplete(Context context) {
        boolean z = true;
        if (!isWallpaperServiceEnabled(context)) {
            return true;
        }
        if (((WallpaperManager) context.getSystemService(WallpaperPreferenceController.KEY_WALLPAPER)).getWallpaperId(1) <= 0) {
            z = false;
        }
        return z;
    }

    private static boolean isWallpaperServiceEnabled(Context context) {
        return context.getResources().getBoolean(17956972);
    }
}
