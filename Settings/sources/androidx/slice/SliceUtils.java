package androidx.slice;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v4.graphics.drawable.IconCompat;
import android.support.v4.util.Pair;
import android.text.TextUtils;
import androidx.slice.Slice.Builder;
import androidx.slice.SliceItem.ActionHandler;
import androidx.slice.core.SliceQuery;
import androidx.versionedparcelable.ParcelUtils;
import com.android.settings.inputmethod.UserDictionaryAddWordContents;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SliceUtils {
    @Deprecated
    public static final int LOADING_ALL = 0;
    @Deprecated
    public static final int LOADING_COMPLETE = 2;
    @Deprecated
    public static final int LOADING_PARTIAL = 1;

    public static class SerializeOptions {
        public static final int MODE_CONVERT = 2;
        public static final int MODE_REMOVE = 1;
        public static final int MODE_THROW = 0;
        private int mActionMode = 0;
        private CompressFormat mFormat = CompressFormat.PNG;
        private int mImageMode = 0;
        private int mMaxHeight = 1000;
        private int mMaxWidth = 1000;
        private int mQuality = 100;

        /* JADX WARNING: Removed duplicated region for block: B:17:0x0036 A:{RETURN} */
        /* JADX WARNING: Removed duplicated region for block: B:21:0x003c  */
        /* JADX WARNING: Removed duplicated region for block: B:18:0x0037  */
        /* JADX WARNING: Removed duplicated region for block: B:17:0x0036 A:{RETURN} */
        /* JADX WARNING: Removed duplicated region for block: B:21:0x003c  */
        /* JADX WARNING: Removed duplicated region for block: B:18:0x0037  */
        /* JADX WARNING: Removed duplicated region for block: B:17:0x0036 A:{RETURN} */
        /* JADX WARNING: Removed duplicated region for block: B:21:0x003c  */
        /* JADX WARNING: Removed duplicated region for block: B:18:0x0037  */
        @android.support.annotation.RestrictTo({android.support.annotation.RestrictTo.Scope.LIBRARY})
        public void checkThrow(java.lang.String r4) {
            /*
            r3 = this;
            r0 = r4.hashCode();
            r1 = -1422950858; // 0xffffffffab2f7e36 float:-6.234764E-13 double:NaN;
            if (r0 == r1) goto L_0x0028;
        L_0x0009:
            r1 = 100313435; // 0x5faa95b float:2.3572098E-35 double:4.9561422E-316;
            if (r0 == r1) goto L_0x001e;
        L_0x000e:
            r1 = 100358090; // 0x5fb57ca float:2.3636175E-35 double:4.95834846E-316;
            if (r0 == r1) goto L_0x0014;
        L_0x0013:
            goto L_0x0032;
        L_0x0014:
            r0 = "input";
            r0 = r4.equals(r0);
            if (r0 == 0) goto L_0x0032;
        L_0x001c:
            r0 = 1;
            goto L_0x0033;
        L_0x001e:
            r0 = "image";
            r0 = r4.equals(r0);
            if (r0 == 0) goto L_0x0032;
        L_0x0026:
            r0 = 2;
            goto L_0x0033;
        L_0x0028:
            r0 = "action";
            r0 = r4.equals(r0);
            if (r0 == 0) goto L_0x0032;
        L_0x0030:
            r0 = 0;
            goto L_0x0033;
        L_0x0032:
            r0 = -1;
        L_0x0033:
            switch(r0) {
                case 0: goto L_0x003c;
                case 1: goto L_0x003c;
                case 2: goto L_0x0037;
                default: goto L_0x0036;
            };
        L_0x0036:
            return;
        L_0x0037:
            r0 = r3.mImageMode;
            if (r0 == 0) goto L_0x0041;
        L_0x003b:
            return;
        L_0x003c:
            r0 = r3.mActionMode;
            if (r0 == 0) goto L_0x0041;
        L_0x0040:
            return;
        L_0x0041:
            r0 = new java.lang.IllegalArgumentException;
            r1 = new java.lang.StringBuilder;
            r1.<init>();
            r1.append(r4);
            r2 = " cannot be serialized";
            r1.append(r2);
            r1 = r1.toString();
            r0.<init>(r1);
            throw r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: androidx.slice.SliceUtils$SerializeOptions.checkThrow(java.lang.String):void");
        }

        @RestrictTo({Scope.LIBRARY})
        public int getActionMode() {
            return this.mActionMode;
        }

        @RestrictTo({Scope.LIBRARY})
        public int getImageMode() {
            return this.mImageMode;
        }

        @RestrictTo({Scope.LIBRARY})
        public int getMaxWidth() {
            return this.mMaxWidth;
        }

        @RestrictTo({Scope.LIBRARY})
        public int getMaxHeight() {
            return this.mMaxHeight;
        }

        @RestrictTo({Scope.LIBRARY})
        public CompressFormat getFormat() {
            return this.mFormat;
        }

        @RestrictTo({Scope.LIBRARY})
        public int getQuality() {
            return this.mQuality;
        }

        public SerializeOptions setActionMode(int mode) {
            this.mActionMode = mode;
            return this;
        }

        public SerializeOptions setImageMode(int mode) {
            this.mImageMode = mode;
            return this;
        }

        public SerializeOptions setMaxImageWidth(int width) {
            this.mMaxWidth = width;
            return this;
        }

        public SerializeOptions setMaxImageHeight(int height) {
            this.mMaxHeight = height;
            return this;
        }

        public SerializeOptions setImageConversionFormat(CompressFormat format, int quality) {
            this.mFormat = format;
            this.mQuality = quality;
            return this;
        }
    }

    public interface SliceActionListener {
        void onSliceAction(Uri uri, Context context, Intent intent);
    }

    public static class SliceParseException extends Exception {
        @RestrictTo({Scope.LIBRARY})
        public SliceParseException(String s, Throwable e) {
            super(s, e);
        }

        @RestrictTo({Scope.LIBRARY})
        public SliceParseException(String s) {
            super(s);
        }
    }

    private SliceUtils() {
    }

    @Deprecated
    public static void serializeSlice(@NonNull Slice s, @NonNull Context context, @NonNull OutputStream output, @NonNull String encoding, @NonNull SerializeOptions options) throws IOException, IllegalArgumentException {
        serializeSlice(s, context, output, options);
    }

    public static void serializeSlice(@NonNull Slice s, @NonNull Context context, @NonNull OutputStream output, @NonNull SerializeOptions options) throws IllegalArgumentException {
        ParcelUtils.toOutputStream(convert(context, s, options), output);
    }

    @SuppressLint({"NewApi"})
    @RestrictTo({Scope.LIBRARY})
    public static Slice convert(Context context, Slice slice, SerializeOptions options) {
        Builder builder = new Builder(slice.getUri());
        builder.setSpec(slice.getSpec());
        builder.addHints(slice.getHints());
        for (SliceItem item : slice.getItems()) {
            String format = item.getFormat();
            Object obj = -1;
            switch (format.hashCode()) {
                case -1422950858:
                    if (format.equals("action")) {
                        obj = 3;
                        break;
                    }
                    break;
                case 104431:
                    if (format.equals("int")) {
                        obj = 5;
                        break;
                    }
                    break;
                case 3327612:
                    if (format.equals("long")) {
                        obj = 6;
                        break;
                    }
                    break;
                case 3556653:
                    if (format.equals("text")) {
                        obj = 4;
                        break;
                    }
                    break;
                case 100313435:
                    if (format.equals("image")) {
                        obj = 1;
                        break;
                    }
                    break;
                case 100358090:
                    if (format.equals("input")) {
                        obj = 2;
                        break;
                    }
                    break;
                case 109526418:
                    if (format.equals("slice")) {
                        obj = null;
                        break;
                    }
                    break;
            }
            switch (obj) {
                case null:
                    builder.addSubSlice(convert(context, item.getSlice(), options), item.getSubType());
                    break;
                case 1:
                    switch (options.getImageMode()) {
                        case 0:
                            throw new IllegalArgumentException("Cannot serialize icon");
                        case 2:
                            builder.addIcon(convert(context, item.getIcon(), options), item.getSubType(), item.getHints());
                            break;
                        default:
                            break;
                    }
                case 2:
                    if (options.getActionMode() != 0) {
                        break;
                    }
                    builder.addRemoteInput(item.getRemoteInput(), item.getSubType(), item.getHints());
                    break;
                case 3:
                    switch (options.getActionMode()) {
                        case 0:
                            throw new IllegalArgumentException("Cannot serialize action");
                        case 2:
                            builder.addAction(new ActionHandler() {
                                public void onAction(SliceItem item, Context context, Intent intent) {
                                }
                            }, convert(context, item.getSlice(), options), item.getSubType());
                            break;
                        default:
                            break;
                    }
                case 4:
                    builder.addText(item.getText(), item.getSubType(), item.getHints());
                    break;
                case 5:
                    builder.addInt(item.getInt(), item.getSubType(), item.getHints());
                    break;
                case 6:
                    builder.addLong(item.getLong(), item.getSubType(), item.getHints());
                    break;
                default:
                    break;
            }
        }
        return builder.build();
    }

    @RestrictTo({Scope.LIBRARY})
    public static IconCompat convert(Context context, IconCompat icon, SerializeOptions options) {
        if (icon.getType() == 2) {
            return icon;
        }
        byte[] data = SliceXml.convertToBytes(icon, context, options);
        return IconCompat.createWithData(data, 0, data.length);
    }

    @NonNull
    public static Slice parseSlice(@NonNull Context context, @NonNull InputStream input, @NonNull String encoding, @NonNull final SliceActionListener listener) throws IOException, SliceParseException {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(input);
        String parcelName = Slice.class.getName();
        bufferedInputStream.mark(parcelName.length() + 4);
        boolean usesParcel = doesStreamStartWith(parcelName, bufferedInputStream);
        bufferedInputStream.reset();
        if (!usesParcel) {
            return SliceXml.parseSlice(context, bufferedInputStream, encoding, listener);
        }
        Slice slice = (Slice) ParcelUtils.fromInputStream(bufferedInputStream);
        setActions(slice, new ActionHandler() {
            public void onAction(SliceItem item, Context context, Intent intent) {
                listener.onSliceAction(item.getSlice().getUri(), context, intent);
            }
        });
        return slice;
    }

    private static void setActions(Slice slice, ActionHandler listener) {
        for (SliceItem sliceItem : slice.getItems()) {
            String format = sliceItem.getFormat();
            Object obj = -1;
            int hashCode = format.hashCode();
            if (hashCode != -1422950858) {
                if (hashCode == 109526418 && format.equals("slice")) {
                    obj = 1;
                }
            } else if (format.equals("action")) {
                obj = null;
            }
            switch (obj) {
                case null:
                    sliceItem.mObj = new Pair(listener, ((Pair) sliceItem.mObj).second);
                    setActions(sliceItem.getSlice(), listener);
                    break;
                case 1:
                    setActions(sliceItem.getSlice(), listener);
                    break;
                default:
                    break;
            }
        }
    }

    private static boolean doesStreamStartWith(String parcelName, BufferedInputStream inputStream) {
        byte[] data = parcelName.getBytes(Charset.forName("UTF-16"));
        byte[] buf = new byte[data.length];
        try {
            if (inputStream.read(buf, 0, 4) >= 0 && inputStream.read(buf, 0, buf.length) >= 0) {
                return Arrays.equals(buf, data);
            }
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    @Deprecated
    public static int getLoadingState(@NonNull Slice slice) {
        boolean hasHintPartial = SliceQuery.find(slice, null, "partial", null) != null;
        if (slice.getItems().size() == 0) {
            return 0;
        }
        if (hasHintPartial) {
            return 1;
        }
        return 2;
    }

    @Nullable
    @Deprecated
    public static List<SliceItem> getSliceActions(@NonNull Slice slice) {
        SliceItem actionGroup = SliceQuery.find(slice, "slice", "actions", null);
        String[] hints = new String[]{"actions", UserDictionaryAddWordContents.EXTRA_SHORTCUT};
        if (actionGroup != null) {
            return SliceQuery.findAll(actionGroup, "slice", hints, null);
        }
        return null;
    }

    @Nullable
    @Deprecated
    public static List<String> getSliceKeywords(@NonNull Slice slice) {
        SliceItem keywordGroup = SliceQuery.find(slice, "slice", "keywords", null);
        if (keywordGroup != null) {
            List<SliceItem> itemList = SliceQuery.findAll(keywordGroup, "text");
            if (itemList != null) {
                ArrayList<String> stringList = new ArrayList();
                for (int i = 0; i < itemList.size(); i++) {
                    String keyword = (String) ((SliceItem) itemList.get(i)).getText();
                    if (!TextUtils.isEmpty(keyword)) {
                        stringList.add(keyword);
                    }
                }
                return stringList;
            }
        }
        return null;
    }
}
