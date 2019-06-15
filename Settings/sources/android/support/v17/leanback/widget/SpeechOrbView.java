package android.support.v17.leanback.widget;

import android.content.Context;
import android.content.res.Resources;
import android.support.v17.leanback.R;
import android.support.v17.leanback.widget.SearchOrbView.Colors;
import android.util.AttributeSet;

public class SpeechOrbView extends SearchOrbView {
    private int mCurrentLevel;
    private boolean mListening;
    private Colors mListeningOrbColors;
    private Colors mNotListeningOrbColors;
    private final float mSoundLevelMaxZoom;

    public SpeechOrbView(Context context) {
        this(context, null);
    }

    public SpeechOrbView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SpeechOrbView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mCurrentLevel = 0;
        this.mListening = false;
        Resources resources = context.getResources();
        this.mSoundLevelMaxZoom = resources.getFraction(R.fraction.lb_search_bar_speech_orb_max_level_zoom, 1, 1);
        this.mNotListeningOrbColors = new Colors(resources.getColor(R.color.lb_speech_orb_not_recording), resources.getColor(R.color.lb_speech_orb_not_recording_pulsed), resources.getColor(R.color.lb_speech_orb_not_recording_icon));
        this.mListeningOrbColors = new Colors(resources.getColor(R.color.lb_speech_orb_recording), resources.getColor(R.color.lb_speech_orb_recording), 0);
        showNotListening();
    }

    /* Access modifiers changed, original: 0000 */
    public int getLayoutResourceId() {
        return R.layout.lb_speech_orb;
    }

    public void setListeningOrbColors(Colors colors) {
        this.mListeningOrbColors = colors;
    }

    public void setNotListeningOrbColors(Colors colors) {
        this.mNotListeningOrbColors = colors;
    }

    public void showListening() {
        setOrbColors(this.mListeningOrbColors);
        setOrbIcon(getResources().getDrawable(R.drawable.lb_ic_search_mic));
        animateOnFocus(true);
        enableOrbColorAnimation(false);
        scaleOrbViewOnly(1.0f);
        this.mCurrentLevel = 0;
        this.mListening = true;
    }

    public void showNotListening() {
        setOrbColors(this.mNotListeningOrbColors);
        setOrbIcon(getResources().getDrawable(R.drawable.lb_ic_search_mic_out));
        animateOnFocus(hasFocus());
        scaleOrbViewOnly(1.0f);
        this.mListening = false;
    }

    public void setSoundLevel(int level) {
        if (this.mListening) {
            if (level > this.mCurrentLevel) {
                this.mCurrentLevel += (level - this.mCurrentLevel) / 2;
            } else {
                this.mCurrentLevel = (int) (((float) this.mCurrentLevel) * 0.7f);
            }
            scaleOrbViewOnly(1.0f + (((this.mSoundLevelMaxZoom - getFocusedZoom()) * ((float) this.mCurrentLevel)) / 100.0f));
        }
    }
}
