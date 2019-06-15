package com.android.settingslib.drawer;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;
import com.android.settingslib.R;
import java.util.List;

public class ProfileSelectDialog extends DialogFragment implements OnClickListener {
    private static final String ARG_SELECTED_TILE = "selectedTile";
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final String TAG = "ProfileSelectDialog";
    private Tile mSelectedTile;

    public static void show(FragmentManager manager, Tile tile) {
        ProfileSelectDialog dialog = new ProfileSelectDialog();
        Bundle args = new Bundle();
        args.putParcelable(ARG_SELECTED_TILE, tile);
        dialog.setArguments(args);
        dialog.show(manager, "select_profile");
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mSelectedTile = (Tile) getArguments().getParcelable(ARG_SELECTED_TILE);
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getActivity();
        Builder builder = new Builder(context);
        builder.setTitle(R.string.choose_profile).setAdapter(UserAdapter.createUserAdapter(UserManager.get(context), context, this.mSelectedTile.userHandle), this);
        return builder.create();
    }

    public void onClick(DialogInterface dialog, int which) {
        UserHandle user = (UserHandle) this.mSelectedTile.userHandle.get(which);
        this.mSelectedTile.intent.addFlags(32768);
        getActivity().startActivityAsUser(this.mSelectedTile.intent, user);
    }

    public static void updateUserHandlesIfNeeded(Context context, Tile tile) {
        List<UserHandle> userHandles = tile.userHandle;
        if (tile.userHandle != null && tile.userHandle.size() > 1) {
            UserManager userManager = UserManager.get(context);
            int i = userHandles.size() - 1;
            while (true) {
                int i2 = i;
                if (i2 >= 0) {
                    if (userManager.getUserInfo(((UserHandle) userHandles.get(i2)).getIdentifier()) == null) {
                        if (DEBUG) {
                            String str = TAG;
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append("Delete the user: ");
                            stringBuilder.append(((UserHandle) userHandles.get(i2)).getIdentifier());
                            Log.d(str, stringBuilder.toString());
                        }
                        userHandles.remove(i2);
                    }
                    i = i2 - 1;
                } else {
                    return;
                }
            }
        }
    }
}
