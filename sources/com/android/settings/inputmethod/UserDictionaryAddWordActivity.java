package com.android.settings.inputmethod;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.View;
import com.android.settings.R;

public class UserDictionaryAddWordActivity extends Activity {
    static final int CODE_ALREADY_PRESENT = 2;
    static final int CODE_CANCEL = 1;
    static final int CODE_WORD_ADDED = 0;
    public static final String MODE_EDIT_ACTION = "com.android.settings.USER_DICTIONARY_EDIT";
    public static final String MODE_INSERT_ACTION = "com.android.settings.USER_DICTIONARY_INSERT";
    private UserDictionaryAddWordContents mContents;

    public void onCreate(Bundle savedInstanceState) {
        int mode;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_dictionary_add_word);
        Intent intent = getIntent();
        String action = intent.getAction();
        if (MODE_EDIT_ACTION.equals(action)) {
            mode = 0;
        } else if (MODE_INSERT_ACTION.equals(action)) {
            mode = 1;
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Unsupported action: ");
            stringBuilder.append(action);
            throw new RuntimeException(stringBuilder.toString());
        }
        Bundle args = intent.getExtras();
        if (args == null) {
            args = new Bundle();
        }
        args.putInt(UserDictionaryAddWordContents.EXTRA_MODE, mode);
        if (savedInstanceState != null) {
            args.putAll(savedInstanceState);
        }
        this.mContents = new UserDictionaryAddWordContents(getWindow().getDecorView(), args);
    }

    public void onSaveInstanceState(Bundle outState) {
        this.mContents.saveStateIntoBundle(outState);
    }

    private void reportBackToCaller(int resultCode, Bundle result) {
        Intent senderIntent = getIntent();
        if (senderIntent.getExtras() != null) {
            Messenger listener = senderIntent.getExtras().get("listener");
            if (listener instanceof Messenger) {
                Messenger messenger = listener;
                Message m = Message.obtain();
                m.obj = result;
                m.what = resultCode;
                try {
                    messenger.send(m);
                } catch (RemoteException e) {
                }
            }
        }
    }

    public void onClickCancel(View v) {
        reportBackToCaller(1, null);
        finish();
    }

    public void onClickConfirm(View v) {
        Bundle parameters = new Bundle();
        reportBackToCaller(this.mContents.apply(this, parameters), parameters);
        finish();
    }
}
