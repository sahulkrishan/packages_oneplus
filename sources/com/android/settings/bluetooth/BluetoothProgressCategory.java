package com.android.settings.bluetooth;

import android.content.Context;
import android.util.AttributeSet;
import com.android.settings.ProgressCategory;
import com.android.settings.R;

public class BluetoothProgressCategory extends ProgressCategory {
    public BluetoothProgressCategory(Context context) {
        super(context);
        init();
    }

    public BluetoothProgressCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BluetoothProgressCategory(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public BluetoothProgressCategory(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setEmptyTextRes(R.string.bluetooth_no_devices_found);
    }
}
