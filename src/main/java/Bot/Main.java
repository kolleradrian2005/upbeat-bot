package Bot;

import javax.security.auth.login.LoginException;

import Bot.Server.WebServer;
import Bot.Server.Database.MySQL;
import Bot.Spotify.Spotify;
import Bot.YouTube.YouTubeAPI;
public class Main {
	
	public static DiscordBot bot;
	public static DisabledChannels disabledchannels;
	public static InitChannels initchannels;
	static Config config;
	static CommandLine cmd;
	static YouTubeAPI youtube;
	public static MySQL sql;
	
	// Main: Setting up configuration files, initializing Discord JDA, YouTube, Spotify, SQL, Webserver and starts command line
	
	public static void main(String[] args) throws LoginException, InterruptedException {
		//WebServer.secureServer();
		sql = new MySQL();
		sql.connect();
		
		config = new Config();
		Spotify.initApi(config.getClienId(), config.getClientSecret());
		youtube = new YouTubeAPI();
		
		bot = new DiscordBot(config.getToken())
				.setPrefix(config.getPrefix());

		try {
			youtube.initApi();
		} catch (Exception e) {
			e.printStackTrace();
		}

		WebServer.setupServer();

		cmd = new CommandLine();
	}
	
	// Reload: Re-read configuration files
	
	public static void reload() {
		config = new Config();
		if (bot == null) {
			bot = new DiscordBot(config.getToken())
				.setPrefix(config.getPrefix());
		}
		else {
			bot.setToken(config.getToken())
				.setPrefix(config.getPrefix())
				.reload();
		}
	}
}
