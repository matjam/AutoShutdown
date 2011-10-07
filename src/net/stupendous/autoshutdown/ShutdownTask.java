package net.stupendous.autoshutdown;

import java.util.Calendar;
import java.util.Locale;
import java.util.Date;
import java.util.TimerTask;

import net.stupendous.autoshutdown.misc.*;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Server;
import org.bukkit.World;

public class ShutdownTask extends TimerTask {
	protected AutoShutdownPlugin plugin = null;
	protected Log log = null;
	
	ShutdownTask(AutoShutdownPlugin instance) {
		plugin = instance;
		log = plugin.log;
	}
	
	public void run() {
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				log.info("Shutting down now!");
				
				plugin.kickAll();
				
				plugin.getServer().savePlayers();
				Server server = plugin.getServer();
				
				// We'll save everything manually. I don't think this can hurt.
				
				server.savePlayers();
				
				for (World world: server.getWorlds()) {
					server.unloadWorld(world, true);
				}
				
				server.shutdown();
				
				log.info("Server shut down."); // Should anyone see this message?	
			}
		});
	}

}
