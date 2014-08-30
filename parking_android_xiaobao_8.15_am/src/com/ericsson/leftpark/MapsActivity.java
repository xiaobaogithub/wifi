package com.ericsson.leftpark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;










import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.MyLocationConfigeration.LocationMode;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class MapsActivity extends Activity implements OnGetGeoCoderResultListener {
	
  
    //驶入的车辆数
	private ParkingSlotApplication number; 
	public int car_numbers = 0;
	
	//按钮&下拉菜单
	private  ImageButton mainButton = null;
	private  ImageButton historyButton = null;
	private  ImageButton timeButton = null;
	private Spinner spinner = null;
	private  Handler handler = new Handler();
	private Runnable runnable = null;
	
	//地图&定位
	private GeoCoder mSearch = null; 
	private BaiduMap mBaiduMap = null;
	private MapView mMapView = null;
	private double mCurrentLantitude = 0;
	private double mCurrentLongitude = 0;
    public LocationClient mLocationClient;  
    public MyLocationListener mMyLocationListener;   
    private volatile boolean isFristLocation = true; 
    
    //从服务器端获取数据
    private int id_number = 0;
    private int id_count;
	private String longitudeClient = null;
	private String latitudeClient = null;
	private int modeState = 0;
	private String currentSTime = Long.toString(System.currentTimeMillis());
	private List<String> usersPositionList = new ArrayList<String>();
	private List<LatLng> userPositionList = new ArrayList<LatLng>();
	private HttpResponse response;
	private HttpGet httpGet;
    private HttpClient httpClient;
    private HttpEntity httpEntity;
    private String result = "";
    private ScanResult mScanResult; 
    private wifiAdmin mWifiAdmin;  
    private List<ScanResult> listScanResult;
    private boolean wifiFlag1 = false;
    private ParkingSlotApplication mode; 
	public static String baseURL = "http://58.247.178.243:8080/parking/location/"; 
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        SDKInitializer.initialize(getApplicationContext());
       //setContentView(R.layout.spinner);
       
        setContentView(R.layout.acativity_map);
        CharSequence titleLable = "地理编码功能";
		setTitle(titleLable);
		
		number = ((ParkingSlotApplication)getApplicationContext()); 
		number.car_number = car_numbers;
		
		mode = ((ParkingSlotApplication)getApplicationContext()); 
		modeState = mode.MODE;
		
        //下拉菜单实现场景的选择==========================================spinner2
        spinner = (Spinner) findViewById(R.id.spinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.driving_points,android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(new spinnerSelectedListener());

		// 地图初始化
		mMapView = (MapView) findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap(); 
        LatLng cenpt = new LatLng(31.237059,121.481);
        MapStatus mMapStatus = new MapStatus.Builder().target(cenpt).zoom(14).build();
        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
        mBaiduMap.setMapStatus(mMapStatusUpdate);
		// 初始化搜索模块，注册事件监听
		mSearch = GeoCoder.newInstance();
		mSearch.setOnGetGeoCodeResultListener(this);
		//定位初始化
		initMyLocation();
		
		// 开启定位图层  
		//mBaiduMap.setMyLocationEnabled(true);
		
		
		//按钮初始化及其触发事件
		//返回主界面MainActivity
		mainButton = (ImageButton)findViewById(R.id.buttonMain);
		mainButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					//InitLocation();
					Intent intent = new Intent(getApplicationContext(), MainActivity.class);
					startActivity(intent);
					finish();
					//handler.removeCallbacks(runnable);
				}
			});
		//返回HistoryActivity界面
		historyButton = (ImageButton)findViewById(R.id.buttonHistory1);
		historyButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//InitLocation();
				Intent intent = new Intent(getApplicationContext(), HistoryActivity.class);
				startActivity(intent);
				finish();
				//handler.removeCallbacks(runnable); 
			}
		});
		//返回设置界面，主要是推送时间的设置
		timeButton = (ImageButton)findViewById(R.id.buttonTime1);
		timeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//InitLocation();
				Intent intent = new Intent(getApplicationContext(),TimeActivity.class);
				startActivity(intent);
				finish();
				//handler.removeCallbacks(runnable); 
			}
		});

	
		/*
		 //定位功能
		locationButton =  (ImageButton)findViewById(R.id.buttonLocation);
		locationButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					//InitLocation();
					MyLocation();
					mLocationClient.start();
					LatLng iLatLng = new LatLng(mCurrentLantitude, mCurrentLongitude);  
					MapStatus mMapStatus = new MapStatus.Builder().target(iLatLng).zoom(16).build();
				    MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(iLatLng);  
				    mBaiduMap.animateMapStatus(u);  
				    //startLocation.setVisibility(startLocation.GONE);
				}
			});
		*/
		
		//为了在地图上显示驶入的车辆数
		runnable = new Runnable() {
			

			@Override
			public void run() {
				// TODO Auto-generated method stub
				
				/*注释1
				//判断是否在公司附近
				boolean flagNear = getAllNetWorkList();
				if(flagNear == true) {
					longitudeClient = "0";
					longitudeClient = "0";
				}
				
				//启动定位以获得当前位置的经纬度
			     mLocationClient.start();	
				//获得modeState，即用户是否开车
				modeState = mode.getMode();
				*/
System.out.println("enter into the runnable");
				//解析从服务器或得的数据
				// 使用GET方法发送请求,需要把参数加在URL后面，用？连接，参数之间用&分隔
				longitudeClient = Double.toString(mCurrentLongitude);
				latitudeClient = Double.toString(mCurrentLantitude);
				currentSTime = Long.toString(System.currentTimeMillis());
System.out.println("&longitude=" + longitudeClient + "&latitude=" + latitudeClient);   
		        String url = baseURL + "?id=" + id_number + "&longitude=" + longitudeClient + "&latitude=" + latitudeClient + "&mode=" + modeState + "&time=" + currentSTime;
		        // 生成请求对象
		        httpGet = new HttpGet(url);
		        httpClient = new DefaultHttpClient();
		        try {
					response = httpClient.execute(httpGet);
				} catch (ClientProtocolException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
		        //这里之后要考虑如果没有获得数据该做什么处理，先输出获取数据失败的提示吧
		        if (null == response) {
		            Toast.makeText(MapsActivity.this, "failure!", Toast.LENGTH_LONG).show();
		        }
		        //获得响应结果
		        httpEntity = response.getEntity();
		        InputStream inputStream;
				try {
					inputStream = httpEntity.getContent();
					BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			        String line = "";
			        while (null != (line = reader.readLine())) { 
			        	result += line;
			        }
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//判断用户是否是第一次发送http请求
				if (id_count == 0) {
					id_count = 1;
					id_number = Integer.parseInt(result);
				}
System.out.println("result= " + result);

				//解析响应结果--->解析成每个用户的数据
			
				if(result != null && result.length()>0) {
					String [] usersList = result.split(".");
					for(int i = 0; i < usersList.length; i++) {
						usersPositionList.add(usersList[i]);
					}
				}
System.out.println("usersPositionList"+ usersPositionList.size());
				//解析响应结果--->分别解析每个用户的数据
				for(int j = 0; j < usersPositionList.size(); j++) {
					String [] userList = usersPositionList.get(j).split(",");
					modeState = Integer.parseInt(userList[3]);
					//这里通过modeState判断用户是否开车，通过flagNear判断用户是否已经在公司附近
					if(modeState == 0) {
						longitudeClient = userList[1];
						latitudeClient = userList[2];
						if((Long.parseLong(latitudeClient) != 0.0) && (Long.parseLong(longitudeClient)!= 0.0) ) {
							LatLng point = new LatLng(Long.parseLong(latitudeClient),Long.parseLong(longitudeClient));
							userPositionList.add(point);
						}
					}
				}
				
				//设置在图上显示的点数即驶入所设定范围内的车辆数
				car_numbers = userPositionList.size();
				number.setNum(car_numbers);
System.out.println("car_numbers"+ car_numbers);				
				//根据经纬度显示爱立信周边地图且显示一定范围驶入的车辆
				LatLng iLatLng = new LatLng(31.225199,121.36149);  
				mSearch.reverseGeoCode(new ReverseGeoCodeOption().location(iLatLng));
System.out.println("map show!!!!!!!!!");
				//或者这样，这种方式指定的经纬度对应的点不会显示出来
				//MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(iLatLng);  
	            //mBaiduMap.animateMapStatus(u);
				//markerShow();
                result = "";
 			   //启动计时器，每隔2分钟定时一次
			   handler.postDelayed(this, 120000);
System.out.println("hand setting!!!!!!!!!");
			}
		};

    }
    

    //判断用户是否已经在公司附近
    public boolean getAllNetWorkList(){
    	
		//开始扫描网络
		mWifiAdmin.startScan();
		listScanResult = mWifiAdmin.getWifiList();
		if(listScanResult != null){
			for(int n = 0; n < listScanResult.size(); n++){
				//得到扫描结果
				mScanResult = listScanResult.get(n);
				if((mScanResult.SSID == "EWA@GUEST") || (mScanResult.SSID == "EWA@ECN")) {
					wifiFlag1 = true;
					return wifiFlag1;
				}
			}
		}
		
		return wifiFlag1;
    }
    

    public class spinnerSelectedListener implements OnItemSelectedListener {

		@Override
		public void onItemSelected(AdapterView<?> adapterView,  View v, int position,long id) {
			// TODO Auto-generated method stub
			
			// 使用暴力反射让Spinner选择同一选项时触发onItemSelected事件
			try {
				java.lang.reflect.Field field = AdapterView.class.getDeclaredField("mOldSelectedPosition");
				field.setAccessible(true);	//设置mOldSelectedPosition可访问
				field.setInt(spinner, AdapterView.INVALID_POSITION); //设置mOldSelectedPosition的值
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(id == 0) {
				mBaiduMap.clear();
				//根据具体地址显示地图
				mSearch.geocode(new GeoCodeOption().city("上海市").address("长宁区天山西路1068号联强国际广场"));
                
			}
			if(id == 1) {
				mBaiduMap.clear();
				//LatLng iLatLng = new LatLng(mCurrentLantitude,mCurrentLongitude);
				//根据经纬度显示方式一，这种方式会把经纬度对应的点显示出来
				//mSearch.reverseGeoCode(new ReverseGeoCodeOption().location(iLatLng));
				//根据经纬度显示方式二，这种方式不会把经纬度对应的点显示出来
                //MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(iLatLng);  
                //mBaiduMap.animateMapStatus(u);
			    handler.postDelayed(runnable, 60000);	
            }
		}
    
  /*
    //利用下拉菜单实现不同场景的选择-----DemoShow的时候是这样做的
   
    public class spinnerSelectedListener implements OnItemSelectedListener {

		@Override
		public void onItemSelected(AdapterView<?> adapterView,  View v, int position,long id) {
			// TODO Auto-generated method stub
			
			// 使用暴力反射让Spinner选择同一选项时触发onItemSelected事件
			try {
				java.lang.reflect.Field field = AdapterView.class.getDeclaredField("mOldSelectedPosition");
				field.setAccessible(true);	//设置mOldSelectedPosition可访问
				field.setInt(spinner, AdapterView.INVALID_POSITION); //设置mOldSelectedPosition的值
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(id == 0) {
				//handler.removeCallbacks(runnable);
				handler.postDelayed(runnable, 0);
				
				handler2.removeCallbacks(runnable2);
				
			}
			else if(id == 1) {
				mBaiduMap.clear();
				LatLng cenpt1 = new LatLng(31.225199,121.36149);
		        MapStatus mMapStatus = new MapStatus.Builder().target(cenpt1).zoom(15).build();
		        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
		        mBaiduMap.setMapStatus(mMapStatusUpdate);
			    
			    int no_cars1 = (int)(Math.random()*7); 
			    //int no_cars2 = (int)(Math.random()*3); 
			    
				 if(no_cars1 < 3) {
					 no_cars1 = no_cars1 + 2;
				 }
				 
				//在地图上显示marka
				for(int m1 = 0; m1 < no_cars1; m1++) {
					 int randomNumber = (int)(Math.random()*19);
					 listfour.add(list.get(randomNumber));
				}
				 
				for(Iterator<LatLng> it = listthree.iterator(); it.hasNext();) {
					LatLng point = it.next();
					BitmapDescriptor bitmap1 = BitmapDescriptorFactory.fromResource(R.drawable.icon_lu); 
					OverlayOptions option = new MarkerOptions().position(point).icon(bitmap1);  
					mBaiduMap.addOverlay(option);	
				}
				
				for(Iterator<LatLng> it = listfour.iterator(); it.hasNext();) {
					LatLng point = it.next();
					BitmapDescriptor bitmap1 = BitmapDescriptorFactory.fromResource(R.drawable.icon_1); 
					OverlayOptions option = new MarkerOptions().position(point).icon(bitmap1);  
					mBaiduMap.addOverlay(option);	
				}
				listfour.clear();
				
				handler.removeCallbacks(runnable);
				handler2.removeCallbacks(runnable2);
			}
			else if(id == 2) {
				handler1.postDelayed(runnable1, 0);
				
				handler.removeCallbacks(runnable);
				handler2.removeCallbacks(runnable2);
			} 
			else if (id == 3) {
				mBaiduMap.clear();
				LatLng cenpt1 = new LatLng(31.225199,121.36149);
		        MapStatus mMapStatus = new MapStatus.Builder().target(cenpt1).zoom(15).build();
		        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
		        mBaiduMap.setMapStatus(mMapStatusUpdate);
		        
		        for(Iterator<LatLng> it = listfive.iterator(); it.hasNext();) {
					LatLng point = it.next();
					BitmapDescriptor bitmap1 = BitmapDescriptorFactory.fromResource(R.drawable.fp3); 
					OverlayOptions option = new MarkerOptions().position(point).icon(bitmap1);  
					mBaiduMap.addOverlay(option);	
		        }
		        
				handler.removeCallbacks(runnable);
				handler2.removeCallbacks(runnable2);
			}
			else if (id == 4) {
				handler2.postDelayed(runnable2, 0);
				
	            handler.removeCallbacks(runnable);
			}
		}
*/

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			
		}
    
    }
   
    
    // 初始化定位相关代码 
    private void initMyLocation()  
    {  
        // 定位初始化  
        mLocationClient = new LocationClient(this); 
        mMyLocationListener = new MyLocationListener();  
        mLocationClient.registerLocationListener(mMyLocationListener);  
        // 设置定位的相关配置  
        LocationClientOption option = new LocationClientOption(); 
        option.setOpenGps(true);// 打开gps，不打开也可以，通过wifi或者数据流量进行定位  
        option.setCoorType("bd09ll"); // 设置坐标类型 ,返回的定位结果是百度经纬度，默认值是gcj02 
        option.setScanSpan(50000); //设置发起定位请求的时间间隔为2S 
        mLocationClient.setLocOption(option); 
        mLocationClient.start(); //启动定位
    }  
    
    // 实现定位回调监听   
    public class MyLocationListener implements BDLocationListener  
    {  
        @Override  
        public void onReceiveLocation(BDLocation location)  
        {  
 
            // map view 销毁后不再处理新接收的位置  
            if (location == null || mMapView == null)  
                return; 
            MyLocationData locData = new MyLocationData.Builder().accuracy(location.getRadius()).latitude(location.getLatitude()).longitude(location.getLongitude()).build();   
            // 设置定位数据  
            mBaiduMap.setMyLocationData(locData);  
            mCurrentLantitude = location.getLatitude();  
            mCurrentLongitude = location.getLongitude(); 
            
System.out.println("init----->"+"mCurrentLantitude= " +mCurrentLantitude+ ",mCurrentLongitude= " +mCurrentLongitude);
            
            /*
            // 第一次定位时，将地图位置移动到当前位置  
            if (isFristLocation)  
            {  
                isFristLocation = false;  
                LatLng fLatLng = new LatLng(location.getLatitude(), location.getLongitude());  
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(fLatLng);  
                mBaiduMap.animateMapStatus(u);  
            }
            //更新地图位置  
            LatLng ll = new LatLng(location.getLatitude(),location.getLongitude());  
            MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);  
            mBaiduMap.animateMapStatus(u);
            */
                
        }

		@Override
		public void onReceivePoi(BDLocation arg0) {
			// TODO Auto-generated method stub
			
		}  
    }  
   
	
	@Override
	protected void onPause() {
		mMapView.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		mMapView.onResume();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		mMapView.onDestroy();
		mSearch.destroy();
		//mBaiduMap.setMyLocationEnabled(false);
		super.onDestroy();
		mMapView = null;
		handler.removeCallbacks(runnable); 
	}

	@Override
	public void onGetGeoCodeResult(GeoCodeResult result) {
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			Toast.makeText(MapsActivity.this, "抱歉，未能找到结果", Toast.LENGTH_SHORT).show();
		}
		mBaiduMap.clear();
		mBaiduMap.addOverlay(new MarkerOptions().position(result.getLocation())
				.icon(BitmapDescriptorFactory
						.fromResource(R.drawable.icon_marka)));
		mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(result.getLocation()));
		//String strInfo = String.format("纬度：%f 经度：%f",result.getLocation().latitude, result.getLocation().longitude);
		//Toast.makeText(MapsActivity.this, strInfo, Toast.LENGTH_SHORT).show();
		markerShow();
			
	}

	@Override
	public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			Toast.makeText(MapsActivity.this, "抱歉，未能找到结果", Toast.LENGTH_SHORT)
					.show();
		}
		mBaiduMap.clear();
		mBaiduMap.addOverlay(new MarkerOptions().position(result.getLocation())
				.icon(BitmapDescriptorFactory
						.fromResource(R.drawable.icon_marka)));
		mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(result
				.getLocation()));
		//Toast.makeText(MapsActivity.this, result.getAddress(),Toast.LENGTH_SHORT).show();
		 markerShow();
	  
	}

	//自动在地图上显示驶入车辆的位置
	public void markerShow() {
		
System.out.println("show marker" + userPositionList.size());	 
			//在地图上显示驶入的车辆
			for(Iterator<LatLng> it = userPositionList.iterator(); it.hasNext();) {
				LatLng point = it.next();
				BitmapDescriptor bitmap1 = BitmapDescriptorFactory.fromResource(R.drawable.icon_lu); 
				OverlayOptions option = new MarkerOptions().position(point).icon(bitmap1);  
				mBaiduMap.addOverlay(option);	
			}
			userPositionList.clear();
		}


//按钮触发事件，利用ViewHolder，没成功，找不到原因，可以delete
/*	
	private void init() {
		fillViewHoler();
		
		ViewHolder.getMainButton().setOnClickListener(mButtonTabListener);
		ViewHolder.getHistoryButton().setOnClickListener(mButtonTabListener);
		ViewHolder.getSettingButton().setOnClickListener(mButtonTabListener);
		ViewHolder.getLocationButton().setOnClickListener(mButtonTabListener);
		ViewHolder.getReverseGeoButton().setOnClickListener(mButtonTabListener);
		
	}

	
	private void fillViewHoler() {
		
		ViewHolder.setMainButton(findViewById(R.id.buttonMain));
		ViewHolder.setHistoryButton(findViewById(R.id.buttonHistory1));
		ViewHolder.setSettingButton(findViewById(R.id.buttonSetting1));
		ViewHolder.setLocationButton(findViewById(R.id.buttonLocation));
	    ViewHolder.setReverseGeoButton(findViewById(R.id.buttonReverse));   
	}
	
	
	private OnClickListener mButtonTabListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent;
		 
			switch(v.getId()) {
		   case R.id.buttonMain:
				intent = new Intent(getApplicationContext(), MainActivity.class);
				startActivity(intent);
				finish();
				break;
			case R.id.buttonHistory1:
				intent = new Intent(getApplicationContext(), HistoryActivity.class);
				startActivity(intent);
				finish();
				break;
			case R.id.buttonSetting1:
				intent = new Intent(getApplicationContext(), SettingActivity.class);
				startActivity(intent);
				finish();
				break;
			case R.id.buttonLocation:
				initMyLocation();
				mLocationClient.start();
				LatLng iLatLng = new LatLng(mCurrentLantitude, mCurrentLongitude);  
				MapStatus mMapStatus = new MapStatus.Builder().target(iLatLng).zoom(12).build();
			    MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(iLatLng);  
			    mBaiduMap.animateMapStatus(u);
			case R.id.buttonReverse:
				LatLng ptCenter1 = new LatLng(31.224950,121.3610000);
				mSearch.reverseGeoCode(new ReverseGeoCodeOption().location(ptCenter1));
			}
		}
		
	};
	
	
	//ViewHolder
	private static class ViewHolder {
		//通过按钮来实现
		private static ImageButton mainButton = null;
		private static ImageButton historyButton = null;
		private static ImageButton settingButton = null;
		private static ImageButton locationButton = null;
		private static ImageButton reverseGeoButton = null;
		
		public static ImageButton getMainButton() {
			return mainButton;
		}
		public static void setMainButton(View mainButton) {
			ViewHolder.mainButton = (ImageButton)mainButton;
		}
		public static ImageButton getHistoryButton() {
			return historyButton;
		}
		public static void setHistoryButton(View buttonHistory) {
			ViewHolder.historyButton = (ImageButton)buttonHistory;
		}
		public static ImageButton getSettingButton() {
			return settingButton;
		}
		public static void setSettingButton(View buttonSetting) {
			ViewHolder.settingButton = (ImageButton)buttonSetting;
		}
		public static ImageButton getLocationButton() {
			return locationButton;
		}
		public static void setLocationButton(View buttonLocation) {
			ViewHolder.locationButton = (ImageButton)buttonLocation;
		}
		public static ImageButton getReverseGeoButton() {
			return reverseGeoButton;
		}
		public static void setReverseGeoButton(View buttonReverseGeo) {
			ViewHolder.reverseGeoButton = (ImageButton)buttonReverseGeo;
		}
		
	}
	*/
}

