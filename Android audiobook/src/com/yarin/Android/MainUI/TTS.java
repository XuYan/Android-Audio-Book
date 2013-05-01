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
	
	LinearLayout linearLayout5;//TTS������������Բ���
	ListView listView;	//���ڳ���SD����������չ��Ϊtxt���ĵ�
	TextView textView;	//���ڳ���txt�ĵ��е�����
	Button speak;	//��ʼ�ʶ���ť
	Button stop;	//ֹͣ�ʶ���ť
	
	/* ����ͼ���б� */
	private ArrayList<String> arr = new ArrayList<String>();
	
	//�������Ƽ����ʵ��
	GestureDetector detector;
	final int FLIP_DISTANCE = 50;//�������ƶ�������֮�����С����
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tts_layout);
		
		//�������Ƽ����
		detector = new GestureDetector(this);
		
		Toast.makeText(TTS.this, "@ TTS mode", Toast.LENGTH_SHORT).show();//��ʾ�û���ǰ����������ģʽ
		
		linearLayout5 = (LinearLayout)findViewById(R.id.linearLayout5);
		listView = (ListView) findViewById(R.id.availabletxt);
		textView = (TextView) findViewById(R.id.currenttxt);
		speak = (Button) findViewById(R.id.speak);
		stop = (Button) findViewById(R.id.stop);
		
		textView.setText("");
		
		changeSkin();
		
		//ȡ��ָ��λ�õ��ļ�������ʾ�������б�
		File home = new File(BOOK_PATH);
		if (home.listFiles(new BookFilter()).length > 0)
		{
			for (File file : home.listFiles(new BookFilter()))
				arr.add(file.getName());
		}
			ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,arr);
			listView.setAdapter(arrayAdapter);
			
		//������б�ʱ,���ű��������Ŀ
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0,View arg1,int arg2,long arg3){
				speak.setEnabled(true);//����speak��ť
				FILE_NAME="/"+arr.get(arg2);
				textView.setText(display());
			}
		});
			
		// ��ʼ��TextToSpeech����
		tts = new TextToSpeech(this, new OnInitListener()
		{
			public void onInit(int status)
			{
				// ���װ��TTS����ɹ�
				if (status == TextToSpeech.SUCCESS)
				{
					// ����ʹ����ʽӢ���ʶ�
					int result = tts.setLanguage(Locale.US);
					// �����֧�������õ�����
					if (result != TextToSpeech.LANG_COUNTRY_AVAILABLE
						&& result != TextToSpeech.LANG_AVAILABLE)
					{
						Toast.makeText(TTS.this, "TTS��ʱ��֧���������Ե��ʶ���", 50000)
							.show();
					}
				}
			}

		});

		speak.setOnClickListener(new OnClickListener()
		{
			public void onClick(View arg0)
			{
				// ִ���ʶ�
				tts.speak(textView.getText().toString(),
					TextToSpeech.QUEUE_ADD, null);
				stop.setEnabled(true);	//�����speak��ť�󣬼���shut_up��ť
				speak.setEnabled(false);	//����speak��ť���
			}
		});
		
		stop.setOnClickListener(new OnClickListener()
		{
			public void onClick(View arg0)
			{
				onStop();
				speak.setEnabled(true);	//�����shut_up��ť��,����speak��ť
				stop.setEnabled(false);	//����shut_up��ť���
				
			}
		});	
	}
	
	//���ĳ���Ƥ����������
		public void changeSkin(){
			SQLiteDatabase skinDB = SQLiteDatabase.openOrCreateDatabase("/mnt/sdcard/skin.db3",null);
			Cursor cursor = skinDB.query("skin_record", new String[]{"name,record"}, "name like ?", new String[]{"a"}, null, null, "_id desc");
	    	if(cursor.moveToFirst()){
	    		switch(Integer.parseInt(cursor.getString(1))){
	        	//Ĭ�������(case 0)�Ǻ�ɫ����
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
		//ֹͣTextToSpeech����
		super.onPause();
		if(tts != null)
			tts.stop();
	}
	
	public void onDestroy()
	{
		// �ر�TextToSpeech����
		if (tts != null)
			tts.shutdown();
		super.onDestroy();
	}
	
	private String display(){
		try {
			// ����ֻ�������SD��������Ӧ�ó�����з���SD��Ȩ��
			if (Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED)) {
				// ��ȡSD����Ӧ�Ĵ洢Ŀ¼
				File sdCardDir = Environment.getExternalStorageDirectory();
				// ��ȡָ���ļ���Ӧ��������
				FileInputStream fis = new FileInputStream(sdCardDir
						.getCanonicalPath()
						+ FILE_NAME);
				// ��ָ����������װ��BufferedReader
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
	
	
	//����activity�ϵĴ����¼�����GestureDetector����
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
			//����ڶ��������¼���X������ڵ�һ�������¼���X���곬��FLIP_DISTANCE,Ҳ�������ƴ������󻬶�
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