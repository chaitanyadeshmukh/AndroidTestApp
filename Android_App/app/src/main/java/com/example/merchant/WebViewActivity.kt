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
            .appendQueryParameter("request_id", "2c2ba4f664505f928ade321a3d5624a3")
            .appendQueryParameter("timestamp", "1769662884072")
            .appendQueryParameter("merchant_id", "amex_dev_b308b688aa8740d4ba1b660bde93a98d")
            .appendQueryParameter("order_details", "eyJraWQiOiJpZF9XV1hicnF3UzRBMGFNIiwiYWxnIjoiSFMyNTYifQ.eyJhbW91bnQiOiIwLjEwIiwiY3VycmVuY3kiOiJHQlAiLCJ0aW1lc3RhbXAiOjE3Njk2NjI4NzU2ODAsIm9yZGVyX2lkIjoiMTc2OTY2Mjg3NTY4MCIsImNvcnJlbGF0aW9uX2lkIjoiMTc2OTY2Mjg3Njc5MSIsImN1c3RvbWVyX2lkIjoiMTIzNDU2Nzg5IiwicGF5bWVudF9jb250ZXh0X2NvZGUiOiJFY29tbWVyY2VHb29kcyIsImRlbGl2ZXJ5X2FkZHJlc3MiOiJleUpyYVdRaU9pSnBaRjlYVjFoaWNuRjNVelJCTUdGTklpd2laVzVqSWpvaVFURXlPRU5DUXkxSVV6STFOaUlzSW1Gc1p5STZJa0V5TlRaTFZ5SjkuRThRNFpyUDVqdmd4Sm5MVGI1cEVMT1B1S1dkVHk0UktGbjBtLUJILUlSSmY0UklELTdOWllRLlkzUmgtU0JJN3czaWZkZGNkc1FXancuQkZzUEF2Wi1jYmhGdkVrY2d0MmppSVlGaXRub09KQkNwOWpnOXJOb3NsdkxJRVVDbFk0OUlDdE5ib3BaRzNteWwxZ2V0T1R6c2s0dTdsTlZpNWpFWWZJNmlDYS1GNTd6aDlWclU3bHBuc1JJU0RxS0JYT2JLRGUxSVdObnNqMUNTWDFBUnRMSERzVVhrYzRzNjYtaUh2OURPdmZrT3U2ZjJ3NUZOemVxZEhONkFrcWlCbDRHYkgxODBpWldfYVFzU1NnX205N01GRDJLVWlGNVdPWVpUX2xsYWc5LWUyUEJYNFhVNkUxRlNtX2ppMFdUZjU3amU1bHNxTnlabkswOS40VUlxLXhaUkpGWXJsTTVReDJkY2h3IiwicmVkaXJlY3RfdXJsIjoiaHR0cHM6Ly9heHBjby5jb20vcGlzcC9yZWRpcmVjdFVybFBvYy9zdWNjZXNzLmh0bWwiLCJlcnJvcl9yZWRpcmVjdF91cmwiOiJodHRwczovL2F4cGNvLmNvbS9waXNwL3JlZGlyZWN0VXJsUG9jL2Vycm9yLmh0bWwiLCJzZXNzaW9uX2lkIjoiMjI0YmI5MTQ4M2ZkNGJhMGJjMzMwMWYxNmM4ZTUwMzQifQ.zmrb8e1S8eNa0WURjcpNYmaVOTLZkaFP3VILA62aF1s")
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
