package swe.kbk.williamsson.CinnaClaim;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * 
 * @author Williamsson
 *
 */


public class DBConnector {
	Connection dbhndl;
	JavaPlugin plugin;
	Logger log;
	public static final String DB_FILE =  "CinnaClaim.db";
	
	public DBConnector(JavaPlugin plugin){
		log = Logger.getLogger("swe.kbk.williamsson.CinnaClaim.dbc");
		this.plugin = plugin;
		try {
			Class.forName("org.sqlite.JDBC");
			dbhndl = DriverManager.getConnection("jdbc:sqlite:" + DB_FILE);
			setupDB();
		} catch (ClassNotFoundException e) {
			log.severe("[" + plugin.getDescription().getName() + "] Found ClassNotFoundException. \n Stopping plugin.");
			Bukkit.getPluginManager().disablePlugin(plugin);
		} catch (SQLException e) {
			log.severe("[" + plugin.getDescription().getName() + "] Could not get a Connection to database. \n Stopping plugin.");
			Bukkit.getPluginManager().disablePlugin(plugin);
		}
	}
	
	public void setupDB(){
		try{
			Statement stat = dbhndl.createStatement();
			stat.executeUpdate("CREATE TABLE IF NOT EXISTS player_plots(player_name varchar(65) PRIMARY KEY, sum_plots int(11), max_plots int(11));");
			stat.executeUpdate("CREATE TABLE IF NOT EXISTS plots (plot_id varchar(65) PRIMARY KEY, is_claimable tinyint(2), plot_size int(11));");
		}catch (SQLException e){
			log.severe("Exception while trying to create relations.");
		}
	}
	
	public String scrub(String s){
		return s.replaceAll("[\'|\"|;|/|\\|\n|\r|\0]", "");
	}
	
	public void setPlotSize(String region, int plotSize){
		if(region.equals(null)){
			
		}else{
			try {
				PreparedStatement prepSwitch = dbhndl.prepareStatement("insert into plots values (?,1,?);");
				prepSwitch.setString(1, region);
				prepSwitch.setInt(2, plotSize);
				prepSwitch.executeUpdate();
			} catch (SQLException e) {
				log.severe("Exception while trying to insert plot size to database.");
				e.printStackTrace();
			}
		}
	}
	
	public int getPlotSize(String plotId){
		try {
				PreparedStatement prepSwitch = dbhndl.prepareStatement("SELECT plot_size FROM plots WHERE plot_id = '" + scrub(plotId) + "'");
				prepSwitch.execute();
				int plotSize = 0;
				ResultSet rs = prepSwitch.getResultSet();
				while(rs.next()) {
					plotSize += rs.getInt("plot_size");
					if(plotSize != 0){
						return plotSize;
					}
				}
		} catch (SQLException e) {
			log.severe("Exception while trying to get plot size from database.");
			e.printStackTrace();
		}
		return 0;
	}
	public boolean isRegionClaimable(String region){
		try {
			int claimable = 0;
				PreparedStatement prepSwitch = dbhndl.prepareStatement("SELECT is_claimable FROM plots WHERE plot_id = ?");
				prepSwitch.setString(1, scrub(region));
				prepSwitch.execute();
				ResultSet rs = prepSwitch.getResultSet();
				while(rs.next()) {
					claimable += rs.getInt("is_claimable");
						if(claimable == 1){
							return true;
						}else{
							return false;
						}
				}
		} catch (SQLException e) {
			log.severe("Exception while trying to set get claimability.");
			e.printStackTrace();
		}
		return false;
	}
	
	
	public int getPlayerSumPlots(String player){
		try {
			Statement stat = dbhndl.createStatement();
			int sumPlots = 0;
			ResultSet rs = stat.executeQuery("SELECT sum_plots FROM player_plots WHERE player_name = '" + scrub(player) + "'");
			while(rs.next()) {
				sumPlots += rs.getInt("sum_plots");
			}
			
			return sumPlots;
		} catch (SQLException e) {
			log.severe("Exception while trying to get " + player + "'s sum plots");
			e.printStackTrace();
			return 0;
		}
	}
	
	public int getPlayerMaxPlots(String player){
		try {
			Statement stat = dbhndl.createStatement();
			int maxPlots = 0;
			ResultSet rs = stat.executeQuery("SELECT max_plots FROM player_plots WHERE player_name = '" + scrub(player) + "'");
			
			while(rs.next()) {
				maxPlots += rs.getInt("max_plots");
			}
			
			return maxPlots;
		} catch (SQLException e) {
			e.printStackTrace();
			log.severe("Exception while trying to get " + player + "'s max plots");
			return 0;
		}
	}
	
	public boolean canPlayerClaim(String player, List<String> regions){
		
		for(String region : regions){
			if(isPlotClaimable(region)){
				int playerMaxPlots = getPlayerMaxPlots(player);
				int playerSumPlots = getPlayerSumPlots(player);
				
				if((playerSumPlots + getPlotSize(region) ) <= playerMaxPlots ){
						return true;
				}else{
					return false;
				}
			}
		}
		return false;
	}
	
	public void claimRegion(String player, String region){
		try {
			int playerSumPlots = 0;
			
			playerSumPlots = getPlayerSumPlots(player);
			playerSumPlots += getPlotSize(region);
			
			PreparedStatement prepSwitch = dbhndl.prepareStatement("UPDATE player_plots SET sum_plots = (?) WHERE player_name = (?)");
			prepSwitch.setInt(1, playerSumPlots);
			prepSwitch.setString(2, player);
			prepSwitch.executeUpdate();
			player = null;
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void declaimRegion(String player, String region){
		try {
			int playerSumPlots = 0;
			playerSumPlots = getPlayerSumPlots(player);
			playerSumPlots -= getPlotSize(region);
			
			PreparedStatement prepSwitch = dbhndl.prepareStatement("UPDATE player_plots SET sum_plots = (?) WHERE player_name = (?)");
			prepSwitch.setInt(1, playerSumPlots);
			prepSwitch.setString(2, player);
			prepSwitch.executeUpdate();
			player = null;
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	public boolean isPlotClaimable(String plotId){
		
		try{
			Statement stat = dbhndl.createStatement();
			int is_claimable = 0;
			ResultSet rs = stat.executeQuery("SELECT is_claimable FROM plots WHERE plot_id = '" + plotId + "'");
			
			while(rs.next()) {
				is_claimable += rs.getInt("is_claimable");
			}
			if(is_claimable == 1){
				return true;
			}else{
				return false;
			}
		}catch (SQLException e){
			e.printStackTrace();
			log.severe("Exception while trying to get " + plotId + "'s size");
			return false;
		}
	}

	public void insertPlayerToDB(String playerName) {
		try {
				PreparedStatement prepSwitch = dbhndl.prepareStatement("insert into player_plots values (?,0,1);");
				prepSwitch.setString(1, playerName);
				prepSwitch.executeUpdate();
		} catch (SQLException e) {
			log.severe("Exception while trying to insert player to database.");
			e.printStackTrace();
		}
	}

	
	public boolean playerExists(String player) {
		try {
			PreparedStatement checkStat = dbhndl.prepareStatement("select * from player_plots where player_name=(?);");
			checkStat.setString(1, player);
			ResultSet rs = checkStat.executeQuery();
			return rs.next();

		} catch (SQLException e) {
			log.severe("[" + plugin.getDescription().getName() + "] Exception while trying to find a datapost.");
			log.severe(e.getMessage());
			return false;
		}
	}
	
	public boolean setPlayerMaxPlots(String player, int plots){
		
		try {
			
			PreparedStatement checkStat = dbhndl.prepareStatement("update player_plots set max_plots = ? where player_name = ?");
			checkStat.setInt(1, plots);
			checkStat.setString(2, (player));
			checkStat.executeUpdate();
			return true;
			
		} catch(SQLException e){
			
			log.severe("[" + plugin.getDescription().getName() + "] Exception while trying to find a datapost.");
			log.severe(e.getMessage());
			return false;
		}
		
		
	}
	
}
