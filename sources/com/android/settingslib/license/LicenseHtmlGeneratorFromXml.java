package com.android.settingslib.license;

import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

class LicenseHtmlGeneratorFromXml {
    private static final String ATTR_CONTENT_ID = "contentId";
    private static final String HTML_HEAD_STRING = "<html><head>\n<style type=\"text/css\">\nbody { padding: 0; font-family: sans-serif; }\n.same-license { background-color: #eeeeee;\n                border-top: 20px solid white;\n                padding: 10px; }\n.label { font-weight: bold; }\n.file-list { margin-left: 1em; color: blue; }\n</style>\n</head><body topmargin=\"0\" leftmargin=\"0\" rightmargin=\"0\" bottommargin=\"0\">\n<div class=\"toc\">\n<ul>";
    private static final String HTML_MIDDLE_STRING = "</ul>\n</div><!-- table of contents -->\n<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\">";
    private static final String HTML_REAR_STRING = "</table></body></html>";
    private static final String TAG = "LicenseHtmlGeneratorFromXml";
    private static final String TAG_FILE_CONTENT = "file-content";
    private static final String TAG_FILE_NAME = "file-name";
    private static final String TAG_ROOT = "licenses";
    private final Map<String, String> mContentIdToFileContentMap = new HashMap();
    private final Map<String, String> mFileNameToContentIdMap = new HashMap();
    private final List<File> mXmlFiles;

    static class ContentIdAndFileNames {
        final String mContentId;
        final List<String> mFileNameList = new ArrayList();

        ContentIdAndFileNames(String contentId) {
            this.mContentId = contentId;
        }
    }

    private LicenseHtmlGeneratorFromXml(List<File> xmlFiles) {
        this.mXmlFiles = xmlFiles;
    }

    public static boolean generateHtml(List<File> xmlFiles, File outputFile) {
        return new LicenseHtmlGeneratorFromXml(xmlFiles).generateHtml(outputFile);
    }

    private boolean generateHtml(File outputFile) {
        for (File xmlFile : this.mXmlFiles) {
            parse(xmlFile);
        }
        if (this.mFileNameToContentIdMap.isEmpty() || this.mContentIdToFileContentMap.isEmpty()) {
            return false;
        }
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(outputFile);
            generateHtml(this.mFileNameToContentIdMap, this.mContentIdToFileContentMap, writer);
            writer.flush();
            writer.close();
            return true;
        } catch (FileNotFoundException | SecurityException e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Failed to generate ");
            stringBuilder.append(outputFile);
            Log.e(str, stringBuilder.toString(), e);
            if (writer != null) {
                writer.close();
            }
            return false;
        }
    }

    private void parse(File xmlFile) {
        if (xmlFile != null && xmlFile.exists() && xmlFile.length() != 0) {
            InputStreamReader in = null;
            try {
                if (xmlFile.getName().endsWith(".gz")) {
                    in = new InputStreamReader(new GZIPInputStream(new FileInputStream(xmlFile)));
                } else {
                    in = new FileReader(xmlFile);
                }
                parse(in, this.mFileNameToContentIdMap, this.mContentIdToFileContentMap);
                in.close();
            } catch (IOException | XmlPullParserException e) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Failed to parse ");
                stringBuilder.append(xmlFile);
                Log.e(str, stringBuilder.toString(), e);
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e2) {
                        String str2 = TAG;
                        StringBuilder stringBuilder2 = new StringBuilder();
                        stringBuilder2.append("Failed to close ");
                        stringBuilder2.append(xmlFile);
                        Log.w(str2, stringBuilder2.toString());
                    }
                }
            }
        }
    }

    @VisibleForTesting
    static void parse(InputStreamReader in, Map<String, String> outFileNameToContentIdMap, Map<String, String> outContentIdToFileContentMap) throws XmlPullParserException, IOException {
        Map<String, String> fileNameToContentIdMap = new HashMap();
        Map<String, String> contentIdToFileContentMap = new HashMap();
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(in);
        parser.nextTag();
        parser.require(2, "", TAG_ROOT);
        for (int state = parser.getEventType(); state != 1; state = parser.next()) {
            if (state == 2) {
                String contentId;
                String fileName;
                if (TAG_FILE_NAME.equals(parser.getName())) {
                    contentId = parser.getAttributeValue("", ATTR_CONTENT_ID);
                    if (!TextUtils.isEmpty(contentId)) {
                        fileName = readText(parser).trim();
                        if (!TextUtils.isEmpty(fileName)) {
                            fileNameToContentIdMap.put(fileName, contentId);
                        }
                    }
                } else if (TAG_FILE_CONTENT.equals(parser.getName())) {
                    contentId = parser.getAttributeValue("", ATTR_CONTENT_ID);
                    if (!(TextUtils.isEmpty(contentId) || outContentIdToFileContentMap.containsKey(contentId) || contentIdToFileContentMap.containsKey(contentId))) {
                        fileName = readText(parser);
                        if (!TextUtils.isEmpty(fileName)) {
                            contentIdToFileContentMap.put(contentId, fileName);
                        }
                    }
                }
            }
        }
        outFileNameToContentIdMap.putAll(fileNameToContentIdMap);
        outContentIdToFileContentMap.putAll(contentIdToFileContentMap);
    }

    private static String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        StringBuffer result = new StringBuffer();
        int state = parser.next();
        while (state == 4) {
            result.append(parser.getText());
            state = parser.next();
        }
        return result.toString();
    }

    @VisibleForTesting
    static void generateHtml(Map<String, String> fileNameToContentIdMap, Map<String, String> contentIdToFileContentMap, PrintWriter writer) {
        List<String> fileNameList = new ArrayList();
        fileNameList.addAll(fileNameToContentIdMap.keySet());
        Collections.sort(fileNameList);
        writer.println(HTML_HEAD_STRING);
        int count = 0;
        Map<String, Integer> contentIdToOrderMap = new HashMap();
        List<ContentIdAndFileNames> contentIdAndFileNamesList = new ArrayList();
        for (String fileName : fileNameList) {
            String contentId = (String) fileNameToContentIdMap.get(fileName);
            if (!contentIdToOrderMap.containsKey(contentId)) {
                contentIdToOrderMap.put(contentId, Integer.valueOf(count));
                contentIdAndFileNamesList.add(new ContentIdAndFileNames(contentId));
                count++;
            }
            ((ContentIdAndFileNames) contentIdAndFileNamesList.get(((Integer) contentIdToOrderMap.get(contentId)).intValue())).mFileNameList.add(fileName);
            writer.format("<li><a href=\"#id%d\">%s</a></li>\n", new Object[]{Integer.valueOf(id), fileName});
        }
        writer.println(HTML_MIDDLE_STRING);
        count = 0;
        for (ContentIdAndFileNames contentIdAndFileNames : contentIdAndFileNamesList) {
            writer.format("<tr id=\"id%d\"><td class=\"same-license\">\n", new Object[]{Integer.valueOf(count)});
            writer.println("<div class=\"label\">Notices for file(s):</div>");
            writer.println("<div class=\"file-list\">");
            Iterator it = contentIdAndFileNames.mFileNameList.iterator();
            while (it.hasNext()) {
                writer.format("%s <br/>\n", new Object[]{(String) it.next()});
            }
            writer.println("</div><!-- file-list -->");
            writer.println("<pre class=\"license-text\">");
            writer.println((String) contentIdToFileContentMap.get(contentIdAndFileNames.mContentId));
            writer.println("</pre><!-- license-text -->");
            writer.println("</td></tr><!-- same-license -->");
            count++;
        }
        writer.println(HTML_REAR_STRING);
    }
}
