package com.mif.kantinggoapp.tw;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class at2_map extends Activity implements LocationListener{
	
	private static final String TAG = "KantinggoApp::at2_map";
	
	private WebView wv;
	private boolean webviewReady = false;
	private String Map_url = "file:///android_asset/at_googlemap.html";
	
	private LocationManager lms;
	
	//自身經緯度
	private double nowlongitude=0;
	private double nowlatitude=0;
	
	//地點經緯度
	private double placelongitude=0;
	private double placelatitude=0;
	
	//選擇的Provider
	private String choiceProvider;
	private boolean getService = false;
	private boolean IsGooglemapMark = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.at_map);
		//取得前一個Activity傳過來的Bundle物件
		Bundle bundle = getIntent().getExtras();
		placelongitude = Double.parseDouble(bundle.getString("wsmape"));
		placelatitude = Double.parseDouble(bundle.getString("wsmapn"));
		SetView();
		//取得系統定位服務
		lms = (LocationManager) getSystemService(LOCATION_SERVICE);
	}
	
	private void SetView() {
		wv = (WebView)findViewById(R.id.at_wv);
		wv.getSettings().setJavaScriptEnabled(true);
		wv.setWebViewClient(new WebViewClient(){ 
			@Override 
			public void onPageFinished(WebView view, String url) 
			{
				//webview已經載入完畢
				webviewReady = true;
			}
		});
		wv.loadUrl(Map_url);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (lms.isProviderEnabled(LocationManager.GPS_PROVIDER) || lms.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
  			//如果GPS或網路定位開啟，呼叫locationServiceInitial()更新位置
  			getService = true;	//確認開啟定位服務
  			locationServiceInitial();
  		} else {
  			Toast.makeText(at2_map.this, "請開啟定位服務", Toast.LENGTH_SHORT).show();
  			AlertDialog.Builder builder = new AlertDialog.Builder(at2_map.this);
  		    builder.setTitle("警告訊息");
  			builder.setMessage("您尚未開啟定位服務，要前往設定嗎？");
  			builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
  		        @Override
  		        public void onClick(DialogInterface dialog, int which) {
  		        	startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));	//開啟設定頁面
  		        }
  		    });
  		    builder.setNegativeButton("否", new DialogInterface.OnClickListener() {
  		        @Override
  		        public void onClick(DialogInterface dialog, int which) {
  		           Toast.makeText(getApplicationContext(),"定位服務尚未開啟...", Toast.LENGTH_SHORT).show();
  		        }
  		    });
  		    AlertDialog alert = builder.create();
  		    alert.show();
  		}
		if(getService) {
			//服務提供者、更新頻率60000毫秒=1分鐘、最短距離、地點改變時呼叫物件
			lms.requestLocationUpdates(choiceProvider, 0, 0, this);
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		getService = false;
		lms.removeUpdates(this);
	}
	
	private void locationServiceInitial() {
		if(lms.isProviderEnabled(LocationManager.GPS_PROVIDER))//有GPS就設定以gps來定位
			choiceProvider=LocationManager.GPS_PROVIDER;
		else if(lms.isProviderEnabled(LocationManager.NETWORK_PROVIDER))//如果沒gps有網路就以網路來定位
			choiceProvider=LocationManager.NETWORK_PROVIDER;
		Location lo = lms.getLastKnownLocation(choiceProvider);
		getLocation(lo);
	}
	
	private void getLocation(Location location) {	//將定位資訊顯示在畫面中
		if(location != null) {
			nowlongitude = location.getLongitude();	//取得經度
			nowlatitude = location.getLatitude();	//取得緯度
			MarkMyLocation();
		} else {
			Toast.makeText(this, "無法定位座標", Toast.LENGTH_SHORT).show();
		}
	}
	
	private void MarkMyLocation() {
		if(webviewReady){
			if(!IsGooglemapMark){
				Log.i(TAG,"now:"+nowlatitude+","+nowlongitude+" place:"+placelatitude+","+placelongitude);
				String jsurl = "javascript:mark(" +
						nowlatitude + "," +
						nowlongitude + ",1)";
				wv.loadUrl(jsurl);
				String jsurl2 = "javascript:mark(" +
						placelatitude + "," +
						placelongitude + ",2)";
				wv.loadUrl(jsurl2);
				//標示Google Map路線規劃
				String jsurl3 = "javascript:PlanLine(" +
						nowlatitude + "," +
						nowlongitude + "," +
						placelatitude + "," + placelongitude +")";
				wv.loadUrl(jsurl3);
				IsGooglemapMark=true;
			}
		}
	}
	
	@Override
	public void onLocationChanged(Location location) {
		getLocation(location);
	}

	@Override
	public void onProviderDisabled(String provider) {}

	@Override
	public void onProviderEnabled(String provider) {}

	@Override 
	public void onStatusChanged(String provider, int status, Bundle extras) {}
	
	
}
