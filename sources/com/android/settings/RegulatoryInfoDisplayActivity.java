package com.android.settings;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemProperties;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.Locale;

public class RegulatoryInfoDisplayActivity extends Activity implements OnDismissListener {
    private static final String DEFAULT_REGULATORY_INFO_FILEPATH = "/data/misc/elabel/regulatory_info.png";
    private static final String REGULATORY_INFO_FILEPATH_TEMPLATE = "/data/misc/elabel/regulatory_info_%s.png";
    private final String REGULATORY_INFO_RESOURCE = "regulatory_info";

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Resources resources = getResources();
        if (!resources.getBoolean(R.bool.config_show_regulatory_info)) {
            finish();
        }
        Builder builder = new Builder(this).setTitle(R.string.regulatory_labels).setOnDismissListener(this);
        boolean regulatoryInfoDrawableExists = false;
        Bitmap regulatoryInfoBitmap = BitmapFactory.decodeFile(getRegulatoryInfoImageFileName());
        if (regulatoryInfoBitmap != null) {
            regulatoryInfoDrawableExists = true;
        }
        int resId = 0;
        if (!regulatoryInfoDrawableExists) {
            resId = getResourceId();
        }
        if (resId != 0) {
            try {
                Drawable d = getDrawable(resId);
                boolean z = d.getIntrinsicWidth() > 2 && d.getIntrinsicHeight() > 2;
                regulatoryInfoDrawableExists = z;
            } catch (NotFoundException e) {
                regulatoryInfoDrawableExists = false;
            }
        }
        CharSequence regulatoryText = resources.getText(R.string.regulatory_info_text);
        if (regulatoryInfoDrawableExists) {
            View view = getLayoutInflater().inflate(R.layout.regulatory_info, null);
            ImageView image = (ImageView) view.findViewById(R.id.regulatoryInfo);
            if (regulatoryInfoBitmap != null) {
                image.setImageBitmap(regulatoryInfoBitmap);
            } else {
                image.setImageResource(resId);
            }
            builder.setView(view);
            builder.show();
        } else if (regulatoryText.length() > 0) {
            builder.setMessage(regulatoryText);
            ((TextView) builder.show().findViewById(16908299)).setGravity(17);
        } else {
            finish();
        }
    }

    private int getResourceId() {
        int resId = getResources().getIdentifier("regulatory_info", "drawable", getPackageName());
        String sku = getSku();
        if (TextUtils.isEmpty(sku)) {
            return resId;
        }
        String regulatory_info_res = new StringBuilder();
        regulatory_info_res.append("regulatory_info_");
        regulatory_info_res.append(sku.toLowerCase());
        int id = getResources().getIdentifier(regulatory_info_res.toString(), "drawable", getPackageName());
        if (id != 0) {
            return id;
        }
        return resId;
    }

    public void onDismiss(DialogInterface dialog) {
        finish();
    }

    @VisibleForTesting
    public static String getSku() {
        return SystemProperties.get("ro.boot.hardware.sku", "");
    }

    @VisibleForTesting
    public static String getRegulatoryInfoImageFileName() {
        if (TextUtils.isEmpty(getSku())) {
            return DEFAULT_REGULATORY_INFO_FILEPATH;
        }
        return String.format(Locale.US, REGULATORY_INFO_FILEPATH_TEMPLATE, new Object[]{sku.toLowerCase()});
    }
}
