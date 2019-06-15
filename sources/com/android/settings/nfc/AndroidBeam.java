package com.android.settings.nfc;

import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.core.InstrumentedFragment;
import com.android.settings.enterprise.ActionDisabledByAdminDialogHelper;
import com.android.settings.widget.SwitchBar;
import com.android.settings.widget.SwitchBar.OnSwitchChangeListener;
import com.android.settingslib.HelpUtils;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;

public class AndroidBeam extends InstrumentedFragment implements OnSwitchChangeListener {
    private boolean mBeamDisallowedByBase;
    private boolean mBeamDisallowedByOnlyAdmin;
    private NfcAdapter mNfcAdapter;
    private CharSequence mOldActivityTitle;
    private SwitchBar mSwitchBar;
    private View mView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mNfcAdapter = NfcAdapter.getDefaultAdapter(getActivity());
        setHasOptionsMenu(true);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        HelpUtils.prepareHelpMenuItem(getActivity(), menu, (int) R.string.help_uri_beam, getClass().getName());
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        EnforcedAdmin admin = RestrictedLockUtils.checkIfRestrictionEnforced(getActivity(), "no_outgoing_beam", UserHandle.myUserId());
        UserManager um = UserManager.get(getActivity());
        this.mBeamDisallowedByBase = RestrictedLockUtils.hasBaseUserRestriction(getActivity(), "no_outgoing_beam", UserHandle.myUserId());
        if (this.mBeamDisallowedByBase || admin == null) {
            this.mView = inflater.inflate(R.layout.android_beam, container, false);
            return this.mView;
        }
        new ActionDisabledByAdminDialogHelper(getActivity()).prepareDialogBuilder("no_outgoing_beam", admin).show();
        this.mBeamDisallowedByOnlyAdmin = true;
        return new View(getContext());
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SettingsActivity activity = (SettingsActivity) getActivity();
        this.mOldActivityTitle = activity.getActionBar().getTitle();
        this.mSwitchBar = activity.getSwitchBar();
        if (this.mBeamDisallowedByOnlyAdmin) {
            this.mSwitchBar.hide();
        } else {
            SwitchBar switchBar = this.mSwitchBar;
            boolean z = !this.mBeamDisallowedByBase && this.mNfcAdapter.isNdefPushEnabled();
            switchBar.setChecked(z);
            this.mSwitchBar.addOnSwitchChangeListener(this);
            this.mSwitchBar.setEnabled(this.mBeamDisallowedByBase ^ 1);
            this.mSwitchBar.show();
        }
        activity.setTitle(R.string.android_beam_settings_title);
    }

    public void onDestroyView() {
        super.onDestroyView();
        if (this.mOldActivityTitle != null) {
            getActivity().getActionBar().setTitle(this.mOldActivityTitle);
        }
        if (!this.mBeamDisallowedByOnlyAdmin) {
            this.mSwitchBar.removeOnSwitchChangeListener(this);
            this.mSwitchBar.hide();
        }
    }

    public void onSwitchChanged(Switch switchView, boolean desiredState) {
        boolean success;
        this.mSwitchBar.setEnabled(false);
        if (desiredState) {
            success = this.mNfcAdapter.enableNdefPush();
        } else {
            success = this.mNfcAdapter.disableNdefPush();
        }
        if (success) {
            this.mSwitchBar.setChecked(desiredState);
        }
        this.mSwitchBar.setEnabled(true);
    }

    public int getMetricsCategory() {
        return 69;
    }
}
