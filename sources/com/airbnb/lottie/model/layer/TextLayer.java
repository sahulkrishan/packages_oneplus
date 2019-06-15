package com.airbnb.lottie.model.layer;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.TextDelegate;
import com.airbnb.lottie.animation.content.ContentGroup;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.TextKeyframeAnimation;
import com.airbnb.lottie.model.DocumentData;
import com.airbnb.lottie.model.Font;
import com.airbnb.lottie.model.FontCharacter;
import com.airbnb.lottie.model.animatable.AnimatableTextProperties;
import com.airbnb.lottie.model.content.ShapeGroup;
import com.airbnb.lottie.utils.Utils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TextLayer extends BaseLayer {
    @Nullable
    private BaseKeyframeAnimation<Integer, Integer> colorAnimation;
    private final LottieComposition composition;
    private final Map<FontCharacter, List<ContentGroup>> contentsForCharacter = new HashMap();
    private final Paint fillPaint = new Paint(1) {
    };
    private final LottieDrawable lottieDrawable;
    private final Matrix matrix = new Matrix();
    private final RectF rectF = new RectF();
    @Nullable
    private BaseKeyframeAnimation<Integer, Integer> strokeAnimation;
    private final Paint strokePaint = new Paint(1) {
    };
    @Nullable
    private BaseKeyframeAnimation<Float, Float> strokeWidthAnimation;
    private final char[] tempCharArray = new char[1];
    private final TextKeyframeAnimation textAnimation;
    @Nullable
    private BaseKeyframeAnimation<Float, Float> trackingAnimation;

    TextLayer(LottieDrawable lottieDrawable, Layer layerModel) {
        super(lottieDrawable, layerModel);
        this.lottieDrawable = lottieDrawable;
        this.composition = layerModel.getComposition();
        this.textAnimation = layerModel.getText().createAnimation();
        this.textAnimation.addUpdateListener(this);
        addAnimation(this.textAnimation);
        AnimatableTextProperties textProperties = layerModel.getTextProperties();
        if (!(textProperties == null || textProperties.color == null)) {
            this.colorAnimation = textProperties.color.createAnimation();
            this.colorAnimation.addUpdateListener(this);
            addAnimation(this.colorAnimation);
        }
        if (!(textProperties == null || textProperties.stroke == null)) {
            this.strokeAnimation = textProperties.stroke.createAnimation();
            this.strokeAnimation.addUpdateListener(this);
            addAnimation(this.strokeAnimation);
        }
        if (!(textProperties == null || textProperties.strokeWidth == null)) {
            this.strokeWidthAnimation = textProperties.strokeWidth.createAnimation();
            this.strokeWidthAnimation.addUpdateListener(this);
            addAnimation(this.strokeWidthAnimation);
        }
        if (textProperties != null && textProperties.tracking != null) {
            this.trackingAnimation = textProperties.tracking.createAnimation();
            this.trackingAnimation.addUpdateListener(this);
            addAnimation(this.trackingAnimation);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void drawLayer(Canvas canvas, Matrix parentMatrix, int parentAlpha) {
        canvas.save();
        if (!this.lottieDrawable.useTextGlyphs()) {
            canvas.setMatrix(parentMatrix);
        }
        DocumentData documentData = (DocumentData) this.textAnimation.getValue();
        Font font = (Font) this.composition.getFonts().get(documentData.fontName);
        if (font == null) {
            canvas.restore();
            return;
        }
        if (this.colorAnimation != null) {
            this.fillPaint.setColor(((Integer) this.colorAnimation.getValue()).intValue());
        } else {
            this.fillPaint.setColor(documentData.color);
        }
        if (this.strokeAnimation != null) {
            this.strokePaint.setColor(((Integer) this.strokeAnimation.getValue()).intValue());
        } else {
            this.strokePaint.setColor(documentData.strokeColor);
        }
        int alpha = (((Integer) this.transform.getOpacity().getValue()).intValue() * 255) / 100;
        this.fillPaint.setAlpha(alpha);
        this.strokePaint.setAlpha(alpha);
        if (this.strokeWidthAnimation != null) {
            this.strokePaint.setStrokeWidth(((Float) this.strokeWidthAnimation.getValue()).floatValue());
        } else {
            this.strokePaint.setStrokeWidth((((float) documentData.strokeWidth) * this.composition.getDpScale()) * Utils.getScale(parentMatrix));
        }
        if (this.lottieDrawable.useTextGlyphs()) {
            drawTextGlyphs(documentData, parentMatrix, font, canvas);
        } else {
            drawTextWithFont(documentData, font, parentMatrix, canvas);
        }
        canvas.restore();
    }

    private void drawTextGlyphs(DocumentData documentData, Matrix parentMatrix, Font font, Canvas canvas) {
        DocumentData documentData2 = documentData;
        float fontScale = ((float) documentData2.size) / 100.0f;
        float parentScale = Utils.getScale(parentMatrix);
        String text = documentData2.text;
        int i = 0;
        while (true) {
            int i2 = i;
            Canvas canvas2;
            if (i2 < text.length()) {
                FontCharacter character = (FontCharacter) this.composition.getCharacters().get(FontCharacter.hashFor(text.charAt(i2), font.getFamily(), font.getStyle()));
                if (character == null) {
                    canvas2 = canvas;
                } else {
                    drawCharacterAsGlyph(character, parentMatrix, fontScale, documentData2, canvas);
                    float tx = ((((float) character.getWidth()) * fontScale) * this.composition.getDpScale()) * parentScale;
                    float tracking = ((float) documentData2.tracking) / 10.0f;
                    if (this.trackingAnimation != null) {
                        tracking += ((Float) this.trackingAnimation.getValue()).floatValue();
                    }
                    canvas.translate(tx + (tracking * parentScale), 0.0f);
                }
                i = i2 + 1;
            } else {
                canvas2 = canvas;
                return;
            }
        }
    }

    private void drawTextWithFont(DocumentData documentData, Font font, Matrix parentMatrix, Canvas canvas) {
        float parentScale = Utils.getScale(parentMatrix);
        Typeface typeface = this.lottieDrawable.getTypeface(font.getFamily(), font.getStyle());
        if (typeface != null) {
            String text = documentData.text;
            TextDelegate textDelegate = this.lottieDrawable.getTextDelegate();
            if (textDelegate != null) {
                text = textDelegate.getTextInternal(text);
            }
            this.fillPaint.setTypeface(typeface);
            this.fillPaint.setTextSize(((float) documentData.size) * this.composition.getDpScale());
            this.strokePaint.setTypeface(this.fillPaint.getTypeface());
            this.strokePaint.setTextSize(this.fillPaint.getTextSize());
            for (int i = 0; i < text.length(); i++) {
                char character = text.charAt(i);
                drawCharacterFromFont(character, documentData, canvas);
                this.tempCharArray[0] = character;
                float charWidth = this.fillPaint.measureText(this.tempCharArray, 0, 1);
                float tracking = ((float) documentData.tracking) / 10.0f;
                if (this.trackingAnimation != null) {
                    tracking += ((Float) this.trackingAnimation.getValue()).floatValue();
                }
                canvas.translate((tracking * parentScale) + charWidth, 0.0f);
            }
        }
    }

    private void drawCharacterAsGlyph(FontCharacter character, Matrix parentMatrix, float fontScale, DocumentData documentData, Canvas canvas) {
        List<ContentGroup> contentGroups = getContentsForCharacter(character);
        for (int j = 0; j < contentGroups.size(); j++) {
            Path path = ((ContentGroup) contentGroups.get(j)).getPath();
            path.computeBounds(this.rectF, false);
            this.matrix.set(parentMatrix);
            this.matrix.preScale(fontScale, fontScale);
            path.transform(this.matrix);
            if (documentData.strokeOverFill) {
                drawGlyph(path, this.fillPaint, canvas);
                drawGlyph(path, this.strokePaint, canvas);
            } else {
                drawGlyph(path, this.strokePaint, canvas);
                drawGlyph(path, this.fillPaint, canvas);
            }
        }
    }

    private void drawGlyph(Path path, Paint paint, Canvas canvas) {
        if (paint.getColor() != 0) {
            if (paint.getStyle() != Style.STROKE || paint.getStrokeWidth() != 0.0f) {
                canvas.drawPath(path, paint);
            }
        }
    }

    private void drawCharacterFromFont(char c, DocumentData documentData, Canvas canvas) {
        this.tempCharArray[0] = c;
        if (documentData.strokeOverFill) {
            drawCharacter(this.tempCharArray, this.fillPaint, canvas);
            drawCharacter(this.tempCharArray, this.strokePaint, canvas);
            return;
        }
        drawCharacter(this.tempCharArray, this.strokePaint, canvas);
        drawCharacter(this.tempCharArray, this.fillPaint, canvas);
    }

    private void drawCharacter(char[] character, Paint paint, Canvas canvas) {
        if (paint.getColor() != 0) {
            if (paint.getStyle() != Style.STROKE || paint.getStrokeWidth() != 0.0f) {
                canvas.drawText(character, 0, 1, 0.0f, 0.0f, paint);
            }
        }
    }

    private List<ContentGroup> getContentsForCharacter(FontCharacter character) {
        if (this.contentsForCharacter.containsKey(character)) {
            return (List) this.contentsForCharacter.get(character);
        }
        List<ShapeGroup> shapes = character.getShapes();
        int size = shapes.size();
        List<ContentGroup> contents = new ArrayList(size);
        for (int i = 0; i < size; i++) {
            contents.add(new ContentGroup(this.lottieDrawable, this, (ShapeGroup) shapes.get(i)));
        }
        this.contentsForCharacter.put(character, contents);
        return contents;
    }
}
