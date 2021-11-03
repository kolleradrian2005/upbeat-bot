package Bot;

import java.util.List;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

public class DiscordBot {
	
	private JDA jda;
	private String token;
	
	// Constructor
	
	public DiscordBot(String token) {
		setToken(token);
		try {
			initBot();
		} catch (LoginException e) {
			print("Authorization error. Please provide a valid token in config.json then reload the bot!");
			return;
		}
		start();
	}
	
	// Reloading connection
	
	public DiscordBot reload() {
		stop();
		try {
			initBot();
		} catch (LoginException e) {
			print("Authorization error. Please provide a valid token in config.json then reload the bot!");
			return null;
		}
		start();
		return this;
	}
	
	// Starting connection
	
	void start() {
		for (Object listener : jda.getRegisteredListeners()) {
			jda.removeEventListener(listener);
		}
		jda.addEventListener(new Listener());
		jda.getPresence().setStatus(OnlineStatus.ONLINE);
		jda.getPresence().setActivity(Activity.listening("Music"));
	}
	
	// Logging in
	
	void initBot() throws LoginException {
		jda = JDABuilder.createDefault(token).build();
	}
	
	// Stopping connection
	
	void stop() {
		print("Shutting down!");
		if (jda == null) return;
		for (Object listener : jda.getRegisteredListeners()) {
			jda.removeEventListener(listener);
		}
		jda.getPresence().setStatus(OnlineStatus.OFFLINE);
		jda.shutdown();
		jda = null;
	}
	
	// Setting token
	
	public DiscordBot setToken(String token) {
		this.token = token;
		return this;
	}

	// Setting prefix
	
	public DiscordBot setPrefix(String prefix) {
		Listener.prefix = prefix;
		return this;
	}
	
	// Logging

	private void print(Object msg) {
		System.out.println(References.botPrefix + msg);
	}

	public Guild getGuildById(long id) {
		return jda.getGuildById(id);
	}
	
	public User getSelf() {
		return jda.getSelfUser();
	}
	
	public List<Guild> getGuilds() {
		return jda.getGuilds();
	}

	public TextChannel getTextChannel(long id) {
		return jda.getTextChannelById(id);
	}

	public Emote getEmote(String unicode) {
		return null;
	}	
}
