package protto.music;

import java.util.ArrayList;
import java.util.List;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;

import net.dv8tion.jda.core.audio.AudioSendHandler;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.managers.AudioManager;
import protto.Config;
import protto.Utils;

public class Player extends AudioEventAdapter implements AudioSendHandler {
	
	public final Guild guild;
	public final AudioPlayer player;
	private final AudioPlayerManager manager;
	
	public TextChannel lastChannel;
	public List<AudioTrack> queue;
	private AudioFrame lastFrame;
	private byte[] buffer;
	private int volume = Config.DEFAULT_VOLUME;
	
	public Player(final AudioPlayerManager manager, final Guild guild) {
		this.manager = manager;
		this.player = manager.createPlayer();
		this.guild = guild;
		
		// initialize self variables
		queue = new ArrayList<>();
		
		// set event handlers / listeners
		guild.getAudioManager().setSendingHandler(this);
		player.addListener(this);
	}
	
	// make sure player is connected to voice 
	private void connectToVoice(MessageReceivedEvent e, AudioManager voice) {
		if (!voice.isConnected() && !voice.isAttemptingToConnect())
			voice.openAudioConnection(
				e.getMember().getVoiceState().getChannel()); 
	}
	
	// check if player is connected
	public boolean isConnected() {
		return guild.getAudioManager().isConnected();
	}
	
	// get the URL of a given track
	public String getUrl (AudioTrack track) {
		return YTSearch.VIDEO + track.getIdentifier();
	}
	
	// get the URL of the current playing track
	public String getPlayingUrl() {
		if (getPlaying() == null)
			return null;
		return getUrl(getPlaying());
	}
	
	// get formatted time of currently playing track
	public String getPlayingTime() {
		if (getPlaying() == null)
			return "00:00/00:00";
		if (getPlaying().getInfo().isStream)
			return "∞";
		return new TrackTime(getPlaying().getPosition()).toString()
			 + "/"
			 + new TrackTime(getPlaying().getDuration()).toString();
	}
	
	// get the embed link of a track
	public String getEmbedLink(AudioTrack track) {
		if (track == null)
			return "No song is currently playing";
		return String.format("[%s](%s)",
			track.getInfo().title,
			(track instanceof YoutubeAudioTrack) ? 
				YTSearch.VIDEO + track.getIdentifier()
				+ "&t=" + Long.toString(track.getPosition() / 1000) + "s"
			: track.getIdentifier()
		);
	}
	
	// get the current playing track
	public AudioTrack getPlaying() {
		return player.getPlayingTrack();
	}
	
	// get the current playing image
	public String getPlayingImage() {
		if (getPlaying() == null)
			return null; 
		return YTSearch.THUMBNAIL.replaceAll("#ID", getPlaying().getIdentifier());
	}
	
	// leave the voice channel and disconnect everything
	public boolean leave() {
		if (Config.DEBUG)
			Utils.Log(String.format("[%s] Leaving", guild.getId()));
		guild.getAudioManager().closeAudioConnection();
		queue.clear();
		player.stopTrack();
		return true;
	}
	
	// set the volume of the player
	public void volume(int percent) {
		if (percent > 100) percent = 100;
		if (percent < 0) percent = 0;
		player.setVolume(percent);
		volume = percent;
	}
	
	// pause the player
	public void pause() {
		if (!player.isPaused())
			player.setPaused(true);
	}
	
	// un-pause the player
	public void resume() {
		if (player.isPaused())
			player.setPaused(false);
	}
	
	// skip to the next song
	public void skip() {
		skip(null);
	}
	
	// skip the next song
	public void skip(AudioTrack last) {
		if (Config.DEBUG)
			Utils.Log(String.format("[%s] Called Skip", guild.getId()));
		
		// if queue is empty, quit
		if (queue.size() < 1)
			leave();
		
		// if no one in VoiceChannel, quit
		else if (guild.getSelfMember().getVoiceState().getChannel().getMembers().size() < 2)
			leave();
		
		// actually skip song
		else {
			if (Config.DEBUG)
				Utils.Log(String.format("[%s] Skipping", guild.getId()));
			player.playTrack(queue.get(0));
			player.setVolume(volume);
		}
	}
	
	// add a song to the player queue and send message
	public void add(final MessageReceivedEvent e, final String url, boolean front) {
		
		// get last channel talking in and make sure connected to VoiceChannel
		lastChannel = e.getMessage().getTextChannel();
		lastChannel.sendTyping().queue();
		connectToVoice(e, guild.getAudioManager());
		
		// load the requested track/URL
		manager.loadItem(url, new AudioLoadResultHandler(){
			
			// handle adding one track
			@Override
			public void trackLoaded(AudioTrack track) {
				queue.add((front) ? 0 : queue.size(), track);
				if (player.getPlayingTrack() == null)
					skip();
				lastChannel.sendMessage(((front) ? "Inserted" : "Added") + " " + track.getInfo().title).queue();
			}

			// handle adding multiple tracks
			@Override
			public void playlistLoaded(AudioPlaylist playlist) {
				if (front)
					for (int i = playlist.getTracks().size() - 1; i >= 0; i--)
						queue.add(0, playlist.getTracks().get(i));
				else
					for (AudioTrack track : playlist.getTracks())
						queue.add(track);
				if (player.getPlayingTrack() == null)
					skip();
				lastChannel.sendMessage(((front) ? "Inserted" : "Added") + " playlist " + playlist.getName()).queue();
			}

			// handle when to matches for track were found
			@Override
			public void noMatches() {
				lastChannel.sendMessage("No matches found for " + url).queue();
			}

			// handle error when track failed to load
			@Override
			public void loadFailed(FriendlyException ex) {
				if (Config.DEBUG)
					Utils.Error("Failed to load " + url + " -> " + ex.getMessage());
				ex.printStackTrace();
			}
		});
	}
	
	// event called when a track has started playing
	@Override
	public void onTrackStart(AudioPlayer player, AudioTrack track) {
		if (Config.DEBUG)
			Utils.Log(String.format("[%s] Track Started", guild.getId()));
		queue.remove(0);
	}
	
	// event called when a track finishes playing
	@Override
	public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
		if (Config.DEBUG)
			Utils.Log(String.format("[%s] Track Ended. Reason: %s", guild.getId(), endReason));
		if (endReason.mayStartNext)
			skip();
	}
	
	// event called when an error is received on track
	@Override
	public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException ex) {
		if (Config.DEBUG)
			Utils.Log(String.format("[%s] Track Error", guild.getId()));
		skip();
		lastChannel.sendMessage("Failed to play song:\n```" + ex.getMessage() + "```").queue();
		ex.printStackTrace();
	}
	
	// event called when a track is stuck and not playing
	@Override
	public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
		if (Config.DEBUG)
			Utils.Log(String.format("[%s] Track was Stuck", guild.getId()));
		skip();
	}
	
	// override discord audio sender
	@Override
	public boolean canProvide() {
		if (lastFrame == null)
			lastFrame = player.provide();
		return lastFrame != null;
	}

	// send player voice data
	@Override
	public byte[] provide20MsAudio() {
		if (lastFrame == null)
			lastFrame = player.provide();
		buffer = (lastFrame != null) ? lastFrame.data : null;
		lastFrame = null;
		return buffer;
	}
	
	// other function to check if stream is OPUS
	@Override
	public boolean isOpus() {
		return true;
	}
}

