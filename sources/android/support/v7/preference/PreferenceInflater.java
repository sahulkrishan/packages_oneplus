package android.support.v7.preference;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.InflateException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

class PreferenceInflater {
    private static final HashMap<String, Constructor> CONSTRUCTOR_MAP = new HashMap();
    private static final Class<?>[] CONSTRUCTOR_SIGNATURE = new Class[]{Context.class, AttributeSet.class};
    private static final String EXTRA_TAG_NAME = "extra";
    private static final String INTENT_TAG_NAME = "intent";
    private static final String TAG = "PreferenceInflater";
    private final Object[] mConstructorArgs = new Object[2];
    private final Context mContext;
    private String[] mDefaultPackages;
    private PreferenceManager mPreferenceManager;

    public PreferenceInflater(Context context, PreferenceManager preferenceManager) {
        this.mContext = context;
        init(preferenceManager);
    }

    private void init(PreferenceManager preferenceManager) {
        this.mPreferenceManager = preferenceManager;
        setDefaultPackages(new String[]{"android.support.v14.preference.", "android.support.v7.preference."});
    }

    public void setDefaultPackages(String[] defaultPackage) {
        this.mDefaultPackages = defaultPackage;
    }

    public String[] getDefaultPackages() {
        return this.mDefaultPackages;
    }

    public Context getContext() {
        return this.mContext;
    }

    public Preference inflate(int resource, @Nullable PreferenceGroup root) {
        XmlPullParser parser = getContext().getResources().getXml(resource);
        try {
            Preference inflate = inflate(parser, root);
            return inflate;
        } finally {
            parser.close();
        }
    }

    public Preference inflate(XmlPullParser parser, @Nullable PreferenceGroup root) {
        InflateException ex;
        StringBuilder stringBuilder;
        Preference result;
        synchronized (this.mConstructorArgs) {
            int type;
            AttributeSet attrs = Xml.asAttributeSet(parser);
            this.mConstructorArgs[0] = this.mContext;
            do {
                try {
                    type = parser.next();
                    if (type == 2) {
                        break;
                    }
                } catch (InflateException e) {
                    throw e;
                } catch (XmlPullParserException e2) {
                    ex = new InflateException(e2.getMessage());
                    ex.initCause(e2);
                    throw ex;
                } catch (IOException e3) {
                    stringBuilder = new StringBuilder();
                    stringBuilder.append(parser.getPositionDescription());
                    stringBuilder.append(": ");
                    stringBuilder.append(e3.getMessage());
                    ex = new InflateException(stringBuilder.toString());
                    ex.initCause(e3);
                    throw ex;
                }
            } while (type != 1);
            if (type == 2) {
                result = onMergeRoots(root, (PreferenceGroup) createItemFromTag(parser.getName(), attrs));
                rInflate(parser, result, attrs);
            } else {
                stringBuilder = new StringBuilder();
                stringBuilder.append(parser.getPositionDescription());
                stringBuilder.append(": No start tag found!");
                throw new InflateException(stringBuilder.toString());
            }
        }
        return result;
    }

    @NonNull
    private PreferenceGroup onMergeRoots(PreferenceGroup givenRoot, @NonNull PreferenceGroup xmlRoot) {
        if (givenRoot != null) {
            return givenRoot;
        }
        xmlRoot.onAttachedToHierarchy(this.mPreferenceManager);
        return xmlRoot;
    }

    /* JADX WARNING: Removed duplicated region for block: B:24:0x0071 A:{Catch:{ ClassNotFoundException -> 0x0073, Exception -> 0x0071 }, ExcHandler: Exception (r1_2 'e' java.lang.ClassNotFoundException A:{Catch:{ ClassNotFoundException -> 0x0073, Exception -> 0x0071 }}), Splitter:B:2:0x000b} */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing block: B:14:0x0034, code skipped:
            r8 = move-exception;
     */
    /* JADX WARNING: Missing block: B:15:0x0035, code skipped:
            r4 = r8;
            r6 = r6 + 1;
     */
    /* JADX WARNING: Missing block: B:24:0x0071, code skipped:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:30:0x0080, code skipped:
            r3 = new java.lang.StringBuilder();
            r3.append(r12.getPositionDescription());
            r3.append(": Error inflating class ");
            r3.append(r10);
            r2 = new android.view.InflateException(r3.toString());
            r2.initCause(r1);
     */
    /* JADX WARNING: Missing block: B:31:0x00a1, code skipped:
            throw r2;
     */
    private android.support.v7.preference.Preference createItem(@android.support.annotation.NonNull java.lang.String r10, @android.support.annotation.Nullable java.lang.String[] r11, android.util.AttributeSet r12) throws java.lang.ClassNotFoundException, android.view.InflateException {
        /*
        r9 = this;
        r0 = CONSTRUCTOR_MAP;
        r0 = r0.get(r10);
        r0 = (java.lang.reflect.Constructor) r0;
        r1 = 1;
        if (r0 != 0) goto L_0x0075;
    L_0x000b:
        r2 = r9.mContext;	 Catch:{ ClassNotFoundException -> 0x0073, Exception -> 0x0071 }
        r2 = r2.getClassLoader();	 Catch:{ ClassNotFoundException -> 0x0073, Exception -> 0x0071 }
        r3 = 0;
        if (r11 == 0) goto L_0x005c;
    L_0x0014:
        r4 = r11.length;	 Catch:{ ClassNotFoundException -> 0x0073, Exception -> 0x0071 }
        if (r4 != 0) goto L_0x0018;
    L_0x0017:
        goto L_0x005c;
    L_0x0018:
        r4 = 0;
        r5 = r11.length;	 Catch:{ ClassNotFoundException -> 0x0073, Exception -> 0x0071 }
        r6 = 0;
    L_0x001b:
        if (r6 >= r5) goto L_0x0039;
    L_0x001d:
        r7 = r11[r6];	 Catch:{ ClassNotFoundException -> 0x0073, Exception -> 0x0071 }
        r8 = new java.lang.StringBuilder;	 Catch:{ ClassNotFoundException -> 0x0034, Exception -> 0x0071 }
        r8.<init>();	 Catch:{ ClassNotFoundException -> 0x0034, Exception -> 0x0071 }
        r8.append(r7);	 Catch:{ ClassNotFoundException -> 0x0034, Exception -> 0x0071 }
        r8.append(r10);	 Catch:{ ClassNotFoundException -> 0x0034, Exception -> 0x0071 }
        r8 = r8.toString();	 Catch:{ ClassNotFoundException -> 0x0034, Exception -> 0x0071 }
        r8 = r2.loadClass(r8);	 Catch:{ ClassNotFoundException -> 0x0034, Exception -> 0x0071 }
        r3 = r8;
        goto L_0x0039;
    L_0x0034:
        r8 = move-exception;
        r4 = r8;
        r6 = r6 + 1;
        goto L_0x001b;
    L_0x0039:
        if (r3 != 0) goto L_0x0061;
    L_0x003b:
        if (r4 != 0) goto L_0x005b;
    L_0x003d:
        r1 = new android.view.InflateException;	 Catch:{ ClassNotFoundException -> 0x0073, Exception -> 0x0071 }
        r5 = new java.lang.StringBuilder;	 Catch:{ ClassNotFoundException -> 0x0073, Exception -> 0x0071 }
        r5.<init>();	 Catch:{ ClassNotFoundException -> 0x0073, Exception -> 0x0071 }
        r6 = r12.getPositionDescription();	 Catch:{ ClassNotFoundException -> 0x0073, Exception -> 0x0071 }
        r5.append(r6);	 Catch:{ ClassNotFoundException -> 0x0073, Exception -> 0x0071 }
        r6 = ": Error inflating class ";
        r5.append(r6);	 Catch:{ ClassNotFoundException -> 0x0073, Exception -> 0x0071 }
        r5.append(r10);	 Catch:{ ClassNotFoundException -> 0x0073, Exception -> 0x0071 }
        r5 = r5.toString();	 Catch:{ ClassNotFoundException -> 0x0073, Exception -> 0x0071 }
        r1.<init>(r5);	 Catch:{ ClassNotFoundException -> 0x0073, Exception -> 0x0071 }
        throw r1;	 Catch:{ ClassNotFoundException -> 0x0073, Exception -> 0x0071 }
    L_0x005b:
        throw r4;	 Catch:{ ClassNotFoundException -> 0x0073, Exception -> 0x0071 }
    L_0x005c:
        r4 = r2.loadClass(r10);	 Catch:{ ClassNotFoundException -> 0x0073, Exception -> 0x0071 }
        r3 = r4;
    L_0x0061:
        r4 = CONSTRUCTOR_SIGNATURE;	 Catch:{ ClassNotFoundException -> 0x0073, Exception -> 0x0071 }
        r4 = r3.getConstructor(r4);	 Catch:{ ClassNotFoundException -> 0x0073, Exception -> 0x0071 }
        r0 = r4;
        r0.setAccessible(r1);	 Catch:{ ClassNotFoundException -> 0x0073, Exception -> 0x0071 }
        r4 = CONSTRUCTOR_MAP;	 Catch:{ ClassNotFoundException -> 0x0073, Exception -> 0x0071 }
        r4.put(r10, r0);	 Catch:{ ClassNotFoundException -> 0x0073, Exception -> 0x0071 }
        goto L_0x0075;
    L_0x0071:
        r1 = move-exception;
        goto L_0x0080;
    L_0x0073:
        r1 = move-exception;
        goto L_0x00a2;
    L_0x0075:
        r2 = r9.mConstructorArgs;	 Catch:{ ClassNotFoundException -> 0x0073, Exception -> 0x0071 }
        r2[r1] = r12;	 Catch:{ ClassNotFoundException -> 0x0073, Exception -> 0x0071 }
        r1 = r0.newInstance(r2);	 Catch:{ ClassNotFoundException -> 0x0073, Exception -> 0x0071 }
        r1 = (android.support.v7.preference.Preference) r1;	 Catch:{ ClassNotFoundException -> 0x0073, Exception -> 0x0071 }
        return r1;
        r2 = new android.view.InflateException;
        r3 = new java.lang.StringBuilder;
        r3.<init>();
        r4 = r12.getPositionDescription();
        r3.append(r4);
        r4 = ": Error inflating class ";
        r3.append(r4);
        r3.append(r10);
        r3 = r3.toString();
        r2.<init>(r3);
        r2.initCause(r1);
        throw r2;
        throw r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v7.preference.PreferenceInflater.createItem(java.lang.String, java.lang.String[], android.util.AttributeSet):android.support.v7.preference.Preference");
    }

    /* Access modifiers changed, original: protected */
    public Preference onCreateItem(String name, AttributeSet attrs) throws ClassNotFoundException {
        return createItem(name, this.mDefaultPackages, attrs);
    }

    private Preference createItemFromTag(String name, AttributeSet attrs) {
        StringBuilder stringBuilder;
        InflateException ie;
        try {
            if (-1 == name.indexOf(46)) {
                return onCreateItem(name, attrs);
            }
            return createItem(name, null, attrs);
        } catch (InflateException e) {
            throw e;
        } catch (ClassNotFoundException e2) {
            stringBuilder = new StringBuilder();
            stringBuilder.append(attrs.getPositionDescription());
            stringBuilder.append(": Error inflating class (not found)");
            stringBuilder.append(name);
            ie = new InflateException(stringBuilder.toString());
            ie.initCause(e2);
            throw ie;
        } catch (Exception e3) {
            stringBuilder = new StringBuilder();
            stringBuilder.append(attrs.getPositionDescription());
            stringBuilder.append(": Error inflating class ");
            stringBuilder.append(name);
            ie = new InflateException(stringBuilder.toString());
            ie.initCause(e3);
            throw ie;
        }
    }

    private void rInflate(XmlPullParser parser, Preference parent, AttributeSet attrs) throws XmlPullParserException, IOException {
        XmlPullParserException ex;
        int depth = parser.getDepth();
        while (true) {
            int next = parser.next();
            int type = next;
            if ((next == 3 && parser.getDepth() <= depth) || type == 1) {
                return;
            }
            if (type == 2) {
                String name = parser.getName();
                if ("intent".equals(name)) {
                    try {
                        parent.setIntent(Intent.parseIntent(getContext().getResources(), parser, attrs));
                    } catch (IOException e) {
                        ex = new XmlPullParserException("Error parsing preference");
                        ex.initCause(e);
                        throw ex;
                    }
                } else if (EXTRA_TAG_NAME.equals(name)) {
                    getContext().getResources().parseBundleExtra(EXTRA_TAG_NAME, attrs, parent.getExtras());
                    try {
                        skipCurrentTag(parser);
                    } catch (IOException e2) {
                        ex = new XmlPullParserException("Error parsing preference");
                        ex.initCause(e2);
                        throw ex;
                    }
                } else {
                    Preference item = createItemFromTag(name, attrs);
                    ((PreferenceGroup) parent).addItemFromInflater(item);
                    rInflate(parser, item, attrs);
                }
            }
        }
    }

    private static void skipCurrentTag(XmlPullParser parser) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
        }
    }
}
