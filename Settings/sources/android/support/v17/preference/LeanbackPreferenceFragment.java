package android.support.v17.preference;

import android.os.Build.VERSION;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public abstract class LeanbackPreferenceFragment extends BaseLeanbackPreferenceFragment {
    public LeanbackPreferenceFragment() {
        if (VERSION.SDK_INT >= 21) {
            LeanbackPreferenceFragmentTransitionHelperApi21.addTransitions(this);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.leanback_preference_fragment, container, false);
        ViewGroup innerContainer = (ViewGroup) view.findViewById(R.id.main_frame);
        View innerView = super.onCreateView(inflater, innerContainer, savedInstanceState);
        if (innerView != null) {
            innerContainer.addView(innerView);
        }
        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle(getPreferenceScreen().getTitle());
    }

    public void setTitle(CharSequence title) {
        TextView decorTitle;
        View view = getView();
        if (view == null) {
            decorTitle = null;
        } else {
            decorTitle = (TextView) view.findViewById(R.id.decor_title);
        }
        if (decorTitle != null) {
            decorTitle.setText(title);
        }
    }
}
