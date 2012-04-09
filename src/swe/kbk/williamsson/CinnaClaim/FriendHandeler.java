package swe.kbk.williamsson.CinnaClaim;

/**
 * @AUTHOR Williamsson
 * @AUTHOR Jw1342
 * @AUTHOR Zephyyrr
 */
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class FriendHandeler implements CommandExecutor {
	private final CinnaClaim plugin;

	public FriendHandeler(CinnaClaim cinnaClaim) {
		// Save the instance of the main plugin class
		plugin = cinnaClaim;
	}
	
	@Override
	public boolean onCommand(CommandSender arg0, Command arg1, String arg2,	String[] arg3) {
		
		//Gets our player, if it's not a player, complain.
		Player player = null;
		if (arg0 instanceof Player) {
			player = (Player) arg0;
		}else{
			arg0.sendMessage("["
					+ plugin.getDescription().getName()
					+ "] this command can only be performed by an actual player.");
			return true;
		}
		if(!player.hasPermission("cinnaclaim.friend")){
			return false;
		}
		
		//Gets our region manager
		RegionManager rm = plugin.wg.getRegionManager(player.getWorld());
		
		//Checks if the player is providing enough arguments
		if(arg3.length < 2){
			player.sendMessage("You're not providing enough arguments!");
			return true;
		}
		
		if(plugin.getRegions(player, rm).equals(null)){
			player.sendMessage("You're not in a region!");
			return true;
		}
		
		//Command /friend add
		if(arg3[0].equals("add")){
			
			List<String> regions = plugin.getOwnedRegions(plugin.getRegions(player, rm), player, rm);
			String friend = null;
			
			//Ensures that the command sender gives us a player to add as a member
			if(arg3[1].length() > 0){
				friend = arg3[1];
			}else{
				player.sendMessage("You need to specify a playername!");
				return false;
			}
			
			for(String region : regions){
				addFriend(friend, rm.getRegion(region));
				player.sendMessage("Player " + friend + " has been added to the members of your region(s)!");
				return true;
			}
		}
		
		//Command /friend remove
		if(arg3[0].equals("remove") && player.hasPermission("cinnaclaim.friend")){
			List<String> regions = plugin.getOwnedRegions(plugin.getRegions(player, rm), player, rm);
			String friend = null;
			
			//Ensures that the command sender gives us a player to remove as a member
			if(arg3[1].length() > 0){
				friend = arg3[1];
			}else{
				player.sendMessage("You need to specify a playername");
				return false;
			}
			
			for(String region : regions){
				removeFriend(friend, rm.getRegion(region));
				player.sendMessage("Player " + friend + " has been removed from the members of your region(s)!");
				return true;
			}
		}
		
		return true;
	}
	
	
	
	//Obviously, adds a friend (eg a member) to the region
	private void addFriend(String player, ProtectedRegion region){
		
		DefaultDomain dom = region.getMembers();
		dom.addPlayer(player);
		region.setMembers(dom);
	}
	
	
	//Obviously, removes a friend (eg a member) from the region
	private void removeFriend(String player, ProtectedRegion region){
		
		DefaultDomain dom = region.getMembers();
		dom.removePlayer(player);
		region.setMembers(dom);
	}

}
