package com.mif.kantinggoapp.tw;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.mif.kantinggoapp.socket.connectSocket;
import com.mif.kantinggoapp.tool.ChatAdapter;

public class sg_create_room extends Activity implements LocationListener {
	
	//主要 Socket
	static Socket socket_client;
	private final String LOCALHOST = "rys.vexp.idv.tw";
    private final int PORT = 8081;
    public BufferedReader in; //接收
    //Socket 使用者名稱紀錄
    private static String UserName,
    					  UserKey;
	
	//Socket connected Submit
	private Button Connect_Submit,
				   Exit_Submit;
	
	//Socket connected EditText
	private EditText Room_id,
			 		 Room_pwd;
	
	//Socket Web Map
	private WebView wvMap;
	//WebView Url
	private String Map_url = "file:///android_asset/sg.html";
	
	//Content ListView
	private ListView Content_lv;
	private int numLists;
	private ChatAdapter myChatAdapter;
	ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();
	HashMap<String, String> map = new HashMap<String, String>();
	
	//GPS
	private LocationManager lms;
	private String bestProvider = LocationManager.GPS_PROVIDER;
	private static Boolean getService = false;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sg_create);
		
		myChatAdapter = new ChatAdapter(sg_create_room.this);
		numLists = 0;
		
		SetView();
		SetWebView(Map_url);
		SetListView();
		
		SetSubmitListener();
	}
	
	//SetAllViewer
	private void SetView() {
		Connect_Submit = (Button) findViewById(R.id.create_room_submit);
		Exit_Submit = (Button) findViewById(R.id.exit_room_submit);
		Room_id = (EditText) findViewById(R.id.create_room_name_ed);
		Room_pwd = (EditText) findViewById(R.id.create_room_pwd_ed);
		//
		wvMap = (WebView) findViewById(R.id.SocketWebView);
		//
		Content_lv = (ListView) findViewById(R.id.Content_Listview);
	} 
	
	//SetListView
	public void SetListView() {
		//ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("ChatType", "【伺服器提示】");
		map.put("ChatContent", "正在連接，確認伺服器中，請稍後。");
		list.add(map);
		numLists += 1;
		myChatAdapter.setListItem(list);
		myChatAdapter.setNumList(numLists);
		Content_lv.setAdapter(myChatAdapter);
		//Toast.makeText(sg_create_room.this, String.valueOf(numLists), Toast.LENGTH_SHORT).show();
	}
	
	//SetWebView
		public void SetWebView(String url) {
			wvMap.getSettings().setJavaScriptEnabled(true);
			wvMap.setWebChromeClient(new WebChromeClient());
			wvMap.loadUrl(url);
		}
	
	//Connect_Submit Listener
	private void SetSubmitListener() {
		Connect_Submit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(Room_id.getText().toString().length() != 0 
						&& Room_pwd.getText().toString().length() != 0) {
					Thread bg_connect_socket = new connectSocket(sg_create_room.this);
					bg_connect_socket.start();
					UserName = Room_id.getText().toString();
					UserKey = Room_pwd.getText().toString();
					Connect_Submit.setEnabled(false);
					Exit_Submit.setEnabled(true);
					//ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();
					HashMap<String, String> map = new HashMap<String, String>();
					map.put("ChatType", "【伺服器提示】");
					map.put("ChatContent", "伺服器確認完畢，可以開始進行操作");
					list.add(map);
					numLists += 1;
					myChatAdapter.setListItem(list);
					myChatAdapter.setNumList(numLists);
					Content_lv.setAdapter(myChatAdapter);
					//GPS Start
					if(isGPSEnabled(sg_create_room.this) && !getService) {
						getService = true;
						locationServiceInitial();
						lms.requestLocationUpdates(bestProvider, 10000, 0, sg_create_room.this);
						Toast.makeText(sg_create_room.this, "自動搜尋已開啟，再次點選即關閉。", Toast.LENGTH_LONG).show();
					} else if(!isGPSEnabled(sg_create_room.this) && !getService){
						startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));	//開啟設定頁面
						Toast.makeText(sg_create_room.this, "GPS尚未開啟，請開啟後再次返回點選。", Toast.LENGTH_LONG).show();
					}
					
					//Toast.makeText(sg_create_room.this, String.valueOf(numLists), Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(sg_create_room.this, "輸入錯誤或空白", Toast.LENGTH_SHORT).show();
				}
			}
		});
	} 
	//Menu 選單
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//參數1:群組id, 參數2:itemId, 參數3:item順序, 參數4:item名稱
		menu.add(0, 0, 0, "傳送訊息");
		menu.add(0, 1, 1, "朋友位置");
		menu.add(0, 2, 2, "傳送照片");
		return super.onCreateOptionsMenu(menu);
	}
	
	private Uri outputFileUri;
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //依據itemId來判斷使用者點選哪一個item
        switch(item.getItemId()) {
            case 0:		//傳送訊息
            	
            	LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        		View layout = inflater.inflate(R.layout.sg_socket_send_dialog, null);
        		
        		Button Send_btn = (Button) layout.findViewById(R.id.Dialog_send_btn);
        		Button Exit_btn = (Button) layout.findViewById(R.id.Dialog_exit_btn);
        		final EditText send_message = (EditText) layout.findViewById(R.id.Dialog_send_message);
        		
        		AlertDialog.Builder builder = new AlertDialog.Builder(this);
        		builder.setView(layout);
            	final AlertDialog alterDialog = builder.create();
            	alterDialog.show();
            	
            	Send_btn.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						final connectSocket socket_client = new connectSocket(sg_create_room.this); 
				    	final Socket Get_socket = socket_client.get_socket_client();
				    	Thread send_socket_message = new Thread(new Runnable() {
							@Override
							public void run() {
								// TODO Auto-generated method stub
								try {
									socket_client.SendMessage("UserSend@" + UserName + "@" + UserKey + "@" + send_message.getText().toString());
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						});
				    	send_socket_message.start();
				    	alterDialog.dismiss();
					}
				});
            	
            	Exit_btn.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						alterDialog.dismiss();
					}
				});
            	
            break;
            case 1:		//朋友列表
                
            break;
            case 2:		//傳送照片
            	Intent intent =  new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            	//設定圖片的儲存位置，以及檔名
            	File tmpFile = new File(
            	               Environment.getExternalStorageDirectory(), 
            	        "image.jpg");
            	outputFileUri = Uri.fromFile(tmpFile);
            	intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri); 
            	startActivityForResult(intent, 0); 
            	final connectSocket socket_client = new connectSocket(sg_create_room.this); 
    	    	final Socket Get_socket = socket_client.get_socket_client();
    	    	Thread ExitSocket = new Thread(new Runnable() {
    				@Override
    				public void run() {
    					try {
    						socket_client.SendMessage("Exit");
    						Get_socket.close();
    						//messageThread.stop();
    						socket_client.connected = false;
    						runOnUiThread(new Runnable(){
    							@Override
    							public void run() {
    								// TODO Auto-generated method stub
    								//Connect_Submit.setEnabled(true);
    								//Exit_Submit.setEnabled(false);
    							}
    						});
    					} catch (IOException e) {
    						// TODO Auto-generated catch block
    						e.printStackTrace();
    						sg_create_room.this.finish();
    					}
    				}
    			});
    			ExitSocket.start();
            break;
            default:
        }
        return super.onOptionsItemSelected(item);
	}
	
	private Bitmap bitmap;
	private byte[] ByteArray;
	 
	/*@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			Thread bg_connect_socket = new connectSocket(sg_create_room.this);
			bg_connect_socket.start();
			Bitmap bmp = BitmapFactory.decodeFile(outputFileUri.getPath()); //利用BitmapFactory去取得剛剛拍照的圖像
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			bmp.compress(CompressFormat.JPEG, 100, bos);
			ByteArray = bos.toByteArray();
			final connectSocket socket_client = new connectSocket(sg_create_room.this);
			Thread socket_send_image = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						socket_client.SendMessage("UserSendImage@" + UserName + "@" + UserKey + "@" + "123123");
						socket_client.SendImage(ByteArray);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket_client.getOutputStream()));
					//out.write(ByteArray);
					//out.flush();
					//out.close();
				}
			});
			socket_send_image.start();
			//ImageView imgv = (ImageView) this.findViewById(R.id.imageView1);
			//imgv.setImageBitmap(bmp);
			Toast.makeText(this, String.valueOf(ByteArray.length), Toast.LENGTH_SHORT).show();
		}
	}*/
	
	@Override
	public  boolean  onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	    	
	    	//Exit Lms 
	    	lms.removeUpdates(sg_create_room.this);
	    	
	    	final connectSocket socket_client = new connectSocket(sg_create_room.this); 
	    	final Socket Get_socket = socket_client.get_socket_client();
	    	Thread ExitSocket = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						socket_client.SendMessage("Exit");
						Get_socket.close();
						//messageThread.stop();
						socket_client.connected = false;
						runOnUiThread(new Runnable(){
							@Override
							public void run() {
								// TODO Auto-generated method stub
								Connect_Submit.setEnabled(true);
								Exit_Submit.setEnabled(false);
								sg_create_room.this.finish();
							}
						});
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						sg_create_room.this.finish();
					}
				}
			});
			ExitSocket.start();
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}

	//GPS Function
	//ChangeStatus
	public void change_GPS_status() {
		getService = false;
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
	}
	PrintWriter writer; //送出
	private void getLocation(Location location) {	//將定位資訊顯示在畫面中
		if(location != null) {
 
			final Double longitude = location.getLongitude();	//取得經度
			final Double latitude = location.getLatitude();	//取得緯度
			wvMap.loadUrl("javascript:setSelfMarker("+ longitude +"," + latitude + ")");
			
			//Socket 傳送經緯度
			final Socket socket_client = connectSocket.socket_client;
			
			Thread Send_User_Latlon = new Thread(new Runnable() {
				@Override
				public void run() {
					try { 
						writer = new PrintWriter(socket_client.getOutputStream());
						writer.println("UserSendLonLat@" + UserName + "@" + UserKey + "@" + longitude + "&" + latitude);
						writer.flush();
						//socket_client.SendMessage();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
			Send_User_Latlon.run();
			//Send_User_Latlon.start();
			//Toast.makeText(sg.this, String.valueOf(longitude) + " " + String.valueOf(latitude), Toast.LENGTH_SHORT).show();
			//new LoadDataTask(this, longitude, latitude).execute(latitude, longitude);
		}
		else {
			Toast.makeText(this, "無法定位座標", Toast.LENGTH_LONG).show();
		}
	}
	
	
	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		getLocation(location);
	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
}