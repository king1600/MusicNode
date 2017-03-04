package protto;

import java.awt.Color;
import java.util.Random;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.TextChannel;
import protto.http.HttpRequest;
import protto.http.HttpRequestHandler;
import protto.http.HttpResponse;

public class Utils {
	public static Random Rand = new Random();
	private static MessageBuilder MBuilder = new MessageBuilder();
	
	public static void Log(Object message) {
		System.out.println("[*] " + message);
	}
	
	public static void Error(Object message) {
		System.err.println("[x] " + message);
	}
	
	public static HttpResponse makeRequest(HttpRequest req) {
		return HttpRequestHandler.httpRequest(req);
	}
	
	public static String getGoogleKey() {
		return Config.GOOGLE_KEYS[Rand.nextInt(Config.GOOGLE_KEYS.length)];
	}
	
	public static String splitDrop(String data, String delim, int spaces) {
		for (int i = 0; i < spaces; i++)
			data = data.substring((data.indexOf(delim) > -1) ? data.indexOf(delim) + 1 : 0);
		return data;
	}
	
	public static Color getRandomColor() {
		return new Color(Rand.nextInt(255), Rand.nextInt(255), Rand.nextInt(255));
	}
	
	public static boolean isUrl(String test) {
		return (test.toLowerCase().startsWith("http://") ||
				test.toLowerCase().startsWith("https://"));
	}
	
	public static void sendEmbed(final TextChannel channel, final EmbedBuilder builder) {
		channel.sendMessage(MBuilder.setEmbed(builder.build()).build()).queue();
	}
}
