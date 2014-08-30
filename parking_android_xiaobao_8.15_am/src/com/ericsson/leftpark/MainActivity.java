package com.ericsson.leftpark;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.client.methods.HttpGet;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;

import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

@SuppressLint("HandlerLeak")
public class MainActivity extends Activity {
	static final String GET_URL = "http://58.247.178.239:8088/parking/countdown";
	
	//驶近车辆数
	private ParkingSlotApplication number;
	private int car_numbers;
	//notification
	private static Timer ntimer;
	//通知管理器  
    private NotificationManager nm;    
    //通知显示内容   
    private Notification baseNF; 
   
    //获得用户设置的时间
    //private FileService fileService = null;
	//private String msgString = null;
    //private long settingTime= 0;
    //获得本地时间
    //private Date dateCurrent = null;
	//private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
	//private String currentTime;
	//private long currentTimeMill;
	//获得设置的时间差
	private static Timer rtimer;
	//计算时间差
	private ParkingSlotApplication rmTime; 
	private long relativeTime;
	//private long settingTime;
	//为了在后台运行
	private  Handler handler = new Handler();
	private Runnable runnable = null;
	
	
	private SoundPool soundPool ;
	private static Timer timer;
	private Context context;
	PowerManager powerManager = null; 
	WakeLock wakeLock = null;
	
	private int[] sounds = {R.raw.s0, R.raw.s1, R.raw.s2, R.raw.s3, R.raw.s4, R.raw.s5, R.raw.s6, R.raw.s7, R.raw.s8, R.raw.s9,
			R.raw.s10, R.raw.s100, R.raw.chewei, R.raw.shengyu, R.raw.qin,R.raw.text};
	
	
	static final String PLAY_URL = "http://translate.google.com/translate_tts?ie=UTF-8&oe=UTF-8&tl=zh&q=剩余";
	static final String END_STRING = "个车位";
	static final String PLAY_URL_NO_SPACE = "http://translate.google.com/translate_tts?ie=UTF-8&oe=UTF-8&tl=zh&q=亲明天早点来哦";
	static final int REFRESH_TIMER = 1000 * 10;
	static final int DO_REFRESH = 1;
	static final int SPLASH_MSG = 1001;
	static final int PLAY_MSG = 1002;
	static final int DO_CHECK = 1003;
	
	static int checkCount = 0;
	static int index = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//驶近的车辆数
	   number = ((ParkingSlotApplication)getApplicationContext());
	   car_numbers = number.car_number;  
	   //初始化notificationManager
	   String ns = Context.NOTIFICATION_SERVICE; 
       nm = (NotificationManager) getSystemService(ns); 
       
       /*
       //当地的时间
        dateCurrent = new Date();
		currentTime = sdf.format(dateCurrent);
		try {
			dateCurrent = sdf.parse(currentTime);
			currentTimeMill = dateCurrent.getTime();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		
      
       //共享的时间差
       rmTime = ((ParkingSlotApplication)getApplicationContext()); 
       relativeTime = rmTime.shareRelativeTime;  
       //settingTime = rmTime.shareSettingTime;
   	    
		this.powerManager = (PowerManager)this.getSystemService(Context.POWER_SERVICE);
		this.wakeLock = this.powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "My Lock");
		checkCount = ParkingSlotApplication.Instence().readAnswerCount(this);
		if(checkCount == 2) {
			ShowExitDialog();
			return;
		} else if(checkCount >= 0) {
			check();
		} else {
			init();
		}
	}
	

	private void init() {
		fillViewHoler();
		if(ParkingSlotApplication.Instence().bEnableVolume) {
	    	ViewHolder.getButtonEnableVolume().setVisibility(View.VISIBLE);
	    	ViewHolder.getButtonDisableVolume().setVisibility(View.GONE);
		}
		else {
			ViewHolder.getButtonEnableVolume().setVisibility(View.GONE);
	    	ViewHolder.getButtonDisableVolume().setVisibility(View.VISIBLE);
		}
		context = this;;
		ViewHolder.getButtonHistory().setOnClickListener(mButtonTabListener);
		//ViewHolder.getButtonSetting().setOnClickListener(mButtonTabListener);
		ViewHolder.getButtonMap().setOnClickListener(mButtonTabListener);
		ViewHolder.getButtonTime().setOnClickListener(mButtonTabListener);
		ViewHolder.getButtonDisableVolume().setOnClickListener(mButtonVolumeListener);
		ViewHolder.getButtonEnableVolume().setOnClickListener(mButtonVolumeListener);
		
		//notification
		if(ntimer == null) {
			ntimer = new Timer(true);
		}
		
		//rTimer,rtask放在runnable里面
		if(rtimer == null) {
			rtimer = new Timer(true);
			//rtimer.schedule(rtask, 60000, 86400000);
		}
		
		//利用handler和runnable使得程序处于后台状态时也能收到推送消息
		runnable = new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				/////rtimer.schedule(rtask, 120000, 86400000);
				/////handler.postDelayed(this, 86400000);
			}
		};
		
		//每隔5分钟获取一次剩余车辆数，这里还是需要与驶入的车辆数的那个定时器时间间隔进行协调好
		if(timer == null) {
    		timer = new Timer(true);
	    	/////timer.schedule(task, 0, 300000);
	    	/////handler.postDelayed(runnable, 0);
		}
		soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 100);
		
		if(!ParkingSlotApplication.Instence().isFirst) {
    		reFreshText(false);
		}
		
		if(ParkingSlotApplication.Instence().bUpgrade) {
			AlertDialog.Builder builder = new Builder(MainActivity.this);
			builder.setMessage(R.string.warning_upgrade);
			builder.setTitle(R.string.warning_upgrade_title);
			builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(ParkingSlotApplication.Instence().upgrageUrl));
					MainActivity.this.startActivity(intent);
					ParkingSlotApplication.Instence().onTerminate();
				}	 
			}).setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					ParkingSlotApplication.Instence().bUpgrade = false;
				}
			}).show();
		}
		
//		getParkSlot();
	}
	
	

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub	
		super.onPause();
		this.wakeLock.release();
	}


	@Override
	protected void onResume() {
		// TODO Auto-generated method stub	
		super.onResume();
		this.wakeLock.acquire();
	}



	TimerTask task = new TimerTask(){  
	       public void run() {  
	       Message message = new Message();      
	       message.what = DO_REFRESH;      
	       mHandler.sendMessage(message);
//	       System.out.println("timer");
	    }  
	 };
	 
	 TimerTask rtask = new TimerTask(){  
	       public void run() { 
	    	 relativeTime = rmTime.getRelativeTime();
	    	 /////ntimer.schedule(ntask, relativeTime, 86400000);
System.out.println("rTask:"+relativeTime/1000/60);
	    }  
	 };
	 
	 //notification
	 TimerTask ntask = new TimerTask() {
		 
		@Override
		public void run() {
			// TODO Auto-generated method stub
			
			//car_numbers的数据是在进入mapsActivity界面触发下拉菜单之后从服务器所获得的数据
			car_numbers = number.getNum();
	    	//设置notification的相关变量
			int icon = R.drawable.notify;
			CharSequence tickerText = "Notify!";
			long when  = System.currentTimeMillis();
			baseNF = new Notification(icon,tickerText,when);
			baseNF.defaults |= Notification.DEFAULT_SOUND;
			baseNF.defaults |= Notification.DEFAULT_VIBRATE;
			//long[] vibrate = {0,100,200,300}; 
           // baseNF.vibrate = vibrate;
			baseNF.flags |= Notification.FLAG_AUTO_CANCEL;
			//设置通知的事件消息
			Context context = getApplicationContext(); //上下文
			CharSequence contentTitle = "Notification!"; //通知栏标题
			CharSequence contentText = car_numbers + " free parking space left!!";//通知栏内容
			Intent notificationIntent = new Intent(MainActivity.this,notication.class); //点击该通知后要跳转的Activity
			PendingIntent contentIntent = PendingIntent.getActivity(MainActivity.this,0,notificationIntent,0);
			baseNF.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
			//把Notification传递给NotificationManager
			nm.notify(0,baseNF);
		}
		 
	 };
	 
	
	private OnClickListener mButtonTabListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent;
			switch(v.getId()) {
			case R.id.buttonHistory:
				intent = new Intent(getApplicationContext(), HistoryActivity.class);
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
	
	private OnClickListener mButtonVolumeListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch(v.getId()) {
			case R.id.buttonEnableVolume:
				ViewHolder.getButtonEnableVolume().setVisibility(View.GONE);
				ViewHolder.getButtonDisableVolume().setVisibility(View.VISIBLE);
				ParkingSlotApplication.Instence().bEnableVolume = false;
				break;
			case R.id.buttonDisableVolume:
				ViewHolder.getButtonEnableVolume().setVisibility(View.VISIBLE);
				ViewHolder.getButtonDisableVolume().setVisibility(View.GONE);
				ParkingSlotApplication.Instence().bEnableVolume = true;
				break;
			}
		}
		
	};
	
	
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

	private synchronized void getParkSlot() {
		
		new AsyncTask<Void, Void, String>() {		
			@Override
			protected String doInBackground(Void... params) {
				// TODO Auto-generated method stub			
				HttpGet httpRequest = HttpHelper.getHttpMethod(GET_URL, HttpGet.class);
				String result = HttpHelper.doHttpRequest(httpRequest);
				//int i = (int)(Math.random()*80) + 50;
				//String result = i + "";
				return result;
			}
			
			@Override
			protected void onPostExecute(String result) {
				if(result != null) {
					ParkingSlotApplication.Instence().count = Integer.parseInt(result);
		    		reFreshText(true);
				}
			}		
		}.execute();		
	}
	
	private synchronized void reFreshText(boolean play) {
		
		if(ParkingSlotApplication.Instence().count < 0) {
			setTextColor(Color.RED);
			ViewHolder.getTextView2().setText("N");
			ViewHolder.getTextView1().setText("/");
			ViewHolder.getTextView3().setText("A");	
			ParkingSlotApplication.Instence().count = -1;
			ParkingSlotApplication.Instence().lastCount = -1;
			return;
		}

		int x1 = ParkingSlotApplication.Instence().count/100;
		int x2 = ParkingSlotApplication.Instence().count/10 - x1*10;
		int x3 = ParkingSlotApplication.Instence().count - x1*100 - x2*10;
		
//		Log.i("xxxxx", "x1 = " + x1 + "x2 = " + x2 + "x3 =" + x3);
		
		if(ParkingSlotApplication.Instence().count <= 10) {
			setTextColor(Color.RED);
		}
		else if(ParkingSlotApplication.Instence().count >  10 && ParkingSlotApplication.Instence().count <= 50) {
			setTextColor(Color.YELLOW);
		}
		else {
			setTextColor(Color.GREEN);
		}
		
		if(x1 >= 0 && String.valueOf(x1) != ViewHolder.getTextView2().getText().toString()) {
			showAnimation(ViewHolder.getTextView2());
			ViewHolder.getTextView2().setText(String.valueOf(x1));
		}
		
		if(x2 >= 0 && String.valueOf(x2) != ViewHolder.getTextView1().getText().toString()) {
			showAnimation(ViewHolder.getTextView1());
			ViewHolder.getTextView1().setText(String.valueOf(x2));
		}
		
		if(x3 >= 0 && String.valueOf(x3) != ViewHolder.getTextView3().getText().toString()) {
			showAnimation(ViewHolder.getTextView3());
			ViewHolder.getTextView3().setText(String.valueOf(x3));
		}
		
//		System.out.println(ParkingSlotApplication.Instence().count + " : " + ParkingSlotApplication.Instence().lastCount);
		if(Math.abs(ParkingSlotApplication.Instence().count - ParkingSlotApplication.Instence().lastCount) >= ParkingSlotApplication.Instence().loopCount  || 
				(ParkingSlotApplication.Instence().count < 10 && ParkingSlotApplication.Instence().count != ParkingSlotApplication.Instence().lastCount 
				&& ParkingSlotApplication.Instence().bNotifyUnder10) ) {
			if(play) {
				//car_numbers = number.getNum();
    			playText(x1, x2, x3);
			}
			ParkingSlotApplication.Instence().lastCount = ParkingSlotApplication.Instence().count;
		}
	}
	
	private void showAnimation(View view) {
		Animation animation = new TranslateAnimation(0,0,0,-30);
//		animation = new ScaleAnimation(1f,0.2f,1f,0.2f,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
		animation.setInterpolator(MainActivity.this,android.R.anim.cycle_interpolator);
		animation.setDuration(2000);
		view.startAnimation(animation);
	}
	
	private void setTextColor(int color) {
		ViewHolder.getTextView1().setTextColor(color);
		ViewHolder.getTextView2().setTextColor(color);
		ViewHolder.getTextView3().setTextColor(color);
	}
	
	//car_numbers的数据是在进入mapsActivity界面触发下拉菜单之后从服务器所获得的数据
	private void  playText(int x1, int x2, int x3) {
		car_numbers = number.getNum();
		
		if(ParkingSlotApplication.Instence().bEnableVolume == false ) {
			Log.i("0000", "Disable...");
			return;
		}
		Log.i("111", "Enable...");
		int delayMillis = 0;
		if(ParkingSlotApplication.Instence().count == 0) {
			soundPool.load(this, R.raw.qin, 1);
			soundPool.setOnLoadCompleteListener(spListener);
		}
		else if(ParkingSlotApplication.Instence().count > 0) {
			playSound( R.raw.shengyu, delayMillis);
			delayMillis += 600;
			
			if(x1 > 0) {
				playSound( sounds[x1], delayMillis);
				delayMillis += 500;
				playSound( R.raw.s100, 1000);
				delayMillis += 500;
			}
			if(x2 > 0) {
				playSound( sounds[x2], delayMillis);
				delayMillis += 500;
				playSound( R.raw.s10, delayMillis);
				delayMillis += 500;

			}
			if(x1 > 0 && x2 == 0) {
				playSound( sounds[0], delayMillis);
				delayMillis += 500;
			}
			if(x3 > 0) {
				playSound( sounds[x3], delayMillis);
				delayMillis += 500;
				
			}
			if(ParkingSlotApplication.Instence().count > 10) {
	    		playSound( R.raw.chewei, delayMillis);
	    		delayMillis += 2000;
			}
			
			if(car_numbers > 0) 
			{
			    playSound( sounds[car_numbers], delayMillis);
			    delayMillis += 500;
				playSound( R.raw.text, delayMillis);	
			}
			
		}
//		else {
//			soundPool.load(this, sounds[x3], 1);
//			soundPool.setOnLoadCompleteListener(spListener);
//		}
		
	}
	
	private OnLoadCompleteListener spListener = new OnLoadCompleteListener() {

		@Override
		public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
			// TODO Auto-generated method stub
			float soundVolume =  ((AudioManager) getSystemService(Context.AUDIO_SERVICE)).getStreamVolume(AudioManager.STREAM_SYSTEM);
			soundPool.play(sampleId, soundVolume, soundVolume, 1, 0, 1.0f);
		}
		
	};
	

	private void playSound(final int resId, long delayMillis) {
		new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
            	Message msg = new Message();
            	msg.what = PLAY_MSG;
            	msg.arg1 = resId;
            	mHandler.sendMessage(msg);
//            	soundPool.load(context, resId, 1);
//				soundPool.setOnLoadCompleteListener(spListener);
            }
        }, delayMillis);
	}
	
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case SPLASH_MSG:
				ViewHolder.getSplash().setVisibility(View.GONE);
				ViewHolder.getLinearLayout_main().setVisibility(View.VISIBLE);
				break;
			case PLAY_MSG:
				soundPool.load(context, msg.arg1, 1);
				soundPool.setOnLoadCompleteListener(spListener);
				break;		
			case DO_REFRESH:
				 getParkSlot();
				 break;
			case DO_CHECK:
				check();
				break;
			}		
		}
	};
	
	
	private void check() {
		index = 0;
		new AlertDialog.Builder(this).setTitle(R.string.title_security).setIcon(
		     android.R.drawable.ic_dialog_info).setSingleChoiceItems(
		     new String[] { "respect", "perseverance", "precision", "professionalism"}, 0,
		     new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					index = which;
				}
		    	 
		     }).setNegativeButton("OK", new DialogInterface.OnClickListener() {
			     public void onClick(DialogInterface dialog, int which) {
			    	 if(checkCount == 2) {
			    		 ShowExitDialog();
			    		 return;
			    	 }
			         if(index == 2) {		        	 
			        	 ParkingSlotApplication.Instence().saveAnswerCount(MainActivity.this, -1);
			        	 dialog.dismiss();
			        	 init();
			         } else {	        	
			        	 checkCount ++;
			        	 ParkingSlotApplication.Instence().saveAnswerCount(MainActivity.this, checkCount);	
			        	 ShowAnswerWrongDialog();
			        	 if(checkCount == 2) {
				    		 ShowExitDialog();
				    		 return;
				    	 }
			         }
			     }
		     }).show();
	}
	
	private void ShowExitDialog() {
		new AlertDialog.Builder(this).setTitle(R.string.note_exit).setIcon(
		     android.R.drawable.ic_dialog_info).setNegativeButton("OK", new DialogInterface.OnClickListener() {
			     public void onClick(DialogInterface dialog, int which) {
			    	 ParkingSlotApplication.Instence().onTerminate();
			     }
		     }).show();

	}
	
	private void ShowAnswerWrongDialog() {
		new AlertDialog.Builder(this).setTitle(R.string.note_answer_wrong).setIcon(
			     android.R.drawable.ic_dialog_info).setNegativeButton("OK", new DialogInterface.OnClickListener() {
				     public void onClick(DialogInterface dialog, int which) {
				    	 Message message = new Message();      
			  	         message.what = DO_CHECK;      
			  	         mHandler.sendMessage(message);
				     }
			     }).show();
	}

	private void fillViewHoler() {
		ViewHolder.setTextView1(findViewById(R.id.textView1));
		ViewHolder.setTextView2(findViewById(R.id.textView2));
		ViewHolder.setTextView3(findViewById(R.id.textView3));
		ViewHolder.setButtonHistory(findViewById(R.id.buttonHistory));
	//	ViewHolder.setButtonSetting(findViewById(R.id.buttonSetting));
		ViewHolder.setButtonMap(findViewById(R.id.buttonMap));
		ViewHolder.setButtonTime(findViewById(R.id.buttonTime));
		ViewHolder.setButtonEnableVolume(findViewById(R.id.buttonEnableVolume));
		ViewHolder.setButtonDisableVolume(findViewById(R.id.buttonDisableVolume));
		ViewHolder.setSplash(findViewById(R.id.splash));
		ViewHolder.setLinearLayout_main(findViewById(R.id.linear_layout_main));
		if(!ParkingSlotApplication.Instence().isFirst) {
			ViewHolder.getSplash().setVisibility(View.GONE);
			ViewHolder.getLinearLayout_main().setVisibility(View.VISIBLE);
		}
		else {
			ParkingSlotApplication.Instence().isFirst = false;
			new Handler().postDelayed(new Runnable(){
	            @Override
	            public void run() {
	            	Message msg = new Message();
	            	msg.what = SPLASH_MSG;
	            	mHandler.sendMessage(msg);
	            }
	        }, 1000);

		}
	}
	
	
	private static class ViewHolder {
		private static TextView textView1, textView2, textView3;
		private static ImageButton buttonHistory, buttonSetting;
		private static ImageButton buttonEnableVolume, buttonDisableVolume;
		private static ImageView splash;
		private static ImageButton buttonMap,buttonTime;
		private static RelativeLayout linearLayout_main;

		public static TextView getTextView1() {
			return textView1;
		}
		public static void setTextView1(View textView) {
			ViewHolder.textView1 = (TextView)textView;
		}
		public static TextView getTextView2() {
			return textView2;
		}
		public static void setTextView2(View textView) {
			ViewHolder.textView2 = (TextView)textView;
		}
		public static TextView getTextView3() {
			return textView3;
		}
		public static void setTextView3(View textView) {
			ViewHolder.textView3 = (TextView)textView;
		}
		public static ImageButton getButtonHistory() {
			return buttonHistory;
		}
		public static void setButtonHistory(View buttonHistory) {
			ViewHolder.buttonHistory = (ImageButton)buttonHistory;
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
		public static ImageButton getButtonEnableVolume() {
			return buttonEnableVolume;
		}
		public static void setButtonEnableVolume(View buttonEnableVolume) {
			ViewHolder.buttonEnableVolume = (ImageButton)buttonEnableVolume;
		}
		public static ImageButton getButtonDisableVolume() {
			return buttonDisableVolume;
		}
		public static void setButtonDisableVolume(View buttonDisableVolume) {
			ViewHolder.buttonDisableVolume = (ImageButton)buttonDisableVolume;
		}
		public static ImageView getSplash() {
			return splash;
		}
		public static void setSplash(View splash) {
			ViewHolder.splash = (ImageView)splash;
		}
		public static RelativeLayout getLinearLayout_main() {
			return linearLayout_main;
		}
		public static void setLinearLayout_main(View linearLayout_main) {
			ViewHolder.linearLayout_main = (RelativeLayout)linearLayout_main;
		}
		
	}

}
