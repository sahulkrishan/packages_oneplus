package com.android.settings.dashboard.conditional;

import android.content.Context;
import android.support.annotation.VisibleForTesting;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.support.v7.widget.helper.ItemTouchHelper.SimpleCallback;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardAdapter.DashboardItemHolder;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.WirelessUtils;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;
import com.oneplus.lib.util.AnimatorUtils;
import com.oneplus.settings.SettingsBaseApplication;
import java.util.List;
import java.util.Objects;

public class ConditionAdapter extends Adapter<DashboardItemHolder> {
    public static final String TAG = "ConditionAdapter";
    private OnClickListener mConditionClickListener = new OnClickListener() {
        public void onClick(View v) {
            Condition condition = (Condition) v.getTag();
            ConditionAdapter.this.mMetricsFeatureProvider.action(ConditionAdapter.this.mContext, (int) AnimatorUtils.time_part7, condition.getMetricsConstant());
            condition.onPrimaryClick();
        }
    };
    private List<Condition> mConditions;
    private final Context mContext;
    private boolean mExpanded;
    private final MetricsFeatureProvider mMetricsFeatureProvider;
    @VisibleForTesting
    SimpleCallback mSwipeCallback = new SimpleCallback(0, 48) {
        public boolean onMove(RecyclerView recyclerView, ViewHolder viewHolder, ViewHolder target) {
            return true;
        }

        public int getSwipeDirs(RecyclerView recyclerView, ViewHolder viewHolder) {
            return viewHolder.getItemViewType() == R.layout.condition_tile ? super.getSwipeDirs(recyclerView, viewHolder) : 0;
        }

        public void onSwiped(ViewHolder viewHolder, int direction) {
            Object item = ConditionAdapter.this.getItem(viewHolder.getItemId());
            if (item != null) {
                ((Condition) item).silence();
            }
        }
    };

    public ConditionAdapter(Context context, List<Condition> conditions, boolean expanded) {
        this.mContext = context;
        this.mConditions = conditions;
        this.mExpanded = expanded;
        this.mMetricsFeatureProvider = FeatureFactory.getFactory(context).getMetricsFeatureProvider();
        setHasStableIds(true);
    }

    public Object getItem(long itemId) {
        for (Condition condition : this.mConditions) {
            if (((long) Objects.hash(new Object[]{condition.getTitle()})) == itemId) {
                return (Condition) r0.next();
            }
        }
        return null;
    }

    public DashboardItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DashboardItemHolder(LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false));
    }

    public void onBindViewHolder(DashboardItemHolder holder, int position) {
        Condition condition = (Condition) this.mConditions.get(position);
        boolean z = true;
        if (position != this.mConditions.size() - 1) {
            z = false;
        }
        bindViews(condition, holder, z, this.mConditionClickListener);
    }

    public long getItemId(int position) {
        return (long) Objects.hash(new Object[]{((Condition) this.mConditions.get(position)).getTitle()});
    }

    public int getItemViewType(int position) {
        return R.layout.condition_tile;
    }

    public int getItemCount() {
        if (this.mExpanded) {
            return this.mConditions.size();
        }
        return 0;
    }

    public void addDismissHandling(RecyclerView recyclerView) {
        new ItemTouchHelper(this.mSwipeCallback).attachToRecyclerView(recyclerView);
    }

    private void bindViews(final Condition condition, DashboardItemHolder view, boolean isLastItem, OnClickListener onClickListener) {
        if (condition instanceof AirplaneModeCondition) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Airplane mode condition has been bound with isActive=");
            stringBuilder.append(condition.isActive());
            stringBuilder.append(". Airplane mode is currently ");
            stringBuilder.append(WirelessUtils.isAirplaneModeOn(condition.mManager.getContext()));
            Log.d(str, stringBuilder.toString());
        }
        View card = view.itemView.findViewById(R.id.content);
        card.setTag(condition);
        card.setOnClickListener(onClickListener);
        view.icon.setImageDrawable(condition.getIcon());
        view.title.setText(condition.getTitle());
        CharSequence[] actions = condition.getActions();
        setViewVisibility(view.itemView, R.id.buttonBar, actions.length > 0);
        view.summary.setText(condition.getSummary());
        int i = 0;
        while (i < 2) {
            Button button = (Button) view.itemView.findViewById(i == 0 ? R.id.first_action : R.id.second_action);
            if (actions.length <= i) {
                button.setVisibility(8);
            } else if (SettingsBaseApplication.mApplication.getString(R.string.condition_device_muted_action_turn_on_sound).equals(actions[i])) {
                button.setVisibility(8);
            } else {
                button.setVisibility(0);
                button.setText(actions[i]);
                final int index = i;
                button.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        Context context = v.getContext();
                        FeatureFactory.getFactory(context).getMetricsFeatureProvider().action(context, 376, condition.getMetricsConstant());
                        condition.onActionClick(index);
                    }
                });
            }
            i++;
        }
        setViewVisibility(view.itemView, R.id.divider, isLastItem ^ 1);
    }

    private void setViewVisibility(View containerView, int viewId, boolean visible) {
        View view = containerView.findViewById(viewId);
        if (view != null) {
            view.setVisibility(visible ? 0 : 8);
        }
    }

    public void setConditions(List<Condition> conditions) {
        this.mConditions = conditions;
    }
}
