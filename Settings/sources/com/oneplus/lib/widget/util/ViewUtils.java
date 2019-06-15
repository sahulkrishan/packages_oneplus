package com.oneplus.lib.widget.util;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build.VERSION;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import com.oneplus.commonctrl.R;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ViewUtils {
    private static final String TAG = "ViewUtils";
    public static final int VIEW_STATE_ACCELERATED = 64;
    public static final int VIEW_STATE_ACTIVATED = 32;
    public static final int VIEW_STATE_DRAG_CAN_ACCEPT = 256;
    public static final int VIEW_STATE_DRAG_HOVERED = 512;
    public static final int VIEW_STATE_ENABLED = 8;
    public static final int VIEW_STATE_FOCUSED = 4;
    public static final int VIEW_STATE_HOVERED = 128;
    static final int[] VIEW_STATE_IDS = new int[]{16842909, 1, 16842913, 2, 16842908, 4, 16842910, 8, 16842919, 16, 16843518, 32, 16843547, 64, 16843623, 128, 16843624, 256, 16843625, 512};
    public static final int VIEW_STATE_PRESSED = 16;
    public static final int VIEW_STATE_SELECTED = 2;
    private static final int[][] VIEW_STATE_SETS = new int[(1 << (VIEW_STATE_IDS.length / 2))][];
    public static final int VIEW_STATE_WINDOW_FOCUSED = 1;
    private static Method sComputeFitSystemWindowsMethod;

    static {
        int j;
        int[] orderedIds = new int[VIEW_STATE_IDS.length];
        for (int i = 0; i < R.styleable.OPViewDrawableStates.length; i++) {
            int viewState = R.styleable.OPViewDrawableStates[i];
            for (j = 0; j < VIEW_STATE_IDS.length; j += 2) {
                if (VIEW_STATE_IDS[j] == viewState) {
                    orderedIds[i * 2] = viewState;
                    orderedIds[(i * 2) + 1] = VIEW_STATE_IDS[j + 1];
                }
            }
        }
        for (j = 0; j < VIEW_STATE_SETS.length; j++) {
            int[] set = new int[Integer.bitCount(j)];
            int pos = 0;
            for (int j2 = 0; j2 < orderedIds.length; j2 += 2) {
                if ((orderedIds[j2 + 1] & j) != 0) {
                    int pos2 = pos + 1;
                    set[pos] = orderedIds[j2];
                    pos = pos2;
                }
            }
            VIEW_STATE_SETS[j] = set;
        }
        if (VERSION.SDK_INT >= 18) {
            try {
                sComputeFitSystemWindowsMethod = View.class.getDeclaredMethod("computeFitSystemWindows", new Class[]{Rect.class, Rect.class});
                if (!sComputeFitSystemWindowsMethod.isAccessible()) {
                    sComputeFitSystemWindowsMethod.setAccessible(true);
                }
            } catch (NoSuchMethodException e) {
                Log.d(TAG, "Could not find method computeFitSystemWindows. Oh well.");
            }
        }
    }

    public static int[] getViewState(int mask) {
        if (mask < VIEW_STATE_SETS.length) {
            return VIEW_STATE_SETS[mask];
        }
        throw new IllegalArgumentException("Invalid state set mask");
    }

    public static int getAttrDimen(Context context, int attrRes) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attrRes, typedValue, true);
        return TypedValue.complexToDimensionPixelSize(typedValue.data, context.getResources().getDisplayMetrics());
    }

    public static boolean isLayoutRtl(View view) {
        return view.getLayoutDirection() == 1;
    }

    public static void computeFitSystemWindows(View view, Rect inoutInsets, Rect outLocalInsets) {
        if (sComputeFitSystemWindowsMethod != null) {
            try {
                sComputeFitSystemWindowsMethod.invoke(view, new Object[]{inoutInsets, outLocalInsets});
            } catch (Exception e) {
                Log.d(TAG, "Could not invoke computeFitSystemWindows", e);
            }
        }
    }

    public static int combineMeasuredStates(int curState, int newState) {
        return curState | newState;
    }

    public static void makeOptionalFitsSystemWindows(View view) {
        if (VERSION.SDK_INT >= 16) {
            try {
                Method method = view.getClass().getMethod("makeOptionalFitsSystemWindows", new Class[0]);
                if (!method.isAccessible()) {
                    method.setAccessible(true);
                }
                method.invoke(view, new Object[0]);
            } catch (NoSuchMethodException e) {
                Log.d(TAG, "Could not find method makeOptionalFitsSystemWindows. Oh well...");
            } catch (InvocationTargetException e2) {
                Log.d(TAG, "Could not invoke makeOptionalFitsSystemWindows", e2);
            } catch (IllegalAccessException e3) {
                Log.d(TAG, "Could not invoke makeOptionalFitsSystemWindows", e3);
            }
        }
    }

    public static int px2dip(Context context, float pxValue) {
        return (int) ((pxValue / context.getResources().getDisplayMetrics().density) + 0.5f);
    }

    public static int dip2px(Context context, float dipValue) {
        return (int) ((dipValue * context.getResources().getDisplayMetrics().density) + 0.5f);
    }

    public static boolean isVisibleToUser(View view, Rect visibleRect) {
        return view.isAttachedToWindow() && view.getGlobalVisibleRect(visibleRect);
    }

    public static void scaleRect(Rect rect, float scale) {
        if (scale != 1.0f) {
            rect.left = (int) ((((float) rect.left) * scale) + 0.5f);
            rect.top = (int) ((((float) rect.top) * scale) + 0.5f);
            rect.right = (int) ((((float) rect.right) * scale) + 0.5f);
            rect.bottom = (int) ((((float) rect.bottom) * scale) + 0.5f);
        }
    }
}
