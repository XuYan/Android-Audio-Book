package com.yarin.Android.MainUI;

import java.io.File;
import java.io.FilenameFilter;

/* �����ļ����� */
class MusicFilter implements FilenameFilter
{
	public boolean accept(File dir, String name)
	{
		//���ﻹ��������������ʽ�������ļ�
		return (name.endsWith(".wav")|name.endsWith(".m4a")|name.endsWith(".ogg")|name.endsWith(".mp3"));
	}

}