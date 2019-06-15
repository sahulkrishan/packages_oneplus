package com.google.tagmanager.protobuf.nano;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

public final class MessageNanoPrinter {
    private static final String INDENT = "  ";
    private static final int MAX_STRING_LEN = 200;

    private MessageNanoPrinter() {
    }

    public static <T extends MessageNano> String print(T message) {
        if (message == null) {
            return "null";
        }
        StringBuffer buf = new StringBuffer();
        try {
            print(message.getClass().getSimpleName(), message.getClass(), message, new StringBuffer(), buf);
            return buf.toString();
        } catch (IllegalAccessException e) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Error printing proto: ");
            stringBuilder.append(e.getMessage());
            return stringBuilder.toString();
        }
    }

    private static void print(String identifier, Class<?> clazz, Object message, StringBuffer indentBuf, StringBuffer buf) throws IllegalAccessException {
        Object obj = message;
        StringBuffer stringBuffer = indentBuf;
        StringBuffer stringBuffer2 = buf;
        if (!MessageNano.class.isAssignableFrom(clazz)) {
            String str = identifier;
            if (obj != null) {
                str = deCamelCaseify(identifier);
                stringBuffer2.append(stringBuffer);
                stringBuffer2.append(str);
                stringBuffer2.append(": ");
                if (obj instanceof String) {
                    String stringMessage = sanitizeString((String) obj);
                    stringBuffer2.append("\"");
                    stringBuffer2.append(stringMessage);
                    stringBuffer2.append("\"");
                } else {
                    stringBuffer2.append(obj);
                }
                stringBuffer2.append("\n");
            }
        } else if (obj != null) {
            stringBuffer2.append(stringBuffer);
            stringBuffer2.append(identifier);
            stringBuffer.append(INDENT);
            stringBuffer2.append(" <\n");
            Field[] arr$ = clazz.getFields();
            int len$ = arr$.length;
            int i$ = 0;
            while (i$ < len$) {
                Class<?> cls;
                Field field = arr$[i$];
                int modifiers = field.getModifiers();
                String fieldName = field.getName();
                if ((modifiers & 1) == 1 && (modifiers & 8) != 8 && !fieldName.startsWith("_") && !fieldName.endsWith("_")) {
                    Class<?> fieldType = field.getType();
                    Object value = field.get(obj);
                    if (fieldType.isArray()) {
                        Class<?> arrayType = fieldType.getComponentType();
                        if (arrayType != Byte.TYPE) {
                            int len = value == null ? 0 : Array.getLength(value);
                            int i = 0;
                            while (true) {
                                int i2 = i;
                                if (i2 >= len) {
                                    break;
                                }
                                print(fieldName, arrayType, Array.get(value, i2), stringBuffer, stringBuffer2);
                                i = i2 + 1;
                                cls = clazz;
                            }
                        } else {
                            print(fieldName, fieldType, value, stringBuffer, stringBuffer2);
                        }
                    } else {
                        print(fieldName, fieldType, value, stringBuffer, stringBuffer2);
                    }
                }
                i$++;
                cls = clazz;
            }
            stringBuffer.delete(indentBuf.length() - INDENT.length(), indentBuf.length());
            stringBuffer2.append(stringBuffer);
            stringBuffer2.append(">\n");
        }
    }

    private static String deCamelCaseify(String identifier) {
        StringBuffer out = new StringBuffer();
        for (int i = 0; i < identifier.length(); i++) {
            char currentChar = identifier.charAt(i);
            if (i == 0) {
                out.append(Character.toLowerCase(currentChar));
            } else if (Character.isUpperCase(currentChar)) {
                out.append('_');
                out.append(Character.toLowerCase(currentChar));
            } else {
                out.append(currentChar);
            }
        }
        return out.toString();
    }

    private static String sanitizeString(String str) {
        if (!str.startsWith("http") && str.length() > 200) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(str.substring(0, 200));
            stringBuilder.append("[...]");
            str = stringBuilder.toString();
        }
        return escapeString(str);
    }

    private static String escapeString(String str) {
        int strLen = str.length();
        StringBuilder b = new StringBuilder(strLen);
        for (int i = 0; i < strLen; i++) {
            char original = str.charAt(i);
            if (original < ' ' || original > '~' || original == '\"' || original == '\'') {
                b.append(String.format("\\u%04x", new Object[]{Integer.valueOf(original)}));
            } else {
                b.append(original);
            }
        }
        return b.toString();
    }
}
