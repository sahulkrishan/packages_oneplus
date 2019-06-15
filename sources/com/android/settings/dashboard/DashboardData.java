package com.android.settings.dashboard;

import android.graphics.drawable.Drawable;
import android.service.settings.suggestions.Suggestion;
import android.support.annotation.VisibleForTesting;
import android.support.v7.util.DiffUtil.Callback;
import com.android.settings.R;
import com.android.settings.dashboard.conditional.Condition;
import com.android.settingslib.drawer.DashboardCategory;
import com.android.settingslib.drawer.Tile;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DashboardData {
    public static final int MAX_SUGGESTION_COUNT = 2;
    public static final int POSITION_NOT_FOUND = -1;
    @VisibleForTesting
    static final int STABLE_ID_CONDITION_CONTAINER = 4;
    @VisibleForTesting
    static final int STABLE_ID_CONDITION_FOOTER = 3;
    @VisibleForTesting
    static final int STABLE_ID_CONDITION_HEADER = 2;
    @VisibleForTesting
    static final int STABLE_ID_SERCH_LAYOUT = 5;
    static final int STABLE_ID_SUGGESTION_CONDITION_DIVIDER = 1;
    @VisibleForTesting
    static final int STABLE_ID_SUGGESTION_CONTAINER = 0;
    private final DashboardCategory mCategory;
    private final boolean mConditionExpanded;
    private final List<Condition> mConditions;
    private final List<Item> mItems;
    private final List<Suggestion> mSuggestions;

    public static class Builder {
        private DashboardCategory mCategory;
        private boolean mConditionExpanded;
        private List<Condition> mConditions;
        private List<Suggestion> mSuggestions;

        public Builder(DashboardData dashboardData) {
            this.mCategory = dashboardData.mCategory;
            this.mConditions = dashboardData.mConditions;
            this.mSuggestions = dashboardData.mSuggestions;
            this.mConditionExpanded = dashboardData.mConditionExpanded;
        }

        public Builder setCategory(DashboardCategory category) {
            this.mCategory = category;
            return this;
        }

        public Builder setConditions(List<Condition> conditions) {
            this.mConditions = conditions;
            return this;
        }

        public Builder setSuggestions(List<Suggestion> suggestions) {
            this.mSuggestions = suggestions;
            return this;
        }

        public Builder setConditionExpanded(boolean expanded) {
            this.mConditionExpanded = expanded;
            return this;
        }

        public DashboardData build() {
            return new DashboardData(this);
        }
    }

    public static class ConditionHeaderData {
        public final int conditionCount;
        public final List<Drawable> conditionIcons;
        public final CharSequence title;

        public ConditionHeaderData(List<Condition> conditions) {
            this.conditionCount = DashboardData.sizeOf(conditions);
            int i = 0;
            this.title = this.conditionCount > 0 ? ((Condition) conditions.get(0)).getTitle() : null;
            this.conditionIcons = new ArrayList();
            while (true) {
                int i2 = i;
                if (conditions != null && i2 < conditions.size()) {
                    this.conditionIcons.add(((Condition) conditions.get(i2)).getIcon());
                    i = i2 + 1;
                } else {
                    return;
                }
            }
        }
    }

    static class Item {
        private static final int TYPE_CONDITION_CONTAINER = 2131558481;
        private static final int TYPE_CONDITION_FOOTER = 2131558482;
        private static final int TYPE_CONDITION_HEADER = 2131558483;
        private static final int TYPE_DASHBOARD_TILE = 2131558513;
        private static final int TYPE_SUGGESTION_CONDITION_DIVIDER = 2131558561;
        private static final int TYPE_SUGGESTION_CONTAINER = 2131559007;
        public final Object entity;
        public final int id;
        public final int type;

        @Retention(RetentionPolicy.SOURCE)
        public @interface ItemTypes {
        }

        public Item(Object entity, int type, int id) {
            this.entity = entity;
            this.type = type;
            this.id = id;
        }

        /* JADX WARNING: Missing block: B:15:0x0029, code skipped:
            if (r3 != com.android.settings.R.layout.suggestion_container) goto L_0x0065;
     */
        public boolean equals(java.lang.Object r8) {
            /*
            r7 = this;
            r0 = 1;
            if (r7 != r8) goto L_0x0004;
        L_0x0003:
            return r0;
        L_0x0004:
            r1 = r8 instanceof com.android.settings.dashboard.DashboardData.Item;
            r2 = 0;
            if (r1 != 0) goto L_0x000a;
        L_0x0009:
            return r2;
        L_0x000a:
            r1 = r8;
            r1 = (com.android.settings.dashboard.DashboardData.Item) r1;
            r3 = r7.type;
            r4 = r1.type;
            if (r3 != r4) goto L_0x0079;
        L_0x0013:
            r3 = r7.id;
            r4 = r1.id;
            if (r3 == r4) goto L_0x001a;
        L_0x0019:
            goto L_0x0079;
        L_0x001a:
            r3 = r7.type;
            r4 = 2131558481; // 0x7f0d0051 float:1.874228E38 double:1.0531298176E-314;
            if (r3 == r4) goto L_0x004b;
        L_0x0021:
            r4 = 2131558513; // 0x7f0d0071 float:1.8742344E38 double:1.0531298334E-314;
            if (r3 == r4) goto L_0x002c;
        L_0x0026:
            r4 = 2131559007; // 0x7f0d025f float:1.8743346E38 double:1.0531300774E-314;
            if (r3 == r4) goto L_0x004b;
        L_0x002b:
            goto L_0x0065;
        L_0x002c:
            r3 = r7.entity;
            r3 = (com.android.settingslib.drawer.Tile) r3;
            r4 = r1.entity;
            r4 = (com.android.settingslib.drawer.Tile) r4;
            r5 = r3.title;
            r6 = r4.title;
            r5 = android.text.TextUtils.equals(r5, r6);
            if (r5 == 0) goto L_0x0049;
        L_0x003e:
            r5 = r3.summary;
            r6 = r4.summary;
            r5 = android.text.TextUtils.equals(r5, r6);
            if (r5 == 0) goto L_0x0049;
        L_0x0048:
            goto L_0x004a;
        L_0x0049:
            r0 = r2;
        L_0x004a:
            return r0;
        L_0x004b:
            r3 = r7.entity;
            r3 = (java.util.List) r3;
            r4 = r3.isEmpty();
            if (r4 != 0) goto L_0x0065;
        L_0x0055:
            r4 = r3.get(r2);
            r5 = r4 instanceof com.android.settingslib.drawer.Tile;
            if (r5 == 0) goto L_0x0065;
        L_0x005d:
            r5 = r4;
            r5 = (com.android.settingslib.drawer.Tile) r5;
            r5 = r5.remoteViews;
            if (r5 == 0) goto L_0x0065;
        L_0x0064:
            return r2;
        L_0x0065:
            r3 = r7.entity;
            if (r3 != 0) goto L_0x0070;
        L_0x0069:
            r3 = r1.entity;
            if (r3 != 0) goto L_0x006e;
        L_0x006d:
            goto L_0x0078;
        L_0x006e:
            r0 = r2;
            goto L_0x0078;
        L_0x0070:
            r0 = r7.entity;
            r2 = r1.entity;
            r0 = r0.equals(r2);
        L_0x0078:
            return r0;
        L_0x0079:
            return r2;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.settings.dashboard.DashboardData$Item.equals(java.lang.Object):boolean");
        }
    }

    public static class ItemsDataDiffCallback extends Callback {
        private final List<Item> mNewItems;
        private final List<Item> mOldItems;

        public ItemsDataDiffCallback(List<Item> oldItems, List<Item> newItems) {
            this.mOldItems = oldItems;
            this.mNewItems = newItems;
        }

        public int getOldListSize() {
            return this.mOldItems.size();
        }

        public int getNewListSize() {
            return this.mNewItems.size();
        }

        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return ((Item) this.mOldItems.get(oldItemPosition)).id == ((Item) this.mNewItems.get(newItemPosition)).id;
        }

        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return ((Item) this.mOldItems.get(oldItemPosition)).equals(this.mNewItems.get(newItemPosition));
        }
    }

    private DashboardData(Builder builder) {
        this.mCategory = builder.mCategory;
        this.mConditions = builder.mConditions;
        this.mSuggestions = builder.mSuggestions;
        this.mConditionExpanded = builder.mConditionExpanded;
        this.mItems = new ArrayList();
        buildItemsData();
    }

    public int getItemIdByPosition(int position) {
        return ((Item) this.mItems.get(position)).id;
    }

    public int getItemTypeByPosition(int position) {
        return ((Item) this.mItems.get(position)).type;
    }

    public Object getItemEntityByPosition(int position) {
        return ((Item) this.mItems.get(position)).entity;
    }

    public List<Item> getItemList() {
        return this.mItems;
    }

    public int size() {
        return this.mItems.size();
    }

    public Object getItemEntityById(long id) {
        for (Item item : this.mItems) {
            if (((long) item.id) == id) {
                return item.entity;
            }
        }
        return null;
    }

    public DashboardCategory getCategory() {
        return this.mCategory;
    }

    public List<Condition> getConditions() {
        return this.mConditions;
    }

    public List<Suggestion> getSuggestions() {
        return this.mSuggestions;
    }

    public boolean hasSuggestion() {
        return sizeOf(this.mSuggestions) > 0;
    }

    public boolean isConditionExpanded() {
        return this.mConditionExpanded;
    }

    public int getPositionByEntity(Object entity) {
        if (entity == null) {
            return -1;
        }
        int size = this.mItems.size();
        for (int i = 0; i < size; i++) {
            if (entity.equals(((Item) this.mItems.get(i)).entity)) {
                return i;
            }
        }
        return -1;
    }

    public int getPositionByTile(Tile tile) {
        int size = this.mItems.size();
        for (int i = 0; i < size; i++) {
            Tile entity = ((Item) this.mItems.get(i)).entity;
            if (entity == tile) {
                return i;
            }
            if ((entity instanceof Tile) && tile.title.equals(entity.title)) {
                return i;
            }
        }
        return -1;
    }

    private void addToItemList(Object item, int type, int stableId, boolean add) {
        if (add) {
            this.mItems.add(new Item(item, type, stableId));
        }
    }

    private void buildItemsData() {
        addToItemList(null, R.layout.op_search_layout, 5, true);
        List<Condition> conditions = getConditionsToShow(this.mConditions);
        boolean hasConditions = sizeOf(conditions) > 0;
        List<Suggestion> suggestions = getSuggestionsToShow(this.mSuggestions);
        boolean hasSuggestions = sizeOf(suggestions) > 0;
        addToItemList(suggestions, R.layout.suggestion_container, 0, hasSuggestions);
        boolean z = hasSuggestions && hasConditions;
        addToItemList(null, R.layout.horizontal_divider, 1, z);
        ConditionHeaderData conditionHeaderData = new ConditionHeaderData(conditions);
        boolean z2 = hasConditions && !this.mConditionExpanded;
        addToItemList(conditionHeaderData, R.layout.condition_header, 2, z2);
        boolean z3 = hasConditions && this.mConditionExpanded;
        addToItemList(conditions, R.layout.condition_container, 4, z3);
        z3 = hasConditions && this.mConditionExpanded;
        addToItemList(null, R.layout.condition_footer, 3, z3);
        if (this.mCategory != null) {
            List<Tile> tiles = this.mCategory.getTiles();
            for (int i = 0; i < tiles.size(); i++) {
                addToItemList((Tile) tiles.get(i), R.layout.dashboard_tile, Objects.hash(new Object[]{((Tile) tiles.get(i)).title}), true);
            }
        }
    }

    private static int sizeOf(List<?> list) {
        return list == null ? 0 : list.size();
    }

    private List<Condition> getConditionsToShow(List<Condition> conditions) {
        if (conditions == null) {
            return null;
        }
        List<Condition> result = new ArrayList();
        int i = 0;
        int size = conditions == null ? 0 : conditions.size();
        while (i < size) {
            Condition condition = (Condition) conditions.get(i);
            if (condition.shouldShow()) {
                result.add(condition);
            }
            i++;
        }
        return result;
    }

    public List<Condition> getConditionsToShow() {
        return getConditionsToShow(this.mConditions);
    }

    private List<Suggestion> getSuggestionsToShow(List<Suggestion> suggestions) {
        if (suggestions == null) {
            return null;
        }
        if (suggestions.size() <= 2) {
            return suggestions;
        }
        List<Suggestion> suggestionsToShow = new ArrayList(2);
        for (int i = 0; i < 2; i++) {
            suggestionsToShow.add((Suggestion) suggestions.get(i));
        }
        return suggestionsToShow;
    }
}
