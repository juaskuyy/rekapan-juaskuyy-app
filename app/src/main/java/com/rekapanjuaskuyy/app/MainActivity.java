package com.rekapanjuaskuyy.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class MainActivity extends Activity {

    private static final String HOME_URL = "https://juaskuyy.my.id";

    private WebView webView;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
        swipeRefresh = findViewById(R.id.swipeRefresh);

        configureWebView();

        swipeRefresh.setOnRefreshListener(() -> webView.reload());

        webView.loadUrl(HOME_URL);
    }

    private void configureWebView() {
        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setMediaPlaybackRequiresUserGesture(false);

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(
                    WebView view,
                    WebResourceRequest request
            ) {
                return handleUrl(request.getUrl().toString());
            }

            @Override
            public void onPageStarted(
                    WebView view,
                    String url,
                    Bitmap favicon
            ) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(ProgressBar.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                progressBar.setVisibility(ProgressBar.GONE);
                swipeRefresh.setRefreshing(false);
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int progress) {
                progressBar.setProgress(progress);

                if (progress >= 100) {
                    progressBar.setVisibility(ProgressBar.GONE);
                } else {
                    progressBar.setVisibility(ProgressBar.VISIBLE);
                }
            }
        });
    }

    private boolean handleUrl(String url) {
        Uri uri = Uri.parse(url);

        String scheme = uri.getScheme() == null
                ? ""
                : uri.getScheme().toLowerCase();

        String host = uri.getHost() == null
                ? ""
                : uri.getHost().toLowerCase();

        if ((scheme.equals("https") || scheme.equals("http"))
                && (host.equals("juaskuyy.my.id")
                || host.endsWith(".juaskuyy.my.id"))) {
            return false;
        }

        if (scheme.equals("http")
                || scheme.equals("https")
                || scheme.equals("mailto")
                || scheme.equals("tel")
                || scheme.equals("sms")
                || scheme.equals("market")
                || scheme.equals("intent")
                || scheme.equals("whatsapp")
                || scheme.equals("tg")) {

            openExternal(url);
            return true;
        }

        return false;
    }

    private void openExternal(String url) {
        try {
            Intent intent;

            if (url.startsWith("intent://")) {
                intent = Intent.parseUri(
                        url,
                        Intent.URI_INTENT_SCHEME
                );
            } else {
                intent = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(url)
                );
            }

            startActivity(intent);

        } catch (Exception error) {
            Toast.makeText(
                    this,
                    "Aplikasi untuk membuka tautan tidak ditemukan",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        if (webView != null) {
            webView.onPause();
        }

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (webView != null) {
            webView.onResume();
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
        }

        super.onDestroy();
    }
}
