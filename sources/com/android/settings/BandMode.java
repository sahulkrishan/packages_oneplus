package com.android.settings;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;

public class BandMode extends Activity {
    private static final String[] BAND_NAMES = new String[]{"Automatic", "Europe", "United States", "Japan", "Australia", "Australia 2", "Cellular 800", "PCS", "Class 3 (JTACS)", "Class 4 (Korea-PCS)", "Class 5", "Class 6 (IMT2000)", "Class 7 (700Mhz-Upper)", "Class 8 (1800Mhz-Upper)", "Class 9 (900Mhz)", "Class 10 (800Mhz-Secondary)", "Class 11 (Europe PAMR 400Mhz)", "Class 15 (US-AWS)", "Class 16 (US-2500Mhz)"};
    private static final boolean DBG = false;
    private static final int EVENT_BAND_SCAN_COMPLETED = 100;
    private static final int EVENT_BAND_SELECTION_DONE = 200;
    private static final String LOG_TAG = "phone";
    private ListView mBandList;
    private ArrayAdapter mBandListAdapter;
    private OnItemClickListener mBandSelectionHandler = new OnItemClickListener() {
        public void onItemClick(AdapterView parent, View v, int position, long id) {
            BandMode.this.getWindow().setFeatureInt(5, -1);
            BandMode.this.mTargetBand = (BandListItem) parent.getAdapter().getItem(position);
            BandMode.this.mPhone.setBandMode(BandMode.this.mTargetBand.getBand(), BandMode.this.mHandler.obtainMessage(200));
        }
    };
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 100) {
                BandMode.this.bandListLoaded((AsyncResult) msg.obj);
            } else if (i == 200) {
                AsyncResult ar = msg.obj;
                BandMode.this.getWindow().setFeatureInt(5, -2);
                if (!BandMode.this.isFinishing()) {
                    BandMode.this.displayBandSelectionResult(ar.exception);
                }
            }
        }
    };
    private Phone mPhone = null;
    private DialogInterface mProgressPanel;
    private BandListItem mTargetBand = null;

    private static class BandListItem {
        private int mBandMode = 0;

        public BandListItem(int bm) {
            this.mBandMode = bm;
        }

        public int getBand() {
            return this.mBandMode;
        }

        public String toString() {
            if (this.mBandMode < BandMode.BAND_NAMES.length) {
                return BandMode.BAND_NAMES[this.mBandMode];
            }
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Band mode ");
            stringBuilder.append(this.mBandMode);
            return stringBuilder.toString();
        }
    }

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        requestWindowFeature(5);
        setContentView(R.layout.band_mode);
        setTitle(getString(R.string.band_mode_title));
        getWindow().setLayout(-1, -2);
        this.mPhone = PhoneFactory.getDefaultPhone();
        this.mBandList = (ListView) findViewById(R.id.band);
        this.mBandListAdapter = new ArrayAdapter(this, 17367043);
        this.mBandList.setAdapter(this.mBandListAdapter);
        this.mBandList.setOnItemClickListener(this.mBandSelectionHandler);
        loadBandList();
    }

    private void loadBandList() {
        this.mProgressPanel = new Builder(this).setMessage(getString(R.string.band_mode_loading)).show();
        this.mPhone.queryAvailableBandMode(this.mHandler.obtainMessage(100));
    }

    private void bandListLoaded(AsyncResult result) {
        if (this.mProgressPanel != null) {
            this.mProgressPanel.dismiss();
        }
        clearList();
        boolean addBandSuccess = false;
        int i = 0;
        if (result.result != null) {
            int[] bands = result.result;
            if (bands.length == 0) {
                Log.wtf(LOG_TAG, "No Supported Band Modes");
                return;
            }
            int size = bands[0];
            if (size > 0) {
                this.mBandListAdapter.add(new BandListItem(0));
                for (int i2 = 1; i2 <= size; i2++) {
                    if (bands[i2] != 0) {
                        this.mBandListAdapter.add(new BandListItem(bands[i2]));
                    }
                }
                addBandSuccess = true;
            }
        }
        if (!addBandSuccess) {
            while (true) {
                int i3 = i;
                if (i3 >= 19) {
                    break;
                }
                this.mBandListAdapter.add(new BandListItem(i3));
                i = i3 + 1;
            }
        }
        this.mBandList.requestFocus();
    }

    private void displayBandSelectionResult(Throwable ex) {
        String status = new StringBuilder();
        status.append(getString(R.string.band_mode_set));
        status.append(" [");
        status.append(this.mTargetBand.toString());
        status.append("] ");
        status = status.toString();
        StringBuilder stringBuilder;
        if (ex != null) {
            stringBuilder = new StringBuilder();
            stringBuilder.append(status);
            stringBuilder.append(getString(R.string.band_mode_failed));
            status = stringBuilder.toString();
        } else {
            stringBuilder = new StringBuilder();
            stringBuilder.append(status);
            stringBuilder.append(getString(R.string.band_mode_succeeded));
            status = stringBuilder.toString();
        }
        this.mProgressPanel = new Builder(this).setMessage(status).setPositiveButton(17039370, null).show();
    }

    private void clearList() {
        while (this.mBandListAdapter.getCount() > 0) {
            this.mBandListAdapter.remove(this.mBandListAdapter.getItem(0));
        }
    }

    private void log(String msg) {
        String str = LOG_TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[BandsList] ");
        stringBuilder.append(msg);
        Log.d(str, stringBuilder.toString());
    }
}
