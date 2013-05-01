package com.yarin.Android.MainUI;

import java.io.File;
import java.io.FilenameFilter;

/* 过滤文件类型 */
class MusicFilter implements FilenameFilter
{
	public boolean accept(File dir, String name)
	{
		//这里还可以设置其他格式的音乐文件
		return (name.endsWith(".wav")|name.endsWith(".m4a")|name.endsWith(".ogg")|name.endsWith(".mp3"));
	}

}