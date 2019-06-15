package com.android.settings.users;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.UserInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.users.EditUserInfoController.OnContentChangedCallback;

public class RestrictedProfileSettings extends AppRestrictionsFragment implements OnContentChangedCallback {
    private static final int DIALOG_CONFIRM_REMOVE = 2;
    static final int DIALOG_ID_EDIT_USER_INFO = 1;
    public static final String FILE_PROVIDER_AUTHORITY = "com.android.settings.files";
    private ImageView mDeleteButton;
    private EditUserInfoController mEditUserInfoController = new EditUserInfoController();
    private View mHeaderView;
    private ImageView mUserIconView;
    private TextView mUserNameView;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        if (icicle != null) {
            this.mEditUserInfoController.onRestoreInstanceState(icicle);
        }
        init(icicle);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        this.mHeaderView = setPinnedHeaderView((int) R.layout.user_info_header);
        this.mHeaderView.setOnClickListener(this);
        this.mUserIconView = (ImageView) this.mHeaderView.findViewById(16908294);
        this.mUserNameView = (TextView) this.mHeaderView.findViewById(16908310);
        this.mDeleteButton = (ImageView) this.mHeaderView.findViewById(R.id.delete);
        this.mDeleteButton.setOnClickListener(this);
        super.onActivityCreated(savedInstanceState);
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        this.mEditUserInfoController.onSaveInstanceState(outState);
    }

    public void onResume() {
        super.onResume();
        UserInfo info = Utils.getExistingUser(this.mUserManager, this.mUser);
        if (info == null) {
            finishFragment();
            return;
        }
        ((TextView) this.mHeaderView.findViewById(16908310)).setText(info.name);
        ((ImageView) this.mHeaderView.findViewById(16908294)).setImageDrawable(com.android.settingslib.Utils.getUserIcon(getActivity(), this.mUserManager, info));
    }

    public void startActivityForResult(Intent intent, int requestCode) {
        this.mEditUserInfoController.startingActivityForResult();
        super.startActivityForResult(intent, requestCode);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        this.mEditUserInfoController.onActivityResult(requestCode, resultCode, data);
    }

    public void onClick(View view) {
        if (view == this.mHeaderView) {
            showDialog(1);
        } else if (view == this.mDeleteButton) {
            showDialog(2);
        } else {
            super.onClick(view);
        }
    }

    public Dialog onCreateDialog(int dialogId) {
        if (dialogId == 1) {
            return this.mEditUserInfoController.createDialog(this, this.mUserIconView.getDrawable(), this.mUserNameView.getText(), R.string.profile_info_settings_title, this, this.mUser);
        } else if (dialogId == 2) {
            return UserDialogs.createRemoveDialog(getActivity(), this.mUser.getIdentifier(), new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    RestrictedProfileSettings.this.removeUser();
                }
            });
        } else {
            return null;
        }
    }

    public int getDialogMetricsCategory(int dialogId) {
        switch (dialogId) {
            case 1:
                return 590;
            case 2:
                return 591;
            default:
                return 0;
        }
    }

    private void removeUser() {
        getView().post(new Runnable() {
            public void run() {
                RestrictedProfileSettings.this.mUserManager.removeUser(RestrictedProfileSettings.this.mUser.getIdentifier());
                RestrictedProfileSettings.this.finishFragment();
            }
        });
    }

    public void onPhotoChanged(Drawable photo) {
        this.mUserIconView.setImageDrawable(photo);
    }

    public void onLabelChanged(CharSequence label) {
        this.mUserNameView.setText(label);
    }
}
