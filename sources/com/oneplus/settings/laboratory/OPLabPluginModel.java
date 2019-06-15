package com.oneplus.settings.laboratory;

public class OPLabPluginModel {
    private int featureIconId;
    private String featureKey;
    private String featureSummary;
    private String featureTitle;
    private String[] multiToggleName;
    private String packageName;
    private int toggleCount;

    public void setFeatureIconId(int featureIconId) {
        this.featureIconId = featureIconId;
    }

    public int geFeatureIconId() {
        return this.featureIconId;
    }

    public String[] getMultiToggleName() {
        return this.multiToggleName;
    }

    public void setMultiToggleName(String[] multiToggleName) {
        this.multiToggleName = multiToggleName;
    }

    public int getToggleCount() {
        return this.toggleCount;
    }

    public void setToggleCount(int toggleCount) {
        this.toggleCount = toggleCount;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getFeatureTitle() {
        return this.featureTitle;
    }

    public void setFeatureTitle(String featureTitle) {
        this.featureTitle = featureTitle;
    }

    public String getFeatureSummary() {
        return this.featureSummary;
    }

    public void setFeatureSummary(String featureSummary) {
        this.featureSummary = featureSummary;
    }

    public String getFeatureKey() {
        return this.featureKey;
    }

    public void setFeatureKey(String featureKey) {
        this.featureKey = featureKey;
    }
}
