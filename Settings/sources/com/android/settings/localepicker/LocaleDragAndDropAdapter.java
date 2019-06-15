package com.android.settings.localepicker;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.LocaleList;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ItemAnimator.ItemAnimatorFinishedListener;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.support.v7.widget.helper.ItemTouchHelper.SimpleCallback;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import com.android.internal.app.LocalePicker;
import com.android.internal.app.LocaleStore.LocaleInfo;
import com.android.settings.R;
import com.android.settings.shortcut.CreateShortcut.ShortcutsUpdateTask;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

class LocaleDragAndDropAdapter extends Adapter<CustomViewHolder> {
    private static final String CFGKEY_SELECTED_LOCALES = "selectedLocales";
    private static final String TAG = "LocaleDragAndDropAdapter";
    private final Context mContext;
    private boolean mDragEnabled = true;
    private final List<LocaleInfo> mFeedItemList;
    private final ItemTouchHelper mItemTouchHelper;
    private LocaleList mLocalesSetLast = null;
    private LocaleList mLocalesToSetNext = null;
    private NumberFormat mNumberFormatter = NumberFormat.getNumberInstance();
    private RecyclerView mParentView = null;
    private boolean mRemoveMode = false;

    class CustomViewHolder extends ViewHolder implements OnTouchListener {
        private final LocaleDragCell mLocaleDragCell;

        public CustomViewHolder(LocaleDragCell view) {
            super(view);
            this.mLocaleDragCell = view;
            this.mLocaleDragCell.getDragHandle().setOnTouchListener(this);
        }

        public LocaleDragCell getLocaleDragCell() {
            return this.mLocaleDragCell;
        }

        public boolean onTouch(View v, MotionEvent event) {
            if (LocaleDragAndDropAdapter.this.mDragEnabled && MotionEventCompat.getActionMasked(event) == 0) {
                LocaleDragAndDropAdapter.this.mItemTouchHelper.startDrag(this);
            }
            return false;
        }
    }

    public LocaleDragAndDropAdapter(Context context, List<LocaleInfo> feedItemList) {
        this.mFeedItemList = feedItemList;
        this.mContext = context;
        final float dragElevation = TypedValue.applyDimension(1, 8.0f, context.getResources().getDisplayMetrics());
        this.mItemTouchHelper = new ItemTouchHelper(new SimpleCallback(3, 0) {
            private static final int SELECTION_GAINED = 1;
            private static final int SELECTION_LOST = 0;
            private static final int SELECTION_UNCHANGED = -1;
            private int mSelectionStatus = -1;

            public boolean onMove(RecyclerView view, ViewHolder source, ViewHolder target) {
                LocaleDragAndDropAdapter.this.onItemMove(source.getAdapterPosition(), target.getAdapterPosition());
                return true;
            }

            public void onSwiped(ViewHolder viewHolder, int i) {
            }

            public void onChildDraw(Canvas c, RecyclerView recyclerView, ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                if (this.mSelectionStatus != -1) {
                    viewHolder.itemView.setElevation(this.mSelectionStatus == 1 ? dragElevation : 0.0f);
                    this.mSelectionStatus = -1;
                }
            }

            public void onSelectedChanged(ViewHolder viewHolder, int actionState) {
                super.onSelectedChanged(viewHolder, actionState);
                if (actionState == 2) {
                    this.mSelectionStatus = 1;
                } else if (actionState == 0) {
                    this.mSelectionStatus = 0;
                }
            }
        });
    }

    public void setRecyclerView(RecyclerView rv) {
        this.mParentView = rv;
        this.mItemTouchHelper.attachToRecyclerView(rv);
    }

    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new CustomViewHolder((LocaleDragCell) LayoutInflater.from(this.mContext).inflate(R.layout.locale_drag_cell, viewGroup, false));
    }

    public void onBindViewHolder(CustomViewHolder holder, int i) {
        LocaleInfo feedItem = (LocaleInfo) this.mFeedItemList.get(i);
        final LocaleDragCell dragCell = holder.getLocaleDragCell();
        dragCell.setLabelAndDescription(feedItem.getFullNameNative(), feedItem.getFullNameInUiLanguage());
        dragCell.setLocalized(feedItem.isTranslated());
        dragCell.setMiniLabel(this.mNumberFormatter.format((long) (i + 1)));
        dragCell.setShowCheckbox(this.mRemoveMode);
        boolean z = true;
        dragCell.setShowMiniLabel(this.mRemoveMode ^ 1);
        boolean z2 = false;
        if (this.mRemoveMode || !this.mDragEnabled) {
            z = false;
        }
        dragCell.setShowHandle(z);
        if (this.mRemoveMode) {
            z2 = feedItem.getChecked();
        }
        dragCell.setChecked(z2);
        dragCell.setTag(feedItem);
        dragCell.getCheckbox().setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ((LocaleInfo) dragCell.getTag()).setChecked(isChecked);
            }
        });
    }

    public int getItemCount() {
        int itemCount = this.mFeedItemList != null ? this.mFeedItemList.size() : 0;
        if (itemCount < 2 || this.mRemoveMode) {
            setDragEnabled(false);
        } else {
            setDragEnabled(true);
        }
        return itemCount;
    }

    /* Access modifiers changed, original: 0000 */
    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < 0 || toPosition < 0) {
            Log.e(TAG, String.format(Locale.US, "Negative position in onItemMove %d -> %d", new Object[]{Integer.valueOf(fromPosition), Integer.valueOf(toPosition)}));
        } else {
            LocaleInfo saved = (LocaleInfo) this.mFeedItemList.get(fromPosition);
            this.mFeedItemList.remove(fromPosition);
            this.mFeedItemList.add(toPosition, saved);
        }
        notifyItemChanged(fromPosition);
        notifyItemChanged(toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    /* Access modifiers changed, original: 0000 */
    public void setRemoveMode(boolean removeMode) {
        this.mRemoveMode = removeMode;
        int itemCount = this.mFeedItemList.size();
        for (int i = 0; i < itemCount; i++) {
            ((LocaleInfo) this.mFeedItemList.get(i)).setChecked(false);
            notifyItemChanged(i);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public boolean isRemoveMode() {
        return this.mRemoveMode;
    }

    /* Access modifiers changed, original: 0000 */
    public void removeItem(int position) {
        int itemCount = this.mFeedItemList.size();
        if (itemCount > 1 && position >= 0 && position < itemCount) {
            this.mFeedItemList.remove(position);
            notifyDataSetChanged();
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void removeChecked() {
        for (int i = this.mFeedItemList.size() - 1; i >= 0; i--) {
            if (((LocaleInfo) this.mFeedItemList.get(i)).getChecked()) {
                this.mFeedItemList.remove(i);
            }
        }
        notifyDataSetChanged();
        doTheUpdate();
    }

    /* Access modifiers changed, original: 0000 */
    public int getCheckedCount() {
        int result = 0;
        for (LocaleInfo li : this.mFeedItemList) {
            if (li.getChecked()) {
                result++;
            }
        }
        return result;
    }

    /* Access modifiers changed, original: 0000 */
    public void addLocale(LocaleInfo li) {
        if (!this.mFeedItemList.contains(li)) {
            this.mFeedItemList.add(li);
            notifyItemInserted(this.mFeedItemList.size() - 1);
            doTheUpdate();
        }
    }

    public void doTheUpdate() {
        int count = this.mFeedItemList.size();
        Locale[] newList = new Locale[count];
        for (int i = 0; i < count; i++) {
            newList[i] = ((LocaleInfo) this.mFeedItemList.get(i)).getLocale();
        }
        updateLocalesWhenAnimationStops(new LocaleList(newList));
    }

    public void updateLocalesWhenAnimationStops(LocaleList localeList) {
        if (!localeList.equals(this.mLocalesToSetNext)) {
            LocaleList.setDefault(localeList);
            this.mLocalesToSetNext = localeList;
            this.mParentView.getItemAnimator().isRunning(new ItemAnimatorFinishedListener() {
                public void onAnimationsFinished() {
                    if (LocaleDragAndDropAdapter.this.mLocalesToSetNext != null && !LocaleDragAndDropAdapter.this.mLocalesToSetNext.equals(LocaleDragAndDropAdapter.this.mLocalesSetLast)) {
                        LocalePicker.updateLocales(LocaleDragAndDropAdapter.this.mLocalesToSetNext);
                        LocaleDragAndDropAdapter.this.mLocalesSetLast = LocaleDragAndDropAdapter.this.mLocalesToSetNext;
                        new ShortcutsUpdateTask(LocaleDragAndDropAdapter.this.mContext).execute(new Void[0]);
                        LocaleDragAndDropAdapter.this.mLocalesToSetNext = null;
                        LocaleDragAndDropAdapter.this.mNumberFormatter = NumberFormat.getNumberInstance(Locale.getDefault());
                    }
                }
            });
        }
    }

    private void setDragEnabled(boolean enabled) {
        this.mDragEnabled = enabled;
    }

    public void saveState(Bundle outInstanceState) {
        if (outInstanceState != null) {
            ArrayList<String> selectedLocales = new ArrayList();
            for (LocaleInfo li : this.mFeedItemList) {
                if (li.getChecked()) {
                    selectedLocales.add(li.getId());
                }
            }
            outInstanceState.putStringArrayList(CFGKEY_SELECTED_LOCALES, selectedLocales);
        }
    }

    public void restoreState(Bundle savedInstanceState) {
        if (savedInstanceState != null && this.mRemoveMode) {
            ArrayList<String> selectedLocales = savedInstanceState.getStringArrayList(CFGKEY_SELECTED_LOCALES);
            if (selectedLocales != null && !selectedLocales.isEmpty()) {
                for (LocaleInfo li : this.mFeedItemList) {
                    li.setChecked(selectedLocales.contains(li.getId()));
                }
                notifyItemRangeChanged(0, this.mFeedItemList.size());
            }
        }
    }
}
