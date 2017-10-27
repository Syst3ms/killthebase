package fr.syst3ms.labgames.killthebase.enums;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * Created by ARTHUR on 13/10/2017.
 */
public enum TeamType {
	SIX(5, Sets.newHashSet(Team.BLEU, Team.ROUGE), 3, "2x3"), HUIT(6, Sets.newHashSet(Team.values()), 2, "4x2"), DOUZE(
		10,
		Sets.newHashSet(Team.values()),
		3,
		"4x3");

	private final int requiredPlayerAmount;
	private final Set<Team> allowedTeams;
	private final int maxTeamPlayerAmount;
	private final String displayName;

	TeamType(int requiredPlayerAmount, Set<Team> allowedTeams, int maxTeamPlayerAmount, String displayName) {
		this.requiredPlayerAmount = requiredPlayerAmount;
		this.allowedTeams = allowedTeams;
		this.maxTeamPlayerAmount = maxTeamPlayerAmount;
		this.displayName = displayName;
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
}
