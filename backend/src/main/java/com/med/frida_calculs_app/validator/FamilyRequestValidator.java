package com.med.frida_calculs_app.validator;

import com.med.frida_calculs_app.exception.InvalidFamilyCompositionException;
import com.med.frida_calculs_app.model.FamilyRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class FamilyRequestValidator {

    /**
     * Valide la cohérence métier de la composition familiale
     */
    public void validate(FamilyRequest request) {
        log.debug("Validation de la composition familiale: {}", request);

        // Validation 1: Au moins un héritier doit être présent
        if (!request.hasAtLeastOneHeir()) {
            throw new InvalidFamilyCompositionException(
                    "Aucun héritier n'a été spécifié. Au moins un héritier doit être présent.");
        }

        // Validation 1 bis: Père et grand-père paternel ne peuvent pas être vivants en même temps
        if (request.isPereVivant() && request.isGrandPerePaternelVivant()) {
            throw new InvalidFamilyCompositionException(
                    "Le père et le grand-père paternel ne peuvent pas être vivants en même temps.");
        }

        // Validation 2: Valeurs par défaut pour null
        if (request.getNbFilles() == null) {
            request.setNbFilles(0);
        }
        if (request.getNbGarcons() == null) {
            request.setNbGarcons(0);
        }
        if (request.getNbSoeurs() == null) {
            request.setNbSoeurs(0);
        }
        if (request.getNbFreres() == null) {
            request.setNbFreres(0);
        }
        if (request.getNbOncles() == null) {
            request.setNbOncles(0);
        }
        if (request.getNbCousins() == null) {
            request.setNbCousins(0);
        }

        // Validation 3: Cohérence logique - si des enfants existent, la fratrie ne
        // devrait pas hériter
        int nbEnfants = request.getNbFilles() + request.getNbGarcons();
        int nbFratrie = request.getNbSoeurs() + request.getNbFreres();

        if (nbEnfants > 0 && nbFratrie > 0) {
            log.warn("Présence d'enfants ET de fratrie. La fratrie pourrait ne pas hériter selon les règles.");
        }

        // Validation 4: Vérification du sexe
        try {
            request.getSexeDefuntEnum();
        } catch (IllegalArgumentException e) {
            throw new InvalidFamilyCompositionException(
                    "Sexe du défunt invalide: " + request.getSexeDefunt());
        }

        // Validation 5: Nombre total d'héritiers raisonnable
        int totalHeritiers = (request.getNbConjoints() != null ? request.getNbConjoints() : 0) +
                (request.isPereVivant() ? 1 : 0) +
                (request.isMereVivante() ? 1 : 0) +
                (request.isGrandPerePaternelVivant() ? 1 : 0) +
                nbEnfants + nbFratrie + request.getNbOncles() + request.getNbCousins();

        if (totalHeritiers > 100) {
            throw new InvalidFamilyCompositionException(
                    "Le nombre total d'héritiers (" + totalHeritiers + ") est anormalement élevé");
        }

        log.debug("Validation réussie: {} héritier(s) total", totalHeritiers);
    }

    /**
     * Valide la logique spécifique de l'héritage islamique
     */
    public void validateIslamicRules(FamilyRequest request) {
        // Règle: Si le père est vivant et qu'il y a des frères/soeurs,
        // les frères/soeurs ne peuvent pas hériter
        if (request.isPereVivant()) {
            int nbFratrie = (request.getNbSoeurs() != null ? request.getNbSoeurs() : 0) +
                    (request.getNbFreres() != null ? request.getNbFreres() : 0);

            if (nbFratrie > 0) {
                log.info("Attention: En présence du père, la fratrie ne peut généralement pas hériter");
            }
        }
    }
}
