package com.airbnb.lottie;

import android.os.Build.VERSION;
import android.support.v4.util.Pair;
import android.util.ArraySet;
import android.util.Log;
import com.airbnb.lottie.utils.MeanCalculator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class PerformanceTracker {
    private boolean enabled = false;
    private final Comparator<Pair<String, Float>> floatComparator = new Comparator<Pair<String, Float>>() {
        public int compare(Pair<String, Float> o1, Pair<String, Float> o2) {
            float r1 = ((Float) o1.second).floatValue();
            float r2 = ((Float) o2.second).floatValue();
            if (r2 > r1) {
                return 1;
            }
            if (r1 > r2) {
                return -1;
            }
            return 0;
        }
    };
    private final Set<FrameListener> frameListeners = initSet();
    private Map<String, MeanCalculator> layerRenderTimes = new HashMap();

    public interface FrameListener {
        void onFrameRendered(float f);
    }

    private Set<FrameListener> initSet() {
        if (VERSION.SDK_INT >= 23) {
            return new ArraySet();
        }
        return new HashSet();
    }

    /* Access modifiers changed, original: 0000 */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void recordRenderTime(String layerName, float millis) {
        if (this.enabled) {
            MeanCalculator meanCalculator = (MeanCalculator) this.layerRenderTimes.get(layerName);
            if (meanCalculator == null) {
                meanCalculator = new MeanCalculator();
                this.layerRenderTimes.put(layerName, meanCalculator);
            }
            meanCalculator.add(millis);
            if (layerName.equals("root")) {
                for (FrameListener listener : this.frameListeners) {
                    listener.onFrameRendered(millis);
                }
            }
        }
    }

    public void addFrameListener(FrameListener frameListener) {
        this.frameListeners.add(frameListener);
    }

    public void removeFrameListener(FrameListener frameListener) {
        this.frameListeners.add(frameListener);
    }

    public void clearRenderTimes() {
        this.layerRenderTimes.clear();
    }

    public void logRenderTimes() {
        if (this.enabled) {
            List<Pair<String, Float>> sortedRenderTimes = getSortedRenderTimes();
            Log.d(L.TAG, "Render times:");
            for (int i = 0; i < sortedRenderTimes.size(); i++) {
                Pair<String, Float> layer = (Pair) sortedRenderTimes.get(i);
                Log.d(L.TAG, String.format("\t\t%30s:%.2f", new Object[]{layer.first, layer.second}));
            }
        }
    }

    public List<Pair<String, Float>> getSortedRenderTimes() {
        if (!this.enabled) {
            return Collections.emptyList();
        }
        List<Pair<String, Float>> sortedRenderTimes = new ArrayList(this.layerRenderTimes.size());
        for (Entry<String, MeanCalculator> e : this.layerRenderTimes.entrySet()) {
            sortedRenderTimes.add(new Pair(e.getKey(), Float.valueOf(((MeanCalculator) e.getValue()).getMean())));
        }
        Collections.sort(sortedRenderTimes, this.floatComparator);
        return sortedRenderTimes;
    }
}
