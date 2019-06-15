package com.android.settings;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.RemoteException;
import android.os.UserHandle;
import android.security.KeyChain;
import android.security.KeyChain.KeyChainConnection;
import android.security.KeyStore;
import android.security.keymaster.KeyCharacteristics;
import android.support.v4.view.PointerIconCompat;
import android.support.v7.widget.RecyclerView.Adapter;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class UserCredentialsSettings extends SettingsPreferenceFragment implements OnClickListener {
    private static final String TAG = "UserCredentialsSettings";
    private static final SparseArray<Type> credentialViewTypes = new SparseArray();

    static class AliasEntry {
        public String alias;
        public int uid;

        AliasEntry() {
        }
    }

    private class AliasLoader extends AsyncTask<Void, Void, List<Credential>> {
        private AliasLoader() {
        }

        /* Access modifiers changed, original: protected|varargs */
        public List<Credential> doInBackground(Void... params) {
            KeyStore keyStore = KeyStore.getInstance();
            int myUserId = UserHandle.myUserId();
            int systemUid = UserHandle.getUid(myUserId, 1000);
            int wifiUid = UserHandle.getUid(myUserId, PointerIconCompat.TYPE_ALIAS);
            List<Credential> credentials = new ArrayList();
            credentials.addAll(getCredentialsForUid(keyStore, systemUid).values());
            credentials.addAll(getCredentialsForUid(keyStore, wifiUid).values());
            return credentials;
        }

        private boolean isAsymmetric(KeyStore keyStore, String alias, int uid) throws UnrecoverableKeyException {
            KeyCharacteristics keyCharacteristics = new KeyCharacteristics();
            int errorCode = keyStore.getKeyCharacteristics(alias, null, null, uid, keyCharacteristics);
            if (errorCode == 1) {
                Integer keymasterAlgorithm = keyCharacteristics.getEnum(268435458);
                if (keymasterAlgorithm == null) {
                    throw new UnrecoverableKeyException("Key algorithm unknown");
                } else if (keymasterAlgorithm.intValue() == 1 || keymasterAlgorithm.intValue() == 3) {
                    return true;
                } else {
                    return false;
                }
            }
            throw ((UnrecoverableKeyException) new UnrecoverableKeyException("Failed to obtain information about key").initCause(KeyStore.getKeyStoreException(errorCode)));
        }

        private SortedMap<String, Credential> getCredentialsForUid(KeyStore keyStore, int uid) {
            Type[] typeArr;
            UnrecoverableKeyException e;
            String str;
            StringBuilder stringBuilder;
            KeyStore keyStore2 = keyStore;
            int i = uid;
            TreeMap aliasMap = new TreeMap();
            Type[] values = Type.values();
            int length = values.length;
            int i2 = 0;
            while (i2 < length) {
                Type type = values[i2];
                String[] strArr = type.prefix;
                int length2 = strArr.length;
                int i3 = 0;
                while (i3 < length2) {
                    String prefix = strArr[i3];
                    String[] list = keyStore2.list(prefix, i);
                    int length3 = list.length;
                    int i4 = 0;
                    while (i4 < length3) {
                        String alias = list[i4];
                        typeArr = values;
                        if (!(UserHandle.getAppId(uid) == 1000 && (alias.startsWith("profile_key_name_encrypt_") || alias.startsWith("profile_key_name_decrypt_") || alias.startsWith("synthetic_password_")))) {
                            try {
                                if (type == Type.USER_KEY) {
                                    StringBuilder stringBuilder2 = new StringBuilder();
                                    stringBuilder2.append(prefix);
                                    stringBuilder2.append(alias);
                                    try {
                                        if (!isAsymmetric(keyStore2, stringBuilder2.toString(), i)) {
                                        }
                                    } catch (UnrecoverableKeyException e2) {
                                        e = e2;
                                        str = UserCredentialsSettings.TAG;
                                        stringBuilder = new StringBuilder();
                                        stringBuilder.append("Unable to determine algorithm of key: ");
                                        stringBuilder.append(prefix);
                                        stringBuilder.append(alias);
                                        Log.e(str, stringBuilder.toString(), e);
                                        i4++;
                                        values = typeArr;
                                        keyStore2 = keyStore;
                                        i = uid;
                                    }
                                }
                                Credential c = (Credential) aliasMap.get(alias);
                                if (c == null) {
                                    c = new Credential(alias, i);
                                    aliasMap.put(alias, c);
                                } else {
                                    Credential credential = c;
                                }
                                c.storedTypes.add(type);
                            } catch (UnrecoverableKeyException e3) {
                                e = e3;
                                str = UserCredentialsSettings.TAG;
                                stringBuilder = new StringBuilder();
                                stringBuilder.append("Unable to determine algorithm of key: ");
                                stringBuilder.append(prefix);
                                stringBuilder.append(alias);
                                Log.e(str, stringBuilder.toString(), e);
                                i4++;
                                values = typeArr;
                                keyStore2 = keyStore;
                                i = uid;
                            }
                        }
                        i4++;
                        values = typeArr;
                        keyStore2 = keyStore;
                        i = uid;
                    }
                    typeArr = values;
                    i3++;
                    keyStore2 = keyStore;
                    i = uid;
                }
                typeArr = values;
                i2++;
                keyStore2 = keyStore;
                i = uid;
            }
            return aliasMap;
        }

        /* Access modifiers changed, original: protected */
        public void onPostExecute(List<Credential> credentials) {
            if (UserCredentialsSettings.this.isAdded()) {
                if (credentials == null || credentials.size() == 0) {
                    TextView emptyTextView = (TextView) UserCredentialsSettings.this.getActivity().findViewById(16908292);
                    emptyTextView.setText(R.string.user_credential_none_installed);
                    UserCredentialsSettings.this.setEmptyView(emptyTextView);
                } else {
                    UserCredentialsSettings.this.setEmptyView(null);
                }
                UserCredentialsSettings.this.getListView().setAdapter(new CredentialAdapter(credentials, UserCredentialsSettings.this));
            }
        }
    }

    static class Credential implements Parcelable {
        public static final Creator<Credential> CREATOR = new Creator<Credential>() {
            public Credential createFromParcel(Parcel in) {
                return new Credential(in);
            }

            public Credential[] newArray(int size) {
                return new Credential[size];
            }
        };
        final String alias;
        final EnumSet<Type> storedTypes;
        final int uid;

        enum Type {
            CA_CERTIFICATE("CACERT_"),
            USER_CERTIFICATE("USRCERT_"),
            USER_KEY("USRPKEY_", "USRSKEY_");
            
            final String[] prefix;

            private Type(String... prefix) {
                this.prefix = prefix;
            }
        }

        Credential(String alias, int uid) {
            this.storedTypes = EnumSet.noneOf(Type.class);
            this.alias = alias;
            this.uid = uid;
        }

        Credential(Parcel in) {
            this(in.readString(), in.readInt());
            long typeBits = in.readLong();
            for (Type i : Type.values()) {
                if (((1 << i.ordinal()) & typeBits) != 0) {
                    this.storedTypes.add(i);
                }
            }
        }

        public void writeToParcel(Parcel out, int flags) {
            out.writeString(this.alias);
            out.writeInt(this.uid);
            long typeBits = 0;
            Iterator it = this.storedTypes.iterator();
            while (it.hasNext()) {
                typeBits |= 1 << ((Type) it.next()).ordinal();
            }
            out.writeLong(typeBits);
        }

        public int describeContents() {
            return 0;
        }

        public boolean isSystem() {
            return UserHandle.getAppId(this.uid) == 1000;
        }
    }

    private static class CredentialAdapter extends Adapter<ViewHolder> {
        private static final int LAYOUT_RESOURCE = 2131559097;
        private final List<Credential> mItems;
        private final OnClickListener mListener;

        public CredentialAdapter(List<Credential> items, OnClickListener listener) {
            this.mItems = items;
            this.mListener = listener;
        }

        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.user_credential_preference, parent, false));
        }

        public void onBindViewHolder(ViewHolder h, int position) {
            UserCredentialsSettings.getCredentialView((Credential) this.mItems.get(position), R.layout.user_credential_preference, h.itemView, null, false);
            h.itemView.setTag(this.mItems.get(position));
            h.itemView.setOnClickListener(this.mListener);
        }

        public int getItemCount() {
            return this.mItems.size();
        }
    }

    private static class ViewHolder extends android.support.v7.widget.RecyclerView.ViewHolder {
        public ViewHolder(View item) {
            super(item);
        }
    }

    public static class CredentialDialogFragment extends InstrumentedDialogFragment {
        private static final String ARG_CREDENTIAL = "credential";
        private static final String TAG = "CredentialDialogFragment";

        private class RemoveCredentialsTask extends AsyncTask<Credential, Void, Credential[]> {
            private Context context;
            private Fragment targetFragment;

            public RemoveCredentialsTask(Context context, Fragment targetFragment) {
                this.context = context;
                this.targetFragment = targetFragment;
            }

            /* Access modifiers changed, original: protected|varargs */
            public Credential[] doInBackground(Credential... credentials) {
                int length = credentials.length;
                int i = 0;
                while (i < length) {
                    Credential credential = credentials[i];
                    if (credential.isSystem()) {
                        removeGrantsAndDelete(credential);
                        i++;
                    } else {
                        throw new UnsupportedOperationException("Not implemented for wifi certificates. This should not be reachable.");
                    }
                }
                return credentials;
            }

            private void removeGrantsAndDelete(Credential credential) {
                try {
                    KeyChainConnection conn = KeyChain.bind(CredentialDialogFragment.this.getContext());
                    try {
                        conn.getService().removeKeyPair(credential.alias);
                    } catch (RemoteException e) {
                        Log.w(CredentialDialogFragment.TAG, "Removing credentials", e);
                    } catch (Throwable th) {
                        conn.close();
                    }
                    conn.close();
                } catch (InterruptedException e2) {
                    Log.w(CredentialDialogFragment.TAG, "Connecting to KeyChain", e2);
                }
            }

            /* Access modifiers changed, original: protected|varargs */
            public void onPostExecute(Credential... credentials) {
                if ((this.targetFragment instanceof UserCredentialsSettings) && this.targetFragment.isAdded()) {
                    UserCredentialsSettings target = this.targetFragment;
                    for (Credential credential : credentials) {
                        target.announceRemoval(credential.alias);
                    }
                    target.refreshItems();
                }
            }
        }

        public static void show(Fragment target, Credential item) {
            Bundle args = new Bundle();
            args.putParcelable(ARG_CREDENTIAL, item);
            if (target.getFragmentManager().findFragmentByTag(TAG) == null) {
                DialogFragment frag = new CredentialDialogFragment();
                frag.setTargetFragment(target, -1);
                frag.setArguments(args);
                frag.show(target.getFragmentManager(), TAG);
            }
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Credential item = (Credential) getArguments().getParcelable(ARG_CREDENTIAL);
            View root = getActivity().getLayoutInflater().inflate(R.layout.user_credential_dialog, null);
            ViewGroup infoContainer = (ViewGroup) root.findViewById(R.id.credential_container);
            infoContainer.addView(UserCredentialsSettings.getCredentialView(item, R.layout.user_credential, null, infoContainer, true));
            Builder builder = new Builder(getActivity()).setView(root).setTitle(R.string.user_credential_title).setPositiveButton(R.string.done, null);
            String restriction = "no_config_credentials";
            final int myUserId = UserHandle.myUserId();
            if (!RestrictedLockUtils.hasBaseUserRestriction(getContext(), "no_config_credentials", myUserId)) {
                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        EnforcedAdmin admin = RestrictedLockUtils.checkIfRestrictionEnforced(CredentialDialogFragment.this.getContext(), "no_config_credentials", myUserId);
                        if (admin != null) {
                            RestrictedLockUtils.sendShowAdminSupportDetailsIntent(CredentialDialogFragment.this.getContext(), admin);
                        } else {
                            new RemoveCredentialsTask(CredentialDialogFragment.this.getContext(), CredentialDialogFragment.this.getTargetFragment()).execute(new Credential[]{item});
                        }
                        dialog.dismiss();
                    }
                };
                if (item.isSystem()) {
                    builder.setNegativeButton(R.string.trusted_credentials_remove_label, listener);
                }
            }
            return builder.create();
        }

        public int getMetricsCategory() {
            return 533;
        }
    }

    public int getMetricsCategory() {
        return 285;
    }

    public void onResume() {
        super.onResume();
        refreshItems();
    }

    public void onClick(View view) {
        Credential item = (Credential) view.getTag();
        if (item != null) {
            CredentialDialogFragment.show(this, item);
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(R.string.user_credentials);
    }

    /* Access modifiers changed, original: protected */
    public void announceRemoval(String alias) {
        if (isAdded()) {
            getListView().announceForAccessibility(getString(R.string.user_credential_removed, new Object[]{alias}));
        }
    }

    /* Access modifiers changed, original: protected */
    public void refreshItems() {
        if (isAdded()) {
            new AliasLoader().execute(new Void[0]);
        }
    }

    static {
        credentialViewTypes.put(R.id.contents_userkey, Type.USER_KEY);
        credentialViewTypes.put(R.id.contents_usercrt, Type.USER_CERTIFICATE);
        credentialViewTypes.put(R.id.contents_cacrt, Type.CA_CERTIFICATE);
    }

    protected static View getCredentialView(Credential item, int layoutResource, View view, ViewGroup parent, boolean expanded) {
        int i;
        if (view == null) {
            view = LayoutInflater.from(parent.getContext()).inflate(layoutResource, parent, false);
        }
        ((TextView) view.findViewById(R.id.alias)).setText(item.alias);
        TextView textView = (TextView) view.findViewById(R.id.purpose);
        if (item.isSystem()) {
            i = R.string.credential_for_vpn_and_apps;
        } else {
            i = R.string.credential_for_wifi;
        }
        textView.setText(i);
        view.findViewById(R.id.contents).setVisibility(expanded ? 0 : 8);
        if (expanded) {
            for (int i2 = 0; i2 < credentialViewTypes.size(); i2++) {
                view.findViewById(credentialViewTypes.keyAt(i2)).setVisibility(item.storedTypes.contains(credentialViewTypes.valueAt(i2)) ? 0 : 8);
            }
        }
        return view;
    }
}
