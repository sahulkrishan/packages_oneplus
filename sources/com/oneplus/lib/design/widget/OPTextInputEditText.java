package com.oneplus.lib.design.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewParent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import com.oneplus.lib.widget.OPEditText;

public class OPTextInputEditText extends OPEditText {
    public OPTextInputEditText(Context context) {
        super(context);
    }

    public OPTextInputEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OPTextInputEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        InputConnection ic = super.onCreateInputConnection(outAttrs);
        if (ic != null && outAttrs.hintText == null) {
            for (ViewParent parent = getParent(); parent instanceof View; parent = parent.getParent()) {
                if (parent instanceof OPTextInputLayout) {
                    outAttrs.hintText = ((OPTextInputLayout) parent).getHint();
                    break;
                }
            }
        }
        return ic;
    }
}
