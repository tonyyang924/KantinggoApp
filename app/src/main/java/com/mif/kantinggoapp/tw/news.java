package com.mif.kantinggoapp.tw;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class news extends Activity{

	private WebView newswb;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.news);
		SetView();
	}
	
	private void SetView() {
		newswb = (WebView)findViewById(R.id.newswb);
		newswb.getSettings().setJavaScriptEnabled(true); //啟用JavaScript執行功能
		newswb.getSettings().setSupportZoom(true);
		newswb.getSettings().setBuiltInZoomControls(true);
		newswb.setWebChromeClient(new WebChromeClient());
		newswb.loadUrl("file:///android_asset/news.html");
	}
}
