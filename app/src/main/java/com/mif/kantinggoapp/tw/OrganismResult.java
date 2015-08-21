package com.mif.kantinggoapp.tw;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import com.mif.kantinggoapp.tool.ChatAdapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
//Import LoadDataTask

public class OrganismResult extends Activity{
	
	private String SQL,dbfile;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.organismresult);
		
		Bundle bundle = getIntent().getExtras();  
		SQL = bundle.getString("SQL");
		dbfile = bundle.getString("dbfile");
		
		Log.i("TEST",dbfile);
		
		//執行印出結果
		//SelectForResult(SQL);
		new LoadDataTaskResult(this,SQL,dbfile).execute();
	}
	
	class LoadDataTaskResult extends AsyncTask<String, String, String> {
		//Button
		private Button bt_wiki;
		//Listview
		private ListView lv;
		ArrayList<HashMap<String,String>> list = new ArrayList<HashMap<String,String>>();
		private SimpleAdapter adapter;
		//progressdialog
		private ProgressDialog myLoadingDialog;
		//DB
		private SQLiteDatabase db;
		//SQL dbfile
		private String SQL,dbfile;
		//Context
		private Context cxt;
		//結果
		private String[][] Result_array;
		//數量
		private int rows_num;
		public LoadDataTaskResult(Context context,String SQL,String dbfile) {
			this.cxt=context;
			this.SQL=SQL;
			this.dbfile=dbfile;
			//打開資料庫
			openDB();
			//progressdialog
			myLoadingDialog = new ProgressDialog(this.cxt);

			lv = (ListView)((Activity)cxt).findViewById(R.id.listview);
			lv.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> adapterView, View view,
						int position, long id) { 
					String sid = Result_array[position][1];
					showOrganismDialog(sid);
				}
			});
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
		protected String doInBackground(String... arg0) {
			GetData();
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			ShowData();
		}
		
		private void openDB() {
			db=SQLiteDatabase.openOrCreateDatabase(dbfile,null);
		}
		
		private void GetData() {
			Log.i("SQl",SQL);
			Cursor cursor = db.rawQuery(SQL, null);
			rows_num = cursor.getCount();
			int column_num  = cursor.getColumnCount(); 
			if(rows_num>0){
				Result_array = new String[rows_num][column_num];
				cursor.moveToFirst();
				for(int i=0;i<rows_num;i++){
					for(int j=0;j<column_num;j++){
						Result_array[i][j] = String.valueOf(cursor.getString(j));
					}
					cursor.moveToNext();
				}
				cursor.close();
			} else {
				Log.i("TEST","NO:"+String.valueOf(rows_num));
			}
		}
		
		private void ShowData() {
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
			} else {
				Toast.makeText(this.cxt, "沒有資料。", Toast.LENGTH_LONG).show();
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
			final String Name = sid;
			final String NameLG = Result_array2[1];
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
			final ImageView imgView = (ImageView) layout.findViewById(R.id.imageView1);
			bt_wiki = (Button) layout.findViewById(R.id.button1);
			bt_wiki.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					//Uri uri = Uri.parse("http://zh.m.wikipedia.org/wiki/"+Name);
					//Intent intent   = new Intent(Intent.ACTION_VIEW,uri);
					//intent.setClassName("com.android.browser", "com.android.browser.BrowserActivity");
					//startActivity(intent);
					String strURL = "http://zh.m.wikipedia.org/wiki/"+NameLG;
					Intent it = new Intent(Intent.ACTION_VIEW,Uri.parse(strURL));
					cxt.startActivity(it);
				}
			});
			
			String endemicspecies_TEXT = "",
					conservation_TEXT ="";
			
			title.setText(Result_array2[1]);
			scientificname.setText(Result_array2[2]);
			commonnamec.setText(Result_array2[3]);
			orderc.setText(Result_array2[4]);
			familyc.setText(Result_array2[5]);
			Thread ImgGet = new Thread(new Runnable() {
				@Override
				public void run() {
					final Drawable dw = loadImageFromURL("http://global.vexp.net/wetmap/narrowimg/"+Name+"_0.jpg");
					// TODO Auto-generated method stub
					((Activity) cxt).runOnUiThread(new Runnable(){
					      public void run(){ 
					    	//建立訊息 -> ListView
					    	  imgView.setImageDrawable(dw);
					      }
					});
				}
			});
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
			builder = new AlertDialog.Builder(cxt);     
			builder.setView(layout);     
			alertDialog = builder.create();     
			alertDialog.show();
			ImgGet.start();
		}
		
		private Drawable loadImageFromURL(String url){
	        try{
	            InputStream is = (InputStream) new URL(url).getContent();
	            Drawable draw = Drawable.createFromStream(is, "src");
	            return draw;
	        }catch (Exception e) {
	            //TODO handle error
	            Log.i("loadingImg", e.toString());
	            return null;
	        }
	    }
	}
}
