package Bot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

public class Config {
	
	private File file;
	private JSONObject jo;
	String fileName = "config.json"; // Add path if needed, default path is by the jar file.
	
	// Constructor
	
	Config() {
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
	
	// Creating file
	
	private void createConfigFile(File file) throws IOException {
    	file.createNewFile();
    	FileWriter f = new FileWriter(fileName);
    	f.write("{}");
		f.close();
	}
	
	// Initializing the file
	
	private void setup() throws IOException, URISyntaxException, InterruptedException {
		file = new File(fileName);
	    if (!file.exists()) 
	    	createConfigFile(file);
	    
	    InputStream is = new FileInputStream(fileName);
        String jsonTxt = IOUtils.toString(is, "UTF-8");
        is.close();
        if (jsonTxt.length() < 2 ||
        		!jsonTxt.substring(0, 1).equals("{") ||
        		!jsonTxt.substring(jsonTxt.length() - 1, jsonTxt.length()).equals("}"))
        	createConfigFile(file);
       
        is = new FileInputStream(fileName);
        jsonTxt = IOUtils.toString(is, "UTF-8");
        is.close();
	    jo = new JSONObject(jsonTxt);
	    loadDefaults();
	}
	
	// Loading default configurations
	
	private void loadDefaults() {
		if (!jo.has("token")) {
			jo.put("token", "");
		}
		if (!jo.has("prefix")) {
			jo.put("prefix", ".");
		}
		if (!jo.has("clientId")) {
			jo.put("clientId", "");
		}
		if (!jo.has("clientSecret")) {
			jo.put("clientSecret", "");
		}
		save();
	}
	private String get(String key) {
		if (!jo.has(key))
			return "";
		return jo.getString(key);
		
	}
	
	// Saving data to file
	
	@SuppressWarnings("unused")
	private void set(String key, String value) {
		jo.put(key, value);
		save();
	}
	
	// Saving file
	
	private void save() {
		try {
			FileWriter file = new FileWriter(fileName);
			file.write(jo.toString());
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	// Get token from file
	
	public String getToken() {
		return get("token");
	}
	
	// Get prefix from file
	
	public String getPrefix() {
		return get("prefix");
	}
	
	// Get clientId from file
	
	public String getClienId() {
		return get("clientId");
	}
	
	// Get clientSecret from file
	
	public String getClientSecret() {
		return get("clientSecret");
	}
}
