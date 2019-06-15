package com.google.tagmanager;

import com.google.analytics.containertag.common.Key;
import com.google.analytics.containertag.proto.Serving.FunctionCall;
import com.google.analytics.containertag.proto.Serving.Property;
import com.google.analytics.containertag.proto.Serving.Resource;
import com.google.analytics.containertag.proto.Serving.Rule;
import com.google.analytics.containertag.proto.Serving.ServingValue;
import com.google.analytics.midtier.proto.containertag.TypeSystem.Value;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class ResourceUtil {
    private static final int BUFFER_SIZE = 1024;

    public static class ExpandedFunctionCall {
        private final Map<String, Value> mPropertiesMap;
        private final Value mPushAfterEvaluate;

        private ExpandedFunctionCall(Map<String, Value> propertiesMap, Value pushAfterEvaluate) {
            this.mPropertiesMap = propertiesMap;
            this.mPushAfterEvaluate = pushAfterEvaluate;
        }

        public static ExpandedFunctionCallBuilder newBuilder() {
            return new ExpandedFunctionCallBuilder();
        }

        public void updateCacheableProperty(String key, Value v) {
            this.mPropertiesMap.put(key, v);
        }

        public Map<String, Value> getProperties() {
            return Collections.unmodifiableMap(this.mPropertiesMap);
        }

        public Value getPushAfterEvaluate() {
            return this.mPushAfterEvaluate;
        }

        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Properties: ");
            stringBuilder.append(getProperties());
            stringBuilder.append(" pushAfterEvaluate: ");
            stringBuilder.append(this.mPushAfterEvaluate);
            return stringBuilder.toString();
        }
    }

    public static class ExpandedFunctionCallBuilder {
        private final Map<String, Value> mPropertiesMap;
        private Value mPushAfterEvaluate;

        private ExpandedFunctionCallBuilder() {
            this.mPropertiesMap = new HashMap();
        }

        public ExpandedFunctionCallBuilder addProperty(String key, Value value) {
            this.mPropertiesMap.put(key, value);
            return this;
        }

        public ExpandedFunctionCallBuilder setPushAfterEvaluate(Value value) {
            this.mPushAfterEvaluate = value;
            return this;
        }

        public ExpandedFunctionCall build() {
            return new ExpandedFunctionCall(this.mPropertiesMap, this.mPushAfterEvaluate);
        }
    }

    public static class ExpandedResource {
        private final Map<String, List<ExpandedFunctionCall>> mMacros;
        private final int mResourceFormatVersion;
        private final List<ExpandedRule> mRules;
        private final String mVersion;

        private ExpandedResource(List<ExpandedRule> rules, Map<String, List<ExpandedFunctionCall>> macros, String version, int resourceFormatVersion) {
            this.mRules = Collections.unmodifiableList(rules);
            this.mMacros = Collections.unmodifiableMap(macros);
            this.mVersion = version;
            this.mResourceFormatVersion = resourceFormatVersion;
        }

        public static ExpandedResourceBuilder newBuilder() {
            return new ExpandedResourceBuilder();
        }

        public List<ExpandedRule> getRules() {
            return this.mRules;
        }

        public String getVersion() {
            return this.mVersion;
        }

        public int getResourceFormatVersion() {
            return this.mResourceFormatVersion;
        }

        public List<ExpandedFunctionCall> getMacros(String name) {
            return (List) this.mMacros.get(name);
        }

        public Map<String, List<ExpandedFunctionCall>> getAllMacros() {
            return this.mMacros;
        }

        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Rules: ");
            stringBuilder.append(getRules());
            stringBuilder.append("  Macros: ");
            stringBuilder.append(this.mMacros);
            return stringBuilder.toString();
        }
    }

    public static class ExpandedResourceBuilder {
        private final Map<String, List<ExpandedFunctionCall>> mMacros;
        private int mResourceFormatVersion;
        private final List<ExpandedRule> mRules;
        private String mVersion;

        private ExpandedResourceBuilder() {
            this.mRules = new ArrayList();
            this.mMacros = new HashMap();
            this.mVersion = "";
            this.mResourceFormatVersion = 0;
        }

        public ExpandedResourceBuilder addRule(ExpandedRule r) {
            this.mRules.add(r);
            return this;
        }

        public ExpandedResourceBuilder addMacro(ExpandedFunctionCall f) {
            String macroName = Types.valueToString((Value) f.getProperties().get(Key.INSTANCE_NAME.toString()));
            List<ExpandedFunctionCall> macroList = (List) this.mMacros.get(macroName);
            if (macroList == null) {
                macroList = new ArrayList();
                this.mMacros.put(macroName, macroList);
            }
            macroList.add(f);
            return this;
        }

        public ExpandedResourceBuilder setVersion(String version) {
            this.mVersion = version;
            return this;
        }

        public ExpandedResourceBuilder setResourceFormatVersion(int resourceFormatVersion) {
            this.mResourceFormatVersion = resourceFormatVersion;
            return this;
        }

        public ExpandedResource build() {
            return new ExpandedResource(this.mRules, this.mMacros, this.mVersion, this.mResourceFormatVersion);
        }
    }

    public static class ExpandedRule {
        private final List<String> mAddMacroRuleNames;
        private final List<ExpandedFunctionCall> mAddMacros;
        private final List<String> mAddTagRuleNames;
        private final List<ExpandedFunctionCall> mAddTags;
        private final List<ExpandedFunctionCall> mNegativePredicates;
        private final List<ExpandedFunctionCall> mPositivePredicates;
        private final List<String> mRemoveMacroRuleNames;
        private final List<ExpandedFunctionCall> mRemoveMacros;
        private final List<String> mRemoveTagRuleNames;
        private final List<ExpandedFunctionCall> mRemoveTags;

        private ExpandedRule(List<ExpandedFunctionCall> postivePredicates, List<ExpandedFunctionCall> negativePredicates, List<ExpandedFunctionCall> addTags, List<ExpandedFunctionCall> removeTags, List<ExpandedFunctionCall> addMacros, List<ExpandedFunctionCall> removeMacros, List<String> addMacroRuleNames, List<String> removeMacroRuleNames, List<String> addTagRuleNames, List<String> removeTagRuleNames) {
            this.mPositivePredicates = Collections.unmodifiableList(postivePredicates);
            this.mNegativePredicates = Collections.unmodifiableList(negativePredicates);
            this.mAddTags = Collections.unmodifiableList(addTags);
            this.mRemoveTags = Collections.unmodifiableList(removeTags);
            this.mAddMacros = Collections.unmodifiableList(addMacros);
            this.mRemoveMacros = Collections.unmodifiableList(removeMacros);
            this.mAddMacroRuleNames = Collections.unmodifiableList(addMacroRuleNames);
            this.mRemoveMacroRuleNames = Collections.unmodifiableList(removeMacroRuleNames);
            this.mAddTagRuleNames = Collections.unmodifiableList(addTagRuleNames);
            this.mRemoveTagRuleNames = Collections.unmodifiableList(removeTagRuleNames);
        }

        public static ExpandedRuleBuilder newBuilder() {
            return new ExpandedRuleBuilder();
        }

        public List<ExpandedFunctionCall> getPositivePredicates() {
            return this.mPositivePredicates;
        }

        public List<ExpandedFunctionCall> getNegativePredicates() {
            return this.mNegativePredicates;
        }

        public List<ExpandedFunctionCall> getAddTags() {
            return this.mAddTags;
        }

        public List<ExpandedFunctionCall> getRemoveTags() {
            return this.mRemoveTags;
        }

        public List<ExpandedFunctionCall> getAddMacros() {
            return this.mAddMacros;
        }

        public List<String> getAddMacroRuleNames() {
            return this.mAddMacroRuleNames;
        }

        public List<String> getRemoveMacroRuleNames() {
            return this.mRemoveMacroRuleNames;
        }

        public List<String> getAddTagRuleNames() {
            return this.mAddTagRuleNames;
        }

        public List<String> getRemoveTagRuleNames() {
            return this.mRemoveTagRuleNames;
        }

        public List<ExpandedFunctionCall> getRemoveMacros() {
            return this.mRemoveMacros;
        }

        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Positive predicates: ");
            stringBuilder.append(getPositivePredicates());
            stringBuilder.append("  Negative predicates: ");
            stringBuilder.append(getNegativePredicates());
            stringBuilder.append("  Add tags: ");
            stringBuilder.append(getAddTags());
            stringBuilder.append("  Remove tags: ");
            stringBuilder.append(getRemoveTags());
            stringBuilder.append("  Add macros: ");
            stringBuilder.append(getAddMacros());
            stringBuilder.append("  Remove macros: ");
            stringBuilder.append(getRemoveMacros());
            return stringBuilder.toString();
        }
    }

    public static class ExpandedRuleBuilder {
        private final List<String> mAddMacroRuleNames;
        private final List<ExpandedFunctionCall> mAddMacros;
        private final List<String> mAddTagRuleNames;
        private final List<ExpandedFunctionCall> mAddTags;
        private final List<ExpandedFunctionCall> mNegativePredicates;
        private final List<ExpandedFunctionCall> mPositivePredicates;
        private final List<String> mRemoveMacroRuleNames;
        private final List<ExpandedFunctionCall> mRemoveMacros;
        private final List<String> mRemoveTagRuleNames;
        private final List<ExpandedFunctionCall> mRemoveTags;

        private ExpandedRuleBuilder() {
            this.mPositivePredicates = new ArrayList();
            this.mNegativePredicates = new ArrayList();
            this.mAddTags = new ArrayList();
            this.mRemoveTags = new ArrayList();
            this.mAddMacros = new ArrayList();
            this.mRemoveMacros = new ArrayList();
            this.mAddMacroRuleNames = new ArrayList();
            this.mRemoveMacroRuleNames = new ArrayList();
            this.mAddTagRuleNames = new ArrayList();
            this.mRemoveTagRuleNames = new ArrayList();
        }

        public ExpandedRuleBuilder addPositivePredicate(ExpandedFunctionCall f) {
            this.mPositivePredicates.add(f);
            return this;
        }

        public ExpandedRuleBuilder addNegativePredicate(ExpandedFunctionCall f) {
            this.mNegativePredicates.add(f);
            return this;
        }

        public ExpandedRuleBuilder addAddTag(ExpandedFunctionCall f) {
            this.mAddTags.add(f);
            return this;
        }

        public ExpandedRuleBuilder addAddTagRuleName(String ruleName) {
            this.mAddTagRuleNames.add(ruleName);
            return this;
        }

        public ExpandedRuleBuilder addRemoveTag(ExpandedFunctionCall f) {
            this.mRemoveTags.add(f);
            return this;
        }

        public ExpandedRuleBuilder addRemoveTagRuleName(String ruleName) {
            this.mRemoveTagRuleNames.add(ruleName);
            return this;
        }

        public ExpandedRuleBuilder addAddMacro(ExpandedFunctionCall f) {
            this.mAddMacros.add(f);
            return this;
        }

        public ExpandedRuleBuilder addAddMacroRuleName(String ruleName) {
            this.mAddMacroRuleNames.add(ruleName);
            return this;
        }

        public ExpandedRuleBuilder addRemoveMacro(ExpandedFunctionCall f) {
            this.mRemoveMacros.add(f);
            return this;
        }

        public ExpandedRuleBuilder addRemoveMacroRuleName(String ruleName) {
            this.mRemoveMacroRuleNames.add(ruleName);
            return this;
        }

        public ExpandedRule build() {
            return new ExpandedRule(this.mPositivePredicates, this.mNegativePredicates, this.mAddTags, this.mRemoveTags, this.mAddMacros, this.mRemoveMacros, this.mAddMacroRuleNames, this.mRemoveMacroRuleNames, this.mAddTagRuleNames, this.mRemoveTagRuleNames);
        }
    }

    public static class InvalidResourceException extends Exception {
        public InvalidResourceException(String s) {
            super(s);
        }
    }

    private ResourceUtil() {
    }

    public static ExpandedResource getExpandedResource(Resource resource) throws InvalidResourceException {
        Value[] expandedValues = new Value[resource.value.length];
        int i$ = 0;
        for (int i = 0; i < resource.value.length; i++) {
            expandValue(i, resource, expandedValues, new HashSet(0));
        }
        ExpandedResourceBuilder builder = ExpandedResource.newBuilder();
        List<ExpandedFunctionCall> tags = new ArrayList();
        for (int i2 = 0; i2 < resource.tag.length; i2++) {
            tags.add(expandFunctionCall(resource.tag[i2], resource, expandedValues, i2));
        }
        List<ExpandedFunctionCall> predicates = new ArrayList();
        for (int i3 = 0; i3 < resource.predicate.length; i3++) {
            predicates.add(expandFunctionCall(resource.predicate[i3], resource, expandedValues, i3));
        }
        List<ExpandedFunctionCall> macros = new ArrayList();
        for (int i4 = 0; i4 < resource.macro.length; i4++) {
            ExpandedFunctionCall thisMacro = expandFunctionCall(resource.macro[i4], resource, expandedValues, i4);
            builder.addMacro(thisMacro);
            macros.add(thisMacro);
        }
        Rule[] arr$ = resource.rule;
        int len$ = arr$.length;
        while (i$ < len$) {
            builder.addRule(expandRule(arr$[i$], tags, macros, predicates, resource));
            i$++;
        }
        builder.setVersion(resource.version);
        builder.setResourceFormatVersion(resource.resourceFormatVersion);
        return builder.build();
    }

    public static Value newValueBasedOnValue(Value v) {
        Value result = new Value();
        result.type = v.type;
        result.escaping = (int[]) v.escaping.clone();
        if (v.containsReferences) {
            result.containsReferences = v.containsReferences;
        }
        return result;
    }

    private static Value expandValue(int i, Resource resource, Value[] expandedValues, Set<Integer> pendingExpansions) throws InvalidResourceException {
        if (pendingExpansions.contains(Integer.valueOf(i))) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Value cycle detected.  Current value reference: ");
            stringBuilder.append(i);
            stringBuilder.append(".");
            stringBuilder.append("  Previous value references: ");
            stringBuilder.append(pendingExpansions);
            stringBuilder.append(".");
            logAndThrow(stringBuilder.toString());
        }
        Value value = (Value) getWithBoundsCheck(resource.value, i, "values");
        if (expandedValues[i] != null) {
            return expandedValues[i];
        }
        Value toAdd = null;
        pendingExpansions.add(Integer.valueOf(i));
        int i$ = 0;
        ServingValue servingValue;
        int index;
        int[] arr$;
        int len$;
        int index2;
        switch (value.type) {
            case 1:
            case 5:
            case 6:
            case 8:
                toAdd = value;
                break;
            case 2:
                servingValue = getServingValue(value);
                toAdd = newValueBasedOnValue(value);
                toAdd.listItem = new Value[servingValue.listItem.length];
                index = 0;
                arr$ = servingValue.listItem;
                len$ = arr$.length;
                while (i$ < len$) {
                    index2 = index + 1;
                    toAdd.listItem[index] = expandValue(arr$[i$], resource, expandedValues, pendingExpansions);
                    i$++;
                    index = index2;
                }
                break;
            case 3:
                toAdd = newValueBasedOnValue(value);
                servingValue = getServingValue(value);
                if (servingValue.mapKey.length != servingValue.mapValue.length) {
                    StringBuilder stringBuilder2 = new StringBuilder();
                    stringBuilder2.append("Uneven map keys (");
                    stringBuilder2.append(servingValue.mapKey.length);
                    stringBuilder2.append(") and map values (");
                    stringBuilder2.append(servingValue.mapValue.length);
                    stringBuilder2.append(")");
                    logAndThrow(stringBuilder2.toString());
                }
                toAdd.mapKey = new Value[servingValue.mapKey.length];
                toAdd.mapValue = new Value[servingValue.mapKey.length];
                arr$ = servingValue.mapKey;
                len$ = arr$.length;
                int index3 = 0;
                index = 0;
                while (index < len$) {
                    int index4 = index3 + 1;
                    toAdd.mapKey[index3] = expandValue(arr$[index], resource, expandedValues, pendingExpansions);
                    index++;
                    index3 = index4;
                }
                index = 0;
                arr$ = servingValue.mapValue;
                len$ = arr$.length;
                while (i$ < len$) {
                    index2 = index + 1;
                    toAdd.mapValue[index] = expandValue(arr$[i$], resource, expandedValues, pendingExpansions);
                    i$++;
                    index = index2;
                }
                break;
            case 4:
                toAdd = newValueBasedOnValue(value);
                toAdd.macroReference = Types.valueToString(expandValue(getServingValue(value).macroNameReference, resource, expandedValues, pendingExpansions));
                break;
            case 7:
                toAdd = newValueBasedOnValue(value);
                servingValue = getServingValue(value);
                toAdd.templateToken = new Value[servingValue.templateToken.length];
                index = 0;
                arr$ = servingValue.templateToken;
                len$ = arr$.length;
                while (i$ < len$) {
                    index2 = index + 1;
                    toAdd.templateToken[index] = expandValue(arr$[i$], resource, expandedValues, pendingExpansions);
                    i$++;
                    index = index2;
                }
                break;
        }
        if (toAdd == null) {
            StringBuilder stringBuilder3 = new StringBuilder();
            stringBuilder3.append("Invalid value: ");
            stringBuilder3.append(value);
            logAndThrow(stringBuilder3.toString());
        }
        expandedValues[i] = toAdd;
        pendingExpansions.remove(Integer.valueOf(i));
        return toAdd;
    }

    private static ServingValue getServingValue(Value value) throws InvalidResourceException {
        if (((ServingValue) value.getExtension(ServingValue.ext)) == null) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Expected a ServingValue and didn't get one. Value is: ");
            stringBuilder.append(value);
            logAndThrow(stringBuilder.toString());
        }
        return (ServingValue) value.getExtension(ServingValue.ext);
    }

    private static void logAndThrow(String error) throws InvalidResourceException {
        Log.e(error);
        throw new InvalidResourceException(error);
    }

    private static <T> T getWithBoundsCheck(T[] array, int idx, String listName) throws InvalidResourceException {
        if (idx < 0 || idx >= array.length) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Index out of bounds detected: ");
            stringBuilder.append(idx);
            stringBuilder.append(" in ");
            stringBuilder.append(listName);
            logAndThrow(stringBuilder.toString());
        }
        return array[idx];
    }

    private static <T> T getWithBoundsCheck(List<T> list, int idx, String listName) throws InvalidResourceException {
        if (idx < 0 || idx >= list.size()) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Index out of bounds detected: ");
            stringBuilder.append(idx);
            stringBuilder.append(" in ");
            stringBuilder.append(listName);
            logAndThrow(stringBuilder.toString());
        }
        return list.get(idx);
    }

    private static ExpandedFunctionCall expandFunctionCall(FunctionCall functionCall, Resource resource, Value[] expandedValues, int idx) throws InvalidResourceException {
        ExpandedFunctionCallBuilder builder = ExpandedFunctionCall.newBuilder();
        for (Integer i : functionCall.property) {
            Property p = (Property) getWithBoundsCheck(resource.property, Integer.valueOf(i).intValue(), "properties");
            String key = (String) getWithBoundsCheck(resource.key, p.key, "keys");
            Value value = (Value) getWithBoundsCheck((Object[]) expandedValues, p.value, "values");
            if (Key.PUSH_AFTER_EVALUATE.toString().equals(key)) {
                builder.setPushAfterEvaluate(value);
            } else {
                builder.addProperty(key, value);
            }
        }
        return builder.build();
    }

    private static ExpandedRule expandRule(Rule rule, List<ExpandedFunctionCall> tags, List<ExpandedFunctionCall> macros, List<ExpandedFunctionCall> predicates, Resource resource) {
        ExpandedRuleBuilder ruleBuilder = ExpandedRule.newBuilder();
        int i$ = 0;
        for (Integer i : rule.positivePredicate) {
            ruleBuilder.addPositivePredicate((ExpandedFunctionCall) predicates.get(Integer.valueOf(i).intValue()));
        }
        for (int valueOf : rule.negativePredicate) {
            ruleBuilder.addNegativePredicate((ExpandedFunctionCall) predicates.get(Integer.valueOf(valueOf).intValue()));
        }
        for (Integer i2 : rule.addTag) {
            ruleBuilder.addAddTag((ExpandedFunctionCall) tags.get(Integer.valueOf(i2).intValue()));
        }
        for (Integer i22 : rule.addTagRuleName) {
            ruleBuilder.addAddTagRuleName(resource.value[Integer.valueOf(i22).intValue()].string);
        }
        for (Integer i222 : rule.removeTag) {
            ruleBuilder.addRemoveTag((ExpandedFunctionCall) tags.get(Integer.valueOf(i222).intValue()));
        }
        for (Integer i2222 : rule.removeTagRuleName) {
            ruleBuilder.addRemoveTagRuleName(resource.value[Integer.valueOf(i2222).intValue()].string);
        }
        for (Integer i22222 : rule.addMacro) {
            ruleBuilder.addAddMacro((ExpandedFunctionCall) macros.get(Integer.valueOf(i22222).intValue()));
        }
        for (Integer i222222 : rule.addMacroRuleName) {
            ruleBuilder.addAddMacroRuleName(resource.value[Integer.valueOf(i222222).intValue()].string);
        }
        for (Integer i2222222 : rule.removeMacro) {
            ruleBuilder.addRemoveMacro((ExpandedFunctionCall) macros.get(Integer.valueOf(i2222222).intValue()));
        }
        int[] arr$ = rule.removeMacroRuleName;
        int len$ = arr$.length;
        while (i$ < len$) {
            ruleBuilder.addRemoveMacroRuleName(resource.value[Integer.valueOf(arr$[i$]).intValue()].string);
            i$++;
        }
        return ruleBuilder.build();
    }

    public static void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        while (true) {
            int numBytes = in.read(buffer);
            if (numBytes != -1) {
                out.write(buffer, 0, numBytes);
            } else {
                return;
            }
        }
    }
}
