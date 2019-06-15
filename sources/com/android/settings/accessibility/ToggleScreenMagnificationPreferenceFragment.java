package com.android.settings.accessibility;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.PreferenceViewHolder;
import android.view.Display;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Switch;
import android.widget.VideoView;
import com.android.settings.R;
import com.android.settings.widget.SwitchBar.OnSwitchChangeListener;

public class ToggleScreenMagnificationPreferenceFragment extends ToggleFeaturePreferenceFragment implements OnSwitchChangeListener {
    protected Preference mConfigWarningPreference;
    private boolean mInitialSetting = false;
    private boolean mLaunchFromSuw = false;
    protected VideoPreference mVideoPreference;

    protected class VideoPreference extends Preference {
        private OnGlobalLayoutListener mLayoutListener;
        private ImageView mVideoBackgroundView;

        public VideoPreference(Context context) {
            super(context);
        }

        public void onBindViewHolder(PreferenceViewHolder view) {
            PreferenceViewHolder preferenceViewHolder = view;
            super.onBindViewHolder(view);
            Resources res = ToggleScreenMagnificationPreferenceFragment.this.getPrefContext().getResources();
            int backgroundAssetWidth = res.getDimensionPixelSize(R.dimen.screen_magnification_video_background_width);
            int videoAssetWidth = res.getDimensionPixelSize(R.dimen.screen_magnification_video_width);
            int videoAssetHeight = res.getDimensionPixelSize(R.dimen.screen_magnification_video_height);
            int videoAssetMarginTop = res.getDimensionPixelSize(R.dimen.screen_magnification_video_margin_top);
            preferenceViewHolder.setDividerAllowedAbove(false);
            preferenceViewHolder.setDividerAllowedBelow(false);
            this.mVideoBackgroundView = (ImageView) preferenceViewHolder.findViewById(R.id.video_background);
            VideoView videoView = (VideoView) preferenceViewHolder.findViewById(R.id.video);
            videoView.setOnPreparedListener(new OnPreparedListener() {
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.setLooping(true);
                }
            });
            videoView.setAudioFocusRequest(0);
            Bundle args = ToggleScreenMagnificationPreferenceFragment.this.getArguments();
            if (args != null && args.containsKey("video_resource")) {
                videoView.setVideoURI(Uri.parse(String.format("%s://%s/%s", new Object[]{"android.resource", ToggleScreenMagnificationPreferenceFragment.this.getPrefContext().getPackageName(), Integer.valueOf(args.getInt("video_resource"))})));
            }
            videoView.setMediaController(null);
            final VideoView videoView2 = videoView;
            final int i = videoAssetWidth;
            final int i2 = backgroundAssetWidth;
            final int i3 = videoAssetHeight;
            AnonymousClass2 anonymousClass2 = r0;
            final int i4 = videoAssetMarginTop;
            AnonymousClass2 anonymousClass22 = new OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                    int backgroundViewWidth = VideoPreference.this.mVideoBackgroundView.getWidth();
                    LayoutParams videoLp = (LayoutParams) videoView2.getLayoutParams();
                    videoLp.width = (i * backgroundViewWidth) / i2;
                    videoLp.height = (i3 * backgroundViewWidth) / i2;
                    videoLp.setMargins(0, (i4 * backgroundViewWidth) / i2, 0, 0);
                    videoView2.setLayoutParams(videoLp);
                    videoView2.invalidate();
                    videoView2.start();
                }
            };
            this.mLayoutListener = anonymousClass2;
            this.mVideoBackgroundView.getViewTreeObserver().addOnGlobalLayoutListener(this.mLayoutListener);
        }

        /* Access modifiers changed, original: protected */
        public void onPrepareForRemoval() {
            this.mVideoBackgroundView.getViewTreeObserver().removeOnGlobalLayoutListener(this.mLayoutListener);
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mVideoPreference = new VideoPreference(getPrefContext());
        this.mVideoPreference.setSelectable(false);
        this.mVideoPreference.setPersistent(false);
        this.mVideoPreference.setLayoutResource(R.layout.magnification_video_preference);
        this.mConfigWarningPreference = new Preference(getPrefContext());
        this.mConfigWarningPreference.setSelectable(false);
        this.mConfigWarningPreference.setPersistent(false);
        this.mConfigWarningPreference.setVisible(false);
        this.mConfigWarningPreference.setIcon((int) R.drawable.ic_warning_24dp);
        PreferenceScreen preferenceScreen = getPreferenceManager().getPreferenceScreen();
        preferenceScreen.setOrderingAsAdded(false);
        this.mVideoPreference.setOrder(0);
        this.mConfigWarningPreference.setOrder(2);
        preferenceScreen.addPreference(this.mVideoPreference);
        preferenceScreen.addPreference(this.mConfigWarningPreference);
    }

    public void onResume() {
        super.onResume();
        VideoView videoView = (VideoView) getView().findViewById(R.id.video);
        if (videoView != null) {
            videoView.start();
        }
        updateConfigurationWarningIfNeeded();
    }

    public int getMetricsCategory() {
        return 7;
    }

    public void onSwitchChanged(Switch switchView, boolean isChecked) {
        onPreferenceToggled(this.mPreferenceKey, isChecked);
    }

    /* Access modifiers changed, original: protected */
    public void onPreferenceToggled(String preferenceKey, boolean enabled) {
        MagnificationPreferenceFragment.setChecked(getContentResolver(), preferenceKey, enabled);
        updateConfigurationWarningIfNeeded();
    }

    /* Access modifiers changed, original: protected */
    public void onInstallSwitchBarToggleSwitch() {
        super.onInstallSwitchBarToggleSwitch();
        this.mSwitchBar.setCheckedInternal(MagnificationPreferenceFragment.isChecked(getContentResolver(), this.mPreferenceKey));
        this.mSwitchBar.addOnSwitchChangeListener(this);
    }

    /* Access modifiers changed, original: protected */
    public void onRemoveSwitchBarToggleSwitch() {
        super.onRemoveSwitchBarToggleSwitch();
        this.mSwitchBar.removeOnSwitchChangeListener(this);
    }

    /* Access modifiers changed, original: protected */
    public void onProcessArguments(Bundle arguments) {
        super.onProcessArguments(arguments);
        if (arguments != null) {
            if (arguments.containsKey("video_resource")) {
                this.mVideoPreference.setVisible(true);
                arguments.getInt("video_resource");
            } else {
                this.mVideoPreference.setVisible(false);
            }
            if (arguments.containsKey("from_suw")) {
                this.mLaunchFromSuw = arguments.getBoolean("from_suw");
            }
            if (arguments.containsKey("checked")) {
                this.mInitialSetting = arguments.getBoolean("checked");
            }
            if (arguments.containsKey("title_res")) {
                int titleRes = arguments.getInt("title_res");
                if (titleRes > 0) {
                    getActivity().setTitle(titleRes);
                }
            }
        }
    }

    private void updateConfigurationWarningIfNeeded() {
        CharSequence warningMessage = MagnificationPreferenceFragment.getConfigurationWarningStringForSecureSettingsKey(this.mPreferenceKey, getPrefContext());
        if (warningMessage != null) {
            this.mConfigWarningPreference.setSummary(warningMessage);
        }
        this.mConfigWarningPreference.setVisible(warningMessage != null);
    }

    private static int getScreenWidth(Context context) {
        Display display = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }
}
