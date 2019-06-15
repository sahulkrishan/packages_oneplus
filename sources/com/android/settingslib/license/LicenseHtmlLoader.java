package com.android.settingslib.license;

import android.content.Context;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import com.android.settingslib.utils.AsyncLoader;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LicenseHtmlLoader extends AsyncLoader<File> {
    private static final String[] DEFAULT_LICENSE_XML_PATHS = new String[]{"/system/etc/NOTICE.xml.gz", "/vendor/etc/NOTICE.xml.gz", "/odm/etc/NOTICE.xml.gz", "/oem/etc/NOTICE.xml.gz"};
    private static final String NOTICE_HTML_FILE_NAME = "NOTICE.html";
    private static final String TAG = "LicenseHtmlLoader";
    private Context mContext;

    public LicenseHtmlLoader(Context context) {
        super(context);
        this.mContext = context;
    }

    public File loadInBackground() {
        return generateHtmlFromDefaultXmlFiles();
    }

    /* Access modifiers changed, original: protected */
    public void onDiscardResult(File f) {
    }

    private File generateHtmlFromDefaultXmlFiles() {
        List<File> xmlFiles = getVaildXmlFiles();
        if (xmlFiles.isEmpty()) {
            Log.e(TAG, "No notice file exists.");
            return null;
        }
        File cachedHtmlFile = getCachedHtmlFile();
        if (!isCachedHtmlFileOutdated(xmlFiles, cachedHtmlFile) || generateHtmlFile(xmlFiles, cachedHtmlFile)) {
            return cachedHtmlFile;
        }
        return null;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public List<File> getVaildXmlFiles() {
        List<File> xmlFiles = new ArrayList();
        for (String xmlPath : DEFAULT_LICENSE_XML_PATHS) {
            File file = new File(xmlPath);
            if (file.exists() && file.length() != 0) {
                xmlFiles.add(file);
            }
        }
        return xmlFiles;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public File getCachedHtmlFile() {
        return new File(this.mContext.getCacheDir(), NOTICE_HTML_FILE_NAME);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public boolean isCachedHtmlFileOutdated(List<File> xmlFiles, File cachedHtmlFile) {
        if (!cachedHtmlFile.exists() || cachedHtmlFile.length() == 0) {
            return true;
        }
        for (File file : xmlFiles) {
            if (cachedHtmlFile.lastModified() < file.lastModified()) {
                return true;
            }
        }
        return false;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public boolean generateHtmlFile(List<File> xmlFiles, File htmlFile) {
        return LicenseHtmlGeneratorFromXml.generateHtml(xmlFiles, htmlFile);
    }
}
