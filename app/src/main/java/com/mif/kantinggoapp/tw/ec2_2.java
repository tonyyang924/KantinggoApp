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
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mif.kantinggoapp.tool.Connection;
import com.mif.kantinggoapp.util.EntryAdapter;
import com.mif.kantinggoapp.util.EntryItem;
import com.mif.kantinggoapp.util.Item;
import com.mif.kantinggoapp.util.SectionItem;

public class ec2_2 extends Activity{
	
	private String title,typechar;
	private TextView ec2_title;
	private ArrayList<Item> ec2_lv_title = new ArrayList<Item>();
	private DBHelper DH = null;
	private boolean canUseConnect=false;
	
	protected static final int Result_Data = 0x1;
	
	//progressDialog
	private ProgressDialog myDialog;
	
	//Handler 接收Thread訊息
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
				case Result_Data:
					myDialog.dismiss();
					ListView listview = (ListView)findViewById(R.id.ec_lv);
					EntryAdapter adapter = new EntryAdapter(ec2_2.this, ec2_lv_title , R.drawable.twitter_bird);
					listview.setAdapter(adapter);
					listview.setOnItemClickListener(new OnItemClickListener() {
						@Override
						public void onItemClick(AdapterView<?> parent, View view,
								int position, long id) {
							EntryItem item = (EntryItem)ec2_lv_title.get(position);
							Intent intent = new Intent(ec2_2.this,ec3.class);
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
		setContentView(R.layout.ec_list);
		
		Bundle bundle = getIntent().getExtras();  
		title = bundle.getString("title");
		
		openDB();
		
		//判斷是否有網路
		Connection ct = new Connection(this);
		canUseConnect = ct.IsConnected();
		
		SetView();
	}
	
	private void openDB() {
		DH = new DBHelper(this);
	}
	
	private void closeDB() {
		DH.close();
	}
	
	private void SetView() {
		//progressDialog
		myDialog = new ProgressDialog(this);
		//title
		ec2_title = (TextView)findViewById(R.id.title);
		ec2_title.setText(title);
		//判斷分類
		if(title.equals("鳥類")){
			typechar="鳥類";
		} else if(title.equals("爬蟲類")) {
			typechar="爬蟲類";
		} else if(title.equals("哺乳類")) {
			typechar="哺乳類";
		} else if(title.equals("甲殼類")) {
			typechar="甲殼類";
		} else if(title.equals("魚類")) {
			typechar="魚類";
		}
		if(canUseConnect){
			myDialog = ProgressDialog.show
	                 (
	                         this,
	                         "提示視窗",
	                         "載入中",
	                         true
	                       );
			//背景作業取得資料，並顯示UI
			new Thread(new Runnable() {
				@Override
				public void run() {
					ec2_lv_title.clear();
					ec2_lv_title.add(new SectionItem(typechar));
					Log.i("TEST",typechar);
					del();
					parseJSONData(getJSONData());
					handler.obtainMessage(Result_Data).sendToTarget();
				}
			}).start();
		} else {
			Toast.makeText(ec2_2.this, "無法連結網路", Toast.LENGTH_LONG).show();
		}
	}
	
	private String getJSONData(){
		String url = "http://rys.vexp.idv.tw/~kantinggo/_json/Ec_PHPtoJson.php";
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
			JSONArray json = new JSONArray(dataInput);
			for(int i=0;i<json.length();i++){
				JSONObject lib = json.getJSONObject(i);
				add(lib.getString("name"),
						lib.getString("content"),
						lib.getString("typechar"),
						lib.getString("imgurl"));
				if(lib.getString("typechar").equals(typechar)) {
					Log.i("sid",lib.getString("sid"));
					Log.i("name",lib.getString("name"));
					Log.i("content",lib.getString("content"));
					Log.i("typechar",lib.getString("typechar"));
					Log.i("imgurl",lib.getString("imgurl"));
					EntryItem name = new EntryItem(lib.getString("name").toString(), typechar);
					SetEc2ListViewItem(name);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private void SetEc2ListViewItem(EntryItem list) {
		ec2_lv_title.add(list);
	}
	
	
	private void del(){
		SQLiteDatabase db = DH.getWritableDatabase();
		db.delete("organism",null,null);
		db.execSQL("delete from sqlite_sequence where name='organism';");
	}
	/**
	 * arg0 物種名稱
	 * arg1 內容
	 * arg2 類型
	 */
	private void add(String arg0,String arg1,String arg2,String arg3){
		SQLiteDatabase db = DH.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("name", arg0.toString());
		values.put("content", arg1.toString());
		values.put("typechar", arg2.toString());
		values.put("imgurl", arg3.toString());
		db.insert("organism", null, values);
	}
}
