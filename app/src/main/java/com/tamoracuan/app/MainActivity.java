package com.tamoracuan.app;

import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String HOME = "https://tamoracuan.com/";
    private static final int FILE_CHOOSER_REQUEST = 1403;
    private static final int GOLD = Color.rgb(212, 169, 76);
    private static final int DARK = Color.rgb(15, 12, 15);

    private FrameLayout root;
    private WebView webView;
    private View loadingOverlay;
    private View errorOverlay;
    private WebChromeClient.CustomViewCallback fullScreenCallback;
    private View fullScreenView;
    private android.webkit.ValueCallback<Uri[]> fileCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(DARK);
        getWindow().setNavigationBarColor(DARK);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        root = new FrameLayout(this);
        root.setBackgroundColor(DARK);
        setContentView(root);

        webView = new WebView(this);
        configureWebView(webView);
        root.addView(webView, new FrameLayout.LayoutParams(-1, -1));

        loadingOverlay = makeOverlay("TAMORACUAN", "Memuat aplikasi...", false);
        root.addView(loadingOverlay, new FrameLayout.LayoutParams(-1, -1));

        if (savedInstanceState != null && webView.restoreState(savedInstanceState) != null) {
            loadingOverlay.setVisibility(View.GONE);
        } else {
            webView.loadUrl(HOME);
        }
    }

    private void configureWebView(WebView target) {
        target.setBackgroundColor(DARK);
        target.setOverScrollMode(View.OVER_SCROLL_NEVER);
        WebSettings settings = target.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setSupportMultipleWindows(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        settings.setMediaPlaybackRequiresUserGesture(true);
        if (Build.VERSION.SDK_INT >= 26) {
            settings.setSafeBrowsingEnabled(true);
        }

        CookieManager cookies = CookieManager.getInstance();
        cookies.setAcceptCookie(true);
        cookies.setAcceptThirdPartyCookies(target, true);
        target.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return handleExternal(request.getUrl());
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (view == webView && loadingOverlay != null) {
                    loadingOverlay.setVisibility(View.GONE);
                }
                CookieManager.getInstance().flush();
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (view == webView && request.isForMainFrame()) {
                    showError();
                }
            }
        });

        target.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView view,
                    android.webkit.ValueCallback<Uri[]> callback,
                    FileChooserParams params) {
                if (fileCallback != null) fileCallback.onReceiveValue(null);
                fileCallback = callback;
                try {
                    startActivityForResult(params.createIntent(), FILE_CHOOSER_REQUEST);
                    return true;
                } catch (ActivityNotFoundException ex) {
                    fileCallback = null;
                    callback.onReceiveValue(null);
                    Toast.makeText(MainActivity.this, "Pemilih file tidak tersedia", Toast.LENGTH_SHORT).show();
                    return true;
                }
            }

            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
                final Dialog dialog = new Dialog(MainActivity.this, android.R.style.Theme_Material_Light_NoActionBar);
                final WebView popup = new WebView(MainActivity.this);
                configureWebView(popup);
                dialog.setContentView(popup, new ViewGroup.LayoutParams(-1, -1));
                dialog.setOnDismissListener(d -> popup.destroy());
                popup.setWebChromeClient(new WebChromeClient() {
                    @Override
                    public void onCloseWindow(WebView window) { dialog.dismiss(); }
                    @Override
                    public boolean onShowFileChooser(WebView w, android.webkit.ValueCallback<Uri[]> c, FileChooserParams p) {
                        if (fileCallback != null) fileCallback.onReceiveValue(null);
                        fileCallback = c;
                        try {
                            startActivityForResult(p.createIntent(), FILE_CHOOSER_REQUEST);
                            return true;
                        } catch (ActivityNotFoundException e) {
                            fileCallback = null;
                            c.onReceiveValue(null);
                            return true;
                        }
                    }
                });
                dialog.show();
                Window window = dialog.getWindow();
                if (window != null) {
                    window.setLayout(-1, -1);
                    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                }
                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                transport.setWebView(popup);
                resultMsg.sendToTarget();
                return true;
            }

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                if (fullScreenView != null) {
                    callback.onCustomViewHidden();
                    return;
                }
                fullScreenView = view;
                fullScreenCallback = callback;
                root.addView(view, new FrameLayout.LayoutParams(-1, -1));
                webView.setVisibility(View.GONE);
            }

            @Override
            public void onHideCustomView() { hideCustomView(); }
        });

        target.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition,
                                        String mimeType, long contentLength) {
                downloadFile(url, userAgent, contentDisposition, mimeType);
            }
        });
    }

    private boolean handleExternal(Uri uri) {
        if (uri == null) return false;
        String scheme = uri.getScheme();
        if (scheme == null) return false;
        if (scheme.equalsIgnoreCase("https") || scheme.equalsIgnoreCase("http")) return false;
        try {
            Intent intent;
            if (scheme.equalsIgnoreCase("intent")) {
                intent = Intent.parseUri(uri.toString(), Intent.URI_INTENT_SCHEME);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setComponent(null);
                intent.setSelector(null);
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    String fallback = intent.getStringExtra("browser_fallback_url");
                    if (fallback != null) {
                        Uri fb = Uri.parse(fallback);
                        if ("https".equalsIgnoreCase(fb.getScheme())) webView.loadUrl(fallback);
                    }
                }
                return true;
            }
            intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Aplikasi tujuan tidak tersedia", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    private View makeOverlay(String heading, String subheading, boolean withRetry) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(35, 35, 35, 35);
        layout.setBackgroundColor(DARK);
        TextView title = new TextView(this);
        title.setText(heading);
        title.setTextSize(25);
        title.setTextColor(GOLD);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        layout.addView(title);
        TextView subtitle = new TextView(this);
        subtitle.setText(subheading);
        subtitle.setTextColor(Color.WHITE);
        subtitle.setTextSize(14);
        subtitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams subParams = new LinearLayout.LayoutParams(-2, -2);
        subParams.topMargin = 16;
        layout.addView(subtitle, subParams);
        if (withRetry) {
            Button retry = new Button(this);
            retry.setText("Coba lagi");
            retry.setOnClickListener(v -> {
                if (errorOverlay != null) errorOverlay.setVisibility(View.GONE);
                if (loadingOverlay != null) loadingOverlay.setVisibility(View.VISIBLE);
                webView.reload();
            });
            LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(-2, -2);
            btnParams.topMargin = 24;
            layout.addView(retry, btnParams);
        } else {
            ProgressBar spinner = new ProgressBar(this);
            LinearLayout.LayoutParams spinnerParams = new LinearLayout.LayoutParams(-2, -2);
            spinnerParams.topMargin = 24;
            layout.addView(spinner, spinnerParams);
        }
        return layout;
    }

    private void showError() {
        if (errorOverlay == null) {
            errorOverlay = makeOverlay("Tidak dapat terhubung", "Periksa internet, lalu coba lagi.", true);
            root.addView(errorOverlay, new FrameLayout.LayoutParams(-1, -1));
        } else {
            errorOverlay.setVisibility(View.VISIBLE);
        }
    }

    private void downloadFile(String url, String agent, String disposition, String mimeType) {
        try {
            Uri uri = Uri.parse(url);
            if (!"https".equalsIgnoreCase(uri.getScheme())) {
                Toast.makeText(this, "Unduhan ini tidak didukung", Toast.LENGTH_LONG).show();
                return;
            }
            DownloadManager.Request request = new DownloadManager.Request(uri);
            String name = URLUtil.guessFileName(url, disposition, mimeType);
            name = name.replaceAll("[\\\\/:*?\"<>|]", "_");
            request.setTitle(name);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            if (mimeType != null) request.setMimeType(mimeType);
            if (agent != null) request.addRequestHeader("User-Agent", agent);
            String cookies = CookieManager.getInstance().getCookie(url);
            if (cookies != null) request.addRequestHeader("Cookie", cookies);
            if (Build.VERSION.SDK_INT >= 29) {
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name);
            } else {
                request.setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, name);
            }
            DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            dm.enqueue(request);
            Toast.makeText(this, "Mengunduh: " + name, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Unduhan gagal dimulai", Toast.LENGTH_LONG).show();
        }
    }

    private void hideCustomView() {
        if (fullScreenView == null) return;
        root.removeView(fullScreenView);
        fullScreenView = null;
        webView.setVisibility(View.VISIBLE);
        if (fullScreenCallback != null) {
            fullScreenCallback.onCustomViewHidden();
            fullScreenCallback = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_REQUEST && fileCallback != null) {
            Uri[] uris = WebChromeClient.FileChooserParams.parseResult(resultCode, data);
            fileCallback.onReceiveValue(uris);
            fileCallback = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (fullScreenView != null) {
            hideCustomView();
        } else if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        webView.saveState(state);
        super.onSaveInstanceState(state);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (webView != null) webView.onPause();
        CookieManager.getInstance().flush();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) webView.onResume();
    }

    @Override
    protected void onDestroy() {
        if (fileCallback != null) fileCallback.onReceiveValue(null);
        if (webView != null) {
            root.removeView(webView);
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}
