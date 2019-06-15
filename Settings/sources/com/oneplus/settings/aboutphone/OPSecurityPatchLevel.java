package com.oneplus.settings.aboutphone;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settingslib.DeviceInfoUtils;
import com.android.settingslib.wrapper.PackageManagerWrapper;

public class OPSecurityPatchLevel extends Preference {
    private static final Uri INTENT_URI_DATA = Uri.parse("https://source.android.com/security/bulletin/");
    private Context mContext;
    private PackageManagerWrapper mPackageManager;

    public OPSecurityPatchLevel(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initViews(context);
    }

    public OPSecurityPatchLevel(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context);
    }

    public OPSecurityPatchLevel(Context context) {
        super(context);
        initViews(context);
    }

    private void initViews(Context context) {
        this.mContext = context;
        this.mPackageManager = new PackageManagerWrapper(this.mContext.getPackageManager());
        setLayoutResource(R.layout.op_security_patch_level);
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        String mCurrentPatch = DeviceInfoUtils.getSecurityPatch();
        LinearLayout mSecurityPatch = (LinearLayout) view.findViewById(R.id.security_patch);
        TextView mSecurityPatchLevelValue = (TextView) view.findViewById(R.id.security_patch_level_value);
        if (mCurrentPatch != null) {
            mSecurityPatchLevelValue.setText(mCurrentPatch);
        }
        if (mSecurityPatch != null) {
            mSecurityPatch.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setAction("android.intent.action.VIEW");
                    intent.setData(OPSecurityPatchLevel.INTENT_URI_DATA);
                    if (OPSecurityPatchLevel.this.mPackageManager.queryIntentActivities(intent, 0).isEmpty()) {
                        Log.w(OPSecurityPatchLevel.class.getName(), "Stop click action on SECURITY_PATCH_VALUE_ID : queryIntentActivities() returns empty");
                    } else {
                        OPSecurityPatchLevel.this.mContext.startActivity(intent);
                    }
                }
            });
        }
    }
}
