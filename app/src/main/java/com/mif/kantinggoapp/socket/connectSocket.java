package com.mif.kantinggoapp.socket;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import android.app.Activity;
import android.content.Context;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.mif.kantinggoapp.tool.ChatAdapter;
import com.mif.kantinggoapp.tw.R;
import com.mif.kantinggoapp.tw.sg_create_room;

public class connectSocket extends Thread {
	
	//Viewer
	EditText Name_et,
			 Pwd_et;
	ListView Socket_lv;
	ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();
	HashMap<String, String> map = new HashMap<String, String>();
	WebView wv;
	
	//主要 Socket
	public static Socket socket_client;
	private final String LOCALHOST = "rys.vexp.idv.tw";
	private final int PORT = 8081;
	public BufferedReader in; //接收
	private PrintWriter writer; //送出
	MessageThread messageThread;
	
	//是否連接
	public static boolean connected = false;
	
	//Context
	private static Context cxt;
	
	//
	ChatAdapter myChatAdapter = new ChatAdapter(cxt);
	
	
	
	//Handler
	protected static final int Send_data = 0x1;
	protected static final int Server_data = 0x2;
	
	//GPS
	private LocationManager lms;
	
	public connectSocket(Context context) {
		this.cxt = context;
		Name_et = (EditText)((Activity)context).findViewById(R.id.create_room_name_ed);
		Pwd_et = (EditText)((Activity)context).findViewById(R.id.create_room_pwd_ed);
		Socket_lv = (ListView)((Activity)context).findViewById(R.id.Content_Listview);
		wv = (WebView)((Activity)context).findViewById(R.id.SocketWebView);
		wv.getSettings().setJavaScriptEnabled(true);
		wv.setWebChromeClient(new WebChromeClient());
	}
	
	//Handler 接收Thread訊息
	public Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
				case Send_data:
					//Toast.makeText(cxt, "建立成功", Toast.LENGTH_SHORT).show();
					//Toast.makeText(cxt , (String)msg.obj, Toast.LENGTH_SHORT).show();
					final Button ExitSubmit = (Button)((Activity) cxt).findViewById(R.id.exit_room_submit);
					final Button EnterySubmit = (Button)((Activity) cxt).findViewById(R.id.create_room_submit);
					//建立訊息 -> ListView
					HashMap<String, String> map = new HashMap<String, String>();
					map.put("ChatType", "【成功訊息】");
					map.put("ChatContent", "您好，使用者【" + Name_et.getText().toString() + "】"
							+ "您已經成功連結伺服器，並且加入一個【" + Pwd_et.getText().toString() + "】的私人頻道，"
							+ "點選離開群組聊天室後即可離開。");
					list.add(map);
					myChatAdapter = new ChatAdapter(cxt);
					myChatAdapter.setListItem(list);
					//myChatAdapter.setNumList(numLists);
					Socket_lv.setAdapter(myChatAdapter);
					//建立訊息 -> ListView
					ExitSubmit.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							//getService = false;
							lms = (LocationManager) ((Activity)cxt).getSystemService(cxt.LOCATION_SERVICE);	//取得系統定位服務
							lms.removeUpdates((LocationListener) (Activity) cxt);
							sg_create_room main_static_getService = new sg_create_room();
							main_static_getService.change_GPS_status();
							
							//Toast.makeText(cxt, "自動搜尋已關閉，再次點選即開啟。", Toast.LENGTH_LONG).show();
							ExitSubmit.setText("離開群組聊天室");
							ExitSubmit.setEnabled(false);
							EnterySubmit.setEnabled(true);
							//建立訊息 -> ListView
							HashMap<String, String> map = new HashMap<String, String>();
							map.put("ChatType", "【伺服器提示】");
							map.put("ChatContent", "您離開一個私人頻道，再次點選加入群組聊天室即可加入。");
							list.add(map);
							myChatAdapter = new ChatAdapter(cxt);
							myChatAdapter.setListItem(list);
							//myChatAdapter.setNumList(numLists);
							Socket_lv.setAdapter(myChatAdapter);
							//建立訊息 -> ListView
							Thread ExitSocket = new Thread(new Runnable() {
								@Override
								public void run() {
									try {
										SendMessage("Exit");
										socket_client.close();
										//messageThread.stop();
										connected = false;
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							});
							ExitSocket.start();
						}		
					});
				break;
				case Server_data:
					
				break;
			}
		}
	};
	
	public void run() {
		if(!connected) {
			try { 
				socket_client = new Socket(LOCALHOST, PORT);
				in = new BufferedReader(new InputStreamReader(socket_client.getInputStream(),"UTF-8"));
				if (socket_client.isConnected()) {
					connected = true; 
					//進入Log
					SendMessage(Name_et.getText().toString() + "@" + Pwd_et.getText().toString());
					handler.obtainMessage(Send_data, "Hi Im " + socket_client.getLocalAddress().toString()).sendToTarget();
					messageThread = new MessageThread(in);
					messageThread.start();
					//Log.i("Chat", "Socket Connected"+in.readLine());
				} 
			} catch (UnknownHostException e) {
				Log.i("Connected", "UnknownHostException");
				e.printStackTrace();
			} catch (IOException e) {
				Log.i("Connected", "IOException");
				e.printStackTrace();
			}
		}
	}
	
	public void SendMessage(String msg) throws IOException {
		if(msg != null) {
			writer = new PrintWriter(socket_client.getOutputStream());
			writer.println(msg);
			writer.flush();
		}
	}
	
	/*public void SendImage(byte[] byteArray) throws IOException {
		DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket_client.getOutputStream()));
		out.write(byteArray);
		out.flush();
		out.close();
	}*/
	
	class MessageThread extends Thread {
		
		private BufferedReader reader;
		
		public MessageThread(BufferedReader reader) {
            this.reader = reader;
        }
		
		public void run() {
			String msg = "";
			while(connected) {
				try {
					msg = reader.readLine();
					Log.i("Chat", msg);
					if(msg != null) {
						final StringTokenizer st = new StringTokenizer(msg, "@");
						String Action_type = st.nextToken();
						//Log.i("Chat", cmd.trim());
						//if(Action_type.equals("SERVER")) {
						if(Action_type.equals("SERVER2")) {
							((Activity) cxt).runOnUiThread(new Runnable(){
							      public void run(){ 
							    	//建立訊息 -> ListView
									HashMap<String, String> mapServer = new HashMap<String, String>();
									mapServer.put("ChatType", "【伺服器提示】");
									mapServer.put("ChatContent", (String)st.nextToken());
									list.add(mapServer);
									myChatAdapter = new ChatAdapter(cxt);
									myChatAdapter.setListItem(list);
									//myChatAdapter.setNumList(numLists);
									Socket_lv.setAdapter(myChatAdapter);
									//建立訊息 -> ListView
							      }
							});
						} else if(Action_type.equals("Ser_User_message_LatLog")) {
							final String User_name = (String)st.nextToken();
							final String User_Lonlat = (String)st.nextToken();
							((Activity) cxt).runOnUiThread(new Runnable(){
							      public void run(){ 
							    	//建立訊息 -> ListView
									//HashMap<String, String> mapServer = new HashMap<String, String>();
									//mapServer.put("ChatType", "【來自" + User_name + "使用者訊息】");
									//mapServer.put("ChatContent", User_Lonlat);
									//list.add(mapServer);
									//myChatAdapter = new ChatAdapter(cxt);
									//myChatAdapter.setListItem(list);
									//myChatAdapter.setNumList(numLists);
									//Socket_lv.setAdapter(myChatAdapter);
									//建立訊息 -> ListView
									wv.loadUrl("javascript:set_friend_marker('" + User_name + "',"+ User_Lonlat.split("&")[0] +"," + User_Lonlat.split("&")[1] + ")");
							      }
							});
						} else if(Action_type.equals("Ser_User_message")) {
							((Activity) cxt).runOnUiThread(new Runnable(){
							      public void run(){ 
							    	//建立訊息 -> ListView
									HashMap<String, String> mapServer = new HashMap<String, String>();
									mapServer.put("ChatType", "【來自" + (String)st.nextToken() + "使用者訊息】");
									mapServer.put("ChatContent", (String)st.nextToken());
									list.add(mapServer);
									myChatAdapter = new ChatAdapter(cxt);
									myChatAdapter.setListItem(list);
									//myChatAdapter.setNumList(numLists);
									Socket_lv.setAdapter(myChatAdapter);
									//建立訊息 -> ListView
							      }
							});
						}
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
	
	//GetHelper
	public Socket get_socket_client() {
		return socket_client;
	}
	
}