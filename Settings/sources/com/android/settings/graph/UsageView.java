package com.android.settings.graph;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.GravityCompat;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.android.settings.R;

public class UsageView extends FrameLayout {
    private final TextView[] mBottomLabels = new TextView[]{(TextView) findViewById(R.id.label_start), (TextView) findViewById(R.id.label_end)};
    private final TextView[] mLabels = new TextView[]{(TextView) findViewById(R.id.label_bottom), (TextView) findViewById(R.id.label_middle), (TextView) findViewById(R.id.label_top)};
    private final UsageGraph mUsageGraph = ((UsageGraph) findViewById(R.id.usage_graph));

    public UsageView(Context context, AttributeSet attrs) {
        int color;
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.usage_view, this);
        TypedArray a = context.obtainStyledAttributes(attrs, com.android.settingslib.R.styleable.UsageView, 0, 0);
        if (a.hasValue(3)) {
            setSideLabels(a.getTextArray(3));
        }
        if (a.hasValue(2)) {
            setBottomLabels(a.getTextArray(2));
        }
        if (a.hasValue(4)) {
            color = a.getColor(4, 0);
            for (TextView v : this.mLabels) {
                v.setTextColor(color);
            }
            for (TextView v2 : this.mBottomLabels) {
                v2.setTextColor(color);
            }
        }
        if (a.hasValue(0)) {
            color = a.getInt(0, 0);
            if (color == GravityCompat.END) {
                LinearLayout layout = (LinearLayout) findViewById(R.id.graph_label_group);
                LinearLayout labels = (LinearLayout) findViewById(R.id.label_group);
                layout.removeView(labels);
                layout.addView(labels);
                labels.setGravity(GravityCompat.END);
                LinearLayout bottomLabels = (LinearLayout) findViewById(R.id.bottom_label_group);
                View bottomSpace = bottomLabels.findViewById(R.id.bottom_label_space);
                bottomLabels.removeView(bottomSpace);
                bottomLabels.addView(bottomSpace);
            } else if (color != GravityCompat.START) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Unsupported gravity ");
                stringBuilder.append(color);
                throw new IllegalArgumentException(stringBuilder.toString());
            }
        }
        this.mUsageGraph.setAccentColor(a.getColor(1, 0));
        a.recycle();
    }

    public void clearPaths() {
        this.mUsageGraph.clearPaths();
    }

    public void addPath(SparseIntArray points) {
        this.mUsageGraph.addPath(points);
    }

    public void addProjectedPath(SparseIntArray points) {
        this.mUsageGraph.addProjectedPath(points);
    }

    public void configureGraph(int maxX, int maxY) {
        this.mUsageGraph.setMax(maxX, maxY);
    }

    public void setAccentColor(int color) {
        this.mUsageGraph.setAccentColor(color);
    }

    public void setDividerLoc(int dividerLoc) {
        this.mUsageGraph.setDividerLoc(dividerLoc);
    }

    public void setDividerColors(int middleColor, int topColor) {
        this.mUsageGraph.setDividerColors(middleColor, topColor);
    }

    public void setSideLabelWeights(float before, float after) {
        setWeight(R.id.space1, before);
        setWeight(R.id.space2, after);
    }

    private void setWeight(int id, float weight) {
        View v = findViewById(id);
        LayoutParams params = (LayoutParams) v.getLayoutParams();
        params.weight = weight;
        v.setLayoutParams(params);
    }

    public void setSideLabels(CharSequence[] labels) {
        if (labels.length == this.mLabels.length) {
            for (int i = 0; i < this.mLabels.length; i++) {
                this.mLabels[i].setText(labels[i]);
            }
            return;
        }
        throw new IllegalArgumentException("Invalid number of labels");
    }

    public void setBottomLabels(CharSequence[] labels) {
        if (labels.length == this.mBottomLabels.length) {
            for (int i = 0; i < this.mBottomLabels.length; i++) {
                this.mBottomLabels[i].setText(labels[i]);
            }
            return;
        }
        throw new IllegalArgumentException("Invalid number of labels");
    }
}
