package com.google.tagmanager;

import android.net.Uri;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

class PreviewManager {
    static final String BASE_DEBUG_QUERY = "&gtm_debug=x";
    private static final String CONTAINER_BASE_PATTERN = "^tagmanager.c.\\S+:\\/\\/preview\\/p\\?id=\\S+&";
    private static final String CONTAINER_DEBUG_STRING_PATTERN = ".*?&gtm_debug=x$";
    private static final String CONTAINER_PREVIEW_EXIT_URL_PATTERN = "^tagmanager.c.\\S+:\\/\\/preview\\/p\\?id=\\S+&gtm_preview=$";
    private static final String CONTAINER_PREVIEW_URL_PATTERN = "^tagmanager.c.\\S+:\\/\\/preview\\/p\\?id=\\S+&gtm_auth=\\S+&gtm_preview=\\d+(&gtm_debug=x)?$";
    static final String CTFE_URL_PATH_PREFIX = "/r?";
    private static PreviewManager sInstance;
    private volatile String mCTFEUrlPath;
    private volatile String mCTFEUrlQuery;
    private volatile String mContainerId;
    private volatile PreviewMode mPreviewMode;

    enum PreviewMode {
        NONE,
        CONTAINER,
        CONTAINER_DEBUG
    }

    PreviewManager() {
        clear();
    }

    static PreviewManager getInstance() {
        PreviewManager previewManager;
        synchronized (PreviewManager.class) {
            if (sInstance == null) {
                sInstance = new PreviewManager();
            }
            previewManager = sInstance;
        }
        return previewManager;
    }

    /* Access modifiers changed, original: declared_synchronized */
    public synchronized boolean setPreviewData(Uri data) {
        String uriStr = null;
        try {
            uriStr = URLDecoder.decode(data.toString(), "UTF-8");
            StringBuilder stringBuilder;
            if (uriStr.matches(CONTAINER_PREVIEW_URL_PATTERN)) {
                stringBuilder = new StringBuilder();
                stringBuilder.append("Container preview url: ");
                stringBuilder.append(uriStr);
                Log.v(stringBuilder.toString());
                if (uriStr.matches(CONTAINER_DEBUG_STRING_PATTERN)) {
                    this.mPreviewMode = PreviewMode.CONTAINER_DEBUG;
                } else {
                    this.mPreviewMode = PreviewMode.CONTAINER;
                }
                this.mCTFEUrlQuery = getQueryWithoutDebugParameter(data);
                if (this.mPreviewMode == PreviewMode.CONTAINER || this.mPreviewMode == PreviewMode.CONTAINER_DEBUG) {
                    stringBuilder = new StringBuilder();
                    stringBuilder.append(CTFE_URL_PATH_PREFIX);
                    stringBuilder.append(this.mCTFEUrlQuery);
                    this.mCTFEUrlPath = stringBuilder.toString();
                }
                this.mContainerId = getContainerId(this.mCTFEUrlQuery);
                return true;
            } else if (!uriStr.matches(CONTAINER_PREVIEW_EXIT_URL_PATTERN)) {
                stringBuilder = new StringBuilder();
                stringBuilder.append("Invalid preview uri: ");
                stringBuilder.append(uriStr);
                Log.w(stringBuilder.toString());
                return false;
            } else if (!getContainerId(data.getQuery()).equals(this.mContainerId)) {
                return false;
            } else {
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append("Exit preview mode for container: ");
                stringBuilder2.append(this.mContainerId);
                Log.v(stringBuilder2.toString());
                this.mPreviewMode = PreviewMode.NONE;
                this.mCTFEUrlPath = null;
                return true;
            }
        } catch (UnsupportedEncodingException e) {
            return false;
        }
    }

    private String getQueryWithoutDebugParameter(Uri data) {
        return data.getQuery().replace(BASE_DEBUG_QUERY, "");
    }

    /* Access modifiers changed, original: 0000 */
    public PreviewMode getPreviewMode() {
        return this.mPreviewMode;
    }

    /* Access modifiers changed, original: 0000 */
    public String getCTFEUrlPath() {
        return this.mCTFEUrlPath;
    }

    /* Access modifiers changed, original: 0000 */
    public String getContainerId() {
        return this.mContainerId;
    }

    /* Access modifiers changed, original: 0000 */
    public String getCTFEUrlDebugQuery() {
        return this.mCTFEUrlQuery;
    }

    /* Access modifiers changed, original: 0000 */
    public void clear() {
        this.mPreviewMode = PreviewMode.NONE;
        this.mCTFEUrlPath = null;
        this.mContainerId = null;
        this.mCTFEUrlQuery = null;
    }

    private String getContainerId(String query) {
        return query.split("&")[0].split("=")[1];
    }
}
