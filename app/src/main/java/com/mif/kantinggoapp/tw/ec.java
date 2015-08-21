package com.mif.kantinggoapp.tw;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import com.mif.kantinggoapp.util.EntryAdapter;
import com.mif.kantinggoapp.util.EntryItem;
import com.mif.kantinggoapp.util.Item;
import com.mif.kantinggoapp.util.SectionItem;

public class ec extends Activity implements OnItemClickListener{
	private ListView ec_lv;
	private EntryItem item;
	private ArrayList<Item> ec_items = new ArrayList<Item>();
	private boolean IsEcItemSetAlready=false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ec_list);
		SetView();
	}
	
	private void SetView(){
		//設定listview項目
		SetEcListViewItem();
		//Layout
		ec_lv = (ListView)findViewById(R.id.ec_lv);
        EntryAdapter adapter = new EntryAdapter(this, ec_items , R.drawable.twitter_bird);
        ec_lv.setAdapter(adapter);
        ec_lv.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if(!ec_items.get(position).isSection()){
		    item = (EntryItem)ec_items.get(position);
		    Intent intent = new Intent();
		    Bundle bundle = new Bundle();
		    bundle.putString("title", item.title);
		    intent.putExtras(bundle);
		    //切換到ec2_1.java
		    if(item.title=="南仁湖保護區" || item.title=="龍鑾潭保護區"){
				intent.setClass(ec.this, ec2_1.class);
		    } else { //切換到ec2_2.java
		    	intent.setClass(ec.this, ec2_2.class);
		    }
		    startActivity(intent);
		}
	}
	
	private void SetEcListViewItem() {
		if(IsEcItemSetAlready)
			return;
		//Ec Listview Items
		ec_items.add(new SectionItem("生態保護區"));
        ec_items.add(new EntryItem("南仁湖保護區", "Nan Ren Lake"));
        ec_items.add(new EntryItem("龍鑾潭保護區", "Longluan Lake"));
        ec_items.add(new SectionItem("特有生物"));
        ec_items.add(new EntryItem("鳥類", "Bird")); 
        ec_items.add(new EntryItem("爬蟲類", "Reptilian"));
        ec_items.add(new EntryItem("哺乳類", "Mammals"));
        ec_items.add(new EntryItem("甲殼類", "Crustaceans"));
        ec_items.add(new EntryItem("魚類", "Fish"));
        IsEcItemSetAlready=true;
	}
	
}
