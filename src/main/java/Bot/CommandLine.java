package Bot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
				System.out.println("Stopping bot...");
				System.exit(0);
				return;
			case "help":
				print("");
				print("--->");
				print("");
				print("Here are the following commands:");
				print("");
				print("stop              - Terminates the bot.");
				print("volume <amount>   - Sets the volume of the bot on all servers.");
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
			default:
				System.out.println("Unknown command");
				return;
		}
	}
	
	// Print text to command line
	
	public void print(String msg) {
		System.out.println(msg);
	}
}
