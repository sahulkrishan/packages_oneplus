package com.android.settings.slices;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.slices.SlicesDatabaseHelper.IndexColumns;
import com.android.settings.slices.SlicesDatabaseHelper.Tables;
import java.util.List;

class SlicesIndexer implements Runnable {
    private static final String TAG = "SlicesIndexer";
    private Context mContext;
    private SlicesDatabaseHelper mHelper = SlicesDatabaseHelper.getInstance(this.mContext);

    public SlicesIndexer(Context context) {
        this.mContext = context;
    }

    public void run() {
        indexSliceData();
        Log.d(TAG, "postOnBackgroundThread SlicesIndexer end");
    }

    /* Access modifiers changed, original: protected */
    public void indexSliceData() {
        if (this.mHelper.isSliceDataIndexed()) {
            Log.d(TAG, "Slices already indexed - returning.");
            return;
        }
        SQLiteDatabase database;
        try {
            database = this.mHelper.getWritableDatabase();
            long startTime = System.currentTimeMillis();
            database.beginTransaction();
            this.mHelper.reconstruct(this.mHelper.getWritableDatabase());
            insertSliceData(database, getSliceData());
            this.mHelper.setIndexedState();
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Indexing slices database took: ");
            stringBuilder.append(System.currentTimeMillis() - startTime);
            Log.d(str, stringBuilder.toString());
            database.setTransactionSuccessful();
            database.endTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Throwable th) {
            database.endTransaction();
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public List<SliceData> getSliceData() {
        return FeatureFactory.getFactory(this.mContext).getSlicesFeatureProvider().getSliceDataConverter(this.mContext).getSliceData();
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void insertSliceData(SQLiteDatabase database, List<SliceData> indexData) {
        for (SliceData dataRow : indexData) {
            ContentValues values = new ContentValues();
            values.put("key", dataRow.getKey());
            values.put("title", dataRow.getTitle());
            values.put("summary", dataRow.getSummary());
            values.put(IndexColumns.SCREENTITLE, dataRow.getScreenTitle().toString());
            values.put("keywords", dataRow.getKeywords());
            values.put("icon", Integer.valueOf(dataRow.getIconResource()));
            values.put(IndexColumns.FRAGMENT, dataRow.getFragmentClassName());
            values.put("controller", dataRow.getPreferenceController());
            values.put("platform_slice", Boolean.valueOf(dataRow.isPlatformDefined()));
            values.put(IndexColumns.SLICE_TYPE, Integer.valueOf(dataRow.getSliceType()));
            database.replaceOrThrow(Tables.TABLE_SLICES_INDEX, null, values);
        }
    }
}
