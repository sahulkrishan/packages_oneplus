package com.oneplus.settings.aboutphone;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toolbar;
import com.android.settings.R;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.search.SearchFeatureProvider;
import com.oneplus.settings.utils.OPUtils;

public class OPForumContributors extends Activity {
    private TextView mForumContributorsTextView;
    private float mForumContributorsTextViewWidth;
    private ImageView mForumImageview;

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OPUtils.sendAppTracker("about_phone_settings", "click_award");
        setContentView(R.layout.op_forum_award);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.search_settings);
        setActionBar(toolbar);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            if (OPUtils.isO2()) {
                getActionBar().setTitle(R.string.oneplus_o2_contributors);
            } else {
                getActionBar().setTitle(R.string.oneplus_h2_contributors);
            }
        }
        this.mForumImageview = (ImageView) findViewById(R.id.forum_imageview);
        this.mForumContributorsTextView = (TextView) findViewById(R.id.forum_contributors);
        this.mForumContributorsTextView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                OPForumContributors.this.mForumContributorsTextView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                OPForumContributors.this.mForumContributorsTextViewWidth = (float) OPForumContributors.this.mForumContributorsTextView.getWidth();
                OPForumContributors.this.initData();
            }
        });
        if (OPUtils.isWhiteModeOn(getContentResolver())) {
            getWindow().getDecorView().setSystemUiVisibility(8192);
        }
    }

    private void initData() {
        String[] forumname;
        if (OPUtils.isO2()) {
            if (OPUtils.isBlackModeOn(getContentResolver())) {
                this.mForumImageview.setImageResource(R.drawable.op_forum_o2_dark);
            } else {
                this.mForumImageview.setImageResource(R.drawable.op_forum_o2_light);
            }
            forumname = getResources().getStringArray(R.array.forum_contributors_o2);
        } else {
            if (OPUtils.isBlackModeOn(getContentResolver())) {
                this.mForumImageview.setImageResource(R.drawable.op_forum_h2_dark);
            } else {
                this.mForumImageview.setImageResource(R.drawable.op_forum_h2_light);
            }
            forumname = getResources().getStringArray(R.array.forum_contributors_h2);
        }
        String splitString = "    /    ";
        int i = 0;
        String lastName = forumname[0];
        String name = "";
        for (int i2 = 0; i2 < forumname.length; i2++) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(splitString);
            stringBuilder.append(forumname[i2]);
            String appendName = new String(stringBuilder.toString());
            String tempName = new String(lastName);
            StringBuilder stringBuilder2;
            if (i2 == 0) {
                stringBuilder2 = new StringBuilder();
                stringBuilder2.append(name);
                stringBuilder2.append(forumname[i2]);
                name = stringBuilder2.toString();
                lastName = forumname[i2];
            } else if (needSplitText(lastName)) {
                stringBuilder2 = new StringBuilder();
                stringBuilder2.append(name);
                stringBuilder2.append("\n");
                stringBuilder2.append(forumname[i2]);
                name = stringBuilder2.toString();
                lastName = forumname[i2];
            } else {
                stringBuilder2 = new StringBuilder();
                stringBuilder2.append(tempName);
                stringBuilder2.append(appendName);
                String stringBuilder3 = stringBuilder2.toString();
                tempName = stringBuilder3;
                if (needSplitText(stringBuilder3)) {
                    stringBuilder2 = new StringBuilder();
                    stringBuilder2.append(name);
                    stringBuilder2.append("\n");
                    stringBuilder2.append(forumname[i2]);
                    name = stringBuilder2.toString();
                    lastName = forumname[i2];
                } else {
                    stringBuilder2 = new StringBuilder();
                    stringBuilder2.append(name);
                    stringBuilder2.append(appendName);
                    name = stringBuilder2.toString();
                    stringBuilder2 = new StringBuilder();
                    stringBuilder2.append(lastName);
                    stringBuilder2.append(appendName);
                    lastName = stringBuilder2.toString();
                }
            }
        }
        SpannableStringBuilder builder = new SpannableStringBuilder(name);
        CharacterStyle font = new RelativeSizeSpan(0.1f);
        while (i < name.length()) {
            if (Character.valueOf(name.charAt(i)).equals(Character.valueOf('/')) && i + 1 < name.length()) {
                if (OPUtils.isBlackModeOn(getContentResolver())) {
                    builder.setSpan(new ForegroundColorSpan(Color.parseColor("#4db2b2b2")), i - 1, i + 1, 34);
                } else {
                    builder.setSpan(new ForegroundColorSpan(Color.parseColor("#4d121212")), i - 1, i + 1, 34);
                }
            }
            i++;
        }
        this.mForumContributorsTextView.setText(builder);
    }

    public float getFontWidth(Paint paint, String text) {
        return paint.measureText(text);
    }

    public boolean needSplitText(String allNames) {
        if (getFontWidth(this.mForumContributorsTextView.getPaint(), allNames) < this.mForumContributorsTextViewWidth) {
            return false;
        }
        return true;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.op_search_settings, menu);
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        searchMenuItem.setVisible(true);
        searchMenuItem.setIcon(R.drawable.ic_menu_search_material);
        searchMenuItem.setOnMenuItemClickListener(new -$$Lambda$OPForumContributors$Fgq3BTVklCn5TzLTlkK9M4s9qko(this));
        return true;
    }

    public static /* synthetic */ boolean lambda$onCreateOptionsMenu$0(OPForumContributors oPForumContributors, MenuItem target) {
        Intent intent = SearchFeatureProvider.SEARCH_UI_INTENT;
        intent.setPackage(FeatureFactory.getFactory(oPForumContributors.getBaseContext()).getSearchFeatureProvider().getSettingsIntelligencePkgName());
        oPForumContributors.startActivityForResult(intent, 0);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != 16908332) {
            return super.onOptionsItemSelected(item);
        }
        finish();
        return true;
    }
}
