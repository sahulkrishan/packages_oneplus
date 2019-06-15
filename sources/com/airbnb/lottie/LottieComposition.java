package com.airbnb.lottie;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v4.util.LongSparseArray;
import android.support.v4.util.SparseArrayCompat;
import android.util.Log;
import com.airbnb.lottie.model.FileCompositionLoader;
import com.airbnb.lottie.model.Font;
import com.airbnb.lottie.model.FontCharacter;
import com.airbnb.lottie.model.JsonCompositionLoader;
import com.airbnb.lottie.model.layer.Layer;
import com.airbnb.lottie.model.layer.Layer.LayerType;
import com.airbnb.lottie.utils.Utils;
import com.android.settingslib.datetime.ZoneGetter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LottieComposition {
    private final Rect bounds;
    private final SparseArrayCompat<FontCharacter> characters;
    private final float dpScale;
    private final long endFrame;
    private final Map<String, Font> fonts;
    private final float frameRate;
    private final Map<String, LottieImageAsset> images;
    private final LongSparseArray<Layer> layerMap;
    private final List<Layer> layers;
    private final int majorVersion;
    private final int minorVersion;
    private final int patchVersion;
    private final PerformanceTracker performanceTracker;
    private final Map<String, List<Layer>> precomps;
    private final long startFrame;
    private final HashSet<String> warnings;

    public static class Factory {
        private Factory() {
        }

        public static Cancellable fromAssetFileName(Context context, String fileName, OnCompositionLoadedListener loadedListener) {
            try {
                return fromInputStream(context, context.getAssets().open(fileName), loadedListener);
            } catch (IOException e) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Unable to find file ");
                stringBuilder.append(fileName);
                throw new IllegalStateException(stringBuilder.toString(), e);
            }
        }

        public static Cancellable fromInputStream(Context context, InputStream stream, OnCompositionLoadedListener loadedListener) {
            FileCompositionLoader loader = new FileCompositionLoader(context.getResources(), loadedListener);
            loader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new InputStream[]{stream});
            return loader;
        }

        public static LottieComposition fromFileSync(Context context, String fileName) {
            try {
                return fromInputStream(context.getResources(), context.getAssets().open(fileName));
            } catch (IOException e) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Unable to find file ");
                stringBuilder.append(fileName);
                throw new IllegalStateException(stringBuilder.toString(), e);
            }
        }

        public static Cancellable fromJson(Resources res, JSONObject json, OnCompositionLoadedListener loadedListener) {
            JsonCompositionLoader loader = new JsonCompositionLoader(res, loadedListener);
            loader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new JSONObject[]{json});
            return loader;
        }

        @Nullable
        public static LottieComposition fromInputStream(Resources res, InputStream stream) {
            try {
                byte[] buffer = new byte[stream.available()];
                stream.read(buffer);
                LottieComposition fromJsonSync = fromJsonSync(res, new JSONObject(new String(buffer, "UTF-8")));
                Utils.closeQuietly(stream);
                return fromJsonSync;
            } catch (IOException e) {
                Log.e(L.TAG, "Failed to load composition.", new IllegalStateException("Unable to find file.", e));
            } catch (JSONException e2) {
                Log.e(L.TAG, "Failed to load composition.", new IllegalStateException("Unable to load JSON.", e2));
            } catch (Throwable th) {
                Utils.closeQuietly(stream);
            }
            Utils.closeQuietly(stream);
            return null;
        }

        public static LottieComposition fromJsonSync(Resources res, JSONObject json) {
            JSONObject jSONObject = json;
            Rect bounds = null;
            float scale = res.getDisplayMetrics().density;
            int width = jSONObject.optInt("w", -1);
            int height = jSONObject.optInt("h", -1);
            if (!(width == -1 || height == -1)) {
                bounds = new Rect(0, 0, (int) (((float) width) * scale), (int) (((float) height) * scale));
            }
            long startFrame = jSONObject.optLong("ip", 0);
            long endFrame = jSONObject.optLong("op", 0);
            float frameRate = (float) jSONObject.optDouble("fr", 0.0d);
            String version = jSONObject.optString("v");
            String[] versions = version.split("[.]");
            LottieComposition composition = new LottieComposition(bounds, startFrame, endFrame, frameRate, scale, Integer.parseInt(versions[0]), Integer.parseInt(versions[1]), Integer.parseInt(versions[2]));
            JSONArray assetsJson = jSONObject.optJSONArray("assets");
            parseImages(assetsJson, composition);
            parsePrecomps(assetsJson, composition);
            parseFonts(jSONObject.optJSONObject("fonts"), composition);
            parseChars(jSONObject.optJSONArray("chars"), composition);
            parseLayers(jSONObject, composition);
            return composition;
        }

        private static void parseLayers(JSONObject json, LottieComposition composition) {
            JSONArray jsonLayers = json.optJSONArray("layers");
            if (jsonLayers != null) {
                int length = jsonLayers.length();
                int imageCount = 0;
                for (int i = 0; i < length; i++) {
                    Layer layer = com.airbnb.lottie.model.layer.Layer.Factory.newInstance(jsonLayers.optJSONObject(i), composition);
                    if (layer.getLayerType() == LayerType.Image) {
                        imageCount++;
                    }
                    addLayer(composition.layers, composition.layerMap, layer);
                }
                if (imageCount > 4) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("You have ");
                    stringBuilder.append(imageCount);
                    stringBuilder.append(" images. Lottie should primarily be used with shapes. If you are using Adobe Illustrator, convert the Illustrator layers to shape layers.");
                    composition.addWarning(stringBuilder.toString());
                }
            }
        }

        private static void parsePrecomps(@Nullable JSONArray assetsJson, LottieComposition composition) {
            if (assetsJson != null) {
                int length = assetsJson.length();
                for (int i = 0; i < length; i++) {
                    JSONObject assetJson = assetsJson.optJSONObject(i);
                    JSONArray layersJson = assetJson.optJSONArray("layers");
                    if (layersJson != null) {
                        List<Layer> layers = new ArrayList(layersJson.length());
                        LongSparseArray<Layer> layerMap = new LongSparseArray();
                        for (int j = 0; j < layersJson.length(); j++) {
                            Layer layer = com.airbnb.lottie.model.layer.Layer.Factory.newInstance(layersJson.optJSONObject(j), composition);
                            layerMap.put(layer.getId(), layer);
                            layers.add(layer);
                        }
                        composition.precomps.put(assetJson.optString(ZoneGetter.KEY_ID), layers);
                    }
                }
            }
        }

        private static void parseImages(@Nullable JSONArray assetsJson, LottieComposition composition) {
            if (assetsJson != null) {
                int length = assetsJson.length();
                for (int i = 0; i < length; i++) {
                    JSONObject assetJson = assetsJson.optJSONObject(i);
                    if (assetJson.has("p")) {
                        LottieImageAsset image = Factory.newInstance(assetJson);
                        composition.images.put(image.getId(), image);
                    }
                }
            }
        }

        private static void parseFonts(@Nullable JSONObject fonts, LottieComposition composition) {
            if (fonts != null) {
                JSONArray fontsList = fonts.optJSONArray("list");
                if (fontsList != null) {
                    int length = fontsList.length();
                    for (int i = 0; i < length; i++) {
                        Font font = com.airbnb.lottie.model.Font.Factory.newInstance(fontsList.optJSONObject(i));
                        composition.fonts.put(font.getName(), font);
                    }
                }
            }
        }

        private static void parseChars(@Nullable JSONArray charsJson, LottieComposition composition) {
            if (charsJson != null) {
                int length = charsJson.length();
                for (int i = 0; i < length; i++) {
                    FontCharacter character = com.airbnb.lottie.model.FontCharacter.Factory.newInstance(charsJson.optJSONObject(i), composition);
                    composition.characters.put(character.hashCode(), character);
                }
            }
        }

        private static void addLayer(List<Layer> layers, LongSparseArray<Layer> layerMap, Layer layer) {
            layers.add(layer);
            layerMap.put(layer.getId(), layer);
        }
    }

    private LottieComposition(Rect bounds, long startFrame, long endFrame, float frameRate, float dpScale, int major, int minor, int patch) {
        this.precomps = new HashMap();
        this.images = new HashMap();
        this.fonts = new HashMap();
        this.characters = new SparseArrayCompat();
        this.layerMap = new LongSparseArray();
        this.layers = new ArrayList();
        this.warnings = new HashSet();
        this.performanceTracker = new PerformanceTracker();
        this.bounds = bounds;
        this.startFrame = startFrame;
        this.endFrame = endFrame;
        this.frameRate = frameRate;
        this.dpScale = dpScale;
        this.majorVersion = major;
        this.minorVersion = minor;
        this.patchVersion = patch;
        if (!Utils.isAtLeastVersion(this, 4, 5, 0)) {
            addWarning("Lottie only supports bodymovin >= 4.5.0");
        }
    }

    @RestrictTo({Scope.LIBRARY})
    public void addWarning(String warning) {
        Log.w(L.TAG, warning);
        this.warnings.add(warning);
    }

    public ArrayList<String> getWarnings() {
        return new ArrayList(Arrays.asList(this.warnings.toArray(new String[this.warnings.size()])));
    }

    public void setPerformanceTrackingEnabled(boolean enabled) {
        this.performanceTracker.setEnabled(enabled);
    }

    public PerformanceTracker getPerformanceTracker() {
        return this.performanceTracker;
    }

    @RestrictTo({Scope.LIBRARY})
    public Layer layerModelForId(long id) {
        return (Layer) this.layerMap.get(id);
    }

    public Rect getBounds() {
        return this.bounds;
    }

    public long getDuration() {
        return (long) ((((float) (this.endFrame - this.startFrame)) / this.frameRate) * 1000.0f);
    }

    @RestrictTo({Scope.LIBRARY})
    public int getMajorVersion() {
        return this.majorVersion;
    }

    @RestrictTo({Scope.LIBRARY})
    public int getMinorVersion() {
        return this.minorVersion;
    }

    @RestrictTo({Scope.LIBRARY})
    public int getPatchVersion() {
        return this.patchVersion;
    }

    @RestrictTo({Scope.LIBRARY})
    public long getStartFrame() {
        return this.startFrame;
    }

    @RestrictTo({Scope.LIBRARY})
    public long getEndFrame() {
        return this.endFrame;
    }

    public List<Layer> getLayers() {
        return this.layers;
    }

    @Nullable
    @RestrictTo({Scope.LIBRARY})
    public List<Layer> getPrecomps(String id) {
        return (List) this.precomps.get(id);
    }

    public SparseArrayCompat<FontCharacter> getCharacters() {
        return this.characters;
    }

    public Map<String, Font> getFonts() {
        return this.fonts;
    }

    public boolean hasImages() {
        return this.images.isEmpty() ^ 1;
    }

    /* Access modifiers changed, original: 0000 */
    public Map<String, LottieImageAsset> getImages() {
        return this.images;
    }

    public float getDurationFrames() {
        return (((float) getDuration()) * this.frameRate) / 1000.0f;
    }

    public float getDpScale() {
        return this.dpScale;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("LottieComposition:\n");
        for (Layer layer : this.layers) {
            sb.append(layer.toString("\t"));
        }
        return sb.toString();
    }
}
