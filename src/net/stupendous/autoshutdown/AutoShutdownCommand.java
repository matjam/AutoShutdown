package net.stupendous.autoshutdown;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;

import net.stupendous.autoshutdown.misc.*;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AutoShutdownCommand implements CommandExecutor {
	private final AutoShutdownPlugin plugin;
	private final Log log;
	
	public AutoShutdownCommand(AutoShutdownPlugin plugin) {
		this.plugin = plugin;
		this.log = plugin.log;
	}

	enum SubCommand {
		HELP,
		RELOAD,
		CANCEL,
		SET,
		LIST,
		UNKNOWN;
		
		private static SubCommand toSubCommand(String str) {
			try {
				return valueOf(str);
			} catch (Exception ex) {
				return UNKNOWN;
			}
		}
	}
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			if (!((Player)sender).hasPermission("autoshutdown.admin")) {
				Util.replyError(sender, "You don't have permission to use that command.");
				return true;
			}
		}
				
    	if (args.length == 0) {
    		Util.replyError(sender, "Not enough arguments to command. Use /as help to list available commands.");
			return true;
    	}
    	
    	switch (SubCommand.toSubCommand(args[0].toUpperCase())) {
	    	case HELP:
	    		Util.reply(sender, "AutoShutdown plugin help:");
	    		Util.reply(sender, " /%s help", command.getName());
	    		Util.reply(sender, "     Shows this help page");
	    		Util.reply(sender, " /%s reload", command.getName());
	    		Util.reply(sender, "     Reloads the configuration file");
	    		Util.reply(sender, " /%s cancel", command.getName());
	    		Util.reply(sender, "     Cancels the currently executing shutdown");
	    		Util.reply(sender, " /%s set HH:MM:SS", command.getName());
	    		Util.reply(sender, "     Sets a new scheduled shutdown time");
	    		Util.reply(sender, " /%s set now", command.getName());
	    		Util.reply(sender, "     Orders the server to shutdown immediately");
	    		Util.reply(sender, " /%s list", command.getName());
	    		Util.reply(sender, "     lists the currently scheduled shutdowns");
	    		break;
	    	case RELOAD:
	    		plugin.loadConfiguration();
	    		plugin.scheduleAll();
	    		Util.reply(sender, "Configuration reloaded.");
	    		break;
	    	case CANCEL:
	    		if (plugin.shutdownTimer != null) {
	    			plugin.shutdownTimer.cancel();
	    			plugin.shutdownTimer.purge();
	    			plugin.shutdownTimer = null;
		    		plugin.shutdownImminent = false;
		    		
		    		Util.reply(sender, "Shutdown was aborted.");
	    		} else {
	    			Util.replyError(sender, "There is no impending shutdown. If you wish to remove");
	    			Util.replyError(sender, "a scheduled shutdown, remove it from the configuration");
	    			Util.replyError(sender, "and reload.");
	    		}
	    		break;
	    	case SET:
	    		if (args.length < 2) {
	    			Util.replyError(sender, "Usage:");
	    			Util.replyError(sender, "   /as set <time>");
	    			Util.replyError(sender, "<time> can be either 'now' or a 24h time in HH:MM format.");
	    			return true;
	    		}
	    		
	    		Calendar stopTime = null;
	    		
				try {
					stopTime = plugin.scheduleShutdownTime(args[1]);
				} catch (Exception e) {
	    			Util.replyError(sender, "Usage:");
	    			Util.replyError(sender, "   /as set <time>");
	    			Util.replyError(sender, "<time> can be either 'now' or a 24h time in HH:MM format.");
				}
				
				Util.reply(sender, "Shutdown scheduled for %s", stopTime.getTime().toString());

				String timeString = "";
				
				for (Calendar shutdownTime : plugin.shutdownTimes) {
					if (plugin.shutdownTimes.first().equals(shutdownTime)) {
						timeString = timeString.concat(String.format("%d:%02d", shutdownTime.get(Calendar.HOUR_OF_DAY), shutdownTime.get(Calendar.MINUTE)));
					} else {
						timeString = timeString.concat(String.format(",%d:%02d", shutdownTime.get(Calendar.HOUR_OF_DAY), shutdownTime.get(Calendar.MINUTE)));
					}
				}

				plugin.config.setProperty("shutdowntimes", timeString);
				
				try {
					plugin.config.save();
				} catch (Exception e) {
					Util.replyError(sender, "Unable to save configuration: %s", e.getMessage());
				}
				
	    		break;
	    	case LIST:
	    		if (plugin.shutdownTimes.size() != 0) { 
					Util.reply(sender, "Shutdowns scheduled at");
					for (Calendar shutdownTime : plugin.shutdownTimes) {
						Util.reply(sender, "   %s", shutdownTime.getTime().toString());
					}
	    		} else {
					Util.replyError(sender, "No shutdowns scheduled.");
	    		}
	    		break;
	    	case UNKNOWN:
				Util.replyError(sender, "Unknown command. Use /as help to list available commands.");
    	}
    	
		return true;
	}

	
    private void reply(CommandSender sender, String message) {
    	if (sender == null) {
    		log.info(message);
    	} else {
    		sender.sendMessage(message);
    	}
    }
}