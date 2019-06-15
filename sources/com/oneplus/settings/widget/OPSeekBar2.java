package com.oneplus.settings.widget;

import android.content.Context;
import android.util.AttributeSet;
import com.android.settings.R;
import com.oneplus.lib.widget.OPSeekBar;
import com.oneplus.settings.utils.OPUtils;

public class OPSeekBar2 extends OPSeekBar {
    public OPSeekBar2(Context context) {
        super(context);
        init();
    }

    public OPSeekBar2(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public OPSeekBar2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        if (OPUtils.isBlackModeOn(getContext().getContentResolver())) {
            setProgressDrawable(getResources().getDrawable(R.drawable.op_seekbar_track_dark, getContext().getTheme()));
            setThumb(getResources().getDrawable(R.drawable.op_seekbar_thumb_dark, getContext().getTheme()));
            return;
        }
        setProgressDrawable(getResources().getDrawable(R.drawable.op_seekbar_track_light, getContext().getTheme()));
        setThumb(getResources().getDrawable(R.drawable.op_seekbar_thumb_light, getContext().getTheme()));
    }
}
