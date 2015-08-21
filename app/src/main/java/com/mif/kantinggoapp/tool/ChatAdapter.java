package com.mif.kantinggoapp.tool;

import java.util.ArrayList;
import java.util.Map;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.mif.kantinggoapp.tw.R;

public class ChatAdapter extends BaseAdapter
 implements ListAdapter {

	private ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();
	private Context ctx;
	private int num;
	
	public ChatAdapter(Context context) { 
		ctx = context;
	}
	
	public void setListItem(ArrayList<Map<String, String>> lists) {
		list = lists;
	}
	
	public void setNumList(int n) {
		num = n;
    }
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public int getItemViewType(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override 
	public View getView(int position, View convertView, ViewGroup parent) {
		
		Map<String, String> entity = list.get(position);
		/*LinearLayout layout = new LinearLayout(ctx);
		LayoutInflater vi = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		vi.inflate(R.layout.listarray_layout, layout, true);*/
		convertView = LayoutInflater.from(ctx).inflate(R.layout.listarray_layout, null);
		
		TextView ChatType = (TextView) convertView.findViewById(R.id.Chat_type);
		ChatType.setText(entity.get("ChatType"));
		
		TextView Chat_content = (TextView) convertView.findViewById(R.id.Chat_content);
		Chat_content.setText(entity.get("ChatContent"));
		
		TextView Chat_time = (TextView) convertView.findViewById(R.id.Chat_time);
		Chat_time.setText("");
		
		return convertView;
		
	}


	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void registerDataSetObserver(DataSetObserver arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean areAllItemsEnabled() {
		// TODO Auto-generated method stub
		return false; 
	}

	@Override
	public boolean isEnabled(int arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getViewTypeCount() {
		// TODO Auto-generated method stub
		return list.size();
	}


	
}