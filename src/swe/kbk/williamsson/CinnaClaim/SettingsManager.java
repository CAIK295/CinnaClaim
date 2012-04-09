package swe.kbk.williamsson.CinnaClaim;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.sk89q.worldguard.protection.managers.RegionManager;

public class SettingsManager implements CommandExecutor, Listener {
	// instance of the main plugin class
	private final CinnaClaim plugin;
	DBConnector dbc;

	public SettingsManager(CinnaClaim cinnaClaim) {
		// Save the instance of the main plugin class
		plugin = cinnaClaim;
		dbc = plugin.getDbc();

		// Register events
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		
		//Only a player will be able to do this
		Player player = null;
		int sizeint = 0;
		if (sender instanceof Player) {
			player = (Player) sender;
		} else {
			sender.sendMessage("["
					+ plugin.getDescription().getName()
					+ "] this command can only be performed by an actual player.");
			return true;
		}
		
		if (args.length <= 0) {
			return false;
		}
		
		//Permission check
		if(!player.hasPermission("cinnaclaim.cc")){
			return false;
		}
		
		
		//Gets our RegionManager
		RegionManager rm = plugin.getRegionManager(player);
		//Gets all applicable regions
		List<String> plotIds = plugin.getRegions(player, rm);
		
		if(plotIds.isEmpty()){
			player.sendMessage("You're not in a region");
			return true;
		}
		
		
		//command /cc setsize
			if (args[0].equals("setsize")) {
				try {
					sizeint = new Integer(args[1]).intValue();
	
				} catch (Exception e) {
	
					player.sendMessage("Invalid size! :(");
				}
	
				List<String> durr = plugin.getUnownedRegions(plotIds, rm);
	
				for (String region : durr) {
					dbc.setPlotSize(region, sizeint);
					player.sendMessage("Plot has a size of "
							+ dbc.getPlotSize(region));
				}
			}
			
			//Command /cc getsize
			else if (args[0].equals("getsize") && player.hasPermission("cinnaclaim.cc")) {
				
				plotIds = plugin.getRegions(player, rm);

				for (String region : plotIds) {
					player.sendMessage("Plot's size is: " + dbc.getPlotSize(region));
				}

			}
			
			//Command /cc playerplots
			//Returns how many plots a player has, and how many avalible
			else if (args[0].equals("playerplots") && player.hasPermission("cinnaclaim.cc")){
				
				int playerMaxPlots = 0;
				int playerSumPlots = 0;
				
				if(args.length < 2){
					player.sendMessage("You must specify a playername!");
					return false;
				}else{
					
					playerMaxPlots = dbc.getPlayerMaxPlots(args[1]);
					playerSumPlots = dbc.getPlayerSumPlots(args[1]);
					
					player.sendMessage(args[1] + " currently owns " + playerSumPlots + " and have a total of " + playerMaxPlots + " plots.");
					return true;
				}
				
			}
			
			//Command /cc set <player> <plots>
			//sets a players max_plots to that many plots
			else if(args[0].equals("set") && player.hasPermission("cinnaclaim.cc")){
				
				if(args.length < 3){
					player.sendMessage("Not enough arguments, provide a playername and plots to set.");
					return false;
				}else{
					String playerName = args[1];
					int plots = Integer.parseInt(args[2]);
					
					
					if(dbc.setPlayerMaxPlots(playerName, plots)){
						player.sendMessage("Successfully set player " + args[1] + "'s plots to " + dbc.getPlayerMaxPlots(args[1]));
						return true;
					}else{
						player.sendMessage("Something went wrong and I don't know what.");
						return true;
					}
				}
			}
			
	
			
		return true;
	}
}