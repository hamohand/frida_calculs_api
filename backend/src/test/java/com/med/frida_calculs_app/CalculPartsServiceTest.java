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

        @Test
        @DisplayName("Test d'exclusion (Hajb) - Père ou Fils exclut la fratrie")
        void testHajbExclusion() {
                // Given: Père vivant, 1 frère, 1 sœur. Frère et sœur doivent recevoir 0
                FamilyRequest request = FamilyRequest.builder()
                                .sexeDefunt("M")
                                .nbConjoints(0)
                                .pereVivant(true)
                                .mereVivante(false)
                                .nbFilles(0)
                                .nbGarcons(0)
                                .nbSoeurs(1)
                                .nbFreres(1)
                                .build();

                // When
                List<Heritier> result = service.calculParts(request);

                // Then
                Heritier frere = result.stream()
                                .filter(h -> com.med.frida_calculs_app.enums.HeirType.BROTHER.getLabel().equals(h.getHeritier()))
                                .findFirst().orElse(null);
                Heritier soeur = result.stream()
                                .filter(h -> com.med.frida_calculs_app.enums.HeirType.SISTER.getLabel().equals(h.getHeritier()))
                                .findFirst().orElse(null);

                assertNotNull(frere);
                assertNotNull(soeur);
                assertEquals(0, frere.getPart().getNumerateur(), "Le frère doit être exclu (part = 0)");
                assertEquals(0, soeur.getPart().getNumerateur(), "La sœur doit être exclue (part = 0)");
        }

        @Test
        @DisplayName("Test d'Aoul (dépassement) - Époux + 2 Sœurs + Mère")
        void testAoulResolution() {
                // Given: Conjoint = Époux (defunt F), 2 sœurs, mère.
                // Parts théoriques: Époux (1/2 = 3/6), 2 sœurs (2/3 = 4/6), mère (1/6). Total = 8/6.
                // Aoul: dénominateur passe à 8. parts: Époux (3/8), sœurs (4/8 collectif, soit 2/8 chacune), mère (1/8).
                FamilyRequest request = FamilyRequest.builder()
                                .sexeDefunt("F")
                                .nbConjoints(1)
                                .pereVivant(false)
                                .mereVivante(true)
                                .nbFilles(0)
                                .nbGarcons(0)
                                .nbSoeurs(2)
                                .nbFreres(0)
                                .build();

                // When
                List<Heritier> result = service.calculParts(request);

                // Then
                Heritier epoux = result.stream()
                                .filter(h -> com.med.frida_calculs_app.enums.HeirType.SPOUSE.getLabel().equals(h.getHeritier()))
                                .findFirst().orElse(null);
                Heritier mere = result.stream()
                                .filter(h -> com.med.frida_calculs_app.enums.HeirType.MOTHER.getLabel().equals(h.getHeritier()))
                                .findFirst().orElse(null);
                Heritier soeur = result.stream()
                                .filter(h -> com.med.frida_calculs_app.enums.HeirType.SISTER.getLabel().equals(h.getHeritier()))
                                .findFirst().orElse(null);

                assertNotNull(epoux);
                assertNotNull(mere);
                assertNotNull(soeur);

                // Pour vérifier les proportions finales: Époux (3/8), Mère (1/8), Soeur (2/8 par soeur car 4/8 collectif)
                // Sous le même dénominateur (par exemple 8, ou un multiple):
                int d = epoux.getPart().getDenominateur();
                assertEquals(3 * (d / 8), epoux.getPart().getNumerateur(), "L'époux doit recevoir 3/8");
                assertEquals(1 * (d / 8), mere.getPart().getNumerateur(), "La mère doit recevoir 1/8");
                assertEquals(2 * (d / 8), soeur.getPart().getNumerateur(), "Chaque sœur doit recevoir 2/8");
        }

        @Test
        @DisplayName("Test de Radd (redistribution) - 1 Fille + Mère")
        void testRaddResolution() {
                // Given: Fille (1/2 = 3/6), Mère (1/6). Pas d'Asaba ni de conjoint.
                // Somme = 4/6. Radd redistribue le surplus:
                // Somme éligible = 2/3.
                // Fille = 1/2 * 3/2 = 3/4. Mère = 1/6 * 3/2 = 1/4.
                FamilyRequest request = FamilyRequest.builder()
                                .sexeDefunt("M")
                                .nbConjoints(0)
                                .pereVivant(false)
                                .mereVivante(true)
                                .nbFilles(1)
                                .nbGarcons(0)
                                .nbSoeurs(0)
                                .nbFreres(0)
                                .build();

                // When
                List<Heritier> result = service.calculParts(request);

                // Then
                Heritier fille = result.stream()
                                .filter(h -> com.med.frida_calculs_app.enums.HeirType.DAUGHTER.getLabel().equals(h.getHeritier()))
                                .findFirst().orElse(null);
                Heritier mere = result.stream()
                                .filter(h -> com.med.frida_calculs_app.enums.HeirType.MOTHER.getLabel().equals(h.getHeritier()))
                                .findFirst().orElse(null);

                assertNotNull(fille);
                assertNotNull(mere);

                int d = fille.getPart().getDenominateur();
                assertEquals(3 * (d / 4), fille.getPart().getNumerateur(), "La fille doit recevoir 3/4");
                assertEquals(1 * (d / 4), mere.getPart().getNumerateur(), "La mère doit recevoir 1/4");
        }

        @Test
        @DisplayName("Test spécial Gharrawayn - Épouse + Mère + Père")
        void testGharrawaynCase() {
                // Given: Défunt masculin, 1 épouse, père, mère. Pas d'enfants ni fratrie.
                // Épouse (1/4), Mère (1/3 du reste = 1/4), Père (Asaba prend le reste = 1/2)
                FamilyRequest request = FamilyRequest.builder()
                                .sexeDefunt("M")
                                .nbConjoints(1)
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
                Heritier epouse = result.stream()
                                .filter(h -> com.med.frida_calculs_app.enums.HeirType.SPOUSE.getLabel().equals(h.getHeritier()))
                                .findFirst().orElse(null);
                Heritier mere = result.stream()
                                .filter(h -> com.med.frida_calculs_app.enums.HeirType.MOTHER.getLabel().equals(h.getHeritier()))
                                .findFirst().orElse(null);
                Heritier pere = result.stream()
                                .filter(h -> com.med.frida_calculs_app.enums.HeirType.FATHER.getLabel().equals(h.getHeritier()))
                                .findFirst().orElse(null);

                assertNotNull(epouse);
                assertNotNull(mere);
                assertNotNull(pere);

                int d = epouse.getPart().getDenominateur();
                assertEquals(1 * (d / 4), epouse.getPart().getNumerateur(), "L'épouse doit recevoir 1/4");
                assertEquals(1 * (d / 4), mere.getPart().getNumerateur(), "La mère doit recevoir 1/4 (1/3 du reste)");
                assertEquals(2 * (d / 4), pere.getPart().getNumerateur(), "Le père doit recevoir 1/2 (Asaba du reste)");
        }

        @Test
        @DisplayName("Test de validation des cadres légaux en arabe (Fard, Asaba, Aoul, Radd)")
        void testCadresLegauxArabe() {
                // 1. Cas de Aoul (Somme > 1) : Époux + Mère + Père + 2 Filles
                FamilyRequest requestAoul = FamilyRequest.builder()
                                .sexeDefunt("F") // Époux (1/4)
                                .nbConjoints(1)
                                .pereVivant(true) // Père (1/6)
                                .mereVivante(true) // Mère (1/6)
                                .nbFilles(2) // Filles (2/3)
                                .nbGarcons(0)
                                .nbSoeurs(0)
                                .nbFreres(0)
                                .build();
                List<Heritier> aoulResult = service.calculParts(requestAoul);
                for (Heritier h : aoulResult) {
                        if (!"part restant".equals(h.getHeritier())) {
                                assertEquals("العول (Aoul - Réduction)", h.getCadreLegal(), "Le cadre légal en cas d'Aoul doit être العول (Aoul - Réduction) pour " + h.getHeritier());
                        }
                }

                // 2. Cas de Radd : Mère + 1 Fille
                FamilyRequest requestRadd = FamilyRequest.builder()
                                .sexeDefunt("M")
                                .nbConjoints(0)
                                .pereVivant(false)
                                .mereVivante(true)
                                .nbFilles(1)
                                .nbGarcons(0)
                                .nbSoeurs(0)
                                .nbFreres(0)
                                .build();
                List<Heritier> raddResult = service.calculParts(requestRadd);
                Heritier mereRadd = raddResult.stream().filter(h -> "mere".equals(h.getHeritier())).findFirst().orElse(null);
                Heritier filleRadd = raddResult.stream().filter(h -> "fille".equals(h.getHeritier())).findFirst().orElse(null);
                assertNotNull(mereRadd);
                assertNotNull(filleRadd);
                assertEquals("الفرض والرد (Fard et Radd)", mereRadd.getCadreLegal());
                assertEquals("الفرض والرد (Fard et Radd)", filleRadd.getCadreLegal());

                // 3. Cas de Père (Fard + Asaba) : Père + 1 Fille
                FamilyRequest requestPereBoth = FamilyRequest.builder()
                                .sexeDefunt("M")
                                .nbConjoints(0)
                                .pereVivant(true)
                                .mereVivante(false)
                                .nbFilles(1)
                                .nbGarcons(0)
                                .nbSoeurs(0)
                                .nbFreres(0)
                                .build();
                List<Heritier> pereBothResult = service.calculParts(requestPereBoth);
                Heritier pere = pereBothResult.stream().filter(h -> "pere".equals(h.getHeritier())).findFirst().orElse(null);
                Heritier fille = pereBothResult.stream().filter(h -> "fille".equals(h.getHeritier())).findFirst().orElse(null);
                assertNotNull(pere);
                assertNotNull(fille);
                assertEquals("الفرض والعصبة (Fard et Asaba)", pere.getCadreLegal());
                assertEquals("الفرض (Fard - Part fixe)", fille.getCadreLegal());
        }

        @Test
        @DisplayName("Test de calcul avec uniquement épouse et oncles")
        void testCalculWithOnlySpouseAndUncles() {
                // Given: Épouse (1/4), 2 Oncles (qui héritent du reste, soit 3/4 en tant qu'Asaba)
                // Donc épouse = 1/4 = 2/8. Les 2 oncles se partagent 3/4, soit 3/8 chacun.
                FamilyRequest request = FamilyRequest.builder()
                                .sexeDefunt("M")
                                .nbConjoints(1)
                                .pereVivant(false)
                                .mereVivante(false)
                                .nbFilles(0)
                                .nbGarcons(0)
                                .nbSoeurs(0)
                                .nbFreres(0)
                                .nbOncles(2)
                                .nbCousins(0)
                                .build();

                // When
                List<Heritier> result = service.calculParts(request);

                // Then
                assertNotNull(result);
                Heritier epouse = result.stream().filter(h -> "conjoint".equals(h.getHeritier())).findFirst().orElse(null);
                Heritier oncles = result.stream().filter(h -> "oncle paternel".equals(h.getHeritier())).findFirst().orElse(null);
                Heritier reste = result.stream().filter(h -> "part restant".equals(h.getHeritier())).findFirst().orElse(null);

                assertNotNull(epouse);
                assertNotNull(oncles);
                assertNotNull(reste);

                // Dénominateur commun attendu: 8
                int d = epouse.getPart().getDenominateur();
                assertEquals(8, d, "Le dénominateur commun doit être 8");
                assertEquals(2, epouse.getPart().getNumerateur(), "L'épouse doit avoir 2/8 (1/4)");
                assertEquals(3, oncles.getPart().getNumerateur(), "Chaque oncle doit avoir 3/8 (3/4 divisé par 2)");
                assertEquals(0, reste.getPart().getNumerateur(), "Il ne doit y avoir aucun reste");
                assertEquals("العصبة (Asaba - Résiduaire)", oncles.getCadreLegal());
        }

        @Test
        @DisplayName("Test de calcul avec uniquement épouse et cousins")
        void testCalculWithOnlySpouseAndCousins() {
                // Given: Épouse (1/4), 3 Cousins (qui héritent du reste, soit 3/4 en tant qu'Asaba)
                // Donc épouse = 1/4. Les 3 cousins se partagent 3/4, soit 1/4 chacun.
                FamilyRequest request = FamilyRequest.builder()
                                .sexeDefunt("M")
                                .nbConjoints(1)
                                .pereVivant(false)
                                .mereVivante(false)
                                .nbFilles(0)
                                .nbGarcons(0)
                                .nbSoeurs(0)
                                .nbFreres(0)
                                .nbOncles(0)
                                .nbCousins(3)
                                .build();

                // When
                List<Heritier> result = service.calculParts(request);

                // Then
                assertNotNull(result);
                Heritier epouse = result.stream().filter(h -> "conjoint".equals(h.getHeritier())).findFirst().orElse(null);
                Heritier cousins = result.stream().filter(h -> "cousin paternel".equals(h.getHeritier())).findFirst().orElse(null);
                Heritier reste = result.stream().filter(h -> "part restant".equals(h.getHeritier())).findFirst().orElse(null);

                assertNotNull(epouse);
                assertNotNull(cousins);
                assertNotNull(reste);

                int d = epouse.getPart().getDenominateur();
                assertEquals(4, d);
                assertEquals(1, epouse.getPart().getNumerateur());
                assertEquals(1, cousins.getPart().getNumerateur());
                assertEquals(0, reste.getPart().getNumerateur());
                assertEquals("العصبة (Asaba - Résiduaire)", cousins.getCadreLegal());
        }

        @Test
        @DisplayName("Test d'exclusion des oncles et cousins par les frères")
        void testExclusionOnclesEtCousins() {
                // Given: Épouse (1/4), 1 Frère (Asaba prend tout le reste, soit 3/4), 1 Oncle (exclu), 1 Cousin (exclu)
                FamilyRequest request = FamilyRequest.builder()
                                .sexeDefunt("M")
                                .nbConjoints(1)
                                .pereVivant(false)
                                .mereVivante(false)
                                .nbFilles(0)
                                .nbGarcons(0)
                                .nbSoeurs(0)
                                .nbFreres(1)
                                .nbOncles(1)
                                .nbCousins(1)
                                .build();

                // When
                List<Heritier> result = service.calculParts(request);

                // Then
                assertNotNull(result);
                Heritier frere = result.stream().filter(h -> "frere".equals(h.getHeritier())).findFirst().orElse(null);
                Heritier oncle = result.stream().filter(h -> "oncle paternel".equals(h.getHeritier())).findFirst().orElse(null);
                Heritier cousin = result.stream().filter(h -> "cousin paternel".equals(h.getHeritier())).findFirst().orElse(null);

                assertNotNull(frere);
                assertNotNull(oncle);
                assertNotNull(cousin);

                assertTrue(frere.getPart().getNumerateur() > 0, "Le frère doit hériter");
                assertEquals(0, oncle.getPart().getNumerateur(), "L'oncle doit être exclu");
                assertEquals(0, cousin.getPart().getNumerateur(), "Le cousin doit être exclu");
                assertEquals("محجوب (Exclu)", oncle.getCadreLegal());
                assertEquals("محجوب (Exclu)", cousin.getCadreLegal());
        }

        @Test
        @DisplayName("Calcul avec plusieurs conjoints (épouses) - Part partagée")
        void testCalculWithMultipleSpouses() {
                // Given: 2 Épouses, pas d'enfants, pas de parents.
                // La part collective est 1/4. Divisée par 2, chaque épouse reçoit 1/8.
                FamilyRequest request = FamilyRequest.builder()
                                .sexeDefunt("M")
                                .nbConjoints(2)
                                .pereVivant(false)
                                .mereVivante(false)
                                .nbFilles(0)
                                .nbGarcons(0)
                                .nbSoeurs(0)
                                .nbFreres(0)
                                .build();

                // When
                List<Heritier> result = service.calculParts(request);

                // Then
                assertNotNull(result);
                Heritier conjoint = result.stream().filter(h -> "conjoint".equals(h.getHeritier())).findFirst().orElse(null);
                assertNotNull(conjoint);
                assertEquals(1, conjoint.getPart().getNumerateur());
                assertEquals(8, conjoint.getPart().getDenominateur()); // 1/4 divisé par 2 = 1/8
        }
}

