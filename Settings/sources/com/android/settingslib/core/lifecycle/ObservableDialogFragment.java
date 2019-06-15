package com.android.settingslib.core.lifecycle;

import android.app.DialogFragment;
import android.arch.lifecycle.Lifecycle.Event;
import android.arch.lifecycle.LifecycleOwner;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class ObservableDialogFragment extends DialogFragment implements LifecycleOwner {
    protected final Lifecycle mLifecycle = new Lifecycle(this);

    public void onAttach(Context context) {
        super.onAttach(context);
        this.mLifecycle.onAttach(context);
    }

    public void onCreate(Bundle savedInstanceState) {
        this.mLifecycle.onCreate(savedInstanceState);
        this.mLifecycle.handleLifecycleEvent(Event.ON_CREATE);
        super.onCreate(savedInstanceState);
    }

    public void onStart() {
        this.mLifecycle.handleLifecycleEvent(Event.ON_START);
        super.onStart();
    }

    public void onResume() {
        this.mLifecycle.handleLifecycleEvent(Event.ON_RESUME);
        super.onResume();
    }

    public void onPause() {
        this.mLifecycle.handleLifecycleEvent(Event.ON_PAUSE);
        super.onPause();
    }

    public void onStop() {
        this.mLifecycle.handleLifecycleEvent(Event.ON_STOP);
        super.onStop();
    }

    public void onDestroy() {
        this.mLifecycle.handleLifecycleEvent(Event.ON_DESTROY);
        super.onDestroy();
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        this.mLifecycle.onCreateOptionsMenu(menu, inflater);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void onPrepareOptionsMenu(Menu menu) {
        this.mLifecycle.onPrepareOptionsMenu(menu);
        super.onPrepareOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        boolean lifecycleHandled = this.mLifecycle.onOptionsItemSelected(menuItem);
        if (lifecycleHandled) {
            return lifecycleHandled;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    public Lifecycle getLifecycle() {
        return this.mLifecycle;
    }
}
