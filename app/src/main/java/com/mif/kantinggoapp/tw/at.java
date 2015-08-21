package com.mif.kantinggoapp.tw;

import java.util.ArrayList;

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
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.mif.kantinggoapp.tool.Connection;
import com.mif.kantinggoapp.util.EntryAdapter;
import com.mif.kantinggoapp.util.EntryItem;
import com.mif.kantinggoapp.util.Item;

public class at extends Activity{
	
	private ListView at_lv;
	private ProgressDialog myDialog;
	//private ArrayList<Item> at_lv_title = new ArrayList<Item>();
	//private ArrayList<String> at_lv_title = new ArrayList<String>();
	private ArrayList<Item> at_items = new ArrayList<Item>();
	private boolean canUseConnect;
	private DBHelper DH = null;
	private ArrayAdapter<String> adapter;
	protected static final int Result_Data = 0x1;
	
	//Handler 接收Thread訊息
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
				case Result_Data:
					myDialog.dismiss();
					ListView listview = (ListView)findViewById(R.id.at_lv);
					EntryAdapter adapter = new EntryAdapter(at.this, at_items , R.drawable.attraction);
					//adapter = new ArrayAdapter<String>(at.this, android.R.layout.simple_list_item_1,at_lv_title);
					listview.setAdapter(adapter);
					listview.setOnItemClickListener(new OnItemClickListener() {
						@Override
						public void onItemClick(AdapterView<?> parent, View view,
								int position, long id) {
							//ListView lv = (ListView) parent;
							//String item_title = lv.getItemAtPosition(position).toString();
							EntryItem item = (EntryItem)at_items.get(position);
							Intent intent = new Intent(at.this,at2.class);
							Bundle bundle = new Bundle();
							bundle.putString("title", item.title);
							intent.putExtras(bundle);
							startActivity(intent);
						}
					});
				break;
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.at_list);
		
		openDB();
		
		//判斷是否有網路
		Connection ct = new Connection(this);
		canUseConnect = ct.IsConnected();
		
		SetView();
	}
	
	private void openDB() {
		DH = new DBHelper(this);
	}
	
	private void SetView() {
		//progressDialog
		myDialog = new ProgressDialog(this);
		
		if(canUseConnect){
			myDialog = ProgressDialog.show
	         (this,"提示視窗", "載入中",	true);
			//背景作業取得資料，並顯示UI
			new Thread(new Runnable() {
				@Override
				public void run() {
					del();
					parseJSONData(getJSONData()); 
					/*
					String content=null;
					String webdata="";
					String str="http://rys.vexp.idv.tw/~kantinggo/str/android/scene.php";
					URL url;
					try {
						url = new URL(str);
						URLConnection urlC = url.openConnection();
						urlC.setAllowUserInteraction(false);
						BufferedReader br = new BufferedReader(
						new InputStreamReader(urlC.getInputStream(),"utf-8"));
						while ((content=br.readLine())!= null)
							webdata=webdata+content;
					} catch (Exception e) {
						e.printStackTrace();
					}
					String[] AfterSplit = webdata.split(";");
					Log.i("TEST",AfterSplit[0]);
					*/
					handler.obtainMessage(Result_Data).sendToTarget();
				}
			}).start();
		} else {
			Toast.makeText(at.this, "無法連結網路", Toast.LENGTH_LONG).show();
		}
	}
	 
	private JSONArray getJSONData(){
		String url = "http://rys.vexp.idv.tw/~kantinggo/_json/At_PHPtoJson.php";
		HttpGet httpget = new HttpGet(url);
		try {
			HttpResponse httpresponse = getHttpClient().execute(httpget);
			String result = EntityUtils.toString(httpresponse.getEntity());
			JSONArray jsonarr = new JSONArray(result);
			return jsonarr; 
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

	private void parseJSONData(JSONArray dataInput){
		for(int i=0;i<dataInput.length();i++){
			try {
				JSONObject lib = dataInput.getJSONObject(i);
				add(lib.getString("sname"),
						lib.getString("introduce"),
						lib.getString("imgurl"),
						lib.getString("wsmape"),
						lib.getString("wsmapn"));
				//at_lv_title.add(lib.getString("sname").toString());
				at_items.add(new EntryItem(lib.getString("sname").toString(), lib.getString("addr").toString()));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void del(){
		SQLiteDatabase db = DH.getWritableDatabase();
		db.delete("scene",null,null);
		db.execSQL("delete from sqlite_sequence where name='scene';");
	}
	/**
	 * arg0 物種名稱
	 * arg1 介紹
	 * arg2 圖片網址
	 * arg3 經度
	 * arg4 緯度
	 */
	private void add(String arg0,String arg1,String arg2
						,String arg3,String arg4){
		SQLiteDatabase db = DH.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("sname", arg0.toString());
		values.put("introduce", arg1.toString());
		values.put("imgurl", arg2.toString());
		values.put("wsmape", arg3.toString());
		values.put("wsmapn", arg4.toString());
		db.insert("scene", null, values);
	}
	
	
}
