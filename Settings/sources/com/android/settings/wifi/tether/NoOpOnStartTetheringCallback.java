package com.android.settings.wifi.tether;

import android.net.ConnectivityManager.OnStartTetheringCallback;

class NoOpOnStartTetheringCallback {
    NoOpOnStartTetheringCallback() {
    }

    public static OnStartTetheringCallback newInstance() {
        return new OnStartTetheringCallback() {
        };
    }
}
