package com.android.settings.development;

import android.content.Context;
import android.content.res.Resources;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManager.DisplayListener;
import android.os.Handler;
import android.os.Looper;
import android.support.v14.preference.SwitchPreference;
import android.util.AttributeSet;
import android.view.Display;
import java.util.ArrayList;
import java.util.List;

public class ColorModePreference extends SwitchPreference implements DisplayListener {
    private int mCurrentIndex;
    private List<ColorModeDescription> mDescriptions;
    private Display mDisplay;
    private DisplayManager mDisplayManager = ((DisplayManager) getContext().getSystemService(DisplayManager.class));

    private static class ColorModeDescription {
        private int colorMode;
        private String summary;
        private String title;

        private ColorModeDescription() {
        }
    }

    public static List<ColorModeDescription> getColorModeDescriptions(Context context) {
        List<ColorModeDescription> colorModeDescriptions = new ArrayList();
        Resources resources = context.getResources();
        int[] colorModes = resources.getIntArray(2130903101);
        String[] titles = resources.getStringArray(2130903102);
        String[] descriptions = resources.getStringArray(2130903100);
        int i = 0;
        while (i < colorModes.length) {
            if (!(colorModes[i] == -1 || i == 1)) {
                ColorModeDescription desc = new ColorModeDescription();
                desc.colorMode = colorModes[i];
                desc.title = titles[i];
                desc.summary = descriptions[i];
                colorModeDescriptions.add(desc);
            }
            i++;
        }
        return colorModeDescriptions;
    }

    public ColorModePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public int getColorModeCount() {
        return this.mDescriptions.size();
    }

    public void startListening() {
        this.mDisplayManager.registerDisplayListener(this, new Handler(Looper.getMainLooper()));
    }

    public void stopListening() {
        this.mDisplayManager.unregisterDisplayListener(this);
    }

    public void onDisplayAdded(int displayId) {
        if (displayId == 0) {
            updateCurrentAndSupported();
        }
    }

    public void onDisplayChanged(int displayId) {
        if (displayId == 0) {
            updateCurrentAndSupported();
        }
    }

    public void onDisplayRemoved(int displayId) {
    }

    public void updateCurrentAndSupported() {
        boolean z = false;
        this.mDisplay = this.mDisplayManager.getDisplay(0);
        this.mDescriptions = getColorModeDescriptions(getContext());
        int currentColorMode = this.mDisplay.getColorMode();
        this.mCurrentIndex = -1;
        for (int i = 0; i < this.mDescriptions.size(); i++) {
            if (((ColorModeDescription) this.mDescriptions.get(i)).colorMode == currentColorMode) {
                this.mCurrentIndex = i;
                break;
            }
        }
        if (this.mCurrentIndex == 1) {
            z = true;
        }
        setChecked(z);
    }

    /* Access modifiers changed, original: protected */
    public boolean persistBoolean(boolean value) {
        if (this.mDescriptions.size() == 2) {
            ColorModeDescription desc = (ColorModeDescription) this.mDescriptions.get(value);
            this.mDisplay.requestColorMode(desc.colorMode);
            this.mCurrentIndex = this.mDescriptions.indexOf(desc);
        }
        return true;
    }
}
