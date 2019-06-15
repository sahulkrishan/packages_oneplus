package android.support.v17.leanback.graphics;

import android.graphics.Rect;

public class BoundsRule {
    public ValueRule bottom;
    public ValueRule left;
    public ValueRule right;
    public ValueRule top;

    public static final class ValueRule {
        int mAbsoluteValue;
        float mFraction;

        public static ValueRule inheritFromParent(float fraction) {
            return new ValueRule(0, fraction);
        }

        public static ValueRule absoluteValue(int absoluteValue) {
            return new ValueRule(absoluteValue, 0.0f);
        }

        public static ValueRule inheritFromParentWithOffset(float fraction, int value) {
            return new ValueRule(value, fraction);
        }

        ValueRule(int absoluteValue, float fraction) {
            this.mAbsoluteValue = absoluteValue;
            this.mFraction = fraction;
        }

        ValueRule(ValueRule rule) {
            this.mFraction = rule.mFraction;
            this.mAbsoluteValue = rule.mAbsoluteValue;
        }

        public void setFraction(float fraction) {
            this.mFraction = fraction;
        }

        public float getFraction() {
            return this.mFraction;
        }

        public void setAbsoluteValue(int absoluteValue) {
            this.mAbsoluteValue = absoluteValue;
        }

        public int getAbsoluteValue() {
            return this.mAbsoluteValue;
        }
    }

    public void calculateBounds(Rect rect, Rect result) {
        if (this.left == null) {
            result.left = rect.left;
        } else {
            result.left = doCalculate(rect.left, this.left, rect.width());
        }
        if (this.right == null) {
            result.right = rect.right;
        } else {
            result.right = doCalculate(rect.left, this.right, rect.width());
        }
        if (this.top == null) {
            result.top = rect.top;
        } else {
            result.top = doCalculate(rect.top, this.top, rect.height());
        }
        if (this.bottom == null) {
            result.bottom = rect.bottom;
        } else {
            result.bottom = doCalculate(rect.top, this.bottom, rect.height());
        }
    }

    public BoundsRule(BoundsRule boundsRule) {
        ValueRule valueRule = null;
        this.left = boundsRule.left != null ? new ValueRule(boundsRule.left) : null;
        this.right = boundsRule.right != null ? new ValueRule(boundsRule.right) : null;
        this.top = boundsRule.top != null ? new ValueRule(boundsRule.top) : null;
        if (boundsRule.bottom != null) {
            valueRule = new ValueRule(boundsRule.bottom);
        }
        this.bottom = valueRule;
    }

    private int doCalculate(int value, ValueRule rule, int size) {
        return (rule.mAbsoluteValue + value) + ((int) (rule.mFraction * ((float) size)));
    }
}
