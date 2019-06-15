package com.oneplus.settings;

import android.content.Context;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;
import com.android.settings.LegalSettings;
import com.android.settings.R;
import com.oneplus.settings.utils.OPUtils;

public class OPSecurityDetectionActivity extends BaseActivity {
    private static final int KEY_SECURITY_DETECTION_TYPE = 11;
    static final String LEGAL_OPPO_URL = "https://www.oppo.com/cn/service/help/640?id=640&name=%E6%9C%8D%E5%8A%A1%E6%94%BF%E7%AD%96&hdscreen=1";
    static final String LEGAL_TENCENT_URL = "http://www.qq.com/privacy.htm";
    private Context mContext;

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = this;
        setContentView(R.layout.activity_app_security_detection);
        TextView tencent_legal = (TextView) findViewById(R.id.tencent_legal);
        TextView oppo_legal = (TextView) findViewById(R.id.oppo_legal);
        tencent_legal.setText(OPUtils.parseLink(getString(R.string.op_app_security_check_text_legal_tencent), LEGAL_TENCENT_URL, getString(R.string.op_app_security_check_text_legal_tencent_link), ""));
        tencent_legal.setMovementMethod(LinkMovementMethod.getInstance());
        String linkFrontContent2 = getString(R.string.op_app_security_check_text_legal_oppo);
        String linkUrl2 = LEGAL_OPPO_URL;
        oppo_legal.setText(OPUtils.parseLinkLaunchAction(linkFrontContent2, getString(R.string.op_app_security_check_text_legal_oppo_link), "", new ClickableSpan() {
            public void onClick(View view) {
                LegalSettings.startLegalActivity(OPSecurityDetectionActivity.this.mContext, 11);
            }
        }));
        oppo_legal.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
