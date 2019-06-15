package android.support.v7.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.TypedArrayUtils;
import android.util.AttributeSet;
import com.android.settings.R;

public abstract class DialogPreference extends Preference {
    private Drawable mDialogIcon;
    private int mDialogLayoutResId;
    private CharSequence mDialogMessage;
    private CharSequence mDialogTitle;
    private CharSequence mNegativeButtonText;
    private CharSequence mPositiveButtonText;

    public interface TargetFragment {
        Preference findPreference(CharSequence charSequence);
    }

    public DialogPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DialogPreference, defStyleAttr, defStyleRes);
        this.mDialogTitle = TypedArrayUtils.getString(a, 9, 0);
        if (this.mDialogTitle == null) {
            this.mDialogTitle = getTitle();
        }
        this.mDialogMessage = TypedArrayUtils.getString(a, 8, 1);
        this.mDialogIcon = TypedArrayUtils.getDrawable(a, 6, 2);
        this.mPositiveButtonText = TypedArrayUtils.getString(a, 12, 3);
        this.mNegativeButtonText = TypedArrayUtils.getString(a, 10, 4);
        this.mDialogLayoutResId = TypedArrayUtils.getResourceId(a, 7, 5, 0);
        a.recycle();
    }

    public DialogPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public DialogPreference(Context context, AttributeSet attrs) {
        this(context, attrs, TypedArrayUtils.getAttr(context, R.attr.dialogPreferenceStyle, 16842897));
    }

    public DialogPreference(Context context) {
        this(context, null);
    }

    public void setDialogTitle(CharSequence dialogTitle) {
        this.mDialogTitle = dialogTitle;
    }

    public void setDialogTitle(int dialogTitleResId) {
        setDialogTitle(getContext().getString(dialogTitleResId));
    }

    public CharSequence getDialogTitle() {
        return this.mDialogTitle;
    }

    public void setDialogMessage(CharSequence dialogMessage) {
        this.mDialogMessage = dialogMessage;
    }

    public void setDialogMessage(int dialogMessageResId) {
        setDialogMessage(getContext().getString(dialogMessageResId));
    }

    public CharSequence getDialogMessage() {
        return this.mDialogMessage;
    }

    public void setDialogIcon(Drawable dialogIcon) {
        this.mDialogIcon = dialogIcon;
    }

    public void setDialogIcon(int dialogIconRes) {
        this.mDialogIcon = ContextCompat.getDrawable(getContext(), dialogIconRes);
    }

    public Drawable getDialogIcon() {
        return this.mDialogIcon;
    }

    public void setPositiveButtonText(CharSequence positiveButtonText) {
        this.mPositiveButtonText = positiveButtonText;
    }

    public void setPositiveButtonText(int positiveButtonTextResId) {
        setPositiveButtonText(getContext().getString(positiveButtonTextResId));
    }

    public CharSequence getPositiveButtonText() {
        return this.mPositiveButtonText;
    }

    public void setNegativeButtonText(CharSequence negativeButtonText) {
        this.mNegativeButtonText = negativeButtonText;
    }

    public void setNegativeButtonText(int negativeButtonTextResId) {
        setNegativeButtonText(getContext().getString(negativeButtonTextResId));
    }

    public CharSequence getNegativeButtonText() {
        return this.mNegativeButtonText;
    }

    public void setDialogLayoutResource(int dialogLayoutResId) {
        this.mDialogLayoutResId = dialogLayoutResId;
    }

    public int getDialogLayoutResource() {
        return this.mDialogLayoutResId;
    }

    /* Access modifiers changed, original: protected */
    public void onClick() {
        getPreferenceManager().showDialog(this);
    }
}
