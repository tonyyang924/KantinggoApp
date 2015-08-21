package com.mif.kantinggoapp.tw;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.mif.kantinggoapp.tool.Connection;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ec2_1 extends Activity{

	private TextView ec2_title,content;
	private ImageView img;
	//private LinearLayout layout;
	private String title;
	private Bitmap bitmap;
	private String ImageUrl;
	private boolean canUseConnect=false;
	protected static final int Image_data = 0x2;
	
	//progressDialog
	private ProgressDialog myDialog;
	
	//Handler 接收Thread訊息
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
				case Image_data:
					myDialog.dismiss();
					Log.i("TEST",String.valueOf(bitmap));
					if(title.equals("南仁湖保護區")){
						img.setImageBitmap(bitmap);
						content.setText("「南仁山生態保護區」位於國家公園 東側的滿州鄉南仁村，保護區海拔最高不 過526公尺，卻是台灣少數僅存的低海拔 原始熱帶季風雨林，同時形成熱帶、亞熱 帶與溫帶植物分布於南仁山區，受恆春半 島特殊季風及雨量季節性分佈的影響，共 有 2200餘種植物。如「大頭茶」潔白 似茶 花，是東北季風盛行的迎風陡坡的 常見植物。\n"+
								"砂島生態保護區 龍坑生態保護區 香蕉灣生態保育區 社頂高位珊瑚礁 生態保護區 「南仁湖」位於天然山谷中，僅一條步道能前往，單程4.3公里。沿途生態 樣貌豐富，常可見「青斑蝶」、「黃蝶」的蹤影。南仁湖原本是水稻田，由於 稻田東邊出水口被堵塞，水量累積逐日增多，形成寬廣湖泊，在這一片廣大水 域之前有處小水潭，才是真正的「南仁古湖」，但由於湖水面積小容易被忽略 。湖旁可見的「黃灰澤蟹」，是台灣特有種的淡水蟹，屬於陸蟹的一種，也是 唯一不須到海邊進行繁殖的蟹種。"+
								"目前南仁山為生態保護區，僅供學術研究，禁止遊客進入。事先必須向墾 丁國家公園管理處提出申請，前往南仁湖也必須先於墾丁國家公園遊客中心做 行前教育，而每年有兩個月的封山期間亦不受理申請進入。");
					}
					if(title.equals("龍鑾潭保護區")){
						img.setImageBitmap(bitmap);
						content.setText("「龍鑾潭生態保護區」位於恆春城西南方約3公里處，滿水面積可達175公頃，屬於國家公園的特別景觀區。龍鑾潭在清領時期是恆春地區農漁業生產的重要區域，但原屬地勢低窪濕地的龍鑾潭，隨著乾濕季節的更替，其面積也隨著變化不定。雨季時期，龍鑾潭面積擴大，附近田園全成水鄉澤國，為農民帶來很大災害。因此政府在民國37年將龍鑾潭建為水庫，目前所見的龍鑾潭其實是屬於半人工的水澤濕地。龍鑾潭與其潭區周遭的水澤、魚塭、農田、灌叢和次生林等不同型態的土地利用方式，組成良好的鳥類棲息環境。同時龍鑾潭的水澤環境，蘊含豐富的水生動物和昆蟲，更提供鳥兒們絕佳的覓食場所，造就了一處南臺灣的野鳥樂園。據歷年來的調查，龍鑾潭區域的鳥種達200種以上，其中以候鳥居多，佔了70%以上。依照時序的變化，每個月的鳥況各具特色。每年，八、九月之際，大批的鷸科鳥類過境恆春半島，在此覓食、休息、補充體力後，得以繼續牠們南向的旅程；冬季時，大群的雁鴨停留在龍鑾潭及其附近的水塘度過冬天，直至翌年春天才陸續北返，回到他們的繁殖區。近幾年來，國內賞鳥及研究鳥類生態的風氣漸盛，龍鑾潭地區乃成為一處賞鳥者與研究自然生態人士的聖地。 為了保護龍鑾潭特有的生態環境，並提供一個高品質的賞鳥與鳥類研究的樂園，墾丁國家公園管理處自民國77年起，積極從事龍鑾潭區域的整體規劃，以候鳥棲息地之保育為前提，對自然地的利用提供予使用者教育、生態與休憩三種綜合性功能之構想。並於83年1月1日開放啟用龍鑾潭自然中心，提供民眾高品質的環境教育空間。");
					}
					//layout.addView(img);
					//layout.addView(tv);
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
		
		//判斷是否有網路
		Connection ct = new Connection(this);
		canUseConnect = ct.IsConnected();
		
		SetView();
	}
	
	private void SetView() {
		
		//progressDialog
		myDialog = new ProgressDialog(this);
		//params
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(     
				LinearLayout.LayoutParams.WRAP_CONTENT,     
				LinearLayout.LayoutParams.WRAP_CONTENT 
			);
		lp.setMargins(20, 20, 20, 20);
		//title
		ec2_title = (TextView)findViewById(R.id.title);
		ec2_title.setText(title);
		//main layout
		//layout = (LinearLayout)findViewById(R.id.layout);
		img = (ImageView)findViewById(R.id.img);
		//content 
		content = (TextView)findViewById(R.id.content);
		//
		if(title.equals("南仁湖保護區")){
			ImageUrl = "http://rys.vexp.idv.tw/~kantinggo/_img/non_img.jpg";
		} else if(title.equals("龍鑾潭保護區")){
			ImageUrl = "http://www.ktnp.gov.tw/upload/ecologyPlace/20100511_220357.2524.jpg";
		}
		if(canUseConnect) {
			myDialog = ProgressDialog.show
	                 (
	                         this,
	                         "提示視窗",
	                         "載入中",
	                         true
	                       );
			new Thread(new Runnable() {
				@Override
				public void run() {
					Log.i("test",String.valueOf(ImageUrl));
					bitmap = getHttpBitmap(ImageUrl);
					handler.obtainMessage(Image_data).sendToTarget();
				}
			}).start();
		} else {
			Toast.makeText(ec2_1.this, "無法連結網路", Toast.LENGTH_LONG).show();
		}
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
