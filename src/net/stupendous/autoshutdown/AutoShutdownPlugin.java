package net.stupendous.autoshutdown;

import net.stupendous.autoshutdown.misc.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Properties;
import javax.persistence.PersistenceException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.plugin.Plugin;

public class AutoShutdownPlugin extends JavaPlugin {
	public String pluginName = "AutoShutdown"; // Need to do this because there is no way to load the PDF at initialisation time.
	public final Log log = new Log(pluginName);
	protected Properties config = new Properties();
	public PluginDescriptionFile pdf = null; 
	protected AutoShutdownTask task = null;
	protected Timer timer = null;
	protected BukkitScheduler scheduler = null;
	protected Calendar stopTime = null;
	
	public void onDisable() {
		if (timer != null) {
			timer.cancel();
			timer.purge();
			timer = null;
		}
		
        log.info("Version %s disabled.", pdf.getVersion());
    }

    public void onEnable() {
    	pdf = this.getDescription();
    	scheduler = this.getServer().getScheduler();

    	if (!loadProperties("AutoShutdown.properties")) {
        	log.info("Unable to enable plugin. Properties file AutoShutdown/AutoShutdown.properties does not exist and could not be created.");
        	return;
        }
    	
    	CommandExecutor autoShutdownCommandExecutor = new AutoShutdownCommand(this);
    	getCommand("autoshutdown").setExecutor(autoShutdownCommandExecutor);
    	getCommand("as").setExecutor(autoShutdownCommandExecutor);
   	
    	configure();
    
    	Util.init(this, pluginName, log);
    	
        log.info("Version %s enabled.", pdf.getVersion());
    }

    protected void configure() {
    		try {
				configure(config.getProperty("shutdown.time"));
			} catch (Exception e) {
				log.severe("Unable to configure shutdown time using properties file.");
				log.severe("Is the format of shutdown time correct? It should be only HH:MM:SS.");
			}
    }
    
    protected void configure(String timeSpec) throws Exception {
    	if (!timeSpec.matches("^now$") && !timeSpec.matches("^[0-9]{1,2}:[0-9]{1,2}:[0-9]{1,2}$")) {
    		throw new Exception("Incorrect time specification.");
    	}

    	// Should kill the current Timer before setting a new one.
    	
    	if (timer != null) {
    		timer.cancel();
    		timer.purge();
    		timer = null;
    	}

		timer = new Timer(); 
    	Calendar now = Calendar.getInstance();
    	stopTime = Calendar.getInstance();

    	if (timeSpec.matches("^now$")) {
    		stopTime.add(Calendar.SECOND, 30);
    	} else {
	    	String timecomponent[] = timeSpec.split(":");
	    	stopTime.set(Calendar.HOUR_OF_DAY, Integer.valueOf(timecomponent[0]).intValue());
	    	stopTime.set(Calendar.MINUTE, Integer.valueOf(timecomponent[1]).intValue());
	    	stopTime.set(Calendar.SECOND, Integer.valueOf(timecomponent[2]).intValue());
    	} 
    	
    	if (now.compareTo(stopTime) >= 0) {
    		stopTime.add(Calendar.DAY_OF_MONTH, 1);
    	}
    	
		log.info("Scheduled to shutdown at %s", stopTime.getTime().toString());

    	task = new AutoShutdownTask(this);
    	
    	try {
    		timer.schedule(task, stopTime.getTime());
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	String warntimes[] = config.getProperty("shutdown.warn").split(",");
    	for (String time : warntimes) {
    		WarnTask warnTask = new WarnTask(this, Integer.valueOf(time).intValue());
    		Calendar warnTime = (Calendar) stopTime.clone();
    		warnTime.add(Calendar.SECOND, 0 - Integer.valueOf(time).intValue());
    		if (now.compareTo(warnTime) < 0) {
    			// Only bother scheduling it if the time hasn't already passed.
    			timer.schedule(warnTask, warnTime.getTime());
    		}
    	}
    }

	protected void kickAll() {
		if (Boolean.valueOf(config.getProperty("shutdown.kick")).booleanValue() != true) {
			return;
		}
		
		log.info("Kicking all players ...");
		
		Player[] players = getServer().getOnlinePlayers();
		
		for (Player player : players) {
			log.info("Kicking player %s.", player.getName());
			player.kickPlayer(config.getProperty("shutdown.reason"));
		}
	}

	private boolean loadProperties(String file) {
		File propFile = new File("plugins/" + pluginName + "/" + file);
		
		if (!propFile.exists()) {
			
			// mkdir just in case
			
			File confDir = new File("plugins/" + pluginName);
			confDir.mkdir();
			
			// Copy the default configuration from the jar to the config dir
			
			BufferedReader br = new BufferedReader(new InputStreamReader(this.getClassLoader().getResourceAsStream(file)));
			BufferedWriter bw = null;
			try {
				bw = new BufferedWriter(new FileWriter(propFile));
			} catch (IOException e) {
				log.severe("Unable to write to %s: %s", propFile.getPath(), e.getMessage());
				return false;
			}
			
			// This effectively converts the file's line endings. Not sure if thats appropriate.
			// There is probably a better way to copy a file from the jar.
			
			String line = null;
			
			try {
				while ((line = br.readLine()) != null) {
					bw.write(line);
					bw.newLine();		
				}
				bw.close();
				br.close();
			} catch (IOException e) {
				log.severe("Cannot copy properties file: %s", e.getMessage());
				return false;
			}
			
		}
		
		// Now load the properties file.
		
		try {
			config.load(new FileInputStream(propFile));
		} catch (IOException e) {
			log.severe("Unable to load properties file: %s", e.getMessage());
			return false;
		}
		
		return true;
	}
	
}

