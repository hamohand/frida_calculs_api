package com.med.frida_calculs_app;

import com.med.frida_calculs_app.enums.HeirType;
import com.med.frida_calculs_app.model.Fraction;
import com.med.frida_calculs_app.model.Heritier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class CalculPartsService {

    public List<Heritier> calculParts(com.med.frida_calculs_app.model.FamilyRequest request) {
        String sexe_defunt = request.getSexeDefunt();
        boolean conjoint_vivant = request.getNbConjoints() != null && request.getNbConjoints() > 0;
        int nb_conjoints = request.getNbConjoints() != null ? request.getNbConjoints() : 0;
        boolean pere_vivant = request.isPereVivant();
        boolean grand_pere_vivant = request.isGrandPerePaternelVivant();
        boolean mere_vivante = request.isMereVivante();
        int nb_filles = request.getNbFilles() != null ? request.getNbFilles() : 0;
        int nb_garcons = request.getNbGarcons() != null ? request.getNbGarcons() : 0;
        int nb_soeurs = request.getNbSoeurs() != null ? request.getNbSoeurs() : 0;
        int nb_freres = request.getNbFreres() != null ? request.getNbFreres() : 0;
        int nb_oncles = request.getNbOncles() != null ? request.getNbOncles() : 0;
        int nb_cousins = request.getNbCousins() != null ? request.getNbCousins() : 0;

        log.info("Nouveau calcul fiable : conjoint={}, pere={}, mere={}, filles={}, garcons={}, soeurs={}, freres={}, oncles={}, cousins={}",
                conjoint_vivant, pere_vivant, mere_vivante, nb_filles, nb_garcons, nb_soeurs, nb_freres, nb_oncles, nb_cousins);

        // --- PHASE 1 : Règles d'exclusion (Hajb) ---
        // Le père exclut le grand-père
        if (pere_vivant) {
            grand_pere_vivant = false;
        }

        // Le père ou un fils ou le grand-père excluent totalement les frères et sœurs
        int active_freres = nb_freres;
        int active_soeurs = nb_soeurs;
        if (pere_vivant || grand_pere_vivant || nb_garcons > 0) {
            active_freres = 0;
            active_soeurs = 0;
        }

        // Les oncles paternels sont exclus par le père, grand-père, un garçon ou un frère
        int active_oncles = nb_oncles;
        if (pere_vivant || grand_pere_vivant || nb_garcons > 0 || nb_freres > 0) {
            active_oncles = 0;
        }

        // Les cousins paternels sont exclus par le père, grand-père, un garçon, un frère ou un oncle
        int active_cousins = nb_cousins;
        if (pere_vivant || grand_pere_vivant || nb_garcons > 0 || nb_freres > 0 || nb_oncles > 0) {
            active_cousins = 0;
        }

        // --- PHASE 2 : Parts théoriques fixes (Fard) sur la totalité de l'héritage ---
        Fraction f_conjoint = new Fraction(0);
        Fraction f_mere = new Fraction(0);
        Fraction f_pere = new Fraction(0);
        Fraction f_grand_pere = new Fraction(0);
        Fraction f_filles = new Fraction(0);
        Fraction f_soeurs = new Fraction(0);

        // 1. Conjoint
        if (conjoint_vivant) {
            boolean has_descendants = (nb_filles > 0 || nb_garcons > 0);
            if (has_descendants) {
                f_conjoint = (sexe_defunt.equalsIgnoreCase("M") || sexe_defunt.equalsIgnoreCase("Masculin"))
                        ? new Fraction(1, 8)
                        : new Fraction(1, 4);
            } else {
                f_conjoint = (sexe_defunt.equalsIgnoreCase("M") || sexe_defunt.equalsIgnoreCase("Masculin"))
                        ? new Fraction(1, 4)
                        : new Fraction(1, 2);
            }
        }

        // 2. Mère
        if (mere_vivante) {
            boolean has_descendants = (nb_filles > 0 || nb_garcons > 0);
            boolean has_multiple_siblings = (active_freres + active_soeurs) >= 2;
            if (has_descendants || has_multiple_siblings) {
                f_mere = new Fraction(1, 6);
            } else {
                // Cas spécial Gharrawayn (Conjoint + Père + Mère sans descendants ni fratrie active)
                if (conjoint_vivant && pere_vivant && (active_freres + active_soeurs) == 0) {
                    if (f_conjoint.getNumerateur() == 1 && f_conjoint.getDenominateur() == 2) { // Époux
                        f_mere = new Fraction(1, 6); // 1/3 du reste (1/2) = 1/6
                    } else if (f_conjoint.getNumerateur() == 1 && f_conjoint.getDenominateur() == 4) { // Épouse
                        f_mere = new Fraction(1, 4); // 1/3 du reste (3/4) = 1/4
                    } else {
                        f_mere = new Fraction(1, 3);
                    }
                } else {
                    f_mere = new Fraction(1, 3);
                }
            }
        }

        // 3. Père
        if (pere_vivant) {
            boolean has_descendants = (nb_filles > 0 || nb_garcons > 0);
            if (has_descendants) {
                f_pere = new Fraction(1, 6); // Part fixe minimale
            } else {
                f_pere = new Fraction(0); // Purement Asaba, prend le reste
            }
        } else if (grand_pere_vivant) {
            // Grand-père remplace le père
            boolean has_descendants = (nb_filles > 0 || nb_garcons > 0);
            if (has_descendants) {
                f_grand_pere = new Fraction(1, 6); // Part fixe minimale
            } else {
                f_grand_pere = new Fraction(0); // Purement Asaba, prend le reste
            }
        }

        // 4. Filles (seulement s'il n'y a pas de garçon, sinon elles sont Asaba)
        if (nb_filles > 0 && nb_garcons == 0) {
            if (nb_filles == 1) {
                f_filles = new Fraction(1, 2);
            } else {
                f_filles = new Fraction(2, 3);
            }
        }

        // 5. Sœurs (seulement s'il n'y a pas de frère ni de descendant ni de père)
        if (active_soeurs > 0 && active_freres == 0 && nb_filles == 0 && nb_garcons == 0) {
            if (active_soeurs == 1) {
                f_soeurs = new Fraction(1, 2);
            } else {
                f_soeurs = new Fraction(2, 3);
            }
        }

        // --- PHASE 3 : Résolution mathématique (Somme des parts fixes) ---
        Fraction sommeFixe = f_conjoint.ajouter(f_mere).ajouter(f_pere).ajouter(f_grand_pere).ajouter(f_filles).ajouter(f_soeurs);

        // Variables pour stocker les parts finales calculées
        Fraction final_conjoint = f_conjoint;
        Fraction final_mere = f_mere;
        Fraction final_pere = f_pere;
        Fraction final_grand_pere = f_grand_pere;
        Fraction final_fille = (nb_filles > 0 && nb_garcons == 0) ? f_filles.diviser(nb_filles) : null;
        Fraction final_garcon = null;
        Fraction final_soeur = (active_soeurs > 0 && active_freres == 0 && nb_filles == 0 && nb_garcons == 0)
                ? f_soeurs.diviser(active_soeurs) : null;
        Fraction final_frere = null;
        Fraction final_oncle = null;
        Fraction final_cousin = null;
        Fraction final_part_restant = new Fraction(0);

        // On met toutes les parts fixes initiales sous le même dénominateur pour tester le dépassement (Aoul)
        List<Fraction> listFixed = new ArrayList<>();
        listFixed.add(f_conjoint);
        listFixed.add(f_mere);
        listFixed.add(f_pere);
        listFixed.add(f_grand_pere);
        listFixed.add(f_filles);
        listFixed.add(f_soeurs);

        List<Fraction> listCommon = Fraction.reduireAuMemDenominateur(listFixed);
        int commonDen = listCommon.get(0).getDenominateur();
        int sumNum = 0;
        for (Fraction f : listCommon) {
            sumNum += f.getNumerateur();
        }

        if (sumNum > commonDen) {
            // --- CAS A : AOUL (Somme des parts > 1) ---
            // On augmente le dénominateur de tous les héritiers fixes
            final_conjoint = new Fraction(listCommon.get(0).getNumerateur(), sumNum);
            final_mere = new Fraction(listCommon.get(1).getNumerateur(), sumNum);
            final_pere = new Fraction(listCommon.get(2).getNumerateur(), sumNum);
            final_grand_pere = new Fraction(listCommon.get(3).getNumerateur(), sumNum);
            if (nb_filles > 0) {
                final_fille = new Fraction(listCommon.get(4).getNumerateur(), sumNum).diviser(nb_filles);
            }
            if (active_soeurs > 0) {
                final_soeur = new Fraction(listCommon.get(5).getNumerateur(), sumNum).diviser(active_soeurs);
            }
            // Les héritiers Asaba n'ont rien
            if (nb_garcons > 0) {
                final_garcon = new Fraction(0);
                if (nb_filles > 0) final_fille = new Fraction(0);
            }
            if (active_freres > 0) {
                final_frere = new Fraction(0);
                if (active_soeurs > 0) final_soeur = new Fraction(0);
            }
            if (active_oncles > 0) {
                final_oncle = new Fraction(0);
            }
            if (active_cousins > 0) {
                final_cousin = new Fraction(0);
            }
        } else {
            // --- CAS B : Reste <= 1 ---
            Fraction residue = new Fraction(commonDen - sumNum, commonDen);

            // Est-ce qu'il y a des héritiers Asaba en jeu ?
            boolean has_asaba = false;
            
            // Priorité 1: Enfants (Garçon et Fille)
            if (nb_garcons > 0) {
                has_asaba = true;
                int parts = (nb_garcons * 2) + nb_filles;
                final_garcon = residue.multiplier(2).diviser(parts);
                final_fille = residue.multiplier(1).diviser(parts);
            }
            // Priorité 2: Père
            else if (pere_vivant) {
                has_asaba = true;
                final_pere = final_pere.ajouter(residue);
            }
            // Priorité 2 bis: Grand-père
            else if (grand_pere_vivant) {
                has_asaba = true;
                final_grand_pere = final_grand_pere.ajouter(residue);
            }
            // Priorité 3: Fratrie
            else if (active_freres > 0) {
                has_asaba = true;
                int parts = (active_freres * 2) + active_soeurs;
                final_frere = residue.multiplier(2).diviser(parts);
                final_soeur = residue.multiplier(1).diviser(parts);
            }
            // Priorité 4: Sœurs avec filles (العصبة مع الغير)
            else if (nb_filles > 0 && active_soeurs > 0) {
                has_asaba = true;
                final_soeur = residue.diviser(active_soeurs);
            }
            // Priorité 5: Oncles Paternels
            else if (active_oncles > 0) {
                has_asaba = true;
                final_oncle = residue.diviser(active_oncles);
            }
            // Priorité 6: Cousins Paternels
            else if (active_cousins > 0) {
                has_asaba = true;
                final_cousin = residue.diviser(active_cousins);
            }

            if (!has_asaba && residue.getNumerateur() > 0) {
                // --- CAS C : RADD (Surplus sans Asaba) ---
                // Redistribuer aux héritiers fixes autres que le conjoint
                Fraction sommeEligible = f_mere.ajouter(f_filles).ajouter(f_soeurs);
                if (sommeEligible.getNumerateur() > 0) {
                    Fraction multiplierFactor = new Fraction(1).soustraire(f_conjoint).diviser(sommeEligible);
                    if (mere_vivante) {
                        final_mere = f_mere.multiplier(multiplierFactor);
                    }
                    if (nb_filles > 0) {
                        final_fille = f_filles.multiplier(multiplierFactor).diviser(nb_filles);
                    }
                    if (active_soeurs > 0) {
                        final_soeur = f_soeurs.multiplier(multiplierFactor).diviser(active_soeurs);
                    }
                } else {
                    // Seul le conjoint est présent (le reliquat reste non distribué)
                    final_part_restant = residue;
                }
            }
        }

        // --- Détermination des cadres légaux en arabe ---
        String cadre_conjoint = "";
        String cadre_mere = "";
        String cadre_pere = "";
        String cadre_grand_pere = "";
        String cadre_fille = "";
        String cadre_garcon = "";
        String cadre_soeur = "";
        String cadre_frere = "";
        String cadre_oncle = "";
        String cadre_cousin = "";

        if (sumNum > commonDen) {
            // Cas de dépassement (Aoul)
            cadre_conjoint = "العول (Aoul - Réduction)";
            cadre_mere = "العول (Aoul - Réduction)";
            cadre_pere = "العول (Aoul - Réduction)";
            cadre_grand_pere = "العول (Aoul - Réduction)";
            cadre_fille = "العول (Aoul - Réduction)";
            cadre_soeur = "العول (Aoul - Réduction)";
            cadre_garcon = "العصبة (Asaba - Résiduaire)";
            cadre_frere = "العصبة (Asaba - Résiduaire)";
            cadre_oncle = "العصبة (Asaba - Résiduaire)";
            cadre_cousin = "العصبة (Asaba - Résiduaire)";
        } else {
            Fraction residue = new Fraction(commonDen - sumNum, commonDen);
            boolean has_asaba = false;

            if (nb_garcons > 0) {
                has_asaba = true;
                cadre_garcon = "العصبة (Asaba - Résiduaire)";
                cadre_fille = "العصبة (Asaba - Résiduaire)";
            } else if (pere_vivant) {
                has_asaba = true;
                if (nb_filles > 0) {
                    cadre_pere = "الفرض والعصبة (Fard et Asaba)";
                } else {
                    cadre_pere = "العصبة (Asaba - Résiduaire)";
                }
            } else if (grand_pere_vivant) {
                has_asaba = true;
                if (nb_filles > 0) {
                    cadre_grand_pere = "الفرض والعصبة (Fard et Asaba)";
                } else {
                    cadre_grand_pere = "العصبة (Asaba - Résiduaire)";
                }
            } else if (active_freres > 0) {
                has_asaba = true;
                cadre_frere = "العصبة (Asaba - Résiduaire)";
                cadre_soeur = "العصبة (Asaba - Résiduaire)";
            } else if (nb_filles > 0 && active_soeurs > 0) {
                has_asaba = true;
                cadre_soeur = "العصبة (Asaba - Résiduaire)";
            } else if (active_oncles > 0) {
                has_asaba = true;
                cadre_oncle = "العصبة (Asaba - Résiduaire)";
            } else if (active_cousins > 0) {
                has_asaba = true;
                cadre_cousin = "العصبة (Asaba - Résiduaire)";
            }

            boolean is_radd = !has_asaba && residue.getNumerateur() > 0;

            if (conjoint_vivant) {
                cadre_conjoint = "الفرض (Fard - Part fixe)";
            }
            if (mere_vivante) {
                cadre_mere = is_radd ? "الفرض والرد (Fard et Radd)" : "الفرض (Fard - Part fixe)";
            }
            if (pere_vivant && cadre_pere.isEmpty()) {
                cadre_pere = "الفرض (Fard - Part fixe)";
            }
            if (grand_pere_vivant && cadre_grand_pere.isEmpty()) {
                cadre_grand_pere = "الفرض (Fard - Part fixe)";
            }
            if (nb_filles > 0 && cadre_fille.isEmpty()) {
                cadre_fille = is_radd ? "الفرض والرد (Fard et Radd)" : "الفرض (Fard - Part fixe)";
            }
            if (nb_soeurs > 0 && cadre_soeur.isEmpty()) {
                if (active_soeurs == 0) {
                    cadre_soeur = "محجوب (Exclu)";
                } else {
                    cadre_soeur = is_radd ? "الفرض والرد (Fard et Radd)" : "الفرض (Fard - Part fixe)";
                }
            }
            if (nb_freres > 0 && cadre_frere.isEmpty()) {
                if (active_freres == 0) {
                    cadre_frere = "محجوب (Exclu)";
                } else {
                    cadre_frere = "العصبة (Asaba - Résiduaire)";
                }
            }
            if (nb_oncles > 0 && cadre_oncle.isEmpty()) {
                if (active_oncles == 0) {
                    cadre_oncle = "محجوب (Exclu)";
                } else {
                    cadre_oncle = "العصبة (Asaba - Résiduaire)";
                }
            }
            if (nb_cousins > 0 && cadre_cousin.isEmpty()) {
                if (active_cousins == 0) {
                    cadre_cousin = "محجوب (Exclu)";
                } else {
                    cadre_cousin = "العصبة (Asaba - Résiduaire)";
                }
            }
        }

        // --- PHASE 4 : Construction de la liste finale des héritiers ---
        List<Heritier> heritiersList = new ArrayList<>();
        List<Fraction> fractionsList = new ArrayList<>();

        if (conjoint_vivant) {
            Heritier h = new Heritier(HeirType.SPOUSE, final_conjoint);
            h.setCadreLegal(cadre_conjoint);
            heritiersList.add(h);
            fractionsList.add(h.getPart());
        }
        if (pere_vivant) {
            Heritier h = new Heritier(HeirType.FATHER, final_pere != null ? final_pere : new Fraction(0));
            h.setCadreLegal(cadre_pere);
            heritiersList.add(h);
            fractionsList.add(h.getPart());
        }
        if (grand_pere_vivant) {
            Heritier h = new Heritier(HeirType.PATERNAL_GRANDFATHER, final_grand_pere != null ? final_grand_pere : new Fraction(0));
            h.setCadreLegal(cadre_grand_pere);
            heritiersList.add(h);
            fractionsList.add(h.getPart());
        }
        if (mere_vivante) {
            Heritier h = new Heritier(HeirType.MOTHER, final_mere != null ? final_mere : new Fraction(0));
            h.setCadreLegal(cadre_mere);
            heritiersList.add(h);
            fractionsList.add(h.getPart());
        }
        if (nb_filles > 0) {
            Heritier h = new Heritier(HeirType.DAUGHTER, final_fille != null ? final_fille : new Fraction(0));
            h.setCadreLegal(cadre_fille);
            heritiersList.add(h);
            fractionsList.add(h.getPart());
        }
        if (nb_garcons > 0) {
            Heritier h = new Heritier(HeirType.SON, final_garcon != null ? final_garcon : new Fraction(0));
            h.setCadreLegal(cadre_garcon);
            heritiersList.add(h);
            fractionsList.add(h.getPart());
        }
        if (nb_soeurs > 0) {
            Heritier h = new Heritier(HeirType.SISTER, final_soeur != null ? final_soeur : new Fraction(0));
            h.setCadreLegal(cadre_soeur);
            heritiersList.add(h);
            fractionsList.add(h.getPart());
        }
        if (nb_freres > 0) {
            Heritier h = new Heritier(HeirType.BROTHER, final_frere != null ? final_frere : new Fraction(0));
            h.setCadreLegal(cadre_frere);
            heritiersList.add(h);
            fractionsList.add(h.getPart());
        }
        if (nb_oncles > 0) {
            Heritier h = new Heritier(HeirType.PATERNAL_UNCLE, final_oncle != null ? final_oncle : new Fraction(0));
            h.setCadreLegal(cadre_oncle);
            heritiersList.add(h);
            fractionsList.add(h.getPart());
        }
        if (nb_cousins > 0) {
            Heritier h = new Heritier(HeirType.PATERNAL_COUSIN, final_cousin != null ? final_cousin : new Fraction(0));
            h.setCadreLegal(cadre_cousin);
            heritiersList.add(h);
            fractionsList.add(h.getPart());
        }

        // Part restante
        Heritier h_rest = new Heritier(HeirType.REMAINDER, final_part_restant);
        heritiersList.add(h_rest);
        fractionsList.add(h_rest.getPart());

        // Enregistrer la part légale d'origine (Fraction irréductible)
        for (Heritier h : heritiersList) {
            if (h.getPart() != null) {
                h.setPartLegale(new Fraction(h.getPart().getNumerateur(), h.getPart().getDenominateur()));
            }
        }

        // Réduction globale au même dénominateur
        List<Fraction> fractionListMemeDenominateur = Fraction.reduireAuMemDenominateur(fractionsList);
        for (int i = 0; i < heritiersList.size(); i++) {
            heritiersList.get(i).setPart(fractionListMemeDenominateur.get(i));
        }

        return heritiersList;
    }
}
