package com.yarin.Android.MainUI;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.SubMenu;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DoubleMode extends Activity{
	private TextView explanation = null;
	
	private LinearLayout linearLayout = null;
	private SQLiteDatabase skinDB;//skinDB用于存储用户选定的程序皮肤
	final String CREATE_TABLE_SQL = "create table skin_record(_id integer primary key autoincrement, name, record)";//创建存储程序皮肤的表格skin_record
	private int skinID=0;
	
	Intent intent = new Intent();
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.displaymenu);
				
		explanation = (TextView)this.findViewById(R.id.explanation);
		linearLayout = (LinearLayout)findViewById(R.id.linearLayout);
		
		//从数据库中调取程序背景记录并更改程序背景
		getSkinRecord();
		
		String string = "Welcome to my audio book player client. " +
						"Two playing modes are provided in my player client. " +
						"Click Menu Button to select mode.";
		explanation.setTextSize(15);
		explanation.setText(string);
	}
	
	/*创建menu*/
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuItem TTS = menu.add("Play Text Files");
		TTS.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			
			public boolean onMenuItemClick(MenuItem arg0) {
				// TODO Auto-generated method stub
				skinRecord();
				/* 指定intent要启动的类 */
				intent.setClass(DoubleMode.this, TTS.class);
				/* 启动一个新的Activity */
				startActivity(intent);
				/* 关闭当前的Activity */
				DoubleMode.this.finish();
				return false;
			}
		});
		
		MenuItem AF = menu.add("Play Audio Files");
		AF.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			
			public boolean onMenuItemClick(MenuItem arg0) {
				// TODO Auto-generated method stub
				skinRecord();
				intent.setClass(DoubleMode.this, InitialUI.class);
				startActivity(intent);
				DoubleMode.this.finish();
				return false;
			}
		});
		
		//“Change Skins”选项包含子菜单
		SubMenu skin = menu.addSubMenu("Change Skins");
		skin.setHeaderTitle("Select a skin for program");
		MenuItem black = skin.add("Black");
		MenuItem dragon = skin.add("Thunder Dragon");
		MenuItem horse = skin.add("Impetuous Zebra");
		
		black.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			
			public boolean onMenuItemClick(MenuItem arg0) {
				// TODO Auto-generated method stub
				linearLayout.setBackgroundColor(Color.BLACK);
				skinID = 0;
				return false;
			}
		});
		
		dragon.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			
			public boolean onMenuItemClick(MenuItem arg0) {
				// TODO Auto-generated method stub
				linearLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_dragon));
				skinID = 1;
				return false;
			}
		});
		
		horse.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			
			public boolean onMenuItemClick(MenuItem arg0) {
				// TODO Auto-generated method stub
				linearLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_horse));
				skinID = 2;
				return false;
			}
		});
		
		MenuItem exit = menu.add("Exit");
		exit.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			
			public boolean onMenuItemClick(MenuItem arg0) {
				// TODO Auto-generated method stub
				skinRecord();
				DoubleMode.this.finish();
				return false;
			}
		});
		
		return super.onCreateOptionsMenu(menu);
	}
	
	//当用户退出程序时，将程序皮肤录入数据库skin.db3
	public void skinRecord(){
		Cursor cursor = skinDB.query("skin_record", new String[]{"name,record"}, "name like ?", new String[]{"a"}, null, null, "_id desc");
		if(cursor.moveToFirst()){
			ContentValues values = new ContentValues();
			values.put("record", skinID+"");
			skinDB.update("skin_record", values, "name like ?", new String[]{"a"});
		}else{
			skinDB.execSQL("insert into skin_record values(null , ? , ?)", new String[]{"a", skinID+""});
		}
		cursor.close();
		skinDB.close();
	}
	
	public void getSkinRecord(){
				//从数据库中调取用户选定的程序皮肤记录
				try{
					skinDB = SQLiteDatabase.openOrCreateDatabase("/mnt/sdcard/skin.db3",null);
		        	Cursor cursor = skinDB.query("skin_record", new String[]{"name,record"}, "name like ?", new String[]{"a"}, null, null, "_id desc");
		        	if(cursor.moveToFirst())
		        		skinID = Integer.parseInt(cursor.getString(1));
		        	cursor.close();
		        	
		        	switch(skinID){
		        	//默认情况下(case 0)是黑色背景
		        	case 0:
		        		break;
		        	case 1:
		        		linearLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_dragon));
		        		break;
		        	case 2:
		        		linearLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_horse));
		        		break;
		        		}
		        	
		        	}catch(Exception ex){
		        	skinDB.execSQL(CREATE_TABLE_SQL);
		        	}
	}

}