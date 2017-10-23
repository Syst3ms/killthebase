package fr.syst3ms.labgames.killthebase.classes;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.sun.org.apache.xpath.internal.operations.Mult;
import fr.labgames.api.utils.messages.MessageManager;
import fr.syst3ms.labgames.killthebase.enums.TeamColor;
import fr.syst3ms.labgames.killthebase.enums.TeamType;
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
    private Map<Player, TeamColor> playerToTeamMap = new HashMap<>();
    private Multimap<TeamColor, Player> teamToPlayerMap = HashMultimap.create();

    public TeamManager(TeamType teamType) {
        this.teamType = teamType;
    }

    public Multimap<TeamColor, Player> getTeamToPlayerMap() {
        return teamToPlayerMap;
    }

    public void requestTeamJoin(TeamColor team, Player p) {
        if (playerToTeamMap.get(p) == team){
            switch (team) {
                case BLEU:
                    p.sendMessage(MessageManager.getMessageGameAlreadyJoinTeamBlue());
                    break;
                case ROUGE:
                    p.sendMessage(MessageManager.getMessageGameAlreadyJoinTeamRed());
                    break;
                case VERT:
                    p.sendMessage(MessageManager.getMessageGameAlreadyJoinTeamGreen());
                    break;
                case JAUNE:
                    p.sendMessage(MessageManager.getMessageGameAlreadyJoinTeamYellow());
                    break;
            }
            return;
        }
        assert teamType.getAllowedTeamColors().contains(team);
        if (playerToTeamMap.values().stream().filter(tc -> tc == team).count() == teamType.getMaxTeamPlayerAmount()) {
            switch (team) {
                case BLEU:
                    p.sendMessage(MessageManager.getMessageGameJoinTeamBlueFull());
                    break;
                case ROUGE:
                    p.sendMessage(MessageManager.getMessageGameJoinTeamRedFull());
                    break;
                case VERT:
                    p.sendMessage(MessageManager.getMessageGameJoinTeamGreenFull());
                    break;
                case JAUNE:
                    p.sendMessage(MessageManager.getMessageGameJoinTeamYellowFull());
                    break;
            }
        } else {
            playerToTeamMap.put(p, team);
            teamToPlayerMap.put(team, p);
            switch (team) {
                case BLEU:
                    p.sendMessage(MessageManager.getMessageGameJoinTeamBlue());
                    break;
                case ROUGE:
                    p.sendMessage(MessageManager.getMessageGameJoinTeamRed());
                    break;
                case VERT:
                    p.sendMessage(MessageManager.getMessageGameJoinTeamGreen());
                    break;
                case JAUNE:
                    p.sendMessage(MessageManager.getMessageGameJoinTeamYellow());
                    break;
            }
        }
    }

    public void removePlayer(Player p) {
        teamToPlayerMap.remove(getTeam(p), p);
        playerToTeamMap.remove(p);
    }

    public TeamColor getTeam(Player p) {
        return playerToTeamMap.get(p);
    }

    public List<Player> getPlayers(TeamColor team) {
        return new ArrayList<>(teamToPlayerMap.get(team));
    }

    public Map<Player, TeamColor> getPlayerToTeamMap() {
        return playerToTeamMap;
    }

    public TeamType getTeamType() {
        return teamType;
    }
}
