package com.med.frida_calculs_app.enums;

import lombok.Getter;

@Getter
public enum HeirType {
    SPOUSE("conjoint"),
    FATHER("pere"),
    MOTHER("mere"),
    DAUGHTER("fille"),
    SON("garçon"),
    SISTER("soeur"),
    BROTHER("frere"),
    REMAINDER("part restant");

    private final String label;

    HeirType(String label) {
        this.label = label;
    }
}
