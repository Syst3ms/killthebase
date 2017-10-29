package fr.syst3ms.labgames.killthebase.enums;

import com.google.common.collect.Sets;

import java.util.Set;
import java.util.stream.Stream;

/**
 * Created by ARTHUR on 13/10/2017.
 */
public enum TeamType {
	SIX(5, Sets.newHashSet(Team.BLEU, Team.ROUGE), 3, "2x3", 6),
	HUIT(6, Sets.newHashSet(Team.values()), 2, "4x2", 8),
	DOUZE(10, Sets.newHashSet(Team.values()), 3, "4x3", 12);

	private final int requiredPlayerAmount;
	private final Set<Team> allowedTeams;
	private final int maxTeamPlayerAmount;
	private final String displayName;
    private final int totalPlayerAmount;

    TeamType(int requiredPlayerAmount, Set<Team> allowedTeams, int maxTeamPlayerAmount, String displayName, int totalPlayerAmount) {
		this.requiredPlayerAmount = requiredPlayerAmount;
		this.allowedTeams = allowedTeams;
		this.maxTeamPlayerAmount = maxTeamPlayerAmount;
		this.displayName = displayName;
        this.totalPlayerAmount = totalPlayerAmount;
    }

	public int getRequiredPlayerAmount() {
		return requiredPlayerAmount;
	}

	public Set<Team> getAllowedTeams() {
		return allowedTeams;
	}

	public int getMaxTeamPlayerAmount() {
		return maxTeamPlayerAmount;
	}

	public String getDisplayName() {
		return displayName;
	}

	public static TeamType byMaxPlayers(int maxPlayers) {
		return Stream.of(values()).filter(tt -> tt.totalPlayerAmount == maxPlayers).findFirst().orElseThrow(() -> new IllegalArgumentException((maxPlayers + " is not a valid player amount !")));
	}

    public int getTotalPlayerAmount() {
        return totalPlayerAmount;
    }
}
