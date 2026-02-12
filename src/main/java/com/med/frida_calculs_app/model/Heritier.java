package com.med.frida_calculs_app.model;

import lombok.Data;

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
    private Fraction part;

    public Heritier(String heritier, Fraction part) {
        this.heritier = heritier;
        this.part = part;
    }

    public Heritier(com.med.frida_calculs_app.enums.HeirType type, Fraction part) {
        this.heritier = type.getLabel();
        this.part = part;
    }

    public Heritier(String heritier) {
        this.heritier = heritier;
    }

    public Heritier(com.med.frida_calculs_app.enums.HeirType type) {
        this.heritier = type.getLabel();
    }

    public Heritier() {
    }
}
