package com.oneplus.settings;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;
import com.android.settings.R;
import com.oneplus.settings.utils.OPUtils;

public class OPSecurityDetectionActivity extends BaseActivity {
    static final String LEGAL_OPPO_URL = "https://www.oppo.com/cn/service/help/640?id=640&name=%E6%9C%8D%E5%8A%A1%E6%94%BF%E7%AD%96&hdscreen=1";
    static final String LEGAL_TENCENT_URL = "http://www.qq.com/privacy.htm";

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_security_detection);
        TextView tencent_legal = (TextView) findViewById(R.id.tencent_legal);
        TextView oppo_legal = (TextView) findViewById(R.id.oppo_legal);
        tencent_legal.setText(OPUtils.parseLink(getString(R.string.op_app_security_check_text_legal_tencent), LEGAL_TENCENT_URL, getString(R.string.op_app_security_check_text_legal_tencent_link), ""));
        tencent_legal.setMovementMethod(LinkMovementMethod.getInstance());
        oppo_legal.setText(OPUtils.parseLink(getString(R.string.op_app_security_check_text_legal_oppo), LEGAL_OPPO_URL, getString(R.string.op_app_security_check_text_legal_oppo_link), ""));
        oppo_legal.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
