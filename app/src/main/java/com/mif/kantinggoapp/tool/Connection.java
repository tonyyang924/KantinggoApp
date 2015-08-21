package com.mif.kantinggoapp.tool;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Connection {
	
	private Context cxt;
	
	public Connection(Context cxt){
		this.cxt=cxt;
	}
	
	public boolean IsConnected() {
		boolean result=false;
		ConnectivityManager CM = (ConnectivityManager) this.cxt.getSystemService(cxt.CONNECTIVITY_SERVICE);
		NetworkInfo info = CM.getActiveNetworkInfo();
		if(info==null || !info.isConnected()){
			result=false;
		} else {
			if(!info.isAvailable()){
				result=false;
			} else {
				result=true;
			}
		}
		return result;
	}
}
