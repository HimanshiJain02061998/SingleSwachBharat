package com.appynitty.kotlinsbalibrary.common.ui.privacyPolicy

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import android.view.MenuItem
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import com.appynitty.kotlinsbalibrary.R
import com.appynitty.kotlinsbalibrary.common.utils.BackBtnPressedUtil
import com.appynitty.kotlinsbalibrary.databinding.ActivityPrivacyPolicyBinding


class PrivacyPolicyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrivacyPolicyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPrivacyPolicyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initToolbar()
        BackBtnPressedUtil.handleBackBtnPressed(this, this, this)

        setUpWebView()
    }


    @SuppressLint("SetJavaScriptEnabled")
    private fun setUpWebView() {

        binding.privacyWebView.webViewClient = WebViewClient()
        binding.privacyWebView.loadUrl("http://202.65.157.253:4055/PrivacyPolicyBhor.html")

        val webSettings: WebSettings = binding.privacyWebView.settings
        webSettings.javaScriptEnabled = true

    }

    private fun initToolbar() {
        binding.toolbar.title = resources.getString(R.string.privacy_policy_activity)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && binding.privacyWebView.canGoBack()) {
            binding.privacyWebView.goBack()
            return true
        }
        // If it wasn't the Back key or there's no webpage history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> BackBtnPressedUtil.exitOnBackPressed(this)
        }
        return super.onOptionsItemSelected(item)
    }

}