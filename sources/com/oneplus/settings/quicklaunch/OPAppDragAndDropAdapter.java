package com.oneplus.settings.quicklaunch;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Bundle;
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
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import com.android.settings.R;
import com.oneplus.settings.better.OPAppModel;
import com.oneplus.settings.utils.OPUtils;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

class OPAppDragAndDropAdapter extends Adapter<CustomViewHolder> {
    private static final String CFGKEY_SELECTED_LOCALES = "selectedLocales";
    private static final String TAG = "LocaleDragAndDropAdapter";
    private List<OPAppModel> mAppItemList;
    private final Context mContext;
    private boolean mDragEnabled = true;
    private final ItemTouchHelper mItemTouchHelper;
    private NumberFormat mNumberFormatter = NumberFormat.getNumberInstance();
    private RecyclerView mParentView = null;
    private boolean mRemoveMode = false;

    class CustomViewHolder extends ViewHolder implements OnTouchListener {
        private final OPAppDragCell mAppDragCell;

        public CustomViewHolder(OPAppDragCell view) {
            super(view);
            this.mAppDragCell = view;
            this.mAppDragCell.getDragHandle().setOnTouchListener(this);
        }

        public OPAppDragCell getAppDragCell() {
            return this.mAppDragCell;
        }

        public boolean onTouch(View v, MotionEvent event) {
            if (OPAppDragAndDropAdapter.this.mDragEnabled && MotionEventCompat.getActionMasked(event) == 0) {
                OPAppDragAndDropAdapter.this.mItemTouchHelper.startDrag(this);
            }
            return false;
        }
    }

    public void setAppList(List<OPAppModel> appItemList) {
        this.mAppItemList = appItemList;
        notifyDataSetChanged();
    }

    public OPAppDragAndDropAdapter(Context context, List<OPAppModel> appItemList) {
        this.mAppItemList = appItemList;
        this.mContext = context;
        final float dragElevation = TypedValue.applyDimension(1, 8.0f, context.getResources().getDisplayMetrics());
        this.mItemTouchHelper = new ItemTouchHelper(new SimpleCallback(3, 0) {
            private static final int SELECTION_GAINED = 1;
            private static final int SELECTION_LOST = 0;
            private static final int SELECTION_UNCHANGED = -1;
            private int mSelectionStatus = -1;

            public boolean onMove(RecyclerView view, ViewHolder source, ViewHolder target) {
                OPAppDragAndDropAdapter.this.onItemMove(source.getAdapterPosition(), target.getAdapterPosition());
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
        return new CustomViewHolder((OPAppDragCell) LayoutInflater.from(this.mContext).inflate(R.layout.op_app_drag_cell, viewGroup, false));
    }

    public void onBindViewHolder(CustomViewHolder holder, final int i) {
        OPAppModel appItem = (OPAppModel) this.mAppItemList.get(i);
        final OPAppDragCell dragCell = holder.getAppDragCell();
        dragCell.setLabelAndDescription(appItem.getLabel(), "");
        if (appItem.getType() == 1) {
            dragCell.setAppIcon(appItem.getShortCutIcon());
            dragCell.setSmallIcon(appItem.getAppIcon());
        } else {
            dragCell.setAppIcon(appItem.getAppIcon());
            dragCell.setSmallIcon(null);
        }
        dragCell.setShowCheckbox(false);
        dragCell.setShowAppIcon(true);
        dragCell.setShowHandle(true);
        dragCell.setChecked(false);
        dragCell.setTag(appItem);
        dragCell.getCheckbox().setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ((OPAppModel) dragCell.getTag()).setSelected(isChecked);
            }
        });
        dragCell.getDeleteButton().setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                OPAppDragAndDropAdapter.this.removeItem(i);
            }
        });
    }

    public int getItemCount() {
        int itemCount = this.mAppItemList != null ? this.mAppItemList.size() : 0;
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
            OPAppModel saved = (OPAppModel) this.mAppItemList.get(fromPosition);
            this.mAppItemList.remove(fromPosition);
            this.mAppItemList.add(toPosition, saved);
        }
        notifyItemChanged(fromPosition);
        notifyItemChanged(toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    /* Access modifiers changed, original: 0000 */
    public void setRemoveMode(boolean removeMode) {
        this.mRemoveMode = removeMode;
        int itemCount = this.mAppItemList.size();
        for (int i = 0; i < itemCount; i++) {
            ((OPAppModel) this.mAppItemList.get(i)).setSelected(false);
            notifyItemChanged(i);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public boolean isRemoveMode() {
        return this.mRemoveMode;
    }

    /* Access modifiers changed, original: 0000 */
    public void removeItem(int position) {
        int itemCount = this.mAppItemList.size();
        if (position >= 0 && position < itemCount) {
            OPAppModel appModel = (OPAppModel) this.mAppItemList.get(position);
            StringBuilder allQuickLaunch = new StringBuilder(OPUtils.getAllQuickLaunchStrings(this.mContext));
            String quickApp;
            int index;
            if (appModel.getType() == 0) {
                quickApp = OPUtils.getQuickLaunchAppString(appModel);
                index = allQuickLaunch.indexOf(quickApp);
                allQuickLaunch.delete(index, quickApp.length() + index);
            } else if (appModel.getType() == 1) {
                quickApp = OPUtils.getQuickLaunchShortcutsString(appModel);
                index = allQuickLaunch.indexOf(quickApp);
                allQuickLaunch.delete(index, quickApp.length() + index);
            } else if (appModel.getType() == 2) {
                quickApp = OPUtils.getQuickPayAppString(appModel);
                index = allQuickLaunch.indexOf(quickApp);
                allQuickLaunch.delete(index, quickApp.length() + index);
            }
            OPUtils.saveQuickLaunchStrings(this.mContext, allQuickLaunch.toString());
            this.mAppItemList.remove(position);
            notifyDataSetChanged();
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void removeChecked() {
        for (int i = this.mAppItemList.size() - 1; i >= 0; i--) {
            if (((OPAppModel) this.mAppItemList.get(i)).isSelected()) {
                OPAppModel appModel = (OPAppModel) this.mAppItemList.get(i);
                StringBuilder allQuickLaunch = new StringBuilder(OPUtils.getAllQuickLaunchStrings(this.mContext));
                String quickApp;
                int index;
                if (appModel.getType() == 0) {
                    quickApp = OPUtils.getQuickLaunchAppString(appModel);
                    index = allQuickLaunch.indexOf(quickApp);
                    allQuickLaunch.delete(index, quickApp.length() + index);
                } else if (appModel.getType() == 1) {
                    quickApp = OPUtils.getQuickLaunchShortcutsString(appModel);
                    index = allQuickLaunch.indexOf(quickApp);
                    allQuickLaunch.delete(index, quickApp.length() + index);
                } else if (appModel.getType() == 2) {
                    quickApp = OPUtils.getQuickPayAppString(appModel);
                    index = allQuickLaunch.indexOf(quickApp);
                    allQuickLaunch.delete(index, quickApp.length() + index);
                }
                OPUtils.saveQuickLaunchStrings(this.mContext, allQuickLaunch.toString());
                this.mAppItemList.remove(i);
            }
        }
        notifyDataSetChanged();
        doTheUpdate();
    }

    /* Access modifiers changed, original: 0000 */
    public int isSelectedCount() {
        int result = 0;
        for (OPAppModel li : this.mAppItemList) {
            if (li.isSelected()) {
                result++;
            }
        }
        return result;
    }

    public void doTheUpdate() {
        updateLocalesWhenAnimationStops();
    }

    public void updateLocalesWhenAnimationStops() {
        final int count = this.mAppItemList.size();
        this.mParentView.getItemAnimator().isRunning(new ItemAnimatorFinishedListener() {
            public void onAnimationsFinished() {
                StringBuilder allQuickLaunch = new StringBuilder();
                for (int i = 0; i < count; i++) {
                    OPAppModel appModel = (OPAppModel) OPAppDragAndDropAdapter.this.mAppItemList.get(i);
                    if (appModel.getType() == 0) {
                        allQuickLaunch.append(OPUtils.getQuickLaunchAppString(appModel));
                    } else if (appModel.getType() == 1) {
                        allQuickLaunch.append(OPUtils.getQuickLaunchShortcutsString(appModel));
                    } else if (appModel.getType() == 2) {
                        allQuickLaunch.append(OPUtils.getQuickPayAppString(appModel));
                    }
                    OPUtils.saveQuickLaunchStrings(OPAppDragAndDropAdapter.this.mContext, allQuickLaunch.toString());
                }
            }
        });
    }

    private void setDragEnabled(boolean enabled) {
        this.mDragEnabled = enabled;
    }

    public void saveState(Bundle outInstanceState) {
        if (outInstanceState != null) {
            ArrayList<String> selectedLocales = new ArrayList();
            for (OPAppModel li : this.mAppItemList) {
                if (li.isSelected()) {
                    selectedLocales.add(li.getPkgName());
                }
            }
            outInstanceState.putStringArrayList(CFGKEY_SELECTED_LOCALES, selectedLocales);
        }
    }

    public void restoreState(Bundle savedInstanceState) {
        if (savedInstanceState != null && this.mRemoveMode) {
            ArrayList<String> selectedLocales = savedInstanceState.getStringArrayList(CFGKEY_SELECTED_LOCALES);
            if (selectedLocales != null && !selectedLocales.isEmpty()) {
                for (OPAppModel li : this.mAppItemList) {
                    li.setSelected(selectedLocales.contains(li.getPkgName()));
                }
                notifyItemRangeChanged(0, this.mAppItemList.size());
            }
        }
    }
}
