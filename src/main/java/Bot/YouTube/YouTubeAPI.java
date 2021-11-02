package Bot.YouTube;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

public class YouTubeAPI {
	
	private final String apiKeyLocation = "apiKey.txt";
    private String apiKey = "";
    
    // Initializing YouTube API: Reading apiKey from file
    
	public YouTubeAPI initApi() {
		File apiKeyFile = new File(apiKeyLocation);
		// Create file if doesn't exists
		if (!apiKeyFile.exists()) {
			try {
				apiKeyFile.createNewFile();
			} catch (IOException e) {
				System.out.println("Could not create file: " + apiKeyLocation);
				e.printStackTrace();
				return this;
			}
			System.out.println("Please privide a YouTube api key here: " + apiKeyLocation);
			return this;
		}
		// Read from file
		try {
			InputStream stream = new FileInputStream(apiKeyFile);
			String text = IOUtils.toString(stream, "UTF-8");
			//Check if the key matches Google YouTube API Key pattern
			String pattern = "AIza[0-9A-Za-z\\\\\\\\-_]{35}";
			if (text.matches(pattern)) {
				System.out.println("Found YouTube API key!");
				apiKey = text; // Save key to variable
				return this;
			} else {
				System.out.println("Please privide a valid YouTube api key here: " + apiKeyLocation);
				return this;
			}
		} catch (FileNotFoundException e) {
			System.out.println("Could not open file: " + apiKeyLocation);
			e.printStackTrace();
			return this;
		} catch (IOException e) {
			System.out.println("Could not read file: " + apiKeyLocation);
			e.printStackTrace();
		}
		return this;
	}
	
	// Searching with YouTube API
	
	public String search(String searchFor) throws Exception {
		if (apiKey == "") throw new Exception();
		searchFor = searchFor.replaceAll(" ","+");
		
		if (searchFor.substring(searchFor.length() - 1).equals("+")) {
			searchFor = searchFor.substring(0, searchFor.length() - 1);
		}
		String fetchUrl = "https://www.googleapis.com/youtube/v3/search?key=" + apiKey + "&q=" + searchFor;
		
		JSONObject resultJSON;
		resultJSON = readJsonFromUrl(fetchUrl);
		if (resultJSON == null) throw new Exception();
		String videoId = resultJSON
			.getJSONArray("items") // Get results
			.getJSONObject(0) // Get first results
			.getJSONObject("id") // Get id from first result
			.getString("videoId"); // get videoId from id of first result
		String url = "https://www.youtube.com/watch?v=" + videoId;
		return url;
	}
	
	// Read string from reader
	
	private static String readAll(Reader rd) throws IOException {
	    StringBuilder sb = new StringBuilder();
	    int cp;
	    while ((cp = rd.read()) != -1) {
	      sb.append((char) cp);
	    }
	    return sb.toString();
	  }
	
	// Fetch JSON from URL

	public static JSONObject readJsonFromUrl(String url) throws Exception {
		URL URL = new URL(url);
	    HttpURLConnection connection = (HttpURLConnection)URL.openConnection();
	    connection.setRequestMethod("GET");
	    connection.connect();
	    int statusCode = connection.getResponseCode();
	    if (statusCode != 200)  {
	    	System.out.println("Statuscode: " + statusCode + " ");
	    	throw new Exception();
    	}
	    InputStream is;
	    is = URL.openStream();
	    try {
	      BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
	      String jsonText = readAll(rd);
	      JSONObject json = new JSONObject(jsonText);
	      return json;
	    } finally {
	      is.close();
	    }
	}
}
