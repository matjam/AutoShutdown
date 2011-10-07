package net.stupendous.autoshutdown;

import java.util.Calendar;
import java.util.Locale;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import net.stupendous.autoshutdown.misc.*;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Server;
import org.bukkit.World;

public class ShutdownScheduleTask extends TimerTask {
	protected AutoShutdownPlugin plugin = null;
	protected Log log = null;
	
	ShutdownScheduleTask(AutoShutdownPlugin instance) {
		plugin = instance;
		log = plugin.log;
	}
	
	public void run() {
		// Because the Java scheduler will call this task in a separate thread,
		// we need to use the bukkit scheduler to call our code in the main thread.
		//
		// This is because everything we do is not thread safe, so its best that
		// we do everything in the main thread.
		
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				runTask();
			}
		});
	}
	
	private void runTask() {
		// Don't do anything if we're already in the process of shutting down.
		if (plugin.shutdownImminent == true)			
			return;
		
		Calendar now = Calendar.getInstance();

		// Number of milliseconds before the shutdown that we warn the first time
		long firstWarning = plugin.warnTimes.get(0) * 1000; 
		
		// Check to see if any of the shutdown times fall within the first warning.
		for (Calendar cal : plugin.shutdownTimes) {
			if (cal.getTimeInMillis() - now.getTimeInMillis() <= firstWarning) {
				plugin.shutdownImminent = true;
				plugin.shutdownTimer = new Timer();
				
				// Schedule all the warning messages to fire at the appropriate times.
				
				for (Integer warnTime : plugin.warnTimes) {
					long longWarnTime = warnTime.longValue() * 1000;
					
					if (longWarnTime < cal.getTimeInMillis() - now.getTimeInMillis()) {
						plugin.shutdownTimer.schedule(new WarnTask(plugin, warnTime.longValue()), 
								cal.getTimeInMillis() - now.getTimeInMillis() - longWarnTime); 
					}
				}
				
				// Schedule the ShutdownTask to shut the server down at the right time.
				
				plugin.shutdownTimer.schedule(new ShutdownTask(plugin), cal.getTime());
				
				break; // We don't need to schedule any more.
			}
		}
	}

}
