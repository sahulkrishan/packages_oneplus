package com.caverock.androidsvg;

import android.graphics.Matrix;
import android.support.v4.media.MediaPlayer2;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import com.android.settings.datausage.BillingCycleSettings;
import com.android.settings.wifi.ConfigureWifiSettings;
import com.android.settingslib.accessibility.AccessibilityUtils;
import com.android.settingslib.datetime.ZoneGetter;
import com.caverock.androidsvg.CSSParser.MediaType;
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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DefaultHandler2;

public class SVGParser extends DefaultHandler2 {
    private static /* synthetic */ int[] $SWITCH_TABLE$com$caverock$androidsvg$SVGParser$SVGAttr = null;
    private static final String CURRENTCOLOR = "currentColor";
    private static final String FEATURE_STRING_PREFIX = "http://www.w3.org/TR/SVG11/feature#";
    private static final String NONE = "none";
    private static final String SVG_NAMESPACE = "http://www.w3.org/2000/svg";
    private static final String TAG = "SVGParser";
    private static final String TAG_A = "a";
    private static final String TAG_CIRCLE = "circle";
    private static final String TAG_CLIPPATH = "clipPath";
    private static final String TAG_DEFS = "defs";
    private static final String TAG_DESC = "desc";
    private static final String TAG_ELLIPSE = "ellipse";
    private static final String TAG_G = "g";
    private static final String TAG_IMAGE = "image";
    private static final String TAG_LINE = "line";
    private static final String TAG_LINEARGRADIENT = "linearGradient";
    private static final String TAG_MARKER = "marker";
    private static final String TAG_MASK = "mask";
    private static final String TAG_PATH = "path";
    private static final String TAG_PATTERN = "pattern";
    private static final String TAG_POLYGON = "polygon";
    private static final String TAG_POLYLINE = "polyline";
    private static final String TAG_RADIALGRADIENT = "radialGradient";
    private static final String TAG_RECT = "rect";
    private static final String TAG_SOLIDCOLOR = "solidColor";
    private static final String TAG_STOP = "stop";
    private static final String TAG_STYLE = "style";
    private static final String TAG_SVG = "svg";
    private static final String TAG_SWITCH = "switch";
    private static final String TAG_SYMBOL = "symbol";
    private static final String TAG_TEXT = "text";
    private static final String TAG_TEXTPATH = "textPath";
    private static final String TAG_TITLE = "title";
    private static final String TAG_TREF = "tref";
    private static final String TAG_TSPAN = "tspan";
    private static final String TAG_USE = "use";
    private static final String TAG_VIEW = "view";
    private static final String VALID_DISPLAY_VALUES = "|inline|block|list-item|run-in|compact|marker|table|inline-table|table-row-group|table-header-group|table-footer-group|table-row|table-column-group|table-column|table-cell|table-caption|none|";
    private static final String VALID_VISIBILITY_VALUES = "|visible|hidden|collapse|";
    private static final String XLINK_NAMESPACE = "http://www.w3.org/1999/xlink";
    private static HashMap<String, Alignment> aspectRatioKeywords = new HashMap();
    private static HashMap<String, Integer> colourKeywords = new HashMap();
    private static HashMap<String, Length> fontSizeKeywords = new HashMap(9);
    private static HashMap<String, FontStyle> fontStyleKeywords = new HashMap(3);
    private static HashMap<String, Integer> fontWeightKeywords = new HashMap(13);
    protected static HashSet<String> supportedFeatures = new HashSet();
    private SvgContainer currentElement = null;
    private int ignoreDepth;
    private boolean ignoring = false;
    private boolean inMetadataElement = false;
    private boolean inStyleElement = false;
    private StringBuilder metadataElementContents = null;
    private String metadataTag = null;
    private StringBuilder styleElementContents = null;
    private HashSet<String> supportedFormats = null;
    private SVG svgDocument = null;

    private enum SVGAttr {
        CLASS,
        clip,
        clip_path,
        clipPathUnits,
        clip_rule,
        color,
        cx,
        cy,
        direction,
        dx,
        dy,
        fx,
        fy,
        d,
        display,
        fill,
        fill_rule,
        fill_opacity,
        font,
        font_family,
        font_size,
        font_weight,
        font_style,
        gradientTransform,
        gradientUnits,
        height,
        href,
        id,
        marker,
        marker_start,
        marker_mid,
        marker_end,
        markerHeight,
        markerUnits,
        markerWidth,
        mask,
        maskContentUnits,
        maskUnits,
        media,
        offset,
        opacity,
        orient,
        overflow,
        pathLength,
        patternContentUnits,
        patternTransform,
        patternUnits,
        points,
        preserveAspectRatio,
        r,
        refX,
        refY,
        requiredFeatures,
        requiredExtensions,
        requiredFormats,
        requiredFonts,
        rx,
        ry,
        solid_color,
        solid_opacity,
        spreadMethod,
        startOffset,
        stop_color,
        stop_opacity,
        stroke,
        stroke_dasharray,
        stroke_dashoffset,
        stroke_linecap,
        stroke_linejoin,
        stroke_miterlimit,
        stroke_opacity,
        stroke_width,
        style,
        systemLanguage,
        text_anchor,
        text_decoration,
        transform,
        type,
        vector_effect,
        version,
        viewBox,
        width,
        x,
        y,
        x1,
        y1,
        x2,
        y2,
        viewport_fill,
        viewport_fill_opacity,
        visibility,
        UNSUPPORTED;

        public static SVGAttr fromString(String str) {
            if (str.equals("class")) {
                return CLASS;
            }
            if (str.indexOf(95) != -1) {
                return UNSUPPORTED;
            }
            try {
                return valueOf(str.replace('-', '_'));
            } catch (IllegalArgumentException e) {
                return UNSUPPORTED;
            }
        }
    }

    protected static class TextScanner {
        protected String input;
        protected int position = 0;

        public TextScanner(String input) {
            this.input = input.trim();
        }

        public boolean empty() {
            return this.position == this.input.length();
        }

        /* Access modifiers changed, original: protected */
        public boolean isWhitespace(int c) {
            return c == 32 || c == 10 || c == 13 || c == 9;
        }

        public void skipWhitespace() {
            while (this.position < this.input.length() && isWhitespace(this.input.charAt(this.position))) {
                this.position++;
            }
        }

        /* Access modifiers changed, original: protected */
        public boolean isEOL(int c) {
            return c == 10 || c == 13;
        }

        public boolean skipCommaWhitespace() {
            skipWhitespace();
            if (this.position == this.input.length() || this.input.charAt(this.position) != ',') {
                return false;
            }
            this.position++;
            skipWhitespace();
            return true;
        }

        public Float nextFloat() {
            int floatEnd = scanForFloat();
            if (floatEnd == this.position) {
                return null;
            }
            Float result = Float.valueOf(Float.parseFloat(this.input.substring(this.position, floatEnd)));
            this.position = floatEnd;
            return result;
        }

        public Float possibleNextFloat() {
            int start = this.position;
            skipCommaWhitespace();
            Float result = nextFloat();
            if (result != null) {
                return result;
            }
            this.position = start;
            return null;
        }

        public Integer nextInteger() {
            int intEnd = scanForInteger();
            if (intEnd == this.position) {
                return null;
            }
            Integer result = Integer.valueOf(Integer.parseInt(this.input.substring(this.position, intEnd)));
            this.position = intEnd;
            return result;
        }

        public Integer nextChar() {
            if (this.position == this.input.length()) {
                return null;
            }
            String str = this.input;
            int i = this.position;
            this.position = i + 1;
            return Integer.valueOf(str.charAt(i));
        }

        public Length nextLength() {
            Float scalar = nextFloat();
            if (scalar == null) {
                return null;
            }
            Unit unit = nextUnit();
            if (unit == null) {
                return new Length(scalar.floatValue(), Unit.px);
            }
            return new Length(scalar.floatValue(), unit);
        }

        public Boolean nextFlag() {
            if (this.position == this.input.length()) {
                return null;
            }
            char ch = this.input.charAt(this.position);
            if (ch != '0' && ch != '1') {
                return null;
            }
            boolean z = true;
            this.position++;
            if (ch != '1') {
                z = false;
            }
            return Boolean.valueOf(z);
        }

        public boolean consume(char ch) {
            boolean found = this.position < this.input.length() && this.input.charAt(this.position) == ch;
            if (found) {
                this.position++;
            }
            return found;
        }

        public boolean consume(String str) {
            int len = str.length();
            boolean found = this.position <= this.input.length() - len && this.input.substring(this.position, this.position + len).equals(str);
            if (found) {
                this.position += len;
            }
            return found;
        }

        /* Access modifiers changed, original: protected */
        public int advanceChar() {
            if (this.position == this.input.length()) {
                return -1;
            }
            this.position++;
            if (this.position < this.input.length()) {
                return this.input.charAt(this.position);
            }
            return -1;
        }

        public String nextToken() {
            return nextToken(' ');
        }

        public String nextToken(char terminator) {
            if (empty()) {
                return null;
            }
            char ch = this.input.charAt(this.position);
            if (isWhitespace(ch) || ch == terminator) {
                return null;
            }
            int start = this.position;
            ch = advanceChar();
            while (ch != 65535 && ch != terminator && !isWhitespace(ch)) {
                ch = advanceChar();
            }
            return this.input.substring(start, this.position);
        }

        public String nextFunction() {
            if (empty()) {
                return null;
            }
            int end;
            int start = this.position;
            int ch = this.input.charAt(this.position);
            while (true) {
                if ((ch < 97 || ch > 122) && (ch < 65 || ch > 90)) {
                    end = this.position;
                } else {
                    ch = advanceChar();
                }
            }
            end = this.position;
            while (isWhitespace(ch)) {
                ch = advanceChar();
            }
            if (ch == 40) {
                this.position++;
                return this.input.substring(start, end);
            }
            this.position = start;
            return null;
        }

        private int scanForFloat() {
            if (empty()) {
                return this.position;
            }
            int lastValidPos;
            int lastValidPos2 = this.position;
            int start = this.position;
            int ch = this.input.charAt(this.position);
            if (ch == 45 || ch == 43) {
                ch = advanceChar();
            }
            if (Character.isDigit(ch)) {
                lastValidPos = this.position + 1;
                ch = advanceChar();
                lastValidPos2 = lastValidPos;
                while (Character.isDigit(ch)) {
                    lastValidPos2 = this.position + 1;
                    ch = advanceChar();
                }
            }
            if (ch == 46) {
                lastValidPos = this.position + 1;
                ch = advanceChar();
                lastValidPos2 = lastValidPos;
                while (Character.isDigit(ch)) {
                    lastValidPos2 = this.position + 1;
                    ch = advanceChar();
                }
            }
            if (ch == 101 || ch == 69) {
                ch = advanceChar();
                if (ch == 45 || ch == 43) {
                    ch = advanceChar();
                }
                if (Character.isDigit(ch)) {
                    int lastValidPos3 = this.position + 1;
                    ch = advanceChar();
                    lastValidPos2 = lastValidPos3;
                    while (Character.isDigit(ch)) {
                        lastValidPos2 = this.position + 1;
                        ch = advanceChar();
                    }
                }
            }
            this.position = start;
            return lastValidPos2;
        }

        private int scanForInteger() {
            if (empty()) {
                return this.position;
            }
            int lastValidPos = this.position;
            int start = this.position;
            int ch = this.input.charAt(this.position);
            if (ch == 45 || ch == 43) {
                ch = advanceChar();
            }
            if (Character.isDigit(ch)) {
                int lastValidPos2 = this.position + 1;
                ch = advanceChar();
                lastValidPos = lastValidPos2;
                while (Character.isDigit(ch)) {
                    lastValidPos = this.position + 1;
                    ch = advanceChar();
                }
            }
            this.position = start;
            return lastValidPos;
        }

        public String ahead() {
            int start = this.position;
            while (!empty() && !isWhitespace(this.input.charAt(this.position))) {
                this.position++;
            }
            String str = this.input.substring(start, this.position);
            this.position = start;
            return str;
        }

        public Unit nextUnit() {
            if (empty()) {
                return null;
            }
            if (this.input.charAt(this.position) == 37) {
                this.position++;
                return Unit.percent;
            } else if (this.position > this.input.length() - 2) {
                return null;
            } else {
                try {
                    Unit result = Unit.valueOf(this.input.substring(this.position, this.position + 2).toLowerCase(Locale.US));
                    this.position += 2;
                    return result;
                } catch (IllegalArgumentException e) {
                    return null;
                }
            }
        }

        public boolean hasLetter() {
            if (this.position == this.input.length()) {
                return false;
            }
            char ch = this.input.charAt(this.position);
            if ((ch < 'a' || ch > 'z') && (ch < 'A' || ch > 'Z')) {
                return false;
            }
            return true;
        }

        public String nextQuotedString() {
            if (empty()) {
                return null;
            }
            int start = this.position;
            int ch = this.input.charAt(this.position);
            int endQuote = ch;
            if (ch != 39 && ch != 34) {
                return null;
            }
            ch = advanceChar();
            while (ch != -1 && ch != endQuote) {
                ch = advanceChar();
            }
            if (ch == -1) {
                this.position = start;
                return null;
            }
            this.position++;
            return this.input.substring(start + 1, this.position - 1);
        }

        public String restOfText() {
            if (empty()) {
                return null;
            }
            int start = this.position;
            this.position = this.input.length();
            return this.input.substring(start);
        }
    }

    static /* synthetic */ int[] $SWITCH_TABLE$com$caverock$androidsvg$SVGParser$SVGAttr() {
        int[] iArr = $SWITCH_TABLE$com$caverock$androidsvg$SVGParser$SVGAttr;
        if (iArr != null) {
            return iArr;
        }
        iArr = new int[SVGAttr.values().length];
        try {
            iArr[SVGAttr.CLASS.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[SVGAttr.UNSUPPORTED.ordinal()] = 92;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[SVGAttr.clip.ordinal()] = 2;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[SVGAttr.clipPathUnits.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[SVGAttr.clip_path.ordinal()] = 3;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[SVGAttr.clip_rule.ordinal()] = 5;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[SVGAttr.color.ordinal()] = 6;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[SVGAttr.cx.ordinal()] = 7;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[SVGAttr.cy.ordinal()] = 8;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[SVGAttr.d.ordinal()] = 14;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[SVGAttr.direction.ordinal()] = 9;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[SVGAttr.display.ordinal()] = 15;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[SVGAttr.dx.ordinal()] = 10;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[SVGAttr.dy.ordinal()] = 11;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[SVGAttr.fill.ordinal()] = 16;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[SVGAttr.fill_opacity.ordinal()] = 18;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[SVGAttr.fill_rule.ordinal()] = 17;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[SVGAttr.font.ordinal()] = 19;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[SVGAttr.font_family.ordinal()] = 20;
        } catch (NoSuchFieldError e19) {
        }
        try {
            iArr[SVGAttr.font_size.ordinal()] = 21;
        } catch (NoSuchFieldError e20) {
        }
        try {
            iArr[SVGAttr.font_style.ordinal()] = 23;
        } catch (NoSuchFieldError e21) {
        }
        try {
            iArr[SVGAttr.font_weight.ordinal()] = 22;
        } catch (NoSuchFieldError e22) {
        }
        try {
            iArr[SVGAttr.fx.ordinal()] = 12;
        } catch (NoSuchFieldError e23) {
        }
        try {
            iArr[SVGAttr.fy.ordinal()] = 13;
        } catch (NoSuchFieldError e24) {
        }
        try {
            iArr[SVGAttr.gradientTransform.ordinal()] = 24;
        } catch (NoSuchFieldError e25) {
        }
        try {
            iArr[SVGAttr.gradientUnits.ordinal()] = 25;
        } catch (NoSuchFieldError e26) {
        }
        try {
            iArr[SVGAttr.height.ordinal()] = 26;
        } catch (NoSuchFieldError e27) {
        }
        try {
            iArr[SVGAttr.href.ordinal()] = 27;
        } catch (NoSuchFieldError e28) {
        }
        try {
            iArr[SVGAttr.id.ordinal()] = 28;
        } catch (NoSuchFieldError e29) {
        }
        try {
            iArr[SVGAttr.marker.ordinal()] = 29;
        } catch (NoSuchFieldError e30) {
        }
        try {
            iArr[SVGAttr.markerHeight.ordinal()] = 33;
        } catch (NoSuchFieldError e31) {
        }
        try {
            iArr[SVGAttr.markerUnits.ordinal()] = 34;
        } catch (NoSuchFieldError e32) {
        }
        try {
            iArr[SVGAttr.markerWidth.ordinal()] = 35;
        } catch (NoSuchFieldError e33) {
        }
        try {
            iArr[SVGAttr.marker_end.ordinal()] = 32;
        } catch (NoSuchFieldError e34) {
        }
        try {
            iArr[SVGAttr.marker_mid.ordinal()] = 31;
        } catch (NoSuchFieldError e35) {
        }
        try {
            iArr[SVGAttr.marker_start.ordinal()] = 30;
        } catch (NoSuchFieldError e36) {
        }
        try {
            iArr[SVGAttr.mask.ordinal()] = 36;
        } catch (NoSuchFieldError e37) {
        }
        try {
            iArr[SVGAttr.maskContentUnits.ordinal()] = 37;
        } catch (NoSuchFieldError e38) {
        }
        try {
            iArr[SVGAttr.maskUnits.ordinal()] = 38;
        } catch (NoSuchFieldError e39) {
        }
        try {
            iArr[SVGAttr.media.ordinal()] = 39;
        } catch (NoSuchFieldError e40) {
        }
        try {
            iArr[SVGAttr.offset.ordinal()] = 40;
        } catch (NoSuchFieldError e41) {
        }
        try {
            iArr[SVGAttr.opacity.ordinal()] = 41;
        } catch (NoSuchFieldError e42) {
        }
        try {
            iArr[SVGAttr.orient.ordinal()] = 42;
        } catch (NoSuchFieldError e43) {
        }
        try {
            iArr[SVGAttr.overflow.ordinal()] = 43;
        } catch (NoSuchFieldError e44) {
        }
        try {
            iArr[SVGAttr.pathLength.ordinal()] = 44;
        } catch (NoSuchFieldError e45) {
        }
        try {
            iArr[SVGAttr.patternContentUnits.ordinal()] = 45;
        } catch (NoSuchFieldError e46) {
        }
        try {
            iArr[SVGAttr.patternTransform.ordinal()] = 46;
        } catch (NoSuchFieldError e47) {
        }
        try {
            iArr[SVGAttr.patternUnits.ordinal()] = 47;
        } catch (NoSuchFieldError e48) {
        }
        try {
            iArr[SVGAttr.points.ordinal()] = 48;
        } catch (NoSuchFieldError e49) {
        }
        try {
            iArr[SVGAttr.preserveAspectRatio.ordinal()] = 49;
        } catch (NoSuchFieldError e50) {
        }
        try {
            iArr[SVGAttr.r.ordinal()] = 50;
        } catch (NoSuchFieldError e51) {
        }
        try {
            iArr[SVGAttr.refX.ordinal()] = 51;
        } catch (NoSuchFieldError e52) {
        }
        try {
            iArr[SVGAttr.refY.ordinal()] = 52;
        } catch (NoSuchFieldError e53) {
        }
        try {
            iArr[SVGAttr.requiredExtensions.ordinal()] = 54;
        } catch (NoSuchFieldError e54) {
        }
        try {
            iArr[SVGAttr.requiredFeatures.ordinal()] = 53;
        } catch (NoSuchFieldError e55) {
        }
        try {
            iArr[SVGAttr.requiredFonts.ordinal()] = 56;
        } catch (NoSuchFieldError e56) {
        }
        try {
            iArr[SVGAttr.requiredFormats.ordinal()] = 55;
        } catch (NoSuchFieldError e57) {
        }
        try {
            iArr[SVGAttr.rx.ordinal()] = 57;
        } catch (NoSuchFieldError e58) {
        }
        try {
            iArr[SVGAttr.ry.ordinal()] = 58;
        } catch (NoSuchFieldError e59) {
        }
        try {
            iArr[SVGAttr.solid_color.ordinal()] = 59;
        } catch (NoSuchFieldError e60) {
        }
        try {
            iArr[SVGAttr.solid_opacity.ordinal()] = 60;
        } catch (NoSuchFieldError e61) {
        }
        try {
            iArr[SVGAttr.spreadMethod.ordinal()] = 61;
        } catch (NoSuchFieldError e62) {
        }
        try {
            iArr[SVGAttr.startOffset.ordinal()] = 62;
        } catch (NoSuchFieldError e63) {
        }
        try {
            iArr[SVGAttr.stop_color.ordinal()] = 63;
        } catch (NoSuchFieldError e64) {
        }
        try {
            iArr[SVGAttr.stop_opacity.ordinal()] = 64;
        } catch (NoSuchFieldError e65) {
        }
        try {
            iArr[SVGAttr.stroke.ordinal()] = 65;
        } catch (NoSuchFieldError e66) {
        }
        try {
            iArr[SVGAttr.stroke_dasharray.ordinal()] = 66;
        } catch (NoSuchFieldError e67) {
        }
        try {
            iArr[SVGAttr.stroke_dashoffset.ordinal()] = 67;
        } catch (NoSuchFieldError e68) {
        }
        try {
            iArr[SVGAttr.stroke_linecap.ordinal()] = 68;
        } catch (NoSuchFieldError e69) {
        }
        try {
            iArr[SVGAttr.stroke_linejoin.ordinal()] = 69;
        } catch (NoSuchFieldError e70) {
        }
        try {
            iArr[SVGAttr.stroke_miterlimit.ordinal()] = 70;
        } catch (NoSuchFieldError e71) {
        }
        try {
            iArr[SVGAttr.stroke_opacity.ordinal()] = 71;
        } catch (NoSuchFieldError e72) {
        }
        try {
            iArr[SVGAttr.stroke_width.ordinal()] = 72;
        } catch (NoSuchFieldError e73) {
        }
        try {
            iArr[SVGAttr.style.ordinal()] = 73;
        } catch (NoSuchFieldError e74) {
        }
        try {
            iArr[SVGAttr.systemLanguage.ordinal()] = 74;
        } catch (NoSuchFieldError e75) {
        }
        try {
            iArr[SVGAttr.text_anchor.ordinal()] = 75;
        } catch (NoSuchFieldError e76) {
        }
        try {
            iArr[SVGAttr.text_decoration.ordinal()] = 76;
        } catch (NoSuchFieldError e77) {
        }
        try {
            iArr[SVGAttr.transform.ordinal()] = 77;
        } catch (NoSuchFieldError e78) {
        }
        try {
            iArr[SVGAttr.type.ordinal()] = 78;
        } catch (NoSuchFieldError e79) {
        }
        try {
            iArr[SVGAttr.vector_effect.ordinal()] = 79;
        } catch (NoSuchFieldError e80) {
        }
        try {
            iArr[SVGAttr.version.ordinal()] = 80;
        } catch (NoSuchFieldError e81) {
        }
        try {
            iArr[SVGAttr.viewBox.ordinal()] = 81;
        } catch (NoSuchFieldError e82) {
        }
        try {
            iArr[SVGAttr.viewport_fill.ordinal()] = 89;
        } catch (NoSuchFieldError e83) {
        }
        try {
            iArr[SVGAttr.viewport_fill_opacity.ordinal()] = 90;
        } catch (NoSuchFieldError e84) {
        }
        try {
            iArr[SVGAttr.visibility.ordinal()] = 91;
        } catch (NoSuchFieldError e85) {
        }
        try {
            iArr[SVGAttr.width.ordinal()] = 82;
        } catch (NoSuchFieldError e86) {
        }
        try {
            iArr[SVGAttr.x.ordinal()] = 83;
        } catch (NoSuchFieldError e87) {
        }
        try {
            iArr[SVGAttr.x1.ordinal()] = 85;
        } catch (NoSuchFieldError e88) {
        }
        try {
            iArr[SVGAttr.x2.ordinal()] = 87;
        } catch (NoSuchFieldError e89) {
        }
        try {
            iArr[SVGAttr.y.ordinal()] = 84;
        } catch (NoSuchFieldError e90) {
        }
        try {
            iArr[SVGAttr.y1.ordinal()] = 86;
        } catch (NoSuchFieldError e91) {
        }
        try {
            iArr[SVGAttr.y2.ordinal()] = 88;
        } catch (NoSuchFieldError e92) {
        }
        $SWITCH_TABLE$com$caverock$androidsvg$SVGParser$SVGAttr = iArr;
        return iArr;
    }

    static {
        colourKeywords.put("aliceblue", Integer.valueOf(15792383));
        colourKeywords.put("antiquewhite", Integer.valueOf(16444375));
        colourKeywords.put("aqua", Integer.valueOf(65535));
        colourKeywords.put("aquamarine", Integer.valueOf(8388564));
        colourKeywords.put("azure", Integer.valueOf(15794175));
        colourKeywords.put("beige", Integer.valueOf(16119260));
        colourKeywords.put("bisque", Integer.valueOf(16770244));
        colourKeywords.put("black", Integer.valueOf(0));
        colourKeywords.put("blanchedalmond", Integer.valueOf(16772045));
        colourKeywords.put("blue", Integer.valueOf(255));
        colourKeywords.put("blueviolet", Integer.valueOf(9055202));
        colourKeywords.put("brown", Integer.valueOf(10824234));
        colourKeywords.put("burlywood", Integer.valueOf(14596231));
        colourKeywords.put("cadetblue", Integer.valueOf(6266528));
        colourKeywords.put("chartreuse", Integer.valueOf(8388352));
        colourKeywords.put("chocolate", Integer.valueOf(13789470));
        colourKeywords.put("coral", Integer.valueOf(16744272));
        colourKeywords.put("cornflowerblue", Integer.valueOf(6591981));
        colourKeywords.put("cornsilk", Integer.valueOf(16775388));
        colourKeywords.put("crimson", Integer.valueOf(14423100));
        colourKeywords.put("cyan", Integer.valueOf(65535));
        colourKeywords.put("darkblue", Integer.valueOf(Const.CODE_C1_TGW));
        colourKeywords.put("darkcyan", Integer.valueOf(35723));
        colourKeywords.put("darkgoldenrod", Integer.valueOf(12092939));
        colourKeywords.put("darkgray", Integer.valueOf(11119017));
        colourKeywords.put("darkgreen", Integer.valueOf(25600));
        colourKeywords.put("darkgrey", Integer.valueOf(11119017));
        colourKeywords.put("darkkhaki", Integer.valueOf(12433259));
        colourKeywords.put("darkmagenta", Integer.valueOf(9109643));
        colourKeywords.put("darkolivegreen", Integer.valueOf(5597999));
        colourKeywords.put("darkorange", Integer.valueOf(16747520));
        colourKeywords.put("darkorchid", Integer.valueOf(10040012));
        colourKeywords.put("darkred", Integer.valueOf(9109504));
        colourKeywords.put("darksalmon", Integer.valueOf(15308410));
        colourKeywords.put("darkseagreen", Integer.valueOf(9419919));
        colourKeywords.put("darkslateblue", Integer.valueOf(4734347));
        colourKeywords.put("darkslategray", Integer.valueOf(3100495));
        colourKeywords.put("darkslategrey", Integer.valueOf(3100495));
        colourKeywords.put("darkturquoise", Integer.valueOf(52945));
        colourKeywords.put("darkviolet", Integer.valueOf(9699539));
        colourKeywords.put("deeppink", Integer.valueOf(16716947));
        colourKeywords.put("deepskyblue", Integer.valueOf(49151));
        colourKeywords.put("dimgray", Integer.valueOf(6908265));
        colourKeywords.put("dimgrey", Integer.valueOf(6908265));
        colourKeywords.put("dodgerblue", Integer.valueOf(2003199));
        colourKeywords.put("firebrick", Integer.valueOf(11674146));
        colourKeywords.put("floralwhite", Integer.valueOf(16775920));
        colourKeywords.put("forestgreen", Integer.valueOf(2263842));
        colourKeywords.put("fuchsia", Integer.valueOf(16711935));
        colourKeywords.put("gainsboro", Integer.valueOf(14474460));
        colourKeywords.put("ghostwhite", Integer.valueOf(16316671));
        colourKeywords.put("gold", Integer.valueOf(16766720));
        colourKeywords.put("goldenrod", Integer.valueOf(14329120));
        colourKeywords.put("gray", Integer.valueOf(8421504));
        colourKeywords.put("green", Integer.valueOf(32768));
        colourKeywords.put("greenyellow", Integer.valueOf(11403055));
        colourKeywords.put("grey", Integer.valueOf(8421504));
        colourKeywords.put("honeydew", Integer.valueOf(15794160));
        colourKeywords.put("hotpink", Integer.valueOf(16738740));
        colourKeywords.put("indianred", Integer.valueOf(13458524));
        colourKeywords.put("indigo", Integer.valueOf(4915330));
        colourKeywords.put("ivory", Integer.valueOf(16777200));
        colourKeywords.put("khaki", Integer.valueOf(15787660));
        colourKeywords.put("lavender", Integer.valueOf(15132410));
        colourKeywords.put("lavenderblush", Integer.valueOf(16773365));
        colourKeywords.put("lawngreen", Integer.valueOf(8190976));
        colourKeywords.put("lemonchiffon", Integer.valueOf(16775885));
        colourKeywords.put("lightblue", Integer.valueOf(11393254));
        colourKeywords.put("lightcoral", Integer.valueOf(15761536));
        colourKeywords.put("lightcyan", Integer.valueOf(14745599));
        colourKeywords.put("lightgoldenrodyellow", Integer.valueOf(16448210));
        colourKeywords.put("lightgray", Integer.valueOf(13882323));
        colourKeywords.put("lightgreen", Integer.valueOf(9498256));
        colourKeywords.put("lightgrey", Integer.valueOf(13882323));
        colourKeywords.put("lightpink", Integer.valueOf(16758465));
        colourKeywords.put("lightsalmon", Integer.valueOf(16752762));
        colourKeywords.put("lightseagreen", Integer.valueOf(2142890));
        colourKeywords.put("lightskyblue", Integer.valueOf(8900346));
        colourKeywords.put("lightslategray", Integer.valueOf(7833753));
        colourKeywords.put("lightslategrey", Integer.valueOf(7833753));
        colourKeywords.put("lightsteelblue", Integer.valueOf(11584734));
        colourKeywords.put("lightyellow", Integer.valueOf(16777184));
        colourKeywords.put("lime", Integer.valueOf(MotionEventCompat.ACTION_POINTER_INDEX_MASK));
        colourKeywords.put("limegreen", Integer.valueOf(3329330));
        colourKeywords.put("linen", Integer.valueOf(16445670));
        colourKeywords.put("magenta", Integer.valueOf(16711935));
        colourKeywords.put("maroon", Integer.valueOf(8388608));
        colourKeywords.put("mediumaquamarine", Integer.valueOf(6737322));
        colourKeywords.put("mediumblue", Integer.valueOf(205));
        colourKeywords.put("mediumorchid", Integer.valueOf(12211667));
        colourKeywords.put("mediumpurple", Integer.valueOf(9662683));
        colourKeywords.put("mediumseagreen", Integer.valueOf(3978097));
        colourKeywords.put("mediumslateblue", Integer.valueOf(8087790));
        colourKeywords.put("mediumspringgreen", Integer.valueOf(64154));
        colourKeywords.put("mediumturquoise", Integer.valueOf(4772300));
        colourKeywords.put("mediumvioletred", Integer.valueOf(13047173));
        colourKeywords.put("midnightblue", Integer.valueOf(1644912));
        colourKeywords.put("mintcream", Integer.valueOf(16121850));
        colourKeywords.put("mistyrose", Integer.valueOf(16770273));
        colourKeywords.put("moccasin", Integer.valueOf(16770229));
        colourKeywords.put("navajowhite", Integer.valueOf(16768685));
        colourKeywords.put("navy", Integer.valueOf(128));
        colourKeywords.put("oldlace", Integer.valueOf(16643558));
        colourKeywords.put("olive", Integer.valueOf(8421376));
        colourKeywords.put("olivedrab", Integer.valueOf(7048739));
        colourKeywords.put("orange", Integer.valueOf(16753920));
        colourKeywords.put("orangered", Integer.valueOf(16729344));
        colourKeywords.put("orchid", Integer.valueOf(14315734));
        colourKeywords.put("palegoldenrod", Integer.valueOf(15657130));
        colourKeywords.put("palegreen", Integer.valueOf(10025880));
        colourKeywords.put("paleturquoise", Integer.valueOf(11529966));
        colourKeywords.put("palevioletred", Integer.valueOf(14381203));
        colourKeywords.put("papayawhip", Integer.valueOf(16773077));
        colourKeywords.put("peachpuff", Integer.valueOf(16767673));
        colourKeywords.put("peru", Integer.valueOf(13468991));
        colourKeywords.put("pink", Integer.valueOf(16761035));
        colourKeywords.put("plum", Integer.valueOf(14524637));
        colourKeywords.put("powderblue", Integer.valueOf(11591910));
        colourKeywords.put("purple", Integer.valueOf(8388736));
        colourKeywords.put("red", Integer.valueOf(16711680));
        colourKeywords.put("rosybrown", Integer.valueOf(12357519));
        colourKeywords.put("royalblue", Integer.valueOf(4286945));
        colourKeywords.put("saddlebrown", Integer.valueOf(9127187));
        colourKeywords.put("salmon", Integer.valueOf(16416882));
        colourKeywords.put("sandybrown", Integer.valueOf(16032864));
        colourKeywords.put("seagreen", Integer.valueOf(3050327));
        colourKeywords.put("seashell", Integer.valueOf(16774638));
        colourKeywords.put("sienna", Integer.valueOf(10506797));
        colourKeywords.put("silver", Integer.valueOf(12632256));
        colourKeywords.put("skyblue", Integer.valueOf(8900331));
        colourKeywords.put("slateblue", Integer.valueOf(6970061));
        colourKeywords.put("slategray", Integer.valueOf(7372944));
        colourKeywords.put("slategrey", Integer.valueOf(7372944));
        colourKeywords.put("snow", Integer.valueOf(16775930));
        colourKeywords.put("springgreen", Integer.valueOf(65407));
        colourKeywords.put("steelblue", Integer.valueOf(4620980));
        colourKeywords.put("tan", Integer.valueOf(13808780));
        colourKeywords.put("teal", Integer.valueOf(32896));
        colourKeywords.put("thistle", Integer.valueOf(14204888));
        colourKeywords.put("tomato", Integer.valueOf(16737095));
        colourKeywords.put("turquoise", Integer.valueOf(4251856));
        colourKeywords.put("violet", Integer.valueOf(15631086));
        colourKeywords.put("wheat", Integer.valueOf(16113331));
        colourKeywords.put("white", Integer.valueOf(ViewCompat.MEASURED_SIZE_MASK));
        colourKeywords.put("whitesmoke", Integer.valueOf(16119285));
        colourKeywords.put("yellow", Integer.valueOf(16776960));
        colourKeywords.put("yellowgreen", Integer.valueOf(10145074));
        fontSizeKeywords.put("xx-small", new Length(0.694f, Unit.pt));
        fontSizeKeywords.put("x-small", new Length(0.833f, Unit.pt));
        fontSizeKeywords.put("small", new Length(10.0f, Unit.pt));
        fontSizeKeywords.put("medium", new Length(12.0f, Unit.pt));
        fontSizeKeywords.put("large", new Length(14.4f, Unit.pt));
        fontSizeKeywords.put("x-large", new Length(17.3f, Unit.pt));
        fontSizeKeywords.put("xx-large", new Length(20.7f, Unit.pt));
        fontSizeKeywords.put("smaller", new Length(83.33f, Unit.percent));
        fontSizeKeywords.put("larger", new Length(120.0f, Unit.percent));
        fontWeightKeywords.put("normal", Integer.valueOf(400));
        fontWeightKeywords.put("bold", Integer.valueOf(700));
        fontWeightKeywords.put("bolder", Integer.valueOf(1));
        fontWeightKeywords.put("lighter", Integer.valueOf(-1));
        fontWeightKeywords.put("100", Integer.valueOf(100));
        fontWeightKeywords.put("200", Integer.valueOf(200));
        fontWeightKeywords.put("300", Integer.valueOf(300));
        fontWeightKeywords.put("400", Integer.valueOf(400));
        fontWeightKeywords.put("500", Integer.valueOf(500));
        fontWeightKeywords.put("600", Integer.valueOf(ConfigureWifiSettings.WIFI_WAKEUP_REQUEST_CODE));
        fontWeightKeywords.put("700", Integer.valueOf(700));
        fontWeightKeywords.put("800", Integer.valueOf(MediaPlayer2.MEDIA_INFO_BAD_INTERLEAVING));
        fontWeightKeywords.put("900", Integer.valueOf(MediaPlayer2.MEDIA_INFO_TIMED_TEXT_ERROR));
        fontStyleKeywords.put("normal", FontStyle.Normal);
        fontStyleKeywords.put("italic", FontStyle.Italic);
        fontStyleKeywords.put("oblique", FontStyle.Oblique);
        aspectRatioKeywords.put(NONE, Alignment.None);
        aspectRatioKeywords.put("xMinYMin", Alignment.XMinYMin);
        aspectRatioKeywords.put("xMidYMin", Alignment.XMidYMin);
        aspectRatioKeywords.put("xMaxYMin", Alignment.XMaxYMin);
        aspectRatioKeywords.put("xMinYMid", Alignment.XMinYMid);
        aspectRatioKeywords.put("xMidYMid", Alignment.XMidYMid);
        aspectRatioKeywords.put("xMaxYMid", Alignment.XMaxYMid);
        aspectRatioKeywords.put("xMinYMax", Alignment.XMinYMax);
        aspectRatioKeywords.put("xMidYMax", Alignment.XMidYMax);
        aspectRatioKeywords.put("xMaxYMax", Alignment.XMaxYMax);
        supportedFeatures.add("Structure");
        supportedFeatures.add("BasicStructure");
        supportedFeatures.add("ConditionalProcessing");
        supportedFeatures.add("Image");
        supportedFeatures.add("Style");
        supportedFeatures.add("ViewportAttribute");
        supportedFeatures.add("Shape");
        supportedFeatures.add("BasicText");
        supportedFeatures.add("PaintAttribute");
        supportedFeatures.add("BasicPaintAttribute");
        supportedFeatures.add("OpacityAttribute");
        supportedFeatures.add("BasicGraphicsAttribute");
        supportedFeatures.add("Marker");
        supportedFeatures.add("Gradient");
        supportedFeatures.add("Pattern");
        supportedFeatures.add("Clip");
        supportedFeatures.add("BasicClip");
        supportedFeatures.add("Mask");
        supportedFeatures.add("View");
    }

    /* Access modifiers changed, original: protected */
    public void setSupportedFormats(String[] mimeTypes) {
        this.supportedFormats = new HashSet(mimeTypes.length);
        Collections.addAll(this.supportedFormats, mimeTypes);
    }

    /* Access modifiers changed, original: protected */
    public SVG parse(InputStream is) throws SVGParseException {
        try {
            XMLReader xr = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
            xr.setContentHandler(this);
            xr.setProperty("http://xml.org/sax/properties/lexical-handler", this);
            xr.parse(new InputSource(is));
            return this.svgDocument;
        } catch (IOException e) {
            throw new SVGParseException("File error", e);
        } catch (ParserConfigurationException e2) {
            throw new SVGParseException("XML Parser problem", e2);
        } catch (SAXException e3) {
            StringBuilder stringBuilder = new StringBuilder("SVG parse error: ");
            stringBuilder.append(e3.getMessage());
            throw new SVGParseException(stringBuilder.toString(), e3);
        }
    }

    public void startDocument() throws SAXException {
        super.startDocument();
        this.svgDocument = new SVG();
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        if (this.ignoring) {
            this.ignoreDepth++;
        } else if (SVG_NAMESPACE.equals(uri) || "".equals(uri)) {
            if (localName.equals(TAG_SVG)) {
                svg(attributes);
            } else if (localName.equals(TAG_G)) {
                g(attributes);
            } else if (localName.equals(TAG_DEFS)) {
                defs(attributes);
            } else if (localName.equals(TAG_USE)) {
                use(attributes);
            } else if (localName.equals(TAG_PATH)) {
                path(attributes);
            } else if (localName.equals(TAG_RECT)) {
                rect(attributes);
            } else if (localName.equals(TAG_CIRCLE)) {
                circle(attributes);
            } else if (localName.equals(TAG_ELLIPSE)) {
                ellipse(attributes);
            } else if (localName.equals(TAG_LINE)) {
                line(attributes);
            } else if (localName.equals(TAG_POLYLINE)) {
                polyline(attributes);
            } else if (localName.equals(TAG_POLYGON)) {
                polygon(attributes);
            } else if (localName.equals(TAG_TEXT)) {
                text(attributes);
            } else if (localName.equals(TAG_TSPAN)) {
                tspan(attributes);
            } else if (localName.equals(TAG_TREF)) {
                tref(attributes);
            } else if (localName.equals(TAG_SWITCH)) {
                zwitch(attributes);
            } else if (localName.equals(TAG_SYMBOL)) {
                symbol(attributes);
            } else if (localName.equals(TAG_MARKER)) {
                marker(attributes);
            } else if (localName.equals(TAG_LINEARGRADIENT)) {
                linearGradient(attributes);
            } else if (localName.equals(TAG_RADIALGRADIENT)) {
                radialGradient(attributes);
            } else if (localName.equals(TAG_STOP)) {
                stop(attributes);
            } else if (localName.equals(TAG_A)) {
                g(attributes);
            } else if (localName.equals("title") || localName.equals(TAG_DESC)) {
                this.inMetadataElement = true;
                this.metadataTag = localName;
            } else if (localName.equals(TAG_CLIPPATH)) {
                clipPath(attributes);
            } else if (localName.equals(TAG_TEXTPATH)) {
                textPath(attributes);
            } else if (localName.equals(TAG_PATTERN)) {
                pattern(attributes);
            } else if (localName.equals(TAG_IMAGE)) {
                image(attributes);
            } else if (localName.equals(TAG_VIEW)) {
                view(attributes);
            } else if (localName.equals(TAG_MASK)) {
                mask(attributes);
            } else if (localName.equals(TAG_STYLE)) {
                style(attributes);
            } else if (localName.equals(TAG_SOLIDCOLOR)) {
                solidColor(attributes);
            } else {
                this.ignoring = true;
                this.ignoreDepth = 1;
            }
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        if (!this.ignoring) {
            if (this.inMetadataElement) {
                if (this.metadataElementContents == null) {
                    this.metadataElementContents = new StringBuilder(length);
                }
                this.metadataElementContents.append(ch, start, length);
            } else if (this.inStyleElement) {
                if (this.styleElementContents == null) {
                    this.styleElementContents = new StringBuilder(length);
                }
                this.styleElementContents.append(ch, start, length);
            } else {
                if (this.currentElement instanceof TextContainer) {
                    SvgConditionalContainer parent = this.currentElement;
                    int numOlderSiblings = parent.children.size();
                    SvgObject previousSibling = numOlderSiblings == 0 ? null : (SvgObject) parent.children.get(numOlderSiblings - 1);
                    if (previousSibling instanceof TextSequence) {
                        TextSequence textSequence = (TextSequence) previousSibling;
                        StringBuilder stringBuilder = new StringBuilder(String.valueOf(textSequence.text));
                        stringBuilder.append(new String(ch, start, length));
                        textSequence.text = stringBuilder.toString();
                    } else {
                        ((SvgConditionalContainer) this.currentElement).addChild(new TextSequence(new String(ch, start, length)));
                    }
                }
            }
        }
    }

    public void comment(char[] ch, int start, int length) throws SAXException {
        if (!this.ignoring && this.inStyleElement) {
            if (this.styleElementContents == null) {
                this.styleElementContents = new StringBuilder(length);
            }
            this.styleElementContents.append(ch, start, length);
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        if (this.ignoring) {
            int i = this.ignoreDepth - 1;
            this.ignoreDepth = i;
            if (i == 0) {
                this.ignoring = false;
                return;
            }
        }
        if (!SVG_NAMESPACE.equals(uri) && !"".equals(uri)) {
            return;
        }
        if (localName.equals("title") || localName.equals(TAG_DESC)) {
            this.inMetadataElement = false;
            if (this.metadataTag.equals("title")) {
                this.svgDocument.setTitle(this.metadataElementContents.toString());
            } else if (this.metadataTag.equals(TAG_DESC)) {
                this.svgDocument.setDesc(this.metadataElementContents.toString());
            }
            this.metadataElementContents.setLength(0);
        } else if (!localName.equals(TAG_STYLE) || this.styleElementContents == null) {
            if (localName.equals(TAG_SVG) || localName.equals(TAG_DEFS) || localName.equals(TAG_G) || localName.equals(TAG_USE) || localName.equals(TAG_IMAGE) || localName.equals(TAG_TEXT) || localName.equals(TAG_TSPAN) || localName.equals(TAG_SWITCH) || localName.equals(TAG_SYMBOL) || localName.equals(TAG_MARKER) || localName.equals(TAG_LINEARGRADIENT) || localName.equals(TAG_RADIALGRADIENT) || localName.equals(TAG_STOP) || localName.equals(TAG_CLIPPATH) || localName.equals(TAG_TEXTPATH) || localName.equals(TAG_PATTERN) || localName.equals(TAG_VIEW) || localName.equals(TAG_MASK) || localName.equals(TAG_SOLIDCOLOR)) {
                this.currentElement = ((SvgObject) this.currentElement).parent;
            }
        } else {
            this.inStyleElement = false;
            parseCSSStyleSheet(this.styleElementContents.toString());
            this.styleElementContents.setLength(0);
        }
    }

    public void endDocument() throws SAXException {
        super.endDocument();
    }

    private void dumpNode(SvgObject elem, String indent) {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder(String.valueOf(indent));
        stringBuilder.append(elem);
        Log.d(str, stringBuilder.toString());
        if (elem instanceof SvgConditionalContainer) {
            str = new StringBuilder(String.valueOf(indent));
            str.append("  ");
            str = str.toString();
            for (SvgObject indent2 : ((SvgConditionalContainer) elem).children) {
                dumpNode(indent2, str);
            }
            indent = str;
        }
    }

    private void debug(String format, Object... args) {
    }

    private void svg(Attributes attributes) throws SAXException {
        debug("<svg>", new Object[0]);
        Svg obj = new Svg();
        obj.document = this.svgDocument;
        obj.parent = this.currentElement;
        parseAttributesCore(obj, attributes);
        parseAttributesStyle(obj, attributes);
        parseAttributesConditional(obj, attributes);
        parseAttributesViewBox(obj, attributes);
        parseAttributesSVG(obj, attributes);
        if (this.currentElement == null) {
            this.svgDocument.setRootElement(obj);
        } else {
            this.currentElement.addChild(obj);
        }
        this.currentElement = obj;
    }

    private void parseAttributesSVG(Svg obj, Attributes attributes) throws SAXException {
        for (int i = 0; i < attributes.getLength(); i++) {
            String val = attributes.getValue(i).trim();
            int i2 = $SWITCH_TABLE$com$caverock$androidsvg$SVGParser$SVGAttr()[SVGAttr.fromString(attributes.getLocalName(i)).ordinal()];
            if (i2 == 26) {
                obj.height = parseLength(val);
                if (obj.height.isNegative()) {
                    throw new SAXException("Invalid <svg> element. height cannot be negative");
                }
            } else if (i2 != 80) {
                switch (i2) {
                    case 82:
                        obj.width = parseLength(val);
                        if (!obj.width.isNegative()) {
                            break;
                        }
                        throw new SAXException("Invalid <svg> element. width cannot be negative");
                    case 83:
                        obj.x = parseLength(val);
                        break;
                    case 84:
                        obj.y = parseLength(val);
                        break;
                    default:
                        continue;
                }
            } else {
                obj.version = val;
            }
        }
    }

    private void g(Attributes attributes) throws SAXException {
        debug("<g>", new Object[0]);
        if (this.currentElement != null) {
            Group obj = new Group();
            obj.document = this.svgDocument;
            obj.parent = this.currentElement;
            parseAttributesCore(obj, attributes);
            parseAttributesStyle(obj, attributes);
            parseAttributesTransform(obj, attributes);
            parseAttributesConditional(obj, attributes);
            this.currentElement.addChild(obj);
            this.currentElement = obj;
            return;
        }
        throw new SAXException("Invalid document. Root element must be <svg>");
    }

    private void defs(Attributes attributes) throws SAXException {
        debug("<defs>", new Object[0]);
        if (this.currentElement != null) {
            Defs obj = new Defs();
            obj.document = this.svgDocument;
            obj.parent = this.currentElement;
            parseAttributesCore(obj, attributes);
            parseAttributesStyle(obj, attributes);
            parseAttributesTransform(obj, attributes);
            this.currentElement.addChild(obj);
            this.currentElement = obj;
            return;
        }
        throw new SAXException("Invalid document. Root element must be <svg>");
    }

    private void use(Attributes attributes) throws SAXException {
        debug("<use>", new Object[0]);
        if (this.currentElement != null) {
            Use obj = new Use();
            obj.document = this.svgDocument;
            obj.parent = this.currentElement;
            parseAttributesCore(obj, attributes);
            parseAttributesStyle(obj, attributes);
            parseAttributesTransform(obj, attributes);
            parseAttributesConditional(obj, attributes);
            parseAttributesUse(obj, attributes);
            this.currentElement.addChild(obj);
            this.currentElement = obj;
            return;
        }
        throw new SAXException("Invalid document. Root element must be <svg>");
    }

    private void parseAttributesUse(Use obj, Attributes attributes) throws SAXException {
        for (int i = 0; i < attributes.getLength(); i++) {
            String val = attributes.getValue(i).trim();
            int i2 = $SWITCH_TABLE$com$caverock$androidsvg$SVGParser$SVGAttr()[SVGAttr.fromString(attributes.getLocalName(i)).ordinal()];
            switch (i2) {
                case 26:
                    obj.height = parseLength(val);
                    if (!obj.height.isNegative()) {
                        break;
                    }
                    throw new SAXException("Invalid <use> element. height cannot be negative");
                case 27:
                    if (!XLINK_NAMESPACE.equals(attributes.getURI(i))) {
                        break;
                    }
                    obj.href = val;
                    break;
                default:
                    switch (i2) {
                        case 82:
                            obj.width = parseLength(val);
                            if (!obj.width.isNegative()) {
                                break;
                            }
                            throw new SAXException("Invalid <use> element. width cannot be negative");
                        case 83:
                            obj.x = parseLength(val);
                            break;
                        case 84:
                            obj.y = parseLength(val);
                            break;
                        default:
                            continue;
                    }
            }
        }
    }

    private void image(Attributes attributes) throws SAXException {
        debug("<image>", new Object[0]);
        if (this.currentElement != null) {
            Image obj = new Image();
            obj.document = this.svgDocument;
            obj.parent = this.currentElement;
            parseAttributesCore(obj, attributes);
            parseAttributesStyle(obj, attributes);
            parseAttributesTransform(obj, attributes);
            parseAttributesConditional(obj, attributes);
            parseAttributesImage(obj, attributes);
            this.currentElement.addChild(obj);
            this.currentElement = obj;
            return;
        }
        throw new SAXException("Invalid document. Root element must be <svg>");
    }

    private void parseAttributesImage(Image obj, Attributes attributes) throws SAXException {
        for (int i = 0; i < attributes.getLength(); i++) {
            String val = attributes.getValue(i).trim();
            int i2 = $SWITCH_TABLE$com$caverock$androidsvg$SVGParser$SVGAttr()[SVGAttr.fromString(attributes.getLocalName(i)).ordinal()];
            if (i2 != 49) {
                switch (i2) {
                    case 26:
                        obj.height = parseLength(val);
                        if (!obj.height.isNegative()) {
                            break;
                        }
                        throw new SAXException("Invalid <use> element. height cannot be negative");
                    case 27:
                        if (!XLINK_NAMESPACE.equals(attributes.getURI(i))) {
                            break;
                        }
                        obj.href = val;
                        break;
                    default:
                        switch (i2) {
                            case 82:
                                obj.width = parseLength(val);
                                if (!obj.width.isNegative()) {
                                    break;
                                }
                                throw new SAXException("Invalid <use> element. width cannot be negative");
                            case 83:
                                obj.x = parseLength(val);
                                break;
                            case 84:
                                obj.y = parseLength(val);
                                break;
                            default:
                                continue;
                        }
                }
            } else {
                parsePreserveAspectRatio(obj, val);
            }
        }
    }

    private void path(Attributes attributes) throws SAXException {
        debug("<path>", new Object[0]);
        if (this.currentElement != null) {
            Path obj = new Path();
            obj.document = this.svgDocument;
            obj.parent = this.currentElement;
            parseAttributesCore(obj, attributes);
            parseAttributesStyle(obj, attributes);
            parseAttributesTransform(obj, attributes);
            parseAttributesConditional(obj, attributes);
            parseAttributesPath(obj, attributes);
            this.currentElement.addChild(obj);
            return;
        }
        throw new SAXException("Invalid document. Root element must be <svg>");
    }

    private void parseAttributesPath(Path obj, Attributes attributes) throws SAXException {
        for (int i = 0; i < attributes.getLength(); i++) {
            String val = attributes.getValue(i).trim();
            int i2 = $SWITCH_TABLE$com$caverock$androidsvg$SVGParser$SVGAttr()[SVGAttr.fromString(attributes.getLocalName(i)).ordinal()];
            if (i2 == 14) {
                obj.d = parsePath(val);
            } else if (i2 != 44) {
                continue;
            } else {
                obj.pathLength = Float.valueOf(parseFloat(val));
                if (obj.pathLength.floatValue() < 0.0f) {
                    throw new SAXException("Invalid <path> element. pathLength cannot be negative");
                }
            }
        }
    }

    private void rect(Attributes attributes) throws SAXException {
        debug("<rect>", new Object[0]);
        if (this.currentElement != null) {
            Rect obj = new Rect();
            obj.document = this.svgDocument;
            obj.parent = this.currentElement;
            parseAttributesCore(obj, attributes);
            parseAttributesStyle(obj, attributes);
            parseAttributesTransform(obj, attributes);
            parseAttributesConditional(obj, attributes);
            parseAttributesRect(obj, attributes);
            this.currentElement.addChild(obj);
            return;
        }
        throw new SAXException("Invalid document. Root element must be <svg>");
    }

    private void parseAttributesRect(Rect obj, Attributes attributes) throws SAXException {
        for (int i = 0; i < attributes.getLength(); i++) {
            String val = attributes.getValue(i).trim();
            int i2 = $SWITCH_TABLE$com$caverock$androidsvg$SVGParser$SVGAttr()[SVGAttr.fromString(attributes.getLocalName(i)).ordinal()];
            if (i2 != 26) {
                switch (i2) {
                    case 57:
                        obj.rx = parseLength(val);
                        if (!obj.rx.isNegative()) {
                            break;
                        }
                        throw new SAXException("Invalid <rect> element. rx cannot be negative");
                    case 58:
                        obj.ry = parseLength(val);
                        if (!obj.ry.isNegative()) {
                            break;
                        }
                        throw new SAXException("Invalid <rect> element. ry cannot be negative");
                    default:
                        switch (i2) {
                            case 82:
                                obj.width = parseLength(val);
                                if (!obj.width.isNegative()) {
                                    break;
                                }
                                throw new SAXException("Invalid <rect> element. width cannot be negative");
                            case 83:
                                obj.x = parseLength(val);
                                break;
                            case 84:
                                obj.y = parseLength(val);
                                break;
                            default:
                                continue;
                        }
                }
            } else {
                obj.height = parseLength(val);
                if (obj.height.isNegative()) {
                    throw new SAXException("Invalid <rect> element. height cannot be negative");
                }
            }
        }
    }

    private void circle(Attributes attributes) throws SAXException {
        debug("<circle>", new Object[0]);
        if (this.currentElement != null) {
            Circle obj = new Circle();
            obj.document = this.svgDocument;
            obj.parent = this.currentElement;
            parseAttributesCore(obj, attributes);
            parseAttributesStyle(obj, attributes);
            parseAttributesTransform(obj, attributes);
            parseAttributesConditional(obj, attributes);
            parseAttributesCircle(obj, attributes);
            this.currentElement.addChild(obj);
            return;
        }
        throw new SAXException("Invalid document. Root element must be <svg>");
    }

    private void parseAttributesCircle(Circle obj, Attributes attributes) throws SAXException {
        for (int i = 0; i < attributes.getLength(); i++) {
            String val = attributes.getValue(i).trim();
            int i2 = $SWITCH_TABLE$com$caverock$androidsvg$SVGParser$SVGAttr()[SVGAttr.fromString(attributes.getLocalName(i)).ordinal()];
            if (i2 != 50) {
                switch (i2) {
                    case 7:
                        obj.cx = parseLength(val);
                        break;
                    case 8:
                        obj.cy = parseLength(val);
                        break;
                    default:
                        break;
                }
            }
            obj.r = parseLength(val);
            if (obj.r.isNegative()) {
                throw new SAXException("Invalid <circle> element. r cannot be negative");
            }
        }
    }

    private void ellipse(Attributes attributes) throws SAXException {
        debug("<ellipse>", new Object[0]);
        if (this.currentElement != null) {
            Ellipse obj = new Ellipse();
            obj.document = this.svgDocument;
            obj.parent = this.currentElement;
            parseAttributesCore(obj, attributes);
            parseAttributesStyle(obj, attributes);
            parseAttributesTransform(obj, attributes);
            parseAttributesConditional(obj, attributes);
            parseAttributesEllipse(obj, attributes);
            this.currentElement.addChild(obj);
            return;
        }
        throw new SAXException("Invalid document. Root element must be <svg>");
    }

    private void parseAttributesEllipse(Ellipse obj, Attributes attributes) throws SAXException {
        for (int i = 0; i < attributes.getLength(); i++) {
            String val = attributes.getValue(i).trim();
            switch ($SWITCH_TABLE$com$caverock$androidsvg$SVGParser$SVGAttr()[SVGAttr.fromString(attributes.getLocalName(i)).ordinal()]) {
                case 7:
                    obj.cx = parseLength(val);
                    break;
                case 8:
                    obj.cy = parseLength(val);
                    break;
                case 57:
                    obj.rx = parseLength(val);
                    if (!obj.rx.isNegative()) {
                        break;
                    }
                    throw new SAXException("Invalid <ellipse> element. rx cannot be negative");
                case 58:
                    obj.ry = parseLength(val);
                    if (!obj.ry.isNegative()) {
                        break;
                    }
                    throw new SAXException("Invalid <ellipse> element. ry cannot be negative");
                default:
                    break;
            }
        }
    }

    private void line(Attributes attributes) throws SAXException {
        debug("<line>", new Object[0]);
        if (this.currentElement != null) {
            Line obj = new Line();
            obj.document = this.svgDocument;
            obj.parent = this.currentElement;
            parseAttributesCore(obj, attributes);
            parseAttributesStyle(obj, attributes);
            parseAttributesTransform(obj, attributes);
            parseAttributesConditional(obj, attributes);
            parseAttributesLine(obj, attributes);
            this.currentElement.addChild(obj);
            return;
        }
        throw new SAXException("Invalid document. Root element must be <svg>");
    }

    private void parseAttributesLine(Line obj, Attributes attributes) throws SAXException {
        for (int i = 0; i < attributes.getLength(); i++) {
            String val = attributes.getValue(i).trim();
            switch ($SWITCH_TABLE$com$caverock$androidsvg$SVGParser$SVGAttr()[SVGAttr.fromString(attributes.getLocalName(i)).ordinal()]) {
                case 85:
                    obj.x1 = parseLength(val);
                    break;
                case 86:
                    obj.y1 = parseLength(val);
                    break;
                case 87:
                    obj.x2 = parseLength(val);
                    break;
                case 88:
                    obj.y2 = parseLength(val);
                    break;
                default:
                    break;
            }
        }
    }

    private void polyline(Attributes attributes) throws SAXException {
        debug("<polyline>", new Object[0]);
        if (this.currentElement != null) {
            PolyLine obj = new PolyLine();
            obj.document = this.svgDocument;
            obj.parent = this.currentElement;
            parseAttributesCore(obj, attributes);
            parseAttributesStyle(obj, attributes);
            parseAttributesTransform(obj, attributes);
            parseAttributesConditional(obj, attributes);
            parseAttributesPolyLine(obj, attributes, TAG_POLYLINE);
            this.currentElement.addChild(obj);
            return;
        }
        throw new SAXException("Invalid document. Root element must be <svg>");
    }

    private void parseAttributesPolyLine(PolyLine obj, Attributes attributes, String tag) throws SAXException {
        for (int i = 0; i < attributes.getLength(); i++) {
            if (SVGAttr.fromString(attributes.getLocalName(i)) == SVGAttr.points) {
                TextScanner scan = new TextScanner(attributes.getValue(i));
                List<Float> points = new ArrayList();
                scan.skipWhitespace();
                while (!scan.empty()) {
                    Float x = scan.nextFloat();
                    if (x != null) {
                        scan.skipCommaWhitespace();
                        Float y = scan.nextFloat();
                        if (y != null) {
                            scan.skipCommaWhitespace();
                            points.add(x);
                            points.add(y);
                        } else {
                            StringBuilder stringBuilder = new StringBuilder("Invalid <");
                            stringBuilder.append(tag);
                            stringBuilder.append("> points attribute. There should be an even number of coordinates.");
                            throw new SAXException(stringBuilder.toString());
                        }
                    }
                    StringBuilder stringBuilder2 = new StringBuilder("Invalid <");
                    stringBuilder2.append(tag);
                    stringBuilder2.append("> points attribute. Non-coordinate content found in list.");
                    throw new SAXException(stringBuilder2.toString());
                }
                obj.points = new float[points.size()];
                int j = 0;
                for (Float f : points) {
                    int j2 = j + 1;
                    obj.points[j] = f.floatValue();
                    j = j2;
                }
            }
        }
    }

    private void polygon(Attributes attributes) throws SAXException {
        debug("<polygon>", new Object[0]);
        if (this.currentElement != null) {
            Polygon obj = new Polygon();
            obj.document = this.svgDocument;
            obj.parent = this.currentElement;
            parseAttributesCore(obj, attributes);
            parseAttributesStyle(obj, attributes);
            parseAttributesTransform(obj, attributes);
            parseAttributesConditional(obj, attributes);
            parseAttributesPolyLine(obj, attributes, TAG_POLYGON);
            this.currentElement.addChild(obj);
            return;
        }
        throw new SAXException("Invalid document. Root element must be <svg>");
    }

    private void text(Attributes attributes) throws SAXException {
        debug("<text>", new Object[0]);
        if (this.currentElement != null) {
            Text obj = new Text();
            obj.document = this.svgDocument;
            obj.parent = this.currentElement;
            parseAttributesCore(obj, attributes);
            parseAttributesStyle(obj, attributes);
            parseAttributesTransform(obj, attributes);
            parseAttributesConditional(obj, attributes);
            parseAttributesTextPosition(obj, attributes);
            this.currentElement.addChild(obj);
            this.currentElement = obj;
            return;
        }
        throw new SAXException("Invalid document. Root element must be <svg>");
    }

    private void parseAttributesTextPosition(TextPositionedContainer obj, Attributes attributes) throws SAXException {
        for (int i = 0; i < attributes.getLength(); i++) {
            String val = attributes.getValue(i).trim();
            switch ($SWITCH_TABLE$com$caverock$androidsvg$SVGParser$SVGAttr()[SVGAttr.fromString(attributes.getLocalName(i)).ordinal()]) {
                case 10:
                    obj.dx = parseLengthList(val);
                    break;
                case 11:
                    obj.dy = parseLengthList(val);
                    break;
                case 83:
                    obj.x = parseLengthList(val);
                    break;
                case 84:
                    obj.y = parseLengthList(val);
                    break;
                default:
                    break;
            }
        }
    }

    private void tspan(Attributes attributes) throws SAXException {
        debug("<tspan>", new Object[0]);
        if (this.currentElement == null) {
            throw new SAXException("Invalid document. Root element must be <svg>");
        } else if (this.currentElement instanceof TextContainer) {
            TSpan obj = new TSpan();
            obj.document = this.svgDocument;
            obj.parent = this.currentElement;
            parseAttributesCore(obj, attributes);
            parseAttributesStyle(obj, attributes);
            parseAttributesConditional(obj, attributes);
            parseAttributesTextPosition(obj, attributes);
            this.currentElement.addChild(obj);
            this.currentElement = obj;
            if (obj.parent instanceof TextRoot) {
                obj.setTextRoot((TextRoot) obj.parent);
            } else {
                obj.setTextRoot(((TextChild) obj.parent).getTextRoot());
            }
        } else {
            throw new SAXException("Invalid document. <tspan> elements are only valid inside <text> or other <tspan> elements.");
        }
    }

    private void tref(Attributes attributes) throws SAXException {
        debug("<tref>", new Object[0]);
        if (this.currentElement == null) {
            throw new SAXException("Invalid document. Root element must be <svg>");
        } else if (this.currentElement instanceof TextContainer) {
            TRef obj = new TRef();
            obj.document = this.svgDocument;
            obj.parent = this.currentElement;
            parseAttributesCore(obj, attributes);
            parseAttributesStyle(obj, attributes);
            parseAttributesConditional(obj, attributes);
            parseAttributesTRef(obj, attributes);
            this.currentElement.addChild(obj);
            if (obj.parent instanceof TextRoot) {
                obj.setTextRoot((TextRoot) obj.parent);
            } else {
                obj.setTextRoot(((TextChild) obj.parent).getTextRoot());
            }
        } else {
            throw new SAXException("Invalid document. <tref> elements are only valid inside <text> or <tspan> elements.");
        }
    }

    private void parseAttributesTRef(TRef obj, Attributes attributes) throws SAXException {
        int i = 0;
        while (i < attributes.getLength()) {
            String val = attributes.getValue(i).trim();
            if ($SWITCH_TABLE$com$caverock$androidsvg$SVGParser$SVGAttr()[SVGAttr.fromString(attributes.getLocalName(i)).ordinal()] == 27 && XLINK_NAMESPACE.equals(attributes.getURI(i))) {
                obj.href = val;
            }
            i++;
        }
    }

    private void zwitch(Attributes attributes) throws SAXException {
        debug("<switch>", new Object[0]);
        if (this.currentElement != null) {
            Switch obj = new Switch();
            obj.document = this.svgDocument;
            obj.parent = this.currentElement;
            parseAttributesCore(obj, attributes);
            parseAttributesStyle(obj, attributes);
            parseAttributesTransform(obj, attributes);
            parseAttributesConditional(obj, attributes);
            this.currentElement.addChild(obj);
            this.currentElement = obj;
            return;
        }
        throw new SAXException("Invalid document. Root element must be <svg>");
    }

    private void parseAttributesConditional(SvgConditional obj, Attributes attributes) throws SAXException {
        for (int i = 0; i < attributes.getLength(); i++) {
            String val = attributes.getValue(i).trim();
            int i2 = $SWITCH_TABLE$com$caverock$androidsvg$SVGParser$SVGAttr()[SVGAttr.fromString(attributes.getLocalName(i)).ordinal()];
            if (i2 != 74) {
                switch (i2) {
                    case 53:
                        obj.setRequiredFeatures(parseRequiredFeatures(val));
                        break;
                    case 54:
                        obj.setRequiredExtensions(val);
                        break;
                    case 55:
                        obj.setRequiredFormats(parseRequiredFormats(val));
                        break;
                    case 56:
                        List<String> fonts = parseFontFamily(val);
                        obj.setRequiredFonts(fonts != null ? new HashSet(fonts) : new HashSet(0));
                        break;
                    default:
                        break;
                }
            }
            obj.setSystemLanguage(parseSystemLanguage(val));
        }
    }

    private void symbol(Attributes attributes) throws SAXException {
        debug("<symbol>", new Object[0]);
        if (this.currentElement != null) {
            Symbol obj = new Symbol();
            obj.document = this.svgDocument;
            obj.parent = this.currentElement;
            parseAttributesCore(obj, attributes);
            parseAttributesStyle(obj, attributes);
            parseAttributesConditional(obj, attributes);
            parseAttributesViewBox(obj, attributes);
            this.currentElement.addChild(obj);
            this.currentElement = obj;
            return;
        }
        throw new SAXException("Invalid document. Root element must be <svg>");
    }

    private void marker(Attributes attributes) throws SAXException {
        debug("<marker>", new Object[0]);
        if (this.currentElement != null) {
            Marker obj = new Marker();
            obj.document = this.svgDocument;
            obj.parent = this.currentElement;
            parseAttributesCore(obj, attributes);
            parseAttributesStyle(obj, attributes);
            parseAttributesConditional(obj, attributes);
            parseAttributesViewBox(obj, attributes);
            parseAttributesMarker(obj, attributes);
            this.currentElement.addChild(obj);
            this.currentElement = obj;
            return;
        }
        throw new SAXException("Invalid document. Root element must be <svg>");
    }

    private void parseAttributesMarker(Marker obj, Attributes attributes) throws SAXException {
        for (int i = 0; i < attributes.getLength(); i++) {
            String val = attributes.getValue(i).trim();
            int i2 = $SWITCH_TABLE$com$caverock$androidsvg$SVGParser$SVGAttr()[SVGAttr.fromString(attributes.getLocalName(i)).ordinal()];
            if (i2 != 42) {
                switch (i2) {
                    case 33:
                        obj.markerHeight = parseLength(val);
                        if (!obj.markerHeight.isNegative()) {
                            break;
                        }
                        throw new SAXException("Invalid <marker> element. markerHeight cannot be negative");
                    case 34:
                        if ("strokeWidth".equals(val)) {
                            obj.markerUnitsAreUser = false;
                            break;
                        } else if ("userSpaceOnUse".equals(val)) {
                            obj.markerUnitsAreUser = true;
                            break;
                        } else {
                            throw new SAXException("Invalid value for attribute markerUnits");
                        }
                    case 35:
                        obj.markerWidth = parseLength(val);
                        if (!obj.markerWidth.isNegative()) {
                            break;
                        }
                        throw new SAXException("Invalid <marker> element. markerWidth cannot be negative");
                    default:
                        switch (i2) {
                            case 51:
                                obj.refX = parseLength(val);
                                break;
                            case 52:
                                obj.refY = parseLength(val);
                                break;
                            default:
                                break;
                        }
                }
            } else if ("auto".equals(val)) {
                obj.orient = Float.valueOf(Float.NaN);
            } else {
                obj.orient = Float.valueOf(parseFloat(val));
            }
        }
    }

    private void linearGradient(Attributes attributes) throws SAXException {
        debug("<linearGradiant>", new Object[0]);
        if (this.currentElement != null) {
            SvgLinearGradient obj = new SvgLinearGradient();
            obj.document = this.svgDocument;
            obj.parent = this.currentElement;
            parseAttributesCore(obj, attributes);
            parseAttributesStyle(obj, attributes);
            parseAttributesGradient(obj, attributes);
            parseAttributesLinearGradient(obj, attributes);
            this.currentElement.addChild(obj);
            this.currentElement = obj;
            return;
        }
        throw new SAXException("Invalid document. Root element must be <svg>");
    }

    private void parseAttributesGradient(GradientElement obj, Attributes attributes) throws SAXException {
        for (int i = 0; i < attributes.getLength(); i++) {
            String val = attributes.getValue(i).trim();
            int i2 = $SWITCH_TABLE$com$caverock$androidsvg$SVGParser$SVGAttr()[SVGAttr.fromString(attributes.getLocalName(i)).ordinal()];
            if (i2 != 27) {
                if (i2 != 61) {
                    switch (i2) {
                        case 24:
                            obj.gradientTransform = parseTransformList(val);
                            break;
                        case 25:
                            if ("objectBoundingBox".equals(val)) {
                                obj.gradientUnitsAreUser = Boolean.valueOf(false);
                                break;
                            } else if ("userSpaceOnUse".equals(val)) {
                                obj.gradientUnitsAreUser = Boolean.valueOf(true);
                                break;
                            } else {
                                throw new SAXException("Invalid value for attribute gradientUnits");
                            }
                        default:
                            continue;
                    }
                } else {
                    try {
                        obj.spreadMethod = GradientSpread.valueOf(val);
                    } catch (IllegalArgumentException e) {
                        StringBuilder stringBuilder = new StringBuilder("Invalid spreadMethod attribute. \"");
                        stringBuilder.append(val);
                        stringBuilder.append("\" is not a valid value.");
                        throw new SAXException(stringBuilder.toString());
                    }
                }
            } else if (XLINK_NAMESPACE.equals(attributes.getURI(i))) {
                obj.href = val;
            }
        }
    }

    private void parseAttributesLinearGradient(SvgLinearGradient obj, Attributes attributes) throws SAXException {
        for (int i = 0; i < attributes.getLength(); i++) {
            String val = attributes.getValue(i).trim();
            switch ($SWITCH_TABLE$com$caverock$androidsvg$SVGParser$SVGAttr()[SVGAttr.fromString(attributes.getLocalName(i)).ordinal()]) {
                case 85:
                    obj.x1 = parseLength(val);
                    break;
                case 86:
                    obj.y1 = parseLength(val);
                    break;
                case 87:
                    obj.x2 = parseLength(val);
                    break;
                case 88:
                    obj.y2 = parseLength(val);
                    break;
                default:
                    break;
            }
        }
    }

    private void radialGradient(Attributes attributes) throws SAXException {
        debug("<radialGradient>", new Object[0]);
        if (this.currentElement != null) {
            SvgRadialGradient obj = new SvgRadialGradient();
            obj.document = this.svgDocument;
            obj.parent = this.currentElement;
            parseAttributesCore(obj, attributes);
            parseAttributesStyle(obj, attributes);
            parseAttributesGradient(obj, attributes);
            parseAttributesRadialGradient(obj, attributes);
            this.currentElement.addChild(obj);
            this.currentElement = obj;
            return;
        }
        throw new SAXException("Invalid document. Root element must be <svg>");
    }

    private void parseAttributesRadialGradient(SvgRadialGradient obj, Attributes attributes) throws SAXException {
        for (int i = 0; i < attributes.getLength(); i++) {
            String val = attributes.getValue(i).trim();
            switch ($SWITCH_TABLE$com$caverock$androidsvg$SVGParser$SVGAttr()[SVGAttr.fromString(attributes.getLocalName(i)).ordinal()]) {
                case 7:
                    obj.cx = parseLength(val);
                    break;
                case 8:
                    obj.cy = parseLength(val);
                    break;
                case 12:
                    obj.fx = parseLength(val);
                    break;
                case 13:
                    obj.fy = parseLength(val);
                    break;
                case 50:
                    obj.r = parseLength(val);
                    if (!obj.r.isNegative()) {
                        break;
                    }
                    throw new SAXException("Invalid <radialGradient> element. r cannot be negative");
                default:
                    break;
            }
        }
    }

    private void stop(Attributes attributes) throws SAXException {
        debug("<stop>", new Object[0]);
        if (this.currentElement == null) {
            throw new SAXException("Invalid document. Root element must be <svg>");
        } else if (this.currentElement instanceof GradientElement) {
            Stop obj = new Stop();
            obj.document = this.svgDocument;
            obj.parent = this.currentElement;
            parseAttributesCore(obj, attributes);
            parseAttributesStyle(obj, attributes);
            parseAttributesStop(obj, attributes);
            this.currentElement.addChild(obj);
            this.currentElement = obj;
        } else {
            throw new SAXException("Invalid document. <stop> elements are only valid inside <linearGradiant> or <radialGradient> elements.");
        }
    }

    private void parseAttributesStop(Stop obj, Attributes attributes) throws SAXException {
        for (int i = 0; i < attributes.getLength(); i++) {
            String val = attributes.getValue(i).trim();
            if ($SWITCH_TABLE$com$caverock$androidsvg$SVGParser$SVGAttr()[SVGAttr.fromString(attributes.getLocalName(i)).ordinal()] == 40) {
                obj.offset = parseGradiantOffset(val);
            }
        }
    }

    private Float parseGradiantOffset(String val) throws SAXException {
        if (val.length() != 0) {
            int end = val.length();
            boolean isPercent = false;
            if (val.charAt(val.length() - 1) == '%') {
                end--;
                isPercent = true;
            }
            try {
                float scalar = Float.parseFloat(val.substring(0, end));
                float f = 100.0f;
                if (isPercent) {
                    scalar /= 100.0f;
                }
                if (scalar < 0.0f) {
                    f = 0.0f;
                } else if (scalar <= 100.0f) {
                    f = scalar;
                }
                return Float.valueOf(f);
            } catch (NumberFormatException e) {
                StringBuilder stringBuilder = new StringBuilder("Invalid offset value in <stop>: ");
                stringBuilder.append(val);
                throw new SAXException(stringBuilder.toString(), e);
            }
        }
        throw new SAXException("Invalid offset value in <stop> (empty string)");
    }

    private void solidColor(Attributes attributes) throws SAXException {
        debug("<solidColor>", new Object[0]);
        if (this.currentElement != null) {
            SolidColor obj = new SolidColor();
            obj.document = this.svgDocument;
            obj.parent = this.currentElement;
            parseAttributesCore(obj, attributes);
            parseAttributesStyle(obj, attributes);
            this.currentElement.addChild(obj);
            this.currentElement = obj;
            return;
        }
        throw new SAXException("Invalid document. Root element must be <svg>");
    }

    private void clipPath(Attributes attributes) throws SAXException {
        debug("<clipPath>", new Object[0]);
        if (this.currentElement != null) {
            ClipPath obj = new ClipPath();
            obj.document = this.svgDocument;
            obj.parent = this.currentElement;
            parseAttributesCore(obj, attributes);
            parseAttributesStyle(obj, attributes);
            parseAttributesTransform(obj, attributes);
            parseAttributesConditional(obj, attributes);
            parseAttributesClipPath(obj, attributes);
            this.currentElement.addChild(obj);
            this.currentElement = obj;
            return;
        }
        throw new SAXException("Invalid document. Root element must be <svg>");
    }

    private void parseAttributesClipPath(ClipPath obj, Attributes attributes) throws SAXException {
        for (int i = 0; i < attributes.getLength(); i++) {
            String val = attributes.getValue(i).trim();
            if ($SWITCH_TABLE$com$caverock$androidsvg$SVGParser$SVGAttr()[SVGAttr.fromString(attributes.getLocalName(i)).ordinal()] == 4) {
                if ("objectBoundingBox".equals(val)) {
                    obj.clipPathUnitsAreUser = Boolean.valueOf(false);
                } else if ("userSpaceOnUse".equals(val)) {
                    obj.clipPathUnitsAreUser = Boolean.valueOf(true);
                } else {
                    throw new SAXException("Invalid value for attribute clipPathUnits");
                }
            }
        }
    }

    private void textPath(Attributes attributes) throws SAXException {
        debug("<textPath>", new Object[0]);
        if (this.currentElement != null) {
            TextPath obj = new TextPath();
            obj.document = this.svgDocument;
            obj.parent = this.currentElement;
            parseAttributesCore(obj, attributes);
            parseAttributesStyle(obj, attributes);
            parseAttributesConditional(obj, attributes);
            parseAttributesTextPath(obj, attributes);
            this.currentElement.addChild(obj);
            this.currentElement = obj;
            if (obj.parent instanceof TextRoot) {
                obj.setTextRoot((TextRoot) obj.parent);
                return;
            } else {
                obj.setTextRoot(((TextChild) obj.parent).getTextRoot());
                return;
            }
        }
        throw new SAXException("Invalid document. Root element must be <svg>");
    }

    private void parseAttributesTextPath(TextPath obj, Attributes attributes) throws SAXException {
        for (int i = 0; i < attributes.getLength(); i++) {
            String val = attributes.getValue(i).trim();
            int i2 = $SWITCH_TABLE$com$caverock$androidsvg$SVGParser$SVGAttr()[SVGAttr.fromString(attributes.getLocalName(i)).ordinal()];
            if (i2 != 27) {
                if (i2 == 62) {
                    obj.startOffset = parseLength(val);
                }
            } else if (XLINK_NAMESPACE.equals(attributes.getURI(i))) {
                obj.href = val;
            }
        }
    }

    private void pattern(Attributes attributes) throws SAXException {
        debug("<pattern>", new Object[0]);
        if (this.currentElement != null) {
            Pattern obj = new Pattern();
            obj.document = this.svgDocument;
            obj.parent = this.currentElement;
            parseAttributesCore(obj, attributes);
            parseAttributesStyle(obj, attributes);
            parseAttributesConditional(obj, attributes);
            parseAttributesViewBox(obj, attributes);
            parseAttributesPattern(obj, attributes);
            this.currentElement.addChild(obj);
            this.currentElement = obj;
            return;
        }
        throw new SAXException("Invalid document. Root element must be <svg>");
    }

    private void parseAttributesPattern(Pattern obj, Attributes attributes) throws SAXException {
        for (int i = 0; i < attributes.getLength(); i++) {
            String val = attributes.getValue(i).trim();
            switch ($SWITCH_TABLE$com$caverock$androidsvg$SVGParser$SVGAttr()[SVGAttr.fromString(attributes.getLocalName(i)).ordinal()]) {
                case 26:
                    obj.height = parseLength(val);
                    if (!obj.height.isNegative()) {
                        break;
                    }
                    throw new SAXException("Invalid <pattern> element. height cannot be negative");
                case 27:
                    if (!XLINK_NAMESPACE.equals(attributes.getURI(i))) {
                        break;
                    }
                    obj.href = val;
                    break;
                case 45:
                    if ("objectBoundingBox".equals(val)) {
                        obj.patternContentUnitsAreUser = Boolean.valueOf(false);
                        break;
                    } else if ("userSpaceOnUse".equals(val)) {
                        obj.patternContentUnitsAreUser = Boolean.valueOf(true);
                        break;
                    } else {
                        throw new SAXException("Invalid value for attribute patternContentUnits");
                    }
                case 46:
                    obj.patternTransform = parseTransformList(val);
                    break;
                case 47:
                    if ("objectBoundingBox".equals(val)) {
                        obj.patternUnitsAreUser = Boolean.valueOf(false);
                        break;
                    } else if ("userSpaceOnUse".equals(val)) {
                        obj.patternUnitsAreUser = Boolean.valueOf(true);
                        break;
                    } else {
                        throw new SAXException("Invalid value for attribute patternUnits");
                    }
                case 82:
                    obj.width = parseLength(val);
                    if (!obj.width.isNegative()) {
                        break;
                    }
                    throw new SAXException("Invalid <pattern> element. width cannot be negative");
                case 83:
                    obj.x = parseLength(val);
                    break;
                case 84:
                    obj.y = parseLength(val);
                    break;
                default:
                    break;
            }
        }
    }

    private void view(Attributes attributes) throws SAXException {
        debug("<view>", new Object[0]);
        if (this.currentElement != null) {
            View obj = new View();
            obj.document = this.svgDocument;
            obj.parent = this.currentElement;
            parseAttributesCore(obj, attributes);
            parseAttributesConditional(obj, attributes);
            parseAttributesViewBox(obj, attributes);
            this.currentElement.addChild(obj);
            this.currentElement = obj;
            return;
        }
        throw new SAXException("Invalid document. Root element must be <svg>");
    }

    private void mask(Attributes attributes) throws SAXException {
        debug("<mask>", new Object[0]);
        if (this.currentElement != null) {
            Mask obj = new Mask();
            obj.document = this.svgDocument;
            obj.parent = this.currentElement;
            parseAttributesCore(obj, attributes);
            parseAttributesStyle(obj, attributes);
            parseAttributesConditional(obj, attributes);
            parseAttributesMask(obj, attributes);
            this.currentElement.addChild(obj);
            this.currentElement = obj;
            return;
        }
        throw new SAXException("Invalid document. Root element must be <svg>");
    }

    private void parseAttributesMask(Mask obj, Attributes attributes) throws SAXException {
        for (int i = 0; i < attributes.getLength(); i++) {
            String val = attributes.getValue(i).trim();
            int i2 = $SWITCH_TABLE$com$caverock$androidsvg$SVGParser$SVGAttr()[SVGAttr.fromString(attributes.getLocalName(i)).ordinal()];
            if (i2 != 26) {
                switch (i2) {
                    case 37:
                        if ("objectBoundingBox".equals(val)) {
                            obj.maskContentUnitsAreUser = Boolean.valueOf(false);
                            break;
                        } else if ("userSpaceOnUse".equals(val)) {
                            obj.maskContentUnitsAreUser = Boolean.valueOf(true);
                            break;
                        } else {
                            throw new SAXException("Invalid value for attribute maskContentUnits");
                        }
                    case 38:
                        if ("objectBoundingBox".equals(val)) {
                            obj.maskUnitsAreUser = Boolean.valueOf(false);
                            break;
                        } else if ("userSpaceOnUse".equals(val)) {
                            obj.maskUnitsAreUser = Boolean.valueOf(true);
                            break;
                        } else {
                            throw new SAXException("Invalid value for attribute maskUnits");
                        }
                    default:
                        switch (i2) {
                            case 82:
                                obj.width = parseLength(val);
                                if (!obj.width.isNegative()) {
                                    break;
                                }
                                throw new SAXException("Invalid <mask> element. width cannot be negative");
                            case 83:
                                obj.x = parseLength(val);
                                break;
                            case 84:
                                obj.y = parseLength(val);
                                break;
                            default:
                                continue;
                        }
                }
            } else {
                obj.height = parseLength(val);
                if (obj.height.isNegative()) {
                    throw new SAXException("Invalid <mask> element. height cannot be negative");
                }
            }
        }
    }

    private void parseAttributesCore(SvgElementBase obj, Attributes attributes) throws SAXException {
        int i = 0;
        while (i < attributes.getLength()) {
            String qname = attributes.getQName(i);
            if (qname.equals(ZoneGetter.KEY_ID) || qname.equals("xml:id")) {
                obj.id = attributes.getValue(i).trim();
                return;
            } else if (qname.equals("xml:space")) {
                String val = attributes.getValue(i).trim();
                if ("default".equals(val)) {
                    obj.spacePreserve = Boolean.FALSE;
                    return;
                } else if ("preserve".equals(val)) {
                    obj.spacePreserve = Boolean.TRUE;
                    return;
                } else {
                    StringBuilder stringBuilder = new StringBuilder("Invalid value for \"xml:space\" attribute: ");
                    stringBuilder.append(val);
                    throw new SAXException(stringBuilder.toString());
                }
            } else {
                i++;
            }
        }
    }

    private void parseAttributesStyle(SvgElementBase obj, Attributes attributes) throws SAXException {
        for (int i = 0; i < attributes.getLength(); i++) {
            String val = attributes.getValue(i).trim();
            if (val.length() != 0) {
                int i2 = $SWITCH_TABLE$com$caverock$androidsvg$SVGParser$SVGAttr()[SVGAttr.fromString(attributes.getLocalName(i)).ordinal()];
                if (i2 == 1) {
                    obj.classNames = CSSParser.parseClassAttribute(val);
                } else if (i2 != 73) {
                    if (obj.baseStyle == null) {
                        obj.baseStyle = new Style();
                    }
                    processStyleProperty(obj.baseStyle, attributes.getLocalName(i), attributes.getValue(i).trim());
                } else {
                    parseStyle(obj, val);
                }
            }
        }
    }

    private static void parseStyle(SvgElementBase obj, String style) throws SAXException {
        TextScanner scan = new TextScanner(style.replaceAll("/\\*.*?\\*/", ""));
        while (true) {
            String propertyName = scan.nextToken(AccessibilityUtils.ENABLED_ACCESSIBILITY_SERVICES_SEPARATOR);
            scan.skipWhitespace();
            if (scan.consume((char) AccessibilityUtils.ENABLED_ACCESSIBILITY_SERVICES_SEPARATOR)) {
                scan.skipWhitespace();
                String propertyValue = scan.nextToken(';');
                if (propertyValue != null) {
                    scan.skipWhitespace();
                    if (scan.empty() || scan.consume(';')) {
                        if (obj.style == null) {
                            obj.style = new Style();
                        }
                        processStyleProperty(obj.style, propertyName, propertyValue);
                        scan.skipWhitespace();
                    }
                } else {
                    return;
                }
            }
            return;
        }
    }

    protected static void processStyleProperty(Style style, String localName, String val) throws SAXException {
        if (val.length() != 0 && !val.equals("inherit")) {
            int i = $SWITCH_TABLE$com$caverock$androidsvg$SVGParser$SVGAttr()[SVGAttr.fromString(localName).ordinal()];
            switch (i) {
                case 2:
                    style.clip = parseClip(val);
                    style.specifiedFlags |= 1048576;
                    break;
                case 3:
                    style.clipPath = parseFunctionalIRI(val, localName);
                    style.specifiedFlags |= 268435456;
                    break;
                default:
                    switch (i) {
                        case 5:
                            style.clipRule = parseFillRule(val);
                            style.specifiedFlags |= 536870912;
                            break;
                        case 6:
                            style.color = parseColour(val);
                            style.specifiedFlags |= PlaybackStateCompat.ACTION_SKIP_TO_QUEUE_ITEM;
                            break;
                        default:
                            String str;
                            StringBuilder stringBuilder;
                            StringBuilder stringBuilder2;
                            switch (i) {
                                case 15:
                                    if (val.indexOf(124) < 0) {
                                        str = VALID_DISPLAY_VALUES;
                                        stringBuilder = new StringBuilder(String.valueOf('|'));
                                        stringBuilder.append(val);
                                        stringBuilder.append('|');
                                        if (str.indexOf(stringBuilder.toString()) != -1) {
                                            style.display = Boolean.valueOf(val.equals(NONE) ^ 1);
                                            style.specifiedFlags |= 16777216;
                                            break;
                                        }
                                    }
                                    stringBuilder2 = new StringBuilder("Invalid value for \"display\" attribute: ");
                                    stringBuilder2.append(val);
                                    throw new SAXException(stringBuilder2.toString());
                                case 16:
                                    style.fill = parsePaintSpecifier(val, "fill");
                                    style.specifiedFlags |= 1;
                                    break;
                                case 17:
                                    style.fillRule = parseFillRule(val);
                                    style.specifiedFlags |= 2;
                                    break;
                                case 18:
                                    style.fillOpacity = Float.valueOf(parseOpacity(val));
                                    style.specifiedFlags |= 4;
                                    break;
                                case 19:
                                    parseFont(style, val);
                                    break;
                                case 20:
                                    style.fontFamily = parseFontFamily(val);
                                    style.specifiedFlags |= PlaybackStateCompat.ACTION_PLAY_FROM_URI;
                                    break;
                                case 21:
                                    style.fontSize = parseFontSize(val);
                                    style.specifiedFlags |= PlaybackStateCompat.ACTION_PREPARE;
                                    break;
                                case 22:
                                    style.fontWeight = parseFontWeight(val);
                                    style.specifiedFlags |= PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID;
                                    break;
                                case 23:
                                    style.fontStyle = parseFontStyle(val);
                                    style.specifiedFlags |= PlaybackStateCompat.ACTION_PREPARE_FROM_SEARCH;
                                    break;
                                default:
                                    switch (i) {
                                        case 29:
                                            style.markerStart = parseFunctionalIRI(val, localName);
                                            style.markerMid = style.markerStart;
                                            style.markerEnd = style.markerStart;
                                            style.specifiedFlags |= 14680064;
                                            break;
                                        case 30:
                                            style.markerStart = parseFunctionalIRI(val, localName);
                                            style.specifiedFlags |= PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE;
                                            break;
                                        case 31:
                                            style.markerMid = parseFunctionalIRI(val, localName);
                                            style.specifiedFlags |= 4194304;
                                            break;
                                        case 32:
                                            style.markerEnd = parseFunctionalIRI(val, localName);
                                            style.specifiedFlags |= 8388608;
                                            break;
                                        default:
                                            switch (i) {
                                                case 59:
                                                    if (val.equals(CURRENTCOLOR)) {
                                                        style.solidColor = CurrentColor.getInstance();
                                                    } else {
                                                        style.solidColor = parseColour(val);
                                                    }
                                                    style.specifiedFlags |= 2147483648L;
                                                    break;
                                                case 60:
                                                    style.solidOpacity = Float.valueOf(parseOpacity(val));
                                                    style.specifiedFlags |= 4294967296L;
                                                    break;
                                                default:
                                                    switch (i) {
                                                        case 63:
                                                            if (val.equals(CURRENTCOLOR)) {
                                                                style.stopColor = CurrentColor.getInstance();
                                                            } else {
                                                                style.stopColor = parseColour(val);
                                                            }
                                                            style.specifiedFlags |= 67108864;
                                                            break;
                                                        case 64:
                                                            style.stopOpacity = Float.valueOf(parseOpacity(val));
                                                            style.specifiedFlags |= 134217728;
                                                            break;
                                                        case 65:
                                                            style.stroke = parsePaintSpecifier(val, "stroke");
                                                            style.specifiedFlags |= 8;
                                                            break;
                                                        case 66:
                                                            if (NONE.equals(val)) {
                                                                style.strokeDashArray = null;
                                                            } else {
                                                                style.strokeDashArray = parseStrokeDashArray(val);
                                                            }
                                                            style.specifiedFlags |= 512;
                                                            break;
                                                        case 67:
                                                            style.strokeDashOffset = parseLength(val);
                                                            style.specifiedFlags |= PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID;
                                                            break;
                                                        case 68:
                                                            style.strokeLineCap = parseStrokeLineCap(val);
                                                            style.specifiedFlags |= 64;
                                                            break;
                                                        case 69:
                                                            style.strokeLineJoin = parseStrokeLineJoin(val);
                                                            style.specifiedFlags |= 128;
                                                            break;
                                                        case 70:
                                                            style.strokeMiterLimit = Float.valueOf(parseFloat(val));
                                                            style.specifiedFlags |= 256;
                                                            break;
                                                        case 71:
                                                            style.strokeOpacity = Float.valueOf(parseOpacity(val));
                                                            style.specifiedFlags |= 16;
                                                            break;
                                                        case 72:
                                                            style.strokeWidth = parseLength(val);
                                                            style.specifiedFlags |= 32;
                                                            break;
                                                        default:
                                                            switch (i) {
                                                                case 75:
                                                                    style.textAnchor = parseTextAnchor(val);
                                                                    style.specifiedFlags |= PlaybackStateCompat.ACTION_SET_REPEAT_MODE;
                                                                    break;
                                                                case 76:
                                                                    style.textDecoration = parseTextDecoration(val);
                                                                    style.specifiedFlags |= PlaybackStateCompat.ACTION_PREPARE_FROM_URI;
                                                                    break;
                                                                default:
                                                                    switch (i) {
                                                                        case 89:
                                                                            if (val.equals(CURRENTCOLOR)) {
                                                                                style.viewportFill = CurrentColor.getInstance();
                                                                            } else {
                                                                                style.viewportFill = parseColour(val);
                                                                            }
                                                                            style.specifiedFlags |= 8589934592L;
                                                                            break;
                                                                        case 90:
                                                                            style.viewportFillOpacity = Float.valueOf(parseOpacity(val));
                                                                            style.specifiedFlags |= 17179869184L;
                                                                            break;
                                                                        case 91:
                                                                            if (val.indexOf(124) < 0) {
                                                                                str = VALID_VISIBILITY_VALUES;
                                                                                stringBuilder = new StringBuilder(String.valueOf('|'));
                                                                                stringBuilder.append(val);
                                                                                stringBuilder.append('|');
                                                                                if (str.indexOf(stringBuilder.toString()) != -1) {
                                                                                    style.visibility = Boolean.valueOf(val.equals("visible"));
                                                                                    style.specifiedFlags |= 33554432;
                                                                                    break;
                                                                                }
                                                                            }
                                                                            stringBuilder2 = new StringBuilder("Invalid value for \"visibility\" attribute: ");
                                                                            stringBuilder2.append(val);
                                                                            throw new SAXException(stringBuilder2.toString());
                                                                        default:
                                                                            switch (i) {
                                                                                case 9:
                                                                                    style.direction = parseTextDirection(val);
                                                                                    style.specifiedFlags |= 68719476736L;
                                                                                    break;
                                                                                case 36:
                                                                                    style.mask = parseFunctionalIRI(val, localName);
                                                                                    style.specifiedFlags |= BillingCycleSettings.GIB_IN_BYTES;
                                                                                    break;
                                                                                case 41:
                                                                                    style.opacity = Float.valueOf(parseOpacity(val));
                                                                                    style.specifiedFlags |= PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH;
                                                                                    break;
                                                                                case 43:
                                                                                    style.overflow = parseOverflow(val);
                                                                                    style.specifiedFlags |= PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE_ENABLED;
                                                                                    break;
                                                                                case 79:
                                                                                    style.vectorEffect = parseVectorEffect(val);
                                                                                    style.specifiedFlags |= 34359738368L;
                                                                                    break;
                                                                            }
                                                                            break;
                                                                    }
                                                            }
                                                    }
                                            }
                                    }
                            }
                    }
            }
        }
    }

    private void parseAttributesViewBox(SvgViewBoxContainer obj, Attributes attributes) throws SAXException {
        for (int i = 0; i < attributes.getLength(); i++) {
            String val = attributes.getValue(i).trim();
            int i2 = $SWITCH_TABLE$com$caverock$androidsvg$SVGParser$SVGAttr()[SVGAttr.fromString(attributes.getLocalName(i)).ordinal()];
            if (i2 == 49) {
                parsePreserveAspectRatio(obj, val);
            } else if (i2 == 81) {
                obj.viewBox = parseViewBox(val);
            }
        }
    }

    private void parseAttributesTransform(HasTransform obj, Attributes attributes) throws SAXException {
        for (int i = 0; i < attributes.getLength(); i++) {
            if (SVGAttr.fromString(attributes.getLocalName(i)) == SVGAttr.transform) {
                obj.setTransform(parseTransformList(attributes.getValue(i)));
            }
        }
    }

    private Matrix parseTransformList(String val) throws SAXException {
        Matrix matrix = new Matrix();
        TextScanner scan = new TextScanner(val);
        scan.skipWhitespace();
        while (!scan.empty()) {
            String cmd = scan.nextFunction();
            StringBuilder stringBuilder;
            if (cmd != null) {
                Float a;
                Float b;
                StringBuilder stringBuilder2;
                Float sy;
                if (cmd.equals("matrix")) {
                    scan.skipWhitespace();
                    a = scan.nextFloat();
                    scan.skipCommaWhitespace();
                    b = scan.nextFloat();
                    scan.skipCommaWhitespace();
                    Float c = scan.nextFloat();
                    scan.skipCommaWhitespace();
                    Float d = scan.nextFloat();
                    scan.skipCommaWhitespace();
                    Float e = scan.nextFloat();
                    scan.skipCommaWhitespace();
                    Float f = scan.nextFloat();
                    scan.skipWhitespace();
                    if (f == null || !scan.consume(')')) {
                        stringBuilder2 = new StringBuilder("Invalid transform list: ");
                        stringBuilder2.append(val);
                        throw new SAXException(stringBuilder2.toString());
                    }
                    Matrix m = new Matrix();
                    m.setValues(new float[]{a.floatValue(), c.floatValue(), e.floatValue(), b.floatValue(), d.floatValue(), f.floatValue(), 0.0f, 0.0f, 1.0f});
                    matrix.preConcat(m);
                } else if (cmd.equals("translate")) {
                    scan.skipWhitespace();
                    a = scan.nextFloat();
                    b = scan.possibleNextFloat();
                    scan.skipWhitespace();
                    if (a == null || !scan.consume(')')) {
                        stringBuilder2 = new StringBuilder("Invalid transform list: ");
                        stringBuilder2.append(val);
                        throw new SAXException(stringBuilder2.toString());
                    } else if (b == null) {
                        matrix.preTranslate(a.floatValue(), 0.0f);
                    } else {
                        matrix.preTranslate(a.floatValue(), b.floatValue());
                    }
                } else if (cmd.equals("scale")) {
                    scan.skipWhitespace();
                    a = scan.nextFloat();
                    sy = scan.possibleNextFloat();
                    scan.skipWhitespace();
                    if (a == null || !scan.consume(')')) {
                        StringBuilder stringBuilder3 = new StringBuilder("Invalid transform list: ");
                        stringBuilder3.append(val);
                        throw new SAXException(stringBuilder3.toString());
                    } else if (sy == null) {
                        matrix.preScale(a.floatValue(), a.floatValue());
                    } else {
                        matrix.preScale(a.floatValue(), sy.floatValue());
                    }
                } else if (cmd.equals("rotate")) {
                    scan.skipWhitespace();
                    a = scan.nextFloat();
                    sy = scan.possibleNextFloat();
                    b = scan.possibleNextFloat();
                    scan.skipWhitespace();
                    StringBuilder stringBuilder4;
                    if (a == null || !scan.consume(')')) {
                        stringBuilder4 = new StringBuilder("Invalid transform list: ");
                        stringBuilder4.append(val);
                        throw new SAXException(stringBuilder4.toString());
                    } else if (sy == null) {
                        matrix.preRotate(a.floatValue());
                    } else if (b != null) {
                        matrix.preRotate(a.floatValue(), sy.floatValue(), b.floatValue());
                    } else {
                        stringBuilder4 = new StringBuilder("Invalid transform list: ");
                        stringBuilder4.append(val);
                        throw new SAXException(stringBuilder4.toString());
                    }
                } else if (cmd.equals("skewX")) {
                    scan.skipWhitespace();
                    a = scan.nextFloat();
                    scan.skipWhitespace();
                    if (a == null || !scan.consume(')')) {
                        stringBuilder2 = new StringBuilder("Invalid transform list: ");
                        stringBuilder2.append(val);
                        throw new SAXException(stringBuilder2.toString());
                    }
                    matrix.preSkew((float) Math.tan(Math.toRadians((double) a.floatValue())), 0.0f);
                } else if (cmd.equals("skewY")) {
                    scan.skipWhitespace();
                    a = scan.nextFloat();
                    scan.skipWhitespace();
                    if (a == null || !scan.consume(')')) {
                        stringBuilder2 = new StringBuilder("Invalid transform list: ");
                        stringBuilder2.append(val);
                        throw new SAXException(stringBuilder2.toString());
                    }
                    matrix.preSkew(0.0f, (float) Math.tan(Math.toRadians((double) a.floatValue())));
                } else if (cmd != null) {
                    stringBuilder = new StringBuilder("Invalid transform list fn: ");
                    stringBuilder.append(cmd);
                    stringBuilder.append(")");
                    throw new SAXException(stringBuilder.toString());
                }
                if (scan.empty()) {
                    break;
                }
                scan.skipCommaWhitespace();
            } else {
                stringBuilder = new StringBuilder("Bad transform function encountered in transform list: ");
                stringBuilder.append(val);
                throw new SAXException(stringBuilder.toString());
            }
        }
        return matrix;
    }

    protected static Length parseLength(String val) throws SAXException {
        if (val.length() != 0) {
            int end = val.length();
            Unit unit = Unit.px;
            char lastChar = val.charAt(end - 1);
            if (lastChar == '%') {
                end--;
                unit = Unit.percent;
            } else if (end > 2 && Character.isLetter(lastChar) && Character.isLetter(val.charAt(end - 2))) {
                end -= 2;
                try {
                    unit = Unit.valueOf(val.substring(end).toLowerCase(Locale.US));
                } catch (IllegalArgumentException e) {
                    StringBuilder stringBuilder = new StringBuilder("Invalid length unit specifier: ");
                    stringBuilder.append(val);
                    throw new SAXException(stringBuilder.toString());
                }
            }
            try {
                return new Length(Float.parseFloat(val.substring(0, end)), unit);
            } catch (NumberFormatException e2) {
                StringBuilder stringBuilder2 = new StringBuilder("Invalid length value: ");
                stringBuilder2.append(val);
                throw new SAXException(stringBuilder2.toString(), e2);
            }
        }
        throw new SAXException("Invalid length value (empty string)");
    }

    private static List<Length> parseLengthList(String val) throws SAXException {
        if (val.length() != 0) {
            List<Length> coords = new ArrayList(1);
            TextScanner scan = new TextScanner(val);
            scan.skipWhitespace();
            while (!scan.empty()) {
                Float scalar = scan.nextFloat();
                if (scalar != null) {
                    Unit unit = scan.nextUnit();
                    if (unit == null) {
                        unit = Unit.px;
                    }
                    coords.add(new Length(scalar.floatValue(), unit));
                    scan.skipCommaWhitespace();
                } else {
                    StringBuilder stringBuilder = new StringBuilder("Invalid length list value: ");
                    stringBuilder.append(scan.ahead());
                    throw new SAXException(stringBuilder.toString());
                }
            }
            return coords;
        }
        throw new SAXException("Invalid length list (empty string)");
    }

    private static float parseFloat(String val) throws SAXException {
        if (val.length() != 0) {
            try {
                return Float.parseFloat(val);
            } catch (NumberFormatException e) {
                StringBuilder stringBuilder = new StringBuilder("Invalid float value: ");
                stringBuilder.append(val);
                throw new SAXException(stringBuilder.toString(), e);
            }
        }
        throw new SAXException("Invalid float value (empty string)");
    }

    private static float parseOpacity(String val) throws SAXException {
        float o = parseFloat(val);
        if (o < 0.0f) {
            return 0.0f;
        }
        return o > 1.0f ? 1.0f : o;
    }

    private static Box parseViewBox(String val) throws SAXException {
        TextScanner scan = new TextScanner(val);
        scan.skipWhitespace();
        Float minX = scan.nextFloat();
        scan.skipCommaWhitespace();
        Float minY = scan.nextFloat();
        scan.skipCommaWhitespace();
        Float width = scan.nextFloat();
        scan.skipCommaWhitespace();
        Float height = scan.nextFloat();
        if (minX == null || minY == null || width == null || height == null) {
            throw new SAXException("Invalid viewBox definition - should have four numbers");
        } else if (width.floatValue() < 0.0f) {
            throw new SAXException("Invalid viewBox. width cannot be negative");
        } else if (height.floatValue() >= 0.0f) {
            return new Box(minX.floatValue(), minY.floatValue(), width.floatValue(), height.floatValue());
        } else {
            throw new SAXException("Invalid viewBox. height cannot be negative");
        }
    }

    private static void parsePreserveAspectRatio(SvgPreserveAspectRatioContainer obj, String val) throws SAXException {
        TextScanner scan = new TextScanner(val);
        scan.skipWhitespace();
        Scale scale = null;
        String word = scan.nextToken();
        if ("defer".equals(word)) {
            scan.skipWhitespace();
            word = scan.nextToken();
        }
        Alignment align = (Alignment) aspectRatioKeywords.get(word);
        scan.skipWhitespace();
        if (!scan.empty()) {
            String meetOrSlice = scan.nextToken();
            if (meetOrSlice.equals("meet")) {
                scale = Scale.Meet;
            } else if (meetOrSlice.equals("slice")) {
                scale = Scale.Slice;
            } else {
                StringBuilder stringBuilder = new StringBuilder("Invalid preserveAspectRatio definition: ");
                stringBuilder.append(val);
                throw new SAXException(stringBuilder.toString());
            }
        }
        obj.preserveAspectRatio = new PreserveAspectRatio(align, scale);
    }

    private static SvgPaint parsePaintSpecifier(String val, String attrName) throws SAXException {
        if (!val.startsWith("url(")) {
            return parseColourSpecifer(val);
        }
        int closeBracket = val.indexOf(")");
        if (closeBracket != -1) {
            String href = val.substring(4, closeBracket).trim();
            SvgPaint fallback = null;
            val = val.substring(closeBracket + 1).trim();
            if (val.length() > 0) {
                fallback = parseColourSpecifer(val);
            }
            return new PaintReference(href, fallback);
        }
        StringBuilder stringBuilder = new StringBuilder("Bad ");
        stringBuilder.append(attrName);
        stringBuilder.append(" attribute. Unterminated url() reference");
        throw new SAXException(stringBuilder.toString());
    }

    private static SvgPaint parseColourSpecifer(String val) throws SAXException {
        if (val.equals(NONE)) {
            return null;
        }
        if (val.equals(CURRENTCOLOR)) {
            return CurrentColor.getInstance();
        }
        return parseColour(val);
    }

    private static Colour parseColour(String val) throws SAXException {
        int h1;
        int h2;
        int h3;
        if (val.charAt(0) == '#') {
            try {
                if (val.length() == 7) {
                    return new Colour(Integer.parseInt(val.substring(1), 16));
                }
                if (val.length() == 4) {
                    int threehex = Integer.parseInt(val.substring(1), 16);
                    h1 = threehex & 3840;
                    h2 = threehex & 240;
                    h3 = threehex & 15;
                    return new Colour((((((h1 << 16) | (h1 << 12)) | (h2 << 8)) | (h2 << 4)) | (h3 << 4)) | h3);
                }
                StringBuilder stringBuilder = new StringBuilder("Bad hex colour value: ");
                stringBuilder.append(val);
                throw new SAXException(stringBuilder.toString());
            } catch (NumberFormatException e) {
                StringBuilder stringBuilder2 = new StringBuilder("Bad colour value: ");
                stringBuilder2.append(val);
                throw new SAXException(stringBuilder2.toString());
            }
        } else if (!val.toLowerCase(Locale.US).startsWith("rgb(")) {
            return parseColourKeyword(val);
        } else {
            TextScanner scan = new TextScanner(val.substring(4));
            scan.skipWhitespace();
            h1 = parseColourComponent(scan);
            scan.skipCommaWhitespace();
            h2 = parseColourComponent(scan);
            scan.skipCommaWhitespace();
            h3 = parseColourComponent(scan);
            scan.skipWhitespace();
            if (scan.consume(')')) {
                return new Colour(((h1 << 16) | (h2 << 8)) | h3);
            }
            StringBuilder stringBuilder3 = new StringBuilder("Bad rgb() colour value: ");
            stringBuilder3.append(val);
            throw new SAXException(stringBuilder3.toString());
        }
    }

    private static int parseColourComponent(TextScanner scan) throws SAXException {
        int comp = scan.nextInteger().intValue();
        int i = 0;
        if (scan.consume('%')) {
            if (comp >= 0) {
                i = comp > 100 ? 100 : comp;
            }
            return (i * 255) / 100;
        }
        int i2 = 255;
        if (comp < 0) {
            i2 = 0;
        } else if (comp <= 255) {
            i2 = comp;
        }
        return i2;
    }

    private static Colour parseColourKeyword(String name) throws SAXException {
        Integer col = (Integer) colourKeywords.get(name.toLowerCase(Locale.US));
        if (col != null) {
            return new Colour(col.intValue());
        }
        StringBuilder stringBuilder = new StringBuilder("Invalid colour keyword: ");
        stringBuilder.append(name);
        throw new SAXException(stringBuilder.toString());
    }

    private static void parseFont(Style style, String val) throws SAXException {
        Integer fontWeight = null;
        FontStyle fontStyle = null;
        String fontVariant = null;
        StringBuilder stringBuilder = new StringBuilder(String.valueOf('|'));
        stringBuilder.append(val);
        stringBuilder.append('|');
        if ("|caption|icon|menu|message-box|small-caption|status-bar|".indexOf(stringBuilder.toString()) == -1) {
            String item;
            TextScanner scan = new TextScanner(val);
            while (true) {
                item = scan.nextToken('/');
                scan.skipWhitespace();
                if (item != null) {
                    if (fontWeight == null || fontStyle == null) {
                        if (!item.equals("normal")) {
                            if (fontWeight == null) {
                                fontWeight = (Integer) fontWeightKeywords.get(item);
                                if (fontWeight != null) {
                                }
                            }
                            if (fontStyle == null) {
                                fontStyle = (FontStyle) fontStyleKeywords.get(item);
                                if (fontStyle != null) {
                                }
                            }
                            if (fontVariant != null || !item.equals("small-caps")) {
                                break;
                            }
                            fontVariant = item;
                        }
                    } else {
                        break;
                    }
                }
                throw new SAXException("Invalid font style attribute: missing font size and family");
            }
            Length fontSize = parseFontSize(item);
            if (scan.consume('/')) {
                scan.skipWhitespace();
                item = scan.nextToken();
                if (item != null) {
                    parseLength(item);
                    scan.skipWhitespace();
                } else {
                    throw new SAXException("Invalid font style attribute: missing line-height");
                }
            }
            style.fontFamily = parseFontFamily(scan.restOfText());
            style.fontSize = fontSize;
            style.fontWeight = Integer.valueOf(fontWeight == null ? 400 : fontWeight.intValue());
            style.fontStyle = fontStyle == null ? FontStyle.Normal : fontStyle;
            style.specifiedFlags |= 122880;
        }
    }

    private static List<String> parseFontFamily(String val) throws SAXException {
        List<String> fonts = null;
        TextScanner scan = new TextScanner(val);
        do {
            String item = scan.nextQuotedString();
            if (item == null) {
                item = scan.nextToken(',');
            }
            if (item == null) {
                break;
            }
            if (fonts == null) {
                fonts = new ArrayList();
            }
            fonts.add(item);
            scan.skipCommaWhitespace();
        } while (!scan.empty());
        return fonts;
    }

    private static Length parseFontSize(String val) throws SAXException {
        Length size = (Length) fontSizeKeywords.get(val);
        if (size == null) {
            return parseLength(val);
        }
        return size;
    }

    private static Integer parseFontWeight(String val) throws SAXException {
        Integer wt = (Integer) fontWeightKeywords.get(val);
        if (wt != null) {
            return wt;
        }
        StringBuilder stringBuilder = new StringBuilder("Invalid font-weight property: ");
        stringBuilder.append(val);
        throw new SAXException(stringBuilder.toString());
    }

    private static FontStyle parseFontStyle(String val) throws SAXException {
        FontStyle fs = (FontStyle) fontStyleKeywords.get(val);
        if (fs != null) {
            return fs;
        }
        StringBuilder stringBuilder = new StringBuilder("Invalid font-style property: ");
        stringBuilder.append(val);
        throw new SAXException(stringBuilder.toString());
    }

    private static TextDecoration parseTextDecoration(String val) throws SAXException {
        if (NONE.equals(val)) {
            return TextDecoration.None;
        }
        if ("underline".equals(val)) {
            return TextDecoration.Underline;
        }
        if ("overline".equals(val)) {
            return TextDecoration.Overline;
        }
        if ("line-through".equals(val)) {
            return TextDecoration.LineThrough;
        }
        if ("blink".equals(val)) {
            return TextDecoration.Blink;
        }
        StringBuilder stringBuilder = new StringBuilder("Invalid text-decoration property: ");
        stringBuilder.append(val);
        throw new SAXException(stringBuilder.toString());
    }

    private static TextDirection parseTextDirection(String val) throws SAXException {
        if ("ltr".equals(val)) {
            return TextDirection.LTR;
        }
        if ("rtl".equals(val)) {
            return TextDirection.RTL;
        }
        StringBuilder stringBuilder = new StringBuilder("Invalid direction property: ");
        stringBuilder.append(val);
        throw new SAXException(stringBuilder.toString());
    }

    private static FillRule parseFillRule(String val) throws SAXException {
        if ("nonzero".equals(val)) {
            return FillRule.NonZero;
        }
        if ("evenodd".equals(val)) {
            return FillRule.EvenOdd;
        }
        StringBuilder stringBuilder = new StringBuilder("Invalid fill-rule property: ");
        stringBuilder.append(val);
        throw new SAXException(stringBuilder.toString());
    }

    private static LineCaps parseStrokeLineCap(String val) throws SAXException {
        if ("butt".equals(val)) {
            return LineCaps.Butt;
        }
        if ("round".equals(val)) {
            return LineCaps.Round;
        }
        if ("square".equals(val)) {
            return LineCaps.Square;
        }
        StringBuilder stringBuilder = new StringBuilder("Invalid stroke-linecap property: ");
        stringBuilder.append(val);
        throw new SAXException(stringBuilder.toString());
    }

    private static LineJoin parseStrokeLineJoin(String val) throws SAXException {
        if ("miter".equals(val)) {
            return LineJoin.Miter;
        }
        if ("round".equals(val)) {
            return LineJoin.Round;
        }
        if ("bevel".equals(val)) {
            return LineJoin.Bevel;
        }
        StringBuilder stringBuilder = new StringBuilder("Invalid stroke-linejoin property: ");
        stringBuilder.append(val);
        throw new SAXException(stringBuilder.toString());
    }

    private static Length[] parseStrokeDashArray(String val) throws SAXException {
        TextScanner scan = new TextScanner(val);
        scan.skipWhitespace();
        if (scan.empty()) {
            return null;
        }
        Length dash = scan.nextLength();
        if (dash == null) {
            return null;
        }
        if (dash.isNegative()) {
            StringBuilder stringBuilder = new StringBuilder("Invalid stroke-dasharray. Dash segemnts cannot be negative: ");
            stringBuilder.append(val);
            throw new SAXException(stringBuilder.toString());
        }
        float sum = dash.floatValue();
        List<Length> dashes = new ArrayList();
        dashes.add(dash);
        while (!scan.empty()) {
            scan.skipCommaWhitespace();
            dash = scan.nextLength();
            StringBuilder stringBuilder2;
            if (dash == null) {
                stringBuilder2 = new StringBuilder("Invalid stroke-dasharray. Non-Length content found: ");
                stringBuilder2.append(val);
                throw new SAXException(stringBuilder2.toString());
            } else if (dash.isNegative()) {
                stringBuilder2 = new StringBuilder("Invalid stroke-dasharray. Dash segemnts cannot be negative: ");
                stringBuilder2.append(val);
                throw new SAXException(stringBuilder2.toString());
            } else {
                dashes.add(dash);
                sum += dash.floatValue();
            }
        }
        if (sum == 0.0f) {
            return null;
        }
        return (Length[]) dashes.toArray(new Length[dashes.size()]);
    }

    private static TextAnchor parseTextAnchor(String val) throws SAXException {
        if ("start".equals(val)) {
            return TextAnchor.Start;
        }
        if ("middle".equals(val)) {
            return TextAnchor.Middle;
        }
        if ("end".equals(val)) {
            return TextAnchor.End;
        }
        StringBuilder stringBuilder = new StringBuilder("Invalid text-anchor property: ");
        stringBuilder.append(val);
        throw new SAXException(stringBuilder.toString());
    }

    private static Boolean parseOverflow(String val) throws SAXException {
        if ("visible".equals(val) || "auto".equals(val)) {
            return Boolean.TRUE;
        }
        if ("hidden".equals(val) || "scroll".equals(val)) {
            return Boolean.FALSE;
        }
        StringBuilder stringBuilder = new StringBuilder("Invalid toverflow property: ");
        stringBuilder.append(val);
        throw new SAXException(stringBuilder.toString());
    }

    private static CSSClipRect parseClip(String val) throws SAXException {
        if ("auto".equals(val)) {
            return null;
        }
        if (val.toLowerCase(Locale.US).startsWith("rect(")) {
            TextScanner scan = new TextScanner(val.substring(5));
            scan.skipWhitespace();
            Length top = parseLengthOrAuto(scan);
            scan.skipCommaWhitespace();
            Length right = parseLengthOrAuto(scan);
            scan.skipCommaWhitespace();
            Length bottom = parseLengthOrAuto(scan);
            scan.skipCommaWhitespace();
            Length left = parseLengthOrAuto(scan);
            scan.skipWhitespace();
            if (scan.consume(')')) {
                return new CSSClipRect(top, right, bottom, left);
            }
            StringBuilder stringBuilder = new StringBuilder("Bad rect() clip definition: ");
            stringBuilder.append(val);
            throw new SAXException(stringBuilder.toString());
        }
        throw new SAXException("Invalid clip attribute shape. Only rect() is supported.");
    }

    private static Length parseLengthOrAuto(TextScanner scan) {
        if (scan.consume("auto")) {
            return new Length(0.0f);
        }
        return scan.nextLength();
    }

    private static VectorEffect parseVectorEffect(String val) throws SAXException {
        if (NONE.equals(val)) {
            return VectorEffect.None;
        }
        if ("non-scaling-stroke".equals(val)) {
            return VectorEffect.NonScalingStroke;
        }
        StringBuilder stringBuilder = new StringBuilder("Invalid vector-effect property: ");
        stringBuilder.append(val);
        throw new SAXException(stringBuilder.toString());
    }

    /* JADX WARNING: Missing block: B:79:0x040f, code skipped:
            r7 = r10;
            r8 = r11;
     */
    /* JADX WARNING: Missing block: B:80:0x0411, code skipped:
            r5 = r19;
            r6 = r20;
     */
    /* JADX WARNING: Missing block: B:92:0x04bd, code skipped:
            r0.skipWhitespace();
     */
    /* JADX WARNING: Missing block: B:93:0x04c4, code skipped:
            if (r0.empty() == null) goto L_0x04c8;
     */
    /* JADX WARNING: Missing block: B:94:0x04c7, code skipped:
            return r9;
     */
    /* JADX WARNING: Missing block: B:96:0x04cc, code skipped:
            if (r0.hasLetter() == null) goto L_0x04d7;
     */
    /* JADX WARNING: Missing block: B:97:0x04ce, code skipped:
            r2 = r0.nextChar().intValue();
     */
    private static com.caverock.androidsvg.SVG.PathDefinition parsePath(java.lang.String r30) throws org.xml.sax.SAXException {
        /*
        r0 = new com.caverock.androidsvg.SVGParser$TextScanner;
        r1 = r30;
        r0.<init>(r1);
        r2 = 63;
        r3 = 0;
        r4 = 0;
        r5 = 0;
        r6 = 0;
        r7 = 0;
        r8 = 0;
        r9 = new com.caverock.androidsvg.SVG$PathDefinition;
        r9.<init>();
        r10 = r0.empty();
        if (r10 == 0) goto L_0x001b;
    L_0x001a:
        return r9;
    L_0x001b:
        r10 = r0.nextChar();
        r2 = r10.intValue();
        r10 = 77;
        r15 = 109; // 0x6d float:1.53E-43 double:5.4E-322;
        if (r2 == r10) goto L_0x002c;
    L_0x0029:
        if (r2 == r15) goto L_0x002c;
    L_0x002b:
        return r9;
    L_0x002c:
        r0.skipWhitespace();
        r10 = 108; // 0x6c float:1.51E-43 double:5.34E-322;
        r11 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        switch(r2) {
            case 65: goto L_0x0417;
            case 67: goto L_0x0351;
            case 72: goto L_0x0305;
            case 76: goto L_0x02a7;
            case 77: goto L_0x0239;
            case 81: goto L_0x01af;
            case 83: goto L_0x00fa;
            case 84: goto L_0x0085;
            case 86: goto L_0x004a;
            case 90: goto L_0x0041;
            case 97: goto L_0x0417;
            case 99: goto L_0x0351;
            case 104: goto L_0x0305;
            case 108: goto L_0x02a7;
            case 109: goto L_0x0239;
            case 113: goto L_0x01af;
            case 115: goto L_0x00fa;
            case 116: goto L_0x0085;
            case 118: goto L_0x004a;
            case 122: goto L_0x0041;
            default: goto L_0x0036;
        };
    L_0x0036:
        r29 = r3;
        r19 = r5;
        r20 = r6;
        r25 = r7;
        r26 = r8;
        return r9;
    L_0x0041:
        r9.close();
        r7 = r5;
        r3 = r5;
        r8 = r6;
        r4 = r6;
        goto L_0x04bd;
    L_0x004a:
        r10 = r0.nextFloat();
        if (r10 != 0) goto L_0x0069;
    L_0x0050:
        r11 = "SVGParser";
        r12 = new java.lang.StringBuilder;
        r13 = "Bad path coords for ";
        r12.<init>(r13);
        r12.append(r2);
        r13 = " path segment";
        r12.append(r13);
        r12 = r12.toString();
        android.util.Log.e(r11, r12);
        return r9;
    L_0x0069:
        r11 = 118; // 0x76 float:1.65E-43 double:5.83E-322;
        if (r2 != r11) goto L_0x0076;
    L_0x006d:
        r11 = r10.floatValue();
        r11 = r11 + r4;
        r10 = java.lang.Float.valueOf(r11);
    L_0x0076:
        r11 = r10.floatValue();
        r9.lineTo(r3, r11);
        r11 = r10.floatValue();
        r8 = r11;
        r4 = r11;
        goto L_0x04bd;
    L_0x0085:
        r10 = r11 * r3;
        r10 = r10 - r7;
        r10 = java.lang.Float.valueOf(r10);
        r11 = r11 * r4;
        r11 = r11 - r8;
        r11 = java.lang.Float.valueOf(r11);
        r12 = r0.nextFloat();
        r0.skipCommaWhitespace();
        r13 = r0.nextFloat();
        if (r13 != 0) goto L_0x00b8;
    L_0x009f:
        r14 = "SVGParser";
        r15 = new java.lang.StringBuilder;
        r1 = "Bad path coords for ";
        r15.<init>(r1);
        r15.append(r2);
        r1 = " path segment";
        r15.append(r1);
        r1 = r15.toString();
        android.util.Log.e(r14, r1);
        return r9;
    L_0x00b8:
        r1 = 116; // 0x74 float:1.63E-43 double:5.73E-322;
        if (r2 != r1) goto L_0x00ce;
    L_0x00bc:
        r1 = r12.floatValue();
        r1 = r1 + r3;
        r12 = java.lang.Float.valueOf(r1);
        r1 = r13.floatValue();
        r1 = r1 + r4;
        r13 = java.lang.Float.valueOf(r1);
    L_0x00ce:
        r1 = r10.floatValue();
        r14 = r11.floatValue();
        r15 = r12.floatValue();
        r19 = r5;
        r5 = r13.floatValue();
        r9.quadTo(r1, r14, r15, r5);
        r1 = r10.floatValue();
        r5 = r11.floatValue();
        r3 = r12.floatValue();
        r4 = r13.floatValue();
        r7 = r1;
        r8 = r5;
        r5 = r19;
        goto L_0x04bd;
    L_0x00fa:
        r19 = r5;
        r1 = r11 * r3;
        r1 = r1 - r7;
        r1 = java.lang.Float.valueOf(r1);
        r11 = r11 * r4;
        r11 = r11 - r8;
        r5 = java.lang.Float.valueOf(r11);
        r10 = r0.nextFloat();
        r0.skipCommaWhitespace();
        r11 = r0.nextFloat();
        r0.skipCommaWhitespace();
        r12 = r0.nextFloat();
        r0.skipCommaWhitespace();
        r13 = r0.nextFloat();
        if (r13 != 0) goto L_0x013f;
    L_0x0124:
        r14 = "SVGParser";
        r15 = new java.lang.StringBuilder;
        r20 = r6;
        r6 = "Bad path coords for ";
        r15.<init>(r6);
        r15.append(r2);
        r6 = " path segment";
        r15.append(r6);
        r6 = r15.toString();
        android.util.Log.e(r14, r6);
        return r9;
    L_0x013f:
        r20 = r6;
        r6 = 115; // 0x73 float:1.61E-43 double:5.7E-322;
        if (r2 != r6) goto L_0x0169;
    L_0x0145:
        r6 = r12.floatValue();
        r6 = r6 + r3;
        r12 = java.lang.Float.valueOf(r6);
        r6 = r13.floatValue();
        r6 = r6 + r4;
        r13 = java.lang.Float.valueOf(r6);
        r6 = r10.floatValue();
        r6 = r6 + r3;
        r10 = java.lang.Float.valueOf(r6);
        r6 = r11.floatValue();
        r6 = r6 + r4;
        r11 = java.lang.Float.valueOf(r6);
    L_0x0169:
        r6 = r10;
        r15 = r11;
        r14 = r12;
        r11 = r1.floatValue();
        r12 = r5.floatValue();
        r16 = r6.floatValue();
        r17 = r15.floatValue();
        r21 = r14.floatValue();
        r22 = r13.floatValue();
        r10 = r9;
        r23 = r1;
        r1 = r13;
        r13 = r16;
        r24 = r5;
        r5 = r14;
        r14 = r17;
        r25 = r7;
        r26 = r8;
        r7 = r15;
        r8 = 109; // 0x6d float:1.53E-43 double:5.4E-322;
        r15 = r21;
        r16 = r22;
        r10.cubicTo(r11, r12, r13, r14, r15, r16);
        r10 = r6.floatValue();
        r11 = r7.floatValue();
        r3 = r5.floatValue();
        r4 = r1.floatValue();
        goto L_0x040f;
    L_0x01af:
        r19 = r5;
        r20 = r6;
        r25 = r7;
        r26 = r8;
        r8 = r15;
        r1 = r0.nextFloat();
        r0.skipCommaWhitespace();
        r5 = r0.nextFloat();
        r0.skipCommaWhitespace();
        r6 = r0.nextFloat();
        r0.skipCommaWhitespace();
        r7 = r0.nextFloat();
        if (r7 != 0) goto L_0x01ec;
    L_0x01d3:
        r8 = "SVGParser";
        r10 = new java.lang.StringBuilder;
        r11 = "Bad path coords for ";
        r10.<init>(r11);
        r10.append(r2);
        r11 = " path segment";
        r10.append(r11);
        r10 = r10.toString();
        android.util.Log.e(r8, r10);
        return r9;
    L_0x01ec:
        r10 = 113; // 0x71 float:1.58E-43 double:5.6E-322;
        if (r2 != r10) goto L_0x0214;
    L_0x01f0:
        r10 = r6.floatValue();
        r10 = r10 + r3;
        r6 = java.lang.Float.valueOf(r10);
        r10 = r7.floatValue();
        r10 = r10 + r4;
        r7 = java.lang.Float.valueOf(r10);
        r10 = r1.floatValue();
        r10 = r10 + r3;
        r1 = java.lang.Float.valueOf(r10);
        r10 = r5.floatValue();
        r10 = r10 + r4;
        r5 = java.lang.Float.valueOf(r10);
    L_0x0214:
        r10 = r1.floatValue();
        r11 = r5.floatValue();
        r12 = r6.floatValue();
        r13 = r7.floatValue();
        r9.quadTo(r10, r11, r12, r13);
        r10 = r1.floatValue();
        r11 = r5.floatValue();
        r3 = r6.floatValue();
        r4 = r7.floatValue();
        goto L_0x040f;
    L_0x0239:
        r19 = r5;
        r20 = r6;
        r25 = r7;
        r26 = r8;
        r8 = r15;
        r1 = r0.nextFloat();
        r0.skipCommaWhitespace();
        r5 = r0.nextFloat();
        if (r5 != 0) goto L_0x0268;
    L_0x024f:
        r6 = "SVGParser";
        r7 = new java.lang.StringBuilder;
        r8 = "Bad path coords for ";
        r7.<init>(r8);
        r7.append(r2);
        r8 = " path segment";
        r7.append(r8);
        r7 = r7.toString();
        android.util.Log.e(r6, r7);
        return r9;
    L_0x0268:
        if (r2 != r8) goto L_0x0282;
    L_0x026a:
        r6 = r9.isEmpty();
        if (r6 != 0) goto L_0x0282;
    L_0x0270:
        r6 = r1.floatValue();
        r6 = r6 + r3;
        r1 = java.lang.Float.valueOf(r6);
        r6 = r5.floatValue();
        r6 = r6 + r4;
        r5 = java.lang.Float.valueOf(r6);
    L_0x0282:
        r6 = r1.floatValue();
        r7 = r5.floatValue();
        r9.moveTo(r6, r7);
        r6 = r1.floatValue();
        r7 = r6;
        r11 = r6;
        r3 = r6;
        r6 = r5.floatValue();
        r12 = r6;
        r13 = r6;
        r4 = r6;
        if (r2 != r8) goto L_0x029e;
    L_0x029d:
        goto L_0x02a0;
    L_0x029e:
        r10 = 76;
    L_0x02a0:
        r2 = r10;
        r5 = r11;
        r8 = r12;
        r6 = r13;
        goto L_0x04bd;
    L_0x02a7:
        r19 = r5;
        r20 = r6;
        r25 = r7;
        r26 = r8;
        r8 = r15;
        r1 = r0.nextFloat();
        r0.skipCommaWhitespace();
        r5 = r0.nextFloat();
        if (r5 != 0) goto L_0x02d6;
    L_0x02bd:
        r6 = "SVGParser";
        r7 = new java.lang.StringBuilder;
        r8 = "Bad path coords for ";
        r7.<init>(r8);
        r7.append(r2);
        r8 = " path segment";
        r7.append(r8);
        r7 = r7.toString();
        android.util.Log.e(r6, r7);
        return r9;
    L_0x02d6:
        if (r2 != r10) goto L_0x02ea;
    L_0x02d8:
        r6 = r1.floatValue();
        r6 = r6 + r3;
        r1 = java.lang.Float.valueOf(r6);
        r6 = r5.floatValue();
        r6 = r6 + r4;
        r5 = java.lang.Float.valueOf(r6);
    L_0x02ea:
        r6 = r1.floatValue();
        r7 = r5.floatValue();
        r9.lineTo(r6, r7);
        r6 = r1.floatValue();
        r7 = r6;
        r3 = r6;
        r6 = r5.floatValue();
        r10 = r6;
        r4 = r6;
        r8 = r10;
        goto L_0x0411;
    L_0x0305:
        r19 = r5;
        r20 = r6;
        r25 = r7;
        r26 = r8;
        r8 = r15;
        r1 = r0.nextFloat();
        if (r1 != 0) goto L_0x032d;
    L_0x0314:
        r5 = "SVGParser";
        r6 = new java.lang.StringBuilder;
        r7 = "Bad path coords for ";
        r6.<init>(r7);
        r6.append(r2);
        r7 = " path segment";
        r6.append(r7);
        r6 = r6.toString();
        android.util.Log.e(r5, r6);
        return r9;
    L_0x032d:
        r5 = 104; // 0x68 float:1.46E-43 double:5.14E-322;
        if (r2 != r5) goto L_0x033a;
    L_0x0331:
        r5 = r1.floatValue();
        r5 = r5 + r3;
        r1 = java.lang.Float.valueOf(r5);
    L_0x033a:
        r5 = r1.floatValue();
        r9.lineTo(r5, r4);
        r5 = r1.floatValue();
        r6 = r5;
        r3 = r5;
        r7 = r6;
        r5 = r19;
        r6 = r20;
        r8 = r26;
        goto L_0x04bd;
    L_0x0351:
        r19 = r5;
        r20 = r6;
        r25 = r7;
        r26 = r8;
        r8 = r15;
        r1 = r0.nextFloat();
        r0.skipCommaWhitespace();
        r5 = r0.nextFloat();
        r0.skipCommaWhitespace();
        r6 = r0.nextFloat();
        r0.skipCommaWhitespace();
        r7 = r0.nextFloat();
        r0.skipCommaWhitespace();
        r10 = r0.nextFloat();
        r0.skipCommaWhitespace();
        r11 = r0.nextFloat();
        if (r11 != 0) goto L_0x039c;
    L_0x0383:
        r8 = "SVGParser";
        r12 = new java.lang.StringBuilder;
        r13 = "Bad path coords for ";
        r12.<init>(r13);
        r12.append(r2);
        r13 = " path segment";
        r12.append(r13);
        r12 = r12.toString();
        android.util.Log.e(r8, r12);
        return r9;
    L_0x039c:
        r12 = 99;
        if (r2 != r12) goto L_0x03d6;
    L_0x03a0:
        r12 = r10.floatValue();
        r12 = r12 + r3;
        r10 = java.lang.Float.valueOf(r12);
        r12 = r11.floatValue();
        r12 = r12 + r4;
        r11 = java.lang.Float.valueOf(r12);
        r12 = r1.floatValue();
        r12 = r12 + r3;
        r1 = java.lang.Float.valueOf(r12);
        r12 = r5.floatValue();
        r12 = r12 + r4;
        r5 = java.lang.Float.valueOf(r12);
        r12 = r6.floatValue();
        r12 = r12 + r3;
        r6 = java.lang.Float.valueOf(r12);
        r12 = r7.floatValue();
        r12 = r12 + r4;
        r7 = java.lang.Float.valueOf(r12);
    L_0x03d6:
        r15 = r10;
        r14 = r11;
        r11 = r1.floatValue();
        r12 = r5.floatValue();
        r13 = r6.floatValue();
        r16 = r7.floatValue();
        r17 = r15.floatValue();
        r18 = r14.floatValue();
        r10 = r9;
        r8 = r14;
        r14 = r16;
        r27 = r1;
        r1 = r15;
        r15 = r17;
        r16 = r18;
        r10.cubicTo(r11, r12, r13, r14, r15, r16);
        r10 = r6.floatValue();
        r11 = r7.floatValue();
        r3 = r1.floatValue();
        r4 = r8.floatValue();
    L_0x040f:
        r7 = r10;
        r8 = r11;
    L_0x0411:
        r5 = r19;
        r6 = r20;
        goto L_0x04bd;
    L_0x0417:
        r19 = r5;
        r20 = r6;
        r25 = r7;
        r26 = r8;
        r1 = r0.nextFloat();
        r0.skipCommaWhitespace();
        r5 = r0.nextFloat();
        r0.skipCommaWhitespace();
        r6 = r0.nextFloat();
        r0.skipCommaWhitespace();
        r7 = r0.nextFlag();
        r0.skipCommaWhitespace();
        r8 = r0.nextFlag();
        r0.skipCommaWhitespace();
        r10 = r0.nextFloat();
        r0.skipCommaWhitespace();
        r11 = r0.nextFloat();
        if (r11 == 0) goto L_0x04dd;
    L_0x044f:
        r12 = r1.floatValue();
        r13 = 0;
        r12 = (r12 > r13 ? 1 : (r12 == r13 ? 0 : -1));
        if (r12 < 0) goto L_0x04dd;
    L_0x0458:
        r12 = r5.floatValue();
        r12 = (r12 > r13 ? 1 : (r12 == r13 ? 0 : -1));
        if (r12 >= 0) goto L_0x0466;
    L_0x0460:
        r28 = r1;
        r29 = r3;
        goto L_0x04e1;
    L_0x0466:
        r12 = 97;
        if (r2 != r12) goto L_0x047c;
    L_0x046a:
        r12 = r10.floatValue();
        r12 = r12 + r3;
        r10 = java.lang.Float.valueOf(r12);
        r12 = r11.floatValue();
        r12 = r12 + r4;
        r11 = java.lang.Float.valueOf(r12);
    L_0x047c:
        r15 = r10;
        r14 = r11;
        r11 = r1.floatValue();
        r12 = r5.floatValue();
        r13 = r6.floatValue();
        r16 = r7.booleanValue();
        r17 = r8.booleanValue();
        r18 = r15.floatValue();
        r21 = r14.floatValue();
        r10 = r9;
        r28 = r1;
        r1 = r14;
        r14 = r16;
        r29 = r3;
        r3 = r15;
        r15 = r17;
        r16 = r18;
        r17 = r21;
        r10.arcTo(r11, r12, r13, r14, r15, r16, r17);
        r10 = r3.floatValue();
        r11 = r10;
        r12 = r1.floatValue();
        r13 = r12;
        r4 = r12;
        r3 = r10;
        r7 = r11;
        r8 = r13;
        goto L_0x0411;
    L_0x04bd:
        r0.skipWhitespace();
        r1 = r0.empty();
        if (r1 == 0) goto L_0x04c8;
        return r9;
    L_0x04c8:
        r1 = r0.hasLetter();
        if (r1 == 0) goto L_0x04d7;
    L_0x04ce:
        r1 = r0.nextChar();
        r2 = r1.intValue();
    L_0x04d7:
        r1 = r30;
        r15 = 109; // 0x6d float:1.53E-43 double:5.4E-322;
        goto L_0x002c;
    L_0x04dd:
        r28 = r1;
        r29 = r3;
    L_0x04e1:
        r1 = "SVGParser";
        r3 = new java.lang.StringBuilder;
        r12 = "Bad path coords for ";
        r3.<init>(r12);
        r3.append(r2);
        r12 = " path segment";
        r3.append(r12);
        r3 = r3.toString();
        android.util.Log.e(r1, r3);
        return r9;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.caverock.androidsvg.SVGParser.parsePath(java.lang.String):com.caverock.androidsvg.SVG$PathDefinition");
    }

    private static Set<String> parseRequiredFeatures(String val) throws SAXException {
        TextScanner scan = new TextScanner(val);
        HashSet<String> result = new HashSet();
        while (!scan.empty()) {
            String feature = scan.nextToken();
            if (feature.startsWith(FEATURE_STRING_PREFIX)) {
                result.add(feature.substring(FEATURE_STRING_PREFIX.length()));
            } else {
                result.add("UNSUPPORTED");
            }
            scan.skipWhitespace();
        }
        return result;
    }

    private static Set<String> parseSystemLanguage(String val) throws SAXException {
        TextScanner scan = new TextScanner(val);
        HashSet<String> result = new HashSet();
        while (!scan.empty()) {
            String language = scan.nextToken();
            int hyphenPos = language.indexOf(45);
            if (hyphenPos != -1) {
                language = language.substring(0, hyphenPos);
            }
            result.add(new Locale(language, "", "").getLanguage());
            scan.skipWhitespace();
        }
        return result;
    }

    private static Set<String> parseRequiredFormats(String val) throws SAXException {
        TextScanner scan = new TextScanner(val);
        HashSet<String> result = new HashSet();
        while (!scan.empty()) {
            result.add(scan.nextToken());
            scan.skipWhitespace();
        }
        return result;
    }

    private static String parseFunctionalIRI(String val, String attrName) throws SAXException {
        if (val.equals(NONE)) {
            return null;
        }
        if (val.startsWith("url(") && val.endsWith(")")) {
            return val.substring(4, val.length() - 1).trim();
        }
        StringBuilder stringBuilder = new StringBuilder("Bad ");
        stringBuilder.append(attrName);
        stringBuilder.append(" attribute. Expected \"none\" or \"url()\" format");
        throw new SAXException(stringBuilder.toString());
    }

    private void style(Attributes attributes) throws SAXException {
        debug("<style>", new Object[0]);
        if (this.currentElement != null) {
            boolean isTextCSS = true;
            String media = "all";
            for (int i = 0; i < attributes.getLength(); i++) {
                String val = attributes.getValue(i).trim();
                int i2 = $SWITCH_TABLE$com$caverock$androidsvg$SVGParser$SVGAttr()[SVGAttr.fromString(attributes.getLocalName(i)).ordinal()];
                if (i2 == 39) {
                    media = val;
                } else if (i2 == 78) {
                    isTextCSS = val.equals("text/css");
                }
            }
            if (isTextCSS && CSSParser.mediaMatches(media, MediaType.screen)) {
                this.inStyleElement = true;
                return;
            }
            this.ignoring = true;
            this.ignoreDepth = 1;
            return;
        }
        throw new SAXException("Invalid document. Root element must be <svg>");
    }

    private void parseCSSStyleSheet(String sheet) throws SAXException {
        this.svgDocument.addCSSRules(new CSSParser(MediaType.screen).parse(sheet));
    }
}
