package com.yarin.Android.MainUI;

import android.content.Context;
import android.media.AudioManager;
import android.view.View;
import android.widget.Button;

public class VolumeAdjust{

	// private ImageButton muteButton;
	private AudioManager audioMa;
	private Context context;
	public int currentVolume;//当前音量
	public static boolean isMute = true;
	
	//构造函数???
	public VolumeAdjust(Context context){
		this.context=context;
	}

	//调低音量
	public Button.OnClickListener downButton = new Button.OnClickListener() {
		public void onClick(View arg0) {
			audioMa = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
			//处于静音状态时，点击调高音量按钮，静音撤销并降低音量
			audioMa.setStreamMute(AudioManager.STREAM_MUSIC, false);
			isMute = true;
			audioMa.adjustVolume(AudioManager.ADJUST_LOWER, 0);
			android.util.Log.i("ok", "ok");//日志
		}
	};

	//调高音量
	public Button.OnClickListener upButton = new Button.OnClickListener(){
		public void onClick(View arg0){
			audioMa = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
			//处于静音状态时，点击调高音量按钮，静音撤销并调高音量
			audioMa.setStreamMute(AudioManager.STREAM_MUSIC, false);
			isMute = true;
			audioMa.adjustVolume(AudioManager.ADJUST_RAISE,0);
		}
	};
	
	//静音
	public Button.OnClickListener muteButton = new Button.OnClickListener(){
		public void onClick(View arg0){
			audioMa = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
			currentVolume = audioMa.getStreamVolume(AudioManager.STREAM_MUSIC);
			if(isMute)
			{
				audioMa.setStreamMute(AudioManager.STREAM_MUSIC, isMute);
				isMute = false;
			}		
			else
			{
				audioMa.setStreamMute(AudioManager.STREAM_MUSIC, isMute);
				isMute = true;
			}

		}
	};
	
}
