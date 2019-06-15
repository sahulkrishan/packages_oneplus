package com.android.settings.location;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.Uri.Builder;
import android.support.v4.graphics.drawable.IconCompat;
import androidx.slice.Slice;
import androidx.slice.builders.ListBuilder;
import androidx.slice.builders.SliceAction;
import com.android.settings.R;
import com.android.settings.SubSettings;
import com.android.settings.search.DatabaseIndexingUtils;
import com.android.settingslib.Utils;

public class LocationSliceBuilder {
    public static final Uri LOCATION_URI = new Builder().scheme("content").authority("android.settings.slices").appendPath("action").appendPath("location").build();

    private LocationSliceBuilder() {
    }

    public static Slice getSlice(Context context) {
        IconCompat icon = IconCompat.createWithResource(context, (int) R.drawable.ic_signal_location);
        CharSequence title = context.getString(R.string.location_settings_title);
        return new ListBuilder(context, LOCATION_URI, -1).setAccentColor(Utils.getColorAccent(context)).addRow(new -$$Lambda$LocationSliceBuilder$b_EpqAhS4ORYylfhNREU0o0sGYE(title, icon, new SliceAction(getPrimaryAction(context), icon, title))).build();
    }

    public static Intent getIntent(Context context) {
        String screenTitle = context.getText(R.string.location_settings_title).toString();
        return DatabaseIndexingUtils.buildSearchResultPageIntent(context, LocationSettings.class.getName(), "location", screenTitle, 63).setClassName(context.getPackageName(), SubSettings.class.getName()).setData(new Builder().appendPath("location").build());
    }

    private static PendingIntent getPrimaryAction(Context context) {
        return PendingIntent.getActivity(context, 0, getIntent(context), 0);
    }
}
