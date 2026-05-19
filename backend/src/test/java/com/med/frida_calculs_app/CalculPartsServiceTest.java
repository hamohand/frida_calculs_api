package com.med.frida_calculs_app;

import com.med.frida_calculs_app.model.FamilyRequest;
import com.med.frida_calculs_app.model.Heritier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("Tests du service de calcul des parts d'héritage")
class CalculPartsServiceTest {

        @Autowired
        private CalculPartsService service;

        @BeforeEach
        void setUp() {
                assertNotNull(service, "Le service devrait être injecté");
        }

        @Test
        @DisplayName("Calcul avec conjoint et enfants - Défunt masculin")
        void testCalculWithSpouseAndChildren_MaleDeceased() {
                // Given
                FamilyRequest request = FamilyRequest.builder()
                                .sexeDefunt("M")
                                .nbConjoints(1)
                                .pereVivant(false)
                                .mereVivante(false)
                                .nbFilles(1)
                                .nbGarcons(1)
                                .nbSoeurs(0)
                                .nbFreres(0)
                                .build();

                // When
                List<Heritier> result = service.calculParts(request);

                // Then
                assertNotNull(result, "Le résultat ne devrait pas être null");
                assertFalse(result.isEmpty(), "Le résultat ne devrait pas être vide");

                // Vérifier que le conjoint a sa part (1/8 car il y a des enfants)
                Heritier conjoint = result.stream()
                                .filter(h -> com.med.frida_calculs_app.enums.HeirType.SPOUSE.getLabel()
                                                .equals(h.getHeritier()))
                                .findFirst()
                                .orElse(null);

                assertNotNull(conjoint, "Le conjoint devrait être dans les héritiers");
                assertNotNull(conjoint.getPart(), "Le conjoint devrait avoir une part");
                assertTrue(conjoint.getPart().getNumerateur() > 0, "La part du conjoint devrait être positive");

                // Vérifier que les enfants ont leurs parts
                long nbEnfantsAvecPart = result.stream()
                                .filter(h -> com.med.frida_calculs_app.enums.HeirType.DAUGHTER.getLabel()
                                                .equals(h.getHeritier()) ||
                                                com.med.frida_calculs_app.enums.HeirType.SON.getLabel()
                                                                .equals(h.getHeritier()))
                                .count();

                assertEquals(2, nbEnfantsAvecPart, "Il devrait y avoir 2 types d'enfants (fille et garçon)");
        }

        @Test
        @DisplayName("Calcul avec parents uniquement")
        void testCalculWithParentsOnly() {
                // Given
                FamilyRequest request = FamilyRequest.builder()
                                .sexeDefunt("M")
                                .nbConjoints(0)
                                .pereVivant(true)
                                .mereVivante(true)
                                .nbFilles(0)
                                .nbGarcons(0)
                                .nbSoeurs(0)
                                .nbFreres(0)
                                .build();

                // When
                List<Heritier> result = service.calculParts(request);

                // Then
                assertNotNull(result);

                // Le père devrait avoir une part
                Heritier pere = result.stream()
                                .filter(h -> com.med.frida_calculs_app.enums.HeirType.FATHER.getLabel()
                                                .equals(h.getHeritier()))
                                .findFirst()
                                .orElse(null);

                assertNotNull(pere, "Le père devrait être dans les héritiers");
                assertNotNull(pere.getPart(), "Le père devrait avoir une part");

                // La mère devrait avoir une part
                Heritier mere = result.stream()
                                .filter(h -> com.med.frida_calculs_app.enums.HeirType.MOTHER.getLabel()
                                                .equals(h.getHeritier()))
                                .findFirst()
                                .orElse(null);

                assertNotNull(mere, "La mère devrait être dans les héritiers");
                assertNotNull(mere.getPart(), "La mère devrait avoir une part");
        }

        @Test
        @DisplayName("Calcul avec valeurs null - Devrait utiliser 0 par défaut")
        void testCalculWithNullValues() {
                // Given
                FamilyRequest request = FamilyRequest.builder()
                                .sexeDefunt("F")
                                .nbConjoints(1)
                                .pereVivant(false)
                                .mereVivante(false)
                                .nbFilles(null) // null devrait être traité comme 0
                                .nbGarcons(null) // null devrait être traité comme 0
                                .nbSoeurs(null) // null devrait être traité comme 0
                                .nbFreres(null) // null devrait être traité comme 0
                                .build();

                // When & Then - Ne devrait pas lancer d'exception
                assertDoesNotThrow(() -> {
                        List<Heritier> result = service.calculParts(request);

                        assertNotNull(result);
                        assertFalse(result.isEmpty());
                });
        }

        @Test
        @DisplayName("Vérification du dénominateur commun")
        void testCommonDenominator() {
                // Given
                FamilyRequest request = FamilyRequest.builder()
                                .sexeDefunt("M")
                                .nbConjoints(1)
                                .pereVivant(true)
                                .mereVivante(true)
                                .nbFilles(2)
                                .nbGarcons(1)
                                .nbSoeurs(0)
                                .nbFreres(0)
                                .build();

                // When
                List<Heritier> result = service.calculParts(request);

                // Then
                assertNotNull(result);
                assertFalse(result.isEmpty());

                // Tous les héritiers devraient avoir le même dénominateur
                int commonDenominator = result.get(0).getPart().getDenominateur();

                boolean allSameDenominator = result.stream()
                                .filter(h -> h.getPart() != null)
                                .allMatch(h -> h.getPart().getDenominateur() == commonDenominator);

                assertTrue(allSameDenominator, "Toutes les parts devraient avoir le même dénominateur");
        }

        @Test
        @DisplayName("Part du garçon devrait être le double de celle de la fille")
        void testBoyShareIsDoubleThatOfGirl() {
                // Given - Uniquement des enfants, pas de conjoint ni parents
                FamilyRequest request = FamilyRequest.builder()
                                .sexeDefunt("M")
                                .nbConjoints(0)
                                .pereVivant(false)
                                .mereVivante(false)
                                .nbFilles(1)
                                .nbGarcons(1)
                                .nbSoeurs(0)
                                .nbFreres(0)
                                .build();

                // When
                List<Heritier> result = service.calculParts(request);

                // Then
                Heritier fille = result.stream()
                                .filter(h -> com.med.frida_calculs_app.enums.HeirType.DAUGHTER.getLabel()
                                                .equals(h.getHeritier()))
                                .findFirst()
                                .orElse(null);

                Heritier garcon = result.stream()
                                .filter(h -> com.med.frida_calculs_app.enums.HeirType.SON.getLabel()
                                                .equals(h.getHeritier()))
                                .findFirst()
                                .orElse(null);

                assertNotNull(fille, "La fille devrait être dans les héritiers");
                assertNotNull(garcon, "Le garçon devrait être dans les héritiers");

                // La part du garçon devrait être le double de celle de la fille
                int partGarcon = garcon.getPart().getNumerateur();
                int partFille = fille.getPart().getNumerateur();

                assertEquals(partFille * 2, partGarcon,
                                "La part du garçon devrait être le double de celle de la fille");
        }
}
