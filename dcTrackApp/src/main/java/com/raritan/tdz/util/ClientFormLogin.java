package com.raritan.tdz.util;

/*
 * ====================================================================
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.DefaultHttpClientConnection;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import com.raritan.tdz.util.LogManager;
import com.raritan.tdz.util.MessageContext;

/**
 * A example that demonstrates how HttpClient APIs can be used to perform
 * form-based logon.
 */
public class ClientFormLogin {

	private static Logger appLogger = Logger.getLogger(ClientFormLogin.class);	
	
	public static void log(String methodName, String msg) {
		LogManager.debug(new MessageContext(ClientFormLogin.class, methodName, msg ), appLogger);		
	}
	
	public static String login(String pageurl, String actionurl, String name, String pass) {

		final String methodName="login";
		
		String rst = "";

		log(methodName, pageurl + " " + actionurl);
		log(methodName, "Login: " + name + " " + pass);
		
		DefaultHttpClient client = new DefaultHttpClient();
		DefaultHttpClient httpclient = WebClientDevWrapper.wrapClient(client);
		try {
			//HttpGet httpget = new HttpGet("https://192.168.51.220/");
			HttpGet httpget = new HttpGet(pageurl);
			
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();


			log(methodName, "Login form get: " + response.getStatusLine());
			
			// For httpcore-4.0
			entity.consumeContent();
			// For httpcore-4.1
			//EntityUtils.consume(entity);

			log(methodName, "Initial set of cookies:");
			List<Cookie> cookies = httpclient.getCookieStore().getCookies();
			if (cookies.isEmpty()) {
				log(methodName, "None");
			} else {
				for (int i = 0; i < cookies.size(); i++) {
					log(methodName, "- " + cookies.get(i).toString());
				}
			}

			//HttpPost httpost = new HttpPost("https://192.168.51.220/login/login");
			HttpPost httpost = new HttpPost(actionurl);

			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			nvps.add(new BasicNameValuePair("login", name));
			nvps.add(new BasicNameValuePair("password", pass));

			httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

			response = httpclient.execute(httpost);
			entity = response.getEntity();

			log(methodName, "Login form get: " + response.getStatusLine());

			// For httpcore-4.0
			entity.consumeContent();
			// For httpcore-4.1
			//EntityUtils.consume(entity);

			log(methodName, "Post logon cookies:");
			cookies = httpclient.getCookieStore().getCookies();
			if (cookies.isEmpty()) {
				log(methodName, "None");
			} else {
				for (int i = 0; i < cookies.size(); i++) {
					log(methodName, "- " + cookies.get(i).toString());
					if (cookies.get(i).getName().equals("DCTRACK_INIT_SESSION")) {
						rst = cookies.get(i).getValue();
					}

				}
			}
		} catch (Exception e) {
			log(methodName, "Exception: " + e.getMessage());
		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			httpclient.getConnectionManager().shutdown();
		}
		return rst;
	}
	
	public static String login(String pageurl, String actionurl, String name, String pass, List<Cookie> cookies) {

		final String methodName="login";
		
		String rst = "";

		log(methodName, pageurl + " " + actionurl);
		log(methodName, "Login: " + name + " " + pass);
		
		DefaultHttpClient client = new DefaultHttpClient();
		DefaultHttpClient httpclient = WebClientDevWrapper.wrapClient(client);
		try {
			//HttpGet httpget = new HttpGet("https://192.168.51.220/");
			HttpGet httpget = new HttpGet(pageurl);
			
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();


			log(methodName, "Login form get: " + response.getStatusLine());
			
			// For httpcore-4.0
			entity.consumeContent();
			// For httpcore-4.1
			//EntityUtils.consume(entity);

			log(methodName, "Initial set of cookies:");
			List<Cookie> cookieList = httpclient.getCookieStore().getCookies();
			if (cookieList.isEmpty()) {
				log(methodName, "None");
			} else {
				for (int i = 0; i < cookieList.size(); i++) {
					log(methodName, "- " + cookieList.get(i).toString());
				}
			}

			//HttpPost httpost = new HttpPost("https://192.168.51.220/login/login");
			HttpPost httpost = new HttpPost(actionurl);

			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			nvps.add(new BasicNameValuePair("login", name));
			nvps.add(new BasicNameValuePair("password", pass));

			httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

			response = httpclient.execute(httpost);
			entity = response.getEntity();

			log(methodName, "Login form get: " + response.getStatusLine());

			// For httpcore-4.0
			entity.consumeContent();
			// For httpcore-4.1
			//EntityUtils.consume(entity);

			log(methodName, "Post logon cookies:");
			cookieList = httpclient.getCookieStore().getCookies();
			cookies.addAll(cookieList);
			if (cookies.isEmpty()) {
				log(methodName, "None");
			} else {
				for (int i = 0; i < cookies.size(); i++) {
					log(methodName, "- " + cookies.get(i).toString());
					if (cookies.get(i).getName().equals("DCTRACK_INIT_SESSION")) {
						rst = cookies.get(i).getValue();
					}

				}
			}
		} catch (Exception e) {
			log(methodName, "Exception: " + e.getMessage());
		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			httpclient.getConnectionManager().shutdown();
		}
		return rst;
	}
	
	public static void logout(String actionurl, List<Cookie> cookies){
		final String methodName="logout";
		DefaultHttpClient client = new DefaultHttpClient();
		DefaultHttpClient httpclient = WebClientDevWrapper.wrapClient(client);
		
		try {
			
			CookieStore store = new BasicCookieStore();
			for (Cookie cookie:cookies){
				store.addCookie(cookie);
			}
			httpclient.setCookieStore(store);
			
			//HttpGet httpget = new HttpGet("https://192.168.51.220/");
			HttpGet httpget = new HttpGet(actionurl);
			
			
			HttpResponse response = httpclient.execute(httpget);
			log(methodName, "Respone: " + response);
		} catch (Exception e) {
			log(methodName, "Exception: " + e.getMessage());
		}finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			httpclient.getConnectionManager().shutdown();
		}
		
	}
}
