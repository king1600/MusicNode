package protto;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.ShutdownEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceMuteEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class Bot extends ListenerAdapter {
	
	public JDA jda = null;
	public Handler handler;
	
	public Bot() {
		handler = new Handler();
	}
	
	public void setJDA(final JDA jda) {
		this.jda = jda;
	}
	
	@Override
	public void onReady(ReadyEvent e) {
		Utils.Log("Bot is ready");
	}
	
	@Override
	public void onMessageReceived(MessageReceivedEvent e) {
		handler.Handle(e);
	}
	
	@Override
	public void onShutdown(ShutdownEvent e) {
		
	}
	
	private void checkMute(GuildVoiceMuteEvent e) {
		if (e.isMuted())
			handler.musicManager.getGuildPlayer(e.getGuild()).pause();
		else
			handler.musicManager.getGuildPlayer(e.getGuild()).resume();
	}
	
	@Override
	public void onGuildVoiceMute(GuildVoiceMuteEvent e) {
		if (handler.musicManager.players.containsKey(e.getGuild().getId()))
			if (handler.musicManager.getGuildPlayer(e.getGuild()).getPlaying() != null)
				checkMute(e);
	}
}
