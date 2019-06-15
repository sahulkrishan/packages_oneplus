package com.google.tagmanager;

import com.google.analytics.midtier.proto.containertag.TypeSystem.Value;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

class Types {
    private static Boolean DEFAULT_BOOLEAN = new Boolean(false);
    private static Double DEFAULT_DOUBLE = new Double(0.0d);
    private static Long DEFAULT_INT64 = new Long(0);
    private static List<Object> DEFAULT_LIST = new ArrayList(0);
    private static Map<Object, Object> DEFAULT_MAP = new HashMap();
    private static TypedNumber DEFAULT_NUMBER = TypedNumber.numberWithInt64(0);
    private static final Object DEFAULT_OBJECT = null;
    private static String DEFAULT_STRING = new String("");
    private static Value DEFAULT_VALUE = objectToValue(DEFAULT_STRING);

    private Types() {
    }

    public static Object getDefaultObject() {
        return DEFAULT_OBJECT;
    }

    public static Long getDefaultInt64() {
        return DEFAULT_INT64;
    }

    public static Double getDefaultDouble() {
        return DEFAULT_DOUBLE;
    }

    public static Boolean getDefaultBoolean() {
        return DEFAULT_BOOLEAN;
    }

    public static TypedNumber getDefaultNumber() {
        return DEFAULT_NUMBER;
    }

    public static String getDefaultString() {
        return DEFAULT_STRING;
    }

    public static List<Object> getDefaultList() {
        return DEFAULT_LIST;
    }

    public static Map<Object, Object> getDefaultMap() {
        return DEFAULT_MAP;
    }

    public static Value getDefaultValue() {
        return DEFAULT_VALUE;
    }

    public static String objectToString(Object o) {
        return o == null ? DEFAULT_STRING : o.toString();
    }

    public static TypedNumber objectToNumber(Object o) {
        if (o instanceof TypedNumber) {
            return (TypedNumber) o;
        }
        if (isInt64ableNumber(o)) {
            return TypedNumber.numberWithInt64(getInt64(o));
        }
        if (isDoubleableNumber(o)) {
            return TypedNumber.numberWithDouble(Double.valueOf(getDouble(o)));
        }
        return parseNumber(objectToString(o));
    }

    public static Long objectToInt64(Object o) {
        return isInt64ableNumber(o) ? Long.valueOf(getInt64(o)) : parseInt64(objectToString(o));
    }

    public static Double objectToDouble(Object o) {
        return isDoubleableNumber(o) ? Double.valueOf(getDouble(o)) : parseDouble(objectToString(o));
    }

    public static Boolean objectToBoolean(Object o) {
        return o instanceof Boolean ? (Boolean) o : parseBoolean(objectToString(o));
    }

    public static String valueToString(Value v) {
        return objectToString(valueToObject(v));
    }

    public static TypedNumber valueToNumber(Value v) {
        return objectToNumber(valueToObject(v));
    }

    public static Long valueToInt64(Value v) {
        return objectToInt64(valueToObject(v));
    }

    public static Double valueToDouble(Value v) {
        return objectToDouble(valueToObject(v));
    }

    public static Boolean valueToBoolean(Value v) {
        return objectToBoolean(valueToObject(v));
    }

    public static Value objectToValue(Object o) {
        Value returnValue = new Value();
        boolean containsRef = false;
        if (o instanceof Value) {
            return (Value) o;
        }
        List<Value> valueList;
        if (o instanceof String) {
            returnValue.type = 1;
            returnValue.string = (String) o;
        } else if (o instanceof List) {
            returnValue.type = 2;
            List<Object> objectList = (List) o;
            valueList = new ArrayList(objectList.size());
            for (Object listObject : objectList) {
                Value listValue = objectToValue(listObject);
                if (listValue == DEFAULT_VALUE) {
                    return DEFAULT_VALUE;
                }
                boolean z = containsRef || listValue.containsReferences;
                containsRef = z;
                valueList.add(listValue);
            }
            returnValue.listItem = (Value[]) valueList.toArray(new Value[0]);
        } else if (o instanceof Map) {
            returnValue.type = 3;
            Set<Entry<Object, Object>> entries = ((Map) o).entrySet();
            valueList = new ArrayList(entries.size());
            List<Value> values = new ArrayList(entries.size());
            for (Entry<Object, Object> entry : entries) {
                Value key = objectToValue(entry.getKey());
                Value value = objectToValue(entry.getValue());
                if (key == DEFAULT_VALUE || value == DEFAULT_VALUE) {
                    return DEFAULT_VALUE;
                }
                boolean z2 = containsRef || key.containsReferences || value.containsReferences;
                containsRef = z2;
                valueList.add(key);
                values.add(value);
            }
            returnValue.mapKey = (Value[]) valueList.toArray(new Value[0]);
            returnValue.mapValue = (Value[]) values.toArray(new Value[0]);
        } else if (isDoubleableNumber(o)) {
            returnValue.type = 1;
            returnValue.string = o.toString();
        } else if (isInt64ableNumber(o)) {
            returnValue.type = 6;
            returnValue.integer = getInt64(o);
        } else if (o instanceof Boolean) {
            returnValue.type = 8;
            returnValue.boolean_ = ((Boolean) o).booleanValue();
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Converting to Value from unknown object type: ");
            stringBuilder.append(o == null ? "null" : o.getClass().toString());
            Log.e(stringBuilder.toString());
            return DEFAULT_VALUE;
        }
        returnValue.containsReferences = containsRef;
        return returnValue;
    }

    public static Value functionIdToValue(String id) {
        Value value = new Value();
        value.type = 5;
        value.functionId = id;
        return value;
    }

    public static Value macroReferenceToValue(String macroName, int... escapings) {
        Value value = new Value();
        value.type = 4;
        value.macroReference = macroName;
        value.containsReferences = true;
        value.escaping = (int[]) escapings.clone();
        return value;
    }

    public static Value templateToValue(Value... tokens) {
        Value value = new Value();
        value.type = 7;
        value.templateToken = new Value[tokens.length];
        boolean containsRef = false;
        for (int i = 0; i < tokens.length; i++) {
            value.templateToken[i] = tokens[i];
            boolean z = containsRef || tokens[i].containsReferences;
            containsRef = z;
        }
        value.containsReferences = containsRef;
        return value;
    }

    private static boolean isDoubleableNumber(Object o) {
        return (o instanceof Double) || (o instanceof Float) || ((o instanceof TypedNumber) && ((TypedNumber) o).isDouble());
    }

    private static double getDouble(Object o) {
        if (o instanceof Number) {
            return ((Number) o).doubleValue();
        }
        Log.e("getDouble received non-Number");
        return 0.0d;
    }

    private static boolean isInt64ableNumber(Object o) {
        return (o instanceof Byte) || (o instanceof Short) || (o instanceof Integer) || (o instanceof Long) || ((o instanceof TypedNumber) && ((TypedNumber) o).isInt64());
    }

    private static long getInt64(Object o) {
        if (o instanceof Number) {
            return ((Number) o).longValue();
        }
        Log.e("getInt64 received non-Number");
        return 0;
    }

    private static TypedNumber parseNumber(String s) {
        try {
            return TypedNumber.numberWithString(s);
        } catch (NumberFormatException e) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Failed to convert '");
            stringBuilder.append(s);
            stringBuilder.append("' to a number.");
            Log.e(stringBuilder.toString());
            return DEFAULT_NUMBER;
        }
    }

    private static Long parseInt64(String s) {
        TypedNumber result = parseNumber(s);
        return result == DEFAULT_NUMBER ? DEFAULT_INT64 : Long.valueOf(result.longValue());
    }

    private static Double parseDouble(String s) {
        TypedNumber result = parseNumber(s);
        return result == DEFAULT_NUMBER ? DEFAULT_DOUBLE : Double.valueOf(result.doubleValue());
    }

    private static Boolean parseBoolean(String s) {
        if ("true".equalsIgnoreCase(s)) {
            return Boolean.TRUE;
        }
        if ("false".equalsIgnoreCase(s)) {
            return Boolean.FALSE;
        }
        return DEFAULT_BOOLEAN;
    }

    public static Object valueToObject(Value v) {
        if (v == null) {
            return DEFAULT_OBJECT;
        }
        int i = 0;
        Value[] arr$;
        int len$;
        StringBuilder stringBuilder;
        switch (v.type) {
            case 1:
                return v.string;
            case 2:
                ArrayList<Object> result = new ArrayList(v.listItem.length);
                arr$ = v.listItem;
                len$ = arr$.length;
                while (i < len$) {
                    Object o = valueToObject(arr$[i]);
                    if (o == DEFAULT_OBJECT) {
                        return DEFAULT_OBJECT;
                    }
                    result.add(o);
                    i++;
                }
                return result;
            case 3:
                if (v.mapKey.length != v.mapValue.length) {
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("Converting an invalid value to object: ");
                    stringBuilder.append(v.toString());
                    Log.e(stringBuilder.toString());
                    return DEFAULT_OBJECT;
                }
                Map<Object, Object> result2 = new LinkedHashMap(v.mapValue.length);
                while (i < v.mapKey.length) {
                    Object key = valueToObject(v.mapKey[i]);
                    Object value = valueToObject(v.mapValue[i]);
                    if (key == DEFAULT_OBJECT || value == DEFAULT_OBJECT) {
                        return DEFAULT_OBJECT;
                    }
                    result2.put(key, value);
                    i++;
                }
                return result2;
            case 4:
                Log.e("Trying to convert a macro reference to object");
                return DEFAULT_OBJECT;
            case 5:
                Log.e("Trying to convert a function id to object");
                return DEFAULT_OBJECT;
            case 6:
                return Long.valueOf(v.integer);
            case 7:
                StringBuffer result3 = new StringBuffer();
                arr$ = v.templateToken;
                len$ = arr$.length;
                while (i < len$) {
                    String s = valueToString(arr$[i]);
                    if (s == DEFAULT_STRING) {
                        return DEFAULT_OBJECT;
                    }
                    result3.append(s);
                    i++;
                }
                return result3.toString();
            case 8:
                return Boolean.valueOf(v.boolean_);
            default:
                stringBuilder = new StringBuilder();
                stringBuilder.append("Failed to convert a value of type: ");
                stringBuilder.append(v.type);
                Log.e(stringBuilder.toString());
                return DEFAULT_OBJECT;
        }
    }
}
