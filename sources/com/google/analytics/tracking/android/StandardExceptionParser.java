package com.google.analytics.tracking.android;

import android.content.Context;
import android.support.v4.os.EnvironmentCompat;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeSet;

public class StandardExceptionParser implements ExceptionParser {
    private final TreeSet<String> includedPackages = new TreeSet();

    public StandardExceptionParser(Context context, Collection<String> additionalPackages) {
        setIncludedPackages(context, additionalPackages);
    }

    /* JADX WARNING: Removed duplicated region for block: B:32:0x0084 A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x007f  */
    public void setIncludedPackages(android.content.Context r10, java.util.Collection<java.lang.String> r11) {
        /*
        r9 = this;
        r0 = r9.includedPackages;
        r0.clear();
        r0 = new java.util.HashSet;
        r0.<init>();
        if (r11 == 0) goto L_0x000f;
    L_0x000c:
        r0.addAll(r11);
    L_0x000f:
        if (r10 == 0) goto L_0x0046;
    L_0x0011:
        r1 = r10.getApplicationContext();	 Catch:{ NameNotFoundException -> 0x0040 }
        r1 = r1.getPackageName();	 Catch:{ NameNotFoundException -> 0x0040 }
        r2 = r9.includedPackages;	 Catch:{ NameNotFoundException -> 0x0040 }
        r2.add(r1);	 Catch:{ NameNotFoundException -> 0x0040 }
        r2 = r10.getApplicationContext();	 Catch:{ NameNotFoundException -> 0x0040 }
        r2 = r2.getPackageManager();	 Catch:{ NameNotFoundException -> 0x0040 }
        r3 = 15;
        r2 = r2.getPackageInfo(r1, r3);	 Catch:{ NameNotFoundException -> 0x0040 }
        r3 = r2.activities;	 Catch:{ NameNotFoundException -> 0x0040 }
        if (r3 == 0) goto L_0x003f;
    L_0x0030:
        r4 = r3;
        r5 = r4.length;	 Catch:{ NameNotFoundException -> 0x0040 }
        r6 = 0;
    L_0x0033:
        if (r6 >= r5) goto L_0x003f;
    L_0x0035:
        r7 = r4[r6];	 Catch:{ NameNotFoundException -> 0x0040 }
        r8 = r7.packageName;	 Catch:{ NameNotFoundException -> 0x0040 }
        r0.add(r8);	 Catch:{ NameNotFoundException -> 0x0040 }
        r6 = r6 + 1;
        goto L_0x0033;
    L_0x003f:
        goto L_0x0046;
    L_0x0040:
        r1 = move-exception;
        r2 = "No package found";
        com.google.analytics.tracking.android.Log.i(r2);
    L_0x0046:
        r1 = r0.iterator();
    L_0x004a:
        r2 = r1.hasNext();
        if (r2 == 0) goto L_0x0085;
    L_0x0050:
        r2 = r1.next();
        r2 = (java.lang.String) r2;
        r3 = 1;
        r4 = r9.includedPackages;
        r4 = r4.iterator();
    L_0x005d:
        r5 = r4.hasNext();
        if (r5 == 0) goto L_0x007d;
    L_0x0063:
        r5 = r4.next();
        r5 = (java.lang.String) r5;
        r6 = r2.startsWith(r5);
        if (r6 != 0) goto L_0x007b;
    L_0x006f:
        r4 = r5.startsWith(r2);
        if (r4 == 0) goto L_0x007d;
    L_0x0075:
        r4 = r9.includedPackages;
        r4.remove(r5);
        goto L_0x007d;
    L_0x007b:
        r3 = 0;
        goto L_0x005d;
    L_0x007d:
        if (r3 == 0) goto L_0x0084;
    L_0x007f:
        r4 = r9.includedPackages;
        r4.add(r2);
    L_0x0084:
        goto L_0x004a;
    L_0x0085:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.analytics.tracking.android.StandardExceptionParser.setIncludedPackages(android.content.Context, java.util.Collection):void");
    }

    /* Access modifiers changed, original: protected */
    public Throwable getCause(Throwable t) {
        Throwable result = t;
        while (result.getCause() != null) {
            result = result.getCause();
        }
        return result;
    }

    /* Access modifiers changed, original: protected */
    public StackTraceElement getBestStackTraceElement(Throwable t) {
        StackTraceElement[] elements = t.getStackTrace();
        if (elements == null || elements.length == 0) {
            return null;
        }
        for (StackTraceElement e : elements) {
            String className = e.getClassName();
            Iterator i$ = this.includedPackages.iterator();
            while (i$.hasNext()) {
                if (className.startsWith((String) i$.next())) {
                    return e;
                }
            }
        }
        return elements[0];
    }

    /* Access modifiers changed, original: protected */
    public String getDescription(Throwable cause, StackTraceElement element, String threadName) {
        StringBuilder descriptionBuilder = new StringBuilder();
        descriptionBuilder.append(cause.getClass().getSimpleName());
        if (element != null) {
            String[] classNameParts = element.getClassName().split("\\.");
            String className = EnvironmentCompat.MEDIA_UNKNOWN;
            if (classNameParts != null && classNameParts.length > 0) {
                className = classNameParts[classNameParts.length - 1];
            }
            descriptionBuilder.append(String.format(" (@%s:%s:%s)", new Object[]{className, element.getMethodName(), Integer.valueOf(element.getLineNumber())}));
        }
        if (threadName != null) {
            descriptionBuilder.append(String.format(" {%s}", new Object[]{threadName}));
        }
        return descriptionBuilder.toString();
    }

    public String getDescription(String threadName, Throwable t) {
        return getDescription(getCause(t), getBestStackTraceElement(getCause(t)), threadName);
    }
}
