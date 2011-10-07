package net.stupendous.autoshutdown;

import java.util.Calendar;
import java.util.Locale;
import java.util.Date;
import java.util.TimerTask;

import net.stupendous.autoshutdown.misc.*;
import org.bukkit.plugin.java.JavaPlugin;

public class WarnTask extends TimerTask {
	protected AutoShutdownPlugin plugin = null;
	protected Log log = null;
	protected int seconds = 0;
	
	WarnTask(AutoShutdownPlugin instance, int seconds) {
		plugin = instance;
		log = plugin.log;
		this.seconds = seconds;
	}
	
	public void run() {
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				if (seconds < 59) {
					if (seconds <= 1) {
						Util.broadcast("Server is shutting down NOW!");
					} else {
						Util.broadcast("Server is shutting down in %d seconds ...", seconds);
					}
				} else {
					if (seconds/60 <= 1) {
						Util.broadcast("Server is shutting down in 1 minute ...");
					} else {
						Util.broadcast("Server is shutting down in %d minutes ...", seconds/60);
					}
				}
			}
		});
	}
}
