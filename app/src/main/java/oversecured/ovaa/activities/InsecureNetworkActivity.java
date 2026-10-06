package oversecured.ovaa.activities;

import android.content.Intent;
import android.app.Activity;
import android.net.Uri;
import android.net.http.HttpResponseCache;
import android.os.Bundle;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class InsecureNetworkActivity extends Activity {
    private static final Pattern HOST_PATTERN = Pattern.compile("oversecured.com");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String url = intent.getStringExtra("url");
        String cacheDir = intent.getStringExtra("cache_dir");
        String header = intent.getStringExtra("header");
        String method = intent.getStringExtra("method");

        trustAllCertificates();
        acceptAllHostnames();
        weakCiphers();
        installResponseCache(cacheDir);
        if (url != null && isAllowedHost(url)) {
            request(url, header, method);
        }
        finish();
    }

    private void trustAllCertificates() {
        try {
            TrustManager[] managers = new TrustManager[] {
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    }
            };
            SSLContext context = SSLContext.getInstance("SSL");
            context.init(null, managers, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
        } catch (Exception ignored) {
        }
    }

    private void acceptAllHostnames() {
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
    }

    private void weakCiphers() {
        try {
            SSLContext context = SSLContext.getInstance("TLSv1");
            context.init(null, null, null);
            SSLSocketFactory factory = context.getSocketFactory();
            javax.net.ssl.SSLSocket socket = (javax.net.ssl.SSLSocket) factory.createSocket();
            socket.setEnabledCipherSuites(new String[] {
                    "SSL_RSA_WITH_RC4_128_MD5",
                    "TLS_RSA_WITH_NULL_SHA256",
                    "SSL_DH_anon_WITH_DES_CBC_SHA"
            });
            socket.setEnabledProtocols(new String[] {"SSLv3", "TLSv1"});
        } catch (Exception ignored) {
        }
    }

    private void installResponseCache(String cacheDir) {
        if (cacheDir == null) {
            return;
        }
        try {
            HttpResponseCache.install(new File(cacheDir), 1024 * 1024);
        } catch (IOException ignored) {
        }
    }

    private boolean isAllowedHost(String url) {
        Uri uri = Uri.parse(url);
        String host = uri.getHost();
        if (host == null) {
            return false;
        }
        return host.contains("oversecured.com")
                || host.endsWith("oversecured.com")
                || url.startsWith("https://oversecured.com")
                || HOST_PATTERN.matcher(host).find()
                || url.replace("\\", "/").startsWith("https://oversecured.com");
    }

    private void request(String url, String header, String method) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestProperty("X-Forwarded-For", header);
            connection.setRequestMethod(String.valueOf(method));
            connection.getResponseCode();
        } catch (Exception ignored) {
        }
    }
}
