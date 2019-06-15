package com.android.settings.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.Uri.Builder;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.android.settings.R;
import com.oneplus.custom.utils.OpCustomizeSettings;
import com.oneplus.custom.utils.OpCustomizeSettings.CUSTOM_TYPE;
import com.oneplus.lib.widget.recyclerview.LinearLayoutManager;
import com.oneplus.lib.widget.recyclerview.OPItemDecoration;
import com.oneplus.lib.widget.recyclerview.OPRecyclerView;
import com.oneplus.lib.widget.recyclerview.RecyclerView;
import com.oneplus.lib.widget.recyclerview.RecyclerView.Adapter;
import com.oneplus.lib.widget.recyclerview.RecyclerView.State;
import com.oneplus.lib.widget.recyclerview.RecyclerView.ViewHolder;
import com.oneplus.settings.utils.OPConstants;
import com.oneplus.settings.utils.OPUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OPCustomFingeprintAnimVideoPreference extends Preference {
    public static final int ANIM_STYLE_0 = 3;
    public static final int ANIM_STYLE_1 = 0;
    public static final int ANIM_STYLE_2 = 1;
    public static final int ANIM_STYLE_3 = 2;
    public static final int ANIM_STYLE_4 = 9;
    private static final String TAG = "VideoPreference";
    private static final int TYPE_GESTURE_NAVIGATION_BAR = 3;
    private AnimStyleAdapter mAdapter;
    @VisibleForTesting
    boolean mAnimationAvailable;
    private List<AnimEntity> mAnims = new ArrayList();
    private float mAspectRadio = 1.0f;
    private final Context mContext;
    private VH mCurrentVH;
    @VisibleForTesting
    MediaPlayer mMediaPlayer;
    private int mPreviewResource;
    private OPRecyclerView mRecyclerView;
    private int mSelectedAnimIndex;
    private Uri mVideoPath;
    private boolean mVideoPaused;
    private boolean mVideoReady;

    class AnimEntity {
        int animIndex;
        String animName;
        int animResId;
        boolean selected = false;

        public AnimEntity(String animName, int animResId, int animIndex) {
            this.animName = animName;
            this.animResId = animResId;
            this.animIndex = animIndex;
        }
    }

    class AnimStyleAdapter extends Adapter<VH> {
        AnimStyleAdapter() {
        }

        public VH onCreateViewHolder(ViewGroup viewGroup, int i) {
            return new VH(LayoutInflater.from(OPCustomFingeprintAnimVideoPreference.this.mContext).inflate(R.layout.op_custom_fingerprint_anim_choose_item, null));
        }

        public void onBindViewHolder(final VH vh, final int position) {
            final AnimEntity anim = (AnimEntity) OPCustomFingeprintAnimVideoPreference.this.mAnims.get(position);
            vh.imageView.setImageResource(anim.animResId);
            vh.textView.setText(anim.animName);
            vh.itemView.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    OPCustomFingeprintAnimVideoPreference.this.setSelectedAnim(position);
                    OPCustomFingeprintAnimVideoPreference.this.mCurrentVH.viewBorder.setBackgroundResource(R.drawable.op_custom_fingerprint_anim_item_unselected_border);
                    OPCustomFingeprintAnimVideoPreference.this.mCurrentVH.viewShadow.setVisibility(4);
                    vh.viewBorder.setBackgroundResource(R.drawable.op_custom_fingerprint_anim_item_selected_border);
                    vh.viewShadow.setVisibility(0);
                    OPCustomFingeprintAnimVideoPreference.this.mCurrentVH = vh;
                    OPCustomFingeprintAnimVideoPreference.this.changeAnimStyle(anim.animIndex);
                    OPCustomFingeprintAnimVideoPreference.this.mSelectedAnimIndex = position;
                }
            });
            if (anim.selected) {
                vh.viewBorder.setBackgroundResource(R.drawable.op_custom_fingerprint_anim_item_selected_border);
                vh.viewShadow.setVisibility(0);
                OPCustomFingeprintAnimVideoPreference.this.mCurrentVH = vh;
            } else {
                vh.viewBorder.setBackgroundResource(R.drawable.op_custom_fingerprint_anim_item_unselected_border);
                vh.viewShadow.setVisibility(4);
            }
            if (position == OPCustomFingeprintAnimVideoPreference.this.mAnims.size() - 1) {
                vh.layoutContainer.setPadding(0, (int) OPCustomFingeprintAnimVideoPreference.this.mContext.getResources().getDimension(R.dimen.oneplus_contorl_layout_margin_top2), (int) OPCustomFingeprintAnimVideoPreference.this.mContext.getResources().getDimension(R.dimen.oneplus_contorl_layout_margin_top2), 5);
            } else {
                vh.layoutContainer.setPadding(0, (int) OPCustomFingeprintAnimVideoPreference.this.mContext.getResources().getDimension(R.dimen.oneplus_contorl_layout_margin_top2), 0, 5);
            }
            vh.layoutContainer.requestLayout();
        }

        public int getItemCount() {
            return OPCustomFingeprintAnimVideoPreference.this.mAnims.size();
        }
    }

    class VH extends ViewHolder {
        ImageView imageView;
        RelativeLayout layoutContainer;
        TextView textView;
        View viewBorder;
        View viewShadow;

        public VH(View itemView) {
            super(itemView);
            this.imageView = (ImageView) itemView.findViewById(R.id.img_anim_choose_item);
            this.textView = (TextView) itemView.findViewById(R.id.tv_anim_name);
            this.viewBorder = itemView.findViewById(R.id.view_anim_border);
            this.viewShadow = itemView.findViewById(R.id.view_anim_shadow);
            this.layoutContainer = (RelativeLayout) itemView.findViewById(R.id.layout_container_finger_anim_item);
        }
    }

    class SpaceItemDecoration extends OPItemDecoration {
        public SpaceItemDecoration(int space) {
            super(space);
        }

        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, State state) {
            super.getItemOffsets(outRect, view, parent, state);
            outRect.left = (int) OPCustomFingeprintAnimVideoPreference.this.mContext.getResources().getDimension(R.dimen.oneplus_settings_layout_margin_left2);
        }
    }

    public OPCustomFingeprintAnimVideoPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.VideoPreference, 0, 0);
        try {
            int animation = attributes.getResourceId(0, 0);
            this.mVideoPath = new Builder().scheme("android.resource").authority(context.getPackageName()).appendPath(String.valueOf(getCustomAnimationId(System.getIntForUser(this.mContext.getContentResolver(), OPConstants.OP_CUSTOM_UNLOCK_ANIMATION_STYLE_KEY, 0, -2)))).build();
            this.mMediaPlayer = MediaPlayer.create(this.mContext, this.mVideoPath);
            if (this.mMediaPlayer == null || this.mMediaPlayer.getDuration() <= 0) {
                setVisible(false);
                attributes.recycle();
            }
            setVisible(true);
            int layoutId = R.layout.op_custom_fingerprint_anim_choose_layout;
            if (CUSTOM_TYPE.MCL.equals(OpCustomizeSettings.getCustomType())) {
                layoutId = R.layout.op_custom_fingerprint_anim_choose_layout_mcl;
            }
            setLayoutResource(layoutId);
            this.mPreviewResource = attributes.getResourceId(1, 0);
            this.mMediaPlayer.setOnSeekCompleteListener(new -$$Lambda$OPCustomFingeprintAnimVideoPreference$xNjQuqlzU7hQu-C5vLxvtUk8_xY(this));
            this.mMediaPlayer.setOnPreparedListener(-$$Lambda$OPCustomFingeprintAnimVideoPreference$-76bRwQYPctZEhfpNbtqN2ejOb4.INSTANCE);
            this.mAnimationAvailable = true;
            updateAspectRatio();
            attributes.recycle();
        } catch (Exception e) {
            Log.w(TAG, "Animation resource not found. Will not show animation.");
        } catch (Throwable th) {
            attributes.recycle();
        }
    }

    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        holder.itemView.setBackground(null);
        if (this.mAnimationAvailable) {
            ((ScrollView) holder.findViewById(R.id.video_container_scrollview)).setOnTouchListener(new OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    return false;
                }
            });
            View group = holder.findViewById(R.id.video_container_group);
            if (OPUtils.isBlackModeOn(this.mContext.getContentResolver())) {
                group.setBackgroundColor(Color.parseColor("#282828"));
            } else {
                group.setBackgroundColor(Color.parseColor("#f5f5f5"));
            }
            TextureView video = (TextureView) holder.findViewById(R.id.video_texture_view);
            final ImageView imageView = (ImageView) holder.findViewById(R.id.video_preview_image);
            final ImageView playButton = (ImageView) holder.findViewById(R.id.video_play_button);
            AspectRatioFrameLayout layout = (AspectRatioFrameLayout) holder.findViewById(R.id.video_container);
            imageView.setImageResource(this.mPreviewResource);
            layout.setAspectRatio(this.mAspectRadio);
            video.setSurfaceTextureListener(new SurfaceTextureListener() {
                public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
                    if (OPCustomFingeprintAnimVideoPreference.this.mMediaPlayer != null) {
                        OPCustomFingeprintAnimVideoPreference.this.mMediaPlayer.setSurface(new Surface(surfaceTexture));
                        OPCustomFingeprintAnimVideoPreference.this.mVideoReady = false;
                        OPCustomFingeprintAnimVideoPreference.this.mMediaPlayer.seekTo(0);
                    }
                }

                public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
                }

                public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                    imageView.setVisibility(0);
                    return false;
                }

                public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
                    if (OPCustomFingeprintAnimVideoPreference.this.mVideoReady) {
                        if (imageView.getVisibility() == 0) {
                            imageView.setVisibility(8);
                        }
                        if (!(OPCustomFingeprintAnimVideoPreference.this.mVideoPaused || OPCustomFingeprintAnimVideoPreference.this.mMediaPlayer == null || OPCustomFingeprintAnimVideoPreference.this.mMediaPlayer.isPlaying())) {
                            OPCustomFingeprintAnimVideoPreference.this.mMediaPlayer.start();
                            playButton.setVisibility(8);
                        }
                    }
                    if (OPCustomFingeprintAnimVideoPreference.this.mMediaPlayer != null && !OPCustomFingeprintAnimVideoPreference.this.mMediaPlayer.isPlaying()) {
                        playButton.getVisibility();
                    }
                }
            });
            this.mRecyclerView = (OPRecyclerView) holder.findViewById(R.id.custom_fingerprint_anim_style_recyclerview);
            LinearLayoutManager manager = new LinearLayoutManager(this.mContext);
            manager.setOrientation(0);
            this.mRecyclerView.setLayoutManager(manager);
            this.mRecyclerView.addOPItemDecoration(new SpaceItemDecoration(0));
            initStyleAnimViews();
            this.mAdapter = new AnimStyleAdapter();
            this.mRecyclerView.setAdapter(this.mAdapter);
            this.mSelectedAnimIndex = getSelectedAnimIndex();
            if (this.mSelectedAnimIndex >= 0 && this.mSelectedAnimIndex < this.mAnims.size()) {
                manager.scrollToPosition(this.mSelectedAnimIndex);
            }
            RelativeLayout layoutVideoContainer = (RelativeLayout) holder.findViewById(R.id.video_container_group);
            if (!isNavigationTypeIsGesture()) {
                layoutVideoContainer.setPadding(0, 5, 0, 5);
                layoutVideoContainer.requestLayout();
            }
        }
    }

    private int getCustomAnimationId(int style) {
        if (style == 9) {
            return R.raw.op_custom_fingerprint_anim_4;
        }
        switch (style) {
            case 0:
                return R.raw.op_custom_fingerprint_anim_1;
            case 1:
                return R.raw.op_custom_fingerprint_anim_2;
            case 2:
                return R.raw.op_custom_fingerprint_anim_3;
            case 3:
                return R.raw.op_custom_fingerprint_anim_0;
            default:
                return R.raw.op_custom_fingerprint_anim_1;
        }
    }

    private void changeAnimStyle(int value) {
        playAnimByStyle(value);
    }

    private void setAnimStyle(int value) {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("setAnimStyle value:");
        stringBuilder.append(value);
        Log.d(str, stringBuilder.toString());
        System.putIntForUser(this.mContext.getContentResolver(), OPConstants.OP_CUSTOM_UNLOCK_ANIMATION_STYLE_KEY, value, -2);
        OPUtils.sendAppTrackerForFodAnimStyle();
    }

    private void playAnimByStyle(int value) {
        if (this.mMediaPlayer == null) {
            this.mMediaPlayer = new MediaPlayer();
        }
        this.mVideoPath = new Builder().scheme("android.resource").authority(this.mContext.getPackageName()).appendPath(String.valueOf(getCustomAnimationId(value))).build();
        try {
            this.mMediaPlayer.reset();
            this.mMediaPlayer.setDataSource(this.mContext, this.mVideoPath);
            this.mMediaPlayer.prepare();
            this.mMediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onDetached() {
        if (this.mMediaPlayer != null) {
            this.mMediaPlayer.stop();
            this.mMediaPlayer.reset();
            this.mMediaPlayer.release();
        }
        super.onDetached();
    }

    public void saveSelectedAnim() {
        setAnimStyle(((AnimEntity) this.mAnims.get(this.mSelectedAnimIndex)).animIndex);
    }

    public void onViewVisible(boolean videoPaused) {
        this.mVideoPaused = videoPaused;
        if (this.mVideoReady && this.mMediaPlayer != null && !this.mMediaPlayer.isPlaying()) {
            this.mMediaPlayer.seekTo(0);
        }
    }

    public void onViewInvisible() {
        if (this.mMediaPlayer != null && this.mMediaPlayer.isPlaying()) {
            this.mMediaPlayer.pause();
        }
    }

    public boolean isVideoPaused() {
        return this.mVideoPaused;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void updateAspectRatio() {
        this.mAspectRadio = ((float) this.mMediaPlayer.getVideoWidth()) / ((float) this.mMediaPlayer.getVideoHeight());
    }

    private void initStyleAnimViews() {
        AnimEntity anim1;
        AnimEntity anim2;
        AnimEntity anim3;
        this.mAnims.clear();
        AnimEntity anim0 = null;
        if (CUSTOM_TYPE.MCL.equals(OpCustomizeSettings.getCustomType())) {
            anim0 = new AnimEntity(this.mContext.getString(R.string.op_theme_3_title), R.drawable.op_img_fod_00, 3);
        }
        AnimEntity anim4 = null;
        if (OPUtils.isSM8150Products()) {
            anim1 = new AnimEntity(this.mContext.getString(R.string.oneplus_select_fingerprint_animation_effect_1), R.drawable.op_img_fod_01, 0);
            anim2 = new AnimEntity(this.mContext.getString(R.string.oneplus_select_fingerprint_animation_effect_4), R.drawable.op_img_fod_02, 1);
            anim3 = new AnimEntity(this.mContext.getString(R.string.oneplus_select_fingerprint_animation_effect_3), R.drawable.op_img_fod_03, 2);
            anim4 = new AnimEntity(this.mContext.getString(R.string.oneplus_select_fingerprint_animation_effect_none), R.drawable.op_img_fod_04, 9);
        } else {
            anim1 = new AnimEntity(this.mContext.getString(R.string.oneplus_select_fingerprint_animation_effect_1), R.drawable.op_img_fod_01, 0);
            anim2 = new AnimEntity(this.mContext.getString(R.string.oneplus_select_fingerprint_animation_effect_2), R.drawable.op_img_fod_02, 1);
            anim3 = new AnimEntity(this.mContext.getString(R.string.oneplus_select_fingerprint_animation_effect_3), R.drawable.op_img_fod_03, 2);
        }
        int style = System.getIntForUser(this.mContext.getContentResolver(), OPConstants.OP_CUSTOM_UNLOCK_ANIMATION_STYLE_KEY, 0, -2);
        if (style != 9) {
            switch (style) {
                case 0:
                    anim1.selected = true;
                    break;
                case 1:
                    anim2.selected = true;
                    break;
                case 2:
                    anim3.selected = true;
                    break;
                case 3:
                    anim0.selected = true;
                    break;
                default:
                    anim1.selected = true;
                    break;
            }
        } else if (anim4 != null) {
            anim4.selected = true;
        }
        if (anim0 != null) {
            this.mAnims.add(anim0);
        }
        this.mAnims.add(anim1);
        this.mAnims.add(anim2);
        this.mAnims.add(anim3);
        if (anim4 != null) {
            this.mAnims.add(anim4);
        }
    }

    private int getSelectedAnimIndex() {
        for (int i = 0; i < this.mAnims.size(); i++) {
            if (((AnimEntity) this.mAnims.get(i)).selected) {
                return i;
            }
        }
        return 0;
    }

    private void setSelectedAnim(int index) {
        for (int i = 0; i < this.mAnims.size(); i++) {
            if (index == i) {
                ((AnimEntity) this.mAnims.get(i)).selected = true;
            } else {
                ((AnimEntity) this.mAnims.get(i)).selected = false;
            }
        }
    }

    private boolean isNavigationTypeIsGesture() {
        boolean z = false;
        try {
            if (System.getInt(this.mContext.getContentResolver(), "op_navigation_bar_type") == 3) {
                z = true;
            }
            return z;
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }
}
