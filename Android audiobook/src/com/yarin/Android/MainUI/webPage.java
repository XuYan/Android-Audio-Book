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
		
		/*��������WebViewClient,��loadUrl������ҳ������������*/
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
		
		//������ҳ������
		dBook.setWebChromeClient(new WebChromeClient(){
			public void onProgressChanged(WebView view, int progress){
				setTitle("loading...Please wait for a moment" + progress + "%");
				setProgress(progress * 100);
				if (progress == 100) {
					setTitle(R.string.app_name); 
				}
			}
		});
		
		//����quitToBookShelf��ť����Ӧ�����������ҳ�������˻���InitialUI.java����
		quitToBookShelf.setOnClickListener(new Button.OnClickListener(){
			public void onClick(View v)
			{
				Intent intent = new Intent();
				intent.setClass(webPage.this, InitialUI.class);
				startActivity(intent);
				finish();
			}
		});
		
		//����backward��ť����Ӧ��ʹ��ǰ��ҳ��������һҳ
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
		
		//����forward��ť����Ӧ��ʹ��ǰ��ҳǰ������һҳ
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