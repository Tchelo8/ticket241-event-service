package com.tchelo.event_service_ticket241.config;

import com.tchelo.event_service_ticket241.entity.Category;
import com.tchelo.event_service_ticket241.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CategorySeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) {
        // Seeding uniquement si la table est vide — on ne réinsère pas si les catégories existent déjà
        if (categoryRepository.count() > 0) {
            log.info("Les catégories existent déjà en base de données, aucune insertion nécessaire.");
            return;
        }

        log.info("Initialisation des catégories par défaut sur Ticket241...");

        List<Category> defaultCategories = List.of(
                buildCategory("CONCERT",     "Concert",       "Concerts de musique live, récitals et performances musicales"),
                buildCategory("SPORT",       "Sport",         "Événements sportifs, compétitions et matchs"),
                buildCategory("THEATRE",     "Théâtre",       "Pièces de théâtre, spectacles dramatiques et comédie"),
                buildCategory("CONFERENCE",  "Conférence",    "Conférences, séminaires et keynotes"),
                buildCategory("FESTIVAL",    "Festival",      "Festivals de musique, arts et culture"),
                buildCategory("CINEMA",      "Cinéma",        "Avant-premières, projections et festivals de cinéma"),
                buildCategory("EXPOSITION",  "Exposition",    "Expositions d'art, musées et galeries"),
                buildCategory("DANSE",       "Danse",         "Spectacles de danse, ballets et performances chorégraphiques"),
                buildCategory("COMEDIE",     "Comédie",       "Spectacles de comédie, stand-up et humour"),
                buildCategory("OPERA",       "Opéra",         "Opéras, opérettes et spectacles lyriques"),
                buildCategory("CIRQUE",      "Cirque",        "Spectacles de cirque et arts du spectacle"),
                buildCategory("FAMILLE",     "Famille",       "Événements familiaux et pour enfants"),
                buildCategory("GASTRONOMIE", "Gastronomie",   "Événements gastronomiques, dégustations et salons culinaires"),
                buildCategory("FORMATION",   "Formation",     "Formations, ateliers et workshops"),
                buildCategory("NETWORKING",  "Networking",    "Événements de networking et de réseautage professionnel"),
                buildCategory("CHARITY",     "Charity",       "Événements caritatifs et collectes de fonds"),
                buildCategory("RELIGION",    "Religion",      "Événements religieux et spirituels"),
                buildCategory("AUTRE",       "Autre",         "Autres types d'événements")
        );

        categoryRepository.saveAll(defaultCategories);

        log.info("{} catégories initialisées avec succès.", defaultCategories.size());
    }

    private Category buildCategory(String name, String displayName, String description) {
        return Category.builder()
                .name(name)
                .displayName(displayName)
                .description(description)
                .isActive(true)
                .build();
    }
}
