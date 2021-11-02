package Bot;

import java.net.MalformedURLException;
import java.net.URL;

public class Helper {
	
	// Converting long time to text format hh:mm:ss
	
	public static String convertTimeToString(long time) {
		long duration = time / 1000;
		long hours = (long) (duration / 3600);
		long mins = (long) duration / 60 - hours * 60;
		long seconds = (long) duration - 60 * mins - 3600 * hours;
		
		String hoursStr = convertToTwoDigit(hours);
		String minsStr = convertToTwoDigit(mins);
		String secondsStr = convertToTwoDigit(seconds);
		
		return hoursStr + ":" + minsStr + ":" + secondsStr;
	}
	
	// Convert integer to two digit text
	
	public static String convertToTwoDigit(long num) {
		String str = String.valueOf(num);
		if (num < 10)
			str = "0" + str;
		return str;
	}
	
	// Determine whether String is instance of Integer
	
	public static boolean isInt(String str) {
		try {
		    Integer.parseInt(str);
		    return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	// Determine whether String is a URL
	
	public static boolean isUrl(String input) {
        try {
            new URL(input);
            return true;
        } catch (MalformedURLException ignored) {
            return false;
        }
    }
	
	// Determine whether String is instance of a YouTube link
	
	public static boolean isYoutubeLink(String input) {
		return input.contains("youtube.com") || input.contains("youtu.be");
    }
	
	// Determine whether String is instance of a Spotify link
	
	public static boolean isSpotifyLink(String input) {
		return input.contains("spotify.com");
    }

	public static boolean isTwitchLink(String input) {
		return input.contains("twitch.tv");
	}

	public static boolean isSoundCloudLink(String input) {
		return input.contains("soundcloud.com");
	}
}
