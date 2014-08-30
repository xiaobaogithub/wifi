package com.ericsson.leftpark;

import java.io.IOException;
import java.io.InputStream;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

public class ParkingController {
	static final String GET_URL = "http://58.247.178.239:8088/parking/countdown";
	static final String HISTORY_URL = "http://58.247.178.239:8088/parking/history?start=";
	static final String FILENAME = "history.data";
	
	private DefaultHttpClient httpClient = null;

	public ParkingController() {
		httpClient = new DefaultHttpClient();
		httpClient.getParams().setParameter(HttpProtocolParams.HTTP_CONTENT_CHARSET, HTTP.UTF_8);
		httpClient.getParams().setParameter(HttpConnectionParams.CONNECTION_TIMEOUT, 15*1000);
//		httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, new HttpHost("www-proxy.ericsson.se", 8080));
	}
	
	public String getParkingSlot() {
		HttpGet httpGet = new HttpGet(GET_URL);
		try {
			HttpResponse response = httpClient.execute(httpGet);
//			Log.i("Get :", "result = " + response.getStatusLine().getStatusCode());
			if(response.getStatusLine().getStatusCode() == 200) {
				String str = getContentFromStream(response.getEntity().getContent());
				if(str != null) {
					ParkingSlotApplication.Instence().count = Integer.valueOf(str);
					System.out.println("parking space is :" + ParkingSlotApplication.Instence().count);
					return str;
				}
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return null;
	}
	
	public String getHistory(String start, String end) {
		
		HttpGet httpGet = new HttpGet(HISTORY_URL + start + "&end=" + end);
		try {
			HttpResponse response = httpClient.execute(httpGet);
//			Log.i("Get :", "result = " + response.getStatusLine().getStatusCode());
			if(response.getStatusLine().getStatusCode() == 200) {
				String str = getContentFromStream(response.getEntity().getContent());
				if(str != null && str.length()>0) {
					
					System.out.println("history :" + str);
					return str;
				}
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private static String getContentFromStream(InputStream inputStream) throws IOException {
		StringBuilder builder = new StringBuilder();
		byte[] buffer = new byte[1024*10];
		int bufferLength = 0;
		while ((bufferLength = inputStream.read(buffer)) != -1) {
			builder.append(new String(buffer, 0, bufferLength));
		}
		return builder.toString();
	}
}