package com.android.settingslib.core.lifecycle;

import android.app.Activity;
import android.arch.lifecycle.Lifecycle.Event;
import android.arch.lifecycle.LifecycleOwner;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.Menu;
import android.view.MenuItem;

public class ObservableActivity extends Activity implements LifecycleOwner {
    private final Lifecycle mLifecycle = new Lifecycle(this);

    public Lifecycle getLifecycle() {
        return this.mLifecycle;
    }

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        this.mLifecycle.onAttach(this);
        this.mLifecycle.onCreate(savedInstanceState);
        this.mLifecycle.handleLifecycleEvent(Event.ON_CREATE);
        super.onCreate(savedInstanceState);
    }

    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        this.mLifecycle.onAttach(this);
        this.mLifecycle.onCreate(savedInstanceState);
        this.mLifecycle.handleLifecycleEvent(Event.ON_CREATE);
        super.onCreate(savedInstanceState, persistentState);
    }

    /* Access modifiers changed, original: protected */
    public void onStart() {
        this.mLifecycle.handleLifecycleEvent(Event.ON_START);
        super.onStart();
    }

    /* Access modifiers changed, original: protected */
    public void onResume() {
        this.mLifecycle.handleLifecycleEvent(Event.ON_RESUME);
        super.onResume();
    }

    /* Access modifiers changed, original: protected */
    public void onPause() {
        this.mLifecycle.handleLifecycleEvent(Event.ON_PAUSE);
        super.onPause();
    }

    /* Access modifiers changed, original: protected */
    public void onStop() {
        this.mLifecycle.handleLifecycleEvent(Event.ON_STOP);
        super.onStop();
    }

    /* Access modifiers changed, original: protected */
    public void onDestroy() {
        this.mLifecycle.handleLifecycleEvent(Event.ON_DESTROY);
        super.onDestroy();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        if (!super.onCreateOptionsMenu(menu)) {
            return false;
        }
        this.mLifecycle.onCreateOptionsMenu(menu, null);
        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        if (!super.onPrepareOptionsMenu(menu)) {
            return false;
        }
        this.mLifecycle.onPrepareOptionsMenu(menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        boolean lifecycleHandled = this.mLifecycle.onOptionsItemSelected(menuItem);
        if (lifecycleHandled) {
            return lifecycleHandled;
        }
        return super.onOptionsItemSelected(menuItem);
    }
}
