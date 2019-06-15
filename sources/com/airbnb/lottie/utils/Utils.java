package com.airbnb.lottie.utils;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.os.Build.VERSION;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import com.airbnb.lottie.L;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.animation.content.TrimPathContent;
import java.io.Closeable;

public final class Utils {
    private static final float SQRT_2 = ((float) Math.sqrt(2.0d));
    private static DisplayMetrics displayMetrics;
    private static final PathMeasure pathMeasure = new PathMeasure();
    private static final float[] points = new float[4];
    private static final Path tempPath = new Path();
    private static final Path tempPath2 = new Path();

    private Utils() {
    }

    public static Path createPath(PointF startPoint, PointF endPoint, PointF cp1, PointF cp2) {
        Path path = new Path();
        path.moveTo(startPoint.x, startPoint.y);
        if (cp1 == null || cp2 == null || (cp1.length() == 0.0f && cp2.length() == 0.0f)) {
            path.lineTo(endPoint.x, endPoint.y);
        } else {
            Path path2 = path;
            path2.cubicTo(cp1.x + startPoint.x, cp1.y + startPoint.y, cp2.x + endPoint.x, cp2.y + endPoint.y, endPoint.x, endPoint.y);
        }
        return path;
    }

    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception e) {
            }
        }
    }

    public static int getScreenWidth(Context context) {
        if (displayMetrics == null) {
            displayMetrics = new DisplayMetrics();
        }
        ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    public static int getScreenHeight(Context context) {
        if (displayMetrics == null) {
            displayMetrics = new DisplayMetrics();
        }
        ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    public static float getScale(Matrix matrix) {
        points[0] = 0.0f;
        points[1] = 0.0f;
        points[2] = SQRT_2;
        points[3] = SQRT_2;
        matrix.mapPoints(points);
        return ((float) Math.hypot((double) (points[2] - points[0]), (double) (points[3] - points[1]))) / 2.0f;
    }

    public static void applyTrimPathIfNeeded(Path path, @Nullable TrimPathContent trimPath) {
        if (trimPath != null) {
            applyTrimPathIfNeeded(path, ((Float) trimPath.getStart().getValue()).floatValue() / 100.0f, ((Float) trimPath.getEnd().getValue()).floatValue() / 100.0f, ((Float) trimPath.getOffset().getValue()).floatValue() / 360.0f);
        }
    }

    public static void applyTrimPathIfNeeded(Path path, float startValue, float endValue, float offsetValue) {
        L.beginSection("applyTrimPathIfNeeded");
        pathMeasure.setPath(path, false);
        float length = pathMeasure.getLength();
        if (startValue == 1.0f && endValue == 0.0f) {
            L.endSection("applyTrimPathIfNeeded");
        } else if (length < 1.0f || ((double) Math.abs((endValue - startValue) - 1.0f)) < 0.01d) {
            L.endSection("applyTrimPathIfNeeded");
        } else {
            float start = length * startValue;
            float end = length * endValue;
            float offset = offsetValue * length;
            float newStart = Math.min(start, end) + offset;
            float newEnd = Math.max(start, end) + offset;
            if (newStart >= length && newEnd >= length) {
                newStart = (float) MiscUtils.floorMod(newStart, length);
                newEnd = (float) MiscUtils.floorMod(newEnd, length);
            }
            if (newStart < 0.0f) {
                newStart = (float) MiscUtils.floorMod(newStart, length);
            }
            if (newEnd < 0.0f) {
                newEnd = (float) MiscUtils.floorMod(newEnd, length);
            }
            if (newStart == newEnd) {
                path.reset();
                L.endSection("applyTrimPathIfNeeded");
                return;
            }
            if (newStart >= newEnd) {
                newStart -= length;
            }
            tempPath.reset();
            pathMeasure.getSegment(newStart, newEnd, tempPath, true);
            if (newEnd > length) {
                tempPath2.reset();
                pathMeasure.getSegment(0.0f, newEnd % length, tempPath2, true);
                tempPath.addPath(tempPath2);
            } else if (newStart < 0.0f) {
                tempPath2.reset();
                pathMeasure.getSegment(length + newStart, length, tempPath2, true);
                tempPath.addPath(tempPath2);
            }
            path.set(tempPath);
            L.endSection("applyTrimPathIfNeeded");
        }
    }

    public static boolean isAtLeastVersion(LottieComposition composition, int major, int minor, int patch) {
        boolean z = false;
        if (composition.getMajorVersion() < major) {
            return false;
        }
        if (composition.getMajorVersion() > major) {
            return true;
        }
        if (composition.getMinorVersion() < minor) {
            return false;
        }
        if (composition.getMinorVersion() > minor) {
            return true;
        }
        if (composition.getPatchVersion() >= patch) {
            z = true;
        }
        return z;
    }

    public static int hashFor(float a, float b, float c, float d) {
        int result = 17;
        if (a != 0.0f) {
            result = (int) (((float) (31 * 17)) * a);
        }
        if (b != 0.0f) {
            result = (int) (((float) (31 * result)) * b);
        }
        if (c != 0.0f) {
            result = (int) (((float) (31 * result)) * c);
        }
        if (d != 0.0f) {
            return (int) (((float) (31 * result)) * d);
        }
        return result;
    }

    public static float getAnimationScale(Context context) {
        if (VERSION.SDK_INT >= 17) {
            return Global.getFloat(context.getContentResolver(), "animator_duration_scale", 1.0f);
        }
        return System.getFloat(context.getContentResolver(), "animator_duration_scale", 1.0f);
    }
}
