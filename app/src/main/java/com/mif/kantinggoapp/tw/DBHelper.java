package com.mif.kantinggoapp.tw;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {
	
	/**
	 * @param _DBVersion 版本
	 */
	private final static int _DBVersion = 6; 
	/**
	 * @param _DBName    資料庫名稱
	 */
	private final static String _DBName = "kantinggo.db";
	/**
	 * @param _TBName	   資料表名稱
	 */
	private final static String _TBName = "organism";
	private final static String _TBName2 = "scene";
	private final static String _TBName3 = "food";
	
	public DBHelper(Context context) {
		//super(context, name, factory, version);
		super(context, _DBName, null, _DBVersion);
		Log.i("DBversion",String.valueOf(_DBVersion));
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS " +
				_TBName + " ( " + 
				"_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				"name VARCHAR(20), " +
				"content TEXT, " +
				"typechar VARCHAR(20), " +
				"imgurl TEXT" +
				");");
		db.execSQL("CREATE TABLE IF NOT EXISTS " +
				_TBName2+ " ( " +
				"_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				"sname VARCHAR(20), " +
				"introduce TEXT, " +
				"imgurl TEXT, " + 
				"wsmape real, " + 
				"wsmapn real"+ 
				");");
		db.execSQL("CREATE TABLE IF NOT EXISTS " +
				_TBName3+ " ( " +
				"_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				"tid INTEGER NOT NULL, " +
				"name VARCHAR(20), " +
				"tname VARCHAR(20), " +
				"tel VARCHAR(50), " +
				"addr VARCHAR(100), " +
				"time VARCHAR(50), " +
				"longitude REAL, " +
				"latitude REAL, " +
				"recommend INTEGER " +
				");");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.i("upgrade","執行upgrade"); 
		if(newVersion > oldVersion){
			db.beginTransaction();//建立交易
			
			boolean success = false; //判斷參數
			
			db.execSQL("CREATE TABLE IF NOT EXISTS " +
					_TBName3+ " ( " +
					"_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
					"tid INTEGER NOT NULL, " +
					"name VARCHAR(20), " +
					"tname VARCHAR(20), " +
					"tel VARCHAR(50), " +
					"addr VARCHAR(100), " +
					"time VARCHAR(50), " +
					"longitude REAL, " +
					"latitude REAL, " +
					"recommend INTEGER " +
					");");
			oldVersion++;
			
			success = true;
			
			if(success){
				db.setTransactionSuccessful();//正確交易才成功
			}
			db.endTransaction();
		} else {
			onCreate(db);
		}
	}
	
}
