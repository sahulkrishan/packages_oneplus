package androidx.slice.widget;

import android.content.Context;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v17.leanback.media.MediaPlayerGlue;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import androidx.slice.SliceItem;
import androidx.slice.core.SliceAction;
import androidx.slice.core.SliceQuery;
import androidx.slice.view.R;
import androidx.slice.widget.SliceView.OnSliceActionListener;
import java.util.ArrayList;
import java.util.List;

@RestrictTo({Scope.LIBRARY})
public class LargeSliceAdapter extends Adapter<SliceViewHolder> {
    static final int HEADER_INDEX = 0;
    static final int TYPE_DEFAULT = 1;
    static final int TYPE_GRID = 3;
    static final int TYPE_HEADER = 2;
    static final int TYPE_MESSAGE = 4;
    static final int TYPE_MESSAGE_LOCAL = 5;
    private AttributeSet mAttrs;
    private int mColor;
    private final Context mContext;
    private int mDefStyleAttr;
    private int mDefStyleRes;
    private final IdGenerator mIdGen = new IdGenerator();
    private long mLastUpdated;
    private SliceView mParent;
    private boolean mShowLastUpdated;
    private List<SliceAction> mSliceActions;
    private OnSliceActionListener mSliceObserver;
    private List<SliceWrapper> mSlices = new ArrayList();
    private LargeTemplateView mTemplateView;

    private static class IdGenerator {
        private final ArrayMap<String, Long> mCurrentIds;
        private long mNextLong;
        private final ArrayMap<String, Integer> mUsedIds;

        private IdGenerator() {
            this.mNextLong = 0;
            this.mCurrentIds = new ArrayMap();
            this.mUsedIds = new ArrayMap();
        }

        public long getId(SliceItem item, int mode) {
            String str = genString(item);
            if (SliceQuery.find(item, null, "summary", null) != null) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(str);
                stringBuilder.append(mode);
                str = stringBuilder.toString();
            }
            if (!this.mCurrentIds.containsKey(str)) {
                ArrayMap arrayMap = this.mCurrentIds;
                long j = this.mNextLong;
                this.mNextLong = 1 + j;
                arrayMap.put(str, Long.valueOf(j));
            }
            long id = ((Long) this.mCurrentIds.get(str)).longValue();
            Integer usedIdIndex = (Integer) this.mUsedIds.get(str);
            int index = usedIdIndex != null ? usedIdIndex.intValue() : 0;
            this.mUsedIds.put(str, Integer.valueOf(index + 1));
            return ((long) (index * MediaPlayerGlue.FAST_FORWARD_REWIND_STEP)) + id;
        }

        private String genString(SliceItem item) {
            if ("slice".equals(item.getFormat()) || "action".equals(item.getFormat())) {
                return String.valueOf(item.getSlice().getItems().size());
            }
            return item.toString();
        }

        public void resetUsage() {
            this.mUsedIds.clear();
        }
    }

    protected static class SliceWrapper {
        private final long mId;
        private final SliceItem mItem;
        private final int mType;

        public SliceWrapper(SliceItem item, IdGenerator idGen, int mode) {
            this.mItem = item;
            this.mType = getFormat(item);
            this.mId = idGen.getId(item, mode);
        }

        public static int getFormat(SliceItem item) {
            if ("message".equals(item.getSubType())) {
                if (SliceQuery.findSubtype(item, null, "source") != null) {
                    return 4;
                }
                return 5;
            } else if (item.hasHint("horizontal")) {
                return 3;
            } else {
                if (item.hasHint("list_item")) {
                    return 1;
                }
                return 2;
            }
        }
    }

    public class SliceViewHolder extends ViewHolder implements OnTouchListener, OnClickListener {
        public final SliceChildView mSliceChildView;

        public SliceViewHolder(View itemView) {
            super(itemView);
            this.mSliceChildView = itemView instanceof SliceChildView ? (SliceChildView) itemView : null;
        }

        /* Access modifiers changed, original: 0000 */
        public void bind(SliceItem item, int position) {
            if (this.mSliceChildView != null && item != null) {
                this.mSliceChildView.setOnClickListener(this);
                this.mSliceChildView.setOnTouchListener(this);
                boolean isHeader = position == 0;
                this.mSliceChildView.setMode(LargeSliceAdapter.this.mParent != null ? LargeSliceAdapter.this.mParent.getMode() : 2);
                this.mSliceChildView.setTint(LargeSliceAdapter.this.mColor);
                this.mSliceChildView.setStyle(LargeSliceAdapter.this.mAttrs, LargeSliceAdapter.this.mDefStyleAttr, LargeSliceAdapter.this.mDefStyleRes);
                this.mSliceChildView.setSliceItem(item, isHeader, position, LargeSliceAdapter.this.getItemCount(), LargeSliceAdapter.this.mSliceObserver);
                this.mSliceChildView.setSliceActions(isHeader ? LargeSliceAdapter.this.mSliceActions : null);
                this.mSliceChildView.setLastUpdated(isHeader ? LargeSliceAdapter.this.mLastUpdated : -1);
                SliceChildView sliceChildView = this.mSliceChildView;
                boolean z = isHeader && LargeSliceAdapter.this.mShowLastUpdated;
                sliceChildView.setShowLastUpdated(z);
                if (this.mSliceChildView instanceof RowView) {
                    ((RowView) this.mSliceChildView).setSingleItem(LargeSliceAdapter.this.getItemCount() == 1);
                }
                this.mSliceChildView.setTag(new int[]{ListContent.getRowType(LargeSliceAdapter.this.mContext, item, isHeader, LargeSliceAdapter.this.mSliceActions), position});
            }
        }

        public void onClick(View v) {
            if (LargeSliceAdapter.this.mParent != null) {
                LargeSliceAdapter.this.mParent.setClickInfo((int[]) v.getTag());
                LargeSliceAdapter.this.mParent.performClick();
            }
        }

        public boolean onTouch(View v, MotionEvent event) {
            if (LargeSliceAdapter.this.mTemplateView != null) {
                LargeSliceAdapter.this.mTemplateView.onForegroundActivated(event);
            }
            return false;
        }
    }

    public LargeSliceAdapter(Context context) {
        this.mContext = context;
        setHasStableIds(true);
    }

    public void setParents(SliceView parent, LargeTemplateView templateView) {
        this.mParent = parent;
        this.mTemplateView = templateView;
    }

    public void setSliceObserver(OnSliceActionListener observer) {
        this.mSliceObserver = observer;
    }

    public void setSliceActions(List<SliceAction> actions) {
        this.mSliceActions = actions;
        notifyHeaderChanged();
    }

    public void setSliceItems(List<SliceItem> slices, int color, int mode) {
        if (slices == null) {
            this.mSlices.clear();
        } else {
            this.mIdGen.resetUsage();
            this.mSlices = new ArrayList(slices.size());
            for (SliceItem s : slices) {
                this.mSlices.add(new SliceWrapper(s, this.mIdGen, mode));
            }
        }
        this.mColor = color;
        notifyDataSetChanged();
    }

    public void setStyle(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this.mAttrs = attrs;
        this.mDefStyleAttr = defStyleAttr;
        this.mDefStyleRes = defStyleRes;
        notifyDataSetChanged();
    }

    public void setShowLastUpdated(boolean showLastUpdated) {
        this.mShowLastUpdated = showLastUpdated;
        notifyHeaderChanged();
    }

    public void setLastUpdated(long lastUpdated) {
        this.mLastUpdated = lastUpdated;
        notifyHeaderChanged();
    }

    public SliceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = inflateForType(viewType);
        v.setLayoutParams(new LayoutParams(-1, -2));
        return new SliceViewHolder(v);
    }

    public int getItemViewType(int position) {
        return ((SliceWrapper) this.mSlices.get(position)).mType;
    }

    public long getItemId(int position) {
        return ((SliceWrapper) this.mSlices.get(position)).mId;
    }

    public int getItemCount() {
        return this.mSlices.size();
    }

    public void onBindViewHolder(SliceViewHolder holder, int position) {
        holder.bind(((SliceWrapper) this.mSlices.get(position)).mItem, position);
    }

    private void notifyHeaderChanged() {
        if (getItemCount() > 0) {
            notifyItemChanged(0);
        }
    }

    private View inflateForType(int viewType) {
        View v = new RowView(this.mContext);
        switch (viewType) {
            case 3:
                return LayoutInflater.from(this.mContext).inflate(R.layout.abc_slice_grid, null);
            case 4:
                return LayoutInflater.from(this.mContext).inflate(R.layout.abc_slice_message, null);
            case 5:
                return LayoutInflater.from(this.mContext).inflate(R.layout.abc_slice_message_local, null);
            default:
                return v;
        }
    }
}
