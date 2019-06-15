package com.google.tagmanager;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Build.VERSION;
import com.google.android.gms.common.util.VisibleForTesting;
import com.google.common.net.HttpHeaders;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;

class SimpleNetworkDispatcher implements Dispatcher {
    private static final String USER_AGENT_TEMPLATE = "%s/%s (Linux; U; Android %s; %s; %s Build/%s)";
    private final Context ctx;
    private DispatchListener dispatchListener;
    private final HttpClient httpClient;
    private final String userAgent = createUserAgentString("GoogleTagManager", "3.02", VERSION.RELEASE, getUserAgentLanguage(Locale.getDefault()), Build.MODEL, Build.ID);

    public interface DispatchListener {
        void onHitDispatched(Hit hit);

        void onHitPermanentDispatchFailure(Hit hit);

        void onHitTransientDispatchFailure(Hit hit);
    }

    @VisibleForTesting
    SimpleNetworkDispatcher(HttpClient httpClient, Context ctx, DispatchListener dispatchListener) {
        this.ctx = ctx.getApplicationContext();
        this.httpClient = httpClient;
        this.dispatchListener = dispatchListener;
    }

    public boolean okToDispatch() {
        NetworkInfo network = ((ConnectivityManager) this.ctx.getSystemService("connectivity")).getActiveNetworkInfo();
        if (network != null && network.isConnected()) {
            return true;
        }
        Log.v("...no network connectivity");
        return false;
    }

    public void dispatchHits(List<Hit> hits) {
        int maxHits = Math.min(hits.size(), 40);
        boolean firstSend = true;
        for (int i = 0; i < maxHits; i++) {
            Hit hit = (Hit) hits.get(i);
            URL url = getUrl(hit);
            if (url == null) {
                Log.w("No destination: discarding hit.");
                this.dispatchListener.onHitPermanentDispatchFailure(hit);
            } else {
                HttpEntityEnclosingRequest request = constructGtmRequest(url);
                if (request == null) {
                    this.dispatchListener.onHitPermanentDispatchFailure(hit);
                } else {
                    HttpHost targetHost = new HttpHost(url.getHost(), url.getPort(), url.getProtocol());
                    request.addHeader(HttpHeaders.HOST, targetHost.toHostString());
                    logDebugInformation(request);
                    if (firstSend) {
                        try {
                            NetworkReceiver.sendRadioPoweredBroadcast(this.ctx);
                            firstSend = false;
                        } catch (ClientProtocolException e) {
                            Log.w("ClientProtocolException sending hit; discarding hit...");
                            this.dispatchListener.onHitPermanentDispatchFailure(hit);
                        } catch (IOException e2) {
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append("Exception sending hit: ");
                            stringBuilder.append(e2.getClass().getSimpleName());
                            Log.w(stringBuilder.toString());
                            Log.w(e2.getMessage());
                            this.dispatchListener.onHitTransientDispatchFailure(hit);
                        }
                    }
                    ClientProtocolException e22 = this.httpClient.execute(targetHost, request);
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
                        this.dispatchListener.onHitTransientDispatchFailure(hit);
                    } else {
                        this.dispatchListener.onHitDispatched(hit);
                    }
                }
            }
        }
    }

    public void close() {
        this.httpClient.getConnectionManager().shutdown();
    }

    private HttpEntityEnclosingRequest constructGtmRequest(URL url) {
        HttpEntityEnclosingRequest request = null;
        try {
            request = new BasicHttpEntityEnclosingRequest("GET", url.toURI().toString());
            request.addHeader(HttpHeaders.USER_AGENT, this.userAgent);
            return request;
        } catch (URISyntaxException e) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Exception sending hit: ");
            stringBuilder.append(e.getClass().getSimpleName());
            Log.w(stringBuilder.toString());
            Log.w(e.getMessage());
            return request;
        }
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

    static String getUserAgentLanguage(Locale locale) {
        if (locale == null || locale.getLanguage() == null || locale.getLanguage().length() == 0) {
            return null;
        }
        StringBuilder lang = new StringBuilder();
        lang.append(locale.getLanguage().toLowerCase());
        if (!(locale.getCountry() == null || locale.getCountry().length() == 0)) {
            lang.append("-");
            lang.append(locale.getCountry().toLowerCase());
        }
        return lang.toString();
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public URL getUrl(Hit hit) {
        try {
            return new URL(hit.getHitUrl());
        } catch (MalformedURLException e) {
            Log.e("Error trying to parse the GTM url.");
            return null;
        }
    }
}
