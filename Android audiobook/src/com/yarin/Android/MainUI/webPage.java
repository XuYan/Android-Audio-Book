package com.yarin.Android.MainUI;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

public class webPage extends Activity {
	
	private WebView dBook = null;
	private Button quitToBookShelf = null;
	private Button webBackward = null;
	private Button webForward = null;
	private String strURL = "http://www.baidu.com";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
		setContentView(R.layout.webpage);
		
		dBook = (WebView)findViewById(R.id.dBook);
		quitToBookShelf = (Button)findViewById(R.id.back_to_book_shelf);
		webBackward = (Button)findViewById(R.id.webbackward);
		webForward = (Button)findViewById(R.id.webforward);
		
		
		dBook.getSettings().setJavaScriptEnabled(true);
		
		dBook.loadUrl(strURL);
		Toast.makeText(webPage.this, getString(R.string.load)+strURL,Toast.LENGTH_LONG).show();
		
		/*若无设置WebViewClient,以loadUrl加载网页会打开内置浏览器*/
		dBook.setWebViewClient(new WebViewClient(){
			public boolean shouldOverrideUrlLoading(WebView view, String url){
				view.loadUrl(url);
				return true;
			}

			public void onPageFinished(WebView view, String url){
				if(view.canGoBack())
					webBackward.setEnabled(true);
				else
					webBackward.setEnabled(false);
				if(view.canGoForward())
					webForward.setEnabled(true);
				else
					webForward.setEnabled(false);
				
			}
		});
		
		dBook.setDownloadListener(new MyWebViewDownLoadListener());
		
		//加载网页进度条
		dBook.setWebChromeClient(new WebChromeClient(){
			public void onProgressChanged(WebView view, int progress){
				setTitle("loading...Please wait for a moment" + progress + "%");
				setProgress(progress * 100);
				if (progress == 100) {
					setTitle(R.string.app_name); 
				}
			}
		});
		
		//设置quitToBookShelf按钮的响应，结束浏览网页，程序退回至InitialUI.java界面
		quitToBookShelf.setOnClickListener(new Button.OnClickListener(){
			public void onClick(View v)
			{
				Intent intent = new Intent();
				intent.setClass(webPage.this, InitialUI.class);
				startActivity(intent);
				finish();
			}
		});
		
		//设置backward按钮的响应，使当前网页后退至上一页
		webBackward.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(dBook.canGoBack()){
					dBook.goBack();
					//webForward.setEnabled(true);
				}
				/*if(!dBook.canGoBack())
					webBackward.setEnabled(false);*/
			}
		});
		
		//设置forward按钮的响应，使当前网页前进至后一页
		webForward.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(dBook.canGoForward()){
					dBook.goForward();
					//webBackward.setEnabled(true);
				}
				/*if(!dBook.canGoForward())
					webForward.setEnabled(false);*/
			}
		});
	}

    
    class MyWebViewDownLoadListener implements DownloadListener{
    	public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
    		try {
    			HttpDownloader httpDownloader = new HttpDownloader();
    			httpDownloader.downloadFile(url, webPage.this);
    		} catch (Exception e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    	}
    }
    }