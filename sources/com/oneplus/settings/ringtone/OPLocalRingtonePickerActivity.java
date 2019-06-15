package com.oneplus.settings.ringtone;

import android.app.ActionBar;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore.Audio.Media;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.android.settings.R;
import com.oneplus.settings.ringtone.OPLocalRingtoneAdapter.RingtoneData;
import com.oneplus.settings.utils.OPFirewallUtils;
import com.oneplus.settings.utils.OPUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class OPLocalRingtonePickerActivity extends OPRingtoneBaseActivity {
    private static final String ALARMS_PATH;
    private static final String AUDIO_FILE_SELECTION_ALL;
    private static final String AUDIO_FILE_SELECTION_PART;
    public static String AUDIO_SECTION = null;
    private static final long MINTIME = 60000;
    private static final String NOTIFICATIONS_PATH;
    private static final String[] PROJECTION;
    private static final String RECORD_PATH;
    private static final Uri RECORD_URI = Media.EXTERNAL_CONTENT_URI;
    private static final String RINGTONE_PATH;
    private static final String SDCARD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static String SECTION_AFTER = " and _data NOT LIKE '%/.%' and _data NOT LIKE '%cache%' and _data NOT LIKE '%/res/%' and _data NOT LIKE '%/plugins/%' and _data NOT LIKE '%/temp/%' and _data NOT LIKE '%/tencent/MobileQQ/qbiz/%' and _data NOT LIKE '%/tencent/MobileQQ/PhotoPlus/%' and _data NOT LIKE '%/thumb/%' and _data NOT LIKE '%/oem_log/%'";
    private static final String[] SELECTION_ARGS_ALL;
    private static final String[] SELECTION_ARGS_PART;
    private boolean isFirst = true;
    private ListView mListView;
    private TextView mNofileView;
    private OPLocalRingtoneAdapter mOPLocalRingtoneAdapter;
    private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            RingtoneData data = (RingtoneData) parent.getItemAtPosition(position);
            OPLocalRingtonePickerActivity.this.mUriForDefaultItem = data.mUri;
            OPLocalRingtonePickerActivity.this.updateChecks(OPLocalRingtonePickerActivity.this.mUriForDefaultItem);
            if (OPLocalRingtonePickerActivity.this.mSetExternalThread != null) {
                OPLocalRingtonePickerActivity.this.mSetExternalThread.stopThread();
                OPLocalRingtonePickerActivity.this.mSetExternalThread = null;
            }
            OPLocalRingtonePickerActivity.this.mSetExternalThread = new SetExternalThread(data);
            OPLocalRingtonePickerActivity.this.mSetExternalThread.start();
        }
    };
    private OnItemSelectedListener mOnItemSelectedListener = new OnItemSelectedListener() {
        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
            if (OPLocalRingtonePickerActivity.this.isFirst) {
                OPLocalRingtonePickerActivity.this.isFirst = false;
                return;
            }
            OPLocalRingtonePickerActivity.this.stopAnyPlayingRingtone();
            String name = OPLocalRingtonePickerActivity.class.getName();
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("mOnItemSelectedListener position = ");
            stringBuilder.append(position);
            Log.v(name, stringBuilder.toString());
            switch (position) {
                case 0:
                    OPLocalRingtonePickerActivity.this.startTask(0);
                    break;
                case 1:
                    OPLocalRingtonePickerActivity.this.startTask(1);
                    break;
            }
        }

        public void onNothingSelected(AdapterView<?> adapterView) {
        }
    };
    private ProgressBar mProgressBar;
    private SetExternalThread mSetExternalThread;
    private List mSystemRings = null;
    private WorkAsyncTask mWorkAsyncTask;

    class SetExternalThread extends Thread {
        private boolean isClose = false;
        private RingtoneData mPreference;

        public SetExternalThread(RingtoneData data) {
            this.mPreference = data;
        }

        public void run() {
            Uri uriItem = OPLocalRingtonePickerActivity.this.updateExternalFile(this.mPreference);
            if (!this.isClose && uriItem != null && !OPLocalRingtonePickerActivity.this.mContactsRingtone) {
                if (OPLocalRingtonePickerActivity.this.getSimId() == 2) {
                    OPRingtoneManager.setActualRingtoneUriBySubId(OPLocalRingtonePickerActivity.this.getApplicationContext(), 1, uriItem);
                } else if (OPLocalRingtonePickerActivity.this.getSimId() == 1) {
                    OPRingtoneManager.setActualRingtoneUriBySubId(OPLocalRingtonePickerActivity.this.getApplicationContext(), 0, uriItem);
                } else if (!OPLocalRingtonePickerActivity.this.isThreePart()) {
                    OPRingtoneManager.setActualDefaultRingtoneUri(OPLocalRingtonePickerActivity.this.getApplicationContext(), OPLocalRingtonePickerActivity.this.mType, uriItem);
                }
                if (OPLocalRingtonePickerActivity.this.mHasDefaultItem) {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra(OPRingtoneManager.EXTRA_RINGTONE_PICKED_URI, OPLocalRingtonePickerActivity.this.mUriForDefaultItem);
                    OPLocalRingtonePickerActivity.this.setResult(-1, resultIntent);
                } else {
                    if (OPLocalRingtonePickerActivity.this.mUriForDefaultItem.equals(uriItem)) {
                        OPRingtoneManager.updateDb(OPLocalRingtonePickerActivity.this.getApplicationContext(), uriItem, OPLocalRingtonePickerActivity.this.mType);
                    }
                    OPMyLog.d("chenhl", "set ringtone ok!");
                }
            }
        }

        public void stopThread() {
            this.isClose = true;
        }
    }

    private class WorkAsyncTask extends AsyncTask<Integer, Void, Void> {
        private boolean isclose = false;
        private ContentResolver resolver;

        public WorkAsyncTask(ContentResolver rs) {
            this.resolver = rs;
        }

        /* Access modifiers changed, original: protected|varargs */
        public Void doInBackground(Integer... params) {
            Cursor cursor;
            if (params[0].intValue() == 0) {
                cursor = this.resolver.query(OPLocalRingtonePickerActivity.RECORD_URI, OPLocalRingtonePickerActivity.PROJECTION, OPLocalRingtonePickerActivity.AUDIO_FILE_SELECTION_ALL, OPLocalRingtonePickerActivity.SELECTION_ARGS_ALL, "date_modified DESC,title DESC");
            } else {
                cursor = this.resolver.query(OPLocalRingtonePickerActivity.RECORD_URI, OPLocalRingtonePickerActivity.PROJECTION, OPLocalRingtonePickerActivity.AUDIO_FILE_SELECTION_PART, OPLocalRingtonePickerActivity.SELECTION_ARGS_PART, "date_modified DESC,title DESC");
            }
            if (cursor == null) {
                return null;
            }
            if (OPLocalRingtonePickerActivity.this.mSystemRings == null) {
                OPLocalRingtonePickerActivity.this.mSystemRings = new ArrayList();
            }
            OPLocalRingtonePickerActivity.this.mSystemRings.clear();
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("isclose:");
                    stringBuilder.append(this.isclose);
                    OPMyLog.d("111", stringBuilder.toString());
                    if (this.isclose) {
                        break;
                    }
                    String path = cursor.getString(3);
                    if (!(path == null || !new File(path).exists() || OPLocalRingtonePickerActivity.this.isApeFile(cursor.getString(4), path))) {
                        String title;
                        Uri uri = OPRingtoneManager.getUriFromCursor(cursor);
                        if (TextUtils.isEmpty(OPUtils.getFileNameNoEx(cursor.getString(1)))) {
                            title = cursor.getString(cursor.getColumnIndex("title")).toString();
                        } else {
                            title = cursor.getString(cursor.getColumnIndex("_display_name")).toString();
                        }
                        RingtoneData data = new RingtoneData(uri, title, uri.equals(ContentProvider.getUriWithoutUserId(OPLocalRingtonePickerActivity.this.mUriForDefaultItem)));
                        data.filepath = cursor.getString(3);
                        data.mimetype = cursor.getString(4);
                        OPLocalRingtonePickerActivity.this.mSystemRings.add(data);
                    }
                } while (cursor.moveToNext());
            }
            if (cursor != null) {
                cursor.close();
            }
            return null;
        }

        /* Access modifiers changed, original: protected */
        public void onPostExecute(Void result) {
            if (!this.isclose) {
                OPLocalRingtonePickerActivity.this.mProgressBar.setVisibility(8);
                OPLocalRingtonePickerActivity.this.mListView.setVisibility(0);
                if (OPLocalRingtonePickerActivity.this.mOPLocalRingtoneAdapter == null) {
                    OPLocalRingtonePickerActivity.this.mOPLocalRingtoneAdapter = new OPLocalRingtoneAdapter(OPLocalRingtonePickerActivity.this, OPLocalRingtonePickerActivity.this.mSystemRings);
                    if (OPLocalRingtonePickerActivity.this.mListView != null) {
                        OPLocalRingtonePickerActivity.this.mListView.setAdapter(OPLocalRingtonePickerActivity.this.mOPLocalRingtoneAdapter);
                    }
                } else {
                    OPLocalRingtonePickerActivity.this.mOPLocalRingtoneAdapter.notifyDataSetChanged();
                }
            }
        }

        public void setClose() {
            this.isclose = true;
        }
    }

    static {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("(_data LIKE '%.wma' or _data LIKE '%.mp3' or _data LIKE '%.aac' or _data LIKE '%.mid' or _data LIKE '%.ogg' or _data LIKE '%.flac' or _data LIKE '%.ape' or _data LIKE '%.ra' or _data LIKE '%.mod' or _data LIKE '%.m4a' or _data LIKE '%.amr' )");
        stringBuilder.append(SECTION_AFTER);
        AUDIO_SECTION = stringBuilder.toString();
        String[] strArr = new String[6];
        strArr[0] = OPFirewallUtils._ID;
        strArr[1] = "_display_name";
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append("\"");
        stringBuilder2.append(Media.EXTERNAL_CONTENT_URI);
        stringBuilder2.append("\"");
        strArr[2] = stringBuilder2.toString();
        strArr[3] = "_data";
        strArr[4] = "mime_type";
        strArr[5] = "title";
        PROJECTION = strArr;
        stringBuilder = new StringBuilder();
        stringBuilder.append("_data not like ? and");
        stringBuilder.append(AUDIO_SECTION);
        AUDIO_FILE_SELECTION_ALL = stringBuilder.toString();
        stringBuilder = new StringBuilder();
        stringBuilder.append("_data not like ? and duration < 60000 and ");
        stringBuilder.append(AUDIO_SECTION);
        AUDIO_FILE_SELECTION_PART = stringBuilder.toString();
        stringBuilder = new StringBuilder();
        stringBuilder.append(SDCARD_PATH);
        stringBuilder.append("/Ringtones/");
        RINGTONE_PATH = stringBuilder.toString();
        stringBuilder = new StringBuilder();
        stringBuilder.append(SDCARD_PATH);
        stringBuilder.append("/Notifications/");
        NOTIFICATIONS_PATH = stringBuilder.toString();
        stringBuilder = new StringBuilder();
        stringBuilder.append(SDCARD_PATH);
        stringBuilder.append("/Alarms/");
        ALARMS_PATH = stringBuilder.toString();
        stringBuilder = new StringBuilder();
        stringBuilder.append(SDCARD_PATH);
        stringBuilder.append("/Record/");
        RECORD_PATH = stringBuilder.toString();
        strArr = new String[1];
        stringBuilder2 = new StringBuilder();
        stringBuilder2.append(RECORD_PATH);
        stringBuilder2.append("%");
        strArr[0] = stringBuilder2.toString();
        SELECTION_ARGS_ALL = strArr;
        strArr = new String[1];
        stringBuilder2 = new StringBuilder();
        stringBuilder2.append(RECORD_PATH);
        stringBuilder2.append("%");
        strArr[0] = stringBuilder2.toString();
        SELECTION_ARGS_PART = strArr;
    }

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            String mUriForDefaultItemStr = savedInstanceState.getString(OPRingtoneBaseActivity.KEY_SELECTED_ITEM_URI);
            if (mUriForDefaultItemStr != null) {
                this.mUriForDefaultItem = Uri.parse(mUriForDefaultItemStr);
            }
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.op_preference_list_content_material);
        initActionbar();
        this.mListView = getListView();
        this.mNofileView = (TextView) findViewById(R.id.id_empty);
        this.mProgressBar = (ProgressBar) findViewById(R.id.id_progress);
        this.mListView.setEmptyView(this.mNofileView);
        this.mListView.setOnItemClickListener(this.mOnItemClickListener);
        Log.v(OPLocalRingtonePickerActivity.class.getName(), "onCreate startTask");
        startTask(0);
    }

    private void initActionbar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(16);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        View layout = View.inflate(this, R.layout.op_spinner_main, null);
        Spinner spinner = (Spinner) layout.findViewById(R.id.id_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter(this, R.layout.op_simple_spinner_item, 16908308, getResources().getStringArray(R.array.oneplus_select_items));
        adapter.setDropDownViewResource(17367049);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this.mOnItemSelectedListener);
        actionBar.setCustomView(layout);
    }

    /* Access modifiers changed, original: protected */
    public void updateSelected() {
        if (this.mSystemRings != null) {
        }
    }

    private void updateChecks(Uri uri) {
        if (this.mSystemRings != null && this.mOPLocalRingtoneAdapter != null) {
            for (RingtoneData p : this.mSystemRings) {
                p.isCheck = p.mUri.equals(uri);
            }
            this.mOPLocalRingtoneAdapter.notifyDataSetChanged();
        }
    }

    /* JADX WARNING: Missing block: B:18:0x0052, code skipped:
            if (r0 == null) goto L_0x005b;
     */
    private boolean isApeFile(java.lang.String r7, java.lang.String r8) {
        /*
        r6 = this;
        r0 = "audio/*";
        r0 = r0.equals(r7);
        r1 = 0;
        if (r0 == 0) goto L_0x005b;
    L_0x0009:
        r0 = 0;
        r7 = 0;
        r2 = new android.media.MediaExtractor;	 Catch:{ IOException -> 0x003b }
        r2.<init>();	 Catch:{ IOException -> 0x003b }
        r0 = r2;
        r0.setDataSource(r8);	 Catch:{ IOException -> 0x003b }
        r2 = r1;
    L_0x0015:
        r3 = r0.getTrackCount();	 Catch:{ IOException -> 0x003b }
        if (r2 >= r3) goto L_0x0034;
    L_0x001b:
        r3 = r0.getTrackFormat(r2);	 Catch:{ IOException -> 0x003b }
        r4 = "mime";
        r4 = r3.getString(r4);	 Catch:{ IOException -> 0x003b }
        if (r4 == 0) goto L_0x0031;
    L_0x0027:
        r5 = "audio/";
        r5 = r4.startsWith(r5);	 Catch:{ IOException -> 0x003b }
        if (r5 == 0) goto L_0x0031;
    L_0x002f:
        r7 = r4;
        goto L_0x0034;
    L_0x0031:
        r2 = r2 + 1;
        goto L_0x0015;
    L_0x0035:
        r0.release();
        goto L_0x005b;
    L_0x0039:
        r1 = move-exception;
        goto L_0x0055;
    L_0x003b:
        r2 = move-exception;
        r3 = "";
        r4 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0039 }
        r4.<init>();	 Catch:{ all -> 0x0039 }
        r5 = "ringtoneCopyFrom3rdParty: ";
        r4.append(r5);	 Catch:{ all -> 0x0039 }
        r4.append(r2);	 Catch:{ all -> 0x0039 }
        r4 = r4.toString();	 Catch:{ all -> 0x0039 }
        com.oneplus.settings.ringtone.OPMyLog.e(r3, r4);	 Catch:{ all -> 0x0039 }
        if (r0 == 0) goto L_0x005b;
    L_0x0054:
        goto L_0x0035;
    L_0x0055:
        if (r0 == 0) goto L_0x005a;
    L_0x0057:
        r0.release();
    L_0x005a:
        throw r1;
    L_0x005b:
        if (r7 == 0) goto L_0x0067;
    L_0x005d:
        r0 = android.media.MediaFile.getFileTypeForMimeType(r7);
        r2 = 10;
        if (r0 <= r2) goto L_0x0066;
    L_0x0065:
        goto L_0x0067;
    L_0x0066:
        return r1;
    L_0x0067:
        r0 = 1;
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.settings.ringtone.OPLocalRingtonePickerActivity.isApeFile(java.lang.String, java.lang.String):boolean");
    }

    private void startTask(int state) {
        if (this.mWorkAsyncTask != null) {
            this.mWorkAsyncTask.setClose();
            this.mWorkAsyncTask = null;
        }
        this.mProgressBar.setVisibility(0);
        this.mNofileView.setVisibility(8);
        this.mListView.setVisibility(8);
        this.mWorkAsyncTask = new WorkAsyncTask(getContentResolver());
        this.mWorkAsyncTask.execute(new Integer[]{Integer.valueOf(state)});
    }

    private Uri updateExternalFile(RingtoneData preference) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("getKey:");
        stringBuilder.append(preference.mUri);
        OPMyLog.d("chenhl", stringBuilder.toString());
        String path = preference.filepath;
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append("path:");
        stringBuilder2.append(path);
        OPMyLog.d("chenhl", stringBuilder2.toString());
        File oldfile = new File(path);
        if (oldfile.exists()) {
            playRingtone(300, this.mUriForDefaultItem);
            if (path == null || path.startsWith("/storage/emulated/legacy") || path.startsWith(SDCARD_PATH)) {
                return this.mUriForDefaultItem;
            }
            StringBuilder stringBuilder3 = new StringBuilder();
            stringBuilder3.append(checkDir());
            stringBuilder3.append(oldfile.getName());
            File newFile = new File(stringBuilder3.toString());
            if (!newFile.exists()) {
                copyFile(oldfile, newFile);
            }
            return updateDb(preference, newFile.getAbsolutePath());
        }
        this.mHandler.post(new Runnable() {
            public void run() {
                Toast.makeText(OPLocalRingtonePickerActivity.this, OPLocalRingtonePickerActivity.this.getString(R.string.oneplus_file_not_exist), 0).show();
            }
        });
        return null;
    }

    private String checkDir() {
        String dirPath = RINGTONE_PATH;
        if (this.mType == 2 || this.mType == 8) {
            dirPath = NOTIFICATIONS_PATH;
        } else if (this.mType == 4) {
            dirPath = ALARMS_PATH;
        }
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dirPath;
    }

    public void copyFile(File oldfile, File newFile) {
        int bytesum = 0;
        try {
            if (oldfile.exists()) {
                InputStream inStream = new FileInputStream(oldfile);
                FileOutputStream fs = new FileOutputStream(newFile);
                byte[] buffer = new byte[1444];
                while (true) {
                    int read = inStream.read(buffer);
                    int byteread = read;
                    if (read != -1) {
                        bytesum += byteread;
                        fs.write(buffer, 0, byteread);
                    } else {
                        inStream.close();
                        fs.close();
                        return;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Uri updateDb(RingtoneData p, String path) {
        StringBuilder stringBuilder;
        Uri uri = Media.EXTERNAL_CONTENT_URI;
        Uri uri2 = uri;
        Cursor cursor1 = getContentResolver().query(uri2, new String[]{OPFirewallUtils._ID}, "_data=?", new String[]{path}, null);
        if (cursor1 == null || !cursor1.moveToFirst()) {
            uri2 = new ContentValues();
            uri2.put("_data", path);
            uri2.put("title", p.title);
            uri2.put("mime_type", p.mimetype);
            if (this.mType == 1) {
                uri2.put("is_ringtone", Boolean.valueOf(true));
            } else if (this.mType == 2 || this.mType == 8) {
                uri2.put("is_notification", Boolean.valueOf(true));
            } else {
                uri2.put("is_alarm", Boolean.valueOf(true));
            }
            ContentResolver contentResolver = getContentResolver();
            stringBuilder = new StringBuilder();
            stringBuilder.append("_data=\"");
            stringBuilder.append(path);
            stringBuilder.append("\"");
            contentResolver.delete(uri, stringBuilder.toString(), null);
            uri2 = getContentResolver().insert(uri, uri2);
        } else {
            uri2 = ContentUris.withAppendedId(uri, cursor1.getLong(0));
        }
        if (cursor1 != null) {
            cursor1.close();
        }
        stringBuilder = new StringBuilder();
        stringBuilder.append("defaultitem:");
        stringBuilder.append(uri2);
        stringBuilder.append(" path:");
        stringBuilder.append(path);
        OPMyLog.d("chenhl", stringBuilder.toString());
        return uri2;
    }

    /* Access modifiers changed, original: protected */
    public void onDestroy() {
        if (this.mWorkAsyncTask != null) {
            this.mWorkAsyncTask.setClose();
            this.mWorkAsyncTask = null;
        }
        super.onDestroy();
    }
}
