package swe.kbk.williamsson.CinnaClaim;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class CCPlayerListener implements Listener {
	private final CinnaClaim plugin;


	public CCPlayerListener(CinnaClaim cinnaClaim) {
		plugin = cinnaClaim;
		plugin.setDbc(plugin.dbc);
		plugin.setLog(plugin.log);
		
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		String playerName = null;
		
		Player player = event.getPlayer();
		playerName = player.getName();
		
//		Checks if the player exists in the database
		if(plugin.getDbc().playerExists(playerName)){
			//Do nothing
		}else{
			//Creates some entries in the database for unexisting players.
			plugin.getDbc().insertPlayerToDB(playerName);
		}
		

	}

	
	
	
	
	
}
