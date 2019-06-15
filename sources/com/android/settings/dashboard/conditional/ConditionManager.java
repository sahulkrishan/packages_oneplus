package com.android.settings.dashboard.conditional;

import android.content.Context;
import android.os.AsyncTask;
import android.os.PersistableBundle;
import android.util.Log;
import android.util.Xml;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class ConditionManager implements LifecycleObserver, OnResume, OnPause {
    private static final String ATTR_CLASS = "cls";
    private static final Comparator<Condition> CONDITION_COMPARATOR = new Comparator<Condition>() {
        public int compare(Condition lhs, Condition rhs) {
            return Long.compare(lhs.getLastChange(), rhs.getLastChange());
        }
    };
    private static final boolean DEBUG = false;
    private static final String FILE_NAME = "condition_state.xml";
    private static final String PKG = "com.android.settings.dashboard.conditional.";
    private static final String TAG = "ConditionManager";
    private static final String TAG_CONDITION = "c";
    private static final String TAG_CONDITIONS = "cs";
    private static ConditionManager sInstance;
    private final ArrayList<Condition> mConditions;
    private final Context mContext;
    private final ArrayList<ConditionListener> mListeners = new ArrayList();
    private File mXmlFile;

    public interface ConditionListener {
        void onConditionsChanged();
    }

    private class ConditionLoader extends AsyncTask<Void, Void, ArrayList<Condition>> {
        private ConditionLoader() {
        }

        /* synthetic */ ConditionLoader(ConditionManager x0, AnonymousClass1 x1) {
            this();
        }

        /* Access modifiers changed, original: protected|varargs */
        public ArrayList<Condition> doInBackground(Void... params) {
            Log.d(ConditionManager.TAG, "loading conditions from xml");
            ArrayList<Condition> conditions = new ArrayList();
            ConditionManager.this.mXmlFile = new File(ConditionManager.this.mContext.getFilesDir(), ConditionManager.FILE_NAME);
            if (ConditionManager.this.mXmlFile.exists()) {
                ConditionManager.this.readFromXml(ConditionManager.this.mXmlFile, conditions);
            }
            ConditionManager.this.addMissingConditions(conditions);
            return conditions;
        }

        /* Access modifiers changed, original: protected */
        public void onPostExecute(ArrayList<Condition> conditions) {
            Log.d(ConditionManager.TAG, "conditions loaded from xml, refreshing conditions");
            ConditionManager.this.mConditions.clear();
            ConditionManager.this.mConditions.addAll(conditions);
            ConditionManager.this.refreshAll();
        }
    }

    private ConditionManager(Context context, boolean loadConditionsNow) {
        this.mContext = context;
        this.mConditions = new ArrayList();
        if (loadConditionsNow) {
            Log.d(TAG, "conditions loading synchronously");
            ConditionLoader loader = new ConditionLoader(this, null);
            loader.onPostExecute(loader.doInBackground(new Void[0]));
            return;
        }
        Log.d(TAG, "conditions loading asychronously");
        new ConditionLoader(this, null).execute(new Void[0]);
    }

    public void refreshAll() {
        int N = this.mConditions.size();
        for (int i = 0; i < N; i++) {
            ((Condition) this.mConditions.get(i)).refreshState();
        }
    }

    private void readFromXml(File xmlFile, ArrayList<Condition> conditions) {
        try {
            XmlPullParser parser = Xml.newPullParser();
            FileReader in = new FileReader(xmlFile);
            parser.setInput(in);
            for (int state = parser.getEventType(); state != 1; state = parser.next()) {
                if (TAG_CONDITION.equals(parser.getName())) {
                    int depth = parser.getDepth();
                    String clz = parser.getAttributeValue("", ATTR_CLASS);
                    if (!clz.startsWith(PKG)) {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(PKG);
                        stringBuilder.append(clz);
                        clz = stringBuilder.toString();
                    }
                    Condition condition = createCondition(Class.forName(clz));
                    PersistableBundle bundle = PersistableBundle.restoreFromXml(parser);
                    if (condition != null) {
                        condition.restoreState(bundle);
                        conditions.add(condition);
                    } else {
                        String str = TAG;
                        StringBuilder stringBuilder2 = new StringBuilder();
                        stringBuilder2.append("failed to add condition: ");
                        stringBuilder2.append(clz);
                        Log.e(str, stringBuilder2.toString());
                    }
                    while (parser.getDepth() > depth) {
                        parser.next();
                    }
                }
            }
            in.close();
        } catch (IOException | ClassNotFoundException | XmlPullParserException e) {
            Log.w(TAG, "Problem reading condition_state.xml", e);
        }
    }

    private void saveToXml() {
        try {
            XmlSerializer serializer = Xml.newSerializer();
            FileWriter writer = new FileWriter(this.mXmlFile);
            serializer.setOutput(writer);
            serializer.startDocument("UTF-8", Boolean.valueOf(true));
            serializer.startTag("", TAG_CONDITIONS);
            int N = this.mConditions.size();
            for (int i = 0; i < N; i++) {
                PersistableBundle bundle = new PersistableBundle();
                if (((Condition) this.mConditions.get(i)).saveState(bundle)) {
                    serializer.startTag("", TAG_CONDITION);
                    serializer.attribute("", ATTR_CLASS, ((Condition) this.mConditions.get(i)).getClass().getSimpleName());
                    bundle.saveToXml(serializer);
                    serializer.endTag("", TAG_CONDITION);
                }
            }
            serializer.endTag("", TAG_CONDITIONS);
            serializer.flush();
            writer.close();
        } catch (IOException | XmlPullParserException e) {
            Log.w(TAG, "Problem writing condition_state.xml", e);
        }
    }

    private void addMissingConditions(ArrayList<Condition> conditions) {
        addIfMissing(AirplaneModeCondition.class, conditions);
        addIfMissing(HotspotCondition.class, conditions);
        addIfMissing(DndCondition.class, conditions);
        addIfMissing(BatterySaverCondition.class, conditions);
        addIfMissing(CellularDataCondition.class, conditions);
        addIfMissing(BackgroundDataCondition.class, conditions);
        addIfMissing(WorkModeCondition.class, conditions);
        addIfMissing(NightDisplayCondition.class, conditions);
        addIfMissing(RingerMutedCondition.class, conditions);
        addIfMissing(RingerVibrateCondition.class, conditions);
        addIfMissing(OPOTACondition.class, conditions);
        Collections.sort(conditions, CONDITION_COMPARATOR);
    }

    private void addIfMissing(Class<? extends Condition> clz, ArrayList<Condition> conditions) {
        if (getCondition(clz, conditions) == null) {
            Condition condition = createCondition(clz);
            if (condition != null) {
                conditions.add(condition);
            }
        }
    }

    private Condition createCondition(Class<?> clz) {
        if (AirplaneModeCondition.class == clz) {
            return new AirplaneModeCondition(this);
        }
        if (HotspotCondition.class == clz) {
            return new HotspotCondition(this);
        }
        if (DndCondition.class == clz) {
            return new DndCondition(this);
        }
        if (BatterySaverCondition.class == clz) {
            return new BatterySaverCondition(this);
        }
        if (CellularDataCondition.class == clz) {
            return new CellularDataCondition(this);
        }
        if (BackgroundDataCondition.class == clz) {
            return new BackgroundDataCondition(this);
        }
        if (WorkModeCondition.class == clz) {
            return new WorkModeCondition(this);
        }
        if (NightDisplayCondition.class == clz) {
            return new NightDisplayCondition(this);
        }
        if (RingerMutedCondition.class == clz) {
            return new RingerMutedCondition(this);
        }
        if (RingerVibrateCondition.class == clz) {
            return new RingerVibrateCondition(this);
        }
        if (OPOTACondition.class == clz) {
            return new OPOTACondition(this);
        }
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("unknown condition class: ");
        stringBuilder.append(clz.getSimpleName());
        Log.e(str, stringBuilder.toString());
        return null;
    }

    /* Access modifiers changed, original: 0000 */
    public Context getContext() {
        return this.mContext;
    }

    public <T extends Condition> T getCondition(Class<T> clz) {
        return getCondition(clz, this.mConditions);
    }

    private <T extends Condition> T getCondition(Class<T> clz, List<Condition> conditions) {
        int N = conditions.size();
        for (int i = 0; i < N; i++) {
            if (clz.equals(((Condition) conditions.get(i)).getClass())) {
                return (Condition) conditions.get(i);
            }
        }
        return null;
    }

    public List<Condition> getConditions() {
        return this.mConditions;
    }

    public List<Condition> getVisibleConditions() {
        List<Condition> conditions = new ArrayList();
        int N = this.mConditions.size();
        for (int i = 0; i < N; i++) {
            if (((Condition) this.mConditions.get(i)).shouldShow()) {
                conditions.add((Condition) this.mConditions.get(i));
            }
        }
        return conditions;
    }

    public void notifyChanged(Condition condition) {
        saveToXml();
        Collections.sort(this.mConditions, CONDITION_COMPARATOR);
        int N = this.mListeners.size();
        for (int i = 0; i < N; i++) {
            ((ConditionListener) this.mListeners.get(i)).onConditionsChanged();
        }
    }

    public void addListener(ConditionListener listener) {
        this.mListeners.add(listener);
        listener.onConditionsChanged();
    }

    public void remListener(ConditionListener listener) {
        this.mListeners.remove(listener);
    }

    public void onResume() {
        int size = this.mConditions.size();
        for (int i = 0; i < size; i++) {
            ((Condition) this.mConditions.get(i)).onResume();
        }
    }

    public void onPause() {
        int size = this.mConditions.size();
        for (int i = 0; i < size; i++) {
            ((Condition) this.mConditions.get(i)).onPause();
        }
    }

    public static ConditionManager get(Context context) {
        return get(context, true);
    }

    public static ConditionManager get(Context context, boolean loadConditionsNow) {
        if (sInstance == null) {
            sInstance = new ConditionManager(context.getApplicationContext(), loadConditionsNow);
        }
        return sInstance;
    }
}
