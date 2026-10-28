package com.curriculo.inteligente.experience;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class ExperienceSelectionPolicy {

    public static final String DETAILED = "DETAILED";
    public static final String SUMMARY = "SUMMARY";
    public static final String HIDDEN = "HIDDEN";
    public static final String AUTO = "AUTO";

    private ExperienceSelectionPolicy() {
    }

    public record Selection(UUID id, String detailLevel) {
    }

    public static List<Selection> select(List<Experience> items) {
        List<Experience> visible = items.stream()
                .filter(item -> !HIDDEN.equals(item.getDetailPreference()))
                .sorted(recency())
                .toList();
        int autoDetailed = 0;
        List<Selection> selected = new ArrayList<>();
        for (Experience item : visible) {
            String level;
            if (DETAILED.equals(item.getDetailPreference())) {
                level = DETAILED;
            } else if (SUMMARY.equals(item.getDetailPreference())) {
                level = SUMMARY;
            } else if (autoDetailed < 2) {
                level = DETAILED;
                autoDetailed++;
            } else {
                level = SUMMARY;
            }
            selected.add(new Selection(item.getId(), level));
        }
        return selected;
    }

    static Comparator<Experience> recency() {
        return Comparator
                .comparing(Experience::isCurrentJob).reversed()
                .thenComparing(Experience::getEndYear, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(Experience::getEndMonth, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(Experience::getStartYear, Comparator.reverseOrder())
                .thenComparing(Experience::getStartMonth, Comparator.nullsLast(Comparator.reverseOrder()));
    }
}
