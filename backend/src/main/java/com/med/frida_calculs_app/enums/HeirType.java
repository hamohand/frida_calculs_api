package com.med.frida_calculs_app.enums;

import lombok.Getter;

@Getter
public enum HeirType {
    SPOUSE("conjoint", "de la totalité de l'héritage"),
    FATHER("pere", "de la totalité de l'héritage"),
    MOTHER("mere", "de la totalité de l'héritage"),
    DAUGHTER("fille", "du reste de l'héritage"),
    SON("garçon", "du reste de l'héritage"),
    SISTER("soeur", "du reste de l'héritage"),
    BROTHER("frere", "du reste de l'héritage"),
    PATERNAL_GRANDFATHER("grand-père paternel", "de la totalité de l'héritage"),
    PATERNAL_UNCLE("oncle paternel", "du reste de l'héritage"),
    PATERNAL_COUSIN("cousin paternel", "du reste de l'héritage"),
    REMAINDER("part restant", "de la totalité de l'héritage (non distribuée)");

    private final String label;
    private final String baseCalcul;

    HeirType(String label, String baseCalcul) {
        this.label = label;
        this.baseCalcul = baseCalcul;
    }
    
    public String getBaseCalcul() {
        return baseCalcul;
    }
}
