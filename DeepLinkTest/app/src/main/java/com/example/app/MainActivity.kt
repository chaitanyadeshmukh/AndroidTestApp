package com.example.app

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivityDeepLink"
    }

    private lateinit var statusLabel: TextView
    private lateinit var continueButton: Button
    private var currentRedirectUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup programmatic UI for a "bank-like" feel
        setupBankUI()

        // Log the incoming intent and data for debugging
        Log.d(TAG, "onCreate: intent=$intent, data=${intent?.data}")

        intent?.data?.let { uri ->
            handleDeepLink(uri)
        }
    }

    /**
     * Creates a simple bank dashboard UI programmatically.
     */
    private fun setupBankUI() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#F5F5F5"))
            setPadding(48, 48, 48, 48)
        }

        // Bank Header
        val header = TextView(this).apply {
            text = "MV Virtual Bank"
            textSize = 28f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#1A237E"))
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 80)
        }
        root.addView(header)

        // Balance Card
        val balanceCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(48, 48, 48, 48)
            elevation = 12f
        }
        
        balanceCard.addView(TextView(this).apply {
            text = "Available Balance"
            textSize = 16f
            setTextColor(Color.GRAY)
        })
        
        balanceCard.addView(TextView(this).apply {
            text = "$ 12,450.85"
            textSize = 36f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.BLACK)
            setPadding(0, 16, 0, 0)
        })
        root.addView(balanceCard)

        // Status / Deep Link Info
        statusLabel = TextView(this).apply {
            text = "Deep Link Status: Waiting..."
            textSize = 14f
            setPadding(0, 60, 0, 0)
            setTextColor(Color.DKGRAY)
            gravity = Gravity.CENTER
        }
        root.addView(statusLabel)

        // Continue Account Button
        continueButton = Button(this).apply {
            text = "Continue Account"
            isEnabled = false
            alpha = 0.5f // Visual cue for disabled state
            setBackgroundColor(Color.parseColor("#BDBDBD")) // Gray when disabled
            setTextColor(Color.WHITE)
            setOnClickListener {
                currentRedirectUrl?.let { url ->
                    try {
                        Log.d(TAG, "Opening redirect URL: $url")
                        val browserIntent = Intent(Intent.ACTION_VIEW, url.toUri())
                        browserIntent.addCategory(Intent.CATEGORY_BROWSABLE)
                        browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(browserIntent)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error opening browser", e)
                        statusLabel.text = "Error: Could not open browser"
                    }
                }
            }
            // Layout params for better appearance
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 60
            }
            layoutParams = params
        }
        root.addView(continueButton)

        setContentView(root)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        Log.d(TAG, "onNewIntent: intent=$intent, data=${intent?.data}")
        intent?.data?.let { uri ->
            handleDeepLink(uri)
        }
    }

    /**
     * Parses the incoming URI and routes the user to the appropriate destination.
     * Supports legacy products, MV bank portals, and the new Lisa App Bank responses.
     */
    private fun handleDeepLink(uri: Uri) {
        Log.d(TAG, "handleDeepLink: uri=$uri")
        statusLabel.text = "Processing Deep Link..."
        
        // Reset redirect state
        currentRedirectUrl = null
        continueButton.isEnabled = false
        continueButton.alpha = 0.5f
        continueButton.setBackgroundColor(Color.parseColor("#BDBDBD")) // Gray when disabled

        val segments = uri.pathSegments
        
        if (segments.isEmpty()) {
            statusLabel.text = "Deep Link Status: No path segments"
            return
        }

        when (segments[0]) {
            "product" -> {
                val productId = segments.getOrNull(1) ?: return
                statusLabel.text = "Navigating to Product: $productId"
                navigateToDetail(uri, productId)
            }
            "pisp" -> {
                if (segments.size >= 3 && segments[1] == "src") {
                    when (segments[2]) {
                        "mv-bank" -> {
                            // Pattern: /pisp/src/mv-bank/{env}/index.html
                            val env = segments.getOrNull(3) ?: "unknown"
                            statusLabel.text = "Navigating to MV Bank ($env)"
                            navigateToDetail(uri, "MV Bank Portal ($env)")
                        }
                        "lisa-app-bank" -> {
                            // Pattern: /pisp/src/lisa-app-bank/{merchant_app}/
                            val merchantApp = segments.getOrNull(3) ?: "unknown"
                            
                            // Extract OpenID/OAuth params
                            val clientId = uri.getQueryParameter("client_id")
                            val state = uri.getQueryParameter("state")
                            val nonce = uri.getQueryParameter("nonce")
                            val scope = uri.getQueryParameter("scope")
                            val request = uri.getQueryParameter("request")
                            val redirectUri = uri.getQueryParameter("redirect_uri")
                            
                            Log.d(TAG, "Lisa App Bank - Merchant: $redirectUri")

                            // Enable button if redirect_uri is present and append state_id/nonce
                            if (redirectUri != null) {
                                val updatedRedirectUri = redirectUri.toUri().buildUpon()
                                    .appendQueryParameter("state_id", state)
                                    .appendQueryParameter("code", nonce)
                                    .appendQueryParameter("id_token", request)
                                    .build().toString().replaceFirst("?", "#")
                                
                                Log.d(TAG, "Final Redirect URI: $updatedRedirectUri")
                                currentRedirectUrl = updatedRedirectUri
                                continueButton.isEnabled = true
                                continueButton.alpha = 1.0f
                                continueButton.setBackgroundColor(Color.parseColor("#1A237E")) // Dark Blue when enabled
                            }
                            
                            statusLabel.text = "Auth Response for: $merchantApp"
                            
                            val detailInfo = buildString {
                                append("Lisa Bank Auth\n")
                                append("Merchant: $merchantApp\n")
                                append("Client: ${clientId?.take(8)}...\n")
                                append("Scope: $scope")
                            }
                            
                           // navigateToDetail(uri, detailInfo)
                        }
                        else -> {
                            statusLabel.text = "Deep Link Status: Unknown bank source ${segments[2]}"
                        }
                    }
                } else {
                    statusLabel.text = "Deep Link Status: Invalid pisp pattern"
                }
            }
            else -> {
                statusLabel.text = "Deep Link Status: Unrecognized route ${segments[0]}"
            }
        }
    }

    private fun navigateToDetail(uri: Uri, identifier: String) {
        val detailIntent = Intent(this, DetailActivity::class.java).apply {
            putExtra(DetailActivity.EXTRA_PRODUCT_ID, identifier)
            data = uri
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(detailIntent)
    }
}
