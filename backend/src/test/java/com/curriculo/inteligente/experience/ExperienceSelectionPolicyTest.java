package com.curriculo.inteligente.experience;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExperienceSelectionPolicyTest {

    @Test
    void keepsTwoRecentExperiencesDetailedAndSummarizesTheRest() {
        Experience current = experience("Atual", true, null, 2024);
        Experience previous = experience("Anterior", false, 2023, 2020);
        Experience old = experience("Antiga", false, 2015, 2010);
        Experience hidden = experience("Oculta", false, 2012, 2008);
        hidden.setDetailPreference(ExperienceSelectionPolicy.HIDDEN);

        List<ExperienceSelectionPolicy.Selection> selected = ExperienceSelectionPolicy.select(List.of(old, hidden, previous, current));

        assertEquals(3, selected.size());
        assertEquals(ExperienceSelectionPolicy.DETAILED, level(selected, current.getId()));
        assertEquals(ExperienceSelectionPolicy.DETAILED, level(selected, previous.getId()));
        assertEquals(ExperienceSelectionPolicy.SUMMARY, level(selected, old.getId()));
    }

    @Test
    void respectsAnExplicitSummaryPreference() {
        Experience forced = experience("Forçada", true, null, 2024);
        forced.setDetailPreference(ExperienceSelectionPolicy.SUMMARY);
        List<ExperienceSelectionPolicy.Selection> selected = ExperienceSelectionPolicy.select(List.of(forced));
        assertEquals(ExperienceSelectionPolicy.SUMMARY, selected.get(0).detailLevel());
    }

    private static String level(List<ExperienceSelectionPolicy.Selection> selected, UUID id) {
        return selected.stream().filter(item -> item.id().equals(id)).findFirst().orElseThrow().detailLevel();
    }

    private static Experience experience(String company, boolean current, Integer endYear, int startYear) {
        Experience experience = new Experience();
        experience.setCompany(company);
        experience.setRoleTitle("Ofício");
        experience.setCurrentJob(current);
        experience.setEndYear(endYear);
        experience.setStartYear(startYear);
        experience.setUserId(UUID.randomUUID());
        try {
            var id = Experience.class.getDeclaredField("id");
            id.setAccessible(true);
            id.set(experience, UUID.randomUUID());
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
        return experience;
    }
}
