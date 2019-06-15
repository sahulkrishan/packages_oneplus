package com.android.settings.password;

import android.content.Context;
import android.content.Intent;

public class ManagedLockPasswordProvider {
    public static ManagedLockPasswordProvider get(Context context, int userId) {
        return new ManagedLockPasswordProvider();
    }

    protected ManagedLockPasswordProvider() {
    }

    /* Access modifiers changed, original: 0000 */
    public boolean isSettingManagedPasswordSupported() {
        return false;
    }

    /* Access modifiers changed, original: 0000 */
    public boolean isManagedPasswordChoosable() {
        return false;
    }

    /* Access modifiers changed, original: 0000 */
    public CharSequence getPickerOptionTitle(boolean forFingerprint) {
        return "";
    }

    /* Access modifiers changed, original: 0000 */
    public Intent createIntent(boolean requirePasswordToDecrypt, String password) {
        return null;
    }
}
