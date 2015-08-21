package com.mif.kantinggoapp.tool;

import java.util.ArrayList;
import java.util.HashMap;

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
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.mif.kantinggoapp.tw.DBHelper;
import com.mif.kantinggoapp.tw.R;

public class LoadDataTask extends AsyncTask<Double, Integer, String>{
	
	private SQLiteDatabase db;
	private ProgressDialog myLoadingDialog;
	private Context cxt;
	private String dbfile;
	private StringBuilder sb;
	
	//Lon, Lat
	private Double lon, lat;
	
	//WebView
	private WebView wv;
	
	//ListView
	private ListView lv;
	ArrayList<HashMap<String,String>> list = new ArrayList<HashMap<String,String>>();
	private SimpleAdapter adapter;
	
	//Result Array
	private String[][] Result_array;
	
	//搜尋什麼
	private String SearchMain;
	
	//數量
	private int rows_num;
	
	//DB Helper
	private DBHelper DH = null;
	
	public LoadDataTask(Context context, String SearchMain) {
		//資料庫位置
		dbfile = "/data/data/com.mif.kantinggoapp.tw/databases/wetcollect.db";
		//context.getFilesDir().getPath();
		//打開資料庫
		openDB(dbfile, lon, lat); 
		//context
		this.cxt=context;
		//打開SQLiteHelper
		SQLiteOpenHelper();
		//progressdialog
		myLoadingDialog = new ProgressDialog(this.cxt);
		//
		this.SearchMain = SearchMain;
		//lon, lat
		//查詢的sql語法
		//this.SQL=SQL;
		wv = (WebView)((Activity)context).findViewById(R.id.at_wv);
		lv = (ListView)((Activity)context).findViewById(R.id.listView1);
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view,
					int position, long id) { 
				String sid = Result_array[position][1];
				showOrganismDialog(sid);
			}
		});
		//layout=(LinearLayout)((Activity)context).findViewById(R.id.layout);
		//test=(TextView)((Activity)context).findViewById(R.id.test);
	}
	
	private void SQLiteOpenHelper() {
		DH = new DBHelper(cxt);
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		myLoadingDialog.setMessage("Loading");
	    myLoadingDialog.setIndeterminate(false);
	    myLoadingDialog.setCancelable(false);
	    myLoadingDialog.show();
	}
	
	@Override
	protected String doInBackground(Double... doubles) {
		this.lon = doubles[1];
		this.lat = doubles[0];
		String SQL="";
		if(SearchMain.equals("生物")){
			SQL="SELECT A.cid,A.sid,A.wsmape,A.wsmapn,A.cdate,A.c5,A.c7,A.c8,A.c4,B.scientificnamec,A.cdate2,B.familyc,B.scientificname "
					+ " FROM collect AS A,species AS B "
					+ " WHERE A.sid=B.sid "
					+ " AND (A.wsmape >= ("+lon+"-0.0005)) AND (A.wsmape <= ("+lon+"+0.0005)) AND (A.wsmapn >= ("+lat+"-0.005)) AND (A.wsmapn <= ("+lat+"+0.005)) ";
			getData(SQL);
		} else if(SearchMain.equals("美食")){
			//先刪除
			del(1);
			//解析json格式資料並加入SQLite資料庫
			parseJSONData(1,getJSONData(1));
			SQL="SELECT tid,name,tname,tel,addr,time,longitude,latitude,recommend "
					+ " FROM food "
					+ " WHERE (longitude >= ("+lon+"-0.0005)) AND (longitude <= ("+lon+"+0.0005)) AND (latitude >= ("+lat+"-0.005)) AND (latitude <= ("+lat+"+0.005)) ";
			getData2(SQL);
		} else if(SearchMain.equals("景點")) {
			//先刪除
			del(2);
			//解析json格式資料並加入SQLite資料庫
			parseJSONData(2,getJSONData(2));
			SQL="SELECT sname,introduce,imgurl,wsmape::double precision,wsmapn::double precision "
					+ " FROM scene "
					+ " WHERE (wsmape >= ("+lon+"-0.0005)) AND (wsmape <= ("+lon+"+0.0005)) AND (wsmapn >= ("+lat+"-0.005)) AND (wsmapn <= ("+lat+"+0.005)) ";
			getData2(SQL);
		}

		
		return null;
	}
 
	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		ShowData(); 
	}
	
	private void openDB(String file, Double lon, Double lat) {
		db=SQLiteDatabase.openOrCreateDatabase(file,null);
	}
	
	//Select Query 
	private void getData(String SQL) {
		Cursor cursor = db.rawQuery(SQL, null);
		rows_num = cursor.getCount();
		int column_num  = cursor.getColumnCount(); 
		if(rows_num > 0) {
			Result_array = new String[rows_num][column_num];
			cursor.moveToFirst();
			for(int i=0;i<rows_num;i++){
				for(int j=0;j<column_num;j++){ 
					Result_array[i][j] = String.valueOf(cursor.getString(j));
				}
				cursor.moveToNext();
				//wv.loadUrl("javascript:set_search_marker("+String.valueOf(cursor.getString(2)) + "," + String.valueOf(cursor.getString(3))+");");
			}
		}
		cursor.close();
	} 
	
	private void getData2(String SQL) {
		SQLiteDatabase db2 = DH.getWritableDatabase();
		Cursor cursor = db2.rawQuery(SQL, null);
		rows_num = cursor.getCount();
		int column_num  = cursor.getColumnCount(); 
		if(rows_num > 0) {
			Result_array = new String[rows_num][column_num];
			cursor.moveToFirst();
			for(int i=0;i<rows_num;i++){
				for(int j=0;j<column_num;j++){ 
					Result_array[i][j] = String.valueOf(cursor.getString(j));
				}
				cursor.moveToNext();
				//wv.loadUrl("javascript:set_search_marker("+String.valueOf(cursor.getString(2)) + "," + String.valueOf(cursor.getString(3))+");");
			}
		}
		cursor.close();
	}

	private String getJSONData(int n){
		String url = "";
		if(n==1) { //食物
			url = "http://rys.vexp.idv.tw/~kantinggo/_json/Sg_PHPtoJson_Food.php";
		} else if(n==2) { //景點
			url = "http://rys.vexp.idv.tw/~kantinggo/_json/At_PHPtoJson.php";
		}
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

	private void parseJSONData(int n,String dataInput){
		try {
			JSONArray json = new JSONArray(dataInput);
			for(int i=0;i<json.length();i++){
				JSONObject lib = json.getJSONObject(i);
				if(n==1){
					add(1,	lib.getString("tid"),
							lib.getString("name"),
							lib.getString("tname"),
							lib.getString("tel"),
							lib.getString("addr"),
							lib.getString("time"),
							lib.getString("longitude"),
							lib.getString("latitude"),
							lib.getString("recommend"));
				} else if(n==2) {
					add(2,lib.getString("sname"),
							lib.getString("introduce"),
							lib.getString("imgurl"),
							lib.getString("wsmape"),
							lib.getString("wsmapn"));
				}
				
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private void del(int n){
		SQLiteDatabase db = DH.getWritableDatabase();
		if(n==1) {
			db.delete("food",null,null);
			db.execSQL("delete from sqlite_sequence where name='food';");
		} else if(n==2) {
			db.delete("scene",null,null);
			db.execSQL("delete from sqlite_sequence where name='scene';");
		}
	}
	
	private void add(int n,String... arg){
		SQLiteDatabase db = DH.getWritableDatabase();
		if(n==1){
			ContentValues values = new ContentValues();
			values.put("tid", arg[0].toString());
			values.put("name", arg[1].toString());
			values.put("tname", arg[2].toString());
			values.put("tel", arg[3].toString());
			values.put("addr", arg[4].toString());
			values.put("time", arg[5].toString());
			values.put("longitude", arg[6].toString());
			values.put("latitude", arg[7].toString());
			values.put("recommend", arg[8].toString());
			db.insert("food", null, values);
		} else if(n==2) {
			ContentValues values = new ContentValues();
			values.put("sname", arg[0].toString());
			values.put("introduce", arg[1].toString());
			values.put("imgurl", arg[2].toString());
			values.put("wsmape", arg[3].toString());
			values.put("wsmapn", arg[4].toString());
			db.insert("scene", null, values);
		}
	}

	private void ShowData() { 
		if(SearchMain.equals("生物")){
			if(rows_num>0){
				for(int i = 0 ; i < Result_array.length; i++){
					 HashMap<String,String> item = new HashMap<String,String>();
					 item.put( "SName_Ch", Result_array[i][9]);
					 item.put( "Sid", "("+Result_array[i][0]+")");
					 item.put( "SName_En", "英文學名："+Result_array[i][12]);
					 item.put( "FamilyC", "科名："+Result_array[i][11]);
					 list.add( item );
				}
				adapter = new SimpleAdapter( 
						cxt, 
						list,
						R.layout.listview_style2,
						new String[] { "SName_Ch", "Sid", "SName_En" , "FamilyC" },
						new int[] { R.id.Scientificnamec, R.id.Sid , R.id.Scientificname, R.id.familyc} );
				lv.setAdapter(adapter);
				for(int i = 0 ; i< Result_array.length ; i++) {
					if(i==0) {
						wv.loadUrl("javascript:clearMarker(markers_Search_Array);");
					}
					wv.loadUrl("javascript:set_search_marker('"+ Result_array[i][9] + "'," + Result_array[i][2] + "," + Result_array[i][3]+");");
				}
			} else {
				Toast.makeText(this.cxt, "沒有資料。", Toast.LENGTH_LONG).show();
			}
		} else if(SearchMain.equals("美食")) {
			if(rows_num>0){
				String[] title_food = null;
				for(int i = 0 ; i < Result_array.length; i++){
					title_food[i] = Result_array[i][2];
				}
				ArrayAdapter<String> adapter_food = new ArrayAdapter<String>(cxt,
						 android.R.layout.simple_list_item_1, title_food); 
				lv.setAdapter(adapter_food);
				for(int i = 0 ; i< Result_array.length ; i++) {
					if(i==0) {
						wv.loadUrl("javascript:clearMarker(markers_Search_Array);");
					}
					wv.loadUrl("javascript:set_search_marker('"+ Result_array[i][9] + "'," + Result_array[i][2] + "," + Result_array[i][3]+");");
				}
			} else {
				Toast.makeText(this.cxt, "沒有資料。", Toast.LENGTH_LONG).show();
			}
		} else if(SearchMain.equals("景點")) {
			if(rows_num>0){
				String[] title_scene = null;
				for(int i = 0 ; i < Result_array.length; i++){
					title_scene[i] = Result_array[i][1];
				}
				ArrayAdapter<String> adapter_scene = new ArrayAdapter<String>(cxt,
						 android.R.layout.simple_list_item_1, title_scene); 
				lv.setAdapter(adapter_scene);
				for(int i = 0 ; i< Result_array.length ; i++) {
					if(i==0) {
						wv.loadUrl("javascript:clearMarker(markers_Search_Array);");
					}
					wv.loadUrl("javascript:set_search_marker('"+ Result_array[i][9] + "'," + Result_array[i][2] + "," + Result_array[i][3]+");");
				}
			} else {
				Toast.makeText(this.cxt, "沒有資料。", Toast.LENGTH_LONG).show();
			}
		}
		myLoadingDialog.dismiss();
	}
	
	private void showOrganismDialog(String sid) {
		
		//搜尋結果2
		String[] Result_array2 = null;
		String SQL2 = "SELECT sid,scientificnamec,scientificname,commonnamec," +
				"orderc,familyc,endemicspecies,conservation,appear FROM species " +
				"WHERE sid="+sid+";";
		Log.i("sql2",SQL2);
		Cursor cursor = db.rawQuery(SQL2, null);
		
		int rows_num2 = cursor.getCount();
		int column_num  = cursor.getColumnCount(); 
		if(rows_num2>0){
			Result_array2 = new String[column_num];
			cursor.moveToFirst();
			Result_array2[0] = String.valueOf(cursor.getString(0));
			Result_array2[1] = String.valueOf(cursor.getString(1));
			Result_array2[2] = String.valueOf(cursor.getString(2));
			Result_array2[3] = String.valueOf(cursor.getString(3));
			Result_array2[4] = String.valueOf(cursor.getString(4));
			Result_array2[5] = String.valueOf(cursor.getString(5));
			Result_array2[6] = String.valueOf(cursor.getString(6));
			Result_array2[7] = String.valueOf(cursor.getString(7));
			Result_array2[8] = String.valueOf(cursor.getString(8));
			cursor.close();
		} else {
			Log.i("TEST","NO:"+String.valueOf(rows_num2));
		}
		
		AlertDialog.Builder builder;     
        AlertDialog alertDialog;     
        LayoutInflater inflater = (LayoutInflater)      
        		cxt.getSystemService(cxt.LAYOUT_INFLATER_SERVICE);     
        View layout = inflater.inflate(R.layout.organism_dialog,null);
        
        TextView title = (TextView) layout.findViewById(R.id.title);
		TextView scientificname = (TextView) layout.findViewById(R.id.scientificname);
		TextView commonnamec = (TextView) layout.findViewById(R.id.commonnamec);
		TextView orderc = (TextView) layout.findViewById(R.id.orderc);
		TextView familyc = (TextView) layout.findViewById(R.id.familyc);
		TextView endemicspecies = (TextView) layout.findViewById(R.id.endemicspecies);
		TextView conservation = (TextView) layout.findViewById(R.id.conservation);
		TextView appear = (TextView) layout.findViewById(R.id.appear);
		
		String endemicspecies_TEXT = "",
				conservation_TEXT ="";
		final String TheChineseName=Result_array2[1];
		title.setText(TheChineseName);
		scientificname.setText(Result_array2[2]);
		commonnamec.setText(Result_array2[3]);
		orderc.setText(Result_array2[4]);
		familyc.setText(Result_array2[5]);
		if(Integer.parseInt(Result_array2[6])==0){
			endemicspecies_TEXT="非特有種";
		} else if(Integer.parseInt(Result_array2[6])==1){
			endemicspecies_TEXT="特有種";
		} else if(Integer.parseInt(Result_array2[6])==2){
			endemicspecies_TEXT="特有亞種";
		} else if(Integer.parseInt(Result_array2[6])==3){
			endemicspecies_TEXT="過境物種";
		} else if(Integer.parseInt(Result_array2[6])==4){
			endemicspecies_TEXT="冬候物種";
		} else if(Integer.parseInt(Result_array2[6])==5){
			endemicspecies_TEXT="疑問種";
		} else if(Integer.parseInt(Result_array2[6])==6){
			endemicspecies_TEXT="迷鳥";
		} else if(Integer.parseInt(Result_array2[6])==7){
			endemicspecies_TEXT="栽培種";
		} else if(Integer.parseInt(Result_array2[6])==8){
			endemicspecies_TEXT="歸化種";
		} else if(Integer.parseInt(Result_array2[6])==9){
			endemicspecies_TEXT="入侵種";
		} else if(Integer.parseInt(Result_array2[6])==10){
			endemicspecies_TEXT="夏候物種";
		}
		endemicspecies.setText(endemicspecies_TEXT);
		if(Integer.parseInt(Result_array2[7])==0){
			conservation_TEXT="非保育類";
		} else if(Integer.parseInt(Result_array2[7])==1){
			conservation_TEXT="瀕臨絕種野生動物";
		} else if(Integer.parseInt(Result_array2[7])==2){
			conservation_TEXT="珍貴稀有野生動物";
		} else if(Integer.parseInt(Result_array2[7])==3){
			conservation_TEXT="應予保育之野生動物";
		} else if(Integer.parseInt(Result_array2[7])==4){
			conservation_TEXT="禁採";
		} else if(Integer.parseInt(Result_array2[7])==5){
			conservation_TEXT="家畜";
		} 
		conservation.setText(conservation_TEXT);
		appear.setText(Result_array2[8]);
		
		Button wikiBtn=(Button)layout.findViewById(R.id.button1);
		wikiBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String strURL = "http://zh.m.wikipedia.org/wiki/"+TheChineseName;
				Intent it = new Intent(Intent.ACTION_VIEW,Uri.parse(strURL));
				cxt.startActivity(it);
			}
		});
		
		builder = new AlertDialog.Builder(cxt);     
		builder.setView(layout);     
		alertDialog = builder.create();     
		alertDialog.show();
	}
}
