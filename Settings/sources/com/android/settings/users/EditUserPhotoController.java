package com.android.settings.users;

import android.app.Fragment;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.os.UserHandle;
import android.provider.ContactsContract.DisplayPhoto;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.android.settingslib.drawable.CircleFramedDrawable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import libcore.io.Streams;

public class EditUserPhotoController {
    private static final String CROP_PICTURE_FILE_NAME = "CropEditUserPhoto.jpg";
    private static final String NEW_USER_PHOTO_FILE_NAME = "NewUserPhoto.png";
    private static final int REQUEST_CODE_CHOOSE_PHOTO = 1001;
    private static final int REQUEST_CODE_CROP_PHOTO = 1003;
    private static final int REQUEST_CODE_TAKE_PHOTO = 1002;
    private static final String TAG = "EditUserPhotoController";
    private static final String TAKE_PICTURE_FILE_NAME = "TakeEditUserPhoto2.jpg";
    private final Context mContext;
    private final Uri mCropPictureUri;
    private final Fragment mFragment;
    private final ImageView mImageView;
    private Bitmap mNewUserPhotoBitmap;
    private Drawable mNewUserPhotoDrawable;
    private final int mPhotoSize = getPhotoSize(this.mContext);
    private final Uri mTakePictureUri;

    private static final class RestrictedMenuItem {
        private final Runnable mAction;
        private final EnforcedAdmin mAdmin;
        private final Context mContext;
        private final boolean mIsRestrictedByBase;
        private final String mTitle;

        public RestrictedMenuItem(Context context, String title, String restriction, Runnable action) {
            this.mContext = context;
            this.mTitle = title;
            this.mAction = action;
            int myUserId = UserHandle.myUserId();
            this.mAdmin = RestrictedLockUtils.checkIfRestrictionEnforced(context, restriction, myUserId);
            this.mIsRestrictedByBase = RestrictedLockUtils.hasBaseUserRestriction(this.mContext, restriction, myUserId);
        }

        public String toString() {
            return this.mTitle;
        }

        /* Access modifiers changed, original: final */
        public final void doAction() {
            if (!isRestrictedByBase()) {
                if (isRestrictedByAdmin()) {
                    RestrictedLockUtils.sendShowAdminSupportDetailsIntent(this.mContext, this.mAdmin);
                } else {
                    this.mAction.run();
                }
            }
        }

        /* Access modifiers changed, original: final */
        public final boolean isRestrictedByAdmin() {
            return this.mAdmin != null;
        }

        /* Access modifiers changed, original: final */
        public final boolean isRestrictedByBase() {
            return this.mIsRestrictedByBase;
        }
    }

    private static final class RestrictedPopupMenuAdapter extends ArrayAdapter<RestrictedMenuItem> {
        public RestrictedPopupMenuAdapter(Context context, List<RestrictedMenuItem> items) {
            super(context, R.layout.restricted_popup_menu_item, R.id.text, items);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            RestrictedMenuItem item = (RestrictedMenuItem) getItem(position);
            TextView text = (TextView) view.findViewById(R.id.text);
            ImageView image = (ImageView) view.findViewById(R.id.restricted_icon);
            int i = 0;
            boolean z = (item.isRestrictedByAdmin() || item.isRestrictedByBase()) ? false : true;
            text.setEnabled(z);
            if (!item.isRestrictedByAdmin() || item.isRestrictedByBase()) {
                i = 8;
            }
            image.setVisibility(i);
            return view;
        }
    }

    public EditUserPhotoController(Fragment fragment, ImageView view, Bitmap bitmap, Drawable drawable, boolean waiting) {
        this.mContext = view.getContext();
        this.mFragment = fragment;
        this.mImageView = view;
        this.mCropPictureUri = createTempImageUri(this.mContext, CROP_PICTURE_FILE_NAME, waiting ^ 1);
        this.mTakePictureUri = createTempImageUri(this.mContext, TAKE_PICTURE_FILE_NAME, waiting ^ 1);
        this.mImageView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                EditUserPhotoController.this.showUpdatePhotoPopup();
            }
        });
        this.mNewUserPhotoBitmap = bitmap;
        this.mNewUserPhotoDrawable = drawable;
    }

    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != -1) {
            return false;
        }
        Uri pictureUri = (data == null || data.getData() == null) ? this.mTakePictureUri : data.getData();
        switch (requestCode) {
            case 1001:
            case 1002:
                if (this.mTakePictureUri.equals(pictureUri)) {
                    cropPhoto();
                } else {
                    copyAndCropPhoto(pictureUri);
                }
                return true;
            case 1003:
                onPhotoCropped(pictureUri, true);
                return true;
            default:
                return false;
        }
    }

    public Bitmap getNewUserPhotoBitmap() {
        return this.mNewUserPhotoBitmap;
    }

    public Drawable getNewUserPhotoDrawable() {
        return this.mNewUserPhotoDrawable;
    }

    private void showUpdatePhotoPopup() {
        boolean canTakePhoto = canTakePhoto();
        boolean canChoosePhoto = canChoosePhoto();
        if (canTakePhoto || canChoosePhoto) {
            Context context = this.mImageView.getContext();
            List<RestrictedMenuItem> items = new ArrayList();
            if (canTakePhoto) {
                items.add(new RestrictedMenuItem(context, context.getString(R.string.user_image_take_photo), "no_set_user_icon", new Runnable() {
                    public void run() {
                        EditUserPhotoController.this.takePhoto();
                    }
                }));
            }
            if (canChoosePhoto) {
                items.add(new RestrictedMenuItem(context, context.getString(R.string.user_image_choose_photo), "no_set_user_icon", new Runnable() {
                    public void run() {
                        EditUserPhotoController.this.choosePhoto();
                    }
                }));
            }
            final ListPopupWindow listPopupWindow = new ListPopupWindow(context);
            listPopupWindow.setAnchorView(this.mImageView);
            listPopupWindow.setModal(true);
            listPopupWindow.setInputMethodMode(2);
            listPopupWindow.setAdapter(new RestrictedPopupMenuAdapter(context, items));
            listPopupWindow.setWidth(Math.max(this.mImageView.getWidth(), context.getResources().getDimensionPixelSize(R.dimen.update_user_photo_popup_min_width)));
            listPopupWindow.setDropDownGravity(GravityCompat.START);
            listPopupWindow.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    listPopupWindow.dismiss();
                    ((RestrictedMenuItem) parent.getAdapter().getItem(position)).doAction();
                }
            });
            listPopupWindow.show();
        }
    }

    private boolean canTakePhoto() {
        return this.mImageView.getContext().getPackageManager().queryIntentActivities(new Intent("android.media.action.IMAGE_CAPTURE"), 65536).size() > 0;
    }

    private boolean canChoosePhoto() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        if (this.mImageView.getContext().getPackageManager().queryIntentActivities(intent, 0).size() > 0) {
            return true;
        }
        return false;
    }

    private void takePhoto() {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        appendOutputExtra(intent, this.mTakePictureUri);
        this.mFragment.startActivityForResult(intent, 1002);
    }

    private void choosePhoto() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT", null);
        intent.setType("image/*");
        appendOutputExtra(intent, this.mTakePictureUri);
        this.mFragment.startActivityForResult(intent, 1001);
    }

    private void copyAndCropPhoto(final Uri pictureUri) {
        new AsyncTask<Void, Void, Void>() {
            /* Access modifiers changed, original: protected|varargs */
            public Void doInBackground(Void... params) {
                Throwable th;
                Throwable th2;
                Throwable th3;
                ContentResolver cr = EditUserPhotoController.this.mContext.getContentResolver();
                try {
                    InputStream in = cr.openInputStream(pictureUri);
                    Throwable th4;
                    try {
                        OutputStream out = cr.openOutputStream(EditUserPhotoController.this.mTakePictureUri);
                        try {
                            Streams.copy(in, out);
                            if (out != null) {
                                AnonymousClass5.$closeResource(null, out);
                            }
                            if (in != null) {
                                AnonymousClass5.$closeResource(null, in);
                            }
                            return null;
                        } catch (Throwable th22) {
                            th3 = th22;
                            th22 = th;
                            th = th3;
                        }
                        if (in != null) {
                            AnonymousClass5.$closeResource(th, in);
                        }
                        throw th4;
                        if (out != null) {
                            AnonymousClass5.$closeResource(th22, out);
                        }
                        throw th;
                    } catch (Throwable th5) {
                        th3 = th5;
                        th5 = th4;
                        th4 = th3;
                    }
                } catch (IOException e) {
                    Log.w(EditUserPhotoController.TAG, "Failed to copy photo", e);
                }
            }

            private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
                if (x0 != null) {
                    try {
                        x1.close();
                        return;
                    } catch (Throwable th) {
                        x0.addSuppressed(th);
                        return;
                    }
                }
                x1.close();
            }

            /* Access modifiers changed, original: protected */
            public void onPostExecute(Void result) {
                if (EditUserPhotoController.this.mFragment.isAdded()) {
                    EditUserPhotoController.this.cropPhoto();
                }
            }
        }.execute(new Void[0]);
    }

    private void cropPhoto() {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(this.mTakePictureUri, "image/*");
        appendOutputExtra(intent, this.mCropPictureUri);
        appendCropExtras(intent);
        if (intent.resolveActivity(this.mContext.getPackageManager()) != null) {
            try {
                StrictMode.disableDeathOnFileUriExposure();
                this.mFragment.startActivityForResult(intent, 1003);
            } finally {
                StrictMode.enableDeathOnFileUriExposure();
            }
        } else {
            onPhotoCropped(this.mTakePictureUri, false);
        }
    }

    private void appendOutputExtra(Intent intent, Uri pictureUri) {
        intent.putExtra("output", pictureUri);
        intent.addFlags(3);
        intent.setClipData(ClipData.newRawUri("output", pictureUri));
    }

    private void appendCropExtras(Intent intent) {
        intent.putExtra("crop", "true");
        intent.putExtra("scale", true);
        intent.putExtra("scaleUpIfNeeded", true);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", this.mPhotoSize);
        intent.putExtra("outputY", this.mPhotoSize);
    }

    private void onPhotoCropped(final Uri data, final boolean cropped) {
        new AsyncTask<Void, Void, Bitmap>() {
            /* Access modifiers changed, original: protected|varargs */
            public Bitmap doInBackground(Void... params) {
                if (cropped) {
                    InputStream imageStream = null;
                    try {
                        imageStream = EditUserPhotoController.this.mContext.getContentResolver().openInputStream(data);
                        Bitmap decodeStream = BitmapFactory.decodeStream(imageStream);
                        if (imageStream != null) {
                            try {
                                imageStream.close();
                            } catch (IOException ioe) {
                                Log.w(EditUserPhotoController.TAG, "Cannot close image stream", ioe);
                            }
                        }
                        return decodeStream;
                    } catch (FileNotFoundException fe) {
                        Log.w(EditUserPhotoController.TAG, "Cannot find image file", fe);
                        if (imageStream != null) {
                            try {
                                imageStream.close();
                            } catch (IOException ioe2) {
                                Log.w(EditUserPhotoController.TAG, "Cannot close image stream", ioe2);
                            }
                        }
                        return null;
                    } catch (Throwable th) {
                        if (imageStream != null) {
                            try {
                                imageStream.close();
                            } catch (IOException ioe3) {
                                Log.w(EditUserPhotoController.TAG, "Cannot close image stream", ioe3);
                            }
                        }
                    }
                } else {
                    Bitmap croppedImage = Bitmap.createBitmap(EditUserPhotoController.this.mPhotoSize, EditUserPhotoController.this.mPhotoSize, Config.ARGB_8888);
                    Canvas canvas = new Canvas(croppedImage);
                    InputStream imageStream2 = null;
                    try {
                        imageStream2 = EditUserPhotoController.this.mContext.getContentResolver().openInputStream(data);
                        Bitmap fullImage = BitmapFactory.decodeStream(imageStream2);
                        if (imageStream2 != null) {
                            try {
                                imageStream2.close();
                            } catch (IOException e) {
                            }
                        }
                        if (fullImage == null) {
                            return null;
                        }
                        int squareSize = Math.min(fullImage.getWidth(), fullImage.getHeight());
                        int left = (fullImage.getWidth() - squareSize) / 2;
                        int top = (fullImage.getHeight() - squareSize) / 2;
                        canvas.drawBitmap(fullImage, new Rect(left, top, left + squareSize, top + squareSize), new Rect(0, 0, EditUserPhotoController.this.mPhotoSize, EditUserPhotoController.this.mPhotoSize), new Paint());
                        return croppedImage;
                    } catch (FileNotFoundException e2) {
                        if (imageStream2 != null) {
                            try {
                                imageStream2.close();
                            } catch (IOException e3) {
                            }
                        }
                        return null;
                    } catch (Throwable th2) {
                        if (imageStream2 != null) {
                            try {
                                imageStream2.close();
                            } catch (IOException e4) {
                            }
                        }
                    }
                }
            }

            /* Access modifiers changed, original: protected */
            public void onPostExecute(Bitmap bitmap) {
                if (bitmap != null) {
                    EditUserPhotoController.this.mNewUserPhotoBitmap = bitmap;
                    EditUserPhotoController.this.mNewUserPhotoDrawable = CircleFramedDrawable.getInstance(EditUserPhotoController.this.mImageView.getContext(), EditUserPhotoController.this.mNewUserPhotoBitmap);
                    EditUserPhotoController.this.mImageView.setImageDrawable(EditUserPhotoController.this.mNewUserPhotoDrawable);
                }
                new File(EditUserPhotoController.this.mContext.getCacheDir(), EditUserPhotoController.TAKE_PICTURE_FILE_NAME).delete();
                new File(EditUserPhotoController.this.mContext.getCacheDir(), EditUserPhotoController.CROP_PICTURE_FILE_NAME).delete();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
    }

    private static int getPhotoSize(Context context) {
        Cursor cursor = context.getContentResolver().query(DisplayPhoto.CONTENT_MAX_DIMENSIONS_URI, new String[]{"display_max_dim"}, null, null, null);
        try {
            cursor.moveToFirst();
            int i = cursor.getInt(0);
            return i;
        } finally {
            cursor.close();
        }
    }

    private Uri createTempImageUri(Context context, String fileName, boolean purge) {
        File folder = context.getCacheDir();
        folder.mkdirs();
        File fullPath = new File(folder, fileName);
        if (purge) {
            fullPath.delete();
        }
        return FileProvider.getUriForFile(context, RestrictedProfileSettings.FILE_PROVIDER_AUTHORITY, fullPath);
    }

    /* Access modifiers changed, original: 0000 */
    public File saveNewUserPhotoBitmap() {
        if (this.mNewUserPhotoBitmap == null) {
            return null;
        }
        try {
            File file = new File(this.mContext.getCacheDir(), NEW_USER_PHOTO_FILE_NAME);
            OutputStream os = new FileOutputStream(file);
            this.mNewUserPhotoBitmap.compress(CompressFormat.PNG, 100, os);
            os.flush();
            os.close();
            return file;
        } catch (IOException e) {
            Log.e(TAG, "Cannot create temp file", e);
            return null;
        }
    }

    static Bitmap loadNewUserPhotoBitmap(File file) {
        return BitmapFactory.decodeFile(file.getAbsolutePath());
    }

    /* Access modifiers changed, original: 0000 */
    public void removeNewUserPhotoBitmapFile() {
        new File(this.mContext.getCacheDir(), NEW_USER_PHOTO_FILE_NAME).delete();
    }
}
