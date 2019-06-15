package com.android.setupwizardlib.items;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.InflateException;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public abstract class SimpleInflater<T> {
    private static final boolean DEBUG = false;
    private static final String TAG = "SimpleInflater";
    protected final Resources mResources;

    public abstract void onAddChildItem(T t, T t2);

    public abstract T onCreateItem(String str, AttributeSet attributeSet);

    protected SimpleInflater(@NonNull Resources resources) {
        this.mResources = resources;
    }

    public Resources getResources() {
        return this.mResources;
    }

    public T inflate(int resId) {
        XmlPullParser parser = getResources().getXml(resId);
        try {
            T inflate = inflate(parser);
            return inflate;
        } finally {
            parser.close();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:10:0x0020 A:{Catch:{ XmlPullParserException -> 0x005e, IOException -> 0x003b }} */
    /* JADX WARNING: Removed duplicated region for block: B:8:0x0012 A:{Catch:{ XmlPullParserException -> 0x005e, IOException -> 0x003b }} */
    public T inflate(org.xmlpull.v1.XmlPullParser r6) {
        /*
        r5 = this;
        r0 = android.util.Xml.asAttributeSet(r6);
    L_0x0004:
        r1 = r6.next();	 Catch:{ XmlPullParserException -> 0x005e, IOException -> 0x003b }
        r2 = r1;
        r3 = 2;
        if (r1 == r3) goto L_0x0010;
    L_0x000c:
        r1 = 1;
        if (r2 == r1) goto L_0x0010;
    L_0x000f:
        goto L_0x0004;
    L_0x0010:
        if (r2 != r3) goto L_0x0020;
    L_0x0012:
        r1 = r6.getName();	 Catch:{ XmlPullParserException -> 0x005e, IOException -> 0x003b }
        r1 = r5.createItemFromTag(r1, r0);	 Catch:{ XmlPullParserException -> 0x005e, IOException -> 0x003b }
        r5.rInflate(r6, r1, r0);	 Catch:{ XmlPullParserException -> 0x005e, IOException -> 0x003b }
        return r1;
    L_0x0020:
        r1 = new android.view.InflateException;	 Catch:{ XmlPullParserException -> 0x005e, IOException -> 0x003b }
        r3 = new java.lang.StringBuilder;	 Catch:{ XmlPullParserException -> 0x005e, IOException -> 0x003b }
        r3.<init>();	 Catch:{ XmlPullParserException -> 0x005e, IOException -> 0x003b }
        r4 = r6.getPositionDescription();	 Catch:{ XmlPullParserException -> 0x005e, IOException -> 0x003b }
        r3.append(r4);	 Catch:{ XmlPullParserException -> 0x005e, IOException -> 0x003b }
        r4 = ": No start tag found!";
        r3.append(r4);	 Catch:{ XmlPullParserException -> 0x005e, IOException -> 0x003b }
        r3 = r3.toString();	 Catch:{ XmlPullParserException -> 0x005e, IOException -> 0x003b }
        r1.<init>(r3);	 Catch:{ XmlPullParserException -> 0x005e, IOException -> 0x003b }
        throw r1;	 Catch:{ XmlPullParserException -> 0x005e, IOException -> 0x003b }
    L_0x003b:
        r1 = move-exception;
        r2 = new android.view.InflateException;
        r3 = new java.lang.StringBuilder;
        r3.<init>();
        r4 = r6.getPositionDescription();
        r3.append(r4);
        r4 = ": ";
        r3.append(r4);
        r4 = r1.getMessage();
        r3.append(r4);
        r3 = r3.toString();
        r2.<init>(r3, r1);
        throw r2;
    L_0x005e:
        r1 = move-exception;
        r2 = new android.view.InflateException;
        r3 = r1.getMessage();
        r2.<init>(r3, r1);
        throw r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.setupwizardlib.items.SimpleInflater.inflate(org.xmlpull.v1.XmlPullParser):java.lang.Object");
    }

    private T createItemFromTag(String name, AttributeSet attrs) {
        try {
            return onCreateItem(name, attrs);
        } catch (InflateException e) {
            throw e;
        } catch (Exception e2) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(attrs.getPositionDescription());
            stringBuilder.append(": Error inflating class ");
            stringBuilder.append(name);
            throw new InflateException(stringBuilder.toString(), e2);
        }
    }

    private void rInflate(XmlPullParser parser, T parent, AttributeSet attrs) throws XmlPullParserException, IOException {
        int depth = parser.getDepth();
        while (true) {
            int next = parser.next();
            int type = next;
            if ((next == 3 && parser.getDepth() <= depth) || type == 1) {
                return;
            }
            if (type == 2) {
                if (!onInterceptCreateItem(parser, parent, attrs)) {
                    T item = createItemFromTag(parser.getName(), attrs);
                    onAddChildItem(parent, item);
                    rInflate(parser, item, attrs);
                }
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public boolean onInterceptCreateItem(XmlPullParser parser, T t, AttributeSet attrs) throws XmlPullParserException {
        return false;
    }
}
