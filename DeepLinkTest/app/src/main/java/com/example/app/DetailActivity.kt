package com.example.app

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PRODUCT_ID = "extra_product_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Simple UI created programmatically for brevity
        val tv = TextView(this).apply {
            textSize = 18f
            setPadding(24, 24, 24, 24)
        }
        setContentView(tv)

        val productId = intent?.getStringExtra(EXTRA_PRODUCT_ID)
        val uri = intent?.data
        tv.text = buildString {
            append("Product ID: ${productId ?: "N/A"}")
            uri?.let {
                append("\n\nDeep link URI:\n$it")
            }
        }
    }
}