package com.android.settings.deviceinfo;

import android.content.res.ColorStateList;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import com.android.settings.R;
import com.android.settings.deviceinfo.StorageSettings.UnmountTask;

public class StorageVolumePreference extends Preference {
    private static final String TAG = StorageVolumePreference.class.getSimpleName();
    private int mColor;
    private final StorageManager mStorageManager;
    private final OnClickListener mUnmountListener = new OnClickListener() {
        public void onClick(View v) {
            new UnmountTask(StorageVolumePreference.this.getContext(), StorageVolumePreference.this.mVolume).execute(new Void[0]);
        }
    };
    private int mUsedPercent = -1;
    private final VolumeInfo mVolume;

    /* JADX WARNING: Removed duplicated region for block: B:26:0x00ca  */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x00db  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x00ca  */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x00db  */
    /* JADX WARNING: Removed duplicated region for block: B:37:? A:{SYNTHETIC, RETURN, SKIP} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x0109  */
    public StorageVolumePreference(android.content.Context r21, android.os.storage.VolumeInfo r22, int r23, long r24) {
        /*
        r20 = this;
        r1 = r20;
        r2 = r21;
        r3 = r22;
        r20.<init>(r21);
        r0 = -1;
        r1.mUsedPercent = r0;
        r4 = new com.android.settings.deviceinfo.StorageVolumePreference$1;
        r4.<init>();
        r1.mUnmountListener = r4;
        r4 = android.os.storage.StorageManager.class;
        r4 = r2.getSystemService(r4);
        r4 = (android.os.storage.StorageManager) r4;
        r1.mStorageManager = r4;
        r1.mVolume = r3;
        r4 = r23;
        r1.mColor = r4;
        r5 = 2131559023; // 0x7f0d026f float:1.8743378E38 double:1.0531300853E-314;
        r1.setLayoutResource(r5);
        r5 = r22.getId();
        r1.setKey(r5);
        r5 = r1.mStorageManager;
        r5 = r5.getBestVolumeDescription(r3);
        r1.setTitle(r5);
        r5 = "private";
        r6 = r22.getId();
        r5 = r5.equals(r6);
        if (r5 == 0) goto L_0x004d;
    L_0x0045:
        r5 = 2131231313; // 0x7f080251 float:1.8078703E38 double:1.052968175E-314;
        r5 = r2.getDrawable(r5);
        goto L_0x0054;
    L_0x004d:
        r5 = 2131231309; // 0x7f08024d float:1.8078695E38 double:1.052968173E-314;
        r5 = r2.getDrawable(r5);
    L_0x0054:
        r6 = r22.isMountedReadable();
        if (r6 == 0) goto L_0x00ed;
    L_0x005a:
        r6 = r22.getPath();
        r7 = 0;
        r9 = 0;
        r0 = r22.getType();
        r11 = 0;
        r13 = 1;
        if (r0 != r13) goto L_0x0096;
    L_0x006b:
        r0 = android.app.usage.StorageStatsManager.class;
        r0 = r2.getSystemService(r0);
        r0 = (android.app.usage.StorageStatsManager) r0;
        r14 = r0;
        r0 = r22.getFsUuid();	 Catch:{ IOException -> 0x008b }
        r15 = r14.getTotalBytes(r0);	 Catch:{ IOException -> 0x008b }
        r0 = r22.getFsUuid();	 Catch:{ IOException -> 0x0089 }
        r17 = r14.getFreeBytes(r0);	 Catch:{ IOException -> 0x0089 }
        r7 = r17;
        r9 = r15 - r7;
        goto L_0x0093;
    L_0x0089:
        r0 = move-exception;
        goto L_0x008e;
    L_0x008b:
        r0 = move-exception;
        r15 = r24;
    L_0x008e:
        r13 = TAG;
        android.util.Log.w(r13, r0);
        r13 = r15;
        goto L_0x00a7;
    L_0x0096:
        r0 = (r24 > r11 ? 1 : (r24 == r11 ? 0 : -1));
        if (r0 > 0) goto L_0x009f;
    L_0x009a:
        r13 = r6.getTotalSpace();
        goto L_0x00a1;
    L_0x009f:
        r13 = r24;
    L_0x00a1:
        r7 = r6.getFreeSpace();
        r9 = r13 - r7;
    L_0x00a7:
        r0 = android.text.format.Formatter.formatFileSize(r2, r9);
        r15 = android.text.format.Formatter.formatFileSize(r2, r13);
        r11 = 2131890541; // 0x7f12116d float:1.9415777E38 double:1.053293877E-314;
        r12 = 2;
        r12 = new java.lang.Object[r12];
        r16 = 0;
        r12[r16] = r0;
        r16 = 1;
        r12[r16] = r15;
        r11 = r2.getString(r11, r12);
        r1.setSummary(r11);
        r11 = 0;
        r11 = (r13 > r11 ? 1 : (r13 == r11 ? 0 : -1));
        if (r11 <= 0) goto L_0x00d1;
    L_0x00ca:
        r11 = 100;
        r11 = r11 * r9;
        r11 = r11 / r13;
        r11 = (int) r11;
        r1.mUsedPercent = r11;
    L_0x00d1:
        r11 = r1.mStorageManager;
        r11 = r11.getStorageLowBytes(r6);
        r11 = (r7 > r11 ? 1 : (r7 == r11 ? 0 : -1));
        if (r11 >= 0) goto L_0x00ec;
    L_0x00db:
        r11 = 16844099; // 0x1010543 float:2.3697333E-38 double:8.3220907E-317;
        r11 = com.android.settingslib.Utils.getColorAttr(r2, r11);
        r1.mColor = r11;
        r11 = 2131231350; // 0x7f080276 float:1.8078779E38 double:1.0529681934E-314;
        r0 = r2.getDrawable(r11);
        r5 = r0;
    L_0x00ec:
        goto L_0x00f8;
    L_0x00ed:
        r6 = r22.getStateDescription();
        r1.setSummary(r6);
        r1.mUsedPercent = r0;
        r13 = r24;
    L_0x00f8:
        r5.mutate();
        r0 = r1.mColor;
        r5.setTint(r0);
        r1.setIcon(r5);
        r0 = r22.getType();
        if (r0 != 0) goto L_0x0115;
    L_0x0109:
        r0 = r22.isMountedReadable();
        if (r0 == 0) goto L_0x0115;
    L_0x010f:
        r0 = 2131558928; // 0x7f0d0210 float:1.8743186E38 double:1.0531300384E-314;
        r1.setWidgetLayoutResource(r0);
    L_0x0115:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settings.deviceinfo.StorageVolumePreference.<init>(android.content.Context, android.os.storage.VolumeInfo, int, long):void");
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        ImageView unmount = (ImageView) view.findViewById(R.id.unmount);
        if (unmount != null) {
            unmount.setImageTintList(ColorStateList.valueOf(getContext().getResources().getColor(R.color.oneplus_contorl_icon_color_active_default)));
            unmount.setOnClickListener(this.mUnmountListener);
        }
        ProgressBar progress = (ProgressBar) view.findViewById(16908301);
        if (this.mVolume.getType() != 1 || this.mUsedPercent == -1) {
            progress.setVisibility(8);
        } else {
            progress.setVisibility(0);
            progress.setProgress(this.mUsedPercent);
            progress.setProgressTintList(ColorStateList.valueOf(this.mColor));
        }
        super.onBindViewHolder(view);
    }
}
