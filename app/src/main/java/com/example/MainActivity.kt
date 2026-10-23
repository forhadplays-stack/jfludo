package com.example

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.RoyalNavyDark

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      MyApplicationTheme {
        LudoGameScreen()
      }
    }
  }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LudoGameScreen() {
  var webViewInstance by remember { mutableStateOf<WebView?>(null) }
  var isLoading by remember { mutableStateOf(true) }

  // Handle system back navigation cleanly
  BackHandler(enabled = true) {
    webViewInstance?.let { wv ->
      // If modal is open in the game or history exists, can handle in web or confirm exit
      wv.evaluateJavascript(
        "(function() { if (document.querySelector('.modal-overlay.open')) { document.querySelectorAll('.modal-overlay').forEach(function(m){m.classList.remove('open');}); return true; } else { document.getElementById('btn-open-menu')?.click(); return false; } })()"
      ) { result ->
        if (result == "false" && wv.canGoBack()) {
          wv.goBack()
        }
      }
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(RoyalNavyDark)
      .safeDrawingPadding()
      .testTag("ludo_game_container"),
    contentAlignment = Alignment.Center
  ) {
    AndroidView(
      modifier = Modifier
        .fillMaxSize()
        .testTag("ludo_webview"),
      factory = { context ->
        WebView(context).apply {
          layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
          )
          setBackgroundColor(0xFF07112C.toInt())
          overScrollMode = View.OVER_SCROLL_NEVER
          isVerticalScrollBarEnabled = false
          isHorizontalScrollBarEnabled = false

          settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            loadWithOverviewMode = true
            useWideViewPort = true
            setSupportZoom(false)
            builtInZoomControls = false
            displayZoomControls = false
            mediaPlaybackRequiresUserGesture = false
            cacheMode = WebSettings.LOAD_DEFAULT
          }

          webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
              super.onPageStarted(view, url, favicon)
              isLoading = true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
              super.onPageFinished(view, url)
              isLoading = false
            }

            override fun onReceivedError(
              view: WebView?,
              request: WebResourceRequest?,
              error: WebResourceError?
            ) {
              super.onReceivedError(view, request, error)
              isLoading = false
            }
          }

          webChromeClient = WebChromeClient()

          loadUrl("file:///android_asset/index.html")
          webViewInstance = this
        }
      },
      update = { wv ->
        webViewInstance = wv
      }
    )

    if (isLoading) {
      CircularProgressIndicator(
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.testTag("game_loading_indicator")
      )
    }
  }
}
