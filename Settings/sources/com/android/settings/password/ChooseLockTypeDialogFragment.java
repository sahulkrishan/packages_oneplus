package com.android.settings.password;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;
import com.android.settings.password.ChooseLockGeneric.ChooseLockGenericFragment;
import com.android.setupwizardlib.util.WizardManagerHelper;
import java.util.List;

public class ChooseLockTypeDialogFragment extends InstrumentedDialogFragment implements OnClickListener {
    private static final String ARG_USER_ID = "userId";
    private ScreenLockAdapter mAdapter;
    private ChooseLockGenericController mController;

    public interface OnLockTypeSelectedListener {
        void onLockTypeSelected(ScreenLockType screenLockType);

        void startChooseLockActivity(ScreenLockType selectedLockType, Activity activity) {
            Intent activityIntent = activity.getIntent();
            Intent intent = new Intent(activity, SetupChooseLockGeneric.class);
            intent.addFlags(33554432);
            ChooseLockTypeDialogFragment.copyBooleanExtra(activityIntent, intent, ChooseLockSettingsHelper.EXTRA_KEY_HAS_CHALLENGE, false);
            ChooseLockTypeDialogFragment.copyBooleanExtra(activityIntent, intent, ChooseLockGenericFragment.EXTRA_SHOW_OPTIONS_BUTTON, false);
            if (activityIntent.hasExtra(ChooseLockGenericFragment.EXTRA_CHOOSE_LOCK_GENERIC_EXTRAS)) {
                intent.putExtras(activityIntent.getBundleExtra(ChooseLockGenericFragment.EXTRA_CHOOSE_LOCK_GENERIC_EXTRAS));
            }
            intent.putExtra("lockscreen.password_type", selectedLockType.defaultQuality);
            intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE, activityIntent.getLongExtra(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE, 0));
            WizardManagerHelper.copyWizardManagerExtras(activityIntent, intent);
            activity.startActivity(intent);
            activity.finish();
        }
    }

    private static class ScreenLockAdapter extends ArrayAdapter<ScreenLockType> {
        private final ChooseLockGenericController mController;

        ScreenLockAdapter(Context context, List<ScreenLockType> locks, ChooseLockGenericController controller) {
            super(context, R.layout.choose_lock_dialog_item, locks);
            this.mController = controller;
        }

        public View getView(int position, View view, ViewGroup parent) {
            Context context = parent.getContext();
            if (view == null) {
                view = LayoutInflater.from(context).inflate(R.layout.choose_lock_dialog_item, parent, false);
            }
            ScreenLockType lock = (ScreenLockType) getItem(position);
            TextView textView = (TextView) view;
            textView.setText(this.mController.getTitle(lock));
            textView.setCompoundDrawablesRelativeWithIntrinsicBounds(getIconForScreenLock(context, lock), null, null, null);
            return view;
        }

        private static Drawable getIconForScreenLock(Context context, ScreenLockType lock) {
            switch (lock) {
                case PATTERN:
                    return context.getDrawable(R.drawable.ic_pattern);
                case PIN:
                    return context.getDrawable(R.drawable.ic_pin);
                case PASSWORD:
                    return context.getDrawable(R.drawable.ic_password);
                default:
                    return null;
            }
        }
    }

    public static ChooseLockTypeDialogFragment newInstance(int userId) {
        Bundle args = new Bundle();
        args.putInt("userId", userId);
        ChooseLockTypeDialogFragment fragment = new ChooseLockTypeDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private static void copyBooleanExtra(Intent from, Intent to, String name, boolean defaultValue) {
        to.putExtra(name, from.getBooleanExtra(name, defaultValue));
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mController = new ChooseLockGenericController(getContext(), getArguments().getInt("userId"));
    }

    public void onClick(DialogInterface dialogInterface, int i) {
        OnLockTypeSelectedListener listener = null;
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof OnLockTypeSelectedListener) {
            listener = (OnLockTypeSelectedListener) parentFragment;
        } else {
            Context context = getContext();
            if (context instanceof OnLockTypeSelectedListener) {
                listener = (OnLockTypeSelectedListener) context;
            }
        }
        if (listener != null) {
            listener.onLockTypeSelected((ScreenLockType) this.mAdapter.getItem(i));
        }
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getContext();
        Builder builder = new Builder(context);
        this.mAdapter = new ScreenLockAdapter(context, this.mController.getVisibleScreenLockTypes(65536, false), this.mController);
        builder.setAdapter(this.mAdapter, this);
        builder.setTitle(R.string.setup_lock_settings_options_dialog_title);
        return builder.create();
    }

    public int getMetricsCategory() {
        return 990;
    }
}
