package com.android.settings.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceGroupAdapter;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.PreferenceViewHolder;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;

public class HighlightablePreferenceGroupAdapter extends PreferenceGroupAdapter {
    @VisibleForTesting
    static final long DELAY_HIGHLIGHT_DURATION_MILLIS = 600;
    private static final long HIGHLIGHT_DURATION = 15000;
    private static final long HIGHLIGHT_FADE_IN_DURATION = 200;
    private static final long HIGHLIGHT_FADE_OUT_DURATION = 500;
    private static final String TAG = "HighlightableAdapter";
    @VisibleForTesting
    boolean mFadeInAnimated;
    @VisibleForTesting
    final int mHighlightColor;
    private final String mHighlightKey;
    private int mHighlightPosition = -1;
    private boolean mHighlightRequested;
    private final int mNormalBackgroundRes;

    public static void adjustInitialExpandedChildCount(SettingsPreferenceFragment host) {
        if (host != null) {
            PreferenceScreen screen = host.getPreferenceScreen();
            if (screen != null) {
                Bundle arguments = host.getArguments();
                if (arguments == null || TextUtils.isEmpty(arguments.getString(SettingsActivity.EXTRA_FRAGMENT_ARG_KEY))) {
                    int initialCount = host.getInitialExpandedChildCount();
                    if (initialCount > 0) {
                        screen.setInitialExpandedChildrenCount(initialCount);
                        return;
                    }
                    return;
                }
                screen.setInitialExpandedChildrenCount(Integer.MAX_VALUE);
            }
        }
    }

    public HighlightablePreferenceGroupAdapter(PreferenceGroup preferenceGroup, String key, boolean highlightRequested) {
        super(preferenceGroup);
        this.mHighlightKey = key;
        this.mHighlightRequested = highlightRequested;
        Context context = preferenceGroup.getContext();
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(16843534, outValue, true);
        this.mNormalBackgroundRes = outValue.resourceId;
        this.mHighlightColor = context.getColor(R.color.preference_highligh_color);
    }

    public void onBindViewHolder(PreferenceViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        updateBackground(holder, position);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void updateBackground(PreferenceViewHolder holder, int position) {
        View v = holder.itemView;
        if (position == this.mHighlightPosition) {
            addHighlightBackground(v, this.mFadeInAnimated ^ 1);
        } else if (Boolean.TRUE.equals(v.getTag(R.id.preference_highlighted))) {
            removeHighlightBackground(v, false);
        }
    }

    public void requestHighlight(View root, RecyclerView recyclerView) {
        if (!this.mHighlightRequested && recyclerView != null && !TextUtils.isEmpty(this.mHighlightKey)) {
            root.postDelayed(new -$$Lambda$HighlightablePreferenceGroupAdapter$Xc5BA2nCks8YuSzn7LsPZS7EmPA(this, recyclerView), DELAY_HIGHLIGHT_DURATION_MILLIS);
        }
    }

    public static /* synthetic */ void lambda$requestHighlight$0(HighlightablePreferenceGroupAdapter highlightablePreferenceGroupAdapter, RecyclerView recyclerView) {
        int position = highlightablePreferenceGroupAdapter.getPreferenceAdapterPosition((String) highlightablePreferenceGroupAdapter.mHighlightKey);
        if (position >= 0) {
            highlightablePreferenceGroupAdapter.mHighlightRequested = true;
            recyclerView.smoothScrollToPosition(position);
            highlightablePreferenceGroupAdapter.mHighlightPosition = position;
            highlightablePreferenceGroupAdapter.notifyItemChanged(position);
        }
    }

    public boolean isHighlightRequested() {
        return this.mHighlightRequested;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void requestRemoveHighlightDelayed(View v) {
        v.postDelayed(new -$$Lambda$HighlightablePreferenceGroupAdapter$CKVsKq8Jy7vb9RpitMwq8ps1ehE(this, v), HIGHLIGHT_DURATION);
    }

    public static /* synthetic */ void lambda$requestRemoveHighlightDelayed$1(HighlightablePreferenceGroupAdapter highlightablePreferenceGroupAdapter, View v) {
        highlightablePreferenceGroupAdapter.mHighlightPosition = -1;
        highlightablePreferenceGroupAdapter.removeHighlightBackground(v, true);
    }

    private void addHighlightBackground(View v, boolean animate) {
        v.setTag(R.id.preference_highlighted, Boolean.valueOf(true));
        if (animate) {
            this.mFadeInAnimated = true;
            int colorTo = this.mHighlightColor;
            ValueAnimator fadeInLoop = ValueAnimator.ofObject(new ArgbEvaluator(), new Object[]{Integer.valueOf(-1), Integer.valueOf(colorTo)});
            fadeInLoop.setDuration(HIGHLIGHT_FADE_IN_DURATION);
            fadeInLoop.addUpdateListener(new -$$Lambda$HighlightablePreferenceGroupAdapter$piymLpeUf2m74Yz5ep7jpdxw2ho(v));
            fadeInLoop.setRepeatMode(2);
            fadeInLoop.setRepeatCount(4);
            fadeInLoop.start();
            Log.d(TAG, "AddHighlight: starting fade in animation");
            requestRemoveHighlightDelayed(v);
            return;
        }
        v.setBackgroundColor(this.mHighlightColor);
        Log.d(TAG, "AddHighlight: Not animation requested - setting highlight background");
        requestRemoveHighlightDelayed(v);
    }

    private void removeHighlightBackground(final View v, boolean animate) {
        if (!animate) {
            v.setTag(R.id.preference_highlighted, Boolean.valueOf(false));
            v.setBackgroundResource(this.mNormalBackgroundRes);
            Log.d(TAG, "RemoveHighlight: No animation requested - setting normal background");
        } else if (Boolean.TRUE.equals(v.getTag(R.id.preference_highlighted))) {
            int colorFrom = this.mHighlightColor;
            v.setTag(R.id.preference_highlighted, Boolean.valueOf(false));
            ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), new Object[]{Integer.valueOf(colorFrom), Integer.valueOf(-1)});
            colorAnimation.setDuration(HIGHLIGHT_FADE_OUT_DURATION);
            colorAnimation.addUpdateListener(new -$$Lambda$HighlightablePreferenceGroupAdapter$HMY634RMu5R2WoggcFMdrEe8uA0(v));
            colorAnimation.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    v.setBackgroundResource(HighlightablePreferenceGroupAdapter.this.mNormalBackgroundRes);
                }
            });
            colorAnimation.start();
            Log.d(TAG, "Starting fade out animation");
        } else {
            Log.d(TAG, "RemoveHighlight: Not highlighted - skipping");
        }
    }
}
