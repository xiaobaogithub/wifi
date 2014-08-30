package com.ericsson.leftpark;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.apache.http.client.methods.HttpGet;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.TextView;

public class HistoryActivity extends Activity implements OnTouchListener, OnGestureListener {
	static final String HISTORY_URL = "http://58.247.178.239:8088/parking/history?start=";
	
	public static final String FILENAME = "history.data";
	private static List<Integer> timeList = new ArrayList<Integer>();
	private static List<String> historyList = new ArrayList<String>();
	private static List<String> curWeekList = new ArrayList<String>();
	
	int currentWeek;
	int weekIndex;
	int dayIndex;
	int dayInWeek;
	private GestureDetector mGestureDetector;
	String start, end;
	String weekEndDate;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history);
		
		fillViewHolder();
		mGestureDetector = new GestureDetector(this);
		ViewHolder.getView().setOnTouchListener(this);
		ViewHolder.getView().setLongClickable(true);
		prepareData();
		ViewHolder.getButtonPark().setOnClickListener(mButtonParkListener);
		//ViewHolder.getButtonSetting().setOnClickListener(mButtonParkListener);
		ViewHolder.getButtonMap().setOnClickListener(mButtonParkListener);
		ViewHolder.getButtonTime().setOnClickListener(mButtonParkListener);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		if(item.getItemId() == R.id.action_exit) {
			ParkingSlotApplication.Instence().onTerminate();
		}
		return super.onOptionsItemSelected(item);
		
	}

	private void getHistoryData(final String start, final String end) {
		new AsyncTask<Void, Void, String>() {

			@Override
			protected String doInBackground(Void... params) {
				// TODO Auto-generated method stub
				//String result = ParkingSlotApplication.Instence().getController().getHistory(start, end);
				HttpGet httpRequest = HttpHelper.getHttpMethod(HISTORY_URL + start + "&end=" + end, HttpGet.class);
				System.out.println(HISTORY_URL + start + "&end=" + end);
				String result = HttpHelper.doHttpRequest(httpRequest);
				if(result != null && result.length()>0) {
					String [] list = result.split(",");
					for(int i=0; i<list.length; i++) {
						historyList.add(list[i]);
					}
	    			writeFile();
				}
				return result;
			}

			@Override
			protected void onPostExecute(String result) {
				// TODO Auto-generated method stub
				super.onPostExecute(result);
				if(result != null) {
					setTimeList();
				}
				else {
					timeList.clear();
					
		    		ViewHolder.getView().invalidate();
		    		ViewHolder.getTextWeek().setText("Week " + currentWeek);
				}
				ViewHolder.getView().caculatePoints(timeList);
			}
			
		}.execute();
	}

	private OnClickListener mButtonParkListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub\
			Intent intent;
			switch(v.getId()) {
			case R.id.buttonPark:	
				intent = new Intent(getApplicationContext(), MainActivity.class);
				startActivity(intent);
				finish();
			break;
			/*
			case R.id.buttonSetting:
				intent = new Intent(getApplicationContext(), SettingActivity.class);
				startActivity(intent);
				finish();
				break;
				*/
			case R.id.buttonMap:
				intent = new Intent(getApplicationContext(), MapsActivity.class);
				startActivity(intent);
				finish();
				break;
			case R.id.buttonTime:
				intent = new Intent(getApplicationContext(), TimeActivity.class);
				startActivity(intent);
				finish();
				break;
			}
		}
		
	};
	
	private void prepareData() {
		String str = readFile(FILENAME);
		historyList.clear();
		currentWeek = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR);
		weekIndex = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR);
		dayIndex = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
		setWeekList();
		if(str == null || str.length() == 0) {
			getHistoryData(curWeekList.get(0), curWeekList.get(curWeekList.size()-1));	
		}
		else {
			String [] list = str.split(",");
			for(int i=0; i<list.length; i++) {
				historyList.add(list[i]);
			}
			setTimeList();
            if(timeList.size() < curWeekList.size()) {
				getHistoryData(curWeekList.get(timeList.size()), curWeekList.get(curWeekList.size()-1));
			}
		}
	}
	
	private void setTimeList() {
		setWeekList();
		boolean bFind = false;
		boolean bHasDate = false;
		timeList.clear();
		for(int i=0; i<curWeekList.size(); i++) {
			bFind = false;
			for(String history : historyList) {
				if(history.substring(0, 8).equals(curWeekList.get(i))) {
					bFind = true;
					bHasDate = true;
				    if(Integer.parseInt(history.substring(8, 10)) > 8) {
				    	timeList.add(60);
				    }
				    else if(Integer.parseInt(history.substring(8, 10)) < 8) {
				    	timeList.add(0);
				    }
				    else {
				    	timeList.add(Integer.parseInt(history.substring(10, 12)));
				    }
				    break;
				}
			}
			if(!bFind) {      
				if(currentWeek == weekIndex && i<(curWeekList.size()-1) ) {
			    	timeList.add(60);
				} else if(currentWeek != weekIndex) {
					timeList.add(60);
				}
			}
//			System.out.println("time : " + timeList.get(timeList.size()-1));
		}
		if(!bHasDate) {
	    	timeList.clear();
		}
//		if(timeList.size() > 0) {
			ViewHolder.getView().caculatePoints(timeList);
    		ViewHolder.getView().invalidate();
    		ViewHolder.getTextWeek().setText("Week " + currentWeek);
    		Calendar c = Calendar.getInstance();
    		c.set(Calendar.DAY_OF_YEAR, dayIndex + 7*(currentWeek-weekIndex+1) - c.get(Calendar.DAY_OF_WEEK)+1);
    		ViewHolder.getTextDate().setText("(" + curWeekList.get(0).subSequence(4, 6) + "." +  curWeekList.get(0).subSequence(6, 8) + " ~ " 
        		    + getCurrentData(c).subSequence(4, 6) + "." +  getCurrentData(c).subSequence(6, 8) + ")");
//		}          
		                                                                                                                                                                                                                                                                                                                                                                   
	}
	
	
	private String getCurrentData(Calendar c) {
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH)+1;
		int date = c.get(Calendar.DATE);
		
		String str = ""+year;
		if(month < 10) {
			str += "0";
		}
		str += month;
		if(date < 10) {
			str += "0";
		}
		str += date;
		
		return str;
	}
	
	private void setWeekList() {
		Calendar.getInstance().setFirstDayOfWeek(Calendar.MONDAY);
		curWeekList.clear();
		dayInWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
		int offDay = (weekIndex-currentWeek)*7 -2;
		int num = 0;
		if(currentWeek == Calendar.getInstance().get(Calendar.WEEK_OF_YEAR)) {
	    	num = Calendar.getInstance().get(Calendar.DAY_OF_WEEK ) -1;
	    	if(num == 0) {
	    		num = 7;
	    	} 
	       	num = num > 5 ? 5:num;
		}
		else {
			num = 5;
		}
		
		Calendar c = Calendar.getInstance();
		for(int i=0; i<num; i++) {
			c.set(Calendar.DAY_OF_YEAR, dayIndex - offDay - dayInWeek + i);
			curWeekList.add(getCurrentData(c).substring(0, 8));
//			System.out.println(getCurrentData(c).substring(0, 8) + " --" + i);
		}
	}
	
	public void writeFile() {
		if(historyList.size() <= 0) {
			return;
		}
		
		Collections.sort(historyList);		
		String str = "";
		int n = 0;
		for(String string : historyList) {
			str += string;
			if(n < historyList.size()-1) {
				str += ",";
			}
		    n++;
		}
		
		System.out.println("write : " + str);
		File file = new File(FILENAME);
		file.delete();
		FileOutputStream fos;
		try {
			fos = openFileOutput(FILENAME, MODE_PRIVATE);
			fos.write(str.getBytes());  
			fos.close(); 
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

	}
	
	public String readFile(String fileName) {
		FileInputStream inputStream;
		try {
			inputStream = openFileInput(FILENAME);
			byte[] b = new byte[inputStream.available()];  
			inputStream.read(b);  
			return new String(b);  
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		return null;
	}
	
	private void playAnimation() {
//		Animation animation = new ScaleAnimation(1f,0.5f,1f,0.5f,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
//		Animation animation = new TranslateAnimation(0,50,0,0);	
		Animation animation = new AlphaAnimation(1f,0.1f);
//		animation.setInterpolator(this,android.R.anim.accelerate_decelerate_interpolator);
		animation.setDuration(1000);
		ViewHolder.getTextWeek().startAnimation(animation);
	}
	
	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		if((e1.getX() - e2.getX()) > 100) {		
			if(currentWeek < weekIndex) {
				currentWeek ++;
				setTimeList();
				if(timeList.size() < curWeekList.size()) {
					getHistoryData(curWeekList.get(timeList.size()), curWeekList.get(curWeekList.size()-1));
				}
			}
			playAnimation();
			return true;
		}
		else if((e2.getX() - e1.getX()) > 100) {		
			if(currentWeek > 1) {
				currentWeek --;
				setTimeList();
				if(timeList.size() < curWeekList.size()) {
					getHistoryData(curWeekList.get(timeList.size()), curWeekList.get(curWeekList.size()-1));
				}
			}
			playAnimation();
			return true;
		}
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		return mGestureDetector.onTouchEvent(event);
	}
	
	private void fillViewHolder() {
		ViewHolder.setButtonPark(findViewById(R.id.buttonPark));
	//	ViewHolder.setButtonSetting(findViewById(R.id.buttonSetting));
		ViewHolder.setButtonMap(findViewById(R.id.buttonMap));
		ViewHolder.setButtonTime(findViewById(R.id.buttonTime));
		ViewHolder.setView(findViewById(R.id.diagrom));
		ViewHolder.setTextWeek(findViewById(R.id.textWeek));
		ViewHolder.setTextDate(findViewById(R.id.textDate));
	}
	
	private static class ViewHolder {
		private static ImageButton buttonPark, buttonSetting,buttonMap,buttonTime;
		private static PSDiagrom view;
		private static TextView textWeek, textDate;

		public static ImageButton getButtonPark() {
			return buttonPark;
		}
		
		public static void setButtonPark(View buttonPark) {
			ViewHolder.buttonPark = (ImageButton)buttonPark;
		}
		
		public static PSDiagrom getView() {
			return view;
		}
		
		public static void setView(View view) {
			ViewHolder.view = (PSDiagrom)view;
		}
		
		public static TextView getTextWeek() {
			return textWeek;
		}
		
		public static void setTextWeek(View textWeek) {
			ViewHolder.textWeek = (TextView)textWeek;
		}
		
		public static TextView getTextDate() {
			return textDate;
		}
		
		public static void setTextDate(View textDate) {
			ViewHolder.textDate = (TextView)textDate;
		}
       /*
		public static ImageButton getButtonSetting() {
			return buttonSetting;
		}

		public static void setButtonSetting(View buttonSetting) {
			ViewHolder.buttonSetting = (ImageButton)buttonSetting;
		}	
		*/
		public static ImageButton getButtonMap() {
			return buttonMap;
		}

		public static void setButtonMap(View buttonMap) {
			ViewHolder.buttonMap = (ImageButton)buttonMap;
		}	
		public static ImageButton getButtonTime() {
			return buttonTime;
		}

		public static void setButtonTime(View buttonTime) {
			ViewHolder.buttonTime = (ImageButton)buttonTime;
		}
		
	}

}
