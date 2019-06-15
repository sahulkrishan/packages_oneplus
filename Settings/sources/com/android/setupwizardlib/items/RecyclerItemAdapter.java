package com.android.setupwizardlib.items;

import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.support.annotation.VisibleForTesting;
import android.support.v7.widget.RecyclerView.Adapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import com.android.setupwizardlib.R;
import com.android.setupwizardlib.items.ItemHierarchy.Observer;

public class RecyclerItemAdapter extends Adapter<ItemViewHolder> implements Observer {
    private static final String TAG = "RecyclerItemAdapter";
    public static final String TAG_NO_BACKGROUND = "noBackground";
    private final ItemHierarchy mItemHierarchy;
    private OnItemSelectedListener mListener;

    public interface OnItemSelectedListener {
        void onItemSelected(IItem iItem);
    }

    @VisibleForTesting
    static class PatchedLayerDrawable extends LayerDrawable {
        PatchedLayerDrawable(Drawable[] layers) {
            super(layers);
        }

        public boolean getPadding(Rect padding) {
            return super.getPadding(padding) && !(padding.left == 0 && padding.top == 0 && padding.right == 0 && padding.bottom == 0);
        }
    }

    public RecyclerItemAdapter(ItemHierarchy hierarchy) {
        this.mItemHierarchy = hierarchy;
        this.mItemHierarchy.registerObserver(this);
    }

    public IItem getItem(int position) {
        return this.mItemHierarchy.getItemAt(position);
    }

    public long getItemId(int position) {
        IItem mItem = getItem(position);
        long j = -1;
        if (!(mItem instanceof AbstractItem)) {
            return -1;
        }
        int id = ((AbstractItem) mItem).getId();
        if (id > 0) {
            j = (long) id;
        }
        return j;
    }

    public int getItemCount() {
        return this.mItemHierarchy.getCount();
    }

    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        final ItemViewHolder viewHolder = new ItemViewHolder(view);
        if (!TAG_NO_BACKGROUND.equals(view.getTag())) {
            TypedArray typedArray = parent.getContext().obtainStyledAttributes(R.styleable.SuwRecyclerItemAdapter);
            Drawable selectableItemBackground = typedArray.getDrawable(R.styleable.SuwRecyclerItemAdapter_android_selectableItemBackground);
            if (selectableItemBackground == null) {
                selectableItemBackground = typedArray.getDrawable(R.styleable.SuwRecyclerItemAdapter_selectableItemBackground);
            }
            Drawable background = view.getBackground();
            if (background == null) {
                background = typedArray.getDrawable(R.styleable.SuwRecyclerItemAdapter_android_colorBackground);
            }
            if (selectableItemBackground == null || background == null) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Cannot resolve required attributes. selectableItemBackground=");
                stringBuilder.append(selectableItemBackground);
                stringBuilder.append(" background=");
                stringBuilder.append(background);
                Log.e(str, stringBuilder.toString());
            } else {
                view.setBackgroundDrawable(new PatchedLayerDrawable(new Drawable[]{background, selectableItemBackground}));
            }
            typedArray.recycle();
        }
        view.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                IItem item = viewHolder.getItem();
                if (RecyclerItemAdapter.this.mListener != null && item != null && item.isEnabled()) {
                    RecyclerItemAdapter.this.mListener.onItemSelected(item);
                }
            }
        });
        return viewHolder;
    }

    public void onBindViewHolder(ItemViewHolder holder, int position) {
        IItem item = getItem(position);
        holder.setEnabled(item.isEnabled());
        holder.setItem(item);
        item.onBindView(holder.itemView);
    }

    public int getItemViewType(int position) {
        return getItem(position).getLayoutResource();
    }

    public void onChanged(ItemHierarchy hierarchy) {
        notifyDataSetChanged();
    }

    public void onItemRangeChanged(ItemHierarchy itemHierarchy, int positionStart, int itemCount) {
        notifyItemRangeChanged(positionStart, itemCount);
    }

    public void onItemRangeInserted(ItemHierarchy itemHierarchy, int positionStart, int itemCount) {
        notifyItemRangeInserted(positionStart, itemCount);
    }

    public void onItemRangeMoved(ItemHierarchy itemHierarchy, int fromPosition, int toPosition, int itemCount) {
        if (itemCount == 1) {
            notifyItemMoved(fromPosition, toPosition);
            return;
        }
        Log.i(TAG, "onItemRangeMoved with more than one item");
        notifyDataSetChanged();
    }

    public void onItemRangeRemoved(ItemHierarchy itemHierarchy, int positionStart, int itemCount) {
        notifyItemRangeRemoved(positionStart, itemCount);
    }

    public ItemHierarchy findItemById(int id) {
        return this.mItemHierarchy.findItemById(id);
    }

    public ItemHierarchy getRootItemHierarchy() {
        return this.mItemHierarchy;
    }

    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        this.mListener = listener;
    }
}
