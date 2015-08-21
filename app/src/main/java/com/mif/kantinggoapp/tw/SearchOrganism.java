package com.mif.kantinggoapp.tw;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class SearchOrganism extends Activity implements Button.OnClickListener{
	
	private String dbdir;
	private String dbfile;
	private SQLiteDatabase db;
	
	private Button submit;
	private EditText MonthField,
					 ScientificnamecField,
					 CollecterField,
					 AppraisalerField,
					 CollectmethodField;
	private Spinner YearSp,DataTimeSp,SearchAreaSp;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.searchorganism);
		//設定database存取位置
		MyDataBasePath();
		//設定view
		SetView();
		//listener
		SetListener();
	}
	
	private void MyDataBasePath() {
		dbdir="/data/data/com.mif.kantinggoapp.tw/databases";
		dbfile=dbdir+"/wetcollect.db";
	}
	
	private void SetView() {
		submit=(Button)findViewById(R.id.submit);
		YearSp=(Spinner)findViewById(R.id.YearSp);
		ArrayAdapter<String> year_adapter = new ArrayAdapter<String>(
				this,android.R.layout.simple_spinner_item,
				new String[]{"所有年份","2009","2010","2011","2012","2013","2014"});
		year_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		YearSp.setAdapter(year_adapter);
		MonthField=(EditText)findViewById(R.id.monthfield);
		DataTimeSp=(Spinner)findViewById(R.id.DataTimeSp);
		ArrayAdapter<String> datatime_adapter = new ArrayAdapter<String>(
				this,android.R.layout.simple_spinner_item,
				new String[]{"全天","日間","夜間","清晨","黃昏"});
		datatime_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		DataTimeSp.setAdapter(datatime_adapter);
		ScientificnamecField=(EditText)findViewById(R.id.scientificnamecfield);
		SearchAreaSp=(Spinner)findViewById(R.id.SearchAreaSp);
		ArrayAdapter<String> searcharea_adapter = new ArrayAdapter<String>(
				this,android.R.layout.simple_spinner_item,
				new String[]{"所有地區","龍鑾潭","南仁湖","其他地區"});
		searcharea_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		SearchAreaSp.setAdapter(searcharea_adapter);
		CollecterField=(EditText)findViewById(R.id.collecterfield);
		AppraisalerField=(EditText)findViewById(R.id.appraisalerfield);
		CollectmethodField=(EditText)findViewById(R.id.collectmethodfield);
	}
	
	private void SetListener() {
		submit.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v.equals(submit)){
			//取得目前系統時間
			SimpleDateFormat date = new java.text.SimpleDateFormat("HH:mm");
			String currentDate = date.format(new Date(System.currentTimeMillis()));
			String theTime[] = currentDate.split(":");
			int hour = Integer.parseInt(theTime[0]);
			int min = Integer.parseInt(theTime[1]);
			int NowTime = (hour*60)+min;
			Log.i("system","System.currentTimeMillis()");
			Log.i("time:",currentDate);
			Log.i("time_int:",String.valueOf(NowTime));
			//宣告額外SQL語法
			String year_sql="",month_sql="",datatime_sql="",
					Scientificnamec_sql="",SearchArea_sql="",Collecter_sql="",
					Appraisaler_sql="",Collectmethod_sql="",Appear_sql=" and B.appear='全天出現' ";
			
			//SQL參考
			//SELECT A.cid,A.sid,A.wsmape,A.wsmapn,A.cdate,A.c5,A.c7,A.c8,A.c4,B.scientificnamec,A.cdate2
			//FROM collect AS A,species AS B WHERE A.sid=B.sid 
			//and substring(A.cdate,1,4)='2011' 
			//and B.scientificnamec LIKE '%測試%' 
			//and A.wsmape >= 120.72894752025604 and A.wsmape <= 120.75601100921631 
			//and A.wsmapn >= 21.96502939223797 and A.wsmapn <= 21.992859455552463  
			//and A.c7 like '%coll%'  and A.c8 like '%apss%'  
			//and A.c4 like '%method%'  
			//and substring(A.cdate,6,2)='02' and A.cdate2 like '%全天%'  
			//and wsmapn<22.123492380625162 order by cid asc ;"
			
			//判斷欄位是否填寫，再填入額外的SQL語法
			if(!"".equals(YearSp.getSelectedItem().toString().trim())){ //年份
				String year = YearSp.getSelectedItem().toString().trim();
				if(!year.equals("所有年份")){
					year_sql=" and substr(A.cdate,1,4)='"+year+"' ";
				}
			}
			if(!"".equals(MonthField.getText().toString().trim())){ //月份
				int month = Integer.parseInt(MonthField.getText().toString().trim());
				if(month/10>=1){ //10 11 12月份
					month_sql=" and substr(A.cdate,6,2)='"+month+"' ";
				} else { //1~9月份，補個0
					month_sql=" and substr(A.cdate,6,2)='0"+month+"' ";
				}
			}
			if(!"".equals(DataTimeSp.getSelectedItem().toString().trim())){ //調查時段
				String datatime = DataTimeSp.getSelectedItem().toString().trim();
				datatime_sql=" and A.cdate2 like '%"+datatime+"%' ";
			}
			if(!"".equals(ScientificnamecField.getText().toString().trim())){ //中文學名
				String scientificnamec = ScientificnamecField.getText().toString().trim();
				Scientificnamec_sql=" and B.scientificnamec LIKE '%"+scientificnamec+"%' ";
			}
			if(!"".equals(SearchAreaSp.getSelectedItem().toString().trim())){ //地區
				String SearchArea = SearchAreaSp.getSelectedItem().toString().trim();
				if(SearchArea.equals("所有地區")){
					SearchArea_sql="";
				} else if(SearchArea.equals("龍鑾潭")){
					SearchArea_sql=" and A.wsmape >= 120.72894752025604 and A.wsmape <= 120.75601100921631 " +
							"and A.wsmapn >= 21.96502939223797 and A.wsmapn <= 21.992859455552463 ";
				} else if(SearchArea.equals("南仁湖")){
					SearchArea_sql=" and A.wsmape >= 120.85879325866699 and A.wsmape <= 120.87282657623291 " +
							"and A.wsmapn >= 22.080350879315684 and A.wsmapn <= 22.092519310958505 ";
				} else if(SearchArea.equals("其他地區")){
					SearchArea_sql=" and A.wsmapn < 21.96756414607923 ";
				}
			}
			if(!"".equals(CollecterField.getText().toString().trim())){ //調查者
				String Collecter = CollecterField.getText().toString().trim();
				Collecter_sql=" and A.c7 LIKE '%"+Collecter+"%' ";
			}
			if(!"".equals(AppraisalerField.getText().toString().trim())){ //鑑定者
				String Appraisaler = AppraisalerField.getText().toString().trim();
				Appraisaler_sql=" and A.c8 LIKE '%"+Appraisaler+"%' ";
			}
			if(!"".equals(CollectmethodField.getText().toString().trim())){ //調查方法
				String Collectmethod = CollectmethodField.getText().toString().trim();
				Collectmethod_sql=" and A.c4 LIKE '%"+Collectmethod+"%' ";
			}
			if(NowTime<=360 || (NowTime>1080 && NowTime<=1439) ){ //如果凌晨和晚上，判定為夜間出沒
				Appear_sql=" and B.appear='夜間出沒' ";
			}
			String SQL = "SELECT A.cid,A.sid,A.wsmape,A.wsmapn,A.cdate,A.c5,A.c7,A.c8,A.c4,B.scientificnamec,A.cdate2,B.familyc,B.scientificname "
					   + " FROM collect AS A,species AS B "
					   + " WHERE A.sid=B.sid "
					   +year_sql+month_sql+datatime_sql+Scientificnamec_sql
					   +SearchArea_sql+Collecter_sql
					   +Appraisaler_sql+Collectmethod_sql+Appear_sql
					   +" AND wsmapn<22.123492380625162";
			Log.i("TEST",SQL);
			Intent intent = new Intent(SearchOrganism.this,OrganismResult.class);
			Bundle bundle = new Bundle();
			bundle.putString("SQL", SQL);
			bundle.putString("dbfile", dbfile);
			intent.putExtras(bundle);
			startActivity(intent);
		}
	}
	
	
}
