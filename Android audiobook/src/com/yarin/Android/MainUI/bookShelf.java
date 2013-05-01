package com.yarin.Android.MainUI;

import java.io.File;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

public class bookShelf extends ListActivity implements OnGestureListener{
	private LinearLayout linearLayout3 = null;
	
	/* ����ͼ���·�� */
	private static final String MUSIC_PATH = new String("/mnt/sdcard/");
	
	/* ����ͼ���б� */
	private ArrayList<String> bookArrayList = new ArrayList<String>();
	
	/* ����ͼ���б���ÿһ�е������� */
	private bookShelfAdapter bookAdapter;
	
	/*��ǩ*/
	public static int flag = 1;
	public int isPlaying;
	
	//�������Ƽ����ʵ��
	GestureDetector detector;
	final int FLIP_DISTANCE = 50;//�������ƶ�������֮�����С����
	
	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bookshelf);
		
		linearLayout3 = (LinearLayout)findViewById(R.id.linearLayout3);
		changeSkin();
		
		musicList();
		
		//�������Ƽ����
		detector = new GestureDetector(this);
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
	        		linearLayout3.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_dragon));	        		break;
	        	case 2:
	        		linearLayout3.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_horse));
	        		break;
	        		}
	    	}	
	    	cursor.close();
	    	skinDB.close();
		}
	
	/* �����б� */
	public void musicList()
	{
		//ȡ��ָ��λ�õ��ļ�������ʾ�������б�
		File home = new File(MUSIC_PATH);
		if (home.listFiles(new MusicFilter()).length > 0)
		{
			for (File file : home.listFiles(new MusicFilter()))
			{
				bookArrayList.add(file.getName());
			}

			bookAdapter = new bookShelfAdapter(bookShelf.this, bookArrayList);
			setListAdapter(bookAdapter);
		}
		else
			Toast.makeText(this, "no audio books with extension name mp3 are stored in SD card", Toast.LENGTH_LONG).show();
	}
	
	/* �����ǵ���б�ʱ�����ű���������� */
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		//�״ν���bookShelf���棬�޺�̨����ͼ�鲥��
		if(flag==1){
			Bundle data = new Bundle();
			//String bookName = bookList.get(position).get("bookName").toString();
	        data.putStringArrayList("bookList", bookArrayList);
			data.putInt("bookNum", position);
			Intent intent = new Intent(bookShelf.this,MainUIActivity.class);
	        intent.putExtras(data);
	        startActivity(intent);
			this.overridePendingTransition(R.anim.zoom_in,R.anim.zoom_out);
	        flag=0;
	        this.finish();
		}
		//��̨����ͼ�����ڲ��ţ�flag==0
		else{
			Intent intent = getIntent();
			Bundle data = new Bundle();
			data.putStringArrayList("bookList", bookArrayList);
	        data.putInt("bookNum", position);
	        intent.putExtras(data);
	        bookShelf.this.setResult(0,intent);
	        bookShelf.this.finish();
			this.overridePendingTransition(R.anim.zoom_in,R.anim.zoom_out);
		}

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
			if (flag == 1) {
				Intent intent = new Intent(bookShelf.this, InitialUI.class);
				startActivity(intent);
				this.overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
				this.finish();
				return true;
			}
			else{
				final Intent intent = getIntent();
				Bundle data = intent.getExtras();
				isPlaying = data.getInt("isPlaying");
				if(isPlaying == 1){	//�����̨������ͼ�����ڲ��ţ�����Alert Dialog
					Dialog dialog = new AlertDialog.Builder(bookShelf.this)
					.setTitle("Please Pay Attention")//���ñ���
					.setMessage("Return to previous interface will lead to stop of current playing audio book")//��������
					.setPositiveButton("yes", //����"ȷ��"��ť
					new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int whichButton)
						{
							flag = 1;
					        bookShelf.this.setResult(1,intent);
					        bookShelf.this.finish();   
						}
					})
					.setNeutralButton("no",//����"ȡ��"��ť
					new DialogInterface.OnClickListener()
					{
					public void onClick(DialogInterface dialog, int whichButton)
					{
						
					}
					})
					.create();//����
					dialog.show();
				}
				else{	//�����̨������ͼ�����ڲ���,ֱ����תҳ��
			        flag = 1;
					bookShelf.this.setResult(2,intent);
			        bookShelf.this.finish();   
				}
				return true;
			}
		}
		return false;
	}
		
}