@file:Suppress("DEPRECATION")

package com.lee.webvideo

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.util.TypedValue
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.AbsoluteLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.google.android.exoplayer2.SimpleExoPlayer
import com.lee.videoview.media.MediaSourceCreator
import com.lee.videoview.orientation.OrientationManager
import com.lee.videoview.widget.video.CustomVideoView
import kotlinx.android.synthetic.main.activity_exo_video.*
import org.json.JSONObject
import kotlin.math.roundToInt

class ExoVideoActivity : AppCompatActivity() {

    companion object {
        private const val WEB_URL = "http://192.168.1.10:8080/"
    }

    private lateinit var orientationManager: OrientationManager

    private var fullscreenView: CustomVideoView? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exo_video)
        orientationManager = OrientationManager(this)
        web_view.settings.javaScriptEnabled = true
        web_view.overScrollMode = WebView.OVER_SCROLL_IF_CONTENT_SCROLLS
        web_view.webViewClient = WebViewClient()
        web_view.webChromeClient = WebChromeClient()
        web_view.addJavascriptInterface(ExoJavaScriptInterface(), "android")
        WebView.setWebContentsDebuggingEnabled(true)
        web_view.loadUrl(WEB_URL)
    }

    override fun onResume() {
        super.onResume()
        orientationManager.enable()
    }

    override fun onPause() {
        super.onPause()
        orientationManager.disable()
    }

    override fun onDestroy() {
        super.onDestroy()
        web_view.destroy()
        video_view.player?.release()
    }

    override fun onBackPressed() {
        if (video_view.onBackPressed()) {
            return
        }
        super.onBackPressed()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            window.decorView.systemUiVisibility = fullscreenFlags
            video_view.onFullscreenEntered()
            if (fullscreenView == null) {
                fullscreenView = CustomVideoView(this).apply {
                    orientationOwner = orientationManager
                }
            }
            swapPlayer(video_view, fullscreenView)
            fullscreenView?.let {
                it.onFullscreenEntered()
                fullscreen_container.addView(
                    it, ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                    )
                )
            }
        } else {
            window.decorView.systemUiVisibility = 0
            swapPlayer(fullscreenView, video_view)
            fullscreenView?.let {
                it.onFullscreenExited()
                fullscreen_container.removeView(it)
            }
            video_view.onFullscreenExited()
        }
    }

    private fun play(id: String, url: String) {
        if (id != video_container.tag || !video_container.isVisible) {
            web_view.evaluateJavascript(getElementRectById(id)) {
                try {
                    val jb = JSONObject(it)
                    val width = jb.getDouble("width")
                    val height = jb.getDouble("height")
                    val left = jb.getDouble("left")
                    val top = jb.getDouble("top")
                    val scrollY = web_view.scrollY
                    video_container.updateLayoutParams<AbsoluteLayout.LayoutParams> {
                        this.width = (width + 1).roundToInt().dp
                        this.height = (height + 1).roundToInt().dp
                        x = left.toInt().dp
                        y = scrollY + top.toInt().dp
                    }
                    video_container.isVisible = true
                    video_container.tag = id

                    val dataSource = MediaSourceCreator(this).buildMediaSource(url.toUri())
                    val player = SimpleExoPlayer.Builder(baseContext).build()
                    video_view.player = player
                    video_view.orientationOwner = orientationManager
                    player.prepare(dataSource)
                    player.playWhenReady = true
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun getElementRectById(id: String): String {
        return """
                function getElementRect(id) {
                    let el = window.document.getElementById(id);
                    let rect = el.getBoundingClientRect();
                    return {'top': rect.top, 'left': rect.left, 'width': rect.width, 'height': rect.height};
                }
                getElementRect('$id');
            """
    }

    private fun swapPlayer(src: CustomVideoView?, dst: CustomVideoView?) {
        dst?.player = src?.player
        src?.player = null
    }

    inner class ExoJavaScriptInterface {

        @JavascriptInterface
        fun play(json: String) {
            try {
                val jb = JSONObject(json)
                val id = jb.getString("id")
                val url = jb.getString("url")
                if (!id.isNullOrEmpty() && !url.isNullOrEmpty()) {
                    runOnUiThread {
                        play(id, url)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}

val Int.dp: Int
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    ).toInt()