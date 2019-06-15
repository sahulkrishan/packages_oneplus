package com.google.tagmanager;

import com.google.common.base.Ascii;

final class Base64 {
    private static final byte[] ALPHABET = new byte[]{(byte) 65, (byte) 66, (byte) 67, (byte) 68, (byte) 69, (byte) 70, (byte) 71, (byte) 72, (byte) 73, (byte) 74, (byte) 75, (byte) 76, (byte) 77, (byte) 78, (byte) 79, (byte) 80, (byte) 81, (byte) 82, (byte) 83, (byte) 84, (byte) 85, (byte) 86, (byte) 87, (byte) 88, (byte) 89, (byte) 90, (byte) 97, (byte) 98, (byte) 99, (byte) 100, (byte) 101, (byte) 102, (byte) 103, (byte) 104, (byte) 105, (byte) 106, (byte) 107, (byte) 108, (byte) 109, (byte) 110, (byte) 111, (byte) 112, (byte) 113, (byte) 114, (byte) 115, (byte) 116, (byte) 117, (byte) 118, (byte) 119, (byte) 120, (byte) 121, (byte) 122, (byte) 48, (byte) 49, (byte) 50, (byte) 51, (byte) 52, (byte) 53, (byte) 54, (byte) 55, (byte) 56, (byte) 57, (byte) 43, (byte) 47};
    private static final byte[] DECODABET = new byte[]{(byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, WHITE_SPACE_ENC, WHITE_SPACE_ENC, (byte) -9, (byte) -9, WHITE_SPACE_ENC, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, WHITE_SPACE_ENC, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) 62, (byte) -9, (byte) -9, (byte) -9, (byte) 63, (byte) 52, (byte) 53, (byte) 54, (byte) 55, (byte) 56, (byte) 57, (byte) 58, (byte) 59, (byte) 60, PADDING_BYTE, (byte) -9, (byte) -9, (byte) -9, (byte) -1, (byte) -9, (byte) -9, (byte) -9, (byte) 0, (byte) 1, (byte) 2, (byte) 3, (byte) 4, (byte) 5, (byte) 6, (byte) 7, (byte) 8, (byte) 9, (byte) 10, Ascii.VT, Ascii.FF, Ascii.CR, Ascii.SO, Ascii.SI, Ascii.DLE, (byte) 17, Ascii.DC2, (byte) 19, Ascii.DC4, Ascii.NAK, Ascii.SYN, Ascii.ETB, Ascii.CAN, Ascii.EM, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, Ascii.SUB, Ascii.ESC, Ascii.FS, Ascii.GS, Ascii.RS, Ascii.US, (byte) 32, (byte) 33, (byte) 34, (byte) 35, (byte) 36, (byte) 37, (byte) 38, (byte) 39, (byte) 40, (byte) 41, (byte) 42, (byte) 43, (byte) 44, (byte) 45, (byte) 46, (byte) 47, (byte) 48, (byte) 49, (byte) 50, (byte) 51, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9};
    private static final byte EQUALS_SIGN_ENC = (byte) -1;
    private static final byte NEW_LINE = (byte) 10;
    private static final byte PADDING_BYTE = (byte) 61;
    private static final byte[] WEBSAFE_ALPHABET = new byte[]{(byte) 65, (byte) 66, (byte) 67, (byte) 68, (byte) 69, (byte) 70, (byte) 71, (byte) 72, (byte) 73, (byte) 74, (byte) 75, (byte) 76, (byte) 77, (byte) 78, (byte) 79, (byte) 80, (byte) 81, (byte) 82, (byte) 83, (byte) 84, (byte) 85, (byte) 86, (byte) 87, (byte) 88, (byte) 89, (byte) 90, (byte) 97, (byte) 98, (byte) 99, (byte) 100, (byte) 101, (byte) 102, (byte) 103, (byte) 104, (byte) 105, (byte) 106, (byte) 107, (byte) 108, (byte) 109, (byte) 110, (byte) 111, (byte) 112, (byte) 113, (byte) 114, (byte) 115, (byte) 116, (byte) 117, (byte) 118, (byte) 119, (byte) 120, (byte) 121, (byte) 122, (byte) 48, (byte) 49, (byte) 50, (byte) 51, (byte) 52, (byte) 53, (byte) 54, (byte) 55, (byte) 56, (byte) 57, (byte) 45, (byte) 95};
    private static final byte[] WEBSAFE_DECODABET = new byte[]{(byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, WHITE_SPACE_ENC, WHITE_SPACE_ENC, (byte) -9, (byte) -9, WHITE_SPACE_ENC, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, WHITE_SPACE_ENC, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) 62, (byte) -9, (byte) -9, (byte) 52, (byte) 53, (byte) 54, (byte) 55, (byte) 56, (byte) 57, (byte) 58, (byte) 59, (byte) 60, PADDING_BYTE, (byte) -9, (byte) -9, (byte) -9, (byte) -1, (byte) -9, (byte) -9, (byte) -9, (byte) 0, (byte) 1, (byte) 2, (byte) 3, (byte) 4, (byte) 5, (byte) 6, (byte) 7, (byte) 8, (byte) 9, (byte) 10, Ascii.VT, Ascii.FF, Ascii.CR, Ascii.SO, Ascii.SI, Ascii.DLE, (byte) 17, Ascii.DC2, (byte) 19, Ascii.DC4, Ascii.NAK, Ascii.SYN, Ascii.ETB, Ascii.CAN, Ascii.EM, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) 63, (byte) -9, Ascii.SUB, Ascii.ESC, Ascii.FS, Ascii.GS, Ascii.RS, Ascii.US, (byte) 32, (byte) 33, (byte) 34, (byte) 35, (byte) 36, (byte) 37, (byte) 38, (byte) 39, (byte) 40, (byte) 41, (byte) 42, (byte) 43, (byte) 44, (byte) 45, (byte) 46, (byte) 47, (byte) 48, (byte) 49, (byte) 50, (byte) 51, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9};
    private static final byte WHITE_SPACE_ENC = (byte) -5;

    public static class Base64DecoderException extends IllegalArgumentException {
        public Base64DecoderException(String s) {
            super(s);
        }
    }

    private Base64() {
    }

    public static byte[] getAlphabet() {
        return (byte[]) ALPHABET.clone();
    }

    public static byte[] getWebsafeAlphabet() {
        return (byte[]) WEBSAFE_ALPHABET.clone();
    }

    private static byte[] encode3to4(byte[] source, int srcOffset, int numSigBytes, byte[] destination, int destOffset, byte[] alphabet) {
        int inBuff = 0;
        int i = (numSigBytes > 0 ? (source[srcOffset] << 24) >>> 8 : 0) | (numSigBytes > 1 ? (source[srcOffset + 1] << 24) >>> 16 : 0);
        if (numSigBytes > 2) {
            inBuff = (source[srcOffset + 2] << 24) >>> 24;
        }
        inBuff |= i;
        switch (numSigBytes) {
            case 1:
                destination[destOffset] = alphabet[inBuff >>> 18];
                destination[destOffset + 1] = alphabet[(inBuff >>> 12) & 63];
                destination[destOffset + 2] = PADDING_BYTE;
                destination[destOffset + 3] = PADDING_BYTE;
                return destination;
            case 2:
                destination[destOffset] = alphabet[inBuff >>> 18];
                destination[destOffset + 1] = alphabet[(inBuff >>> 12) & 63];
                destination[destOffset + 2] = alphabet[(inBuff >>> 6) & 63];
                destination[destOffset + 3] = PADDING_BYTE;
                return destination;
            case 3:
                destination[destOffset] = alphabet[inBuff >>> 18];
                destination[destOffset + 1] = alphabet[(inBuff >>> 12) & 63];
                destination[destOffset + 2] = alphabet[(inBuff >>> 6) & 63];
                destination[destOffset + 3] = alphabet[inBuff & 63];
                return destination;
            default:
                return destination;
        }
    }

    @Deprecated
    public static String encode(byte[] source) {
        return encode(source, 0, source.length, ALPHABET, true);
    }

    public static String encode(byte[] source, boolean doPadding) {
        return encode(source, 0, source.length, ALPHABET, doPadding);
    }

    public static String encodeWebSafe(byte[] source, boolean doPadding) {
        return encode(source, 0, source.length, WEBSAFE_ALPHABET, doPadding);
    }

    private static String encode(byte[] source, int off, int len, byte[] alphabet, boolean doPadding) {
        byte[] outBuff = encode(source, off, len, alphabet, (int) 2147483647);
        int outLen = outBuff.length;
        while (!doPadding && outLen > 0 && outBuff[outLen - 1] == PADDING_BYTE) {
            outLen--;
        }
        return new String(outBuff, 0, outLen);
    }

    public static byte[] encode(byte[] source, int off, int len, byte[] alphabet, int maxLineLength) {
        int lineLength;
        int i = len;
        int i2 = maxLineLength;
        int len43 = ((i + 2) / 3) * 4;
        byte[] outBuff = new byte[((len43 / i2) + len43)];
        int len2 = i - 2;
        int lineLength2 = 0;
        int d = 0;
        int e = 0;
        while (true) {
            lineLength = lineLength2;
            if (d >= len2) {
                break;
            }
            int inBuff = (((source[d + off] << 24) >>> 8) | ((source[(d + 1) + off] << 24) >>> 16)) | ((source[(d + 2) + off] << 24) >>> 24);
            outBuff[e] = alphabet[inBuff >>> 18];
            outBuff[e + 1] = alphabet[(inBuff >>> 12) & 63];
            outBuff[e + 2] = alphabet[(inBuff >>> 6) & 63];
            outBuff[e + 3] = alphabet[inBuff & 63];
            int lineLength3 = lineLength + 4;
            if (lineLength3 == i2) {
                outBuff[e + 4] = (byte) 10;
                e++;
                lineLength2 = 0;
            } else {
                lineLength2 = lineLength3;
            }
            d += 3;
            e += 4;
        }
        if (d < i) {
            encode3to4(source, d + off, i - d, outBuff, e, alphabet);
            if (lineLength + 4 == i2) {
                outBuff[e + 4] = (byte) 10;
                e++;
            }
            e += 4;
        }
        return outBuff;
    }

    private static int decode4to3(byte[] source, int srcOffset, byte[] destination, int destOffset, byte[] decodabet) {
        int outBuff;
        if (source[srcOffset + 2] == PADDING_BYTE) {
            destination[destOffset] = (byte) ((((decodabet[source[srcOffset]] << 24) >>> 6) | ((decodabet[source[srcOffset + 1]] << 24) >>> 12)) >>> 16);
            return 1;
        } else if (source[srcOffset + 3] == PADDING_BYTE) {
            outBuff = (((decodabet[source[srcOffset]] << 24) >>> 6) | ((decodabet[source[srcOffset + 1]] << 24) >>> 12)) | ((decodabet[source[srcOffset + 2]] << 24) >>> 18);
            destination[destOffset] = (byte) (outBuff >>> 16);
            destination[destOffset + 1] = (byte) (outBuff >>> 8);
            return 2;
        } else {
            outBuff = ((((decodabet[source[srcOffset]] << 24) >>> 6) | ((decodabet[source[srcOffset + 1]] << 24) >>> 12)) | ((decodabet[source[srcOffset + 2]] << 24) >>> 18)) | ((decodabet[source[srcOffset + 3]] << 24) >>> 24);
            destination[destOffset] = (byte) (outBuff >> 16);
            destination[destOffset + 1] = (byte) (outBuff >> 8);
            destination[destOffset + 2] = (byte) outBuff;
            return 3;
        }
    }

    public static byte[] decode(String s) throws Base64DecoderException {
        byte[] bytes = s.getBytes();
        return decode(bytes, 0, bytes.length);
    }

    public static byte[] decodeWebSafe(String s) throws Base64DecoderException {
        byte[] bytes = s.getBytes();
        return decodeWebSafe(bytes, 0, bytes.length);
    }

    public static byte[] decode(byte[] source) throws Base64DecoderException {
        return decode(source, 0, source.length);
    }

    public static byte[] decodeWebSafe(byte[] source) throws Base64DecoderException {
        return decodeWebSafe(source, 0, source.length);
    }

    public static byte[] decode(byte[] source, int off, int len) throws Base64DecoderException {
        return decode(source, off, len, DECODABET);
    }

    public static byte[] decodeWebSafe(byte[] source, int off, int len) throws Base64DecoderException {
        return decode(source, off, len, WEBSAFE_DECODABET);
    }

    private static byte[] decode(byte[] source, int off, int len, byte[] decodabet) throws Base64DecoderException {
        StringBuilder stringBuilder;
        int i = len;
        byte[] bArr = decodabet;
        byte[] outBuff = new byte[(2 + ((i * 3) / 4))];
        int outBuffPosn = 0;
        byte[] b4 = new byte[4];
        int b4Posn = 0;
        boolean paddingByteSeen = false;
        int i2 = 0;
        while (i2 < i) {
            byte sbiCrop = (byte) (source[i2 + off] & 127);
            byte sbiDecode = bArr[sbiCrop];
            if (sbiDecode >= WHITE_SPACE_ENC) {
                Object obj;
                Object obj2;
                if (sbiDecode >= (byte) -1) {
                    if (sbiCrop == PADDING_BYTE) {
                        if (!paddingByteSeen) {
                            if (i2 >= 2) {
                                byte lastByte = (byte) (source[(i - 1) + off] & 127);
                                if (lastByte == PADDING_BYTE || lastByte == (byte) 10) {
                                    paddingByteSeen = true;
                                } else {
                                    throw new Base64DecoderException("encoded value has invalid trailing byte");
                                }
                            }
                            stringBuilder = new StringBuilder();
                            stringBuilder.append("Invalid padding byte found in position ");
                            stringBuilder.append(i2);
                            throw new Base64DecoderException(stringBuilder.toString());
                        }
                    } else if (paddingByteSeen) {
                        stringBuilder = new StringBuilder();
                        stringBuilder.append("Data found after trailing padding byte at index ");
                        stringBuilder.append(i2);
                        throw new Base64DecoderException(stringBuilder.toString());
                    } else {
                        int b4Posn2 = b4Posn + 1;
                        b4[b4Posn] = sbiCrop;
                        obj = 4;
                        if (b4Posn2 == 4) {
                            outBuffPosn += decode4to3(b4, 0, outBuff, outBuffPosn, bArr);
                            b4Posn2 = 0;
                        }
                        b4Posn = b4Posn2;
                        i2++;
                        obj2 = obj;
                    }
                }
                obj = 4;
                i2++;
                obj2 = obj;
            } else {
                stringBuilder = new StringBuilder();
                stringBuilder.append("Bad Base64 input character at ");
                stringBuilder.append(i2);
                stringBuilder.append(": ");
                stringBuilder.append(source[i2 + off]);
                stringBuilder.append("(decimal)");
                throw new Base64DecoderException(stringBuilder.toString());
            }
        }
        if (b4Posn != 0) {
            if (b4Posn != 1) {
                b4[b4Posn] = PADDING_BYTE;
                outBuffPosn += decode4to3(b4, 0, outBuff, outBuffPosn, bArr);
            } else {
                stringBuilder = new StringBuilder();
                stringBuilder.append("single trailing character at offset ");
                stringBuilder.append(i - 1);
                throw new Base64DecoderException(stringBuilder.toString());
            }
        }
        byte[] out = new byte[outBuffPosn];
        System.arraycopy(outBuff, 0, out, 0, outBuffPosn);
        return out;
    }
}
