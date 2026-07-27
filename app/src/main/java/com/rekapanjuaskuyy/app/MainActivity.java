package com.rekapanjuaskuyy.app;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.core.view.WindowCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Locale;

public class MainActivity extends Activity {

    private static final String HOME_URL = "https://juaskuyy.my.id";
    private static final int STORAGE_PERMISSION_REQUEST = 1001;

    private WebView webView;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Mengaktifkan tampilan edge-to-edge.
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        // Membuat status bar dan navigation bar transparan.
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);

        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
        swipeRefresh = findViewById(R.id.swipeRefresh);

        requestLegacyStoragePermission();
        configureWebView();

        swipeRefresh.setOnRefreshListener(() -> webView.reload());

        webView.loadUrl(HOME_URL);
    }

    private void requestLegacyStoragePermission() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P
                && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_REQUEST
            );
        }
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
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

        webView.addJavascriptInterface(
                new DownloadBridge(this),
                "AndroidDownload"
        );

        webView.setDownloadListener(
                (url, userAgent, contentDisposition, mimeType, contentLength) -> {

                    if (url == null || url.trim().isEmpty()) {
                        showToast("File tidak dapat diunduh");
                        return;
                    }

                    if (url.startsWith("blob:")
                            || url.startsWith("data:")) {

                        downloadBlobFile(
                                url,
                                mimeType,
                                contentDisposition
                        );

                    } else {

                        downloadNetworkFile(
                                url,
                                userAgent,
                                contentDisposition,
                                mimeType
                        );
                    }
                }
        );

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(
                    WebView view,
                    WebResourceRequest request
            ) {
                return handleUrl(
                        request.getUrl().toString()
                );
            }

            @Override
            public void onPageStarted(
                    WebView view,
                    String url,
                    Bitmap favicon
            ) {
                super.onPageStarted(view, url, favicon);

                progressBar.setVisibility(
                        ProgressBar.VISIBLE
                );
            }

            @Override
            public void onPageFinished(
                    WebView view,
                    String url
            ) {
                super.onPageFinished(view, url);

                progressBar.setVisibility(
                        ProgressBar.GONE
                );

                swipeRefresh.setRefreshing(false);
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onProgressChanged(
                    WebView view,
                    int progress
            ) {
                progressBar.setProgress(progress);

                progressBar.setVisibility(
                        progress >= 100
                                ? ProgressBar.GONE
                                : ProgressBar.VISIBLE
                );
            }
        });
    }

    private void downloadNetworkFile(
            String url,
            String userAgent,
            String contentDisposition,
            String mimeType
    ) {
        try {
            String fileName = URLUtil.guessFileName(
                    url,
                    contentDisposition,
                    mimeType
            );

            fileName = sanitizeFileName(fileName);

            DownloadManager.Request request =
                    new DownloadManager.Request(
                            Uri.parse(url)
                    );

            request.setTitle(fileName);

            request.setDescription(
                    "Mengunduh file Rekapan Juaskuyy"
            );

            request.setNotificationVisibility(
                    DownloadManager.Request
                            .VISIBILITY_VISIBLE_NOTIFY_COMPLETED
            );

            request.setAllowedOverMetered(true);
            request.setAllowedOverRoaming(true);

            if (mimeType != null
                    && !mimeType.isEmpty()) {

                request.setMimeType(mimeType);
            }

            if (userAgent != null) {
                request.addRequestHeader(
                        "User-Agent",
                        userAgent
                );
            }

            String cookies =
                    CookieManager.getInstance()
                            .getCookie(url);

            if (cookies != null) {
                request.addRequestHeader(
                        "Cookie",
                        cookies
                );
            }

            request.setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    fileName
            );

            DownloadManager manager =
                    (DownloadManager)
                            getSystemService(
                                    DOWNLOAD_SERVICE
                            );

            manager.enqueue(request);

            showToast(
                    "File sedang diunduh ke folder Download"
            );

        } catch (Exception error) {

            showToast("Gagal mengunduh file");
        }
    }

    private void downloadBlobFile(
            String url,
            String mimeType,
            String contentDisposition
    ) {
        String fallbackName =
                URLUtil.guessFileName(
                        url,
                        contentDisposition,
                        mimeType
                );

        fallbackName =
                sanitizeFileName(fallbackName);

        String safeUrl =
                escapeForJavaScript(url);

        String safeMime =
                escapeForJavaScript(
                        mimeType == null
                                ? "application/octet-stream"
                                : mimeType
                );

        String safeName =
                escapeForJavaScript(
                        fallbackName
                );

        String script =
                "(async function(){"
                        + "try{"
                        + "const response=await fetch('"
                        + safeUrl
                        + "');"
                        + "const blob=await response.blob();"
                        + "const reader=new FileReader();"
                        + "reader.onloadend=function(){"
                        + "AndroidDownload.saveBase64("
                        + "reader.result,'"
                        + safeMime
                        + "','"
                        + safeName
                        + "');"
                        + "};"
                        + "reader.readAsDataURL(blob);"
                        + "}catch(e){"
                        + "AndroidDownload.downloadFailed();"
                        + "}"
                        + "})();";

        webView.evaluateJavascript(
                script,
                null
        );

        showToast("Menyiapkan file...");
    }

    private boolean handleUrl(String url) {
        Uri uri = Uri.parse(url);

        String scheme =
                uri.getScheme() == null
                        ? ""
                        : uri.getScheme()
                        .toLowerCase(Locale.ROOT);

        String host =
                uri.getHost() == null
                        ? ""
                        : uri.getHost()
                        .toLowerCase(Locale.ROOT);

        if ((scheme.equals("https")
                || scheme.equals("http"))
                && (host.equals("juaskuyy.my.id")
                || host.endsWith(
                        ".juaskuyy.my.id"
                ))) {

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
            Intent intent =
                    url.startsWith("intent://")
                            ? Intent.parseUri(
                            url,
                            Intent.URI_INTENT_SCHEME
                    )
                            : new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(url)
                    );

            startActivity(intent);

        } catch (Exception error) {

            showToast(
                    "Aplikasi untuk membuka tautan tidak ditemukan"
            );
        }
    }

    private String sanitizeFileName(
            String fileName
    ) {
        if (fileName == null
                || fileName.trim().isEmpty()) {

            return "rekapan-juaskuyy-"
                    + System.currentTimeMillis()
                    + ".pdf";
        }

        return fileName.replaceAll(
                "[\\\\/:*?\"<>|]",
                "_"
        );
    }

    private String escapeForJavaScript(
            String value
    ) {
        return value
                .replace("\\", "\\\\")
                .replace("'", "\\'");
    }

    private void saveBase64ToDownloads(
            String dataUrl,
            String mimeType,
            String fileName
    ) {
        try {
            String base64Data =
                    dataUrl.contains(",")
                            ? dataUrl.substring(
                            dataUrl.indexOf(',') + 1
                    )
                            : dataUrl;

            byte[] fileBytes =
                    Base64.decode(
                            base64Data,
                            Base64.DEFAULT
                    );

            String finalName =
                    sanitizeFileName(fileName);

            if (Build.VERSION.SDK_INT
                    >= Build.VERSION_CODES.Q) {

                ContentValues values =
                        new ContentValues();

                values.put(
                        MediaStore.Downloads.DISPLAY_NAME,
                        finalName
                );

                values.put(
                        MediaStore.Downloads.MIME_TYPE,
                        mimeType == null
                                ? "application/octet-stream"
                                : mimeType
                );

                values.put(
                        MediaStore.Downloads.RELATIVE_PATH,
                        Environment.DIRECTORY_DOWNLOADS
                );

                values.put(
                        MediaStore.Downloads.IS_PENDING,
                        1
                );

                Uri uri =
                        getContentResolver().insert(
                                MediaStore.Downloads
                                        .EXTERNAL_CONTENT_URI,
                                values
                        );

                if (uri == null) {
                    throw new IllegalStateException(
                            "Tidak dapat membuat file"
                    );
                }

                try (OutputStream output =
                             getContentResolver()
                                     .openOutputStream(uri)) {

                    if (output == null) {
                        throw new IllegalStateException(
                                "Tidak dapat membuka file"
                        );
                    }

                    output.write(fileBytes);
                }

                values.clear();

                values.put(
                        MediaStore.Downloads.IS_PENDING,
                        0
                );

                getContentResolver().update(
                        uri,
                        values,
                        null,
                        null
                );

            } else {

                File downloads =
                        Environment
                                .getExternalStoragePublicDirectory(
                                        Environment
                                                .DIRECTORY_DOWNLOADS
                                );

                if (!downloads.exists()
                        && !downloads.mkdirs()) {

                    throw new IllegalStateException(
                            "Folder Download tidak tersedia"
                    );
                }

                File destination =
                        new File(
                                downloads,
                                finalName
                        );

                try (FileOutputStream output =
                             new FileOutputStream(
                                     destination
                             )) {

                    output.write(fileBytes);
                }
            }

            runOnUiThread(
                    () -> showToast(
                            "File tersimpan di folder Download"
                    )
            );

        } catch (Exception error) {

            runOnUiThread(
                    () -> showToast(
                            "Gagal menyimpan file"
                    )
            );
        }
    }

    private void showToast(String message) {
        Toast.makeText(
                this,
                message,
                Toast.LENGTH_SHORT
        ).show();
    }

    private class DownloadBridge {

        private final Context context;

        DownloadBridge(Context context) {
            this.context = context;
        }

        @JavascriptInterface
        public void saveBase64(
                String dataUrl,
                String mimeType,
                String fileName
        ) {
            saveBase64ToDownloads(
                    dataUrl,
                    mimeType,
                    fileName
            );
        }

        @JavascriptInterface
        public void downloadFailed() {
            runOnUiThread(
                    () -> Toast.makeText(
                            context,
                            "File gagal disiapkan",
                            Toast.LENGTH_SHORT
                    ).show()
            );
        }
    }

    @Override
    public void onBackPressed() {
        if (webView != null
                && webView.canGoBack()) {

            webView.goBack();

        } else {

            super.onBackPressed();
        }
    }
        }
