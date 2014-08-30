package com.ericsson.leftpark;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;

import com.ericsson.leftpark.MapsActivity.spinnerSelectedListener;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class TimeActivity extends Activity{
	
	private Button showtimebutton = null;
	private Button copyButton = null;
	private Spinner spinner_time = null;
	private TextView texttime = null;
	private TextView spinnertext = null;
	private TextView textmode = null;
	
	private  ImageButton mainButton = null;
	private  ImageButton historyButton = null;
	private  ImageButton timegButton = null;
	private  ImageButton mapButton = null;

	private ParkingSlotApplication mode;
	private int modeState;
	private static final int TIME_PICKER_ID = 1;
	
	private ParkingSlotApplication rTime;
	private FileService fileService = new FileService();
	private Calendar calendar = Calendar.getInstance();
	private Date date = null;
	private Date dateCurrent = null;
	private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
	private String currentTime;
	private long currentTimeMill;
	private long settingTimeMill = 0;
	private long relativeTimeMill = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.time_mode);
		
		
		dateCurrent = new Date();
		currentTime = sdf.format(dateCurrent);
		//计算共享的相对时间
		rTime = ((ParkingSlotApplication)getApplicationContext()); 
		rTime.shareRelativeTime = relativeTimeMill;
		
		mode = ((ParkingSlotApplication)getApplicationContext()); 
		modeState = mode.MODE;
		
		showtimebutton = (Button)findViewById(R.id.timebutton);
		showtimebutton.setOnClickListener(new showTimeListener());
		texttime = (TextView)findViewById(R.id.texttime);
		textmode = (TextView)findViewById(R.id.textmode);
		
		spinner_time = (Spinner) findViewById(R.id.spinner_time);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.time_array,android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_time.setAdapter(adapter);
		spinner_time.setOnItemSelectedListener(new spinnerSelectedListener());
		spinnertext = (TextView)findViewById(R.id.spinnertext);
		//spinnertext.setTextColor(Color.BLUE);
		
		mainButton = (ImageButton)findViewById(R.id.buttonMain);
		mainButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					//InitLocation();
					Intent intent = new Intent(getApplicationContext(), MainActivity.class);
					startActivity(intent);
					finish();
				}
			});
		
		historyButton = (ImageButton)findViewById(R.id.buttonHistory2);
		historyButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//InitLocation();
				Intent intent = new Intent(getApplicationContext(), HistoryActivity.class);
				startActivity(intent);
				finish();
			}
		});
		
		timegButton = (ImageButton)findViewById(R.id.buttontimemode);
		timegButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//InitLocation();
				Intent intent = new Intent(getApplicationContext(),TimeActivity.class);
				startActivity(intent);
				finish();
			}
		});
		
		mapButton = (ImageButton)findViewById(R.id.buttonMap2);
		mapButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//InitLocation();
				Intent intent = new Intent(getApplicationContext(),MapsActivity.class);
				startActivity(intent);
				finish();
			}
		});
		
		copyButton = (Button)findViewById(R.id.buttonCopy);
		copyButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//InitLocation();
				Intent intent = new Intent(getApplicationContext(),SettingActivity.class);
				startActivity(intent);
				finish();
			}
		});
	}
	
	//点击按钮，触发监听事件里的showDialog方法，继而调用onCreateDialog方法，然后调用onTimeListener
	public class showTimeListener implements OnClickListener {

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			extracted();
		}

		private void extracted() {
			showDialog(TIME_PICKER_ID);
		}
    	
    }
	

	//通过value进行传递设置的时间
    TimePickerDialog.OnTimeSetListener onTimeListener =new TimePickerDialog.OnTimeSetListener() {

		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			// TODO Auto-generated method stub	
            //String value = calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" +calendar.get(Calendar.DAY_OF_MONTH) + " " +hourOfDay + ":" + minute;
			String value = hourOfDay + ":" + minute;
			texttime.setText("The time you set is:  " + value);
            //Toast.makeText(TimeActivity.this, "The time you set is: "+value, Toast.LENGTH_LONG).show();
			//将设置的时间转化为Date类型,然后转化成毫秒再存入文件notificationTime.txt中
			try {
				date = sdf.parse(value);
				settingTimeMill = date.getTime();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			fileService.saveContentToSdcard("notificationTime.txt", Long.toString(settingTimeMill));
			rTime.setRelativeTime(settingTimeMill);
			
			
			try {
				dateCurrent = sdf.parse(currentTime);
				currentTimeMill = dateCurrent.getTime();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
			relativeTimeMill = Math.abs(settingTimeMill - currentTimeMill);
			rTime.setRelativeTime(relativeTimeMill);
			
System.out.println("TimeActivity:" + relativeTimeMill/1000/60);
		}
 
   };
   
   protected Dialog onCreateDialog(int id) {
	   switch(id) {
	   case TIME_PICKER_ID:
		   return new TimePickerDialog(this,onTimeListener, 12, 00, true);
	   }
	   
	   return null;
   }


	//选择是否开车，通过属性mode进行传递
	public class spinnerSelectedListener implements OnItemSelectedListener {

		@Override
		public void onItemSelected(AdapterView<?> adapterView,  View v, int position,long id) {
			// TODO Auto-generated method stub
			
			// 使用暴力反射让Spinner选择同一选项时触发onItemSelected事件
			try {
				java.lang.reflect.Field field = AdapterView.class.getDeclaredField("mOldSelectedPosition");
				field.setAccessible(true);	//设置mOldSelectedPosition可访问
				field.setInt(spinner_time, AdapterView.INVALID_POSITION); //设置mOldSelectedPosition的值
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(id == 0) {
				textmode.setText(" ");
			}
			else if(id == 1) {
				modeState = 0;
				mode.setMode(modeState);
				textmode.setText("You drive car today.");
//System.out.println(nowDate);
				//Toast.makeText(TimeActivity.this, "You drive car today.", Toast.LENGTH_SHORT).show();
			} else if(id == 2) {
				modeState = 1;
				mode.setMode(modeState);
				textmode.setText("You don't drive car today.");
				//Toast.makeText(TimeActivity.this, "You don't drive car today.", Toast.LENGTH_SHORT).show();
			}		
		}
		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			
		}	
	}		
}
