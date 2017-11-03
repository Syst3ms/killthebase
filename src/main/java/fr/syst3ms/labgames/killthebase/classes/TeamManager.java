package fr.syst3ms.labgames.killthebase.classes;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import fr.labgames.api.utils.messages.MessageManager;
import fr.syst3ms.labgames.killthebase.enums.Team;
import fr.syst3ms.labgames.killthebase.enums.TeamType;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ARTHUR on 13/10/2017.
 */
public class TeamManager {
	private final TeamType teamType;
	private Map<Player, Team> playerToTeamMap = new HashMap<>();
	private Multimap<Team, Player> teamToPlayerMap = HashMultimap.create();

	public TeamManager(TeamType teamType) {
		this.teamType = teamType;
	}

	public Multimap<Team, Player> getTeamToPlayerMap() {
		return teamToPlayerMap;
	}

	public void requestTeamJoin(Team team, Player p) {
		if (playerToTeamMap.get(p) == team) {
            p.sendMessage("\u00a77Tu est d\u00e9j\u00e0 dans l'\u00e9quipe " + team.getColor() + team.getFeminine());
            return;
		}
		assert teamType.getAllowedTeams().contains(team);
		if (teamToPlayerMap.get(team).size() >= teamType.getMaxTeamPlayerAmount()) {
			p.sendMessage("L\'\u00e9quipe " + team.getColor() + team.getFeminine() + ChatColor.GRAY + " est déjà pleine !");
		} else {
			playerToTeamMap.put(p, team);
			teamToPlayerMap.put(team, p);
			p.sendMessage("\u00a77Tu as rejoint l\'\u00e9quipe " + team.getColor() + team.getFeminine());
		}
	}

	public void removePlayer(Player p) {
		teamToPlayerMap.remove(getTeam(p), p);
		playerToTeamMap.remove(p);
	}

	public Team getTeam(Player p) {
		return playerToTeamMap.get(p);
	}

	public List<Player> getPlayers(Team team) {
		return new ArrayList<>(teamToPlayerMap.get(team));
	}

	public Map<Player, Team> getPlayerToTeamMap() {
		return playerToTeamMap;
	}

	public TeamType getTeamType() {
		return teamType;
	}
}
