package Bot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import Bot.music.GuildMusicManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.managers.AudioManager;

public class Commands {
	
	// Handle incoming commands
	
	public static void handleCommand(MessageReceivedEvent event, String command, List<String> args) {
		if (command.equals("enable"))
			enableCommand(event);
		else if (ChatManager.getDisabledChannels().contains(event.getTextChannel().getIdLong())) {
        	event.getMessage().delete().submit();
			return;
		}
    	else if (command.equals("ping"))
            pingCommand(event);
        else if (command.equals("play") || command.equals("p"))
        	playCommand(event, args);
        else if (command.equals("first") || command.equals("playfirst") || command.equals("pfirst"))
        	firstCommand(event, args);
        else if (command.equals("h") || command.equals("help"))
        	helpCommand(event);
        else if (command.equals("fs") || command.equals("s") || command.equals("skip"))
        	skipCommand(event, args);
        else if (command.equals("pause"))
        	pauseCommand(event);
        else if (command.equals("continue"))
        	continueCommand(event);
        else if (command.equals("volume") || command.equals("vol"))
        	return;//Commands.volumeCommand(event, args); --> TODO Grief safety
        else if (command.equals("remove"))
        	removeCommand(event, args);
        else if (command.equals("queue") || command.equals("q"))
        	queueCommand(event);
        else if (command.equals("sweep"))
        	sweepCommand(event, args);
        else if (command.equals("init"))
        	initCommand(event, args);
        else if (command.equals("disable"))
        	disableCommand(event);
        else if (command.equals("disconnect") || command.equals("dc"))
        	disconnectCommand(event);
        else if (command.equals("shuffle"))
        	shuffleCommand(event);
        else if (command.equals("bass"))
        	bassCommand(event, args);
        else if (command.equals("nightcore") || command.equals("nc"))
        	nightcoreCommand(event);
        else if (command.equals("clear"))
        	clearCommand(event);
        else if (command.equals("loop"))
        	loopCommand(event);
	}
	
	// Ping
	
	public static void pingCommand(MessageReceivedEvent event) {
		MessageChannel channel = event.getChannel();
		long time = System.currentTimeMillis();
        channel.sendTyping().queue();
        channel.sendMessage("Pong!")
               .queue(response -> {
                   response.editMessageFormat("Pong: `%d ms`", System.currentTimeMillis() - time).queue();
               });
    }
	
	// Help
	
	public static void helpCommand(MessageReceivedEvent event) {
    	TextChannel channel = event.getTextChannel();
    	EmbedBuilder e1 = new EmbedBuilder();
    	
    	e1.setTitle("List of commands");
    	// 1.0.0
    	e1.addField(Listener.prefix + "play <link | search arguments> *(Alias: " + Listener.prefix + "p)*", "Plays or continues music depending on given arguments.", false);
    	e1.addField(Listener.prefix + "skip*(Aliases: " + Listener.prefix  + "s " + Listener.prefix + "fs)*", "Skips current music.", false);
    	e1.addField(Listener.prefix + "skip <amount>*(Aliases: " + Listener.prefix + "s " + Listener.prefix  + "fs)*", "Skips *amount* music.", false);
    	e1.addField(Listener.prefix + "pause", "Pauses current music.", false);
    	e1.addField(Listener.prefix + "continue *(Aliases: " + Listener.prefix  + "play " + Listener.prefix + "p)*", "Continues current music.", false);
    	e1.addField(Listener.prefix + "remove <number>", "Removes the last *number* tracks from the queue.", false);
    	e1.addField(Listener.prefix + "queue *(Alias: " + Listener.prefix  + "q)*", "Lists the current tracks in queue.", false);
    	e1.addField(Listener.prefix + "sweep <duration>", "Sweeps *duration* to current music. You can either sweep backward and forwards by changing the prefix.", false);
    	// 2.0.0
    	e1.addField(Listener.prefix + "bass", "Applies bass boost to music player.", false);
    	e1.addField(Listener.prefix + "nightcore *(Alias: " + Listener.prefix + "nc)*", "Applies nightcore effect to music player.", false);
    	e1.addField(Listener.prefix + "clear", "Clears all effects from music player.", false);
    	e1.addField(Listener.prefix + "disconnect *(Alias: " + Listener.prefix + "dc)*", "Disconnects the bot from voice channel.", false);
    	e1.addField(Listener.prefix + "shuffle", "Mixes the queue.", false);
    	
    	Member selfMember = event.getGuild().getSelfMember();
    	e1.setColor(selfMember.getColor());
    	
    	try {
    		sendEmbed(channel, e1.build());
    	} catch (Exception ex) {}
    }
	
	// Play
	
	public static void playCommand(MessageReceivedEvent event, List<String> args) {
		if (args.size() == 0) {
			continueCommand(event);
			return;
		}
    	AudioManager audio = event.getGuild().getAudioManager();
    	GuildVoiceState memberVoiceState = event.getMember().getVoiceState();
    	TextChannel txtChannel = event.getTextChannel();
    	if (!memberVoiceState.inVoiceChannel()) {
    		ChatManager.sendErrorMessageWithEmbedWithUser(txtChannel, event.getAuthor(), "Please join a voice channel first!");
    		return;
    	}
    	VoiceChannel vc = memberVoiceState.getChannel();
    	Member selfMember = event.getGuild().getSelfMember();
    	
    	if (!selfMember.hasPermission(vc, Permission.VOICE_CONNECT)) {
    		ChatManager.sendErrorMessageWithEmbedWithUser(txtChannel, event.getAuthor(), "I don't have the permission to join your voice channel!");
    		return;
    	}
    	
    	audio.openAudioConnection(vc);
    	
    	Bot.AudioManager manager = Bot.AudioManager.get();
    	
		manager.loadAndPlay(event.getTextChannel(), args, event.getAuthor(), false);
    	manager.getGuildMusicManager(event.getGuild()).player.setVolume(Listener.targetVolume);
    }
	
	// First
	
		public static void firstCommand(MessageReceivedEvent event, List<String> args) {
			if (args.size() == 0) {
				continueCommand(event);
				return;
			}
	    	AudioManager audio = event.getGuild().getAudioManager();
	    	GuildVoiceState memberVoiceState = event.getMember().getVoiceState();
	    	TextChannel txtChannel = event.getTextChannel();
	    	if (!memberVoiceState.inVoiceChannel()) {
	    		ChatManager.sendErrorMessageWithEmbedWithUser(txtChannel, event.getAuthor(), "Please join a voice channel first!");
	    		return;
	    	}
	    	VoiceChannel vc = memberVoiceState.getChannel();
	    	Member selfMember = event.getGuild().getSelfMember();
	    	
	    	if (!selfMember.hasPermission(vc, Permission.VOICE_CONNECT)) {
	    		ChatManager.sendErrorMessageWithEmbedWithUser(txtChannel, event.getAuthor(), "I don't have the permission to join your voice channel!");
	    		return;
	    	}
	    	
	    	audio.openAudioConnection(vc);
	    	
	    	Bot.AudioManager manager = Bot.AudioManager.get();
	    	
			manager.loadAndPlay(event.getTextChannel(), args, event.getAuthor(), true);
	    	manager.getGuildMusicManager(event.getGuild()).player.setVolume(Listener.targetVolume);
	    }
		
	// Skip
	
	public static void skipCommand(MessageReceivedEvent event, List<String> args) {
    	Bot.AudioManager manager = Bot.AudioManager.get();
    	GuildMusicManager musicManager = manager.getGuildMusicManager(event.getGuild());
    	TextChannel txtChannel = event.getTextChannel();
    	
    	if (musicManager.scheduler.getCurrentTrack() == null) {
    		ChatManager.sendErrorMessageWithEmbedWithUser(txtChannel, event.getAuthor(), "Currently there is no track playing!");
    		return;
    	}
    	
		if (args.size() == 0) {
			ChatManager.sendMessageWithEmbedWithUser(txtChannel, event.getAuthor(), "Skipping current track...");
			musicManager.player.stopTrack();
			musicManager.player.setPaused(false);
			musicManager.scheduler.nextTrack();
			return;
		}
		if (!Helper.isInt(args.get(0))) {
			ChatManager.sendErrorMessageWithEmbedWithUser(txtChannel, event.getAuthor(), "Please provide a valid number!");
			return;
		}
		int amount = Integer.parseInt(args.get(0));
		
		if (amount < 0) {
			ChatManager.sendErrorMessageWithEmbedWithUser(txtChannel, event.getAuthor(), "The amount must be atleast 0!");
			return;
		}
		
		int maxSkippable = musicManager.scheduler.getQueue().size();
		if (musicManager.scheduler.getCurrentTrack() != null)
			maxSkippable += 1;
		
		musicManager.player.stopTrack();
		musicManager.player.setPaused(false);
		
		if (maxSkippable < amount)
			amount = maxSkippable;
		
		
		for (int i = 0; i < amount - 1; i++) {
			musicManager.scheduler.removeFirst();
		}
		musicManager.scheduler.nextTrack();
		if (amount > 1)
			ChatManager.sendMessageWithEmbedWithUser(txtChannel, event.getAuthor(), "Skipping " + amount + " songs!");
		else
			ChatManager.sendMessageWithEmbedWithUser(txtChannel, event.getAuthor(), "Skipping the current song!");
    	return;
    }
	
	// Pause
	
	public static void pauseCommand(MessageReceivedEvent event) {
    	Bot.AudioManager manager = Bot.AudioManager.get();
    	GuildMusicManager musicManager = manager.getGuildMusicManager(event.getGuild());
    	TextChannel txtChannel = event.getTextChannel();
    	
    	if(musicManager.player.getPlayingTrack() == null) {
    		ChatManager.sendErrorMessageWithEmbedWithUser(txtChannel, event.getAuthor(), "Currently there is no song on track!");
    		return;
    	}
    	ChatManager.sendMessageWithEmbedWithUser(txtChannel, event.getAuthor(), "Pausing the current track!");
    	musicManager.player.setPaused(true);
    	TimeOutHandler.scheduleTimeOut(Main.bot.getGuildById(event.getGuild().getIdLong()));
    }
	
	// Continue
	
	public static void continueCommand(MessageReceivedEvent event) {
    	Bot.AudioManager manager = Bot.AudioManager.get();
    	GuildMusicManager musicManager = manager.getGuildMusicManager(event.getGuild());
    	TextChannel txtChannel = event.getTextChannel();
    	
    	if(musicManager.player.getPlayingTrack() == null) {
    		ChatManager.sendErrorMessageWithEmbedWithUser(txtChannel, event.getAuthor(), "Currently there is no song on track!");
    		return;
    	}
    	ChatManager.sendMessageWithEmbedWithUser(txtChannel, event.getAuthor(), "Continuing current track!");
    	musicManager.player.setPaused(false);
    	TimeOutHandler.cancelTimeOut(Main.bot.getGuildById(event.getGuild().getIdLong()));
    }
	
	// Volume
	
    public static void volumeCommand(MessageReceivedEvent event, List<String> args) {
    	TextChannel txtChannel = event.getTextChannel();
		if (args.size() == 0) {
			ChatManager.sendErrorMessageWithEmbedWithUser(txtChannel, event.getAuthor(), "Please provide the arguments!");
			return;
		}
		if (!Helper.isInt(args.get(0))) {
			ChatManager.sendErrorMessageWithEmbedWithUser(txtChannel, event.getAuthor(), "Please provide a valid number!");
			return;
		}
		int amount = Integer.parseInt(args.get(0));
		if (amount < 0) {
			ChatManager.sendErrorMessageWithEmbedWithUser(txtChannel, event.getAuthor(), "The amount must be atleast 0!");
			return;
		}
		Bot.AudioManager.get().getGuildMusicManager(event.getGuild()).player.setVolume(amount);
    }
    
    // Sweep
    
    public static void sweepCommand(MessageReceivedEvent event, List<String> args) {
    	TextChannel txtChannel = event.getTextChannel();
		if (args.size() == 0) {
			ChatManager.sendErrorMessageWithEmbedWithUser(txtChannel, event.getAuthor(), "Please provide the arguments!");
			return;
		}
		if (!Helper.isInt(args.get(0))) {
			ChatManager.sendErrorMessageWithEmbedWithUser(txtChannel, event.getAuthor(), "Please provide a valid number!");
			return;
		}
		int duration = Integer.parseInt(args.get(0)) * 1000;
		Bot.AudioManager.get().sweep(txtChannel, duration, event.getAuthor());
    }
    
    // Remove
    
    public static void removeCommand(MessageReceivedEvent event, List<String> args) {
    	TextChannel txtChannel = event.getTextChannel();
    	var manager = Bot.AudioManager.get().getGuildMusicManager(event.getGuild());
    	
    	if (manager.scheduler.getQueue().size() == 0) {
    		ChatManager.sendErrorMessageWithEmbedWithUser(txtChannel, event.getAuthor(), "There are no tracks in queue!");
			return;
    	}
    	
		if (args.size() == 0) {
			ChatManager.sendMessageWithEmbedWithUser(txtChannel, event.getAuthor(), "Removing the last element from the queue!");
			manager.scheduler.removeFromQueue(1);
			return;
		}
		if (!Helper.isInt(args.get(0))) {
			ChatManager.sendErrorMessageWithEmbedWithUser(txtChannel, event.getAuthor(), "Please provide a valid number!");
			return;
		}
		int amount = Integer.parseInt(args.get(0));
		if (amount < 0) {
			ChatManager.sendErrorMessageWithEmbedWithUser(txtChannel, event.getAuthor(), "The amount must be atleast 0!");
			return;
		}
		int maxSkippable = manager.scheduler.getQueue().size();
		if (maxSkippable < amount)
			amount = maxSkippable;
		
		manager.scheduler.removeFromQueue(amount);
		
		ChatManager.sendMessageWithEmbedWithUser(txtChannel, event.getAuthor(), "Removing the last " + amount + " elements from the queue!");
    }
    
    // Queue
    
    public static void queueCommand(MessageReceivedEvent event) {
		List<AudioTrack> tracks = new ArrayList<AudioTrack>(Bot.AudioManager.get().getGuildMusicManager(event.getGuild()).scheduler.getQueue());
		AudioTrack currentTrack = Bot.AudioManager.get().getGuildMusicManager(event.getGuild()).scheduler.getCurrentTrack();
		
    	TextChannel channel = event.getTextChannel();
    	EmbedBuilder e = new EmbedBuilder();
    	
    	if (currentTrack != null)
    		e.addField("Currently playing" , currentTrack.getInfo().title, false);

    	Member selfMember = event.getGuild().getSelfMember();
    	e.setColor(selfMember.getColor());
		
    	
		if (tracks.size() == 0) {
			e.addField("Current queue", "The queue is empty!", false);
		} else {
			String msg = "";
			for (int i = 0; i < tracks.size(); i++) {
				String addon = (i+1) + ". " + tracks.get(i).getInfo().title + "\n";
				if ((addon + msg).length() > 1024)
					break;
				msg += addon;
			}
			e.addField("Current queue", msg, false);
			e.setFooter("Currently there are " + tracks.size() + " songs in queue!");
		}
		sendEmbed(channel, e.build());
    }
    
    // Init
    
    public static void initCommand(MessageReceivedEvent event, List<String> args) {
    	TextChannel txtChannel = event.getTextChannel();
		long guildId = event.getGuild().getIdLong();
		
		// Check if user has administrator
		
		if (!event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
			ChatManager.sendErrorMessageWithEmbedWithUser(txtChannel, event.getAuthor(), "You dont have permission to use this command!");
			return;
		}
		
		// Purge channel message
		
		ChatManager.purgeChannel(txtChannel);
    	
    	Member selfMember = event.getGuild().getSelfMember();
    	AudioTrack currentTrack = Bot.AudioManager.get().getGuildMusicManager(event.getGuild()).scheduler.getCurrentTrack();
    	
    	// Setting up embed
    	
    	EmbedBuilder e = new EmbedBuilder();
    	e.setTitle("Music interface");
    	e.setColor(selfMember.getColor());
		MessageEmbed embed = e.build();
		
		// Saving embed
		
		InitManager.setEmbedBuilder(guildId, e);
		InitManager.setEmbed(guildId, embed);
		InitManager.setInitChannel(event.getGuild(), txtChannel);
    	
		// Sending embed
		
    	txtChannel.sendMessageEmbeds(embed).queue(message -> {
    		InitManager.setMessage(guildId, message);
    		InitManager.setTrack(guildId, currentTrack);
    	});
    }
    
    // Disable
    
    public static void disableCommand(MessageReceivedEvent event) {
    	
    	TextChannel txtChannel = event.getTextChannel();
    	
    	// Check if user has administrator
		
		if (!event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
			ChatManager.sendErrorMessageWithEmbedWithUser(txtChannel, event.getAuthor(), "You dont have permission to use this command!");
			return;
		}
		
    	ChatManager.addDisabledChannel(txtChannel.getIdLong());
    	ChatManager.sendMessageWithEmbedWithUser(txtChannel, event.getAuthor(), "Not listening for commands in this channel anymore!");
    }
    
    // Enable
    
    public static void enableCommand(MessageReceivedEvent event) {
    	
    	TextChannel txtChannel = event.getTextChannel();
    	
    	// Check if user has administrator
		
		if (!event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
			ChatManager.sendErrorMessageWithEmbedWithUser(txtChannel, event.getAuthor(), "You dont have permission to use this command!");
			return;
		}
		
    	if (!ChatManager.getDisabledChannels().contains(txtChannel.getIdLong())) {
    		ChatManager.sendErrorMessageWithEmbedWithUser(txtChannel, event.getAuthor(), "This channel is not disabled for me!");
    		return;
    	}
    	ChatManager.removeDisabledChannel(txtChannel.getIdLong());
    	ChatManager.sendMessageWithEmbedWithUser(txtChannel, event.getAuthor(), "Started listening on this channel!");
    }
    
    // Disconnect
    
    public static void disconnectCommand(MessageReceivedEvent event) {
    	AudioManager audio = event.getGuild().getAudioManager();
    	GuildVoiceState memberVoiceState = event.getMember().getVoiceState();
    	
    	if (memberVoiceState.inVoiceChannel()) {
    		audio.closeAudioConnection();
    	}
    }
    
    // Shuffle
    
    public static void shuffleCommand(MessageReceivedEvent event) {
    	TextChannel txtChannel = event.getTextChannel();
    	GuildMusicManager guildMusicManager = Bot.AudioManager.get().getGuildMusicManager(event.getGuild());
    	List<Object> queue = Arrays.asList(guildMusicManager.scheduler.getQueue().toArray());
    	ChatManager.sendMessageWithEmbedWithUser(txtChannel, event.getAuthor(), "Mixing tracks");
    	Collections.shuffle(queue);
    	guildMusicManager.scheduler.purge();
    	for (Object trackObj : queue) {
    		guildMusicManager.scheduler.queue((AudioTrack) trackObj);
    	}
    	
    }
    
    // Bass
    
    public static void bassCommand(MessageReceivedEvent event, List<String> args) {
    	TextChannel txtChannel = event.getTextChannel();
    	Integer amount = null;
    	if (args.size() > 0) {
    		if (Helper.isInt(args.get(0))) {
    			amount = Integer.valueOf(args.get(0));
    		}
    	}
    	FilterManager.applyFilter(txtChannel, FilterType.BASS, amount);
		ChatManager.sendMessageWithEmbedWithUser(txtChannel, event.getAuthor(), "Boosting bass");
    }
    
    // Nightcore
    
    public static void nightcoreCommand(MessageReceivedEvent event) {
    	TextChannel txtChannel = event.getTextChannel();
    	FilterManager.applyFilter(txtChannel, FilterType.NIGHTCORE, null);
		ChatManager.sendMessageWithEmbedWithUser(txtChannel, event.getAuthor(), "Applying nightcore");
    }
    
    // Loop
    
    public static void loopCommand(MessageReceivedEvent event) {
    	TextChannel txtChannel = event.getTextChannel();
    	if (Bot.AudioManager.get().getGuildMusicManager(event.getGuild()).scheduler.toggleLoop()) {
    		ChatManager.sendMessageWithEmbedWithUser(txtChannel, event.getAuthor(), "Looping has been enabled!");
    	} else {
    		ChatManager.sendMessageWithEmbedWithUser(txtChannel, event.getAuthor(), "Looping has been disabled!");
    	}
    }
    
    // Clear
    
    public static void clearCommand(MessageReceivedEvent event) {
    	TextChannel txtChannel = event.getTextChannel();
    	FilterManager.applyFilter(txtChannel, FilterType.CLEAR, null);
		ChatManager.sendMessageWithEmbedWithUser(txtChannel, event.getAuthor(), "Clearing filters");
    }
    
    // Sending embed to text channel
    
    public static void sendEmbed(TextChannel channel, MessageEmbed msg) {
    	channel.sendTyping().queue();
    	channel.sendMessageEmbeds(msg).queue();
    }
}
