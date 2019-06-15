package androidx.slice.compat;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v4.text.BidiFormatter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
import android.text.Html;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.widget.TextView;
import androidx.slice.core.R;

@RestrictTo({Scope.LIBRARY})
public class SlicePermissionActivity extends Activity implements OnClickListener, OnDismissListener {
    private static final float MAX_LABEL_SIZE_PX = 500.0f;
    private static final String TAG = "SlicePermissionActivity";
    private String mCallingPkg;
    private String mProviderPkg;
    private Uri mUri;

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mUri = (Uri) getIntent().getParcelableExtra(SliceProviderCompat.EXTRA_BIND_URI);
        this.mCallingPkg = getIntent().getStringExtra("pkg");
        this.mProviderPkg = getIntent().getStringExtra(SliceProviderCompat.EXTRA_PROVIDER_PKG);
        try {
            PackageManager pm = getPackageManager();
            CharSequence app1 = BidiFormatter.getInstance().unicodeWrap(loadSafeLabel(pm, pm.getApplicationInfo(this.mCallingPkg, 0)).toString());
            CharSequence app2 = BidiFormatter.getInstance().unicodeWrap(loadSafeLabel(pm, pm.getApplicationInfo(this.mProviderPkg, 0)).toString());
            AlertDialog dialog = new Builder(this).setTitle(getString(R.string.abc_slice_permission_title, new Object[]{app1, app2})).setView(R.layout.abc_slice_permission_request).setNegativeButton(R.string.abc_slice_permission_deny, (OnClickListener) this).setPositiveButton(R.string.abc_slice_permission_allow, (OnClickListener) this).setOnDismissListener(this).show();
            ((TextView) dialog.getWindow().getDecorView().findViewById(R.id.text1)).setText(getString(R.string.abc_slice_permission_text_1, new Object[]{app2}));
            ((TextView) dialog.getWindow().getDecorView().findViewById(R.id.text2)).setText(getString(R.string.abc_slice_permission_text_2, new Object[]{app2}));
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Couldn't find package", e);
            finish();
        }
    }

    private CharSequence loadSafeLabel(PackageManager pm, ApplicationInfo appInfo) {
        String labelStr = Html.fromHtml(appInfo.loadLabel(pm).toString()).toString();
        int labelLength = labelStr.length();
        String labelStr2 = labelStr;
        int offset = 0;
        while (offset < labelLength) {
            int codePoint = labelStr2.codePointAt(offset);
            int type = Character.getType(codePoint);
            if (type == 13 || type == 15 || type == 14) {
                labelStr2 = labelStr2.substring(0, offset);
                break;
            }
            if (type == 12) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(labelStr2.substring(0, offset));
                stringBuilder.append(" ");
                stringBuilder.append(labelStr2.substring(Character.charCount(codePoint) + offset));
                labelStr2 = stringBuilder.toString();
            }
            offset += Character.charCount(codePoint);
        }
        String labelStr3 = labelStr2.trim();
        if (labelStr3.isEmpty()) {
            return appInfo.packageName;
        }
        TextPaint paint = new TextPaint();
        paint.setTextSize(42.0f);
        return TextUtils.ellipsize(labelStr3, paint, MAX_LABEL_SIZE_PX, TruncateAt.END);
    }

    public void onClick(DialogInterface dialog, int which) {
        if (which == -1) {
            SliceProviderCompat.grantSlicePermission(this, getPackageName(), this.mCallingPkg, this.mUri.buildUpon().path("").build());
        }
        finish();
    }

    public void onDismiss(DialogInterface dialog) {
        finish();
    }
}
