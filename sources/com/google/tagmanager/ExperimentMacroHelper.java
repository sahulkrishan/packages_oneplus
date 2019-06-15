package com.google.tagmanager;

import com.google.analytics.containertag.proto.Serving.GaExperimentRandom;
import com.google.analytics.containertag.proto.Serving.GaExperimentSupplemental;
import com.google.analytics.containertag.proto.Serving.Supplemental;
import com.google.analytics.midtier.proto.containertag.TypeSystem.Value;
import java.util.Map;

public class ExperimentMacroHelper {
    public static void handleExperimentSupplemental(DataLayer dataLayer, Supplemental supplemental) {
        if (supplemental.experimentSupplemental == null) {
            Log.w("supplemental missing experimentSupplemental");
            return;
        }
        clearKeys(dataLayer, supplemental.experimentSupplemental);
        pushValues(dataLayer, supplemental.experimentSupplemental);
        setRandomValues(dataLayer, supplemental.experimentSupplemental);
    }

    private static void clearKeys(DataLayer dataLayer, GaExperimentSupplemental expSupplemental) {
        for (Value value : expSupplemental.valueToClear) {
            dataLayer.clearPersistentKeysWithPrefix(Types.valueToString(value));
        }
    }

    private static void pushValues(DataLayer dataLayer, GaExperimentSupplemental expSupplemental) {
        for (Value value : expSupplemental.valueToPush) {
            Map<Object, Object> map = valueToMap(value);
            if (map != null) {
                dataLayer.push(map);
            }
        }
    }

    private static void setRandomValues(DataLayer dataLayer, GaExperimentSupplemental expSupplemental) {
        DataLayer dataLayer2 = dataLayer;
        GaExperimentRandom[] arr$ = expSupplemental.experimentRandom;
        int len$ = arr$.length;
        int i$ = 0;
        while (i$ < len$) {
            GaExperimentRandom[] arr$2;
            int i$2;
            GaExperimentRandom expRandom = arr$[i$];
            if (expRandom.key == null) {
                Log.w("GaExperimentRandom: No key");
                arr$2 = arr$;
                i$2 = i$;
            } else {
                Object random = dataLayer2.get(expRandom.key);
                Long randomValue;
                if (random instanceof Number) {
                    randomValue = Long.valueOf(((Number) random).longValue());
                } else {
                    randomValue = null;
                }
                long minRandom = expRandom.minRandom;
                long maxRandom = expRandom.maxRandom;
                if (expRandom.retainOriginalValue && randomValue != null && randomValue.longValue() >= minRandom && randomValue.longValue() <= maxRandom) {
                    i$2 = i$;
                } else if (minRandom <= maxRandom) {
                    i$2 = i$;
                    random = Long.valueOf(Math.round((Math.random() * ((double) (maxRandom - minRandom))) + ((double) minRandom)));
                } else {
                    arr$2 = arr$;
                    i$2 = i$;
                    Log.w("GaExperimentRandom: random range invalid");
                }
                dataLayer2.clearPersistentKeysWithPrefix(expRandom.key);
                Map<Object, Object> map = dataLayer2.expandKeyValue(expRandom.key, random);
                if (expRandom.lifetimeInMilliseconds <= 0) {
                    arr$2 = arr$;
                } else if (map.containsKey("gtm")) {
                    arr$2 = arr$;
                    Object o = map.get("gtm");
                    if ((o instanceof Map) != null) {
                        ((Map) o).put("lifetime", Long.valueOf(expRandom.lifetimeInMilliseconds));
                    } else {
                        Log.w("GaExperimentRandom: gtm not a map");
                    }
                } else {
                    Object[] objArr = new Object[2];
                    objArr[0] = "lifetime";
                    arr$2 = arr$;
                    objArr[1] = Long.valueOf(expRandom.lifetimeInMilliseconds);
                    map.put("gtm", DataLayer.mapOf(objArr));
                }
                dataLayer2.push(map);
            }
            i$ = i$2 + 1;
            arr$ = arr$2;
            GaExperimentSupplemental gaExperimentSupplemental = expSupplemental;
        }
    }

    private static Map<Object, Object> valueToMap(Value value) {
        Map<Object, Object> valueAsObject = Types.valueToObject(value);
        if (valueAsObject instanceof Map) {
            return valueAsObject;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("value: ");
        stringBuilder.append(valueAsObject);
        stringBuilder.append(" is not a map value, ignored.");
        Log.w(stringBuilder.toString());
        return null;
    }
}
