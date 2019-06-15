package com.oneplus.settings.opfinger;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.Log;
import com.caverock.androidsvg.PreserveAspectRatio;
import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;
import java.util.ArrayList;
import java.util.List;

public class SvgHelper {
    private static final String LOG_TAG = "SVG";
    private final List<SvgPath> mPaths = new ArrayList();
    private final Paint mSourcePaint;
    private SVG mSvg;

    public static class SvgPath {
        private static final Region sMaxClip = new Region(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        private static final Region sRegion = new Region();
        final Rect bounds;
        final float length;
        final PathMeasure measure;
        final Paint paint;
        final Path path;
        final Path renderPath = new Path();

        SvgPath(Path path, Paint paint) {
            this.path = path;
            this.paint = paint;
            this.measure = new PathMeasure(path, false);
            this.length = this.measure.getLength();
            sRegion.setPath(path, sMaxClip);
            this.bounds = sRegion.getBounds();
        }
    }

    public SvgHelper(Paint sourcePaint) {
        this.mSourcePaint = sourcePaint;
    }

    public void load(Context context, int svgResource) {
        if (this.mSvg == null) {
            try {
                this.mSvg = SVG.getFromResource(context, svgResource);
                this.mSvg.setDocumentPreserveAspectRatio(PreserveAspectRatio.UNSCALED);
            } catch (SVGParseException e) {
                Log.e(LOG_TAG, "Could not load specified SVG resource", e);
            }
        }
    }

    public List<SvgPath> getPathsForViewport(final int width, final int height) {
        this.mPaths.clear();
        Canvas canvas = new Canvas() {
            private final Matrix mMatrix = new Matrix();

            public int getWidth() {
                return width;
            }

            public int getHeight() {
                return height;
            }

            public void drawPath(Path path, Paint paint) {
                Path dst = new Path();
                getMatrix(this.mMatrix);
                path.transform(this.mMatrix, dst);
                SvgHelper.this.mPaths.add(new SvgPath(dst, new Paint(SvgHelper.this.mSourcePaint)));
            }
        };
        RectF viewBox = this.mSvg.getDocumentViewBox();
        float scale = Math.min(((float) width) / viewBox.width(), ((float) height) / viewBox.height());
        canvas.translate((((float) width) - (viewBox.width() * scale)) / 2.0f, (((float) height) - (viewBox.height() * scale)) / 2.0f);
        canvas.scale(scale, scale);
        this.mSvg.renderToCanvas(canvas);
        return this.mPaths;
    }
}
