package com.android.settings.search;

import com.android.settings.search.IndexDatabaseHelper.IndexColumns;

public class DatabaseResultLoader {
    public static final int COLUMN_INDEX_ID = 0;
    public static final int COLUMN_INDEX_INTENT_ACTION_TARGET_PACKAGE = 8;
    public static final int COLUMN_INDEX_KEY = 10;
    public static final String[] SELECT_COLUMNS = new String[]{IndexColumns.DOCID, IndexColumns.DATA_TITLE, IndexColumns.DATA_SUMMARY_ON, IndexColumns.DATA_SUMMARY_OFF, IndexColumns.CLASS_NAME, IndexColumns.SCREEN_TITLE, "icon", IndexColumns.INTENT_ACTION, IndexColumns.INTENT_TARGET_PACKAGE, IndexColumns.INTENT_TARGET_CLASS, IndexColumns.DATA_KEY_REF, IndexColumns.PAYLOAD_TYPE, IndexColumns.PAYLOAD};
    private static final String TAG = "DatabaseResultLoader";
}
