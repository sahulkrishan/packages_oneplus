package com.android.settings.development;

import android.bluetooth.BluetoothCodecConfig;

public class BluetoothA2dpConfigStore {
    private int mBitsPerSample = 0;
    private int mChannelMode = 0;
    private int mCodecPriority = 0;
    private long mCodecSpecific1Value;
    private long mCodecSpecific2Value;
    private long mCodecSpecific3Value;
    private long mCodecSpecific4Value;
    private int mCodecType = 1000000;
    private int mSampleRate = 0;

    public void setCodecType(int codecType) {
        this.mCodecType = codecType;
    }

    public void setCodecPriority(int codecPriority) {
        this.mCodecPriority = codecPriority;
    }

    public void setSampleRate(int sampleRate) {
        this.mSampleRate = sampleRate;
    }

    public void setBitsPerSample(int bitsPerSample) {
        this.mBitsPerSample = bitsPerSample;
    }

    public void setChannelMode(int channelMode) {
        this.mChannelMode = channelMode;
    }

    public void setCodecSpecific1Value(int codecSpecific1Value) {
        this.mCodecSpecific1Value = (long) codecSpecific1Value;
    }

    public void setCodecSpecific2Value(int codecSpecific2Value) {
        this.mCodecSpecific2Value = (long) codecSpecific2Value;
    }

    public void setCodecSpecific3Value(int codecSpecific3Value) {
        this.mCodecSpecific3Value = (long) codecSpecific3Value;
    }

    public void setCodecSpecific4Value(int codecSpecific4Value) {
        this.mCodecSpecific4Value = (long) codecSpecific4Value;
    }

    public BluetoothCodecConfig createCodecConfig() {
        return new BluetoothCodecConfig(this.mCodecType, this.mCodecPriority, this.mSampleRate, this.mBitsPerSample, this.mChannelMode, this.mCodecSpecific1Value, this.mCodecSpecific2Value, this.mCodecSpecific3Value, this.mCodecSpecific4Value);
    }
}
