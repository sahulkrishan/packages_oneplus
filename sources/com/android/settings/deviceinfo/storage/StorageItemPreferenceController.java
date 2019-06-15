package com.android.settings.deviceinfo.storage;

import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.storage.VolumeInfo;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import android.util.SparseArray;
import com.android.settings.R;
import com.android.settings.Settings.GamesStorageActivity;
import com.android.settings.Settings.MoviesStorageActivity;
import com.android.settings.Settings.PhotosStorageActivity;
import com.android.settings.Settings.StorageUseActivity;
import com.android.settings.applications.manageapplications.ManageApplications;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.datausage.BillingCycleSettings;
import com.android.settings.deviceinfo.StorageItemPreference;
import com.android.settings.deviceinfo.storage.StorageAsyncLoader.AppsStorageResult;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;
import com.android.settingslib.deviceinfo.StorageMeasurement.MeasurementDetails;
import com.android.settingslib.deviceinfo.StorageVolumeProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StorageItemPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    @VisibleForTesting
    static final String AUDIO_KEY = "pref_music_audio";
    private static final String AUTHORITY_MEDIA = "com.android.providers.media.documents";
    @VisibleForTesting
    static final String FILES_KEY = "pref_files";
    @VisibleForTesting
    static final String GAME_KEY = "pref_games";
    @VisibleForTesting
    static final String MOVIES_KEY = "pref_movies";
    @VisibleForTesting
    static final String OTHER_APPS_KEY = "pref_other_apps";
    @VisibleForTesting
    static final String PHOTO_KEY = "pref_photos_videos";
    private static final String SYSTEM_FRAGMENT_TAG = "SystemInfo";
    @VisibleForTesting
    static final String SYSTEM_KEY = "pref_system";
    private static final String TAG = "StorageItemPreference";
    private StorageItemPreference mAppPreference;
    private StorageItemPreference mAudioPreference;
    private StorageItemPreference mFilePreference;
    private final Fragment mFragment;
    private StorageItemPreference mGamePreference;
    private boolean mIsWorkProfile;
    private final MetricsFeatureProvider mMetricsFeatureProvider;
    private StorageItemPreference mMoviesPreference;
    private StorageItemPreference mPhotoPreference;
    private PreferenceScreen mScreen;
    private final StorageVolumeProvider mSvp;
    private StorageItemPreference mSystemPreference;
    private long mTotalSize;
    private long mUsedBytes;
    private int mUserId;
    private VolumeInfo mVolume;

    public StorageItemPreferenceController(Context context, Fragment hostFragment, VolumeInfo volume, StorageVolumeProvider svp) {
        super(context);
        this.mFragment = hostFragment;
        this.mVolume = volume;
        this.mSvp = svp;
        this.mMetricsFeatureProvider = FeatureFactory.getFactory(context).getMetricsFeatureProvider();
        this.mUserId = UserHandle.myUserId();
    }

    public StorageItemPreferenceController(Context context, Fragment hostFragment, VolumeInfo volume, StorageVolumeProvider svp, boolean isWorkProfile) {
        this(context, hostFragment, volume, svp);
        this.mIsWorkProfile = isWorkProfile;
    }

    public boolean isAvailable() {
        return true;
    }

    public boolean handlePreferenceTreeClick(android.support.v7.preference.Preference r7) {
        /*
        r6 = this;
        r0 = 0;
        if (r7 != 0) goto L_0x0004;
    L_0x0003:
        return r0;
    L_0x0004:
        r1 = 0;
        r2 = r7.getKey();
        if (r2 != 0) goto L_0x000c;
    L_0x000b:
        return r0;
    L_0x000c:
        r2 = r7.getKey();
        r3 = -1;
        r4 = r2.hashCode();
        r5 = 1;
        switch(r4) {
            case -1642571429: goto L_0x0056;
            case -1641885275: goto L_0x004c;
            case -1488779334: goto L_0x0042;
            case 283435296: goto L_0x0038;
            case 826139871: goto L_0x002e;
            case 1007071179: goto L_0x0024;
            case 1161100765: goto L_0x001a;
            default: goto L_0x0019;
        };
    L_0x0019:
        goto L_0x0060;
    L_0x001a:
        r4 = "pref_other_apps";
        r2 = r2.equals(r4);
        if (r2 == 0) goto L_0x0060;
    L_0x0022:
        r2 = 4;
        goto L_0x0061;
    L_0x0024:
        r4 = "pref_system";
        r2 = r2.equals(r4);
        if (r2 == 0) goto L_0x0060;
    L_0x002c:
        r2 = 6;
        goto L_0x0061;
    L_0x002e:
        r4 = "pref_movies";
        r2 = r2.equals(r4);
        if (r2 == 0) goto L_0x0060;
    L_0x0036:
        r2 = 3;
        goto L_0x0061;
    L_0x0038:
        r4 = "pref_music_audio";
        r2 = r2.equals(r4);
        if (r2 == 0) goto L_0x0060;
    L_0x0040:
        r2 = r5;
        goto L_0x0061;
    L_0x0042:
        r4 = "pref_photos_videos";
        r2 = r2.equals(r4);
        if (r2 == 0) goto L_0x0060;
    L_0x004a:
        r2 = r0;
        goto L_0x0061;
    L_0x004c:
        r4 = "pref_games";
        r2 = r2.equals(r4);
        if (r2 == 0) goto L_0x0060;
    L_0x0054:
        r2 = 2;
        goto L_0x0061;
    L_0x0056:
        r4 = "pref_files";
        r2 = r2.equals(r4);
        if (r2 == 0) goto L_0x0060;
    L_0x005e:
        r2 = 5;
        goto L_0x0061;
    L_0x0060:
        r2 = r3;
    L_0x0061:
        switch(r2) {
            case 0: goto L_0x00ac;
            case 1: goto L_0x00a7;
            case 2: goto L_0x00a2;
            case 3: goto L_0x009d;
            case 4: goto L_0x0093;
            case 5: goto L_0x007b;
            case 6: goto L_0x0065;
            default: goto L_0x0064;
        };
    L_0x0064:
        goto L_0x00b1;
    L_0x0065:
        r2 = new com.android.settings.deviceinfo.PrivateVolumeSettings$SystemInfoFragment;
        r2.<init>();
        r3 = r6.mFragment;
        r2.setTargetFragment(r3, r0);
        r0 = r6.mFragment;
        r0 = r0.getFragmentManager();
        r3 = "SystemInfo";
        r2.show(r0, r3);
        return r5;
    L_0x007b:
        r1 = r6.getFilesIntent();
        r2 = r6.mContext;
        r2 = com.android.settings.overlay.FeatureFactory.getFactory(r2);
        r2 = r2.getMetricsFeatureProvider();
        r3 = r6.mContext;
        r4 = 841; // 0x349 float:1.178E-42 double:4.155E-321;
        r0 = new android.util.Pair[r0];
        r2.action(r3, r4, r0);
        goto L_0x00b1;
    L_0x0093:
        r0 = r6.mVolume;
        if (r0 != 0) goto L_0x0098;
    L_0x0097:
        goto L_0x00b1;
    L_0x0098:
        r1 = r6.getAppsIntent();
        goto L_0x00b1;
    L_0x009d:
        r1 = r6.getMoviesIntent();
        goto L_0x00b1;
    L_0x00a2:
        r1 = r6.getGamesIntent();
        goto L_0x00b1;
    L_0x00a7:
        r1 = r6.getAudioIntent();
        goto L_0x00b1;
    L_0x00ac:
        r1 = r6.getPhotosIntent();
    L_0x00b1:
        if (r1 == 0) goto L_0x00be;
    L_0x00b3:
        r0 = "android.intent.extra.USER_ID";
        r2 = r6.mUserId;
        r1.putExtra(r0, r2);
        r6.launchIntent(r1);
        return r5;
    L_0x00be:
        r0 = super.handlePreferenceTreeClick(r7);
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settings.deviceinfo.storage.StorageItemPreferenceController.handlePreferenceTreeClick(android.support.v7.preference.Preference):boolean");
    }

    public String getPreferenceKey() {
        return null;
    }

    public void setVolume(VolumeInfo volume) {
        this.mVolume = volume;
        setFilesPreferenceVisibility();
    }

    private void setFilesPreferenceVisibility() {
        if (this.mScreen != null) {
            VolumeInfo sharedVolume = this.mSvp.findEmulatedForPrivate(this.mVolume);
            boolean hideFilePreference = sharedVolume == null || !sharedVolume.isMountedReadable();
            if (hideFilePreference) {
                this.mScreen.removePreference(this.mFilePreference);
            } else {
                this.mScreen.addPreference(this.mFilePreference);
            }
        }
    }

    public void setUserId(UserHandle userHandle) {
        this.mUserId = userHandle.getIdentifier();
        tintPreference(this.mPhotoPreference);
        tintPreference(this.mMoviesPreference);
        tintPreference(this.mAudioPreference);
        tintPreference(this.mGamePreference);
        tintPreference(this.mAppPreference);
        tintPreference(this.mSystemPreference);
        tintPreference(this.mFilePreference);
    }

    private void tintPreference(Preference preference) {
        if (preference != null) {
            preference.setIcon(applyTint(this.mContext, preference.getIcon()));
        }
    }

    private static Drawable applyTint(Context context, Drawable icon) {
        TypedArray array = context.obtainStyledAttributes(new int[]{16843817});
        icon = icon.mutate();
        icon.setTint(array.getColor(0, 0));
        array.recycle();
        return icon;
    }

    public void displayPreference(PreferenceScreen screen) {
        this.mScreen = screen;
        this.mPhotoPreference = (StorageItemPreference) screen.findPreference(PHOTO_KEY);
        this.mAudioPreference = (StorageItemPreference) screen.findPreference(AUDIO_KEY);
        this.mGamePreference = (StorageItemPreference) screen.findPreference(GAME_KEY);
        this.mMoviesPreference = (StorageItemPreference) screen.findPreference(MOVIES_KEY);
        this.mAppPreference = (StorageItemPreference) screen.findPreference(OTHER_APPS_KEY);
        this.mSystemPreference = (StorageItemPreference) screen.findPreference(SYSTEM_KEY);
        this.mFilePreference = (StorageItemPreference) screen.findPreference(FILES_KEY);
        setFilesPreferenceVisibility();
    }

    public void onLoadFinished(SparseArray<AppsStorageResult> result, int userId) {
        AppsStorageResult data = (AppsStorageResult) result.get(userId);
        this.mPhotoPreference.setStorageSize((data.photosAppsSize + data.externalStats.imageBytes) + data.externalStats.videoBytes, this.mTotalSize);
        this.mAudioPreference.setStorageSize(data.musicAppsSize + data.externalStats.audioBytes, this.mTotalSize);
        this.mGamePreference.setStorageSize(data.gamesSize, this.mTotalSize);
        this.mMoviesPreference.setStorageSize(data.videoAppsSize, this.mTotalSize);
        this.mAppPreference.setStorageSize(data.otherAppsSize, this.mTotalSize);
        this.mFilePreference.setStorageSize((((data.externalStats.totalBytes - data.externalStats.audioBytes) - data.externalStats.videoBytes) - data.externalStats.imageBytes) - data.externalStats.appBytes, this.mTotalSize);
        if (this.mSystemPreference != null) {
            long attributedSize = 0;
            for (int i = 0; i < result.size(); i++) {
                AppsStorageResult otherData = (AppsStorageResult) result.valueAt(i);
                attributedSize = (attributedSize + ((((otherData.gamesSize + otherData.musicAppsSize) + otherData.videoAppsSize) + otherData.photosAppsSize) + otherData.otherAppsSize)) + (otherData.externalStats.totalBytes - otherData.externalStats.appBytes);
            }
            this.mSystemPreference.setStorageSize(Math.max(BillingCycleSettings.GIB_IN_BYTES, this.mUsedBytes - attributedSize), this.mTotalSize);
        }
    }

    public void setUsedSize(long usedSizeBytes) {
        this.mUsedBytes = usedSizeBytes;
    }

    public void setTotalSize(long totalSizeBytes) {
        this.mTotalSize = totalSizeBytes;
    }

    public static List<String> getUsedKeys() {
        List<String> list = new ArrayList();
        list.add(PHOTO_KEY);
        list.add(AUDIO_KEY);
        list.add(GAME_KEY);
        list.add(MOVIES_KEY);
        list.add(OTHER_APPS_KEY);
        list.add(SYSTEM_KEY);
        list.add(FILES_KEY);
        return list;
    }

    private Intent getPhotosIntent() {
        Bundle args = getWorkAnnotatedBundle(2);
        args.putString(ManageApplications.EXTRA_CLASSNAME, PhotosStorageActivity.class.getName());
        args.putInt(ManageApplications.EXTRA_STORAGE_TYPE, 3);
        return new SubSettingLauncher(this.mContext).setDestination(ManageApplications.class.getName()).setTitle((int) R.string.storage_photos_videos).setArguments(args).setSourceMetricsCategory(this.mMetricsFeatureProvider.getMetricsCategory(this.mFragment)).toIntent();
    }

    private Intent getAudioIntent() {
        if (this.mVolume == null) {
            return null;
        }
        Bundle args = getWorkAnnotatedBundle(4);
        args.putString(ManageApplications.EXTRA_CLASSNAME, StorageUseActivity.class.getName());
        args.putString(ManageApplications.EXTRA_VOLUME_UUID, this.mVolume.getFsUuid());
        args.putString(ManageApplications.EXTRA_VOLUME_NAME, this.mVolume.getDescription());
        args.putInt(ManageApplications.EXTRA_STORAGE_TYPE, 1);
        return new SubSettingLauncher(this.mContext).setDestination(ManageApplications.class.getName()).setTitle((int) R.string.storage_music_audio).setArguments(args).setSourceMetricsCategory(this.mMetricsFeatureProvider.getMetricsCategory(this.mFragment)).toIntent();
    }

    private Intent getAppsIntent() {
        if (this.mVolume == null) {
            return null;
        }
        Bundle args = getWorkAnnotatedBundle(3);
        args.putString(ManageApplications.EXTRA_CLASSNAME, StorageUseActivity.class.getName());
        args.putString(ManageApplications.EXTRA_VOLUME_UUID, this.mVolume.getFsUuid());
        args.putString(ManageApplications.EXTRA_VOLUME_NAME, this.mVolume.getDescription());
        return new SubSettingLauncher(this.mContext).setDestination(ManageApplications.class.getName()).setTitle((int) R.string.apps_storage).setArguments(args).setSourceMetricsCategory(this.mMetricsFeatureProvider.getMetricsCategory(this.mFragment)).toIntent();
    }

    private Intent getGamesIntent() {
        Bundle args = getWorkAnnotatedBundle(1);
        args.putString(ManageApplications.EXTRA_CLASSNAME, GamesStorageActivity.class.getName());
        return new SubSettingLauncher(this.mContext).setDestination(ManageApplications.class.getName()).setTitle((int) R.string.game_storage_settings).setArguments(args).setSourceMetricsCategory(this.mMetricsFeatureProvider.getMetricsCategory(this.mFragment)).toIntent();
    }

    private Intent getMoviesIntent() {
        Bundle args = getWorkAnnotatedBundle(1);
        args.putString(ManageApplications.EXTRA_CLASSNAME, MoviesStorageActivity.class.getName());
        return new SubSettingLauncher(this.mContext).setDestination(ManageApplications.class.getName()).setTitle((int) R.string.storage_movies_tv).setArguments(args).setSourceMetricsCategory(this.mMetricsFeatureProvider.getMetricsCategory(this.mFragment)).toIntent();
    }

    private Bundle getWorkAnnotatedBundle(int additionalCapacity) {
        Bundle args = new Bundle(2 + additionalCapacity);
        args.putBoolean(ManageApplications.EXTRA_WORK_ONLY, this.mIsWorkProfile);
        args.putInt(ManageApplications.EXTRA_WORK_ID, this.mUserId);
        return args;
    }

    private Intent getFilesIntent() {
        return this.mSvp.findEmulatedForPrivate(this.mVolume).buildBrowseIntent();
    }

    private void launchIntent(Intent intent) {
        try {
            int userId = intent.getIntExtra("android.intent.extra.USER_ID", -1);
            if (userId == -1) {
                this.mFragment.startActivity(intent);
            } else {
                this.mFragment.getActivity().startActivityAsUser(intent, new UserHandle(userId));
            }
        } catch (ActivityNotFoundException e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("No activity found for ");
            stringBuilder.append(intent);
            Log.w(str, stringBuilder.toString());
        }
    }

    private static long totalValues(MeasurementDetails details, int userId, String... keys) {
        long total = 0;
        Map<String, Long> map = (Map) details.mediaSize.get(userId);
        if (map != null) {
            for (String key : keys) {
                if (map.containsKey(key)) {
                    total += ((Long) map.get(key)).longValue();
                }
            }
        } else {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("MeasurementDetails mediaSize array does not have key for user ");
            stringBuilder.append(userId);
            Log.w(str, stringBuilder.toString());
        }
        return total;
    }
}
