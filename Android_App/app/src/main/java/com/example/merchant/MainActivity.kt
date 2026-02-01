package com.example.merchant

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var tvStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tvStatus)

        // Handle the intent data
        handleIntent(intent)

        val btnCheckout: Button = findViewById(R.id.btnCheckout)
        btnCheckout.setOnClickListener {
            val intent = Intent(this, WebViewActivity::class.java)
            startActivity(intent)
        }

    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val appLinkAction: String? = intent?.action
        val appLinkData: Uri? = intent?.data
        
        Log.d(TAG, "handleIntent: action=$appLinkAction, data=$appLinkData")

        val params = parseQueryParameters(appLinkData)
        if (params.isNotEmpty()) {
            val joined = params.entries.joinToString("\n") { "${it.key}: ${it.value}" }
            tvStatus.text = "Checkout Status:\n$joined"
            Toast.makeText(this, "Response Received", Toast.LENGTH_SHORT).show()
        }
    }

    private fun parseQueryParameters(uri: Uri?): Map<String, String> {
        val map = mutableMapOf<String, String>()
        if (uri == null) return map

        try {
            val names = uri.queryParameterNames
            if (names.isNotEmpty()) {
                names.forEach { name ->
                    map[name] = uri.getQueryParameter(name) ?: ""
                }
            } else {
                val raw = uri.query
                if (!raw.isNullOrEmpty()) {
                    raw.split("&").mapNotNull { pair ->
                        val idx = pair.indexOf("=")
                        if (idx > 0) {
                            val k = Uri.decode(pair.take(idx))
                            val v = Uri.decode(pair.substring(idx + 1))
                            k to v
                        } else null
                    }.forEach { (k, v) -> map[k] = v }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "parseQueryParameters: failed", e)
        }
        return map
    }
}
