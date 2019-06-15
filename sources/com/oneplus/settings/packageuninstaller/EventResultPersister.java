package com.oneplus.settings.packageuninstaller;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.AtomicFile;
import android.util.Log;
import android.util.SparseArray;
import android.util.Xml;
import androidx.slice.compat.SliceProviderCompat;
import com.android.settingslib.datetime.ZoneGetter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

class EventResultPersister {
    static final String EXTRA_ID = "EventResultPersister.EXTRA_ID";
    static final int GENERATE_NEW_ID = Integer.MIN_VALUE;
    private static final String LOG_TAG = EventResultPersister.class.getSimpleName();
    private int mCounter;
    private boolean mIsPersistScheduled;
    private boolean mIsPersistingStateValid;
    private final Object mLock = new Object();
    private final SparseArray<EventResultObserver> mObservers = new SparseArray();
    private final SparseArray<EventResult> mResults = new SparseArray();
    private final AtomicFile mResultsFile;

    private class EventResult {
        public final int legacyStatus;
        @Nullable
        public final String message;
        public final int status;

        private EventResult(int status, int legacyStatus, @Nullable String message) {
            this.status = status;
            this.legacyStatus = legacyStatus;
            this.message = message;
        }
    }

    interface EventResultObserver {
        void onResult(int i, int i2, @Nullable String str);
    }

    class OutOfIdsException extends Exception {
        OutOfIdsException() {
        }
    }

    public int getNewId() throws OutOfIdsException {
        int i;
        synchronized (this.mLock) {
            if (this.mCounter != Integer.MAX_VALUE) {
                this.mCounter++;
                writeState();
                i = this.mCounter - 1;
            } else {
                throw new OutOfIdsException();
            }
        }
        return i;
    }

    private static void nextElement(@NonNull XmlPullParser parser) throws XmlPullParserException, IOException {
        int type;
        do {
            type = parser.next();
            if (type == 2) {
                return;
            }
        } while (type != 1);
    }

    private static int readIntAttribute(@NonNull XmlPullParser parser, @NonNull String name) {
        return Integer.parseInt(parser.getAttributeValue(null, name));
    }

    private static String readStringAttribute(@NonNull XmlPullParser parser, @NonNull String name) {
        return parser.getAttributeValue(null, name);
    }

    EventResultPersister(@NonNull File resultFile) {
        this.mResultsFile = new AtomicFile(resultFile);
        this.mCounter = -2147483647;
        FileInputStream stream;
        try {
            stream = this.mResultsFile.openRead();
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(stream, StandardCharsets.UTF_8.name());
            nextElement(parser);
            while (parser.getEventType() != 1) {
                String tagName = parser.getName();
                if ("results".equals(tagName)) {
                    this.mCounter = readIntAttribute(parser, "counter");
                } else if (SliceProviderCompat.EXTRA_RESULT.equals(tagName)) {
                    int id = readIntAttribute(parser, ZoneGetter.KEY_ID);
                    int status = readIntAttribute(parser, NotificationCompat.CATEGORY_STATUS);
                    int legacyStatus = readIntAttribute(parser, "legacyStatus");
                    String statusMessage = readStringAttribute(parser, "statusMessage");
                    if (this.mResults.get(id) == null) {
                        this.mResults.put(id, new EventResult(status, legacyStatus, statusMessage));
                    } else {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("id ");
                        stringBuilder.append(id);
                        stringBuilder.append(" has two results");
                        throw new Exception(stringBuilder.toString());
                    }
                } else {
                    throw new Exception("unexpected tag");
                }
                nextElement(parser);
            }
            if (stream != null) {
                stream.close();
            }
        } catch (Exception e) {
            this.mResults.clear();
            writeState();
        } catch (Throwable th) {
            r1.addSuppressed(th);
        }
    }

    /* Access modifiers changed, original: 0000 */
    /* JADX WARNING: Missing block: B:21:0x007c, code skipped:
            return;
     */
    public void onEventReceived(@android.support.annotation.NonNull android.content.Context r20, @android.support.annotation.NonNull android.content.Intent r21) {
        /*
        r19 = this;
        r7 = r19;
        r8 = r21;
        r0 = "android.content.pm.extra.STATUS";
        r1 = 0;
        r9 = r8.getIntExtra(r0, r1);
        r0 = -1;
        if (r9 != r0) goto L_0x001c;
    L_0x000e:
        r0 = "android.intent.extra.INTENT";
        r0 = r8.getParcelableExtra(r0);
        r0 = (android.content.Intent) r0;
        r10 = r20;
        r10.startActivity(r0);
        return;
    L_0x001c:
        r10 = r20;
        r0 = "EventResultPersister.EXTRA_ID";
        r11 = r8.getIntExtra(r0, r1);
        r0 = "android.content.pm.extra.STATUS_MESSAGE";
        r12 = r8.getStringExtra(r0);
        r0 = "android.content.pm.extra.LEGACY_STATUS";
        r13 = r8.getIntExtra(r0, r1);
        r2 = 0;
        r14 = r7.mLock;
        monitor-enter(r14);
        r0 = r7.mObservers;	 Catch:{ all -> 0x007d }
        r0 = r0.size();	 Catch:{ all -> 0x007d }
    L_0x003b:
        if (r1 >= r0) goto L_0x0057;
    L_0x003d:
        r3 = r7.mObservers;	 Catch:{ all -> 0x007d }
        r3 = r3.keyAt(r1);	 Catch:{ all -> 0x007d }
        if (r3 != r11) goto L_0x0054;
    L_0x0045:
        r3 = r7.mObservers;	 Catch:{ all -> 0x007d }
        r3 = r3.valueAt(r1);	 Catch:{ all -> 0x007d }
        r3 = (com.oneplus.settings.packageuninstaller.EventResultPersister.EventResultObserver) r3;	 Catch:{ all -> 0x007d }
        r2 = r3;
        r3 = r7.mObservers;	 Catch:{ all -> 0x007d }
        r3.removeAt(r1);	 Catch:{ all -> 0x007d }
        goto L_0x0057;
    L_0x0054:
        r1 = r1 + 1;
        goto L_0x003b;
    L_0x0057:
        r15 = r2;
        if (r15 == 0) goto L_0x0061;
    L_0x005a:
        r15.onResult(r9, r13, r12);	 Catch:{ all -> 0x005e }
        goto L_0x007b;
    L_0x005e:
        r0 = move-exception;
        r2 = r15;
        goto L_0x007e;
    L_0x0061:
        r6 = r7.mResults;	 Catch:{ all -> 0x005e }
        r5 = new com.oneplus.settings.packageuninstaller.EventResultPersister$EventResult;	 Catch:{ all -> 0x005e }
        r16 = 0;
        r1 = r5;
        r2 = r7;
        r3 = r9;
        r4 = r13;
        r17 = r0;
        r0 = r5;
        r5 = r12;
        r8 = r6;
        r6 = r16;
        r1.<init>(r3, r4, r5);	 Catch:{ all -> 0x005e }
        r8.put(r11, r0);	 Catch:{ all -> 0x005e }
        r19.writeState();	 Catch:{ all -> 0x005e }
    L_0x007b:
        monitor-exit(r14);	 Catch:{ all -> 0x005e }
        return;
    L_0x007d:
        r0 = move-exception;
    L_0x007e:
        monitor-exit(r14);	 Catch:{ all -> 0x007d }
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.settings.packageuninstaller.EventResultPersister.onEventReceived(android.content.Context, android.content.Intent):void");
    }

    private void writeState() {
        synchronized (this.mLock) {
            this.mIsPersistingStateValid = false;
            if (!this.mIsPersistScheduled) {
                this.mIsPersistScheduled = true;
                AsyncTask.execute(new -$$Lambda$EventResultPersister$zHzPUvQ151m1efiCPydr8fc75IA(this));
            }
        }
    }

    public static /* synthetic */ void lambda$writeState$0(EventResultPersister eventResultPersister) {
        while (true) {
            int counter;
            SparseArray<EventResult> results;
            synchronized (eventResultPersister.mLock) {
                counter = eventResultPersister.mCounter;
                results = eventResultPersister.mResults.clone();
                eventResultPersister.mIsPersistingStateValid = true;
            }
            FileOutputStream stream = null;
            try {
                stream = eventResultPersister.mResultsFile.startWrite();
                XmlSerializer serializer = Xml.newSerializer();
                serializer.setOutput(stream, StandardCharsets.UTF_8.name());
                serializer.startDocument(null, Boolean.valueOf(true));
                serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
                serializer.startTag(null, "results");
                serializer.attribute(null, "counter", Integer.toString(counter));
                int numResults = results.size();
                for (int i = 0; i < numResults; i++) {
                    serializer.startTag(null, SliceProviderCompat.EXTRA_RESULT);
                    serializer.attribute(null, ZoneGetter.KEY_ID, Integer.toString(results.keyAt(i)));
                    serializer.attribute(null, NotificationCompat.CATEGORY_STATUS, Integer.toString(((EventResult) results.valueAt(i)).status));
                    serializer.attribute(null, "legacyStatus", Integer.toString(((EventResult) results.valueAt(i)).legacyStatus));
                    if (((EventResult) results.valueAt(i)).message != null) {
                        serializer.attribute(null, "statusMessage", ((EventResult) results.valueAt(i)).message);
                    }
                    serializer.endTag(null, SliceProviderCompat.EXTRA_RESULT);
                }
                serializer.endTag(null, "results");
                serializer.endDocument();
                eventResultPersister.mResultsFile.finishWrite(stream);
            } catch (IOException e) {
                if (stream != null) {
                    eventResultPersister.mResultsFile.failWrite(stream);
                }
                Log.e(LOG_TAG, "error writing results", e);
                eventResultPersister.mResultsFile.delete();
            }
            synchronized (eventResultPersister.mLock) {
                if (eventResultPersister.mIsPersistingStateValid) {
                    eventResultPersister.mIsPersistScheduled = false;
                    return;
                }
            }
        }
        while (true) {
        }
    }

    /* Access modifiers changed, original: 0000 */
    public int addObserver(int id, @NonNull EventResultObserver observer) throws OutOfIdsException {
        synchronized (this.mLock) {
            int resultIndex = -1;
            if (id == Integer.MIN_VALUE) {
                try {
                    id = getNewId();
                } catch (Throwable th) {
                }
            } else {
                resultIndex = this.mResults.indexOfKey(id);
            }
            if (resultIndex >= 0) {
                EventResult result = (EventResult) this.mResults.valueAt(resultIndex);
                observer.onResult(result.status, result.legacyStatus, result.message);
                this.mResults.removeAt(resultIndex);
                writeState();
            } else {
                this.mObservers.put(id, observer);
            }
        }
        return id;
    }

    /* Access modifiers changed, original: 0000 */
    public void removeObserver(int id) {
        synchronized (this.mLock) {
            this.mObservers.delete(id);
        }
    }
}
