package android.support.v17.leanback.app;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.speech.SpeechRecognizer;
import android.support.v17.leanback.R;
import android.support.v17.leanback.widget.ObjectAdapter;
import android.support.v17.leanback.widget.ObjectAdapter.DataObserver;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter.ViewHolder;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v17.leanback.widget.SearchBar;
import android.support.v17.leanback.widget.SearchBar.SearchBarListener;
import android.support.v17.leanback.widget.SearchBar.SearchBarPermissionListener;
import android.support.v17.leanback.widget.SearchOrbView.Colors;
import android.support.v17.leanback.widget.SpeechRecognitionCallback;
import android.support.v17.leanback.widget.VerticalGridView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.CompletionInfo;
import android.widget.FrameLayout;
import java.util.ArrayList;
import java.util.List;

@Deprecated
public class SearchFragment extends Fragment {
    private static final String ARG_PREFIX = SearchFragment.class.getCanonicalName();
    private static final String ARG_QUERY;
    private static final String ARG_TITLE;
    static final int AUDIO_PERMISSION_REQUEST_CODE = 0;
    static final boolean DEBUG = false;
    private static final String EXTRA_LEANBACK_BADGE_PRESENT = "LEANBACK_BADGE_PRESENT";
    static final int QUERY_COMPLETE = 2;
    static final int RESULTS_CHANGED = 1;
    static final long SPEECH_RECOGNITION_DELAY_MS = 300;
    static final String TAG = SearchFragment.class.getSimpleName();
    final DataObserver mAdapterObserver = new DataObserver() {
        public void onChanged() {
            SearchFragment.this.mHandler.removeCallbacks(SearchFragment.this.mResultsChangedCallback);
            SearchFragment.this.mHandler.post(SearchFragment.this.mResultsChangedCallback);
        }
    };
    boolean mAutoStartRecognition = true;
    private Drawable mBadgeDrawable;
    private ExternalQuery mExternalQuery;
    final Handler mHandler = new Handler();
    private boolean mIsPaused;
    private OnItemViewClickedListener mOnItemViewClickedListener;
    OnItemViewSelectedListener mOnItemViewSelectedListener;
    String mPendingQuery = null;
    private boolean mPendingStartRecognitionWhenPaused;
    private SearchBarPermissionListener mPermissionListener = new SearchBarPermissionListener() {
        public void requestAudioPermission() {
            PermissionHelper.requestPermissions(SearchFragment.this, new String[]{"android.permission.RECORD_AUDIO"}, 0);
        }
    };
    SearchResultProvider mProvider;
    ObjectAdapter mResultAdapter;
    final Runnable mResultsChangedCallback = new Runnable() {
        public void run() {
            if (!(SearchFragment.this.mRowsFragment == null || SearchFragment.this.mRowsFragment.getAdapter() == SearchFragment.this.mResultAdapter || (SearchFragment.this.mRowsFragment.getAdapter() == null && SearchFragment.this.mResultAdapter.size() == 0))) {
                SearchFragment.this.mRowsFragment.setAdapter(SearchFragment.this.mResultAdapter);
                SearchFragment.this.mRowsFragment.setSelectedPosition(0);
            }
            SearchFragment.this.updateSearchBarVisibility();
            SearchFragment searchFragment = SearchFragment.this;
            searchFragment.mStatus |= 1;
            if ((SearchFragment.this.mStatus & 2) != 0) {
                SearchFragment.this.updateFocus();
            }
            SearchFragment.this.updateSearchBarNextFocusId();
        }
    };
    RowsFragment mRowsFragment;
    SearchBar mSearchBar;
    private final Runnable mSetSearchResultProvider = new Runnable() {
        public void run() {
            if (SearchFragment.this.mRowsFragment != null) {
                ObjectAdapter adapter = SearchFragment.this.mProvider.getResultsAdapter();
                if (adapter != SearchFragment.this.mResultAdapter) {
                    boolean firstTime = SearchFragment.this.mResultAdapter == null;
                    SearchFragment.this.releaseAdapter();
                    SearchFragment.this.mResultAdapter = adapter;
                    if (SearchFragment.this.mResultAdapter != null) {
                        SearchFragment.this.mResultAdapter.registerObserver(SearchFragment.this.mAdapterObserver);
                    }
                    if (!(firstTime && (SearchFragment.this.mResultAdapter == null || SearchFragment.this.mResultAdapter.size() == 0))) {
                        SearchFragment.this.mRowsFragment.setAdapter(SearchFragment.this.mResultAdapter);
                    }
                    SearchFragment.this.executePendingQuery();
                }
                SearchFragment.this.updateSearchBarNextFocusId();
                if (SearchFragment.this.mAutoStartRecognition) {
                    SearchFragment.this.mHandler.removeCallbacks(SearchFragment.this.mStartRecognitionRunnable);
                    SearchFragment.this.mHandler.postDelayed(SearchFragment.this.mStartRecognitionRunnable, SearchFragment.SPEECH_RECOGNITION_DELAY_MS);
                } else {
                    SearchFragment.this.updateFocus();
                }
            }
        }
    };
    private SpeechRecognitionCallback mSpeechRecognitionCallback;
    private SpeechRecognizer mSpeechRecognizer;
    final Runnable mStartRecognitionRunnable = new Runnable() {
        public void run() {
            SearchFragment.this.mAutoStartRecognition = false;
            SearchFragment.this.mSearchBar.startRecognition();
        }
    };
    int mStatus;
    private String mTitle;

    static class ExternalQuery {
        String mQuery;
        boolean mSubmit;

        ExternalQuery(String query, boolean submit) {
            this.mQuery = query;
            this.mSubmit = submit;
        }
    }

    public interface SearchResultProvider {
        ObjectAdapter getResultsAdapter();

        boolean onQueryTextChange(String str);

        boolean onQueryTextSubmit(String str);
    }

    static {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(ARG_PREFIX);
        stringBuilder.append(".query");
        ARG_QUERY = stringBuilder.toString();
        stringBuilder = new StringBuilder();
        stringBuilder.append(ARG_PREFIX);
        stringBuilder.append(".title");
        ARG_TITLE = stringBuilder.toString();
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0 && permissions.length > 0 && permissions[0].equals("android.permission.RECORD_AUDIO") && grantResults[0] == 0) {
            startRecognition();
        }
    }

    public static Bundle createArgs(Bundle args, String query) {
        return createArgs(args, query, null);
    }

    public static Bundle createArgs(Bundle args, String query, String title) {
        if (args == null) {
            args = new Bundle();
        }
        args.putString(ARG_QUERY, query);
        args.putString(ARG_TITLE, title);
        return args;
    }

    public static SearchFragment newInstance(String query) {
        SearchFragment fragment = new SearchFragment();
        fragment.setArguments(createArgs(null, query));
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        if (this.mAutoStartRecognition) {
            this.mAutoStartRecognition = savedInstanceState == null;
        }
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.lb_search_fragment, container, false);
        this.mSearchBar = (SearchBar) ((FrameLayout) root.findViewById(R.id.lb_search_frame)).findViewById(R.id.lb_search_bar);
        this.mSearchBar.setSearchBarListener(new SearchBarListener() {
            public void onSearchQueryChange(String query) {
                if (SearchFragment.this.mProvider != null) {
                    SearchFragment.this.retrieveResults(query);
                } else {
                    SearchFragment.this.mPendingQuery = query;
                }
            }

            public void onSearchQuerySubmit(String query) {
                SearchFragment.this.submitQuery(query);
            }

            public void onKeyboardDismiss(String query) {
                SearchFragment.this.queryComplete();
            }
        });
        this.mSearchBar.setSpeechRecognitionCallback(this.mSpeechRecognitionCallback);
        this.mSearchBar.setPermissionListener(this.mPermissionListener);
        applyExternalQuery();
        readArguments(getArguments());
        if (this.mBadgeDrawable != null) {
            setBadgeDrawable(this.mBadgeDrawable);
        }
        if (this.mTitle != null) {
            setTitle(this.mTitle);
        }
        if (getChildFragmentManager().findFragmentById(R.id.lb_results_frame) == null) {
            this.mRowsFragment = new RowsFragment();
            getChildFragmentManager().beginTransaction().replace(R.id.lb_results_frame, this.mRowsFragment).commit();
        } else {
            this.mRowsFragment = (RowsFragment) getChildFragmentManager().findFragmentById(R.id.lb_results_frame);
        }
        this.mRowsFragment.setOnItemViewSelectedListener(new OnItemViewSelectedListener() {
            public void onItemSelected(ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
                SearchFragment.this.updateSearchBarVisibility();
                if (SearchFragment.this.mOnItemViewSelectedListener != null) {
                    SearchFragment.this.mOnItemViewSelectedListener.onItemSelected(itemViewHolder, item, rowViewHolder, row);
                }
            }
        });
        this.mRowsFragment.setOnItemViewClickedListener(this.mOnItemViewClickedListener);
        this.mRowsFragment.setExpand(true);
        if (this.mProvider != null) {
            onSetSearchResultProvider();
        }
        return root;
    }

    private void resultsAvailable() {
        if ((this.mStatus & 2) != 0) {
            focusOnResults();
        }
        updateSearchBarNextFocusId();
    }

    public void onStart() {
        super.onStart();
        VerticalGridView list = this.mRowsFragment.getVerticalGridView();
        int mContainerListAlignTop = getResources().getDimensionPixelSize(R.dimen.lb_search_browse_rows_align_top);
        list.setItemAlignmentOffset(0);
        list.setItemAlignmentOffsetPercent(-1.0f);
        list.setWindowAlignmentOffset(mContainerListAlignTop);
        list.setWindowAlignmentOffsetPercent(-1.0f);
        list.setWindowAlignment(0);
        list.setFocusable(false);
        list.setFocusableInTouchMode(false);
    }

    public void onResume() {
        super.onResume();
        this.mIsPaused = false;
        if (this.mSpeechRecognitionCallback == null && this.mSpeechRecognizer == null) {
            this.mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(FragmentUtil.getContext(this));
            this.mSearchBar.setSpeechRecognizer(this.mSpeechRecognizer);
        }
        if (this.mPendingStartRecognitionWhenPaused) {
            this.mPendingStartRecognitionWhenPaused = false;
            this.mSearchBar.startRecognition();
            return;
        }
        this.mSearchBar.stopRecognition();
    }

    public void onPause() {
        releaseRecognizer();
        this.mIsPaused = true;
        super.onPause();
    }

    public void onDestroy() {
        releaseAdapter();
        super.onDestroy();
    }

    public RowsFragment getRowsFragment() {
        return this.mRowsFragment;
    }

    private void releaseRecognizer() {
        if (this.mSpeechRecognizer != null) {
            this.mSearchBar.setSpeechRecognizer(null);
            this.mSpeechRecognizer.destroy();
            this.mSpeechRecognizer = null;
        }
    }

    public void startRecognition() {
        if (this.mIsPaused) {
            this.mPendingStartRecognitionWhenPaused = true;
        } else {
            this.mSearchBar.startRecognition();
        }
    }

    public void setSearchResultProvider(SearchResultProvider searchResultProvider) {
        if (this.mProvider != searchResultProvider) {
            this.mProvider = searchResultProvider;
            onSetSearchResultProvider();
        }
    }

    public void setOnItemViewSelectedListener(OnItemViewSelectedListener listener) {
        this.mOnItemViewSelectedListener = listener;
    }

    public void setOnItemViewClickedListener(OnItemViewClickedListener listener) {
        if (listener != this.mOnItemViewClickedListener) {
            this.mOnItemViewClickedListener = listener;
            if (this.mRowsFragment != null) {
                this.mRowsFragment.setOnItemViewClickedListener(this.mOnItemViewClickedListener);
            }
        }
    }

    public void setTitle(String title) {
        this.mTitle = title;
        if (this.mSearchBar != null) {
            this.mSearchBar.setTitle(title);
        }
    }

    public String getTitle() {
        if (this.mSearchBar != null) {
            return this.mSearchBar.getTitle();
        }
        return null;
    }

    public void setBadgeDrawable(Drawable drawable) {
        this.mBadgeDrawable = drawable;
        if (this.mSearchBar != null) {
            this.mSearchBar.setBadgeDrawable(drawable);
        }
    }

    public Drawable getBadgeDrawable() {
        if (this.mSearchBar != null) {
            return this.mSearchBar.getBadgeDrawable();
        }
        return null;
    }

    public void setSearchAffordanceColors(Colors colors) {
        if (this.mSearchBar != null) {
            this.mSearchBar.setSearchAffordanceColors(colors);
        }
    }

    public void setSearchAffordanceColorsInListening(Colors colors) {
        if (this.mSearchBar != null) {
            this.mSearchBar.setSearchAffordanceColorsInListening(colors);
        }
    }

    public void displayCompletions(List<String> completions) {
        this.mSearchBar.displayCompletions((List) completions);
    }

    public void displayCompletions(CompletionInfo[] completions) {
        this.mSearchBar.displayCompletions(completions);
    }

    @Deprecated
    public void setSpeechRecognitionCallback(SpeechRecognitionCallback callback) {
        this.mSpeechRecognitionCallback = callback;
        if (this.mSearchBar != null) {
            this.mSearchBar.setSpeechRecognitionCallback(this.mSpeechRecognitionCallback);
        }
        if (callback != null) {
            releaseRecognizer();
        }
    }

    public void setSearchQuery(String query, boolean submit) {
        if (query != null) {
            this.mExternalQuery = new ExternalQuery(query, submit);
            applyExternalQuery();
            if (this.mAutoStartRecognition) {
                this.mAutoStartRecognition = false;
                this.mHandler.removeCallbacks(this.mStartRecognitionRunnable);
            }
        }
    }

    public void setSearchQuery(Intent intent, boolean submit) {
        ArrayList<String> matches = intent.getStringArrayListExtra("android.speech.extra.RESULTS");
        if (matches != null && matches.size() > 0) {
            setSearchQuery((String) matches.get(0), submit);
        }
    }

    public Intent getRecognizerIntent() {
        Intent recognizerIntent = new Intent("android.speech.action.RECOGNIZE_SPEECH");
        recognizerIntent.putExtra("android.speech.extra.LANGUAGE_MODEL", "free_form");
        boolean z = true;
        recognizerIntent.putExtra("android.speech.extra.PARTIAL_RESULTS", true);
        if (!(this.mSearchBar == null || this.mSearchBar.getHint() == null)) {
            recognizerIntent.putExtra("android.speech.extra.PROMPT", this.mSearchBar.getHint());
        }
        String str = EXTRA_LEANBACK_BADGE_PRESENT;
        if (this.mBadgeDrawable == null) {
            z = false;
        }
        recognizerIntent.putExtra(str, z);
        return recognizerIntent;
    }

    /* Access modifiers changed, original: 0000 */
    public void retrieveResults(String searchQuery) {
        if (this.mProvider.onQueryTextChange(searchQuery)) {
            this.mStatus &= -3;
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void submitQuery(String query) {
        queryComplete();
        if (this.mProvider != null) {
            this.mProvider.onQueryTextSubmit(query);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void queryComplete() {
        this.mStatus |= 2;
        focusOnResults();
    }

    /* Access modifiers changed, original: 0000 */
    public void updateSearchBarVisibility() {
        int position = this.mRowsFragment != null ? this.mRowsFragment.getSelectedPosition() : -1;
        SearchBar searchBar = this.mSearchBar;
        int i = (position <= 0 || this.mResultAdapter == null || this.mResultAdapter.size() == 0) ? 0 : 8;
        searchBar.setVisibility(i);
    }

    /* Access modifiers changed, original: 0000 */
    public void updateSearchBarNextFocusId() {
        if (this.mSearchBar != null && this.mResultAdapter != null) {
            int viewId;
            if (this.mResultAdapter.size() == 0 || this.mRowsFragment == null || this.mRowsFragment.getVerticalGridView() == null) {
                viewId = 0;
            } else {
                viewId = this.mRowsFragment.getVerticalGridView().getId();
            }
            this.mSearchBar.setNextFocusDownId(viewId);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void updateFocus() {
        if (this.mResultAdapter == null || this.mResultAdapter.size() <= 0 || this.mRowsFragment == null || this.mRowsFragment.getAdapter() != this.mResultAdapter) {
            this.mSearchBar.requestFocus();
        } else {
            focusOnResults();
        }
    }

    /* JADX WARNING: Missing block: B:10:0x0028, code skipped:
            return;
     */
    private void focusOnResults() {
        /*
        r1 = this;
        r0 = r1.mRowsFragment;
        if (r0 == 0) goto L_0x0028;
    L_0x0004:
        r0 = r1.mRowsFragment;
        r0 = r0.getVerticalGridView();
        if (r0 == 0) goto L_0x0028;
    L_0x000c:
        r0 = r1.mResultAdapter;
        r0 = r0.size();
        if (r0 != 0) goto L_0x0015;
    L_0x0014:
        goto L_0x0028;
    L_0x0015:
        r0 = r1.mRowsFragment;
        r0 = r0.getVerticalGridView();
        r0 = r0.requestFocus();
        if (r0 == 0) goto L_0x0027;
    L_0x0021:
        r0 = r1.mStatus;
        r0 = r0 & -2;
        r1.mStatus = r0;
    L_0x0027:
        return;
    L_0x0028:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v17.leanback.app.SearchFragment.focusOnResults():void");
    }

    private void onSetSearchResultProvider() {
        this.mHandler.removeCallbacks(this.mSetSearchResultProvider);
        this.mHandler.post(this.mSetSearchResultProvider);
    }

    /* Access modifiers changed, original: 0000 */
    public void releaseAdapter() {
        if (this.mResultAdapter != null) {
            this.mResultAdapter.unregisterObserver(this.mAdapterObserver);
            this.mResultAdapter = null;
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void executePendingQuery() {
        if (this.mPendingQuery != null && this.mResultAdapter != null) {
            String query = this.mPendingQuery;
            this.mPendingQuery = null;
            retrieveResults(query);
        }
    }

    private void applyExternalQuery() {
        if (this.mExternalQuery != null && this.mSearchBar != null) {
            this.mSearchBar.setSearchQuery(this.mExternalQuery.mQuery);
            if (this.mExternalQuery.mSubmit) {
                submitQuery(this.mExternalQuery.mQuery);
            }
            this.mExternalQuery = null;
        }
    }

    private void readArguments(Bundle args) {
        if (args != null) {
            if (args.containsKey(ARG_QUERY)) {
                setSearchQuery(args.getString(ARG_QUERY));
            }
            if (args.containsKey(ARG_TITLE)) {
                setTitle(args.getString(ARG_TITLE));
            }
        }
    }

    private void setSearchQuery(String query) {
        this.mSearchBar.setSearchQuery(query);
    }
}
