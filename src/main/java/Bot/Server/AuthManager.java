package Bot.Server;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;

import Bot.Main;

public class AuthManager {
	
	private static final SecureRandom secureRandom = new SecureRandom();
	private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder();
	
	// Authorize with username & password
	
	public static ArrayList<Object> login(String username, String password) {
		ArrayList<Object> ret = new ArrayList<Object>();
		boolean result = Main.sql.authorize(username, password);
		if (result) {
			ret.add(true);
			String cookie = generateCookie();
			Main.sql.setCookie(username, cookie);
			ret.add(cookie);
		} else {
			ret.add(false);
		}
		return ret;
	}
	
	// Authorize with username & cookie
	
	public static ArrayList<Object> checkCookie(String username, String cookie) {
		ArrayList<Object> ret = new ArrayList<Object>();
		if (Main.sql.checkCookie(username, cookie)) {
			ret.add(true);
			return ret;
		}
		ret.add(false);
		return ret;
	}
	
	// Generate a random cookie

	private static String generateCookie() {
	    byte[] randomBytes = new byte[24];
	    secureRandom.nextBytes(randomBytes);
	    return base64Encoder.encodeToString(randomBytes);
	}
}
