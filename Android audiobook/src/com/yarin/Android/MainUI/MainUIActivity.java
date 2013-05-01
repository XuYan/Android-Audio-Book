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
	
	public int isPlaying = 0;	//isPlaying������ʶ��ǰ״̬����������ͼ�����ڲ���
	
	GestureDetector detector;
	final int FLIP_DISTANCE = 50;//�������ƶ�������֮�����С����
	
	private LinearLayout linearLayout4 = null;//�����Ž�����������Բ��֣����ڸ��Ľ��汳��
	
	private TextView bookName = null;//��ʾָ��ͼ������
	
	private TextView currentTime = null;//��ǰ����ʱ��
	SeekBar mSeekBar;//������
	private TextView durationTime = null;//������ʱ��
	private boolean stopSeekBar = false;
	
	private ImageButton imageSwitch = null;//��ͼ���ı��л���ť
	private ImageView imageView = null; //��ͼ��ͼ
	private String illustrationsPath = "/mnt/sdcard/audioBook/illustrations/";//imageView����ʾ�Ĳ�ͼ��·��
	private TextView textView = null;	//��Ƶ�ļ�ͬ���ı���ͼ
	private ImageButton imageSwitch2 = null;//�ı�����ͼ�л���ť
	
	private int mark = 0;//��ǩ��mark����1˵��ͬ���ı���txt��ʽ�ģ�mark����2˵��ͬ���ı���lrc��ʽ��
	//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	private Handler handler2 = new Handler();
	private ArrayList<Queue> queues = null;//����Ԫ��Ϊ���е�ArrayList:queues
	private UpdateTimeCallback updateTimeCallback = null;//�߳�
	private long nextTimeMill = 0;//����ͼ��ͬ���ı�ÿ�е���ʼʱ�䣬��λΪms
	private long previousTimeMill = 0;//��¼��һ��nextTimeMill��ʱ�䣬��λΪms(�˱������ڵ��û�����������λ��֮ǰ��ĳһʱ��ʱ)
	private long currentTimeMill = 0;
	private String message = null;//ͬ���ı����ڵ�һ���ı�
	//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	
	private Handler handler3 = new Handler();
	private TextThread textThread = null;//�߳�
	private String line;//txt�ĵ��е�һ������
    private double totalLength = 0;//txt�ĵ��ܹ��ж����ֽ�
    private double lengthPerLine = 0;//txt�ĵ�ÿ���ж����ֽ�
    private double totalTimeMill = 0;//����ͼ�����ʱ�䣬��λΪms
    private double timePerLine = 0;//����ͼ��ÿ�ε�ʱ�䣬��λΪms
    private double accumulatedTimeMill = 0;//�ӿ�ʼ���ŵ��´θ���textView���ۻ�ʱ��
	private BufferedReader br;
	private double previousTime = 0;
	
	/* ����������ť */
	private Handler fhandler = new Handler();//���ڿ������
	private Rewind rewind = new Rewind();
	private Forward forward = new Forward();
	private ImageButton mRewindImageButton = null;
	private ImageButton	mStopImageButton	= null;
	private ImageButton	mStartImageButton	= null;
	private ImageButton	mPauseImageButton	= null;
	private ImageButton mForwardImageButton = null;
	
	private ImageButton	mFrontImageButton	= null;//��һ��
	public ImageButton downButton = null;//������С
	public ImageButton upButton = null;//��������
	public ImageButton muteButton = null;//������ť
	private ImageButton	mNextImageButton	= null;//��һ��
	private Button mSleepButton = null;//��ʱ��ť
	private Handler timingHandler = null;
	private boolean countDownActivation = false;
	
	//��ʱ�رչ��ܶԻ����е�widgets
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
	
	
	/* MediaPlayer���� */
	public MediaPlayer	mMediaPlayer	= null;
	
	/* �����б� */
	private ArrayList<String> mMusicList = new ArrayList<String>();
	
	/* ��ǰ���Ÿ��������� */
	public int currentListItem;
	
	/* ���ֵ�·�� */
	private static final String MUSIC_PATH = new String("/mnt/sdcard/");

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		db = SQLiteDatabase.openOrCreateDatabase("/mnt/sdcard/audioBook/db/pastTime.db3", null);
		/* ����MediaPlayer���� */
		mMediaPlayer	= new MediaPlayer();

		//�����ʼ��
		detector = new GestureDetector(this);//�������Ƽ����
		
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
		downButton = (ImageButton) findViewById(R.id.downButton);//����������ť
		upButton = (ImageButton) findViewById(R.id.upButton);//����������ť
		muteButton = (ImageButton) findViewById(R.id.muteButton);//������ť
		mNextImageButton = (ImageButton) findViewById(R.id.NextImageButton); 
		mSleepButton = (Button)findViewById(R.id.sleep);//��ʱ��������ť
		
		final Builder countDownBuilder = new AlertDialog.Builder(this);
		
		//���ĳ���Ƥ����������
		changeSkin();
		
		//ȡ��bookShelf.java�д�����bookNum����������Ӧ����Ƶ�ļ�
		Intent intent = getIntent();//���������activity֮ǰ��activity��Ӧ��Intent
		Bundle data = intent.getExtras();//��ȡ��intent��Я��������
		currentListItem = data.getInt("bookNum");//��Bundle���ݰ���ȡ������
		mMusicList = data.getStringArrayList("bookList");
		bookName.setText(showCurrentBook(currentListItem,mMusicList));//������idΪbookName��textView����ʾ��ǰ���ŵ���Ƶ�ļ�������
		playMusic(MUSIC_PATH + mMusicList.get(currentListItem));
		addIllustrations(mMusicList.get(currentListItem));//addIllustrations�Ĳ���Ϊ�û���bookShelf�е��������ͼ������֣���չ��Ϊ.mp3
		try{
		goOn();
		}catch(Exception ex){
			db.execSQL(CREATE_TABLE_SQL);//���û��״����г���·��/mnt/sdcard/audioBook/db/pastTime.db3��û�н���pasttime��tableʱ��ִ�д����쳣��׽����
		}

		//������������
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
		
		
		//����������ť������
		VolumeAdjust volumeAdj = new VolumeAdjust(this);//����һ��VolumeAdjust���ʵ��volumeAdj
		downButton.setOnClickListener(volumeAdj.downButton);
		upButton.setOnClickListener(volumeAdj.upButton);
		muteButton.setOnClickListener(volumeAdj.muteButton);
		

		//��ͼ���ı��л���ť
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
		
		//�ı�����ͼ�л���ť
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
		
		//���˰�ť
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
		
		//ֹͣ��ť
		mStopImageButton.setOnClickListener(new ImageButton.OnClickListener() 
		{
			public void onClick(View v)
			{
					//����MediaPlayer����ʼ״̬
					isPlaying = 0;
					mMediaPlayer.reset();
					mSeekBar.setProgress(0);
					currentTime.setText(toTime(mMediaPlayer.getCurrentPosition()));
					stopSync();
			}
		}); 
		
		//��ʼ��ť
		mStartImageButton.setOnClickListener(new ImageButton.OnClickListener() 
		{
			public void onClick(View v)
			{
				playMusic(MUSIC_PATH + mMusicList.get(currentListItem));
			}
		});  
		
		//��ͣ
		mPauseImageButton.setOnClickListener(new ImageButton.OnClickListener() 
		{
			public void onClick(View view)
			{
				if (mMediaPlayer.isPlaying())
				{
					/* ��ͣ */
					mMediaPlayer.pause();
					record();
					if(mark == 2)
						handler2.removeCallbacks(updateTimeCallback);//����ͬ����������ͼ��lrc��ʽ�ı�
					else if(mark == 1)
						handler3.removeCallbacks(textThread);
				}
				else 
				{
					/* ��ʼ���� */
					mMediaPlayer.start();
					if(mark == 2)
						handler2.postDelayed(updateTimeCallback, 5);
					else if(mark == 1)
						handler3.postDelayed(textThread, (long)(accumulatedTimeMill-mMediaPlayer.getCurrentPosition()));
				}
			}
		});
		
		//�����ť
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
		
		//��һ��
		mNextImageButton.setOnClickListener(new ImageButton.OnClickListener() 
		{
			public void onClick(View arg0)
			{
				nextMusic();
			}
		});
		//��һ��
		mFrontImageButton.setOnClickListener(new ImageButton.OnClickListener() 
		{
			public void onClick(View arg0)
			{
				FrontMusic();
			}
		});
		
		//sleep��ť��Ӧ
        mSleepButton.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				CountDownDialog(countDownBuilder);
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
            		System.exit(0);//��ȫ�˳�
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
				countDownActivation = true;//��ǩ:����ֵΪtrueʱ����"��ʱ�ر�"�߳���������
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
	
	//���·��ؼ�
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
			/* ����Ҫ���ŵ��ļ���·�� */
			mMediaPlayer.setDataSource(path);
			/* ׼������ */
			mMediaPlayer.prepare();
			mSeekBar.setMax(mMediaPlayer.getDuration());
			durationTime.setText(toTime(mMediaPlayer.getDuration()));
			textFormat();//��ʼͬ������ͼ���ı�
			/* ��ʼ���� */
			mMediaPlayer.start();
			updateSeekBar();
			mMediaPlayer.setOnCompletionListener(new OnCompletionListener() 
			{
				public void onCompletion(MediaPlayer arg0)
				{
					mMediaPlayer.seekTo(0);//��ǰ����ͼ�鲥����Ϻ�����Զ�����
					//�������һ��֮�������һ��
					nextMusic();
				}
			});
		}catch (IOException e){
			Log.d("test","test");
		}
	}

	/* ����seekBar��currentTime*/
	private void updateSeekBar()
	{
		//����Thread���ڶ���ˢ��SeekBar
		DelayThread dThread = new DelayThread(1000);
		dThread.start();
	}
	
	//������ʵʱ����(ÿ�����)
	private Handler handler = new Handler(){
		public void handleMessage(Message msg){
				currentTime.setText(toTime(mMediaPlayer
						.getCurrentPosition()));
				mSeekBar.setProgress(mMediaPlayer.getCurrentPosition());
		}
	};
	
	/* ��һ�� */
	private void nextMusic()
	{
		record();//����ǰ���ŵ�����ͼ����Ѳ���ʱ���¼�����ݿ���
		
		if (++currentListItem >= mMusicList.size())
		{
			stopSync();
			currentListItem = 0;
			/* ����MediaPlayer */
			mMediaPlayer.reset();
			playMusic(MUSIC_PATH + mMusicList.get(currentListItem));
			addIllustrations(mMusicList.get(currentListItem));
			bookName.setText(showCurrentBook(currentListItem,mMusicList));
			goOn();
		}
		else
		{
			stopSync();
			/* ����MediaPlayer */
			mMediaPlayer.reset();
			playMusic(MUSIC_PATH + mMusicList.get(currentListItem));
			addIllustrations(mMusicList.get(currentListItem));
			bookName.setText(showCurrentBook(currentListItem,mMusicList));
			goOn();
		}
	}
	
	/* ��һ�� */
	private void FrontMusic()
	{
		record();//����ǰ���ŵ�����ͼ����Ѳ���ʱ���¼�����ݿ���
		
		if (--currentListItem < 0)
		{
			stopSync();
			currentListItem = mMusicList.size()-1;
			/* ����MediaPlayer */
			mMediaPlayer.reset();
			playMusic(MUSIC_PATH + mMusicList.get(currentListItem));
			addIllustrations(mMusicList.get(currentListItem));
			bookName.setText(showCurrentBook(currentListItem,mMusicList));
			goOn();
		}
		else
		{
			stopSync();
			/* ����MediaPlayer */
			mMediaPlayer.reset();
			playMusic(MUSIC_PATH + mMusicList.get(currentListItem));
			addIllustrations(mMusicList.get(currentListItem));
			bookName.setText(showCurrentBook(currentListItem,mMusicList));
			goOn();
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
			if(mMediaPlayer.isPlaying())	//�жϵ�ǰ״̬����������ͼ�����ڲ���,�����
				isPlaying = 1;
			countDownActivation = false;//��ǩ:����ֵΪfalseʱ����"��ʱ�ر�"�̼߳���ֹͣ
			Intent intent = new Intent(MainUIActivity.this, bookShelf.class);
			Bundle data = new Bundle();
			data.putInt("isPlaying", isPlaying);
			intent.putExtras(data);
			startActivityForResult(intent,0);
			this.overridePendingTransition(R.anim.zoom_in,R.anim.zoom_out);
			stopSeekBar = true;//ֹͣ���²��Ž��������߳�tThread
			return true;
			}
			return false;
	}
	
	
	/*��д�÷������÷����Իص��ķ�ʽ����ȡָ��Activity���صĽ��*/
	public void onActivityResult(int requestCode,int resultCode,Intent intent){
		if(requestCode==0&&resultCode==0){	//���1:��̨������ͼ�����ڲ���,�û��������е�����ͼ��
			stopSync();
			record();
			mMediaPlayer.reset();
			mMediaPlayer.release();
			mMediaPlayer = new MediaPlayer();
			Bundle data = intent.getExtras();
			currentListItem = data.getInt("bookNum");//��Bundle���ݰ���ȡ������
			mMusicList = data.getStringArrayList("bookList");
			bookName.setText(showCurrentBook(currentListItem,mMusicList));//������idΪbookName��textView����ʾ��ǰ���ŵ���Ƶ�ļ�������
			playMusic(MUSIC_PATH + mMusicList.get(currentListItem));
			addIllustrations(mMusicList.get(currentListItem));
			goOn();
			}
		else if(requestCode==0&&resultCode==1){	//���2:��̨������ͼ�����ڲ���,�û������ܽ��淵��Play Audio Files ��ʼ����
				record();
				stopSync();
				mMediaPlayer.reset();
				mMediaPlayer.release();
		        Intent intent1 = new Intent(MainUIActivity.this, InitialUI.class);
				startActivity(intent1);
				finish();
			}
		else if(requestCode==0&&resultCode==2){	//���3:��̨������ͼ�����ڲ���,�û������ܽ��淵��Play Audio Files��ʼ����
				record();
	        	Intent intent1 = new Intent(MainUIActivity.this, InitialUI.class);
	        	startActivity(intent1);
				finish();
			}
		}
	

	/* ת��ʱ���ʽ�����뵽ʱ���룩 */
	public String toTime(int time){
		time /= 1000;
		int minute = time / 60;
		int hour = minute / 60;
		int second = time % 60;
		return String.format("%02d:%02d:%02d", hour, minute, second);
	}
	
	//��ʾ��ǰ���ڲ��ŵ�����ͼ�������
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
	
	//����ǰ���ŵ�����ͼ������ײ�ͼ���뵽imageView��,��ͼ֧��jpg��png��ʽ
	public void addIllustrations(String bookName){
		String bookNameJpg, bookNamePng;
		bookNameJpg = bookName.substring(0, bookName.lastIndexOf('.')+1) + "jpg";
		bookNamePng = bookName.substring(0, bookName.lastIndexOf('.')+1) + "png";
		String illustrationsPath1 = illustrationsPath + bookNameJpg;
		String illustrationsPath2 = illustrationsPath + bookNamePng;
		//����ļ��Ƿ����
		File f1 = new File(illustrationsPath1);
		File f2 = new File(illustrationsPath2);
		if(f1.exists()){	//���jpg��ʽ��ͼ�Ƿ����
			Bitmap bm = BitmapFactory.decodeFile(illustrationsPath1);
			imageView.setImageBitmap(bm);
		}
		else if(f2.exists()){	//���png��ʽ��ͼ�Ƿ����
			Bitmap bm = BitmapFactory.decodeFile(illustrationsPath2);
			imageView.setImageBitmap(bm);
		}
		else{
			Toast.makeText(this, "No illustrations exist!", Toast.LENGTH_SHORT).show();
			imageView.setImageResource(R.drawable.illustration_background);//����ǰ���ŵ�����ͼ��û��jpg��png��ʽ�Ĳ�ͼ����imageView�еĲ�ͼ�滻�ɴ���ɫ����ͼ
		}

	}
	
	//���ݿ��������pastTime.db3�ļ��в���ÿ������ͼ���Ѿ����ŵ�ʱ��
	public void insertData(SQLiteDatabase db, String bookName, String pastTime)
	{
		db.execSQL("insert into pasttime values(null, ?, ?)", new String[]{bookName, pastTime});
	}
	
	//����ǰ���ŵ�����ͼ��Ĳ���ʱ���¼�����ݿ���
	public void record(){
		Cursor cursor = db.query("pasttime", new String[]{"name,time"}, "name like ?", new String[]{mMusicList.get(currentListItem)}, null, null, null, null);

		if(cursor.moveToFirst()){	//������ݿ����е�ǰ���ڲ��ŵ�����ͼ��ļ�¼��������Ѳ���ʱ��
			ContentValues values = new ContentValues();
			values.put("time", mMediaPlayer.getCurrentPosition());
			db.update("pasttime", values, "name like ?", new String[]{mMusicList.get(currentListItem)});
		}
		else	//���򣬾Ͳ���һ���¼�¼
			insertData(db, mMusicList.get(currentListItem), ""+mMediaPlayer.getCurrentPosition());
		cursor.close();
	}
	
	//�����ݿ���ȡ���������ŵ�����ͼ��Ĳ��ż�¼��ʵ�ֶϵ�����
	public void goOn(){
		Cursor cursor = db.query("pasttime", new String[]{"name,time"}, "name like ?", new String[]{mMusicList.get(currentListItem)}, null, null, null, null);
		
		if(cursor.moveToFirst()){
			int time = cursor.getInt(1);
			mMediaPlayer.seekTo(time);
		}
		cursor.close();
	}
	
	
	//���߳�,�ڲ���
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
	 * �ж�sdcard/audioBook���ı��ļ���lrc��ʽ�Ļ���txt��ʽ��
	 */
	private void textFormat(){
		String lyricName = mMusicList.get(currentListItem).substring(0, mMusicList.get(currentListItem).lastIndexOf('.')+1) + "lrc";//����ͼ���ı���lrc��ʽ��ͬ����ʾ
		String textName = mMusicList.get(currentListItem).substring(0, mMusicList.get(currentListItem).lastIndexOf('.')+1) + "txt";//����ͼ���ı���txt��ʽ��ͬ����ʾ
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
		case 2: prepareLrc(lyricName);;//����ͼ���ı���lrc��ʽ��ͬ����ʾ
				break;
		}
	}
	
	private void stopSync(){
		switch(mark){
		case 1:	try{
			br.close();
		}catch(Exception ex){
			
		}
				handler3.removeCallbacks(textThread);		//��Handler�����Ƴ�textThread
				textView.setText("");
				accumulatedTimeMill = 0;
				totalLength = 0;
		case 2: handler2.removeCallbacks(updateTimeCallback);		//��Handler�����Ƴ�updateTimeCallback
				textView.setText("");
		}
	}
	
	
	/**
	 * ����txt��ʽ�ļ������֣�����ȡ�ĵ��е���Ϣ
	 * @param textName
	 */
	private void prepareTxt(String txtName,File file){
		try {
			FileInputStream inputStream = new FileInputStream("/mnt/sdcard/audioBook/txt/"+txtName);
		    br = new BufferedReader(new InputStreamReader(inputStream));
	        br.mark( ( int )file.length() + 1 );//�������������
	        while((line = br.readLine()) != null){
	           	totalLength+=line.getBytes().length;
	        }
			br.reset();//��mark����һ�п�ʼ��
			textThread = new TextThread(br);
			handler3.post(textThread);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	/**
	 * ���ݸ���ļ������֣�����ȡ����ļ����е���Ϣ
	 * @param lrcName
	 */
	private void prepareLrc(String lrcName){
		try {
			InputStream inputStream = new FileInputStream("/mnt/sdcard/audioBook/lrc/"+lrcName);
			LrcProcessor lrcProcessor = new LrcProcessor();
			queues = lrcProcessor.process(inputStream);
			//����һ��UpdateTimeCallback����
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
			//��ArrayList����ȡ����Ӧ�Ķ������
			times = queues.get(0);
			messages = queues.get(1);
		}
		

		/**
		 * run���������е�while����ѭ�����ڽ���û��ƶ�����������ɵ��ı�����ͬ������
		 */
		public void run() {
			//����ƫ������Ҳ����˵�ӿ�ʼ����MP3������Ϊֹ���������˶���ʱ�䣬�Ժ���Ϊ��λ
			long offset = mMediaPlayer.getCurrentPosition();
			if(currentTimeMill == 0){
				nextTimeMill = (Long)times.poll();
				message = (String)messages.poll();
			}
			if(offset > nextTimeMill){
				try{
					previousTimeMill = nextTimeMill;
					nextTimeMill = (Long)times.poll();//ȡ��times�����е���һ��ֵ
					while(offset >=nextTimeMill){//���û�������������ĳһ����ʱ���ʱ��offset���ܻ�Զ���ڵ�ǰnextTimeMill
						message = (String)messages.poll();//��times������ȡ����ֵ��������Ǳȴ�messages������ȡ����ֵ����Ŵ�1
						nextTimeMill = (Long)times.poll();
					}
					textView.setText(message);//��nextTimeMill����offsetʱ�����ý���messages������ȡ�������к�С1���ı���ӡ����
					message = (String)messages.poll();
				}catch(NullPointerException ex){//�����ǰnextTimeMill��ֵ�Ѿ�ΪQueue times�е����һ��ֵ����try{}�еڶ��еĴ������ɿ�ָ���쳣
					textView.setText(message);
				}
			}
			
			if(offset <= previousTimeMill){//���û�������������ĳһ��ǰʱ���ʱ
				prepareLrc(mMusicList.get(currentListItem).substring(0, mMusicList.get(currentListItem).lastIndexOf('.')+1) + "lrc");
				return;
			}
			currentTimeMill = currentTimeMill + 10;
			handler2.postDelayed(updateTimeCallback, 10);
		}
		
	}
	
	
	class TextThread implements Runnable{
		
		BufferedReader br;
		public TextThread(BufferedReader br){//TextThread��Ĺ��캯��
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
					if(mMediaPlayer.getCurrentPosition()-accumulatedTimeMill>=0)//���û������������һ����ʱ���
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