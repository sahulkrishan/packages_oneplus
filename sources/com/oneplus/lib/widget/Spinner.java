package com.oneplus.lib.widget;

import android.content.Context;
import android.content.res.Resources.Theme;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.SpinnerAdapter;
import android.widget.ThemedSpinnerAdapter;
import com.oneplus.lib.widget.util.ViewUtils;

public class Spinner extends android.widget.Spinner {
    private static final int[] ATTRS_ANDROID_SPINNERMODE = new int[]{16843505};
    private static final boolean IS_AT_LEAST_JB = (VERSION.SDK_INT >= 16);
    static final boolean IS_AT_LEAST_M = (VERSION.SDK_INT >= 23);
    private static final int MAX_ITEMS_MEASURED = 15;
    private static final int MODE_DIALOG = 0;
    private static final int MODE_DROPDOWN = 1;
    private static final int MODE_THEME = -1;
    private static final int SELECTED_INDEX_BOTTOM = 2;
    private static final int SELECTED_INDEX_MIDDLE = 1;
    private static final int SELECTED_INDEX_TOP = 0;
    private static final String TAG = "OpSpinner";
    private DropDownAdapter mDropDownAdapter;
    private int mDropDownWidth;
    private ForwardingListener mForwardingListener;
    private DropdownPopup mPopup;
    private final Context mPopupContext;
    private final boolean mPopupSet;
    private Drawable[] mSelectedItemBackground;
    private SpinnerAdapter mTempAdapter;
    private final Rect mTempRect;

    private static class DropDownAdapter implements ListAdapter, SpinnerAdapter {
        private SpinnerAdapter mAdapter;
        private int mLastSelectedPosition = -1;
        private ListAdapter mListAdapter;
        private Drawable[] mSelectedItemBackground;

        public DropDownAdapter(SpinnerAdapter adapter, Theme dropDownTheme) {
            this.mAdapter = adapter;
            if (adapter instanceof ListAdapter) {
                this.mListAdapter = (ListAdapter) adapter;
            }
            if (dropDownTheme == null) {
                return;
            }
            ThemedSpinnerAdapter themedAdapter;
            if (Spinner.IS_AT_LEAST_M && (adapter instanceof ThemedSpinnerAdapter)) {
                themedAdapter = (ThemedSpinnerAdapter) adapter;
                if (themedAdapter.getDropDownViewTheme() != dropDownTheme) {
                    themedAdapter.setDropDownViewTheme(dropDownTheme);
                }
            } else if (adapter instanceof ThemedSpinnerAdapter) {
                themedAdapter = (ThemedSpinnerAdapter) adapter;
                if (VERSION.SDK_INT >= 23 && themedAdapter.getDropDownViewTheme() == null) {
                    themedAdapter.setDropDownViewTheme(dropDownTheme);
                }
            }
        }

        public void setSelectedItemBackground(Drawable[] drawables) {
            this.mSelectedItemBackground = drawables;
        }

        public int getCount() {
            return this.mAdapter == null ? 0 : this.mAdapter.getCount();
        }

        public Object getItem(int position) {
            return this.mAdapter == null ? null : this.mAdapter.getItem(position);
        }

        public long getItemId(int position) {
            return this.mAdapter == null ? -1 : this.mAdapter.getItemId(position);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            return getDropDownView(position, convertView, parent);
        }

        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            if (this.mAdapter == null) {
                return null;
            }
            View dropDownView = this.mAdapter.getDropDownView(position, convertView, parent);
            if (position != this.mLastSelectedPosition) {
                dropDownView.setBackground(null);
            } else if (position == 0) {
                dropDownView.setBackground(this.mSelectedItemBackground[0]);
            } else if (position == getCount() - 1) {
                dropDownView.setBackground(this.mSelectedItemBackground[2]);
            } else {
                dropDownView.setBackground(this.mSelectedItemBackground[1]);
            }
            return dropDownView;
        }

        public boolean hasStableIds() {
            return this.mAdapter != null && this.mAdapter.hasStableIds();
        }

        public void setSelectedItem(int selectedItem) {
            this.mLastSelectedPosition = selectedItem;
        }

        public void registerDataSetObserver(DataSetObserver observer) {
            if (this.mAdapter != null) {
                this.mAdapter.registerDataSetObserver(observer);
            }
        }

        public void unregisterDataSetObserver(DataSetObserver observer) {
            if (this.mAdapter != null) {
                this.mAdapter.unregisterDataSetObserver(observer);
            }
        }

        public boolean areAllItemsEnabled() {
            ListAdapter adapter = this.mListAdapter;
            if (adapter != null) {
                return adapter.areAllItemsEnabled();
            }
            return true;
        }

        public boolean isEnabled(int position) {
            ListAdapter adapter = this.mListAdapter;
            if (adapter != null) {
                return adapter.isEnabled(position);
            }
            return true;
        }

        public int getItemViewType(int position) {
            return 0;
        }

        public int getViewTypeCount() {
            return 1;
        }

        public boolean isEmpty() {
            return getCount() == 0;
        }
    }

    private class DropdownPopup extends ListPopupWindow {
        ListAdapter mAdapter;
        private CharSequence mHintText;
        private final Rect mVisibleRect = new Rect();

        public DropdownPopup(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            setAnchorView(Spinner.this);
            setModal(true);
            setPromptPosition(0);
            setOnItemClickListener(new OnItemClickListener(Spinner.this) {
                public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
                    Spinner.this.setSelection(position);
                    if (Spinner.this.getOnItemClickListener() != null) {
                        Spinner.this.performItemClick(v, position, DropdownPopup.this.mAdapter.getItemId(position));
                    }
                    DropdownPopup.this.dismiss();
                }
            });
        }

        public void setAdapter(ListAdapter adapter) {
            super.setAdapter(adapter);
            this.mAdapter = adapter;
        }

        /* Access modifiers changed, original: protected */
        public boolean needInterceptorTouchEvent() {
            return false;
        }

        public CharSequence getHintText() {
            return this.mHintText;
        }

        public void setPromptText(CharSequence hintText) {
            this.mHintText = hintText;
        }

        /* Access modifiers changed, original: 0000 */
        public void computeContentWidth() {
            int i;
            Drawable background = getBackground();
            int hOffset = 0;
            if (background != null) {
                background.getPadding(Spinner.this.mTempRect);
                if (ViewUtils.isLayoutRtl(Spinner.this)) {
                    i = Spinner.this.mTempRect.right;
                } else {
                    i = -Spinner.this.mTempRect.left;
                }
                hOffset = i;
            } else {
                Rect access$100 = Spinner.this.mTempRect;
                Spinner.this.mTempRect.right = 0;
                access$100.left = 0;
            }
            i = Spinner.this.getPaddingLeft();
            int spinnerPaddingRight = Spinner.this.getPaddingRight();
            int spinnerWidth = Spinner.this.getWidth();
            if (Spinner.this.mDropDownWidth == -2) {
                int contentWidth = Spinner.this.compatMeasureContentWidth((SpinnerAdapter) this.mAdapter, getBackground());
                int contentWidthLimit = (Spinner.this.getContext().getResources().getDisplayMetrics().widthPixels - Spinner.this.mTempRect.left) - Spinner.this.mTempRect.right;
                if (contentWidth > contentWidthLimit) {
                    contentWidth = contentWidthLimit;
                }
                setContentWidth(Math.max(contentWidth, (spinnerWidth - i) - spinnerPaddingRight));
            } else if (Spinner.this.mDropDownWidth == -1) {
                setContentWidth((spinnerWidth - i) - spinnerPaddingRight);
            } else {
                setContentWidth(Spinner.this.mDropDownWidth);
            }
            if (ViewUtils.isLayoutRtl(Spinner.this)) {
                hOffset += (spinnerWidth - spinnerPaddingRight) - getWidth();
            } else {
                hOffset += i;
            }
            setHorizontalOffset(hOffset);
        }

        public void show() {
            boolean wasShowing = isShowing();
            computeContentWidth();
            setInputMethodMode(2);
            super.show();
            getListView().setChoiceMode(1);
            setSelection(Spinner.this.getSelectedItemPosition());
            if (Spinner.this.mDropDownAdapter != null) {
                Spinner.this.mDropDownAdapter.setSelectedItem(Spinner.this.getSelectedItemPosition());
            }
            if (!wasShowing) {
                ViewTreeObserver vto = Spinner.this.getViewTreeObserver();
                if (vto != null) {
                    final OnGlobalLayoutListener layoutListener = new OnGlobalLayoutListener() {
                        public void onGlobalLayout() {
                            if (DropdownPopup.this.isVisibleToUser(Spinner.this)) {
                                DropdownPopup.this.computeContentWidth();
                                super.show();
                                return;
                            }
                            DropdownPopup.this.dismiss();
                        }
                    };
                    vto.addOnGlobalLayoutListener(layoutListener);
                    setOnDismissListener(new OnDismissListener() {
                        public void onDismiss() {
                            ViewTreeObserver vto = Spinner.this.getViewTreeObserver();
                            if (vto != null) {
                                vto.removeGlobalOnLayoutListener(layoutListener);
                            }
                        }
                    });
                }
            }
        }

        /* Access modifiers changed, original: 0000 */
        public boolean isVisibleToUser(View view) {
            return view.isAttachedToWindow() && view.getGlobalVisibleRect(this.mVisibleRect);
        }
    }

    public Spinner(Context context) {
        this(context, null);
    }

    public Spinner(Context context, int mode) {
        this(context, null, 16842881, mode);
    }

    public Spinner(Context context, AttributeSet attrs) {
        this(context, attrs, 16842881);
    }

    public Spinner(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, -1);
    }

    public Spinner(Context context, AttributeSet attrs, int defStyleAttr, int mode) {
        this(context, attrs, defStyleAttr, mode, null);
    }

    /* JADX WARNING: Missing block: B:21:0x009f, code skipped:
            if (r3 != null) goto L_0x00a1;
     */
    /* JADX WARNING: Missing block: B:22:0x00a1, code skipped:
            r3.recycle();
     */
    /* JADX WARNING: Missing block: B:27:0x00af, code skipped:
            if (r3 == null) goto L_0x00ba;
     */
    public Spinner(android.content.Context r10, android.util.AttributeSet r11, int r12, int r13, android.content.res.Resources.Theme r14) {
        /*
        r9 = this;
        r9.<init>(r10, r11, r12);
        r0 = 3;
        r0 = new android.graphics.drawable.Drawable[r0];
        r9.mSelectedItemBackground = r0;
        r0 = new android.graphics.Rect;
        r0.<init>();
        r9.mTempRect = r0;
        r0 = com.oneplus.commonctrl.R.styleable.Spinner;
        r1 = 0;
        r0 = r10.obtainStyledAttributes(r11, r0, r12, r1);
        r2 = com.oneplus.commonctrl.R.styleable.Spinner_android_popupTheme;
        r2 = r0.getResourceId(r2, r1);
        if (r14 == 0) goto L_0x0034;
    L_0x001e:
        r3 = android.os.Build.VERSION.SDK_INT;
        r4 = 23;
        if (r3 < r4) goto L_0x002c;
    L_0x0024:
        r3 = new android.view.ContextThemeWrapper;
        r3.<init>(r10, r14);
        r9.mPopupContext = r3;
        goto L_0x0040;
    L_0x002c:
        r3 = new android.view.ContextThemeWrapper;
        r3.<init>(r10, r2);
        r9.mPopupContext = r3;
        goto L_0x0040;
    L_0x0034:
        if (r2 == 0) goto L_0x003e;
    L_0x0036:
        r3 = new android.view.ContextThemeWrapper;
        r3.<init>(r10, r2);
        r9.mPopupContext = r3;
        goto L_0x0040;
    L_0x003e:
        r9.mPopupContext = r10;
    L_0x0040:
        r3 = r9.mSelectedItemBackground;
        r4 = r9.getResources();
        r5 = com.oneplus.commonctrl.R.drawable.op_drop_down_item_background_top;
        r6 = r9.mPopupContext;
        r6 = r6.getTheme();
        r4 = r4.getDrawable(r5, r6);
        r3[r1] = r4;
        r3 = r9.mSelectedItemBackground;
        r4 = 2;
        r5 = r9.getResources();
        r6 = com.oneplus.commonctrl.R.drawable.op_drop_down_item_background_bottom;
        r7 = r9.mPopupContext;
        r7 = r7.getTheme();
        r5 = r5.getDrawable(r6, r7);
        r3[r4] = r5;
        r3 = r9.mSelectedItemBackground;
        r4 = r9.getResources();
        r5 = com.oneplus.commonctrl.R.drawable.op_drop_down_item_background;
        r6 = r9.mPopupContext;
        r6 = r6.getTheme();
        r4 = r4.getDrawable(r5, r6);
        r5 = 1;
        r3[r5] = r4;
        r3 = r9.mPopupContext;
        r4 = 0;
        if (r3 == 0) goto L_0x00f2;
    L_0x0083:
        r3 = -1;
        if (r13 != r3) goto L_0x00ba;
    L_0x0086:
        r3 = android.os.Build.VERSION.SDK_INT;
        r6 = 11;
        if (r3 < r6) goto L_0x00b9;
    L_0x008c:
        r3 = r4;
        r6 = ATTRS_ANDROID_SPINNERMODE;	 Catch:{ Exception -> 0x00a7 }
        r6 = r10.obtainStyledAttributes(r11, r6, r12, r1);	 Catch:{ Exception -> 0x00a7 }
        r3 = r6;
        r6 = r3.hasValue(r1);	 Catch:{ Exception -> 0x00a7 }
        if (r6 == 0) goto L_0x009f;
    L_0x009a:
        r6 = r3.getInt(r1, r1);	 Catch:{ Exception -> 0x00a7 }
        r13 = r6;
    L_0x009f:
        if (r3 == 0) goto L_0x00b2;
    L_0x00a1:
        r3.recycle();
        goto L_0x00b2;
    L_0x00a5:
        r1 = move-exception;
        goto L_0x00b3;
    L_0x00a7:
        r6 = move-exception;
        r7 = "OpSpinner";
        r8 = "Could not read android:spinnerMode";
        android.util.Log.i(r7, r8, r6);	 Catch:{ all -> 0x00a5 }
        if (r3 == 0) goto L_0x00b2;
    L_0x00b1:
        goto L_0x00a1;
    L_0x00b2:
        goto L_0x00ba;
    L_0x00b3:
        if (r3 == 0) goto L_0x00b8;
    L_0x00b5:
        r3.recycle();
    L_0x00b8:
        throw r1;
    L_0x00b9:
        r13 = 1;
    L_0x00ba:
        if (r13 != r5) goto L_0x00f2;
    L_0x00bc:
        r3 = new com.oneplus.lib.widget.Spinner$DropdownPopup;
        r6 = r9.mPopupContext;
        r3.<init>(r6, r11, r12);
        r6 = r9.mPopupContext;
        r7 = com.oneplus.commonctrl.R.styleable.Spinner;
        r1 = r6.obtainStyledAttributes(r11, r7, r12, r1);
        r6 = com.oneplus.commonctrl.R.styleable.Spinner_android_dropDownWidth;
        r7 = -2;
        r6 = r1.getLayoutDimension(r6, r7);
        r9.mDropDownWidth = r6;
        r6 = com.oneplus.commonctrl.R.styleable.Spinner_android_popupBackground;
        r6 = r1.getDrawable(r6);
        r3.setBackgroundDrawable(r6);
        r6 = com.oneplus.commonctrl.R.styleable.Spinner_android_prompt;
        r6 = r0.getString(r6);
        r3.setPromptText(r6);
        r1.recycle();
        r9.mPopup = r3;
        r6 = new com.oneplus.lib.widget.Spinner$1;
        r6.<init>(r9, r3);
        r9.mForwardingListener = r6;
    L_0x00f2:
        r1 = com.oneplus.commonctrl.R.styleable.Spinner_android_entries;
        r1 = r0.getTextArray(r1);
        if (r1 == 0) goto L_0x010b;
    L_0x00fa:
        r3 = new android.widget.ArrayAdapter;
        r6 = 17367048; // 0x1090008 float:2.5162948E-38 double:8.580462E-317;
        r3.<init>(r10, r6, r1);
        r6 = 17367049; // 0x1090009 float:2.516295E-38 double:8.5804623E-317;
        r3.setDropDownViewResource(r6);
        r9.setAdapter(r3);
    L_0x010b:
        r0.recycle();
        r9.mPopupSet = r5;
        r3 = r9.mTempAdapter;
        if (r3 == 0) goto L_0x011b;
    L_0x0114:
        r3 = r9.mTempAdapter;
        r9.setAdapter(r3);
        r9.mTempAdapter = r4;
    L_0x011b:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.lib.widget.Spinner.<init>(android.content.Context, android.util.AttributeSet, int, int, android.content.res.Resources$Theme):void");
    }

    public Context getPopupContext() {
        if (this.mPopup != null) {
            return this.mPopupContext;
        }
        if (IS_AT_LEAST_M) {
            return super.getPopupContext();
        }
        return null;
    }

    public void setPopupBackgroundDrawable(Drawable background) {
        if (this.mPopup != null) {
            this.mPopup.setBackgroundDrawable(background);
        } else if (IS_AT_LEAST_JB) {
            super.setPopupBackgroundDrawable(background);
        }
    }

    public void setPopupBackgroundResource(int resId) {
        setPopupBackgroundDrawable(getResources().getDrawable(resId));
    }

    public Drawable getPopupBackground() {
        if (this.mPopup != null) {
            return this.mPopup.getBackground();
        }
        if (IS_AT_LEAST_JB) {
            return super.getPopupBackground();
        }
        return null;
    }

    public void setDropDownVerticalOffset(int pixels) {
        if (this.mPopup != null) {
            this.mPopup.setVerticalOffset(pixels);
        } else if (IS_AT_LEAST_JB) {
            super.setDropDownVerticalOffset(pixels);
        }
    }

    public int getDropDownVerticalOffset() {
        if (this.mPopup != null) {
            return this.mPopup.getVerticalOffset();
        }
        if (IS_AT_LEAST_JB) {
            return super.getDropDownVerticalOffset();
        }
        return 0;
    }

    public void setDropDownHorizontalOffset(int pixels) {
        if (this.mPopup != null) {
            this.mPopup.setHorizontalOffset(pixels);
        } else if (IS_AT_LEAST_JB) {
            super.setDropDownHorizontalOffset(pixels);
        }
    }

    public int getDropDownHorizontalOffset() {
        if (this.mPopup != null) {
            return this.mPopup.getHorizontalOffset();
        }
        if (IS_AT_LEAST_JB) {
            return super.getDropDownHorizontalOffset();
        }
        return 0;
    }

    public void setDropDownWidth(int pixels) {
        if (this.mPopup != null) {
            this.mDropDownWidth = pixels;
        } else if (IS_AT_LEAST_JB) {
            super.setDropDownWidth(pixels);
        }
    }

    public int getDropDownWidth() {
        if (this.mPopup != null) {
            return this.mDropDownWidth;
        }
        if (IS_AT_LEAST_JB) {
            return super.getDropDownWidth();
        }
        return 0;
    }

    public void setAdapter(SpinnerAdapter adapter) {
        if (this.mPopupSet) {
            super.setAdapter(adapter);
            if (this.mPopup != null) {
                this.mDropDownAdapter = new DropDownAdapter(adapter, (this.mPopupContext == null ? getContext() : this.mPopupContext).getTheme());
                this.mDropDownAdapter.setSelectedItemBackground(this.mSelectedItemBackground);
                this.mPopup.setAdapter(this.mDropDownAdapter);
            }
            return;
        }
        this.mTempAdapter = adapter;
    }

    /* Access modifiers changed, original: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mPopup != null && this.mPopup.isShowing()) {
            this.mPopup.dismiss();
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (this.mForwardingListener == null || !this.mForwardingListener.onTouch(this, event)) {
            return super.onTouchEvent(event);
        }
        return true;
    }

    /* Access modifiers changed, original: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (this.mPopup != null && MeasureSpec.getMode(widthMeasureSpec) == Integer.MIN_VALUE) {
            setMeasuredDimension(Math.min(Math.max(getMeasuredWidth(), compatMeasureContentWidth(getAdapter(), getBackground())), MeasureSpec.getSize(widthMeasureSpec)), getMeasuredHeight());
        }
    }

    public boolean performClick() {
        if (this.mPopup == null) {
            return super.performClick();
        }
        if (!this.mPopup.isShowing()) {
            this.mPopup.show();
        }
        return true;
    }

    public void setPrompt(CharSequence prompt) {
        if (this.mPopup != null) {
            this.mPopup.setPromptText(prompt);
        } else {
            super.setPrompt(prompt);
        }
    }

    public CharSequence getPrompt() {
        return this.mPopup != null ? this.mPopup.getHintText() : super.getPrompt();
    }

    /* Access modifiers changed, original: 0000 */
    public int compatMeasureContentWidth(SpinnerAdapter adapter, Drawable background) {
        if (adapter == null) {
            return 0;
        }
        View itemView = null;
        int itemType = 0;
        int widthMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredWidth(), 0);
        int heightMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredHeight(), 0);
        int start = Math.max(0, getSelectedItemPosition());
        int end = Math.min(adapter.getCount(), start + 15);
        start = 0;
        for (int i = Math.max(0, start - (15 - (end - start))); i < end; i++) {
            int positionType = adapter.getItemViewType(i);
            if (positionType != itemType) {
                itemType = positionType;
                itemView = null;
            }
            itemView = adapter.getView(i, itemView, this);
            if (itemView.getLayoutParams() == null) {
                itemView.setLayoutParams(new LayoutParams(-2, -2));
            }
            itemView.measure(widthMeasureSpec, heightMeasureSpec);
            start = Math.max(start, itemView.getMeasuredWidth());
        }
        if (background != null) {
            background.getPadding(this.mTempRect);
            start += this.mTempRect.left + this.mTempRect.right;
        }
        return start;
    }
}
