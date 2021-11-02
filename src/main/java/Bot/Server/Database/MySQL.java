package Bot.Server.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MySQL {
	
	Statement statement;
	
	// Connect to database
	
	public void connect() throws SQLException, ClassNotFoundException {
		Connection db = DriverManager.getConnection("jdbc:mysql://localhost:3306/UpBeat",
				"admin",
				"password"
				);
		statement = db.createStatement();
	}
	
	// Check in database if username & password has a match
	
	public boolean authorize(String username, String password) {
		if (statement == null) return false;
		try {
			ResultSet query = statement.executeQuery("SELECT * FROM UpBeat.Users WHERE Password = '" + password + "' AND Username = '" + username + "'");
			List<String> results = new ArrayList<String>();
			while (query.next()) {
				results.add(query.getString("Username"));
			}
			if (!results.isEmpty()) {
				return true;
			}
		} catch (SQLException e) {}
		return false;
	}
	
	// Save cookie in database
	
	public void setCookie(String username, String cookie) {
		if (statement == null) return;
		try {
			String cmd  = "UPDATE `Users` SET cookie = '" + cookie + "' WHERE Username = '" + username + "';";
			statement.execute(cmd);
		} catch (SQLException e) {}
	}
	
	// Check in database if username & cookie has a match
	
	public boolean checkCookie(String username, String cookie) {
		if (statement == null) return false;
		try {
			ResultSet query = statement.executeQuery("SELECT * FROM UpBeat.Users WHERE Cookie = '" + cookie + "' AND Username = '" + username + "'");
			List<String> results = new ArrayList<String>();
			while (query.next()) {
				results.add(query.getString("Username"));
			}
			if (!results.isEmpty()) {
				return true;
			}
		} catch (SQLException e) {}
		return false;
	}
}
