package com.med.frida_calculs_app.model;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
public class Heritier {
    /**
     * heritier : représente un type d'héritier membre de la famille du défunt :
     * conjoint, père, mère, fille, garcon, soeur, frère
     * et une éventuelle part de l'héritage restant non distribuée par
     * l'application.
     * part : fraction représentant l'éventuelle part de l'héritage sinon 0.
     */
    private String heritier;
    @Schema(description = "Base de la fraction (ex: de la totalité, du reste)")
    private String baseCalcul;
    private Fraction part;

    public Heritier(String heritier, Fraction part) {
        this.heritier = heritier;
        this.part = part;
    }

    public Heritier(com.med.frida_calculs_app.enums.HeirType type, Fraction part) {
        this.heritier = type.getLabel();
        this.baseCalcul = type.getBaseCalcul();
        this.part = part;
    }

    public Heritier(String heritier) {
        this.heritier = heritier;
    }

    public Heritier(com.med.frida_calculs_app.enums.HeirType type) {
        this.heritier = type.getLabel();
        this.baseCalcul = type.getBaseCalcul();
    }

    public Heritier() {
    }

    @Schema(description = "Fraction irréductible représentant la part légale du type d'héritier")
    public Fraction getPartIrreductible() {
        if (this.part != null && this.part.getDenominateur() != 0) {
            // Le constructeur de Fraction applique automatiquement la réduction (simplification)
            return new Fraction(this.part.getNumerateur(), this.part.getDenominateur());
        }
        return null;
    }
}
