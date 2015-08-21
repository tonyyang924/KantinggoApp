package com.mif.kantinggoapp.util;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mif.kantinggoapp.tw.R;

public class EntryAdapter extends ArrayAdapter<Item> {
	private static final String TAG = "Kantinggo::EntryAdapter";
	private Context context;
	private ArrayList<Item> items;
	private int drawimage;
	private LayoutInflater vi;
	

	public EntryAdapter(Context context,ArrayList<Item> items, int drawimage) {
		super(context,0, items);
		this.context = context;
		this.items = items;
		this.drawimage = drawimage;
		vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;

		final Item i = items.get(position);
		if (i != null) {
			if(i.isSection()){
				SectionItem si = (SectionItem)i;
				v = vi.inflate(R.layout.list_item_section, null);

				v.setOnClickListener(null);
				v.setOnLongClickListener(null);
				v.setLongClickable(false);
				
				final TextView sectionView = (TextView) v.findViewById(R.id.list_item_section_text);
				sectionView.setText(si.getTitle());
				sectionView.setTextSize(20);
			}else{
				EntryItem ei = (EntryItem)i;
				v = vi.inflate(R.layout.list_item_entry, null);
				final ImageView image = (ImageView)v.findViewById(R.id.list_item_entry_drawable);
				final TextView title = (TextView)v.findViewById(R.id.list_item_entry_title);
				final TextView subtitle = (TextView)v.findViewById(R.id.list_item_entry_summary);
				
				
				if (title != null) 
					title.setText(ei.title);
				if(subtitle != null)
					subtitle.setText(ei.subtitle);
				if(image != null)
					image.setImageResource(drawimage);
			}
		}
		return v;
	}

}
