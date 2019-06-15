package android.support.v17.leanback.widget;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v17.leanback.R;
import android.support.v17.leanback.transition.TransitionEpicenterCallback;
import android.support.v17.leanback.transition.TransitionHelper;
import android.support.v17.leanback.transition.TransitionListener;
import android.support.v17.leanback.widget.GuidedActionAdapter.EditListener;
import android.support.v17.leanback.widget.ItemAlignmentFacet.ItemAlignmentDef;
import android.support.v17.leanback.widget.picker.DatePicker;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.BuildCompat;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.AccessibilityDelegate;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Checkable;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class GuidedActionsStylist implements FragmentAnimationProvider {
    private static final String TAG = "GuidedActionsStylist";
    public static final int VIEW_TYPE_DATE_PICKER = 1;
    public static final int VIEW_TYPE_DEFAULT = 0;
    static final ItemAlignmentFacet sGuidedActionItemAlignFacet = new ItemAlignmentFacet();
    private VerticalGridView mActionsGridView;
    private boolean mBackToCollapseActivatorView = true;
    private boolean mBackToCollapseSubActions = true;
    private View mBgView;
    private boolean mButtonActions;
    private View mContentView;
    private int mDescriptionMinLines;
    private float mDisabledChevronAlpha;
    private float mDisabledDescriptionAlpha;
    private float mDisabledTextAlpha;
    private int mDisplayHeight;
    private EditListener mEditListener;
    private float mEnabledChevronAlpha;
    private float mEnabledDescriptionAlpha;
    private float mEnabledTextAlpha;
    Object mExpandTransition;
    private GuidedAction mExpandedAction = null;
    private float mKeyLinePercent;
    ViewGroup mMainView;
    private View mSubActionsBackground;
    VerticalGridView mSubActionsGridView;
    private int mTitleMaxLines;
    private int mTitleMinLines;
    private int mVerticalPadding;

    public static class ViewHolder extends android.support.v7.widget.RecyclerView.ViewHolder implements FacetProvider {
        GuidedAction mAction;
        View mActivatorView;
        ImageView mCheckmarkView;
        ImageView mChevronView;
        private View mContentView;
        final AccessibilityDelegate mDelegate;
        TextView mDescriptionView;
        int mEditingMode;
        ImageView mIconView;
        private final boolean mIsSubAction;
        Animator mPressAnimator;
        TextView mTitleView;

        public ViewHolder(View v) {
            this(v, false);
        }

        public ViewHolder(View v, boolean isSubAction) {
            super(v);
            this.mEditingMode = 0;
            this.mDelegate = new AccessibilityDelegate() {
                public void onInitializeAccessibilityEvent(View host, AccessibilityEvent event) {
                    super.onInitializeAccessibilityEvent(host, event);
                    boolean z = ViewHolder.this.mAction != null && ViewHolder.this.mAction.isChecked();
                    event.setChecked(z);
                }

                public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
                    super.onInitializeAccessibilityNodeInfo(host, info);
                    boolean z = false;
                    boolean z2 = (ViewHolder.this.mAction == null || ViewHolder.this.mAction.getCheckSetId() == 0) ? false : true;
                    info.setCheckable(z2);
                    if (ViewHolder.this.mAction != null && ViewHolder.this.mAction.isChecked()) {
                        z = true;
                    }
                    info.setChecked(z);
                }
            };
            this.mContentView = v.findViewById(R.id.guidedactions_item_content);
            this.mTitleView = (TextView) v.findViewById(R.id.guidedactions_item_title);
            this.mActivatorView = v.findViewById(R.id.guidedactions_activator_item);
            this.mDescriptionView = (TextView) v.findViewById(R.id.guidedactions_item_description);
            this.mIconView = (ImageView) v.findViewById(R.id.guidedactions_item_icon);
            this.mCheckmarkView = (ImageView) v.findViewById(R.id.guidedactions_item_checkmark);
            this.mChevronView = (ImageView) v.findViewById(R.id.guidedactions_item_chevron);
            this.mIsSubAction = isSubAction;
            v.setAccessibilityDelegate(this.mDelegate);
        }

        public View getContentView() {
            return this.mContentView;
        }

        public TextView getTitleView() {
            return this.mTitleView;
        }

        public EditText getEditableTitleView() {
            return this.mTitleView instanceof EditText ? (EditText) this.mTitleView : null;
        }

        public TextView getDescriptionView() {
            return this.mDescriptionView;
        }

        public EditText getEditableDescriptionView() {
            return this.mDescriptionView instanceof EditText ? (EditText) this.mDescriptionView : null;
        }

        public ImageView getIconView() {
            return this.mIconView;
        }

        public ImageView getCheckmarkView() {
            return this.mCheckmarkView;
        }

        public ImageView getChevronView() {
            return this.mChevronView;
        }

        public boolean isInEditing() {
            return this.mEditingMode != 0;
        }

        public boolean isInEditingText() {
            return this.mEditingMode == 1 || this.mEditingMode == 2;
        }

        public boolean isInEditingTitle() {
            return this.mEditingMode == 1;
        }

        public boolean isInEditingDescription() {
            return this.mEditingMode == 2;
        }

        public boolean isInEditingActivatorView() {
            return this.mEditingMode == 3;
        }

        public View getEditingView() {
            switch (this.mEditingMode) {
                case 1:
                    return this.mTitleView;
                case 2:
                    return this.mDescriptionView;
                case 3:
                    return this.mActivatorView;
                default:
                    return null;
            }
        }

        public boolean isSubAction() {
            return this.mIsSubAction;
        }

        public GuidedAction getAction() {
            return this.mAction;
        }

        /* Access modifiers changed, original: 0000 */
        public void setActivated(boolean activated) {
            this.mActivatorView.setActivated(activated);
            if (this.itemView instanceof GuidedActionItemContainer) {
                ((GuidedActionItemContainer) this.itemView).setFocusOutAllowed(activated ^ 1);
            }
        }

        public Object getFacet(Class<?> facetClass) {
            if (facetClass == ItemAlignmentFacet.class) {
                return GuidedActionsStylist.sGuidedActionItemAlignFacet;
            }
            return null;
        }

        /* Access modifiers changed, original: 0000 */
        public void press(boolean pressed) {
            if (this.mPressAnimator != null) {
                this.mPressAnimator.cancel();
                this.mPressAnimator = null;
            }
            int themeAttrId = pressed ? R.attr.guidedActionPressedAnimation : R.attr.guidedActionUnpressedAnimation;
            Context ctx = this.itemView.getContext();
            TypedValue typedValue = new TypedValue();
            if (ctx.getTheme().resolveAttribute(themeAttrId, typedValue, true)) {
                this.mPressAnimator = AnimatorInflater.loadAnimator(ctx, typedValue.resourceId);
                this.mPressAnimator.setTarget(this.itemView);
                this.mPressAnimator.addListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animation) {
                        ViewHolder.this.mPressAnimator = null;
                    }
                });
                this.mPressAnimator.start();
            }
        }
    }

    static {
        ItemAlignmentDef alignedDef = new ItemAlignmentDef();
        alignedDef.setItemAlignmentViewId(R.id.guidedactions_item_title);
        alignedDef.setAlignedToTextViewBaseline(true);
        alignedDef.setItemAlignmentOffset(0);
        alignedDef.setItemAlignmentOffsetWithPadding(true);
        alignedDef.setItemAlignmentOffsetPercent(0.0f);
        sGuidedActionItemAlignFacet.setAlignmentDefs(new ItemAlignmentDef[]{alignedDef});
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container) {
        float keylinePercent = inflater.getContext().getTheme().obtainStyledAttributes(R.styleable.LeanbackGuidedStepTheme).getFloat(R.styleable.LeanbackGuidedStepTheme_guidedStepKeyline, 40.0f);
        this.mMainView = (ViewGroup) inflater.inflate(onProvideLayoutId(), container, false);
        this.mContentView = this.mMainView.findViewById(this.mButtonActions ? R.id.guidedactions_content2 : R.id.guidedactions_content);
        this.mBgView = this.mMainView.findViewById(this.mButtonActions ? R.id.guidedactions_list_background2 : R.id.guidedactions_list_background);
        if (this.mMainView instanceof VerticalGridView) {
            this.mActionsGridView = (VerticalGridView) this.mMainView;
        } else {
            this.mActionsGridView = (VerticalGridView) this.mMainView.findViewById(this.mButtonActions ? R.id.guidedactions_list2 : R.id.guidedactions_list);
            if (this.mActionsGridView != null) {
                this.mActionsGridView.setWindowAlignmentOffsetPercent(keylinePercent);
                this.mActionsGridView.setWindowAlignment(0);
                if (!this.mButtonActions) {
                    this.mSubActionsGridView = (VerticalGridView) this.mMainView.findViewById(R.id.guidedactions_sub_list);
                    this.mSubActionsBackground = this.mMainView.findViewById(R.id.guidedactions_sub_list_background);
                }
            } else {
                throw new IllegalStateException("No ListView exists.");
            }
        }
        this.mActionsGridView.setFocusable(false);
        this.mActionsGridView.setFocusableInTouchMode(false);
        Context ctx = this.mMainView.getContext();
        TypedValue val = new TypedValue();
        this.mEnabledChevronAlpha = getFloat(ctx, val, R.attr.guidedActionEnabledChevronAlpha);
        this.mDisabledChevronAlpha = getFloat(ctx, val, R.attr.guidedActionDisabledChevronAlpha);
        this.mTitleMinLines = getInteger(ctx, val, R.attr.guidedActionTitleMinLines);
        this.mTitleMaxLines = getInteger(ctx, val, R.attr.guidedActionTitleMaxLines);
        this.mDescriptionMinLines = getInteger(ctx, val, R.attr.guidedActionDescriptionMinLines);
        this.mVerticalPadding = getDimension(ctx, val, R.attr.guidedActionVerticalPadding);
        this.mDisplayHeight = ((WindowManager) ctx.getSystemService("window")).getDefaultDisplay().getHeight();
        this.mEnabledTextAlpha = Float.valueOf(ctx.getResources().getString(R.string.lb_guidedactions_item_unselected_text_alpha)).floatValue();
        this.mDisabledTextAlpha = Float.valueOf(ctx.getResources().getString(R.string.lb_guidedactions_item_disabled_text_alpha)).floatValue();
        this.mEnabledDescriptionAlpha = Float.valueOf(ctx.getResources().getString(R.string.lb_guidedactions_item_unselected_description_text_alpha)).floatValue();
        this.mDisabledDescriptionAlpha = Float.valueOf(ctx.getResources().getString(R.string.lb_guidedactions_item_disabled_description_text_alpha)).floatValue();
        this.mKeyLinePercent = GuidanceStylingRelativeLayout.getKeyLinePercent(ctx);
        if (this.mContentView instanceof GuidedActionsRelativeLayout) {
            ((GuidedActionsRelativeLayout) this.mContentView).setInterceptKeyEventListener(new InterceptKeyEventListener() {
                public boolean onInterceptKeyEvent(KeyEvent event) {
                    if (event.getKeyCode() != 4 || event.getAction() != 1 || GuidedActionsStylist.this.mExpandedAction == null || ((!GuidedActionsStylist.this.mExpandedAction.hasSubActions() || !GuidedActionsStylist.this.isBackKeyToCollapseSubActions()) && (!GuidedActionsStylist.this.mExpandedAction.hasEditableActivatorView() || !GuidedActionsStylist.this.isBackKeyToCollapseActivatorView()))) {
                        return false;
                    }
                    GuidedActionsStylist.this.collapseAction(true);
                    return true;
                }
            });
        }
        return this.mMainView;
    }

    public void setAsButtonActions() {
        if (this.mMainView == null) {
            this.mButtonActions = true;
            return;
        }
        throw new IllegalStateException("setAsButtonActions() must be called before creating views");
    }

    public boolean isButtonActions() {
        return this.mButtonActions;
    }

    public void onDestroyView() {
        this.mExpandedAction = null;
        this.mExpandTransition = null;
        this.mActionsGridView = null;
        this.mSubActionsGridView = null;
        this.mSubActionsBackground = null;
        this.mContentView = null;
        this.mBgView = null;
        this.mMainView = null;
    }

    public VerticalGridView getActionsGridView() {
        return this.mActionsGridView;
    }

    public VerticalGridView getSubActionsGridView() {
        return this.mSubActionsGridView;
    }

    public int onProvideLayoutId() {
        return this.mButtonActions ? R.layout.lb_guidedbuttonactions : R.layout.lb_guidedactions;
    }

    public int getItemViewType(GuidedAction action) {
        if (action instanceof GuidedDatePickerAction) {
            return 1;
        }
        return 0;
    }

    public int onProvideItemLayoutId() {
        return R.layout.lb_guidedactions_item;
    }

    public int onProvideItemLayoutId(int viewType) {
        if (viewType == 0) {
            return onProvideItemLayoutId();
        }
        if (viewType == 1) {
            return R.layout.lb_guidedactions_datepicker_item;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("ViewType ");
        stringBuilder.append(viewType);
        stringBuilder.append(" not supported in GuidedActionsStylist");
        throw new RuntimeException(stringBuilder.toString());
    }

    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        boolean z = false;
        View v = LayoutInflater.from(parent.getContext()).inflate(onProvideItemLayoutId(), parent, false);
        if (parent == this.mSubActionsGridView) {
            z = true;
        }
        return new ViewHolder(v, z);
    }

    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0) {
            return onCreateViewHolder(parent);
        }
        boolean z = false;
        View v = LayoutInflater.from(parent.getContext()).inflate(onProvideItemLayoutId(viewType), parent, false);
        if (parent == this.mSubActionsGridView) {
            z = true;
        }
        return new ViewHolder(v, z);
    }

    public void onBindViewHolder(ViewHolder vh, GuidedAction action) {
        vh.mAction = action;
        if (vh.mTitleView != null) {
            vh.mTitleView.setInputType(action.getInputType());
            vh.mTitleView.setText(action.getTitle());
            vh.mTitleView.setAlpha(action.isEnabled() ? this.mEnabledTextAlpha : this.mDisabledTextAlpha);
            vh.mTitleView.setFocusable(false);
            vh.mTitleView.setClickable(false);
            vh.mTitleView.setLongClickable(false);
            if (BuildCompat.isAtLeastP()) {
                if (action.isEditable()) {
                    vh.mTitleView.setAutofillHints(action.getAutofillHints());
                } else {
                    vh.mTitleView.setAutofillHints((String[]) null);
                }
            } else if (VERSION.SDK_INT >= 26) {
                vh.mTitleView.setImportantForAutofill(2);
            }
        }
        if (vh.mDescriptionView != null) {
            vh.mDescriptionView.setInputType(action.getDescriptionInputType());
            vh.mDescriptionView.setText(action.getDescription());
            vh.mDescriptionView.setVisibility(TextUtils.isEmpty(action.getDescription()) ? 8 : 0);
            vh.mDescriptionView.setAlpha(action.isEnabled() ? this.mEnabledDescriptionAlpha : this.mDisabledDescriptionAlpha);
            vh.mDescriptionView.setFocusable(false);
            vh.mDescriptionView.setClickable(false);
            vh.mDescriptionView.setLongClickable(false);
            if (BuildCompat.isAtLeastP()) {
                if (action.isDescriptionEditable()) {
                    vh.mDescriptionView.setAutofillHints(action.getAutofillHints());
                } else {
                    vh.mDescriptionView.setAutofillHints((String[]) null);
                }
            } else if (VERSION.SDK_INT >= 26) {
                vh.mTitleView.setImportantForAutofill(2);
            }
        }
        if (vh.mCheckmarkView != null) {
            onBindCheckMarkView(vh, action);
        }
        setIcon(vh.mIconView, action);
        if (!action.hasMultilineDescription()) {
            if (vh.mTitleView != null) {
                setMaxLines(vh.mTitleView, this.mTitleMinLines);
            }
            if (vh.mDescriptionView != null) {
                setMaxLines(vh.mDescriptionView, this.mDescriptionMinLines);
            }
        } else if (vh.mTitleView != null) {
            setMaxLines(vh.mTitleView, this.mTitleMaxLines);
            vh.mTitleView.setInputType(vh.mTitleView.getInputType() | 131072);
            if (vh.mDescriptionView != null) {
                vh.mDescriptionView.setInputType(vh.mDescriptionView.getInputType() | 131072);
                vh.mDescriptionView.setMaxHeight(getDescriptionMaxHeight(vh.itemView.getContext(), vh.mTitleView));
            }
        }
        if (vh.mActivatorView != null) {
            onBindActivatorView(vh, action);
        }
        setEditingMode(vh, false, false);
        if (action.isFocusable()) {
            vh.itemView.setFocusable(true);
            ((ViewGroup) vh.itemView).setDescendantFocusability(131072);
        } else {
            vh.itemView.setFocusable(false);
            ((ViewGroup) vh.itemView).setDescendantFocusability(393216);
        }
        setupImeOptions(vh, action);
        updateChevronAndVisibility(vh);
    }

    public void openInEditMode(GuidedAction action) {
        final GuidedActionAdapter guidedActionAdapter = (GuidedActionAdapter) getActionsGridView().getAdapter();
        int actionIndex = guidedActionAdapter.getActions().indexOf(action);
        if (actionIndex >= 0 && action.isEditable()) {
            getActionsGridView().setSelectedPosition(actionIndex, (ViewHolderTask) new ViewHolderTask() {
                public void run(android.support.v7.widget.RecyclerView.ViewHolder viewHolder) {
                    guidedActionAdapter.mGroup.openIme(guidedActionAdapter, (ViewHolder) viewHolder);
                }
            });
        }
    }

    private static void setMaxLines(TextView view, int maxLines) {
        if (maxLines == 1) {
            view.setSingleLine(true);
            return;
        }
        view.setSingleLine(false);
        view.setMaxLines(maxLines);
    }

    /* Access modifiers changed, original: protected */
    public void setupImeOptions(ViewHolder vh, GuidedAction action) {
        setupNextImeOptions(vh.getEditableTitleView());
        setupNextImeOptions(vh.getEditableDescriptionView());
    }

    private void setupNextImeOptions(EditText edit) {
        if (edit != null) {
            edit.setImeOptions(5);
        }
    }

    @Deprecated
    public void setEditingMode(ViewHolder vh, GuidedAction action, boolean editing) {
        if (editing != vh.isInEditing() && isInExpandTransition()) {
            onEditingModeChange(vh, action, editing);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void setEditingMode(ViewHolder vh, boolean editing) {
        setEditingMode(vh, editing, true);
    }

    /* Access modifiers changed, original: 0000 */
    public void setEditingMode(ViewHolder vh, boolean editing, boolean withTransition) {
        if (editing != vh.isInEditing() && !isInExpandTransition()) {
            onEditingModeChange(vh, editing, withTransition);
        }
    }

    /* Access modifiers changed, original: protected */
    @Deprecated
    public void onEditingModeChange(ViewHolder vh, GuidedAction action, boolean editing) {
    }

    /* Access modifiers changed, original: protected */
    @CallSuper
    public void onEditingModeChange(ViewHolder vh, boolean editing, boolean withTransition) {
        GuidedAction action = vh.getAction();
        TextView titleView = vh.getTitleView();
        TextView descriptionView = vh.getDescriptionView();
        if (editing) {
            CharSequence editTitle = action.getEditTitle();
            if (!(titleView == null || editTitle == null)) {
                titleView.setText(editTitle);
            }
            CharSequence editDescription = action.getEditDescription();
            if (!(descriptionView == null || editDescription == null)) {
                descriptionView.setText(editDescription);
            }
            if (action.isDescriptionEditable()) {
                if (descriptionView != null) {
                    descriptionView.setVisibility(0);
                    descriptionView.setInputType(action.getDescriptionEditInputType());
                }
                vh.mEditingMode = 2;
            } else if (action.isEditable()) {
                if (titleView != null) {
                    titleView.setInputType(action.getEditInputType());
                }
                vh.mEditingMode = 1;
            } else if (vh.mActivatorView != null) {
                onEditActivatorView(vh, editing, withTransition);
                vh.mEditingMode = 3;
            }
        } else {
            if (titleView != null) {
                titleView.setText(action.getTitle());
            }
            if (descriptionView != null) {
                descriptionView.setText(action.getDescription());
            }
            if (vh.mEditingMode == 2) {
                if (descriptionView != null) {
                    descriptionView.setVisibility(TextUtils.isEmpty(action.getDescription()) ? 8 : 0);
                    descriptionView.setInputType(action.getDescriptionInputType());
                }
            } else if (vh.mEditingMode == 1) {
                if (titleView != null) {
                    titleView.setInputType(action.getInputType());
                }
            } else if (vh.mEditingMode == 3 && vh.mActivatorView != null) {
                onEditActivatorView(vh, editing, withTransition);
            }
            vh.mEditingMode = 0;
        }
        onEditingModeChange(vh, action, editing);
    }

    public void onAnimateItemFocused(ViewHolder vh, boolean focused) {
    }

    public void onAnimateItemPressed(ViewHolder vh, boolean pressed) {
        vh.press(pressed);
    }

    public void onAnimateItemPressedCancelled(ViewHolder vh) {
        vh.press(false);
    }

    public void onAnimateItemChecked(ViewHolder vh, boolean checked) {
        if (vh.mCheckmarkView instanceof Checkable) {
            ((Checkable) vh.mCheckmarkView).setChecked(checked);
        }
    }

    public void onBindCheckMarkView(ViewHolder vh, GuidedAction action) {
        if (action.getCheckSetId() != 0) {
            vh.mCheckmarkView.setVisibility(0);
            int attrId = action.getCheckSetId() == -1 ? 16843290 : 16843289;
            Context context = vh.mCheckmarkView.getContext();
            Drawable drawable = null;
            TypedValue typedValue = new TypedValue();
            if (context.getTheme().resolveAttribute(attrId, typedValue, true)) {
                drawable = ContextCompat.getDrawable(context, typedValue.resourceId);
            }
            vh.mCheckmarkView.setImageDrawable(drawable);
            if (vh.mCheckmarkView instanceof Checkable) {
                ((Checkable) vh.mCheckmarkView).setChecked(action.isChecked());
                return;
            }
            return;
        }
        vh.mCheckmarkView.setVisibility(8);
    }

    public void onBindActivatorView(ViewHolder vh, GuidedAction action) {
        if (action instanceof GuidedDatePickerAction) {
            GuidedDatePickerAction dateAction = (GuidedDatePickerAction) action;
            DatePicker dateView = vh.mActivatorView;
            dateView.setDatePickerFormat(dateAction.getDatePickerFormat());
            if (dateAction.getMinDate() != Long.MIN_VALUE) {
                dateView.setMinDate(dateAction.getMinDate());
            }
            if (dateAction.getMaxDate() != Long.MAX_VALUE) {
                dateView.setMaxDate(dateAction.getMaxDate());
            }
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(dateAction.getDate());
            dateView.updateDate(c.get(1), c.get(2), c.get(5), false);
        }
    }

    public boolean onUpdateActivatorView(ViewHolder vh, GuidedAction action) {
        if (action instanceof GuidedDatePickerAction) {
            GuidedDatePickerAction dateAction = (GuidedDatePickerAction) action;
            DatePicker dateView = vh.mActivatorView;
            if (dateAction.getDate() != dateView.getDate()) {
                dateAction.setDate(dateView.getDate());
                return true;
            }
        }
        return false;
    }

    @RestrictTo({Scope.LIBRARY_GROUP})
    public void setEditListener(EditListener listener) {
        this.mEditListener = listener;
    }

    /* Access modifiers changed, original: 0000 */
    public void onEditActivatorView(final ViewHolder vh, boolean editing, boolean withTransition) {
        if (editing) {
            startExpanded(vh, withTransition);
            vh.itemView.setFocusable(false);
            vh.mActivatorView.requestFocus();
            vh.mActivatorView.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (!GuidedActionsStylist.this.isInExpandTransition()) {
                        ((GuidedActionAdapter) GuidedActionsStylist.this.getActionsGridView().getAdapter()).performOnActionClick(vh);
                    }
                }
            });
            return;
        }
        if (onUpdateActivatorView(vh, vh.getAction()) && this.mEditListener != null) {
            this.mEditListener.onGuidedActionEditedAndProceed(vh.getAction());
        }
        vh.itemView.setFocusable(true);
        vh.itemView.requestFocus();
        startExpanded(null, withTransition);
        vh.mActivatorView.setOnClickListener(null);
        vh.mActivatorView.setClickable(false);
    }

    public void onBindChevronView(ViewHolder vh, GuidedAction action) {
        boolean hasNext = action.hasNext();
        boolean hasSubActions = action.hasSubActions();
        if (hasNext || hasSubActions) {
            vh.mChevronView.setVisibility(0);
            vh.mChevronView.setAlpha(action.isEnabled() ? this.mEnabledChevronAlpha : this.mDisabledChevronAlpha);
            if (hasNext) {
                float r = (this.mMainView == null || this.mMainView.getLayoutDirection() != 1) ? 0.0f : 180.0f;
                vh.mChevronView.setRotation(r);
                return;
            } else if (action == this.mExpandedAction) {
                vh.mChevronView.setRotation(270.0f);
                return;
            } else {
                vh.mChevronView.setRotation(90.0f);
                return;
            }
        }
        vh.mChevronView.setVisibility(8);
    }

    @Deprecated
    public void setExpandedViewHolder(ViewHolder avh) {
        expandAction(avh == null ? null : avh.getAction(), isExpandTransitionSupported());
    }

    public boolean isInExpandTransition() {
        return this.mExpandTransition != null;
    }

    public boolean isExpandTransitionSupported() {
        return VERSION.SDK_INT >= 21;
    }

    @Deprecated
    public void startExpandedTransition(ViewHolder avh) {
        expandAction(avh == null ? null : avh.getAction(), isExpandTransitionSupported());
    }

    public final void setBackKeyToCollapseSubActions(boolean backToCollapse) {
        this.mBackToCollapseSubActions = backToCollapse;
    }

    public final boolean isBackKeyToCollapseSubActions() {
        return this.mBackToCollapseSubActions;
    }

    public final void setBackKeyToCollapseActivatorView(boolean backToCollapse) {
        this.mBackToCollapseActivatorView = backToCollapse;
    }

    public final boolean isBackKeyToCollapseActivatorView() {
        return this.mBackToCollapseActivatorView;
    }

    public void expandAction(GuidedAction action, boolean withTransition) {
        if (!isInExpandTransition() && this.mExpandedAction == null) {
            int actionPosition = ((GuidedActionAdapter) getActionsGridView().getAdapter()).indexOf(action);
            if (actionPosition >= 0) {
                boolean runTransition = isExpandTransitionSupported() && withTransition;
                if (runTransition) {
                    getActionsGridView().setSelectedPosition(actionPosition, (ViewHolderTask) new ViewHolderTask() {
                        public void run(android.support.v7.widget.RecyclerView.ViewHolder vh) {
                            ViewHolder avh = (ViewHolder) vh;
                            if (avh.getAction().hasEditableActivatorView()) {
                                GuidedActionsStylist.this.setEditingMode(avh, true, true);
                            } else {
                                GuidedActionsStylist.this.startExpanded(avh, true);
                            }
                        }
                    });
                } else {
                    getActionsGridView().setSelectedPosition(actionPosition, (ViewHolderTask) new ViewHolderTask() {
                        public void run(android.support.v7.widget.RecyclerView.ViewHolder vh) {
                            ViewHolder avh = (ViewHolder) vh;
                            if (avh.getAction().hasEditableActivatorView()) {
                                GuidedActionsStylist.this.setEditingMode(avh, true, false);
                            } else {
                                GuidedActionsStylist.this.onUpdateExpandedViewHolder(avh);
                            }
                        }
                    });
                    if (action.hasSubActions()) {
                        onUpdateSubActionsGridView(action, true);
                    }
                }
            }
        }
    }

    public void collapseAction(boolean withTransition) {
        if (!isInExpandTransition() && this.mExpandedAction != null) {
            boolean runTransition = isExpandTransitionSupported() && withTransition;
            int actionPosition = ((GuidedActionAdapter) getActionsGridView().getAdapter()).indexOf(this.mExpandedAction);
            if (actionPosition >= 0) {
                if (this.mExpandedAction.hasEditableActivatorView()) {
                    setEditingMode((ViewHolder) getActionsGridView().findViewHolderForPosition(actionPosition), false, runTransition);
                } else {
                    startExpanded(null, runTransition);
                }
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    public int getKeyLine() {
        return (int) ((this.mKeyLinePercent * ((float) this.mActionsGridView.getHeight())) / 100.0f);
    }

    /* Access modifiers changed, original: 0000 */
    public void startExpanded(ViewHolder avh, boolean withTransition) {
        ViewHolder viewHolder = avh;
        ViewHolder focusAvh = null;
        int count = this.mActionsGridView.getChildCount();
        int i = 0;
        while (i < count) {
            ViewHolder vh = (ViewHolder) this.mActionsGridView.getChildViewHolder(this.mActionsGridView.getChildAt(i));
            if (viewHolder != null || vh.itemView.getVisibility() != 0) {
                if (viewHolder != null && vh.getAction() == avh.getAction()) {
                    focusAvh = vh;
                    break;
                }
                i++;
            } else {
                focusAvh = vh;
                break;
            }
        }
        if (focusAvh != null) {
            boolean isExpand = viewHolder != null;
            boolean isSubActionTransition = focusAvh.getAction().hasSubActions();
            if (withTransition) {
                float slideDistance;
                Object set = TransitionHelper.createTransitionSet(false);
                if (isSubActionTransition) {
                    slideDistance = (float) focusAvh.itemView.getHeight();
                } else {
                    slideDistance = ((float) focusAvh.itemView.getHeight()) * 0.5f;
                }
                Object slideAndFade = TransitionHelper.createFadeAndShortSlide(112, slideDistance);
                TransitionHelper.setEpicenterCallback(slideAndFade, new TransitionEpicenterCallback() {
                    Rect mRect = new Rect();

                    public Rect onGetEpicenter(Object transition) {
                        int centerY = GuidedActionsStylist.this.getKeyLine();
                        this.mRect.set(0, centerY, 0, centerY);
                        return this.mRect;
                    }
                });
                Object changeFocusItemTransform = TransitionHelper.createChangeTransform();
                Object changeFocusItemBounds = TransitionHelper.createChangeBounds(false);
                Object fade = TransitionHelper.createFadeTransition(3);
                Object changeGridBounds = TransitionHelper.createChangeBounds(false);
                if (viewHolder == null) {
                    TransitionHelper.setStartDelay(slideAndFade, 150);
                    TransitionHelper.setStartDelay(changeFocusItemTransform, 100);
                    TransitionHelper.setStartDelay(changeFocusItemBounds, 100);
                    TransitionHelper.setStartDelay(changeGridBounds, 100);
                } else {
                    TransitionHelper.setStartDelay(fade, 100);
                    TransitionHelper.setStartDelay(changeGridBounds, 50);
                    TransitionHelper.setStartDelay(changeFocusItemTransform, 50);
                    TransitionHelper.setStartDelay(changeFocusItemBounds, 50);
                }
                int i2 = 0;
                while (true) {
                    int i3 = i2;
                    if (i3 >= count) {
                        break;
                    }
                    int count2 = count;
                    ViewHolder count3 = (ViewHolder) this.mActionsGridView.getChildViewHolder(this.mActionsGridView.getChildAt(i3));
                    if (count3 != focusAvh) {
                        TransitionHelper.include(slideAndFade, count3.itemView);
                        ViewHolder viewHolder2 = count3;
                        TransitionHelper.exclude(fade, count3.itemView, (boolean) 1);
                    } else if (isSubActionTransition) {
                        TransitionHelper.include(changeFocusItemTransform, count3.itemView);
                        TransitionHelper.include(changeFocusItemBounds, count3.itemView);
                    }
                    i2 = i3 + 1;
                    count = count2;
                }
                TransitionHelper.include(changeGridBounds, this.mSubActionsGridView);
                TransitionHelper.include(changeGridBounds, this.mSubActionsBackground);
                TransitionHelper.addTransition(set, slideAndFade);
                if (isSubActionTransition) {
                    TransitionHelper.addTransition(set, changeFocusItemTransform);
                    TransitionHelper.addTransition(set, changeFocusItemBounds);
                }
                TransitionHelper.addTransition(set, fade);
                TransitionHelper.addTransition(set, changeGridBounds);
                this.mExpandTransition = set;
                TransitionHelper.addTransitionListener(this.mExpandTransition, new TransitionListener() {
                    public void onTransitionEnd(Object transition) {
                        GuidedActionsStylist.this.mExpandTransition = null;
                    }
                });
                if (isExpand && isSubActionTransition) {
                    count = viewHolder.itemView.getBottom();
                    this.mSubActionsGridView.offsetTopAndBottom(count - this.mSubActionsGridView.getTop());
                    this.mSubActionsBackground.offsetTopAndBottom(count - this.mSubActionsBackground.getTop());
                }
                TransitionHelper.beginDelayedTransition(this.mMainView, this.mExpandTransition);
            }
            onUpdateExpandedViewHolder(avh);
            if (isSubActionTransition) {
                onUpdateSubActionsGridView(focusAvh.getAction(), isExpand);
            }
        }
    }

    public boolean isSubActionsExpanded() {
        return this.mExpandedAction != null && this.mExpandedAction.hasSubActions();
    }

    public boolean isExpanded() {
        return this.mExpandedAction != null;
    }

    public GuidedAction getExpandedAction() {
        return this.mExpandedAction;
    }

    public void onUpdateExpandedViewHolder(ViewHolder avh) {
        int i = 0;
        if (avh == null) {
            this.mExpandedAction = null;
            this.mActionsGridView.setPruneChild(true);
        } else if (avh.getAction() != this.mExpandedAction) {
            this.mExpandedAction = avh.getAction();
            this.mActionsGridView.setPruneChild(false);
        }
        this.mActionsGridView.setAnimateChildLayout(false);
        int count = this.mActionsGridView.getChildCount();
        while (i < count) {
            updateChevronAndVisibility((ViewHolder) this.mActionsGridView.getChildViewHolder(this.mActionsGridView.getChildAt(i)));
            i++;
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void onUpdateSubActionsGridView(GuidedAction action, boolean expand) {
        if (this.mSubActionsGridView != null) {
            MarginLayoutParams lp = (MarginLayoutParams) this.mSubActionsGridView.getLayoutParams();
            GuidedActionAdapter adapter = (GuidedActionAdapter) this.mSubActionsGridView.getAdapter();
            if (expand) {
                lp.topMargin = -2;
                lp.height = -1;
                this.mSubActionsGridView.setLayoutParams(lp);
                this.mSubActionsGridView.setVisibility(0);
                this.mSubActionsBackground.setVisibility(0);
                this.mSubActionsGridView.requestFocus();
                adapter.setActions(action.getSubActions());
                return;
            }
            lp.topMargin = this.mActionsGridView.getLayoutManager().findViewByPosition(((GuidedActionAdapter) this.mActionsGridView.getAdapter()).indexOf(action)).getBottom();
            lp.height = 0;
            this.mSubActionsGridView.setVisibility(4);
            this.mSubActionsBackground.setVisibility(4);
            this.mSubActionsGridView.setLayoutParams(lp);
            adapter.setActions(Collections.EMPTY_LIST);
            this.mActionsGridView.requestFocus();
        }
    }

    private void updateChevronAndVisibility(ViewHolder vh) {
        if (!vh.isSubAction()) {
            if (this.mExpandedAction == null) {
                vh.itemView.setVisibility(0);
                vh.itemView.setTranslationY(0.0f);
                if (vh.mActivatorView != null) {
                    vh.setActivated(false);
                }
            } else if (vh.getAction() == this.mExpandedAction) {
                vh.itemView.setVisibility(0);
                if (vh.getAction().hasSubActions()) {
                    vh.itemView.setTranslationY((float) (getKeyLine() - vh.itemView.getBottom()));
                } else if (vh.mActivatorView != null) {
                    vh.itemView.setTranslationY(0.0f);
                    vh.setActivated(true);
                }
            } else {
                vh.itemView.setVisibility(4);
                vh.itemView.setTranslationY(0.0f);
            }
        }
        if (vh.mChevronView != null) {
            onBindChevronView(vh, vh.getAction());
        }
    }

    public void onImeAppearing(@NonNull List<Animator> list) {
    }

    public void onImeDisappearing(@NonNull List<Animator> list) {
    }

    private float getFloat(Context ctx, TypedValue typedValue, int attrId) {
        ctx.getTheme().resolveAttribute(attrId, typedValue, true);
        return Float.valueOf(ctx.getResources().getString(typedValue.resourceId)).floatValue();
    }

    private int getInteger(Context ctx, TypedValue typedValue, int attrId) {
        ctx.getTheme().resolveAttribute(attrId, typedValue, true);
        return ctx.getResources().getInteger(typedValue.resourceId);
    }

    private int getDimension(Context ctx, TypedValue typedValue, int attrId) {
        ctx.getTheme().resolveAttribute(attrId, typedValue, true);
        return ctx.getResources().getDimensionPixelSize(typedValue.resourceId);
    }

    private boolean setIcon(ImageView iconView, GuidedAction action) {
        Drawable icon = null;
        if (iconView != null) {
            icon = action.getIcon();
            if (icon != null) {
                iconView.setImageLevel(icon.getLevel());
                iconView.setImageDrawable(icon);
                iconView.setVisibility(0);
            } else {
                iconView.setVisibility(8);
            }
        }
        if (icon != null) {
            return true;
        }
        return false;
    }

    private int getDescriptionMaxHeight(Context context, TextView title) {
        return (this.mDisplayHeight - (this.mVerticalPadding * 2)) - ((2 * this.mTitleMaxLines) * title.getLineHeight());
    }
}
