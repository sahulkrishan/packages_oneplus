package androidx.slice;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.app.RemoteInput;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v4.graphics.drawable.IconCompat;
import android.support.v4.util.Pair;
import android.text.TextUtils;
import androidx.slice.Slice.SliceHint;
import androidx.versionedparcelable.CustomVersionedParcelable;
import java.util.Arrays;
import java.util.List;

public final class SliceItem extends CustomVersionedParcelable {
    private static final String FORMAT = "format";
    private static final String HINTS = "hints";
    private static final String OBJ = "obj";
    private static final String OBJ_2 = "obj_2";
    private static final String SUBTYPE = "subtype";
    String mFormat;
    @RestrictTo({Scope.LIBRARY})
    @SliceHint
    protected String[] mHints;
    SliceItemHolder mHolder;
    Object mObj;
    String mSubType;

    @RestrictTo({Scope.LIBRARY_GROUP})
    public interface ActionHandler {
        void onAction(SliceItem sliceItem, Context context, Intent intent);
    }

    @RestrictTo({Scope.LIBRARY})
    public @interface SliceType {
    }

    @RestrictTo({Scope.LIBRARY})
    public SliceItem(Object obj, @SliceType String format, String subType, @SliceHint String[] hints) {
        this.mHints = new String[0];
        this.mHints = hints;
        this.mFormat = format;
        this.mSubType = subType;
        this.mObj = obj;
    }

    @RestrictTo({Scope.LIBRARY})
    public SliceItem(Object obj, @SliceType String format, String subType, @SliceHint List<String> hints) {
        this(obj, format, subType, (String[]) hints.toArray(new String[hints.size()]));
    }

    @RestrictTo({Scope.LIBRARY})
    public SliceItem() {
        this.mHints = new String[0];
    }

    @RestrictTo({Scope.LIBRARY})
    public SliceItem(PendingIntent intent, Slice slice, String format, String subType, @SliceHint String[] hints) {
        this(new Pair(intent, slice), format, subType, hints);
    }

    @RestrictTo({Scope.LIBRARY})
    public SliceItem(ActionHandler action, Slice slice, String format, String subType, @SliceHint String[] hints) {
        this(new Pair(action, slice), format, subType, hints);
    }

    @NonNull
    @SliceHint
    public List<String> getHints() {
        return Arrays.asList(this.mHints);
    }

    @RestrictTo({Scope.LIBRARY})
    public void addHint(@SliceHint String hint) {
        this.mHints = (String[]) ArrayUtils.appendElement(String.class, this.mHints, hint);
    }

    @SliceType
    public String getFormat() {
        return this.mFormat;
    }

    public String getSubType() {
        return this.mSubType;
    }

    public CharSequence getText() {
        return (CharSequence) this.mObj;
    }

    public IconCompat getIcon() {
        return (IconCompat) this.mObj;
    }

    public PendingIntent getAction() {
        return (PendingIntent) ((Pair) this.mObj).first;
    }

    public void fireAction(@Nullable Context context, @Nullable Intent i) throws CanceledException {
        Object action = ((Pair) this.mObj).first;
        if (action instanceof PendingIntent) {
            ((PendingIntent) action).send(context, 0, i, null, null);
        } else {
            ((ActionHandler) action).onAction(this, context, i);
        }
    }

    @RequiresApi(20)
    @RestrictTo({Scope.LIBRARY_GROUP})
    public RemoteInput getRemoteInput() {
        return (RemoteInput) this.mObj;
    }

    public int getInt() {
        return ((Integer) this.mObj).intValue();
    }

    public Slice getSlice() {
        if ("action".equals(getFormat())) {
            return (Slice) ((Pair) this.mObj).second;
        }
        return (Slice) this.mObj;
    }

    public long getLong() {
        return ((Long) this.mObj).longValue();
    }

    @Deprecated
    public long getTimestamp() {
        return ((Long) this.mObj).longValue();
    }

    public boolean hasHint(@SliceHint String hint) {
        return ArrayUtils.contains(this.mHints, hint);
    }

    @RestrictTo({Scope.LIBRARY})
    public SliceItem(Bundle in) {
        this.mHints = new String[0];
        this.mHints = in.getStringArray(HINTS);
        this.mFormat = in.getString(FORMAT);
        this.mSubType = in.getString(SUBTYPE);
        this.mObj = readObj(this.mFormat, in);
    }

    @RestrictTo({Scope.LIBRARY})
    public Bundle toBundle() {
        Bundle b = new Bundle();
        b.putStringArray(HINTS, this.mHints);
        b.putString(FORMAT, this.mFormat);
        b.putString(SUBTYPE, this.mSubType);
        writeObj(b, this.mObj, this.mFormat);
        return b;
    }

    @RestrictTo({Scope.LIBRARY})
    public boolean hasHints(@SliceHint String[] hints) {
        if (hints == null) {
            return true;
        }
        for (String hint : hints) {
            if (!TextUtils.isEmpty(hint) && !ArrayUtils.contains(this.mHints, hint)) {
                return false;
            }
        }
        return true;
    }

    @RestrictTo({Scope.LIBRARY})
    public boolean hasAnyHints(@SliceHint String... hints) {
        if (hints == null) {
            return false;
        }
        for (String hint : hints) {
            if (ArrayUtils.contains(this.mHints, hint)) {
                return true;
            }
        }
        return false;
    }

    private void writeObj(android.os.Bundle r4, java.lang.Object r5, java.lang.String r6) {
        /*
        r3 = this;
        r0 = r6.hashCode();
        switch(r0) {
            case -1422950858: goto L_0x0044;
            case 104431: goto L_0x003a;
            case 3327612: goto L_0x0030;
            case 3556653: goto L_0x0026;
            case 100313435: goto L_0x001c;
            case 100358090: goto L_0x0012;
            case 109526418: goto L_0x0008;
            default: goto L_0x0007;
        };
    L_0x0007:
        goto L_0x004e;
    L_0x0008:
        r0 = "slice";
        r0 = r6.equals(r0);
        if (r0 == 0) goto L_0x004e;
    L_0x0010:
        r0 = 2;
        goto L_0x004f;
    L_0x0012:
        r0 = "input";
        r0 = r6.equals(r0);
        if (r0 == 0) goto L_0x004e;
    L_0x001a:
        r0 = 1;
        goto L_0x004f;
    L_0x001c:
        r0 = "image";
        r0 = r6.equals(r0);
        if (r0 == 0) goto L_0x004e;
    L_0x0024:
        r0 = 0;
        goto L_0x004f;
    L_0x0026:
        r0 = "text";
        r0 = r6.equals(r0);
        if (r0 == 0) goto L_0x004e;
    L_0x002e:
        r0 = 4;
        goto L_0x004f;
    L_0x0030:
        r0 = "long";
        r0 = r6.equals(r0);
        if (r0 == 0) goto L_0x004e;
    L_0x0038:
        r0 = 6;
        goto L_0x004f;
    L_0x003a:
        r0 = "int";
        r0 = r6.equals(r0);
        if (r0 == 0) goto L_0x004e;
    L_0x0042:
        r0 = 5;
        goto L_0x004f;
    L_0x0044:
        r0 = "action";
        r0 = r6.equals(r0);
        if (r0 == 0) goto L_0x004e;
    L_0x004c:
        r0 = 3;
        goto L_0x004f;
    L_0x004e:
        r0 = -1;
    L_0x004f:
        switch(r0) {
            case 0: goto L_0x00ab;
            case 1: goto L_0x00a2;
            case 2: goto L_0x0095;
            case 3: goto L_0x0078;
            case 4: goto L_0x006f;
            case 5: goto L_0x0061;
            case 6: goto L_0x0053;
            default: goto L_0x0052;
        };
    L_0x0052:
        goto L_0x00b8;
    L_0x0053:
        r0 = "obj";
        r1 = r3.mObj;
        r1 = (java.lang.Long) r1;
        r1 = r1.longValue();
        r4.putLong(r0, r1);
        goto L_0x00b8;
    L_0x0061:
        r0 = "obj";
        r1 = r3.mObj;
        r1 = (java.lang.Integer) r1;
        r1 = r1.intValue();
        r4.putInt(r0, r1);
        goto L_0x00b8;
    L_0x006f:
        r0 = "obj";
        r1 = r5;
        r1 = (java.lang.CharSequence) r1;
        r4.putCharSequence(r0, r1);
        goto L_0x00b8;
    L_0x0078:
        r0 = "obj";
        r1 = r5;
        r1 = (android.support.v4.util.Pair) r1;
        r1 = r1.first;
        r1 = (android.app.PendingIntent) r1;
        r4.putParcelable(r0, r1);
        r0 = "obj_2";
        r1 = r5;
        r1 = (android.support.v4.util.Pair) r1;
        r1 = r1.second;
        r1 = (androidx.slice.Slice) r1;
        r1 = r1.toBundle();
        r4.putBundle(r0, r1);
        goto L_0x00b8;
    L_0x0095:
        r0 = "obj";
        r1 = r5;
        r1 = (androidx.slice.Slice) r1;
        r1 = r1.toBundle();
        r4.putParcelable(r0, r1);
        goto L_0x00b8;
    L_0x00a2:
        r0 = "obj";
        r1 = r5;
        r1 = (android.os.Parcelable) r1;
        r4.putParcelable(r0, r1);
        goto L_0x00b8;
    L_0x00ab:
        r0 = "obj";
        r1 = r5;
        r1 = (android.support.v4.graphics.drawable.IconCompat) r1;
        r1 = r1.toBundle();
        r4.putBundle(r0, r1);
    L_0x00b8:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.slice.SliceItem.writeObj(android.os.Bundle, java.lang.Object, java.lang.String):void");
    }

    private static java.lang.Object readObj(java.lang.String r4, android.os.Bundle r5) {
        /*
        r0 = r4.hashCode();
        switch(r0) {
            case -1422950858: goto L_0x0044;
            case 104431: goto L_0x003a;
            case 3327612: goto L_0x0030;
            case 3556653: goto L_0x0026;
            case 100313435: goto L_0x001c;
            case 100358090: goto L_0x0012;
            case 109526418: goto L_0x0008;
            default: goto L_0x0007;
        };
    L_0x0007:
        goto L_0x004e;
    L_0x0008:
        r0 = "slice";
        r0 = r4.equals(r0);
        if (r0 == 0) goto L_0x004e;
    L_0x0010:
        r0 = 2;
        goto L_0x004f;
    L_0x0012:
        r0 = "input";
        r0 = r4.equals(r0);
        if (r0 == 0) goto L_0x004e;
    L_0x001a:
        r0 = 1;
        goto L_0x004f;
    L_0x001c:
        r0 = "image";
        r0 = r4.equals(r0);
        if (r0 == 0) goto L_0x004e;
    L_0x0024:
        r0 = 0;
        goto L_0x004f;
    L_0x0026:
        r0 = "text";
        r0 = r4.equals(r0);
        if (r0 == 0) goto L_0x004e;
    L_0x002e:
        r0 = 3;
        goto L_0x004f;
    L_0x0030:
        r0 = "long";
        r0 = r4.equals(r0);
        if (r0 == 0) goto L_0x004e;
    L_0x0038:
        r0 = 6;
        goto L_0x004f;
    L_0x003a:
        r0 = "int";
        r0 = r4.equals(r0);
        if (r0 == 0) goto L_0x004e;
    L_0x0042:
        r0 = 5;
        goto L_0x004f;
    L_0x0044:
        r0 = "action";
        r0 = r4.equals(r0);
        if (r0 == 0) goto L_0x004e;
    L_0x004c:
        r0 = 4;
        goto L_0x004f;
    L_0x004e:
        r0 = -1;
    L_0x004f:
        switch(r0) {
            case 0: goto L_0x00b0;
            case 1: goto L_0x00a9;
            case 2: goto L_0x009d;
            case 3: goto L_0x0096;
            case 4: goto L_0x007f;
            case 5: goto L_0x0074;
            case 6: goto L_0x0069;
            default: goto L_0x0052;
        };
    L_0x0052:
        r0 = new java.lang.RuntimeException;
        r1 = new java.lang.StringBuilder;
        r1.<init>();
        r2 = "Unsupported type ";
        r1.append(r2);
        r1.append(r4);
        r1 = r1.toString();
        r0.<init>(r1);
        throw r0;
    L_0x0069:
        r0 = "obj";
        r0 = r5.getLong(r0);
        r0 = java.lang.Long.valueOf(r0);
        return r0;
    L_0x0074:
        r0 = "obj";
        r0 = r5.getInt(r0);
        r0 = java.lang.Integer.valueOf(r0);
        return r0;
    L_0x007f:
        r0 = new android.support.v4.util.Pair;
        r1 = "obj";
        r1 = r5.getParcelable(r1);
        r2 = new androidx.slice.Slice;
        r3 = "obj_2";
        r3 = r5.getBundle(r3);
        r2.<init>(r3);
        r0.<init>(r1, r2);
        return r0;
    L_0x0096:
        r0 = "obj";
        r0 = r5.getCharSequence(r0);
        return r0;
    L_0x009d:
        r0 = new androidx.slice.Slice;
        r1 = "obj";
        r1 = r5.getBundle(r1);
        r0.<init>(r1);
        return r0;
    L_0x00a9:
        r0 = "obj";
        r0 = r5.getParcelable(r0);
        return r0;
    L_0x00b0:
        r0 = "obj";
        r0 = r5.getBundle(r0);
        r0 = android.support.v4.graphics.drawable.IconCompat.createFromBundle(r0);
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.slice.SliceItem.readObj(java.lang.String, android.os.Bundle):java.lang.Object");
    }

    @android.support.annotation.RestrictTo({android.support.annotation.RestrictTo.Scope.LIBRARY})
    public static java.lang.String typeToString(java.lang.String r2) {
        /*
        r0 = r2.hashCode();
        switch(r0) {
            case -1422950858: goto L_0x0044;
            case 104431: goto L_0x003a;
            case 3327612: goto L_0x0030;
            case 3556653: goto L_0x0026;
            case 100313435: goto L_0x001c;
            case 100358090: goto L_0x0012;
            case 109526418: goto L_0x0008;
            default: goto L_0x0007;
        };
    L_0x0007:
        goto L_0x004e;
    L_0x0008:
        r0 = "slice";
        r0 = r2.equals(r0);
        if (r0 == 0) goto L_0x004e;
    L_0x0010:
        r0 = 0;
        goto L_0x004f;
    L_0x0012:
        r0 = "input";
        r0 = r2.equals(r0);
        if (r0 == 0) goto L_0x004e;
    L_0x001a:
        r0 = 6;
        goto L_0x004f;
    L_0x001c:
        r0 = "image";
        r0 = r2.equals(r0);
        if (r0 == 0) goto L_0x004e;
    L_0x0024:
        r0 = 2;
        goto L_0x004f;
    L_0x0026:
        r0 = "text";
        r0 = r2.equals(r0);
        if (r0 == 0) goto L_0x004e;
    L_0x002e:
        r0 = 1;
        goto L_0x004f;
    L_0x0030:
        r0 = "long";
        r0 = r2.equals(r0);
        if (r0 == 0) goto L_0x004e;
    L_0x0038:
        r0 = 5;
        goto L_0x004f;
    L_0x003a:
        r0 = "int";
        r0 = r2.equals(r0);
        if (r0 == 0) goto L_0x004e;
    L_0x0042:
        r0 = 4;
        goto L_0x004f;
    L_0x0044:
        r0 = "action";
        r0 = r2.equals(r0);
        if (r0 == 0) goto L_0x004e;
    L_0x004c:
        r0 = 3;
        goto L_0x004f;
    L_0x004e:
        r0 = -1;
    L_0x004f:
        switch(r0) {
            case 0: goto L_0x0076;
            case 1: goto L_0x0073;
            case 2: goto L_0x0070;
            case 3: goto L_0x006d;
            case 4: goto L_0x006a;
            case 5: goto L_0x0067;
            case 6: goto L_0x0064;
            default: goto L_0x0052;
        };
    L_0x0052:
        r0 = new java.lang.StringBuilder;
        r0.<init>();
        r1 = "Unrecognized format: ";
        r0.append(r1);
        r0.append(r2);
        r0 = r0.toString();
        return r0;
    L_0x0064:
        r0 = "RemoteInput";
        return r0;
    L_0x0067:
        r0 = "Long";
        return r0;
    L_0x006a:
        r0 = "Int";
        return r0;
    L_0x006d:
        r0 = "Action";
        return r0;
    L_0x0070:
        r0 = "Image";
        return r0;
    L_0x0073:
        r0 = "Text";
        return r0;
    L_0x0076:
        r0 = "Slice";
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.slice.SliceItem.typeToString(java.lang.String):java.lang.String");
    }

    public String toString() {
        return toString("");
    }

    @android.support.annotation.RestrictTo({android.support.annotation.RestrictTo.Scope.LIBRARY})
    public java.lang.String toString(java.lang.String r4) {
        /*
        r3 = this;
        r0 = new java.lang.StringBuilder;
        r0.<init>();
        r1 = r3.getFormat();
        r2 = r1.hashCode();
        switch(r2) {
            case -1422950858: goto L_0x0043;
            case 104431: goto L_0x0039;
            case 3327612: goto L_0x002f;
            case 3556653: goto L_0x0025;
            case 100313435: goto L_0x001b;
            case 109526418: goto L_0x0011;
            default: goto L_0x0010;
        };
    L_0x0010:
        goto L_0x004d;
    L_0x0011:
        r2 = "slice";
        r1 = r1.equals(r2);
        if (r1 == 0) goto L_0x004d;
    L_0x0019:
        r1 = 0;
        goto L_0x004e;
    L_0x001b:
        r2 = "image";
        r1 = r1.equals(r2);
        if (r1 == 0) goto L_0x004d;
    L_0x0023:
        r1 = 3;
        goto L_0x004e;
    L_0x0025:
        r2 = "text";
        r1 = r1.equals(r2);
        if (r1 == 0) goto L_0x004d;
    L_0x002d:
        r1 = 2;
        goto L_0x004e;
    L_0x002f:
        r2 = "long";
        r1 = r1.equals(r2);
        if (r1 == 0) goto L_0x004d;
    L_0x0037:
        r1 = 5;
        goto L_0x004e;
    L_0x0039:
        r2 = "int";
        r1 = r1.equals(r2);
        if (r1 == 0) goto L_0x004d;
    L_0x0041:
        r1 = 4;
        goto L_0x004e;
    L_0x0043:
        r2 = "action";
        r1 = r1.equals(r2);
        if (r1 == 0) goto L_0x004d;
    L_0x004b:
        r1 = 1;
        goto L_0x004e;
    L_0x004d:
        r1 = -1;
    L_0x004e:
        switch(r1) {
            case 0: goto L_0x00af;
            case 1: goto L_0x0094;
            case 2: goto L_0x0081;
            case 3: goto L_0x0076;
            case 4: goto L_0x006b;
            case 5: goto L_0x0060;
            default: goto L_0x0051;
        };
    L_0x0051:
        r0.append(r4);
        r1 = r3.getFormat();
        r1 = typeToString(r1);
        r0.append(r1);
        goto L_0x00bb;
    L_0x0060:
        r0.append(r4);
        r1 = r3.getLong();
        r0.append(r1);
        goto L_0x00bb;
    L_0x006b:
        r0.append(r4);
        r1 = r3.getInt();
        r0.append(r1);
        goto L_0x00bb;
    L_0x0076:
        r0.append(r4);
        r1 = r3.getIcon();
        r0.append(r1);
        goto L_0x00bb;
    L_0x0081:
        r0.append(r4);
        r1 = 34;
        r0.append(r1);
        r2 = r3.getText();
        r0.append(r2);
        r0.append(r1);
        goto L_0x00bb;
    L_0x0094:
        r0.append(r4);
        r1 = r3.getAction();
        r0.append(r1);
        r1 = ",\n";
        r0.append(r1);
        r1 = r3.getSlice();
        r1 = r1.toString(r4);
        r0.append(r1);
        goto L_0x00bb;
    L_0x00af:
        r1 = r3.getSlice();
        r1 = r1.toString(r4);
        r0.append(r1);
    L_0x00bb:
        r1 = "slice";
        r2 = r3.getFormat();
        r1 = r1.equals(r2);
        if (r1 != 0) goto L_0x00d1;
    L_0x00c7:
        r1 = 32;
        r0.append(r1);
        r1 = r3.mHints;
        androidx.slice.Slice.addHints(r0, r1);
    L_0x00d1:
        r1 = ",\n";
        r0.append(r1);
        r1 = r0.toString();
        return r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.slice.SliceItem.toString(java.lang.String):java.lang.String");
    }

    public void onPreParceling(boolean isStream) {
        this.mHolder = new SliceItemHolder(this.mFormat, this.mObj, isStream);
    }

    public void onPostParceling() {
        this.mObj = this.mHolder.getObj(this.mFormat);
        this.mHolder = null;
    }
}
