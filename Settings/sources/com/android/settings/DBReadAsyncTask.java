package com.android.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

public class DBReadAsyncTask extends AsyncTask<Void, Void, Boolean> {
    public static final String AUTHORITY = "com.qti.smq.Feedback.provider";
    public static final String KEY_VALUE = "app_status";
    public static final String SMQ_KEY_VALUE = "app_status";
    final Uri CONTENT_URI = Uri.parse("content://com.qti.smq.Feedback.provider");
    final Uri SNAP_CONTENT_URI = Uri.withAppendedPath(this.CONTENT_URI, "smq_settings");
    Context mContext;

    public DBReadAsyncTask(Context mContext) {
        this.mContext = mContext;
    }

    /* Access modifiers changed, original: protected|varargs */
    public Boolean doInBackground(Void... params) {
        String whereClause = "key=?";
        Cursor c = this.mContext.getContentResolver().query(this.SNAP_CONTENT_URI, null, "key=?", new String[]{"app_status"}, null);
        SharedPreferences sharedPreferences = this.mContext.getSharedPreferences(SmqSettings.SMQ_PREFS_NAME, 0);
        if (c == null || c.getCount() <= 0) {
            Editor editor = sharedPreferences.edit();
            editor.putInt("app_status", 0);
            editor.commit();
        } else {
            c.moveToFirst();
            int value = c.getInt(1);
            if (sharedPreferences.getInt("app_status", 0) != value) {
                Editor editor2 = sharedPreferences.edit();
                editor2.putInt("app_status", value);
                editor2.commit();
            }
        }
        if (c != null) {
            c.close();
        }
        return Boolean.valueOf(true);
    }
}
