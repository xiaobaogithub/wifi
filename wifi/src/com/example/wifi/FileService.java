//file management
package com.example.wifi;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import android.content.Context;
import android.os.Environment;

public class FileService {
	
	private Context context;
	
	public FileService(Context context) {
		this.context = context;
	}
	
	public FileService() {
		
	}
	
	public void saveContentToSdcard(String fileName, String content) {
		//boolean flag = false;
		FileOutputStream fileOutputStream = null;
		//或得sdcard卡所在的路徑
		File file  = new File(Environment.getExternalStorageDirectory(),fileName);
		//判断sdcard卡是否可用
		if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			try {
				fileOutputStream = new FileOutputStream(file);
				fileOutputStream.write(content.getBytes()); //将当前字符换转换为字符数组
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if(fileOutputStream != null) {
					try {
						fileOutputStream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		
		//return flag;
	}
	
	public String getContent(String fileName) {
		
		String str = "";
		String line = "";
		File urlFile = new File(Environment.getExternalStorageDirectory(),fileName);
		
		try {
			InputStreamReader isr = new InputStreamReader(new FileInputStream(urlFile),"UTF-8");
			BufferedReader br = new BufferedReader(isr);
			while((line = br.readLine()) != null) {
				str = str + line;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		return str;
	}
	
	
	public String getFileFromSdcard(String fileName) {
		FileInputStream fileInputStream = null;
		//缓冲区缓存，和磁盘无关，勿需要关闭
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		File file = new File(Environment.getExternalStorageDirectory(),fileName);
		if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			try {
				fileInputStream = new FileInputStream(file);
				int len = 0;
				byte[] data = new byte[1024];
				//读到字节缓冲数组当中
				while((len = fileInputStream.read(data)) != -1) {
					outputStream.write(data,0,len);
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if(fileInputStream != null) {
					try {
						fileInputStream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		
		return new String(outputStream.toByteArray());
	}

}


