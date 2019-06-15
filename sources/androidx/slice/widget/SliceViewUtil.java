package androidx.slice.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.IconCompat;
import android.text.format.DateUtils;
import android.view.ViewGroup;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import java.util.Calendar;

@RestrictTo({Scope.LIBRARY})
public class SliceViewUtil {
    @ColorInt
    public static int getColorAccent(@NonNull Context context) {
        return getColorAttr(context, 16843829);
    }

    @ColorInt
    public static int getColorError(@NonNull Context context) {
        return getColorAttr(context, 16844099);
    }

    @ColorInt
    public static int getDefaultColor(@NonNull Context context, int resId) {
        return ContextCompat.getColorStateList(context, resId).getDefaultColor();
    }

    @ColorInt
    public static int getDisabled(@NonNull Context context, int inputColor) {
        return applyAlphaAttr(context, 16842803, inputColor);
    }

    @ColorInt
    public static int applyAlphaAttr(@NonNull Context context, @AttrRes int attr, int inputColor) {
        TypedArray ta = context.obtainStyledAttributes(new int[]{attr});
        float alpha = ta.getFloat(0, 0.0f);
        ta.recycle();
        return applyAlpha(alpha, inputColor);
    }

    @ColorInt
    public static int applyAlpha(float alpha, int inputColor) {
        return Color.argb((int) (alpha * ((float) Color.alpha(inputColor))), Color.red(inputColor), Color.green(inputColor), Color.blue(inputColor));
    }

    @ColorInt
    public static int getColorAttr(@NonNull Context context, @AttrRes int attr) {
        TypedArray ta = context.obtainStyledAttributes(new int[]{attr});
        int colorAccent = ta.getColor(0, 0);
        ta.recycle();
        return colorAccent;
    }

    public static int getThemeAttr(@NonNull Context context, @AttrRes int attr) {
        TypedArray ta = context.obtainStyledAttributes(new int[]{attr});
        int theme = ta.getResourceId(0, 0);
        ta.recycle();
        return theme;
    }

    public static Drawable getDrawable(@NonNull Context context, @AttrRes int attr) {
        TypedArray ta = context.obtainStyledAttributes(new int[]{attr});
        Drawable drawable = ta.getDrawable(0);
        ta.recycle();
        return drawable;
    }

    public static IconCompat createIconFromDrawable(Drawable d) {
        if (d instanceof BitmapDrawable) {
            return IconCompat.createWithBitmap(((BitmapDrawable) d).getBitmap());
        }
        Bitmap b = Bitmap.createBitmap(d.getIntrinsicWidth(), d.getIntrinsicHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(b);
        d.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        d.draw(canvas);
        return IconCompat.createWithBitmap(b);
    }

    public static void createCircledIcon(@NonNull Context context, int iconSizePx, IconCompat icon, boolean isLarge, ViewGroup parent) {
        ImageView v = new ImageView(context);
        v.setImageDrawable(icon.loadDrawable(context));
        v.setScaleType(isLarge ? ScaleType.CENTER_CROP : ScaleType.CENTER_INSIDE);
        parent.addView(v);
        LayoutParams lp = (LayoutParams) v.getLayoutParams();
        if (isLarge) {
            Bitmap iconBm = Bitmap.createBitmap(iconSizePx, iconSizePx, Config.ARGB_8888);
            Canvas iconCanvas = new Canvas(iconBm);
            v.layout(0, 0, iconSizePx, iconSizePx);
            v.draw(iconCanvas);
            v.setImageBitmap(getCircularBitmap(iconBm));
        } else {
            v.setColorFilter(-1);
        }
        lp.width = iconSizePx;
        lp.height = iconSizePx;
        lp.gravity = 17;
    }

    @NonNull
    public static Bitmap getCircularBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawCircle((float) (bitmap.getWidth() / 2), (float) (bitmap.getHeight() / 2), (float) (bitmap.getWidth() / 2), paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    public static CharSequence getRelativeTimeString(long time) {
        return DateUtils.getRelativeTimeSpanString(time, Calendar.getInstance().getTimeInMillis(), 60000, 262144);
    }

    public static int resolveLayoutDirection(int layoutDir) {
        if (layoutDir == 2 || layoutDir == 3 || layoutDir == 1 || layoutDir == 0) {
            return layoutDir;
        }
        return -1;
    }

    private SliceViewUtil() {
    }
}
