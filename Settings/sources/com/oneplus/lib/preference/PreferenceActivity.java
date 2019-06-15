package com.oneplus.lib.preference;

import android.app.Fragment;
import android.app.FragmentBreadCrumbs;
import android.app.FragmentTransaction;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.oneplus.commonctrl.R;
import com.oneplus.lib.preference.PreferenceFragment.OnPreferenceStartFragmentCallback;
import com.oneplus.lib.preference.PreferenceManager.OnPreferenceTreeClickListener;
import java.util.ArrayList;
import java.util.List;

public abstract class PreferenceActivity extends ListActivity implements OnPreferenceTreeClickListener, OnPreferenceStartFragmentCallback {
    private static final String BACK_STACK_PREFS = ":android:prefs";
    private static final String CUR_HEADER_TAG = ":android:cur_header";
    public static final String EXTRA_NO_HEADERS = ":android:no_headers";
    private static final String EXTRA_PREFS_SET_BACK_TEXT = "extra_prefs_set_back_text";
    private static final String EXTRA_PREFS_SET_NEXT_TEXT = "extra_prefs_set_next_text";
    private static final String EXTRA_PREFS_SHOW_BUTTON_BAR = "extra_prefs_show_button_bar";
    private static final String EXTRA_PREFS_SHOW_SKIP = "extra_prefs_show_skip";
    public static final String EXTRA_SHOW_FRAGMENT = ":android:show_fragment";
    public static final String EXTRA_SHOW_FRAGMENT_ARGUMENTS = ":android:show_fragment_args";
    public static final String EXTRA_SHOW_FRAGMENT_SHORT_TITLE = ":android:show_fragment_short_title";
    public static final String EXTRA_SHOW_FRAGMENT_TITLE = ":android:show_fragment_title";
    private static final int FIRST_REQUEST_CODE = 100;
    private static final String HEADERS_TAG = ":android:headers";
    public static final long HEADER_ID_UNDEFINED = -1;
    private static final int MSG_BIND_PREFERENCES = 1;
    private static final int MSG_BUILD_HEADERS = 2;
    private static final String PREFERENCES_TAG = ":android:preferences";
    private static final String TAG = "PreferenceActivity";
    private Header mCurHeader;
    private FragmentBreadCrumbs mFragmentBreadCrumbs;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    PreferenceActivity.this.bindPreferences();
                    return;
                case 2:
                    ArrayList<Header> oldHeaders = new ArrayList(PreferenceActivity.this.mHeaders);
                    PreferenceActivity.this.mHeaders.clear();
                    PreferenceActivity.this.onBuildHeaders(PreferenceActivity.this.mHeaders);
                    if (PreferenceActivity.this.getListAdapter() instanceof BaseAdapter) {
                        ((BaseAdapter) PreferenceActivity.this.getListAdapter()).notifyDataSetChanged();
                    }
                    Header header = PreferenceActivity.this.onGetNewHeader();
                    Header mappedHeader;
                    if (header != null && header.fragment != null) {
                        mappedHeader = PreferenceActivity.this.findBestMatchingHeader(header, oldHeaders);
                        if (mappedHeader == null || PreferenceActivity.this.mCurHeader != mappedHeader) {
                            PreferenceActivity.this.switchToHeader(header);
                            return;
                        }
                        return;
                    } else if (PreferenceActivity.this.mCurHeader != null) {
                        mappedHeader = PreferenceActivity.this.findBestMatchingHeader(PreferenceActivity.this.mCurHeader, PreferenceActivity.this.mHeaders);
                        if (mappedHeader != null) {
                            PreferenceActivity.this.setSelectedHeader(mappedHeader);
                            return;
                        }
                        return;
                    } else {
                        return;
                    }
                default:
                    return;
            }
        }
    };
    private final ArrayList<Header> mHeaders = new ArrayList();
    private FrameLayout mListFooter;
    private Button mNextButton;
    private int mPreferenceHeaderItemResId = 0;
    private boolean mPreferenceHeaderRemoveEmptyIcon = false;
    private PreferenceManager mPreferenceManager;
    private ViewGroup mPrefsContainer;
    private Bundle mSavedInstanceState;
    private boolean mSinglePane;

    public static final class Header implements Parcelable {
        public static final Creator<Header> CREATOR = new Creator<Header>() {
            public Header createFromParcel(Parcel source) {
                return new Header(source);
            }

            public Header[] newArray(int size) {
                return new Header[size];
            }
        };
        public CharSequence breadCrumbShortTitle;
        public int breadCrumbShortTitleRes;
        public CharSequence breadCrumbTitle;
        public int breadCrumbTitleRes;
        public Bundle extras;
        public String fragment;
        public Bundle fragmentArguments;
        public int iconRes;
        public long id = -1;
        public Intent intent;
        public CharSequence summary;
        public int summaryRes;
        public CharSequence title;
        public int titleRes;

        public CharSequence getTitle(Resources res) {
            if (this.titleRes != 0) {
                return res.getText(this.titleRes);
            }
            return this.title;
        }

        public CharSequence getSummary(Resources res) {
            if (this.summaryRes != 0) {
                return res.getText(this.summaryRes);
            }
            return this.summary;
        }

        public CharSequence getBreadCrumbTitle(Resources res) {
            if (this.breadCrumbTitleRes != 0) {
                return res.getText(this.breadCrumbTitleRes);
            }
            return this.breadCrumbTitle;
        }

        public CharSequence getBreadCrumbShortTitle(Resources res) {
            if (this.breadCrumbShortTitleRes != 0) {
                return res.getText(this.breadCrumbShortTitleRes);
            }
            return this.breadCrumbShortTitle;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(this.id);
            dest.writeInt(this.titleRes);
            TextUtils.writeToParcel(this.title, dest, flags);
            dest.writeInt(this.summaryRes);
            TextUtils.writeToParcel(this.summary, dest, flags);
            dest.writeInt(this.breadCrumbTitleRes);
            TextUtils.writeToParcel(this.breadCrumbTitle, dest, flags);
            dest.writeInt(this.breadCrumbShortTitleRes);
            TextUtils.writeToParcel(this.breadCrumbShortTitle, dest, flags);
            dest.writeInt(this.iconRes);
            dest.writeString(this.fragment);
            dest.writeBundle(this.fragmentArguments);
            if (this.intent != null) {
                dest.writeInt(1);
                this.intent.writeToParcel(dest, flags);
            } else {
                dest.writeInt(0);
            }
            dest.writeBundle(this.extras);
        }

        public void readFromParcel(Parcel in) {
            this.id = in.readLong();
            this.titleRes = in.readInt();
            this.title = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
            this.summaryRes = in.readInt();
            this.summary = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
            this.breadCrumbTitleRes = in.readInt();
            this.breadCrumbTitle = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
            this.breadCrumbShortTitleRes = in.readInt();
            this.breadCrumbShortTitle = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
            this.iconRes = in.readInt();
            this.fragment = in.readString();
            this.fragmentArguments = in.readBundle();
            if (in.readInt() != 0) {
                this.intent = (Intent) Intent.CREATOR.createFromParcel(in);
            }
            this.extras = in.readBundle();
        }

        Header(Parcel in) {
            readFromParcel(in);
        }
    }

    private static class HeaderAdapter extends ArrayAdapter<Header> {
        private LayoutInflater mInflater;
        private int mLayoutResId;
        private boolean mRemoveIconIfEmpty;

        private static class HeaderViewHolder {
            ImageView icon;
            TextView summary;
            TextView title;

            private HeaderViewHolder() {
            }

            /* synthetic */ HeaderViewHolder(AnonymousClass1 x0) {
                this();
            }
        }

        public HeaderAdapter(Context context, List<Header> objects, int layoutResId, boolean removeIconBehavior) {
            super(context, 0, objects);
            this.mInflater = (LayoutInflater) context.getSystemService("layout_inflater");
            this.mLayoutResId = layoutResId;
            this.mRemoveIconIfEmpty = removeIconBehavior;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            HeaderViewHolder holder;
            if (convertView == null) {
                view = this.mInflater.inflate(this.mLayoutResId, parent, false);
                holder = new HeaderViewHolder();
                holder.icon = (ImageView) view.findViewById(16908294);
                holder.title = (TextView) view.findViewById(16908310);
                holder.summary = (TextView) view.findViewById(16908304);
                view.setTag(holder);
            } else {
                view = convertView;
                holder = (HeaderViewHolder) view.getTag();
            }
            Header header = (Header) getItem(position);
            if (!this.mRemoveIconIfEmpty) {
                holder.icon.setImageResource(header.iconRes);
            } else if (header.iconRes == 0) {
                holder.icon.setVisibility(8);
            } else {
                holder.icon.setVisibility(0);
                holder.icon.setImageResource(header.iconRes);
            }
            holder.title.setText(header.getTitle(getContext().getResources()));
            CharSequence summary = header.getSummary(getContext().getResources());
            if (TextUtils.isEmpty(summary)) {
                holder.summary.setVisibility(8);
            } else {
                holder.summary.setVisibility(0);
                holder.summary.setText(summary);
            }
            return view;
        }
    }

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CharSequence initialShortTitleStr = null;
        TypedArray sa = obtainStyledAttributes(null, R.styleable.PreferenceActivity, R.attr.op_preferenceActivityStyle, 0);
        int layoutResId = sa.getResourceId(R.styleable.PreferenceActivity_android_layout, R.layout.preference_list_content);
        this.mPreferenceHeaderItemResId = sa.getResourceId(R.styleable.PreferenceActivity_oneplusHeaderLayout, R.layout.preference_header_item);
        this.mPreferenceHeaderRemoveEmptyIcon = sa.getBoolean(R.styleable.PreferenceActivity_headerRemoveIconIfEmpty, false);
        sa.recycle();
        setContentView(layoutResId);
        this.mListFooter = (FrameLayout) findViewById(R.id.list_footer);
        this.mPrefsContainer = (ViewGroup) findViewById(R.id.prefs_frame);
        boolean z = onIsHidingHeaders() || !onIsMultiPane();
        this.mSinglePane = z;
        String initialFragment = getIntent().getStringExtra(EXTRA_SHOW_FRAGMENT);
        Bundle initialArguments = getIntent().getBundleExtra(EXTRA_SHOW_FRAGMENT_ARGUMENTS);
        int initialTitle = getIntent().getIntExtra(EXTRA_SHOW_FRAGMENT_TITLE, 0);
        int initialShortTitle = getIntent().getIntExtra(EXTRA_SHOW_FRAGMENT_SHORT_TITLE, 0);
        if (savedInstanceState != null) {
            ArrayList<Header> headers = savedInstanceState.getParcelableArrayList(HEADERS_TAG);
            if (headers != null) {
                this.mHeaders.addAll(headers);
                int curHeader = savedInstanceState.getInt(CUR_HEADER_TAG, -1);
                if (curHeader >= 0 && curHeader < this.mHeaders.size()) {
                    setSelectedHeader((Header) this.mHeaders.get(curHeader));
                }
            }
        } else if (initialFragment == null || !this.mSinglePane) {
            onBuildHeaders(this.mHeaders);
            if (this.mHeaders.size() > 0 && !this.mSinglePane) {
                if (initialFragment == null) {
                    switchToHeader(onGetInitialHeader());
                } else {
                    switchToHeader(initialFragment, initialArguments);
                }
            }
        } else {
            switchToHeader(initialFragment, initialArguments);
            if (initialTitle != 0) {
                showBreadCrumbs(getText(initialTitle), initialShortTitle != 0 ? getText(initialShortTitle) : null);
            }
        }
        if (initialFragment != null && this.mSinglePane) {
            findViewById(R.id.headers).setVisibility(8);
            this.mPrefsContainer.setVisibility(0);
            if (initialTitle != 0) {
                CharSequence initialTitleStr = getText(initialTitle);
                if (initialShortTitle != 0) {
                    initialShortTitleStr = getText(initialShortTitle);
                }
                showBreadCrumbs(initialTitleStr, initialShortTitleStr);
            }
        } else if (this.mHeaders.size() > 0) {
            setListAdapter(new HeaderAdapter(this, this.mHeaders, this.mPreferenceHeaderItemResId, this.mPreferenceHeaderRemoveEmptyIcon));
            if (!this.mSinglePane) {
                getListView().setChoiceMode(1);
                if (this.mCurHeader != null) {
                    setSelectedHeader(this.mCurHeader);
                }
                this.mPrefsContainer.setVisibility(0);
            }
        } else {
            setContentView(R.layout.preference_list_content_single);
            this.mListFooter = (FrameLayout) findViewById(R.id.list_footer);
            this.mPrefsContainer = (ViewGroup) findViewById(R.id.prefs);
            this.mPreferenceManager = new PreferenceManager(this, 100);
            this.mPreferenceManager.setOnPreferenceTreeClickListener(this);
        }
        Intent intent = getIntent();
        if (intent.getBooleanExtra(EXTRA_PREFS_SHOW_BUTTON_BAR, false)) {
            String buttonText;
            findViewById(R.id.button_bar).setVisibility(0);
            Button backButton = (Button) findViewById(R.id.back_button);
            backButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    PreferenceActivity.this.setResult(0);
                    PreferenceActivity.this.finish();
                }
            });
            Button skipButton = (Button) findViewById(R.id.skip_button);
            skipButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    PreferenceActivity.this.setResult(-1);
                    PreferenceActivity.this.finish();
                }
            });
            this.mNextButton = (Button) findViewById(R.id.next_button);
            this.mNextButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    PreferenceActivity.this.setResult(-1);
                    PreferenceActivity.this.finish();
                }
            });
            if (intent.hasExtra(EXTRA_PREFS_SET_NEXT_TEXT)) {
                buttonText = intent.getStringExtra(EXTRA_PREFS_SET_NEXT_TEXT);
                if (TextUtils.isEmpty(buttonText)) {
                    this.mNextButton.setVisibility(8);
                } else {
                    this.mNextButton.setText(buttonText);
                }
            }
            if (intent.hasExtra(EXTRA_PREFS_SET_BACK_TEXT)) {
                buttonText = intent.getStringExtra(EXTRA_PREFS_SET_BACK_TEXT);
                if (TextUtils.isEmpty(buttonText)) {
                    backButton.setVisibility(8);
                } else {
                    backButton.setText(buttonText);
                }
            }
            if (intent.getBooleanExtra(EXTRA_PREFS_SHOW_SKIP, false)) {
                skipButton.setVisibility(0);
            }
        }
    }

    public boolean hasHeaders() {
        return getListView().getVisibility() == 0 && this.mPreferenceManager == null;
    }

    public List<Header> getHeaders() {
        return this.mHeaders;
    }

    public boolean isMultiPane() {
        return hasHeaders() && this.mPrefsContainer.getVisibility() == 0;
    }

    public boolean onIsMultiPane() {
        return getResources().getBoolean(R.bool.preferences_prefer_dual_pane);
    }

    public boolean onIsHidingHeaders() {
        return getIntent().getBooleanExtra(EXTRA_NO_HEADERS, false);
    }

    public Header onGetInitialHeader() {
        for (int i = 0; i < this.mHeaders.size(); i++) {
            Header h = (Header) this.mHeaders.get(i);
            if (h.fragment != null) {
                return h;
            }
        }
        throw new IllegalStateException("Must have at least one header with a fragment");
    }

    public Header onGetNewHeader() {
        return null;
    }

    public void onBuildHeaders(List<Header> list) {
    }

    public void invalidateHeaders() {
        if (!this.mHandler.hasMessages(2)) {
            this.mHandler.sendEmptyMessage(2);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:129:0x01c8  */
    /* JADX WARNING: Removed duplicated region for block: B:129:0x01c8  */
    /* JADX WARNING: Removed duplicated region for block: B:129:0x01c8  */
    public void loadHeadersFromResource(int r18, java.util.List<com.oneplus.lib.preference.PreferenceActivity.Header> r19) {
        /*
        r17 = this;
        r0 = 0;
        r1 = r0;
        r0 = r17.getResources();	 Catch:{ XmlPullParserException -> 0x01b6, IOException -> 0x01a7, all -> 0x019f }
        r2 = r18;
        r0 = r0.getXml(r2);	 Catch:{ XmlPullParserException -> 0x019b, IOException -> 0x0197, all -> 0x0193 }
        r1 = r0;
        r0 = android.util.Xml.asAttributeSet(r1);	 Catch:{ XmlPullParserException -> 0x019b, IOException -> 0x0197, all -> 0x0193 }
    L_0x0011:
        r3 = r1.next();	 Catch:{ XmlPullParserException -> 0x019b, IOException -> 0x0197, all -> 0x0193 }
        r4 = r3;
        r5 = 1;
        if (r3 == r5) goto L_0x001d;
    L_0x0019:
        r3 = 2;
        if (r4 == r3) goto L_0x001d;
    L_0x001c:
        goto L_0x0011;
    L_0x001d:
        r3 = r1.getName();	 Catch:{ XmlPullParserException -> 0x019b, IOException -> 0x0197, all -> 0x0193 }
        r6 = "preference-headers";
        r6 = r6.equals(r3);	 Catch:{ XmlPullParserException -> 0x019b, IOException -> 0x0197, all -> 0x0193 }
        if (r6 == 0) goto L_0x0168;
    L_0x0029:
        r6 = 0;
        r7 = r1.getDepth();	 Catch:{ XmlPullParserException -> 0x019b, IOException -> 0x0197, all -> 0x0193 }
    L_0x002e:
        r8 = r1.next();	 Catch:{ XmlPullParserException -> 0x019b, IOException -> 0x0197, all -> 0x0193 }
        r4 = r8;
        if (r8 == r5) goto L_0x015e;
    L_0x0035:
        r8 = 3;
        if (r4 != r8) goto L_0x003e;
    L_0x0038:
        r9 = r1.getDepth();	 Catch:{ XmlPullParserException -> 0x019b, IOException -> 0x0197, all -> 0x0193 }
        if (r9 <= r7) goto L_0x015e;
    L_0x003e:
        if (r4 == r8) goto L_0x0157;
    L_0x0040:
        r9 = 4;
        if (r4 != r9) goto L_0x0045;
    L_0x0043:
        goto L_0x0157;
    L_0x0045:
        r10 = r1.getName();	 Catch:{ XmlPullParserException -> 0x019b, IOException -> 0x0197, all -> 0x0193 }
        r3 = r10;
        r10 = "header";
        r10 = r10.equals(r3);	 Catch:{ XmlPullParserException -> 0x019b, IOException -> 0x0197, all -> 0x0193 }
        if (r10 == 0) goto L_0x014f;
    L_0x0052:
        r10 = new com.oneplus.lib.preference.PreferenceActivity$Header;	 Catch:{ XmlPullParserException -> 0x019b, IOException -> 0x0197, all -> 0x0193 }
        r10.<init>();	 Catch:{ XmlPullParserException -> 0x019b, IOException -> 0x0197, all -> 0x0193 }
        r11 = com.oneplus.commonctrl.R.styleable.PreferenceHeader;	 Catch:{ XmlPullParserException -> 0x019b, IOException -> 0x0197, all -> 0x0193 }
        r12 = r17;
        r11 = r12.obtainStyledAttributes(r0, r11);	 Catch:{ XmlPullParserException -> 0x014c, IOException -> 0x0149, all -> 0x0147 }
        r13 = com.oneplus.commonctrl.R.styleable.PreferenceHeader_android_id;	 Catch:{ XmlPullParserException -> 0x014c, IOException -> 0x0149, all -> 0x0147 }
        r14 = -1;
        r13 = r11.getResourceId(r13, r14);	 Catch:{ XmlPullParserException -> 0x014c, IOException -> 0x0149, all -> 0x0147 }
        r13 = (long) r13;	 Catch:{ XmlPullParserException -> 0x014c, IOException -> 0x0149, all -> 0x0147 }
        r10.id = r13;	 Catch:{ XmlPullParserException -> 0x014c, IOException -> 0x0149, all -> 0x0147 }
        r13 = com.oneplus.commonctrl.R.styleable.PreferenceHeader_android_title;	 Catch:{ XmlPullParserException -> 0x014c, IOException -> 0x0149, all -> 0x0147 }
        r13 = r11.peekValue(r13);	 Catch:{ XmlPullParserException -> 0x014c, IOException -> 0x0149, all -> 0x0147 }
        if (r13 == 0) goto L_0x0082;
    L_0x0071:
        r14 = r13.type;	 Catch:{ XmlPullParserException -> 0x014c, IOException -> 0x0149, all -> 0x0147 }
        if (r14 != r8) goto L_0x0082;
    L_0x0075:
        r14 = r13.resourceId;	 Catch:{ XmlPullParserException -> 0x014c, IOException -> 0x0149, all -> 0x0147 }
        if (r14 == 0) goto L_0x007e;
    L_0x0079:
        r14 = r13.resourceId;	 Catch:{ XmlPullParserException -> 0x014c, IOException -> 0x0149, all -> 0x0147 }
        r10.titleRes = r14;	 Catch:{ XmlPullParserException -> 0x014c, IOException -> 0x0149, all -> 0x0147 }
        goto L_0x0082;
    L_0x007e:
        r14 = r13.string;	 Catch:{ XmlPullParserException -> 0x014c, IOException -> 0x0149, all -> 0x0147 }
        r10.title = r14;	 Catch:{ XmlPullParserException -> 0x014c, IOException -> 0x0149, all -> 0x0147 }
    L_0x0082:
        r14 = com.oneplus.commonctrl.R.styleable.PreferenceHeader_android_summary;	 Catch:{ XmlPullParserException -> 0x014c, IOException -> 0x0149, all -> 0x0147 }
        r14 = r11.peekValue(r14);	 Catch:{ XmlPullParserException -> 0x014c, IOException -> 0x0149, all -> 0x0147 }
        r13 = r14;
        if (r13 == 0) goto L_0x009c;
    L_0x008b:
        r14 = r13.type;	 Catch:{ XmlPullParserException -> 0x014c, IOException -> 0x0149, all -> 0x0147 }
        if (r14 != r8) goto L_0x009c;
    L_0x008f:
        r14 = r13.resourceId;	 Catch:{ XmlPullParserException -> 0x014c, IOException -> 0x0149, all -> 0x0147 }
        if (r14 == 0) goto L_0x0098;
    L_0x0093:
        r14 = r13.resourceId;	 Catch:{ XmlPullParserException -> 0x014c, IOException -> 0x0149, all -> 0x0147 }
        r10.summaryRes = r14;	 Catch:{ XmlPullParserException -> 0x014c, IOException -> 0x0149, all -> 0x0147 }
        goto L_0x009c;
    L_0x0098:
        r14 = r13.string;	 Catch:{ XmlPullParserException -> 0x014c, IOException -> 0x0149, all -> 0x0147 }
        r10.summary = r14;	 Catch:{ XmlPullParserException -> 0x014c, IOException -> 0x0149, all -> 0x0147 }
    L_0x009c:
        r14 = com.oneplus.commonctrl.R.styleable.PreferenceHeader_android_breadCrumbTitle;	 Catch:{ XmlPullParserException -> 0x014c, IOException -> 0x0149, all -> 0x0147 }
        r14 = r11.peekValue(r14);	 Catch:{ XmlPullParserException -> 0x014c, IOException -> 0x0149, all -> 0x0147 }
        r13 = r14;
        if (r13 == 0) goto L_0x00b6;
    L_0x00a5:
        r14 = r13.type;	 Catch:{ XmlPullParserException -> 0x014c, IOException -> 0x0149, all -> 0x0147 }
        if (r14 != r8) goto L_0x00b6;
    L_0x00a9:
        r14 = r13.resourceId;	 Catch:{ XmlPullParserException -> 0x014c, IOException -> 0x0149, all -> 0x0147 }
        if (r14 == 0) goto L_0x00b2;
    L_0x00ad:
        r14 = r13.resourceId;	 Catch:{ XmlPullParserException -> 0x014c, IOException -> 0x0149, all -> 0x0147 }
        r10.breadCrumbTitleRes = r14;	 Catch:{ XmlPullParserException -> 0x014c, IOException -> 0x0149, all -> 0x0147 }
        goto L_0x00b6;
    L_0x00b2:
        r14 = r13.string;	 Catch:{ XmlPullParserException -> 0x014c, IOException -> 0x0149, all -> 0x0147 }
        r10.breadCrumbTitle = r14;	 Catch:{ XmlPullParserException -> 0x014c, IOException -> 0x0149, all -> 0x0147 }
    L_0x00b6:
        r14 = com.oneplus.commonctrl.R.styleable.PreferenceHeader_android_breadCrumbShortTitle;	 Catch:{ XmlPullParserException -> 0x014c, IOException -> 0x0149, all -> 0x0147 }
        r14 = r11.peekValue(r14);	 Catch:{ XmlPullParserException -> 0x014c, IOException -> 0x0149, all -> 0x0147 }
        r13 = r14;
        if (r13 == 0) goto L_0x00d0;
    L_0x00bf:
        r14 = r13.type;	 Catch:{ XmlPullParserException -> 0x014c, IOException -> 0x0149, all -> 0x0147 }
        if (r14 != r8) goto L_0x00d0;
    L_0x00c3:
        r14 = r13.resourceId;	 Catch:{ XmlPullParserException -> 0x014c, IOException -> 0x0149, all -> 0x0147 }
        if (r14 == 0) goto L_0x00cc;
    L_0x00c7:
        r14 = r13.resourceId;	 Catch:{ XmlPullParserException -> 0x014c, IOException -> 0x0149, all -> 0x0147 }
        r10.breadCrumbShortTitleRes = r14;	 Catch:{ XmlPullParserException -> 0x014c, IOException -> 0x0149, all -> 0x0147 }
        goto L_0x00d0;
    L_0x00cc:
        r14 = r13.string;	 Catch:{ XmlPullParserException -> 0x014c, IOException -> 0x0149, all -> 0x0147 }
        r10.breadCrumbShortTitle = r14;	 Catch:{ XmlPullParserException -> 0x014c, IOException -> 0x0149, all -> 0x0147 }
    L_0x00d0:
        r14 = com.oneplus.commonctrl.R.styleable.PreferenceHeader_android_icon;	 Catch:{ XmlPullParserException -> 0x014c, IOException -> 0x0149, all -> 0x0147 }
        r15 = 0;
        r14 = r11.getResourceId(r14, r15);	 Catch:{ XmlPullParserException -> 0x014c, IOException -> 0x0149, all -> 0x0147 }
        r10.iconRes = r14;	 Catch:{ XmlPullParserException -> 0x014c, IOException -> 0x0149, all -> 0x0147 }
        r14 = com.oneplus.commonctrl.R.styleable.PreferenceHeader_android_fragment;	 Catch:{ XmlPullParserException -> 0x014c, IOException -> 0x0149, all -> 0x0147 }
        r14 = r11.getString(r14);	 Catch:{ XmlPullParserException -> 0x014c, IOException -> 0x0149, all -> 0x0147 }
        r10.fragment = r14;	 Catch:{ XmlPullParserException -> 0x014c, IOException -> 0x0149, all -> 0x0147 }
        r11.recycle();	 Catch:{ XmlPullParserException -> 0x014c, IOException -> 0x0149, all -> 0x0147 }
        if (r6 != 0) goto L_0x00ec;
    L_0x00e6:
        r14 = new android.os.Bundle;	 Catch:{ XmlPullParserException -> 0x014c, IOException -> 0x0149, all -> 0x0147 }
        r14.<init>();	 Catch:{ XmlPullParserException -> 0x014c, IOException -> 0x0149, all -> 0x0147 }
        r6 = r14;
    L_0x00ec:
        r14 = r1.getDepth();	 Catch:{ XmlPullParserException -> 0x014c, IOException -> 0x0149, all -> 0x0147 }
    L_0x00f0:
        r15 = r1.next();	 Catch:{ XmlPullParserException -> 0x014c, IOException -> 0x0149, all -> 0x0147 }
        r4 = r15;
        if (r15 == r5) goto L_0x0137;
    L_0x00f7:
        if (r4 != r8) goto L_0x00ff;
    L_0x00f9:
        r15 = r1.getDepth();	 Catch:{ XmlPullParserException -> 0x014c, IOException -> 0x0149, all -> 0x0147 }
        if (r15 <= r14) goto L_0x0137;
    L_0x00ff:
        if (r4 == r8) goto L_0x0134;
    L_0x0101:
        if (r4 != r9) goto L_0x0104;
    L_0x0103:
        goto L_0x0134;
    L_0x0104:
        r15 = r1.getName();	 Catch:{ XmlPullParserException -> 0x014c, IOException -> 0x0149, all -> 0x0147 }
        r5 = "extra";
        r5 = r15.equals(r5);	 Catch:{ XmlPullParserException -> 0x014c, IOException -> 0x0149, all -> 0x0147 }
        if (r5 == 0) goto L_0x011d;
    L_0x0110:
        r5 = r17.getResources();	 Catch:{ XmlPullParserException -> 0x014c, IOException -> 0x0149, all -> 0x0147 }
        r8 = "extra";
        r5.parseBundleExtra(r8, r0, r6);	 Catch:{ XmlPullParserException -> 0x014c, IOException -> 0x0149, all -> 0x0147 }
        com.oneplus.lib.util.XmlUtils.skipCurrentTag(r1);	 Catch:{ XmlPullParserException -> 0x014c, IOException -> 0x0149, all -> 0x0147 }
        goto L_0x0133;
    L_0x011d:
        r5 = "intent";
        r5 = r15.equals(r5);	 Catch:{ XmlPullParserException -> 0x014c, IOException -> 0x0149, all -> 0x0147 }
        if (r5 == 0) goto L_0x0130;
    L_0x0125:
        r5 = r17.getResources();	 Catch:{ XmlPullParserException -> 0x014c, IOException -> 0x0149, all -> 0x0147 }
        r5 = android.content.Intent.parseIntent(r5, r1, r0);	 Catch:{ XmlPullParserException -> 0x014c, IOException -> 0x0149, all -> 0x0147 }
        r10.intent = r5;	 Catch:{ XmlPullParserException -> 0x014c, IOException -> 0x0149, all -> 0x0147 }
        goto L_0x0133;
    L_0x0130:
        com.oneplus.lib.util.XmlUtils.skipCurrentTag(r1);	 Catch:{ XmlPullParserException -> 0x014c, IOException -> 0x0149, all -> 0x0147 }
    L_0x0134:
        r5 = 1;
        r8 = 3;
        goto L_0x00f0;
    L_0x0137:
        r5 = r6.size();	 Catch:{ XmlPullParserException -> 0x014c, IOException -> 0x0149, all -> 0x0147 }
        if (r5 <= 0) goto L_0x0141;
    L_0x013d:
        r10.fragmentArguments = r6;	 Catch:{ XmlPullParserException -> 0x014c, IOException -> 0x0149, all -> 0x0147 }
        r5 = 0;
        r6 = r5;
    L_0x0141:
        r5 = r19;
        r5.add(r10);	 Catch:{ XmlPullParserException -> 0x0191, IOException -> 0x018f }
        goto L_0x015b;
    L_0x0147:
        r0 = move-exception;
        goto L_0x01a4;
    L_0x0149:
        r0 = move-exception;
        goto L_0x01ac;
    L_0x014c:
        r0 = move-exception;
        goto L_0x01bb;
    L_0x014f:
        r12 = r17;
        r5 = r19;
        com.oneplus.lib.util.XmlUtils.skipCurrentTag(r1);	 Catch:{ XmlPullParserException -> 0x0191, IOException -> 0x018f }
        goto L_0x015b;
    L_0x0157:
        r12 = r17;
        r5 = r19;
    L_0x015b:
        r5 = 1;
        goto L_0x002e;
    L_0x015e:
        r12 = r17;
        r5 = r19;
        if (r1 == 0) goto L_0x0167;
    L_0x0164:
        r1.close();
    L_0x0167:
        return;
    L_0x0168:
        r12 = r17;
        r5 = r19;
        r6 = new java.lang.RuntimeException;	 Catch:{ XmlPullParserException -> 0x0191, IOException -> 0x018f }
        r7 = new java.lang.StringBuilder;	 Catch:{ XmlPullParserException -> 0x0191, IOException -> 0x018f }
        r7.<init>();	 Catch:{ XmlPullParserException -> 0x0191, IOException -> 0x018f }
        r8 = "XML document must start with <preference-headers> tag; found";
        r7.append(r8);	 Catch:{ XmlPullParserException -> 0x0191, IOException -> 0x018f }
        r7.append(r3);	 Catch:{ XmlPullParserException -> 0x0191, IOException -> 0x018f }
        r8 = " at ";
        r7.append(r8);	 Catch:{ XmlPullParserException -> 0x0191, IOException -> 0x018f }
        r8 = r1.getPositionDescription();	 Catch:{ XmlPullParserException -> 0x0191, IOException -> 0x018f }
        r7.append(r8);	 Catch:{ XmlPullParserException -> 0x0191, IOException -> 0x018f }
        r7 = r7.toString();	 Catch:{ XmlPullParserException -> 0x0191, IOException -> 0x018f }
        r6.<init>(r7);	 Catch:{ XmlPullParserException -> 0x0191, IOException -> 0x018f }
        throw r6;	 Catch:{ XmlPullParserException -> 0x0191, IOException -> 0x018f }
    L_0x018f:
        r0 = move-exception;
        goto L_0x01ae;
    L_0x0191:
        r0 = move-exception;
        goto L_0x01bd;
    L_0x0193:
        r0 = move-exception;
        r12 = r17;
        goto L_0x01a4;
    L_0x0197:
        r0 = move-exception;
        r12 = r17;
        goto L_0x01ac;
    L_0x019b:
        r0 = move-exception;
        r12 = r17;
        goto L_0x01bb;
    L_0x019f:
        r0 = move-exception;
        r12 = r17;
        r2 = r18;
    L_0x01a4:
        r5 = r19;
        goto L_0x01c6;
    L_0x01a7:
        r0 = move-exception;
        r12 = r17;
        r2 = r18;
    L_0x01ac:
        r5 = r19;
    L_0x01ae:
        r3 = new java.lang.RuntimeException;	 Catch:{ all -> 0x01c5 }
        r4 = "Error parsing headers";
        r3.<init>(r4, r0);	 Catch:{ all -> 0x01c5 }
        throw r3;	 Catch:{ all -> 0x01c5 }
    L_0x01b6:
        r0 = move-exception;
        r12 = r17;
        r2 = r18;
    L_0x01bb:
        r5 = r19;
    L_0x01bd:
        r3 = new java.lang.RuntimeException;	 Catch:{ all -> 0x01c5 }
        r4 = "Error parsing headers";
        r3.<init>(r4, r0);	 Catch:{ all -> 0x01c5 }
        throw r3;	 Catch:{ all -> 0x01c5 }
    L_0x01c5:
        r0 = move-exception;
    L_0x01c6:
        if (r1 == 0) goto L_0x01cb;
    L_0x01c8:
        r1.close();
    L_0x01cb:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.lib.preference.PreferenceActivity.loadHeadersFromResource(int, java.util.List):void");
    }

    /* Access modifiers changed, original: protected */
    public boolean isValidFragment(String fragmentName) {
        if (getApplicationInfo().targetSdkVersion < 19) {
            return true;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Subclasses of PreferenceActivity must override isValidFragment(String) to verify that the Fragment class is valid! ");
        stringBuilder.append(getClass().getName());
        stringBuilder.append(" has not checked if fragment ");
        stringBuilder.append(fragmentName);
        stringBuilder.append(" is valid.");
        throw new RuntimeException(stringBuilder.toString());
    }

    public void setListFooter(View view) {
        this.mListFooter.removeAllViews();
        this.mListFooter.addView(view, new LayoutParams(-1, -2));
    }

    /* Access modifiers changed, original: protected */
    public void onStop() {
        super.onStop();
        if (this.mPreferenceManager != null) {
            this.mPreferenceManager.dispatchActivityStop();
        }
    }

    /* Access modifiers changed, original: protected */
    public void onDestroy() {
        this.mHandler.removeMessages(1);
        this.mHandler.removeMessages(2);
        super.onDestroy();
        if (this.mPreferenceManager != null) {
            this.mPreferenceManager.dispatchActivityDestroy();
        }
    }

    /* Access modifiers changed, original: protected */
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (this.mHeaders.size() > 0) {
            outState.putParcelableArrayList(HEADERS_TAG, this.mHeaders);
            if (this.mCurHeader != null) {
                int index = this.mHeaders.indexOf(this.mCurHeader);
                if (index >= 0) {
                    outState.putInt(CUR_HEADER_TAG, index);
                }
            }
        }
        if (this.mPreferenceManager != null) {
            PreferenceScreen preferenceScreen = getPreferenceScreen();
            if (preferenceScreen != null) {
                Bundle container = new Bundle();
                preferenceScreen.saveHierarchyState(container);
                outState.putBundle(PREFERENCES_TAG, container);
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public void onRestoreInstanceState(Bundle state) {
        if (this.mPreferenceManager != null) {
            Bundle container = state.getBundle(PREFERENCES_TAG);
            if (container != null) {
                PreferenceScreen preferenceScreen = getPreferenceScreen();
                if (preferenceScreen != null) {
                    preferenceScreen.restoreHierarchyState(container);
                    this.mSavedInstanceState = state;
                    return;
                }
            }
        }
        super.onRestoreInstanceState(state);
    }

    /* Access modifiers changed, original: protected */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (this.mPreferenceManager != null) {
            this.mPreferenceManager.dispatchActivityResult(requestCode, resultCode, data);
        }
    }

    public void onContentChanged() {
        super.onContentChanged();
        if (this.mPreferenceManager != null) {
            postBindPreferences();
        }
    }

    /* Access modifiers changed, original: protected */
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        if (getListAdapter() != null) {
            Object item = getListAdapter().getItem(position);
            if (item instanceof Header) {
                onHeaderClick((Header) item, position);
            }
        }
    }

    public void onHeaderClick(Header header, int position) {
        if (header.fragment != null) {
            if (this.mSinglePane) {
                int titleRes = header.breadCrumbTitleRes;
                int shortTitleRes = header.breadCrumbShortTitleRes;
                if (titleRes == 0) {
                    titleRes = header.titleRes;
                    shortTitleRes = 0;
                }
                startWithFragment(header.fragment, header.fragmentArguments, null, 0, titleRes, shortTitleRes);
                return;
            }
            switchToHeader(header);
        } else if (header.intent != null) {
            startActivity(header.intent);
        }
    }

    public Intent onBuildStartFragmentIntent(String fragmentName, Bundle args, int titleRes, int shortTitleRes) {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setClass(this, getClass());
        intent.putExtra(EXTRA_SHOW_FRAGMENT, fragmentName);
        intent.putExtra(EXTRA_SHOW_FRAGMENT_ARGUMENTS, args);
        intent.putExtra(EXTRA_SHOW_FRAGMENT_TITLE, titleRes);
        intent.putExtra(EXTRA_SHOW_FRAGMENT_SHORT_TITLE, shortTitleRes);
        intent.putExtra(EXTRA_NO_HEADERS, true);
        return intent;
    }

    public void startWithFragment(String fragmentName, Bundle args, Fragment resultTo, int resultRequestCode) {
        startWithFragment(fragmentName, args, resultTo, resultRequestCode, 0, 0);
    }

    public void startWithFragment(String fragmentName, Bundle args, Fragment resultTo, int resultRequestCode, int titleRes, int shortTitleRes) {
        Intent intent = onBuildStartFragmentIntent(fragmentName, args, titleRes, shortTitleRes);
        if (resultTo == null) {
            startActivity(intent);
        } else {
            resultTo.startActivityForResult(intent, resultRequestCode);
        }
    }

    public void showBreadCrumbs(CharSequence title, CharSequence shortTitle) {
        if (this.mFragmentBreadCrumbs == null) {
            try {
                this.mFragmentBreadCrumbs = (FragmentBreadCrumbs) findViewById(16908310);
                if (this.mFragmentBreadCrumbs == null) {
                    if (title != null) {
                        setTitle(title);
                    }
                    return;
                }
                if (this.mSinglePane) {
                    this.mFragmentBreadCrumbs.setVisibility(8);
                    View bcSection = findViewById(R.id.breadcrumb_section);
                    if (bcSection != null) {
                        bcSection.setVisibility(8);
                    }
                    setTitle(title);
                }
                this.mFragmentBreadCrumbs.setMaxVisible(2);
                this.mFragmentBreadCrumbs.setActivity(this);
            } catch (ClassCastException e) {
                setTitle(title);
                return;
            }
        }
        if (this.mFragmentBreadCrumbs.getVisibility() != 0) {
            setTitle(title);
        } else {
            this.mFragmentBreadCrumbs.setTitle(title, shortTitle);
            this.mFragmentBreadCrumbs.setParentTitle(null, null, null);
        }
    }

    public void setParentTitle(CharSequence title, CharSequence shortTitle, OnClickListener listener) {
        if (this.mFragmentBreadCrumbs != null) {
            this.mFragmentBreadCrumbs.setParentTitle(title, shortTitle, listener);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void setSelectedHeader(Header header) {
        this.mCurHeader = header;
        int index = this.mHeaders.indexOf(header);
        if (index >= 0) {
            getListView().setItemChecked(index, true);
        } else {
            getListView().clearChoices();
        }
        showBreadCrumbs(header);
    }

    /* Access modifiers changed, original: 0000 */
    public void showBreadCrumbs(Header header) {
        if (header != null) {
            CharSequence title = header.getBreadCrumbTitle(getResources());
            if (title == null) {
                title = header.getTitle(getResources());
            }
            if (title == null) {
                title = getTitle();
            }
            showBreadCrumbs(title, header.getBreadCrumbShortTitle(getResources()));
            return;
        }
        showBreadCrumbs(getTitle(), null);
    }

    private void switchToHeaderInner(String fragmentName, Bundle args) {
        getFragmentManager().popBackStack(BACK_STACK_PREFS, 1);
        if (isValidFragment(fragmentName)) {
            Fragment f = Fragment.instantiate(this, fragmentName, args);
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            transaction.replace(R.id.prefs, f);
            transaction.commitAllowingStateLoss();
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Invalid fragment for this activity: ");
        stringBuilder.append(fragmentName);
        throw new IllegalArgumentException(stringBuilder.toString());
    }

    public void switchToHeader(String fragmentName, Bundle args) {
        Header selectedHeader = null;
        for (int i = 0; i < this.mHeaders.size(); i++) {
            if (fragmentName.equals(((Header) this.mHeaders.get(i)).fragment)) {
                selectedHeader = (Header) this.mHeaders.get(i);
                break;
            }
        }
        setSelectedHeader(selectedHeader);
        switchToHeaderInner(fragmentName, args);
    }

    public void switchToHeader(Header header) {
        if (this.mCurHeader == header) {
            getFragmentManager().popBackStack(BACK_STACK_PREFS, 1);
        } else if (header.fragment != null) {
            switchToHeaderInner(header.fragment, header.fragmentArguments);
            setSelectedHeader(header);
        } else {
            throw new IllegalStateException("can't switch to header that has no fragment");
        }
    }

    /* Access modifiers changed, original: 0000 */
    public Header findBestMatchingHeader(Header cur, ArrayList<Header> from) {
        int j;
        Header oh;
        ArrayList<Header> matches = new ArrayList();
        int j2 = 0;
        for (j = 0; j < from.size(); j++) {
            oh = (Header) from.get(j);
            if (cur == oh || (cur.id != -1 && cur.id == oh.id)) {
                matches.clear();
                matches.add(oh);
                break;
            }
            if (cur.fragment != null) {
                if (cur.fragment.equals(oh.fragment)) {
                    matches.add(oh);
                }
            } else if (cur.intent != null) {
                if (cur.intent.equals(oh.intent)) {
                    matches.add(oh);
                }
            } else if (cur.title != null && cur.title.equals(oh.title)) {
                matches.add(oh);
            }
        }
        j = matches.size();
        if (j == 1) {
            return (Header) matches.get(0);
        }
        if (j > 1) {
            while (j2 < j) {
                oh = (Header) matches.get(j2);
                if (cur.fragmentArguments != null && cur.fragmentArguments.equals(oh.fragmentArguments)) {
                    return oh;
                }
                if (cur.extras != null && cur.extras.equals(oh.extras)) {
                    return oh;
                }
                if (cur.title != null && cur.title.equals(oh.title)) {
                    return oh;
                }
                j2++;
            }
        }
        return null;
    }

    public void startPreferenceFragment(Fragment fragment, boolean push) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.prefs, fragment);
        if (push) {
            transaction.setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.addToBackStack(BACK_STACK_PREFS);
        } else {
            transaction.setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        }
        transaction.commitAllowingStateLoss();
    }

    public void startPreferencePanel(String fragmentClass, Bundle args, int titleRes, CharSequence titleText, Fragment resultTo, int resultRequestCode) {
        if (this.mSinglePane) {
            startWithFragment(fragmentClass, args, resultTo, resultRequestCode, titleRes, 0);
            return;
        }
        Fragment f = Fragment.instantiate(this, fragmentClass, args);
        if (resultTo != null) {
            f.setTargetFragment(resultTo, resultRequestCode);
        }
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.prefs, f);
        if (titleRes != 0) {
            transaction.setBreadCrumbTitle(titleRes);
        } else if (titleText != null) {
            transaction.setBreadCrumbTitle(titleText);
        }
        transaction.setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.addToBackStack(BACK_STACK_PREFS);
        transaction.commitAllowingStateLoss();
    }

    public void finishPreferencePanel(Fragment caller, int resultCode, Intent resultData) {
        if (this.mSinglePane) {
            setResult(resultCode, resultData);
            finish();
            return;
        }
        onBackPressed();
        if (caller != null && caller.getTargetFragment() != null) {
            caller.getTargetFragment().onActivityResult(caller.getTargetRequestCode(), resultCode, resultData);
        }
    }

    public boolean onPreferenceStartFragment(PreferenceFragment caller, Preference pref) {
        startPreferencePanel(pref.getFragment(), pref.getExtras(), pref.getTitleRes(), pref.getTitle(), null, 0);
        return true;
    }

    private void postBindPreferences() {
        if (!this.mHandler.hasMessages(1)) {
            this.mHandler.obtainMessage(1).sendToTarget();
        }
    }

    private void bindPreferences() {
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        if (preferenceScreen != null) {
            preferenceScreen.bind(getListView());
            if (this.mSavedInstanceState != null) {
                super.onRestoreInstanceState(this.mSavedInstanceState);
                this.mSavedInstanceState = null;
            }
        }
    }

    @Deprecated
    public PreferenceManager getPreferenceManager() {
        return this.mPreferenceManager;
    }

    private void requirePreferenceManager() {
        if (this.mPreferenceManager != null) {
            return;
        }
        if (getListAdapter() == null) {
            throw new RuntimeException("This should be called after super.onCreate.");
        }
        throw new RuntimeException("Modern two-pane PreferenceActivity requires use of a PreferenceFragment");
    }

    @Deprecated
    public void setPreferenceScreen(PreferenceScreen preferenceScreen) {
        requirePreferenceManager();
        if (this.mPreferenceManager.setPreferences(preferenceScreen) && preferenceScreen != null) {
            postBindPreferences();
            CharSequence title = getPreferenceScreen().getTitle();
            if (title != null) {
                setTitle(title);
            }
        }
    }

    @Deprecated
    public PreferenceScreen getPreferenceScreen() {
        if (this.mPreferenceManager != null) {
            return this.mPreferenceManager.getPreferenceScreen();
        }
        return null;
    }

    @Deprecated
    public void addPreferencesFromIntent(Intent intent) {
        requirePreferenceManager();
        setPreferenceScreen(this.mPreferenceManager.inflateFromIntent(intent, getPreferenceScreen()));
    }

    @Deprecated
    public void addPreferencesFromResource(int preferencesResId) {
        requirePreferenceManager();
        setPreferenceScreen(this.mPreferenceManager.inflateFromResource(this, preferencesResId, getPreferenceScreen()));
    }

    @Deprecated
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        return false;
    }

    @Deprecated
    public Preference findPreference(CharSequence key) {
        if (this.mPreferenceManager == null) {
            return null;
        }
        return this.mPreferenceManager.findPreference(key);
    }

    /* Access modifiers changed, original: protected */
    public void onNewIntent(Intent intent) {
        if (this.mPreferenceManager != null) {
            this.mPreferenceManager.dispatchNewIntent(intent);
        }
    }

    /* Access modifiers changed, original: protected */
    public boolean hasNextButton() {
        return this.mNextButton != null;
    }

    /* Access modifiers changed, original: protected */
    public Button getNextButton() {
        return this.mNextButton;
    }
}
