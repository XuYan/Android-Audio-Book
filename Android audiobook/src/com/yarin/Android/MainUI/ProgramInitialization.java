package com.yarin.Android.MainUI;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class ProgramInitialization extends Activity {
	
	private File fileDir;
	private File sdcardDir;
	
	private ArrayList<String> path = new ArrayList<String>();
	
	private boolean hasSDCard;
	
	private String audioBookPath;//audioBook�ļ��е�·��
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.program_initialization);
        
        final Handler initHandler = new Handler(){
        	public void handleMessage(Message msg){
        		switch(msg.what){
        			case 0:
        				setTitle("Folder \"audioBook\"is created successfully!");
        				break;
        			case 1:
        				setTitle("Folder \"db\" is create successfully");
        				break;
        			case 2:
        				setTitle("Folder \"illustrations\" is create successfully");
        				break;
        			case 3:
        				setTitle("Folder \"lrcPath\" is create successfully");
        				break;
        			case 4:
        				setTitle("Folder \"txtPath\" is create successfully");
        				break;
        			case 5:
        				Intent intent = new Intent(ProgramInitialization.this, DoubleMode.class);
        				startActivity(intent);
        				finish();
        				overridePendingTransition(R.anim.zoom_in,R.anim.zoom_out);
        				break;
        			case 0x1233:
        				Toast.makeText(ProgramInitialization.this, "SD card has been found", Toast.LENGTH_LONG);
        		}
        	}
        };
        
        new Thread(){
        	public void run(){ 
        		  int i;
        		  String folderPath;//ArrayList<String> path�д洢����Ҫ�����ļ��е�·��
        		  Message initMessage = initHandler.obtainMessage();
				  String status = Environment.getExternalStorageState();
				  try{
				  //�ж�SD���Ƿ����
				  if (status.equals(Environment.MEDIA_MOUNTED)){
					  initMessage.what = 0x1233;
			    	  initHandler.sendMessage(initMessage);
					  sdcardDir = Environment.getExternalStorageDirectory();
					  hasSDCard = true;
					  audioBookPath = sdcardDir.getPath()+java.io.File.separator+"audioBook";
					  /*Log.d("sdcardDir","sdcardDir is"+sdcardDir);
				      Log.d("path","path is"+path);
				      Log.d("sdcardDir.getParent()","sdcardDir.getParent() is"+sdcardDir.getParent());
				      Log.d("sdcardDir.getName()","sdcardDir.getName() is"+sdcardDir.getName());*/
					  
					  /*
					   *����װ�ú��״�������Ҫ��ʼ��һЩ��Ҫ�ļ���
					   *if��������ж���Щ��Ҫ�ļ����Ƿ��Ѿ�����
					   *��������ڣ���ͨ��initialization()��ʼ��
					   *��֮������˯��������Զ�ת��DoubleMode.java
					   */
					  if(!new File(audioBookPath).exists()){
					      initialization();
					      
					      for(i=0;i<5;i++){
					    	  folderPath = path.get(i);
					    	  createFolder(folderPath);
			        		  initMessage = initHandler.obtainMessage();
					    	  initMessage.what = i;
					    	  initHandler.sendMessage(initMessage);
					    	  sleep(1500);
					      }
					  }
					  else
						  sleep(3000);

				      
	        		  initMessage = initHandler.obtainMessage();
			    	  initMessage.what = 5;
			    	  initHandler.sendMessage(initMessage);
		    	     
				  }
				  else {
					  Toast.makeText(ProgramInitialization.this, "SD card has not been inserted", Toast.LENGTH_LONG);
				      fileDir = ProgramInitialization.this.getFilesDir();
				      hasSDCard = false;
				      audioBookPath=fileDir.getPath()+java.io.File.separator+"audioBook";
				      /*Log.d("fileDir","fileDir is"+fileDir);
				      Log.d("path","path is"+path);
				      Log.d("fileDir.getParent()","fileDir.getParent() is"+fileDir.getParent());
				      Log.d("fileDir.getName()","fileDir.getName() is"+fileDir.getName());*/
					  if(!new File(audioBookPath).exists()){
					      initialization();
					      
					      for(i=0;i<5;i++){
					    	  folderPath = path.get(i);
					    	  createFolder(folderPath);
					    	  initMessage.what = i;
					    	  initHandler.sendMessage(initMessage);
					    	  sleep(1500);
					      }
					  }
					  else
						  sleep(3000);
				      
	        		  initMessage = initHandler.obtainMessage();
			    	  initMessage.what = 5;
			    	  initHandler.sendMessage(initMessage);

				  }
				  }catch(InterruptedException ex){
					  Log.d("InterruptedException","InterruptedException has occured");
				  }


        	}
        }.start();
        
		}

    //������Ҫ�����ĳ����ļ���·��
    public void initialization(){
    	if(hasSDCard)
        	path.add(0,sdcardDir.getPath()+java.io.File.separator+"audioBook");
    	else
    		path.add(fileDir.getPath()+java.io.File.separator+"audioBook");
    	path.add(1,path.get(0) + java.io.File.separator+"db");
    	path.add(2,path.get(0) + java.io.File.separator+"illustrations");
    	path.add(3,path.get(0) + java.io.File.separator+"lrc");
    	path.add(4,path.get(0) + java.io.File.separator+"txt");
    }
    
    //���������ļ���
    public void createFolder(String path){
    	File destDir = new File(path);
    	if (!destDir.exists())
    		   destDir.mkdirs();
    }
}