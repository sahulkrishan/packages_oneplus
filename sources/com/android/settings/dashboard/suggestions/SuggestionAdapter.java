package com.android.settings.dashboard.suggestions;

import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.service.settings.suggestions.Suggestion;
import android.support.annotation.VisibleForTesting;
import android.support.v7.widget.RecyclerView.Adapter;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout.LayoutParams;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardAdapter.DashboardItemHolder;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.Utils;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnSaveInstanceState;
import com.android.settingslib.suggestions.SuggestionControllerMixin;
import com.android.settingslib.utils.IconCache;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SuggestionAdapter extends Adapter<DashboardItemHolder> implements LifecycleObserver, OnSaveInstanceState {
    private static final String STATE_SUGGESTIONS_SHOWN_LOGGED = "suggestions_shown_logged";
    private static final String STATE_SUGGESTION_LIST = "suggestion_list";
    public static final String TAG = "SuggestionAdapter";
    private final IconCache mCache;
    private final Callback mCallback;
    private final CardConfig mConfig;
    private final Context mContext;
    private final MetricsFeatureProvider mMetricsFeatureProvider;
    private final SuggestionControllerMixin mSuggestionControllerMixin;
    private final SuggestionFeatureProvider mSuggestionFeatureProvider;
    private List<Suggestion> mSuggestions;
    private final ArrayList<String> mSuggestionsShownLogged;

    public interface Callback {
        void onSuggestionClosed(Suggestion suggestion);
    }

    @VisibleForTesting
    static class CardConfig {
        private static CardConfig sConfig;
        private final int mMarginInner;
        private final int mMarginOuter;
        private final WindowManager mWindowManager;

        private CardConfig(Context context) {
            this.mWindowManager = (WindowManager) context.getSystemService("window");
            Resources res = context.getResources();
            this.mMarginInner = res.getDimensionPixelOffset(R.dimen.suggestion_card_inner_margin);
            this.mMarginOuter = res.getDimensionPixelOffset(R.dimen.suggestion_card_outer_margin);
        }

        public static CardConfig get(Context context) {
            if (sConfig == null) {
                sConfig = new CardConfig(context);
            }
            return sConfig;
        }

        /* Access modifiers changed, original: 0000 */
        @VisibleForTesting
        public void setCardLayout(DashboardItemHolder holder, int position) {
            LayoutParams params = new LayoutParams(getWidthForTwoCrads(), -2);
            params.setMarginStart(position == 0 ? this.mMarginOuter : this.mMarginInner);
            params.setMarginEnd(position != 0 ? this.mMarginOuter : 0);
            holder.itemView.setLayoutParams(params);
        }

        private int getWidthForTwoCrads() {
            return ((getScreenWidth() - this.mMarginInner) - (this.mMarginOuter * 2)) / 2;
        }

        /* Access modifiers changed, original: 0000 */
        @VisibleForTesting
        public int getScreenWidth() {
            DisplayMetrics metrics = new DisplayMetrics();
            this.mWindowManager.getDefaultDisplay().getMetrics(metrics);
            return metrics.widthPixels;
        }
    }

    public SuggestionAdapter(Context context, SuggestionControllerMixin suggestionControllerMixin, Bundle savedInstanceState, Callback callback, Lifecycle lifecycle) {
        this.mContext = context;
        this.mSuggestionControllerMixin = suggestionControllerMixin;
        this.mCache = new IconCache(context);
        FeatureFactory factory = FeatureFactory.getFactory(context);
        this.mMetricsFeatureProvider = factory.getMetricsFeatureProvider();
        this.mSuggestionFeatureProvider = factory.getSuggestionFeatureProvider(context);
        this.mCallback = callback;
        if (savedInstanceState != null) {
            this.mSuggestions = savedInstanceState.getParcelableArrayList(STATE_SUGGESTION_LIST);
            this.mSuggestionsShownLogged = savedInstanceState.getStringArrayList(STATE_SUGGESTIONS_SHOWN_LOGGED);
        } else {
            this.mSuggestionsShownLogged = new ArrayList();
        }
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
        this.mConfig = CardConfig.get(context);
        setHasStableIds(true);
    }

    public DashboardItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DashboardItemHolder(LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false));
    }

    public void onBindViewHolder(DashboardItemHolder holder, int position) {
        Suggestion suggestion = (Suggestion) this.mSuggestions.get(position);
        String id = suggestion.getId();
        int suggestionCount = this.mSuggestions.size();
        if (!this.mSuggestionsShownLogged.contains(id)) {
            this.mMetricsFeatureProvider.action(this.mContext, 384, id, new Pair[0]);
            this.mSuggestionsShownLogged.add(id);
        }
        Drawable drawable = this.mCache.getIcon(suggestion.getIcon());
        if (!(drawable == null || (suggestion.getFlags() & 2) == 0)) {
            drawable.setTint(Utils.getColorAccent(this.mContext));
        }
        holder.icon.setImageDrawable(drawable);
        holder.title.setText(suggestion.getTitle());
        holder.title.setTypeface(Typeface.create(this.mContext.getString(17039704), 0));
        if (suggestionCount == 1) {
            CharSequence summary = suggestion.getSummary();
            if (TextUtils.isEmpty(summary)) {
                holder.summary.setVisibility(8);
            } else {
                holder.summary.setText(summary);
                holder.summary.setVisibility(0);
            }
        } else {
            this.mConfig.setCardLayout(holder, position);
        }
        View closeButton = holder.itemView.findViewById(R.id.close_button);
        if (closeButton != null) {
            closeButton.setOnClickListener(new -$$Lambda$SuggestionAdapter$3YCJShAgHMZGvTmpJ4rD8V_2WkA(this, suggestion));
        }
        View clickHandler = holder.itemView;
        View primaryAction = holder.itemView.findViewById(16908300);
        if (primaryAction != null) {
            clickHandler = primaryAction;
        }
        clickHandler.setOnClickListener(new -$$Lambda$SuggestionAdapter$o_nlX1JhE-RQCl3p5ch8A_R_uN0(this, id, suggestion));
    }

    public static /* synthetic */ void lambda$onBindViewHolder$0(SuggestionAdapter suggestionAdapter, Suggestion suggestion, View v) {
        suggestionAdapter.mSuggestionFeatureProvider.dismissSuggestion(suggestionAdapter.mContext, suggestionAdapter.mSuggestionControllerMixin, suggestion);
        if (suggestionAdapter.mCallback != null) {
            suggestionAdapter.mCallback.onSuggestionClosed(suggestion);
        }
    }

    public static /* synthetic */ void lambda$onBindViewHolder$1(SuggestionAdapter suggestionAdapter, String id, Suggestion suggestion, View v) {
        suggestionAdapter.mMetricsFeatureProvider.action(suggestionAdapter.mContext, 386, id, new Pair[0]);
        try {
            suggestion.getPendingIntent().send();
            suggestionAdapter.mSuggestionControllerMixin.launchSuggestion(suggestion);
        } catch (CanceledException e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Failed to start suggestion ");
            stringBuilder.append(suggestion.getTitle());
            Log.w(str, stringBuilder.toString());
        }
    }

    public long getItemId(int position) {
        return (long) Objects.hash(new Object[]{((Suggestion) this.mSuggestions.get(position)).getId()});
    }

    public int getItemViewType(int position) {
        if ((getSuggestion(position).getFlags() & 1) != 0) {
            return R.layout.suggestion_tile_with_button;
        }
        if (getItemCount() == 1) {
            return R.layout.suggestion_tile;
        }
        return R.layout.suggestion_tile_two_cards;
    }

    public int getItemCount() {
        return this.mSuggestions.size();
    }

    public Suggestion getSuggestion(int position) {
        long itemId = getItemId(position);
        if (this.mSuggestions == null) {
            return null;
        }
        for (Suggestion suggestion : this.mSuggestions) {
            if (((long) Objects.hash(new Object[]{suggestion.getId()})) == itemId) {
                return (Suggestion) r2.next();
            }
        }
        return null;
    }

    public void removeSuggestion(Suggestion suggestion) {
        int position = this.mSuggestions.indexOf(suggestion);
        this.mSuggestions.remove(suggestion);
        notifyItemRemoved(position);
    }

    public void onSaveInstanceState(Bundle outState) {
        if (this.mSuggestions != null) {
            outState.putParcelableArrayList(STATE_SUGGESTION_LIST, new ArrayList(this.mSuggestions));
        }
        outState.putStringArrayList(STATE_SUGGESTIONS_SHOWN_LOGGED, this.mSuggestionsShownLogged);
    }

    public void setSuggestions(List<Suggestion> suggestions) {
        this.mSuggestions = suggestions;
    }

    public List<Suggestion> getSuggestions() {
        return this.mSuggestions;
    }
}
