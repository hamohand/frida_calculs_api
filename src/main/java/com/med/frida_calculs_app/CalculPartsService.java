package com.med.frida_calculs_app;

import com.med.frida_calculs_app.model.Fraction;
import com.med.frida_calculs_app.model.Heritier;
import com.med.frida_calculs_app.model.Heritiers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.med.frida_calculs_app.model.Heritiers.*;

@Service
@Slf4j
//@AllArgsConstructor @Data
public class CalculPartsService {
    // Très important : permet de garder la fraction de l'héritage restant à partager
    //par les autres éventuels héritiers, au début la partRestant est entière (fraction = 1)
    private final Fraction HERITAGE = new Fraction(1); // constante

    public List<Heritier> calculParts(String sexe_defunt, boolean conjoint_vivant, boolean pere_vivant, boolean mere_vivante, Integer nb_filles, Integer nb_garcons, Integer nb_soeurs, Integer nb_freres) {
// Affectation des valeurs par défaut si les paramètres ne sont pas présents (null)
        nb_filles = (nb_filles != null) ? nb_filles : 0;
        nb_garcons = (nb_garcons != null) ? nb_garcons : 0;
        nb_soeurs = (nb_soeurs != null) ? nb_soeurs : 0;
        nb_freres = (nb_freres != null) ? nb_freres : 0;

        log.info("Paramètres traités : "
                + "conjoint_vivant = " + conjoint_vivant + ", "
                + "pere_vivant = " + pere_vivant + ", "
                + "mere_vivante = " + mere_vivante + ", "
                + "nb_filles = " + nb_filles + ", "
                + "nb_garcons = " + nb_garcons + ", "
                + "nb_soeurs = " + nb_soeurs + ", "
                + "nb_freres = " + nb_freres
        );

        // Liste des héritiers
        List<Heritier> heritiersList = new ArrayList<>(); // Liste (heritier + part)
        List<Fraction> fractionsList = new ArrayList<>();// // pour calculer le dénominateur commun
        //Héritiers
        Heritiers heritiers = new Heritiers();

        //Initialisation : partRestant
        // fraction de l'héritage non encore distribué (=HERITAGE au départ)
        Fraction fractionRestant = HERITAGE;
        log.info("Héritage part de départ : " + fractionRestant + "");

        // ---------------------- Conjoint ----------------------
        //conjoint
        if (conjoint_vivant){
            heritiers.setConjoint(conjoint(sexe_defunt, nb_filles, nb_garcons)); // part conjoint
            fractionRestant = fractionRestant.soustraire(heritiers.getConjoint().getPart()); // Fraction restant après le conjoint
            heritiersList.add(heritiers.getConjoint()); //ajouter à la liste des héritiers
            fractionsList.add(heritiers.getConjoint().getPart()); // pour le calcul du même dénominateur
            log.info("conjoint : " + heritiers.getConjoint().getPart() + " ");
            log.info("restant après conjoint : " + fractionRestant + " ");
        }
        // ---------------------- Parents ----------------------
        //Père
        if(pere_vivant){
            heritiers.setPere(pere(conjoint_vivant,nb_filles,nb_garcons));
            fractionRestant = fractionRestant.soustraire(heritiers.getPere().getPart());
            heritiersList.add(heritiers.getPere());
            fractionsList.add(heritiers.getPere().getPart());
            log.info("pere : " + heritiers.getPere().getPart() + " ");
            log.info("restant après père : " + fractionRestant + " ");
        }
        //Mère
        if (mere_vivante){
            heritiers.setMere(mere(conjoint_vivant,nb_filles,nb_garcons,nb_soeurs,nb_freres));
            fractionRestant = fractionRestant.soustraire(heritiers.getMere().getPart());
            heritiersList.add(heritiers.getMere());
            fractionsList.add(heritiers.getMere().getPart());
            log.info("mere : " + heritiers.getMere().getPart() + " ");
            log.info("restant après mère : " + fractionRestant + "");
        }
              // ---------------------- Enfants ----------------------
        // Fille
        if (nb_filles!=0){
            heritiers.setFille(fille(fractionRestant, nb_filles, nb_garcons));
            fractionRestant = fractionRestant.soustraire((heritiers.getFille().getPart()).multiplier(nb_filles));
            heritiersList.add(heritiers.getFille());
            fractionsList.add(heritiers.getFille().getPart());
            log.info("fille : " + heritiers.getFille().getPart() + " ");
            log.info("restant après fille : " + fractionRestant + " ");
        }
        //Garçons
        if (nb_garcons!=0){
            heritiers.setGarcon(garcon(fractionRestant, nb_filles,nb_garcons));
            fractionRestant = fractionRestant.soustraire((heritiers.getGarcon().getPart()).multiplier(nb_garcons));
            heritiersList.add(heritiers.getGarcon());
            fractionsList.add(heritiers.getGarcon().getPart());
            log.info("garcon : " + heritiers.getGarcon().getPart() + " ");
            log.info("restant après garçon : " + fractionRestant + " ");
        }
        // ---------------------- Fraterie ----------------------
        // Soeur
        if (nb_soeurs!=0 && fractionRestant.getNumerateur()!=0){
            heritiers.setSoeur(soeur(fractionRestant, nb_soeurs, nb_freres));
            fractionRestant = fractionRestant.soustraire((heritiers.getSoeur().getPart()).multiplier(nb_soeurs));
            heritiersList.add(heritiers.getSoeur());
            fractionsList.add(heritiers.getSoeur().getPart());
            log.info("soeur : " + heritiers.getSoeur().getPart() + " ");
            log.info("part restant après soeur : " + fractionRestant + " ");
        }
        //Frere
        if (nb_freres!=0 && fractionRestant.getNumerateur()!=0){
            heritiers.setFrere(frere(fractionRestant, nb_freres));
            fractionRestant = fractionRestant.soustraire((heritiers.getFrere().getPart()).multiplier(nb_freres));
            heritiersList.add(heritiers.getFrere());
            fractionsList.add(heritiers.getFrere().getPart());
            log.info("frere : " + heritiers.getFrere().getPart() + " ");
            log.info("part restant après frère : " + fractionRestant + " ");
        }
        // ---------------------- Part restant non distribuée ----------------------
        // Part restant
        heritiers.getPartRestant().setPart(fractionRestant);
        heritiersList.add(heritiers.getPartRestant());
        fractionsList.add(heritiers.getPartRestant().getPart());
        log.info("Part restant non distribué à la fin : " + heritiers.getPartRestant().getPart() + " ");

        //--------------------------------------------------------------
        //Réduction au même dénominateur
        List<Fraction> fractionListMemeDenominateur = Fraction.reduireAuMemDenominateur(fractionsList);
        log.info("fractionListMemeDenominateur : " + fractionListMemeDenominateur);
        //parts au même dénominateur
        for (int i = 0; i < heritiersList.size(); i++) {
            heritiersList.get(i).setPart(fractionListMemeDenominateur.get(i));
        }
        return heritiersList;
    }


    //-------------------------------------------------
/*
    // les attributs
    private final int nbFilles;
    private final int nbGarcons;
    private final int nbConjoints;

    // la loi dit : constantes
    private final int NUMERATEUR_CONJOINT = 1;
    private final int NUMERATEUR_ENFANTS = 7;
    private final int DENOMINATEUR = 8;
    private final int COEF_F = 1;
    private final int COEF_M = 2;

    //les variables : initialisations
    private int numerateurEnfants = NUMERATEUR_ENFANTS; //
    private int numerateurConjoint = NUMERATEUR_CONJOINT; // pas de conjoint
    private int numerateurFille = 0; // pas de filles
    private int numerateurGarcon = 0; // pas de garçons
    private int denominateur = DENOMINATEUR; //
    private int coefFilles = COEF_F;
    private int coefGarcons = COEF_M;

    //constructeur
    public CalculPartsService2(int nbFilles, int nbGarcons, int nbConjoints) {
        this.nbFilles = nbFilles;
        this.nbGarcons = nbGarcons;
        this.nbConjoints = nbConjoints;
    }

    public Map<String, Integer> getFractions() {
        return fractions;
    }

    //fractions
    private Map<String, Integer> fractions = new HashMap<>();

    public void calculPartsEnfants() {
        fractions.put("nbGarcons", nbGarcons);
        fractions.put("nbFilles", nbFilles);
        fractions.put("nbConjoints", nbConjoints);

        //Nombre total de parts des enfant : fille= 1 part, garçon= 2 parts
        int nbTotalPartsEnfants = (nbGarcons *2) + nbFilles;
        if(nbFilles == 0) { // pas de filles
            nbTotalPartsEnfants = nbGarcons;
            coefGarcons = 1;
        }

        // Existence du conjoint ?
        if(nbConjoints == 0) { // pas de conjoint
            numerateurConjoint = 0;
            numerateurEnfants = 1;
            denominateur = 1;
        } else {
            if (Math.floorMod(nbTotalPartsEnfants, numerateurEnfants) == 0) { // si multiple de 7
                nbTotalPartsEnfants = Math.floorDiv(nbTotalPartsEnfants, numerateurEnfants);
                numerateurEnfants = 1;
            }
            if (nbTotalPartsEnfants != 0){ //conjoint + enfants
                numerateurConjoint = nbTotalPartsEnfants;
            } else { // conjoint mais pas d'enfants
                numerateurConjoint = 1;
            }

        }

        // Fraction  ****
        //numérateur
        if(nbFilles != 0) {
            numerateurFille = numerateurEnfants * coefFilles;
        }
        if(nbGarcons != 0) {
            numerateurGarcon = numerateurEnfants * coefGarcons;
        }

        //dénominateur différent de zéro
        if (nbTotalPartsEnfants == 0 && nbConjoints==0 ){ // pas d'enfants
            denominateur = -1;
        } else if (nbTotalPartsEnfants != 0){
            denominateur = nbTotalPartsEnfants * denominateur;
        }

        //
        fractions.put("numerateurFille", numerateurFille);
        fractions.put("numerateurGarcon", numerateurGarcon);
        fractions.put("numerateurConjoint", numerateurConjoint);
        fractions.put("denominateur", denominateur);

        log.info("nbConjoints" + nbConjoints);

        log.info("nbFilles : " + nbFilles);
        log.info("nbGarcons : " + nbGarcons);
        log.info("nbTotalPartsEnfants : " + nbTotalPartsEnfants);

        log.info("fraction fille: " + fractions.get("numerateurFille")+"/"+fractions.get("denominateur"));
        log.info("fraction garcon: " + fractions.get("numerateurGarcon")+"/"+fractions.get("denominateur"));
        log.info("fraction conjoint: " + fractions.get("numerateurConjoint")+"/"+fractions.get("denominateur"));

        // Parts : facteur multiplicatif = nombre réel pour les futurs calculs des parts d'un bien (somme d'argent par exemple)
        float partFille = (float) numerateurFille / denominateur;
        float partGarcon = (float) numerateurGarcon / denominateur;
        float conjoint = (float) numerateurConjoint / denominateur;
    }
*/

}
