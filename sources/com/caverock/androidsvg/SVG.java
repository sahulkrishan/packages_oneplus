package com.caverock.androidsvg;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Picture;
import android.graphics.RectF;
import android.util.Log;
import com.caverock.androidsvg.CSSParser.Rule;
import com.caverock.androidsvg.CSSParser.Ruleset;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.xml.sax.SAXException;

public class SVG {
    private static final int DEFAULT_PICTURE_HEIGHT = 512;
    private static final int DEFAULT_PICTURE_WIDTH = 512;
    private static final List<SvgObject> EMPTY_CHILD_LIST = new ArrayList(0);
    protected static final long SPECIFIED_ALL = -1;
    protected static final long SPECIFIED_CLIP = 1048576;
    protected static final long SPECIFIED_CLIP_PATH = 268435456;
    protected static final long SPECIFIED_CLIP_RULE = 536870912;
    protected static final long SPECIFIED_COLOR = 4096;
    protected static final long SPECIFIED_DIRECTION = 68719476736L;
    protected static final long SPECIFIED_DISPLAY = 16777216;
    protected static final long SPECIFIED_FILL = 1;
    protected static final long SPECIFIED_FILL_OPACITY = 4;
    protected static final long SPECIFIED_FILL_RULE = 2;
    protected static final long SPECIFIED_FONT_FAMILY = 8192;
    protected static final long SPECIFIED_FONT_SIZE = 16384;
    protected static final long SPECIFIED_FONT_STYLE = 65536;
    protected static final long SPECIFIED_FONT_WEIGHT = 32768;
    protected static final long SPECIFIED_MARKER_END = 8388608;
    protected static final long SPECIFIED_MARKER_MID = 4194304;
    protected static final long SPECIFIED_MARKER_START = 2097152;
    protected static final long SPECIFIED_MASK = 1073741824;
    protected static final long SPECIFIED_NON_INHERITING = 68133849088L;
    protected static final long SPECIFIED_OPACITY = 2048;
    protected static final long SPECIFIED_OVERFLOW = 524288;
    protected static final long SPECIFIED_SOLID_COLOR = 2147483648L;
    protected static final long SPECIFIED_SOLID_OPACITY = 4294967296L;
    protected static final long SPECIFIED_STOP_COLOR = 67108864;
    protected static final long SPECIFIED_STOP_OPACITY = 134217728;
    protected static final long SPECIFIED_STROKE = 8;
    protected static final long SPECIFIED_STROKE_DASHARRAY = 512;
    protected static final long SPECIFIED_STROKE_DASHOFFSET = 1024;
    protected static final long SPECIFIED_STROKE_LINECAP = 64;
    protected static final long SPECIFIED_STROKE_LINEJOIN = 128;
    protected static final long SPECIFIED_STROKE_MITERLIMIT = 256;
    protected static final long SPECIFIED_STROKE_OPACITY = 16;
    protected static final long SPECIFIED_STROKE_WIDTH = 32;
    protected static final long SPECIFIED_TEXT_ANCHOR = 262144;
    protected static final long SPECIFIED_TEXT_DECORATION = 131072;
    protected static final long SPECIFIED_VECTOR_EFFECT = 34359738368L;
    protected static final long SPECIFIED_VIEWPORT_FILL = 8589934592L;
    protected static final long SPECIFIED_VIEWPORT_FILL_OPACITY = 17179869184L;
    protected static final long SPECIFIED_VISIBILITY = 33554432;
    private static final double SQRT2 = 1.414213562373095d;
    protected static final String SUPPORTED_SVG_VERSION = "1.2";
    private static final String TAG = "AndroidSVG";
    private static final String VERSION = "1.2.0";
    private Ruleset cssRules = new Ruleset();
    private String desc = "";
    private SVGExternalFileResolver fileResolver = null;
    private float renderDPI = 96.0f;
    private Svg rootElement = null;
    private String title = "";

    protected static class Box implements Cloneable {
        public float height;
        public float minX;
        public float minY;
        public float width;

        public Box(float minX, float minY, float width, float height) {
            this.minX = minX;
            this.minY = minY;
            this.width = width;
            this.height = height;
        }

        public static Box fromLimits(float minX, float minY, float maxX, float maxY) {
            return new Box(minX, minY, maxX - minX, maxY - minY);
        }

        public RectF toRectF() {
            return new RectF(this.minX, this.minY, maxX(), maxY());
        }

        public float maxX() {
            return this.minX + this.width;
        }

        public float maxY() {
            return this.minY + this.height;
        }

        public void union(Box other) {
            if (other.minX < this.minX) {
                this.minX = other.minX;
            }
            if (other.minY < this.minY) {
                this.minY = other.minY;
            }
            if (other.maxX() > maxX()) {
                this.width = other.maxX() - this.minX;
            }
            if (other.maxY() > maxY()) {
                this.height = other.maxY() - this.minY;
            }
        }

        public String toString() {
            StringBuilder stringBuilder = new StringBuilder("[");
            stringBuilder.append(this.minX);
            stringBuilder.append(" ");
            stringBuilder.append(this.minY);
            stringBuilder.append(" ");
            stringBuilder.append(this.width);
            stringBuilder.append(" ");
            stringBuilder.append(this.height);
            stringBuilder.append("]");
            return stringBuilder.toString();
        }
    }

    protected static class CSSClipRect {
        public Length bottom;
        public Length left;
        public Length right;
        public Length top;

        public CSSClipRect(Length top, Length right, Length bottom, Length left) {
            this.top = top;
            this.right = right;
            this.bottom = bottom;
            this.left = left;
        }
    }

    protected enum GradientSpread {
        pad,
        reflect,
        repeat
    }

    protected interface HasTransform {
        void setTransform(Matrix matrix);
    }

    protected static class Length implements Cloneable {
        private static /* synthetic */ int[] $SWITCH_TABLE$com$caverock$androidsvg$SVG$Unit;
        Unit unit = Unit.px;
        float value = 0.0f;

        static /* synthetic */ int[] $SWITCH_TABLE$com$caverock$androidsvg$SVG$Unit() {
            int[] iArr = $SWITCH_TABLE$com$caverock$androidsvg$SVG$Unit;
            if (iArr != null) {
                return iArr;
            }
            iArr = new int[Unit.values().length];
            try {
                iArr[Unit.cm.ordinal()] = 5;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[Unit.em.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[Unit.ex.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[Unit.in.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[Unit.mm.ordinal()] = 6;
            } catch (NoSuchFieldError e5) {
            }
            try {
                iArr[Unit.pc.ordinal()] = 8;
            } catch (NoSuchFieldError e6) {
            }
            try {
                iArr[Unit.percent.ordinal()] = 9;
            } catch (NoSuchFieldError e7) {
            }
            try {
                iArr[Unit.pt.ordinal()] = 7;
            } catch (NoSuchFieldError e8) {
            }
            try {
                iArr[Unit.px.ordinal()] = 1;
            } catch (NoSuchFieldError e9) {
            }
            $SWITCH_TABLE$com$caverock$androidsvg$SVG$Unit = iArr;
            return iArr;
        }

        public Length(float value, Unit unit) {
            this.value = value;
            this.unit = unit;
        }

        public Length(float value) {
            this.value = value;
            this.unit = Unit.px;
        }

        public float floatValue() {
            return this.value;
        }

        public float floatValueX(SVGAndroidRenderer renderer) {
            switch ($SWITCH_TABLE$com$caverock$androidsvg$SVG$Unit()[this.unit.ordinal()]) {
                case 1:
                    return this.value;
                case 2:
                    return this.value * renderer.getCurrentFontSize();
                case 3:
                    return this.value * renderer.getCurrentFontXHeight();
                case 4:
                    return this.value * renderer.getDPI();
                case 5:
                    return (this.value * renderer.getDPI()) / 2.54f;
                case 6:
                    return (this.value * renderer.getDPI()) / 25.4f;
                case 7:
                    return (this.value * renderer.getDPI()) / 72.0f;
                case 8:
                    return (this.value * renderer.getDPI()) / 6.0f;
                case 9:
                    Box viewPortUser = renderer.getCurrentViewPortInUserUnits();
                    if (viewPortUser == null) {
                        return this.value;
                    }
                    return (this.value * viewPortUser.width) / 100.0f;
                default:
                    return this.value;
            }
        }

        public float floatValueY(SVGAndroidRenderer renderer) {
            if (this.unit != Unit.percent) {
                return floatValueX(renderer);
            }
            Box viewPortUser = renderer.getCurrentViewPortInUserUnits();
            if (viewPortUser == null) {
                return this.value;
            }
            return (this.value * viewPortUser.height) / 100.0f;
        }

        public float floatValue(SVGAndroidRenderer renderer) {
            if (this.unit != Unit.percent) {
                return floatValueX(renderer);
            }
            Box viewPortUser = renderer.getCurrentViewPortInUserUnits();
            if (viewPortUser == null) {
                return this.value;
            }
            float w = viewPortUser.width;
            float h = viewPortUser.height;
            if (w == h) {
                return (this.value * w) / 100.0f;
            }
            return (this.value * ((float) (Math.sqrt((double) ((w * w) + (h * h))) / SVG.SQRT2))) / 100.0f;
        }

        public float floatValue(SVGAndroidRenderer renderer, float max) {
            if (this.unit == Unit.percent) {
                return (this.value * max) / 100.0f;
            }
            return floatValueX(renderer);
        }

        public float floatValue(float dpi) {
            int i = $SWITCH_TABLE$com$caverock$androidsvg$SVG$Unit()[this.unit.ordinal()];
            if (i == 1) {
                return this.value;
            }
            switch (i) {
                case 4:
                    return this.value * dpi;
                case 5:
                    return (this.value * dpi) / 2.54f;
                case 6:
                    return (this.value * dpi) / 25.4f;
                case 7:
                    return (this.value * dpi) / 72.0f;
                case 8:
                    return (this.value * dpi) / 6.0f;
                default:
                    return this.value;
            }
        }

        public boolean isZero() {
            return this.value == 0.0f;
        }

        public boolean isNegative() {
            return this.value < 0.0f;
        }

        public String toString() {
            StringBuilder stringBuilder = new StringBuilder(String.valueOf(String.valueOf(this.value)));
            stringBuilder.append(this.unit);
            return stringBuilder.toString();
        }
    }

    protected interface NotDirectlyRendered {
    }

    protected interface PathInterface {
        void arcTo(float f, float f2, float f3, boolean z, boolean z2, float f4, float f5);

        void close();

        void cubicTo(float f, float f2, float f3, float f4, float f5, float f6);

        void lineTo(float f, float f2);

        void moveTo(float f, float f2);

        void quadTo(float f, float f2, float f3, float f4);
    }

    protected static class Style implements Cloneable {
        public static final int FONT_WEIGHT_BOLD = 700;
        public static final int FONT_WEIGHT_BOLDER = 1;
        public static final int FONT_WEIGHT_LIGHTER = -1;
        public static final int FONT_WEIGHT_NORMAL = 400;
        public CSSClipRect clip;
        public String clipPath;
        public FillRule clipRule;
        public Colour color;
        public TextDirection direction;
        public Boolean display;
        public SvgPaint fill;
        public Float fillOpacity;
        public FillRule fillRule;
        public List<String> fontFamily;
        public Length fontSize;
        public FontStyle fontStyle;
        public Integer fontWeight;
        public String markerEnd;
        public String markerMid;
        public String markerStart;
        public String mask;
        public Float opacity;
        public Boolean overflow;
        public SvgPaint solidColor;
        public Float solidOpacity;
        public long specifiedFlags = 0;
        public SvgPaint stopColor;
        public Float stopOpacity;
        public SvgPaint stroke;
        public Length[] strokeDashArray;
        public Length strokeDashOffset;
        public LineCaps strokeLineCap;
        public LineJoin strokeLineJoin;
        public Float strokeMiterLimit;
        public Float strokeOpacity;
        public Length strokeWidth;
        public TextAnchor textAnchor;
        public TextDecoration textDecoration;
        public VectorEffect vectorEffect;
        public SvgPaint viewportFill;
        public Float viewportFillOpacity;
        public Boolean visibility;

        public enum FillRule {
            NonZero,
            EvenOdd
        }

        public enum FontStyle {
            Normal,
            Italic,
            Oblique
        }

        public enum LineCaps {
            Butt,
            Round,
            Square
        }

        public enum LineJoin {
            Miter,
            Round,
            Bevel
        }

        public enum TextAnchor {
            Start,
            Middle,
            End
        }

        public enum TextDecoration {
            None,
            Underline,
            Overline,
            LineThrough,
            Blink
        }

        public enum TextDirection {
            LTR,
            RTL
        }

        public enum VectorEffect {
            None,
            NonScalingStroke
        }

        protected Style() {
        }

        public static Style getDefaultStyle() {
            Style def = new Style();
            def.specifiedFlags = -1;
            def.fill = Colour.BLACK;
            def.fillRule = FillRule.NonZero;
            def.fillOpacity = Float.valueOf(1.0f);
            def.stroke = null;
            def.strokeOpacity = Float.valueOf(1.0f);
            def.strokeWidth = new Length(1.0f);
            def.strokeLineCap = LineCaps.Butt;
            def.strokeLineJoin = LineJoin.Miter;
            def.strokeMiterLimit = Float.valueOf(4.0f);
            def.strokeDashArray = null;
            def.strokeDashOffset = new Length(0.0f);
            def.opacity = Float.valueOf(1.0f);
            def.color = Colour.BLACK;
            def.fontFamily = null;
            def.fontSize = new Length(12.0f, Unit.pt);
            def.fontWeight = Integer.valueOf(400);
            def.fontStyle = FontStyle.Normal;
            def.textDecoration = TextDecoration.None;
            def.direction = TextDirection.LTR;
            def.textAnchor = TextAnchor.Start;
            def.overflow = Boolean.valueOf(true);
            def.clip = null;
            def.markerStart = null;
            def.markerMid = null;
            def.markerEnd = null;
            def.display = Boolean.TRUE;
            def.visibility = Boolean.TRUE;
            def.stopColor = Colour.BLACK;
            def.stopOpacity = Float.valueOf(1.0f);
            def.clipPath = null;
            def.clipRule = FillRule.NonZero;
            def.mask = null;
            def.solidColor = null;
            def.solidOpacity = Float.valueOf(1.0f);
            def.viewportFill = null;
            def.viewportFillOpacity = Float.valueOf(1.0f);
            def.vectorEffect = VectorEffect.None;
            return def;
        }

        public void resetNonInheritingProperties() {
            resetNonInheritingProperties(false);
        }

        public void resetNonInheritingProperties(boolean isRootSVG) {
            this.display = Boolean.TRUE;
            this.overflow = isRootSVG ? Boolean.TRUE : Boolean.FALSE;
            this.clip = null;
            this.clipPath = null;
            this.opacity = Float.valueOf(1.0f);
            this.stopColor = Colour.BLACK;
            this.stopOpacity = Float.valueOf(1.0f);
            this.mask = null;
            this.solidColor = null;
            this.solidOpacity = Float.valueOf(1.0f);
            this.viewportFill = null;
            this.viewportFillOpacity = Float.valueOf(1.0f);
            this.vectorEffect = VectorEffect.None;
        }

        /* Access modifiers changed, original: protected */
        public Object clone() {
            try {
                Style obj = (Style) super.clone();
                if (this.strokeDashArray != null) {
                    obj.strokeDashArray = (Length[]) this.strokeDashArray.clone();
                }
                return obj;
            } catch (CloneNotSupportedException e) {
                throw new InternalError(e.toString());
            }
        }
    }

    protected interface SvgConditional {
        String getRequiredExtensions();

        Set<String> getRequiredFeatures();

        Set<String> getRequiredFonts();

        Set<String> getRequiredFormats();

        Set<String> getSystemLanguage();

        void setRequiredExtensions(String str);

        void setRequiredFeatures(Set<String> set);

        void setRequiredFonts(Set<String> set);

        void setRequiredFormats(Set<String> set);

        void setSystemLanguage(Set<String> set);
    }

    protected interface SvgContainer {
        void addChild(SvgObject svgObject) throws SAXException;

        List<SvgObject> getChildren();
    }

    protected static class SvgObject {
        public SVG document;
        public SvgContainer parent;

        protected SvgObject() {
        }

        public String toString() {
            return getClass().getSimpleName();
        }
    }

    protected static abstract class SvgPaint implements Cloneable {
        protected SvgPaint() {
        }
    }

    protected interface TextChild {
        TextRoot getTextRoot();

        void setTextRoot(TextRoot textRoot);
    }

    protected interface TextRoot {
    }

    protected enum Unit {
        px,
        em,
        ex,
        in,
        cm,
        mm,
        pt,
        pc,
        percent
    }

    protected static class Colour extends SvgPaint {
        public static final Colour BLACK = new Colour(0);
        public int colour;

        public Colour(int val) {
            this.colour = val;
        }

        public String toString() {
            return String.format("#%06x", new Object[]{Integer.valueOf(this.colour)});
        }
    }

    protected static class CurrentColor extends SvgPaint {
        private static CurrentColor instance = new CurrentColor();

        private CurrentColor() {
        }

        public static CurrentColor getInstance() {
            return instance;
        }
    }

    protected static class PaintReference extends SvgPaint {
        public SvgPaint fallback;
        public String href;

        public PaintReference(String href, SvgPaint fallback) {
            this.href = href;
            this.fallback = fallback;
        }

        public String toString() {
            StringBuilder stringBuilder = new StringBuilder(String.valueOf(this.href));
            stringBuilder.append(" ");
            stringBuilder.append(this.fallback);
            return stringBuilder.toString();
        }
    }

    protected static class PathDefinition implements PathInterface {
        private static final byte ARCTO = (byte) 4;
        private static final byte CLOSE = (byte) 8;
        private static final byte CUBICTO = (byte) 2;
        private static final byte LINETO = (byte) 1;
        private static final byte MOVETO = (byte) 0;
        private static final byte QUADTO = (byte) 3;
        private List<Byte> commands;
        private List<Float> coords;

        public PathDefinition() {
            this.commands = null;
            this.coords = null;
            this.commands = new ArrayList();
            this.coords = new ArrayList();
        }

        public boolean isEmpty() {
            return this.commands.isEmpty();
        }

        public void moveTo(float x, float y) {
            this.commands.add(Byte.valueOf((byte) 0));
            this.coords.add(Float.valueOf(x));
            this.coords.add(Float.valueOf(y));
        }

        public void lineTo(float x, float y) {
            this.commands.add(Byte.valueOf((byte) 1));
            this.coords.add(Float.valueOf(x));
            this.coords.add(Float.valueOf(y));
        }

        public void cubicTo(float x1, float y1, float x2, float y2, float x3, float y3) {
            this.commands.add(Byte.valueOf((byte) 2));
            this.coords.add(Float.valueOf(x1));
            this.coords.add(Float.valueOf(y1));
            this.coords.add(Float.valueOf(x2));
            this.coords.add(Float.valueOf(y2));
            this.coords.add(Float.valueOf(x3));
            this.coords.add(Float.valueOf(y3));
        }

        public void quadTo(float x1, float y1, float x2, float y2) {
            this.commands.add(Byte.valueOf((byte) 3));
            this.coords.add(Float.valueOf(x1));
            this.coords.add(Float.valueOf(y1));
            this.coords.add(Float.valueOf(x2));
            this.coords.add(Float.valueOf(y2));
        }

        public void arcTo(float rx, float ry, float xAxisRotation, boolean largeArcFlag, boolean sweepFlag, float x, float y) {
            this.commands.add(Byte.valueOf((byte) (((largeArcFlag ? 2 : 0) | 4) | sweepFlag)));
            this.coords.add(Float.valueOf(rx));
            this.coords.add(Float.valueOf(ry));
            this.coords.add(Float.valueOf(xAxisRotation));
            this.coords.add(Float.valueOf(x));
            this.coords.add(Float.valueOf(y));
        }

        public void close() {
            this.commands.add(Byte.valueOf((byte) 8));
        }

        public void enumeratePath(PathInterface handler) {
            Iterator<Float> coordsIter = this.coords.iterator();
            for (Byte command : this.commands) {
                byte command2 = command.byteValue();
                if (command2 != (byte) 8) {
                    switch (command2) {
                        case (byte) 0:
                            handler.moveTo(((Float) coordsIter.next()).floatValue(), ((Float) coordsIter.next()).floatValue());
                            break;
                        case (byte) 1:
                            handler.lineTo(((Float) coordsIter.next()).floatValue(), ((Float) coordsIter.next()).floatValue());
                            break;
                        case (byte) 2:
                            handler.cubicTo(((Float) coordsIter.next()).floatValue(), ((Float) coordsIter.next()).floatValue(), ((Float) coordsIter.next()).floatValue(), ((Float) coordsIter.next()).floatValue(), ((Float) coordsIter.next()).floatValue(), ((Float) coordsIter.next()).floatValue());
                            break;
                        case (byte) 3:
                            handler.quadTo(((Float) coordsIter.next()).floatValue(), ((Float) coordsIter.next()).floatValue(), ((Float) coordsIter.next()).floatValue(), ((Float) coordsIter.next()).floatValue());
                            break;
                        default:
                            handler.arcTo(((Float) coordsIter.next()).floatValue(), ((Float) coordsIter.next()).floatValue(), ((Float) coordsIter.next()).floatValue(), (command2 & 2) != 0, (command2 & 1) != 0, ((Float) coordsIter.next()).floatValue(), ((Float) coordsIter.next()).floatValue());
                            break;
                    }
                }
                handler.close();
            }
        }
    }

    protected static class SvgElementBase extends SvgObject {
        public Style baseStyle = null;
        public List<String> classNames = null;
        public String id = null;
        public Boolean spacePreserve = null;
        public Style style = null;

        protected SvgElementBase() {
        }
    }

    protected static class TextSequence extends SvgObject implements TextChild {
        public String text;
        private TextRoot textRoot;

        public TextSequence(String text) {
            this.text = text;
        }

        public String toString() {
            StringBuilder stringBuilder = new StringBuilder(String.valueOf(getClass().getSimpleName()));
            stringBuilder.append(" '");
            stringBuilder.append(this.text);
            stringBuilder.append("'");
            return stringBuilder.toString();
        }

        public void setTextRoot(TextRoot obj) {
            this.textRoot = obj;
        }

        public TextRoot getTextRoot() {
            return this.textRoot;
        }
    }

    protected static class GradientElement extends SvgElementBase implements SvgContainer {
        public List<SvgObject> children = new ArrayList();
        public Matrix gradientTransform;
        public Boolean gradientUnitsAreUser;
        public String href;
        public GradientSpread spreadMethod;

        protected GradientElement() {
        }

        public List<SvgObject> getChildren() {
            return this.children;
        }

        public void addChild(SvgObject elem) throws SAXException {
            if (elem instanceof Stop) {
                this.children.add(elem);
                return;
            }
            StringBuilder stringBuilder = new StringBuilder("Gradient elements cannot contain ");
            stringBuilder.append(elem);
            stringBuilder.append(" elements.");
            throw new SAXException(stringBuilder.toString());
        }
    }

    protected static class SolidColor extends SvgElementBase implements SvgContainer {
        public Length solidColor;
        public Length solidOpacity;

        protected SolidColor() {
        }

        public List<SvgObject> getChildren() {
            return SVG.EMPTY_CHILD_LIST;
        }

        public void addChild(SvgObject elem) throws SAXException {
        }
    }

    protected static class Stop extends SvgElementBase implements SvgContainer {
        public Float offset;

        protected Stop() {
        }

        public List<SvgObject> getChildren() {
            return SVG.EMPTY_CHILD_LIST;
        }

        public void addChild(SvgObject elem) throws SAXException {
        }
    }

    protected static class SvgElement extends SvgElementBase {
        public Box boundingBox = null;

        protected SvgElement() {
        }
    }

    protected static class SvgConditionalContainer extends SvgElement implements SvgContainer, SvgConditional {
        public List<SvgObject> children = new ArrayList();
        public String requiredExtensions = null;
        public Set<String> requiredFeatures = null;
        public Set<String> requiredFonts = null;
        public Set<String> requiredFormats = null;
        public Set<String> systemLanguage = null;

        protected SvgConditionalContainer() {
        }

        public List<SvgObject> getChildren() {
            return this.children;
        }

        public void addChild(SvgObject elem) throws SAXException {
            this.children.add(elem);
        }

        public void setRequiredFeatures(Set<String> features) {
            this.requiredFeatures = features;
        }

        public Set<String> getRequiredFeatures() {
            return this.requiredFeatures;
        }

        public void setRequiredExtensions(String extensions) {
            this.requiredExtensions = extensions;
        }

        public String getRequiredExtensions() {
            return this.requiredExtensions;
        }

        public void setSystemLanguage(Set<String> languages) {
            this.systemLanguage = languages;
        }

        public Set<String> getSystemLanguage() {
            return null;
        }

        public void setRequiredFormats(Set<String> mimeTypes) {
            this.requiredFormats = mimeTypes;
        }

        public Set<String> getRequiredFormats() {
            return this.requiredFormats;
        }

        public void setRequiredFonts(Set<String> fontNames) {
            this.requiredFonts = fontNames;
        }

        public Set<String> getRequiredFonts() {
            return this.requiredFonts;
        }
    }

    protected static class SvgConditionalElement extends SvgElement implements SvgConditional {
        public String requiredExtensions = null;
        public Set<String> requiredFeatures = null;
        public Set<String> requiredFonts = null;
        public Set<String> requiredFormats = null;
        public Set<String> systemLanguage = null;

        protected SvgConditionalElement() {
        }

        public void setRequiredFeatures(Set<String> features) {
            this.requiredFeatures = features;
        }

        public Set<String> getRequiredFeatures() {
            return this.requiredFeatures;
        }

        public void setRequiredExtensions(String extensions) {
            this.requiredExtensions = extensions;
        }

        public String getRequiredExtensions() {
            return this.requiredExtensions;
        }

        public void setSystemLanguage(Set<String> languages) {
            this.systemLanguage = languages;
        }

        public Set<String> getSystemLanguage() {
            return this.systemLanguage;
        }

        public void setRequiredFormats(Set<String> mimeTypes) {
            this.requiredFormats = mimeTypes;
        }

        public Set<String> getRequiredFormats() {
            return this.requiredFormats;
        }

        public void setRequiredFonts(Set<String> fontNames) {
            this.requiredFonts = fontNames;
        }

        public Set<String> getRequiredFonts() {
            return this.requiredFonts;
        }
    }

    protected static class SvgLinearGradient extends GradientElement {
        public Length x1;
        public Length x2;
        public Length y1;
        public Length y2;

        protected SvgLinearGradient() {
        }
    }

    protected static class SvgRadialGradient extends GradientElement {
        public Length cx;
        public Length cy;
        public Length fx;
        public Length fy;
        public Length r;

        protected SvgRadialGradient() {
        }
    }

    protected static abstract class GraphicsElement extends SvgConditionalElement implements HasTransform {
        public Matrix transform;

        protected GraphicsElement() {
        }

        public void setTransform(Matrix transform) {
            this.transform = transform;
        }
    }

    protected static class Group extends SvgConditionalContainer implements HasTransform {
        public Matrix transform;

        protected Group() {
        }

        public void setTransform(Matrix transform) {
            this.transform = transform;
        }
    }

    protected static class Mask extends SvgConditionalContainer implements NotDirectlyRendered {
        public Length height;
        public Boolean maskContentUnitsAreUser;
        public Boolean maskUnitsAreUser;
        public Length width;
        public Length x;
        public Length y;

        protected Mask() {
        }
    }

    protected static class SvgPreserveAspectRatioContainer extends SvgConditionalContainer {
        public PreserveAspectRatio preserveAspectRatio = null;

        protected SvgPreserveAspectRatioContainer() {
        }
    }

    protected static class TextContainer extends SvgConditionalContainer {
        protected TextContainer() {
        }

        public void addChild(SvgObject elem) throws SAXException {
            if (elem instanceof TextChild) {
                this.children.add(elem);
                return;
            }
            StringBuilder stringBuilder = new StringBuilder("Text content elements cannot contain ");
            stringBuilder.append(elem);
            stringBuilder.append(" elements.");
            throw new SAXException(stringBuilder.toString());
        }
    }

    protected static class Circle extends GraphicsElement {
        public Length cx;
        public Length cy;
        public Length r;

        protected Circle() {
        }
    }

    protected static class ClipPath extends Group implements NotDirectlyRendered {
        public Boolean clipPathUnitsAreUser;

        protected ClipPath() {
        }
    }

    protected static class Defs extends Group implements NotDirectlyRendered {
        protected Defs() {
        }
    }

    protected static class Ellipse extends GraphicsElement {
        public Length cx;
        public Length cy;
        public Length rx;
        public Length ry;

        protected Ellipse() {
        }
    }

    protected static class Image extends SvgPreserveAspectRatioContainer implements HasTransform {
        public Length height;
        public String href;
        public Matrix transform;
        public Length width;
        public Length x;
        public Length y;

        protected Image() {
        }

        public void setTransform(Matrix transform) {
            this.transform = transform;
        }
    }

    protected static class Line extends GraphicsElement {
        public Length x1;
        public Length x2;
        public Length y1;
        public Length y2;

        protected Line() {
        }
    }

    protected static class Path extends GraphicsElement {
        public PathDefinition d;
        public Float pathLength;

        protected Path() {
        }
    }

    protected static class PolyLine extends GraphicsElement {
        public float[] points;

        protected PolyLine() {
        }
    }

    protected static class Rect extends GraphicsElement {
        public Length height;
        public Length rx;
        public Length ry;
        public Length width;
        public Length x;
        public Length y;

        protected Rect() {
        }
    }

    protected static class SvgViewBoxContainer extends SvgPreserveAspectRatioContainer {
        public Box viewBox;

        protected SvgViewBoxContainer() {
        }
    }

    protected static class Switch extends Group {
        protected Switch() {
        }
    }

    protected static class TRef extends TextContainer implements TextChild {
        public String href;
        private TextRoot textRoot;

        protected TRef() {
        }

        public void setTextRoot(TextRoot obj) {
            this.textRoot = obj;
        }

        public TextRoot getTextRoot() {
            return this.textRoot;
        }
    }

    protected static class TextPath extends TextContainer implements TextChild {
        public String href;
        public Length startOffset;
        private TextRoot textRoot;

        protected TextPath() {
        }

        public void setTextRoot(TextRoot obj) {
            this.textRoot = obj;
        }

        public TextRoot getTextRoot() {
            return this.textRoot;
        }
    }

    protected static class TextPositionedContainer extends TextContainer {
        public List<Length> dx;
        public List<Length> dy;
        public List<Length> x;
        public List<Length> y;

        protected TextPositionedContainer() {
        }
    }

    protected static class Use extends Group {
        public Length height;
        public String href;
        public Length width;
        public Length x;
        public Length y;

        protected Use() {
        }
    }

    protected static class Marker extends SvgViewBoxContainer implements NotDirectlyRendered {
        public Length markerHeight;
        public boolean markerUnitsAreUser;
        public Length markerWidth;
        public Float orient;
        public Length refX;
        public Length refY;

        protected Marker() {
        }
    }

    protected static class Pattern extends SvgViewBoxContainer implements NotDirectlyRendered {
        public Length height;
        public String href;
        public Boolean patternContentUnitsAreUser;
        public Matrix patternTransform;
        public Boolean patternUnitsAreUser;
        public Length width;
        public Length x;
        public Length y;

        protected Pattern() {
        }
    }

    protected static class Polygon extends PolyLine {
        protected Polygon() {
        }
    }

    protected static class Svg extends SvgViewBoxContainer {
        public Length height;
        public String version;
        public Length width;
        public Length x;
        public Length y;

        protected Svg() {
        }
    }

    protected static class Symbol extends SvgViewBoxContainer implements NotDirectlyRendered {
        protected Symbol() {
        }
    }

    protected static class TSpan extends TextPositionedContainer implements TextChild {
        private TextRoot textRoot;

        protected TSpan() {
        }

        public void setTextRoot(TextRoot obj) {
            this.textRoot = obj;
        }

        public TextRoot getTextRoot() {
            return this.textRoot;
        }
    }

    protected static class Text extends TextPositionedContainer implements TextRoot, HasTransform {
        public Matrix transform;

        protected Text() {
        }

        public void setTransform(Matrix transform) {
            this.transform = transform;
        }
    }

    protected static class View extends SvgViewBoxContainer implements NotDirectlyRendered {
        protected View() {
        }
    }

    protected SVG() {
    }

    public static SVG getFromInputStream(InputStream is) throws SVGParseException {
        return new SVGParser().parse(is);
    }

    public static SVG getFromString(String svg) throws SVGParseException {
        return new SVGParser().parse(new ByteArrayInputStream(svg.getBytes()));
    }

    public static SVG getFromResource(Context context, int resourceId) throws SVGParseException {
        return new SVGParser().parse(context.getResources().openRawResource(resourceId));
    }

    public static SVG getFromAsset(AssetManager assetManager, String filename) throws SVGParseException, IOException {
        SVGParser parser = new SVGParser();
        InputStream is = assetManager.open(filename);
        SVG svg = parser.parse(is);
        is.close();
        return svg;
    }

    public void registerExternalFileResolver(SVGExternalFileResolver fileResolver) {
        this.fileResolver = fileResolver;
    }

    public void setRenderDPI(float dpi) {
        this.renderDPI = dpi;
    }

    public float getRenderDPI() {
        return this.renderDPI;
    }

    public Picture renderToPicture() {
        Length width = this.rootElement.width;
        if (width == null) {
            return renderToPicture(512, 512);
        }
        float h;
        float w = width.floatValue(this.renderDPI);
        Box rootViewBox = this.rootElement.viewBox;
        if (rootViewBox != null) {
            h = (rootViewBox.height * w) / rootViewBox.width;
        } else {
            Length height = this.rootElement.height;
            if (height != null) {
                h = height.floatValue(this.renderDPI);
            } else {
                h = w;
            }
        }
        return renderToPicture((int) Math.ceil((double) w), (int) Math.ceil((double) h));
    }

    public Picture renderToPicture(int widthInPixels, int heightInPixels) {
        Picture picture = new Picture();
        new SVGAndroidRenderer(picture.beginRecording(widthInPixels, heightInPixels), new Box(0.0f, 0.0f, (float) widthInPixels, (float) heightInPixels), this.renderDPI).renderDocument(this, null, null, false);
        picture.endRecording();
        return picture;
    }

    public Picture renderViewToPicture(String viewId, int widthInPixels, int heightInPixels) {
        SvgObject obj = getElementById(viewId);
        if (obj == null || !(obj instanceof View)) {
            return null;
        }
        View view = (View) obj;
        if (view.viewBox == null) {
            Log.w(TAG, "View element is missing a viewBox attribute.");
            return null;
        }
        Picture picture = new Picture();
        new SVGAndroidRenderer(picture.beginRecording(widthInPixels, heightInPixels), new Box(0.0f, 0.0f, (float) widthInPixels, (float) heightInPixels), this.renderDPI).renderDocument(this, view.viewBox, view.preserveAspectRatio, false);
        picture.endRecording();
        return picture;
    }

    public void renderToCanvas(Canvas canvas) {
        renderToCanvas(canvas, null);
    }

    public void renderToCanvas(Canvas canvas, RectF viewPort) {
        Box svgViewPort;
        if (viewPort != null) {
            svgViewPort = Box.fromLimits(viewPort.left, viewPort.top, viewPort.right, viewPort.bottom);
        } else {
            svgViewPort = new Box(0.0f, 0.0f, (float) canvas.getWidth(), (float) canvas.getHeight());
        }
        new SVGAndroidRenderer(canvas, svgViewPort, this.renderDPI).renderDocument(this, null, null, true);
    }

    public void renderViewToCanvas(String viewId, Canvas canvas) {
        renderViewToCanvas(viewId, canvas, null);
    }

    public void renderViewToCanvas(String viewId, Canvas canvas, RectF viewPort) {
        SvgObject obj = getElementById(viewId);
        if (obj != null && (obj instanceof View)) {
            View view = (View) obj;
            if (view.viewBox == null) {
                Log.w(TAG, "View element is missing a viewBox attribute.");
                return;
            }
            Box svgViewPort;
            if (viewPort != null) {
                svgViewPort = Box.fromLimits(viewPort.left, viewPort.top, viewPort.right, viewPort.bottom);
            } else {
                svgViewPort = new Box(0.0f, 0.0f, (float) canvas.getWidth(), (float) canvas.getHeight());
            }
            new SVGAndroidRenderer(canvas, svgViewPort, this.renderDPI).renderDocument(this, view.viewBox, view.preserveAspectRatio, true);
        }
    }

    public String getVersion() {
        return VERSION;
    }

    public String getDocumentTitle() {
        if (this.rootElement != null) {
            return this.title;
        }
        throw new IllegalArgumentException("SVG document is empty");
    }

    public String getDocumentDescription() {
        if (this.rootElement != null) {
            return this.desc;
        }
        throw new IllegalArgumentException("SVG document is empty");
    }

    public String getDocumentSVGVersion() {
        if (this.rootElement != null) {
            return this.rootElement.version;
        }
        throw new IllegalArgumentException("SVG document is empty");
    }

    public Set<String> getViewList() {
        if (this.rootElement != null) {
            List<SvgObject> viewElems = getElementsByTagName(View.class);
            Set<String> viewIds = new HashSet(viewElems.size());
            for (SvgObject elem : viewElems) {
                View view = (View) elem;
                if (view.id != null) {
                    viewIds.add(view.id);
                } else {
                    Log.w(TAG, "getViewList(): found a <view> without an id attribute");
                }
            }
            return viewIds;
        }
        throw new IllegalArgumentException("SVG document is empty");
    }

    public float getDocumentWidth() {
        if (this.rootElement != null) {
            return getDocumentDimensions(this.renderDPI).width;
        }
        throw new IllegalArgumentException("SVG document is empty");
    }

    public void setDocumentWidth(float pixels) {
        if (this.rootElement != null) {
            this.rootElement.width = new Length(pixels);
            return;
        }
        throw new IllegalArgumentException("SVG document is empty");
    }

    public void setDocumentWidth(String value) throws SVGParseException {
        if (this.rootElement != null) {
            try {
                this.rootElement.width = SVGParser.parseLength(value);
                return;
            } catch (SAXException e) {
                throw new SVGParseException(e.getMessage());
            }
        }
        throw new IllegalArgumentException("SVG document is empty");
    }

    public float getDocumentHeight() {
        if (this.rootElement != null) {
            return getDocumentDimensions(this.renderDPI).height;
        }
        throw new IllegalArgumentException("SVG document is empty");
    }

    public void setDocumentHeight(float pixels) {
        if (this.rootElement != null) {
            this.rootElement.height = new Length(pixels);
            return;
        }
        throw new IllegalArgumentException("SVG document is empty");
    }

    public void setDocumentHeight(String value) throws SVGParseException {
        if (this.rootElement != null) {
            try {
                this.rootElement.height = SVGParser.parseLength(value);
                return;
            } catch (SAXException e) {
                throw new SVGParseException(e.getMessage());
            }
        }
        throw new IllegalArgumentException("SVG document is empty");
    }

    public void setDocumentViewBox(float minX, float minY, float width, float height) {
        if (this.rootElement != null) {
            this.rootElement.viewBox = new Box(minX, minY, width, height);
            return;
        }
        throw new IllegalArgumentException("SVG document is empty");
    }

    public RectF getDocumentViewBox() {
        if (this.rootElement == null) {
            throw new IllegalArgumentException("SVG document is empty");
        } else if (this.rootElement.viewBox == null) {
            return null;
        } else {
            return this.rootElement.viewBox.toRectF();
        }
    }

    public void setDocumentPreserveAspectRatio(PreserveAspectRatio preserveAspectRatio) {
        if (this.rootElement != null) {
            this.rootElement.preserveAspectRatio = preserveAspectRatio;
            return;
        }
        throw new IllegalArgumentException("SVG document is empty");
    }

    public PreserveAspectRatio getDocumentPreserveAspectRatio() {
        if (this.rootElement == null) {
            throw new IllegalArgumentException("SVG document is empty");
        } else if (this.rootElement.preserveAspectRatio == null) {
            return null;
        } else {
            return this.rootElement.preserveAspectRatio;
        }
    }

    public float getDocumentAspectRatio() {
        if (this.rootElement != null) {
            Length w = this.rootElement.width;
            Length h = this.rootElement.height;
            if (w == null || h == null || w.unit == Unit.percent || h.unit == Unit.percent) {
                if (this.rootElement.viewBox == null || this.rootElement.viewBox.width == 0.0f || this.rootElement.viewBox.height == 0.0f) {
                    return -1.0f;
                }
                return this.rootElement.viewBox.width / this.rootElement.viewBox.height;
            } else if (w.isZero() || h.isZero()) {
                return -1.0f;
            } else {
                return w.floatValue(this.renderDPI) / h.floatValue(this.renderDPI);
            }
        }
        throw new IllegalArgumentException("SVG document is empty");
    }

    /* Access modifiers changed, original: protected */
    public Svg getRootElement() {
        return this.rootElement;
    }

    /* Access modifiers changed, original: protected */
    public void setRootElement(Svg rootElement) {
        this.rootElement = rootElement;
    }

    /* Access modifiers changed, original: protected */
    public SvgObject resolveIRI(String iri) {
        if (iri != null && iri.length() > 1 && iri.startsWith("#")) {
            return getElementById(iri.substring(1));
        }
        return null;
    }

    private Box getDocumentDimensions(float dpi) {
        Length w = this.rootElement.width;
        Length h = this.rootElement.height;
        if (w == null || w.isZero() || w.unit == Unit.percent || w.unit == Unit.em || w.unit == Unit.ex) {
            return new Box(-1.0f, -1.0f, -1.0f, -1.0f);
        }
        float hOut;
        float wOut = w.floatValue(dpi);
        if (h != null) {
            if (h.isZero() || h.unit == Unit.percent || h.unit == Unit.em || h.unit == Unit.ex) {
                return new Box(-1.0f, -1.0f, -1.0f, -1.0f);
            }
            hOut = h.floatValue(dpi);
        } else if (this.rootElement.viewBox != null) {
            hOut = (this.rootElement.viewBox.height * wOut) / this.rootElement.viewBox.width;
        } else {
            hOut = wOut;
        }
        return new Box(0.0f, 0.0f, wOut, hOut);
    }

    /* Access modifiers changed, original: protected */
    public void addCSSRules(Ruleset ruleset) {
        this.cssRules.addAll(ruleset);
    }

    /* Access modifiers changed, original: protected */
    public List<Rule> getCSSRules() {
        return this.cssRules.getRules();
    }

    /* Access modifiers changed, original: protected */
    public boolean hasCSSRules() {
        return this.cssRules.isEmpty() ^ 1;
    }

    /* Access modifiers changed, original: protected */
    public void setTitle(String title) {
        this.title = title;
    }

    /* Access modifiers changed, original: protected */
    public void setDesc(String desc) {
        this.desc = desc;
    }

    /* Access modifiers changed, original: protected */
    public SVGExternalFileResolver getFileResolver() {
        return this.fileResolver;
    }

    /* Access modifiers changed, original: protected */
    public SvgObject getElementById(String id) {
        if (id.equals(this.rootElement.id)) {
            return this.rootElement;
        }
        return getElementById(this.rootElement, id);
    }

    private SvgElementBase getElementById(SvgContainer obj, String id) {
        SvgElementBase elem = (SvgElementBase) obj;
        if (id.equals(elem.id)) {
            return elem;
        }
        for (SvgObject child : obj.getChildren()) {
            if (child instanceof SvgElementBase) {
                SvgElementBase childElem = (SvgElementBase) child;
                if (id.equals(childElem.id)) {
                    return childElem;
                }
                if (child instanceof SvgContainer) {
                    SvgElementBase found = getElementById((SvgContainer) child, id);
                    if (found != null) {
                        return found;
                    }
                } else {
                    continue;
                }
            }
        }
        return null;
    }

    /* Access modifiers changed, original: protected */
    public List<SvgObject> getElementsByTagName(Class clazz) {
        return getElementsByTagName(this.rootElement, clazz);
    }

    private List<SvgObject> getElementsByTagName(SvgContainer obj, Class clazz) {
        List<SvgObject> result = new ArrayList();
        if (obj.getClass() == clazz) {
            result.add((SvgObject) obj);
        }
        for (SvgObject child : obj.getChildren()) {
            if (child.getClass() == clazz) {
                result.add(child);
            }
            if (child instanceof SvgContainer) {
                getElementsByTagName((SvgContainer) child, clazz);
            }
        }
        return result;
    }
}
