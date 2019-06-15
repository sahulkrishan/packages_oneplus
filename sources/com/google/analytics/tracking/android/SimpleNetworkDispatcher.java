package com.google.analytics.tracking.android;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Build.VERSION;
import android.text.TextUtils;
import com.google.android.gms.common.util.VisibleForTesting;
import com.google.common.net.HttpHeaders;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;

class SimpleNetworkDispatcher implements Dispatcher {
    private static final String USER_AGENT_TEMPLATE = "%s/%s (Linux; U; Android %s; %s; %s Build/%s)";
    private final Context ctx;
    private GoogleAnalytics gaInstance;
    private final HttpClient httpClient;
    private URL mOverrideHostUrl;
    private final String userAgent;

    @VisibleForTesting
    SimpleNetworkDispatcher(HttpClient httpClient, GoogleAnalytics gaInstance, Context ctx) {
        this.ctx = ctx.getApplicationContext();
        this.userAgent = createUserAgentString("GoogleAnalytics", "3.0", VERSION.RELEASE, Utils.getLanguage(Locale.getDefault()), Build.MODEL, Build.ID);
        this.httpClient = httpClient;
        this.gaInstance = gaInstance;
    }

    SimpleNetworkDispatcher(HttpClient httpClient, Context ctx) {
        this(httpClient, GoogleAnalytics.getInstance(ctx), ctx);
    }

    public boolean okToDispatch() {
        NetworkInfo network = ((ConnectivityManager) this.ctx.getSystemService("connectivity")).getActiveNetworkInfo();
        if (network != null && network.isConnected()) {
            return true;
        }
        Log.v("...no network connectivity");
        return false;
    }

    public int dispatchHits(List<Hit> hits) {
        SimpleNetworkDispatcher simpleNetworkDispatcher = this;
        int maxHits = Math.min(hits.size(), 40);
        int i = 0;
        boolean firstSend = true;
        int hitsDispatched = 0;
        while (i < maxHits) {
            Hit hit = (Hit) hits.get(i);
            URL url = simpleNetworkDispatcher.getUrl(hit);
            if (url != null) {
                HttpHost targetHost = new HttpHost(url.getHost(), url.getPort(), url.getProtocol());
                String params = TextUtils.isEmpty(hit.getHitParams()) ? "" : HitBuilder.postProcessHit(hit, System.currentTimeMillis());
                HttpEntityEnclosingRequest request = simpleNetworkDispatcher.buildRequest(params, url.getPath());
                if (request != null) {
                    request.addHeader(HttpHeaders.HOST, targetHost.toHostString());
                    if (Log.isVerbose()) {
                        simpleNetworkDispatcher.logDebugInformation(request);
                    }
                    if (params.length() > 8192) {
                        Log.w("Hit too long (> 8192 bytes)--not sent");
                    } else if (simpleNetworkDispatcher.gaInstance.isDryRunEnabled()) {
                        Log.i("Dry run enabled. Hit not actually sent.");
                    } else {
                        if (firstSend) {
                            try {
                                GANetworkReceiver.sendRadioPoweredBroadcast(simpleNetworkDispatcher.ctx);
                                firstSend = false;
                            } catch (ClientProtocolException e) {
                                Log.w("ClientProtocolException sending hit; discarding hit...");
                            } catch (IOException e2) {
                                StringBuilder stringBuilder = new StringBuilder();
                                stringBuilder.append("Exception sending hit: ");
                                stringBuilder.append(e2.getClass().getSimpleName());
                                Log.w(stringBuilder.toString());
                                Log.w(e2.getMessage());
                                return hitsDispatched;
                            }
                        }
                        ClientProtocolException e22 = simpleNetworkDispatcher.httpClient.execute(targetHost, request);
                        int statusCode = e22.getStatusLine().getStatusCode();
                        HttpEntity entity = e22.getEntity();
                        if (entity != null) {
                            entity.consumeContent();
                        }
                        if (statusCode != 200) {
                            StringBuilder stringBuilder2 = new StringBuilder();
                            stringBuilder2.append("Bad response: ");
                            stringBuilder2.append(e22.getStatusLine().getStatusCode());
                            Log.w(stringBuilder2.toString());
                        }
                    }
                }
            } else if (Log.isVerbose()) {
                StringBuilder stringBuilder3 = new StringBuilder();
                stringBuilder3.append("No destination: discarding hit: ");
                stringBuilder3.append(hit.getHitParams());
                Log.w(stringBuilder3.toString());
            } else {
                Log.w("No destination: discarding hit.");
            }
            hitsDispatched++;
            i++;
            simpleNetworkDispatcher = this;
        }
        List<Hit> list = hits;
        return hitsDispatched;
    }

    public void close() {
        this.httpClient.getConnectionManager().shutdown();
    }

    private HttpEntityEnclosingRequest buildRequest(String params, String path) {
        if (TextUtils.isEmpty(params)) {
            Log.w("Empty hit, discarding.");
            return null;
        }
        HttpEntityEnclosingRequest request;
        String full = new StringBuilder();
        full.append(path);
        full.append("?");
        full.append(params);
        full = full.toString();
        if (full.length() < 2036) {
            request = new BasicHttpEntityEnclosingRequest("GET", full);
        } else {
            HttpEntityEnclosingRequest request2 = new BasicHttpEntityEnclosingRequest("POST", path);
            try {
                request2.setEntity(new StringEntity(params));
                request = request2;
            } catch (UnsupportedEncodingException e) {
                Log.w("Encoding error, discarding hit");
                return null;
            }
        }
        request.addHeader(HttpHeaders.USER_AGENT, this.userAgent);
        return request;
    }

    private void logDebugInformation(HttpEntityEnclosingRequest request) {
        int len$;
        StringBuffer httpHeaders = new StringBuffer();
        for (Header header : request.getAllHeaders()) {
            httpHeaders.append(header.toString());
            httpHeaders.append("\n");
        }
        httpHeaders.append(request.getRequestLine().toString());
        httpHeaders.append("\n");
        if (request.getEntity() != null) {
            try {
                InputStream is = request.getEntity().getContent();
                if (is != null) {
                    len$ = is.available();
                    if (len$ > 0) {
                        byte[] b = new byte[len$];
                        is.read(b);
                        httpHeaders.append("POST:\n");
                        httpHeaders.append(new String(b));
                        httpHeaders.append("\n");
                    }
                }
            } catch (IOException e) {
                Log.v("Error Writing hit to log...");
            }
        }
        Log.v(httpHeaders.toString());
    }

    /* Access modifiers changed, original: 0000 */
    public String createUserAgentString(String product, String version, String release, String language, String model, String id) {
        return String.format(USER_AGENT_TEMPLATE, new Object[]{product, version, release, language, model, id});
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public URL getUrl(Hit hit) {
        if (this.mOverrideHostUrl != null) {
            return this.mOverrideHostUrl;
        }
        try {
            return new URL("http:".equals(hit.getHitUrlScheme()) ? "http://www.google-analytics.com/collect" : "https://ssl.google-analytics.com/collect");
        } catch (MalformedURLException e) {
            Log.e("Error trying to parse the hardcoded host url. This really shouldn't happen.");
            return null;
        }
    }

    @VisibleForTesting
    public void overrideHostUrl(String hostUrl) {
        try {
            this.mOverrideHostUrl = new URL(hostUrl);
        } catch (MalformedURLException e) {
            this.mOverrideHostUrl = null;
        }
    }
}
