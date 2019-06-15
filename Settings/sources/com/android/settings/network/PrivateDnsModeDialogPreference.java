package com.android.settings.network;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.provider.Settings.Global;
import android.support.annotation.VisibleForTesting;
import android.system.Os;
import android.system.OsConstants;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.utils.AnnotationSpan;
import com.android.settings.utils.AnnotationSpan.LinkInfo;
import com.android.settingslib.CustomDialogPreference;
import com.android.settingslib.HelpUtils;
import java.util.HashMap;
import java.util.Map;

public class PrivateDnsModeDialogPreference extends CustomDialogPreference implements OnClickListener, OnCheckedChangeListener, TextWatcher {
    private static final int[] ADDRESS_FAMILIES = new int[]{OsConstants.AF_INET, OsConstants.AF_INET6};
    public static final String ANNOTATION_URL = "url";
    @VisibleForTesting
    static final String HOSTNAME_KEY = "private_dns_specifier";
    @VisibleForTesting
    static final String MODE_KEY = "private_dns_mode";
    private static final Map<String, Integer> PRIVATE_DNS_MAP = new HashMap();
    private static final String TAG = "PrivateDnsModeDialog";
    @VisibleForTesting
    EditText mEditText;
    @VisibleForTesting
    String mMode;
    @VisibleForTesting
    RadioGroup mRadioGroup;
    private final LinkInfo mUrlLinkInfo = new LinkInfo("url", -$$Lambda$PrivateDnsModeDialogPreference$I1bK8FTmQSNCc-qXqZ0usMONEsU.INSTANCE);

    static {
        PRIVATE_DNS_MAP.put("off", Integer.valueOf(R.id.private_dns_mode_off));
        PRIVATE_DNS_MAP.put("opportunistic", Integer.valueOf(R.id.private_dns_mode_opportunistic));
        PRIVATE_DNS_MAP.put("hostname", Integer.valueOf(R.id.private_dns_mode_provider));
    }

    public static String getModeFromSettings(ContentResolver cr) {
        String mode = Global.getString(cr, MODE_KEY);
        if (!PRIVATE_DNS_MAP.containsKey(mode)) {
            mode = Global.getString(cr, "private_dns_default_mode");
        }
        return PRIVATE_DNS_MAP.containsKey(mode) ? mode : "off";
    }

    public static String getHostnameFromSettings(ContentResolver cr) {
        return Global.getString(cr, HOSTNAME_KEY);
    }

    public PrivateDnsModeDialogPreference(Context context) {
        super(context);
    }

    public PrivateDnsModeDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PrivateDnsModeDialogPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public PrivateDnsModeDialogPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    static /* synthetic */ void lambda$new$0(View widget) {
        Context context = widget.getContext();
        Intent intent = HelpUtils.getHelpIntent(context, context.getString(R.string.help_uri_private_dns), context.getClass().getName());
        if (intent != null) {
            try {
                widget.startActivityForResult(intent, 0);
            } catch (ActivityNotFoundException e) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Activity was not found for intent, ");
                stringBuilder.append(intent.toString());
                Log.w(str, stringBuilder.toString());
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public void onBindDialogView(View view) {
        Context context = getContext();
        ContentResolver contentResolver = context.getContentResolver();
        this.mMode = getModeFromSettings(context.getContentResolver());
        this.mEditText = (EditText) view.findViewById(R.id.private_dns_mode_provider_hostname);
        this.mEditText.addTextChangedListener(this);
        this.mEditText.setText(getHostnameFromSettings(contentResolver));
        this.mRadioGroup = (RadioGroup) view.findViewById(R.id.private_dns_radio_group);
        this.mRadioGroup.setOnCheckedChangeListener(this);
        this.mRadioGroup.check(((Integer) PRIVATE_DNS_MAP.getOrDefault(this.mMode, Integer.valueOf(R.id.private_dns_mode_opportunistic))).intValue());
        TextView helpTextView = (TextView) view.findViewById(R.id.private_dns_help_info);
        helpTextView.setMovementMethod(LinkMovementMethod.getInstance());
        if (new LinkInfo(context, "url", HelpUtils.getHelpIntent(context, context.getString(R.string.help_uri_private_dns), context.getClass().getName())).isActionable()) {
            helpTextView.setText(AnnotationSpan.linkify(context.getText(R.string.private_dns_help_message), linkInfo));
        }
    }

    public void onClick(DialogInterface dialog, int which) {
        if (which == -1) {
            Context context = getContext();
            if (this.mMode.equals("hostname")) {
                Global.putString(context.getContentResolver(), HOSTNAME_KEY, this.mEditText.getText().toString());
            }
            FeatureFactory.getFactory(context).getMetricsFeatureProvider().action(context, 1249, this.mMode, new Pair[0]);
            Global.putString(context.getContentResolver(), MODE_KEY, this.mMode);
        }
    }

    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.private_dns_mode_off /*2131362879*/:
                this.mMode = "off";
                break;
            case R.id.private_dns_mode_opportunistic /*2131362880*/:
                this.mMode = "opportunistic";
                break;
            case R.id.private_dns_mode_provider /*2131362881*/:
                this.mMode = "hostname";
                break;
        }
        updateDialogInfo();
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    public void afterTextChanged(Editable s) {
        updateDialogInfo();
    }

    private boolean isWeaklyValidatedHostname(String hostname) {
        String WEAK_HOSTNAME_REGEX = "^[a-zA-Z0-9_.-]+$";
        if (!hostname.matches("^[a-zA-Z0-9_.-]+$")) {
            return false;
        }
        for (int address_family : ADDRESS_FAMILIES) {
            if (Os.inet_pton(address_family, hostname) != null) {
                return false;
            }
        }
        return true;
    }

    private Button getSaveButton() {
        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog == null) {
            return null;
        }
        return dialog.getButton(-1);
    }

    private void updateDialogInfo() {
        boolean modeProvider = "hostname".equals(this.mMode);
        if (this.mEditText != null) {
            this.mEditText.setEnabled(modeProvider);
        }
        Button saveButton = getSaveButton();
        if (saveButton != null) {
            boolean isWeaklyValidatedHostname;
            if (modeProvider) {
                isWeaklyValidatedHostname = isWeaklyValidatedHostname(this.mEditText.getText().toString());
            } else {
                isWeaklyValidatedHostname = true;
            }
            saveButton.setEnabled(isWeaklyValidatedHostname);
        }
    }
}
