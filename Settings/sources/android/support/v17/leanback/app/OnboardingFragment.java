package android.support.v17.leanback.app;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v17.leanback.R;
import android.support.v17.leanback.widget.PagingIndicator;
import android.support.v4.view.GravityCompat;
import android.util.Property;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

@Deprecated
public abstract class OnboardingFragment extends Fragment {
    private static final boolean DEBUG = false;
    private static final long DESCRIPTION_START_DELAY_MS = 33;
    private static final long HEADER_ANIMATION_DURATION_MS = 417;
    private static final long HEADER_APPEAR_DELAY_MS = 500;
    private static final TimeInterpolator HEADER_APPEAR_INTERPOLATOR = new DecelerateInterpolator();
    private static final TimeInterpolator HEADER_DISAPPEAR_INTERPOLATOR = new AccelerateInterpolator();
    private static final String KEY_CURRENT_PAGE_INDEX = "leanback.onboarding.current_page_index";
    private static final String KEY_ENTER_ANIMATION_FINISHED = "leanback.onboarding.enter_animation_finished";
    private static final String KEY_LOGO_ANIMATION_FINISHED = "leanback.onboarding.logo_animation_finished";
    private static final long LOGO_SPLASH_PAUSE_DURATION_MS = 1333;
    private static final int SLIDE_DISTANCE = 60;
    private static final String TAG = "OnboardingF";
    private static int sSlideDistance;
    private AnimatorSet mAnimator;
    @ColorInt
    private int mArrowBackgroundColor = 0;
    private boolean mArrowBackgroundColorSet;
    @ColorInt
    private int mArrowColor = 0;
    private boolean mArrowColorSet;
    int mCurrentPageIndex;
    TextView mDescriptionView;
    @ColorInt
    private int mDescriptionViewTextColor = 0;
    private boolean mDescriptionViewTextColorSet;
    @ColorInt
    private int mDotBackgroundColor = 0;
    private boolean mDotBackgroundColorSet;
    boolean mEnterAnimationFinished;
    private int mIconResourceId;
    boolean mIsLtr;
    boolean mLogoAnimationFinished;
    private int mLogoResourceId;
    private ImageView mLogoView;
    private ImageView mMainIconView;
    private final OnClickListener mOnClickListener = new OnClickListener() {
        public void onClick(View view) {
            if (OnboardingFragment.this.mLogoAnimationFinished) {
                if (OnboardingFragment.this.mCurrentPageIndex == OnboardingFragment.this.getPageCount() - 1) {
                    OnboardingFragment.this.onFinishFragment();
                } else {
                    OnboardingFragment.this.moveToNextPage();
                }
            }
        }
    };
    private final OnKeyListener mOnKeyListener = new OnKeyListener() {
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            boolean z = false;
            if (!OnboardingFragment.this.mLogoAnimationFinished) {
                if (keyCode != 4) {
                    z = true;
                }
                return z;
            } else if (event.getAction() == 0) {
                return false;
            } else {
                if (keyCode != 4) {
                    switch (keyCode) {
                        case 21:
                            if (OnboardingFragment.this.mIsLtr) {
                                OnboardingFragment.this.moveToPreviousPage();
                            } else {
                                OnboardingFragment.this.moveToNextPage();
                            }
                            return true;
                        case 22:
                            if (OnboardingFragment.this.mIsLtr) {
                                OnboardingFragment.this.moveToNextPage();
                            } else {
                                OnboardingFragment.this.moveToPreviousPage();
                            }
                            return true;
                        default:
                            return false;
                    }
                } else if (OnboardingFragment.this.mCurrentPageIndex == 0) {
                    return false;
                } else {
                    OnboardingFragment.this.moveToPreviousPage();
                    return true;
                }
            }
        }
    };
    PagingIndicator mPageIndicator;
    View mStartButton;
    private CharSequence mStartButtonText;
    private boolean mStartButtonTextSet;
    private ContextThemeWrapper mThemeWrapper;
    TextView mTitleView;
    @ColorInt
    private int mTitleViewTextColor = 0;
    private boolean mTitleViewTextColorSet;

    public abstract int getPageCount();

    public abstract CharSequence getPageDescription(int i);

    public abstract CharSequence getPageTitle(int i);

    @Nullable
    public abstract View onCreateBackgroundView(LayoutInflater layoutInflater, ViewGroup viewGroup);

    @Nullable
    public abstract View onCreateContentView(LayoutInflater layoutInflater, ViewGroup viewGroup);

    @Nullable
    public abstract View onCreateForegroundView(LayoutInflater layoutInflater, ViewGroup viewGroup);

    /* Access modifiers changed, original: protected */
    public void moveToPreviousPage() {
        if (this.mLogoAnimationFinished && this.mCurrentPageIndex > 0) {
            this.mCurrentPageIndex--;
            onPageChangedInternal(this.mCurrentPageIndex + 1);
        }
    }

    /* Access modifiers changed, original: protected */
    public void moveToNextPage() {
        if (this.mLogoAnimationFinished && this.mCurrentPageIndex < getPageCount() - 1) {
            this.mCurrentPageIndex++;
            onPageChangedInternal(this.mCurrentPageIndex - 1);
        }
    }

    @Nullable
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        resolveTheme();
        boolean z = false;
        ViewGroup view = (ViewGroup) getThemeInflater(inflater).inflate(R.layout.lb_onboarding_fragment, container, false);
        if (getResources().getConfiguration().getLayoutDirection() == 0) {
            z = true;
        }
        this.mIsLtr = z;
        this.mPageIndicator = (PagingIndicator) view.findViewById(R.id.page_indicator);
        this.mPageIndicator.setOnClickListener(this.mOnClickListener);
        this.mPageIndicator.setOnKeyListener(this.mOnKeyListener);
        this.mStartButton = view.findViewById(R.id.button_start);
        this.mStartButton.setOnClickListener(this.mOnClickListener);
        this.mStartButton.setOnKeyListener(this.mOnKeyListener);
        this.mMainIconView = (ImageView) view.findViewById(R.id.main_icon);
        this.mLogoView = (ImageView) view.findViewById(R.id.logo);
        this.mTitleView = (TextView) view.findViewById(R.id.title);
        this.mDescriptionView = (TextView) view.findViewById(R.id.description);
        if (this.mTitleViewTextColorSet) {
            this.mTitleView.setTextColor(this.mTitleViewTextColor);
        }
        if (this.mDescriptionViewTextColorSet) {
            this.mDescriptionView.setTextColor(this.mDescriptionViewTextColor);
        }
        if (this.mDotBackgroundColorSet) {
            this.mPageIndicator.setDotBackgroundColor(this.mDotBackgroundColor);
        }
        if (this.mArrowColorSet) {
            this.mPageIndicator.setArrowColor(this.mArrowColor);
        }
        if (this.mArrowBackgroundColorSet) {
            this.mPageIndicator.setDotBackgroundColor(this.mArrowBackgroundColor);
        }
        if (this.mStartButtonTextSet) {
            ((Button) this.mStartButton).setText(this.mStartButtonText);
        }
        Context context = FragmentUtil.getContext(this);
        if (sSlideDistance == 0) {
            sSlideDistance = (int) (60.0f * context.getResources().getDisplayMetrics().scaledDensity);
        }
        view.requestFocus();
        return view;
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState == null) {
            this.mCurrentPageIndex = 0;
            this.mLogoAnimationFinished = false;
            this.mEnterAnimationFinished = false;
            this.mPageIndicator.onPageSelected(0, false);
            view.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
                public boolean onPreDraw() {
                    OnboardingFragment.this.getView().getViewTreeObserver().removeOnPreDrawListener(this);
                    if (!OnboardingFragment.this.startLogoAnimation()) {
                        OnboardingFragment.this.mLogoAnimationFinished = true;
                        OnboardingFragment.this.onLogoAnimationFinished();
                    }
                    return true;
                }
            });
            return;
        }
        this.mCurrentPageIndex = savedInstanceState.getInt(KEY_CURRENT_PAGE_INDEX);
        this.mLogoAnimationFinished = savedInstanceState.getBoolean(KEY_LOGO_ANIMATION_FINISHED);
        this.mEnterAnimationFinished = savedInstanceState.getBoolean(KEY_ENTER_ANIMATION_FINISHED);
        if (this.mLogoAnimationFinished) {
            onLogoAnimationFinished();
        } else if (!startLogoAnimation()) {
            this.mLogoAnimationFinished = true;
            onLogoAnimationFinished();
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_CURRENT_PAGE_INDEX, this.mCurrentPageIndex);
        outState.putBoolean(KEY_LOGO_ANIMATION_FINISHED, this.mLogoAnimationFinished);
        outState.putBoolean(KEY_ENTER_ANIMATION_FINISHED, this.mEnterAnimationFinished);
    }

    public void setTitleViewTextColor(@ColorInt int color) {
        this.mTitleViewTextColor = color;
        this.mTitleViewTextColorSet = true;
        if (this.mTitleView != null) {
            this.mTitleView.setTextColor(color);
        }
    }

    @ColorInt
    public final int getTitleViewTextColor() {
        return this.mTitleViewTextColor;
    }

    public void setDescriptionViewTextColor(@ColorInt int color) {
        this.mDescriptionViewTextColor = color;
        this.mDescriptionViewTextColorSet = true;
        if (this.mDescriptionView != null) {
            this.mDescriptionView.setTextColor(color);
        }
    }

    @ColorInt
    public final int getDescriptionViewTextColor() {
        return this.mDescriptionViewTextColor;
    }

    public void setDotBackgroundColor(@ColorInt int color) {
        this.mDotBackgroundColor = color;
        this.mDotBackgroundColorSet = true;
        if (this.mPageIndicator != null) {
            this.mPageIndicator.setDotBackgroundColor(color);
        }
    }

    @ColorInt
    public final int getDotBackgroundColor() {
        return this.mDotBackgroundColor;
    }

    public void setArrowColor(@ColorInt int color) {
        this.mArrowColor = color;
        this.mArrowColorSet = true;
        if (this.mPageIndicator != null) {
            this.mPageIndicator.setArrowColor(color);
        }
    }

    @ColorInt
    public final int getArrowColor() {
        return this.mArrowColor;
    }

    public void setArrowBackgroundColor(@ColorInt int color) {
        this.mArrowBackgroundColor = color;
        this.mArrowBackgroundColorSet = true;
        if (this.mPageIndicator != null) {
            this.mPageIndicator.setArrowBackgroundColor(color);
        }
    }

    @ColorInt
    public final int getArrowBackgroundColor() {
        return this.mArrowBackgroundColor;
    }

    public final CharSequence getStartButtonText() {
        return this.mStartButtonText;
    }

    public void setStartButtonText(CharSequence text) {
        this.mStartButtonText = text;
        this.mStartButtonTextSet = true;
        if (this.mStartButton != null) {
            ((Button) this.mStartButton).setText(this.mStartButtonText);
        }
    }

    public int onProvideTheme() {
        return -1;
    }

    private void resolveTheme() {
        Context context = FragmentUtil.getContext(this);
        int theme = onProvideTheme();
        if (theme == -1) {
            int resId = R.attr.onboardingTheme;
            TypedValue typedValue = new TypedValue();
            if (context.getTheme().resolveAttribute(resId, typedValue, true)) {
                this.mThemeWrapper = new ContextThemeWrapper(context, typedValue.resourceId);
                return;
            }
            return;
        }
        this.mThemeWrapper = new ContextThemeWrapper(context, theme);
    }

    private LayoutInflater getThemeInflater(LayoutInflater inflater) {
        return this.mThemeWrapper == null ? inflater : inflater.cloneInContext(this.mThemeWrapper);
    }

    public final void setLogoResourceId(int id) {
        this.mLogoResourceId = id;
    }

    public final int getLogoResourceId() {
        return this.mLogoResourceId;
    }

    /* Access modifiers changed, original: protected */
    @Nullable
    public Animator onCreateLogoAnimation() {
        return null;
    }

    /* Access modifiers changed, original: 0000 */
    public boolean startLogoAnimation() {
        final Context context = FragmentUtil.getContext(this);
        if (context == null) {
            return false;
        }
        Animator animator;
        if (this.mLogoResourceId != 0) {
            this.mLogoView.setVisibility(0);
            this.mLogoView.setImageResource(this.mLogoResourceId);
            Animator inAnimator = AnimatorInflater.loadAnimator(context, R.animator.lb_onboarding_logo_enter);
            AnimatorInflater.loadAnimator(context, R.animator.lb_onboarding_logo_exit).setStartDelay(LOGO_SPLASH_PAUSE_DURATION_MS);
            Animator logoAnimator = new AnimatorSet();
            logoAnimator.playSequentially(new Animator[]{inAnimator, outAnimator});
            logoAnimator.setTarget(this.mLogoView);
            animator = logoAnimator;
        } else {
            animator = onCreateLogoAnimation();
        }
        if (animator == null) {
            return false;
        }
        animator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                if (context != null) {
                    OnboardingFragment.this.mLogoAnimationFinished = true;
                    OnboardingFragment.this.onLogoAnimationFinished();
                }
            }
        });
        animator.start();
        return true;
    }

    /* Access modifiers changed, original: protected */
    @Nullable
    public Animator onCreateEnterAnimation() {
        return null;
    }

    /* Access modifiers changed, original: 0000 */
    public void hideLogoView() {
        this.mLogoView.setVisibility(8);
        if (this.mIconResourceId != 0) {
            this.mMainIconView.setImageResource(this.mIconResourceId);
            this.mMainIconView.setVisibility(0);
        }
        View container = getView();
        LayoutInflater inflater = getThemeInflater(LayoutInflater.from(FragmentUtil.getContext(this)));
        ViewGroup backgroundContainer = (ViewGroup) container.findViewById(R.id.background_container);
        View background = onCreateBackgroundView(inflater, backgroundContainer);
        if (background != null) {
            backgroundContainer.setVisibility(0);
            backgroundContainer.addView(background);
        }
        ViewGroup contentContainer = (ViewGroup) container.findViewById(R.id.content_container);
        View content = onCreateContentView(inflater, contentContainer);
        if (content != null) {
            contentContainer.setVisibility(0);
            contentContainer.addView(content);
        }
        ViewGroup foregroundContainer = (ViewGroup) container.findViewById(R.id.foreground_container);
        View foreground = onCreateForegroundView(inflater, foregroundContainer);
        if (foreground != null) {
            foregroundContainer.setVisibility(0);
            foregroundContainer.addView(foreground);
        }
        container.findViewById(R.id.page_container).setVisibility(0);
        container.findViewById(R.id.content_container).setVisibility(0);
        if (getPageCount() > 1) {
            this.mPageIndicator.setPageCount(getPageCount());
            this.mPageIndicator.onPageSelected(this.mCurrentPageIndex, false);
        }
        if (this.mCurrentPageIndex == getPageCount() - 1) {
            this.mStartButton.setVisibility(0);
        } else {
            this.mPageIndicator.setVisibility(0);
        }
        this.mTitleView.setText(getPageTitle(this.mCurrentPageIndex));
        this.mDescriptionView.setText(getPageDescription(this.mCurrentPageIndex));
    }

    /* Access modifiers changed, original: protected */
    public void onLogoAnimationFinished() {
        startEnterAnimation(false);
    }

    /* Access modifiers changed, original: protected|final */
    public final void startEnterAnimation(boolean force) {
        Context context = FragmentUtil.getContext(this);
        if (context != null) {
            hideLogoView();
            if (!this.mEnterAnimationFinished || force) {
                List<Animator> animators = new ArrayList();
                Animator animator = AnimatorInflater.loadAnimator(context, R.animator.lb_onboarding_page_indicator_enter);
                animator.setTarget(getPageCount() <= 1 ? this.mStartButton : this.mPageIndicator);
                animators.add(animator);
                animator = onCreateTitleAnimator();
                if (animator != null) {
                    animator.setTarget(this.mTitleView);
                    animators.add(animator);
                }
                animator = onCreateDescriptionAnimator();
                if (animator != null) {
                    animator.setTarget(this.mDescriptionView);
                    animators.add(animator);
                }
                Animator customAnimator = onCreateEnterAnimation();
                if (customAnimator != null) {
                    animators.add(customAnimator);
                }
                if (!animators.isEmpty()) {
                    this.mAnimator = new AnimatorSet();
                    this.mAnimator.playTogether(animators);
                    this.mAnimator.start();
                    this.mAnimator.addListener(new AnimatorListenerAdapter() {
                        public void onAnimationEnd(Animator animation) {
                            OnboardingFragment.this.mEnterAnimationFinished = true;
                        }
                    });
                    getView().requestFocus();
                }
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public Animator onCreateDescriptionAnimator() {
        return AnimatorInflater.loadAnimator(FragmentUtil.getContext(this), R.animator.lb_onboarding_description_enter);
    }

    /* Access modifiers changed, original: protected */
    public Animator onCreateTitleAnimator() {
        return AnimatorInflater.loadAnimator(FragmentUtil.getContext(this), R.animator.lb_onboarding_title_enter);
    }

    /* Access modifiers changed, original: protected|final */
    public final boolean isLogoAnimationFinished() {
        return this.mLogoAnimationFinished;
    }

    /* Access modifiers changed, original: protected|final */
    public final int getCurrentPageIndex() {
        return this.mCurrentPageIndex;
    }

    /* Access modifiers changed, original: protected */
    public void onFinishFragment() {
    }

    private void onPageChangedInternal(int previousPage) {
        Animator fadeAnimator;
        if (this.mAnimator != null) {
            this.mAnimator.end();
        }
        this.mPageIndicator.onPageSelected(this.mCurrentPageIndex, true);
        List<Animator> animators = new ArrayList();
        Animator createAnimator;
        if (previousPage < getCurrentPageIndex()) {
            animators.add(createAnimator(this.mTitleView, false, GravityCompat.START, 0));
            createAnimator = createAnimator(this.mDescriptionView, false, GravityCompat.START, DESCRIPTION_START_DELAY_MS);
            fadeAnimator = createAnimator;
            animators.add(createAnimator);
            animators.add(createAnimator(this.mTitleView, true, GravityCompat.END, HEADER_APPEAR_DELAY_MS));
            animators.add(createAnimator(this.mDescriptionView, true, GravityCompat.END, 533));
        } else {
            animators.add(createAnimator(this.mTitleView, false, GravityCompat.END, 0));
            createAnimator = createAnimator(this.mDescriptionView, false, GravityCompat.END, DESCRIPTION_START_DELAY_MS);
            fadeAnimator = createAnimator;
            animators.add(createAnimator);
            animators.add(createAnimator(this.mTitleView, true, GravityCompat.START, HEADER_APPEAR_DELAY_MS));
            animators.add(createAnimator(this.mDescriptionView, true, GravityCompat.START, 533));
        }
        final int currentPageIndex = getCurrentPageIndex();
        fadeAnimator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                OnboardingFragment.this.mTitleView.setText(OnboardingFragment.this.getPageTitle(currentPageIndex));
                OnboardingFragment.this.mDescriptionView.setText(OnboardingFragment.this.getPageDescription(currentPageIndex));
            }
        });
        Context context = FragmentUtil.getContext(this);
        Animator navigatorFadeOutAnimator;
        Animator buttonFadeInAnimator;
        if (getCurrentPageIndex() == getPageCount() - 1) {
            this.mStartButton.setVisibility(0);
            navigatorFadeOutAnimator = AnimatorInflater.loadAnimator(context, R.animator.lb_onboarding_page_indicator_fade_out);
            navigatorFadeOutAnimator.setTarget(this.mPageIndicator);
            navigatorFadeOutAnimator.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    OnboardingFragment.this.mPageIndicator.setVisibility(8);
                }
            });
            animators.add(navigatorFadeOutAnimator);
            buttonFadeInAnimator = AnimatorInflater.loadAnimator(context, R.animator.lb_onboarding_start_button_fade_in);
            buttonFadeInAnimator.setTarget(this.mStartButton);
            animators.add(buttonFadeInAnimator);
        } else if (previousPage == getPageCount() - 1) {
            this.mPageIndicator.setVisibility(0);
            navigatorFadeOutAnimator = AnimatorInflater.loadAnimator(context, R.animator.lb_onboarding_page_indicator_fade_in);
            navigatorFadeOutAnimator.setTarget(this.mPageIndicator);
            animators.add(navigatorFadeOutAnimator);
            buttonFadeInAnimator = AnimatorInflater.loadAnimator(context, R.animator.lb_onboarding_start_button_fade_out);
            buttonFadeInAnimator.setTarget(this.mStartButton);
            buttonFadeInAnimator.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    OnboardingFragment.this.mStartButton.setVisibility(8);
                }
            });
            animators.add(buttonFadeInAnimator);
        }
        this.mAnimator = new AnimatorSet();
        this.mAnimator.playTogether(animators);
        this.mAnimator.start();
        onPageChanged(this.mCurrentPageIndex, previousPage);
    }

    /* Access modifiers changed, original: protected */
    public void onPageChanged(int newPage, int previousPage) {
    }

    private Animator createAnimator(View view, boolean fadeIn, int slideDirection, long startDelay) {
        Animator fadeAnimator;
        Animator slideAnimator;
        boolean isLtr = getView().getLayoutDirection() == 0;
        boolean slideRight = (isLtr && slideDirection == GravityCompat.END) || ((!isLtr && slideDirection == GravityCompat.START) || slideDirection == 5);
        Property property;
        float[] fArr;
        if (fadeIn) {
            fadeAnimator = ObjectAnimator.ofFloat(view, View.ALPHA, new float[]{0.0f, 1.0f});
            property = View.TRANSLATION_X;
            fArr = new float[2];
            fArr[0] = (float) (slideRight ? sSlideDistance : -sSlideDistance);
            fArr[1] = 0.0f;
            slideAnimator = ObjectAnimator.ofFloat(view, property, fArr);
            fadeAnimator.setInterpolator(HEADER_APPEAR_INTERPOLATOR);
            slideAnimator.setInterpolator(HEADER_APPEAR_INTERPOLATOR);
        } else {
            fadeAnimator = ObjectAnimator.ofFloat(view, View.ALPHA, new float[]{1.0f, 0.0f});
            property = View.TRANSLATION_X;
            fArr = new float[2];
            fArr[0] = 0.0f;
            fArr[1] = (float) (slideRight ? sSlideDistance : -sSlideDistance);
            slideAnimator = ObjectAnimator.ofFloat(view, property, fArr);
            fadeAnimator.setInterpolator(HEADER_DISAPPEAR_INTERPOLATOR);
            slideAnimator.setInterpolator(HEADER_DISAPPEAR_INTERPOLATOR);
        }
        fadeAnimator.setDuration(HEADER_ANIMATION_DURATION_MS);
        fadeAnimator.setTarget(view);
        slideAnimator.setDuration(HEADER_ANIMATION_DURATION_MS);
        slideAnimator.setTarget(view);
        AnimatorSet animator = new AnimatorSet();
        animator.playTogether(new Animator[]{fadeAnimator, slideAnimator});
        if (startDelay > 0) {
            animator.setStartDelay(startDelay);
        }
        return animator;
    }

    public final void setIconResouceId(int resourceId) {
        this.mIconResourceId = resourceId;
        if (this.mMainIconView != null) {
            this.mMainIconView.setImageResource(resourceId);
            this.mMainIconView.setVisibility(0);
        }
    }

    public final int getIconResourceId() {
        return this.mIconResourceId;
    }
}
