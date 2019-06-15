package com.android.settings.users;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.UserInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import com.android.settings.R;
import com.android.settingslib.Utils;
import com.android.settingslib.drawable.CircleFramedDrawable;
import java.io.File;

public class EditUserInfoController {
    private static final String KEY_AWAITING_RESULT = "awaiting_result";
    private static final String KEY_SAVED_PHOTO = "pending_photo";
    private Dialog mEditUserInfoDialog;
    private EditUserPhotoController mEditUserPhotoController;
    private Bitmap mSavedPhoto;
    private UserHandle mUser;
    private UserManager mUserManager;
    private boolean mWaitingForActivityResult = false;

    public interface OnContentChangedCallback {
        void onLabelChanged(CharSequence charSequence);

        void onPhotoChanged(Drawable drawable);
    }

    public void clear() {
        this.mEditUserPhotoController.removeNewUserPhotoBitmapFile();
        this.mEditUserInfoDialog = null;
        this.mSavedPhoto = null;
    }

    public Dialog getDialog() {
        return this.mEditUserInfoDialog;
    }

    public void onRestoreInstanceState(Bundle icicle) {
        String pendingPhoto = icicle.getString(KEY_SAVED_PHOTO);
        if (pendingPhoto != null) {
            this.mSavedPhoto = EditUserPhotoController.loadNewUserPhotoBitmap(new File(pendingPhoto));
        }
        this.mWaitingForActivityResult = icicle.getBoolean(KEY_AWAITING_RESULT, false);
    }

    public void onSaveInstanceState(Bundle outState) {
        if (!(this.mEditUserInfoDialog == null || !this.mEditUserInfoDialog.isShowing() || this.mEditUserPhotoController == null)) {
            File file = this.mEditUserPhotoController.saveNewUserPhotoBitmap();
            if (file != null) {
                outState.putString(KEY_SAVED_PHOTO, file.getPath());
            }
        }
        if (this.mWaitingForActivityResult) {
            outState.putBoolean(KEY_AWAITING_RESULT, this.mWaitingForActivityResult);
        }
    }

    public void startingActivityForResult() {
        this.mWaitingForActivityResult = true;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        this.mWaitingForActivityResult = false;
        if (this.mEditUserInfoDialog != null && this.mEditUserInfoDialog.isShowing() && !this.mEditUserPhotoController.onActivityResult(requestCode, resultCode, data)) {
        }
    }

    public Dialog createDialog(Fragment fragment, Drawable currentUserIcon, CharSequence currentUserName, int titleResId, OnContentChangedCallback callback, UserHandle user) {
        Drawable drawable;
        Activity activity = fragment.getActivity();
        this.mUser = user;
        if (this.mUserManager == null) {
            this.mUserManager = UserManager.get(activity);
        }
        View content = activity.getLayoutInflater().inflate(R.layout.edit_user_info_dialog_content, null);
        UserInfo info = this.mUserManager.getUserInfo(this.mUser.getIdentifier());
        EditText userNameView = (EditText) content.findViewById(R.id.user_name);
        userNameView.setText(info.name);
        ImageView userPhotoView = (ImageView) content.findViewById(R.id.user_photo);
        if (this.mSavedPhoto != null) {
            drawable = CircleFramedDrawable.getInstance(activity, this.mSavedPhoto);
        } else {
            drawable = currentUserIcon;
            if (drawable == null) {
                drawable = Utils.getUserIcon(activity, this.mUserManager, info);
            }
        }
        Drawable drawable2 = drawable;
        userPhotoView.setImageDrawable(drawable2);
        this.mEditUserPhotoController = new EditUserPhotoController(fragment, userPhotoView, this.mSavedPhoto, drawable2, this.mWaitingForActivityResult);
        final EditText editText = userNameView;
        final CharSequence charSequence = currentUserName;
        AnonymousClass2 anonymousClass2 = r0;
        final OnContentChangedCallback onContentChangedCallback = callback;
        int i = 17039370;
        final Drawable drawable3 = currentUserIcon;
        Builder cancelable = new Builder(activity).setTitle(R.string.profile_info_settings_title).setView(content).setCancelable(true);
        final Fragment fragment2 = fragment;
        AnonymousClass2 anonymousClass22 = new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == -1) {
                    CharSequence userName = editText.getText();
                    if (!TextUtils.isEmpty(userName) && (charSequence == null || !userName.toString().equals(charSequence.toString()))) {
                        if (onContentChangedCallback != null) {
                            onContentChangedCallback.onLabelChanged(userName.toString());
                        }
                        EditUserInfoController.this.mUserManager.setUserName(EditUserInfoController.this.mUser.getIdentifier(), userName.toString());
                    }
                    Drawable drawable = EditUserInfoController.this.mEditUserPhotoController.getNewUserPhotoDrawable();
                    Bitmap bitmap = EditUserInfoController.this.mEditUserPhotoController.getNewUserPhotoBitmap();
                    if (!(drawable == null || bitmap == null || drawable.equals(drawable3))) {
                        if (onContentChangedCallback != null) {
                            onContentChangedCallback.onPhotoChanged(drawable);
                        }
                        new AsyncTask<Void, Void, Void>() {
                            /* Access modifiers changed, original: protected|varargs */
                            public Void doInBackground(Void... params) {
                                EditUserInfoController.this.mUserManager.setUserIcon(EditUserInfoController.this.mUser.getIdentifier(), EditUserInfoController.this.mEditUserPhotoController.getNewUserPhotoBitmap());
                                return null;
                            }
                        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
                    }
                    fragment2.getActivity().removeDialog(1);
                }
                EditUserInfoController.this.clear();
            }
        };
        this.mEditUserInfoDialog = cancelable.setPositiveButton(17039370, anonymousClass2).setNegativeButton(17039360, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                EditUserInfoController.this.clear();
            }
        }).create();
        this.mEditUserInfoDialog.getWindow().setSoftInputMode(4);
        return this.mEditUserInfoDialog;
    }
}
