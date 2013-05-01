package com.yarin.Android.MainUI;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class TTS extends Activity implements OnGestureListener{
	private static final String BOOK_PATH = new String("/mnt/sdcard/"); 
	public String FILE_NAME;
	
	TextToSpeech tts;
	
	LinearLayout linearLayout5;//TTS界面的整个线性布局
	ListView listView;	//用于呈现SD卡中所有拓展名为txt的文档
	TextView textView;	//用于呈现txt文档中的内容
	Button speak;	//开始朗读按钮
	Button stop;	//停止朗读按钮
	
	/* 有声图书列表 */
	private ArrayList<String> arr = new ArrayList<String>();
	
	//定义手势检测器实例
	GestureDetector detector;
	final int FLIP_DISTANCE = 50;//定义手势动作两点之间的最小距离
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tts_layout);
		
		//创建手势检测器
		detector = new GestureDetector(this);
		
		Toast.makeText(TTS.this, "@ TTS mode", Toast.LENGTH_SHORT).show();//提示用户当前程序所处的模式
		
		linearLayout5 = (LinearLayout)findViewById(R.id.linearLayout5);
		listView = (ListView) findViewById(R.id.availabletxt);
		textView = (TextView) findViewById(R.id.currenttxt);
		speak = (Button) findViewById(R.id.speak);
		stop = (Button) findViewById(R.id.stop);
		
		textView.setText("");
		
		changeSkin();
		
		//取得指定位置的文件设置显示到播放列表
		File home = new File(BOOK_PATH);
		if (home.listFiles(new BookFilter()).length > 0)
		{
			for (File file : home.listFiles(new BookFilter()))
				arr.add(file.getName());
		}
			ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,arr);
			listView.setAdapter(arrayAdapter);
			
		//当点击列表时,播放被点击的书目
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0,View arg1,int arg2,long arg3){
				speak.setEnabled(true);//激活speak按钮
				FILE_NAME="/"+arr.get(arg2);
				textView.setText(display());
			}
		});
			
		// 初始化TextToSpeech对象
		tts = new TextToSpeech(this, new OnInitListener()
		{
			public void onInit(int status)
			{
				// 如果装载TTS引擎成功
				if (status == TextToSpeech.SUCCESS)
				{
					// 设置使用美式英语朗读
					int result = tts.setLanguage(Locale.US);
					// 如果不支持所设置的语言
					if (result != TextToSpeech.LANG_COUNTRY_AVAILABLE
						&& result != TextToSpeech.LANG_AVAILABLE)
					{
						Toast.makeText(TTS.this, "TTS暂时不支持这种语言的朗读。", 50000)
							.show();
					}
				}
			}

		});

		speak.setOnClickListener(new OnClickListener()
		{
			public void onClick(View arg0)
			{
				// 执行朗读
				tts.speak(textView.getText().toString(),
					TextToSpeech.QUEUE_ADD, null);
				stop.setEnabled(true);	//当点击speak按钮后，激活shut_up按钮
				speak.setEnabled(false);	//并将speak按钮变灰
			}
		});
		
		stop.setOnClickListener(new OnClickListener()
		{
			public void onClick(View arg0)
			{
				onStop();
				speak.setEnabled(true);	//当点击shut_up按钮后,激活speak按钮
				stop.setEnabled(false);	//并将shut_up按钮变灰
				
			}
		});	
	}
	
	//更改程序皮肤（背景）
		public void changeSkin(){
			SQLiteDatabase skinDB = SQLiteDatabase.openOrCreateDatabase("/mnt/sdcard/skin.db3",null);
			Cursor cursor = skinDB.query("skin_record", new String[]{"name,record"}, "name like ?", new String[]{"a"}, null, null, "_id desc");
	    	if(cursor.moveToFirst()){
	    		switch(Integer.parseInt(cursor.getString(1))){
	        	//默认情况下(case 0)是黑色背景
	        	case 0:
	        		break;
	        	case 1:
	        		linearLayout5.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_dragon));
	        		break;
	        	case 2:
	        		linearLayout5.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_horse));
	        		break;
	        		}
	    	}	
	    	cursor.close();
	    	skinDB.close();
		}

	public void onStop()
	{
		//停止TextToSpeech对象
		super.onPause();
		if(tts != null)
			tts.stop();
	}
	
	public void onDestroy()
	{
		// 关闭TextToSpeech对象
		if (tts != null)
			tts.shutdown();
		super.onDestroy();
	}
	
	private String display(){
		try {
			// 如果手机插入了SD卡，而且应用程序具有访问SD的权限
			if (Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED)) {
				// 获取SD卡对应的存储目录
				File sdCardDir = Environment.getExternalStorageDirectory();
				// 获取指定文件对应的输入流
				FileInputStream fis = new FileInputStream(sdCardDir
						.getCanonicalPath()
						+ FILE_NAME);
				// 将指定输入流包装成BufferedReader
				BufferedReader br = new BufferedReader(new InputStreamReader(
						fis));
				StringBuilder sb = new StringBuilder("");
				String line = null;
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
				return sb.toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	//将该activity上的触碰事件交给GestureDetector处理
		@Override
		public boolean onTouchEvent(MotionEvent me)
		{
		return detector.onTouchEvent(me);	
		}

		public boolean onDown(MotionEvent arg0){
			return false;
		}
		
		public void onLongPress(MotionEvent event){	}
		
		public boolean onScroll(MotionEvent event1, MotionEvent event2, float arg2, float arg3){
			return false;
		}
		
		public void onShowPress(MotionEvent event){}
		
		public boolean onSingleTapUp(MotionEvent event){
			return false;
		}
		
		public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY){
			//如果第二个触电事件的X坐标大于第一个触电事件的X坐标超过FLIP_DISTANCE,也就是手势从右向左滑动
			if (event2.getX() - event1.getX() > FLIP_DISTANCE) {
				Intent intent = new Intent(TTS.this, DoubleMode.class);
				startActivity(intent);
				this.overridePendingTransition(R.anim.zoom_in,R.anim.zoom_out);
				this.finish();
				return true;
				}
				return false;
		}
}