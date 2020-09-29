package com.lee.webvideo

import android.view.ViewGroup

interface VideoWebViewOwner {

    fun getVideoContainer(): ViewGroup

    fun onFullscreenEventChanged(isFullscreen: Boolean)
}