package com.google.analytics.tracking.android;

import android.text.TextUtils;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

class HitBuilder {
    HitBuilder() {
    }

    static Map<String, String> generateHitParams(Map<String, String> hit) {
        Map<String, String> params = new HashMap();
        for (Entry<String, String> entry : hit.entrySet()) {
            if (((String) entry.getKey()).startsWith("&")) {
                if (entry.getValue() != null) {
                    String urlParam = ((String) entry.getKey()).substring(1);
                    if (!TextUtils.isEmpty(urlParam)) {
                        params.put(urlParam, entry.getValue());
                    }
                }
            }
        }
        return params;
    }

    static String postProcessHit(Hit hit, long currentTimeMillis) {
        StringBuilder builder = new StringBuilder();
        builder.append(hit.getHitParams());
        if (hit.getHitTime() > 0) {
            long queueTime = currentTimeMillis - hit.getHitTime();
            if (queueTime >= 0) {
                builder.append("&qt");
                builder.append("=");
                builder.append(queueTime);
            }
        }
        builder.append("&z");
        builder.append("=");
        builder.append(hit.getHitId());
        return builder.toString();
    }

    static String encode(String input) {
        try {
            return URLEncoder.encode(input, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("URL encoding failed for: ");
            stringBuilder.append(input);
            throw new AssertionError(stringBuilder.toString());
        }
    }
}
