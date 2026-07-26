package dev.meowmayo.mmcore.utils.tracker;

import java.util.ArrayList;
import java.util.List;

public class Tiers {
    List<Phase> phases;
    private String tier;

    public Tiers(String tier) {
        this.phases = new ArrayList<>();
        this.tier = tier;
    }

    public void addPhase(Phase phase) {
        phases.add(phase);
    }

    public List<Phase> getPhases() {
        return phases;
    }

    public int getTierCount() {
        return phases.size();
    }

    public String getTierName() {
        return tier;
    }
}
