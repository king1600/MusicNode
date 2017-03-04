package protto.http;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

public class HttpResponse {

	private int code;
	private int length;
	private String body;
	private String reason;
	private String contentType;
	private Map<String, String> headers;
	
	public HttpResponse(HttpURLConnection conn, String data) throws Exception {
		body = data;
		headers = new HashMap<>();
		code = conn.getResponseCode();
		length = conn.getContentLength();
		reason = conn.getResponseMessage();
		contentType = conn.getContentType();
		for (String key : conn.getHeaderFields().keySet())
			headers.put(key, conn.getHeaderFields().get(key).toString());
		if (data != null)
			length = data.length();
		if (length < 0)
			length = 0;
	}
	
	public int getCode() {
		return code;
	}
	
	public int length() {
		return length;
	}
	
	public String getBody() {
		return body;
	}
	
	public String getReason() {
		return reason;
	}
	
	public String getContentType() {
		return contentType;
	}
	
	public Map<String, String> getHeaders() {
		return headers;
	}
	
	public JSONObject getJson() {
		if (contentType.contains("json")) {
			try {
				return new JSONObject(getBody());
			} catch (Exception ex) {}
		}
		return null;
	}
	
	@Override
	public String toString() {
		return getBody();
	}
}
