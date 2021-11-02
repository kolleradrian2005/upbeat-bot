package Bot;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.RateLimitedException;

public class ChatManager {

	// Array for collecting channels, that shouldn't be listened by the bot
	private static ArrayList<Long> disabledChannels = new ArrayList<Long>();
	
	// Add disabled channel
	
	public static void addDisabledChannel(long id) {
		disabledChannels.add(id);
		Main.disabledchannels.addDisabledChannel(id);
	}
	
	// Remove disabled channel
	
	public static void removeDisabledChannel(long id) {
		disabledChannels.remove(id);
		Main.disabledchannels.removeDisabledChannel(id);
	}
	
	// Get current disabled channels
	
	@SuppressWarnings("unchecked")
	public static ArrayList<Long> getDisabledChannels() {
		return (ArrayList<Long>) disabledChannels.clone();
	}
	
	// Initialize disabled channels
	
	public static void loadDisabledChannel() {
		disabledChannels = Main.disabledchannels.getDisabledChannels();
	}
	
	// Send message in embed with user icon at the beginning
	
	public static void sendMessageWithEmbedWithUser(TextChannel channel, User user, String msg) {
		EmbedBuilder eb = new EmbedBuilder();
		Member selfMember = channel.getGuild().getSelfMember();
    	eb.setColor(selfMember.getColor());
		eb.setAuthor(" | " + msg, null, user.getEffectiveAvatarUrl());
		channel.sendTyping().queue();
		channel.sendMessageEmbeds(eb.build()).queue();;
	}
	
	// Send error message in embed with user icon at the beginning
	
	public static void sendErrorMessageWithEmbedWithUser(TextChannel channel, User user, String msg) {
		EmbedBuilder eb = new EmbedBuilder();
    	eb.setColor(Color.RED);
		eb.setAuthor(" | " + msg, null, user.getEffectiveAvatarUrl());
		channel.sendTyping().queue();
		channel.sendMessageEmbeds(eb.build()).queue();;
	}
	
	// Send message in embed with user icon at the beginning. It also sets footer of embed
	
	public static void sendMessageWithEmbedWithUserWithFooter(TextChannel channel, User user, String msg, String footer) {
		EmbedBuilder eb = new EmbedBuilder();
		Member selfMember = channel.getGuild().getSelfMember();
    	eb.setColor(selfMember.getColor());
		eb.setAuthor(" | " + msg, null, user.getEffectiveAvatarUrl());
		eb.setFooter(footer);
		channel.sendTyping().queue();
		channel.sendMessageEmbeds(eb.build()).queue();;
	}
	
	// Purges channel
	
	public static void purgeChannel(TextChannel txtChannel) {
		List<Message> messages;
		try {
			messages = txtChannel.getHistory().retrievePast(100).complete(true);
		} catch (RateLimitedException e) {
			return;
		}
    	if (messages.size() == 0) return;
    	if (messages.size() == 1)
    		messages.get(0).delete().submit();
		else if (!(messages.size() < 2)) {
    		try {
				txtChannel.deleteMessages(messages).complete(false);
			} catch (RateLimitedException e) {
				return;
			}
    		purgeChannel(txtChannel);
		}
	}
	
}
