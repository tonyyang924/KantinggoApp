package com.mif.kantinggoapp.tw;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

public class Main extends Activity implements Button.OnClickListener{

	private ImageButton newsBtn,Ecological_listBtn,AttractionsBtn,SynthesizeGuideBtn,exitBtn
					,SearchOrganismBtn,SearchStationBtn;
	
	//Zip File
	private String dbdir;
	private String dbfile;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		//設定database存取位置
		MyDataBasePath();
		//複製到database空間
		DatabaseProcess();
		SetView();
		SetListener();
	} 
	
	//Zip function
	private void MyDataBasePath() {
		dbdir="/data/data/com.mif.kantinggoapp.tw/databases";
		File myDataPathdbdir = new File(dbdir);
		if(!myDataPathdbdir.exists())
			(new File(dbdir)).mkdirs();
		else
			Toast.makeText(Main.this, "Database Path已存在!", Toast.LENGTH_SHORT).show();
		dbfile=dbdir+"/wetcollect.db";
	}
	private void DatabaseProcess() {
		//複製到database空間
		try{
			InputStream fis = getBaseContext().getAssets().open("wetcollect.zip");
			ZipInputStream zin = new  ZipInputStream(new BufferedInputStream(fis));
			int BUFFER = 4096;
			int count;
			byte[] buffer = new byte[4096];
			FileOutputStream fos = new FileOutputStream(dbfile);
			BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);
			while(( zin.getNextEntry()) != null) {
				while ((count = zin.read(buffer, 0, BUFFER)) != -1) {
					//System.out.write(x);
					dest.write(buffer, 0, count);
				}
				dest.flush();
				dest.close();
				fos.close();
			}
			zin.close();
			Toast.makeText(Main.this, "Done!!", Toast.LENGTH_SHORT).show();
		}
		catch (Exception e){}
	}
	//Zip File End
	
	private void SetView() {
		//newsBtn = (Button)findViewById(R.id.news);
		Ecological_listBtn = (ImageButton)findViewById(R.id.Ecological_list);
		AttractionsBtn = (ImageButton)findViewById(R.id.Attractions);
		SynthesizeGuideBtn = (ImageButton)findViewById(R.id.SynthesizeGuide);
		SearchOrganismBtn = (ImageButton)findViewById(R.id.SearchOrganism);
		SearchStationBtn = (ImageButton)findViewById(R.id.SearchStation);
		exitBtn = (ImageButton)findViewById(R.id.exit);
	}
	
	private void SetListener() {
		//newsBtn.setOnClickListener(this);
		Ecological_listBtn.setOnClickListener(this);
		AttractionsBtn.setOnClickListener(this);
		SynthesizeGuideBtn.setOnClickListener(this);
		SearchOrganismBtn.setOnClickListener(this);
		SearchStationBtn.setOnClickListener(this);
		exitBtn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		/*******
		if(v.equals(newsBtn)){
			Intent intent = new Intent(Main.this, news.class);
			startActivity(intent);
		****/
		if(v.equals(Ecological_listBtn)){
			Intent intent = new Intent(Main.this, ec.class);
			startActivity(intent);
		} else if(v.equals(AttractionsBtn)){
			Intent intent = new Intent(Main.this, at.class);
			startActivity(intent);
		} else if(v.equals(SynthesizeGuideBtn)){
			Intent intent = new Intent(Main.this, sg.class);
			startActivity(intent);
		
		} else if(v.equals(SearchOrganismBtn)){
			Intent intent = new Intent(Main.this, SearchOrganism.class);
			startActivity(intent);
		
		} else if(v.equals(SearchStationBtn)){
			Intent intent = new Intent(Main.this, SearchStation.class);
			startActivity(intent);
		} else if(v.equals(exitBtn)){
			ConfirmExit();
		}
		
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {//捕捉返回鍵
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {   
        	//按返回鍵，則執行退出確認
        	ConfirmExit();
            return true;   
        }   
        return super.onKeyDown(keyCode, event);
    }
	
	private void ConfirmExit(){//退出確認
        AlertDialog.Builder ad=new AlertDialog.Builder(Main.this);
        ad.setTitle("離開");
        ad.setMessage("確定要離開?");
        ad.setPositiveButton("是", new DialogInterface.OnClickListener() {//退出按鈕
            public void onClick(DialogInterface dialog, int i) {
                // TODO Auto-generated method stub
            	Main.this.finish();//關閉activity
            }
        });
        ad.setNegativeButton("否",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                //不退出不用執行任何操作
            }
        });
        ad.show();//示對話框
    }
}
