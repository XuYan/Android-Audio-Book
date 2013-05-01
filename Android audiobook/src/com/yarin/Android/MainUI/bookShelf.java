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
	
	/* 有声图书的路径 */
	private static final String MUSIC_PATH = new String("/mnt/sdcard/");
	
	/* 有声图书列表 */
	private ArrayList<String> bookArrayList = new ArrayList<String>();
	
	/* 有声图书列表中每一行的适配器 */
	private bookShelfAdapter bookAdapter;
	
	/*标签*/
	public static int flag = 1;
	public int isPlaying;
	
	//定义手势检测器实例
	GestureDetector detector;
	final int FLIP_DISTANCE = 50;//定义手势动作两点之间的最小距离
	
	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bookshelf);
		
		linearLayout3 = (LinearLayout)findViewById(R.id.linearLayout3);
		changeSkin();
		
		musicList();
		
		//创建手势检测器
		detector = new GestureDetector(this);
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
	        		linearLayout3.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_dragon));	        		break;
	        	case 2:
	        		linearLayout3.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_horse));
	        		break;
	        		}
	    	}	
	    	cursor.close();
	    	skinDB.close();
		}
	
	/* 播放列表 */
	public void musicList()
	{
		//取得指定位置的文件设置显示到播放列表
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
	
	/* 当我们点击列表时，播放被点击的音乐 */
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		//首次进入bookShelf界面，无后台有声图书播放
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
		//后台有声图书正在播放，flag==0
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
				if(isPlaying == 1){	//如果后台有有声图书正在播放，弹出Alert Dialog
					Dialog dialog = new AlertDialog.Builder(bookShelf.this)
					.setTitle("Please Pay Attention")//设置标题
					.setMessage("Return to previous interface will lead to stop of current playing audio book")//设置内容
					.setPositiveButton("yes", //设置"确定"按钮
					new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int whichButton)
						{
							flag = 1;
					        bookShelf.this.setResult(1,intent);
					        bookShelf.this.finish();   
						}
					})
					.setNeutralButton("no",//设置"取消"按钮
					new DialogInterface.OnClickListener()
					{
					public void onClick(DialogInterface dialog, int whichButton)
					{
						
					}
					})
					.create();//创建
					dialog.show();
				}
				else{	//如果后台无有声图书正在播放,直接跳转页面
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