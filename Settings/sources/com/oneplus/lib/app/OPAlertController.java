package com.oneplus.lib.app;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.v4.provider.FontsContractCompat.FontRequestCallback;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnApplyWindowInsetsListener;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowInsets;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.CursorAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import com.oneplus.commonctrl.R;
import java.lang.ref.WeakReference;

public class OPAlertController {
    private ListAdapter mAdapter;
    private int mAlertDialogLayout;
    private final OnClickListener mButtonHandler = new OnClickListener() {
        public void onClick(View v) {
            Message m;
            if (v == OPAlertController.this.mButtonPositive && OPAlertController.this.mButtonPositiveMessage != null) {
                m = Message.obtain(OPAlertController.this.mButtonPositiveMessage);
            } else if (v == OPAlertController.this.mButtonNegative && OPAlertController.this.mButtonNegativeMessage != null) {
                m = Message.obtain(OPAlertController.this.mButtonNegativeMessage);
            } else if (v != OPAlertController.this.mButtonNeutral || OPAlertController.this.mButtonNeutralMessage == null) {
                m = null;
            } else {
                m = Message.obtain(OPAlertController.this.mButtonNeutralMessage);
            }
            if (m != null) {
                m.sendToTarget();
            }
            OPAlertController.this.mHandler.obtainMessage(1, OPAlertController.this.mDialogInterface).sendToTarget();
        }
    };
    private Button mButtonNegative;
    private Message mButtonNegativeMessage;
    private CharSequence mButtonNegativeText;
    private Button mButtonNeutral;
    private Message mButtonNeutralMessage;
    private CharSequence mButtonNeutralText;
    private Button mButtonPositive;
    private Message mButtonPositiveMessage;
    private CharSequence mButtonPositiveText;
    private int mCheckedItem = -1;
    private final Context mContext;
    private View mCustomTitleView;
    private final DialogInterface mDialogInterface;
    private boolean mForceInverseBackground;
    private Handler mHandler;
    private Drawable mIcon;
    private int mIconId = 0;
    private ImageView mIconView;
    private int mListItemLayout;
    private int mListLayout;
    private ListView mListView;
    private CharSequence mMessage;
    private TextView mMessageView;
    private int mMultiChoiceItemLayout;
    private boolean mOnlyDarkTheme;
    private boolean mOnlyLightTheme;
    private int mProgressStyle = -1;
    private ScrollView mScrollView;
    private int mSingleChoiceItemLayout;
    private View mSpaceView;
    private CharSequence mTitle;
    private TextView mTitleView;
    private LinearLayout mTitle_template;
    private View mView;
    private int mViewLayoutResId;
    private int mViewSpacingBottom;
    private int mViewSpacingLeft;
    private int mViewSpacingRight;
    private boolean mViewSpacingSpecified = false;
    private int mViewSpacingTop;
    private final Window mWindow;

    public static class AlertParams {
        public ListAdapter mAdapter;
        public boolean mCancelable;
        public int mCheckedItem = -1;
        public boolean[] mCheckedItems;
        public final Context mContext;
        public Cursor mCursor;
        public View mCustomTitleView;
        public boolean mForceInverseBackground;
        public Drawable mIcon;
        public int mIconAttrId = 0;
        public int mIconId = 0;
        public final LayoutInflater mInflater;
        public String mIsCheckedColumn;
        public boolean mIsMultiChoice;
        public boolean mIsSingleChoice;
        public CharSequence[] mItems;
        public String mLabelColumn;
        public CharSequence mMessage;
        public DialogInterface.OnClickListener mNegativeButtonListener;
        public CharSequence mNegativeButtonText;
        public DialogInterface.OnClickListener mNeutralButtonListener;
        public CharSequence mNeutralButtonText;
        public OnCancelListener mOnCancelListener;
        public OnMultiChoiceClickListener mOnCheckboxClickListener;
        public DialogInterface.OnClickListener mOnClickListener;
        public OnDismissListener mOnDismissListener;
        public OnItemSelectedListener mOnItemSelectedListener;
        public OnKeyListener mOnKeyListener;
        public OnPrepareListViewListener mOnPrepareListViewListener;
        public boolean mOnlyDarkTheme = false;
        public boolean mOnlyLightTheme = false;
        public DialogInterface.OnClickListener mPositiveButtonListener;
        public CharSequence mPositiveButtonText;
        public boolean mRecycleOnMeasure = true;
        public CharSequence mTitle;
        public View mView;
        public int mViewLayoutResId;
        public int mViewSpacingBottom;
        public int mViewSpacingLeft;
        public int mViewSpacingRight;
        public boolean mViewSpacingSpecified = false;
        public int mViewSpacingTop;

        public interface OnPrepareListViewListener {
            void onPrepareListView(ListView listView);
        }

        public AlertParams(Context context) {
            this.mContext = context;
            this.mCancelable = true;
            this.mInflater = (LayoutInflater) context.getSystemService("layout_inflater");
        }

        public void apply(OPAlertController dialog) {
            if (this.mCustomTitleView != null) {
                dialog.setCustomTitle(this.mCustomTitleView);
            } else {
                if (this.mTitle != null) {
                    dialog.setTitle(this.mTitle);
                }
                if (this.mIcon != null) {
                    dialog.setIcon(this.mIcon);
                }
                if (this.mIconId != 0) {
                    dialog.setIcon(this.mIconId);
                }
                if (this.mIconAttrId != 0) {
                    dialog.setIcon(dialog.getIconAttributeResId(this.mIconAttrId));
                }
            }
            dialog.setOnlyDarkTheme(this.mOnlyDarkTheme);
            dialog.setOnlyLightTheme(this.mOnlyLightTheme);
            if (this.mMessage != null) {
                dialog.setMessage(this.mMessage);
            }
            if (this.mPositiveButtonText != null) {
                dialog.setButton(-1, this.mPositiveButtonText, this.mPositiveButtonListener, null);
            }
            if (this.mNegativeButtonText != null) {
                dialog.setButton(-2, this.mNegativeButtonText, this.mNegativeButtonListener, null);
            }
            if (this.mNeutralButtonText != null) {
                dialog.setButton(-3, this.mNeutralButtonText, this.mNeutralButtonListener, null);
            }
            if (this.mForceInverseBackground) {
                dialog.setInverseBackgroundForced(true);
            }
            if (!(this.mItems == null && this.mCursor == null && this.mAdapter == null)) {
                createListView(dialog);
            }
            if (this.mView != null) {
                if (this.mViewSpacingSpecified) {
                    dialog.setView(this.mView, this.mViewSpacingLeft, this.mViewSpacingTop, this.mViewSpacingRight, this.mViewSpacingBottom);
                    return;
                }
                dialog.setView(this.mView);
            } else if (this.mViewLayoutResId != 0) {
                dialog.setView(this.mViewLayoutResId);
            }
        }

        private void createListView(final OPAlertController dialog) {
            ListAdapter adapter;
            ListAdapter adapter2;
            final RecycleListView listView = (RecycleListView) this.mInflater.inflate(dialog.mListLayout, null);
            if (this.mIsMultiChoice) {
                if (this.mCursor == null) {
                    final RecycleListView recycleListView = listView;
                    adapter = new ArrayAdapter<CharSequence>(this.mContext, dialog.mMultiChoiceItemLayout, 16908308, this.mItems) {
                        public View getView(int position, View convertView, ViewGroup parent) {
                            View view = super.getView(position, convertView, parent);
                            if (AlertParams.this.mCheckedItems != null && AlertParams.this.mCheckedItems[position]) {
                                recycleListView.setItemChecked(position, true);
                            }
                            return view;
                        }
                    };
                } else {
                    final RecycleListView recycleListView2 = listView;
                    final OPAlertController oPAlertController = dialog;
                    Object adapter3 = new CursorAdapter(this.mContext, this.mCursor, false) {
                        private final int mIsCheckedIndex;
                        private final int mLabelIndex;

                        public void bindView(View view, Context context, Cursor cursor) {
                            ((CheckedTextView) view.findViewById(16908308)).setText(cursor.getString(this.mLabelIndex));
                            RecycleListView recycleListView = recycleListView2;
                            int position = cursor.getPosition();
                            boolean z = true;
                            if (cursor.getInt(this.mIsCheckedIndex) != 1) {
                                z = false;
                            }
                            recycleListView.setItemChecked(position, z);
                        }

                        public View newView(Context context, Cursor cursor, ViewGroup parent) {
                            return AlertParams.this.mInflater.inflate(oPAlertController.mMultiChoiceItemLayout, parent, false);
                        }
                    };
                }
                adapter2 = adapter3;
            } else {
                int layout;
                if (this.mIsSingleChoice) {
                    layout = dialog.mSingleChoiceItemLayout;
                } else {
                    layout = dialog.mListItemLayout;
                }
                adapter2 = this.mCursor != null ? new SimpleCursorAdapter(this.mContext, layout, this.mCursor, new String[]{this.mLabelColumn}, new int[]{16908308}) : this.mAdapter != null ? this.mAdapter : new CheckedItemAdapter(this.mContext, layout, 16908308, this.mItems);
            }
            adapter3 = adapter2;
            if (this.mOnPrepareListViewListener != null) {
                this.mOnPrepareListViewListener.onPrepareListView(listView);
            }
            dialog.mAdapter = adapter3;
            dialog.mCheckedItem = this.mCheckedItem;
            if (this.mOnClickListener != null) {
                listView.setOnItemClickListener(new OnItemClickListener() {
                    public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
                        AlertParams.this.mOnClickListener.onClick(dialog.mDialogInterface, position);
                        if (!AlertParams.this.mIsSingleChoice) {
                            dialog.mDialogInterface.dismiss();
                        }
                    }
                });
            } else if (this.mOnCheckboxClickListener != null) {
                listView.setOnItemClickListener(new OnItemClickListener() {
                    public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
                        if (AlertParams.this.mCheckedItems != null) {
                            AlertParams.this.mCheckedItems[position] = listView.isItemChecked(position);
                        }
                        AlertParams.this.mOnCheckboxClickListener.onClick(dialog.mDialogInterface, position, listView.isItemChecked(position));
                    }
                });
            }
            if (this.mOnItemSelectedListener != null) {
                listView.setOnItemSelectedListener(this.mOnItemSelectedListener);
            }
            if (this.mIsSingleChoice) {
                listView.setChoiceMode(1);
            } else if (this.mIsMultiChoice) {
                listView.setChoiceMode(2);
            }
            listView.mRecycleOnMeasure = this.mRecycleOnMeasure;
            dialog.mListView = listView;
        }
    }

    private static final class ButtonHandler extends Handler {
        private static final int MSG_DISMISS_DIALOG = 1;
        private WeakReference<DialogInterface> mDialog;

        public ButtonHandler(DialogInterface dialog) {
            this.mDialog = new WeakReference(dialog);
        }

        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i != 1) {
                switch (i) {
                    case FontRequestCallback.FAIL_REASON_FONT_LOAD_ERROR /*-3*/:
                    case -2:
                    case -1:
                        ((DialogInterface.OnClickListener) msg.obj).onClick((DialogInterface) this.mDialog.get(), msg.what);
                        return;
                    default:
                        return;
                }
            }
            ((DialogInterface) msg.obj).dismiss();
        }
    }

    private static class CheckedItemAdapter extends ArrayAdapter<CharSequence> {
        public CheckedItemAdapter(Context context, int resource, int textViewResourceId, CharSequence[] objects) {
            super(context, resource, textViewResourceId, objects);
        }

        public boolean hasStableIds() {
            return true;
        }

        public long getItemId(int position) {
            return (long) position;
        }
    }

    public static class RecycleListView extends ListView {
        boolean mRecycleOnMeasure = true;

        public RecycleListView(Context context) {
            super(context);
        }

        public RecycleListView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public RecycleListView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        public RecycleListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
        }

        /* Access modifiers changed, original: protected */
        public boolean recycleOnMeasure() {
            return this.mRecycleOnMeasure;
        }
    }

    private static boolean shouldCenterSingleButton(Context context) {
        return false;
    }

    public OPAlertController(Context context, DialogInterface di, Window window) {
        this.mContext = context;
        this.mDialogInterface = di;
        Log.i("OPAlertController", "OPAlertController start !!!");
        this.mWindow = window;
        this.mHandler = new ButtonHandler(di);
        TypedArray a = context.obtainStyledAttributes(null, R.styleable.OPAlertDialog, R.attr.OPAlertDialogStyle, 0);
        if (this.mDialogInterface instanceof OPProgressDialog) {
            this.mAlertDialogLayout = R.layout.op_alert_progress_dialog_material;
        } else {
            this.mAlertDialogLayout = a.getResourceId(R.styleable.OPAlertDialog_android_layout, R.layout.op_alert_dialog_material);
        }
        this.mListLayout = a.getResourceId(R.styleable.OPAlertDialog_op_listLayout, R.layout.op_select_dialog_material);
        this.mMultiChoiceItemLayout = a.getResourceId(R.styleable.OPAlertDialog_op_multiChoiceItemLayout, R.layout.op_select_dialog_multichoice_material);
        this.mSingleChoiceItemLayout = a.getResourceId(R.styleable.OPAlertDialog_op_singleChoiceItemLayout, R.layout.op_select_dialog_singlechoice_material);
        this.mListItemLayout = a.getResourceId(R.styleable.OPAlertDialog_op_listItemLayout, R.layout.op_select_dialog_item_material);
        a.recycle();
    }

    static boolean canTextInput(View v) {
        if (v.onCheckIsTextEditor()) {
            return true;
        }
        if (!(v instanceof ViewGroup)) {
            return false;
        }
        ViewGroup vg = (ViewGroup) v;
        int i = vg.getChildCount();
        while (i > 0) {
            i--;
            if (canTextInput(vg.getChildAt(i))) {
                return true;
            }
        }
        return false;
    }

    public void installContent() {
        this.mWindow.requestFeature(1);
        this.mWindow.setContentView(this.mAlertDialogLayout);
        setupView();
        setupDecor();
    }

    /* Access modifiers changed, original: protected */
    public void setProgressStyle(int style) {
        this.mProgressStyle = style;
    }

    public void setTitle(CharSequence title) {
        this.mTitle = title;
        if (this.mTitleView != null) {
            this.mTitleView.setText(title);
        }
        updateMessageView();
    }

    public void setCustomTitle(View customTitleView) {
        this.mCustomTitleView = customTitleView;
    }

    public void setMessage(CharSequence message) {
        this.mMessage = message;
        if (this.mMessageView != null) {
            this.mMessageView.setText(message);
        }
        updateMessageView();
    }

    public void setOnlyDarkTheme(boolean onlyDarkTheme) {
        this.mOnlyDarkTheme = onlyDarkTheme;
    }

    public void setOnlyLightTheme(boolean onlyLightTheme) {
        this.mOnlyLightTheme = onlyLightTheme;
    }

    private void updateTitleView() {
        if (!(this.mDialogInterface instanceof OPProgressDialog)) {
            updateMessageView();
            if (this.mTitleView != null) {
                boolean isBold = false;
                boolean hasMessage = TextUtils.isEmpty(this.mMessage) ^ 1;
                if (!(this.mIconId == 0 && this.mIcon == null && this.mListView == null && hasMessage)) {
                    isBold = true;
                }
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("isBold : ");
                stringBuilder.append(isBold);
                Log.i("OPAlertController", stringBuilder.toString());
                if (isBold) {
                    if (this.mDialogInterface instanceof OPProgressDialog) {
                        this.mTitleView.setTextColor(this.mContext.getResources().getColor(R.color.oneplus_contorl_text_color_primary_dark));
                    } else {
                        this.mTitleView.setTextAppearance(this.mContext, R.style.oneplus_contorl_text_style_title);
                    }
                } else if (this.mTitle_template != null) {
                    this.mTitle_template.setPadding(this.mTitle_template.getPaddingStart(), this.mTitle_template.getPaddingTop(), this.mTitle_template.getPaddingEnd(), this.mContext.getResources().getDimensionPixelSize(R.dimen.oneplus_contorl_layout_margin_bottom1));
                }
                if (this.mOnlyDarkTheme) {
                    this.mTitleView.setTextColor(this.mContext.getResources().getColor(R.color.oneplus_contorl_text_color_primary_dark));
                } else if (this.mOnlyLightTheme) {
                    this.mTitleView.setTextColor(this.mContext.getResources().getColor(R.color.oneplus_contorl_text_color_primary_light));
                }
                stringBuilder = new StringBuilder();
                stringBuilder.append("mTitleView.getTextSize() : ");
                stringBuilder.append(this.mTitleView.getTextSize());
                Log.i("OPAlertController", stringBuilder.toString());
            }
        }
    }

    private void updateMessageView() {
        if (!(this.mDialogInterface instanceof OPProgressDialog) && this.mMessageView != null) {
            int top;
            int bottom;
            if (TextUtils.isEmpty(this.mTitle) ^ 1) {
                top = this.mContext.getResources().getDimensionPixelSize(R.dimen.oneplus_contorl_layout_margin_top1);
                bottom = this.mContext.getResources().getDimensionPixelSize(R.dimen.oneplus_contorl_layout_margin_bottom1);
                if (this.mIconId == 0 && this.mIcon == null && this.mListView == null) {
                    bottom += this.mContext.getResources().getDimensionPixelSize(R.dimen.oneplus_contorl_layout_margin_top2);
                }
                this.mMessageView.setTextAppearance(this.mContext, R.style.oneplus_contorl_text_style_body1);
                if (this.mOnlyDarkTheme) {
                    this.mMessageView.setTextColor(this.mContext.getResources().getColor(R.color.oneplus_contorl_text_color_secondary_dark));
                } else if (this.mOnlyLightTheme) {
                    this.mMessageView.setTextColor(this.mContext.getResources().getColor(R.color.oneplus_contorl_text_color_secondary_light));
                } else {
                    this.mMessageView.setTextColor(this.mContext.getResources().getColor(R.color.oneplus_contorl_text_color_secondary_default));
                }
            } else {
                top = this.mContext.getResources().getDimensionPixelSize(R.dimen.oneplus_contorl_layout_margin_top3);
                bottom = this.mContext.getResources().getDimensionPixelSize(R.dimen.oneplus_contorl_layout_margin_bottom2);
                this.mMessageView.setTextAppearance(this.mContext, R.style.oneplus_contorl_text_style_subheading);
                if (this.mOnlyDarkTheme) {
                    this.mMessageView.setTextColor(this.mContext.getResources().getColor(R.color.oneplus_contorl_text_color_primary_dark));
                } else if (this.mOnlyLightTheme) {
                    this.mMessageView.setTextColor(this.mContext.getResources().getColor(R.color.oneplus_contorl_text_color_primary_light));
                } else {
                    this.mMessageView.setTextColor(this.mContext.getResources().getColor(R.color.oneplus_contorl_text_color_primary_default));
                }
            }
            this.mMessageView.setPadding(0, top, 0, bottom);
        }
    }

    public void setView(int layoutResId) {
        this.mView = null;
        this.mViewLayoutResId = layoutResId;
        this.mViewSpacingSpecified = false;
    }

    public void setView(View view) {
        this.mView = view;
        this.mViewLayoutResId = 0;
        this.mViewSpacingSpecified = false;
    }

    public void setView(View view, int viewSpacingLeft, int viewSpacingTop, int viewSpacingRight, int viewSpacingBottom) {
        this.mView = view;
        this.mViewLayoutResId = 0;
        this.mViewSpacingSpecified = true;
        this.mViewSpacingLeft = viewSpacingLeft;
        this.mViewSpacingTop = viewSpacingTop;
        this.mViewSpacingRight = viewSpacingRight;
        this.mViewSpacingBottom = viewSpacingBottom;
    }

    public void setButton(int whichButton, CharSequence text, DialogInterface.OnClickListener listener, Message msg) {
        if (msg == null && listener != null) {
            msg = this.mHandler.obtainMessage(whichButton, listener);
        }
        switch (whichButton) {
            case FontRequestCallback.FAIL_REASON_FONT_LOAD_ERROR /*-3*/:
                this.mButtonNeutralText = text;
                this.mButtonNeutralMessage = msg;
                return;
            case -2:
                this.mButtonNegativeText = text;
                this.mButtonNegativeMessage = msg;
                return;
            case -1:
                this.mButtonPositiveText = text;
                this.mButtonPositiveMessage = msg;
                return;
            default:
                throw new IllegalArgumentException("Button does not exist");
        }
    }

    public void setIcon(int resId) {
        this.mIcon = null;
        this.mIconId = resId;
        if (this.mIconView == null) {
            return;
        }
        if (resId != 0) {
            this.mIconView.setImageResource(this.mIconId);
        } else {
            this.mIconView.setVisibility(8);
        }
    }

    public void setIcon(Drawable icon) {
        this.mIcon = icon;
        this.mIconId = 0;
        if (this.mIconView == null) {
            return;
        }
        if (icon != null) {
            this.mIconView.setImageDrawable(icon);
        } else {
            this.mIconView.setVisibility(8);
        }
    }

    public int getIconAttributeResId(int attrId) {
        TypedValue out = new TypedValue();
        this.mContext.getTheme().resolveAttribute(attrId, out, true);
        return out.resourceId;
    }

    public void setInverseBackgroundForced(boolean forceInverseBackground) {
        this.mForceInverseBackground = forceInverseBackground;
    }

    public ListView getListView() {
        return this.mListView;
    }

    public Button getButton(int whichButton) {
        switch (whichButton) {
            case FontRequestCallback.FAIL_REASON_FONT_LOAD_ERROR /*-3*/:
                return this.mButtonNeutral;
            case -2:
                return this.mButtonNegative;
            case -1:
                return this.mButtonPositive;
            default:
                return null;
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return this.mScrollView != null && this.mScrollView.executeKeyEvent(event);
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return this.mScrollView != null && this.mScrollView.executeKeyEvent(event);
    }

    private void setupDecor() {
        View decor = this.mWindow.getDecorView();
        final View parent = this.mWindow.findViewById(R.id.parentPanel);
        if (parent != null && decor != null) {
            decor.setOnApplyWindowInsetsListener(new OnApplyWindowInsetsListener() {
                public WindowInsets onApplyWindowInsets(View view, WindowInsets insets) {
                    if (insets.isRound()) {
                        int roundOffset = OPAlertController.this.mContext.getResources().getDimensionPixelOffset(R.dimen.alert_dialog_round_padding);
                        parent.setPadding(roundOffset, roundOffset, roundOffset, roundOffset);
                    }
                    return insets.consumeSystemWindowInsets();
                }
            });
            decor.setFitsSystemWindows(true);
            decor.requestApplyInsets();
        }
    }

    private ViewGroup resolvePanel(View customPanel, View defaultPanel) {
        if (customPanel == null) {
            if (defaultPanel instanceof ViewStub) {
                defaultPanel = ((ViewStub) defaultPanel).inflate();
            }
            return (ViewGroup) defaultPanel;
        }
        if (defaultPanel != null) {
            ViewParent parent = defaultPanel.getParent();
            if (parent instanceof ViewGroup) {
                ((ViewGroup) parent).removeView(defaultPanel);
            }
        }
        if (customPanel instanceof ViewStub) {
            customPanel = ((ViewStub) customPanel).inflate();
        }
        return (ViewGroup) customPanel;
    }

    private void setupView() {
        View spacer;
        View parentPanel = this.mWindow.findViewById(R.id.parentPanel);
        View defaultTopPanel = parentPanel.findViewById(R.id.topPanel);
        View defaultContentPanel = parentPanel.findViewById(R.id.contentPanel);
        View defaultButtonPanel = parentPanel.findViewById(R.id.buttonPanel);
        View customPanel = (ViewGroup) parentPanel.findViewById(R.id.customPanel);
        setupCustomContent(customPanel);
        View customTopPanel = customPanel.findViewById(R.id.topPanel);
        View customContentPanel = customPanel.findViewById(R.id.contentPanel);
        View customButtonPanel = customPanel.findViewById(R.id.buttonPanel);
        View topPanel = resolvePanel(customTopPanel, defaultTopPanel);
        View contentPanel = resolvePanel(customContentPanel, defaultContentPanel);
        View buttonPanel = resolvePanel(customButtonPanel, defaultButtonPanel);
        setupContent(contentPanel);
        setupButtons(buttonPanel);
        setupTitle(topPanel);
        int indicators = 1;
        boolean z = (customPanel == null || customPanel.getVisibility() == 8) ? false : true;
        boolean hasCustomPanel = z;
        z = (topPanel == null || topPanel.getVisibility() == 8) ? false : true;
        boolean hasTopPanel = z;
        boolean z2 = (buttonPanel == null || buttonPanel.getVisibility() == 8) ? false : true;
        boolean hasButtonPanel = z2;
        if (!(hasButtonPanel || contentPanel == null)) {
            spacer = contentPanel.findViewById(R.id.textSpacerNoButtons);
            if (spacer != null && TextUtils.isEmpty(this.mTitle)) {
                spacer.setVisibility(0);
            }
        }
        if (hasTopPanel && this.mScrollView != null) {
            this.mScrollView.setClipToPadding(true);
        }
        if (!hasCustomPanel) {
            spacer = this.mListView != null ? this.mListView : this.mScrollView;
            if (spacer != null) {
                if (!hasTopPanel) {
                    indicators = 0;
                }
                spacer.setScrollIndicators(indicators | (hasButtonPanel ? 2 : 0), 3);
            }
        }
        View buttonPanel2 = buttonPanel;
        TypedArray a = this.mContext.obtainStyledAttributes(null, R.styleable.OPAlertDialog, 16842845, 0);
        TypedArray a2 = a;
        setBackground(a, topPanel, contentPanel, customPanel, buttonPanel2, hasTopPanel, hasCustomPanel, hasButtonPanel);
        a2.recycle();
    }

    private void setupCustomContent(ViewGroup customPanel) {
        View customView;
        boolean hasCustomView = false;
        if (this.mView != null) {
            customView = this.mView;
        } else if (this.mViewLayoutResId != 0) {
            customView = LayoutInflater.from(this.mContext).inflate(this.mViewLayoutResId, customPanel, false);
        } else {
            customView = null;
        }
        if (customView != null) {
            hasCustomView = true;
        }
        if (!(hasCustomView && canTextInput(customView))) {
            this.mWindow.setFlags(131072, 131072);
        }
        if (hasCustomView) {
            FrameLayout custom = (FrameLayout) this.mWindow.findViewById(16908331);
            custom.addView(customView, new LayoutParams(-1, -1));
            if (this.mViewSpacingSpecified) {
                custom.setPadding(this.mViewSpacingLeft, this.mViewSpacingTop, this.mViewSpacingRight, this.mViewSpacingBottom);
            }
            if (this.mListView != null) {
                ((LinearLayout.LayoutParams) customPanel.getLayoutParams()).weight = 0.0f;
                return;
            }
            return;
        }
        customPanel.setVisibility(8);
    }

    private void setupTitle(ViewGroup topPanel) {
        if (this.mCustomTitleView != null) {
            topPanel.addView(this.mCustomTitleView, 0, new LayoutParams(-1, -2));
            this.mWindow.findViewById(R.id.title_template).setVisibility(8);
            return;
        }
        this.mIconView = (ImageView) this.mWindow.findViewById(16908294);
        this.mTitle_template = (LinearLayout) this.mWindow.findViewById(R.id.title_template);
        if (TextUtils.isEmpty(this.mTitle) ^ 1) {
            this.mTitleView = (TextView) this.mWindow.findViewById(R.id.alertTitle);
            this.mTitleView.setText(this.mTitle);
            if (this.mIconId != 0) {
                this.mIconView.setImageResource(this.mIconId);
            } else if (this.mIcon != null) {
                this.mIconView.setImageDrawable(this.mIcon);
            } else {
                this.mTitleView.setPadding(this.mIconView.getPaddingLeft(), this.mIconView.getPaddingTop(), this.mIconView.getPaddingRight(), this.mIconView.getPaddingBottom());
                this.mIconView.setVisibility(8);
            }
        } else {
            this.mWindow.findViewById(R.id.title_template).setVisibility(8);
            this.mIconView.setVisibility(8);
            topPanel.setVisibility(8);
        }
        updateTitleView();
    }

    private void setupContent(ViewGroup contentPanel) {
        this.mScrollView = (ScrollView) contentPanel.findViewById(R.id.scrollView);
        this.mScrollView.setFocusable(false);
        this.mMessageView = (TextView) contentPanel.findViewById(16908299);
        if (this.mMessageView != null) {
            if (this.mMessage != null) {
                this.mMessageView.setText(this.mMessage);
            } else {
                this.mMessageView.setVisibility(8);
                this.mScrollView.removeView(this.mMessageView);
                if (this.mListView != null) {
                    ViewGroup scrollParent = (ViewGroup) this.mScrollView.getParent();
                    int childIndex = scrollParent.indexOfChild(this.mScrollView);
                    scrollParent.removeViewAt(childIndex);
                    scrollParent.addView(this.mListView, childIndex, new LayoutParams(-1, -1));
                } else {
                    contentPanel.setVisibility(8);
                }
            }
        }
    }

    private static void manageScrollIndicators(View v, View upIndicator, View downIndicator) {
        int i = 4;
        if (upIndicator != null) {
            upIndicator.setVisibility(v.canScrollVertically(-1) ? 0 : 4);
        }
        if (downIndicator != null) {
            if (v.canScrollVertically(1)) {
                i = 0;
            }
            downIndicator.setVisibility(i);
        }
    }

    private void setupButtons(ViewGroup buttonPanel) {
        int whichButtons = 0;
        if (this.mDialogInterface instanceof OPProgressDialog) {
            Log.i("ProgressDialog", "setupButtons mSpaceView set GONE");
            this.mSpaceView = buttonPanel.findViewById(R.id.spacer);
            if (this.mProgressStyle == 0 && this.mSpaceView != null) {
                this.mSpaceView.setVisibility(8);
                Log.i("ProgressDialog", "setupButtons mSpaceView set GONE");
            }
        }
        this.mButtonPositive = (Button) buttonPanel.findViewById(16908313);
        this.mButtonPositive.setOnClickListener(this.mButtonHandler);
        boolean z = false;
        if (TextUtils.isEmpty(this.mButtonPositiveText)) {
            this.mButtonPositive.setVisibility(8);
        } else {
            this.mButtonPositive.setText(this.mButtonPositiveText);
            this.mButtonPositive.setVisibility(0);
            whichButtons = 0 | 1;
        }
        this.mButtonNegative = (Button) buttonPanel.findViewById(16908314);
        this.mButtonNegative.setOnClickListener(this.mButtonHandler);
        if (TextUtils.isEmpty(this.mButtonNegativeText)) {
            this.mButtonNegative.setVisibility(8);
        } else {
            this.mButtonNegative.setText(this.mButtonNegativeText);
            this.mButtonNegative.setVisibility(0);
            whichButtons |= 2;
        }
        this.mButtonNeutral = (Button) buttonPanel.findViewById(16908315);
        this.mButtonNeutral.setOnClickListener(this.mButtonHandler);
        if (TextUtils.isEmpty(this.mButtonNeutralText)) {
            this.mButtonNeutral.setVisibility(8);
        } else {
            this.mButtonNeutral.setText(this.mButtonNeutralText);
            this.mButtonNeutral.setVisibility(0);
            whichButtons |= 4;
        }
        if (shouldCenterSingleButton(this.mContext)) {
            if (whichButtons == 1) {
                centerButton(this.mButtonPositive);
            } else if (whichButtons == 2) {
                centerButton(this.mButtonNegative);
            } else if (whichButtons == 4) {
                centerButton(this.mButtonNeutral);
            }
        }
        if (whichButtons != 0) {
            z = true;
        }
        if (!z) {
            buttonPanel.setVisibility(8);
        }
    }

    private void centerButton(Button button) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) button.getLayoutParams();
        params.gravity = 1;
        params.weight = 0.5f;
        button.setLayoutParams(params);
    }

    private void setBackground(TypedArray a, View topPanel, View contentPanel, View customPanel, View buttonPanel, boolean hasTitle, boolean hasCustomView, boolean hasButtons) {
        View[] views = new View[4];
        boolean[] light = new boolean[4];
        View lastView = null;
        boolean lastLight = false;
        int pos = 0;
        boolean z = false;
        if (hasTitle) {
            views[0] = topPanel;
            light[0] = false;
            pos = 0 + 1;
        }
        int fullDark = 0;
        int topDark = 0;
        views[pos] = contentPanel.getVisibility() == 8 ? null : contentPanel;
        if (this.mListView != null) {
            z = true;
        }
        light[pos] = z;
        pos++;
        if (hasCustomView) {
            views[pos] = customPanel;
            light[pos] = this.mForceInverseBackground;
            pos++;
        }
        if (hasButtons) {
            views[pos] = buttonPanel;
            light[pos] = true;
        }
        boolean setView = false;
        for (pos = 0; pos < views.length; pos++) {
            View v = views[pos];
            if (v != null) {
                if (lastView != null) {
                    if (setView) {
                        lastView.setBackgroundResource(lastLight ? 0 : 0);
                    } else {
                        lastView.setBackgroundResource(lastLight ? 0 : topDark);
                    }
                    setView = true;
                }
                View lastView2 = v;
                lastLight = light[pos];
                lastView = lastView2;
            }
        }
        if (lastView != null) {
            if (setView) {
                int i = lastLight ? hasButtons ? 0 : 0 : 0;
                lastView.setBackgroundResource(i);
            } else {
                lastView.setBackgroundResource(lastLight ? 0 : fullDark);
            }
        }
        ListView listView = this.mListView;
        if (listView != null && this.mAdapter != null) {
            listView.setAdapter(this.mAdapter);
            int checkedItem = this.mCheckedItem;
            if (checkedItem > -1) {
                listView.setItemChecked(checkedItem, true);
                listView.setSelection(checkedItem);
            }
        }
    }
}
