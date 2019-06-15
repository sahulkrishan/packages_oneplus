package androidx.slice;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build.VERSION;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v4.graphics.drawable.IconCompat;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Base64;
import androidx.slice.Slice.Builder;
import androidx.slice.Slice.SliceHint;
import androidx.slice.SliceUtils.SerializeOptions;
import androidx.slice.SliceUtils.SliceActionListener;
import androidx.slice.SliceUtils.SliceParseException;
import com.android.settingslib.SliceBroadcastRelay;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

@RestrictTo({Scope.LIBRARY})
class SliceXml {
    private static final String ATTR_FORMAT = "format";
    private static final String ATTR_HINTS = "hints";
    private static final String ATTR_ICON_PACKAGE = "pkg";
    private static final String ATTR_ICON_RES_TYPE = "resType";
    private static final String ATTR_ICON_TYPE = "iconType";
    private static final String ATTR_SUBTYPE = "subtype";
    private static final String ATTR_URI = "uri";
    private static final String ICON_TYPE_DEFAULT = "def";
    private static final String ICON_TYPE_RES = "res";
    private static final String ICON_TYPE_URI = "uri";
    private static final String NAMESPACE = null;
    private static final String TAG_ACTION = "action";
    private static final String TAG_ITEM = "item";
    private static final String TAG_SLICE = "slice";

    public static Slice parseSlice(Context context, InputStream input, String encoding, SliceActionListener listener) throws IOException, SliceParseException {
        try {
            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
            parser.setInput(input, encoding);
            int outerDepth = parser.getDepth();
            Slice s = null;
            while (true) {
                int next = parser.next();
                int type = next;
                if (next == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                    return s;
                }
                if (type == 2) {
                    s = parseSlice(context, parser, listener);
                }
            }
            return s;
        } catch (XmlPullParserException e) {
            throw new IOException("Unable to init XML Serialization", e);
        }
    }

    @SuppressLint({"WrongConstant"})
    private static Slice parseSlice(Context context, XmlPullParser parser, SliceActionListener listener) throws IOException, XmlPullParserException, SliceParseException {
        if ("slice".equals(parser.getName()) || TAG_ACTION.equals(parser.getName())) {
            int outerDepth = parser.getDepth();
            Builder b = new Builder(Uri.parse(parser.getAttributeValue(NAMESPACE, SliceBroadcastRelay.EXTRA_URI)));
            b.addHints(hints(parser.getAttributeValue(NAMESPACE, ATTR_HINTS)));
            while (true) {
                int next = parser.next();
                int type = next;
                if (next != 1 && (type != 3 || parser.getDepth() > outerDepth)) {
                    if (type == 2 && "item".equals(parser.getName())) {
                        parseItem(context, b, parser, listener);
                    }
                }
            }
            return b.build();
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Unexpected tag ");
        stringBuilder.append(parser.getName());
        throw new IOException(stringBuilder.toString());
    }

    @android.annotation.SuppressLint({"DefaultCharset"})
    private static void parseItem(android.content.Context r23, androidx.slice.Slice.Builder r24, org.xmlpull.v1.XmlPullParser r25, androidx.slice.SliceUtils.SliceActionListener r26) throws java.io.IOException, org.xmlpull.v1.XmlPullParserException, androidx.slice.SliceUtils.SliceParseException {
        /*
        r1 = r23;
        r2 = r24;
        r3 = r25;
        r4 = r26;
        r5 = r25.getDepth();
        r0 = NAMESPACE;
        r6 = "format";
        r6 = r3.getAttributeValue(r0, r6);
        r0 = NAMESPACE;
        r7 = "subtype";
        r7 = r3.getAttributeValue(r0, r7);
        r0 = NAMESPACE;
        r8 = "hints";
        r8 = r3.getAttributeValue(r0, r8);
        r0 = NAMESPACE;
        r9 = "iconType";
        r9 = r3.getAttributeValue(r0, r9);
        r0 = NAMESPACE;
        r10 = "pkg";
        r10 = r3.getAttributeValue(r0, r10);
        r0 = NAMESPACE;
        r11 = "resType";
        r11 = r3.getAttributeValue(r0, r11);
        r0 = hints(r8);
    L_0x0040:
        r12 = r0;
        r0 = r25.next();
        r13 = r0;
        r14 = 1;
        if (r0 == r14) goto L_0x020c;
    L_0x0049:
        r0 = 3;
        if (r13 != r0) goto L_0x0059;
    L_0x004c:
        r15 = r25.getDepth();
        if (r15 <= r5) goto L_0x0053;
    L_0x0052:
        goto L_0x0059;
    L_0x0053:
        r21 = r5;
        r22 = r8;
        goto L_0x0210;
    L_0x0059:
        r15 = 4;
        if (r13 != r15) goto L_0x01cf;
    L_0x005c:
        r17 = r6.hashCode();
        r18 = -1;
        switch(r17) {
            case 104431: goto L_0x0092;
            case 3327612: goto L_0x0087;
            case 3556653: goto L_0x007c;
            case 100313435: goto L_0x0071;
            case 100358090: goto L_0x0066;
            default: goto L_0x0065;
        };
    L_0x0065:
        goto L_0x009d;
    L_0x0066:
        r15 = "input";
        r15 = r6.equals(r15);
        if (r15 == 0) goto L_0x009d;
    L_0x006e:
        r16 = 0;
        goto L_0x009f;
    L_0x0071:
        r15 = "image";
        r15 = r6.equals(r15);
        if (r15 == 0) goto L_0x009d;
    L_0x0079:
        r16 = 1;
        goto L_0x009f;
    L_0x007c:
        r15 = "text";
        r15 = r6.equals(r15);
        if (r15 == 0) goto L_0x009d;
    L_0x0084:
        r16 = 3;
        goto L_0x009f;
    L_0x0087:
        r15 = "long";
        r15 = r6.equals(r15);
        if (r15 == 0) goto L_0x009d;
    L_0x008f:
        r16 = 4;
        goto L_0x009f;
    L_0x0092:
        r15 = "int";
        r15 = r6.equals(r15);
        if (r15 == 0) goto L_0x009d;
    L_0x009a:
        r16 = 2;
        goto L_0x009f;
    L_0x009d:
        r16 = r18;
    L_0x009f:
        switch(r16) {
            case 0: goto L_0x01ca;
            case 1: goto L_0x00f8;
            case 2: goto L_0x00e6;
            case 3: goto L_0x00c9;
            case 4: goto L_0x00bd;
            default: goto L_0x00a2;
        };
    L_0x00a2:
        r21 = r5;
        r22 = r8;
        r0 = new java.lang.IllegalArgumentException;
        r5 = new java.lang.StringBuilder;
        r5.<init>();
        r8 = "Unrecognized format ";
        r5.append(r8);
        r5.append(r6);
        r5 = r5.toString();
        r0.<init>(r5);
        throw r0;
    L_0x00bd:
        r0 = r25.getText();
        r14 = java.lang.Long.parseLong(r0);
        r2.addLong(r14, r7, r12);
        goto L_0x00f2;
    L_0x00c9:
        r14 = r25.getText();
        r15 = android.os.Build.VERSION.SDK_INT;
        r0 = 22;
        if (r15 >= r0) goto L_0x00de;
    L_0x00d3:
        r0 = new java.lang.String;
        r15 = 2;
        r15 = android.util.Base64.decode(r14, r15);
        r0.<init>(r15);
        r14 = r0;
    L_0x00de:
        r0 = android.text.Html.fromHtml(r14);
        r2.addText(r0, r7, r12);
        goto L_0x00f2;
    L_0x00e6:
        r0 = r25.getText();
        r14 = java.lang.Integer.parseInt(r0);
        r2.addInt(r14, r7, r12);
    L_0x00f2:
        r21 = r5;
        r22 = r8;
        goto L_0x0205;
    L_0x00f8:
        r0 = r9.hashCode();
        r15 = 112800; // 0x1b8a0 float:1.58066E-40 double:5.57306E-319;
        if (r0 == r15) goto L_0x0112;
    L_0x0101:
        r15 = 116076; // 0x1c56c float:1.62657E-40 double:5.7349E-319;
        if (r0 == r15) goto L_0x0107;
    L_0x0106:
        goto L_0x011c;
    L_0x0107:
        r0 = "uri";
        r0 = r9.equals(r0);
        if (r0 == 0) goto L_0x011c;
    L_0x010f:
        r18 = 1;
        goto L_0x011c;
    L_0x0112:
        r0 = "res";
        r0 = r9.equals(r0);
        if (r0 == 0) goto L_0x011c;
    L_0x011a:
        r18 = 0;
    L_0x011c:
        switch(r18) {
            case 0: goto L_0x014d;
            case 1: goto L_0x013b;
            default: goto L_0x011f;
        };
    L_0x011f:
        r21 = r5;
        r22 = r8;
        r0 = r25.getText();
        r5 = 2;
        r5 = android.util.Base64.decode(r0, r5);
        r8 = r5.length;
        r14 = 0;
        r8 = android.graphics.BitmapFactory.decodeByteArray(r5, r14, r8);
        r14 = android.support.v4.graphics.drawable.IconCompat.createWithBitmap(r8);
        r2.addIcon(r14, r7, r12);
        goto L_0x01c9;
    L_0x013b:
        r0 = r25.getText();
        r14 = android.support.v4.graphics.drawable.IconCompat.createWithContentUri(r0);
        r2.addIcon(r14, r7, r12);
    L_0x0147:
        r21 = r5;
        r22 = r8;
        goto L_0x01c9;
    L_0x014d:
        r0 = r25.getText();
        r15 = r0;
        r0 = r23.getPackageManager();	 Catch:{ NameNotFoundException -> 0x01ad }
        r0 = r0.getResourcesForApplication(r10);	 Catch:{ NameNotFoundException -> 0x01ad }
        r16 = r0.getIdentifier(r15, r11, r10);	 Catch:{ NameNotFoundException -> 0x01ad }
        r19 = r16;
        r14 = r19;
        if (r14 == 0) goto L_0x017a;
        r20 = r0;
        r0 = 0;
        r0 = r1.createPackageContext(r10, r0);	 Catch:{ NameNotFoundException -> 0x0174 }
        r0 = android.support.v4.graphics.drawable.IconCompat.createWithResource(r0, r14);	 Catch:{ NameNotFoundException -> 0x0174 }
        r2.addIcon(r0, r7, r12);	 Catch:{ NameNotFoundException -> 0x0174 }
        goto L_0x0147;
    L_0x0174:
        r0 = move-exception;
        r21 = r5;
        r22 = r8;
        goto L_0x01b2;
    L_0x017a:
        r20 = r0;
        r0 = new androidx.slice.SliceUtils$SliceParseException;	 Catch:{ NameNotFoundException -> 0x01ad }
        r21 = r5;
        r5 = new java.lang.StringBuilder;	 Catch:{ NameNotFoundException -> 0x01a9 }
        r5.<init>();	 Catch:{ NameNotFoundException -> 0x01a9 }
        r22 = r8;
        r8 = "Cannot find resource ";
        r5.append(r8);	 Catch:{ NameNotFoundException -> 0x01a7 }
        r5.append(r10);	 Catch:{ NameNotFoundException -> 0x01a7 }
        r8 = ":";
        r5.append(r8);	 Catch:{ NameNotFoundException -> 0x01a7 }
        r5.append(r11);	 Catch:{ NameNotFoundException -> 0x01a7 }
        r8 = "/";
        r5.append(r8);	 Catch:{ NameNotFoundException -> 0x01a7 }
        r5.append(r15);	 Catch:{ NameNotFoundException -> 0x01a7 }
        r5 = r5.toString();	 Catch:{ NameNotFoundException -> 0x01a7 }
        r0.<init>(r5);	 Catch:{ NameNotFoundException -> 0x01a7 }
        throw r0;	 Catch:{ NameNotFoundException -> 0x01a7 }
    L_0x01a7:
        r0 = move-exception;
        goto L_0x01b2;
    L_0x01a9:
        r0 = move-exception;
        r22 = r8;
        goto L_0x01b2;
    L_0x01ad:
        r0 = move-exception;
        r21 = r5;
        r22 = r8;
    L_0x01b2:
        r5 = new androidx.slice.SliceUtils$SliceParseException;
        r8 = new java.lang.StringBuilder;
        r8.<init>();
        r14 = "Invalid icon package ";
        r8.append(r14);
        r8.append(r10);
        r8 = r8.toString();
        r5.<init>(r8, r0);
        throw r5;
    L_0x01c9:
        goto L_0x0205;
    L_0x01ca:
        r21 = r5;
        r22 = r8;
        goto L_0x0205;
    L_0x01cf:
        r21 = r5;
        r22 = r8;
        r0 = 2;
        if (r13 != r0) goto L_0x01ea;
    L_0x01d6:
        r0 = "slice";
        r5 = r25.getName();
        r0 = r0.equals(r5);
        if (r0 == 0) goto L_0x01ea;
    L_0x01e2:
        r0 = parseSlice(r1, r3, r4);
        r2.addSubSlice(r0, r7);
        goto L_0x0205;
    L_0x01ea:
        r0 = 2;
        if (r13 != r0) goto L_0x0205;
    L_0x01ed:
        r0 = "action";
        r5 = r25.getName();
        r0 = r0.equals(r5);
        if (r0 == 0) goto L_0x0205;
    L_0x01f9:
        r0 = new androidx.slice.SliceXml$1;
        r0.<init>(r4);
        r5 = parseSlice(r1, r3, r4);
        r2.addAction(r0, r5, r7);
    L_0x0205:
        r0 = r12;
        r5 = r21;
        r8 = r22;
        goto L_0x0040;
    L_0x020c:
        r21 = r5;
        r22 = r8;
    L_0x0210:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.slice.SliceXml.parseItem(android.content.Context, androidx.slice.Slice$Builder, org.xmlpull.v1.XmlPullParser, androidx.slice.SliceUtils$SliceActionListener):void");
    }

    @SliceHint
    private static String[] hints(String hintStr) {
        return TextUtils.isEmpty(hintStr) ? new String[0] : hintStr.split(",");
    }

    public static void serializeSlice(Slice s, Context context, OutputStream output, String encoding, SerializeOptions options) throws IOException {
        try {
            XmlSerializer serializer = XmlPullParserFactory.newInstance().newSerializer();
            serializer.setOutput(output, encoding);
            serializer.startDocument(encoding, null);
            serialize(s, context, options, serializer, false, null);
            serializer.endDocument();
            serializer.flush();
        } catch (XmlPullParserException e) {
            throw new IOException("Unable to init XML Serialization", e);
        }
    }

    private static void serialize(Slice s, Context context, SerializeOptions options, XmlSerializer serializer, boolean isAction, String subType) throws IOException {
        serializer.startTag(NAMESPACE, isAction ? TAG_ACTION : "slice");
        serializer.attribute(NAMESPACE, SliceBroadcastRelay.EXTRA_URI, s.getUri().toString());
        if (subType != null) {
            serializer.attribute(NAMESPACE, ATTR_SUBTYPE, subType);
        }
        if (!s.getHints().isEmpty()) {
            serializer.attribute(NAMESPACE, ATTR_HINTS, hintStr(s.getHints()));
        }
        for (SliceItem item : s.getItems()) {
            serialize(item, context, options, serializer);
        }
        serializer.endTag(NAMESPACE, isAction ? TAG_ACTION : "slice");
    }

    private static void serialize(SliceItem item, Context context, SerializeOptions options, XmlSerializer serializer) throws IOException {
        String format = item.getFormat();
        options.checkThrow(format);
        serializer.startTag(NAMESPACE, "item");
        serializer.attribute(NAMESPACE, ATTR_FORMAT, format);
        if (item.getSubType() != null) {
            serializer.attribute(NAMESPACE, ATTR_SUBTYPE, item.getSubType());
        }
        if (!item.getHints().isEmpty()) {
            serializer.attribute(NAMESPACE, ATTR_HINTS, hintStr(item.getHints()));
        }
        int i = -1;
        switch (format.hashCode()) {
            case -1422950858:
                if (format.equals(TAG_ACTION)) {
                    i = 0;
                    break;
                }
                break;
            case 104431:
                if (format.equals("int")) {
                    i = 3;
                    break;
                }
                break;
            case 3327612:
                if (format.equals("long")) {
                    i = 6;
                    break;
                }
                break;
            case 3556653:
                if (format.equals("text")) {
                    i = 5;
                    break;
                }
                break;
            case 100313435:
                if (format.equals("image")) {
                    i = 2;
                    break;
                }
                break;
            case 100358090:
                if (format.equals("input")) {
                    i = 1;
                    break;
                }
                break;
            case 109526418:
                if (format.equals("slice")) {
                    i = 4;
                    break;
                }
                break;
        }
        StringBuilder stringBuilder;
        switch (i) {
            case 0:
                if (options.getActionMode() == 2) {
                    serialize(item.getSlice(), context, options, serializer, true, item.getSubType());
                    break;
                } else if (options.getActionMode() == 0) {
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("Slice contains an action ");
                    stringBuilder.append(item);
                    throw new IllegalArgumentException(stringBuilder.toString());
                }
                break;
            case 1:
                break;
            case 2:
                if (options.getImageMode() == 2) {
                    IconCompat icon = item.getIcon();
                    int type = icon.getType();
                    if (type != 2) {
                        if (type == 4) {
                            if (!"file".equals(icon.getUri().getScheme())) {
                                serializeIcon(serializer, icon, context, options);
                                break;
                            } else {
                                serializeFileIcon(serializer, icon, context);
                                break;
                            }
                        }
                        serializeIcon(serializer, icon, context, options);
                        break;
                    }
                    serializeResIcon(serializer, icon, context);
                    break;
                } else if (options.getImageMode() == 0) {
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("Slice contains an image ");
                    stringBuilder.append(item);
                    throw new IllegalArgumentException(stringBuilder.toString());
                }
                break;
            case 3:
                serializer.text(String.valueOf(item.getInt()));
                break;
            case 4:
                serialize(item.getSlice(), context, options, serializer, false, item.getSubType());
                break;
            case 5:
                String text;
                if (!(item.getText() instanceof Spanned)) {
                    text = String.valueOf(item.getText());
                    if (VERSION.SDK_INT < 22) {
                        text = Base64.encodeToString(text.getBytes(StandardCharsets.UTF_8), 2);
                    }
                    serializer.text(text);
                    break;
                }
                text = Html.toHtml((Spanned) item.getText());
                if (VERSION.SDK_INT < 22) {
                    text = Base64.encodeToString(text.getBytes(StandardCharsets.UTF_8), 2);
                }
                serializer.text(text);
                break;
            case 6:
                serializer.text(String.valueOf(item.getLong()));
                break;
            default:
                stringBuilder = new StringBuilder();
                stringBuilder.append("Unrecognized format ");
                stringBuilder.append(format);
                throw new IllegalArgumentException(stringBuilder.toString());
        }
        serializer.endTag(NAMESPACE, "item");
    }

    private static void serializeResIcon(XmlSerializer serializer, IconCompat icon, Context context) throws IOException {
        try {
            Resources res = context.getPackageManager().getResourcesForApplication(icon.getResPackage());
            int id = icon.getResId();
            serializer.attribute(NAMESPACE, ATTR_ICON_TYPE, ICON_TYPE_RES);
            serializer.attribute(NAMESPACE, "pkg", res.getResourcePackageName(id));
            serializer.attribute(NAMESPACE, ATTR_ICON_RES_TYPE, res.getResourceTypeName(id));
            serializer.text(res.getResourceEntryName(id));
        } catch (NameNotFoundException e) {
            throw new IllegalArgumentException("Slice contains invalid icon", e);
        }
    }

    private static void serializeFileIcon(XmlSerializer serializer, IconCompat icon, Context context) throws IOException {
        serializer.attribute(NAMESPACE, ATTR_ICON_TYPE, SliceBroadcastRelay.EXTRA_URI);
        serializer.text(icon.getUri().toString());
    }

    private static void serializeIcon(XmlSerializer serializer, IconCompat icon, Context context, SerializeOptions options) throws IOException {
        byte[] outputStream = convertToBytes(icon, context, options);
        serializer.attribute(NAMESPACE, ATTR_ICON_TYPE, ICON_TYPE_DEFAULT);
        serializer.text(new String(Base64.encode(outputStream, 2), StandardCharsets.UTF_8));
    }

    public static byte[] convertToBytes(IconCompat icon, Context context, SerializeOptions options) {
        Drawable d = icon.loadDrawable(context);
        int width = d.getIntrinsicWidth();
        int height = d.getIntrinsicHeight();
        if (width > options.getMaxWidth()) {
            height = (int) (((double) (options.getMaxWidth() * height)) / ((double) width));
            width = options.getMaxWidth();
        }
        if (height > options.getMaxHeight()) {
            width = (int) (((double) (options.getMaxHeight() * width)) / ((double) height));
            height = options.getMaxHeight();
        }
        Bitmap b = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas c = new Canvas(b);
        d.setBounds(0, 0, c.getWidth(), c.getHeight());
        d.draw(c);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        b.compress(options.getFormat(), options.getQuality(), outputStream);
        b.recycle();
        return outputStream.toByteArray();
    }

    private static String hintStr(List<String> hints) {
        return TextUtils.join(",", hints);
    }

    private SliceXml() {
    }
}
