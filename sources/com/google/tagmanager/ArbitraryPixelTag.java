package com.google.tagmanager;

import android.content.Context;
import android.net.Uri;
import android.net.Uri.Builder;
import com.google.analytics.containertag.common.FunctionType;
import com.google.analytics.containertag.common.Key;
import com.google.analytics.midtier.proto.containertag.TypeSystem.Value;
import com.google.android.gms.common.util.VisibleForTesting;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

class ArbitraryPixelTag extends TrackingTag {
    private static final String ADDITIONAL_PARAMS = Key.ADDITIONAL_PARAMS.toString();
    static final String ARBITRARY_PIXEL_UNREPEATABLE;
    private static final String ID = FunctionType.ARBITRARY_PIXEL.toString();
    private static final String UNREPEATABLE = Key.UNREPEATABLE.toString();
    private static final String URL = Key.URL.toString();
    private static final Set<String> unrepeatableIds = new HashSet();
    private final Context mContext;
    private final HitSenderProvider mHitSenderProvider;

    public interface HitSenderProvider {
        HitSender get();
    }

    static {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("gtm_");
        stringBuilder.append(ID);
        stringBuilder.append("_unrepeatable");
        ARBITRARY_PIXEL_UNREPEATABLE = stringBuilder.toString();
    }

    public static String getFunctionId() {
        return ID;
    }

    public ArbitraryPixelTag(final Context context) {
        this(context, new HitSenderProvider() {
            public HitSender get() {
                return DelayedHitSender.getInstance(context);
            }
        });
    }

    @VisibleForTesting
    ArbitraryPixelTag(Context context, HitSenderProvider hitSenderProvider) {
        super(ID, URL);
        this.mHitSenderProvider = hitSenderProvider;
        this.mContext = context;
    }

    public void evaluateTrackingTag(Map<String, Value> tag) {
        String unrepeatableId = tag.get(UNREPEATABLE) != null ? Types.valueToString((Value) tag.get(UNREPEATABLE)) : null;
        if (unrepeatableId == null || !idProcessed(unrepeatableId)) {
            StringBuilder stringBuilder;
            Builder uriBuilder = Uri.parse(Types.valueToString((Value) tag.get(URL))).buildUpon();
            Value additionalParamsList = (Value) tag.get(ADDITIONAL_PARAMS);
            if (additionalParamsList != null) {
                List<Object> l = Types.valueToObject(additionalParamsList);
                if (l instanceof List) {
                    for (Map<Object, Object> m : l) {
                        if (m instanceof Map) {
                            for (Entry<Object, Object> entry : m.entrySet()) {
                                uriBuilder.appendQueryParameter(entry.getKey().toString(), entry.getValue().toString());
                            }
                        } else {
                            StringBuilder stringBuilder2 = new StringBuilder();
                            stringBuilder2.append("ArbitraryPixel: additional params contains non-map: not sending partial hit: ");
                            stringBuilder2.append(uriBuilder.build().toString());
                            Log.e(stringBuilder2.toString());
                            return;
                        }
                    }
                }
                stringBuilder = new StringBuilder();
                stringBuilder.append("ArbitraryPixel: additional params not a list: not sending partial hit: ");
                stringBuilder.append(uriBuilder.build().toString());
                Log.e(stringBuilder.toString());
                return;
            }
            String uri = uriBuilder.build().toString();
            this.mHitSenderProvider.get().sendHit(uri);
            stringBuilder = new StringBuilder();
            stringBuilder.append("ArbitraryPixel: url = ");
            stringBuilder.append(uri);
            Log.v(stringBuilder.toString());
            if (unrepeatableId != null) {
                synchronized (ArbitraryPixelTag.class) {
                    unrepeatableIds.add(unrepeatableId);
                    SharedPreferencesUtil.saveAsync(this.mContext, ARBITRARY_PIXEL_UNREPEATABLE, unrepeatableId, "true");
                }
            }
        }
    }

    private synchronized boolean idProcessed(String unrepeatableId) {
        if (idInCache(unrepeatableId)) {
            return true;
        }
        if (!idInSharedPreferences(unrepeatableId)) {
            return false;
        }
        unrepeatableIds.add(unrepeatableId);
        return true;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public boolean idInSharedPreferences(String unrepeatableId) {
        return this.mContext.getSharedPreferences(ARBITRARY_PIXEL_UNREPEATABLE, 0).contains(unrepeatableId);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void clearCache() {
        unrepeatableIds.clear();
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public boolean idInCache(String unrepeatableId) {
        return unrepeatableIds.contains(unrepeatableId);
    }
}
