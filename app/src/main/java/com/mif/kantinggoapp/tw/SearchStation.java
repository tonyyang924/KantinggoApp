package com.mif.kantinggoapp.tw;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class SearchStation extends Activity implements Button.OnClickListener,LocationListener{
	private Button PlanLineBtn,ShowTimeLineBtn;
	private WebView wv;
	private boolean webviewReady = false;
	
	//地圖網址
	private String Map_url = "file:///android_asset/searchstation.html";
	
	private LocationManager lms;
	
	//自身經緯度
	private double nowlongitude=0;
	private double nowlatitude=0;
	
	//選擇的Provider
	private String choiceProvider;
	private boolean getService = false;
	
	//臺北 板橋 桃園 中壢 新竹 臺中 彰化
    //斗六 嘉義 臺南 高雄 屏東 宜蘭 花蓮 臺東
	private double BigStationsLatLng[][] ={
			{25.047924,121.517081},{25.014051,121.463815},
			{24.989206,121.313549},{24.953454,121.225736},
			{24.801643,120.971696},{24.136781,120.685008},
			{24.081666,120.538539},{23.711793,120.541171},
			{23.479306,120.440828},{22.997144,120.212966},
			{22.63962,120.302111},{22.669306,120.486203},
			{24.754512,121.758253},{23.992868,121.600993},
			{22.793711,121.123161}
	};
	
	private String BigStationName[] = {
		"臺北火車站","板橋火車站","桃園火車站","中壢火車站","新竹火車站","臺中火車站","彰化火車站",
		"斗六火車站","嘉義火車站","臺南火車站","高雄火車站","屏東火車站","宜蘭火車站","花蓮火車站","臺東火車站"
	};
	
	private String BigStationId[] = {
		"1008","1011","1015","1017","1025","1319","1120",
		"1210","1215","1228","1238","1406","1820","1715","1632"
	};
	//哪個火車站
	int TheNumOfStation;
	//火車站代號
	private String StationId;
	//火車站名稱
	private String StationName;
	//火車班次出發時間
	private int StationStartTimeMin;
	//從外面來到火車站要多少時間
	private int StationDistanceTimeMin;
	//現在時間
	private int NowTime;
	//撈取火車時刻表中，指定第幾班
	private int TheNumOfClass;
	//火車站班次資訊 
	private String[][] SResult;
	//是否有規劃路線
	private boolean IsMarkRoutes = false;
	
	protected static final int Result_Data = 0x1;
	protected static final int DistanceTime_Data = 0x2;
		
	//Handler 接收Thread訊息
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
				case Result_Data:
					String result = null;
					if(msg.obj instanceof String) {
						result = (String) msg.obj;
					}
					if(result != null) {
						String line[] = result.split(";");
						SResult = new String[line.length][9];
						//車種,車次,經由,發車站,開車時間,到達時間,行駛時間,備註,票價
						for(int i=0;i<line.length;i++){
							SResult[i] = line[i].split(",");
						}
						TheNumOfClass=0;
						String theTime[] = SResult[TheNumOfClass][4].split(":");
						int hour = Integer.parseInt(theTime[0]);
						int min = Integer.parseInt(theTime[1]);
						StationStartTimeMin = (hour*60)+min;
						Log.i("TEST",String.valueOf(StationStartTimeMin));
						GetDistanceTime();
					}
				break;
				case DistanceTime_Data:
					SimpleDateFormat date = new java.text.SimpleDateFormat("HH:mm");
					String currentDate = date.format(new Date(System.currentTimeMillis()));
					String theTime[] = currentDate.split(":");
					int hour = Integer.parseInt(theTime[0]);
					int min = Integer.parseInt(theTime[1]);
					NowTime = (hour*60)+min;
					//現在時間與火車出發時間的差距
					int X = StationStartTimeMin-NowTime;
					//如果時間差距大於路徑時間 
					//表示可以來得及搭得上火車
					Log.i("T",String.valueOf(X));
					if(X>StationDistanceTimeMin){
						Log.i("T","X:"+String.valueOf(X)+"distance:"+StationDistanceTimeMin);
						Log.i("T","第"+TheNumOfClass+"班火車 時間足夠可以過來。");
						IsMarkRoutes=true;
					} else {
						Log.i("T","來不及，換下一班。");
						TheNumOfClass++;
						if(TheNumOfClass<SResult.length){
							String theTime1[] = SResult[TheNumOfClass][4].split(":");
							int hour1 = Integer.parseInt(theTime1[0]);
							int min1 = Integer.parseInt(theTime1[1]);
							StationStartTimeMin = (hour1*60)+min1;
							GetDistanceTime();
						} else {
							Toast.makeText(SearchStation.this, "沒有符合條件的火車班次。", Toast.LENGTH_SHORT).show();
						}
					}
				break;
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.searchstation);
		//設定view與listener
		SetView();
		SetListener();
		//取得系統定位服務
		lms = (LocationManager) getSystemService(LOCATION_SERVICE);
	}
	
	private void SetView() {
		//progressDialog
		//myDialog = new ProgressDialog(this);
		PlanLineBtn=(Button)findViewById(R.id.PlanLineBtn);
		ShowTimeLineBtn = (Button)findViewById(R.id.ShowTimeLineBtn);
		wv = (WebView)findViewById(R.id.webview);
		wv.getSettings().setJavaScriptEnabled(true);
		//webview.setWebChromeClient(new WebChromeClient());
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
	
	private void SetListener() {
		ShowTimeLineBtn.setOnClickListener(this);
		PlanLineBtn.setOnClickListener(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (lms.isProviderEnabled(LocationManager.GPS_PROVIDER) || lms.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
  			//如果GPS或網路定位開啟，呼叫locationServiceInitial()更新位置
  			getService = true;	//確認開啟定位服務
  			locationServiceInitial();
  		} else {
  			Toast.makeText(SearchStation.this, "請開啟定位服務", Toast.LENGTH_SHORT).show();
  			AlertDialog.Builder builder = new AlertDialog.Builder(SearchStation.this);
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
			//Toast.makeText(SearchStation.this, 
			//		"經度:"+nowlongitude+" 緯度:"+nowlatitude,
			//		Toast.LENGTH_SHORT).show();
			MarkMyLocation();
		} else {
			Toast.makeText(this, "無法定位座標", Toast.LENGTH_SHORT).show();
		}
	}
	
	private void MarkMyLocation() {
		if(webviewReady){
			String jsurl = "javascript:mark(" +
					nowlatitude + "," +
					nowlongitude + ",1)";
			wv.loadUrl(jsurl);
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

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v.equals(PlanLineBtn)){
			//如果經緯度是0（表示沒有得到經緯度），就不執行地圖路線規劃 
			if(nowlongitude==0&&nowlatitude==0) {
				//為了手機Demo，拿掉判斷經緯度取得方式，
				//經緯度改成定在崑山科技大學
				
				//Toast.makeText(SearchStation.this, "目前沒有經緯度座標，無法規劃路線。", Toast.LENGTH_LONG).show();
				//return;
				Toast.makeText(SearchStation.this, "無法取得位置，設定預設位置為崑山科技大學。", Toast.LENGTH_SHORT).show();
				nowlongitude=120.25319;
				nowlatitude=22.999128;
			}
			//如果webview讀取完畢
			if(webviewReady){
				//取得最近火車站ID與標示路線
				MarkRoutes();
				
				//得到火車時刻表
				GetTrain();
				
				//取得到最近火車站要多久時間
				//GetDistanceTime();
				
			} else {
				Toast.makeText(SearchStation.this, "WebView尚未讀取完畢。", Toast.LENGTH_SHORT).show();
			}
		} else if(v.equals(ShowTimeLineBtn)){
			if(!IsMarkRoutes){
				Toast.makeText(SearchStation.this, "還沒規劃路線。", Toast.LENGTH_LONG).show();
				return;
			}
			showCustomDialog();
		}
	}
	
	private void MarkRoutes() {
		//先設定最短距離拿來判斷
		int shortdistance=0;
		//紀錄位於陣列中的第幾個(第幾個火車站)
		TheNumOfStation=9999;
		
		for(int i=0;i<BigStationsLatLng.length;i++){
			if(i==0){
				//第一個預設為最短距離
				shortdistance=(int)getDistance(nowlatitude,nowlongitude,
						BigStationsLatLng[i][0],BigStationsLatLng[i][1]);
				//記入第幾個火車站
				TheNumOfStation=i;
			} else {
				//暫存與火車站之距離
				int tmpdistance = (int)getDistance(nowlatitude,nowlongitude,
						BigStationsLatLng[i][0],BigStationsLatLng[i][1]);
				//如果與火車站的距離比最短距離還小
				//那就設定為最短距離，並且紀錄第幾個火車站
				if(tmpdistance<shortdistance){
					shortdistance=tmpdistance;
					TheNumOfStation=i;
				}
			}
		}
		
		//緯度及經度
		double endlat=0,
				endlng=0;
		//紀錄站點名稱 及 火車時刻表查詢的ID
		String StationName = BigStationName[TheNumOfStation];
		this.StationName=StationName;
		String StationId = BigStationId[TheNumOfStation];
		this.StationId=StationId;
		if(TheNumOfStation!=9999){
			endlat=BigStationsLatLng[TheNumOfStation][0];
			endlng=BigStationsLatLng[TheNumOfStation][1];
		}
		
		if(endlat==0&&endlng==0){
			Toast.makeText(SearchStation.this, "沒有最近火車站之位置經緯度。", Toast.LENGTH_SHORT).show();
			return;
		}
		
		//提示前往訊息
		Toast.makeText(SearchStation.this, "前往"+StationName, Toast.LENGTH_LONG).show();
		
		//標示Google Map路線規劃
		String jsurl = "javascript:PlanLine(" +
				nowlatitude + "," +
				nowlongitude + "," +
				endlat + "," + endlng +")";
		wv.loadUrl(jsurl);
	}
	
	//取得距離
	private double getDistance(double lat1, double lon1, double lat2, double lon2) {  
		float[] results=new float[1];  
		Location.distanceBetween(lat1, lon1, lat2, lon2, results);  
		return results[0];  
	}
	
	private void GetTrain(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				String path="http://rys.vexp.idv.tw/~kantinggo/str/GoToFangliao.php?fromstation="+StationId;
				try {
					requestByGet(path,1);
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	// Get方式請求  
	public void requestByGet(String path, int a) throws Throwable {
		try {
		    // 新建一個URL對象  
		    URL url = new URL(path);  
		    //建立一個名為resultData的StringBuilder來儲存回傳的資訊
		    
		    // 打開一個HttpURLConnection連接  
		    HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();  

		    if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
		    	InputStream  input = new BufferedInputStream(httpConn.getInputStream());
		    	if(a==1){
		    		handler.obtainMessage(Result_Data,readStream(input)).sendToTarget();
		    	} else if(a==2) {
		    		handler.obtainMessage(DistanceTime_Data,readStream(input)).sendToTarget();
		    	}
		    	input.close();
		    	//disconnect
				httpConn.disconnect();
            }

		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private String readStream(InputStream is) {
	    try {
			ByteArrayOutputStream bo = new ByteArrayOutputStream();
			int i = is.read();
			while(i != -1) {
				bo.write(i);
				i = is.read();
			}
			return bo.toString();
	    } catch (IOException e) {
	    	return "";
	    }
	}
	
	private void GetDistanceTime(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				//StationName 
				String path="http://maps.googleapis.com/maps/api/directions/json?origin="+nowlatitude+","+nowlongitude+"&destination="+StationName+"&sensor=false";
				parseJSONData(getJSONData(path)); 
				handler.obtainMessage(DistanceTime_Data).sendToTarget();
			}
		}).start();
	}
	
	private String getJSONData(String url){
		HttpGet httpget = new HttpGet(url);
		try {
			HttpResponse httpresponse = getHttpClient().execute(httpget);
			String result = EntityUtils.toString(httpresponse.getEntity());
			return result; 
		} catch (Exception err){
			return null;
		}
	}
	
	private DefaultHttpClient getHttpClient() {
		HttpParams httpparams = new BasicHttpParams();
		int timeoutConnection = 5000;
		HttpConnectionParams.setConnectionTimeout(httpparams, timeoutConnection);
		int timeoutSocket = 3000;
		HttpConnectionParams.setSoTimeout(httpparams, timeoutSocket);
		DefaultHttpClient dfaultHC = new DefaultHttpClient(httpparams);
		return dfaultHC;
	}

	private void parseJSONData(String dataInput){
		try {
			JSONObject json = new JSONObject(dataInput);
			JSONArray routes = json.getJSONArray("routes");
			JSONArray legs = routes.getJSONObject(0).getJSONArray("legs");
			JSONObject duration = legs.getJSONObject(0).getJSONObject("duration");
			StationDistanceTimeMin = (int) Math.ceil(Double.parseDouble(duration.getString("value"))/60);
			Log.i("TEST",String.valueOf(StationDistanceTimeMin));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private void showCustomDialog() {
		AlertDialog.Builder builder;     
        AlertDialog alertDialog;     
        Context mContext = SearchStation.this;
        LayoutInflater inflater = (LayoutInflater)      
        		mContext.getSystemService(LAYOUT_INFLATER_SERVICE);     
        View layout = inflater.inflate(R.layout.custom_dialog,null);     
        TextView title = (TextView) layout.findViewById(R.id.title);
		TextView t1 = (TextView) layout.findViewById(R.id.t1);
		TextView t2 = (TextView) layout.findViewById(R.id.t2);
		TextView t3 = (TextView) layout.findViewById(R.id.t3);
		TextView t4 = (TextView) layout.findViewById(R.id.t4);
		TextView t5 = (TextView) layout.findViewById(R.id.t5);
		TextView t6 = (TextView) layout.findViewById(R.id.t6);
		TextView t7 = (TextView) layout.findViewById(R.id.t7);
		TextView t8 = (TextView) layout.findViewById(R.id.t8);
		title.setText("前往地點："+BigStationName[TheNumOfStation]);
		t1.setText(SResult[TheNumOfClass][0]);
		t2.setText(SResult[TheNumOfClass][1]);
		t3.setText(SResult[TheNumOfClass][2]);
		t4.setText(SResult[TheNumOfClass][3]);
		t5.setText(SResult[TheNumOfClass][4]);
		t6.setText(SResult[TheNumOfClass][5]);
		t7.setText(SResult[TheNumOfClass][6]);
		t8.setText(SResult[TheNumOfClass][7]);
		builder = new AlertDialog.Builder(mContext);     
		builder.setView(layout);     
		alertDialog = builder.create();     
		alertDialog.show();   
	}
}
