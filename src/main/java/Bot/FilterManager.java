package Bot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.sedmelluq.discord.lavaplayer.filter.AudioFilter;
import com.sedmelluq.discord.lavaplayer.filter.PcmFilterFactory;
import com.sedmelluq.discord.lavaplayer.filter.ResamplingPcmAudioFilter;
import com.sedmelluq.discord.lavaplayer.filter.UniversalPcmAudioFilter;
import com.sedmelluq.discord.lavaplayer.filter.equalizer.Equalizer;
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat;
import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import Bot.music.GuildMusicManager;
import net.dv8tion.jda.api.entities.TextChannel;

enum FilterType {
	CLEAR,
	BASS,
	NIGHTCORE
}

public class FilterManager {
	
	// Apply filter to current track
	
	static void applyFilter(TextChannel channel, FilterType type, @Nullable Integer amount) {
    	GuildMusicManager guildMusicManager = Bot.AudioManager.get().getGuildMusicManager(channel.getGuild());
    	PcmFilterFactory factory = new PcmFilterFactory() {
			@Override
			public List<AudioFilter> buildChain(AudioTrack track, AudioDataFormat format, UniversalPcmAudioFilter output) {
				List<AudioFilter> filters = new ArrayList<AudioFilter>();
				switch (type) {
					case BASS:
						var equalizer = new Equalizer(format.channelCount, output);
						int multiplier = 1;
				    	if (amount != null) {
				    		multiplier = amount;
				    	}
			            equalizer.setGain(0, (0.25f / 3) * multiplier);
			            equalizer.setGain(1, (0.25f / 4) * multiplier);
			            equalizer.setGain(2, (0.25f / 5) * multiplier);
			            equalizer.setGain(3, (0.25f / 5) * multiplier);
			            filters.add(equalizer);
						break;
					case NIGHTCORE:
						var resamplingFilter = new ResamplingPcmAudioFilter(new AudioConfiguration(), format.channelCount, output, format.sampleRate, (int) (format.sampleRate / 1.25));
			            filters.add(resamplingFilter);
						break;
					case CLEAR:
						break;
				}
				return filters;
			}
    	};
    	AudioTrack currentTrack = guildMusicManager.player.getPlayingTrack();
		guildMusicManager.player.setFilterFactory(factory);
		
		var clone = currentTrack.makeClone();
		clone.setPosition(currentTrack.getPosition());
		guildMusicManager.player.startTrack(clone, false);
	}
}
