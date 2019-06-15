package com.android.settings.accessibility;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.AccessibilityServiceInfo.CapabilityInfo;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.graphics.drawable.Drawable;
import android.os.storage.StorageManager;
import android.text.BidiFormatter;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.android.settings.R;
import java.util.List;
import java.util.Locale;

public class AccessibilityServiceWarning {
    public static Dialog createCapabilitiesDialog(Activity parentActivity, AccessibilityServiceInfo info, OnClickListener listener) {
        AlertDialog ad = new Builder(parentActivity).setTitle(parentActivity.getString(R.string.enable_service_title, new Object[]{getServiceName(parentActivity, info)})).setView(createEnableDialogContentView(parentActivity, info)).setPositiveButton(17039370, listener).setNegativeButton(17039360, listener).create();
        OnTouchListener filterTouchListener = -$$Lambda$AccessibilityServiceWarning$D3xqJyTKInilYjQAxG1fpVU1D1M.INSTANCE;
        Window window = ad.getWindow();
        LayoutParams params = window.getAttributes();
        params.privateFlags |= 524288;
        window.setAttributes(params);
        ad.create();
        ad.getButton(-1).setOnTouchListener(filterTouchListener);
        ad.setCanceledOnTouchOutside(true);
        return ad;
    }

    static /* synthetic */ boolean lambda$createCapabilitiesDialog$0(View v, MotionEvent event) {
        if ((event.getFlags() & 1) == 0 && (event.getFlags() & 2) == 0) {
            return false;
        }
        if (event.getAction() == 1) {
            Toast.makeText(v.getContext(), R.string.touch_filtered_warning, 0).show();
        }
        return true;
    }

    private static boolean isFullDiskEncrypted() {
        return StorageManager.isNonDefaultBlockEncrypted();
    }

    private static View createEnableDialogContentView(Context context, AccessibilityServiceInfo info) {
        Context context2 = context;
        LayoutInflater inflater = (LayoutInflater) context2.getSystemService("layout_inflater");
        View content = inflater.inflate(R.layout.enable_accessibility_service_dialog_content, null);
        TextView encryptionWarningView = (TextView) content.findViewById(R.id.encryption_warning);
        if (isFullDiskEncrypted()) {
            encryptionWarningView.setText(context2.getString(R.string.enable_service_encryption_warning, new Object[]{getServiceName(context, info)}));
            encryptionWarningView.setVisibility(0);
        } else {
            encryptionWarningView.setVisibility(8);
        }
        TextView capabilitiesHeaderView = (TextView) content.findViewById(R.id.capabilities_header);
        capabilitiesHeaderView.setText(context2.getString(R.string.capabilities_list_title, new Object[]{getServiceName(context, info)}));
        LinearLayout capabilitiesView = (LinearLayout) content.findViewById(R.id.capabilities);
        int i = 17367097;
        View capabilityView = inflater.inflate(17367097, null);
        ImageView imageView = (ImageView) capabilityView.findViewById(16909166);
        Drawable dotDrawable = context2.getDrawable(17302765);
        dotDrawable.setTint(context2.getColor(R.color.oneplus_settings_text_color_primary));
        imageView.setImageDrawable(dotDrawable);
        TextView labelView = (TextView) capabilityView.findViewById(16909170);
        labelView.setText(context2.getString(R.string.capability_title_receiveAccessibilityEvents));
        labelView.setTextColor(context2.getColor(R.color.oneplus_settings_text_color_primary));
        TextView descriptionView = (TextView) capabilityView.findViewById(16909172);
        descriptionView.setText(context2.getString(R.string.capability_desc_receiveAccessibilityEvents));
        descriptionView.setTextColor(context2.getColor(R.color.oneplus_contorl_text_color_secondary_default));
        List<CapabilityInfo> capabilities = info.getCapabilityInfos(context2);
        capabilitiesView.addView(capabilityView);
        int capabilityCount = capabilities.size();
        int i2 = 0;
        while (true) {
            int i3 = i2;
            TextView capabilitiesHeaderView2;
            if (i3 < capabilityCount) {
                TextView encryptionWarningView2 = encryptionWarningView;
                CapabilityInfo encryptionWarningView3 = (CapabilityInfo) capabilities.get(i3);
                capabilitiesHeaderView2 = capabilitiesHeaderView;
                capabilityView = inflater.inflate(i, null);
                ((ImageView) capabilityView.findViewById(16909166)).setImageDrawable(dotDrawable);
                labelView = (TextView) capabilityView.findViewById(16909170);
                labelView.setText(context2.getString(encryptionWarningView3.titleResId));
                labelView.setTextColor(context2.getColor(R.color.oneplus_settings_text_color_primary));
                descriptionView = (TextView) capabilityView.findViewById(16909172);
                descriptionView.setText(context2.getString(encryptionWarningView3.descResId));
                descriptionView.setTextColor(context2.getColor(R.color.oneplus_contorl_text_color_secondary_default));
                capabilitiesView.addView(capabilityView);
                i2 = i3 + 1;
                encryptionWarningView = encryptionWarningView2;
                capabilitiesHeaderView = capabilitiesHeaderView2;
                i = 17367097;
            } else {
                capabilitiesHeaderView2 = capabilitiesHeaderView;
                return content;
            }
        }
    }

    private static CharSequence getServiceName(Context context, AccessibilityServiceInfo info) {
        Locale locale = context.getResources().getConfiguration().getLocales().get(0);
        return BidiFormatter.getInstance(locale).unicodeWrap(info.getResolveInfo().loadLabel(context.getPackageManager()));
    }
}
