package com.google.tagmanager;

import com.google.android.gms.common.util.VisibleForTesting;

class CtfeHost {
    private static final String CTFE_SERVER_ADDRESS = "https://www.googletagmanager.com";
    @VisibleForTesting
    static final String CTFE_URL_PATH_PREFIX = "/d?";
    static final String DEBUG_EVENT_NUMBER_QUERY = "&event_number=";
    private String mCtfeServerAddress = CTFE_SERVER_ADDRESS;

    public void setCtfeServerAddress(String newCtfeAddress) {
        this.mCtfeServerAddress = newCtfeAddress;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("The Ctfe server endpoint was changed to: ");
        stringBuilder.append(newCtfeAddress);
        Log.i(stringBuilder.toString());
    }

    public String getCtfeServerAddress() {
        return this.mCtfeServerAddress;
    }

    /* Access modifiers changed, original: 0000 */
    public String constructCtfeDebugUrl(int currentEventNumber) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.mCtfeServerAddress);
        stringBuilder.append(CTFE_URL_PATH_PREFIX);
        stringBuilder.append(PreviewManager.getInstance().getCTFEUrlDebugQuery());
        stringBuilder.append(DEBUG_EVENT_NUMBER_QUERY);
        stringBuilder.append(currentEventNumber);
        return stringBuilder.toString();
    }
}
