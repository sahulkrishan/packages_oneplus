package android.support.v17.preference;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v14.preference.PreferenceFragment;
import android.support.v17.leanback.widget.VerticalGridView;
import android.support.v7.preference.PreferenceRecyclerViewAccessibilityDelegate;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

public abstract class BaseLeanbackPreferenceFragment extends PreferenceFragment {
    public RecyclerView onCreateRecyclerView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        VerticalGridView verticalGridView = (VerticalGridView) inflater.inflate(R.layout.leanback_preferences_list, parent, false);
        verticalGridView.setWindowAlignment(3);
        verticalGridView.setFocusScrollStrategy(0);
        verticalGridView.setAccessibilityDelegateCompat(new PreferenceRecyclerViewAccessibilityDelegate(verticalGridView));
        return verticalGridView;
    }

    @RestrictTo({Scope.LIBRARY_GROUP})
    public Fragment getCallbackFragment() {
        return getParentFragment();
    }
}
