package protto.music;

import org.json.JSONObject;

import protto.Utils;
import protto.http.HttpRequest;

public class YTSearch {

	public enum VidType { VIDEO, PLAYLIST };
	public static final String PLAYLIST  = "https://www.youtube.com/playlist?list=";
	public static final String VIDEO     = "https://www.youtube.com/watch?v=";
	public static final String THUMBNAIL = "https://img.youtube.com/vi/#ID/hqdefault.jpg";
	
	public static String Search(String query) {
		return Search(query, VidType.VIDEO);
	}
	
	public static String Search(String query, VidType type) {
		JSONObject result = Utils.makeRequest(
			new HttpRequest("https://www.googleapis.com/youtube/v3/search")
				.addParam("q", query)
				.addParam("key", Utils.getGoogleKey())
				.addParam("part", "snippet")
				.addParam("maxResults", 1)
				.addParam("type", type.name().toLowerCase())
		).getJson();
		
		if (result.getJSONObject("pageInfo").getInt("totalResults") < 1)
			return null;
		
		try {
			return
				((type == VidType.VIDEO) ? VIDEO : PLAYLIST) + 
				result.getJSONArray("items")
				.getJSONObject(0).getJSONObject("id")
				.getString(type.name().toLowerCase() + "Id");
		} catch (Exception ex) {
			return null;
		}
	}
}