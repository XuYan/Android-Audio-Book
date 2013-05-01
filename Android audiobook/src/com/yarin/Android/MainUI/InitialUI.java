package com.yarin.Android.MainUI;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

public class InitialUI extends Activity implements OnGestureListener{
	LinearLayout linearLayout2 = null;
	/* �����б� */
	ListView listView=null;
	Intent intent = null;
	
	//�������Ƽ����ʵ��
	GestureDetector detector;
	final int FLIP_DISTANCE = 50;//�������ƶ�������֮�����С����
	
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.initial);
		
		//�������Ƽ����
		detector = new GestureDetector(this);
		
		Toast.makeText(InitialUI.this, "@ AudioFile mode", Toast.LENGTH_SHORT).show();//��ʾ�û���ǰ����������ģʽ
		
		linearLayout2 = (LinearLayout)findViewById(R.id.linearLayout2);
		listView = (ListView)findViewById(R.id.listView);
		
		changeSkin();
		
		String[] options = {"View Book Shelf","View online book store"};
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1,options);
		
		listView.setAdapter(adapter);
		
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long id) {
				switch (position) {
				case 0:
			        intent = new Intent(InitialUI.this, bookShelf.class);
					startActivity(intent);
					finish();
					break;

				case 1:
					intent = new Intent(InitialUI.this, webPage.class);
					startActivity(intent);
					finish();
					break;
				}
				
			}
		});
	
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
			Intent intent = new Intent(InitialUI.this, DoubleMode.class);
			startActivity(intent);
			this.overridePendingTransition(R.anim.zoom_in,R.anim.zoom_out);
			this.finish();
			return true;
			}
			return false;
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
        		linearLayout2.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_dragon));
        		break;
        	case 2:
        		linearLayout2.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_horse));
        		break;
        		}
    	}	
    	cursor.close();
    	skinDB.close();
	}
}