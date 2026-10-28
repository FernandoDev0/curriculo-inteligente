package com.curriculo.inteligente.interview;

import com.curriculo.inteligente.common.Text;

import java.util.List;
import java.util.Locale;

public final class QuestionBank {

    public static final String FIXED_FOLLOWUP =
            "Quais tarefas você realizava de fato? Conte apenas o que você mesmo fazia, sem completar o que não aconteceu.";

    private static final List<Item> IDENTIFICATION = List.of(
            item("full_name", "IDENTIFICATION", "Qual é o seu nome completo?", true),
            item("target_role", "IDENTIFICATION", "Qual cargo você deseja ocupar?", true),
            item("current_profession", "IDENTIFICATION", "Qual é sua profissão ou área de atuação atual?", false),
            item("city_state", "IDENTIFICATION", "Em qual cidade e estado você mora?", false),
            item("email", "IDENTIFICATION", "Qual é seu e-mail profissional?", false),
            item("phone", "IDENTIFICATION", "Qual é seu telefone ou WhatsApp profissional?", false),
            item("linkedin", "IDENTIFICATION", "Você possui LinkedIn? Se sim, informe o endereço.", false),
            item("other_links", "IDENTIFICATION", "Deseja incluir GitHub, portfólio ou outras redes profissionais?", false),
            item("work_mode", "IDENTIFICATION", "Você está disponível para trabalho remoto, híbrido ou presencial?", false),
            item("target_opportunity", "IDENTIFICATION", "Já existe uma vaga ou empresa de interesse?", false),
            item("age_choice", "IDENTIFICATION", "Deseja informar a idade? Por padrão ela não entra no currículo.", false),
            item("career_change", "IDENTIFICATION", "Você está mudando de profissão?", false));

    private static final List<Item> CAREER = List.of(
            item("desired_profession", "CAREER", "Qual profissão você deseja exercer?", true),
            item("career_change_reason", "CAREER", "Por que deseja migrar?", false),
            item("transferable", "CAREER", "Quais habilidades da profissão atual podem ser aproveitadas?", false),
            item("career_courses", "CAREER", "Fez cursos relacionados à nova área?", false),
            item("personal_projects", "CAREER", "Desenvolveu projetos pessoais? Eles não serão tratados como emprego.", false),
            item("portfolio", "CAREER", "Possui portfólio ou link de demonstração?", false),
            item("volunteer", "CAREER", "Já realizou trabalho voluntário ou atividade prática na nova área?", false));

    private static final List<String> EXPERIENCE = List.of(
            "company", "role_title", "period", "day_in_the_life", "activities", "tools",
            "problems", "people", "responsibility", "autonomy", "results", "more_experiences");

    private static final List<Item> EDUCATION = List.of(
            item("schooling", "EDUCATION", "Qual é sua escolaridade?", false),
            item("high_school", "EDUCATION", "Você concluiu o ensino médio?", false),
            item("course", "EDUCATION", "Qual é o curso, se houver?", false),
            item("institution", "EDUCATION", "Qual é a instituição?", false),
            item("edu_status", "EDUCATION", "Está concluído, em andamento ou interrompido?", false),
            item("edu_year", "EDUCATION", "Qual é o ano de conclusão ou a previsão?", false),
            item("courses", "EDUCATION", "Possui cursos, certificações ou treinamentos relevantes?", false),
            item("languages", "EDUCATION", "Quais idiomas você conhece, e qual é seu nível real de compreensão, leitura, escrita e conversação?", false),
            item("confirm_skills", "SKILLS", "Há alguma ferramenta ou competência, além das que você já citou, que deva entrar no currículo?", false));

    private QuestionBank() {
    }

    public record Item(String key, String stage, String text, boolean required) {
        public boolean canSkip() {
            return !required;
        }
    }

    public static Item first() {
        return IDENTIFICATION.get(0);
    }

    public static Item describe(String key) {
        if (key != null && key.contains(".followup.")) {
            String base = key.substring(0, key.indexOf(".followup."));
            return new Item(key, "EXPERIENCE", FIXED_FOLLOWUP, false);
        }
        if (key != null && key.startsWith("exp:")) {
            String field = key.substring(key.lastIndexOf(':') + 1);
            return new Item(key, "EXPERIENCE", experienceText(field), requiredExperience(field));
        }
        for (Item item : IDENTIFICATION) {
            if (item.key.equals(key)) {
                return item;
            }
        }
        for (Item item : CAREER) {
            if (item.key.equals(key)) {
                return item;
            }
        }
        for (Item item : EDUCATION) {
            if (item.key.equals(key)) {
                return item;
            }
        }
        return null;
    }

    public static String next(String current, String answer, int followupsAlreadyAsked) {
        if (current != null && current.contains(".followup.")) {
            String base = current.substring(0, current.indexOf(".followup."));
            return afterExperienceField(base);
        }
        if (isThin(current, answer) && followupsAlreadyAsked < 2) {
            return current + ".followup." + (followupsAlreadyAsked + 1);
        }
        if ("career_change".equals(current)) {
            return affirmative(answer) ? CAREER.get(0).key : "exp:1:company";
        }
        int ident = indexOf(IDENTIFICATION, current);
        if (ident >= 0 && ident < IDENTIFICATION.size() - 1) {
            return IDENTIFICATION.get(ident + 1).key;
        }
        int career = indexOf(CAREER, current);
        if (career >= 0) {
            return career == CAREER.size() - 1 ? "exp:1:company" : CAREER.get(career + 1).key;
        }
        if (current != null && current.startsWith("exp:")) {
            int round = Integer.parseInt(current.split(":")[1]);
            String field = current.substring(current.lastIndexOf(':') + 1);
            if ("more_experiences".equals(field)) {
                return affirmative(answer) ? "exp:" + (round + 1) + ":company" : EDUCATION.get(0).key;
            }
            return afterExperienceField(current);
        }
        int education = indexOf(EDUCATION, current);
        if (education >= 0 && education < EDUCATION.size() - 1) {
            return EDUCATION.get(education + 1).key;
        }
        return null;
    }

    public static int progress(String key) {
        if (key == null) {
            return 100;
        }
        int total = IDENTIFICATION.size() + CAREER.size() + EXPERIENCE.size() + EDUCATION.size();
        int position = indexOf(IDENTIFICATION, key);
        if (position >= 0) {
            return percent(position, total);
        }
        position = indexOf(CAREER, key);
        if (position >= 0) {
            return percent(IDENTIFICATION.size() + position, total);
        }
        if (key.startsWith("exp:")) {
            String field = key.substring(key.lastIndexOf(':') + 1);
            int fieldIndex = Math.max(0, EXPERIENCE.indexOf(field.replaceAll("\\.followup.*", "")));
            return percent(IDENTIFICATION.size() + CAREER.size() + fieldIndex, total);
        }
        position = indexOf(EDUCATION, key);
        if (position >= 0) {
            return percent(IDENTIFICATION.size() + CAREER.size() + EXPERIENCE.size() + position, total);
        }
        return 50;
    }

    public static boolean affirmative(String answer) {
        String plain = Text.plain(answer);
        return plain.startsWith("sim") || plain.startsWith("quero") || plain.startsWith("estou") || plain.equals("s");
    }

    private static String afterExperienceField(String key) {
        int round = Integer.parseInt(key.split(":")[1]);
        String field = key.substring(key.lastIndexOf(':') + 1);
        int index = EXPERIENCE.indexOf(field);
        if (index < 0 || index == EXPERIENCE.size() - 1) {
            return EDUCATION.get(0).key;
        }
        return "exp:" + round + ":" + EXPERIENCE.get(index + 1);
    }

    private static boolean isThin(String key, String answer) {
        if (key == null || answer == null) {
            return false;
        }
        String field = key.contains(":") ? key.substring(key.lastIndexOf(':') + 1) : key;
        if (!field.equals("activities") && !field.equals("problems") && !field.equals("results") && !field.equals("day_in_the_life")) {
            return false;
        }
        String plain = Text.plain(answer);
        return plain.length() < 40
                || plain.contains("ajudava a equipe")
                || plain.contains("mexia com planilha")
                || plain.contains("fazia de tudo");
    }

    private static boolean requiredExperience(String field) {
        return field.equals("company") || field.equals("role_title") || field.equals("period");
    }

    private static String experienceText(String field) {
        return switch (field) {
            case "company" -> "Qual era o nome da empresa ou da atividade?";
            case "role_title" -> "Qual era o seu cargo?";
            case "period" -> "Quanto tempo trabalhou nessa função? Informe mês e ano. Se segue nela, diga que é o trabalho atual.";
            case "day_in_the_life" -> "Como era um dia normal de trabalho?";
            case "activities" -> "Quais atividades você realizava pessoalmente?";
            case "tools" -> "Quais ferramentas, sistemas, equipamentos ou tecnologias utilizava?";
            case "problems" -> "Quais problemas você precisava resolver?";
            case "people" -> "Com quais equipes, clientes ou departamentos trabalhava?";
            case "responsibility" -> "Qual era sua responsabilidade principal?";
            case "autonomy" -> "Você tinha autonomia para tomar decisões?";
            case "results" -> "Existe algum resultado, melhoria ou contribuição que possa comprovar?";
            case "more_experiences" -> "Deseja contar outra experiência?";
            default -> "Pode detalhar mais o que fazia?";
        };
    }

    private static Item item(String key, String stage, String text, boolean required) {
        return new Item(key, stage, text, required);
    }

    private static int indexOf(List<Item> items, String key) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).key.equals(key)) {
                return i;
            }
        }
        return -1;
    }

    private static int percent(int position, int total) {
        return Math.min(99, (int) Math.round(position * 100.0 / total));
    }

    public static String plainLocale(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }
}
