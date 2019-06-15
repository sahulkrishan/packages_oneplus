package com.android.settings.notification;

import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settings.widget.SeekBarPreference;
import com.oneplus.settings.notification.OPSeekBarVolumizer;
import java.util.Objects;

public class VolumeSeekBarPreference extends SeekBarPreference {
    private static final String TAG = "VolumeSeekBarPreference";
    @VisibleForTesting
    AudioManager mAudioManager;
    private Callback mCallback;
    private int mIconResId;
    private ImageView mIconView;
    private int mMuteIconResId;
    private boolean mMuted;
    private SeekBar mSeekBar;
    private boolean mStopped;
    private int mStream;
    private String mSuppressionText;
    private TextView mSuppressionTextView;
    private OPSeekBarVolumizer mVolumizer;
    private boolean mZenMuted;

    public interface Callback {
        void onSampleStarting(OPSeekBarVolumizer oPSeekBarVolumizer);

        void onStreamValueChanged(int i, int i2);
    }

    public VolumeSeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setLayoutResource(R.layout.preference_volume_slider);
        this.mAudioManager = (AudioManager) context.getSystemService("audio");
    }

    public VolumeSeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayoutResource(R.layout.preference_volume_slider);
        this.mAudioManager = (AudioManager) context.getSystemService("audio");
    }

    public VolumeSeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.preference_volume_slider);
        this.mAudioManager = (AudioManager) context.getSystemService("audio");
    }

    public VolumeSeekBarPreference(Context context) {
        super(context);
        setLayoutResource(R.layout.preference_volume_slider);
        this.mAudioManager = (AudioManager) context.getSystemService("audio");
    }

    public void setStream(int stream) {
        this.mStream = stream;
        setMax(this.mAudioManager.getStreamMaxVolume(this.mStream));
        setMin(this.mAudioManager.getStreamMinVolumeInt(this.mStream));
        setProgress(this.mAudioManager.getStreamVolume(this.mStream));
    }

    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }

    public void setSeekbar(int progress) {
        if (this.mSeekBar != null) {
            this.mSeekBar.setProgress(progress);
        }
    }

    public void onActivityResume() {
        if (this.mStopped) {
            this.mStopped = false;
            init();
        }
    }

    public void onActivityPause() {
        this.mStopped = true;
        if (this.mVolumizer != null) {
            this.mVolumizer.stop();
            this.mVolumizer = null;
        }
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        if (this.mStream == 0) {
            Log.w(TAG, "No stream found, not binding volumizer");
            return;
        }
        this.mSeekBar = (SeekBar) view.findViewById(16909273);
        this.mIconView = (ImageView) view.findViewById(16908294);
        this.mSuppressionTextView = (TextView) view.findViewById(R.id.suppression_text);
        init();
    }

    private void init() {
        if (this.mSeekBar != null) {
            com.oneplus.settings.notification.OPSeekBarVolumizer.Callback sbvc = new com.oneplus.settings.notification.OPSeekBarVolumizer.Callback() {
                public void onSampleStarting(OPSeekBarVolumizer sbv) {
                    if (VolumeSeekBarPreference.this.mCallback != null) {
                        VolumeSeekBarPreference.this.mCallback.onSampleStarting(sbv);
                    }
                }

                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                    if (VolumeSeekBarPreference.this.mCallback != null) {
                        VolumeSeekBarPreference.this.mCallback.onStreamValueChanged(VolumeSeekBarPreference.this.mStream, progress);
                        VolumeSeekBarPreference.this.updateIconView();
                    }
                }

                public void onMuted(boolean muted, boolean zenMuted) {
                    VolumeSeekBarPreference.this.mMuted = muted;
                    VolumeSeekBarPreference.this.mZenMuted = zenMuted;
                    VolumeSeekBarPreference.this.updateIconView();
                }
            };
            Uri sampleUri = this.mStream == 3 ? getMediaVolumeUri() : null;
            if (this.mVolumizer == null) {
                this.mVolumizer = new OPSeekBarVolumizer(getContext(), this.mStream, sampleUri, sbvc);
            }
            this.mVolumizer.start();
            this.mVolumizer.setSeekBar(this.mSeekBar);
            updateIconView();
            updateSuppressionText();
            if (!isEnabled()) {
                this.mSeekBar.setEnabled(false);
                this.mVolumizer.stop();
            }
        }
    }

    private void updateIconView() {
        if (this.mIconView != null && !this.mStopped) {
            if (this.mIconResId != 0) {
                this.mIconView.setImageResource(this.mIconResId);
            } else if (this.mSeekBar.getProgress() != 0 || this.mMuteIconResId == 0) {
                this.mIconView.setImageDrawable(getIcon());
            } else {
                this.mIconView.setImageResource(this.mMuteIconResId);
            }
        }
    }

    public void showIcon(int resId) {
        if (this.mIconResId != resId) {
            this.mIconResId = resId;
            updateIconView();
        }
    }

    public void setMuteIcon(int resId) {
        if (this.mMuteIconResId != resId) {
            this.mMuteIconResId = resId;
            updateIconView();
        }
    }

    private Uri getMediaVolumeUri() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("android.resource://");
        stringBuilder.append(getContext().getPackageName());
        stringBuilder.append("/");
        stringBuilder.append(R.raw.media_volume);
        return Uri.parse(stringBuilder.toString());
    }

    public void setSuppressionText(String text) {
        if (!Objects.equals(text, this.mSuppressionText)) {
            this.mSuppressionText = text;
            updateSuppressionText();
        }
    }

    private void updateSuppressionText() {
        if (this.mSuppressionTextView != null && this.mSeekBar != null) {
            this.mSuppressionTextView.setText(this.mSuppressionText);
            boolean showSuppression = TextUtils.isEmpty(this.mSuppressionText) ^ 1;
            int i = 0;
            this.mSuppressionTextView.setVisibility(showSuppression ? 0 : 8);
            SeekBar seekBar = this.mSeekBar;
            if (showSuppression) {
                i = 4;
            }
            seekBar.setVisibility(i);
        }
    }
}
