package Bot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import Bot.music.GuildMusicManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.Component;

public class InitManager {
	
	private static Map<Long, TextChannel> initChannels = new HashMap<>();
	private static final Map<Long, MessageEmbed> embeds = new HashMap<>();
	private static final Map<Long, Message> messages = new HashMap<>();
	private static final Map<Long, EmbedBuilder> embedBuilders = new HashMap<>();
	private static final Map<Long, Thread> updateThreads = new HashMap<>();
	
	public static Collection<TextChannel> getInitChannels() {
		return initChannels.values();
	}
	
	// Initialize disabled channels
	
	public static void load() {
		for (Long channelId : Main.initchannels.getInitChannels()) {
			TextChannel channel = Main.bot.getTextChannel(channelId);
			
			Guild guild = channel.getGuild();
			Long guildId = guild.getIdLong();
			
			// Purge channel message
			
			ChatManager.purgeChannel(channel);
	    	
	    	Member selfMember = guild.getSelfMember();
	    	AudioTrack currentTrack = Bot.AudioManager.get().getGuildMusicManager(guild).scheduler.getCurrentTrack();
	    	
	    	// Setting up embed
	    	
	    	EmbedBuilder e = new EmbedBuilder();
	    	e.setTitle("Music interface");
	    	e.setColor(selfMember.getColor());
			MessageEmbed embed = e.build();
			
			// Saving embed
			
			InitManager.setEmbedBuilder(guildId, e);
			InitManager.setEmbed(guildId, embed);
			InitManager.setInitChannel(guild, channel);
	    	
			// Sending embed
			
			channel.sendMessageEmbeds(embed).queue(message -> {
	    		InitManager.setMessage(guildId, message);
	    		InitManager.setTrack(guildId, currentTrack);
	    	});
		}
	}
	// Initialize disabled channels
	
	public static void setInitChannel(Guild guild, TextChannel initChannel) {
		if (initChannels.containsKey(guild.getIdLong()))
			Main.initchannels.removeInitChannel(initChannels.remove(guild.getIdLong()).getIdLong());
		initChannels.put(guild.getIdLong(), initChannel);
		Main.initchannels.addInitChannel(initChannel.getIdLong());
	}
	
	public static MessageEmbed getEmbed(long guildId) {
		return embeds.get(guildId);
	}
	
	public static Message getMessage(long guildId) {
		return messages.get(guildId);
	}
	
	public static EmbedBuilder getEmbedBuilder(long guildId) {
		return embedBuilders.get(guildId);
	}
	
	public static void setEmbed(long guildId, MessageEmbed embed) {
		if (embeds.containsKey(guildId))
			embeds.remove(guildId);
		embeds.put(guildId, embed);
	}
	
	public static void setMessage(long guildId, Message message) {
		if (messages.containsKey(guildId))
			messages.remove(guildId);
		messages.put(guildId, message);
	}
	
	@SuppressWarnings("deprecation")
	public static void deleteMessage(long guildId) {
		if (messages.containsKey(guildId)) {
			messages.remove(guildId);
		}
		if (embeds.containsKey(guildId)) {
			embeds.remove(guildId);
		}
		if (initChannels.containsKey(guildId)) {
			Main.initchannels.removeInitChannel(initChannels.remove(guildId).getIdLong());
		}
		if (embedBuilders.containsKey(guildId)) {
			embedBuilders.remove(guildId);
		}
		if (updateThreads.containsKey(guildId)) {
			updateThreads.remove(guildId).stop();
		}
	}
	
	public static void setEmbedBuilder(long guildId, EmbedBuilder embed) {
		if (embedBuilders.containsKey(guildId))
			embedBuilders.remove(guildId);
		embedBuilders.put(guildId, embed);
	}
	
	public static boolean isInit(Message msg) {
		
		if (msg.getEmbeds().size() != 1 || !msg.getMember().getUser().equals(Main.bot.getSelf())) return false;
		if (msg.getEmbeds().contains(getEmbed(msg.getGuild().getIdLong()))) return true;
		
		return false;
	}

	// Refresh thread time updates every 2 seconds
	
	private static Thread updateThread(long guildId) {
		return new Thread() {
			public void run(){
		      while (true) {
		    	  GuildMusicManager manager = AudioManager.get().getGuildMusicManager(Main.bot.getGuildById(guildId));
	    		  AudioTrack currentTrack = manager.scheduler.getCurrentTrack();
	    		  if (manager.isPlaying())
	    			  setTime(guildId, currentTrack.getPosition(), currentTrack.getDuration());
		    	  try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		      }
		    }
		};
	}
	
	// Handle update threads
	
	@SuppressWarnings("deprecation")
	private static void handleUpdate(long guildId, boolean hasTrack) {
		if (!hasTrack && updateThreads.containsKey(guildId)) {
			updateThreads.remove(guildId).stop();
		} else if (hasTrack && !updateThreads.containsKey(guildId)) {
			Thread updateThread = updateThread(guildId);
			updateThread.start();
			updateThreads.put(guildId, updateThread);
		}
	}
	
	// Set time on embed
	
	private static void setTime(long guildId, long time, long maxTime) {
		
		EmbedBuilder e = getEmbedBuilder(guildId);
		if (e == null) return;
		List<MessageEmbed.Field> fields = new ArrayList<MessageEmbed.Field>();
		fields.addAll(e.getFields());
		e.clearFields();
		MessageEmbed.Field cf = getFieldWithName(fields, "Time");
		if (cf != null)
			fields.remove(cf);
		fields.add(new MessageEmbed.Field("Time", Helper.convertTimeToString(time) + " / " + Helper.convertTimeToString(maxTime), false));
		
		for (MessageEmbed.Field field : fields) {
			e.addField(field);
		}
		setEmbed(guildId, e.build());
		Message msg = getMessage(guildId);
		if (msg == null) return;
		try {
			if (msg.getEmbeds().isEmpty()) {
				messages.remove(guildId);
				return;
			}
			msg.editMessageEmbeds(e.build()).queue(response -> {
				setMessage(guildId, response);
			});
		} catch (Exception ex) {
			messages.remove(guildId);
		}
	}
	
	// Handle reactions when track added or removed
	
	public static void handleButtons(long guildId) {
		
		GuildMusicManager guildMusicManager = AudioManager.get().getGuildMusicManager(Main.bot.getGuildById(guildId));
		boolean hasTrack = guildMusicManager.isPlaying();

		Message	msg = getMessage(guildId);
		if (msg == null) return;
		try {
			msg.clearReactions().complete();
		} catch (Exception e) {}
		
		Bot.AudioManager manager = Bot.AudioManager.get();
    	GuildMusicManager musicManager = manager.getGuildMusicManager(Main.bot.getGuildById(guildId));
		boolean paused = musicManager.player.isPaused();
		
		Collection<Component> comps = new ArrayList<Component>();
		
		if (hasTrack) {
			if (!paused)
				comps.add(Button.secondary(guildId + "pause", Emoji.fromUnicode("U+23F8")));
			else
				comps.add(Button.secondary(guildId + "continue", Emoji.fromUnicode("U+25B6")));
			if (musicManager.scheduler.getQueue().size() != 0) {
				comps.add(Button.secondary(guildId + "skip", Emoji.fromUnicode("U+23E9")));
			}
		}
		if (comps.size() > 0)
			msg.editMessage(getMessage(guildId)).setActionRows(ActionRow.of(comps)).queue();
		else
			msg.editMessage(getMessage(guildId)).setActionRows().queue();
	}
	
	// Set track on embed
	
	public static void setTrack(long guildId, @Nullable AudioTrack track) {
		boolean isPlaying = track != null;
		handleUpdate(guildId, isPlaying); // Start or stop tracking time
		EmbedBuilder e = getEmbedBuilder(guildId);
		if (e == null) return;
		List<MessageEmbed.Field> fields = new ArrayList<MessageEmbed.Field>();
		fields.addAll(e.getFields());
		e.clearFields();
		
		// Empty the board
		
		List<String> removableFieldsbyName = Arrays.asList(new String[] {
				"Currently playing",
				"Url",
				"Upcoming",
				"Time"
				});
		for (String fieldName : removableFieldsbyName) {
			MessageEmbed.Field currentField = getFieldWithName(fields, fieldName);
			if (currentField != null) {
				fields.remove(currentField);
			}
		}
		
		// Build upcoming
		
		String upcoming = "";
		
    	List<AudioTrack> queue = new ArrayList<AudioTrack>(Bot.AudioManager.get().getGuildMusicManager(Main.bot.getGuildById(guildId)).scheduler.getQueue());
		if (queue.size() != 0) {
			upcoming = queue.get(0).getInfo().title;
		}
		
		// Fill the board
		
		if (isPlaying) {
			String durationStr = Helper.convertTimeToString(track.getDuration());
			fields.add(new MessageEmbed.Field("Currently playing", track.getInfo().title + " ", false));
			fields.add(new MessageEmbed.Field("Url", track.getInfo().uri + " ", false));
			if (!upcoming.equals("") && upcoming != null)
				fields.add(new MessageEmbed.Field("Upcoming", upcoming + " ", false));
			fields.add(new MessageEmbed.Field("Time", durationStr + " ", false));
		}
		
		// Add board to embed
		
		for (MessageEmbed.Field field : fields) {
			if (field != null)
				e.addField(field);
		}
		
		var built = e.build();
		setEmbed(guildId, built); // Save embed
		
		// Edit message
		
		Message msg = getMessage(guildId);
		if (msg == null) return;
		
		try {
			msg.editMessageEmbeds(built).queue(response -> {
				setMessage(guildId, response);
				handleButtons(guildId);
			});
		} catch (Exception ex) {
			messages.remove(guildId);
		}
	}
	
	// Go through a list of fields, and return if the name is what we're looking for
	
	private static MessageEmbed.Field getFieldWithName(List<MessageEmbed.Field> fields, String title) {
		for (MessageEmbed.Field field : fields) {
			if (field == null) return null;
			if (field.getName().equals(title)) return field;
		}
		return null;
	}
}
