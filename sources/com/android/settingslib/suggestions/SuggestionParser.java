package com.android.settingslib.suggestions;

import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.UserInfo;
import android.content.res.Resources.NotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.util.Xml;
import android.view.InflateException;
import com.android.settingslib.drawer.Tile;
import com.android.settingslib.drawer.TileUtils;
import com.oneplus.settings.timer.timepower.SettingsUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class SuggestionParser {
    private static final String DEFAULT_SMART_DISMISS_CONTROL = "0";
    private static final String IS_DISMISSED = "_is_dismissed";
    private static final String META_DATA_ADMIN_USER_TYPE_VALUE = "admin";
    public static final String META_DATA_DISMISS_CONTROL = "com.android.settings.dismiss";
    private static final String META_DATA_GUEST_USER_TYPE_VALUE = "guest";
    private static final String META_DATA_IS_CONNECTION_REQUIRED = "com.android.settings.require_connection";
    private static final String META_DATA_IS_SUPPORTED = "com.android.settings.is_supported";
    private static final String META_DATA_PRIMARY_USER_TYPE_VALUE = "primary";
    private static final String META_DATA_REQUIRE_ACCOUNT = "com.android.settings.require_account";
    public static final String META_DATA_REQUIRE_FEATURE = "com.android.settings.require_feature";
    private static final String META_DATA_REQUIRE_USER_TYPE = "com.android.settings.require_user_type";
    private static final String META_DATA_RESTRICTED_USER_TYPE_VALUE = "restricted";
    public static final String SETUP_TIME = "_setup_time";
    private static final String TAG = "SuggestionParser";
    private final ArrayMap<Pair<String, String>, Tile> mAddCache;
    private final Context mContext;
    private final String mDefaultDismissControl;
    private final SharedPreferences mSharedPrefs;
    private final List<SuggestionCategory> mSuggestionList;

    private static class SuggestionOrderInflater {
        private static final String ATTR_CATEGORY = "category";
        private static final String ATTR_EXCLUSIVE = "exclusive";
        private static final String ATTR_EXCLUSIVE_EXPIRE_DAYS = "exclusiveExpireDays";
        private static final String ATTR_MULTIPLE = "multiple";
        private static final String ATTR_PACKAGE = "package";
        private static final String TAG_ITEM = "step";
        private static final String TAG_LIST = "optional-steps";
        private final Context mContext;

        public SuggestionOrderInflater(Context context) {
            this.mContext = context;
        }

        public Object parse(int resource) {
            int type;
            StringBuilder stringBuilder;
            XmlPullParser parser = this.mContext.getResources().getXml(resource);
            AttributeSet attrs = Xml.asAttributeSet(parser);
            do {
                try {
                    type = parser.next();
                    if (type == 2) {
                        break;
                    }
                } catch (IOException | XmlPullParserException e) {
                    String str = SuggestionParser.TAG;
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("Problem parser resource ");
                    stringBuilder.append(resource);
                    Log.w(str, stringBuilder.toString(), e);
                    return null;
                }
            } while (type != 1);
            if (type == 2) {
                Object xmlRoot = onCreateItem(parser.getName(), attrs);
                rParse(parser, xmlRoot, attrs);
                return xmlRoot;
            }
            stringBuilder = new StringBuilder();
            stringBuilder.append(parser.getPositionDescription());
            stringBuilder.append(": No start tag found!");
            throw new InflateException(stringBuilder.toString());
        }

        private void rParse(XmlPullParser parser, Object parent, AttributeSet attrs) throws XmlPullParserException, IOException {
            int depth = parser.getDepth();
            while (true) {
                int next = parser.next();
                int type = next;
                if ((next == 3 && parser.getDepth() <= depth) || type == 1) {
                    return;
                }
                if (type == 2) {
                    Object item = onCreateItem(parser.getName(), attrs);
                    onAddChildItem(parent, item);
                    rParse(parser, item, attrs);
                }
            }
        }

        /* Access modifiers changed, original: protected */
        public void onAddChildItem(Object parent, Object child) {
            if ((parent instanceof List) && (child instanceof SuggestionCategory)) {
                ((List) parent).add((SuggestionCategory) child);
                return;
            }
            throw new IllegalArgumentException("Parent was not a list");
        }

        /* Access modifiers changed, original: protected */
        public Object onCreateItem(String name, AttributeSet attrs) {
            if (name.equals(TAG_LIST)) {
                return new ArrayList();
            }
            if (name.equals(TAG_ITEM)) {
                long expireDays;
                SuggestionCategory category = new SuggestionCategory();
                category.category = attrs.getAttributeValue(null, ATTR_CATEGORY);
                category.pkg = attrs.getAttributeValue(null, "package");
                String multiple = attrs.getAttributeValue(null, ATTR_MULTIPLE);
                boolean z = false;
                boolean z2 = !TextUtils.isEmpty(multiple) && Boolean.parseBoolean(multiple);
                category.multiple = z2;
                String exclusive = attrs.getAttributeValue(null, ATTR_EXCLUSIVE);
                if (!TextUtils.isEmpty(exclusive) && Boolean.parseBoolean(exclusive)) {
                    z = true;
                }
                category.exclusive = z;
                String expireDaysAttr = attrs.getAttributeValue(null, ATTR_EXCLUSIVE_EXPIRE_DAYS);
                if (TextUtils.isEmpty(expireDaysAttr)) {
                    expireDays = -1;
                } else {
                    expireDays = (long) Integer.parseInt(expireDaysAttr);
                }
                category.exclusiveExpireDaysInMillis = SettingsUtil.MILLIS_OF_DAY * expireDays;
                return category;
            }
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Unknown item ");
            stringBuilder.append(name);
            throw new IllegalArgumentException(stringBuilder.toString());
        }
    }

    public SuggestionParser(Context context, SharedPreferences sharedPrefs, int orderXml, String defaultDismissControl) {
        this(context, sharedPrefs, (List) new SuggestionOrderInflater(context).parse(orderXml), defaultDismissControl);
    }

    public SuggestionParser(Context context, SharedPreferences sharedPrefs, int orderXml) {
        this(context, sharedPrefs, orderXml, DEFAULT_SMART_DISMISS_CONTROL);
    }

    @VisibleForTesting
    public SuggestionParser(Context context, SharedPreferences sharedPrefs, List<SuggestionCategory> suggestionList, String defaultDismissControl) {
        this.mAddCache = new ArrayMap();
        this.mContext = context;
        this.mSuggestionList = suggestionList;
        this.mSharedPrefs = sharedPrefs;
        this.mDefaultDismissControl = defaultDismissControl;
    }

    public SuggestionList getSuggestions(boolean isSmartSuggestionEnabled) {
        SuggestionList suggestionList = new SuggestionList();
        int N = this.mSuggestionList.size();
        for (int i = 0; i < N; i++) {
            SuggestionCategory category = (SuggestionCategory) this.mSuggestionList.get(i);
            List<Tile> suggestions;
            if (!category.exclusive || isExclusiveCategoryExpired(category)) {
                suggestions = new ArrayList();
                readSuggestions(category, suggestions, isSmartSuggestionEnabled);
                suggestionList.addSuggestions(category, suggestions);
            } else {
                suggestions = new ArrayList();
                readSuggestions(category, suggestions, false);
                if (!suggestions.isEmpty()) {
                    SuggestionList exclusiveList = new SuggestionList();
                    exclusiveList.addSuggestions(category, suggestions);
                    return exclusiveList;
                }
            }
        }
        return suggestionList;
    }

    public boolean dismissSuggestion(Tile suggestion) {
        String keyBase = suggestion.intent.getComponent().flattenToShortString();
        Editor edit = this.mSharedPrefs.edit();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(keyBase);
        stringBuilder.append(IS_DISMISSED);
        edit.putBoolean(stringBuilder.toString(), true).commit();
        return true;
    }

    @VisibleForTesting
    public void filterSuggestions(List<Tile> suggestions, int countBefore, boolean isSmartSuggestionEnabled) {
        int i = countBefore;
        while (i < suggestions.size()) {
            if (!isAvailable((Tile) suggestions.get(i)) || !isSupported((Tile) suggestions.get(i)) || !satisifesRequiredUserType((Tile) suggestions.get(i)) || !satisfiesRequiredAccount((Tile) suggestions.get(i)) || !satisfiesConnectivity((Tile) suggestions.get(i)) || isDismissed((Tile) suggestions.get(i), isSmartSuggestionEnabled)) {
                int i2 = i - 1;
                suggestions.remove(i);
                i = i2;
            }
            i++;
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void readSuggestions(SuggestionCategory category, List<Tile> suggestions, boolean isSmartSuggestionEnabled) {
        SuggestionCategory suggestionCategory = category;
        List<Tile> list = suggestions;
        int countBefore = suggestions.size();
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory(suggestionCategory.category);
        if (suggestionCategory.pkg != null) {
            intent.setPackage(suggestionCategory.pkg);
        }
        TileUtils.getTilesForIntent(this.mContext, new UserHandle(UserHandle.myUserId()), intent, this.mAddCache, null, list, true, false, false, true);
        filterSuggestions(list, countBefore, isSmartSuggestionEnabled);
        if (!suggestionCategory.multiple && suggestions.size() > countBefore + 1) {
            Tile item = (Tile) list.remove(suggestions.size() - 1);
            while (suggestions.size() > countBefore) {
                Tile last = (Tile) list.remove(suggestions.size() - 1);
                if (last.priority > item.priority) {
                    item = last;
                }
            }
            if (!isCategoryDone(suggestionCategory.category)) {
                list.add(item);
            }
        }
    }

    private boolean isAvailable(Tile suggestion) {
        String featuresRequired = suggestion.metaData.getString(META_DATA_REQUIRE_FEATURE);
        if (featuresRequired != null) {
            for (String feature : featuresRequired.split(",")) {
                if (TextUtils.isEmpty(feature)) {
                    String str = TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Found empty substring when parsing required features: ");
                    stringBuilder.append(featuresRequired);
                    Log.w(str, stringBuilder.toString());
                } else if (!this.mContext.getPackageManager().hasSystemFeature(feature)) {
                    String str2 = TAG;
                    StringBuilder stringBuilder2 = new StringBuilder();
                    stringBuilder2.append(suggestion.title);
                    stringBuilder2.append(" requires unavailable feature ");
                    stringBuilder2.append(feature);
                    Log.i(str2, stringBuilder2.toString());
                    return false;
                }
            }
        }
        return true;
    }

    private boolean satisifesRequiredUserType(Tile suggestion) {
        String requiredUser = suggestion.metaData.getString(META_DATA_REQUIRE_USER_TYPE);
        if (requiredUser == null) {
            return true;
        }
        UserInfo userInfo = ((UserManager) this.mContext.getSystemService(UserManager.class)).getUserInfo(UserHandle.myUserId());
        for (String userType : requiredUser.split("\\|")) {
            boolean primaryUserCondtionMet = userInfo.isPrimary() && META_DATA_PRIMARY_USER_TYPE_VALUE.equals(userType);
            boolean adminUserConditionMet = userInfo.isAdmin() && META_DATA_ADMIN_USER_TYPE_VALUE.equals(userType);
            boolean guestUserCondtionMet = userInfo.isGuest() && META_DATA_GUEST_USER_TYPE_VALUE.equals(userType);
            boolean restrictedUserCondtionMet = userInfo.isRestricted() && META_DATA_RESTRICTED_USER_TYPE_VALUE.equals(userType);
            if (primaryUserCondtionMet || adminUserConditionMet || guestUserCondtionMet || restrictedUserCondtionMet) {
                return true;
            }
        }
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(suggestion.title);
        stringBuilder.append(" requires user type ");
        stringBuilder.append(requiredUser);
        Log.i(str, stringBuilder.toString());
        return false;
    }

    public boolean satisfiesRequiredAccount(Tile suggestion) {
        String requiredAccountType = suggestion.metaData.getString(META_DATA_REQUIRE_ACCOUNT);
        boolean satisfiesRequiredAccount = true;
        if (requiredAccountType == null) {
            return true;
        }
        if (((AccountManager) this.mContext.getSystemService(AccountManager.class)).getAccountsByType(requiredAccountType).length <= 0) {
            satisfiesRequiredAccount = false;
        }
        if (!satisfiesRequiredAccount) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(suggestion.title);
            stringBuilder.append(" requires unavailable account type ");
            stringBuilder.append(requiredAccountType);
            Log.i(str, stringBuilder.toString());
        }
        return satisfiesRequiredAccount;
    }

    public boolean isSupported(Tile suggestion) {
        String str;
        StringBuilder stringBuilder;
        int isSupportedResource = suggestion.metaData.getInt(META_DATA_IS_SUPPORTED);
        try {
            if (suggestion.intent == null) {
                return false;
            }
            boolean isSupported = isSupportedResource != 0 ? this.mContext.getPackageManager().getResourcesForActivity(suggestion.intent.getComponent()).getBoolean(isSupportedResource) : true;
            if (!isSupported) {
                String str2 = TAG;
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append(suggestion.title);
                stringBuilder2.append(" requires unsupported resource ");
                stringBuilder2.append(isSupportedResource);
                Log.i(str2, stringBuilder2.toString());
            }
            return isSupported;
        } catch (NameNotFoundException e) {
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("Cannot find resources for ");
            stringBuilder.append(suggestion.intent.getComponent());
            Log.w(str, stringBuilder.toString());
            return false;
        } catch (NotFoundException e2) {
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("Cannot find resources for ");
            stringBuilder.append(suggestion.intent.getComponent());
            Log.w(str, stringBuilder.toString(), e2);
            return false;
        }
    }

    private boolean satisfiesConnectivity(Tile suggestion) {
        boolean satisfiesConnectivity = true;
        if (!suggestion.metaData.getBoolean(META_DATA_IS_CONNECTION_REQUIRED)) {
            return true;
        }
        NetworkInfo netInfo = ((ConnectivityManager) this.mContext.getSystemService("connectivity")).getActiveNetworkInfo();
        if (netInfo == null || !netInfo.isConnectedOrConnecting()) {
            satisfiesConnectivity = false;
        }
        if (!satisfiesConnectivity) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(suggestion.title);
            stringBuilder.append(" is missing required connection.");
            Log.i(str, stringBuilder.toString());
        }
        return satisfiesConnectivity;
    }

    public boolean isCategoryDone(String category) {
        String name = new StringBuilder();
        name.append("suggested.completed_category.");
        name.append(category);
        return Secure.getInt(this.mContext.getContentResolver(), name.toString(), 0) != 0;
    }

    public void markCategoryDone(String category) {
        String name = new StringBuilder();
        name.append("suggested.completed_category.");
        name.append(category);
        Secure.putInt(this.mContext.getContentResolver(), name.toString(), 1);
    }

    private boolean isExclusiveCategoryExpired(SuggestionCategory category) {
        String keySetupTime = new StringBuilder();
        keySetupTime.append(category.category);
        keySetupTime.append(SETUP_TIME);
        keySetupTime = keySetupTime.toString();
        long currentTime = System.currentTimeMillis();
        if (!this.mSharedPrefs.contains(keySetupTime)) {
            this.mSharedPrefs.edit().putLong(keySetupTime, currentTime).commit();
        }
        boolean z = false;
        if (category.exclusiveExpireDaysInMillis < 0) {
            return false;
        }
        long elapsedTime = currentTime - this.mSharedPrefs.getLong(keySetupTime, 0);
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Day ");
        stringBuilder.append(elapsedTime / SettingsUtil.MILLIS_OF_DAY);
        stringBuilder.append(" for ");
        stringBuilder.append(category.category);
        Log.d(str, stringBuilder.toString());
        if (elapsedTime > category.exclusiveExpireDaysInMillis) {
            z = true;
        }
        return z;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public boolean isDismissed(Tile suggestion, boolean isSmartSuggestionEnabled) {
        String dismissControl = getDismissControl(suggestion, isSmartSuggestionEnabled);
        String keyBase = suggestion.intent.getComponent().flattenToShortString();
        SharedPreferences sharedPreferences = this.mSharedPrefs;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(keyBase);
        stringBuilder.append(SETUP_TIME);
        if (!sharedPreferences.contains(stringBuilder.toString())) {
            Editor edit = this.mSharedPrefs.edit();
            stringBuilder = new StringBuilder();
            stringBuilder.append(keyBase);
            stringBuilder.append(SETUP_TIME);
            edit.putLong(stringBuilder.toString(), System.currentTimeMillis()).commit();
        }
        boolean isDismissed = this.mSharedPrefs;
        stringBuilder = new StringBuilder();
        stringBuilder.append(keyBase);
        stringBuilder.append(IS_DISMISSED);
        if (isDismissed.getBoolean(stringBuilder.toString(), false)) {
            return true;
        }
        if (dismissControl == null) {
            return false;
        }
        int firstAppearDay = parseDismissString(dismissControl);
        SharedPreferences sharedPreferences2 = this.mSharedPrefs;
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append(keyBase);
        stringBuilder2.append(SETUP_TIME);
        if (System.currentTimeMillis() < getEndTime(sharedPreferences2.getLong(stringBuilder2.toString(), 0), firstAppearDay)) {
            return true;
        }
        Editor edit2 = this.mSharedPrefs.edit();
        StringBuilder stringBuilder3 = new StringBuilder();
        stringBuilder3.append(keyBase);
        stringBuilder3.append(IS_DISMISSED);
        edit2.putBoolean(stringBuilder3.toString(), false).commit();
        return false;
    }

    private long getEndTime(long startTime, int daysDelay) {
        return startTime + (((long) daysDelay) * SettingsUtil.MILLIS_OF_DAY);
    }

    private int parseDismissString(String dismissControl) {
        return Integer.parseInt(dismissControl.split(",")[0]);
    }

    private String getDismissControl(Tile suggestion, boolean isSmartSuggestionEnabled) {
        if (isSmartSuggestionEnabled) {
            return this.mDefaultDismissControl;
        }
        return suggestion.metaData.getString(META_DATA_DISMISS_CONTROL);
    }
}
