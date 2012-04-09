package swe.kbk.williamsson.CinnaClaim;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.managers.RegionManager;

public class UnclaimHandeler implements Listener, CommandExecutor {
	// instance of the main plugin class
	private final CinnaClaim plugin;

	public UnclaimHandeler(CinnaClaim cinnaClaim) {
		// Save the instance of the main plugin class
		plugin = cinnaClaim;

		// Register events
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		} else {
			sender.sendMessage("["
					+ plugin.getDescription().getName()
					+ "] this command can only be performed by an actual player.");
			return true;
		}
		
		if(!player.hasPermission("cinnaclaim.unclaim")){
			return false;
		}
			if (args.length > 0) {
				player.sendMessage("Too many arguments! I CAN'T HANDLE THIS SHIT ANYMORE!");
			} else{
				return unclaimRegion(player);
			}
		return true;
	}

	private boolean unclaimRegion(Player player) {
		//Make sure we have a worldguard instance
				if (plugin.wg != null){

					// Get WorldGuards Region Manager
					RegionManager rm = plugin.getRegionManager(player);
					
					// Make sure a RegionManager was found
					if (rm != null) {
						
						Vector playerVector = new Vector(player.getLocation().getX(),
								player.getLocation().getY(), player.getLocation().getZ());
						
						List<String> regionsPlayerStandsIn = rm.getApplicableRegionsIDs(playerVector);
						
						List<String> regions = plugin.getOwnedRegions(regionsPlayerStandsIn, player, rm);
						
						removeOwner(player, rm, regions);
						for(String region : regions){
							plugin.getDbc().declaimRegion(player.getName(), region);
						}
						return true;
					}
				}
				// No region found at players position
				return false;
	}

	
	private void removeOwner(Player player, RegionManager rm, List<String> regions) {
		DefaultDomain domain = new DefaultDomain();
		domain.removePlayer(player.getName());

		for(String region : regions){
			rm.getRegion(region).setOwners(domain);
			player.sendMessage("Region unclaimed!");
			try {
				rm.save();
			} catch (ProtectionDatabaseException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	
	
	
	
	
}