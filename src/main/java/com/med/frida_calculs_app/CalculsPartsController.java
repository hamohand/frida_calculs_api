package com.med.frida_calculs_app;

import com.med.frida_calculs_app.model.Heritier;
import com.med.frida_calculs_app.model.FamilyRequest;
import com.med.frida_calculs_app.model.HeritageResponse;
import com.med.frida_calculs_app.validator.FamilyRequestValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequestMapping("/api/v1/heritage")
@RestController
@Tag(name = "Héritage", description = "API de calcul des parts d'héritage selon la loi islamique")
public class CalculsPartsController {

    private final CalculPartsService calculPartsService;
    private final FamilyRequestValidator validator;

    @Autowired
    public CalculsPartsController(CalculPartsService calculPartsService, FamilyRequestValidator validator) {
        this.calculPartsService = calculPartsService;
        this.validator = validator;
    }

    @GetMapping("/calculs")
    @CrossOrigin(origins = { "http://localhost:4200", "http://localhost:8080" }, methods = { RequestMethod.GET,
            RequestMethod.POST })
    @Tag(name = "Heritage", description = "API pour le calcul des parts d'héritage")
    public List<Heritier> calculParts(String sexe_defunt, boolean conjoint_vivant, boolean pere_vivant,
            boolean mere_vivante, Integer nb_filles, Integer nb_garcons, Integer nb_soeurs, Integer nb_freres) {
        log.info("Calculs");
        return calculPartsService.calculParts(sexe_defunt, conjoint_vivant, pere_vivant, mere_vivante, nb_filles,
                nb_garcons, nb_soeurs, nb_freres);
        // return calculPartsService.calculParts( "M", true, false,false, 1, 1, 0, 0);
    }

    @PostMapping("/calculate")
    @Operation(summary = "Calculer les parts d'héritage", description = "Calcule les parts d'héritage selon la loi islamique à partir de la composition familiale")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Calcul effectué avec succès", content = @Content(schema = @Schema(implementation = HeritageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides ou composition familiale incorrecte"),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    public ResponseEntity<HeritageResponse> calculerHeritage(@Valid @RequestBody FamilyRequest request) {
        log.info(
                "Requête de calcul reçue: sexe={}, conjoint={}, père={}, mère={}, filles={}, garçons={}, soeurs={}, frères={}",
                request.getSexeDefunt(), request.getNbConjoints(), request.isPereVivant(),
                request.isMereVivante(), request.getNbFilles(), request.getNbGarcons(),
                request.getNbSoeurs(), request.getNbFreres());

        // Validation métier
        validator.validate(request);
        validator.validateIslamicRules(request);

        // Calcul des parts
        List<Heritier> heritiers = calculPartsService.calculParts(
                request.getSexeDefunt(),
                (request.getNbConjoints() != null && request.getNbConjoints() > 0),
                request.isPereVivant(),
                request.isMereVivante(),
                request.getNbFilles(),
                request.getNbGarcons(),
                request.getNbSoeurs(),
                request.getNbFreres());

        // Construction de la réponse enrichie
        HeritageResponse response = HeritageResponse.fromCalculation(
                request,
                heritiers,
                "Calcul des parts d'héritage effectué avec succès");

        log.info("Calcul terminé: {} héritier(s), dénominateur commun: {}, calcul complet: {}",
                response.getNombreHeritiers(), response.getDenominateurCommun(), response.getCalculComplet());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/calculs")
    @Operation(summary = "[Déprécié] Calculer les parts d'héritage", description = "Endpoint déprécié. Utilisez /calculate à la place.", deprecated = true)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Calcul réussi"),
            @ApiResponse(responseCode = "400", description = "Requête invalide"),
            @ApiResponse(responseCode = "500", description = "Erreur serveur")
    })
    public ResponseEntity<HeritageResponse> calculerHeritageDeprecated(@Valid @RequestBody FamilyRequest request) {
        log.warn("Utilisation de l'endpoint déprécié /calculs. Utilisez /calculate à la place.");
        return calculerHeritage(request);
    }

    @GetMapping("/status")
    @Operation(summary = "Vérifier le statut de l'API")
    public ResponseEntity<String> status() {
        return ResponseEntity.ok("API Frida Calculs - v1.0.0 - Opérationnelle ✓");
    }
}
