package com.example.merchant

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri

class WebViewActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "WebViewActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)

        val webView: WebView = findViewById(R.id.webView)
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadsImagesAutomatically = true
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
        webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                url: String?
            ): Boolean {
                return handleUri(Uri.parse(url))
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return handleUri(request?.url)
            }

            private fun handleUri(uri: Uri?): Boolean {
                if (uri == null) return false
                Log.d("WEBVIEW", "Clicked URL: $uri")

                // 🔑 Hand over Bank App App Links to Android
                if (
                    uri.scheme == "https" &&
                    uri.host == "www.axpco.com" &&
                    uri.path?.startsWith("/pisp/src/lisa-app-bank") == true
                ) {
                    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                        addCategory(Intent.CATEGORY_BROWSABLE)
                    }
                    startActivity(intent)
                    return true   // ⛔ DO NOT load in WebView
                }

                if (uri.scheme == "example" && uri.host == "gizmos") {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = uri
                        addCategory(Intent.CATEGORY_DEFAULT)
                        addCategory(Intent.CATEGORY_BROWSABLE)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }

                    if (intent.resolveActivity(packageManager) != null) {
                        startActivity(intent)
                    } else {
                        Log.e("WEBVIEW", "No app found for $uri")
                    }
                    return true   // 🔑 REQUIRED
                }
                return false
            }
        }
        webView.settings.allowFileAccess = true

        val baseUri = "https://paywithbank-dev.americanexpress.com/payments/v1/pisp/".toUri()
        val builtUri = baseUri.buildUpon()
            .appendQueryParameter("custom_data", "eydrZXkxJzondmFsdWUxJywgJ2tleTInOid2YWx1ZTInfQ==")
            .appendQueryParameter("locale", "en_GB")
            .appendQueryParameter("request_id", "922e7e5b4754dd9afb49a2d61a5c7bd0")
            .appendQueryParameter("timestamp", "1770006100282")
            .appendQueryParameter("merchant_id", "amex_dev_b308b688aa8740d4ba1b660bde93a98d")
            .appendQueryParameter("order_details", "eyJraWQiOiJpZF9XV1hicnF3UzRBMGFNIiwiYWxnIjoiSFMyNTYifQ.eyJhbW91bnQiOiIwLjEwIiwiY3VycmVuY3kiOiJHQlAiLCJ0aW1lc3RhbXAiOjE3NzAwMDYwODMzOTksIm9yZGVyX2lkIjoiMTc3MDAwNjA4MzM5OSIsImNvcnJlbGF0aW9uX2lkIjoiMTc3MDAwNjA4NDUxMCIsImN1c3RvbWVyX2lkIjoiMTIzNDU2Nzg5IiwicGF5bWVudF9jb250ZXh0X2NvZGUiOiJFY29tbWVyY2VHb29kcyIsImRlbGl2ZXJ5X2FkZHJlc3MiOiJleUpyYVdRaU9pSnBaRjlYVjFoaWNuRjNVelJCTUdGTklpd2laVzVqSWpvaVFURXlPRU5DUXkxSVV6STFOaUlzSW1Gc1p5STZJa0V5TlRaTFZ5SjkuM3NySlJiQ2dkZHU4NTE0Qlo3eGZ5Ukk2RHdVSExzMHlEbW5LUWxaRDRuZTR4Ym9FRFR4clBnLjd5ZVRpZjYwWjdhQWlYVnpNMFNuZHcucGJDa2lQY3BtTzY5MkJKbHdhSUtldngzQkN3Z2ZhUVJsUWFOZGlBWExsSXZXNUViNjgzNkZreUVYdTloOXhDZkc4SjNadmFjTGxMNzE4SlhWc2w3bUxUd2lrcE1fbXFOUGNqdmVRb204cFpKRmNtbm9mVWItQ2VXS0NkQmZGd1pmbElUdW1VUS1yd2ZUeGhJeFdSVHM1UGtpRzYwa09UTmhYUzg2Z0VlbmVPQkFSVGcwQ3NfcUpsb0ctLWJBWTBVVjdDcmRvOWZnbFhrdmgtNGw5ZzZONHBldUV0U0s4X1E1T0x5cFNEUzVZZ2tzQS1lekl5dENXQWdYMEJLbmIzUi5Fc3VISlh6UTJuT2h1WDVkdjEwY3NnIiwicmVkaXJlY3RfdXJsIjoiaHR0cHM6Ly9heHBjby5jb20vcGlzcC9yZWRpcmVjdFVybFBvYy9zdWNjZXNzLmh0bWwiLCJlcnJvcl9yZWRpcmVjdF91cmwiOiJodHRwczovL2F4cGNvLmNvbS9waXNwL3JlZGlyZWN0VXJsUG9jL2Vycm9yLmh0bWwiLCJzZXNzaW9uX2lkIjoiMjI0YmI5MTQ4M2ZkNGJhMGJjMzMwMWYxNmM4ZTUwMzQiLCJhcHBfbGluayI6Imh0dHBzOi8vd3d3LmF4cGNvLmNvbS9waXNwL3NyYy9saXNhLWFwcC1iYW5rL21lcmNoYW50X3JlZGlyZWN0In0.cpkDyvurC8ur504iE4cS8kjlYoIOaulsP9Q8oxyEFe8")
            .appendQueryParameter("country_code", "GB")
            .build()

        webView.loadUrl(builtUri.toString())
    }

    override fun onBackPressed() {
        val webView: WebView = findViewById(R.id.webView)
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
