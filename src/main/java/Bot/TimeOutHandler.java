package Bot;

import java.util.HashMap;
import java.util.Map;

import net.dv8tion.jda.api.entities.Guild;

public class TimeOutHandler {
	
	private static final long time = 300000/* = 5 minutes*/; // Determines time to stay in milliseconds
	private static Map<Guild, TimeOut> threads = new HashMap<Guild, TimeOut>();
	
	public static void scheduleTimeOut(Guild guild) {
		if (threads.containsKey(guild)) {
			threads.remove(guild).stop();
		}
		TimeOut timer = new TimeOut();
		CallBack callback = new CallBack() {
			@Override
			public void method() {
				guild.getAudioManager().closeAudioConnection();
			}
		};
		timer.start(time, callback);
		threads.put(guild, timer);
	}
	public static void cancelTimeOut(Guild guild) {
		if (!threads.containsKey(guild)) return;
		TimeOut timer = threads.remove(guild);
		timer.stop();
	}
}
interface CallBack {
    void method();
}