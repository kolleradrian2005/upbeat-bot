package Bot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import Bot.music.GuildMusicManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Listener extends ListenerAdapter {
	
	static String prefix = "."; // Determines the prefix of commands
	public static int targetVolume = 20; // Determines default volume
	
	// Handle incoming messages
	
    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
    	
    	Message msg = event.getMessage();
        
        if (InitManager.getInitChannels().contains(event.getTextChannel()) && !InitManager.isInit(msg)) {
        	msg.delete().submit();
        }
        
        String content = msg.getContentRaw();
        
        if (!(content.length() > prefix.length()) || !content.substring(0, prefix.length()).equals(prefix)) return;
        
        List<String> args = new ArrayList<String>(Arrays.asList(content.split(" ")));
        String command = args.remove(0).substring(1);
       
        Commands.handleCommand(event, command, args);
    }
    
    // Handle incoming reactions on embed board
    
    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
    	MessageReaction react = event.getReaction();
    	Message msg = event.retrieveMessage().complete();
    	
        if (!InitManager.isInit(msg)) // Return if reaction is not on embed board
        	return;
        
        // Run command for each reaction if not sent by self
        
        if (event.getUser() != Main.bot.getSelf()) {
        	Commands.handleReactionCommand(event);
        }
        
        // Remove all reactions if not sent by self
        
    	List<User> users = react.retrieveUsers().complete();
    	for (User user : users) {
    		if (user != Main.bot.getSelf())
    			react.removeReaction(user).complete();
    	}
    }
    
    // Called when someone joins a voice channel
    
    @Override
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
    	if (!event.getMember().getUser().equals(Main.bot.getSelf())) return; // Return if not self
    	Guild guild = event.getGuild();
    	AudioManager manager = AudioManager.get();
    	if (manager.getGuildMusicManager(guild).scheduler.getCurrentTrack() == null);
    		TimeOutHandler.scheduleTimeOut(guild);
    }
    
    // Called when someone leaves a voice channel
    
    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
    	if (!event.getMember().getUser().equals(Main.bot.getSelf())) return; // Return if not self
    	Guild guild = event.getGuild();
    	TimeOutHandler.cancelTimeOut(guild);
    }
    
	// Handle clicked buttons
	
    @Override
    public void onButtonClick(ButtonClickEvent event)
    {
    	AudioManager manager = AudioManager.get();
    	String buttonId = event.getButton().getId();
    	if (buttonId.contains("continue")) {
    		Long guildId = Long.parseLong(buttonId.substring(0, buttonId.length()-8));
    		GuildMusicManager guildMusicManager = manager.getGuildMusicManager(Main.bot.getGuildById(guildId));
    		guildMusicManager.player.setPaused(false);
    		event.deferEdit().submit();
    		InitManager.handleEmotes(guildId, guildMusicManager.scheduler.getCurrentTrack() != null);
    	} else if (buttonId.contains("pause")) {
    		Long guildId = Long.parseLong(buttonId.substring(0, buttonId.length()-5));
    		GuildMusicManager guildMusicManager = manager.getGuildMusicManager(Main.bot.getGuildById(guildId));
    		guildMusicManager.player.setPaused(true);
    		event.deferEdit().submit();
    		InitManager.handleEmotes(guildId, guildMusicManager.scheduler.getCurrentTrack() != null);
    	} else if (buttonId.contains("skip")) {
    		Long guildId = Long.parseLong(buttonId.substring(0, buttonId.length()-4));
    		GuildMusicManager guildMusicManager = manager.getGuildMusicManager(Main.bot.getGuildById(guildId));
    		guildMusicManager.scheduler.nextTrack();
    		event.deferEdit().submit();
    		InitManager.handleEmotes(guildId, guildMusicManager.scheduler.getCurrentTrack() != null);
    	}
    }
    
	// Handle deleted messages
	
    @Override
    public void onMessageDelete(MessageDeleteEvent event)
    {
    	Message initMsg = InitManager.getMessage(event.getGuild().getIdLong());
    	if (initMsg != null && event.getMessageIdLong() == initMsg.getIdLong()) {
    		
    		InitManager.deleteMessage(event.getGuild().getIdLong());
    	}
    }
    
    // Say when the service is ready
    
	@Override
	public void onReady(ReadyEvent event) {
		Main.disabledchannels = new DisabledChannels();
		ChatManager.loadDisabledChannel();
		
		Main.initchannels = new InitChannels();
		InitManager.load();
		
		System.out.println("Prepare for trouble, make it double!");
	}
}
