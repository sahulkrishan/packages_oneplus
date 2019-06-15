package com.oneplus.settings.opfinger;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewCompat;

public class RectangleMask extends Drawable {
    private static float m_ScaleX;
    private static float m_ScaleY;
    private double dHeight;
    private double dWidth;
    private float m_Angle;
    private Point m_BottomLeft;
    private Point m_BottomRight;
    private FlipType m_Flip;
    private int m_Height = ((int) this.dHeight);
    private MaskType m_Mask;
    private final Paint m_Paint;
    private Rect m_Rect;
    private Rect m_RectRotated;
    private Point m_TopLeft;
    private Point m_TopRight;
    private int m_Width = ((int) this.dWidth);

    public enum FlipType {
        FlipNone,
        FlipX,
        FlipY,
        FlipXY
    }

    public enum MaskType {
        NormalMask,
        LatestMask,
        NextMask,
        TestMask
    }

    public RectangleMask(Point top_left, Point top_right, Point bottom_left, Point bottom_right, MaskType mask, FlipType flip) {
        Point point = top_left;
        Point point2 = bottom_left;
        Point point3 = bottom_right;
        this.m_BottomLeft = point2;
        this.m_BottomRight = point3;
        this.m_TopLeft = point;
        this.m_TopRight = top_right;
        this.m_Mask = mask;
        this.m_Flip = flip;
        int dx1 = point3.x - point2.x;
        int dy1 = point3.y - point2.y;
        this.dWidth = Math.sqrt((double) ((dx1 * dx1) + (dy1 * dy1)));
        int dx2 = point2.x - point.x;
        int dy2 = point.y - point2.y;
        this.dHeight = Math.sqrt((double) ((dx2 * dx2) + (dy2 * dy2)));
        this.m_Angle = (float) ((180.0d * Math.tan(((double) dy1) / ((double) dx1))) / 3.141592653589793d);
        int iLeft = point2.x;
        int iRight = this.m_Width + iLeft;
        int iTop = point.y;
        this.m_Rect = new Rect(iLeft, iTop, iRight, this.m_Height + iTop);
        this.m_Paint = new Paint();
        this.m_Paint.setAntiAlias(true);
    }

    public void draw(Canvas canvas) {
        int iColor;
        int iAlpha;
        int iRectFlippedBottom;
        boolean z;
        boolean bFrame;
        Canvas canvas2 = canvas;
        int iRotate = 0;
        switch (this.m_Mask) {
            case LatestMask:
                iColor = Color.rgb(143, 188, 143);
                iAlpha = Const.CODE_C1_SPA;
                break;
            case NextMask:
                iColor = Color.rgb(0, 159, 227);
                iAlpha = Const.CODE_C1_SPA;
                iRotate = 90;
                break;
            case TestMask:
                iColor = -65536;
                iAlpha = 255;
                break;
            default:
                iColor = ViewCompat.MEASURED_STATE_MASK;
                iAlpha = 48;
                iRotate = 90;
                break;
        }
        int iAlpha2 = iAlpha;
        this.m_Paint.setColor(iColor);
        this.m_Paint.setAlpha(iAlpha2);
        canvas.save();
        canvas2.rotate(-this.m_Angle);
        int FRAME_LINE_WIDTH = this.m_Rect.width() / 12;
        int iRectLeft = (int) (((float) this.m_Rect.left) * m_ScaleX);
        int iRectTop = (int) (((float) this.m_Rect.top) * m_ScaleY);
        int iRectRight = (int) (((float) this.m_Rect.right) * m_ScaleX);
        boolean iRectBottom = (int) (((float) this.m_Rect.bottom) * m_ScaleY);
        int iCanvasWidth = canvas.getWidth();
        int iCanvasHeight = canvas.getHeight();
        boolean iRectFlippedBottom2;
        switch (this.m_Flip) {
            case FlipNone:
                iRotate = iRectLeft;
                iColor = iRectTop;
                iAlpha = iRectRight;
                iRectFlippedBottom2 = iRectBottom;
                break;
            case FlipX:
                iRotate = (iCanvasWidth - 1) - iRectRight;
                iColor = iRectTop;
                iAlpha = (iCanvasWidth - 1) - iRectLeft;
                iRectFlippedBottom2 = iRectBottom;
                break;
            case FlipY:
                iRotate = iRectLeft;
                iColor = (iCanvasHeight - 1) - iRectBottom;
                iAlpha = iRectRight;
                iRectFlippedBottom2 = (iCanvasHeight - 1) - iRectTop;
                break;
            default:
                iRotate = (iCanvasWidth - 1) - iRectRight;
                iColor = (iCanvasHeight - 1) - iRectBottom;
                iAlpha = (iCanvasWidth - 1) - iRectLeft;
                iRectFlippedBottom2 = (iCanvasHeight - 1) - iRectTop;
                break;
        }
        int iRectFlippedLeft = iRotate;
        int i = iAlpha;
        iAlpha = iColor;
        iColor = i;
        Rect rectFlipped = new Rect(iRectFlippedLeft, iAlpha, iColor, iRectFlippedBottom2);
        int iFrameWidth = (int) (((float) FRAME_LINE_WIDTH) * m_ScaleX);
        if (this.m_Mask == MaskType.NormalMask) {
            canvas2.drawRect(rectFlipped, this.m_Paint);
        } else {
            int i2 = iRectFlippedBottom2;
            Rect rect;
            if (this.m_Mask == MaskType.NextMask) {
                canvas2.drawRect(rectFlipped, this.m_Paint);
                this.m_Paint.setColor(-1);
                this.m_Paint.setAlpha(255);
                this.m_Paint.setStrokeWidth(3.0f);
                rect = rectFlipped;
            } else {
                int i3;
                if (this.m_Mask == MaskType.LatestMask) {
                    canvas2.drawRect(rectFlipped, this.m_Paint);
                    if (null != null) {
                        this.m_Paint.setColor(ViewCompat.MEASURED_STATE_MASK);
                        this.m_Paint.setAlpha(255);
                        this.m_Paint.setStrokeWidth(2.0f);
                        i3 = iRectFlippedLeft;
                        rect = rectFlipped;
                        Canvas canvas3 = canvas2;
                        z = false;
                        bFrame = iRectBottom;
                        canvas3.drawLine((float) iRectLeft, (float) iRectTop, (float) iRectRight, (float) iRectTop, this.m_Paint);
                        canvas3.drawLine((float) iRectLeft, (float) iRectTop, (float) iRectLeft, (float) bFrame, this.m_Paint);
                        canvas3.drawLine((float) iRectLeft, (float) bFrame, (float) iRectRight, (float) bFrame, this.m_Paint);
                        canvas3.drawLine((float) iRectRight, (float) iRectTop, (float) iRectRight, (float) bFrame, this.m_Paint);
                    } else {
                        i3 = iRectFlippedLeft;
                        z = false;
                        bFrame = iRectBottom;
                    }
                } else {
                    i3 = iRectFlippedLeft;
                    z = false;
                    bFrame = iRectBottom;
                    if (this.m_Mask == MaskType.TestMask) {
                        canvas2.drawRect(new Rect(iRectTop, iRectLeft, bFrame, iRectRight), this.m_Paint);
                    }
                }
                canvas.restore();
            }
        }
        z = false;
        bFrame = iRectBottom;
        canvas.restore();
    }

    public int getOpacity() {
        return 0;
    }

    public void setAlpha(int alpha) {
        this.m_Paint.setAlpha(alpha);
    }

    public void setColorFilter(ColorFilter cf) {
        this.m_Paint.setColorFilter(cf);
    }

    public MaskType getMaskType() {
        return this.m_Mask;
    }

    public void setMaskType(MaskType mask) {
        this.m_Mask = mask;
    }

    public static void setScale(float fScaleX, float fScaleY) {
        m_ScaleX = fScaleX;
        m_ScaleY = fScaleY;
    }
}
