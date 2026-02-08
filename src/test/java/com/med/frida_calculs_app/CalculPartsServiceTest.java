package com.med.frida_calculs_app;

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
        String sexeDefunt = "M";
        boolean conjointVivant = true;
        boolean pereVivant = false;
        boolean mereVivante = false;
        Integer nbFilles = 1;
        Integer nbGarcons = 1;
        Integer nbSoeurs = 0;
        Integer nbFreres = 0;

        // When
        List<Heritier> result = service.calculParts(
            sexeDefunt, conjointVivant, pereVivant, mereVivante,
            nbFilles, nbGarcons, nbSoeurs, nbFreres
        );

        // Then
        assertNotNull(result, "Le résultat ne devrait pas être null");
        assertFalse(result.isEmpty(), "Le résultat ne devrait pas être vide");

        // Vérifier que le conjoint a sa part (1/8 car il y a des enfants)
        Heritier conjoint = result.stream()
            .filter(h -> "conjoint".equals(h.getHeritier()))
            .findFirst()
            .orElse(null);

        assertNotNull(conjoint, "Le conjoint devrait être dans les héritiers");
        assertNotNull(conjoint.getPart(), "Le conjoint devrait avoir une part");
        assertTrue(conjoint.getPart().getNumerateur() > 0, "La part du conjoint devrait être positive");

        // Vérifier que les enfants ont leurs parts
        long nbEnfantsAvecPart = result.stream()
            .filter(h -> "fille".equals(h.getHeritier()) || "garçon".equals(h.getHeritier()))
            .count();

        assertEquals(2, nbEnfantsAvecPart, "Il devrait y avoir 2 types d'enfants (fille et garçon)");
    }

    @Test
    @DisplayName("Calcul avec parents uniquement")
    void testCalculWithParentsOnly() {
        // Given
        String sexeDefunt = "M";
        boolean conjointVivant = false;
        boolean pereVivant = true;
        boolean mereVivante = true;
        Integer nbFilles = 0;
        Integer nbGarcons = 0;
        Integer nbSoeurs = 0;
        Integer nbFreres = 0;

        // When
        List<Heritier> result = service.calculParts(
            sexeDefunt, conjointVivant, pereVivant, mereVivante,
            nbFilles, nbGarcons, nbSoeurs, nbFreres
        );

        // Then
        assertNotNull(result);

        // Le père devrait avoir une part
        Heritier pere = result.stream()
            .filter(h -> "pere".equals(h.getHeritier()))
            .findFirst()
            .orElse(null);

        assertNotNull(pere, "Le père devrait être dans les héritiers");
        assertNotNull(pere.getPart(), "Le père devrait avoir une part");

        // La mère devrait avoir une part
        Heritier mere = result.stream()
            .filter(h -> "mere".equals(h.getHeritier()))
            .findFirst()
            .orElse(null);

        assertNotNull(mere, "La mère devrait être dans les héritiers");
        assertNotNull(mere.getPart(), "La mère devrait avoir une part");
    }

    @Test
    @DisplayName("Calcul avec valeurs null - Devrait utiliser 0 par défaut")
    void testCalculWithNullValues() {
        // Given
        String sexeDefunt = "F";
        boolean conjointVivant = true;
        boolean pereVivant = false;
        boolean mereVivante = false;
        Integer nbFilles = null;  // null devrait être traité comme 0
        Integer nbGarcons = null; // null devrait être traité comme 0
        Integer nbSoeurs = null;  // null devrait être traité comme 0
        Integer nbFreres = null;  // null devrait être traité comme 0

        // When & Then - Ne devrait pas lancer d'exception
        assertDoesNotThrow(() -> {
            List<Heritier> result = service.calculParts(
                sexeDefunt, conjointVivant, pereVivant, mereVivante,
                nbFilles, nbGarcons, nbSoeurs, nbFreres
            );

            assertNotNull(result);
            assertFalse(result.isEmpty());
        });
    }

    @Test
    @DisplayName("Vérification du dénominateur commun")
    void testCommonDenominator() {
        // Given
        String sexeDefunt = "M";
        boolean conjointVivant = true;
        boolean pereVivant = true;
        boolean mereVivante = true;
        Integer nbFilles = 2;
        Integer nbGarcons = 1;
        Integer nbSoeurs = 0;
        Integer nbFreres = 0;

        // When
        List<Heritier> result = service.calculParts(
            sexeDefunt, conjointVivant, pereVivant, mereVivante,
            nbFilles, nbGarcons, nbSoeurs, nbFreres
        );

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
        String sexeDefunt = "M";
        boolean conjointVivant = false;
        boolean pereVivant = false;
        boolean mereVivante = false;
        Integer nbFilles = 1;
        Integer nbGarcons = 1;
        Integer nbSoeurs = 0;
        Integer nbFreres = 0;

        // When
        List<Heritier> result = service.calculParts(
            sexeDefunt, conjointVivant, pereVivant, mereVivante,
            nbFilles, nbGarcons, nbSoeurs, nbFreres
        );

        // Then
        Heritier fille = result.stream()
            .filter(h -> "fille".equals(h.getHeritier()))
            .findFirst()
            .orElse(null);

        Heritier garcon = result.stream()
            .filter(h -> "garçon".equals(h.getHeritier()))
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
