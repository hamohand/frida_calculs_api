package com.med.frida_calculs_app.model;

import com.med.frida_calculs_app.enums.Sexe;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requête pour calculer les parts d'héritage selon la loi islamique")
public class FamilyRequest {

    @NotNull(message = "Le sexe du défunt est obligatoire")
    @Schema(description = "Sexe du défunt", example = "M", allowableValues = { "M",
            "F" }, requiredMode = Schema.RequiredMode.REQUIRED)
    private String sexeDefunt;

    @Min(value = 0, message = "Le nombre de conjoints ne peut pas être négatif")
    @Max(value = 1, message = "Le nombre de conjoints ne peut pas dépasser 1 (pour le moment)")
    @Schema(description = "Nombre de conjoints vivants (0 ou 1)", example = "1", defaultValue = "0", minimum = "0", maximum = "1")
    private Integer nbConjoints;

    @Schema(description = "Le père est-il vivant?", example = "false", defaultValue = "false")
    private boolean pereVivant;

    @Schema(description = "La mère est-elle vivante?", example = "false", defaultValue = "false")
    private boolean mereVivante;

    @Min(value = 0, message = "Le nombre de filles ne peut pas être négatif")
    @Max(value = 50, message = "Le nombre de filles ne peut pas dépasser 50")
    @Schema(description = "Nombre de filles", example = "1", defaultValue = "0", minimum = "0", maximum = "50")
    private Integer nbFilles;

    @Min(value = 0, message = "Le nombre de garçons ne peut pas être négatif")
    @Max(value = 50, message = "Le nombre de garçons ne peut pas dépasser 50")
    @Schema(description = "Nombre de garçons", example = "1", defaultValue = "0", minimum = "0", maximum = "50")
    private Integer nbGarcons;

    @Min(value = 0, message = "Le nombre de soeurs ne peut pas être négatif")
    @Max(value = 50, message = "Le nombre de soeurs ne peut pas dépasser 50")
    @Schema(description = "Nombre de soeurs", example = "0", defaultValue = "0", minimum = "0", maximum = "50")
    private Integer nbSoeurs;

    @Min(value = 0, message = "Le nombre de frères ne peut pas être négatif")
    @Max(value = 50, message = "Le nombre de frères ne peut pas dépasser 50")
    @Schema(description = "Nombre de frères", example = "0", defaultValue = "0", minimum = "0", maximum = "50")
    private Integer nbFreres;

    /**
     * Validation métier : au moins un héritier doit être présent
     */
    public boolean hasAtLeastOneHeir() {
        return (nbConjoints != null && nbConjoints > 0) || pereVivant || mereVivante ||
                (nbFilles != null && nbFilles > 0) ||
                (nbGarcons != null && nbGarcons > 0) ||
                (nbSoeurs != null && nbSoeurs > 0) ||
                (nbFreres != null && nbFreres > 0);
    }

    /**
     * Convertit le sexe String en enum Sexe
     */
    public Sexe getSexeDefuntEnum() {
        return Sexe.fromCode(sexeDefunt);
    }
}
