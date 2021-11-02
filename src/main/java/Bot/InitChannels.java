package Bot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.apache.commons.io.IOUtils;

public class InitChannels {
	
	private File file;
	String fileName = "initchannels.txt"; // Add path if needed, default path is by the jar file.
	
	// Constructor
	
	InitChannels() {
		try {
			setup();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	// Initializing the file
	
	private void setup() throws IOException, URISyntaxException, InterruptedException {
		file = new File(fileName);
	    if (!file.exists()) 
	    	file.createNewFile();
	}
	
	// Get all init channels
	
	ArrayList<Long> getInitChannels() {
		ArrayList<Long> channels = new ArrayList<Long>();
		try {
			FileInputStream is = new FileInputStream(fileName);
			String txt = IOUtils.toString(is, "UTF-8");
			String counter = "";
			for (char c : txt.toCharArray()) {
				if (c == '\n') {
					channels.add(Long.parseLong(counter));
					counter = "";
				} else {
					counter += c;
				}
			}
		} catch (IOException e) {
			return channels;
		}
		return channels;
	}
	
	// Add a init channel to file
	
	void addInitChannel(long id) {
		try {
			FileWriter writer = new FileWriter(fileName);
			char[] chars = String.valueOf(id).toCharArray();
			for (char c : chars) {
				writer.append(c);
			}
			writer.append('\n');
			writer.close();
		} catch (IOException e) {
			return;
		}
		return;
	}
	
	// Remove a init channel from file
	
	void removeInitChannel(long id) {
		try {
		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));

		String lineToRemove = String.valueOf(id);
		String currentLine;

		while((currentLine = reader.readLine()) != null) {
		    // trim newline when comparing with lineToRemove
		    String trimmedLine = currentLine.trim();
		    if(trimmedLine.equals(lineToRemove)) continue;
		    writer.write(currentLine + System.getProperty("line.separator"));
		}
		writer.close(); 
		reader.close(); 
		} catch (Exception e) {
			
		}
	}
}
