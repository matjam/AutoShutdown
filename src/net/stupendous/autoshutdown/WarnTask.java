package net.stupendous.autoshutdown;

import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import net.stupendous.autoshutdown.misc.Log;
import net.stupendous.autoshutdown.misc.Util;

public class WarnTask extends TimerTask {
	protected AutoShutdownPlugin plugin = null;
	protected Log log = null;
	protected long seconds = 0;
	
	WarnTask(AutoShutdownPlugin instance, long seconds) {
		plugin = instance;
		log = plugin.log;
		this.seconds = seconds;
	}
	
	public void run() {
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				if (TimeUnit.SECONDS.toMinutes(seconds) > 0) {
					if (TimeUnit.SECONDS.toMinutes(seconds) == 1) {
						if (seconds - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(seconds)) == 0) {
							Util.broadcast("Server is shutting down in 1 minute ...");
						} else {
							Util.broadcast("Server is shutting down in 1 minute %d seconds ...",
									seconds - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(seconds)));
						}
						
					} else {
						if (seconds - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(seconds)) == 0) {
							Util.broadcast("Server is shutting down in %d minutes ...", 
									TimeUnit.SECONDS.toMinutes(seconds));
						} else {
							Util.broadcast("Server is shutting down in %d minutes %d seconds ...", 
									TimeUnit.SECONDS.toMinutes(seconds),
									seconds - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(seconds)));
						}
						
					}
				} else {
					if (TimeUnit.SECONDS.toSeconds(seconds) == 1) {
						Util.broadcast("Server is shutting down NOW!");
					} else {
						Util.broadcast("Server is shutting down in %d seconds ...",	seconds);
					}
				}
			}
		});
	}
}
