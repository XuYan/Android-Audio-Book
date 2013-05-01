package com.yarin.Android.MainUI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;

public class HttpDownloader{
	public void downloadFile(final String strPath,final webPage wa) throws Exception{
		
		final ProgressBarDialog downloadProgress = new ProgressBarDialog(wa);

		final Handler mHandler = new Handler(new Handler.Callback() {
			public boolean handleMessage(Message msg) {
				switch (msg.what) {
				case 1:
					downloadProgress.show();
					break;
				case 4:
					downloadProgress.dismiss();
					break;
				case 2:
					downloadProgress.setDMax(msg.arg1);
					break;
				case 3:
					downloadProgress.setDProgress(msg.arg1);
					break;
				}
				return true;
			}
		});
		
		//����һ��AlertDialog.Builder����
		final Builder builder = new AlertDialog.Builder(wa);
		//���öԻ���ı���
		builder.setTitle("Save the audio book as");
		//װ��/res/layout/saveas.xml���沼��
		final LinearLayout saveAsAlertDialog = (LinearLayout)wa.getLayoutInflater().inflate(R.layout.saveas,null);
		//���öԻ�����ʾ��view����
		builder.setView(saveAsAlertDialog);
		builder.setPositiveButton("Confirm"
				//Ϊ��ť���ü�����
				,new OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which)
					{
						new Thread(new Runnable() {
							public void run() {
								try {
									mHandler.sendEmptyMessage(1);
									EditText saveAs = (EditText)saveAsAlertDialog.findViewById(R.id.save_as); 
									String audioBookName;
									String path = "/mnt/sdcard/";
									int fileSize;//�ļ�����
									int downloadFileSize = 0;//�����ڵ��ļ�����
									
									audioBookName = saveAs.getText().toString()+".mp3";
									
									URL myURL = new URL(strPath);
									URLConnection conn = myURL.openConnection();
									conn.connect();
									
									InputStream is = conn.getInputStream();
									if (is==null) throw new RuntimeException("stream is null");
									
									fileSize = conn.getContentLength();
									Message msg = mHandler.obtainMessage();
									msg.arg1 = fileSize;
									msg.what = 2;
									mHandler.sendMessage(msg);
									
									if (fileSize <= 0) throw new RuntimeException("cannot know the size of the file");
									Log.d("size",fileSize+"");
									
									File myTempFile = new File(path,audioBookName);
									
									FileOutputStream fos = new FileOutputStream(myTempFile);
									byte buf[] = new byte[512];
									
									do
									{
										int numread = is.read(buf);
										Message msg2 = mHandler.obtainMessage();
										msg2.arg1 = downloadFileSize;
										msg2.what = 3;
										mHandler.sendMessage(msg2);
										if(numread==-1)
										{
											mHandler.sendEmptyMessage(4);
											break;
										}
										fos.write(buf, 0, numread);
										downloadFileSize+=numread;
										
									}while(true);
									
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}).start();
					}
				});
		//Ϊ�Ի�������һ��ȡ����ť
		builder.setNegativeButton("Cancel"
				, new OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which)
					{
						
					}
				});
		builder.create().show();
	}
}