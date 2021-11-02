package Bot;

public class TimeOut {
	
	private CallBack callback;
	private Thread thread;
	
	public void start(long time, CallBack callback) {
		this.callback = callback;
		thread = thread(time);
		thread.start();
	}
	private Thread thread(long time) {
		return new Thread(){
		    public void run(){
		    	try {
		    		Thread.sleep(time);
					expired();
				} catch (InterruptedException e) {
					return;
				}
		    }
		};
	}
	private void expired() {
		callback.method();
	}
	@SuppressWarnings("deprecation")
	public void stop() {
		thread.stop();
	}
}