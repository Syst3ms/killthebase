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
		if (!config.isSet("type")) {
			if (config.isString("type")) {
				String type = config.getString("type");
				try {
					this.teamType = TeamType.valueOf(type.toUpperCase());
				} catch (IllegalArgumentException e) {
					throw new IllegalArgumentException("Le type d'équipes n'est pas reconnu !");
				}
			} else if (config.isInt("type")) {
				int type = config.getInt("type");
				switch (type) {
					case 6:
						this.teamType = TeamType.SIX;
						break;
					case 8:
						this.teamType = TeamType.HUIT;
						break;
					case 12:
						this.teamType = TeamType.DOUZE;
						break;
					default:
						throw new IllegalArgumentException("Le nombre de joueurs ne correspond à aucun type d'équipe !");
				}
			} else {
				throw new IllegalArgumentException("Le format du type d'équipe n'est pas reconnu !");
			}
		} else {
			throw new IllegalArgumentException("Le type d'équipe n'a pas été reconnu !");
		}
        Bukkit.getPluginManager().registerEvents(new LobbyListener(), this);
		Bukkit.getPluginManager().registerEvents(new GameListener(), this);
	}



    public TeamType getTeamType() {
        return teamType;
    }

}
