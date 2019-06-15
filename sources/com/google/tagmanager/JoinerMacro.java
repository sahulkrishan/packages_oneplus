package com.google.tagmanager;

import com.google.analytics.containertag.common.FunctionType;
import com.google.analytics.containertag.common.Key;
import java.io.UnsupportedEncodingException;
import java.util.Set;

class JoinerMacro extends FunctionCallImplementation {
    private static final String ARG0 = Key.ARG0.toString();
    private static final String DEFAULT_ITEM_SEPARATOR = "";
    private static final String DEFAULT_KEY_VALUE_SEPARATOR = "=";
    private static final String ESCAPE = Key.ESCAPE.toString();
    private static final String ID = FunctionType.JOINER.toString();
    private static final String ITEM_SEPARATOR = Key.ITEM_SEPARATOR.toString();
    private static final String KEY_VALUE_SEPARATOR = Key.KEY_VALUE_SEPARATOR.toString();

    private enum EscapeType {
        NONE,
        URL,
        BACKSLASH
    }

    public static String getFunctionId() {
        return ID;
    }

    public JoinerMacro() {
        super(ID, ARG0);
    }

    public boolean isCacheable() {
        return true;
    }

    /* JADX WARNING: Missing block: B:26:0x00a2, code skipped:
            if (r11 >= r2.mapKey.length) goto L_0x00e1;
     */
    /* JADX WARNING: Missing block: B:27:0x00a4, code skipped:
            if (r11 <= 0) goto L_0x00a9;
     */
    /* JADX WARNING: Missing block: B:28:0x00a6, code skipped:
            r10.append(r4);
     */
    public com.google.analytics.midtier.proto.containertag.TypeSystem.Value evaluate(java.util.Map<java.lang.String, com.google.analytics.midtier.proto.containertag.TypeSystem.Value> r18) {
        /*
        r17 = this;
        r0 = r17;
        r1 = r18;
        r2 = ARG0;
        r2 = r1.get(r2);
        r2 = (com.google.analytics.midtier.proto.containertag.TypeSystem.Value) r2;
        if (r2 != 0) goto L_0x0013;
    L_0x000e:
        r3 = com.google.tagmanager.Types.getDefaultValue();
        return r3;
    L_0x0013:
        r3 = ITEM_SEPARATOR;
        r3 = r1.get(r3);
        r3 = (com.google.analytics.midtier.proto.containertag.TypeSystem.Value) r3;
        if (r3 == 0) goto L_0x0022;
    L_0x001d:
        r4 = com.google.tagmanager.Types.valueToString(r3);
        goto L_0x0024;
    L_0x0022:
        r4 = "";
    L_0x0024:
        r5 = KEY_VALUE_SEPARATOR;
        r5 = r1.get(r5);
        r5 = (com.google.analytics.midtier.proto.containertag.TypeSystem.Value) r5;
        if (r5 == 0) goto L_0x0033;
    L_0x002e:
        r6 = com.google.tagmanager.Types.valueToString(r5);
        goto L_0x0035;
    L_0x0033:
        r6 = "=";
    L_0x0035:
        r7 = com.google.tagmanager.JoinerMacro.EscapeType.NONE;
        r8 = ESCAPE;
        r8 = r1.get(r8);
        r8 = (com.google.analytics.midtier.proto.containertag.TypeSystem.Value) r8;
        r9 = 0;
        if (r8 == 0) goto L_0x008a;
    L_0x0042:
        r10 = com.google.tagmanager.Types.valueToString(r8);
        r11 = "url";
        r11 = r11.equals(r10);
        if (r11 == 0) goto L_0x0051;
    L_0x004e:
        r7 = com.google.tagmanager.JoinerMacro.EscapeType.URL;
        goto L_0x008a;
    L_0x0051:
        r11 = "backslash";
        r11 = r11.equals(r10);
        if (r11 == 0) goto L_0x0071;
    L_0x0059:
        r7 = com.google.tagmanager.JoinerMacro.EscapeType.BACKSLASH;
        r11 = new java.util.HashSet;
        r11.<init>();
        r9 = r11;
        r0.addTo(r9, r4);
        r0.addTo(r9, r6);
        r11 = 92;
        r11 = java.lang.Character.valueOf(r11);
        r9.remove(r11);
        goto L_0x008a;
    L_0x0071:
        r11 = new java.lang.StringBuilder;
        r11.<init>();
        r12 = "Joiner: unsupported escape type: ";
        r11.append(r12);
        r11.append(r10);
        r11 = r11.toString();
        com.google.tagmanager.Log.e(r11);
        r11 = com.google.tagmanager.Types.getDefaultValue();
        return r11;
    L_0x008a:
        r10 = new java.lang.StringBuilder;
        r10.<init>();
        r11 = r2.type;
        r12 = 0;
        switch(r11) {
            case 2: goto L_0x00c6;
            case 3: goto L_0x009d;
            default: goto L_0x0095;
        };
    L_0x0095:
        r1 = com.google.tagmanager.Types.valueToString(r2);
        r0.append(r10, r1, r7, r9);
        goto L_0x00e1;
    L_0x009e:
        r11 = r12;
        r12 = r2.mapKey;
        r12 = r12.length;
        if (r11 >= r12) goto L_0x00c5;
    L_0x00a4:
        if (r11 <= 0) goto L_0x00a9;
    L_0x00a6:
        r10.append(r4);
    L_0x00a9:
        r12 = r2.mapKey;
        r12 = r12[r11];
        r12 = com.google.tagmanager.Types.valueToString(r12);
        r13 = r2.mapValue;
        r13 = r13[r11];
        r13 = com.google.tagmanager.Types.valueToString(r13);
        r0.append(r10, r12, r7, r9);
        r10.append(r6);
        r0.append(r10, r13, r7, r9);
        r12 = r11 + 1;
        goto L_0x009e;
    L_0x00c5:
        goto L_0x00e1;
    L_0x00c6:
        r11 = 1;
        r13 = r2.listItem;
        r14 = r13.length;
    L_0x00ca:
        if (r12 >= r14) goto L_0x00e0;
    L_0x00cc:
        r15 = r13[r12];
        if (r11 != 0) goto L_0x00d3;
    L_0x00d0:
        r10.append(r4);
    L_0x00d3:
        r11 = 0;
        r1 = com.google.tagmanager.Types.valueToString(r15);
        r0.append(r10, r1, r7, r9);
        r12 = r12 + 1;
        r1 = r18;
        goto L_0x00ca;
    L_0x00e1:
        r1 = r10.toString();
        r1 = com.google.tagmanager.Types.objectToValue(r1);
        return r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.tagmanager.JoinerMacro.evaluate(java.util.Map):com.google.analytics.midtier.proto.containertag.TypeSystem$Value");
    }

    private void addTo(Set<Character> set, String string) {
        for (int i = 0; i < string.length(); i++) {
            set.add(Character.valueOf(string.charAt(i)));
        }
    }

    private void append(StringBuilder sb, String s, EscapeType escapeType, Set<Character> charsToBackslashEscape) {
        sb.append(escape(s, escapeType, charsToBackslashEscape));
    }

    private String escape(String s, EscapeType escapeType, Set<Character> charsToBackslashEscape) {
        switch (escapeType) {
            case URL:
                try {
                    return ValueEscapeUtil.urlEncode(s);
                } catch (UnsupportedEncodingException e) {
                    Log.e("Joiner: unsupported encoding", e);
                    return s;
                }
            case BACKSLASH:
                s = s.replace("\\", "\\\\");
                for (Character c : charsToBackslashEscape) {
                    String charAsString = c.toString();
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("\\");
                    stringBuilder.append(charAsString);
                    s = s.replace(charAsString, stringBuilder.toString());
                }
                return s;
            default:
                return s;
        }
    }
}
