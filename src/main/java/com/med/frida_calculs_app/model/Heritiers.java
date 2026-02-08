package com.med.frida_calculs_app.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Slf4j
public class Heritiers {
    /**
     * Ensemble de tous les héritiers de la frida.
     * Toutes les parts sont réduites au même dénominateur.
     * Avec l'éventuelle part restant non encore distribuée.
     * A constant representing the fraction 0/1. This is a commonly used value
     * for mathematical operations and serves as a standard representation
     * of zero in fractional form. The fraction is automatically reduced
     * during instantiation to ensure it is in its simplest form.
     */
    //Constantes
    private static final Fraction F0 = new Fraction(0);
    private static final Fraction F1 = new Fraction(1);
    private static final Fraction F12 = new Fraction(1,2);
    private static final Fraction F13 = new Fraction(1,3);
    private static final Fraction F14 = new Fraction(1,4);
    private static final Fraction F16 = new Fraction(1,6);
    private static final Fraction F18 = new Fraction(1,8);
    private static final Fraction F23 = new Fraction(2,3);

    // Très important : permet de garder la fraction de l'héritage restant à partager
    //par les autres éventuels héritiers, au début la partRestant est entière (fraction = 1)
    private static final Fraction HERITAGE = new Fraction(1); // constante
    //-------------
    //Variables --- à calculer
    //Conjoint
    private Heritier conjoint = new Heritier();
    //Parents
    private  Heritier pere = new Heritier("pere", F0);
    private  Heritier mere = new Heritier("mere", F0);
    //Enfants
    private  Heritier fille = new Heritier("fille", F0);
    private  Heritier garcon = new Heritier("garçon", F0);
    //Fratrie
    private  Heritier frere = new Heritier("frere", F0);
    private  Heritier soeur = new Heritier("soeur", F0);
    // fraction de l'héritage non encore distribué (=HERITAGE au départ)
    private Heritier partRestant = new Heritier("part restant",HERITAGE);

    /*private Heritiers(Heritier conjoint, Heritier pere, Heritier mere, Heritier fille, Heritier garcon, Heritier frere, Heritier soeur, Heritier partRestant) {
        this.conjoint = conjoint;
        this.pere = pere;
        this.mere = mere;
        this.fille = fille;
        this.garcon = garcon;
        this.frere = frere;
        this.soeur = soeur;
        this.partRestant = partRestant;
    }*/

    // Méthodes --- calcul
    // ----------------------------- V12 conjoint---------------------------
    //Le conjoint
    public static Heritier conjoint(String sexe_defunt, Integer nb_filles, Integer nb_garcons){
        int nb_enfants = nb_filles+nb_garcons;
        Fraction part;
        Heritier heritier = new Heritier("conjoint");
        if(nb_enfants == 0){
            if(Objects.equals(sexe_defunt, "M") || Objects.equals(sexe_defunt, "Masculin") ){
                part = HERITAGE.multiplier(F14);
            }else {
                part = HERITAGE.multiplier(F12);
            }
        } else {
            if(Objects.equals(sexe_defunt, "M")){
                part = HERITAGE.multiplier(F18);
            } else {
                part = HERITAGE.multiplier(F14);
            }
        }
        heritier.setPart(part);
        return heritier;
    }
    // ----------------------------- V11 parents---------------------------
    //Père
    public static Heritier pere(boolean conjoint_vivant, int nb_filles, int nb_garcons){
        int nb_enfants = nb_filles + nb_garcons;
        Fraction part;
        Heritier heritier = new Heritier("pere");
        if ((nb_enfants != 0) || (conjoint_vivant)) { // il y a des enfants ou conjoint vivant
            part = HERITAGE.multiplier(F16);
        }
        else { //pas d'enfants ni de conjoint
            part = HERITAGE.multiplier(F23);
        }
        heritier.setPart(part);
        return heritier;
    }
    //Mère
    public static Heritier mere(boolean conjoint_vivant, int nb_filles, int nb_garcons, int nb_soeurs, int nb_freres){
        int nb_enfants = nb_filles + nb_garcons;
        int nb_freres_soeurs = nb_freres + nb_soeurs;
        Fraction part;
        Heritier heritier = new Heritier("mere");
        if ((nb_enfants != 0) || (conjoint_vivant)) { // il y a des enfants ou conjoint vivant
            part = HERITAGE.multiplier(F16);
            heritier.setPart(HERITAGE.multiplier(F16));
        }
        else { //pas d'enfants ni de conjoint
            if (nb_freres_soeurs == 0){ // ni de frères ni soeurs
                part = HERITAGE.multiplier(F13);
            } else { //avec frères et soeurs
                part = HERITAGE.multiplier(F16); // 1/6 pour la fraterie
            }
        }
        heritier.setPart(part);
        return heritier;
        // la suite dans les versets suivants
    }

    // ----------------------------- V11 enfants---------------------------
    // Fille
    public static Heritier fille(Fraction fractionRestant, Integer nb_filles, Integer nb_garcons){
        int nb_parts_enfants; // nombres de demi-parts
        Fraction part;
        Heritier heritier = new Heritier("fille");
        if (nb_garcons == 0) { // pas de garçon V11
            if (nb_filles == 1) { // fille unique
                part = fractionRestant.multiplier(F12);
            }
            else { // plusieurs filles
                part = fractionRestant.multiplier(F23).diviser(nb_filles);
            }
        }
        else { // avec des garçons
            nb_parts_enfants = (nb_garcons * 2) + nb_filles;
            part = fractionRestant.diviser(nb_parts_enfants);
        }
        heritier.setPart(part);
        return heritier;
    }
    // Garçon
    public static Heritier garcon(Fraction fractionRestant, Integer nb_filles, Integer nb_garcons){
        Fraction part;
        Heritier heritier = new Heritier("garcon");
        part = fractionRestant.diviser(nb_garcons);
        heritier.setPart(part);
        return heritier;
    }
    // ----------------------------- V11 enfants---------------------------
    // Soeur
    public static Heritier soeur(Fraction fractionRestant, Integer nb_soeurs, Integer nb_freres){
        int nb_parts_fraterie; // nombre de demi-parts
        Fraction part;
        Heritier heritier = new Heritier("soeur");
        if (nb_freres == 0) { // pas de frères
            if (nb_soeurs == 1) { // soeur unique
                part = fractionRestant.multiplier(F12);
            }
            else { // plusieurs soeurs
                part = fractionRestant.multiplier(F23).diviser(nb_soeurs);
            }
        } else { // avec des garçons
            nb_parts_fraterie = (nb_freres * 2) + nb_soeurs;
            part = fractionRestant.diviser(nb_parts_fraterie);
        }
        heritier.setPart(part);
        return heritier;
    }
    // Frere
    public static Heritier frere(Fraction fractionRestant, Integer nb_freres){
        Fraction part;
        Heritier heritier = new Heritier("frere");
        part = fractionRestant.diviser(nb_freres);
        heritier.setPart(part);
        return heritier;
    }

}
