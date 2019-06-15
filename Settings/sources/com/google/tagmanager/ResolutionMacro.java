package com.google.tagmanager;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import com.google.analytics.containertag.common.FunctionType;
import com.google.analytics.midtier.proto.containertag.TypeSystem.Value;
import java.util.Map;

class ResolutionMacro extends FunctionCallImplementation {
    private static final String ID = FunctionType.RESOLUTION.toString();
    private final Context mContext;

    public static String getFunctionId() {
        return ID;
    }

    public ResolutionMacro(Context context) {
        super(ID, new String[0]);
        this.mContext = context;
    }

    public boolean isCacheable() {
        return true;
    }

    public Value evaluate(Map<String, Value> map) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(screenWidth);
        stringBuilder.append("x");
        stringBuilder.append(screenHeight);
        return Types.objectToValue(stringBuilder.toString());
    }
}
