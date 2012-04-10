package swe.kbk.williamsson.CinnaClaim;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;


public class CinnaClaim extends JavaPlugin {
	
	public Logger log = Logger.getLogger("Minecraft");
	public WorldGuardPlugin wg = null;
	public DBConnector dbc;
	public SettingsManager sm;
	public RegionManager rm;
	public CCPlayerListener pl;
	public FriendHandeler fh;
	
	public void onEnable() {
		//Instantiera variables, or whatever we're doing
		ClaimHandeler claimListener = new ClaimHandeler(this);
		UnclaimHandeler unclaimListener = new UnclaimHandeler(this);
		fh = new FriendHandeler(this);
		dbc = new DBConnector(this);
		sm = new SettingsManager(this);
		pl = new CCPlayerListener(this);
		
		wg = getWorldGuardPlugin();
		
		 //Log that we're up and running!
		PluginDescriptionFile pdFile = this.getDescription();
		this.log.info(pdFile.getName() + " version " + pdFile.getVersion()
				+ " has been enabled!");
		
		getServer().getPluginManager().registerEvents(new CCPlayerListener(this), this);
		
		//Register that we want theese commands.
		getCommand("claim").setExecutor(claimListener);
		getCommand("unclaim").setExecutor(unclaimListener);
		getCommand("friend").setExecutor(fh);
		getCommand("cc").setExecutor(sm);
		
		dbc.setupDB();
	}
	
	public void onDisable() {
		log.info("[" + getDescription().getName() + "] Has been disabled!");
		
		try {
			dbc.dbhndl.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	//Hook into WorldGuard
	 private WorldGuardPlugin getWorldGuardPlugin() {
	        // Return value variable, by default null
	        WorldGuardPlugin wg = null;
	       
	        // Try to get the plugin instance
	        Plugin p = getServer().getPluginManager().getPlugin("WorldGuard");
	       
	        // Make sure its not null and its WorldGuard
	        if ((p != null) && (p instanceof WorldGuardPlugin)) {
	            // Save the instance of WorldGuard in the return value variable;
	            wg = (WorldGuardPlugin) p;
	            log.info("[MyPlugin] WorldGuard v" + p.getDescription().getVersion() + " detected! Enabling WorldGuard Support.");
	        }

	        // Return the instance (or null if no WorldGuard found)
	        return wg;
	    }
	 
	 //Get regions on a players position
	 public List<String> getRegions(Player player, RegionManager rm) {
			// Get the players position (as a WorldEdit Vector)
			Vector playerVector = new Vector(player.getLocation().getX(),
					player.getLocation().getY(), player.getLocation().getZ());
			
			List<String> regionIds = rm.getApplicableRegionsIDs(playerVector);
			return regionIds;
		}
	 
	 //Takes a list of regions and returns those without an owner
	 public List<String> getUnownedRegions(List<String> regionIDs, RegionManager rm){
			List<String> playersRegions = new ArrayList<String>();
			
			for(String hurr : regionIDs){
				if((rm.getRegion(hurr).getOwners()).getPlayers().isEmpty()){
					playersRegions.add(hurr);
				}
			}
			return playersRegions;
		}
	 
	 //Takes a list of regions, and I don't know what this does.
	 public String regionHasOwner(List<String> regionIds, RegionManager rm){
		 
		 for(String hurr : regionIds){
			 if(!rm.getRegion(hurr).hasMembersOrOwners()){
				 return hurr;
			 }
		 }
		 
		 return null;
	 }
	 
	 //Takes a List of regions, and checks something.
	 public String checkRegionHasOwner(Player player, RegionManager rm,
				List<String> regionIds) {
			String regionName = this.regionHasOwner(regionIds, rm);
			
			return regionName;
		}
	 
	 	//Returns a list with all regions the player stands in
		public List<String> getCurrentRegions(Player player, RegionManager rm) {
			List<String> regionIds = this.getRegions(player, rm);

			return regionIds;
		}

		//Returns a list with all regions the player stands in, and that he is a owner of
		public List<String> getOwnedRegions(List<String> regionIDs, Player player, RegionManager rm){
			List<String> playersRegions = new ArrayList<String>();
			
			for(String hurr : regionIDs){
				if(rm.getRegion(hurr).getOwners().contains(this.wg.wrapPlayer(player))){
					playersRegions.add(hurr);
				}
			}
			return playersRegions;
		}
		
		
		public RegionManager getRegionManager(Player player){
			
			RegionManager rm = this.wg.getRegionManager(player.getWorld());
			
			return rm;
			
		}
		
		/**
		 * @return the dbc
		 */
		DBConnector getDbc() {
			return dbc;
		}

		/**
		 * @param dbc the dbc to set
		 */
		void setDbc(DBConnector dbc) {
			this.dbc = dbc;
		}

		/**
		 * @return the ce
		 */
		CCPlayerListener getCe() {
			return pl;
		}

		/**
		 * @param ce the ce to set
		 */
		void setCe(CCPlayerListener ce) {
			this.pl = ce;
		}

		/**
		 * @return the log
		 */
		Logger getLog() {
			return log;
		}

		/**
		 * @param log the log to set
		 */
		void setLog(Logger log) {
			this.log = log;
		}
}