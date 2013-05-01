package com.yarin.Android.MainUI;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class bookShelfAdapter extends BaseAdapter{
	private Context context;
	private LayoutInflater bookContainer;
	private ArrayList<String> bookArrayList;
	//private List<Map<String, Object>> bookList;
	
	private boolean Clickable = false;
	private int prePosition;
	
	Toast toast;//检测到未插入SDcard时显示toast告知用户
	
	ViewHolder holder = null;
	
/*	public bookShelfAdapter(Context context, ArrayList<String> arrayList, List<Map<String,Object>> list){
		this.bookContainer = LayoutInflater.from(context);
		this.bookArrayList = arrayList;
		this.bookList = list;
	}*/
	
	public bookShelfAdapter(Context context, ArrayList<String> arrayList){
		this.context = context;
		this.bookContainer = LayoutInflater.from(context);
		this.bookArrayList = arrayList;
	}
	
	
	static class ViewHolder
	{
		public TextView bookName;
		public Button deleteItem;
	}
	

	public int getCount(){
		return  bookArrayList.size();
	}
	

	public Object getItem(int position){
		return bookArrayList.get(position);
	}
	

	public long getItemId(int position){
		return position;
	}
	

	public View getView(final int position, View convertView, final ViewGroup parent){
		
		if(convertView == null){
			holder=new ViewHolder();
			
			convertView = bookContainer.inflate(R.layout.bookshelfitem, null);
			holder.bookName = (TextView)convertView.findViewById(R.id.bookName);
			holder.deleteItem = (Button)convertView.findViewById(R.id.deleteItem);

			convertView.setTag(holder);
		}
		else{
			holder = (ViewHolder)convertView.getTag();
		}

		
		//holder.bookName.setText((String)bookList.get(position).get("bookName"));
		holder.bookName.setText(bookArrayList.get(position));
		
		holder.bookName.setOnLongClickListener(new View.OnLongClickListener() {			

			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				if(Clickable)
					parent.getChildAt(prePosition).findViewById(R.id.deleteItem).setVisibility(View.INVISIBLE);
				Clickable = true;
				parent.getChildAt(position).findViewById(R.id.deleteItem).setVisibility(View.VISIBLE);
				prePosition = position;
				return true;
			}
		});
		
		holder.deleteItem.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				Clickable = false;
				parent.getChildAt(position).findViewById(R.id.deleteItem).setVisibility(View.INVISIBLE);
				deleteInSDCard(context,bookArrayList.get(position));
				bookArrayList.remove(position);
				notifyDataSetChanged();
			}
		});
		
		return convertView;
	}
	
	//在SD卡中删除指定文件
	public void deleteInSDCard(Context context, String fileName){
		String filePath;
		//判断插入了SD card
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			filePath = Environment.getExternalStorageDirectory().getPath()+java.io.File.separator+fileName;
			File file = new File(filePath);
			if(file.exists())
				file.delete();
			else
				toast = Toast.makeText(context, "the audio book "+fileName+" has not been found in SD card", Toast.LENGTH_LONG);
		}

	}
}