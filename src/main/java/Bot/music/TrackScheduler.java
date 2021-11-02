package Bot.music;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import Bot.InitManager;
import Bot.Main;
import Bot.TimeOutHandler;

public class TrackScheduler extends AudioEventAdapter {
	
	  private final AudioPlayer player;
	  private final LinkedBlockingQueue<AudioTrack> queue;
	  private final long guildId;
	  private boolean loop = false;
	  
	  public TrackScheduler(AudioPlayer player, long guildId) {
	    this.player = player;
	    this.guildId = guildId;
	    this.queue = new LinkedBlockingQueue<>();
	  }

	  public void queue(AudioTrack track) {
	    if (!player.startTrack(track, true)) {
	      queue.offer(track);
	      InitManager.setTrack(guildId, getCurrentTrack());
	    } else {
	    	TimeOutHandler.cancelTimeOut(Main.bot.getGuildById(guildId));
			InitManager.setTrack(guildId, track);
	    }
	  }
	  public void queueFirst(AudioTrack track) {
		  if (queue.size() == 0) {
			  
			  if (!player.startTrack(track, true)) {
				  queue(track);
			  } else {
				  TimeOutHandler.cancelTimeOut(Main.bot.getGuildById(guildId));
			  }
		  } else {
				try {
					Object[] currentQueue = queue.toArray();
					List<AudioTrack> currentQueueList = new ArrayList<>();
					for (Object obj : currentQueue) {
						currentQueueList.add((AudioTrack) obj);
					}
					currentQueueList.add(0, track);
					queue.clear();
					queue.addAll(currentQueueList);
					//queue.put(track);
				} catch (Exception e) {
					queue(track);
					e.printStackTrace();
				}
		  }
	  }
	  public void removeFirst() {
		  queue.poll();
	  }
	  public BlockingQueue<AudioTrack> getQueue() {
	    return queue;
	  }
	  public void removeFromQueue(int amount) {
		  for (int i = 0; i < amount ; i++)
			  if (queue.size() != 0) {
				  var track = queue.toArray()[queue.size()-1];
				  queue.remove(track);
			  }
			  else
				  break;
	  }
	  public void purge() {
		  queue.clear();
	  }
	  public boolean toggleLoop() {
		  loop = !loop;
		  return loop;
	  }
	  public boolean nextTrack() {
		  AudioTrack track = queue.poll();
		  InitManager.setTrack(guildId, track);
		  if (player.startTrack(track, false)) {
			  TimeOutHandler.cancelTimeOut(Main.bot.getGuildById(guildId));
			  return true;
		  } else {
			  TimeOutHandler.scheduleTimeOut(Main.bot.getGuildById(guildId));
			  return false;
		  }
	  }
	  public AudioTrack getCurrentTrack() {
		    return player.getPlayingTrack();
	  }
	  @Override
	  public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
		if (loop && !endReason.equals(AudioTrackEndReason.REPLACED)) {
		  AudioTrack clone = track.makeClone();
		  InitManager.setTrack(guildId, clone);
		  if (player.startTrack(clone, false)) {
			  TimeOutHandler.cancelTimeOut(Main.bot.getGuildById(guildId));
		  } else {
			  TimeOutHandler.scheduleTimeOut(Main.bot.getGuildById(guildId));
		  }
		}
	    if (endReason.mayStartNext) {
	      nextTrack();
	    } else {
	    	TimeOutHandler.scheduleTimeOut(Main.bot.getGuildById(guildId));
	    }
	  }
	}
