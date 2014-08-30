package com.ericsson.leftpark;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import android.annotation.SuppressLint;


public class HttpHelper {
	
	public static final String HTTP_HEADER_ACCEPT = "Accept";
	public static final String HTTP_HEADER_CONTENT_TYPE = "Content-Type";
//	public static final String HTTP_CONTENT_TYPE_JSON = "application/json";
	
	private static DefaultHttpClient httpClient = null;
	
	public static DefaultHttpClient getDefaultHttpClient() {
		return httpClient;
	}
	
	 public static <T extends HttpRequestBase> T getHttpMethod(String relativeURL, Class<T> clazz) {
	    	return getDefaultHttpMethod(relativeURL, clazz);
	    }
	 
	 public static <T extends HttpRequestBase> T getDefaultHttpMethod(String url, Class<T> clazz) {
		 if(httpClient == null) {
			 HttpParams params = new BasicHttpParams();
			 DefaultHttpClient client = new DefaultHttpClient();
			 ClientConnectionManager mgr = client.getConnectionManager();
			 httpClient = new DefaultHttpClient( new ThreadSafeClientConnManager(params, mgr.getSchemeRegistry()), params);
			 httpClient.getParams().setParameter(HttpProtocolParams.HTTP_CONTENT_CHARSET, HTTP.UTF_8);
			 httpClient.getParams().setParameter(HttpConnectionParams.CONNECTION_TIMEOUT, 15*1000);
			 httpClient.getParams().setParameter(HttpProtocolParams.USER_AGENT, getUserAgentString());

//		 httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, new HttpHost("www-proxy.ericsson.se", 8080));
		 }
	    	
	    	T httpRequest = null;

			try {
				Constructor<T> constructor = clazz.getConstructor(new Class[]{ String.class });
				httpRequest = constructor.newInstance(url);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
//			httpRequest.setHeader(Constants.HTTP_HEADER_USER_NAME, userName);
//			httpRequest.setHeader(HTTP_HEADER_ACCEPT, HTTP_CONTENT_TYPE_JSON);
//			httpRequest.setHeader(HTTP_HEADER_CONTENT_TYPE, HTTP_CONTENT_TYPE_JSON);			
	    	return httpRequest;
	    }
	
	@SuppressLint("ShowToast")
	public static <T extends HttpRequestBase> String doHttpRequest(T httpRequest)  {
		
    		String result = null;
	        HttpResponse response;
    		try {
	    		response = httpClient.execute(httpRequest);
	    		int responseCode = response.getStatusLine().getStatusCode();
		    	if (responseCode == HttpStatus.SC_OK || responseCode == HttpStatus.SC_ACCEPTED){
		    		result = getContentFromStream(response.getEntity().getContent());
		    	} 
		    } catch (ClientProtocolException e) {
		    	// TODO Auto-generated catch block
		    	e.printStackTrace();
		    } catch (IOException e) {
		    	e.printStackTrace();

	    	} catch(Exception e) {
//	    		Toast.makeText(ParkingSlotApplication.Instence().getApplicationContext(), "newwork error!", Toast.LENGTH_LONG);
	    	}
    		finally {
		    	if(httpRequest != null) {
		    		httpRequest.abort();
//		    		httpClient.getConnectionManager().closeExpiredConnections();
		    	}
	    	}
	    	return result;
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
	
	private static String getUserAgentString() {
		StringBuffer sb = new StringBuffer();
		sb.append("os/Android");
		sb.append(" deviceId/" + ParkingSlotApplication.Instence().getDeviceId());
		return sb.toString();
	}
}