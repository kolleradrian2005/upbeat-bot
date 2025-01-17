package Bot.Spotify;
import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.apache.hc.core5.http.ParseException;

import Bot.References;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;

public class Spotify {
	
	private static SpotifyApi spotifyApi;
	private static ClientCredentialsRequest clientCredentialsRequest;
	
	// Getter for spotify
	
	public static SpotifyApi getSpotifyapi() {
		return spotifyApi;
	}
	
	// Initialize Spotify API
	
	public static SpotifyApi initApi(String clientId, String clientSecret) {
		spotifyApi = new SpotifyApi
				.Builder()
			    .setClientId(clientId)
			    .setClientSecret(clientSecret)
			    .build();
		clientCredentialsRequest = spotifyApi
				.clientCredentials()
				.build();
		clientCredentials_Async();
		return spotifyApi;
	}
	
	public static void clientCredentials_Sync() {
	    try {
			final ClientCredentials clientCredentials = clientCredentialsRequest.execute();

			// Set access token for further "spotifyApi" object usage
			spotifyApi.setAccessToken(clientCredentials.getAccessToken());

			print("Expires in: " + clientCredentials.getExpiresIn() + " seconds.");
	    } catch (IOException | SpotifyWebApiException | ParseException e) {
			print("Error: " + e.getMessage());
	    }
	}
	public static void clientCredentials_Async() {
		try {
			final CompletableFuture<ClientCredentials> clientCredentialsFuture = clientCredentialsRequest.executeAsync();

			// Thread free to do other tasks...

			final ClientCredentials clientCredentials = clientCredentialsFuture.join();

			// Set access token for further "spotifyApi" object usage
			spotifyApi.setAccessToken(clientCredentials.getAccessToken());

			print("Expires in: " + clientCredentials.getExpiresIn() + " seconds.");
		} catch (CompletionException e) {
			print("Error: " + e.getCause().getMessage());
		} catch (CancellationException e) {
			print("Async operation cancelled.");
		}
	}

	// Logging

	private static void print(String msg) {
		System.out.println(References.spotifyPrefix + msg);
	}
}
