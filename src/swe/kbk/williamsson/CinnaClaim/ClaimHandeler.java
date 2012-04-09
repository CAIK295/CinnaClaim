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
import org.bukkit.event.Listener;

import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.managers.RegionManager;

public class ClaimHandeler implements Listener, CommandExecutor {
	// instance of the main plugin class
	private final CinnaClaim plugin;

	public ClaimHandeler(CinnaClaim cinnaClaim) {
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
		if(!player.hasPermission("cinnaclaim.claim")){
			player.sendMessage("You don't have permission to do that");
			return true;
		}
			if (args.length > 0) {
				player.sendMessage("Too many arguments!");
			} else{
				return claimRegion(player);
			}
		return true;
	}

	private boolean claimRegion(Player player){
		
		//Make sure we have a worldguard instance
		if (plugin.wg != null){

			// Get WorldGuards Region Manager
			RegionManager rm = plugin.getRegionManager(player);
			
			// Make sure a RegionManager was found
			if (rm != null) {
				
				List<String> regionIds = plugin.getCurrentRegions(player, rm);
				if(regionIds == null){
					player.sendMessage("It seems as if you're not in a claimable region.");
					return true;
				}
				
				List<String> regions = plugin.getUnownedRegions(regionIds, rm);
				
				if(!plugin.getDbc().canPlayerClaim(player.getName(), regionIds)){
					
					for(String region : regions){
						if(plugin.getDbc().isRegionClaimable(region)){
							player.sendMessage("That region isn't claimable.");
							return true;
						}else{
							player.sendMessage("You can't claim this plot, perhaps not enough claim points?");
							return true;
						}
					}
					
				}
				
				for(String region : regions){
					if (setOwner(player, rm, region)) {
						plugin.getDbc().claimRegion(player.getName(), region);
					}
				}
			}
		}
		// No region found at players position
		return true;
	}

	private boolean setOwner(Player player, RegionManager rm, String regionName) {
		
		DefaultDomain domain = new DefaultDomain();
		
		domain.addPlayer(player.getName());
		
		if(!rm.getRegion(regionName).getOwners().contains(plugin.wg.wrapPlayer(player)))
		{
			rm.getRegion(regionName).setOwners(domain);
			
			player.sendMessage("Region claimed!");
			
			try {
				rm.save();
				return true;
			} catch (ProtectionDatabaseException e) {
				e.printStackTrace();
			}
			
		return true;
		}
		else
		{
			player.sendMessage("You can't claim a region you already own!");
			return true;
		}
		
	}
	
	
	
}
