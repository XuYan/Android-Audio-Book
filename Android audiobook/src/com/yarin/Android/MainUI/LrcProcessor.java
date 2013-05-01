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
		//���ʱ�������
		Queue<Long> timeMills = new LinkedList<Long>();
		//���ʱ�������Ӧ�ĸ��
		Queue<String> messages = new LinkedList<String>();
		ArrayList<Queue> queues = new ArrayList<Queue>();
		//���lrc�ļ���[]�ڵ����ݣ�����00:03.15
		String timeStr;
		try {
			//����BufferedReader����
			InputStreamReader inputReader = new InputStreamReader(inputStream);
			BufferedReader bufferedReader = new BufferedReader(inputReader);
			String temp = null;
			//����һ��������ʽ����
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
				} else 	//��ǰ����û��[ʱ��]��ǩ���ı�����һ�н�β
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
	 * �����ӣ���ȫ��ת���ɺ���
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