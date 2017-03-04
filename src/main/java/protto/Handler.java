package protto;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import protto.music.Manager;
import protto.music.Player;
import protto.music.TrackTime;
import protto.music.YTSearch;
import protto.music.YTSearch.VidType;

public class Handler {
	
	public Manager musicManager;
	
	public Handler() {
		musicManager = new Manager();
	}
	
	private void seek(Player player, TrackTime time) {
		if (player.getPlaying() != null)
			player.getPlaying().setPosition(time.ms);
	}
	
	private void addMusic(final MessageReceivedEvent e, final String[] args, boolean reversed) {
		if (args.length > 1) {
			
			// extract query and url type
			VidType vType = VidType.VIDEO;
			String query = Utils.splitDrop(e.getMessage().getContent(), " ", 1);
			if (args[1].toLowerCase().equals("playlist") && args.length > 2) {
				vType = VidType.PLAYLIST;
				query = Utils.splitDrop(query, " ", 1);
			}
			query = (!Utils.isUrl(query)) ? YTSearch.Search(query, vType) : query;
			
			// handle message and request
			if (query == null)
				e.getChannel().sendMessage("Search not found").queue();
			else if (query.contains(YTSearch.VIDEO) && vType == VidType.PLAYLIST)
				e.getChannel().sendMessage(String.format("Use '%sadd' instead", Config.PREFIX)).queue();
			else if (query.contains(YTSearch.PLAYLIST) && vType == VidType.VIDEO)
				e.getChannel().sendMessage(String.format("Use '%sadd playlist' instead", Config.PREFIX)).queue();
			else
				musicManager.getGuildPlayer(e.getGuild()).add(e, query, reversed);
		}
	}
	
	private void removeItems(final MessageReceivedEvent e, final String[] args) {
		List<AudioTrack> queue = musicManager.getGuildPlayer(e.getGuild()).queue;
		TextChannel channel = e.getTextChannel();
		
		if (args.length > 1) {
			if (NumberUtils.isCreatable(args[1])) {
				int start = Integer.parseInt(args[1]), stop = -1;
				for (int i = 2; i < 4; i++)
					if (args.length > i)
						if (NumberUtils.isCreatable(args[i]))
							stop = Integer.parseInt(args[i]);
				stop = (stop < 1 || stop > queue.size()) ? queue.size() : stop;
				start = (start < 1) ? 0 : start - 1;
				queue.subList(start, stop).clear();
				channel.sendMessage(String.format("Tracks %d to %d have been removed", start + 1, stop)).queue();
			} else {
				channel.sendMessage("Invalid values").queue();
			}
		} else {
			channel.sendMessage("No values provided").queue();
		}
	}
	
	private void displayQueue(final MessageReceivedEvent e, final String[] args) {
		Player player = musicManager.getGuildPlayer(e.getGuild());
		TextChannel channel = e.getTextChannel();
		
		// setup variables
		String descr = "";
		String foot  = "";
		String title = "";
		String track = "";
		int shown    = 0;
		
		// fetch info to variables
		if (player.queue.size() > 0) {
			for (int i = 0; i < player.queue.size() && i < Config.QUEUE_BUFFER; i++) {
				track = (i + 1) + ". " + player.getEmbedLink(player.queue.get(i));
				if (track.length() + descr.length() >= 1024) break;
				descr += track + "\n";
				shown++;
			}
		}
		foot  = (shown < player.queue.size()) ? "+" + String.valueOf(player.queue.size() - shown) + " more" : null;
		descr = (descr.length() > 1) ? descr = descr.substring(0, descr.length() - 1) : "None";
		title = (player.getPlaying() != null) ? 
			player.getEmbedLink(player.getPlaying()) + String.format(" `%s`", player.getPlayingTime()) : "None";
					
		// send embed about queue info
		Utils.sendEmbed(channel, new EmbedBuilder()
			.addField("Playing:", title, false)
			.addField("Queue:", descr, false)
			.setThumbnail(player.getPlayingImage())
			.setFooter(foot, null)
			.setColor(Utils.getRandomColor())
		);
	}
	
	public void Handle(MessageReceivedEvent e) {
		
		// ignore invalid messages
		if (e.getAuthor().isBot() || e.isFromType(ChannelType.PRIVATE))
			return;
		
		// Actually handle messages
		if (e.getMessage().getContent().startsWith(Config.PREFIX))
		{
			// get message data
			String content = e.getMessage().getContent();
			String[] args  = content.split(" ");
			TextChannel channel = e.getTextChannel();
			String command = args[0].substring(Config.PREFIX.length()).toLowerCase();
			
			// log to console command
			Utils.Log(String.format("[%s] > %s", e.getMember().getEffectiveName(), content));
			
			// handle commands
			switch (command)
			{
				// basic ping test
				case "ping":
				{
					channel.sendMessage("Pong m8").queue();
					break;
				}
				
				// add a link/search to back  of queue
				case "add":
				{
					addMusic(e, args, false);
					break;
				}
				
				// add a link/search to front of queue
				case "insert":
				{
					addMusic(e, args, true);
					break;
				}
				
				// set the volume
				case "volume":
				{
					if (!e.getMember().getVoiceState().inVoiceChannel())
						break;
					try {
						musicManager.getGuildPlayer(e.getGuild()).volume(Integer.parseInt(args[1]));
					} catch (Exception ex) {
						channel.sendMessage("Value not provided").queue();
					}
					break;
				}
				
				// pause the music
				case "pause":
				{
					if (!e.getMember().getVoiceState().inVoiceChannel())
						break;
					musicManager.getGuildPlayer(e.getGuild()).pause();
					break;
				}
				
				// resume the paused music
				case "resume":
				{
					if (!e.getMember().getVoiceState().inVoiceChannel())
						break;
					musicManager.getGuildPlayer(e.getGuild()).resume();
					break;
				}
				
				// skip to next song
				case "skip":
				{
					if (!e.getMember().getVoiceState().inVoiceChannel())
						break;
					musicManager.getGuildPlayer(e.getGuild()).skip();
					break;
				}
				
				// force leave
				case "leave":
				{
					if (!e.getMember().getVoiceState().inVoiceChannel())
						break;
					musicManager.getGuildPlayer(e.getGuild()).leave();
					break;
				}
				
				// queue controls
				case "queue":
				{
					if (!musicManager.players.containsKey(e.getGuild().getId()))
						break;
					displayQueue(e, args);
					break;
				}
				
				// removing items from queue
				case "remove":
				{
					if (!musicManager.players.containsKey(e.getGuild().getId()))
						break;
					removeItems(e, args);
					break;
				}
				
				// swap items in queue
				case "shuffle":
				{
					if (!musicManager.players.containsKey(e.getGuild().getId()))
						break;
					Collections.shuffle(musicManager.getGuildPlayer(e.getGuild()).queue, Utils.Rand);
					channel.sendMessage("Queue shuffled").queue();
					break;
				}
				
				// clear the queue
				case "clear":
				{
					if (!musicManager.players.containsKey(e.getGuild().getId()))
						break;
					musicManager.getGuildPlayer(e.getGuild()).queue.clear();
					channel.sendMessage("Queue cleared").queue();
					break;
				}
				
				// seek song to a position
				case "seek":
				{
					if (!musicManager.players.containsKey(e.getGuild().getId()))
						break;
					seek(musicManager.getGuildPlayer(e.getGuild()), new TrackTime(args[1]));
					break;
				}
				
				// rewind song by amount
				case "rewind":
				{
					if (!musicManager.players.containsKey(e.getGuild().getId()))
						break;
					Player player = musicManager.getGuildPlayer(e.getGuild());
					seek(player, new TrackTime(player.getPlaying().getPosition())
							.sub(new TrackTime(args[1])));
					break;
				}
				
				// forward song by amount
				case "forward":
				{
					if (!musicManager.players.containsKey(e.getGuild().getId()))
						break;
					Player player = musicManager.getGuildPlayer(e.getGuild());
					seek(player, new TrackTime(player.getPlaying().getPosition())
							.add(new TrackTime(args[1])));
					break;
				}
			}
		}
	}
}
