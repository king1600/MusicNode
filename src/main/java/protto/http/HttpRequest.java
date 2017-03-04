package protto.http;

import org.json.JSONObject;

public class HttpRequest {
	
	private String url;
	private String data;
	private String method;
	private JSONObject params;
	private JSONObject headers;
	
	public HttpRequest() {
		
	}
	
	public HttpRequest(String url) {
		this.url = url;
		this.data = null;
		this.method = "GET";
		this.params = new JSONObject();
		this.headers = new JSONObject();
	}
	
	public HttpRequest setUrl(String url) {
		this.url = url;
		return this;
	}
	
	public HttpRequest setMethod(String method) {
		this.method = method;
		return this;
	}
	
	public HttpRequest addParam(String key, Object value) {
		params.put(key, value.toString());
		return this;
	}
	
	public HttpRequest addHeader(String key, String value) {
		headers.put(key.toLowerCase(), value);
		return this;
	}
	
	public HttpRequest setBody(String body) {
		this.data = body;
		return this;
	}
	
	public HttpRequest setBody(JSONObject body) {
		this.data = body.toString();
		String contentType = headers.getString("content-type");
		if (contentType == null)
			contentType = "application/json";
		else
			contentType += ";application/json";
		addHeader("Content-Type", contentType);
		return this;
	}
	
	public String getUrl() {
		return url;
	}
	
	public String getData() {
		return data;
	}
	
	public String getMethod() {
		return method;
	}
	
	public String getBody() {
		return data;
	}
	
	public JSONObject getParams() {
		return params;
	}
	
	public JSONObject getHeaders() {
		return headers;
	}
}
