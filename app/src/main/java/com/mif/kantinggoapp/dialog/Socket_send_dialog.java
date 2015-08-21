package com.mif.kantinggoapp.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.mif.kantinggoapp.tw.R;


public class Socket_send_dialog extends AlertDialog.Builder {
	
	private Context cxt;
	private Button Send_btn,
				   Exit_btn;
	
	public Socket_send_dialog(Context context) {
		
		super(context);
		
		this.cxt = context;
		
		@SuppressWarnings("static-access")
		LayoutInflater inflater = (LayoutInflater)((Activity)cxt).getSystemService(cxt.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.sg_socket_send_dialog, null);
		setView(layout);
		
		Send_btn = (Button) layout.findViewById(R.id.Dialog_send_btn);
		Exit_btn = (Button) layout.findViewById(R.id.Dialog_exit_btn);
		
	}
	
}