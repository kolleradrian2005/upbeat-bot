package Bot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.entities.Guild;

public class CommandLine {
	
	// Constructor
	
	CommandLine() {
		start();
	}
	
	// Start listening on command line
	
	private void start() {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String input;
		try {
			input = reader.readLine();
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
		List<String> args = new ArrayList<String>(Arrays.asList(input.split(" ")));
		
        String command = args.remove(0);
       
		if (command != "" && command != " ")
			try {
				runCommand(command, args);
			} catch (Exception e) {}
		start();
	}
	
	// Run command
	
	private void runCommand(String command, List<String> args) {
		switch (command.toLowerCase()) {
			case "stop":
				print(References.botPrefix + "Stopping bot...");
				System.exit(0);
				return;
			case "help":
				print("");
				print("--->");
				print("");
				print("Here is the list of the commands:");
				print("");
				print("stop              - Terminates the bot.");
				print("volume <amount>   - Sets the volume of the bot on all servers.");
				print("reload            - Reloads the bot.");
				print("info              - Displays information about the music player (servers - tracks).");
				print("");
				print("--->");
				return;
			case "volume":
				if (args.size() == 0) {
					print("Please provide the arguments!");
					return;
				}
				if (!Helper.isInt(args.get(0))) {
					print("Please provide a valid number!");
					return;
				}
				int amount = Integer.parseInt(args.get(0));
				if (amount < 0) {
					print("The amount must be atleast 0!");
					return;
				}
				AudioManager.get().setAllVolume(amount);
				Listener.targetVolume = amount;
				return;
			case "reload":
				try {
					Main.reload();
				} catch (Exception e) {
					print("Failed to reload");
					e.printStackTrace();
				}
				return;
			case "info":
				print("Information about the music player: ");
				for (Guild guild : Main.bot.getGuilds()) {
					AudioTrack currentTrack = AudioManager.get().getGuildMusicManager(guild).player.getPlayingTrack();
					if (currentTrack == null)
						print("  " + guild.getName());
					else
						print("  " + guild.getName() + " - " + currentTrack.getInfo().title + " (" + Helper.convertTimeToString(currentTrack.getPosition()) + " / " + Helper.convertTimeToString(currentTrack.getDuration()) + ") " + (AudioManager.get().getGuildMusicManager(guild).player.isPaused() ? "PAUSED" : ""));
				}
				return;
			default:
				print("Unknown command");
				return;
		}
	}
	
	// Logging
	
	private void print(String msg) {
		System.out.println(msg);
	}
}
