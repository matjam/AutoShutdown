package net.stupendous.autoshutdown;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TreeSet;

import net.stupendous.autoshutdown.misc.Log;
import net.stupendous.autoshutdown.misc.Util;

import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class AutoShutdownPlugin extends JavaPlugin {
	public String pluginName = "AutoShutdown"; // Need to do this because there is no way to load the PDF at initialisation time.
	public final Log log = new Log(pluginName);
	public PluginDescriptionFile pdf = null; 
	protected ShutdownScheduleTask task = null;
	protected Timer backgroundTimer = null;
	protected Timer shutdownTimer = null;
	protected BukkitScheduler scheduler = null;
	protected boolean shutdownImminent = false;
	protected TreeSet<Calendar> shutdownTimes = new TreeSet<Calendar>();
	protected ArrayList<Integer> warnTimes = new ArrayList<Integer>();
	protected String shutdownReason = "Scheduled Shutdown";
	
	File propFile = null;

	public void onDisable() {
		shutdownImminent = false;
		
		if (backgroundTimer != null) {
			backgroundTimer.cancel();
			backgroundTimer.purge();
			backgroundTimer = null;
		}

		if (shutdownTimer != null) {
			shutdownTimer.cancel();
			shutdownTimer.purge();
			shutdownTimer = null;
		}

        log.info("Version %s disabled.", pdf.getVersion());
    }

    public void onEnable() {
    	pdf = this.getDescription();
    	scheduler = this.getServer().getScheduler();
    	shutdownImminent = false;
    	shutdownTimes.clear();
		
    	loadConfiguration();
    	
    	CommandExecutor autoShutdownCommandExecutor = new AutoShutdownCommand(this);
    	getCommand("autoshutdown").setExecutor(autoShutdownCommandExecutor);
    	getCommand("as").setExecutor(autoShutdownCommandExecutor);
   	
    	scheduleAll();
    
    	Util.init(this, pluginName, log);
    	
    	// A timer to run the AutoShutdownTask on a fixed schedule - once per minute.
    	
    	if (backgroundTimer != null) {
    		backgroundTimer.cancel();
    		backgroundTimer.purge();
    		backgroundTimer = null;
    	}

		backgroundTimer = new Timer(); 
    	
		// A timer to manage all the tasks we will schedule that will both warn
		// users of impending shutdowns, and perform the shutdown itself.
		//
		// Reason for using a separate timer is so that we can cancel the whole
		// log in one fell swoop without fucking up the backgroundTimer.
		
		if (shutdownTimer != null) {
			shutdownTimer.cancel();
			shutdownTimer.purge();
			shutdownTimer = null;
		}
		
		// shutdownTimer = new Timer();

    	Calendar now = Calendar.getInstance();
    	now.set(Calendar.SECOND, 0);
    	now.add(Calendar.MINUTE, 1);
    	
    	// It's not that I don't trust the Java scheduler. It's just that I don't
    	// trust the Java scheduler.
    	
    	now.add(Calendar.MILLISECOND, 50);  

    	try {
    		backgroundTimer.scheduleAtFixedRate(new ShutdownScheduleTask(this), now.getTime(), 60000);
    	} catch (Exception e) {
    		log.severe("Failed to schedule AutoShutdownTask: %s", e.getMessage());
    	}
    	
    	saveConfig();
    }

    protected void loadConfiguration() {
    	/* Load configuration and set some sane defaults */
    	getConfig().options().copyDefaults(true);
    	getConfig().getString("shutdowntimes", "02:00,14:00");
    	getConfig().getString("warntimes","900,600,300,240,180,120,60,45,30,15,14,13,12,11,10,9,8,7,6,5,4,3,2,1");
    	getConfig().getBoolean("kickonshutdown", true);
    	getConfig().getString("kickreason", "Scheduled Shutdown.");
    	getConfig().getInt("gracetime", 30);
    }
    
    protected void scheduleAll() {
    	// This configures the shutdown times based on configuration.

    	shutdownTimes.clear();
    	warnTimes.clear();
    	
		try {
			String[] shutdownTimeStrings  = getConfig().getString("shutdowntimes").split(",");
			for(String timeString : shutdownTimeStrings) {
				Calendar cal = scheduleShutdownTime(timeString);
				log.info("Shutdown scheduled for %s", cal.getTime().toString());
			}
		} catch (Exception e) {
			log.severe("Unable to configure shutdown time using properties file.");
			log.severe("Is the format of shutdown time correct? It should be only HH:MM.");
			log.severe("Error: %s", e.getMessage());
		}
		String[] strings = getConfig().getString("warntimes").split(",");
		for(String warnTime : strings) {
				warnTimes.add(Integer.decode(warnTime));
		}
    }
    
    protected Calendar scheduleShutdownTime(String timeSpec) throws Exception {
    	if (timeSpec == null)
    		return null;
    	
    	if (timeSpec.matches("^now$")) {
    		Calendar now = Calendar.getInstance();
    		int secondsToWait = getConfig().getInt("gracetime", 30);
    		now.add(Calendar.SECOND, secondsToWait);
    		
			shutdownImminent = true;
			shutdownTimer = new Timer();
			
			// Schedule all the warning messages to fire at the appropriate times.
			
			for (Integer warnTime : warnTimes) {
				long longWarnTime = warnTime.longValue() * 1000;
				
				if (longWarnTime <= secondsToWait * 1000) {
					shutdownTimer.schedule(new WarnTask(this, warnTime.longValue()), 
							secondsToWait * 1000 - longWarnTime); 
				}
			}
			
			// Schedule the ShutdownTask to shut the server down at the right time.
			
			shutdownTimer.schedule(new ShutdownTask(this), now.getTime());
    		Util.broadcast("The server has been scheduled for immediate shutdown.");
    		
    		return now;
    	}
    	
    	if (!timeSpec.matches("^[0-9]{1,2}:[0-9]{2}$")) {
    		throw new Exception("Incorrect time specification. The format is HH:MM in 24h time.");
    	}

    	// So we add the new time to the list of configured shutdown times.
    	
    	Calendar now = Calendar.getInstance();
    	Calendar shutdownTime = Calendar.getInstance();

    	String timecomponent[] = timeSpec.split(":");
    	shutdownTime.set(Calendar.HOUR_OF_DAY, Integer.valueOf(timecomponent[0]).intValue());
    	shutdownTime.set(Calendar.MINUTE, Integer.valueOf(timecomponent[1]).intValue());
    	shutdownTime.set(Calendar.SECOND, 0);
    	shutdownTime.set(Calendar.MILLISECOND, 0);
    	
    	if (now.compareTo(shutdownTime) >= 0) {
    		shutdownTime.add(Calendar.DAY_OF_MONTH, 1);
    	}

    	shutdownTimes.add(shutdownTime);
		
		return shutdownTime;
    }

	protected void kickAll() {
		if (getConfig().getBoolean("kickonshutdown", true)) {
			return;
		}
		
		log.info("Kicking all players ...");
		
		Player[] players = getServer().getOnlinePlayers();
		
		for (Player player : players) {
			log.info("Kicking player %s.", player.getName());
			player.kickPlayer(getConfig().getString("kickreason"));
		}
	}
}


