package Bot.Server;

import static spark.Spark.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import Bot.AudioManager;
import Bot.Main;
import net.dv8tion.jda.api.entities.Guild;

public class WebServer {
	
	// Build JSON from Map
	
	private static String buildJSON(HashMap<String, Object> content) {
		
		JSONObject json = new JSONObject();
		for (int i = 0; i < content.size(); i++) {
			String key = (String) content.keySet().toArray()[i];
			Object value = content.values().toArray()[i];
			json.put(key, value);
			
		}
		return json.toString();
	}
	public static void setupServer() {
		// Set folder for webserver
		staticFileLocation("/public");
		// Set port to 8666
		port(8666);
		// Enable CORS to enable fetching from flutter web application
		enableCORS("*", "*", "*");
		print("Setting up webserver");
		
		// Website
		
		get("/", (req, res) -> {
			res.type("text/html");
			res.redirect("index.html");
			return null;
		});
		
		// Status
		
		get("/status", (req, res) -> {
			HashMap<String, Object> content = new HashMap<String, Object>();
			content.put("statuscode", 200);
			content.put("message", "OK");
			return buildJSON(content);
		});
		
		// Login
		
		get("/login", (req, res)->{
			String username = req.queryParams("username");
			String password = req.queryParams("password");
			
			HashMap<String, Object> content = new HashMap<String, Object>();
			
			if (username == null || password == null || username.equals("") || password.equals("")) {
				content.put("statuscode", 404);
				content.put("message", "Did not supply proper credentials");
				return buildJSON(content);
			}
			ArrayList<Object> result = AuthManager.login(username, password);

			if ((boolean) result.get(0)) {
				content.put("statuscode", 200);
				content.put("message", "Accepted");
				content.put("cookie", (String) result.get(1));
				return buildJSON(content);
			}
			
			content.put("statuscode", 666);
			content.put("message", "Supplied invalid credentials");
			
			return buildJSON(content);
		});
		
		// Authorize cookie
		
		get("/checkcookie", (req, res)->{
			String username = req.queryParams("username");
			String cookie = req.queryParams("cookie");
			
			HashMap<String, Object> content = new HashMap<String, Object>();
			
			if (username == null || cookie == null || username.equals("") || cookie.equals("")) {
				content.put("statuscode", 404);
				content.put("message", "Did not supply proper credentials");
				return buildJSON(content);
			}
			ArrayList<Object> result = AuthManager.checkCookie(username, cookie);

			if ((boolean) result.get(0)) {
				content.put("statuscode", 200);
				content.put("message", "Accepted");
				return buildJSON(content);
			}
			
			content.put("statuscode", 666);
			content.put("message", "Supplied invalid cookie");
			
			return buildJSON(content);
		});
		
		// Get guilds of the bot
		
		get("/guilds", (req, res)->{
			
			HashMap<String, Object> content = new HashMap<String, Object>();
			
			List<Guild> guilds = Main.bot.getGuilds();
			List<List<Object>> guildNames = new ArrayList<List<Object>>();
			
			for (Guild guild : guilds) {
				List<Object> addable = new ArrayList<Object>(){};
				addable.add(guild.getName());
				addable.add(guild.getId());
				guildNames.add(addable);
			}
			content.put("statuscode", 200);
			content.put("guilds", guildNames);
			
			return buildJSON(content);
		});
		
		// Get information about the audio manager of the guild
		
		get("/info", (req, res)->{
			
			String guildid = req.queryParams("guildid");
			
			HashMap<String, Object> content = new HashMap<String, Object>();
			
			if (guildid == null || guildid.equals("")) {
				content.put("statuscode", 404);
				content.put("message", "Did not supply guildid");
				return buildJSON(content);
			}

			Guild guild = Main.bot.getGuildById(Long.parseLong(guildid));
			if (guild == null) {
				content.put("statuscode", 404);
				content.put("message", "Invalid guildid!");
				return buildJSON(content);
			}
			AudioManager manager = AudioManager.get();
			
			boolean playing = manager.getGuildMusicManager(guild).scheduler.getCurrentTrack() != null;
			content.put("statuscode", 200);
			content.put("playing", playing);
			if (playing) {
				content.put("title", manager.getGuildMusicManager(guild).scheduler.getCurrentTrack().getInfo().title);
				content.put("position", manager.getGuildMusicManager(guild).scheduler.getCurrentTrack().getPosition());
				content.put("duration", manager.getGuildMusicManager(guild).scheduler.getCurrentTrack().getDuration());
				content.put("paused", manager.getGuildMusicManager(guild).player.isPaused());
			}
			return buildJSON(content);
		});
		
		// Continue current track of the audio of the guild
		
		get("/continue", (req, res)->{
			
			String guildid = req.queryParams("guildid");
			
			HashMap<String, Object> content = new HashMap<String, Object>();
			
			if (guildid == null || guildid.equals("")) {
				content.put("statuscode", 404);
				content.put("message", "Did not supply guildid");
				return buildJSON(content);
			}
			if (guildid == null || guildid.equals("")) {
				content.put("statuscode", 404);
				content.put("message", "Did not supply guildid");
				return buildJSON(content);
			}
			Guild guild = Main.bot.getGuildById(Long.parseLong(guildid));
			if (guild == null) {
				content.put("statuscode", 404);
				content.put("message", "Invalid guildid!");
				return buildJSON(content);
			}
			AudioManager manager = AudioManager.get();
			
			manager.getGuildMusicManager(guild).player.setPaused(false);
			content.put("statuscode", 200);
			content.put("message", "Continued!");
			
			return buildJSON(content);
		});
		
		// Pause current track of the audio of the guild
		
		get("/pause", (req, res)->{
			
			String guildid = req.queryParams("guildid");
			
			HashMap<String, Object> content = new HashMap<String, Object>();
			
			if (guildid == null || guildid.equals("")) {
				content.put("statuscode", 404);
				content.put("message", "Did not supply guildid");
				return buildJSON(content);
			}
			if (guildid == null || guildid.equals("")) {
				content.put("statuscode", 404);
				content.put("message", "Did not supply guildid");
				return buildJSON(content);
			}
			Guild guild = Main.bot.getGuildById(Long.parseLong(guildid));
			if (guild == null) {
				content.put("statuscode", 404);
				content.put("message", "Invalid guildid!");
				return buildJSON(content);
			}
			AudioManager manager = AudioManager.get();
			
			manager.getGuildMusicManager(guild).player.setPaused(true);
			content.put("statuscode", 200);
			content.put("message", "Paused!");
			
			return buildJSON(content);
		});
	}
	
	// Apply SSL certification
	
	public static void secureServer() {
		Path certPath = Paths.get("cert");
		try {
			Files.createDirectories(certPath);
		} catch (IOException e) {}
		
		String keystoreFilePath = certPath.toString() + "\\keystore.jks";
		String passwordFilePath = certPath.toString() + "\\password.txt";
		String readmeFilePathString = certPath.toString() + "\\readme.txt";
		
		print("Searching for certificate: " + keystoreFilePath);
		print("Searching for password: " + passwordFilePath);
		
		File keystoreFile = new File(keystoreFilePath);
		File passwordFile = new File(passwordFilePath);
		File readmeFile = new File(readmeFilePathString);
		
		if (!readmeFile.exists()) {
			try {
				readmeFile.createNewFile();
			} catch (IOException e) {}
		}
	    try {
	    	String[] lines = new String[] {
    			"READ ME IF YOU WANT TO ACTIVATE SSL CERTIFICATE",
    			"",
    			"1. Place your keystore file in /cert folder with the following name: keystore.jks",
    			"2. Place the password for the keystore file in /cert folder with the following name: password.txt",
    			"",
    			"(NOTE: The password.txt must contain only a String without enters.)"
	    	};
			RandomAccessFile writer = new RandomAccessFile(readmeFile, "w");
			for (String line : lines) {
				writer.writeBytes(line + "\n");
			}
			writer.close();
		} catch (Exception e) {}
		

		if (!keystoreFile.exists()) {
			print("Could not find keystore file!");
			return;
		}
		if (!passwordFile.exists()) {
			print("Could not find password file!");
			return;
		}
		String keystorePassword = "";
		
	    try {
			RandomAccessFile reader = new RandomAccessFile(passwordFile, "r");
			String line = reader.readLine();
			if (line != null && line != "") {
				keystorePassword = line;
			}
			else {
				print("Could not find password in password.txt!");
				reader.close();
				return;
			}
			reader.close();
		} catch (Exception e) {}
	    
		KeyStore ks;
		try {
			ks = KeyStore.getInstance(KeyStore.getDefaultType());
		} catch (KeyStoreException e) {
			e.printStackTrace();
			return;
		}

		try (FileInputStream fis = new FileInputStream(keystoreFile)) {
		    ks.load(fis, keystorePassword.toCharArray());
		} catch (NoSuchAlgorithmException | CertificateException | IOException e) {
			if (e instanceof IOException) {
				print("Invalid password for certificate!");
				return;
			}
		}

		try {
			secure(keystoreFilePath, keystorePassword, null, null);
			print("Successfully activated certificate!");
		} catch (Exception e) {
			print("Found invalid certificate!");
		}
	}
	
	// Enable CORS
	
	private static void enableCORS(final String origin, final String methods, final String headers) {

	    options("/*", (request, response) -> {

	        String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
	        if (accessControlRequestHeaders != null) {
	            response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
	        }

	        String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
	        if (accessControlRequestMethod != null) {
	            response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
	        }

	        return "OK";
	    });

	    before((request, response) -> {
	        response.header("Access-Control-Allow-Origin", origin);
	        response.header("Access-Control-Request-Method", methods);
	        response.header("Access-Control-Allow-Headers", headers);
	        // Note: this may or may not be necessary in your particular application
	        response.type("application/json");
	    });
	}
	
	// Log to console
	
	private static void print(Object msg) {
		System.out.println(msg);
	}
}
