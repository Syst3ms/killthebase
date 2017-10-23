package fr.syst3ms.labgames.killthebase.enums;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * Created by ARTHUR on 13/10/2017.
 */
public enum TeamType {
    SIX(5, Sets.newHashSet(TeamColor.BLEU, TeamColor.ROUGE), 3),
    HUIT(6, Sets.newHashSet(TeamColor.values()), 2),
    DOUZE(10, Sets.newHashSet(TeamColor.values()), 3);

    private final int requiredPlayerAmount;
    private final Set<TeamColor> allowedTeamColors;
    private final int maxTeamPlayerAmount;

    TeamType(int requiredPlayerAmount, Set<TeamColor> allowedTeamColors, int maxTeamPlayerAmount) {
        this.requiredPlayerAmount = requiredPlayerAmount;
        this.allowedTeamColors = allowedTeamColors;
        this.maxTeamPlayerAmount = maxTeamPlayerAmount;
    }

    public int getRequiredPlayerAmount() {
        return requiredPlayerAmount;
    }

    public Set<TeamColor> getAllowedTeamColors() {
        return allowedTeamColors;
    }

    public int getMaxTeamPlayerAmount() {
        return maxTeamPlayerAmount;
    }
}
