package com.lee.webvideo

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_web_video.*
import org.json.JSONObject

class WebVideoActivity : AppCompatActivity(), VideoWebViewOwner {

    private lateinit var chromeClient: VideoChromeClient

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_video)
        chromeClient = VideoChromeClient(this)
        web_view.settings.javaScriptEnabled = true
        web_view.webChromeClient = chromeClient
        web_view.webViewClient = CustomWebViewClient()
        web_view.loadUrl("https://b23.tv/wBOJJr")
    }

    override fun onDestroy() {
        super.onDestroy()
        web_view.destroy()
    }

    override fun onBackPressed() {
        if (chromeClient.onBackPressed()) {
            if (web_view.canGoBack()) {
                web_view.goBack()
                return
            }
            super.onBackPressed()
        }
    }

    override fun getVideoContainer(): ViewGroup {
        return video_container
    }

    @Suppress("DEPRECATION")
    override fun onFullscreenEventChanged(isFullscreen: Boolean) {
        if (isFullscreen) {
            window.decorView.systemUiVisibility = fullscreenFlags
            web_view.evaluateJavascript(js) { result ->
                val jb = JSONObject(result)
                val width = jb.getInt("width")
                val height = jb.getInt("height")
                if (width > height && chromeClient.isFullscreen) {
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                }
            }
        } else {
            window.decorView.systemUiVisibility = 0
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    private val js = """
        function getVideoRect() {
            let tags = document.getElementsByTagName('video');
            if (tags.length > 0) {
                let video = tags[0];
                return {
                    'width': video.videoWidth,
                    'height': video.videoHeight
                };
            }
            return {};
        }
        getVideoRect();
    """

    private class CustomWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            val url = request?.url?.toString()
            if (url != null && (!url.startsWith("http") || !url.startsWith("https"))) {
                return true
            }
            return super.shouldOverrideUrlLoading(view, request)
        }
    }
}

@Suppress("DEPRECATION")
val fullscreenFlags = (View.SYSTEM_UI_FLAG_FULLSCREEN
        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)