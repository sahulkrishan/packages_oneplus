package com.android.settings.accessibility;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import com.android.internal.widget.SubtitleView;
import com.android.settings.R;

public class EdgeTypePreference extends ListDialogPreference {
    private static final int DEFAULT_BACKGROUND_COLOR = 0;
    private static final int DEFAULT_EDGE_COLOR = -16777216;
    private static final float DEFAULT_FONT_SIZE = 32.0f;
    private static final int DEFAULT_FOREGROUND_COLOR = -1;

    public EdgeTypePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        Resources res = context.getResources();
        setValues(res.getIntArray(R.array.captioning_edge_type_selector_values));
        setTitles(res.getStringArray(R.array.captioning_edge_type_selector_titles));
        setDialogLayoutResource(R.layout.grid_picker_dialog);
        setListItemLayoutResource(R.layout.preset_picker_item);
    }

    public boolean shouldDisableDependents() {
        return getValue() == 0 || super.shouldDisableDependents();
    }

    /* Access modifiers changed, original: protected */
    public void onBindListItem(View view, int index) {
        SubtitleView preview = (SubtitleView) view.findViewById(R.id.preview);
        preview.setForegroundColor(-1);
        preview.setBackgroundColor(0);
        preview.setTextSize(DEFAULT_FONT_SIZE * getContext().getResources().getDisplayMetrics().density);
        preview.setEdgeType(getValueAt(index));
        preview.setEdgeColor(-16777216);
        CharSequence title = getTitleAt(index);
        if (title != null) {
            ((TextView) view.findViewById(R.id.summary)).setText(title);
        }
    }
}
