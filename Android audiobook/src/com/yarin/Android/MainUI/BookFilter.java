package com.yarin.Android.MainUI;

import java.io.File;
import java.io.FilenameFilter;

/* �����ļ����� */
public class BookFilter implements FilenameFilter{
	public boolean accept(File dir, String name)
	{
		//���ﻹ��������������ʽ�������ļ�
		return (name.endsWith(".txt"));
	}
}
