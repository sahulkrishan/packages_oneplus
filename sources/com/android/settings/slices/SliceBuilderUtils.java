package com.android.settings.slices;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.Uri.Builder;
import android.support.v4.graphics.drawable.IconCompat;
import android.support.v4.util.Consumer;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import androidx.slice.Slice;
import androidx.slice.builders.ListBuilder;
import androidx.slice.builders.SliceAction;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.R;
import com.android.settings.SubSettings;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.core.SliderPreferenceController;
import com.android.settings.core.TogglePreferenceController;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.search.DatabaseIndexingUtils;
import com.android.settingslib.Utils;
import com.android.settingslib.core.AbstractPreferenceController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SliceBuilderUtils {
    private static final String TAG = "SliceBuilder";

    public static Slice buildSlice(Context context, SliceData sliceData) {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Creating slice for: ");
        stringBuilder.append(sliceData.getPreferenceController());
        Log.d(str, stringBuilder.toString());
        BasePreferenceController controller = getPreferenceController(context, sliceData);
        Pair<Integer, Object> sliceNamePair = Pair.create(Integer.valueOf(854), sliceData.getKey());
        FeatureFactory.getFactory(context).getMetricsFeatureProvider().action(context, 1371, sliceNamePair);
        if (!controller.isAvailable()) {
            return null;
        }
        if (controller.getAvailabilityStatus() == 4) {
            return buildUnavailableSlice(context, sliceData);
        }
        switch (sliceData.getSliceType()) {
            case 0:
                return buildIntentSlice(context, sliceData, controller);
            case 1:
                return buildToggleSlice(context, sliceData, controller);
            case 2:
                return buildSliderSlice(context, sliceData, controller);
            default:
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append("Slice type passed was invalid: ");
                stringBuilder2.append(sliceData.getSliceType());
                throw new IllegalArgumentException(stringBuilder2.toString());
        }
    }

    public static int getSliceType(Context context, String controllerClassName, String controllerKey) {
        return getPreferenceController(context, controllerClassName, controllerKey).getSliceType();
    }

    public static Pair<Boolean, String> getPathData(Uri uri) {
        String[] split = uri.getPath().split("/", 3);
        if (split.length != 3) {
            return null;
        }
        return new Pair(Boolean.valueOf(TextUtils.equals(SliceDeepLinkSpringBoard.INTENT, split[1])), split[2]);
    }

    public static BasePreferenceController getPreferenceController(Context context, SliceData sliceData) {
        return getPreferenceController(context, sliceData.getPreferenceController(), sliceData.getKey());
    }

    public static PendingIntent getActionIntent(Context context, String action, SliceData data) {
        Intent intent = new Intent(action);
        intent.setClass(context, SliceBroadcastReceiver.class);
        intent.putExtra(SettingsSliceProvider.EXTRA_SLICE_KEY, data.getKey());
        intent.putExtra(SettingsSliceProvider.EXTRA_SLICE_PLATFORM_DEFINED, data.isPlatformDefined());
        return PendingIntent.getBroadcast(context, 0, intent, 268435456);
    }

    public static PendingIntent getContentPendingIntent(Context context, SliceData sliceData) {
        return PendingIntent.getActivity(context, 0, getContentIntent(context, sliceData), 0);
    }

    public static CharSequence getSubtitleText(Context context, AbstractPreferenceController controller, SliceData sliceData) {
        CharSequence summaryText = sliceData.getScreenTitle();
        if (isValidSummary(context, summaryText) && !TextUtils.equals(summaryText, sliceData.getTitle())) {
            return summaryText;
        }
        if (controller != null) {
            summaryText = controller.getSummary();
            if (isValidSummary(context, summaryText)) {
                return summaryText;
            }
        }
        summaryText = sliceData.getSummary();
        if (isValidSummary(context, summaryText)) {
            return summaryText;
        }
        return "";
    }

    public static Uri getUri(String path, boolean isPlatformSlice) {
        String authority;
        if (isPlatformSlice) {
            authority = "android.settings.slices";
        } else {
            authority = SettingsSliceProvider.SLICE_AUTHORITY;
        }
        return new Builder().scheme("content").authority(authority).appendPath(path).build();
    }

    @VisibleForTesting
    static Intent getContentIntent(Context context, SliceData sliceData) {
        Uri contentUri = new Builder().appendPath(sliceData.getKey()).build();
        Intent intent = DatabaseIndexingUtils.buildSearchResultPageIntent(context, sliceData.getFragmentClassName(), sliceData.getKey(), sliceData.getScreenTitle().toString(), 0);
        intent.setClassName(context.getPackageName(), SubSettings.class.getName());
        intent.setData(contentUri);
        return intent;
    }

    private static Slice buildToggleSlice(Context context, SliceData sliceData, BasePreferenceController controller) {
        Context context2 = context;
        SliceData sliceData2 = sliceData;
        AbstractPreferenceController abstractPreferenceController = controller;
        PendingIntent contentIntent = getContentPendingIntent(context, sliceData);
        IconCompat icon = IconCompat.createWithResource(context2, sliceData.getIconResource());
        CharSequence subtitleText = getSubtitleText(context2, abstractPreferenceController, sliceData2);
        int color = Utils.getColorAccent(context);
        SliceAction sliceAction = getToggleAction(context2, sliceData2, ((TogglePreferenceController) abstractPreferenceController).isChecked());
        List<String> keywords = buildSliceKeywords(sliceData);
        Consumer consumer = r1;
        ListBuilder accentColor = new ListBuilder(context2, sliceData.getUri(), -1).setAccentColor(color);
        -$$Lambda$SliceBuilderUtils$-H4Orhnw7bHLhjHmJgSvCr6cWP8 -__lambda_slicebuilderutils_-h4orhnw7bhlhjhmjgsvcr6cwp8 = new -$$Lambda$SliceBuilderUtils$-H4Orhnw7bHLhjHmJgSvCr6cWP8(sliceData2, subtitleText, contentIntent, icon, sliceAction);
        return accentColor.addRow(consumer).setKeywords(keywords).build();
    }

    private static Slice buildIntentSlice(Context context, SliceData sliceData, BasePreferenceController controller) {
        PendingIntent contentIntent = getContentPendingIntent(context, sliceData);
        IconCompat icon = IconCompat.createWithResource(context, sliceData.getIconResource());
        CharSequence subtitleText = getSubtitleText(context, controller, sliceData);
        int color = Utils.getColorAccent(context);
        return new ListBuilder(context, sliceData.getUri(), -1).setAccentColor(color).addRow(new -$$Lambda$SliceBuilderUtils$NVqOOBEdIdirrSxUZFgCZXRQ1vA(sliceData, subtitleText, contentIntent, icon)).setKeywords(buildSliceKeywords(sliceData)).build();
    }

    private static Slice buildSliderSlice(Context context, SliceData sliceData, BasePreferenceController controller) {
        Context context2 = context;
        AbstractPreferenceController abstractPreferenceController = controller;
        SliderPreferenceController sliderController = (SliderPreferenceController) abstractPreferenceController;
        PendingIntent actionIntent = getSliderAction(context, sliceData);
        PendingIntent contentIntent = getContentPendingIntent(context, sliceData);
        IconCompat icon = IconCompat.createWithResource(context2, sliceData.getIconResource());
        SliceData sliceData2 = sliceData;
        CharSequence subtitleText = getSubtitleText(context2, abstractPreferenceController, sliceData2);
        int color = Utils.getColorAccent(context);
        SliceAction primaryAction = new SliceAction(contentIntent, icon, sliceData.getTitle());
        List<String> keywords = buildSliceKeywords(sliceData);
        Consumer consumer = r2;
        ListBuilder accentColor = new ListBuilder(context2, sliceData.getUri(), -1).setAccentColor(color);
        -$$Lambda$SliceBuilderUtils$qRPBF1K1kbSIREThP22FAM_L1N0 -__lambda_slicebuilderutils_qrpbf1k1kbsirethp22fam_l1n0 = new -$$Lambda$SliceBuilderUtils$qRPBF1K1kbSIREThP22FAM_L1N0(sliceData2, subtitleText, primaryAction, sliderController, actionIntent);
        return accentColor.addInputRange(consumer).setKeywords(keywords).build();
    }

    private static BasePreferenceController getPreferenceController(Context context, String controllerClassName, String controllerKey) {
        try {
            return BasePreferenceController.createInstance(context, controllerClassName);
        } catch (IllegalStateException e) {
            return BasePreferenceController.createInstance(context, controllerClassName, controllerKey);
        }
    }

    private static SliceAction getToggleAction(Context context, SliceData sliceData, boolean isChecked) {
        return new SliceAction(getActionIntent(context, SettingsSliceProvider.ACTION_TOGGLE_CHANGED, sliceData), null, isChecked);
    }

    private static PendingIntent getSliderAction(Context context, SliceData sliceData) {
        return getActionIntent(context, SettingsSliceProvider.ACTION_SLIDER_CHANGED, sliceData);
    }

    private static boolean isValidSummary(Context context, CharSequence summary) {
        boolean z = false;
        if (summary == null || TextUtils.isEmpty(summary.toString().trim())) {
            return false;
        }
        CharSequence placeHolder = context.getText(R.string.summary_placeholder);
        CharSequence doublePlaceHolder = context.getText(R.string.summary_two_lines_placeholder);
        if (!(TextUtils.equals(summary, placeHolder) || TextUtils.equals(summary, doublePlaceHolder))) {
            z = true;
        }
        return z;
    }

    private static List<String> buildSliceKeywords(SliceData data) {
        List<String> keywords = new ArrayList();
        keywords.add(data.getTitle());
        if (!TextUtils.equals(data.getTitle(), data.getScreenTitle())) {
            keywords.add(data.getScreenTitle().toString());
        }
        String keywordString = data.getKeywords();
        if (keywordString != null) {
            keywords.addAll((List) Arrays.stream(keywordString.split(",")).map(-$$Lambda$SliceBuilderUtils$H4nQFDLpU9w8T-x-9Cq8nlH2grw.INSTANCE).collect(Collectors.toList()));
        }
        return keywords;
    }

    static /* synthetic */ String lambda$buildSliceKeywords$3(String s) {
        String trim = s.trim();
        s = trim;
        return trim;
    }

    private static Slice buildUnavailableSlice(Context context, SliceData data) {
        CharSequence title = data.getTitle();
        List<String> keywords = buildSliceKeywords(data);
        int color = Utils.getColorAccent(context);
        CharSequence summary = context.getText(R.string.disabled_dependent_setting_summary);
        IconCompat icon = IconCompat.createWithResource(context, data.getIconResource());
        return new ListBuilder(context, data.getUri(), -1).setAccentColor(color).addRow(new -$$Lambda$SliceBuilderUtils$JGXESizo03yh-FrnCdjYorH4I8Y(title, icon, summary, new SliceAction(getContentPendingIntent(context, data), icon, title))).setKeywords(keywords).build();
    }
}
