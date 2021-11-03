package Bot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hc.core5.http.ParseException;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.specification.Album;
import com.wrapper.spotify.model_objects.specification.Playlist;
import com.wrapper.spotify.model_objects.specification.PlaylistTrack;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.model_objects.specification.TrackSimplified;
import com.wrapper.spotify.requests.data.albums.GetAlbumRequest;
import com.wrapper.spotify.requests.data.playlists.GetPlaylistRequest;
import com.wrapper.spotify.requests.data.tracks.GetTrackRequest;

import Bot.Spotify.Spotify;
import Bot.music.GuildMusicManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

public class AudioManager {
	
	private static AudioManager manager;
	private final AudioPlayerManager playerManager;
	private final Map<Long, GuildMusicManager> musicManagers;
	
	private final int maxTracksFromSpotifyPlaylist = 10; // Needed because YouTube disables after lots of searches
	
	// Constructor
	
	private AudioManager() {
		this.musicManagers = new HashMap<>();
		this.playerManager = new DefaultAudioPlayerManager();
		AudioSourceManagers.registerRemoteSources(playerManager);
		AudioSourceManagers.registerLocalSource(playerManager);
	}
	
	// Getter
	
	public static synchronized AudioManager get() {
		if (manager == null) {
			manager = new AudioManager();
		}
		return manager;
	}
	
	// Get the music manager of discord server
	
	public synchronized GuildMusicManager getGuildMusicManager(Guild guild) {
		long guildId = guild.getIdLong();
		GuildMusicManager musicManager = musicManagers.get(guildId);
		if (musicManager == null) {
			musicManager = new GuildMusicManager(playerManager, guildId);
			musicManagers.put(guildId, musicManager);
		}
		guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());
		return musicManager;
	}
	
	// Enumerator to be able to identify the type of fetching from youtube
	
	private enum fetchType {
		YOUTUBE_LINK,
		SPOTIFY_LINK,
		YOUTUBE_SEARCH
	}

	// Load and play track(s)
	
	public void loadAndPlay(TextChannel channel, List<String> args, User user, boolean isFirst) {
		GuildMusicManager musicManager = getGuildMusicManager(channel.getGuild());
		if (args.size() == 0) return;
		
		String trackUrl = args.get(0);
		List<String> searchFromSpotify = new ArrayList<String>();
		fetchType fetchType;
		
		if (Helper.isSpotifyLink(trackUrl)) {
			fetchType = Bot.AudioManager.fetchType.SPOTIFY_LINK;
			searchFromSpotify = getSearchFromSpotify(trackUrl);
			if (searchFromSpotify == null || searchFromSpotify.size() == 0) {
				ChatManager.sendMessageWithEmbedWithUser(channel, user, "Please provide a valid spotify link!");
				return;
			}
		} else if (Helper.isYoutubeLink(trackUrl) || Helper.isTwitchLink(trackUrl) || Helper.isSoundCloudLink(trackUrl)) {
			fetchType = Bot.AudioManager.fetchType.YOUTUBE_LINK;
		} else {
			if (Helper.isUrl(trackUrl)) {
				ChatManager.sendMessageWithEmbedWithUser(channel, user, "Please provide a valid link!");
				return;
			}
			fetchType = Bot.AudioManager.fetchType.YOUTUBE_SEARCH;
		}
		
		switch (fetchType) {
			case SPOTIFY_LINK:
				if (searchFromSpotify.size() == 1) {
					String url;
					try {
						url = Main.youtube.search(searchFromSpotify.get(0));
					} catch (Exception e) {
						url = "ytsearch: " + searchFromSpotify.get(0);
					}
					loadItem(musicManager, channel, url, fetchType, user, true, isFirst);
					break;
				}
				int maxCounter = maxTracksFromSpotifyPlaylist;
				ChatManager.sendMessageWithEmbedWithUser(channel, user, "Adding spotify playlist to queue! This may take some time...");
				for (String result : searchFromSpotify) {
					if (maxCounter == 0) break;
					String url;
					try {
						url = Main.youtube.search(result);
					} catch (Exception e) {
						url = "ytsearch: " + result;
					}
					loadItem(musicManager, channel, url, fetchType, user, false, isFirst);
					maxCounter--;
				}
				break;
			case YOUTUBE_LINK:
				loadItem(musicManager, channel, trackUrl, fetchType, user, true, isFirst);
				break;
			case YOUTUBE_SEARCH:
				String searchStr = "";
				for(String arg : args) {
	    			searchStr += arg + " ";
	    		}
				String url;
				try {
					url = Main.youtube.search(searchStr);
				} catch (Exception e) {
					loadItem(musicManager, channel, "ytsearch: " + searchStr, fetchType, user, true, isFirst);
					return;
				}
				loadItem(musicManager, channel, url, fetchType, user, true, isFirst);
				break;
		}
		
	}
	
	// Get results from Spotify
	
	private List<String> getSearchFromSpotify(String link) {
		List<String> results = new ArrayList<String>();
		if (link.contains("/track/")) {
            String[] parsed = link.split("/track/");
            if (parsed.length == 2) {
            	String code = parsed[1];
            	String id = "";
            	for (int i = 0 ; i < code.length(); i++) {
            		char currentChar = code.charAt(i);
            		if (currentChar != '?')
            			id += currentChar;
            		else break;
            	}
                try {
                	final GetTrackRequest request = Spotify.getSpotifyapi().getTrack(id).build();
                	Track exec = request.execute();
                	results.add(exec.getArtists()[0].getName() + " " + exec.getName());
                }
                catch (Exception e) {
                	Spotify.clientCredentials_Sync();
                	final GetTrackRequest request = Spotify.getSpotifyapi().getTrack(id).build();
					try {
						Track exec = request.execute();
						results.add(exec.getArtists()[0].getName() + " " + exec.getName());
					} catch (ParseException | SpotifyWebApiException | IOException e1) {
						e1.printStackTrace();
					}
                }
            }
		} else if (link.contains("/playlist/")) {
			String[] parsed = link.split("/playlist/");
            if (parsed.length == 2) {
            	String code = parsed[1];
            	String id = "";
            	for (int i = 0 ; i < code.length(); i++) {
            		char currentChar = code.charAt(i);
            		if (currentChar != '?')
            			id += currentChar;
            		else break;
            	}
                
                try {
                	final GetPlaylistRequest request = Spotify.getSpotifyapi().getPlaylist(id).build();
                	Playlist exec = request.execute();
                	PlaylistTrack[] tracks = exec.getTracks().getItems();
                	for (PlaylistTrack track : tracks) {
                		String trackId = track.getTrack().getId();
                		GetTrackRequest trackRequest = Spotify.getSpotifyapi().getTrack(trackId).build();
                		results.add(trackRequest.execute().getArtists()[0].getName() + " " + track.getTrack().getName());
                	}
                }
                catch (Exception e) {
                	Spotify.clientCredentials_Sync();
                	final GetPlaylistRequest request = Spotify.getSpotifyapi().getPlaylist(id).build();
					try {
						Playlist exec = request.execute();
	                	PlaylistTrack[] tracks = exec.getTracks().getItems();
	                	for (PlaylistTrack track : tracks) {
	                		String trackId = track.getTrack().getId();
	                		GetTrackRequest trackRequest = Spotify.getSpotifyapi().getTrack(trackId).build();
	                		results.add(trackRequest.execute().getArtists()[0].getName() + " " + track.getTrack().getName());
	                	}
					} catch (ParseException | SpotifyWebApiException | IOException e1) {
						e1.printStackTrace();
					}
                }
            }
		} else if (link.contains("/album/")) {
			String[] parsed = link.split("/album/");
            if (parsed.length == 2) {
            	String code = parsed[1];
            	String id = "";
            	for (int i = 0 ; i < code.length(); i++) {
            		char currentChar = code.charAt(i);
            		if (currentChar != '?')
            			id += currentChar;
            		else break;
            	}
                try {
                	final GetAlbumRequest request = Spotify.getSpotifyapi().getAlbum(id).build();
                	Album exec = request.execute();
                	TrackSimplified[] tracks = exec.getTracks().getItems();
                	for (TrackSimplified track : tracks) {
                		results.add(track.getArtists()[0].getName() + " " + track.getName());
                	}
                }
                catch (Exception e) {
                	Spotify.clientCredentials_Sync();
                	final GetAlbumRequest request = Spotify.getSpotifyapi().getAlbum(id).build();
					try {
						Album exec = request.execute();
	                	TrackSimplified[] tracks = exec.getTracks().getItems();
	                	for (TrackSimplified track : tracks) {
	                		results.add(track.getArtists()[0].getName() + " " + track.getName());
	                	}
					} catch (ParseException | SpotifyWebApiException | IOException e1) {
						e1.printStackTrace();
					}
                }
            }
		}
		return results;
	}
	
	// Load item(s) to queue or throw error
	
	private void loadItem(GuildMusicManager musicManager, TextChannel channel, String input, fetchType fetchType, User user, boolean writeResult, boolean isFirst) {
		playerManager.loadItemOrdered(musicManager, input, new AudioLoadResultHandler() {

			@Override
			public void trackLoaded(AudioTrack track) {
				if (writeResult)
					ChatManager.sendMessageWithEmbedWithUserWithFooter(channel, user, "Adding to queue", track.getInfo().title);
				play(musicManager, track, isFirst);
			}

			@Override
			public void playlistLoaded(AudioPlaylist playlist) {
				if (fetchType == Bot.AudioManager.fetchType.YOUTUBE_SEARCH || fetchType == Bot.AudioManager.fetchType.SPOTIFY_LINK) {
					AudioTrack firstTrack = playlist.getSelectedTrack();
					if (firstTrack == null) {
						firstTrack = playlist.getTracks().get(0);
					}
					if (writeResult)
						ChatManager.sendMessageWithEmbedWithUserWithFooter(channel, user, "Adding track to queue", firstTrack.getInfo().title);
					play(musicManager, firstTrack, isFirst);
				} else {
					List<AudioTrack> tracks = playlist.getTracks();
					List<AudioTrack> selectedTracks = tracks;
					for (AudioTrack track : selectedTracks) {
						if (track == playlist.getSelectedTrack()) {
							break;
						}
						selectedTracks.remove(track);
					}
					
					if (writeResult)
						ChatManager.sendMessageWithEmbedWithUserWithFooter(channel, user, "Adding playlist to queue from track", playlist.getSelectedTrack().getInfo().title);
					
					for (AudioTrack track : selectedTracks) {
						play(musicManager, track, isFirst);
					}
				}
			}

			@Override
			public void noMatches() {
				String inp = input;
				if(input.substring(0, 10).equals("ytsearch: ")) {
					inp = input.substring(10);
				}
				ChatManager.sendErrorMessageWithEmbedWithUser(channel, user, "Nothing found by " + inp);
			}

			@Override
			public void loadFailed(FriendlyException exception) {
				ChatManager.sendErrorMessageWithEmbedWithUser(channel, user, "Could not play: " + exception.getMessage());
			}
		});
	}
	
	// Set global volume of audio
	
	public void setAllVolume(int volume) {
		for (GuildMusicManager musicManager : musicManagers.values()) {
			musicManager.player.setVolume(volume);
		}
	}
	
	// Sweep in current track
	
	public void sweep(TextChannel channel, int duration, User user) {
		Guild guild = channel.getGuild();
		GuildMusicManager musicManager = getGuildMusicManager(guild);
		AudioTrack currentTrack = musicManager.player.getPlayingTrack();
		
		long currentPos = currentTrack.getPosition();
		
		long pos = currentPos + duration;
		
		if (pos < 0)
			pos = 0;
		if (pos > currentTrack.getDuration())
			pos = currentTrack.getDuration();
		
		long changed = (pos - currentPos) / 1000;
		
		if (changed >= 0)
			ChatManager.sendMessageWithEmbedWithUser(channel, user, "Sweeping forward " + String.valueOf(changed) + " seconds.");
		else
			ChatManager.sendMessageWithEmbedWithUser(channel, user, "Sweeping backwards " + String.valueOf(Math.abs(changed)) + " seconds.");
		
		currentTrack.setPosition(pos);
	}
	
	// Play track
	
	private void play(GuildMusicManager musicManager, AudioTrack track, boolean isFirst) {
		if (isFirst)
			musicManager.scheduler.queueFirst(track);
		else
			musicManager.scheduler.queue(track);
	}
}
