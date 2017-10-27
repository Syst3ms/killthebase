package fr.syst3ms.labgames.killthebase;

import fr.syst3ms.labgames.killthebase.enums.TeamType;
import fr.syst3ms.labgames.killthebase.listener.GameListener;
import fr.syst3ms.labgames.killthebase.listener.LobbyListener;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class KillTheBase extends JavaPlugin {
	private static KillTheBase instance;
	private FileConfiguration config = getConfig();
	private TeamType teamType;

	public static KillTheBase getInstance() {
		return instance;
	}

	@Override
	public FileConfiguration getConfig() {
		return config;
	}

	@Override
	public void onEnable() {
		instance = this;
		teamType = TeamType.byMaxPlayers(Bukkit.getMaxPlayers());
		Bukkit.getPluginManager().registerEvents(new LobbyListener(), this);
		Bukkit.getPluginManager().registerEvents(new GameListener(), this);
	}

	public TeamType getTeamType() {
		return teamType;
	}

}
