package com.ixtom.uds.model;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "CompetitionParticipant")
public class CompetitionEntry {

    @EmbeddedId
    private CompetitionEntryId id;

    @MapsId("competitionId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competition_id")
    private Competition competition;

    @MapsId("duelistId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "duelist_id")
    private Duelist duelist;

    private int score = 0;
    private int round = 1;
    private boolean eliminated = false;

    public CompetitionEntry() {}

	public CompetitionEntryId getId() {
		return id;
	}

	public void setId(CompetitionEntryId id) {
		this.id = id;
	}

	public Competition getCompetition() {
		return competition;
	}

	public void setCompetition(Competition competition) {
		this.competition = competition;
	}

	public Duelist getDuelist() {
		return duelist;
	}

	public void setDuelist(Duelist duelist) {
		this.duelist = duelist;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public int getRound() {
		return round;
	}

	public void setRound(int round) {
		this.round = round;
	}

	public boolean isEliminated() {
		return eliminated;
	}

	public void setEliminated(boolean eliminated) {
		this.eliminated = eliminated;
	}


    @Embeddable
    public static class CompetitionEntryId implements Serializable {
        private Long competitionId;
        private Long duelistId;
        public CompetitionEntryId() {}

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;

            CompetitionEntryId other = (CompetitionEntryId) obj;

            return Objects.equals(competitionId, other.competitionId)
                && Objects.equals(duelistId, other.duelistId);
        }

        @Override
        public int hashCode() {
            return 31 * Objects.hashCode(competitionId) + Objects.hashCode(duelistId);
        }
    }
}
