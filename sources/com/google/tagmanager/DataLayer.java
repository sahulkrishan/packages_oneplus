package com.google.tagmanager;

import com.google.android.gms.common.util.VisibleForTesting;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataLayer {
    static final String LIFETIME_KEY = "gtm.lifetime";
    static final String[] LIFETIME_KEY_COMPONENTS = LIFETIME_KEY.toString().split("\\.");
    private static final Pattern LIFETIME_PATTERN = Pattern.compile("(\\d+)\\s*([smhd]?)");
    static final int MAX_QUEUE_DEPTH = 500;
    public static final Object OBJECT_NOT_PRESENT = new Object();
    private final ConcurrentHashMap<Listener, Integer> mListeners;
    private final Map<Object, Object> mModel;
    private final PersistentStore mPersistentStore;
    private final CountDownLatch mPersistentStoreLoaded;
    private final ReentrantLock mPushLock;
    private final LinkedList<Map<Object, Object>> mUpdateQueue;

    static final class KeyValue {
        public final String mKey;
        public final Object mValue;

        KeyValue(String key, Object value) {
            this.mKey = key;
            this.mValue = value;
        }

        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Key: ");
            stringBuilder.append(this.mKey);
            stringBuilder.append(" value: ");
            stringBuilder.append(this.mValue.toString());
            return stringBuilder.toString();
        }

        public int hashCode() {
            return Arrays.hashCode(new Integer[]{Integer.valueOf(this.mKey.hashCode()), Integer.valueOf(this.mValue.hashCode())});
        }

        public boolean equals(Object o) {
            boolean z = false;
            if (!(o instanceof KeyValue)) {
                return false;
            }
            KeyValue other = (KeyValue) o;
            if (this.mKey.equals(other.mKey) && this.mValue.equals(other.mValue)) {
                z = true;
            }
            return z;
        }
    }

    interface Listener {
        void changed(Map<Object, Object> map);
    }

    interface PersistentStore {

        public interface Callback {
            void onKeyValuesLoaded(List<KeyValue> list);
        }

        void clearKeysWithPrefix(String str);

        void loadSaved(Callback callback);

        void saveKeyValues(List<KeyValue> list, long j);
    }

    @VisibleForTesting
    DataLayer() {
        this(new PersistentStore() {
            public void saveKeyValues(List<KeyValue> list, long lifetimeInMillis) {
            }

            public void loadSaved(Callback callback) {
                callback.onKeyValuesLoaded(new ArrayList());
            }

            public void clearKeysWithPrefix(String keyPrefix) {
            }
        });
    }

    DataLayer(PersistentStore persistentStore) {
        this.mPersistentStore = persistentStore;
        this.mListeners = new ConcurrentHashMap();
        this.mModel = new HashMap();
        this.mPushLock = new ReentrantLock();
        this.mUpdateQueue = new LinkedList();
        this.mPersistentStoreLoaded = new CountDownLatch(1);
        loadSavedMaps();
    }

    public void push(Object key, Object value) {
        push(expandKeyValue(key, value));
    }

    public void push(Map<Object, Object> update) {
        try {
            this.mPersistentStoreLoaded.await();
        } catch (InterruptedException e) {
            Log.w("DataLayer.push: unexpected InterruptedException");
        }
        pushWithoutWaitingForSaved(update);
    }

    private void pushWithoutWaitingForSaved(Map<Object, Object> update) {
        this.mPushLock.lock();
        try {
            this.mUpdateQueue.offer(update);
            if (this.mPushLock.getHoldCount() == 1) {
                processQueuedUpdates();
            }
            savePersistentlyIfNeeded(update);
        } finally {
            this.mPushLock.unlock();
        }
    }

    private void loadSavedMaps() {
        this.mPersistentStore.loadSaved(new Callback() {
            public void onKeyValuesLoaded(List<KeyValue> keyValues) {
                for (KeyValue keyValue : keyValues) {
                    DataLayer.this.pushWithoutWaitingForSaved(DataLayer.this.expandKeyValue(keyValue.mKey, keyValue.mValue));
                }
                DataLayer.this.mPersistentStoreLoaded.countDown();
            }
        });
    }

    private void savePersistentlyIfNeeded(Map<Object, Object> update) {
        Long lifetime = getLifetimeValue(update);
        if (lifetime != null) {
            List<KeyValue> flattenedMap = flattenMap(update);
            flattenedMap.remove(LIFETIME_KEY);
            this.mPersistentStore.saveKeyValues(flattenedMap, lifetime.longValue());
        }
    }

    private Long getLifetimeValue(Map<Object, Object> update) {
        Object lifetimeObject = getLifetimeObject(update);
        if (lifetimeObject == null) {
            return null;
        }
        return parseLifetime(lifetimeObject.toString());
    }

    private Object getLifetimeObject(Map<Object, Object> update) {
        Map<Object, Object> current = update;
        for (String component : LIFETIME_KEY_COMPONENTS) {
            if (!(current instanceof Map)) {
                return null;
            }
            current = current.get(component);
        }
        return current;
    }

    /* Access modifiers changed, original: 0000 */
    public void clearPersistentKeysWithPrefix(String prefix) {
        push(prefix, null);
        this.mPersistentStore.clearKeysWithPrefix(prefix);
    }

    private List<KeyValue> flattenMap(Map<Object, Object> map) {
        List<KeyValue> result = new ArrayList();
        flattenMapHelper(map, "", result);
        return result;
    }

    private void flattenMapHelper(Map<Object, Object> map, String keyPrefix, Collection<KeyValue> accum) {
        for (Entry<Object, Object> entry : map.entrySet()) {
            String fullKey = new StringBuilder();
            fullKey.append(keyPrefix);
            fullKey.append(keyPrefix.length() == 0 ? "" : ".");
            fullKey.append(entry.getKey());
            fullKey = fullKey.toString();
            if (entry.getValue() instanceof Map) {
                flattenMapHelper((Map) entry.getValue(), fullKey, accum);
            } else if (!fullKey.equals(LIFETIME_KEY)) {
                accum.add(new KeyValue(fullKey, entry.getValue()));
            }
        }
    }

    @VisibleForTesting
    static Long parseLifetime(String lifetimeString) {
        Matcher m = LIFETIME_PATTERN.matcher(lifetimeString);
        StringBuilder stringBuilder;
        if (m.matches()) {
            long number = 0;
            try {
                number = Long.parseLong(m.group(1));
            } catch (NumberFormatException e) {
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append("illegal number in _lifetime value: ");
                stringBuilder2.append(lifetimeString);
                Log.w(stringBuilder2.toString());
            }
            if (number <= 0) {
                stringBuilder = new StringBuilder();
                stringBuilder.append("non-positive _lifetime: ");
                stringBuilder.append(lifetimeString);
                Log.i(stringBuilder.toString());
                return null;
            }
            String unitString = m.group(2);
            if (unitString.length() == 0) {
                return Long.valueOf(number);
            }
            char charAt = unitString.charAt(0);
            if (charAt == 'd') {
                return Long.valueOf((((1000 * number) * 60) * 60) * 24);
            }
            if (charAt == 'h') {
                return Long.valueOf(((1000 * number) * 60) * 60);
            }
            if (charAt == 'm') {
                return Long.valueOf((1000 * number) * 60);
            }
            if (charAt == 's') {
                return Long.valueOf(1000 * number);
            }
            StringBuilder stringBuilder3 = new StringBuilder();
            stringBuilder3.append("unknown units in _lifetime: ");
            stringBuilder3.append(lifetimeString);
            Log.w(stringBuilder3.toString());
            return null;
        }
        stringBuilder = new StringBuilder();
        stringBuilder.append("unknown _lifetime: ");
        stringBuilder.append(lifetimeString);
        Log.i(stringBuilder.toString());
        return null;
    }

    private void processQueuedUpdates() {
        int numUpdatesProcessed = 0;
        while (true) {
            Map<Object, Object> map = (Map) this.mUpdateQueue.poll();
            Map<Object, Object> update = map;
            if (map != null) {
                processUpdate(update);
                numUpdatesProcessed++;
                if (numUpdatesProcessed > MAX_QUEUE_DEPTH) {
                    this.mUpdateQueue.clear();
                    throw new RuntimeException("Seems like an infinite loop of pushing to the data layer");
                }
            } else {
                return;
            }
        }
    }

    private void processUpdate(Map<Object, Object> update) {
        synchronized (this.mModel) {
            for (Object key : update.keySet()) {
                mergeMap(expandKeyValue(key, update.get(key)), this.mModel);
            }
        }
        notifyListeners(update);
    }

    public Object get(String key) {
        synchronized (this.mModel) {
            Map<Object, Object> target = this.mModel;
            String[] arr$ = key.split("\\.");
            int len$ = arr$.length;
            int i$ = 0;
            while (i$ < len$) {
                String s = arr$[i$];
                if (target instanceof Map) {
                    Map<Object, Object> value = target.get(s);
                    if (value == null) {
                        return null;
                    }
                    target = value;
                    i$++;
                } else {
                    return null;
                }
            }
            return target;
        }
    }

    public static Map<Object, Object> mapOf(Object... objects) {
        if (objects.length % 2 == 0) {
            Map<Object, Object> map = new HashMap();
            for (int i = 0; i < objects.length; i += 2) {
                map.put(objects[i], objects[i + 1]);
            }
            return map;
        }
        throw new IllegalArgumentException("expected even number of key-value pairs");
    }

    public static List<Object> listOf(Object... objects) {
        List<Object> list = new ArrayList();
        for (Object add : objects) {
            list.add(add);
        }
        return list;
    }

    /* Access modifiers changed, original: 0000 */
    public void registerListener(Listener listener) {
        this.mListeners.put(listener, Integer.valueOf(0));
    }

    /* Access modifiers changed, original: 0000 */
    public void unregisterListener(Listener listener) {
        this.mListeners.remove(listener);
    }

    private void notifyListeners(Map<Object, Object> update) {
        for (Listener listener : this.mListeners.keySet()) {
            listener.changed(update);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public Map<Object, Object> expandKeyValue(Object key, Object value) {
        Map<Object, Object> result = new HashMap();
        Map<Object, Object> target = result;
        String[] split = key.toString().split("\\.");
        for (int i = 0; i < split.length - 1; i++) {
            HashMap<Object, Object> map = new HashMap();
            target.put(split[i], map);
            Object target2 = map;
        }
        target2.put(split[split.length - 1], value);
        return result;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void mergeMap(Map<Object, Object> from, Map<Object, Object> to) {
        for (Object key : from.keySet()) {
            List<Object> fromValue = from.get(key);
            if (fromValue instanceof List) {
                if (!(to.get(key) instanceof List)) {
                    to.put(key, new ArrayList());
                }
                mergeList(fromValue, (List) to.get(key));
            } else if (fromValue instanceof Map) {
                if (!(to.get(key) instanceof Map)) {
                    to.put(key, new HashMap());
                }
                mergeMap((Map) fromValue, (Map) to.get(key));
            } else {
                to.put(key, fromValue);
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void mergeList(List<Object> from, List<Object> to) {
        while (to.size() < from.size()) {
            to.add(null);
        }
        for (int index = 0; index < from.size(); index++) {
            List<Object> fromValue = from.get(index);
            if (fromValue instanceof List) {
                if (!(to.get(index) instanceof List)) {
                    to.set(index, new ArrayList());
                }
                mergeList(fromValue, (List) to.get(index));
            } else if (fromValue instanceof Map) {
                if (!(to.get(index) instanceof Map)) {
                    to.set(index, new HashMap());
                }
                mergeMap((Map) fromValue, (Map) to.get(index));
            } else if (fromValue != OBJECT_NOT_PRESENT) {
                to.set(index, fromValue);
            }
        }
    }
}
