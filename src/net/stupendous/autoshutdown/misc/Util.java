/* Util
 * 
 * This is class that contains a dumping ground of methods that need
 * to be used through the code, but don't make sense in any other class. 
 * 
 * Maybe someone who really knows Java well can suggest a better
 * solution to me.
 */

package net.stupendous.autoshutdown.misc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public class Util {
	public static String pluginName = "DERP";
	public static Log log = null;
	public static Plugin plugin = null;
	
	public static void init(Plugin plugin, String pluginName, Log log) {
		Util.plugin = plugin;
		Util.pluginName = pluginName;
		Util.log = log;
	}
	
	public static String parseColor(String s) {
		return parseColor("%s", s);
	} 
	
	public static String parseColor(String format, Object... args) {
		String pre = String.format(format, args);
		StringBuffer post = new StringBuffer();
		
		Pattern p = Pattern.compile("\\&([0-9a-fA-F&])");
		Matcher m = p.matcher(pre);
		
		while (m.find()) {
			if (m.group(1).equalsIgnoreCase("&")) {
				m.appendReplacement(post, "&");
			} else {
				int colorCode = Integer.parseInt(m.group(1), 16);
				String color;
				
				switch (colorCode) {
				case 0:
					color = ChatColor.BLACK.toString();
					break;
				case 1:
					color = ChatColor.DARK_BLUE.toString();
					break;
				case 2:
					color = ChatColor.DARK_GREEN.toString();
					break;
				case 3:
					color = ChatColor.DARK_AQUA.toString();
					break;
				case 4:
					color = ChatColor.DARK_RED.toString();
					break;
				case 5:
					color = ChatColor.DARK_PURPLE.toString();
					break;
				case 6:
					color = ChatColor.GOLD.toString();
					break;
				case 7:
					color = ChatColor.GRAY.toString();
					break;
				case 8:
					color = ChatColor.DARK_GRAY.toString();
					break;
				case 9:
					color = ChatColor.BLUE.toString();
					break;
				case 10:
					color = ChatColor.GREEN.toString();
					break;
				case 11:
					color = ChatColor.AQUA.toString();
					break;
				case 12:
					color = ChatColor.RED.toString();
					break;
				case 13:
					color = ChatColor.LIGHT_PURPLE.toString();
					break;
				case 14:
					color = ChatColor.YELLOW.toString();
					break;
				case 15:
					color = ChatColor.WHITE.toString();
					break;
				default:
					color = ChatColor.WHITE.toString();
				}
				m.appendReplacement(post, color);
			}
		}
		
		m.appendTail(post);
		
		return post.toString();
	}
	
	public static void replyError(CommandSender sender, String s) {
		Util.replyError(sender, "%s", s);
	}
	
    public static void replyError(CommandSender sender, String format, Object... args) {
    	String msg = String.format(format, args);
    	String formattedMessage = parseColor("&2[&a%s&2] &c%s", pluginName, msg); 
    	
    	if (sender == null) {
    		log.info(formattedMessage);
    	} else {
    		sender.sendMessage(formattedMessage);
    	}
    }

	public static void reply(CommandSender sender, String s) {
		Util.reply(sender, "%s", s);
	}

    public static  void reply(CommandSender sender, String format, Object... args) {
    	String msg = String.format(format, args);
    	String formattedMessage = parseColor("&2[&a%s&2] &f%s", pluginName, msg); 
    	
    	if (sender == null) {
    		log.info(formattedMessage);
    	} else {
    		sender.sendMessage(formattedMessage);
    	}
    }

    public static String getJoinedStrings(String[] args, int initialIndex) {
        StringBuilder buffer = new StringBuilder();
        for (int i = initialIndex; i < args.length ; i++) {
            if (i != initialIndex)
            	buffer.append(" ");
            
            buffer.append(args[i]);
        }
        return buffer.toString();
    }

	public static void broadcast(String s) {
		Util.broadcast("%s", s);
	}
	
	public static void broadcast(String format, Object...args) {
    	String msg = String.format(format, args);
    	String formattedMessage = parseColor("&2[&a%s&2] &f%s", pluginName, msg); 
    	
    	plugin.getServer().broadcastMessage(formattedMessage);
	}
}
