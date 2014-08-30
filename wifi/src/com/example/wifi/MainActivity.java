package com.example.wifi;

import java.util.List;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Build;

public class MainActivity extends ActionBarActivity {
	
	private TextView allNetWork;  
    private Button scan;
    private EditText interval;
    private EditText times;
    private EditText name;
    private Button open;  
    private Button close;  
    private Button check;  
    private Button stopscan;
    private wifiAdmin mWifiAdmin;
    private int num = 0;//记录对于同一个Ap收集了几次
    private boolean flag = true;
    private String wifiInfo = "";
    private String apResult = "";
    private  Handler handler = new Handler();
	private Runnable runnable = null;
	private int collextInteval = 60000;
	private int collectTimes = 5;
	private String nameInfo = "AP";
    // 扫描结果列表  
    private List<ScanResult> list;  
    private ScanResult mScanResult;  
    private StringBuffer sb=new StringBuffer(); 
    
    //保存扫描的结果
    private FileService fileService = new FileService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mWifiAdmin = new wifiAdmin(MainActivity.this);  
        init();
        
        //定时的定次数的收集某一个AP的wifi的信息
        runnable = new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				
				if(0 == collectTimes) {
					flag = false;
					num++;
					saveWifiInfoToFile();
				}
				
				if((flag == true) && (num < collectTimes)) {
		    		num++;
		    		saveWifiInfoToFile();
		    	}
		    	else if(flag == true) {
		    		stopScan();
		    	}
				
				//启动计时器，每隔collextInteval定时一次
				handler.postDelayed(this, collextInteval);
			}
        };
        
    }
    
    public void init(){
        allNetWork = (TextView) findViewById(R.id.allNetWork);
        open = (Button) findViewById(R.id.open);  
        close = (Button) findViewById(R.id.close);  
        check = (Button) findViewById(R.id.check);
        interval = (EditText) findViewById(R.id.interval); 
        times = (EditText) findViewById(R.id.times);
        name = (EditText) findViewById(R.id.name);
        scan = (Button) findViewById(R.id.scan); 
        stopscan = (Button) findViewById(R.id.stopscan);
        
        scan.setOnClickListener(new MyListener());
        open.setOnClickListener(new MyListener()); 
        close.setOnClickListener(new MyListener());  
        check.setOnClickListener(new MyListener()); 
        stopscan.setOnClickListener(new MyListener());
    }
    
    private class MyListener implements OnClickListener{

    	@Override
    	public void onClick(View v) {
    		// TODO Auto-generated method stub
    		switch (v.getId()) {
    		case R.id.scan://扫描网络
    			collextInteval = Integer.parseInt(interval.getText().toString().trim());
    	        collectTimes = 1000 * Integer.parseInt(times.getText().toString().trim());
    	        nameInfo = name.getText().toString().trim();
    			handler.postDelayed(runnable, 0);	  
    			break;
           case R.id.open://打开Wifi
        		mWifiAdmin.openWifi();
    			Toast.makeText(MainActivity.this, "the current state of wifi is："+mWifiAdmin.checkState(), 1).show();
    			break;
           case R.id.close://关闭Wifi
    			mWifiAdmin.closeWifi();
    			Toast.makeText(MainActivity.this, "the current state of wifi is："+mWifiAdmin.checkState(), 1).show();
    			break;
           case R.id.check://Wifi状态
        	   Toast.makeText(MainActivity.this, "the current state of wifi is："+mWifiAdmin.checkState(), 1).show();
    			break;
           case R.id.stopscan://取消收集某一个AP的wifi信息的定时器
        	   stopScan();
        	   break;
    		default:
    			break;
    		}
    	}
    	
    }

    public String getAllNetWorkList(){
    	  // 每次点击扫描之前清空上一次的扫描结果
		if(sb!=null){
			sb=new StringBuffer();
		}
		//开始扫描网络
		mWifiAdmin.startScan();
		list=mWifiAdmin.getWifiList();
		if(list!=null){
			for(int i=0;i<list.size();i++){
				//得到扫描结果
				mScanResult=list.get(i);
				sb=sb.append("BSSID: "+mScanResult.BSSID+"  ").append("SSID:　"+mScanResult.SSID+"   ")
				.append("capabilities: "+mScanResult.capabilities+"   ").append("frequency: "+mScanResult.frequency+"   ")
				.append("level: "+mScanResult.level+"\n\n");
			}
			//allNetWork.setText("The " + num +"th scanned wifi information as the following：\n"+sb.toString());
			//fileService.saveContentToSdcard("wifiInformation.txt", sb.toString());
		}
    	
    	return sb.toString();
    	
    }
    
    public void saveWifiInfoToFile() {
    	
    	String str = getAllNetWorkList();
    	wifiInfo = "The " + num + "th " + " collection：" + '\n' + str;
    	allNetWork.setText(wifiInfo);
    	apResult = apResult +  wifiInfo + '\n' + '\n';
    	
    }
    
    public void stopScan() {

 	   handler.removeCallbacks(runnable);
 	  
 	   allNetWork.setText("This collection has completed!");
 	   fileService.saveContentToSdcard(nameInfo + ".txt", apResult);
 	   apResult = "";
 	   num = 0;  
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

}
