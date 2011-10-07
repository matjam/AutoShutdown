package net.stupendous.autoshutdown;

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
		IMMEDIATE,
		SET,
		SHOW,
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
	    		Util.reply(sender, "     Cancels the currently scheduled shutdown");
	    		Util.reply(sender, " /%s set HH:MM:SS", command.getName());
	    		Util.reply(sender, "     Sets a new scheduled shutdown time");
	    		Util.reply(sender, " /%s set now", command.getName());
	    		Util.reply(sender, "     Orders the server to shutdown immediately");
	    		Util.reply(sender, " /%s show", command.getName());
	    		Util.reply(sender, "     Shows the currently scheduled shutdown");
	    		break;
	    	case RELOAD:
	    		plugin.configure();
	    		Util.reply(sender, "Configuration reloaded.");
	    		break;
	    	case CANCEL:
	    		if (plugin.timer != null) {
		    		plugin.timer.cancel();
		    		plugin.timer.purge();
		    		plugin.timer = null; // Yeah, you need to do this.

		    		Util.reply(sender, "Shutdown cancelled. The server will shut down at");
		    		Util.reply(sender, "the next scheduled shutdown time.");
		    		
	    			Util.broadcast("&cAutomated Shutdown at &a%s&c has been cancelled by &a%s&c.", 
	    					plugin.stopTime.getTime().toString(),
	    					sender instanceof Player ? ((Player) sender).getName() : "CONSOLE"
	    			);
	    		} else {
	    			Util.replyError(sender, "Shutdown already cancelled.");
	    		}

	    		break;
	    	case SET:
	    		if (args.length == 1) {
	    			Util.replyError(sender, "Please specify a time specifier in the following format:");
	    			Util.replyError(sender, "     HH:MM:SS");
	    			Util.replyError(sender, "     now");
	    			return true;
	    		}
	    		
				try {
					plugin.configure(args[1]);
				} catch (Exception e) {
					Util.replyError(sender, "Unknown format string. Please use &fHH:MM:SS &cor the string '&fnow&c'");				}
	    		
				Util.reply(sender, "Automatic Shutdown Scheduled at");
				Util.reply(sender, "   %s", plugin.stopTime.getTime().toString());

	    		break;
	    	case SHOW:
	    		if (plugin.timer != null) { 
					Util.reply(sender, "Automatic Shutdown Scheduled at");
					Util.reply(sender, "   %s", plugin.stopTime.getTime().toString());
	    		} else {
					Util.replyError(sender, "sNo Automatic Shutdown scheduled.");
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