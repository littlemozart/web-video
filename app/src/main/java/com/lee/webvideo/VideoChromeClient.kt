package com.lee.webvideo

import android.view.View
import android.webkit.WebChromeClient

class VideoChromeClient(private val webViewOwner: VideoWebViewOwner) : WebChromeClient() {

    private var customView: View? = null
    private var customViewCallback: CustomViewCallback? = null

    var isFullscreen: Boolean = false
        private set

    override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
        if (customView != null) {
            onHideCustomView()
        } else {
            enterFullscreen(view, callback)
        }
    }

    override fun onHideCustomView() {
        exitFullscreen()
    }

    fun onBackPressed(): Boolean {
        if (isFullscreen) {
            onHideCustomView()
            return false
        }
        return true
    }

    private fun enterFullscreen(view: View?, callback: CustomViewCallback?) {
        isFullscreen = true
        customView = view
        customViewCallback = callback
        webViewOwner.getVideoContainer().addView(customView)
        webViewOwner.onFullscreenEventChanged(isFullscreen)
    }

    private fun exitFullscreen() {
        isFullscreen = false
        webViewOwner.getVideoContainer().removeView(customView)
        customView = null
        customViewCallback?.onCustomViewHidden()
        customViewCallback = null
        webViewOwner.onFullscreenEventChanged(isFullscreen)
    }

}