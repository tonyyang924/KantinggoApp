package com.mif.kantinggoapp.tw;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mif.kantinggoapp.tool.Connection;

public class at2 extends Activity {

	private String title;
	private TextView at2_title, content;
	private ImageView img;
	private LinearLayout layout;
	private DBHelper DH = null;
	private String[] queryResult;
	private ProgressDialog myDialog;
	private boolean canUseConnect;
	private Bitmap bitmap;

	protected static final int Result_Data = 0x1;

	// Handler 接收Thread訊息
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Result_Data:
				// params
				LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.WRAP_CONTENT,
						LinearLayout.LayoutParams.WRAP_CONTENT);
				lp.setMargins(20, 20, 20, 20);
				// textview
				content = (TextView) findViewById(R.id.content);
				// content.setBackgroundColor(Color.WHITE);
				// content.setPadding(20, 20, 20, 20);
				// content.setTextSize(20);
				// content.setLayoutParams(lp);
				content.setText(queryResult[1]);
				// image
				img = (ImageView) findViewById(R.id.img);
				img.setImageBitmap(bitmap);
				// layout.addView(img);
				// layout.addView(content);
				myDialog.dismiss();
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.at_scroll);

		openDB();

		// 判斷是否有網路
		Connection ct = new Connection(this);
		canUseConnect = ct.IsConnected();

		Bundle bundle = getIntent().getExtras();
		title = bundle.getString("title");

		// 搜尋
		queryResult = select(title);
		for (int i = 0; i < queryResult.length; i++)
			Log.i("QQ", queryResult[i]);
		SetView();
	}

	private void openDB() {
		DH = new DBHelper(this);
	}

	private void closeDB() {
		DH.close();
	}

	private void SetView() {
		// progressDialog
		myDialog = new ProgressDialog(this);

		at2_title = (TextView) findViewById(R.id.title);
		at2_title.setText(title);

		// layout=(LinearLayout)findViewById(R.id.layout);

		if (canUseConnect) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					bitmap = getHttpBitmap(queryResult[2]);
					handler.obtainMessage(Result_Data).sendToTarget();
				}
			}).start();
		}
	}

	// Select Query
	private String[] select(String SelName) {
		SQLiteDatabase db = DH.getWritableDatabase();
		
		String SQL = "SELECT sname,introduce,imgurl,wsmape,wsmapn FROM scene "
				+ "WHERE sname = '" + SelName + "';";
		Log.i("XD",SQL);
		Cursor cursor = db.rawQuery(SQL, null);
		// if (cursor.moveToNext()){
		if (cursor.getCount() == 0) {
			DH.onCreate(db);
		}
		cursor.moveToFirst();
		String[] result = new String[cursor.getColumnCount()];
		result[0] = cursor.getString(0);
		result[1] = cursor.getString(1);
		result[2] = cursor.getString(2);
		result[3] = cursor.getString(3);
		result[4] = cursor.getString(4);
		// }
		cursor.close();
		return result;
	}

	public static Bitmap getHttpBitmap(String url) {
		URL myFileURL;
		Bitmap bitmap = null;
		try {
			myFileURL = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) myFileURL
					.openConnection();
			conn.setConnectTimeout(6000);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			InputStream is = conn.getInputStream();
			bitmap = BitmapFactory.decodeStream(is);
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bitmap;

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.at_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// action with ID action_refresh was selected
		case R.id.googlemap:
			//Toast.makeText(this, "進入Google map頁面", Toast.LENGTH_SHORT).show();
			Intent intent = new Intent();
			Bundle bundle = new Bundle();
			bundle.putString("wsmape", queryResult[3]);
			bundle.putString("wsmapn", queryResult[4]);
			intent.putExtras(bundle);
			intent.setClass(at2.this, at2_map.class);
			startActivity(intent);
			break;
		// action with ID action_settings was selected
		default:
			break;
		}

		return true;
	}
}
