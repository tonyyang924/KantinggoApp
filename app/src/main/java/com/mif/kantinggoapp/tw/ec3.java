package com.mif.kantinggoapp.tw;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mif.kantinggoapp.tool.Connection;

public class ec3 extends Activity{

	private String title;
	private TextView ec_title;
	private DBHelper DH = null;
	private String[] queryResult;
	//private LinearLayout layout;
	
	private Bitmap bitmap;
	private String ImageUrl;
	private boolean canUseConnect=false;
	protected static final int Image_data = 0x2;
	
	//progressDialog
	ProgressDialog myDialog;
	
	//Handler 接收Thread訊息
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
				case Image_data:
					//params
					LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(     
							LinearLayout.LayoutParams.WRAP_CONTENT,     
							LinearLayout.LayoutParams.WRAP_CONTENT 
						);
					lp.setMargins(20, 20, 20, 20);
					//image
					ImageView img = (ImageView)findViewById(R.id.img);
					img.setImageBitmap(bitmap);
					//textview
					TextView content = (TextView)findViewById(R.id.content);
					//content.setBackgroundColor(Color.WHITE);
					//content.setPadding(20, 20, 20, 20);
					//content.setTextSize(20);
					//content.setLayoutParams(lp);
					content.setText(queryResult[1]);
					//layout.addView(img);
					//layout.addView(content);
					myDialog.dismiss();
				break;
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); 
		setContentView(R.layout.ec_scroll);
		
		Bundle bundle = getIntent().getExtras();  
		title = bundle.getString("title");
		
		openDB();
		
		//判斷是否有網路
		Connection ct = new Connection(this);
		canUseConnect = ct.IsConnected();
		
		//搜尋
		queryResult = select(title);
		
		SetView();
	}
	
	private void openDB() {
		DH = new DBHelper(this);
	}
	
	private void closeDB() {
		DH.close();
	}
	
	private void SetView() {
		//title
		ec_title = (TextView)findViewById(R.id.title);
		ec_title.setText(queryResult[0]);
		//layout = (LinearLayout)findViewById(R.id.layout);
		ImageUrl = queryResult[3];
		if(canUseConnect) {
			myDialog = ProgressDialog.show(this,"提示視窗","載入中",true);
			new Thread(new Runnable() {
				@Override
				public void run() {
					bitmap = getHttpBitmap(ImageUrl);
					handler.obtainMessage(Image_data).sendToTarget();
				}
			}).start();
		}
		
		
		
	}
	
	//Select Query 
	private String[] select(String SelName) {
		SQLiteDatabase db = DH.getWritableDatabase();
		
		Cursor cursor = db.rawQuery("SELECT name,content,typechar,imgurl FROM organism " +
				"WHERE name = '" + SelName + "';", null);
		//if (cursor.moveToNext()){
		cursor.moveToFirst();
		String[] result = new String[4]; 
	    result[0] = cursor.getString(0);
	    result[1] = cursor.getString(1);
	    result[2] = cursor.getString(2);
	    result[3] = cursor.getString(3);
		//}
		cursor.close();
		return result;
	}
	
	/**
     * 
     * @param url
     * @return
     */
    public static Bitmap getHttpBitmap(String url){
    	URL myFileURL;
    	Bitmap bitmap=null;
    	try{
    		myFileURL = new URL(url);
    		HttpURLConnection conn=(HttpURLConnection)myFileURL.openConnection();
    		conn.setConnectTimeout(6000);
    		conn.setDoInput(true);
    		conn.setUseCaches(false);
    		InputStream is = conn.getInputStream();
    		bitmap = BitmapFactory.decodeStream(is);
    		is.close();
    	} catch (Exception e){
    		e.printStackTrace();
    	}
		return bitmap;
    	
    }
}
