package androidx.slice.core;

import android.net.Uri;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.text.TextUtils;
import androidx.slice.Slice;
import androidx.slice.SliceItem;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@RestrictTo({Scope.LIBRARY_GROUP})
public class SliceQuery {

    private interface Filter<T> {
        boolean filter(T t);
    }

    public static boolean hasAnyHints(SliceItem item, String... hints) {
        if (hints == null) {
            return false;
        }
        List<String> itemHints = item.getHints();
        for (String hint : hints) {
            if (itemHints.contains(hint)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasHints(SliceItem item, String... hints) {
        if (hints == null) {
            return true;
        }
        List<String> itemHints = item.getHints();
        for (String hint : hints) {
            if (!TextUtils.isEmpty(hint) && !itemHints.contains(hint)) {
                return false;
            }
        }
        return true;
    }

    public static boolean hasHints(Slice item, String... hints) {
        if (hints == null) {
            return true;
        }
        List<String> itemHints = item.getHints();
        for (String hint : hints) {
            if (!TextUtils.isEmpty(hint) && !itemHints.contains(hint)) {
                return false;
            }
        }
        return true;
    }

    public static SliceItem findNotContaining(SliceItem container, List<SliceItem> list) {
        SliceItem ret = null;
        while (ret == null && list.size() != 0) {
            SliceItem remove = (SliceItem) list.remove(0);
            if (!contains(container, remove)) {
                ret = remove;
            }
        }
        return ret;
    }

    private static boolean contains(SliceItem container, final SliceItem item) {
        boolean z = false;
        if (container == null || item == null) {
            return false;
        }
        if (findFirst(filter(stream(container), new Filter<SliceItem>() {
            public boolean filter(SliceItem s) {
                return s == item;
            }
        }), null) != null) {
            z = true;
        }
        return z;
    }

    public static List<SliceItem> findAll(SliceItem s, String format) {
        return findAll(s, format, null, null);
    }

    public static List<SliceItem> findAll(Slice s, String format, String hints, String nonHints) {
        return findAll(s, format, new String[]{hints}, new String[]{nonHints});
    }

    public static List<SliceItem> findAll(SliceItem s, String format, String hints, String nonHints) {
        return findAll(s, format, new String[]{hints}, new String[]{nonHints});
    }

    public static List<SliceItem> findAll(Slice s, final String format, final String[] hints, final String[] nonHints) {
        return collect(filter(stream(s), new Filter<SliceItem>() {
            public boolean filter(SliceItem item) {
                return SliceQuery.checkFormat(item, format) && SliceQuery.hasHints(item, hints) && !SliceQuery.hasAnyHints(item, nonHints);
            }
        }));
    }

    public static List<SliceItem> findAll(SliceItem s, final String format, final String[] hints, final String[] nonHints) {
        return collect(filter(stream(s), new Filter<SliceItem>() {
            public boolean filter(SliceItem item) {
                return SliceQuery.checkFormat(item, format) && SliceQuery.hasHints(item, hints) && !SliceQuery.hasAnyHints(item, nonHints);
            }
        }));
    }

    public static SliceItem find(Slice s, String format, String hints, String nonHints) {
        return find(s, format, new String[]{hints}, new String[]{nonHints});
    }

    public static SliceItem find(Slice s, String format) {
        return find(s, format, null, null);
    }

    public static SliceItem find(SliceItem s, String format) {
        return find(s, format, null, null);
    }

    public static SliceItem find(SliceItem s, String format, String hints, String nonHints) {
        return find(s, format, new String[]{hints}, new String[]{nonHints});
    }

    public static SliceItem find(Slice s, final String format, final String[] hints, final String[] nonHints) {
        return (SliceItem) findFirst(filter(stream(s), new Filter<SliceItem>() {
            public boolean filter(SliceItem item) {
                return SliceQuery.checkFormat(item, format) && SliceQuery.hasHints(item, hints) && !SliceQuery.hasAnyHints(item, nonHints);
            }
        }), null);
    }

    public static SliceItem findSubtype(Slice s, final String format, final String subtype) {
        return (SliceItem) findFirst(filter(stream(s), new Filter<SliceItem>() {
            public boolean filter(SliceItem item) {
                return SliceQuery.checkFormat(item, format) && SliceQuery.checkSubtype(item, subtype);
            }
        }), null);
    }

    public static SliceItem findSubtype(SliceItem s, final String format, final String subtype) {
        return (SliceItem) findFirst(filter(stream(s), new Filter<SliceItem>() {
            public boolean filter(SliceItem item) {
                return SliceQuery.checkFormat(item, format) && SliceQuery.checkSubtype(item, subtype);
            }
        }), null);
    }

    public static SliceItem find(SliceItem s, final String format, final String[] hints, final String[] nonHints) {
        return (SliceItem) findFirst(filter(stream(s), new Filter<SliceItem>() {
            public boolean filter(SliceItem item) {
                return SliceQuery.checkFormat(item, format) && SliceQuery.hasHints(item, hints) && !SliceQuery.hasAnyHints(item, nonHints);
            }
        }), null);
    }

    private static boolean checkFormat(SliceItem item, String format) {
        return format == null || format.equals(item.getFormat());
    }

    private static boolean checkSubtype(SliceItem item, String subtype) {
        return subtype == null || subtype.equals(item.getSubType());
    }

    public static Iterator<SliceItem> stream(SliceItem slice) {
        ArrayList<SliceItem> items = new ArrayList();
        items.add(slice);
        return getSliceItemStream(items);
    }

    public static Iterator<SliceItem> stream(Slice slice) {
        ArrayList<SliceItem> items = new ArrayList();
        if (slice != null) {
            items.addAll(slice.getItems());
        }
        return getSliceItemStream(items);
    }

    private static Iterator<SliceItem> getSliceItemStream(final ArrayList<SliceItem> items) {
        return new Iterator<SliceItem>() {
            public boolean hasNext() {
                return items.size() != 0;
            }

            public SliceItem next() {
                SliceItem item = (SliceItem) items.remove(0);
                if ("slice".equals(item.getFormat()) || "action".equals(item.getFormat())) {
                    items.addAll(item.getSlice().getItems());
                }
                return item;
            }
        };
    }

    public static SliceItem findTopLevelItem(Slice s, String format, String subtype, String[] hints, String[] nonHints) {
        List<SliceItem> items = s.getItems();
        for (int i = 0; i < items.size(); i++) {
            SliceItem item = (SliceItem) items.get(i);
            if (checkFormat(item, format) && checkSubtype(item, subtype) && hasHints(item, hints) && !hasAnyHints(item, nonHints)) {
                return item;
            }
        }
        return null;
    }

    private static <T> List<T> collect(Iterator<T> iter) {
        List<T> list = new ArrayList();
        while (iter.hasNext()) {
            list.add(iter.next());
        }
        return list;
    }

    private static <T> Iterator<T> filter(final Iterator<T> input, final Filter<T> f) {
        return new Iterator<T>() {
            T mNext = findNext();

            private T findNext() {
                while (input.hasNext()) {
                    T i = input.next();
                    if (f.filter(i)) {
                        return i;
                    }
                }
                return null;
            }

            public boolean hasNext() {
                return this.mNext != null;
            }

            public T next() {
                T ret = this.mNext;
                this.mNext = findNext();
                return ret;
            }
        };
    }

    private static <T> T findFirst(Iterator<T> filter, T def) {
        while (filter.hasNext()) {
            T r = filter.next();
            if (r != null) {
                return r;
            }
        }
        return def;
    }

    @RestrictTo({Scope.LIBRARY_GROUP})
    public static SliceItem findItem(Slice s, final Uri uri) {
        return (SliceItem) findFirst(filter(stream(s), new Filter<SliceItem>() {
            public boolean filter(SliceItem input) {
                if ("action".equals(input.getFormat()) || "slice".equals(input.getFormat())) {
                    return uri.equals(input.getSlice().getUri());
                }
                return false;
            }
        }), null);
    }

    private SliceQuery() {
    }
}
