package com.android.settings.password;

public enum ScreenLockType {
    NONE(0, "unlock_set_off"),
    SWIPE(0, "unlock_set_none"),
    PATTERN(65536, "unlock_set_pattern"),
    PIN(131072, 196608, "unlock_set_pin"),
    PASSWORD(262144, 393216, "unlock_set_password"),
    MANAGED(524288, "unlock_set_managed");
    
    private static final ScreenLockType MAX_QUALITY = null;
    private static final ScreenLockType MIN_QUALITY = null;
    public final int defaultQuality;
    public final int maxQuality;
    public final String preferenceKey;

    static {
        MIN_QUALITY = NONE;
        MAX_QUALITY = MANAGED;
    }

    private ScreenLockType(int quality, String preferenceKey) {
        this(r7, r8, quality, quality, preferenceKey);
    }

    private ScreenLockType(int defaultQuality, int maxQuality, String preferenceKey) {
        this.defaultQuality = defaultQuality;
        this.maxQuality = maxQuality;
        this.preferenceKey = preferenceKey;
    }

    public static ScreenLockType fromQuality(int quality) {
        if (quality == 0) {
            return SWIPE;
        }
        if (quality == 65536) {
            return PATTERN;
        }
        if (quality == 131072 || quality == 196608) {
            return PIN;
        }
        if (quality == 262144 || quality == 327680 || quality == 393216) {
            return PASSWORD;
        }
        if (quality != 524288) {
            return null;
        }
        return MANAGED;
    }

    public static ScreenLockType fromKey(String key) {
        for (ScreenLockType lock : values()) {
            if (lock.preferenceKey.equals(key)) {
                return lock;
            }
        }
        return null;
    }
}
