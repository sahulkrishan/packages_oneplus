package com.oneplus.settings.ui;

import android.content.Context;
import android.content.Intent;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.util.AttributeSet;
import com.oneplus.settings.gestures.OPGestureUtils;
import com.oneplus.settings.utils.OPConstants;

public class OPGesturePreference extends Preference implements OnPreferenceClickListener {
    private Context mContext;

    public OPGesturePreference(Context context) {
        super(context);
        init(context);
    }

    public OPGesturePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public OPGesturePreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;
        setOnPreferenceClickListener(this);
    }

    public boolean onPreferenceClick(Preference preference) {
        Intent intent = new Intent(OPConstants.ONEPLUS_GESTURE_APP_LIST_ACTION);
        intent.putExtra(OPConstants.OP_GESTURE_KEY, OPGestureUtils.getGestureTypebyGestureKey(preference.getKey()));
        intent.putExtra(OPConstants.OP_GESTURE_TITLE, preference.getTitle());
        this.mContext.startActivity(intent);
        return false;
    }
}
