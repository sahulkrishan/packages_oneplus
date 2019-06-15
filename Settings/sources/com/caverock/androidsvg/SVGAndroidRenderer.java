package com.caverock.androidsvg;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.FillType;
import android.graphics.PathMeasure;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.graphics.Typeface;
import android.os.Build.VERSION;
import android.support.v4.media.MediaPlayer2;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import com.android.settings.datausage.BillingCycleSettings;
import com.caverock.androidsvg.CSSParser.Rule;
import com.caverock.androidsvg.PreserveAspectRatio.Alignment;
import com.caverock.androidsvg.PreserveAspectRatio.Scale;
import com.caverock.androidsvg.SVG.Style.FillRule;
import com.caverock.androidsvg.SVG.Style.FontStyle;
import com.caverock.androidsvg.SVG.Style.LineCaps;
import com.caverock.androidsvg.SVG.Style.LineJoin;
import com.caverock.androidsvg.SVG.Style.TextAnchor;
import com.caverock.androidsvg.SVG.Style.TextDecoration;
import com.caverock.androidsvg.SVG.Style.TextDirection;
import com.caverock.androidsvg.SVG.Style.VectorEffect;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Stack;

public class SVGAndroidRenderer {
    private static /* synthetic */ int[] $SWITCH_TABLE$com$caverock$androidsvg$PreserveAspectRatio$Alignment = null;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$caverock$androidsvg$SVG$Style$FillRule = null;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$caverock$androidsvg$SVG$Style$LineCaps = null;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$caverock$androidsvg$SVG$Style$LineJoin = null;
    private static final float BEZIER_ARC_FACTOR = 0.5522848f;
    private static final String DEFAULT_FONT_FAMILY = "sans-serif";
    private static final int LUMINANCE_FACTOR_SHIFT = 15;
    private static final int LUMINANCE_TO_ALPHA_BLUE = 2362;
    private static final int LUMINANCE_TO_ALPHA_GREEN = 23442;
    private static final int LUMINANCE_TO_ALPHA_RED = 6963;
    private static final String TAG = "SVGAndroidRenderer";
    private Stack<Bitmap> bitmapStack;
    private Canvas canvas;
    private Stack<Canvas> canvasStack;
    private Box canvasViewPort;
    private boolean directRenderingMode;
    private SVG document;
    private float dpi;
    private Stack<Matrix> matrixStack;
    private Stack<SvgContainer> parentStack;
    private RendererState state;
    private Stack<RendererState> stateStack;

    private class MarkerVector {
        public float dx = 0.0f;
        public float dy = 0.0f;
        public float x;
        public float y;

        public MarkerVector(float x, float y, float dx, float dy) {
            this.x = x;
            this.y = y;
            double len = Math.sqrt((double) ((dx * dx) + (dy * dy)));
            if (len != 0.0d) {
                this.dx = (float) (((double) dx) / len);
                this.dy = (float) (((double) dy) / len);
            }
        }

        public void add(float x, float y) {
            float dx = x - this.x;
            float dy = y - this.y;
            double len = Math.sqrt((double) ((dx * dx) + (dy * dy)));
            if (len != 0.0d) {
                this.dx += (float) (((double) dx) / len);
                this.dy += (float) (((double) dy) / len);
            }
        }

        public void add(MarkerVector v2) {
            this.dx += v2.dx;
            this.dy += v2.dy;
        }

        public String toString() {
            StringBuilder stringBuilder = new StringBuilder("(");
            stringBuilder.append(this.x);
            stringBuilder.append(",");
            stringBuilder.append(this.y);
            stringBuilder.append(" ");
            stringBuilder.append(this.dx);
            stringBuilder.append(",");
            stringBuilder.append(this.dy);
            stringBuilder.append(")");
            return stringBuilder.toString();
        }
    }

    private class RendererState implements Cloneable {
        public boolean directRendering;
        public Paint fillPaint = new Paint();
        public boolean hasFill;
        public boolean hasStroke;
        public boolean spacePreserve;
        public Paint strokePaint;
        public Style style;
        public Box viewBox;
        public Box viewPort;

        public RendererState() {
            this.fillPaint.setFlags(385);
            this.fillPaint.setStyle(Style.FILL);
            this.fillPaint.setTypeface(Typeface.DEFAULT);
            this.strokePaint = new Paint();
            this.strokePaint.setFlags(385);
            this.strokePaint.setStyle(Style.STROKE);
            this.strokePaint.setTypeface(Typeface.DEFAULT);
            this.style = Style.getDefaultStyle();
        }

        /* Access modifiers changed, original: protected */
        public Object clone() {
            try {
                RendererState obj = (RendererState) super.clone();
                obj.style = (Style) this.style.clone();
                obj.fillPaint = new Paint(this.fillPaint);
                obj.strokePaint = new Paint(this.strokePaint);
                return obj;
            } catch (CloneNotSupportedException e) {
                throw new InternalError(e.toString());
            }
        }
    }

    private abstract class TextProcessor {
        public abstract void processText(String str);

        private TextProcessor() {
        }

        /* synthetic */ TextProcessor(SVGAndroidRenderer sVGAndroidRenderer, TextProcessor textProcessor) {
            this();
        }

        public boolean doTextContainer(TextContainer obj) {
            return true;
        }
    }

    private class MarkerPositionCalculator implements PathInterface {
        private boolean closepathReAdjustPending;
        private MarkerVector lastPos = null;
        private List<MarkerVector> markers = new ArrayList();
        private boolean normalCubic = true;
        private boolean startArc = false;
        private float startX;
        private float startY;
        private int subpathStartIndex = -1;

        public MarkerPositionCalculator(PathDefinition pathDef) {
            pathDef.enumeratePath(this);
            if (this.closepathReAdjustPending) {
                this.lastPos.add((MarkerVector) this.markers.get(this.subpathStartIndex));
                this.markers.set(this.subpathStartIndex, this.lastPos);
                this.closepathReAdjustPending = false;
            }
            if (this.lastPos != null) {
                this.markers.add(this.lastPos);
            }
        }

        public List<MarkerVector> getMarkers() {
            return this.markers;
        }

        public void moveTo(float x, float y) {
            if (this.closepathReAdjustPending) {
                this.lastPos.add((MarkerVector) this.markers.get(this.subpathStartIndex));
                this.markers.set(this.subpathStartIndex, this.lastPos);
                this.closepathReAdjustPending = false;
            }
            if (this.lastPos != null) {
                this.markers.add(this.lastPos);
            }
            this.startX = x;
            this.startY = y;
            this.lastPos = new MarkerVector(x, y, 0.0f, 0.0f);
            this.subpathStartIndex = this.markers.size();
        }

        public void lineTo(float x, float y) {
            this.lastPos.add(x, y);
            this.markers.add(this.lastPos);
            this.lastPos = new MarkerVector(x, y, x - this.lastPos.x, y - this.lastPos.y);
            this.closepathReAdjustPending = false;
        }

        public void cubicTo(float x1, float y1, float x2, float y2, float x3, float y3) {
            if (this.normalCubic || this.startArc) {
                this.lastPos.add(x1, y1);
                this.markers.add(this.lastPos);
                this.startArc = false;
            }
            this.lastPos = new MarkerVector(x3, y3, x3 - x2, y3 - y2);
            this.closepathReAdjustPending = false;
        }

        public void quadTo(float x1, float y1, float x2, float y2) {
            this.lastPos.add(x1, y1);
            this.markers.add(this.lastPos);
            this.lastPos = new MarkerVector(x2, y2, x2 - x1, y2 - y1);
            this.closepathReAdjustPending = false;
        }

        public void arcTo(float rx, float ry, float xAxisRotation, boolean largeArcFlag, boolean sweepFlag, float x, float y) {
            this.startArc = true;
            this.normalCubic = false;
            SVGAndroidRenderer.arcTo(this.lastPos.x, this.lastPos.y, rx, ry, xAxisRotation, largeArcFlag, sweepFlag, x, y, this);
            this.normalCubic = true;
            this.closepathReAdjustPending = false;
        }

        public void close() {
            this.markers.add(this.lastPos);
            lineTo(this.startX, this.startY);
            this.closepathReAdjustPending = true;
        }
    }

    private class PathConverter implements PathInterface {
        float lastX;
        float lastY;
        Path path = new Path();

        public PathConverter(PathDefinition pathDef) {
            pathDef.enumeratePath(this);
        }

        public Path getPath() {
            return this.path;
        }

        public void moveTo(float x, float y) {
            this.path.moveTo(x, y);
            this.lastX = x;
            this.lastY = y;
        }

        public void lineTo(float x, float y) {
            this.path.lineTo(x, y);
            this.lastX = x;
            this.lastY = y;
        }

        public void cubicTo(float x1, float y1, float x2, float y2, float x3, float y3) {
            this.path.cubicTo(x1, y1, x2, y2, x3, y3);
            this.lastX = x3;
            this.lastY = y3;
        }

        public void quadTo(float x1, float y1, float x2, float y2) {
            this.path.quadTo(x1, y1, x2, y2);
            this.lastX = x2;
            this.lastY = y2;
        }

        public void arcTo(float rx, float ry, float xAxisRotation, boolean largeArcFlag, boolean sweepFlag, float x, float y) {
            SVGAndroidRenderer.arcTo(this.lastX, this.lastY, rx, ry, xAxisRotation, largeArcFlag, sweepFlag, x, y, this);
            this.lastX = x;
            this.lastY = y;
        }

        public void close() {
            this.path.close();
        }
    }

    private class PlainTextDrawer extends TextProcessor {
        public float x;
        public float y;

        public PlainTextDrawer(float x, float y) {
            super(SVGAndroidRenderer.this, null);
            this.x = x;
            this.y = y;
        }

        public void processText(String text) {
            SVGAndroidRenderer.debug("TextSequence render", new Object[0]);
            if (SVGAndroidRenderer.this.visible()) {
                if (SVGAndroidRenderer.this.state.hasFill) {
                    SVGAndroidRenderer.this.canvas.drawText(text, this.x, this.y, SVGAndroidRenderer.this.state.fillPaint);
                }
                if (SVGAndroidRenderer.this.state.hasStroke) {
                    SVGAndroidRenderer.this.canvas.drawText(text, this.x, this.y, SVGAndroidRenderer.this.state.strokePaint);
                }
            }
            this.x += SVGAndroidRenderer.this.state.fillPaint.measureText(text);
        }
    }

    private class PlainTextToPath extends TextProcessor {
        public Path textAsPath;
        public float x;
        public float y;

        public PlainTextToPath(float x, float y, Path textAsPath) {
            super(SVGAndroidRenderer.this, null);
            this.x = x;
            this.y = y;
            this.textAsPath = textAsPath;
        }

        public boolean doTextContainer(TextContainer obj) {
            if (!(obj instanceof TextPath)) {
                return true;
            }
            SVGAndroidRenderer.warn("Using <textPath> elements in a clip path is not supported.", new Object[0]);
            return false;
        }

        public void processText(String text) {
            if (SVGAndroidRenderer.this.visible()) {
                Path spanPath = new Path();
                SVGAndroidRenderer.this.state.fillPaint.getTextPath(text, 0, text.length(), this.x, this.y, spanPath);
                this.textAsPath.addPath(spanPath);
            }
            this.x += SVGAndroidRenderer.this.state.fillPaint.measureText(text);
        }
    }

    private class TextBoundsCalculator extends TextProcessor {
        RectF bbox = new RectF();
        float x;
        float y;

        public TextBoundsCalculator(float x, float y) {
            super(SVGAndroidRenderer.this, null);
            this.x = x;
            this.y = y;
        }

        public boolean doTextContainer(TextContainer obj) {
            if (!(obj instanceof TextPath)) {
                return true;
            }
            SvgObject ref = obj.document.resolveIRI(((TextPath) obj).href);
            if (ref == null) {
                SVGAndroidRenderer.error("TextPath path reference '%s' not found", tpath.href);
                return false;
            }
            Path pathObj = (Path) ref;
            Path path = new PathConverter(pathObj.d).getPath();
            if (pathObj.transform != null) {
                path.transform(pathObj.transform);
            }
            RectF pathBounds = new RectF();
            path.computeBounds(pathBounds, true);
            this.bbox.union(pathBounds);
            return false;
        }

        public void processText(String text) {
            if (SVGAndroidRenderer.this.visible()) {
                Rect rect = new Rect();
                SVGAndroidRenderer.this.state.fillPaint.getTextBounds(text, 0, text.length(), rect);
                RectF textbounds = new RectF(rect);
                textbounds.offset(this.x, this.y);
                this.bbox.union(textbounds);
            }
            this.x += SVGAndroidRenderer.this.state.fillPaint.measureText(text);
        }
    }

    private class TextWidthCalculator extends TextProcessor {
        public float x;

        private TextWidthCalculator() {
            super(SVGAndroidRenderer.this, null);
            this.x = 0.0f;
        }

        /* synthetic */ TextWidthCalculator(SVGAndroidRenderer sVGAndroidRenderer, TextWidthCalculator textWidthCalculator) {
            this();
        }

        public void processText(String text) {
            this.x += SVGAndroidRenderer.this.state.fillPaint.measureText(text);
        }
    }

    private class PathTextDrawer extends PlainTextDrawer {
        private Path path;

        public PathTextDrawer(Path path, float x, float y) {
            super(x, y);
            this.path = path;
        }

        public void processText(String text) {
            if (SVGAndroidRenderer.this.visible()) {
                if (SVGAndroidRenderer.this.state.hasFill) {
                    SVGAndroidRenderer.this.canvas.drawTextOnPath(text, this.path, this.x, this.y, SVGAndroidRenderer.this.state.fillPaint);
                }
                if (SVGAndroidRenderer.this.state.hasStroke) {
                    SVGAndroidRenderer.this.canvas.drawTextOnPath(text, this.path, this.x, this.y, SVGAndroidRenderer.this.state.strokePaint);
                }
            }
            this.x += SVGAndroidRenderer.this.state.fillPaint.measureText(text);
        }
    }

    static /* synthetic */ int[] $SWITCH_TABLE$com$caverock$androidsvg$PreserveAspectRatio$Alignment() {
        int[] iArr = $SWITCH_TABLE$com$caverock$androidsvg$PreserveAspectRatio$Alignment;
        if (iArr != null) {
            return iArr;
        }
        iArr = new int[Alignment.values().length];
        try {
            iArr[Alignment.None.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Alignment.XMaxYMax.ordinal()] = 10;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Alignment.XMaxYMid.ordinal()] = 7;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Alignment.XMaxYMin.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[Alignment.XMidYMax.ordinal()] = 9;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[Alignment.XMidYMid.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[Alignment.XMidYMin.ordinal()] = 3;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[Alignment.XMinYMax.ordinal()] = 8;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[Alignment.XMinYMid.ordinal()] = 5;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[Alignment.XMinYMin.ordinal()] = 2;
        } catch (NoSuchFieldError e10) {
        }
        $SWITCH_TABLE$com$caverock$androidsvg$PreserveAspectRatio$Alignment = iArr;
        return iArr;
    }

    static /* synthetic */ int[] $SWITCH_TABLE$com$caverock$androidsvg$SVG$Style$FillRule() {
        int[] iArr = $SWITCH_TABLE$com$caverock$androidsvg$SVG$Style$FillRule;
        if (iArr != null) {
            return iArr;
        }
        iArr = new int[FillRule.values().length];
        try {
            iArr[FillRule.EvenOdd.ordinal()] = 2;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[FillRule.NonZero.ordinal()] = 1;
        } catch (NoSuchFieldError e2) {
        }
        $SWITCH_TABLE$com$caverock$androidsvg$SVG$Style$FillRule = iArr;
        return iArr;
    }

    static /* synthetic */ int[] $SWITCH_TABLE$com$caverock$androidsvg$SVG$Style$LineCaps() {
        int[] iArr = $SWITCH_TABLE$com$caverock$androidsvg$SVG$Style$LineCaps;
        if (iArr != null) {
            return iArr;
        }
        iArr = new int[LineCaps.values().length];
        try {
            iArr[LineCaps.Butt.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[LineCaps.Round.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[LineCaps.Square.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        $SWITCH_TABLE$com$caverock$androidsvg$SVG$Style$LineCaps = iArr;
        return iArr;
    }

    static /* synthetic */ int[] $SWITCH_TABLE$com$caverock$androidsvg$SVG$Style$LineJoin() {
        int[] iArr = $SWITCH_TABLE$com$caverock$androidsvg$SVG$Style$LineJoin;
        if (iArr != null) {
            return iArr;
        }
        iArr = new int[LineJoin.values().length];
        try {
            iArr[LineJoin.Bevel.ordinal()] = 3;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[LineJoin.Miter.ordinal()] = 1;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[LineJoin.Round.ordinal()] = 2;
        } catch (NoSuchFieldError e3) {
        }
        $SWITCH_TABLE$com$caverock$androidsvg$SVG$Style$LineJoin = iArr;
        return iArr;
    }

    private void resetState() {
        this.state = new RendererState();
        this.stateStack = new Stack();
        updateStyle(this.state, Style.getDefaultStyle());
        this.state.viewPort = this.canvasViewPort;
        this.state.spacePreserve = false;
        this.state.directRendering = this.directRenderingMode;
        this.stateStack.push((RendererState) this.state.clone());
        this.canvasStack = new Stack();
        this.bitmapStack = new Stack();
        this.matrixStack = new Stack();
        this.parentStack = new Stack();
    }

    protected SVGAndroidRenderer(Canvas canvas, Box viewPort, float defaultDPI) {
        this.canvas = canvas;
        this.dpi = defaultDPI;
        this.canvasViewPort = viewPort;
    }

    /* Access modifiers changed, original: protected */
    public float getDPI() {
        return this.dpi;
    }

    /* Access modifiers changed, original: protected */
    public float getCurrentFontSize() {
        return this.state.fillPaint.getTextSize();
    }

    /* Access modifiers changed, original: protected */
    public float getCurrentFontXHeight() {
        return this.state.fillPaint.getTextSize() / 2.0f;
    }

    /* Access modifiers changed, original: protected */
    public Box getCurrentViewPortInUserUnits() {
        if (this.state.viewBox != null) {
            return this.state.viewBox;
        }
        return this.state.viewPort;
    }

    /* Access modifiers changed, original: protected */
    public void renderDocument(SVG document, Box viewBox, PreserveAspectRatio positioning, boolean directRenderingMode) {
        this.document = document;
        this.directRenderingMode = directRenderingMode;
        Svg rootObj = document.getRootElement();
        if (rootObj == null) {
            warn("Nothing to render. Document is empty.", new Object[0]);
            return;
        }
        Box box;
        PreserveAspectRatio preserveAspectRatio;
        resetState();
        checkXMLSpaceAttribute(rootObj);
        Length length = rootObj.width;
        Length length2 = rootObj.height;
        if (viewBox != null) {
            box = viewBox;
        } else {
            box = rootObj.viewBox;
        }
        if (positioning != null) {
            preserveAspectRatio = positioning;
        } else {
            preserveAspectRatio = rootObj.preserveAspectRatio;
        }
        render(rootObj, length, length2, box, preserveAspectRatio);
    }

    private void render(SvgObject obj) {
        if (!(obj instanceof NotDirectlyRendered)) {
            statePush();
            checkXMLSpaceAttribute(obj);
            if (obj instanceof Svg) {
                render((Svg) obj);
            } else if (obj instanceof Use) {
                render((Use) obj);
            } else if (obj instanceof Switch) {
                render((Switch) obj);
            } else if (obj instanceof Group) {
                render((Group) obj);
            } else if (obj instanceof Image) {
                render((Image) obj);
            } else if (obj instanceof Path) {
                render((Path) obj);
            } else if (obj instanceof Rect) {
                render((Rect) obj);
            } else if (obj instanceof Circle) {
                render((Circle) obj);
            } else if (obj instanceof Ellipse) {
                render((Ellipse) obj);
            } else if (obj instanceof Line) {
                render((Line) obj);
            } else if (obj instanceof Polygon) {
                render((Polygon) obj);
            } else if (obj instanceof PolyLine) {
                render((PolyLine) obj);
            } else if (obj instanceof Text) {
                render((Text) obj);
            }
            statePop();
        }
    }

    private void renderChildren(SvgContainer obj, boolean isContainer) {
        if (isContainer) {
            parentPush(obj);
        }
        for (SvgObject child : obj.getChildren()) {
            render(child);
        }
        if (isContainer) {
            parentPop();
        }
    }

    private void statePush() {
        this.canvas.save();
        this.stateStack.push(this.state);
        this.state = (RendererState) this.state.clone();
    }

    private void statePop() {
        this.canvas.restore();
        this.state = (RendererState) this.stateStack.pop();
    }

    private void parentPush(SvgContainer obj) {
        this.parentStack.push(obj);
        this.matrixStack.push(this.canvas.getMatrix());
    }

    private void parentPop() {
        this.parentStack.pop();
        this.matrixStack.pop();
    }

    private void updateStyleForElement(RendererState state, SvgElementBase obj) {
        state.style.resetNonInheritingProperties(obj.parent == null);
        if (obj.baseStyle != null) {
            updateStyle(state, obj.baseStyle);
        }
        if (this.document.hasCSSRules()) {
            for (Rule rule : this.document.getCSSRules()) {
                if (CSSParser.ruleMatch(rule.selector, obj)) {
                    updateStyle(state, rule.style);
                }
            }
        }
        if (obj.style != null) {
            updateStyle(state, obj.style);
        }
    }

    private void checkXMLSpaceAttribute(SvgObject obj) {
        if (obj instanceof SvgElementBase) {
            SvgElementBase bobj = (SvgElementBase) obj;
            if (bobj.spacePreserve != null) {
                this.state.spacePreserve = bobj.spacePreserve.booleanValue();
            }
        }
    }

    private void doFilledPath(SvgElement obj, Path path) {
        if (this.state.style.fill instanceof PaintReference) {
            SvgObject ref = this.document.resolveIRI(((PaintReference) this.state.style.fill).href);
            if (ref instanceof Pattern) {
                fillWithPattern(obj, path, (Pattern) ref);
                return;
            }
        }
        this.canvas.drawPath(path, this.state.fillPaint);
    }

    private void doStroke(Path path) {
        if (this.state.style.vectorEffect == VectorEffect.NonScalingStroke) {
            Matrix currentMatrix = this.canvas.getMatrix();
            Path transformedPath = new Path();
            path.transform(currentMatrix, transformedPath);
            this.canvas.setMatrix(new Matrix());
            Shader shader = this.state.strokePaint.getShader();
            Matrix currentShaderMatrix = new Matrix();
            if (shader != null) {
                shader.getLocalMatrix(currentShaderMatrix);
                Matrix newShaderMatrix = new Matrix(currentShaderMatrix);
                newShaderMatrix.postConcat(currentMatrix);
                shader.setLocalMatrix(newShaderMatrix);
            }
            this.canvas.drawPath(transformedPath, this.state.strokePaint);
            this.canvas.setMatrix(currentMatrix);
            if (shader != null) {
                shader.setLocalMatrix(currentShaderMatrix);
                return;
            }
            return;
        }
        this.canvas.drawPath(path, this.state.strokePaint);
    }

    private static void warn(String format, Object... args) {
        Log.w(TAG, String.format(format, args));
    }

    private static void error(String format, Object... args) {
        Log.e(TAG, String.format(format, args));
    }

    private static void debug(String format, Object... args) {
    }

    private static void info(String format, Object... args) {
        Log.i(TAG, String.format(format, args));
    }

    private void render(Svg obj) {
        render(obj, obj.width, obj.height);
    }

    private void render(Svg obj, Length width, Length height) {
        render(obj, width, height, obj.viewBox, obj.preserveAspectRatio);
    }

    private void render(Svg obj, Length width, Length height, Box viewBox, PreserveAspectRatio positioning) {
        debug("Svg render", new Object[0]);
        if ((width == null || !width.isZero()) && (height == null || !height.isZero())) {
            if (positioning == null) {
                positioning = obj.preserveAspectRatio != null ? obj.preserveAspectRatio : PreserveAspectRatio.LETTERBOX;
            }
            updateStyleForElement(this.state, obj);
            if (display()) {
                float _x = 0.0f;
                float _y = 0.0f;
                if (obj.parent != null) {
                    float f = 0.0f;
                    _x = obj.x != null ? obj.x.floatValueX(this) : 0.0f;
                    if (obj.y != null) {
                        f = obj.y.floatValueY(this);
                    }
                    _y = f;
                }
                Box viewPortUser = getCurrentViewPortInUserUnits();
                this.state.viewPort = new Box(_x, _y, width != null ? width.floatValueX(this) : viewPortUser.width, height != null ? height.floatValueY(this) : viewPortUser.height);
                if (!this.state.style.overflow.booleanValue()) {
                    setClipRect(this.state.viewPort.minX, this.state.viewPort.minY, this.state.viewPort.width, this.state.viewPort.height);
                }
                checkForClipPath(obj, this.state.viewPort);
                if (viewBox != null) {
                    this.canvas.concat(calculateViewBoxTransform(this.state.viewPort, viewBox, positioning));
                    this.state.viewBox = obj.viewBox;
                }
                boolean compositing = pushLayer();
                viewportFill();
                renderChildren(obj, true);
                if (compositing) {
                    popLayer(obj);
                }
                updateParentBoundingBox(obj);
            }
        }
    }

    private void render(Group obj) {
        debug("Group render", new Object[0]);
        updateStyleForElement(this.state, obj);
        if (display()) {
            if (obj.transform != null) {
                this.canvas.concat(obj.transform);
            }
            checkForClipPath(obj);
            boolean compositing = pushLayer();
            renderChildren(obj, true);
            if (compositing) {
                popLayer(obj);
            }
            updateParentBoundingBox(obj);
        }
    }

    private void updateParentBoundingBox(SvgElement obj) {
        if (obj.parent != null && obj.boundingBox != null) {
            Matrix m = new Matrix();
            if (((Matrix) this.matrixStack.peek()).invert(m)) {
                float[] pts = new float[]{obj.boundingBox.minX, obj.boundingBox.minY, obj.boundingBox.maxX(), obj.boundingBox.minY, obj.boundingBox.maxX(), obj.boundingBox.maxY(), obj.boundingBox.minX, obj.boundingBox.maxY()};
                m.preConcat(this.canvas.getMatrix());
                m.mapPoints(pts);
                RectF rect = new RectF(pts[0], pts[1], pts[0], pts[1]);
                for (int i = 2; i <= 6; i += 2) {
                    if (pts[i] < rect.left) {
                        rect.left = pts[i];
                    }
                    if (pts[i] > rect.right) {
                        rect.right = pts[i];
                    }
                    if (pts[i + 1] < rect.top) {
                        rect.top = pts[i + 1];
                    }
                    if (pts[i + 1] > rect.bottom) {
                        rect.bottom = pts[i + 1];
                    }
                }
                SvgElement i2 = (SvgElement) this.parentStack.peek();
                if (i2.boundingBox == null) {
                    i2.boundingBox = Box.fromLimits(rect.left, rect.top, rect.right, rect.bottom);
                } else {
                    i2.boundingBox.union(Box.fromLimits(rect.left, rect.top, rect.right, rect.bottom));
                }
            }
        }
    }

    private boolean pushLayer() {
        if (!requiresCompositing()) {
            return false;
        }
        this.canvas.saveLayerAlpha(null, clamp255(this.state.style.opacity.floatValue()), 4);
        this.stateStack.push(this.state);
        this.state = (RendererState) this.state.clone();
        if (this.state.style.mask != null && this.state.directRendering) {
            SvgObject ref = this.document.resolveIRI(this.state.style.mask);
            if (ref == null || !(ref instanceof Mask)) {
                error("Mask reference '%s' not found", this.state.style.mask);
                this.state.style.mask = null;
                return true;
            }
            this.canvasStack.push(this.canvas);
            duplicateCanvas();
        }
        return true;
    }

    private void popLayer(SvgElement obj) {
        if (this.state.style.mask != null && this.state.directRendering) {
            SvgObject ref = this.document.resolveIRI(this.state.style.mask);
            duplicateCanvas();
            renderMask((Mask) ref, obj);
            Bitmap maskedContent = processMaskBitmaps();
            this.canvas = (Canvas) this.canvasStack.pop();
            this.canvas.save();
            this.canvas.setMatrix(new Matrix());
            this.canvas.drawBitmap(maskedContent, 0.0f, 0.0f, this.state.fillPaint);
            maskedContent.recycle();
            this.canvas.restore();
        }
        statePop();
    }

    private boolean requiresCompositing() {
        if (!(this.state.style.mask == null || this.state.directRendering)) {
            warn("Masks are not supported when using getPicture()", new Object[0]);
        }
        if (this.state.style.opacity.floatValue() < 1.0f || (this.state.style.mask != null && this.state.directRendering)) {
            return true;
        }
        return false;
    }

    private void duplicateCanvas() {
        try {
            Bitmap newBM = Bitmap.createBitmap(this.canvas.getWidth(), this.canvas.getHeight(), Config.ARGB_8888);
            this.bitmapStack.push(newBM);
            Canvas newCanvas = new Canvas(newBM);
            newCanvas.setMatrix(this.canvas.getMatrix());
            this.canvas = newCanvas;
        } catch (OutOfMemoryError e) {
            error("Not enough memory to create temporary bitmaps for mask processing", new Object[0]);
            throw e;
        }
    }

    private Bitmap processMaskBitmaps() {
        Bitmap mask = (Bitmap) this.bitmapStack.pop();
        Bitmap maskedContent = (Bitmap) this.bitmapStack.pop();
        int w = mask.getWidth();
        int h = mask.getHeight();
        int[] maskBuf = new int[w];
        int[] maskedContentBuf = new int[w];
        int y = 0;
        while (y < h) {
            mask.getPixels(maskBuf, 0, w, 0, y, w, 1);
            int y2 = y;
            maskedContent.getPixels(maskedContentBuf, 0, w, 0, y, w, 1);
            for (int x = 0; x < w; x++) {
                int px = maskBuf[x];
                int b = px & 255;
                int g = (px >> 8) & 255;
                int r = (px >> 16) & 255;
                int a = (px >> 24) & 255;
                if (a == 0) {
                    maskedContentBuf[x] = 0;
                } else {
                    int maskAlpha = ((((r * LUMINANCE_TO_ALPHA_RED) + (g * LUMINANCE_TO_ALPHA_GREEN)) + (b * LUMINANCE_TO_ALPHA_BLUE)) * a) / 8355840;
                    int content = maskedContentBuf[x];
                    maskedContentBuf[x] = (ViewCompat.MEASURED_SIZE_MASK & content) | (((((content >> 24) & 255) * maskAlpha) / 255) << 24);
                }
            }
            maskedContent.setPixels(maskedContentBuf, 0, w, 0, y2, w, 1);
            y = y2 + 1;
        }
        mask.recycle();
        return maskedContent;
    }

    private void render(Switch obj) {
        debug("Switch render", new Object[0]);
        updateStyleForElement(this.state, obj);
        if (display()) {
            if (obj.transform != null) {
                this.canvas.concat(obj.transform);
            }
            checkForClipPath(obj);
            boolean compositing = pushLayer();
            renderSwitchChild(obj);
            if (compositing) {
                popLayer(obj);
            }
            updateParentBoundingBox(obj);
        }
    }

    private void renderSwitchChild(Switch obj) {
        String deviceLanguage = Locale.getDefault().getLanguage();
        SVGExternalFileResolver fileResolver = this.document.getFileResolver();
        for (SvgObject child : obj.getChildren()) {
            if (child instanceof SvgConditional) {
                SvgConditional condObj = (SvgConditional) child;
                if (condObj.getRequiredExtensions() == null) {
                    Set<String> syslang = condObj.getSystemLanguage();
                    if (syslang != null) {
                        if (syslang.isEmpty()) {
                            continue;
                        } else if (!syslang.contains(deviceLanguage)) {
                        }
                    }
                    Set<String> reqfeat = condObj.getRequiredFeatures();
                    if (reqfeat != null) {
                        if (reqfeat.isEmpty()) {
                            continue;
                        } else if (!SVGParser.supportedFeatures.containsAll(reqfeat)) {
                        }
                    }
                    Set<String> reqfmts = condObj.getRequiredFormats();
                    if (reqfmts != null) {
                        if (!reqfmts.isEmpty()) {
                            if (fileResolver != null) {
                                for (String mimeType : reqfmts) {
                                    if (!fileResolver.isFormatSupported(mimeType)) {
                                        break;
                                    }
                                }
                            }
                        } else {
                            continue;
                        }
                    }
                    Set<String> reqfonts = condObj.getRequiredFonts();
                    if (reqfonts != null) {
                        if (reqfonts.isEmpty()) {
                            continue;
                        } else if (fileResolver != null) {
                            for (String fontName : reqfonts) {
                                if (fileResolver.resolveFont(fontName, this.state.style.fontWeight.intValue(), String.valueOf(this.state.style.fontStyle)) == null) {
                                }
                            }
                        }
                    }
                    render(child);
                    return;
                }
            }
        }
    }

    private void render(Use obj) {
        debug("Use render", new Object[0]);
        if ((obj.width == null || !obj.width.isZero()) && (obj.height == null || !obj.height.isZero())) {
            updateStyleForElement(this.state, obj);
            if (display()) {
                SvgObject ref = obj.document.resolveIRI(obj.href);
                if (ref == null) {
                    error("Use reference '%s' not found", obj.href);
                    return;
                }
                if (obj.transform != null) {
                    this.canvas.concat(obj.transform);
                }
                Matrix m = new Matrix();
                float _y = 0.0f;
                float _x = obj.x != null ? obj.x.floatValueX(this) : 0.0f;
                if (obj.y != null) {
                    _y = obj.y.floatValueY(this);
                }
                m.preTranslate(_x, _y);
                this.canvas.concat(m);
                checkForClipPath(obj);
                boolean compositing = pushLayer();
                parentPush(obj);
                if (ref instanceof Svg) {
                    statePush();
                    Svg svgElem = (Svg) ref;
                    render(svgElem, obj.width != null ? obj.width : svgElem.width, obj.height != null ? obj.height : svgElem.height);
                    statePop();
                } else if (ref instanceof Symbol) {
                    Length _w = obj.width != null ? obj.width : new Length(100.0f, Unit.percent);
                    Length _h = obj.height != null ? obj.height : new Length(100.0f, Unit.percent);
                    statePush();
                    render((Symbol) ref, _w, _h);
                    statePop();
                } else {
                    render(ref);
                }
                parentPop();
                if (compositing) {
                    popLayer(obj);
                }
                updateParentBoundingBox(obj);
            }
        }
    }

    private void render(Path obj) {
        debug("Path render", new Object[0]);
        updateStyleForElement(this.state, obj);
        if (!display() || !visible()) {
            return;
        }
        if (this.state.hasStroke || this.state.hasFill) {
            if (obj.transform != null) {
                this.canvas.concat(obj.transform);
            }
            Path path = new PathConverter(obj.d).getPath();
            if (obj.boundingBox == null) {
                obj.boundingBox = calculatePathBounds(path);
            }
            updateParentBoundingBox(obj);
            checkForGradiantsAndPatterns(obj);
            checkForClipPath(obj);
            boolean compositing = pushLayer();
            if (this.state.hasFill) {
                path.setFillType(getFillTypeFromState());
                doFilledPath(obj, path);
            }
            if (this.state.hasStroke) {
                doStroke(path);
            }
            renderMarkers(obj);
            if (compositing) {
                popLayer(obj);
            }
        }
    }

    private Box calculatePathBounds(Path path) {
        RectF pathBounds = new RectF();
        path.computeBounds(pathBounds, true);
        return new Box(pathBounds.left, pathBounds.top, pathBounds.width(), pathBounds.height());
    }

    private void render(Rect obj) {
        debug("Rect render", new Object[0]);
        if (obj.width != null && obj.height != null && !obj.width.isZero() && !obj.height.isZero()) {
            updateStyleForElement(this.state, obj);
            if (display() && visible()) {
                if (obj.transform != null) {
                    this.canvas.concat(obj.transform);
                }
                Path path = makePathAndBoundingBox(obj);
                updateParentBoundingBox(obj);
                checkForGradiantsAndPatterns(obj);
                checkForClipPath(obj);
                boolean compositing = pushLayer();
                if (this.state.hasFill) {
                    doFilledPath(obj, path);
                }
                if (this.state.hasStroke) {
                    doStroke(path);
                }
                if (compositing) {
                    popLayer(obj);
                }
            }
        }
    }

    private void render(Circle obj) {
        debug("Circle render", new Object[0]);
        if (obj.r != null && !obj.r.isZero()) {
            updateStyleForElement(this.state, obj);
            if (display() && visible()) {
                if (obj.transform != null) {
                    this.canvas.concat(obj.transform);
                }
                Path path = makePathAndBoundingBox(obj);
                updateParentBoundingBox(obj);
                checkForGradiantsAndPatterns(obj);
                checkForClipPath(obj);
                boolean compositing = pushLayer();
                if (this.state.hasFill) {
                    doFilledPath(obj, path);
                }
                if (this.state.hasStroke) {
                    doStroke(path);
                }
                if (compositing) {
                    popLayer(obj);
                }
            }
        }
    }

    private void render(Ellipse obj) {
        debug("Ellipse render", new Object[0]);
        if (obj.rx != null && obj.ry != null && !obj.rx.isZero() && !obj.ry.isZero()) {
            updateStyleForElement(this.state, obj);
            if (display() && visible()) {
                if (obj.transform != null) {
                    this.canvas.concat(obj.transform);
                }
                Path path = makePathAndBoundingBox(obj);
                updateParentBoundingBox(obj);
                checkForGradiantsAndPatterns(obj);
                checkForClipPath(obj);
                boolean compositing = pushLayer();
                if (this.state.hasFill) {
                    doFilledPath(obj, path);
                }
                if (this.state.hasStroke) {
                    doStroke(path);
                }
                if (compositing) {
                    popLayer(obj);
                }
            }
        }
    }

    private void render(Line obj) {
        debug("Line render", new Object[0]);
        updateStyleForElement(this.state, obj);
        if (display() && visible() && this.state.hasStroke) {
            if (obj.transform != null) {
                this.canvas.concat(obj.transform);
            }
            Path path = makePathAndBoundingBox(obj);
            updateParentBoundingBox(obj);
            checkForGradiantsAndPatterns(obj);
            checkForClipPath(obj);
            boolean compositing = pushLayer();
            doStroke(path);
            renderMarkers(obj);
            if (compositing) {
                popLayer(obj);
            }
        }
    }

    private List<MarkerVector> calculateMarkerPositions(Line obj) {
        float f = 0.0f;
        float _x1 = obj.x1 != null ? obj.x1.floatValueX(this) : 0.0f;
        float _y1 = obj.y1 != null ? obj.y1.floatValueY(this) : 0.0f;
        float _x2 = obj.x2 != null ? obj.x2.floatValueX(this) : 0.0f;
        if (obj.y2 != null) {
            f = obj.y2.floatValueY(this);
        }
        float _y2 = f;
        ArrayList markers = new ArrayList(2);
        markers.add(new MarkerVector(_x1, _y1, _x2 - _x1, _y2 - _y1));
        markers.add(new MarkerVector(_x2, _y2, _x2 - _x1, _y2 - _y1));
        return markers;
    }

    private void render(PolyLine obj) {
        debug("PolyLine render", new Object[0]);
        updateStyleForElement(this.state, obj);
        if (!display() || !visible()) {
            return;
        }
        if (this.state.hasStroke || this.state.hasFill) {
            if (obj.transform != null) {
                this.canvas.concat(obj.transform);
            }
            if (obj.points.length >= 2) {
                Path path = makePathAndBoundingBox(obj);
                updateParentBoundingBox(obj);
                checkForGradiantsAndPatterns(obj);
                checkForClipPath(obj);
                boolean compositing = pushLayer();
                if (this.state.hasFill) {
                    doFilledPath(obj, path);
                }
                if (this.state.hasStroke) {
                    doStroke(path);
                }
                renderMarkers(obj);
                if (compositing) {
                    popLayer(obj);
                }
            }
        }
    }

    private List<MarkerVector> calculateMarkerPositions(PolyLine obj) {
        PolyLine polyLine = obj;
        int numPoints = polyLine.points.length;
        if (numPoints < 2) {
            return null;
        }
        List<MarkerVector> markers = new ArrayList();
        MarkerVector lastPos = new MarkerVector(polyLine.points[0], polyLine.points[1], 0.0f, 0.0f);
        float x = 0.0f;
        float y = 0.0f;
        for (int i = 2; i < numPoints; i += 2) {
            x = polyLine.points[i];
            y = polyLine.points[i + 1];
            lastPos.add(x, y);
            markers.add(lastPos);
            lastPos = new MarkerVector(x, y, x - lastPos.x, y - lastPos.y);
        }
        if (!(polyLine instanceof Polygon)) {
            markers.add(lastPos);
        } else if (!(x == polyLine.points[0] || y == polyLine.points[1])) {
            x = polyLine.points[0];
            y = polyLine.points[1];
            lastPos.add(x, y);
            markers.add(lastPos);
            MarkerVector markerVector = new MarkerVector(x, y, x - lastPos.x, y - lastPos.y);
            markerVector.add((MarkerVector) markers.get(0));
            markers.add(markerVector);
            markers.set(0, markerVector);
        }
        return markers;
    }

    private void render(Polygon obj) {
        debug("Polygon render", new Object[0]);
        updateStyleForElement(this.state, obj);
        if (!display() || !visible()) {
            return;
        }
        if (this.state.hasStroke || this.state.hasFill) {
            if (obj.transform != null) {
                this.canvas.concat(obj.transform);
            }
            if (obj.points.length >= 2) {
                Path path = makePathAndBoundingBox((PolyLine) obj);
                updateParentBoundingBox(obj);
                checkForGradiantsAndPatterns(obj);
                checkForClipPath(obj);
                boolean compositing = pushLayer();
                if (this.state.hasFill) {
                    doFilledPath(obj, path);
                }
                if (this.state.hasStroke) {
                    doStroke(path);
                }
                renderMarkers(obj);
                if (compositing) {
                    popLayer(obj);
                }
            }
        }
    }

    private void render(Text obj) {
        debug("Text render", new Object[0]);
        updateStyleForElement(this.state, obj);
        if (display()) {
            if (obj.transform != null) {
                this.canvas.concat(obj.transform);
            }
            float f = 0.0f;
            float x = (obj.x == null || obj.x.size() == 0) ? 0.0f : ((Length) obj.x.get(0)).floatValueX(this);
            float y = (obj.y == null || obj.y.size() == 0) ? 0.0f : ((Length) obj.y.get(0)).floatValueY(this);
            float dx = (obj.dx == null || obj.dx.size() == 0) ? 0.0f : ((Length) obj.dx.get(0)).floatValueX(this);
            if (!(obj.dy == null || obj.dy.size() == 0)) {
                f = ((Length) obj.dy.get(0)).floatValueY(this);
            }
            float dy = f;
            TextAnchor anchor = getAnchorPosition();
            if (anchor != TextAnchor.Start) {
                float textWidth = calculateTextWidth(obj);
                if (anchor == TextAnchor.Middle) {
                    x -= textWidth / 2.0f;
                } else {
                    x -= textWidth;
                }
            }
            if (obj.boundingBox == null) {
                TextBoundsCalculator proc = new TextBoundsCalculator(x, y);
                enumerateTextSpans(obj, proc);
                obj.boundingBox = new Box(proc.bbox.left, proc.bbox.top, proc.bbox.width(), proc.bbox.height());
            }
            updateParentBoundingBox(obj);
            checkForGradiantsAndPatterns(obj);
            checkForClipPath(obj);
            boolean compositing = pushLayer();
            enumerateTextSpans(obj, new PlainTextDrawer(x + dx, y + dy));
            if (compositing) {
                popLayer(obj);
            }
        }
    }

    private TextAnchor getAnchorPosition() {
        if (this.state.style.direction == TextDirection.LTR || this.state.style.textAnchor == TextAnchor.Middle) {
            return this.state.style.textAnchor;
        }
        return this.state.style.textAnchor == TextAnchor.Start ? TextAnchor.End : TextAnchor.Start;
    }

    private void enumerateTextSpans(TextContainer obj, TextProcessor textprocessor) {
        if (display()) {
            Iterator<SvgObject> iter = obj.children.iterator();
            boolean isFirstChild = true;
            while (iter.hasNext()) {
                SvgObject child = (SvgObject) iter.next();
                if (child instanceof TextSequence) {
                    textprocessor.processText(textXMLSpaceTransform(((TextSequence) child).text, isFirstChild, iter.hasNext() ^ 1));
                } else {
                    processTextChild(child, textprocessor);
                }
                isFirstChild = false;
            }
        }
    }

    private void processTextChild(SvgObject obj, TextProcessor textprocessor) {
        if (textprocessor.doTextContainer((TextContainer) obj)) {
            if (obj instanceof TextPath) {
                statePush();
                renderTextPath((TextPath) obj);
                statePop();
            } else if (obj instanceof TSpan) {
                debug("TSpan render", new Object[0]);
                statePush();
                TSpan tspan = (TSpan) obj;
                updateStyleForElement(this.state, tspan);
                if (display()) {
                    float x = 0.0f;
                    float y = 0.0f;
                    float dx = 0.0f;
                    float dy = 0.0f;
                    if (textprocessor instanceof PlainTextDrawer) {
                        float floatValueX = (tspan.x == null || tspan.x.size() == 0) ? ((PlainTextDrawer) textprocessor).x : ((Length) tspan.x.get(0)).floatValueX(this);
                        x = floatValueX;
                        floatValueX = (tspan.y == null || tspan.y.size() == 0) ? ((PlainTextDrawer) textprocessor).y : ((Length) tspan.y.get(0)).floatValueY(this);
                        y = floatValueX;
                        float f = 0.0f;
                        floatValueX = (tspan.dx == null || tspan.dx.size() == 0) ? 0.0f : ((Length) tspan.dx.get(0)).floatValueX(this);
                        dx = floatValueX;
                        if (!(tspan.dy == null || tspan.dy.size() == 0)) {
                            f = ((Length) tspan.dy.get(0)).floatValueY(this);
                        }
                        dy = f;
                    }
                    checkForGradiantsAndPatterns((SvgElement) tspan.getTextRoot());
                    if (textprocessor instanceof PlainTextDrawer) {
                        ((PlainTextDrawer) textprocessor).x = x + dx;
                        ((PlainTextDrawer) textprocessor).y = y + dy;
                    }
                    boolean compositing = pushLayer();
                    enumerateTextSpans(tspan, textprocessor);
                    if (compositing) {
                        popLayer(tspan);
                    }
                }
                statePop();
            } else if (obj instanceof TRef) {
                statePush();
                TRef tref = (TRef) obj;
                updateStyleForElement(this.state, tref);
                if (display()) {
                    checkForGradiantsAndPatterns((SvgElement) tref.getTextRoot());
                    SvgObject ref = obj.document.resolveIRI(tref.href);
                    if (ref == null || !(ref instanceof TextContainer)) {
                        error("Tref reference '%s' not found", tref.href);
                    } else {
                        StringBuilder str = new StringBuilder();
                        extractRawText((TextContainer) ref, str);
                        if (str.length() > 0) {
                            textprocessor.processText(str.toString());
                        }
                    }
                }
                statePop();
            }
        }
    }

    private void renderTextPath(TextPath obj) {
        debug("TextPath render", new Object[0]);
        updateStyleForElement(this.state, obj);
        if (display() && visible()) {
            SvgObject ref = obj.document.resolveIRI(obj.href);
            if (ref == null) {
                error("TextPath reference '%s' not found", obj.href);
                return;
            }
            Path pathObj = (Path) ref;
            Path path = new PathConverter(pathObj.d).getPath();
            if (pathObj.transform != null) {
                path.transform(pathObj.transform);
            }
            float startOffset = obj.startOffset != null ? obj.startOffset.floatValue(this, new PathMeasure(path, false).getLength()) : 0.0f;
            TextAnchor anchor = getAnchorPosition();
            if (anchor != TextAnchor.Start) {
                float textWidth = calculateTextWidth(obj);
                if (anchor == TextAnchor.Middle) {
                    startOffset -= textWidth / 2.0f;
                } else {
                    startOffset -= textWidth;
                }
            }
            checkForGradiantsAndPatterns((SvgElement) obj.getTextRoot());
            boolean compositing = pushLayer();
            enumerateTextSpans(obj, new PathTextDrawer(path, startOffset, 0.0f));
            if (compositing) {
                popLayer(obj);
            }
        }
    }

    private float calculateTextWidth(TextContainer parentTextObj) {
        TextWidthCalculator proc = new TextWidthCalculator(this, null);
        enumerateTextSpans(parentTextObj, proc);
        return proc.x;
    }

    private void extractRawText(TextContainer parent, StringBuilder str) {
        Iterator<SvgObject> iter = parent.children.iterator();
        boolean isFirstChild = true;
        while (iter.hasNext()) {
            SvgObject child = (SvgObject) iter.next();
            if (child instanceof TextContainer) {
                extractRawText((TextContainer) child, str);
            } else if (child instanceof TextSequence) {
                str.append(textXMLSpaceTransform(((TextSequence) child).text, isFirstChild, iter.hasNext() ^ 1));
            }
            isFirstChild = false;
        }
    }

    private String textXMLSpaceTransform(String text, boolean isFirstChild, boolean isLastChild) {
        if (this.state.spacePreserve) {
            return text.replaceAll("[\\n\\t]", " ");
        }
        text = text.replaceAll("\\n", "").replaceAll("\\t", " ");
        if (isFirstChild) {
            text = text.replaceAll("^\\s+", "");
        }
        if (isLastChild) {
            text = text.replaceAll("\\s+$", "");
        }
        return text.replaceAll("\\s{2,}", " ");
    }

    private void render(Symbol obj, Length width, Length height) {
        debug("Symbol render", new Object[0]);
        if ((width == null || !width.isZero()) && (height == null || !height.isZero())) {
            PreserveAspectRatio positioning = obj.preserveAspectRatio != null ? obj.preserveAspectRatio : PreserveAspectRatio.LETTERBOX;
            updateStyleForElement(this.state, obj);
            this.state.viewPort = new Box(0.0f, 0.0f, width != null ? width.floatValueX(this) : this.state.viewPort.width, height != null ? height.floatValueX(this) : this.state.viewPort.height);
            if (!this.state.style.overflow.booleanValue()) {
                setClipRect(this.state.viewPort.minX, this.state.viewPort.minY, this.state.viewPort.width, this.state.viewPort.height);
            }
            if (obj.viewBox != null) {
                this.canvas.concat(calculateViewBoxTransform(this.state.viewPort, obj.viewBox, positioning));
                this.state.viewBox = obj.viewBox;
            }
            boolean compositing = pushLayer();
            renderChildren(obj, true);
            if (compositing) {
                popLayer(obj);
            }
            updateParentBoundingBox(obj);
        }
    }

    /* JADX WARNING: Missing block: B:48:0x0105, code skipped:
            return;
     */
    private void render(com.caverock.androidsvg.SVG.Image r12) {
        /*
        r11 = this;
        r0 = "Image render";
        r1 = 0;
        r2 = new java.lang.Object[r1];
        debug(r0, r2);
        r0 = r12.width;
        if (r0 == 0) goto L_0x0105;
    L_0x000c:
        r0 = r12.width;
        r0 = r0.isZero();
        if (r0 != 0) goto L_0x0105;
    L_0x0014:
        r0 = r12.height;
        if (r0 == 0) goto L_0x0105;
    L_0x0018:
        r0 = r12.height;
        r0 = r0.isZero();
        if (r0 == 0) goto L_0x0022;
    L_0x0020:
        goto L_0x0105;
    L_0x0022:
        r0 = r12.href;
        if (r0 != 0) goto L_0x0027;
    L_0x0026:
        return;
    L_0x0027:
        r0 = r12.preserveAspectRatio;
        if (r0 == 0) goto L_0x002e;
    L_0x002b:
        r0 = r12.preserveAspectRatio;
        goto L_0x0030;
    L_0x002e:
        r0 = com.caverock.androidsvg.PreserveAspectRatio.LETTERBOX;
    L_0x0030:
        r2 = r12.href;
        r2 = r11.checkForImageDataURL(r2);
        if (r2 != 0) goto L_0x0047;
    L_0x0038:
        r3 = r11.document;
        r3 = r3.getFileResolver();
        if (r3 != 0) goto L_0x0041;
    L_0x0040:
        return;
    L_0x0041:
        r4 = r12.href;
        r2 = r3.resolveImage(r4);
    L_0x0047:
        if (r2 != 0) goto L_0x0056;
    L_0x0049:
        r3 = "Could not locate image '%s'";
        r4 = 1;
        r4 = new java.lang.Object[r4];
        r5 = r12.href;
        r4[r1] = r5;
        error(r3, r4);
        return;
    L_0x0056:
        r1 = r11.state;
        r11.updateStyleForElement(r1, r12);
        r1 = r11.display();
        if (r1 != 0) goto L_0x0062;
    L_0x0061:
        return;
    L_0x0062:
        r1 = r11.visible();
        if (r1 != 0) goto L_0x0069;
    L_0x0068:
        return;
    L_0x0069:
        r1 = r12.transform;
        if (r1 == 0) goto L_0x0074;
    L_0x006d:
        r1 = r11.canvas;
        r3 = r12.transform;
        r1.concat(r3);
    L_0x0074:
        r1 = r12.x;
        r3 = 0;
        if (r1 == 0) goto L_0x0080;
    L_0x0079:
        r1 = r12.x;
        r1 = r1.floatValueX(r11);
        goto L_0x0081;
    L_0x0080:
        r1 = r3;
    L_0x0081:
        r4 = r12.y;
        if (r4 == 0) goto L_0x008c;
    L_0x0085:
        r4 = r12.y;
        r4 = r4.floatValueY(r11);
        goto L_0x008d;
    L_0x008c:
        r4 = r3;
    L_0x008d:
        r5 = r12.width;
        r5 = r5.floatValueX(r11);
        r6 = r12.height;
        r6 = r6.floatValueX(r11);
        r7 = r11.state;
        r8 = new com.caverock.androidsvg.SVG$Box;
        r8.<init>(r1, r4, r5, r6);
        r7.viewPort = r8;
        r7 = r11.state;
        r7 = r7.style;
        r7 = r7.overflow;
        r7 = r7.booleanValue();
        if (r7 != 0) goto L_0x00c9;
    L_0x00ae:
        r7 = r11.state;
        r7 = r7.viewPort;
        r7 = r7.minX;
        r8 = r11.state;
        r8 = r8.viewPort;
        r8 = r8.minY;
        r9 = r11.state;
        r9 = r9.viewPort;
        r9 = r9.width;
        r10 = r11.state;
        r10 = r10.viewPort;
        r10 = r10.height;
        r11.setClipRect(r7, r8, r9, r10);
    L_0x00c9:
        r7 = new com.caverock.androidsvg.SVG$Box;
        r8 = r2.getWidth();
        r8 = (float) r8;
        r9 = r2.getHeight();
        r9 = (float) r9;
        r7.<init>(r3, r3, r8, r9);
        r12.boundingBox = r7;
        r7 = r11.canvas;
        r8 = r11.state;
        r8 = r8.viewPort;
        r9 = r12.boundingBox;
        r8 = r11.calculateViewBoxTransform(r8, r9, r0);
        r7.concat(r8);
        r11.updateParentBoundingBox(r12);
        r11.checkForClipPath(r12);
        r7 = r11.pushLayer();
        r11.viewportFill();
        r8 = r11.canvas;
        r9 = r11.state;
        r9 = r9.fillPaint;
        r8.drawBitmap(r2, r3, r3, r9);
        if (r7 == 0) goto L_0x0104;
    L_0x0101:
        r11.popLayer(r12);
    L_0x0104:
        return;
    L_0x0105:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.caverock.androidsvg.SVGAndroidRenderer.render(com.caverock.androidsvg.SVG$Image):void");
    }

    /* JADX WARNING: Missing block: B:15:0x0041, code skipped:
            return null;
     */
    private android.graphics.Bitmap checkForImageDataURL(java.lang.String r5) {
        /*
        r4 = this;
        r0 = "data:";
        r0 = r5.startsWith(r0);
        r1 = 0;
        if (r0 != 0) goto L_0x000a;
    L_0x0009:
        return r1;
    L_0x000a:
        r0 = r5.length();
        r2 = 14;
        if (r0 >= r2) goto L_0x0013;
    L_0x0012:
        return r1;
    L_0x0013:
        r0 = 44;
        r0 = r5.indexOf(r0);
        r2 = -1;
        if (r0 == r2) goto L_0x0041;
    L_0x001c:
        r2 = 12;
        if (r0 >= r2) goto L_0x0021;
    L_0x0020:
        goto L_0x0041;
    L_0x0021:
        r2 = ";base64";
        r3 = r0 + -7;
        r3 = r5.substring(r3, r0);
        r2 = r2.equals(r3);
        if (r2 != 0) goto L_0x0030;
    L_0x002f:
        return r1;
    L_0x0030:
        r1 = r0 + 1;
        r1 = r5.substring(r1);
        r2 = 0;
        r1 = android.util.Base64.decode(r1, r2);
        r3 = r1.length;
        r2 = android.graphics.BitmapFactory.decodeByteArray(r1, r2, r3);
        return r2;
    L_0x0041:
        return r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.caverock.androidsvg.SVGAndroidRenderer.checkForImageDataURL(java.lang.String):android.graphics.Bitmap");
    }

    private boolean display() {
        if (this.state.style.display != null) {
            return this.state.style.display.booleanValue();
        }
        return true;
    }

    private boolean visible() {
        if (this.state.style.visibility != null) {
            return this.state.style.visibility.booleanValue();
        }
        return true;
    }

    private Matrix calculateViewBoxTransform(Box viewPort, Box viewBox, PreserveAspectRatio positioning) {
        Matrix m = new Matrix();
        if (positioning == null || positioning.getAlignment() == null) {
            return m;
        }
        float xScale = viewPort.width / viewBox.width;
        float yScale = viewPort.height / viewBox.height;
        float xOffset = -viewBox.minX;
        float yOffset = -viewBox.minY;
        if (positioning.equals(PreserveAspectRatio.STRETCH)) {
            m.preTranslate(viewPort.minX, viewPort.minY);
            m.preScale(xScale, yScale);
            m.preTranslate(xOffset, yOffset);
            return m;
        }
        float scale = positioning.getScale() == Scale.Slice ? Math.max(xScale, yScale) : Math.min(xScale, yScale);
        float imageW = viewPort.width / scale;
        float imageH = viewPort.height / scale;
        switch ($SWITCH_TABLE$com$caverock$androidsvg$PreserveAspectRatio$Alignment()[positioning.getAlignment().ordinal()]) {
            case 3:
            case 6:
            case 9:
                xOffset -= (viewBox.width - imageW) / 2.0f;
                break;
            case 4:
            case 7:
            case 10:
                xOffset -= viewBox.width - imageW;
                break;
        }
        switch ($SWITCH_TABLE$com$caverock$androidsvg$PreserveAspectRatio$Alignment()[positioning.getAlignment().ordinal()]) {
            case 5:
            case 6:
            case 7:
                yOffset -= (viewBox.height - imageH) / 2.0f;
                break;
            case 8:
            case 9:
            case 10:
                yOffset -= viewBox.height - imageH;
                break;
        }
        m.preTranslate(viewPort.minX, viewPort.minY);
        m.preScale(scale, scale);
        m.preTranslate(xOffset, yOffset);
        return m;
    }

    private boolean isSpecified(Style style, long flag) {
        return (style.specifiedFlags & flag) != 0;
    }

    private void updateStyle(RendererState state, Style style) {
        float intervalSum;
        if (isSpecified(style, PlaybackStateCompat.ACTION_SKIP_TO_QUEUE_ITEM)) {
            state.style.color = style.color;
        }
        if (isSpecified(style, PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH)) {
            state.style.opacity = style.opacity;
        }
        boolean z = false;
        if (isSpecified(style, 1)) {
            state.style.fill = style.fill;
            state.hasFill = style.fill != null;
        }
        if (isSpecified(style, 4)) {
            state.style.fillOpacity = style.fillOpacity;
        }
        if (isSpecified(style, 6149)) {
            setPaintColour(state, true, state.style.fill);
        }
        if (isSpecified(style, 2)) {
            state.style.fillRule = style.fillRule;
        }
        if (isSpecified(style, 8)) {
            state.style.stroke = style.stroke;
            state.hasStroke = style.stroke != null;
        }
        if (isSpecified(style, 16)) {
            state.style.strokeOpacity = style.strokeOpacity;
        }
        if (isSpecified(style, 6168)) {
            setPaintColour(state, false, state.style.stroke);
        }
        if (isSpecified(style, 34359738368L)) {
            state.style.vectorEffect = style.vectorEffect;
        }
        if (isSpecified(style, 32)) {
            state.style.strokeWidth = style.strokeWidth;
            state.strokePaint.setStrokeWidth(state.style.strokeWidth.floatValue(this));
        }
        if (isSpecified(style, 64)) {
            state.style.strokeLineCap = style.strokeLineCap;
            switch ($SWITCH_TABLE$com$caverock$androidsvg$SVG$Style$LineCaps()[style.strokeLineCap.ordinal()]) {
                case 1:
                    state.strokePaint.setStrokeCap(Cap.BUTT);
                    break;
                case 2:
                    state.strokePaint.setStrokeCap(Cap.ROUND);
                    break;
                case 3:
                    state.strokePaint.setStrokeCap(Cap.SQUARE);
                    break;
            }
        }
        if (isSpecified(style, 128)) {
            state.style.strokeLineJoin = style.strokeLineJoin;
            switch ($SWITCH_TABLE$com$caverock$androidsvg$SVG$Style$LineJoin()[style.strokeLineJoin.ordinal()]) {
                case 1:
                    state.strokePaint.setStrokeJoin(Join.MITER);
                    break;
                case 2:
                    state.strokePaint.setStrokeJoin(Join.ROUND);
                    break;
                case 3:
                    state.strokePaint.setStrokeJoin(Join.BEVEL);
                    break;
            }
        }
        if (isSpecified(style, 256)) {
            state.style.strokeMiterLimit = style.strokeMiterLimit;
            state.strokePaint.setStrokeMiter(style.strokeMiterLimit.floatValue());
        }
        if (isSpecified(style, 512)) {
            state.style.strokeDashArray = style.strokeDashArray;
        }
        if (isSpecified(style, PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID)) {
            state.style.strokeDashOffset = style.strokeDashOffset;
        }
        if (isSpecified(style, 1536)) {
            if (state.style.strokeDashArray == null) {
                state.strokePaint.setPathEffect(null);
            } else {
                intervalSum = 0.0f;
                int n = state.style.strokeDashArray.length;
                int arrayLen = n % 2 == 0 ? n : n * 2;
                float[] intervals = new float[arrayLen];
                for (int i = 0; i < arrayLen; i++) {
                    intervals[i] = state.style.strokeDashArray[i % n].floatValue(this);
                    intervalSum += intervals[i];
                }
                if (intervalSum == 0.0f) {
                    state.strokePaint.setPathEffect(null);
                } else {
                    float offset = state.style.strokeDashOffset.floatValue(this);
                    if (offset < 0.0f) {
                        offset = intervalSum + (offset % intervalSum);
                    }
                    state.strokePaint.setPathEffect(new DashPathEffect(intervals, offset));
                }
            }
        }
        if (isSpecified(style, PlaybackStateCompat.ACTION_PREPARE)) {
            intervalSum = getCurrentFontSize();
            state.style.fontSize = style.fontSize;
            state.fillPaint.setTextSize(style.fontSize.floatValue(this, intervalSum));
            state.strokePaint.setTextSize(style.fontSize.floatValue(this, intervalSum));
        }
        if (isSpecified(style, PlaybackStateCompat.ACTION_PLAY_FROM_URI)) {
            state.style.fontFamily = style.fontFamily;
        }
        if (isSpecified(style, PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID)) {
            Style style2;
            if (style.fontWeight.intValue() == -1 && state.style.fontWeight.intValue() > 100) {
                style2 = state.style;
                style2.fontWeight = Integer.valueOf(style2.fontWeight.intValue() - 100);
            } else if (style.fontWeight.intValue() != 1 || state.style.fontWeight.intValue() >= MediaPlayer2.MEDIA_INFO_TIMED_TEXT_ERROR) {
                state.style.fontWeight = style.fontWeight;
            } else {
                style2 = state.style;
                style2.fontWeight = Integer.valueOf(style2.fontWeight.intValue() + 100);
            }
        }
        if (isSpecified(style, PlaybackStateCompat.ACTION_PREPARE_FROM_SEARCH)) {
            state.style.fontStyle = style.fontStyle;
        }
        if (isSpecified(style, 106496)) {
            Typeface font = null;
            SVGExternalFileResolver fileResolver;
            if (state.style.fontFamily == null || this.document == null) {
                fileResolver = null;
            } else {
                fileResolver = this.document.getFileResolver();
                for (String fileResolver2 : state.style.fontFamily) {
                    font = checkGenericFont(fileResolver2, state.style.fontWeight, state.style.fontStyle);
                    if (font == null && fileResolver != null) {
                        font = fileResolver.resolveFont(fileResolver2, state.style.fontWeight.intValue(), String.valueOf(state.style.fontStyle));
                        continue;
                    }
                    if (font != null) {
                    }
                }
            }
            if (font == null) {
                font = checkGenericFont(DEFAULT_FONT_FAMILY, state.style.fontWeight, state.style.fontStyle);
            }
            state.fillPaint.setTypeface(font);
            state.strokePaint.setTypeface(font);
        }
        if (isSpecified(style, PlaybackStateCompat.ACTION_PREPARE_FROM_URI)) {
            state.style.textDecoration = style.textDecoration;
            state.fillPaint.setStrikeThruText(style.textDecoration == TextDecoration.LineThrough);
            state.fillPaint.setUnderlineText(style.textDecoration == TextDecoration.Underline);
            if (VERSION.SDK_INT >= 17) {
                state.strokePaint.setStrikeThruText(style.textDecoration == TextDecoration.LineThrough);
                Paint paint = state.strokePaint;
                if (style.textDecoration == TextDecoration.Underline) {
                    z = true;
                }
                paint.setUnderlineText(z);
            }
        }
        if (isSpecified(style, 68719476736L)) {
            state.style.direction = style.direction;
        }
        if (isSpecified(style, PlaybackStateCompat.ACTION_SET_REPEAT_MODE)) {
            state.style.textAnchor = style.textAnchor;
        }
        if (isSpecified(style, PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE_ENABLED)) {
            state.style.overflow = style.overflow;
        }
        if (isSpecified(style, PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE)) {
            state.style.markerStart = style.markerStart;
        }
        if (isSpecified(style, 4194304)) {
            state.style.markerMid = style.markerMid;
        }
        if (isSpecified(style, 8388608)) {
            state.style.markerEnd = style.markerEnd;
        }
        if (isSpecified(style, 16777216)) {
            state.style.display = style.display;
        }
        if (isSpecified(style, 33554432)) {
            state.style.visibility = style.visibility;
        }
        if (isSpecified(style, 1048576)) {
            state.style.clip = style.clip;
        }
        if (isSpecified(style, 268435456)) {
            state.style.clipPath = style.clipPath;
        }
        if (isSpecified(style, 536870912)) {
            state.style.clipRule = style.clipRule;
        }
        if (isSpecified(style, BillingCycleSettings.GIB_IN_BYTES)) {
            state.style.mask = style.mask;
        }
        if (isSpecified(style, 67108864)) {
            state.style.stopColor = style.stopColor;
        }
        if (isSpecified(style, 134217728)) {
            state.style.stopOpacity = style.stopOpacity;
        }
        if (isSpecified(style, 8589934592L)) {
            state.style.viewportFill = style.viewportFill;
        }
        if (isSpecified(style, 17179869184L)) {
            state.style.viewportFillOpacity = style.viewportFillOpacity;
        }
    }

    private void setPaintColour(RendererState state, boolean isFill, SvgPaint paint) {
        int col;
        float paintOpacity = (isFill ? state.style.fillOpacity : state.style.strokeOpacity).floatValue();
        if (paint instanceof Colour) {
            col = ((Colour) paint).colour;
        } else if (paint instanceof CurrentColor) {
            col = state.style.color.colour;
        } else {
            return;
        }
        col |= clamp255(paintOpacity) << 24;
        if (isFill) {
            state.fillPaint.setColor(col);
        } else {
            state.strokePaint.setColor(col);
        }
    }

    private Typeface checkGenericFont(String fontName, Integer fontWeight, FontStyle fontStyle) {
        int typefaceStyle = 0;
        boolean italic = fontStyle == FontStyle.Italic;
        if (fontWeight.intValue() > 500) {
            typefaceStyle = italic ? 3 : 1;
        } else if (italic) {
            typefaceStyle = 2;
        }
        if (fontName.equals("serif")) {
            return Typeface.create(Typeface.SERIF, typefaceStyle);
        }
        if (fontName.equals(DEFAULT_FONT_FAMILY)) {
            return Typeface.create(Typeface.SANS_SERIF, typefaceStyle);
        }
        if (fontName.equals("monospace")) {
            return Typeface.create(Typeface.MONOSPACE, typefaceStyle);
        }
        if (fontName.equals("cursive")) {
            return Typeface.create(Typeface.SANS_SERIF, typefaceStyle);
        }
        if (fontName.equals("fantasy")) {
            return Typeface.create(Typeface.SANS_SERIF, typefaceStyle);
        }
        return null;
    }

    private int clamp255(float val) {
        int i = (int) (1132462080 * val);
        if (i < 0) {
            return 0;
        }
        return i > 255 ? 255 : i;
    }

    private FillType getFillTypeFromState() {
        if (this.state.style.fillRule == null) {
            return FillType.WINDING;
        }
        if ($SWITCH_TABLE$com$caverock$androidsvg$SVG$Style$FillRule()[this.state.style.fillRule.ordinal()] != 2) {
            return FillType.WINDING;
        }
        return FillType.EVEN_ODD;
    }

    private void setClipRect(float minX, float minY, float width, float height) {
        float left = minX;
        float top = minY;
        float right = minX + width;
        float bottom = minY + height;
        if (this.state.style.clip != null) {
            left += this.state.style.clip.left.floatValueX(this);
            top += this.state.style.clip.top.floatValueY(this);
            right -= this.state.style.clip.right.floatValueX(this);
            bottom -= this.state.style.clip.bottom.floatValueY(this);
        }
        this.canvas.clipRect(left, top, right, bottom);
    }

    private void viewportFill() {
        int col;
        if (this.state.style.viewportFill instanceof Colour) {
            col = ((Colour) this.state.style.viewportFill).colour;
        } else if (this.state.style.viewportFill instanceof CurrentColor) {
            col = this.state.style.color.colour;
        } else {
            return;
        }
        if (this.state.style.viewportFillOpacity != null) {
            col |= clamp255(this.state.style.viewportFillOpacity.floatValue()) << 24;
        }
        this.canvas.drawColor(col);
    }

    private static void arcTo(float lastX, float lastY, float rx, float ry, float angle, boolean largeArcFlag, boolean sweepFlag, float x, float y, PathInterface pather) {
        float f = angle;
        boolean z = sweepFlag;
        float f2 = x;
        float f3 = y;
        if (lastX != f2 || lastY != f3) {
            if (rx == 0.0f || ry == 0.0f) {
                pather.lineTo(f2, f3);
                return;
            }
            double ry_sq;
            float ry2;
            double d;
            float rx2 = Math.abs(rx);
            float ry3 = Math.abs(ry);
            float angleRad = (float) Math.toRadians(((double) f) % 360.0d);
            double cosAngle = Math.cos((double) angleRad);
            double sinAngle = Math.sin((double) angleRad);
            double dx2 = ((double) (lastX - f2)) / 2.0d;
            double dy2 = ((double) (lastY - f3)) / 2.0d;
            double x1 = (cosAngle * dx2) + (sinAngle * dy2);
            double y1 = ((-sinAngle) * dx2) + (cosAngle * dy2);
            dx2 = (double) (rx2 * rx2);
            double sinAngle2 = sinAngle;
            sinAngle = (double) (ry3 * ry3);
            double x1_sq = x1 * x1;
            double y1_sq = y1 * y1;
            double rx_sq = dx2;
            dx2 = (x1_sq / dx2) + (y1_sq / sinAngle);
            if (dx2 > 1.0d) {
                rx2 *= (float) Math.sqrt(dx2);
                ry3 *= (float) Math.sqrt(dx2);
                ry_sq = (double) (ry3 * ry3);
                rx_sq = (double) (rx2 * rx2);
                rx2 = rx2;
                ry2 = ry3;
            } else {
                ry_sq = sinAngle;
                ry2 = ry3;
            }
            dx2 = (double) (largeArcFlag == z ? -1 : 1);
            double sq = (((rx_sq * ry_sq) - (rx_sq * y1_sq)) - (ry_sq * x1_sq)) / ((rx_sq * y1_sq) + (ry_sq * x1_sq));
            double cosAngle2 = cosAngle;
            cosAngle = sq < 0.0d ? 0.0d : sq;
            sq = Math.sqrt(cosAngle) * dx2;
            dx2 = ((((double) rx2) * y1) / ((double) ry2)) * sq;
            float ry4 = ry2;
            cosAngle = (-((((double) ry2) * x1) / ((double) rx2))) * sq;
            sinAngle = ((double) (lastX + f2)) / 2.0d;
            double sy2 = ((double) (lastY + f3)) / 2.0d;
            double y12 = y1;
            double cx = sinAngle + ((cosAngle2 * dx2) - (sinAngle2 * cosAngle));
            double ux = (x1 - dx2) / ((double) rx2);
            double cy = sy2 + ((sinAngle2 * dx2) + (cosAngle2 * cosAngle));
            f = ry4;
            sy2 = (y12 - cosAngle) / ((double) f);
            sinAngle = ((-x1) - dx2) / ((double) rx2);
            double y13 = y12;
            double vy = ((-y13) - cosAngle) / ((double) f);
            y13 = Math.sqrt((ux * ux) + (sy2 * sy2));
            double d2 = -1.0d;
            double angleStart = Math.toDegrees(Math.acos(ux / y13) * (sy2 < 0.0d ? -1.0d : 1.0d));
            y12 = Math.sqrt(((ux * ux) + (sy2 * sy2)) * ((sinAngle * sinAngle) + (vy * vy)));
            double p = (ux * sinAngle) + (sy2 * vy);
            if ((ux * vy) - (sy2 * sinAngle) >= 0.0d) {
                d2 = 1.0d;
            }
            y13 = Math.toDegrees(Math.acos(p / y12) * d2);
            if (z || y13 <= 0.0d) {
                d = 360.0d;
                if (z && y13 < 0.0d) {
                    y13 += 360.0d;
                }
            } else {
                d = 360.0d;
                y13 -= 360.0d;
            }
            float[] bezierPoints = arcToBeziers(angleStart % d, y13 % d);
            Matrix m = new Matrix();
            m.postScale(rx2, f);
            m.postRotate(angle);
            f = (float) cx;
            cx = cy;
            m.postTranslate(f, (float) cx);
            m.mapPoints(bezierPoints);
            bezierPoints[bezierPoints.length - 2] = x;
            bezierPoints[bezierPoints.length - 1] = y;
            int i = 0;
            while (true) {
                double cy2 = cx;
                if (i < bezierPoints.length) {
                    pather.cubicTo(bezierPoints[i], bezierPoints[i + 1], bezierPoints[i + 2], bezierPoints[i + 3], bezierPoints[i + 4], bezierPoints[i + 5]);
                    i += 6;
                    cx = cy2;
                } else {
                    return;
                }
            }
        }
    }

    private static float[] arcToBeziers(double angleStart, double angleExtent) {
        int numSegments = (int) Math.ceil(Math.abs(angleExtent) / 90.0d);
        double angleStart2 = Math.toRadians(angleStart);
        double angleExtent2 = Math.toRadians(angleExtent);
        float angleIncrement = (float) (angleExtent2 / ((double) numSegments));
        double controlLength = (1.3333333333333333d * Math.sin(((double) angleIncrement) / 2.0d)) / (1.0d + Math.cos(((double) angleIncrement) / 2.0d));
        float[] coords = new float[(numSegments * 6)];
        int pos = 0;
        int i = 0;
        while (i < numSegments) {
            double angle = ((double) (((float) i) * angleIncrement)) + angleStart2;
            double dx = Math.cos(angle);
            double dy = Math.sin(angle);
            int pos2 = pos + 1;
            int numSegments2 = numSegments;
            double angleStart3 = angleStart2;
            coords[pos] = (float) (dx - (controlLength * dy));
            numSegments = pos2 + 1;
            coords[pos2] = (float) (dy + (controlLength * dx));
            angle += (double) angleIncrement;
            angleStart2 = Math.cos(angle);
            dx = Math.sin(angle);
            pos = numSegments + 1;
            double angleExtent3 = angleExtent2;
            coords[numSegments] = (float) (angleStart2 + (controlLength * dx));
            numSegments = pos + 1;
            coords[pos] = (float) (dx - (controlLength * angleStart2));
            int pos3 = numSegments + 1;
            coords[numSegments] = (float) angleStart2;
            pos = pos3 + 1;
            coords[pos3] = (float) dx;
            i++;
            numSegments = numSegments2;
            angleStart2 = angleStart3;
            angleExtent2 = angleExtent3;
        }
        return coords;
    }

    private void renderMarkers(GraphicsElement obj) {
        if (this.state.style.markerStart != null || this.state.style.markerMid != null || this.state.style.markerEnd != null) {
            SvgObject ref;
            List<MarkerVector> markers;
            Marker _markerStart = null;
            Marker _markerMid = null;
            Marker _markerEnd = null;
            if (this.state.style.markerStart != null) {
                ref = obj.document.resolveIRI(this.state.style.markerStart);
                if (ref != null) {
                    _markerStart = (Marker) ref;
                } else {
                    error("Marker reference '%s' not found", this.state.style.markerStart);
                }
            }
            if (this.state.style.markerMid != null) {
                ref = obj.document.resolveIRI(this.state.style.markerMid);
                if (ref != null) {
                    _markerMid = (Marker) ref;
                } else {
                    error("Marker reference '%s' not found", this.state.style.markerMid);
                }
            }
            if (this.state.style.markerEnd != null) {
                ref = obj.document.resolveIRI(this.state.style.markerEnd);
                if (ref != null) {
                    _markerEnd = (Marker) ref;
                } else {
                    error("Marker reference '%s' not found", this.state.style.markerEnd);
                }
            }
            if (obj instanceof Path) {
                markers = new MarkerPositionCalculator(((Path) obj).d).getMarkers();
            } else if (obj instanceof Line) {
                markers = calculateMarkerPositions((Line) obj);
            } else {
                markers = calculateMarkerPositions((PolyLine) obj);
            }
            if (markers != null) {
                int markerCount = markers.size();
                if (markerCount != 0) {
                    Style style = this.state.style;
                    Style style2 = this.state.style;
                    this.state.style.markerEnd = null;
                    style2.markerMid = null;
                    style.markerStart = null;
                    if (_markerStart != null) {
                        renderMarker(_markerStart, (MarkerVector) markers.get(0));
                    }
                    if (_markerMid != null) {
                        for (int i = 1; i < markerCount - 1; i++) {
                            renderMarker(_markerMid, (MarkerVector) markers.get(i));
                        }
                    }
                    if (_markerEnd != null) {
                        renderMarker(_markerEnd, (MarkerVector) markers.get(markerCount - 1));
                    }
                }
            }
        }
    }

    private void renderMarker(Marker marker, MarkerVector pos) {
        float aspectScale;
        Marker marker2 = marker;
        MarkerVector markerVector = pos;
        float angle = 0.0f;
        statePush();
        float _refY = 0.0f;
        if (marker2.orient != null) {
            if (!Float.isNaN(marker2.orient.floatValue())) {
                angle = marker2.orient.floatValue();
            } else if (!(markerVector.dx == 0.0f && markerVector.dy == 0.0f)) {
                angle = (float) Math.toDegrees(Math.atan2((double) markerVector.dy, (double) markerVector.dx));
            }
        }
        float unitsScale = marker2.markerUnitsAreUser ? 1.0f : this.state.style.strokeWidth.floatValue(this.dpi);
        this.state = findInheritFromAncestorState(marker);
        Matrix m = new Matrix();
        m.preTranslate(markerVector.x, markerVector.y);
        m.preRotate(angle);
        m.preScale(unitsScale, unitsScale);
        float _refX = marker2.refX != null ? marker2.refX.floatValueX(this) : 0.0f;
        if (marker2.refY != null) {
            _refY = marker2.refY.floatValueY(this);
        }
        float _markerHeight = 3.0f;
        float _markerWidth = marker2.markerWidth != null ? marker2.markerWidth.floatValueX(this) : 3.0f;
        if (marker2.markerHeight != null) {
            _markerHeight = marker2.markerHeight.floatValueY(this);
        }
        Box _viewBox = marker2.viewBox != null ? marker2.viewBox : this.state.viewPort;
        float xScale = _markerWidth / _viewBox.width;
        float yScale = _markerHeight / _viewBox.height;
        PreserveAspectRatio positioning = marker2.preserveAspectRatio != null ? marker2.preserveAspectRatio : PreserveAspectRatio.LETTERBOX;
        if (!positioning.equals(PreserveAspectRatio.STRETCH)) {
            aspectScale = positioning.getScale() == Scale.Slice ? Math.max(xScale, yScale) : Math.min(xScale, yScale);
            yScale = aspectScale;
            xScale = aspectScale;
        }
        m.preTranslate((-_refX) * xScale, (-_refY) * yScale);
        this.canvas.concat(m);
        if (this.state.style.overflow.booleanValue()) {
        } else {
            aspectScale = _viewBox.width * xScale;
            float imageH = _viewBox.height * yScale;
            float xOffset = 0.0f;
            float yOffset = 0.0f;
            switch ($SWITCH_TABLE$com$caverock$androidsvg$PreserveAspectRatio$Alignment()[positioning.getAlignment().ordinal()]) {
                case 3:
                case 6:
                case 9:
                    xOffset = 0.0f - ((_markerWidth - aspectScale) / 2.0f);
                    break;
                case 4:
                case 7:
                case 10:
                    xOffset = 0.0f - (_markerWidth - aspectScale);
                    break;
            }
            float xOffset2 = xOffset;
            switch ($SWITCH_TABLE$com$caverock$androidsvg$PreserveAspectRatio$Alignment()[positioning.getAlignment().ordinal()]) {
                case 7.0E-45f:
                case 8.4E-45f:
                case 9.8E-45f:
                    yOffset = 0.0f - ((_markerHeight - imageH) / 2.0f);
                    break;
                case 1.1E-44f:
                case 1.3E-44f:
                case 1.4E-44f:
                    yOffset = 0.0f - (_markerHeight - imageH);
                    break;
            }
            setClipRect(xOffset2, yOffset, _markerWidth, _markerHeight);
        }
        m.reset();
        m.preScale(xScale, yScale);
        this.canvas.concat(m);
        boolean compositing = pushLayer();
        renderChildren(marker2, false);
        if (compositing) {
            popLayer(marker);
        }
        statePop();
    }

    private RendererState findInheritFromAncestorState(SvgObject obj) {
        RendererState newState = new RendererState();
        updateStyle(newState, Style.getDefaultStyle());
        return findInheritFromAncestorState(obj, newState);
    }

    private RendererState findInheritFromAncestorState(SvgObject obj, RendererState newState) {
        List<SvgElementBase> ancestors = new ArrayList();
        while (true) {
            if (obj instanceof SvgElementBase) {
                ancestors.add(0, (SvgElementBase) obj);
            }
            if (obj.parent == null) {
                break;
            }
            obj = (SvgObject) obj.parent;
        }
        for (SvgElementBase ancestor : ancestors) {
            updateStyleForElement(newState, ancestor);
        }
        newState.viewBox = this.document.getRootElement().viewBox;
        if (newState.viewBox == null) {
            newState.viewBox = this.canvasViewPort;
        }
        newState.viewPort = this.canvasViewPort;
        newState.directRendering = this.state.directRendering;
        return newState;
    }

    private void checkForGradiantsAndPatterns(SvgElement obj) {
        if (this.state.style.fill instanceof PaintReference) {
            decodePaintReference(true, obj.boundingBox, (PaintReference) this.state.style.fill);
        }
        if (this.state.style.stroke instanceof PaintReference) {
            decodePaintReference(false, obj.boundingBox, (PaintReference) this.state.style.stroke);
        }
    }

    private void decodePaintReference(boolean isFill, Box boundingBox, PaintReference paintref) {
        SvgObject ref = this.document.resolveIRI(paintref.href);
        if (ref == null) {
            String str = "%s reference '%s' not found";
            Object[] objArr = new Object[2];
            objArr[0] = isFill ? "Fill" : "Stroke";
            objArr[1] = paintref.href;
            error(str, objArr);
            if (paintref.fallback != null) {
                setPaintColour(this.state, isFill, paintref.fallback);
            } else if (isFill) {
                this.state.hasFill = false;
            } else {
                this.state.hasStroke = false;
            }
            return;
        }
        if (ref instanceof SvgLinearGradient) {
            makeLinearGradiant(isFill, boundingBox, (SvgLinearGradient) ref);
        }
        if (ref instanceof SvgRadialGradient) {
            makeRadialGradiant(isFill, boundingBox, (SvgRadialGradient) ref);
        }
        if (ref instanceof SolidColor) {
            setSolidColor(isFill, (SolidColor) ref);
        }
    }

    private void makeLinearGradiant(boolean isFill, Box boundingBox, SvgLinearGradient gradient) {
        float _y1;
        float _x2;
        float _x1;
        Box box = boundingBox;
        GradientElement gradientElement = gradient;
        if (gradientElement.href != null) {
            fillInChainedGradientFields(gradientElement, gradientElement.href);
        }
        boolean userUnits = gradientElement.gradientUnitsAreUser != null && gradientElement.gradientUnitsAreUser.booleanValue();
        Paint paint = isFill ? this.state.fillPaint : this.state.strokePaint;
        float _y2 = 0.0f;
        if (userUnits) {
            Box viewPortUser = getCurrentViewPortInUserUnits();
            float _x12 = gradientElement.x1 != null ? gradientElement.x1.floatValueX(this) : 0.0f;
            _y1 = gradientElement.y1 != null ? gradientElement.y1.floatValueY(this) : 0.0f;
            _x2 = gradientElement.x2 != null ? gradientElement.x2.floatValueX(this) : viewPortUser.width;
            if (gradientElement.y2 != null) {
                _y2 = gradientElement.y2.floatValueY(this);
            }
            _x1 = _x12;
        } else {
            float _x13 = gradientElement.x1 != null ? gradientElement.x1.floatValue(this, 1.0f) : 0.0f;
            _y1 = gradientElement.y1 != null ? gradientElement.y1.floatValue(this, 1.0f) : 0.0f;
            _x2 = gradientElement.x2 != null ? gradientElement.x2.floatValue(this, 1.0f) : 1.0f;
            if (gradientElement.y2 != null) {
                _y2 = gradientElement.y2.floatValue(this, 1.0f);
            }
            _x1 = _x13;
        }
        float _y12 = _y1;
        float _x22 = _x2;
        statePush();
        this.state = findInheritFromAncestorState(gradientElement);
        Matrix m = new Matrix();
        if (!userUnits) {
            m.preTranslate(box.minX, box.minY);
            m.preScale(box.width, box.height);
        }
        if (gradientElement.gradientTransform != null) {
            m.preConcat(gradientElement.gradientTransform);
        }
        int numStops = gradientElement.children.size();
        if (numStops == 0) {
            statePop();
            if (isFill) {
                this.state.hasFill = false;
            } else {
                this.state.hasStroke = false;
            }
            return;
        }
        Matrix m2;
        int[] colours = new int[numStops];
        float[] positions = new float[numStops];
        int i = 0;
        float lastOffset = -1.0f;
        for (SvgObject child : gradientElement.children) {
            float[] positions2 = positions;
            int numStops2 = numStops;
            m2 = m;
            Stop stop = (Stop) child;
            if (i == 0 || stop.offset.floatValue() >= lastOffset) {
                positions2[i] = stop.offset.floatValue();
                lastOffset = stop.offset.floatValue();
            } else {
                positions2[i] = lastOffset;
            }
            statePush();
            updateStyleForElement(this.state, stop);
            Colour col = this.state.style.stopColor;
            if (col == null) {
                col = Colour.BLACK;
            }
            colours[i] = (clamp255(this.state.style.stopOpacity.floatValue()) << 24) | col.colour;
            i++;
            statePop();
            m = m2;
            positions = positions2;
            numStops = numStops2;
        }
        if ((_x1 == _x22 && _y12 == _y2) || numStops == 1) {
            statePop();
            paint.setColor(colours[numStops - 1]);
            return;
        }
        TileMode tileMode = TileMode.CLAMP;
        if (gradientElement.spreadMethod != null) {
            if (gradientElement.spreadMethod == GradientSpread.reflect) {
                tileMode = TileMode.MIRROR;
            } else if (gradientElement.spreadMethod == GradientSpread.repeat) {
                tileMode = TileMode.REPEAT;
            }
        }
        statePop();
        m2 = m;
        LinearGradient gr = new LinearGradient(_x1, _y12, _x22, _y2, colours, positions, tileMode);
        gr.setLocalMatrix(m2);
        paint.setShader(gr);
    }

    private void makeRadialGradiant(boolean isFill, Box boundingBox, SvgRadialGradient gradient) {
        float _r;
        float _cx;
        float _cy;
        Box box = boundingBox;
        GradientElement gradientElement = gradient;
        if (gradientElement.href != null) {
            fillInChainedGradientFields(gradientElement, gradientElement.href);
        }
        boolean userUnits = gradientElement.gradientUnitsAreUser != null && gradientElement.gradientUnitsAreUser.booleanValue();
        Paint paint = isFill ? this.state.fillPaint : this.state.strokePaint;
        float _cx2;
        if (userUnits) {
            Length fiftyPercent = new Length(50.0f, Unit.percent);
            _cx2 = gradientElement.cx != null ? gradientElement.cx.floatValueX(this) : fiftyPercent.floatValueX(this);
            float _cy2 = gradientElement.cy != null ? gradientElement.cy.floatValueY(this) : fiftyPercent.floatValueY(this);
            _r = gradientElement.r != null ? gradientElement.r.floatValue(this) : fiftyPercent.floatValue(this);
            _cx = _cx2;
            _cy = _cy2;
        } else {
            _cx2 = 0.5f;
            _r = gradientElement.cx != null ? gradientElement.cx.floatValue(this, 1.0f) : 0.5f;
            float _cy3 = gradientElement.cy != null ? gradientElement.cy.floatValue(this, 1.0f) : 0.5f;
            if (gradientElement.r != null) {
                _cx2 = gradientElement.r.floatValue(this, 1.0f);
            }
            _cx = _r;
            _r = _cx2;
            _cy = _cy3;
        }
        statePush();
        this.state = findInheritFromAncestorState(gradientElement);
        Matrix m = new Matrix();
        if (!userUnits) {
            m.preTranslate(box.minX, box.minY);
            m.preScale(box.width, box.height);
        }
        if (gradientElement.gradientTransform != null) {
            m.preConcat(gradientElement.gradientTransform);
        }
        int numStops = gradientElement.children.size();
        if (numStops == 0) {
            statePop();
            if (isFill) {
                this.state.hasFill = false;
            } else {
                this.state.hasStroke = false;
            }
            return;
        }
        float[] positions;
        int numStops2;
        Matrix m2;
        int[] colours = new int[numStops];
        float[] positions2 = new float[numStops];
        int i = 0;
        float lastOffset = -1.0f;
        for (SvgObject child : gradientElement.children) {
            positions = positions2;
            numStops2 = numStops;
            m2 = m;
            Stop stop = (Stop) child;
            if (i == 0 || stop.offset.floatValue() >= lastOffset) {
                positions[i] = stop.offset.floatValue();
                lastOffset = stop.offset.floatValue();
            } else {
                positions[i] = lastOffset;
            }
            statePush();
            updateStyleForElement(this.state, stop);
            Colour col = this.state.style.stopColor;
            if (col == null) {
                col = Colour.BLACK;
            }
            colours[i] = (clamp255(this.state.style.stopOpacity.floatValue()) << 24) | col.colour;
            i++;
            statePop();
            m = m2;
            positions2 = positions;
            numStops = numStops2;
        }
        if (_r == 0.0f) {
            numStops2 = numStops;
            m2 = m;
        } else if (numStops == 1) {
            positions = positions2;
            numStops2 = numStops;
            m2 = m;
        } else {
            TileMode tileMode = TileMode.CLAMP;
            if (gradientElement.spreadMethod != null) {
                if (gradientElement.spreadMethod == GradientSpread.reflect) {
                    tileMode = TileMode.MIRROR;
                } else if (gradientElement.spreadMethod == GradientSpread.repeat) {
                    tileMode = TileMode.REPEAT;
                }
            }
            statePop();
            m2 = m;
            RadialGradient gr = new RadialGradient(_cx, _cy, _r, colours, positions2, tileMode);
            gr.setLocalMatrix(m2);
            paint.setShader(gr);
            return;
        }
        statePop();
        paint.setColor(colours[numStops2 - 1]);
    }

    private void fillInChainedGradientFields(GradientElement gradient, String href) {
        SvgObject ref = gradient.document.resolveIRI(href);
        if (ref == null) {
            warn("Gradient reference '%s' not found", href);
        } else if (!(ref instanceof GradientElement)) {
            error("Gradient href attributes must point to other gradient elements", new Object[0]);
        } else if (ref == gradient) {
            error("Circular reference in gradient href attribute '%s'", href);
        } else {
            GradientElement grRef = (GradientElement) ref;
            if (gradient.gradientUnitsAreUser == null) {
                gradient.gradientUnitsAreUser = grRef.gradientUnitsAreUser;
            }
            if (gradient.gradientTransform == null) {
                gradient.gradientTransform = grRef.gradientTransform;
            }
            if (gradient.spreadMethod == null) {
                gradient.spreadMethod = grRef.spreadMethod;
            }
            if (gradient.children.isEmpty()) {
                gradient.children = grRef.children;
            }
            try {
                if (gradient instanceof SvgLinearGradient) {
                    fillInChainedGradientFields((SvgLinearGradient) gradient, (SvgLinearGradient) ref);
                } else {
                    fillInChainedGradientFields((SvgRadialGradient) gradient, (SvgRadialGradient) ref);
                }
            } catch (ClassCastException e) {
            }
            if (grRef.href != null) {
                fillInChainedGradientFields(gradient, grRef.href);
            }
        }
    }

    private void fillInChainedGradientFields(SvgLinearGradient gradient, SvgLinearGradient grRef) {
        if (gradient.x1 == null) {
            gradient.x1 = grRef.x1;
        }
        if (gradient.y1 == null) {
            gradient.y1 = grRef.y1;
        }
        if (gradient.x2 == null) {
            gradient.x2 = grRef.x2;
        }
        if (gradient.y2 == null) {
            gradient.y2 = grRef.y2;
        }
    }

    private void fillInChainedGradientFields(SvgRadialGradient gradient, SvgRadialGradient grRef) {
        if (gradient.cx == null) {
            gradient.cx = grRef.cx;
        }
        if (gradient.cy == null) {
            gradient.cy = grRef.cy;
        }
        if (gradient.r == null) {
            gradient.r = grRef.r;
        }
        if (gradient.fx == null) {
            gradient.fx = grRef.fx;
        }
        if (gradient.fy == null) {
            gradient.fy = grRef.fy;
        }
    }

    private void setSolidColor(boolean isFill, SolidColor ref) {
        boolean z = false;
        RendererState rendererState;
        if (isFill) {
            if (isSpecified(ref.baseStyle, 2147483648L)) {
                this.state.style.fill = ref.baseStyle.solidColor;
                rendererState = this.state;
                if (ref.baseStyle.solidColor != null) {
                    z = true;
                }
                rendererState.hasFill = z;
            }
            if (isSpecified(ref.baseStyle, 4294967296L)) {
                this.state.style.fillOpacity = ref.baseStyle.solidOpacity;
            }
            if (isSpecified(ref.baseStyle, 6442450944L)) {
                setPaintColour(this.state, isFill, this.state.style.fill);
                return;
            }
            return;
        }
        if (isSpecified(ref.baseStyle, 2147483648L)) {
            this.state.style.stroke = ref.baseStyle.solidColor;
            rendererState = this.state;
            if (ref.baseStyle.solidColor != null) {
                z = true;
            }
            rendererState.hasStroke = z;
        }
        if (isSpecified(ref.baseStyle, 4294967296L)) {
            this.state.style.strokeOpacity = ref.baseStyle.solidOpacity;
        }
        if (isSpecified(ref.baseStyle, 6442450944L)) {
            setPaintColour(this.state, isFill, this.state.style.stroke);
        }
    }

    private void checkForClipPath(SvgElement obj) {
        checkForClipPath(obj, obj.boundingBox);
    }

    private void checkForClipPath(SvgElement obj, Box boundingBox) {
        if (this.state.style.clipPath != null) {
            SvgObject ref = obj.document.resolveIRI(this.state.style.clipPath);
            if (ref == null) {
                error("ClipPath reference '%s' not found", this.state.style.clipPath);
                return;
            }
            ClipPath clipPath = (ClipPath) ref;
            if (clipPath.children.isEmpty()) {
                this.canvas.clipRect(0, 0, 0, 0);
                return;
            }
            boolean userUnits = clipPath.clipPathUnitsAreUser == null || clipPath.clipPathUnitsAreUser.booleanValue();
            if (!(obj instanceof Group) || userUnits) {
                clipStatePush();
                if (!userUnits) {
                    Matrix m = new Matrix();
                    m.preTranslate(boundingBox.minX, boundingBox.minY);
                    m.preScale(boundingBox.width, boundingBox.height);
                    this.canvas.concat(m);
                }
                if (clipPath.transform != null) {
                    this.canvas.concat(clipPath.transform);
                }
                this.state = findInheritFromAncestorState(clipPath);
                checkForClipPath(clipPath);
                Path combinedPath = new Path();
                for (SvgObject child : clipPath.children) {
                    addObjectToClip(child, true, combinedPath, new Matrix());
                }
                this.canvas.clipPath(combinedPath);
                clipStatePop();
                return;
            }
            warn("<clipPath clipPathUnits=\"objectBoundingBox\"> is not supported when referenced from container elements (like %s)", obj.getClass().getSimpleName());
        }
    }

    private void addObjectToClip(SvgObject obj, boolean allowUse, Path combinedPath, Matrix combinedPathMatrix) {
        if (display()) {
            clipStatePush();
            if (obj instanceof Use) {
                if (allowUse) {
                    addObjectToClip((Use) obj, combinedPath, combinedPathMatrix);
                } else {
                    error("<use> elements inside a <clipPath> cannot reference another <use>", new Object[0]);
                }
            } else if (obj instanceof Path) {
                addObjectToClip((Path) obj, combinedPath, combinedPathMatrix);
            } else if (obj instanceof Text) {
                addObjectToClip((Text) obj, combinedPath, combinedPathMatrix);
            } else if (obj instanceof GraphicsElement) {
                addObjectToClip((GraphicsElement) obj, combinedPath, combinedPathMatrix);
            } else {
                error("Invalid %s element found in clipPath definition", obj.getClass().getSimpleName());
            }
            clipStatePop();
        }
    }

    private void clipStatePush() {
        this.canvas.save(1);
        this.stateStack.push(this.state);
        this.state = (RendererState) this.state.clone();
    }

    private void clipStatePop() {
        this.canvas.restore();
        this.state = (RendererState) this.stateStack.pop();
    }

    private FillType getClipRuleFromState() {
        if (this.state.style.clipRule == null) {
            return FillType.WINDING;
        }
        if ($SWITCH_TABLE$com$caverock$androidsvg$SVG$Style$FillRule()[this.state.style.clipRule.ordinal()] != 2) {
            return FillType.WINDING;
        }
        return FillType.EVEN_ODD;
    }

    private void addObjectToClip(Path obj, Path combinedPath, Matrix combinedPathMatrix) {
        updateStyleForElement(this.state, obj);
        if (display() && visible()) {
            if (obj.transform != null) {
                combinedPathMatrix.preConcat(obj.transform);
            }
            Path path = new PathConverter(obj.d).getPath();
            if (obj.boundingBox == null) {
                obj.boundingBox = calculatePathBounds(path);
            }
            checkForClipPath(obj);
            combinedPath.setFillType(getClipRuleFromState());
            combinedPath.addPath(path, combinedPathMatrix);
        }
    }

    private void addObjectToClip(GraphicsElement obj, Path combinedPath, Matrix combinedPathMatrix) {
        updateStyleForElement(this.state, obj);
        if (display() && visible()) {
            Path path;
            if (obj.transform != null) {
                combinedPathMatrix.preConcat(obj.transform);
            }
            if (obj instanceof Rect) {
                path = makePathAndBoundingBox((Rect) obj);
            } else if (obj instanceof Circle) {
                path = makePathAndBoundingBox((Circle) obj);
            } else if (obj instanceof Ellipse) {
                path = makePathAndBoundingBox((Ellipse) obj);
            } else if (obj instanceof PolyLine) {
                path = makePathAndBoundingBox((PolyLine) obj);
            } else {
                return;
            }
            checkForClipPath(obj);
            combinedPath.setFillType(path.getFillType());
            combinedPath.addPath(path, combinedPathMatrix);
        }
    }

    private void addObjectToClip(Use obj, Path combinedPath, Matrix combinedPathMatrix) {
        updateStyleForElement(this.state, obj);
        if (display() && visible()) {
            if (obj.transform != null) {
                combinedPathMatrix.preConcat(obj.transform);
            }
            SvgObject ref = obj.document.resolveIRI(obj.href);
            if (ref == null) {
                error("Use reference '%s' not found", obj.href);
                return;
            }
            checkForClipPath(obj);
            addObjectToClip(ref, false, combinedPath, combinedPathMatrix);
        }
    }

    private void addObjectToClip(Text obj, Path combinedPath, Matrix combinedPathMatrix) {
        updateStyleForElement(this.state, obj);
        if (display()) {
            if (obj.transform != null) {
                combinedPathMatrix.preConcat(obj.transform);
            }
            float dy = 0.0f;
            float x = (obj.x == null || obj.x.size() == 0) ? 0.0f : ((Length) obj.x.get(0)).floatValueX(this);
            float y = (obj.y == null || obj.y.size() == 0) ? 0.0f : ((Length) obj.y.get(0)).floatValueY(this);
            float dx = (obj.dx == null || obj.dx.size() == 0) ? 0.0f : ((Length) obj.dx.get(0)).floatValueX(this);
            if (!(obj.dy == null || obj.dy.size() == 0)) {
                dy = ((Length) obj.dy.get(0)).floatValueY(this);
            }
            if (this.state.style.textAnchor != TextAnchor.Start) {
                float textWidth = calculateTextWidth(obj);
                if (this.state.style.textAnchor == TextAnchor.Middle) {
                    x -= textWidth / 2.0f;
                } else {
                    x -= textWidth;
                }
            }
            if (obj.boundingBox == null) {
                TextBoundsCalculator proc = new TextBoundsCalculator(x, y);
                enumerateTextSpans(obj, proc);
                obj.boundingBox = new Box(proc.bbox.left, proc.bbox.top, proc.bbox.width(), proc.bbox.height());
            }
            checkForClipPath(obj);
            Path textAsPath = new Path();
            enumerateTextSpans(obj, new PlainTextToPath(x + dx, y + dy, textAsPath));
            combinedPath.setFillType(getClipRuleFromState());
            combinedPath.addPath(textAsPath, combinedPathMatrix);
        }
    }

    private Path makePathAndBoundingBox(Line obj) {
        float y2 = 0.0f;
        float x1 = obj.x1 == null ? 0.0f : obj.x1.floatValue(this);
        float y1 = obj.y1 == null ? 0.0f : obj.y1.floatValue(this);
        float x2 = obj.x2 == null ? 0.0f : obj.x2.floatValue(this);
        if (obj.y2 != null) {
            y2 = obj.y2.floatValue(this);
        }
        if (obj.boundingBox == null) {
            obj.boundingBox = new Box(Math.min(x1, y1), Math.min(y1, y2), Math.abs(x2 - x1), Math.abs(y2 - y1));
        }
        Path p = new Path();
        p.moveTo(x1, y1);
        p.lineTo(x2, y2);
        return p;
    }

    private Path makePathAndBoundingBox(Rect obj) {
        float rx;
        float ry;
        Path p;
        float bottom;
        Rect rect = obj;
        if (rect.rx == null && rect.ry == null) {
            rx = 0.0f;
            ry = 0.0f;
        } else if (rect.rx == null) {
            rx = rect.ry.floatValueY(this);
            ry = rx;
        } else if (rect.ry == null) {
            rx = rect.rx.floatValueX(this);
            ry = rx;
        } else {
            rx = rect.rx.floatValueX(this);
            ry = rect.ry.floatValueY(this);
        }
        rx = Math.min(rx, rect.width.floatValueX(this) / 2.0f);
        ry = Math.min(ry, rect.height.floatValueY(this) / 2.0f);
        float x = rect.x != null ? rect.x.floatValueX(this) : 0.0f;
        float y = rect.y != null ? rect.y.floatValueY(this) : 0.0f;
        float w = rect.width.floatValueX(this);
        float h = rect.height.floatValueY(this);
        if (rect.boundingBox == null) {
            rect.boundingBox = new Box(x, y, w, h);
        }
        float right = x + w;
        float bottom2 = y + h;
        Path p2 = new Path();
        float f;
        float f2;
        if (rx == 0.0f) {
            p = p2;
            bottom = bottom2;
            f = h;
            f2 = w;
            w = right;
        } else if (ry == 0.0f) {
            p = p2;
            bottom = bottom2;
            f = h;
            f2 = w;
            w = right;
        } else {
            float cpx = rx * BEZIER_ARC_FACTOR;
            float cpy = BEZIER_ARC_FACTOR * ry;
            p2.moveTo(x, y + ry);
            p = p2;
            bottom = bottom2;
            w = right;
            p2.cubicTo(x, (y + ry) - cpy, (x + rx) - cpx, y, x + rx, y);
            p.lineTo(w - rx, y);
            p.cubicTo((w - rx) + cpx, y, w, (y + ry) - cpy, w, y + ry);
            p.lineTo(w, bottom - ry);
            p.cubicTo(w, (bottom - ry) + cpy, (w - rx) + cpx, bottom, w - rx, bottom);
            p.lineTo(x + rx, bottom);
            p.cubicTo((x + rx) - cpx, bottom, x, (bottom - ry) + cpy, x, bottom - ry);
            p.lineTo(x, y + ry);
            p.close();
            return p;
        }
        p.moveTo(x, y);
        p.lineTo(w, y);
        p.lineTo(w, bottom);
        p.lineTo(x, bottom);
        p.lineTo(x, y);
        p.close();
        return p;
    }

    private Path makePathAndBoundingBox(Circle obj) {
        Circle circle = obj;
        float cy = 0.0f;
        float cx = circle.cx != null ? circle.cx.floatValueX(this) : 0.0f;
        if (circle.cy != null) {
            cy = circle.cy.floatValueY(this);
        }
        float r = circle.r.floatValue(this);
        float left = cx - r;
        float top = cy - r;
        float right = cx + r;
        float bottom = cy + r;
        if (circle.boundingBox == null) {
            circle.boundingBox = new Box(left, top, r * 2.0f, 2.0f * r);
        }
        float cp = r * BEZIER_ARC_FACTOR;
        Path path = new Path();
        Path p = path;
        p.moveTo(cx, top);
        Path p2 = p;
        path.cubicTo(cx + cp, top, right, cy - cp, right, cy);
        path = p2;
        path.cubicTo(right, cy + cp, cx + cp, bottom, cx, bottom);
        path.cubicTo(cx - cp, bottom, left, cy + cp, left, cy);
        path.cubicTo(left, cy - cp, cx - cp, top, cx, top);
        path.close();
        return path;
    }

    private Path makePathAndBoundingBox(Ellipse obj) {
        Ellipse ellipse = obj;
        float cy = 0.0f;
        float cx = ellipse.cx != null ? ellipse.cx.floatValueX(this) : 0.0f;
        if (ellipse.cy != null) {
            cy = ellipse.cy.floatValueY(this);
        }
        float rx = ellipse.rx.floatValueX(this);
        float ry = ellipse.ry.floatValueY(this);
        float left = cx - rx;
        float top = cy - ry;
        float right = cx + rx;
        float bottom = cy + ry;
        if (ellipse.boundingBox == null) {
            ellipse.boundingBox = new Box(left, top, rx * 2.0f, 2.0f * ry);
        }
        float cpx = rx * BEZIER_ARC_FACTOR;
        float cpy = ry * BEZIER_ARC_FACTOR;
        Path path = new Path();
        Path p = path;
        p.moveTo(cx, top);
        Path p2 = p;
        path.cubicTo(cx + cpx, top, right, cy - cpy, right, cy);
        path = p2;
        path.cubicTo(right, cy + cpy, cx + cpx, bottom, cx, bottom);
        path.cubicTo(cx - cpx, bottom, left, cy + cpy, left, cy);
        path.cubicTo(left, cy - cpy, cx - cpx, top, cx, top);
        path.close();
        return path;
    }

    private Path makePathAndBoundingBox(PolyLine obj) {
        Path path = new Path();
        path.moveTo(obj.points[0], obj.points[1]);
        for (int i = 2; i < obj.points.length; i += 2) {
            path.lineTo(obj.points[i], obj.points[i + 1]);
        }
        if (obj instanceof Polygon) {
            path.close();
        }
        if (obj.boundingBox == null) {
            obj.boundingBox = calculatePathBounds(path);
        }
        path.setFillType(getClipRuleFromState());
        return path;
    }

    /* JADX WARNING: Removed duplicated region for block: B:81:0x01f4  */
    private void fillWithPattern(com.caverock.androidsvg.SVG.SvgElement r29, android.graphics.Path r30, com.caverock.androidsvg.SVG.Pattern r31) {
        /*
        r28 = this;
        r0 = r28;
        r1 = r29;
        r2 = r31;
        r3 = r2.patternUnitsAreUser;
        r4 = 0;
        if (r3 == 0) goto L_0x0015;
    L_0x000b:
        r3 = r2.patternUnitsAreUser;
        r3 = r3.booleanValue();
        if (r3 == 0) goto L_0x0015;
    L_0x0013:
        r3 = 1;
        goto L_0x0016;
    L_0x0015:
        r3 = r4;
    L_0x0016:
        r6 = r2.href;
        if (r6 == 0) goto L_0x001f;
    L_0x001a:
        r6 = r2.href;
        r0.fillInChainedPatternFields(r2, r6);
    L_0x001f:
        r6 = 0;
        if (r3 == 0) goto L_0x005a;
    L_0x0022:
        r7 = r2.x;
        if (r7 == 0) goto L_0x002d;
    L_0x0026:
        r7 = r2.x;
        r7 = r7.floatValueX(r0);
        goto L_0x002e;
    L_0x002d:
        r7 = r6;
    L_0x002e:
        r8 = r2.y;
        if (r8 == 0) goto L_0x0039;
    L_0x0032:
        r8 = r2.y;
        r8 = r8.floatValueY(r0);
        goto L_0x003a;
    L_0x0039:
        r8 = r6;
    L_0x003a:
        r9 = r2.width;
        if (r9 == 0) goto L_0x0045;
    L_0x003e:
        r9 = r2.width;
        r9 = r9.floatValueX(r0);
        goto L_0x0046;
    L_0x0045:
        r9 = r6;
    L_0x0046:
        r10 = r2.height;
        if (r10 == 0) goto L_0x0051;
    L_0x004a:
        r10 = r2.height;
        r10 = r10.floatValueY(r0);
        goto L_0x0052;
    L_0x0051:
        r10 = r6;
        r27 = r9;
        r9 = r8;
        r8 = r10;
        r10 = r27;
        goto L_0x00ac;
    L_0x005a:
        r7 = r2.x;
        r8 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        if (r7 == 0) goto L_0x0067;
    L_0x0060:
        r7 = r2.x;
        r7 = r7.floatValue(r0, r8);
        goto L_0x0068;
    L_0x0067:
        r7 = r6;
    L_0x0068:
        r9 = r2.y;
        if (r9 == 0) goto L_0x0073;
    L_0x006c:
        r9 = r2.y;
        r9 = r9.floatValue(r0, r8);
        goto L_0x0074;
    L_0x0073:
        r9 = r6;
    L_0x0074:
        r10 = r2.width;
        if (r10 == 0) goto L_0x007f;
    L_0x0078:
        r10 = r2.width;
        r10 = r10.floatValue(r0, r8);
        goto L_0x0080;
    L_0x007f:
        r10 = r6;
    L_0x0080:
        r11 = r2.height;
        if (r11 == 0) goto L_0x008b;
    L_0x0084:
        r11 = r2.height;
        r8 = r11.floatValue(r0, r8);
        goto L_0x008c;
    L_0x008b:
        r8 = r6;
    L_0x008c:
        r11 = r1.boundingBox;
        r11 = r11.minX;
        r12 = r1.boundingBox;
        r12 = r12.width;
        r12 = r12 * r7;
        r7 = r11 + r12;
        r11 = r1.boundingBox;
        r11 = r11.minY;
        r12 = r1.boundingBox;
        r12 = r12.height;
        r12 = r12 * r9;
        r9 = r11 + r12;
        r11 = r1.boundingBox;
        r11 = r11.width;
        r10 = r10 * r11;
        r11 = r1.boundingBox;
        r11 = r11.height;
        r8 = r8 * r11;
    L_0x00ac:
        r11 = (r10 > r6 ? 1 : (r10 == r6 ? 0 : -1));
        if (r11 == 0) goto L_0x0289;
    L_0x00b0:
        r11 = (r8 > r6 ? 1 : (r8 == r6 ? 0 : -1));
        if (r11 != 0) goto L_0x00b8;
    L_0x00b4:
        r19 = r3;
        goto L_0x028b;
    L_0x00b8:
        r11 = r2.preserveAspectRatio;
        if (r11 == 0) goto L_0x00bf;
    L_0x00bc:
        r11 = r2.preserveAspectRatio;
        goto L_0x00c1;
    L_0x00bf:
        r11 = com.caverock.androidsvg.PreserveAspectRatio.LETTERBOX;
    L_0x00c1:
        r28.statePush();
        r12 = r0.canvas;
        r13 = r30;
        r12.clipPath(r13);
        r12 = new com.caverock.androidsvg.SVGAndroidRenderer$RendererState;
        r12.<init>();
        r14 = com.caverock.androidsvg.SVG.Style.getDefaultStyle();
        r0.updateStyle(r12, r14);
        r14 = r12.style;
        r15 = java.lang.Boolean.valueOf(r4);
        r14.overflow = r15;
        r14 = r0.findInheritFromAncestorState(r2, r12);
        r0.state = r14;
        r14 = r1.boundingBox;
        r15 = r2.patternTransform;
        if (r15 == 0) goto L_0x01bb;
    L_0x00eb:
        r15 = r0.canvas;
        r6 = r2.patternTransform;
        r15.concat(r6);
        r6 = new android.graphics.Matrix;
        r6.<init>();
        r15 = r2.patternTransform;
        r15 = r15.invert(r6);
        if (r15 == 0) goto L_0x01bb;
    L_0x00ff:
        r15 = 8;
        r15 = new float[r15];
        r5 = r1.boundingBox;
        r5 = r5.minX;
        r15[r4] = r5;
        r5 = r1.boundingBox;
        r5 = r5.minY;
        r17 = 1;
        r15[r17] = r5;
        r5 = r1.boundingBox;
        r5 = r5.maxX();
        r18 = 2;
        r15[r18] = r5;
        r5 = 3;
        r4 = r1.boundingBox;
        r4 = r4.minY;
        r15[r5] = r4;
        r4 = 4;
        r5 = r1.boundingBox;
        r5 = r5.maxX();
        r15[r4] = r5;
        r4 = 5;
        r5 = r1.boundingBox;
        r5 = r5.maxY();
        r15[r4] = r5;
        r4 = r1.boundingBox;
        r4 = r4.minX;
        r5 = 6;
        r15[r5] = r4;
        r4 = 7;
        r5 = r1.boundingBox;
        r5 = r5.maxY();
        r15[r4] = r5;
        r4 = r15;
        r6.mapPoints(r4);
        r5 = new android.graphics.RectF;
        r19 = r3;
        r15 = 0;
        r3 = r4[r15];
        r20 = r6;
        r17 = 1;
        r6 = r4[r17];
        r21 = r12;
        r12 = r4[r15];
        r15 = r4[r17];
        r5.<init>(r3, r6, r12, r15);
        r3 = r5;
        r5 = 2;
    L_0x0160:
        r6 = 6;
        if (r5 <= r6) goto L_0x017a;
    L_0x0163:
        r5 = new com.caverock.androidsvg.SVG$Box;
        r6 = r3.left;
        r12 = r3.top;
        r15 = r3.right;
        r13 = r3.left;
        r15 = r15 - r13;
        r13 = r3.bottom;
        r22 = r14;
        r14 = r3.top;
        r13 = r13 - r14;
        r5.<init>(r6, r12, r15, r13);
        r14 = r5;
        goto L_0x01c5;
    L_0x017a:
        r22 = r14;
        r12 = r4[r5];
        r13 = r3.left;
        r12 = (r12 > r13 ? 1 : (r12 == r13 ? 0 : -1));
        if (r12 >= 0) goto L_0x0188;
    L_0x0184:
        r12 = r4[r5];
        r3.left = r12;
    L_0x0188:
        r12 = r4[r5];
        r13 = r3.right;
        r12 = (r12 > r13 ? 1 : (r12 == r13 ? 0 : -1));
        if (r12 <= 0) goto L_0x0194;
    L_0x0190:
        r12 = r4[r5];
        r3.right = r12;
    L_0x0194:
        r12 = r5 + 1;
        r12 = r4[r12];
        r13 = r3.top;
        r12 = (r12 > r13 ? 1 : (r12 == r13 ? 0 : -1));
        if (r12 >= 0) goto L_0x01a4;
    L_0x019e:
        r12 = r5 + 1;
        r12 = r4[r12];
        r3.top = r12;
    L_0x01a4:
        r12 = r5 + 1;
        r12 = r4[r12];
        r13 = r3.bottom;
        r12 = (r12 > r13 ? 1 : (r12 == r13 ? 0 : -1));
        if (r12 <= 0) goto L_0x01b4;
    L_0x01ae:
        r12 = r5 + 1;
        r12 = r4[r12];
        r3.bottom = r12;
    L_0x01b4:
        r5 = r5 + 2;
        r14 = r22;
        r13 = r30;
        goto L_0x0160;
    L_0x01bb:
        r19 = r3;
        r21 = r12;
        r22 = r14;
        r17 = 1;
        r14 = r22;
    L_0x01c5:
        r3 = r14.minX;
        r3 = r3 - r7;
        r3 = r3 / r10;
        r3 = (double) r3;
        r3 = java.lang.Math.floor(r3);
        r3 = (float) r3;
        r3 = r3 * r10;
        r3 = r3 + r7;
        r4 = r14.minY;
        r4 = r4 - r9;
        r4 = r4 / r8;
        r4 = (double) r4;
        r4 = java.lang.Math.floor(r4);
        r4 = (float) r4;
        r4 = r4 * r8;
        r4 = r4 + r9;
        r5 = r14.maxX();
        r6 = r14.maxY();
        r12 = new com.caverock.androidsvg.SVG$Box;
        r13 = 0;
        r12.<init>(r13, r13, r10, r8);
        r13 = r4;
    L_0x01ec:
        r15 = (r13 > r6 ? 1 : (r13 == r6 ? 0 : -1));
        if (r15 < 0) goto L_0x01f4;
    L_0x01f0:
        r28.statePop();
        return;
    L_0x01f4:
        r15 = r3;
    L_0x01f5:
        r16 = (r15 > r5 ? 1 : (r15 == r5 ? 0 : -1));
        if (r16 < 0) goto L_0x01fb;
    L_0x01f9:
        r13 = r13 + r8;
        goto L_0x01ec;
    L_0x01fb:
        r12.minX = r15;
        r12.minY = r13;
        r28.statePush();
        r23 = r3;
        r3 = r0.state;
        r3 = r3.style;
        r3 = r3.overflow;
        r3 = r3.booleanValue();
        if (r3 != 0) goto L_0x0222;
    L_0x0210:
        r3 = r12.minX;
        r24 = r4;
        r4 = r12.minY;
        r25 = r5;
        r5 = r12.width;
        r26 = r6;
        r6 = r12.height;
        r0.setClipRect(r3, r4, r5, r6);
        goto L_0x0228;
    L_0x0222:
        r24 = r4;
        r25 = r5;
        r26 = r6;
    L_0x0228:
        r3 = r2.viewBox;
        if (r3 == 0) goto L_0x0238;
    L_0x022c:
        r3 = r0.canvas;
        r4 = r2.viewBox;
        r4 = r0.calculateViewBoxTransform(r12, r4, r11);
        r3.concat(r4);
        goto L_0x025c;
    L_0x0238:
        r3 = r2.patternContentUnitsAreUser;
        if (r3 == 0) goto L_0x0246;
    L_0x023c:
        r3 = r2.patternContentUnitsAreUser;
        r3 = r3.booleanValue();
        if (r3 != 0) goto L_0x0246;
    L_0x0244:
        r3 = 0;
        goto L_0x0248;
    L_0x0246:
        r3 = r17;
    L_0x0248:
        r4 = r0.canvas;
        r4.translate(r15, r13);
        if (r3 != 0) goto L_0x025c;
    L_0x024f:
        r4 = r0.canvas;
        r5 = r1.boundingBox;
        r5 = r5.width;
        r6 = r1.boundingBox;
        r6 = r6.height;
        r4.scale(r5, r6);
    L_0x025c:
        r3 = r28.pushLayer();
        r4 = r2.children;
        r4 = r4.iterator();
    L_0x0266:
        r5 = r4.hasNext();
        if (r5 != 0) goto L_0x027f;
    L_0x026c:
        if (r3 == 0) goto L_0x0271;
    L_0x026e:
        r0.popLayer(r2);
    L_0x0271:
        r28.statePop();
        r15 = r15 + r10;
        r3 = r23;
        r4 = r24;
        r5 = r25;
        r6 = r26;
        goto L_0x01f5;
    L_0x027f:
        r5 = r4.next();
        r5 = (com.caverock.androidsvg.SVG.SvgObject) r5;
        r0.render(r5);
        goto L_0x0266;
    L_0x0289:
        r19 = r3;
    L_0x028b:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.caverock.androidsvg.SVGAndroidRenderer.fillWithPattern(com.caverock.androidsvg.SVG$SvgElement, android.graphics.Path, com.caverock.androidsvg.SVG$Pattern):void");
    }

    private void fillInChainedPatternFields(Pattern pattern, String href) {
        SvgObject ref = pattern.document.resolveIRI(href);
        if (ref == null) {
            warn("Pattern reference '%s' not found", href);
        } else if (!(ref instanceof Pattern)) {
            error("Pattern href attributes must point to other pattern elements", new Object[0]);
        } else if (ref == pattern) {
            error("Circular reference in pattern href attribute '%s'", href);
        } else {
            Pattern pRef = (Pattern) ref;
            if (pattern.patternUnitsAreUser == null) {
                pattern.patternUnitsAreUser = pRef.patternUnitsAreUser;
            }
            if (pattern.patternContentUnitsAreUser == null) {
                pattern.patternContentUnitsAreUser = pRef.patternContentUnitsAreUser;
            }
            if (pattern.patternTransform == null) {
                pattern.patternTransform = pRef.patternTransform;
            }
            if (pattern.x == null) {
                pattern.x = pRef.x;
            }
            if (pattern.y == null) {
                pattern.y = pRef.y;
            }
            if (pattern.width == null) {
                pattern.width = pRef.width;
            }
            if (pattern.height == null) {
                pattern.height = pRef.height;
            }
            if (pattern.children.isEmpty()) {
                pattern.children = pRef.children;
            }
            if (pattern.viewBox == null) {
                pattern.viewBox = pRef.viewBox;
            }
            if (pattern.preserveAspectRatio == null) {
                pattern.preserveAspectRatio = pRef.preserveAspectRatio;
            }
            if (pRef.href != null) {
                fillInChainedPatternFields(pattern, pRef.href);
            }
        }
    }

    private void renderMask(Mask mask, SvgElement obj) {
        float w;
        Mask mask2 = mask;
        SvgElement svgElement = obj;
        debug("Mask render", new Object[0]);
        boolean maskContentUnitsAreUser = true;
        boolean maskUnitsAreUser = mask2.maskUnitsAreUser != null && mask2.maskUnitsAreUser.booleanValue();
        float h;
        float floatValueX;
        float floatValueY;
        if (maskUnitsAreUser) {
            w = mask2.width != null ? mask2.width.floatValueX(this) : svgElement.boundingBox.width;
            h = mask2.height != null ? mask2.height.floatValueY(this) : svgElement.boundingBox.height;
            if (mask2.x != null) {
                floatValueX = mask2.x.floatValueX(this);
            } else {
                floatValueX = (float) (((double) svgElement.boundingBox.minX) - (((double) svgElement.boundingBox.width) * 0.1d));
            }
            if (mask2.y != null) {
                floatValueY = mask2.y.floatValueY(this);
            } else {
                floatValueY = (float) (((double) svgElement.boundingBox.minY) - (0.1d * ((double) svgElement.boundingBox.height)));
            }
        } else {
            h = -0.1f;
            w = mask2.x != null ? mask2.x.floatValue(this, 1.0f) : -0.1f;
            if (mask2.y != null) {
                h = mask2.y.floatValue(this, 1.0f);
            }
            floatValueY = 1.2f;
            floatValueX = mask2.width != null ? mask2.width.floatValue(this, 1.0f) : 1.2f;
            if (mask2.height != null) {
                floatValueY = mask2.height.floatValue(this, 1.0f);
            }
            float f = floatValueX * svgElement.boundingBox.width;
            floatValueX = svgElement.boundingBox.minX + (svgElement.boundingBox.width * w);
            w = f;
            float f2 = floatValueY * svgElement.boundingBox.height;
            floatValueY = svgElement.boundingBox.minY + (svgElement.boundingBox.height * h);
            h = f2;
        }
        if (w != 0.0f && h != 0.0f) {
            statePush();
            this.state = findInheritFromAncestorState(mask);
            this.state.style.opacity = Float.valueOf(1.0f);
            if (!(mask2.maskContentUnitsAreUser == null || mask2.maskContentUnitsAreUser.booleanValue())) {
                maskContentUnitsAreUser = false;
            }
            if (!maskContentUnitsAreUser) {
                this.canvas.translate(svgElement.boundingBox.minX, svgElement.boundingBox.minY);
                this.canvas.scale(svgElement.boundingBox.width, svgElement.boundingBox.height);
            }
            renderChildren(mask2, false);
            statePop();
        }
    }
}
