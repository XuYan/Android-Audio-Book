package com.yarin.Android.MainUI;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LrcProcessor {
	@SuppressWarnings("rawtypes")
	public ArrayList<Queue> process(InputStream inputStream) {
		//存放时间点数据
		Queue<Long> timeMills = new LinkedList<Long>();
		//存放时间点所对应的歌词
		Queue<String> messages = new LinkedList<String>();
		ArrayList<Queue> queues = new ArrayList<Queue>();
		//存放lrc文件中[]内的内容，例如00:03.15
		String timeStr;
		try {
			//创建BufferedReader对象
			InputStreamReader inputReader = new InputStreamReader(inputStream);
			BufferedReader bufferedReader = new BufferedReader(inputReader);
			String temp = null;
			//创建一个正则表达式对象
			Pattern p = Pattern.compile("\\[([^\\]]+)\\]");
			String result = null;
			boolean b = true;
			while ((temp = bufferedReader.readLine()) != null) {
				try{
				Matcher m = p.matcher(temp);
				if (m.find()) {
					if (result != null) {
						messages.add(result);
					}
					timeStr = m.group();
					Long timeMill = time2Long(timeStr.substring(1, timeStr
							.length() - 1));
					if (b) {
						timeMills.offer(timeMill);
					}
					String msg = temp.substring(10);
					result = "" + msg + "\n";
				} else 	//当前行内没有[时间]标签，文本连上一行结尾
					result = result + temp + "\n";
				
				}catch(RuntimeException ex){
					timeStr = "";
				}
			}
			messages.add(result);
			queues.add(timeMills);
			queues.add(messages);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return queues;
	}
	/**
	 * 将分钟，秒全部转换成毫秒
	 * @param timeStr
	 * @return
	 */
	public Long time2Long(String timeStr) {
		String s[] = timeStr.split(":");
		int min = Integer.parseInt(s[0]);
		String ss[] = s[1].split("\\.");
		int sec = Integer.parseInt(ss[0]);
		int mill = Integer.parseInt(ss[1]);
		return min * 60 * 1000 + sec * 1000 + mill * 10L;
	}

}