package protto.http;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.zip.GZIPInputStream;

public class HttpRequestHandler {
	
	private static StringBuilder builder = new StringBuilder();
	private static HttpURLConnection conn = null;
	private static DataOutputStream writer = null;
	private static InputStream stream = null;
	private static String encoding;
	private static String buffer;
	private static String url;
	
	private static String urlEncode(String data) throws Exception {
		return URLEncoder.encode(data, "UTF-8");
	}
	
	public static HttpResponse httpRequest(HttpRequest req) {
		// prepare variables
		conn    = null;
		
		try {
			// create URL
			url = req.getUrl();
			for (String key : req.getParams().keySet()) {
				url += (url.contains("?")) ? "&" : "?";
				url += key + "=" + urlEncode(req.getParams().getString(key));
			}
			
			// set connection options
			conn = (HttpURLConnection)new URL(url).openConnection();
			conn.setRequestMethod(req.getMethod());
			for (String key : req.getHeaders().keySet())
				conn.setRequestProperty(key, req.getHeaders().getString(key));
			
			// get raw stream
			encoding = conn.getContentEncoding();
			stream   = conn.getInputStream();
			if (encoding != null)
				stream = (encoding.contains("gzip")) ? new GZIPInputStream(stream) : stream;
			if (req.getBody() != null)
				conn.setDoOutput(true);
				
			// create reader and writer
			try (BufferedReader reader   = new BufferedReader(new InputStreamReader(stream))) {
				
				// write data if any given
				if (req.getBody() != null) {
					writer = new DataOutputStream(conn.getOutputStream());
					writer.writeBytes(req.getBody());
					writer.close();
					writer = null;
				}
				
				// read from connection, then build and return response
				while ((buffer = reader.readLine()) != null)
					builder.append(buffer);
				return new HttpResponse(conn, builder.toString());
				
			// handle any read and write errors
			} catch (Exception ex) {
				throw ex;
				
			// close the input stream
			} finally {
				stream.close();
				stream = null;
			}
			
		// catch any connection settings and stream errors
		} catch (Exception ex) {
			ex.printStackTrace();
			
		// close the connection and reset builder
		} finally {
			if (conn != null)
				conn.disconnect();
			builder.setLength(0);
		}
		
		// return null if not successful
		return null;
	}
}
