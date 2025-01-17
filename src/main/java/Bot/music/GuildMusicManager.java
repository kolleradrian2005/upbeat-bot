package Bot.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

public class GuildMusicManager {
	
  public final AudioPlayer player;
  public final TrackScheduler scheduler;

  public GuildMusicManager(AudioPlayerManager manager, long guildId) {
    player = manager.createPlayer();
    scheduler = new TrackScheduler(player, guildId);
    player.addListener(scheduler);
  }
  public boolean isPlaying() {
    return player.getPlayingTrack() != null;
  }
  public AudioPlayerSendHandler getSendHandler() {
    return new AudioPlayerSendHandler(player);
  }
}
