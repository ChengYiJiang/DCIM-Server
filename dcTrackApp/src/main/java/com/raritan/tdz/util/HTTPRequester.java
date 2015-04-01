package com.raritan.tdz.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection; import javax.net.ssl.SSLContext; import javax.net.ssl.TrustManager; import javax.net.ssl.X509TrustManager; import javax.net.ssl.*;


public class HTTPRequester {

	public static void main(String[] args) {

		int cmd = Integer.parseInt(args[0]);
		if (cmd == 1) {
			// do Get
			System.out.println("Response from Get Request: \n"
					+ sendGetRequest(args[1], ""));
		} else if (cmd == 2) {
			System.out.println("Response from POST Request: \n"
					+ sendPutOrPostRequest(args[1], args[2], "POST"));
		} else if (cmd == 3) {
			System.out.println("Response from PUT Request: \n"
					+ sendPutOrPostRequest(args[1], args[2], "PUT"));
		}
		// System.out.println("Response from Get Request: \n"+sendGetRequest(args[0],""));
		// System.out.println("Response from POST Request: \n"+sendPostRequest(args[0],args[1]));
		// System.out.println("Response from DELETE Request: \n"+sendDeleteRequest(args[0],args[1]));
	}

	public static void acceptUnsigned() {
		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(
					java.security.cert.X509Certificate[] certs, String authType) {
			}

			public void checkServerTrusted(
					java.security.cert.X509Certificate[] certs, String authType) {
			}
		} };

		// Install the all-trusting trust manager
		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection
					.setDefaultSSLSocketFactory(sc.getSocketFactory());
			HttpsURLConnection
					.setDefaultHostnameVerifier(new HostnameVerifier() {
						public boolean verify(String string, SSLSession ssls) {
							return true;
						}
					});
		} catch (Exception e) {
		}
	}

	/**
	 * Sends an HTTP GET request to a url
	 * 
	 * @param endpoint
	 *            - The URL of the server.
	 * @param requestParameters
	 *            - all the request parameters (Example:
	 *            "param1=val1&param2=val2").
	 * @return - The response from the end point as string
	 */
	public static String sendGetRequest(String endpoint,
			String requestParameters) {
		String result = null;
		if (endpoint.startsWith("https://")) {
			acceptUnsigned();
			// Send a GET request to the servlet
			try {
				// Construct data
				StringBuffer data = new StringBuffer();
				// Send data
				String urlStr = endpoint;
				if (requestParameters != null && requestParameters.length() > 0) {
					urlStr += "?" + requestParameters;
				}
				URL url = new URL(urlStr);
				URLConnection conn = url.openConnection();
				conn.setAllowUserInteraction(true);
				// Set the auth header
				conn.setRequestProperty("Authorization",
						"Basic d2ViX2FwaTpSYXJpdGFuQDEyMw==");
				// Get the response
				BufferedReader rd = new BufferedReader(new InputStreamReader(
						conn.getInputStream()));
				StringBuffer sb = new StringBuffer();
				String line;
				while ((line = rd.readLine()) != null) {
					sb.append(line);
				}
				rd.close();
				result = sb.toString();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * Reads data from the data and posts it to a server via POST request.
	 * 
	 * @param data
	 *            - The data you want to send
	 * @param urlStr
	 *            - The destination server's url address
	 * @return returns the server's response to output
	 * 
	 * @throws Exception
	 */
	public static String sendPutOrPostRequest(String urlStr, String data,
			String putOrPost) {
		HttpURLConnection urlc = null;
		char[] buffer = new char[data.length()];
		buffer = data.toCharArray();
		String response = "";
		try {
			URL url = new URL(urlStr);
			acceptUnsigned();

			urlc = (HttpURLConnection) url.openConnection();
			// set auth header
			urlc.setRequestProperty("Authorization",
					"Basic d2ViX2FwaTpSYXJpdGFuQDEyMw==");
			urlc.setRequestProperty("Content-Type", "application/json");

			try {
				urlc.setRequestMethod(putOrPost);
			} catch (ProtocolException e) {
				e.printStackTrace();
				return "";
			}
			urlc.setDoOutput(true);
			urlc.setDoInput(true);
			urlc.setUseCaches(false);
			urlc.setAllowUserInteraction(false);

			PrintWriter pw = new PrintWriter(urlc.getOutputStream());
			pw.write(buffer, 0, data.length());
			pw.flush();
			pw.close();

			System.out.println("Response Code is:" + urlc.getResponseCode());
			BufferedReader in = new BufferedReader(new InputStreamReader(
					urlc.getInputStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				response += inputLine;
			}
			in.close();
		} catch (Exception e1) {
			e1.printStackTrace();
			response = e1.toString();
		} finally {
			if (urlc != null)
				urlc.disconnect();
		}

		return response;
	}

	/**
	 * Util function which read any input stream and returns a byte array
	 * 
	 * @param istream
	 *            The stream to be read
	 * @return the byte array of data read.
	 */
	public static byte[] read(InputStream istream) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024]; // Experiment with this value
		int bytesRead;

		while ((bytesRead = istream.read(buffer)) != -1) {
			baos.write(buffer, 0, bytesRead);
		}

		return baos.toByteArray();
	}

}
