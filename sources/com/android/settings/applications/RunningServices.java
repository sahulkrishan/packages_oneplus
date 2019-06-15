package com.android.settings.applications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.widget.LoadingViewController;

public class RunningServices extends SettingsPreferenceFragment {
    private static final int SHOW_BACKGROUND_PROCESSES = 2;
    private static final int SHOW_RUNNING_SERVICES = 1;
    private View mLoadingContainer;
    private LoadingViewController mLoadingViewController;
    private Menu mOptionsMenu;
    private final Runnable mRunningProcessesAvail = new Runnable() {
        public void run() {
            RunningServices.this.mLoadingViewController.showContent(true);
        }
    };
    private RunningProcessesView mRunningProcessesView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(R.string.runningservices_settings_title);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.manage_applications_running, null);
        this.mRunningProcessesView = (RunningProcessesView) rootView.findViewById(R.id.running_processes);
        this.mRunningProcessesView.doCreate();
        this.mLoadingContainer = rootView.findViewById(R.id.loading_container);
        this.mLoadingViewController = new LoadingViewController(this.mLoadingContainer, this.mRunningProcessesView);
        return rootView;
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        this.mOptionsMenu = menu;
        menu.add(0, 1, 1, R.string.show_running_services).setShowAsAction(0);
        menu.add(0, 2, 2, R.string.show_background_processes).setShowAsAction(0);
        updateOptionsMenu();
    }

    public void onResume() {
        super.onResume();
        this.mLoadingViewController.handleLoadingContainer(this.mRunningProcessesView.doResume(this, this.mRunningProcessesAvail), false);
    }

    public void onPause() {
        super.onPause();
        this.mRunningProcessesView.doPause();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                this.mRunningProcessesView.mAdapter.setShowBackground(false);
                break;
            case 2:
                this.mRunningProcessesView.mAdapter.setShowBackground(true);
                break;
            default:
                return false;
        }
        updateOptionsMenu();
        return true;
    }

    public void onPrepareOptionsMenu(Menu menu) {
        updateOptionsMenu();
    }

    private void updateOptionsMenu() {
        boolean showingBackground = this.mRunningProcessesView.mAdapter.getShowBackground();
        this.mOptionsMenu.findItem(1).setVisible(showingBackground);
        this.mOptionsMenu.findItem(2).setVisible(showingBackground ^ 1);
    }

    public int getMetricsCategory() {
        return 404;
    }
}
