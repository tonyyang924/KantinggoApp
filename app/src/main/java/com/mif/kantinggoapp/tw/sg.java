package com.mif.kantinggoapp.tw;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

import com.mif.kantinggoapp.tool.LoadDataTask;

public class sg extends Activity implements OnCheckedChangeListener, LocationListener {
	
	//選擇項目 RadioGroup
	private RadioButton Rb1, Rb2, Rb3;
	private RadioGroup Rp1;
	
	//View
	private ListView Result_lv;
	
	//GPS
	private Button autoSearchBtn;
	private LocationManager lms;
	private String bestProvider = LocationManager.GPS_PROVIDER;
	private Boolean getService = false;
	
	//紀錄搜尋項目
	private String SearchMain="生物";
	
	//WebView
	private WebView wv;
	 
	//Url
	private String Map_url = "file:///android_asset/sg.html";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sg);
		
		SetView();
		
		SetRadioListener();
		SetButtonListener();
		SetWebView(Map_url);
	}
	
	private void SetView() {
		//Button
		autoSearchBtn = (Button) findViewById(R.id.autoSearchBtn);
		//Radio
		Rb1 = (RadioButton) findViewById(R.id.radio0);
		Rb2 = (RadioButton) findViewById(R.id.radio1);
		Rb3 = (RadioButton) findViewById(R.id.radio2);
		Rp1 = (RadioGroup) findViewById(R.id.radioGroup1);
		wv = (WebView) findViewById(R.id.at_wv);
		Result_lv = (ListView) findViewById(R.id.listView1);
	}
	
	public void SetButtonListener() {
		autoSearchBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if(isGPSEnabled(sg.this) && !getService) {
					getService = true;
					autoSearchBtn.setText("停止");
					locationServiceInitial();
					lms.requestLocationUpdates(bestProvider, 10000, 0, sg.this);
					Toast.makeText(sg.this, "自動搜尋已開啟，再次點選即關閉。", Toast.LENGTH_LONG).show();
				} else if(!isGPSEnabled(sg.this) && !getService){
					startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));	//開啟設定頁面
					Toast.makeText(sg.this, "GPS尚未開啟，請開啟後再次返回點選。", Toast.LENGTH_LONG).show();
				} else if(getService) {
					getService = false;
					autoSearchBtn.setText("自動");
					lms.removeUpdates(sg.this);
					Toast.makeText(sg.this, "自動搜尋已關閉，再次點選即開啟。", Toast.LENGTH_LONG).show();
				}
			}
		});
	}
	
	//Set Radio Listener
	public void SetRadioListener() {
		Rp1.setOnCheckedChangeListener(this);
	}
	
	//SetWebView
	public void SetWebView(String url) {
		wv.getSettings().setJavaScriptEnabled(true);
		wv.setWebChromeClient(new WebChromeClient());
		wv.loadUrl(url);
	}

	
	// RadioId -> Radio ID source
	@Override
	public void onCheckedChanged(RadioGroup arg0, int RadioId) { 
		if(RadioId == R.id.radio0) {
			SearchMain = "生物";
		} else if(RadioId == R.id.radio1) {
			SearchMain = "美食";
		} else if(RadioId == R.id.radio2) {
			SearchMain = "景點 ";
		}
	} 
	
	
	//Location
	public static boolean isGPSEnabled(Context context){
		LocationManager locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
		return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}
	//Location-GPS
	private void locationServiceInitial() {
		lms = (LocationManager) getSystemService(LOCATION_SERVICE);	//取得系統定位服務
		Location location = lms.getLastKnownLocation(LocationManager.GPS_PROVIDER);	//使用GPS定位座標
		getLocation(location);
		wv.loadUrl("javascript:setSelfMarker("+ 120.746342 +"," + 21.971146 + ")");
		new LoadDataTask(this,SearchMain).execute(21.971146, 120.746342);
	}   
	private void getLocation(Location location) {	//將定位資訊顯示在畫面中
		if(location != null) {
 
			Double longitude = location.getLongitude();	//取得經度
			Double latitude = location.getLatitude();	//取得緯度
			wv.loadUrl("javascript:setSelfMarker("+ 120.746342 +"," + 21.971146 + ")");
			//Toast.makeText(sg.this, String.valueOf(longitude) + " " + String.valueOf(latitude), Toast.LENGTH_SHORT).show();
			new LoadDataTask(this,SearchMain).execute(21.971146, 120.746342);
		}
		else {
			Toast.makeText(this, "無法定位座標", Toast.LENGTH_LONG).show();
		}
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(getService) {
			//lms.requestLocationUpdates(bestProvider, 1000, 1, this);
			//服務提供者、更新頻率60000毫秒=1分鐘、最短距離、地點改變時呼叫物件
		}
	}
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if(getService) {
			lms.removeUpdates(this);	//離開頁面時停止更新
		}
	}
	@Override
	public void onLocationChanged(Location location) {
		getLocation(location);
	}

	@Override 
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub
		
	}
	
	/***************
	//Menu 選單
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //參數1:群組id, 參數2:itemId, 參數3:item順序, 參數4:item名稱
		menu.add(0, 0, 0, "群組聊天室");
		//menu.add(0, 0, 0, "開創私人導覽");
        //menu.add(0, 1, 1, "加入私人導覽");
        return super.onCreateOptionsMenu(menu);
    }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //依據itemId來判斷使用者點選哪一個item
        switch(item.getItemId()) {
            case 0:                //開創私人導覽
            	Intent it = new Intent();
            	it.setClass(this, sg_create_room.class);
            	startActivity(it);
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }
    ***************/
}
