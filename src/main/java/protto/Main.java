package protto;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;

public class Main {
	public static void main(String[] args) {
		try {
			startBot();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private static void startBot() throws Exception {
		Bot bot = new Bot();
		JDA jda = new JDABuilder(AccountType.BOT)
				.setToken(Config.TOKEN)
				.addListener(bot)
				.setGame(Game.of(Config.PREFIX + "help"))
				.buildBlocking();
		bot.setJDA(jda);
	}
}
