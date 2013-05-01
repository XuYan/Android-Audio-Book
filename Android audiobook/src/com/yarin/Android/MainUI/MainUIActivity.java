package com.yarin.Android.MainUI;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Queue;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

@SuppressWarnings("rawtypes")

public class MainUIActivity extends Activity implements GestureDetector.OnGestureListener{
	SQLiteDatabase db;
	final String CREATE_TABLE_SQL = "create table pasttime(_id integer primary key autoincrement , name, time)";
	
	public int isPlaying = 0;	//isPlaying变量标识当前状态下有无有声图书正在播放
	
	GestureDetector detector;
	final int FLIP_DISTANCE = 50;//定义手势动作两点之间的最小距离
	
	private LinearLayout linearLayout4 = null;//主播放界面的整个线性布局，用于更改界面背景
	
	private TextView bookName = null;//显示指定图书书名
	
	private TextView currentTime = null;//当前播放时间
	SeekBar mSeekBar;//进度条
	private TextView durationTime = null;//歌曲总时间
	private boolean stopSeekBar = false;
	
	private ImageButton imageSwitch = null;//插图到文本切换按钮
	private ImageView imageView = null; //插图视图
	private String illustrationsPath = "/mnt/sdcard/audioBook/illustrations/";//imageView中显示的插图的路径
	private TextView textView = null;	//音频文件同步文本视图
	private ImageButton imageSwitch2 = null;//文本到插图切换按钮
	
	private int mark = 0;//标签：mark等于1说明同步文本是txt格式的，mark等于2说明同步文本是lrc格式的
	//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	private Handler handler2 = new Handler();
	private ArrayList<Queue> queues = null;//声明元素为队列的ArrayList:queues
	private UpdateTimeCallback updateTimeCallback = null;//线程
	private long nextTimeMill = 0;//有声图书同步文本每行的起始时间，单位为ms
	private long previousTimeMill = 0;//记录上一个nextTimeMill的时间，单位为ms(此变量用于当用户将进度条定位到之前的某一时刻时)
	private long currentTimeMill = 0;
	private String message = null;//同步文本框内的一行文本
	//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	
	private Handler handler3 = new Handler();
	private TextThread textThread = null;//线程
	private String line;//txt文档中的一段文字
    private double totalLength = 0;//txt文档总共有多少字节
    private double lengthPerLine = 0;//txt文档每段有多少字节
    private double totalTimeMill = 0;//有声图书的总时间，单位为ms
    private double timePerLine = 0;//有声图书每段的时间，单位为ms
    private double accumulatedTimeMill = 0;//从开始播放到下次更新textView的累积时间
	private BufferedReader br;
	private double previousTime = 0;
	
	/* 几个操作按钮 */
	private Handler fhandler = new Handler();//用于快进快退
	private Rewind rewind = new Rewind();
	private Forward forward = new Forward();
	private ImageButton mRewindImageButton = null;
	private ImageButton	mStopImageButton	= null;
	private ImageButton	mStartImageButton	= null;
	private ImageButton	mPauseImageButton	= null;
	private ImageButton mForwardImageButton = null;
	
	private ImageButton	mFrontImageButton	= null;//上一本
	public ImageButton downButton = null;//音量调小
	public ImageButton upButton = null;//音量调大
	public ImageButton muteButton = null;//静音按钮
	private ImageButton	mNextImageButton	= null;//下一本
	private Button mSleepButton = null;//定时按钮
	private Handler timingHandler = null;
	private boolean countDownActivation = false;
	
	//定时关闭功能对话框中的widgets
    private Spinner hoursSpinner = null;
    private ArrayAdapter<String> hourOptions;
    private static final String[] hours = {"0", "1", "2", "3"};
    private String hourSelection;
    
    private Spinner minutesSpinner = null;
    private ArrayAdapter<String> minuteOptions;
    private static final String[] minutes = {"1", "5", "10", "15", "20", "30", "45", "60"};
    private String minuteSelection;
    
    Button launching = null;
    Button cancel = null;
    
    long beginTime, timeNow, endTime;
	
	
	/* MediaPlayer对象 */
	public MediaPlayer	mMediaPlayer	= null;
	
	/* 播放列表 */
	private ArrayList<String> mMusicList = new ArrayList<String>();
	
	/* 当前播放歌曲的索引 */
	public int currentListItem;
	
	/* 音乐的路径 */
	private static final String MUSIC_PATH = new String("/mnt/sdcard/");

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		db = SQLiteDatabase.openOrCreateDatabase("/mnt/sdcard/audioBook/db/pastTime.db3", null);
		/* 构建MediaPlayer对象 */
		mMediaPlayer	= new MediaPlayer();

		//对象初始化
		detector = new GestureDetector(this);//创建手势检测器
		
		linearLayout4= (LinearLayout)findViewById(R.id.linearLayout4);
		
		bookName = (TextView) findViewById(R.id.bookName);
		
		currentTime = (TextView) findViewById(R.id.currentTime);
		mSeekBar = (SeekBar) findViewById(R.id.seekBar);
		durationTime = (TextView) findViewById(R.id.durationTime);
		
		imageSwitch = (ImageButton) findViewById(R.id.SwithchImage);
		imageView = (ImageView) findViewById(R.id.ImageView);
		textView = (TextView) findViewById(R.id.TextView);
		imageSwitch2 = (ImageButton) findViewById(R.id.SwithchImage2);
		
		mRewindImageButton = (ImageButton) findViewById(R.id.RewindButton);
		mStopImageButton = (ImageButton) findViewById(R.id.StopImageButton);
		mStartImageButton = (ImageButton) findViewById(R.id.StartImageButton); 
		mPauseImageButton = (ImageButton) findViewById(R.id.PauseImageButton);
		mForwardImageButton = (ImageButton) findViewById(R.id.ForwardButton);
		
		mFrontImageButton = (ImageButton) findViewById(R.id.LastImageButton); 
		downButton = (ImageButton) findViewById(R.id.downButton);//调低声音按钮
		upButton = (ImageButton) findViewById(R.id.upButton);//调高声音按钮
		muteButton = (ImageButton) findViewById(R.id.muteButton);//静音按钮
		mNextImageButton = (ImageButton) findViewById(R.id.NextImageButton); 
		mSleepButton = (Button)findViewById(R.id.sleep);//定时结束程序按钮
		
		final Builder countDownBuilder = new AlertDialog.Builder(this);
		
		//更改程序皮肤（背景）
		changeSkin();
		
		//取得bookShelf.java中传来的bookNum，并播放相应的音频文件
		Intent intent = getIntent();//获得启动该activity之前的activity对应的Intent
		Bundle data = intent.getExtras();//获取该intent所携带的数据
		currentListItem = data.getInt("bookNum");//从Bundle数据包中取出数据
		mMusicList = data.getStringArrayList("bookList");
		bookName.setText(showCurrentBook(currentListItem,mMusicList));//用于在id为bookName的textView中显示当前播放的音频文件的名字
		playMusic(MUSIC_PATH + mMusicList.get(currentListItem));
		addIllustrations(mMusicList.get(currentListItem));//addIllustrations的参数为用户在bookShelf中点击的有声图书的名字，拓展名为.mp3
		try{
		goOn();
		}catch(Exception ex){
			db.execSQL(CREATE_TABLE_SQL);//当用户首次运行程序，路径/mnt/sdcard/audioBook/db/pastTime.db3中没有叫做pasttime的table时，执行此行异常捕捉代码
		}

		//进度条监听器
		mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if (fromUser)
					mMediaPlayer.seekTo(progress);
			}
		});
		
		
		//控制音量按钮监听器
		VolumeAdjust volumeAdj = new VolumeAdjust(this);//声明一个VolumeAdjust类的实例volumeAdj
		downButton.setOnClickListener(volumeAdj.downButton);
		upButton.setOnClickListener(volumeAdj.upButton);
		muteButton.setOnClickListener(volumeAdj.muteButton);
		

		//插图到文本切换按钮
		imageSwitch.setOnClickListener(new ImageButton.OnClickListener()
		{
			public void onClick(View v)
			{
				imageSwitch2.setVisibility(View.VISIBLE);
				imageSwitch.setVisibility(View.INVISIBLE);
				imageView.setVisibility(View.GONE);
				textView.setVisibility(View.VISIBLE);
			}
		});
		
		//文本到插图切换按钮
		imageSwitch2.setOnClickListener(new ImageButton.OnClickListener()
		{
			public void onClick(View v)
			{
				imageSwitch.setVisibility(View.VISIBLE);
				imageSwitch2.setVisibility(View.INVISIBLE);
				textView.setVisibility(View.GONE);
				imageView.setVisibility(View.VISIBLE);
			}
		});
		
		//快退按钮
		mRewindImageButton.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					fhandler.post(rewind);
					mMediaPlayer.pause();
					break;

				case MotionEvent.ACTION_UP:
					fhandler.removeCallbacks(rewind);
					mMediaPlayer.start();
					break;
				}
				return false;
			}
		});
		
		//停止按钮
		mStopImageButton.setOnClickListener(new ImageButton.OnClickListener() 
		{
			public void onClick(View v)
			{
					//重置MediaPlayer到初始状态
					isPlaying = 0;
					mMediaPlayer.reset();
					mSeekBar.setProgress(0);
					currentTime.setText(toTime(mMediaPlayer.getCurrentPosition()));
					stopSync();
			}
		}); 
		
		//开始按钮
		mStartImageButton.setOnClickListener(new ImageButton.OnClickListener() 
		{
			public void onClick(View v)
			{
				playMusic(MUSIC_PATH + mMusicList.get(currentListItem));
			}
		});  
		
		//暂停
		mPauseImageButton.setOnClickListener(new ImageButton.OnClickListener() 
		{
			public void onClick(View view)
			{
				if (mMediaPlayer.isPlaying())
				{
					/* 暂停 */
					mMediaPlayer.pause();
					record();
					if(mark == 2)
						handler2.removeCallbacks(updateTimeCallback);//不再同步更新有声图书lrc格式文本
					else if(mark == 1)
						handler3.removeCallbacks(textThread);
				}
				else 
				{
					/* 开始播放 */
					mMediaPlayer.start();
					if(mark == 2)
						handler2.postDelayed(updateTimeCallback, 5);
					else if(mark == 1)
						handler3.postDelayed(textThread, (long)(accumulatedTimeMill-mMediaPlayer.getCurrentPosition()));
				}
			}
		});
		
		//快进按钮
		mForwardImageButton.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					fhandler.post(forward);
					mMediaPlayer.pause();
					break;

				case MotionEvent.ACTION_UP:
					fhandler.removeCallbacks(forward);
					mMediaPlayer.start();
					break;
				}
				return false;
			}
		});
		
		//下一首
		mNextImageButton.setOnClickListener(new ImageButton.OnClickListener() 
		{
			public void onClick(View arg0)
			{
				nextMusic();
			}
		});
		//上一首
		mFrontImageButton.setOnClickListener(new ImageButton.OnClickListener() 
		{
			public void onClick(View arg0)
			{
				FrontMusic();
			}
		});
		
		//sleep按钮响应
        mSleepButton.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				CountDownDialog(countDownBuilder);
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
	       		linearLayout4.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_dragon));
	       		break;
	       	case 2:
	       		linearLayout4.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_horse));
	       		break;
	       		}
	   	}	
	   	cursor.close();
	   	skinDB.close();
	}
	
    public void CountDownDialog(Builder countDownBuilder){
        LayoutInflater timingInflater = LayoutInflater.from(this);
        View timingView = timingInflater.inflate(R.layout.timing_dialog, null);

        countDownBuilder.setView(timingView);
        final Dialog dialog = countDownBuilder.show();
        
        hoursSpinner = (Spinner)timingView.findViewById(R.id.inputHours);
        minutesSpinner = (Spinner)timingView.findViewById(R.id.inputMinutes);
        launching = (Button)timingView.findViewById(R.id.launching);
        cancel = (Button)timingView.findViewById(R.id.cancel);
        
        hourOptions = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, hours);
        hourOptions.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        hoursSpinner.setAdapter(hourOptions);
        
        hoursSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
        	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3){
        		arg0.setVisibility(View.VISIBLE);
        		hourSelection = hours[arg2];
        	}

			public void onNothingSelected(AdapterView<?> arg0) {				
			}
        });
        
        minuteOptions = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, minutes);
        minuteOptions.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        minutesSpinner.setAdapter(minuteOptions);
        
        minutesSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
        	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3){
        		arg0.setVisibility(View.VISIBLE);
        		minuteSelection = minutes[arg2];
        	}
        	
			public void onNothingSelected(AdapterView<?> arg0) {
			}
        });
        
        launching.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				long h,m;
				h = Integer.parseInt(hourSelection);
				m = Integer.parseInt(minuteSelection);
				timeToZero(h,m);
				dialog.dismiss();
			}
		});
        
        cancel.setOnClickListener(new OnClickListener(){
        	public void onClick(View v){
        		dialog.dismiss();
        	}
        });
        
        timingHandler = new Handler(){
        	public void handleMessage(Message msg){
            	if(msg.what == 1){
            		record();
            		stopSync();
            		if(mMediaPlayer.isPlaying())
            			mMediaPlayer.reset();
            		finish();
            		System.exit(0);//完全退出
            	}
	
            	if(msg.what == 2)
            		Log.d("tag","display this message every second");
        	}
        };
    }
    
    public void timeToZero(long hours, long minutes){
		beginTime = System.currentTimeMillis();
		endTime = (hours*60*60+minutes*60)*1000;
		
		new Thread(){
			public void run(){
				countDownActivation = true;//标签:当其值为true时，此"计时关闭"线程正在运行
				while(countDownActivation){
					timeNow = System.currentTimeMillis();
					timingHandler.sendEmptyMessage(2);
					try {
						sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				
					if(timeNow-beginTime>=endTime){
						Message msg = timingHandler.obtainMessage();
						msg.what = 1;
						timingHandler.sendMessage(msg);
						break;
					}
				}
				this.interrupt();
			}
		}.start();
    }
	
	//按下返回键
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if ( keyCode ==  KeyEvent.KEYCODE_BACK)
		{
			mMediaPlayer.stop();
			mMediaPlayer.release();
			this.finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void playMusic(String path)
	{
		try
		{
			/* 设置要播放的文件的路径 */
			mMediaPlayer.setDataSource(path);
			/* 准备播放 */
			mMediaPlayer.prepare();
			mSeekBar.setMax(mMediaPlayer.getDuration());
			durationTime.setText(toTime(mMediaPlayer.getDuration()));
			textFormat();//开始同步更新图书文本
			/* 开始播放 */
			mMediaPlayer.start();
			updateSeekBar();
			mMediaPlayer.setOnCompletionListener(new OnCompletionListener() 
			{
				public void onCompletion(MediaPlayer arg0)
				{
					mMediaPlayer.seekTo(0);//当前有声图书播放完毕后进度自动归零
					//播放完成一首之后进行下一首
					nextMusic();
				}
			});
		}catch (IOException e){
			Log.d("test","test");
		}
	}

	/* 更新seekBar和currentTime*/
	private void updateSeekBar()
	{
		//开辟Thread用于定期刷新SeekBar
		DelayThread dThread = new DelayThread(1000);
		dThread.start();
	}
	
	//进度条实时更新(每秒更新)
	private Handler handler = new Handler(){
		public void handleMessage(Message msg){
				currentTime.setText(toTime(mMediaPlayer
						.getCurrentPosition()));
				mSeekBar.setProgress(mMediaPlayer.getCurrentPosition());
		}
	};
	
	/* 下一首 */
	private void nextMusic()
	{
		record();//将当前播放的有声图书的已播放时间记录到数据库中
		
		if (++currentListItem >= mMusicList.size())
		{
			stopSync();
			currentListItem = 0;
			/* 重置MediaPlayer */
			mMediaPlayer.reset();
			playMusic(MUSIC_PATH + mMusicList.get(currentListItem));
			addIllustrations(mMusicList.get(currentListItem));
			bookName.setText(showCurrentBook(currentListItem,mMusicList));
			goOn();
		}
		else
		{
			stopSync();
			/* 重置MediaPlayer */
			mMediaPlayer.reset();
			playMusic(MUSIC_PATH + mMusicList.get(currentListItem));
			addIllustrations(mMusicList.get(currentListItem));
			bookName.setText(showCurrentBook(currentListItem,mMusicList));
			goOn();
		}
	}
	
	/* 上一首 */
	private void FrontMusic()
	{
		record();//将当前播放的有声图书的已播放时间记录到数据库中
		
		if (--currentListItem < 0)
		{
			stopSync();
			currentListItem = mMusicList.size()-1;
			/* 重置MediaPlayer */
			mMediaPlayer.reset();
			playMusic(MUSIC_PATH + mMusicList.get(currentListItem));
			addIllustrations(mMusicList.get(currentListItem));
			bookName.setText(showCurrentBook(currentListItem,mMusicList));
			goOn();
		}
		else
		{
			stopSync();
			/* 重置MediaPlayer */
			mMediaPlayer.reset();
			playMusic(MUSIC_PATH + mMusicList.get(currentListItem));
			addIllustrations(mMusicList.get(currentListItem));
			bookName.setText(showCurrentBook(currentListItem,mMusicList));
			goOn();
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
			if(mMediaPlayer.isPlaying())	//判断当前状态下有无有声图书正在播放,如果有
				isPlaying = 1;
			countDownActivation = false;//标签:当其值为false时，此"计时关闭"线程即将停止
			Intent intent = new Intent(MainUIActivity.this, bookShelf.class);
			Bundle data = new Bundle();
			data.putInt("isPlaying", isPlaying);
			intent.putExtras(data);
			startActivityForResult(intent,0);
			this.overridePendingTransition(R.anim.zoom_in,R.anim.zoom_out);
			stopSeekBar = true;//停止更新播放进度条的线程tThread
			return true;
			}
			return false;
	}
	
	
	/*重写该方法，该方法以回调的方式来获取指定Activity返回的结果*/
	public void onActivityResult(int requestCode,int resultCode,Intent intent){
		if(requestCode==0&&resultCode==0){	//情况1:后台有有声图书正在播放,用户点击书架中的有声图书
			stopSync();
			record();
			mMediaPlayer.reset();
			mMediaPlayer.release();
			mMediaPlayer = new MediaPlayer();
			Bundle data = intent.getExtras();
			currentListItem = data.getInt("bookNum");//从Bundle数据包中取出数据
			mMusicList = data.getStringArrayList("bookList");
			bookName.setText(showCurrentBook(currentListItem,mMusicList));//用于在id为bookName的textView中显示当前播放的音频文件的名字
			playMusic(MUSIC_PATH + mMusicList.get(currentListItem));
			addIllustrations(mMusicList.get(currentListItem));
			goOn();
			}
		else if(requestCode==0&&resultCode==1){	//情况2:后台有有声图书正在播放,用户想从书架界面返回Play Audio Files 初始界面
				record();
				stopSync();
				mMediaPlayer.reset();
				mMediaPlayer.release();
		        Intent intent1 = new Intent(MainUIActivity.this, InitialUI.class);
				startActivity(intent1);
				finish();
			}
		else if(requestCode==0&&resultCode==2){	//情况3:后台无有声图书正在播放,用户想从书架界面返回Play Audio Files初始界面
				record();
	        	Intent intent1 = new Intent(MainUIActivity.this, InitialUI.class);
	        	startActivity(intent1);
				finish();
			}
		}
	

	/* 转换时间格式（毫秒到时分秒） */
	public String toTime(int time){
		time /= 1000;
		int minute = time / 60;
		int hour = minute / 60;
		int second = time % 60;
		return String.format("%02d:%02d:%02d", hour, minute, second);
	}
	
	//显示当前正在播放的有声图书的名字
	public String showCurrentBook(int num, ArrayList<String> list){
		String bookName;
		int endIndex;
		int No;
		ArrayList<String> bookList;
		String beforeBookName = "current playing audio book is ";
		
		No = num;
		bookList = list;
		bookName = bookList.get(No);
		endIndex = bookName.indexOf('.');
		bookName=beforeBookName+bookName.substring(0, endIndex);
		return bookName;
	}
	
	//将当前播放的有声图书的配套插图插入到imageView中,插图支持jpg和png格式
	public void addIllustrations(String bookName){
		String bookNameJpg, bookNamePng;
		bookNameJpg = bookName.substring(0, bookName.lastIndexOf('.')+1) + "jpg";
		bookNamePng = bookName.substring(0, bookName.lastIndexOf('.')+1) + "png";
		String illustrationsPath1 = illustrationsPath + bookNameJpg;
		String illustrationsPath2 = illustrationsPath + bookNamePng;
		//检查文件是否存在
		File f1 = new File(illustrationsPath1);
		File f2 = new File(illustrationsPath2);
		if(f1.exists()){	//检查jpg格式插图是否存在
			Bitmap bm = BitmapFactory.decodeFile(illustrationsPath1);
			imageView.setImageBitmap(bm);
		}
		else if(f2.exists()){	//检查png格式插图是否存在
			Bitmap bm = BitmapFactory.decodeFile(illustrationsPath2);
			imageView.setImageBitmap(bm);
		}
		else{
			Toast.makeText(this, "No illustrations exist!", Toast.LENGTH_SHORT).show();
			imageView.setImageResource(R.drawable.illustration_background);//若当前播放的有声图书没有jpg或png格式的插图，则将imageView中的插图替换成纯黑色背景图
		}

	}
	
	//数据库操作，向pastTime.db3文件中插入每部有声图书已经播放的时间
	public void insertData(SQLiteDatabase db, String bookName, String pastTime)
	{
		db.execSQL("insert into pasttime values(null, ?, ?)", new String[]{bookName, pastTime});
	}
	
	//将当前播放的有声图书的播放时间记录到数据库中
	public void record(){
		Cursor cursor = db.query("pasttime", new String[]{"name,time"}, "name like ?", new String[]{mMusicList.get(currentListItem)}, null, null, null, null);

		if(cursor.moveToFirst()){	//如果数据库中有当前正在播放的有声图书的记录，则更新已播放时间
			ContentValues values = new ContentValues();
			values.put("time", mMediaPlayer.getCurrentPosition());
			db.update("pasttime", values, "name like ?", new String[]{mMusicList.get(currentListItem)});
		}
		else	//否则，就插入一条新记录
			insertData(db, mMusicList.get(currentListItem), ""+mMediaPlayer.getCurrentPosition());
		cursor.close();
	}
	
	//从数据库中取出即将播放的有声图书的播放记录，实现断电续播
	public void goOn(){
		Cursor cursor = db.query("pasttime", new String[]{"name,time"}, "name like ?", new String[]{mMusicList.get(currentListItem)}, null, null, null, null);
		
		if(cursor.moveToFirst()){
			int time = cursor.getInt(1);
			mMediaPlayer.seekTo(time);
		}
		cursor.close();
	}
	
	
	//新线程,内部类
	public class DelayThread extends Thread{
		int milliseconds;
		
		public DelayThread(int i){
			milliseconds = i;
			stopSeekBar = false;
		}
		
		public void run(){
			while(!stopSeekBar){
				try{
					sleep(milliseconds);
				} catch(InterruptedException e){
					e.printStackTrace();
				}
				
				handler.sendEmptyMessage(0);
			}
		}
	}
	
	/**
	 * 判断sdcard/audioBook中文本文件是lrc格式的还是txt格式的
	 */
	private void textFormat(){
		String lyricName = mMusicList.get(currentListItem).substring(0, mMusicList.get(currentListItem).lastIndexOf('.')+1) + "lrc";//有声图书文本（lrc格式）同步显示
		String textName = mMusicList.get(currentListItem).substring(0, mMusicList.get(currentListItem).lastIndexOf('.')+1) + "txt";//有声图书文本（txt格式）同步显示
		mark = 0;
		
		File file = new File("/mnt/sdcard/audioBook/txt/"+textName);
		if(file.exists())
			mark = 1;
		else{
			file = new File("/mnt/sdcard/audioBook/lrc/"+lyricName);
			if(file.exists())
				mark = 2;
		}

		
		switch(mark){
		case 1: accumulatedTimeMill = 0;
				totalTimeMill = mMediaPlayer.getDuration();
				prepareTxt(textName, file);
				break;
		case 2: prepareLrc(lyricName);;//有声图书文本（lrc格式）同步显示
				break;
		}
	}
	
	private void stopSync(){
		switch(mark){
		case 1:	try{
			br.close();
		}catch(Exception ex){
			
		}
				handler3.removeCallbacks(textThread);		//从Handler当中移除textThread
				textView.setText("");
				accumulatedTimeMill = 0;
				totalLength = 0;
		case 2: handler2.removeCallbacks(updateTimeCallback);		//从Handler当中移除updateTimeCallback
				textView.setText("");
		}
	}
	
	
	/**
	 * 根据txt格式文件的名字，来读取文档中的信息
	 * @param textName
	 */
	private void prepareTxt(String txtName,File file){
		try {
			FileInputStream inputStream = new FileInputStream("/mnt/sdcard/audioBook/txt/"+txtName);
		    br = new BufferedReader(new InputStreamReader(inputStream));
	        br.mark( ( int )file.length() + 1 );//在首行做个标记
	        while((line = br.readLine()) != null){
	           	totalLength+=line.getBytes().length;
	        }
			br.reset();//从mark的那一行开始读
			textThread = new TextThread(br);
			handler3.post(textThread);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	/**
	 * 根据歌词文件的名字，来读取歌词文件当中的信息
	 * @param lrcName
	 */
	private void prepareLrc(String lrcName){
		try {
			InputStream inputStream = new FileInputStream("/mnt/sdcard/audioBook/lrc/"+lrcName);
			LrcProcessor lrcProcessor = new LrcProcessor();
			queues = lrcProcessor.process(inputStream);
			//创建一个UpdateTimeCallback对象
			updateTimeCallback = new UpdateTimeCallback(queues);
			currentTimeMill = 0 ;
			nextTimeMill = 0 ;
			handler2.postDelayed(updateTimeCallback, 5);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	
	class UpdateTimeCallback implements Runnable{
		Queue times = null;
		Queue messages = null;
		public UpdateTimeCallback(ArrayList<Queue> queues) {
			//从ArrayList当中取出相应的对象对象
			times = queues.get(0);
			messages = queues.get(1);
		}
		

		/**
		 * run（）方法中的while（）循环用于解决用户移动进度条所造成的文本不再同步问题
		 */
		public void run() {
			//计算偏移量，也就是说从开始播放MP3到现在为止，共消耗了多少时间，以毫秒为单位
			long offset = mMediaPlayer.getCurrentPosition();
			if(currentTimeMill == 0){
				nextTimeMill = (Long)times.poll();
				message = (String)messages.poll();
			}
			if(offset > nextTimeMill){
				try{
					previousTimeMill = nextTimeMill;
					nextTimeMill = (Long)times.poll();//取出times队列中的下一个值
					while(offset >=nextTimeMill){//当用户单击进度条上某一靠后时间点时，offset可能会远大于当前nextTimeMill
						message = (String)messages.poll();//从times队列中取出的值得序号总是比从messages队列中取出的值得序号大1
						nextTimeMill = (Long)times.poll();
					}
					textView.setText(message);//当nextTimeMill大于offset时，正好将从messages序列中取出的序列号小1的文本打印出来
					message = (String)messages.poll();
				}catch(NullPointerException ex){//如果当前nextTimeMill的值已经为Queue times中的最后一个值，则try{}中第二行的代码会造成空指针异常
					textView.setText(message);
				}
			}
			
			if(offset <= previousTimeMill){//当用户单击进度条上某一靠前时间点时
				prepareLrc(mMusicList.get(currentListItem).substring(0, mMusicList.get(currentListItem).lastIndexOf('.')+1) + "lrc");
				return;
			}
			currentTimeMill = currentTimeMill + 10;
			handler2.postDelayed(updateTimeCallback, 10);
		}
		
	}
	
	
	class TextThread implements Runnable{
		
		BufferedReader br;
		public TextThread(BufferedReader br){//TextThread类的构造函数
			this.br = br;
		}
		
		public void run() {
		  if(mMediaPlayer.getCurrentPosition()-accumulatedTimeMill>=0){
			try{
				if((line = br.readLine()) != null){
					previousTime = mMediaPlayer.getCurrentPosition();
					lengthPerLine = line.getBytes().length;
					timePerLine = (lengthPerLine/totalLength)*totalTimeMill;
					accumulatedTimeMill += timePerLine;
					if(mMediaPlayer.getCurrentPosition()-accumulatedTimeMill>=0)//当用户点击进度条上一靠后时间点
						textView.setText("");
					else
						textView.setText(line);
				}
			}catch(IOException ex){
				Log.d("debug3", "debug3");
			}
		  }
		  if(mMediaPlayer.getCurrentPosition()<previousTime){
			  stopSync();
			  textFormat();
		  }
		  handler3.postDelayed(textThread,500);
		}
    }

	
	class Rewind implements Runnable{
		public void run(){
			int currentTimePosition = mMediaPlayer.getCurrentPosition();
			if(currentTimePosition > 0){
				currentTimePosition-=5000;
				if(currentTimePosition>=0)
					mMediaPlayer.seekTo(currentTimePosition);
				else
					mMediaPlayer.seekTo(0);
				fhandler.postDelayed(rewind, 500);
			}
		}
	}


	class Forward implements Runnable{
		public void run(){
			int currentTimePosition = mMediaPlayer.getCurrentPosition();
			if(currentTimePosition<mMediaPlayer.getDuration()){
				currentTimePosition+=5000;
				if(currentTimePosition>=mMediaPlayer.getDuration())
					mMediaPlayer.seekTo(0);
				else
					mMediaPlayer.seekTo(currentTimePosition);
				fhandler.postDelayed(forward, 500);
			}
		}
	}
	
}