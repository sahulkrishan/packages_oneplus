package com.android.settings.wifi.p2p;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pGroupList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.net.wifi.p2p.WifiP2pManager.PersistentGroupInfoListener;
import android.os.Bundle;
import android.os.SystemProperties;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settingslib.core.AbstractPreferenceController;
import java.util.ArrayList;
import java.util.List;

public class WifiP2pSettings extends DashboardFragment implements PersistentGroupInfoListener, PeerListListener {
    private static final boolean DBG = false;
    private static final int DIALOG_CANCEL_CONNECT = 2;
    private static final int DIALOG_DELETE_GROUP = 4;
    private static final int DIALOG_DISCONNECT = 1;
    private static final int DIALOG_RENAME = 3;
    private static final String SAVE_DEVICE_NAME = "DEV_NAME";
    private static final String SAVE_DIALOG_PEER = "PEER_STATE";
    private static final String SAVE_SELECTED_GROUP = "GROUP_NAME";
    private static final String TAG = "WifiP2pSettings";
    private OnClickListener mCancelConnectListener;
    private Channel mChannel;
    private int mConnectedDevices;
    private OnClickListener mDeleteGroupListener;
    private EditText mDeviceNameText;
    private OnClickListener mDisconnectListener;
    private final IntentFilter mIntentFilter = new IntentFilter();
    private boolean mLastGroupFormed = false;
    private P2pPeerCategoryPreferenceController mPeerCategoryController;
    private WifiP2pDeviceList mPeers = new WifiP2pDeviceList();
    private P2pPersistentCategoryPreferenceController mPersistentCategoryController;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            boolean z = false;
            if ("android.net.wifi.p2p.STATE_CHANGED".equals(action)) {
                WifiP2pSettings wifiP2pSettings = WifiP2pSettings.this;
                if (intent.getIntExtra("wifi_p2p_state", 1) == 2) {
                    z = true;
                }
                wifiP2pSettings.mWifiP2pEnabled = z;
                WifiP2pSettings.this.handleP2pStateChanged();
            } else if ("android.net.wifi.p2p.PEERS_CHANGED".equals(action)) {
                WifiP2pSettings.this.mPeers = (WifiP2pDeviceList) intent.getParcelableExtra("wifiP2pDeviceList");
                WifiP2pSettings.this.handlePeersChanged();
            } else if ("android.net.wifi.p2p.CONNECTION_STATE_CHANGE".equals(action)) {
                if (WifiP2pSettings.this.mWifiP2pManager != null) {
                    WifiP2pInfo wifip2pinfo = (WifiP2pInfo) intent.getParcelableExtra("wifiP2pInfo");
                    if (!(((NetworkInfo) intent.getParcelableExtra("networkInfo")).isConnected() || WifiP2pSettings.this.mLastGroupFormed)) {
                        WifiP2pSettings.this.startSearch();
                    }
                    WifiP2pSettings.this.mLastGroupFormed = wifip2pinfo.groupFormed;
                }
            } else if ("android.net.wifi.p2p.THIS_DEVICE_CHANGED".equals(action)) {
                WifiP2pSettings.this.mThisDevice = (WifiP2pDevice) intent.getParcelableExtra("wifiP2pDevice");
                WifiP2pSettings.this.mThisDevicePreferenceController.updateDeviceName(WifiP2pSettings.this.mThisDevice);
            } else if ("android.net.wifi.p2p.DISCOVERY_STATE_CHANGE".equals(action)) {
                if (intent.getIntExtra("discoveryState", 1) == 2) {
                    WifiP2pSettings.this.updateSearchMenu(true);
                } else {
                    WifiP2pSettings.this.updateSearchMenu(false);
                }
            } else if ("android.net.wifi.p2p.PERSISTENT_GROUPS_CHANGED".equals(action) && WifiP2pSettings.this.mWifiP2pManager != null) {
                WifiP2pSettings.this.mWifiP2pManager.requestPersistentGroupInfo(WifiP2pSettings.this.mChannel, WifiP2pSettings.this);
            }
        }
    };
    private OnClickListener mRenameListener;
    private String mSavedDeviceName;
    private WifiP2pPersistentGroup mSelectedGroup;
    private String mSelectedGroupName;
    private WifiP2pPeer mSelectedWifiPeer;
    private WifiP2pDevice mThisDevice;
    private P2pThisDevicePreferenceController mThisDevicePreferenceController;
    private boolean mWifiP2pEnabled;
    private WifiP2pManager mWifiP2pManager;
    private boolean mWifiP2pSearching;

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.wifi_p2p_settings;
    }

    public int getMetricsCategory() {
        return 109;
    }

    public int getHelpResource() {
        return R.string.help_url_wifi_p2p;
    }

    /* Access modifiers changed, original: protected */
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        List<AbstractPreferenceController> controllers = new ArrayList();
        this.mPersistentCategoryController = new P2pPersistentCategoryPreferenceController(context);
        this.mPeerCategoryController = new P2pPeerCategoryPreferenceController(context);
        this.mThisDevicePreferenceController = new P2pThisDevicePreferenceController(context);
        controllers.add(this.mPersistentCategoryController);
        controllers.add(this.mPeerCategoryController);
        controllers.add(this.mThisDevicePreferenceController);
        return controllers;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        Activity activity = getActivity();
        this.mWifiP2pManager = (WifiP2pManager) getSystemService("wifip2p");
        if (this.mWifiP2pManager != null) {
            this.mChannel = this.mWifiP2pManager.initialize(activity.getApplicationContext(), getActivity().getMainLooper(), null);
            if (this.mChannel == null) {
                Log.e(TAG, "Failed to set up connection with wifi p2p service");
                this.mWifiP2pManager = null;
            }
        } else {
            Log.e(TAG, "mWifiP2pManager is null !");
        }
        if (savedInstanceState != null && savedInstanceState.containsKey(SAVE_DIALOG_PEER)) {
            this.mSelectedWifiPeer = new WifiP2pPeer(getPrefContext(), (WifiP2pDevice) savedInstanceState.getParcelable(SAVE_DIALOG_PEER));
        }
        if (savedInstanceState != null && savedInstanceState.containsKey(SAVE_DEVICE_NAME)) {
            this.mSavedDeviceName = savedInstanceState.getString(SAVE_DEVICE_NAME);
        }
        if (savedInstanceState != null && savedInstanceState.containsKey(SAVE_SELECTED_GROUP)) {
            this.mSelectedGroupName = savedInstanceState.getString(SAVE_SELECTED_GROUP);
        }
        this.mRenameListener = new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == -1 && WifiP2pSettings.this.mWifiP2pManager != null) {
                    String name = WifiP2pSettings.this.mDeviceNameText.getText().toString();
                    if (name != null) {
                        int i = 0;
                        while (i < name.length()) {
                            char cur = name.charAt(i);
                            if (Character.isDigit(cur) || Character.isLetter(cur) || cur == '-' || cur == '_' || cur == ' ') {
                                i++;
                            } else {
                                Toast.makeText(WifiP2pSettings.this.getActivity(), R.string.wifi_p2p_failed_rename_message, 1).show();
                                return;
                            }
                        }
                    }
                    WifiP2pSettings.this.mWifiP2pManager.setDeviceName(WifiP2pSettings.this.mChannel, WifiP2pSettings.this.mDeviceNameText.getText().toString(), new ActionListener() {
                        public void onSuccess() {
                        }

                        public void onFailure(int reason) {
                            Toast.makeText(WifiP2pSettings.this.getActivity(), R.string.wifi_p2p_failed_rename_message, 1).show();
                        }
                    });
                }
            }
        };
        this.mDisconnectListener = new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == -1 && WifiP2pSettings.this.mWifiP2pManager != null) {
                    WifiP2pSettings.this.mWifiP2pManager.removeGroup(WifiP2pSettings.this.mChannel, new ActionListener() {
                        public void onSuccess() {
                        }

                        public void onFailure(int reason) {
                        }
                    });
                }
            }
        };
        this.mCancelConnectListener = new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == -1 && WifiP2pSettings.this.mWifiP2pManager != null) {
                    WifiP2pSettings.this.mWifiP2pManager.cancelConnect(WifiP2pSettings.this.mChannel, new ActionListener() {
                        public void onSuccess() {
                        }

                        public void onFailure(int reason) {
                        }
                    });
                }
            }
        };
        this.mDeleteGroupListener = new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == -1) {
                    if (WifiP2pSettings.this.mWifiP2pManager != null && WifiP2pSettings.this.mSelectedGroup != null) {
                        WifiP2pSettings.this.mWifiP2pManager.deletePersistentGroup(WifiP2pSettings.this.mChannel, WifiP2pSettings.this.mSelectedGroup.getNetworkId(), new ActionListener() {
                            public void onSuccess() {
                            }

                            public void onFailure(int reason) {
                            }
                        });
                        WifiP2pSettings.this.mSelectedGroup = null;
                    }
                } else if (which == -2) {
                    WifiP2pSettings.this.mSelectedGroup = null;
                }
            }
        };
        super.onActivityCreated(savedInstanceState);
    }

    public void onResume() {
        super.onResume();
        this.mIntentFilter.addAction("android.net.wifi.p2p.STATE_CHANGED");
        this.mIntentFilter.addAction("android.net.wifi.p2p.PEERS_CHANGED");
        this.mIntentFilter.addAction("android.net.wifi.p2p.CONNECTION_STATE_CHANGE");
        this.mIntentFilter.addAction("android.net.wifi.p2p.THIS_DEVICE_CHANGED");
        this.mIntentFilter.addAction("android.net.wifi.p2p.DISCOVERY_STATE_CHANGE");
        this.mIntentFilter.addAction("android.net.wifi.p2p.PERSISTENT_GROUPS_CHANGED");
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        getActivity().registerReceiver(this.mReceiver, this.mIntentFilter);
        if (this.mWifiP2pManager != null) {
            this.mWifiP2pManager.requestPeers(this.mChannel, this);
        }
    }

    public void onPause() {
        super.onPause();
        if (this.mWifiP2pManager != null) {
            this.mWifiP2pManager.stopPeerDiscovery(this.mChannel, null);
        }
        getActivity().unregisterReceiver(this.mReceiver);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.wifi_p2p_settings, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem searchMenu = menu.findItem(R.id.action_search);
        MenuItem renameMenu = menu.findItem(R.id.action_rename);
        if (this.mWifiP2pEnabled) {
            searchMenu.setEnabled(true);
            renameMenu.setEnabled(true);
        } else {
            searchMenu.setEnabled(false);
            renameMenu.setEnabled(false);
        }
        if (this.mWifiP2pSearching) {
            searchMenu.setTitle(R.string.wifi_p2p_menu_searching);
        } else {
            searchMenu.setTitle(R.string.wifi_p2p_menu_search);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_rename /*2131361839*/:
                startFragment(this, "com.oneplus.settings.OPDeviceName", R.string.oneplus_mobile_device_name_title, -1, null);
                return true;
            case R.id.action_search /*2131361840*/:
                startSearch();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference instanceof WifiP2pPeer) {
            this.mSelectedWifiPeer = (WifiP2pPeer) preference;
            if (this.mSelectedWifiPeer.device.status == 0) {
                showDialog(1);
            } else if (this.mSelectedWifiPeer.device.status == 1) {
                showDialog(2);
            } else {
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = this.mSelectedWifiPeer.device.deviceAddress;
                int forceWps = SystemProperties.getInt("wifidirect.wps", -1);
                if (forceWps != -1) {
                    config.wps.setup = forceWps;
                } else if (this.mSelectedWifiPeer.device.wpsPbcSupported()) {
                    config.wps.setup = 0;
                } else if (this.mSelectedWifiPeer.device.wpsKeypadSupported()) {
                    config.wps.setup = 2;
                } else {
                    config.wps.setup = 1;
                }
                this.mWifiP2pManager.connect(this.mChannel, config, new ActionListener() {
                    public void onSuccess() {
                    }

                    public void onFailure(int reason) {
                        String str = WifiP2pSettings.TAG;
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(" connect fail ");
                        stringBuilder.append(reason);
                        Log.e(str, stringBuilder.toString());
                        Toast.makeText(WifiP2pSettings.this.getActivity(), R.string.wifi_p2p_failed_connect_message, 0).show();
                    }
                });
            }
        } else if (preference instanceof WifiP2pPersistentGroup) {
            this.mSelectedGroup = (WifiP2pPersistentGroup) preference;
            showDialog(4);
        }
        return super.onPreferenceTreeClick(preference);
    }

    public Dialog onCreateDialog(int id) {
        String deviceName;
        if (id == 1) {
            String msg;
            if (TextUtils.isEmpty(this.mSelectedWifiPeer.device.deviceName)) {
                deviceName = this.mSelectedWifiPeer.device.deviceAddress;
            } else {
                deviceName = this.mSelectedWifiPeer.device.deviceName;
            }
            if (this.mConnectedDevices > 1) {
                msg = getActivity().getString(R.string.wifi_p2p_disconnect_multiple_message, new Object[]{deviceName, Integer.valueOf(this.mConnectedDevices - 1)});
            } else {
                msg = getActivity().getString(R.string.wifi_p2p_disconnect_message, new Object[]{deviceName});
            }
            return new Builder(getActivity()).setTitle(R.string.wifi_p2p_disconnect_title).setMessage(msg).setPositiveButton(getActivity().getString(R.string.dlg_ok), this.mDisconnectListener).setNegativeButton(getActivity().getString(R.string.dlg_cancel), null).create();
        } else if (id == 2) {
            if (TextUtils.isEmpty(this.mSelectedWifiPeer.device.deviceName)) {
                deviceName = this.mSelectedWifiPeer.device.deviceAddress;
            } else {
                deviceName = this.mSelectedWifiPeer.device.deviceName;
            }
            return new Builder(getActivity()).setTitle(R.string.wifi_p2p_cancel_connect_title).setMessage(getActivity().getString(R.string.wifi_p2p_cancel_connect_message, new Object[]{deviceName})).setPositiveButton(getActivity().getString(R.string.dlg_ok), this.mCancelConnectListener).setNegativeButton(getActivity().getString(R.string.dlg_cancel), null).create();
        } else if (id == 3) {
            this.mDeviceNameText = new EditText(getActivity());
            this.mDeviceNameText.setFilters(new InputFilter[]{new LengthFilter(30)});
            if (this.mSavedDeviceName != null) {
                this.mDeviceNameText.setText(this.mSavedDeviceName);
                this.mDeviceNameText.setSelection(this.mSavedDeviceName.length());
            } else if (!(this.mThisDevice == null || TextUtils.isEmpty(this.mThisDevice.deviceName))) {
                this.mDeviceNameText.setText(this.mThisDevice.deviceName);
                this.mDeviceNameText.setSelection(0, this.mThisDevice.deviceName.length());
            }
            this.mSavedDeviceName = null;
            return new Builder(getActivity()).setTitle(R.string.wifi_p2p_menu_rename).setView(this.mDeviceNameText).setPositiveButton(getActivity().getString(R.string.dlg_ok), this.mRenameListener).setNegativeButton(getActivity().getString(R.string.dlg_cancel), null).create();
        } else if (id == 4) {
            return new Builder(getActivity()).setMessage(getActivity().getString(R.string.wifi_p2p_delete_group_message)).setPositiveButton(getActivity().getString(R.string.dlg_ok), this.mDeleteGroupListener).setNegativeButton(getActivity().getString(R.string.dlg_cancel), this.mDeleteGroupListener).create();
        } else {
            return null;
        }
    }

    public int getDialogMetricsCategory(int dialogId) {
        switch (dialogId) {
            case 1:
                return 575;
            case 2:
                return 576;
            case 3:
                return 577;
            case 4:
                return 578;
            default:
                return 0;
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        if (this.mSelectedWifiPeer != null) {
            outState.putParcelable(SAVE_DIALOG_PEER, this.mSelectedWifiPeer.device);
        }
        if (this.mDeviceNameText != null) {
            outState.putString(SAVE_DEVICE_NAME, this.mDeviceNameText.getText().toString());
        }
        if (this.mSelectedGroup != null) {
            outState.putString(SAVE_SELECTED_GROUP, this.mSelectedGroup.getGroupName());
        }
        super.onSaveInstanceState(outState);
    }

    private void handlePeersChanged() {
        this.mPeerCategoryController.removeAllChildren();
        this.mConnectedDevices = 0;
        for (WifiP2pDevice peer : this.mPeers.getDeviceList()) {
            this.mPeerCategoryController.addChild(new WifiP2pPeer(getPrefContext(), peer));
            if (peer.status == 0) {
                this.mConnectedDevices++;
            }
        }
    }

    public void onPersistentGroupInfoAvailable(WifiP2pGroupList groups) {
        this.mPersistentCategoryController.removeAllChildren();
        for (WifiP2pGroup group : groups.getGroupList()) {
            WifiP2pPersistentGroup wppg = new WifiP2pPersistentGroup(getPrefContext(), group);
            this.mPersistentCategoryController.addChild(wppg);
            if (wppg.getGroupName() != null && wppg.getGroupName().equals(this.mSelectedGroupName)) {
                this.mSelectedGroup = wppg;
                this.mSelectedGroupName = null;
            }
        }
        if (this.mSelectedGroupName != null) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(" Selected group ");
            stringBuilder.append(this.mSelectedGroupName);
            stringBuilder.append(" disappered on next query ");
            Log.w(str, stringBuilder.toString());
        }
    }

    public void onPeersAvailable(WifiP2pDeviceList peers) {
        this.mPeers = peers;
        handlePeersChanged();
    }

    private void handleP2pStateChanged() {
        updateSearchMenu(false);
        this.mThisDevicePreferenceController.setEnabled(this.mWifiP2pEnabled);
        this.mPersistentCategoryController.setEnabled(this.mWifiP2pEnabled);
        this.mPeerCategoryController.setEnabled(this.mWifiP2pEnabled);
    }

    private void updateSearchMenu(boolean searching) {
        this.mWifiP2pSearching = searching;
        Activity activity = getActivity();
        if (activity != null) {
            activity.invalidateOptionsMenu();
        }
    }

    private void startSearch() {
        if (this.mWifiP2pManager != null && !this.mWifiP2pSearching) {
            this.mWifiP2pManager.discoverPeers(this.mChannel, new ActionListener() {
                public void onSuccess() {
                }

                public void onFailure(int reason) {
                }
            });
        }
    }
}
