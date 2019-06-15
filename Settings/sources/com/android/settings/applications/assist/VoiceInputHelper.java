package com.android.settings.applications.assist;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.provider.Settings.Secure;
import android.service.voice.VoiceInteractionServiceInfo;
import android.util.ArraySet;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import com.android.internal.R;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.xmlpull.v1.XmlPullParserException;

public final class VoiceInputHelper {
    static final String TAG = "VoiceInputHelper";
    final ArrayList<InteractionInfo> mAvailableInteractionInfos = new ArrayList();
    final List<ResolveInfo> mAvailableRecognition;
    final ArrayList<RecognizerInfo> mAvailableRecognizerInfos = new ArrayList();
    final List<ResolveInfo> mAvailableVoiceInteractions;
    final Context mContext;
    ComponentName mCurrentRecognizer;
    ComponentName mCurrentVoiceInteraction;

    public static class BaseInfo implements Comparable {
        public final CharSequence appLabel;
        public final ComponentName componentName;
        public final String key = this.componentName.flattenToShortString();
        public final CharSequence label;
        public final String labelStr;
        public final ServiceInfo service;
        public final ComponentName settings;

        public BaseInfo(PackageManager pm, ServiceInfo _service, String _settings) {
            this.service = _service;
            this.componentName = new ComponentName(_service.packageName, _service.name);
            this.settings = _settings != null ? new ComponentName(_service.packageName, _settings) : null;
            this.label = _service.loadLabel(pm);
            this.labelStr = this.label.toString();
            this.appLabel = _service.applicationInfo.loadLabel(pm);
        }

        public int compareTo(Object another) {
            return this.labelStr.compareTo(((BaseInfo) another).labelStr);
        }
    }

    public static class InteractionInfo extends BaseInfo {
        public final VoiceInteractionServiceInfo serviceInfo;

        public InteractionInfo(PackageManager pm, VoiceInteractionServiceInfo _service) {
            super(pm, _service.getServiceInfo(), _service.getSettingsActivity());
            this.serviceInfo = _service;
        }
    }

    public static class RecognizerInfo extends BaseInfo {
        public RecognizerInfo(PackageManager pm, ServiceInfo _service, String _settings) {
            super(pm, _service, _settings);
        }
    }

    public VoiceInputHelper(Context context) {
        this.mContext = context;
        this.mAvailableVoiceInteractions = this.mContext.getPackageManager().queryIntentServices(new Intent("android.service.voice.VoiceInteractionService"), 128);
        this.mAvailableRecognition = this.mContext.getPackageManager().queryIntentServices(new Intent("android.speech.RecognitionService"), 128);
    }

    public void buildUi() {
        String currentSetting = Secure.getString(this.mContext.getContentResolver(), "voice_interaction_service");
        ComponentName componentName = null;
        if (currentSetting == null || currentSetting.isEmpty()) {
            this.mCurrentVoiceInteraction = null;
        } else {
            this.mCurrentVoiceInteraction = ComponentName.unflattenFromString(currentSetting);
        }
        ArraySet<ComponentName> interactorRecognizers = new ArraySet();
        int size = this.mAvailableVoiceInteractions.size();
        int i = 0;
        for (int i2 = 0; i2 < size; i2++) {
            ResolveInfo resolveInfo = (ResolveInfo) this.mAvailableVoiceInteractions.get(i2);
            VoiceInteractionServiceInfo info = new VoiceInteractionServiceInfo(this.mContext.getPackageManager(), resolveInfo.serviceInfo);
            if (info.getParseError() != null) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Error in VoiceInteractionService ");
                stringBuilder.append(resolveInfo.serviceInfo.packageName);
                stringBuilder.append("/");
                stringBuilder.append(resolveInfo.serviceInfo.name);
                stringBuilder.append(": ");
                stringBuilder.append(info.getParseError());
                Log.w("VoiceInteractionService", stringBuilder.toString());
            } else {
                this.mAvailableInteractionInfos.add(new InteractionInfo(this.mContext.getPackageManager(), info));
                interactorRecognizers.add(new ComponentName(resolveInfo.serviceInfo.packageName, info.getRecognitionService()));
            }
        }
        Collections.sort(this.mAvailableInteractionInfos);
        String currentSetting2 = Secure.getString(this.mContext.getContentResolver(), "voice_recognition_service");
        if (currentSetting2 == null || currentSetting2.isEmpty()) {
            this.mCurrentRecognizer = null;
        } else {
            this.mCurrentRecognizer = ComponentName.unflattenFromString(currentSetting2);
        }
        size = this.mAvailableRecognition.size();
        int i3 = 0;
        while (true) {
            int i4 = i3;
            if (i4 < size) {
                ResolveInfo resolveInfo2 = (ResolveInfo) this.mAvailableRecognition.get(i4);
                interactorRecognizers.contains(new ComponentName(resolveInfo2.serviceInfo.packageName, resolveInfo2.serviceInfo.name));
                ServiceInfo si = resolveInfo2.serviceInfo;
                XmlResourceParser parser = null;
                String settingsActivity = componentName;
                try {
                    parser = si.loadXmlMetaData(this.mContext.getPackageManager(), "android.speech");
                    if (parser != null) {
                        Resources res = this.mContext.getPackageManager().getResourcesForApplication(si.applicationInfo);
                        AttributeSet attrs = Xml.asAttributeSet(parser);
                        while (true) {
                            int next = parser.next();
                            int type = next;
                            if (next == 1 || type == 2) {
                            }
                        }
                        if ("recognition-service".equals(parser.getName())) {
                            TypedArray array = res.obtainAttributes(attrs, R.styleable.RecognitionService);
                            settingsActivity = array.getString(i);
                            array.recycle();
                            if (parser == null) {
                                this.mAvailableRecognizerInfos.add(new RecognizerInfo(this.mContext.getPackageManager(), resolveInfo2.serviceInfo, settingsActivity));
                                i3 = i4 + 1;
                                componentName = null;
                                i = 0;
                            }
                            parser.close();
                            this.mAvailableRecognizerInfos.add(new RecognizerInfo(this.mContext.getPackageManager(), resolveInfo2.serviceInfo, settingsActivity));
                            i3 = i4 + 1;
                            componentName = null;
                            i = 0;
                        } else {
                            throw new XmlPullParserException("Meta-data does not start with recognition-service tag");
                        }
                    }
                    StringBuilder stringBuilder2 = new StringBuilder();
                    stringBuilder2.append("No android.speech meta-data for ");
                    stringBuilder2.append(si.packageName);
                    throw new XmlPullParserException(stringBuilder2.toString());
                } catch (XmlPullParserException e) {
                    Log.e(TAG, "error parsing recognition service meta-data", e);
                    if (parser == null) {
                    }
                } catch (IOException e2) {
                    Log.e(TAG, "error parsing recognition service meta-data", e2);
                    if (parser != null) {
                    }
                } catch (NameNotFoundException e3) {
                    Log.e(TAG, "error parsing recognition service meta-data", e3);
                    if (parser != null) {
                    }
                } catch (Throwable th) {
                    if (parser != null) {
                        parser.close();
                    }
                }
            } else {
                Collections.sort(this.mAvailableRecognizerInfos);
                return;
            }
        }
    }
}
