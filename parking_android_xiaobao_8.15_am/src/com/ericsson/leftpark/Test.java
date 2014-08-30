package com.ericsson.leftpark;

import android.util.Log;

public class Test {
	
	private final String TAG = "Test";
	
	public Test() {
		
	}
	
	public void saveFile() {
		
		FileService fileService = new FileService();
		fileService.saveContentToSdcard("notificationTime.txt", "push_time");
		//Log.i(TAG,"--->>" + flag);
	}
	
	public void readFile() {
		
		FileService fileService = new FileService();
		String msgString = fileService.getFileFromSdcard("notificationTime.txt");
		Log.i(TAG,"--->>" + msgString);
	}
}
