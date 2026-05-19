package com.med.frida_calculs_app.enums;

public enum TypeHeritier {
    CONJOINT("conjoint"),
    PERE("pere"),
    MERE("mere"),
    FILLE("fille"),
    GARCON("garçon"),
    SOEUR("soeur"),
    FRERE("frere"),
    PART_RESTANT("part restant");

    private final String libelle;

    TypeHeritier(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }

    public static TypeHeritier fromLibelle(String libelle) {
        if (libelle == null) {
            return null;
        }

        String normalized = libelle.trim().toLowerCase();

        for (TypeHeritier type : TypeHeritier.values()) {
            if (type.libelle.equals(normalized)) {
                return type;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return libelle;
    }
}
