package com.google.tagmanager;

import com.google.analytics.containertag.common.Key;
import com.google.analytics.midtier.proto.containertag.TypeSystem.Value;
import java.util.Iterator;
import java.util.Map;

abstract class Predicate extends FunctionCallImplementation {
    private static final String ARG0 = Key.ARG0.toString();
    private static final String ARG1 = Key.ARG1.toString();

    public abstract boolean evaluateNoDefaultValues(Value value, Value value2, Map<String, Value> map);

    public static String getArg0Key() {
        return ARG0;
    }

    public static String getArg1Key() {
        return ARG1;
    }

    public Predicate(String functionId) {
        super(functionId, ARG0, ARG1);
    }

    public Value evaluate(Map<String, Value> parameters) {
        Iterator i$ = parameters.values().iterator();
        while (true) {
            boolean result = false;
            if (!i$.hasNext()) {
                Value arg0 = (Value) parameters.get(ARG0);
                Value arg1 = (Value) parameters.get(ARG1);
                if (!(arg0 == null || arg1 == null)) {
                    result = evaluateNoDefaultValues(arg0, arg1, parameters);
                }
                return Types.objectToValue(Boolean.valueOf(result));
            } else if (((Value) i$.next()) == Types.getDefaultValue()) {
                return Types.objectToValue(Boolean.valueOf(false));
            }
        }
    }

    public boolean isCacheable() {
        return true;
    }
}
