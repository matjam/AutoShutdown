package net.stupendous.autoshutdown;

import org.bukkit.event.server.*;

public class AutoShutdownPluginListener extends ServerListener {
	AutoShutdownPlugin plugin = null;	
	
	AutoShutdownPluginListener(AutoShutdownPlugin plugin) {
		this.plugin = plugin;
	}
	
	public void onPluginEnable (PluginEnableEvent event) {
		plugin.log.info("Plugin detected: %s", event.getPlugin().toString());
	}
}
