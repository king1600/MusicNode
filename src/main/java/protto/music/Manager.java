package protto.music;

import java.util.HashMap;
import java.util.Map;

import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;

import net.dv8tion.jda.core.entities.Guild;

public class Manager {
	
	public Map<String, Player> players;
	public AudioPlayerManager audioManager;
	
	public Manager() {
		players = new HashMap<>();
		audioManager = new DefaultAudioPlayerManager();
		audioManager.registerSourceManager(new YoutubeAudioSourceManager());
		audioManager.registerSourceManager(new TwitchStreamAudioSourceManager());
		audioManager.registerSourceManager(new SoundCloudAudioSourceManager());
		audioManager.registerSourceManager(new VimeoAudioSourceManager());
		audioManager.registerSourceManager(new BandcampAudioSourceManager());
		audioManager.registerSourceManager(new HttpAudioSourceManager());
		audioManager.getConfiguration()
			.setResamplingQuality(AudioConfiguration.ResamplingQuality.MEDIUM);
	}
	
	public synchronized Player getGuildPlayer(Guild guild) {
		Player guildPlayer = players.get(guild.getId());
		
		if (guildPlayer == null) {
			guildPlayer = new Player(audioManager, guild);
			players.put(guild.getId(), guildPlayer);
		}
		
		guild.getAudioManager().setSendingHandler(guildPlayer);
		return guildPlayer;
	}
}
