package fr.syst3ms.labgames.killthebase;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class KillTheBase extends JavaPlugin {
	private FileConfiguration config = getConfig();
	private Type teamType;

	@Override
	public void onEnable() {
		super.onEnable();
		if (!config.isSet("type")) {
			if (config.isString("type")) {
				String type = config.getString("type");
				try {
					this.teamType = Type.valueOf(type.toUpperCase());
				} catch (IllegalArgumentException e) {
					throw new IllegalArgumentException("Le type d'équipes n'est pas reconnu !");
				}
			} else if (config.isInt("type")) {
				int type = config.getInt("type");
				switch (type) {
					case 6:
						this.teamType = Type.SIX;
						break;
					case 8:
						this.teamType = Type.HUIT;
						break;
					case 12:
						this.teamType = Type.DOUZE;
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
	}

	private enum Type {
		SIX, HUIT, DOUZE
	}

}
