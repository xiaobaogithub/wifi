package com.ericsson.leftpark;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.client.methods.HttpGet;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ParseException;
import android.os.Environment;
import android.telephony.TelephonyManager;


public class ParkingSlotApplication extends Application {
	
	private static ParkingSlotApplication instence;
	private ParkingController controller;
	private String deviceId;
	private List<Activity> activities = new ArrayList<Activity>();
	public int count = 0, lastCount = -1;
	public boolean bEnableVolume;
	public boolean isFirst = true;
	public int loopCount = 5;
	public boolean bNotifyUnder10;
	public boolean bUpgrade;
	public String upgrageUrl;
	
	//获得本地时间
    private Date dateCurrent = null;
	private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
	private String currentTime;
	public long currentTimeMill;
	
	//notification
	public int car_number = 0;
    public int MODE = 0;
    public long shareRelativeTime;
    public FileService fileService = null;
    public long shareSettingTime = 0;
    private String stri = "";
	private String line = "";
	
	public static final String FILENAME = "history.data";
	public static final String ANSWER_COUNT_KEY = "ANSWER_COUNT";
	public static final String VERSION_KEY = "VERSION_KEY";
	public static final String UPGRADE_URL = "http://58.247.178.239:8088/manager/upgrade.text";
	
	public synchronized static ParkingSlotApplication Instence() {
		if(instence == null) {
			instence = new ParkingSlotApplication();
		}
		return instence;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		
		
		//为了保存relativeTime的初始值即为文件中存的settingTime与现取的currentTime的差值
		try {
			File urlFile = new File(Environment.getExternalStorageDirectory()+"/" + "notificationTime.txt");
			if(urlFile.exists()) {
				InputStreamReader isr = new InputStreamReader(new FileInputStream(urlFile),"UTF-8");
				BufferedReader br = new BufferedReader(isr);
				while((line = br.readLine()) != null) {
					stri = stri + line;
				}
			}
			else {
				stri = "14400000";
			}
			
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	
		dateCurrent = new Date();
		currentTime = sdf.format(dateCurrent);
		try {
			dateCurrent = sdf.parse(currentTime);
			currentTimeMill = dateCurrent.getTime();
		} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		 }
		shareRelativeTime = Long.parseLong(stri) - currentTimeMill;
//System.out.println("shareRelativeTime" + "--->application" + shareRelativeTime/1000/60);
	
		
		instence = this;
		instence.bEnableVolume = true;
		bNotifyUnder10 = true;
		createController();
		checkParking();
	}
	
	private void createController() {
		if(controller == null) {
			controller = new ParkingController();
		}
	}
	
	public ParkingController getController() {
		return controller;
	}
	
	public String getDeviceId() {
		if(deviceId == null) {
			TelephonyManager tm = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
			deviceId = tm.getDeviceId();
		    deviceId = MD5(deviceId);
		}	
		return deviceId;
	}
	
	public String MD5(String string) {
		if(string == null) {
			return null;
		}
		MessageDigest digester;
		StringBuffer sb = new StringBuffer();
		try {
			digester = MessageDigest.getInstance("MD5");
			digester.update(string.getBytes());  
			byte[] digest = digester.digest();
			for(int i=0; i<digest.length; i++) {
				int val = ((int)digest[i]) & 0xff;
				if(val < 16) {
					sb.append("0");
				}
				sb.append(Integer.toHexString(val));
			}
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}    
		return sb.toString();
	}
	
	public void addActivity(Activity activity) {  
		activities.add(activity);
	}

	@Override
	public void onTerminate() {
		// TODO Auto-generated method stub
		super.onTerminate();
		for (Activity activity : activities) {
			activity.finish();
		}
		
		System.exit(0);
	}
	
	public int readAnswerCount(Context context) {
		SharedPreferences sp = context.getSharedPreferences("Parking", MODE_PRIVATE);
        return sp.getInt(ANSWER_COUNT_KEY, 0);
	}
	
	public void saveAnswerCount(Context context, int count) {
		SharedPreferences sp = context.getSharedPreferences("Parking", MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putInt(ANSWER_COUNT_KEY, count);
		editor.commit();
	}
	
	public void checkParking() {
		String verStr = getString(R.string.version);
		verStr = verStr.substring(8);
		System.out.println("ver : " + verStr);
		HttpGet httpRequest = HttpHelper.getHttpMethod(UPGRADE_URL, HttpGet.class);
		String result = HttpHelper.doHttpRequest(httpRequest);
		if(result != null) {
			System.out.println("server ver : " + result.substring(0,6));
			if(result.substring(0,6).equals(verStr)) {
				bUpgrade = false;
				SharedPreferences sp = getApplicationContext().getSharedPreferences("Parking", MODE_PRIVATE);
				String oldVer = sp.getString(VERSION_KEY, "0.9");
				
				if(!oldVer.equals(verStr)) {
					System.out.println("clear history");
					File file = new File(FILENAME);
					file.delete();
					Editor editor = sp.edit();
					editor.putString(VERSION_KEY, verStr);
					editor.commit();
				}
			} else {
				bUpgrade = true;
				upgrageUrl = result.substring(7);
				System.out.println("upgrageUrl = " + upgrageUrl);
			}
		}
		
	}
	
	//the number of driving cars
	public int  getNum() {
		return car_number;
	}
	
	public void setNum(int i) {
		car_number = i;
	}
	
	//whether drive car---MODE
	public int  getMode() {
		return MODE;
	}
	
	public void setMode(int i) {
		MODE = i;
	}
	
	//time to send the notification
	public long  getRelativeTime() {
		return shareRelativeTime;
	}
	
	public void setRelativeTime(long time) {
		shareRelativeTime = time;
	}
	
	public long  getSettingTime() {
		return shareSettingTime;
	}
	
	public void setSettingTime(long time) {
		shareSettingTime = time;
	}
	
}